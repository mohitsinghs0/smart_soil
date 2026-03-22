package com.example.smart_soil.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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
    private UserRepository userRepository;
    private boolean isLoggingIn = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Initialize repository
        userRepository = new UserRepository(this, prefsManager);
        
        // Initialize views
        emailInput = findViewById(R.id.input_email);
        passwordInput = findViewById(R.id.input_password);
        loginButton = findViewById(R.id.login_button);
        googleButton = findViewById(R.id.google_button);
        forgotPasswordLink = findViewById(R.id.forgot_password_link);
        registerLink = findViewById(R.id.register_link);
        
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
        if (isLoggingIn) {
            Toast.makeText(this, "Login in progress...", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        
        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Master Login Bypass for testing without backend
        if (email.equals("abc@gmail.com") && password.equals("Mohit@5656")) {
            prefsManager.saveUserName("Mohit");
            prefsManager.saveUserEmail("abc@gmail.com");
            prefsManager.saveToken("master_token_123");
            prefsManager.saveUserId(1);
            
            Toast.makeText(this, "Master Login successful!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, DashboardActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        
        // Disable button and show progress
        isLoggingIn = true;
        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");
        
        // Call repository to login
        LoginRequest request = new LoginRequest(email, password);
        userRepository.loginUser(request, new UserRepository.LoginCallback() {
            @Override
            public void onSuccess(com.example.smart_soil.requests.AuthResponse authResponse) {
                isLoggingIn = false;
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    
                    // Navigate to Dashboard
                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                isLoggingIn = false;
                runOnUiThread(() -> {
                    loginButton.setEnabled(true);
                    loginButton.setText("Login");
                    Toast.makeText(LoginActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    Timber.e("Login error: %s", error);
                });
            }
        });
    }
}
