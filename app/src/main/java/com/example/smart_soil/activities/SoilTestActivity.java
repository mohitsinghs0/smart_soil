package com.example.smart_soil.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.example.smart_soil.ml.SoilMLAnalyzer;
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
import java.util.Locale;

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
    private FrameLayout uploadBox;
    private ImageView soilImagePreview;
    private View selectionOptionsContainer;
    private ChipGroup cropsChipGroup;

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

    private SoilMLAnalyzer analyzer;
    private boolean isPredictionLocked = false;

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

        try {
            analyzer = new SoilMLAnalyzer(this);
        } catch (Throwable t) {
            Timber.e(t, "Fatal: Failed to initialize ML analyzer");
        }

        initViews();
        setupFarmSpinner();
        loadFarmsFromApi();

        uploadBox.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        cameraBox.setOnClickListener(v -> openCamera());

        predictButton.setOnClickListener(v -> {
            if (isPredictionLocked) return;

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
        uploadBox = findViewById(R.id.upload_box);
        soilImagePreview = findViewById(R.id.soil_image_preview);
        selectionOptionsContainer = findViewById(R.id.selection_options_container);
        cameraBox = findViewById(R.id.camera_box);
        cropsChipGroup = findViewById(R.id.crops_chip_group);
    }

    private void unlockPrediction() {
        isPredictionLocked = false;
        predictButton.setEnabled(true);
        predictButton.setAlpha(1.0f);
        predictButton.setText("Predict Soil Health");
    }

    private void lockPrediction() {
        isPredictionLocked = true;
        predictButton.setEnabled(false);
        predictButton.setAlpha(0.6f);
        predictButton.setText("Prediction Completed");
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
                if (standardizedImageFile == null || !standardizedImageFile.exists()) {
                    runOnUiThread(this::unlockPrediction);
                    return;
                }

                Bitmap standardizedBitmap = BitmapFactory.decodeFile(standardizedImageFile.getAbsolutePath());
                if (standardizedBitmap == null) {
                    runOnUiThread(this::unlockPrediction);
                    return;
                }
                
                SoilImageValidator.ValidationResult result = SoilImageValidator.validate(standardizedBitmap);
                standardizedBitmap.recycle();

                if (result.isValid) {
                    runOnUiThread(() -> {
                        soilImagePreview.setImageURI(Uri.fromFile(standardizedImageFile));
                        soilImagePreview.setVisibility(View.VISIBLE);
                        selectionOptionsContainer.setVisibility(View.GONE);
                        selectedImageUri = Uri.fromFile(standardizedImageFile);
                        unlockPrediction();
                    });
                } else {
                    runOnUiThread(() -> {
                        selectedImageUri = null;
                        unlockPrediction();
                        new AlertDialog.Builder(this)
                                .setTitle("Invalid Image")
                                .setMessage(result.errorMessage)
                                .setPositiveButton("OK", null)
                                .show();
                    });
                }
            } catch (Exception e) {
                Timber.e(e);
                runOnUiThread(this::unlockPrediction);
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
                    Timber.d("DEBUG_SPINNER: Selected Position: %d, Farm ID: %d", position, selectedFarmServerId);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadFarmsFromApi() {
        String userId = prefsManager.getUserId();
        if (userId == null) return;

        Timber.d("DEBUG_API: Loading farms for user: %s", userId);
        // Filter by current user's ID
        RetrofitClient.getApiService(this).getFarms(getAuthToken(), "eq." + userId).enqueue(new Callback<List<Farm>>() {
            @Override
            public void onResponse(Call<List<Farm>> call, Response<List<Farm>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Farm> farms = response.body();
                    Timber.d("DEBUG_API: Farms received: %d", farms.size());
                    
                    farmList.clear();
                    farmNames.clear();
                    farmList.addAll(farms);

                    int selectionIndex = 0;
                    for (int i = 0; i < farmList.size(); i++) {
                        Farm farm = farmList.get(i);
                        farmNames.add(farm.name + " (" + (farm.village != null ? farm.village : "N/A") + ")");
                        if (preSelectedFarmId != -1 && farm.id == preSelectedFarmId) {
                            selectionIndex = i;
                        }
                    }

                    if (farmNames.isEmpty()) {
                        farmNames.add("No farms found");
                        selectedFarmServerId = -1;
                    } else {
                        selectedFarmServerId = farmList.get(selectionIndex).id;
                    }

                    spinnerAdapter.notifyDataSetChanged();
                    farmSpinner.setSelection(selectionIndex);
                    
                    if (!farmList.isEmpty()) {
                        unlockPrediction();
                    }
                } else {
                    Timber.e("DEBUG_API: Error %d: %s", response.code(), response.message());
                    Toast.makeText(SoilTestActivity.this, "Failed to load farms", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Farm>> call, Throwable t) {
                Timber.e(t, "DEBUG_API: Network failure");
                Toast.makeText(SoilTestActivity.this, "Network error loading farms", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performPrediction() {
        if (analyzer == null || selectedImageUri == null) return;

        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImageUri));
            SoilMLAnalyzer.SoilResult result = analyzer.analyzeSoil(bitmap);
            bitmap.recycle();

            if (result != null) {
                currentPrediction = new SoilTest();
                currentPrediction.soc = result.soc;
                currentPrediction.nitrogen = result.nitrogen;
                currentPrediction.ph = result.ph;
                currentPrediction.phosphorus = 0.0;
                currentPrediction.potassium = 0.0;
                
                List<String> recommendations = SoilPredictionUtil.getRecommendedCrops(result.nitrogen, result.ph, result.soc);
                currentPrediction.recommended_crops = TextUtils.join(", ", recommendations);
                currentPrediction.overallScore = SoilPredictionUtil.calculateOverallScore(result.nitrogen, result.ph, result.soc);

                showResults(currentPrediction);
                lockPrediction();
            } else {
                Toast.makeText(this, "Prediction failed. Check Logs.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Timber.e(e, "Prediction Error");
            Toast.makeText(this, "Prediction error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showResults(SoilTest test) {
        emptyPredictionContainer.setVisibility(View.GONE);
        resultsContainer.setVisibility(View.VISIBLE);
        parametersContainer.removeAllViews();

        // Show the captured image in the results section
        View imageCard = findViewById(R.id.result_image_card);
        ImageView resultIv = findViewById(R.id.result_soil_image);
        if (imageCard != null && resultIv != null && selectedImageUri != null) {
            imageCard.setVisibility(View.VISIBLE);
            resultIv.setImageURI(selectedImageUri);
            // Hide the initial preview to avoid redundancy
            soilImagePreview.setVisibility(View.GONE);
        }

        addParameterResult("SOC (Soil Organic Carbon)", String.format(Locale.US, "%.2f%%", test.soc), "soc", test.soc, 2.0);
        addParameterResult("pH Level", String.format(Locale.US, "%.2f", test.ph), "ph", test.ph, 14);
        addParameterResult("Nitrogen", String.format(Locale.US, "%.2f kg/ha", test.nitrogen), "nitrogen", test.nitrogen, 500);

        showRecommendedCrops(test.recommended_crops);
        updateSocDiagram(test.soc);
    }

    private void updateSocDiagram(double soc) {
        View low = findViewById(R.id.container_soc_low);
        View medium = findViewById(R.id.container_soc_medium);
        View sufficient = findViewById(R.id.container_soc_sufficient);

        if (low == null || medium == null || sufficient == null) return;

        // Reset alphas to dimmed state
        low.setAlpha(0.2f);
        medium.setAlpha(0.2f);
        sufficient.setAlpha(0.2f);

        // Highlight based on value using prediction utility
        String status = SoilPredictionUtil.getStatus("soc", soc);
        if ("Low".equalsIgnoreCase(status)) {
            low.setAlpha(1.0f);
        } else if ("Medium".equalsIgnoreCase(status)) {
            medium.setAlpha(1.0f);
        } else if ("Sufficient".equalsIgnoreCase(status)) {
            sufficient.setAlpha(1.0f);
        }
    }

    private void showRecommendedCrops(String cropsCsv) {
        cropsChipGroup.removeAllViews();
        if (cropsCsv != null && !cropsCsv.isEmpty()) {
            String[] crops = cropsCsv.split(",\\s*");
            for (String crop : crops) {
                Chip chip = new Chip(this);
                chip.setText(crop);
                chip.setChipBackgroundColorResource(R.color.bg_primary);
                chip.setTextColor(ContextCompat.getColor(this, R.color.brand_green));
                chip.setChipStrokeColorResource(R.color.brand_green);
                chip.setChipStrokeWidth(2f);
                cropsChipGroup.addView(chip);
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
        
        // Ensure progress is within 0-100
        int progress = (int) ((value / max) * 100);
        progressBar.setProgress(Math.max(0, Math.min(100, progress)));

        if ("soc".equalsIgnoreCase(paramKey)) {
            int color = SoilPredictionUtil.getSOCColor(status);
            statusTv.setTextColor(color);
            progressBar.getProgressDrawable().setTint(color);
        }

        parametersContainer.addView(view);
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
                    Toast.makeText(SoilTestActivity.this, "Result saved!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(SoilTestActivity.this, "Save failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SoilTest>> call, Throwable t) {
                saveResultButton.setEnabled(true);
                saveResultButton.setText("Save Result");
                Toast.makeText(SoilTestActivity.this, "Network error while saving", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (analyzer != null) analyzer.close();
    }
}
