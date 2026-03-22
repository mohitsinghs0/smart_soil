package com.example.smart_soil.activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.smart_soil.R;
import com.example.smart_soil.models.Farm;
import com.example.smart_soil.services.RetrofitClient;
import com.example.smart_soil.utils.NavigationHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class SoilTestActivity extends BaseActivity {

    private static final int PICK_IMAGE = 1;
    private Spinner farmSpinner;
    private MaterialButton predictButton, saveResultButton, viewHistoryButton;
    private LinearLayout resultsContainer;
    private View emptyPredictionContainer;
    private LinearLayout parametersContainer;
    private ChipGroup cropsChipGroup;
    private FrameLayout uploadBox;
    private ImageView uploadImagePreview;
    private View uploadPlaceholder;
    private Uri selectedImageUri;
    
    private List<Farm> farmList = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;
    private List<String> farmNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soil_test);

        // Initialize views
        farmSpinner = findViewById(R.id.farm_spinner);
        predictButton = findViewById(R.id.predict_button);
        saveResultButton = findViewById(R.id.save_result_button);
        viewHistoryButton = findViewById(R.id.view_history_button);
        resultsContainer = findViewById(R.id.results_container);
        emptyPredictionContainer = findViewById(R.id.empty_prediction_container);
        parametersContainer = findViewById(R.id.parameters_container);
        cropsChipGroup = findViewById(R.id.crops_chip_group);
        uploadBox = findViewById(R.id.upload_box);
        uploadImagePreview = findViewById(R.id.upload_image_preview);
        uploadPlaceholder = findViewById(R.id.upload_placeholder);

        // Setup farm spinner
        setupFarmSpinner();
        loadFarmsFromApi();

        // Image upload click
        uploadBox.setOnClickListener(v -> openGallery());

        // Predict button click
        predictButton.setOnClickListener(v -> {
            if (selectedImageUri == null) {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
                return;
            }
            if (farmList.isEmpty()) {
                Toast.makeText(this, "Please create a farm first", Toast.LENGTH_SHORT).show();
                return;
            }
            showResults();
        });

        // History button click
        viewHistoryButton.setOnClickListener(v -> {
            startActivity(new Intent(this, HistoryActivity.class));
        });

        // Save result (Dummy)
        saveResultButton.setOnClickListener(v -> {
            Toast.makeText(this, "Result saved to history!", Toast.LENGTH_SHORT).show();
        });

        // Setup Custom Nav
        NavigationHelper.setupCustomNav(this, R.id.btn_nav_history);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            uploadImagePreview.setImageURI(selectedImageUri);
            uploadImagePreview.setVisibility(View.VISIBLE);
            uploadPlaceholder.setVisibility(View.GONE);
            
            predictButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.brand_green));
        }
    }

    private void setupFarmSpinner() {
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, farmNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        farmSpinner.setAdapter(spinnerAdapter);
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

    private void showResults() {
        emptyPredictionContainer.setVisibility(View.GONE);
        resultsContainer.setVisibility(View.VISIBLE);

        parametersContainer.removeAllViews();
        cropsChipGroup.removeAllViews();

        addParameterResult("SOC (Soil Organic Carbon)", "1.58%", "High", 80, R.color.status_high, R.drawable.bg_status_high);
        addParameterResult("Nitrogen (N)", "173.9kg/ha", "Medium", 50, R.color.status_medium, R.drawable.bg_status_medium);
        addParameterResult("Phosphorus (P)", "24.4kg/ha", "Medium", 40, R.color.status_medium, R.drawable.bg_status_medium);
        addParameterResult("Potassium (K)", "85.7kg/ha", "Low", 20, R.color.status_low, R.drawable.bg_status_low);
        addParameterResult("pH Level", "7", "Medium", 70, R.color.status_medium, R.drawable.bg_status_medium);

        addCropChip("Wheat", "(Best)", true);
        addCropChip("Soybean", "(Best)", true);
        addCropChip("Rice", "(Best)", true);
        addCropChip("Cotton", "(Alternative)", false);
        addCropChip("Jowar", "(Alternative)", false);
    }

    private void addParameterResult(String name, String value, String status, int progress, int colorRes, int bgRes) {
        View view = LayoutInflater.from(this).inflate(R.layout.list_item_parameter, parametersContainer, false);

        TextView nameTv = view.findViewById(R.id.parameter_name);
        TextView statusTv = view.findViewById(R.id.parameter_status);
        TextView valueTv = view.findViewById(R.id.parameter_value);
        ProgressBar progressBar = view.findViewById(R.id.parameter_progress);

        nameTv.setText(name);
        statusTv.setText(status);
        statusTv.setBackgroundResource(bgRes);
        statusTv.setTextColor(ContextCompat.getColor(this, colorRes));
        valueTv.setText(value);
        progressBar.setProgress(progress);

        LayerDrawable pd = (LayerDrawable) progressBar.getProgressDrawable();
        pd.findDrawableByLayerId(android.R.id.progress).setColorFilter(ContextCompat.getColor(this, colorRes), PorterDuff.Mode.SRC_IN);

        parametersContainer.addView(view);
    }

    private void addCropChip(String name, String type, boolean isBest) {
        Chip chip = new Chip(this);
        chip.setText(name + " " + type);
        chip.setChipIcon(ContextCompat.getDrawable(this, R.drawable.ic_sprout));
        chip.setChipIconVisible(true);
        chip.setCheckable(false);
        chip.setClickable(false);
        
        chip.setChipBackgroundColorResource(R.color.bg_secondary);
        if (isBest) {
            chip.setTextColor(ContextCompat.getColor(this, R.color.brand_green));
            chip.setChipIconTintResource(R.color.brand_green);
        } else {
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            chip.setChipIconTintResource(R.color.text_secondary);
            chip.setAlpha(0.7f);
        }

        cropsChipGroup.addView(chip);
    }
}
