package com.example.smart_soil.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smart_soil.R;
import com.example.smart_soil.requests.LoginRequest;
import com.example.smart_soil.requests.AuthResponse;
import com.example.smart_soil.services.ApiService;
import com.example.smart_soil.services.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class LoginActivity extends BaseActivity {
    
    private EditText emailInput, passwordInput;
    private Button loginButton;
    private TextView forgotPasswordLink, registerLink;
    private com.google.android.material.button.MaterialButton googleButton;
    
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
        
        // Call API
        loginButton.setEnabled(false);
        ApiService apiService = RetrofitClient.getApiService();
        LoginRequest request = new LoginRequest(email, password);
        
        apiService.login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                loginButton.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    
                    if (authResponse.success) {
                        // Save auth data
                        prefsManager.saveToken(authResponse.token);
                        prefsManager.saveUserId(authResponse.user_id);
                        prefsManager.saveUserName(authResponse.name);
                        prefsManager.saveUserEmail(email);
                        
                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        
                        // Navigate to Dashboard
                        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, authResponse.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed. Try again.", Toast.LENGTH_SHORT).show();
                    Timber.e("Login error: %s", response.message());
                }
            }
            
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                loginButton.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Timber.e(t, "Login API error");
            }
        });
    }
}
