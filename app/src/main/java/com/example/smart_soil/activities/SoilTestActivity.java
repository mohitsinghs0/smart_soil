package com.example.smart_soil.activities;

import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.smart_soil.R;
import com.example.smart_soil.models.Farm;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class SoilTestActivity extends BaseActivity {

    private Spinner farmSpinner;
    private MaterialButton predictButton;
    private LinearLayout uploadContainer, resultsContainer, emptyPredictionContainer;
    private LinearLayout parametersContainer;
    private ChipGroup cropsChipGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soil_test);

        // Initialize views
        farmSpinner = findViewById(R.id.farm_spinner);
        predictButton = findViewById(R.id.predict_button);
        uploadContainer = findViewById(R.id.upload_container);
        resultsContainer = findViewById(R.id.results_container);
        emptyPredictionContainer = findViewById(R.id.empty_prediction_container);
        parametersContainer = findViewById(R.id.parameters_container);
        cropsChipGroup = findViewById(R.id.crops_chip_group);

        // Setup farm spinner with dummy data
        setupFarmSpinner();

        // Initially, results are hidden
        resultsContainer.setVisibility(View.GONE);
        uploadContainer.setVisibility(View.VISIBLE);
        emptyPredictionContainer.setVisibility(View.VISIBLE);


        predictButton.setOnClickListener(v -> {
            // Simulate prediction
            Toast.makeText(this, "Predicting soil health...", Toast.LENGTH_SHORT).show();
            showResults();
        });
    }

    private void setupFarmSpinner() {
        // Dummy list of farm names
        List<String> farmNames = new ArrayList<>();
        farmNames.add("Rice - Kondhana");
        farmNames.add("Soybean Plot - Wakad");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, farmNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        farmSpinner.setAdapter(adapter);
    }

    private void showResults() {
        uploadContainer.setVisibility(View.GONE);
        emptyPredictionContainer.setVisibility(View.GONE);
        resultsContainer.setVisibility(View.VISIBLE);

        // Clear previous results
        parametersContainer.removeAllViews();
        cropsChipGroup.removeAllViews();

        // Add dummy parameter results
        addParameterResult("SOC (Soil Organic Carbon)", "1.58%", "High", 80, R.color.status_high);
        addParameterResult("Nitrogen (N)", "173.9kg/ha", "Medium", 50, R.color.status_medium);
        addParameterResult("Phosphorus (P)", "24.4kg/ha", "Medium", 60, R.color.status_medium);
        addParameterResult("Potassium (K)", "85.7kg/ha", "Low", 20, R.color.status_low);
        addParameterResult("pH Level", "7", "Medium", 70, R.color.status_medium);

        // Add dummy recommended crops
        addCropChip("Wheat (Best)", true);
        addCropChip("Soybean (Best)", true);
        addCropChip("Rice (Best)", true);
        addCropChip("Cotton (Alternative)", false);
        addCropChip("Jowar (Alternative)", false);
    }

    private void addParameterResult(String name, String value, String status, int progress, int statusColorRes) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View parameterView = inflater.inflate(R.layout.list_item_parameter, parametersContainer, false);

        TextView nameView = parameterView.findViewById(R.id.parameter_name);
        TextView valueView = parameterView.findViewById(R.id.parameter_value);
        TextView statusView = parameterView.findViewById(R.id.parameter_status);
        ProgressBar progressBar = parameterView.findViewById(R.id.parameter_progress);

        nameView.setText(name);
        valueView.setText(value);
        statusView.setText(status);
        
        int color = ContextCompat.getColor(this, statusColorRes);
        statusView.setTextColor(color);

        progressBar.setProgress(progress);
        LayerDrawable progressDrawable = (LayerDrawable) progressBar.getProgressDrawable();
        progressDrawable.findDrawableByLayerId(android.R.id.progress).setColorFilter(color, PorterDuff.Mode.SRC_IN);

        parametersContainer.addView(parameterView);
    }
    
    private void addCropChip(String cropName, boolean isBest) {
        Chip chip = new Chip(this);
        chip.setText(cropName);
        chip.setChipIcon(ContextCompat.getDrawable(this, R.drawable.ic_sprout));
        
        if (isBest) {
            chip.setChipBackgroundColorResource(R.color.brand_green);
            chip.setTextColor(ContextCompat.getColor(this, R.color.white));
            chip.setChipIconTintResource(R.color.white);
        } else {
            chip.setChipBackgroundColorResource(R.color.bg_secondary);
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            chip.setChipIconTintResource(R.color.text_secondary);
        }

        cropsChipGroup.addView(chip);
    }
}
