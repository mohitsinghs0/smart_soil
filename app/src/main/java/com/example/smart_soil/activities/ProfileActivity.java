package com.example.smart_soil.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.smart_soil.R;
import com.example.smart_soil.repository.UserRepository;
import com.example.smart_soil.utils.NavigationHelper;
import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends BaseActivity {

    private TextView profileEmail;
    private EditText inputFullName, inputMobile;
    private Spinner spinnerGender;
    private MaterialButton saveChangesButton, logoutButton;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize repository
        userRepository = new UserRepository(this, prefsManager);

        // Initialize views
        profileEmail = findViewById(R.id.profile_email);
        inputFullName = findViewById(R.id.input_full_name_profile);
        inputMobile = findViewById(R.id.input_mobile_profile);
        spinnerGender = findViewById(R.id.spinner_gender_profile);
        saveChangesButton = findViewById(R.id.save_changes_button);
        logoutButton = findViewById(R.id.logout_button);

        // Setup gender spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        // Load current user data
        loadUserData();

        // Set click listeners
        saveChangesButton.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Profile updated successfully (Local)", Toast.LENGTH_SHORT).show();
        });

        logoutButton.setOnClickListener(v -> showLogoutConfirmation());

        // Setup Custom Nav
        NavigationHelper.setupCustomNav(this, R.id.btn_nav_profile);
    }

    private void loadUserData() {
        profileEmail.setText(prefsManager.getUserEmail());
        inputFullName.setText(prefsManager.getUserName());
        inputMobile.setText("Not set");
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        userRepository.clearAllUsers();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
