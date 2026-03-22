package com.example.smart_soil.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.animation.AnimationUtils;

import com.example.smart_soil.R;

public class SplashActivity extends BaseActivity {
    
    private static final long SPLASH_DELAY = 2000; // 2 seconds
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        // Find views
        ImageView logoIcon = findViewById(R.id.splash_logo);
        TextView appName = findViewById(R.id.splash_app_name);
        TextView tagline = findViewById(R.id.splash_tagline);
        
        // Start animations
        if (logoIcon.getAnimation() == null) {
            logoIcon.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        }
        if (appName.getAnimation() == null) {
            appName.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        }
        if (tagline.getAnimation() == null) {
            tagline.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        }
        
        // Auto-redirect after SPLASH_DELAY
        new Handler().postDelayed(() -> {
            Intent intent;
            if (isLoggedIn()) {
                // If logged in, go to Dashboard
                intent = new Intent(SplashActivity.this, DashboardActivity.class);
            } else {
                // Otherwise go to Login
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish(); // Close splash activity
        }, SPLASH_DELAY);
    }
}
