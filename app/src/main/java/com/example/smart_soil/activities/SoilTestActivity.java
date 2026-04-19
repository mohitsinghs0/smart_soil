package com.example.smart_soil.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.smart_soil.R;
import com.example.smart_soil.models.Farm;
import com.example.smart_soil.models.SoilTest;
import com.example.smart_soil.models.SoilTestRequest;
import com.example.smart_soil.services.RetrofitClient;
import com.example.smart_soil.utils.ImageStandardizer;
import com.example.smart_soil.utils.NavigationHelper;
import com.example.smart_soil.utils.SoilImageValidator;
import com.example.smart_soil.utils.SoilPredictionUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class SoilTestActivity extends BaseActivity {

    private Spinner farmSpinner;
    private MaterialButton predictButton, saveResultButton, viewHistoryButton;
    private LinearLayout resultsContainer;
    private View emptyPredictionContainer;
    private LinearLayout parametersContainer;
    private ChipGroup cropsChipGroup;
    private FrameLayout uploadBox;
    private ImageView soilImagePreview;
    private View selectionOptionsContainer;
    private Uri selectedImageUri;

    private LinearLayout cameraBox;
    private Uri cameraImageUri;
    private File standardizedImageFile;
    private static final int CAMERA_PERMISSION_CODE = 101;

    private final List<Farm> farmList = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;
    private final List<String> farmNames = new ArrayList<>();
    private SoilTest currentPrediction;
    private int selectedFarmServerId = -1;
    private int preSelectedFarmId = -1;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    processAndShowImage(uri);
                }
            });

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraImageUri != null) {
                    processAndShowImage(cameraImageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soil_test);

        preSelectedFarmId = getIntent().getIntExtra("farm_id", -1);

        initViews();
        setupFarmSpinner();
        loadFarmsFromApi();

        uploadBox.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        cameraBox.setOnClickListener(v -> openCamera());

        predictButton.setOnClickListener(v -> {
            if (selectedFarmServerId <= 0) {
                Toast.makeText(this, "Please select a valid farm", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedImageUri == null) {
                Toast.makeText(this, "Please upload a soil image first", Toast.LENGTH_SHORT).show();
                return;
            }
            performPrediction();
        });

        viewHistoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, HistoryActivity.class);
            if (selectedFarmServerId > 0) {
                intent.putExtra("farm_id", selectedFarmServerId);
            }
            startActivity(intent);
        });

        saveResultButton.setOnClickListener(v -> savePredictionToBackend());

        NavigationHelper.setupCustomNav(this, R.id.fab_nav_center);
    }

    private void initViews() {
        farmSpinner = findViewById(R.id.farm_spinner);
        predictButton = findViewById(R.id.predict_button);
        saveResultButton = findViewById(R.id.save_result_button);
        viewHistoryButton = findViewById(R.id.view_history_button);
        resultsContainer = findViewById(R.id.results_container);
        emptyPredictionContainer = findViewById(R.id.empty_prediction_container);
        parametersContainer = findViewById(R.id.parameters_container);
        cropsChipGroup = findViewById(R.id.crops_chip_group);
        uploadBox = findViewById(R.id.upload_box);
        soilImagePreview = findViewById(R.id.soil_image_preview);
        selectionOptionsContainer = findViewById(R.id.selection_options_container);
        cameraBox = findViewById(R.id.camera_box);
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            return;
        }
        launchCamera();
    }

    private void launchCamera() {
        try {
            File tempFile = new File(getCacheDir(), "cam_capture_" + System.currentTimeMillis() + ".jpg");
            cameraImageUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", tempFile);
            cameraLauncher.launch(cameraImageUri);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void processAndShowImage(Uri uri) {
        runOnUiThread(() -> {
            predictButton.setEnabled(false);
            predictButton.setText("Validating image...");
        });

        new Thread(() -> {
            try {
                standardizedImageFile = ImageStandardizer.standardizeFromUri(uri, this);
                Bitmap standardizedBitmap = BitmapFactory.decodeFile(
                        standardizedImageFile.getAbsolutePath());

                SoilImageValidator.ValidationResult result =
                        SoilImageValidator.validate(standardizedBitmap);

                standardizedBitmap.recycle();

                if (result.isValid) {
                    runOnUiThread(() -> {
                        soilImagePreview.setImageURI(Uri.fromFile(standardizedImageFile));
                        soilImagePreview.setVisibility(View.VISIBLE);
                        selectionOptionsContainer.setVisibility(View.GONE);
                        selectedImageUri = Uri.fromFile(standardizedImageFile);
                        predictButton.setEnabled(true);
                        predictButton.setText("  Predict Soil Health");
                        predictButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.brand_green));
                    });
                } else {
                    runOnUiThread(() -> {
                        standardizedImageFile = null;
                        selectedImageUri = null;
                        soilImagePreview.setVisibility(View.GONE);
                        selectionOptionsContainer.setVisibility(View.VISIBLE);
                        predictButton.setEnabled(true);
                        predictButton.setText("  Predict Soil Health");
                        new AlertDialog.Builder(this)
                                .setTitle("❌ Invalid Image")
                                .setMessage(result.errorMessage)
                                .setPositiveButton("Try Again", (dialog, which) -> dialog.dismiss())
                                .show();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    predictButton.setEnabled(true);
                    predictButton.setText("  Predict Soil Health");
                    Toast.makeText(this, "Image processing failed", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void setupFarmSpinner() {
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, farmNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        farmSpinner.setAdapter(spinnerAdapter);

        farmSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!farmList.isEmpty() && position < farmList.size()) {
                    selectedFarmServerId = farmList.get(position).id;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadFarmsFromApi() {
        RetrofitClient.getApiService(this).getFarms(getAuthToken()).enqueue(new Callback<List<Farm>>() {
            @Override
            public void onResponse(Call<List<Farm>> call, Response<List<Farm>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    farmList.clear();
                    farmNames.clear();
                    farmList.addAll(response.body());
                    int selectionIndex = 0;
                    for (int i = 0; i < farmList.size(); i++) {
                        Farm farm = farmList.get(i);
                        farmNames.add(farm.name + " (" + farm.village + ")");
                        if (preSelectedFarmId != -1 && farm.id == preSelectedFarmId) {
                            selectionIndex = i;
                        }
                    }
                    if (farmNames.isEmpty()) {
                        farmNames.add("No farms found");
                        selectedFarmServerId = -1;
                    } else {
                        spinnerAdapter.notifyDataSetChanged();
                        farmSpinner.setSelection(selectionIndex);
                        selectedFarmServerId = farmList.get(selectionIndex).id;
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Farm>> call, Throwable t) {
                Timber.e(t, "Error loading farms");
                Toast.makeText(SoilTestActivity.this, "Failed to load farms", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performPrediction() {
        currentPrediction = SoilPredictionUtil.predictSoilHealth();
        showResults(currentPrediction);
    }

    private void showResults(SoilTest test) {
        emptyPredictionContainer.setVisibility(View.GONE);
        resultsContainer.setVisibility(View.VISIBLE);
        parametersContainer.removeAllViews();
        cropsChipGroup.removeAllViews();

        addParameterResult("SOC", test.soc + "%", "soc", test.soc, 100);
        addParameterResult("Nitrogen", test.nitrogen + " kg/ha", "nitrogen", test.nitrogen, 400);
        addParameterResult("Phosphorus", test.phosphorus + " kg/ha", "phosphorus", test.phosphorus, 60);
        addParameterResult("Potassium", test.potassium + " kg/ha", "potassium", test.potassium, 450);
        addParameterResult("pH Level", String.valueOf(test.ph), "ph", test.ph, 14);

        if (test.recommended_crops != null) {
            for (String crop : test.recommended_crops.split(",")) {
                addCropChip(crop.trim());
            }
        }
    }

    private void addParameterResult(String name, String valueDisplay, String paramKey, double value, double max) {
        View view = LayoutInflater.from(this).inflate(R.layout.list_item_parameter, parametersContainer, false);
        TextView nameTv = view.findViewById(R.id.parameter_name);
        TextView statusTv = view.findViewById(R.id.parameter_status);
        TextView valueTv = view.findViewById(R.id.parameter_value);
        ProgressBar progressBar = view.findViewById(R.id.parameter_progress);

        String status = SoilPredictionUtil.getStatus(paramKey, value);
        nameTv.setText(name);
        statusTv.setText(status);
        valueTv.setText(valueDisplay);
        progressBar.setProgress((int) ((value / max) * 100));
        parametersContainer.addView(view);
    }

    private void addCropChip(String name) {
        Chip chip = new Chip(this);
        chip.setText(name);
        cropsChipGroup.addView(chip);
    }

    private void savePredictionToBackend() {
        if (currentPrediction == null) return;

        saveResultButton.setEnabled(false);
        saveResultButton.setText("Saving...");

        Long farmId = selectedFarmServerId > 0 ? (long) selectedFarmServerId : null;
        SoilTestRequest request = new SoilTestRequest(
                farmId,
                prefsManager.getUserId(),
                currentPrediction.soc,
                currentPrediction.nitrogen,
                currentPrediction.phosphorus,
                currentPrediction.potassium,
                currentPrediction.ph,
                currentPrediction.recommended_crops,
                currentPrediction.overallScore
        );

        RetrofitClient.getApiService(this).createSoilTest(getAuthToken(), request).enqueue(new Callback<List<SoilTest>>() {
            @Override
            public void onResponse(Call<List<SoilTest>> call, Response<List<SoilTest>> response) {
                saveResultButton.setEnabled(true);
                saveResultButton.setText("Save Result");
                if (response.isSuccessful()) {
                    Toast.makeText(SoilTestActivity.this, "Result saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String error = "Save failed: " + response.code();
                    try (ResponseBody rb = response.errorBody()) {
                        if (rb != null) error = rb.string();
                    } catch (IOException ignored) {}
                    Toast.makeText(SoilTestActivity.this, error, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<SoilTest>> call, Throwable t) {
                saveResultButton.setEnabled(true);
                saveResultButton.setText("Save Result");
                Toast.makeText(SoilTestActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
