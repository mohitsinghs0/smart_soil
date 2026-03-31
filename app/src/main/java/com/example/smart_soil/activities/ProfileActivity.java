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
import com.example.smart_soil.models.User;
import com.example.smart_soil.services.RetrofitClient;
import com.example.smart_soil.utils.NavigationHelper;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class ProfileActivity extends BaseActivity {

    private TextView profileEmail;
    private EditText inputFullName, inputMobile;
    private Spinner spinnerGender;
    private MaterialButton saveChangesButton, logoutButton;

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
        logoutButton = findViewById(R.id.logout_button);

        // Setup gender spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        // Load profile from API
        fetchUserProfile();

        // Set click listeners
        saveChangesButton.setOnClickListener(v -> updateProfile());

        logoutButton.setOnClickListener(v -> showLogoutConfirmation());

        // Setup Custom Nav
        NavigationHelper.setupCustomNav(this, R.id.btn_nav_profile);
    }

    private void fetchUserProfile() {
        RetrofitClient.getApiService().getProfile(getAuthToken()).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    profileEmail.setText(user.email);
                    inputFullName.setText(user.name);
                    inputMobile.setText(user.mobile);
                    
                    if (user.gender != null) {
                        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerGender.getAdapter();
                        int position = adapter.getPosition(user.gender);
                        if (position >= 0) spinnerGender.setSelection(position);
                    }
                    
                    // Update local prefs just in case
                    prefsManager.saveUserName(user.name);
                    prefsManager.saveUserEmail(user.email);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Timber.e(t, "Fetch profile failure");
                // Fallback to local data
                profileEmail.setText(prefsManager.getUserEmail());
                inputFullName.setText(prefsManager.getUserName());
            }
        });
    }

    private void updateProfile() {
        String name = inputFullName.getText().toString().trim();
        String mobile = inputMobile.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();

        if (name.isEmpty()) {
            inputFullName.setError("Name required");
            return;
        }

        User updateRequest = new User();
        updateRequest.name = name;
        updateRequest.mobile = mobile;
        updateRequest.gender = gender;

        saveChangesButton.setEnabled(false);
        saveChangesButton.setText("Saving...");

        RetrofitClient.getApiService().updateProfile(getAuthToken(), updateRequest).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                saveChangesButton.setEnabled(true);
                saveChangesButton.setText("Save Changes");
                if (response.isSuccessful() && response.body() != null) {
                    User updated = response.body();
                    prefsManager.saveUserName(updated.name);
                    Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                saveChangesButton.setEnabled(true);
                saveChangesButton.setText("Save Changes");
                Timber.e(t, "Update profile failure");
                Toast.makeText(ProfileActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
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
        prefsManager.clearAll();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
