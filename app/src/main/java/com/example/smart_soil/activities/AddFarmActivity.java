package com.example.smart_soil.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.example.smart_soil.R;
import com.example.smart_soil.database.FarmEntity;
import com.example.smart_soil.database.SmartSoilDatabase;
import com.example.smart_soil.models.Farm;
import com.example.smart_soil.services.RetrofitClient;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddFarmActivity extends BaseActivity {

    private EditText inputFarmName, inputCrop, inputSoilType, inputArea, inputCity, inputDistrict, inputState, inputVillage;
    private MaterialButton saveFarmButton;
    private Toolbar toolbar;
    
    private boolean isEditMode = false;
    private int farmId = -1;
    private FarmEntity existingFarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_farm);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        inputFarmName = findViewById(R.id.input_farm_name);
        inputCrop = findViewById(R.id.input_crop);
        inputSoilType = findViewById(R.id.input_soil_type);
        inputArea = findViewById(R.id.input_area);
        inputCity = findViewById(R.id.input_city);
        inputDistrict = findViewById(R.id.input_district);
        inputState = findViewById(R.id.input_state);
        inputVillage = findViewById(R.id.input_village);
        saveFarmButton = findViewById(R.id.save_farm_button);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Check for edit mode
        isEditMode = getIntent().getBooleanExtra("edit_mode", false);
        farmId = getIntent().getIntExtra("farm_id", -1);

        if (isEditMode && farmId != -1) {
            setupEditMode();
        }

        // Set click listener
        saveFarmButton.setOnClickListener(v -> {
            if (isEditMode) {
                updateFarm();
            } else {
                saveFarm();
            }
        });
    }

    private void setupEditMode() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit Farm");
        }
        saveFarmButton.setText("Update Farm");
        
        Executors.newSingleThreadExecutor().execute(() -> {
            existingFarm = SmartSoilDatabase.getInstance(this).farmDao().getFarmById(farmId);
            if (existingFarm != null) {
                runOnUiThread(() -> {
                    inputFarmName.setText(existingFarm.getName());
                    inputCrop.setText(existingFarm.getCropType());
                    inputSoilType.setText(existingFarm.getSoilType());
                    inputArea.setText(String.valueOf(existingFarm.getArea()));
                    inputVillage.setText(existingFarm.getVillage());
                    inputCity.setText(existingFarm.getCity());
                    inputDistrict.setText(existingFarm.getDistrict());
                    inputState.setText(existingFarm.getState());
                });
            }
        });
    }

    private void saveFarm() {
        final String farmName = inputFarmName.getText().toString().trim();
        final String crop = inputCrop.getText().toString().trim();
        final String soilType = inputSoilType.getText().toString().trim();
        String areaStr = inputArea.getText().toString().trim();
        final String city = inputCity.getText().toString().trim();
        final String district = inputDistrict.getText().toString().trim();
        final String state = inputState.getText().toString().trim();
        final String village = inputVillage.getText().toString().trim();

        if (farmName.isEmpty()) {
            inputFarmName.setError("Farm name is required");
            return;
        }

        double areaVal = 0.0;
        try {
            if (!areaStr.isEmpty()) areaVal = Double.parseDouble(areaStr);
        } catch (NumberFormatException e) {
            inputArea.setError("Invalid area");
            return;
        }
        final double area = areaVal;

        Map<String, Object> farmData = new HashMap<>();
        farmData.put("name", farmName);
        farmData.put("village", village);
        farmData.put("city", city);
        farmData.put("district", district);
        farmData.put("state", state);
        farmData.put("soil_type", soilType);
        farmData.put("crop_type", crop);
        farmData.put("user_id", prefsManager.getUserId());
        farmData.put("lat", 0.0);
        farmData.put("lng", 0.0);
        farmData.put("area", area);

        saveFarmButton.setEnabled(false);
        saveFarmButton.setText("Saving...");

        RetrofitClient.getApiService(this).createFarm(getAuthToken(), farmData).enqueue(new Callback<List<Farm>>() {
            @Override
            public void onResponse(Call<List<Farm>> call, Response<List<Farm>> response) {
                saveFarmButton.setEnabled(true);
                saveFarmButton.setText("Save Farm");

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    saveLocally(response.body().get(0));
                } else {
                    Toast.makeText(AddFarmActivity.this, "Failed to add farm: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Farm>> call, Throwable t) {
                saveFarmButton.setEnabled(true);
                saveFarmButton.setText("Save Farm");
                Toast.makeText(AddFarmActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateFarm() {
        final String farmName = inputFarmName.getText().toString().trim();
        final String crop = inputCrop.getText().toString().trim();
        final String soilType = inputSoilType.getText().toString().trim();
        String areaStr = inputArea.getText().toString().trim();
        final String city = inputCity.getText().toString().trim();
        final String district = inputDistrict.getText().toString().trim();
        final String state = inputState.getText().toString().trim();
        final String village = inputVillage.getText().toString().trim();

        if (farmName.isEmpty()) {
            inputFarmName.setError("Farm name is required");
            return;
        }

        double areaVal = 0.0;
        try {
            if (!areaStr.isEmpty()) areaVal = Double.parseDouble(areaStr);
        } catch (NumberFormatException e) {
            inputArea.setError("Invalid area");
            return;
        }
        final double area = areaVal;

        Farm farm = new Farm(farmName, village, city, district, state, soilType, crop, 0.0, 0.0, area);
        farm.user_id = prefsManager.getUserId();
        
        String serverIdQuery = null;
        if (existingFarm.getServerId() != null) {
            serverIdQuery = "eq." + existingFarm.getServerId();
        }

        saveFarmButton.setEnabled(false);
        saveFarmButton.setText("Updating...");

        RetrofitClient.getApiService(this).updateFarm(getAuthToken(), serverIdQuery, farm).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                saveFarmButton.setEnabled(true);
                saveFarmButton.setText("Update Farm");

                if (response.isSuccessful()) {
                    updateLocally(farmName, village, city, district, state, soilType, crop, area);
                } else {
                    Toast.makeText(AddFarmActivity.this, "Failed to update farm: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                saveFarmButton.setEnabled(true);
                saveFarmButton.setText("Update Farm");
                Toast.makeText(AddFarmActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveLocally(Farm farm) {
        Executors.newSingleThreadExecutor().execute(() -> {
            FarmEntity entity = new FarmEntity(
                farm.name, 
                farm.village, 
                farm.city, 
                farm.district, 
                farm.state, 
                farm.soil_type, 
                farm.crop_type, 
                farm.latitude != null ? farm.latitude : 0.0, 
                farm.longitude != null ? farm.longitude : 0.0, 
                farm.area != null ? farm.area : 0.0
            );
            entity.setServerId(String.valueOf(farm.id));
            entity.setSynced(true);
            SmartSoilDatabase.getInstance(this).farmDao().insert(entity);
            runOnUiThread(() -> {
                Toast.makeText(this, "Farm added successfully!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void updateLocally(String name, String village, String city, String district, String state, String soilType, String crop, double area) {
        Executors.newSingleThreadExecutor().execute(() -> {
            existingFarm.setName(name);
            existingFarm.setVillage(village);
            existingFarm.setCity(city);
            existingFarm.setDistrict(district);
            existingFarm.setState(state);
            existingFarm.setSoilType(soilType);
            existingFarm.setCropType(crop);
            existingFarm.setArea(area);
            SmartSoilDatabase.getInstance(this).farmDao().update(existingFarm);
            runOnUiThread(() -> {
                Toast.makeText(this, "Farm updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
