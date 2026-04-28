package com.example.smart_soil.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.smart_soil.R;
import com.example.smart_soil.database.FarmEntity;
import com.example.smart_soil.database.SmartSoilDatabase;
import com.example.smart_soil.models.Farm;
import com.example.smart_soil.services.RetrofitClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class AddFarmActivity extends BaseActivity {

    private EditText inputFarmName, inputCrop, inputSoilType, inputArea, inputCity, inputVillage;
    private AutoCompleteTextView inputDistrictDropdown, inputStateDropdown;
    private MaterialButton saveFarmButton, btnAutoLocate;
    private Toolbar toolbar;
    
    private boolean isEditMode = false;
    private int farmId = -1;
    private FarmEntity existingFarm;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private double currentLat = 0.0;
    private double currentLng = 0.0;

    private static final String[] MAHARASHTRA_DISTRICTS = {
            "Ahmednagar", "Akola", "Amravati", "Beed", "Bhandara", "Buldhana",
            "Chandrapur", "Chhatrapati Sambhajinagar", "Dhule", "Gadchiroli",
            "Gondia", "Hingoli", "Jalgaon", "Jalna", "Kolhapur", "Latur",
            "Mumbai City", "Mumbai Suburban", "Nagpur", "Nanded", "Nandurbar",
            "Nashik", "Osmanabad", "Palghar", "Parbhani", "Pune", "Raigad",
            "Ratnagiri", "Sangli", "Satara", "Sindhudurg", "Solapur", "Thane",
            "Wardha", "Washim", "Yavatmal"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_farm);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        inputFarmName = findViewById(R.id.input_farm_name);
        inputCrop = findViewById(R.id.input_crop);
        inputSoilType = findViewById(R.id.input_soil_type);
        inputArea = findViewById(R.id.input_area);
        inputCity = findViewById(R.id.input_city);
        inputDistrictDropdown = findViewById(R.id.input_district_dropdown);
        inputStateDropdown = findViewById(R.id.input_state_dropdown);
        inputVillage = findViewById(R.id.input_village);
        saveFarmButton = findViewById(R.id.save_farm_button);
        btnAutoLocate = findViewById(R.id.btn_auto_locate);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        setupDropdowns();

        // Check for edit mode
        isEditMode = getIntent().getBooleanExtra("edit_mode", false);
        farmId = getIntent().getIntExtra("farm_id", -1);

        if (isEditMode && farmId != -1) {
            setupEditMode();
        }

        // Set click listeners
        btnAutoLocate.setOnClickListener(v -> checkLocationPermission());
        
        saveFarmButton.setOnClickListener(v -> {
            if (isEditMode) {
                updateFarm();
            } else {
                saveFarm();
            }
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        btnAutoLocate.setEnabled(false);
        btnAutoLocate.setText("Detecting...");

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLat = location.getLatitude();
                currentLng = location.getLongitude();
                reverseGeocode(location);
            } else {
                btnAutoLocate.setEnabled(true);
                btnAutoLocate.setText("Auto-Detect Location");
                Toast.makeText(this, "Could not get location. Make sure GPS is on.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            btnAutoLocate.setEnabled(true);
            btnAutoLocate.setText("Auto-Detect Location");
            Toast.makeText(this, "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void reverseGeocode(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                
                String city = address.getLocality();
                String district = address.getSubAdminArea();
                String state = address.getAdminArea();
                String village = address.getSubLocality();

                runOnUiThread(() -> {
                    if (city != null) inputCity.setText(city);
                    if (state != null) inputStateDropdown.setText(state, false);
                    if (district != null) inputDistrictDropdown.setText(district, false);
                    if (village != null) inputVillage.setText(village);
                    
                    btnAutoLocate.setEnabled(true);
                    btnAutoLocate.setText("Location Detected!");
                    Toast.makeText(this, "Location auto-filled!", Toast.LENGTH_SHORT).show();
                });
            }
        } catch (IOException e) {
            Timber.e(e, "Reverse geocoding failed");
            runOnUiThread(() -> {
                btnAutoLocate.setEnabled(true);
                btnAutoLocate.setText("Auto-Detect Location");
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupDropdowns() {
        // Setup State Dropdown
        inputStateDropdown.setText("Maharashtra");
        // We won't lock it if auto-locate is used, but for now keeping default

        // Setup District Dropdown
        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                MAHARASHTRA_DISTRICTS
        );
        inputDistrictDropdown.setAdapter(districtAdapter);
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
                    inputDistrictDropdown.setText(existingFarm.getDistrict(), false);
                    inputStateDropdown.setText(existingFarm.getState(), false);
                    currentLat = existingFarm.getLatitude();
                    currentLng = existingFarm.getLongitude();
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
        final String district = inputDistrictDropdown.getText().toString().trim();
        final String state = inputStateDropdown.getText().toString().trim();
        final String village = inputVillage.getText().toString().trim();

        if (farmName.isEmpty()) {
            inputFarmName.setError("Farm name is required");
            return;
        }

        if (district.isEmpty()) {
            Toast.makeText(this, "Please select a district", Toast.LENGTH_SHORT).show();
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
        farmData.put("lat", currentLat);
        farmData.put("lng", currentLng);
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
        final String district = inputDistrictDropdown.getText().toString().trim();
        final String state = inputStateDropdown.getText().toString().trim();
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

        Farm farm = new Farm(farmName, village, city, district, state, soilType, crop, currentLat, currentLng, area);
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
                    updateLocally(farmName, village, city, district, state, soilType, crop, area, currentLat, currentLng);
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
            entity.setUserId(prefsManager.getUserId());
            entity.setSynced(true);
            SmartSoilDatabase.getInstance(this).farmDao().insert(entity);
            runOnUiThread(() -> {
                Toast.makeText(this, "Farm added successfully!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void updateLocally(String name, String village, String city, String district, String state, String soilType, String crop, double area, double lat, double lng) {
        Executors.newSingleThreadExecutor().execute(() -> {
            existingFarm.setName(name);
            existingFarm.setVillage(village);
            existingFarm.setCity(city);
            existingFarm.setDistrict(district);
            existingFarm.setState(state);
            existingFarm.setSoilType(soilType);
            existingFarm.setCropType(crop);
            existingFarm.setArea(area);
            existingFarm.setLatitude(lat);
            existingFarm.setLongitude(lng);
            SmartSoilDatabase.getInstance(this).farmDao().update(existingFarm);
            runOnUiThread(() -> {
                Toast.makeText(this, "Farm updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
