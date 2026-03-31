package com.example.smart_soil.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.smart_soil.R;
import com.example.smart_soil.requests.LoginRequest;
import com.example.smart_soil.repository.UserRepository;
import com.example.smart_soil.services.RetrofitClient;
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
        
        // Set click listeners
        loginButton.setOnClickListener(v -> performLogin());
        googleButton.setOnClickListener(v -> 
            Toast.makeText(LoginActivity.this, "Google Sign-In coming soon!", Toast.LENGTH_SHORT).show());
        forgotPasswordLink.setOnClickListener(v ->
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));
        registerLink.setOnClickListener(v ->
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        // Hidden feature: Long press logo to change Server IP
        logo.setOnLongClickListener(v -> {
            showChangeIpDialog();
            return true;
        });
    }

    private void showChangeIpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Server Configuration");
        
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_ip, null);
        EditText ipInput = view.findViewById(R.id.edit_ip);
        // Pre-fill with current IP or host
        String currentUrl = RetrofitClient.getBaseUrl();
        ipInput.setText(currentUrl.replace("http://", "").replace(":8080/", ""));
        
        builder.setView(view);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newIp = ipInput.getText().toString().trim();
            if (!newIp.isEmpty()) {
                String newBaseUrl = "http://" + newIp + ":8080/";
                RetrofitClient.setBaseUrl(newBaseUrl);
                // Refresh repository with new client
                userRepository = new UserRepository(this, prefsManager);
                Toast.makeText(this, "Server IP updated to: " + newIp, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void performLogin() {
        if (isLoggingIn) return;

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Master Login Bypass
        if (email.equals("abc@gmail.com") && password.equals("Mohit@5656")) {
            prefsManager.saveUserName("Mohit");
            prefsManager.saveUserEmail("abc@gmail.com");
            prefsManager.saveToken("master_token_123");
            prefsManager.saveUserId(1);
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
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
                    Toast.makeText(LoginActivity.this, "Connection Error: Check Server IP", Toast.LENGTH_LONG).show();
                    Timber.e("Login error: %s", error);
                });
            }
        });
    }
}
