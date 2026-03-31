package com.example.smart_soil.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
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
import com.example.smart_soil.services.RetrofitClient;
import com.example.smart_soil.utils.ImageStandardizer;
import com.example.smart_soil.utils.NavigationHelper;
import com.example.smart_soil.utils.SoilImageValidator;
import com.example.smart_soil.utils.SoilPredictionUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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
    private int selectedFarmId = -1;

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

        initViews();

        // Setup farm spinner
        setupFarmSpinner();
        loadFarmsFromApi();

        // Gallery picker — also standardize gallery images
        uploadBox.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // Camera
        cameraBox.setOnClickListener(v -> openCamera());

        // Predict button click
        predictButton.setOnClickListener(v -> {
            if (selectedFarmId == -1) {
                Toast.makeText(this, "Please select a farm", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedImageUri == null) {
                Toast.makeText(this, "Please upload a soil image first", Toast.LENGTH_SHORT).show();
                return;
            }
            performPrediction();
        });

        // History button click
        viewHistoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, HistoryActivity.class);
            if (selectedFarmId != -1) {
                intent.putExtra("farm_id", selectedFarmId);
            }
            startActivity(intent);
        });

        // Save result
        saveResultButton.setOnClickListener(v -> savePredictionToBackend());

        // Setup Custom Nav
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
        // Show loading state
        runOnUiThread(() -> {
            predictButton.setEnabled(false);
            predictButton.setText("Validating image...");
        });

        new Thread(() -> {
            try {
                // Step 1: Standardize image (resize + compress)
                standardizedImageFile = ImageStandardizer.standardizeFromUri(uri, this);
                Bitmap standardizedBitmap = BitmapFactory.decodeFile(
                        standardizedImageFile.getAbsolutePath());

                // Step 2: Validate — is this actually a soil image?
                SoilImageValidator.ValidationResult result =
                        SoilImageValidator.validate(standardizedBitmap);

                standardizedBitmap.recycle();

                if (result.isValid) {
                    // ✅ Valid soil image — show preview, enable predict
                    runOnUiThread(() -> {
                        soilImagePreview.setImageURI(Uri.fromFile(standardizedImageFile));
                        soilImagePreview.setVisibility(View.VISIBLE);
                        selectionOptionsContainer.setVisibility(View.GONE);
                        selectedImageUri = Uri.fromFile(standardizedImageFile);
                        predictButton.setEnabled(true);
                        predictButton.setText("  Predict Soil Health");
                        predictButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.brand_green));
                        Toast.makeText(this,
                                "✓ Soil image validated (1024×768 @ 85%)",
                                Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // ❌ Invalid image — reset UI + show error dialog
                    runOnUiThread(() -> {
                        // Reset image selection
                        standardizedImageFile = null;
                        selectedImageUri      = null;
                        soilImagePreview.setVisibility(View.GONE);
                        selectionOptionsContainer.setVisibility(View.VISIBLE);
                        predictButton.setEnabled(true);
                        predictButton.setText("  Predict Soil Health");

                        // Show error dialog
                        new AlertDialog.Builder(this)
                                .setTitle("❌ Invalid Image")
                                .setMessage(result.errorMessage)
                                .setPositiveButton("Try Again", (dialog, which) -> dialog.dismiss())
                                .setNegativeButton("Cancel", null)
                                .setCancelable(true)
                                .show();
                    });
                }

            } catch (Exception e) {
                runOnUiThread(() -> {
                    predictButton.setEnabled(true);
                    predictButton.setText("  Predict Soil Health");
                    Toast.makeText(this, "Image processing failed. Please try again.",
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            Toast.makeText(this, "Camera permission needed for best results", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetUI() {
        standardizedImageFile = null;
        selectedImageUri = null;
        currentPrediction = null;
        soilImagePreview.setVisibility(View.GONE);
        selectionOptionsContainer.setVisibility(View.VISIBLE);
        resultsContainer.setVisibility(View.GONE);
        emptyPredictionContainer.setVisibility(View.VISIBLE);
        parametersContainer.removeAllViews();
        cropsChipGroup.removeAllViews();
    }

    private void setupFarmSpinner() {
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, farmNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        farmSpinner.setAdapter(spinnerAdapter);

        farmSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!farmList.isEmpty() && position < farmList.size()) {
                    selectedFarmId = farmList.get(position).id;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadFarmsFromApi() {
        RetrofitClient.getApiService().getFarms(getAuthToken()).enqueue(new Callback<List<Farm>>() {
            @Override
            public void onResponse(Call<List<Farm>> call, Response<List<Farm>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    farmList.clear();
                    farmNames.clear();
                    farmList.addAll(response.body());

                    for (Farm farm : farmList) {
                        farmNames.add(farm.name + " (" + farm.village + ")");
                    }

                    if (farmNames.isEmpty()) {
                        farmNames.add("No farms found. Create one first!");
                        selectedFarmId = -1;
                    } else {
                        spinnerAdapter.notifyDataSetChanged();
                        // Pre-select farm from intent if any
                        int intentFarmId = getIntent().getIntExtra("farm_id", -1);
                        if (intentFarmId != -1) {
                            for (int i = 0; i < farmList.size(); i++) {
                                if (farmList.get(i).id == intentFarmId) {
                                    farmSpinner.setSelection(i);
                                    selectedFarmId = intentFarmId;
                                    break;
                                }
                            }
                        } else {
                            selectedFarmId = farmList.get(0).id;
                        }
                    }
                    spinnerAdapter.notifyDataSetChanged();
                } else {
                    Timber.e("Failed to load farms: %s", response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Farm>> call, Throwable t) {
                Timber.e(t, "Error loading farms");
                Toast.makeText(SoilTestActivity.this, "Network error while loading farms", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performPrediction() {
        // Use the utility to generate a realistic prediction
        currentPrediction = SoilPredictionUtil.predictSoilHealth();
        currentPrediction.farm_id = selectedFarmId;
        showResults(currentPrediction);
    }

    private void showResults(SoilTest test) {
        emptyPredictionContainer.setVisibility(View.GONE);
        resultsContainer.setVisibility(View.VISIBLE);

        parametersContainer.removeAllViews();
        cropsChipGroup.removeAllViews();

        addParameterResult("SOC (Soil Organic Carbon)", test.soc + "%", "soc", test.soc, 100);
        addParameterResult("Nitrogen (N)", test.nitrogen + " kg/ha", "nitrogen", test.nitrogen, 400);
        addParameterResult("Phosphorus (P)", test.phosphorus + " kg/ha", "phosphorus", test.phosphorus, 60);
        addParameterResult("Potassium (K)", test.potassium + " kg/ha", "potassium", test.potassium, 450);
        addParameterResult("pH Level", String.valueOf(test.ph), "ph", test.ph, 14);

        if (test.recommended_crops != null) {
            String[] crops = test.recommended_crops.split(",");
            for (String crop : crops) {
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
        int colorRes = R.color.brand_green;
        int bgRes = R.drawable.bg_status_high;

        if (status.equals("Low")) {
            colorRes = R.color.status_low;
            bgRes = R.drawable.bg_status_low;
        } else if (status.equals("Medium")) {
            colorRes = R.color.status_medium;
            bgRes = R.drawable.bg_status_medium;
        }

        nameTv.setText(name);
        statusTv.setText(status);
        statusTv.setBackgroundResource(bgRes);
        statusTv.setTextColor(ContextCompat.getColor(this, colorRes));
        valueTv.setText(valueDisplay);

        int progress = (int) ((value / max) * 100);
        progressBar.setProgress(progress);

        LayerDrawable pd = (LayerDrawable) progressBar.getProgressDrawable();
        pd.findDrawableByLayerId(android.R.id.progress).setColorFilter(ContextCompat.getColor(this, colorRes), PorterDuff.Mode.SRC_IN);

        parametersContainer.addView(view);
    }

    private void addCropChip(String name) {
        Chip chip = new Chip(this);
        chip.setText(name);
        chip.setChipIcon(ContextCompat.getDrawable(this, R.drawable.ic_sprout));
        chip.setChipIconVisible(true);
        chip.setCheckable(false);
        chip.setClickable(false);

        chip.setChipBackgroundColorResource(R.color.bg_secondary);
        if (name.contains("(Best)")) {
            chip.setTextColor(ContextCompat.getColor(this, R.color.brand_green));
            chip.setChipIconTintResource(R.color.brand_green);
        } else {
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            chip.setChipIconTintResource(R.color.text_secondary);
            chip.setAlpha(0.7f);
        }

        cropsChipGroup.addView(chip);
    }

    private void savePredictionToBackend() {
        if (currentPrediction == null) return;

        saveResultButton.setEnabled(false);
        saveResultButton.setText("Saving...");

        MultipartBody.Part body = null;
        if (selectedImageUri != null) {
            try {
                File file = getFileFromUri(selectedImageUri);
                if (file != null) {
                    RequestBody requestFile = RequestBody.create(file, MediaType.parse("image/*"));
                    body = MultipartBody.Part.createFormData("image", "soil_" + System.currentTimeMillis() + ".jpg", requestFile);
                }
            } catch (IOException e) {
                Timber.e(e, "Error preparing image file");
            }
        }

        RetrofitClient.getApiService().createSoilTest(
                getAuthToken(),
                body,
                currentPrediction.farm_id,
                currentPrediction.soc,
                currentPrediction.nitrogen,
                currentPrediction.phosphorus,
                currentPrediction.potassium,
                currentPrediction.ph,
                currentPrediction.recommended_crops,
                currentPrediction.overallScore
        ).enqueue(new Callback<SoilTest>() {
            @Override
            public void onResponse(Call<SoilTest> call, Response<SoilTest> response) {
                saveResultButton.setEnabled(true);
                saveResultButton.setText("Save Result");

                if (response.isSuccessful()) {
                    Toast.makeText(SoilTestActivity.this, "Result saved successfully!", Toast.LENGTH_SHORT).show();
                    saveResultButton.setVisibility(View.GONE);

                    // Navigate to history
                    Intent intent = new Intent(SoilTestActivity.this, HistoryActivity.class);
                    intent.putExtra("farm_id", selectedFarmId);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = "Failed to save";
                    try (ResponseBody errorBody = response.errorBody()) {
                        if (errorBody != null) {
                            errorMsg = errorBody.string();
                        }
                    } catch (IOException e) {
                        Timber.e(e, "Error reading error body");
                    }
                    Toast.makeText(SoilTestActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    Timber.e("Save failed: %s", errorMsg);
                }
            }

            @Override
            public void onFailure(Call<SoilTest> call, Throwable t) {
                saveResultButton.setEnabled(true);
                saveResultButton.setText("Save Result");
                Timber.e(t, "Save network failure");
                Toast.makeText(SoilTestActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File getFileFromUri(Uri uri) throws IOException {
        if ("file".equals(uri.getScheme())) {
            String path = uri.getPath();
            if (path == null) throw new IOException("URI path is null");
            return new File(path);
        }
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream == null) throw new IOException("Cannot open input stream from URI");
            File file = new File(getCacheDir(), "temp_image.jpg");
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            return file;
        }
    }
}
