package com.example.smart_soil.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.example.smart_soil.R;
import com.example.smart_soil.models.Farm;
import com.example.smart_soil.services.RetrofitClient;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class AddFarmActivity extends BaseActivity {

    private EditText inputFarmName, inputCrop, inputCity, inputDistrict, inputVillage;
    private MaterialButton saveFarmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_farm);

        // Initialize views
        inputFarmName = findViewById(R.id.input_farm_name);
        inputCrop = findViewById(R.id.input_crop);
        inputCity = findViewById(R.id.input_city);
        inputDistrict = findViewById(R.id.input_district);
        inputVillage = findViewById(R.id.input_village);
        saveFarmButton = findViewById(R.id.save_farm_button);

        // Set click listener
        saveFarmButton.setOnClickListener(v -> saveFarm());
    }

    private void saveFarm() {
        String farmName = inputFarmName.getText().toString().trim();
        String crop = inputCrop.getText().toString().trim();
        String city = inputCity.getText().toString().trim();
        String district = inputDistrict.getText().toString().trim();
        String village = inputVillage.getText().toString().trim();

        if (farmName.isEmpty()) {
            inputFarmName.setError("Farm name is required");
            return;
        }

        // Create Farm object
        Farm farm = new Farm(farmName, village, city, district, crop, 0.0, 0.0, 0.0);
        farm.user_id = prefsManager.getUserId();

        // Show loading state
        saveFarmButton.setEnabled(false);
        saveFarmButton.setText("Saving...");

        // API Call
        RetrofitClient.getApiService().createFarm(getAuthToken(), farm).enqueue(new Callback<Farm>() {
            @Override
            public void onResponse(Call<Farm> call, Response<Farm> response) {
                saveFarmButton.setEnabled(true);
                saveFarmButton.setText("Save Farm");

                if (response.isSuccessful()) {
                    Toast.makeText(AddFarmActivity.this, "Farm added successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddFarmActivity.this, "Failed to add farm", Toast.LENGTH_SHORT).show();
                    Timber.e("Add farm error: %s", response.message());
                }
            }

            @Override
            public void onFailure(Call<Farm> call, Throwable t) {
                saveFarmButton.setEnabled(true);
                saveFarmButton.setText("Save Farm");
                Toast.makeText(AddFarmActivity.this, "Network error", Toast.LENGTH_LONG).show();
                Timber.e(t, "Add farm network failure");
            }
        });
    }
}
