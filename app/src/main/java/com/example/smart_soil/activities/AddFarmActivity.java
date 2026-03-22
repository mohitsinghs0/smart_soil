package com.example.smart_soil.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.example.smart_soil.R;
import com.google.android.material.button.MaterialButton;

public class AddFarmActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_farm);

        // Initialize views
        final EditText inputFarmName = findViewById(R.id.input_farm_name);
        final EditText inputCrop = findViewById(R.id.input_crop);
        final EditText inputCity = findViewById(R.id.input_city);
        final EditText inputDistrict = findViewById(R.id.input_district);
        final EditText inputVillage = findViewById(R.id.input_village);
        MaterialButton saveFarmButton = findViewById(R.id.save_farm_button);

        // Set click listener
        saveFarmButton.setOnClickListener(v -> {
            String farmName = inputFarmName.getText().toString().trim();
            String crop = inputCrop.getText().toString().trim();
            String city = inputCity.getText().toString().trim();
            String district = inputDistrict.getText().toString().trim();
            String village = inputVillage.getText().toString().trim();

            if (farmName.isEmpty()) {
                Toast.makeText(AddFarmActivity.this, "Farm name is required.", Toast.LENGTH_SHORT).show();
            } else {
                // In a real app, you would save these values to a database or via API
                Toast.makeText(AddFarmActivity.this, "Saving " + farmName + " in " + village + "...", Toast.LENGTH_SHORT).show();
                finish(); 
            }
        });
    }
}
