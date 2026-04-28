package com.example.smart_soil.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.smart_soil.R;
import com.example.smart_soil.models.SoilTest;
import com.example.smart_soil.services.RetrofitClient;
import com.example.smart_soil.utils.SoilPredictionUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class SoilTestDetailActivity extends BaseActivity {

    private ImageView soilImage;
    private TextView testIdTv, testDateTv;
    private LinearLayout parametersContainer;
    private ChipGroup cropsChipGroup;
    private MaterialButton askAiButton;
    private SoilTest soilTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soil_test_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();

        soilTest = (SoilTest) getIntent().getSerializableExtra("soil_test");
        if (soilTest != null) {
            populateData();
        } else {
            Toast.makeText(this, "Error loading test details", Toast.LENGTH_SHORT).show();
            finish();
        }

        askAiButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AIChatActivity.class);
            intent.putExtra("soil_test_id", (long) soilTest.id);
            startActivity(intent);
        });
        
        // Hide buttons not needed in detail view (Save is already done)
        findViewById(R.id.save_result_button).setVisibility(View.GONE);
        findViewById(R.id.view_history_button).setVisibility(View.GONE);
    }

    private void initViews() {
        soilImage = findViewById(R.id.soil_image_detail);
        testIdTv = findViewById(R.id.detail_test_id);
        testDateTv = findViewById(R.id.detail_test_date);
        parametersContainer = findViewById(R.id.parameters_container);
        cropsChipGroup = findViewById(R.id.crops_chip_group);
        askAiButton = findViewById(R.id.ask_ai_button);
    }

    private void populateData() {
        testIdTv.setText("Test #" + soilTest.id);
        testDateTv.setText("Date: " + (soilTest.test_date != null ? soilTest.test_date : "N/A"));

        if (soilTest.image_path != null && !soilTest.image_path.isEmpty()) {
            String imageUrl = RetrofitClient.getBaseUrl() + "uploads/" + soilTest.image_path;
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_sprout)
                    .into(soilImage);
        }

        parametersContainer.removeAllViews();
        addParameterResult("SOC (Soil Organic Carbon)", String.format(Locale.US, "%.2f%%", soilTest.soc), "soc", soilTest.soc, 2.0);
        addParameterResult("pH Level", String.format(Locale.US, "%.2f", soilTest.ph), "ph", soilTest.ph, 14);
        addParameterResult("Nitrogen", String.format(Locale.US, "%.2f kg/ha", soilTest.nitrogen), "nitrogen", soilTest.nitrogen, 500);

        showRecommendedCrops(soilTest.recommended_crops);
        updateSocDiagram(soilTest.soc);
    }

    private void updateSocDiagram(double soc) {
        View low = findViewById(R.id.container_soc_low);
        View medium = findViewById(R.id.container_soc_medium);
        View sufficient = findViewById(R.id.container_soc_sufficient);

        if (low == null || medium == null || sufficient == null) return;

        low.setAlpha(0.2f);
        medium.setAlpha(0.2f);
        sufficient.setAlpha(0.2f);

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
        
        int progress = (int) ((value / max) * 100);
        progressBar.setProgress(Math.max(0, Math.min(100, progress)));

        if ("soc".equalsIgnoreCase(paramKey)) {
            int color = SoilPredictionUtil.getSOCColor(status);
            statusTv.setTextColor(color);
            progressBar.getProgressDrawable().setTint(color);
        }

        parametersContainer.addView(view);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
