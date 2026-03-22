package com.example.smart_soil.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smart_soil.R;
import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends BaseActivity {

    private TextView profileEmail;
    private EditText inputFullName, inputMobile;
    private Spinner spinnerGender;
    private MaterialButton saveChangesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        profileEmail = findViewById(R.id.profile_email);
        inputFullName = findViewById(R.id.input_full_name_profile);
        inputMobile = findViewById(R.id.input_mobile_profile);
        spinnerGender = findViewById(R.id.spinner_gender_profile);
        saveChangesButton = findViewById(R.id.save_changes_button);

        // Populate with dummy data
        populateDummyData();

        // Set click listener
        saveChangesButton.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Saving changes...", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void populateDummyData() {
        profileEmail.setText("rajputmohitsingh715@gmail.com");
        inputFullName.setText("Mohit Singh");
        inputMobile.setText("8652872732");

        // Set up spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
        spinnerGender.setSelection(0); // Set to "Male"
    }
}
