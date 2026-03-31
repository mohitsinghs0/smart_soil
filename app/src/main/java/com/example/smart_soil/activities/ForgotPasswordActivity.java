package com.example.smart_soil.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smart_soil.R;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends BaseActivity {

    private TextInputEditText forgotEmail;
    private Button submitButton;
    private TextView backToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        forgotEmail = findViewById(R.id.forgot_email);
        submitButton = findViewById(R.id.forgot_submit_button);
        backToLogin = findViewById(R.id.back_to_login_link);

        submitButton.setOnClickListener(v -> {
            String email = forgotEmail.getText().toString().trim();
            if (email.isEmpty()) {
                forgotEmail.setError("Email is required");
                return;
            }
            // For now, just show a toast
            Toast.makeText(this, "Password reset link sent to: " + email, Toast.LENGTH_LONG).show();
            finish();
        });

        backToLogin.setOnClickListener(v -> finish());
    }
}
