package com.example.smart_soil.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smart_soil.R;
import com.google.android.material.textfield.TextInputEditText;

public class ResetPasswordActivity extends BaseActivity {

    private TextInputEditText newPassword, confirmPassword;
    private Button resetButton;
    private TextView backToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        newPassword = findViewById(R.id.reset_new_password);
        confirmPassword = findViewById(R.id.reset_confirm_password);
        resetButton = findViewById(R.id.reset_button);
        backToLogin = findViewById(R.id.reset_back_to_login);

        resetButton.setOnClickListener(v -> {
            String pass = newPassword.getText().toString();
            String confirm = confirmPassword.getText().toString();

            if (pass.length() < 6) {
                newPassword.setError("Password must be at least 6 characters");
                return;
            }

            if (!pass.equals(confirm)) {
                confirmPassword.setError("Passwords do not match");
                return;
            }

            // For now, local mock implementation
            Toast.makeText(this, "Password reset successful!", Toast.LENGTH_SHORT).show();
            finish();
        });

        backToLogin.setOnClickListener(v -> finish());
    }
}
