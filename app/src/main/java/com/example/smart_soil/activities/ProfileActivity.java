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
import com.example.smart_soil.models.Farm;
import com.example.smart_soil.models.SoilTest;
import com.example.smart_soil.models.User;
import com.example.smart_soil.services.RetrofitClient;
import com.example.smart_soil.utils.NavigationHelper;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class ProfileActivity extends BaseActivity {

    private TextView profileEmail, tvUserNameHeader, tvFarmCount, tvTestCount;
    private EditText inputFullName, inputMobile, inputAddress, inputState, inputPincode;
    private Spinner spinnerGender;
    private MaterialButton saveChangesButton, logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        tvUserNameHeader = findViewById(R.id.tv_user_name_header);
        profileEmail = findViewById(R.id.profile_email);
        tvFarmCount = findViewById(R.id.tv_farm_count);
        tvTestCount = findViewById(R.id.tv_test_count);
        
        inputFullName = findViewById(R.id.input_full_name_profile);
        inputMobile = findViewById(R.id.input_mobile_profile);
        inputAddress = findViewById(R.id.input_address_profile);
        inputState = findViewById(R.id.input_state_profile);
        inputPincode = findViewById(R.id.input_pincode_profile);
        
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
        fetchUserStats();

        // Set click listeners
        saveChangesButton.setOnClickListener(v -> updateProfile());

        logoutButton.setOnClickListener(v -> showLogoutConfirmation());

        // Setup Custom Nav
        NavigationHelper.setupCustomNav(this, R.id.btn_nav_profile);
    }

    private void fetchUserProfile() {
        RetrofitClient.getApiService(this).getProfile(getAuthToken()).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    User user = response.body().get(0);
                    
                    tvUserNameHeader.setText(user.name);
                    profileEmail.setText(user.email);
                    inputFullName.setText(user.name);
                    inputMobile.setText(user.mobile);
                    inputAddress.setText(user.address);
                    inputState.setText(user.state);
                    inputPincode.setText(user.pincode);
                    
                    if (user.gender != null) {
                        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerGender.getAdapter();
                        int position = adapter.getPosition(user.gender);
                        if (position >= 0) spinnerGender.setSelection(position);
                    }
                    
                    // Update local prefs
                    prefsManager.saveUserName(user.name);
                    prefsManager.saveUserEmail(user.email);
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Timber.e(t, "Fetch profile failure");
                // Fallback to local data
                profileEmail.setText(prefsManager.getUserEmail());
                inputFullName.setText(prefsManager.getUserName());
                tvUserNameHeader.setText(prefsManager.getUserName());
            }
        });
    }

    private void fetchUserStats() {
        String userId = prefsManager.getUserId();
        
        // Fetch Farm Count
        RetrofitClient.getApiService(this).getFarms(getAuthToken(), "eq." + userId).enqueue(new Callback<List<Farm>>() {
            @Override
            public void onResponse(Call<List<Farm>> call, Response<List<Farm>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvFarmCount.setText(String.valueOf(response.body().size()));
                }
            }
            @Override
            public void onFailure(Call<List<Farm>> call, Throwable t) { Timber.e(t); }
        });

        // Fetch Total Tests Count
        RetrofitClient.getApiService(this).getSoilTestsByUser(getAuthToken(), "eq." + userId).enqueue(new Callback<List<SoilTest>>() {
            @Override
            public void onResponse(Call<List<SoilTest>> call, Response<List<SoilTest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvTestCount.setText(String.valueOf(response.body().size()));
                }
            }
            @Override
            public void onFailure(Call<List<SoilTest>> call, Throwable t) { Timber.e(t); }
        });
    }

    private void updateProfile() {
        String name = inputFullName.getText().toString().trim();
        String mobile = inputMobile.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();
        String address = inputAddress.getText().toString().trim();
        String state = inputState.getText().toString().trim();
        String pincode = inputPincode.getText().toString().trim();

        if (name.isEmpty()) {
            inputFullName.setError("Name required");
            return;
        }

        User updateRequest = new User();
        updateRequest.name = name;
        updateRequest.mobile = mobile;
        updateRequest.gender = gender;
        updateRequest.address = address;
        updateRequest.state = state;
        updateRequest.pincode = pincode;

        saveChangesButton.setEnabled(false);
        saveChangesButton.setText("Saving...");

        RetrofitClient.getApiService(this).updateProfile(getAuthToken(), prefsManager.getUserId(), updateRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                saveChangesButton.setEnabled(true);
                saveChangesButton.setText("Save Changes");
                if (response.isSuccessful()) {
                    prefsManager.saveUserName(name);
                    tvUserNameHeader.setText(name);
                    Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
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
