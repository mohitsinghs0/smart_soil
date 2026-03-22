package com.example.smart_soil.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smart_soil.R;
import com.google.android.material.button.MaterialButton;

public class RegisterActivity extends BaseActivity {

    private EditText inputFullName, inputEmail, inputMobile, inputPassword;
    private Spinner spinnerGender;
    private MaterialButton googleButton, registerButton;
    private TextView loginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        inputFullName = findViewById(R.id.input_full_name);
        inputEmail = findViewById(R.id.input_email);
        spinnerGender = findViewById(R.id.spinner_gender);
        inputMobile = findViewById(R.id.input_mobile);
        inputPassword = findViewById(R.id.input_password);
        googleButton = findViewById(R.id.google_button);
        registerButton = findViewById(R.id.register_button);
        loginLink = findViewById(R.id.login_link);

        // Set click listeners
        registerButton.setOnClickListener(v -> {
            // TODO: Implement registration logic
            Toast.makeText(RegisterActivity.this, "Registering...", Toast.LENGTH_SHORT).show();
        });

        googleButton.setOnClickListener(v -> {
            Toast.makeText(RegisterActivity.this, "Google Sign-Up coming soon!", Toast.LENGTH_SHORT).show();
        });

        loginLink.setOnClickListener(v -> {
            // Navigate back to LoginActivity
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }
}
