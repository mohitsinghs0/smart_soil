package com.example.smart_soil.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smart_soil.R;
import com.example.smart_soil.requests.LoginRequest;
import com.example.smart_soil.repository.UserRepository;
import com.google.android.material.button.MaterialButton;

import timber.log.Timber;

public class LoginActivity extends BaseActivity {
    
    private EditText emailInput, passwordInput;
    private Button loginButton;
    private TextView forgotPasswordLink, registerLink;
    private MaterialButton googleButton;
    private ImageView logo;
    private UserRepository userRepository;
    private boolean isLoggingIn = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Initialize views
        emailInput = findViewById(R.id.input_email);
        passwordInput = findViewById(R.id.input_password);
        loginButton = findViewById(R.id.login_button);
        googleButton = findViewById(R.id.google_button);
        forgotPasswordLink = findViewById(R.id.forgot_password_link);
        registerLink = findViewById(R.id.register_link);
        logo = findViewById(R.id.logo);
        
        // Initialize repository
        userRepository = new UserRepository(this, prefsManager);
        
        // Direct Login Shortcut: Tap logo to bypass login and go to dashboard
        logo.setOnClickListener(v -> {
            Toast.makeText(this, "Bypassing login...", Toast.LENGTH_SHORT).show();
            // Set dummy data to prevent null crashes in other activities
            prefsManager.saveToken("dummy_token");
            prefsManager.saveUserId("7ea710e6-75dc-439e-b8fe-32b9d206f6e1"); // Your ID from screenshot
            prefsManager.saveUserName("Mohit Singh");
            prefsManager.saveUserEmail("rajputmohitsingh715@gmail.com");
            
            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
            finish();
        });

        // Set click listeners
        loginButton.setOnClickListener(v -> performLogin());
        googleButton.setOnClickListener(v -> 
            Toast.makeText(LoginActivity.this, "Google Sign-In coming soon!", Toast.LENGTH_SHORT).show());
        forgotPasswordLink.setOnClickListener(v ->
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));
        registerLink.setOnClickListener(v ->
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void performLogin() {
        if (isLoggingIn) return;

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        isLoggingIn = true;
        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");
        
        LoginRequest request = new LoginRequest(email, password);
        userRepository.loginUser(request, new UserRepository.LoginCallback() {
            @Override
            public void onSuccess(com.example.smart_soil.requests.AuthResponse authResponse) {
                isLoggingIn = false;
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                isLoggingIn = false;
                runOnUiThread(() -> {
                    loginButton.setEnabled(true);
                    loginButton.setText("Login");
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                    Timber.e("Login error: %s", error);
                });
            }
        });
    }
}
