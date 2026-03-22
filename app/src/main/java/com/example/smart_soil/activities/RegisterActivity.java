package com.example.smart_soil.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smart_soil.R;
import com.example.smart_soil.requests.RegisterRequest;
import com.example.smart_soil.repository.UserRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import timber.log.Timber;

public class RegisterActivity extends BaseActivity {

    private EditText inputFullName, inputEmail, inputMobile, inputPassword, inputConfirmPassword;
    private Spinner spinnerGender;
    private MaterialButton googleButton, registerButton;
    private TextView loginLink;
    private UserRepository userRepository;
    private boolean isRegistering = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize repository
        userRepository = new UserRepository(this, prefsManager);

        // Initialize views
        inputFullName = findViewById(R.id.input_full_name);
        inputEmail = findViewById(R.id.input_email);
        spinnerGender = findViewById(R.id.spinner_gender);
        inputMobile = findViewById(R.id.input_mobile);
        inputPassword = findViewById(R.id.input_password);
        inputConfirmPassword = findViewById(R.id.input_confirm_password);
        googleButton = findViewById(R.id.google_button);
        registerButton = findViewById(R.id.register_button);
        loginLink = findViewById(R.id.login_link);

        // Set click listeners
        registerButton.setOnClickListener(v -> performRegistration());

        googleButton.setOnClickListener(v -> {
            Toast.makeText(RegisterActivity.this, "Google Sign-Up coming soon!", Toast.LENGTH_SHORT).show();
        });

        loginLink.setOnClickListener(v -> {
            // Navigate back to LoginActivity
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void performRegistration() {
        if (isRegistering) {
            Toast.makeText(this, "Registration in progress...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get input values
        String fullName = inputFullName.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();
        String mobile = inputMobile.getText().toString().trim();
        String password = inputPassword.getText().toString();
        String confirmPassword = inputConfirmPassword.getText().toString();

        // Validation
        if (!validateInputs(fullName, email, gender, mobile, password, confirmPassword)) {
            return;
        }

        // Disable button and show progress
        isRegistering = true;
        registerButton.setEnabled(false);
        registerButton.setText("Registering...");

        // Create registration request
        RegisterRequest request = new RegisterRequest(fullName, email, password, mobile, gender);

        // Call repository to register
        userRepository.registerUser(request, new UserRepository.RegistrationCallback() {
            @Override
            public void onSuccess(com.example.smart_soil.requests.AuthResponse response) {
                isRegistering = false;
                Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                
                // Navigate to Dashboard
                Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                isRegistering = false;
                runOnUiThread(() -> {
                    registerButton.setEnabled(true);
                    registerButton.setText("Register");
                    Toast.makeText(RegisterActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    Timber.e("Registration error: %s", error);
                });
            }
        });
    }

    private boolean validateInputs(String fullName, String email, String gender, 
                                   String mobile, String password, String confirmPassword) {
        if (fullName.isEmpty()) {
            Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (mobile.isEmpty()) {
            Toast.makeText(this, "Please enter your mobile number", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (mobile.length() < 10) {
            Toast.makeText(this, "Mobile number must be at least 10 digits", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (gender.equals("Select Gender")) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please confirm your password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
