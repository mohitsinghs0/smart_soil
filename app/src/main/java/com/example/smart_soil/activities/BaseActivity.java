package com.example.smart_soil.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.smart_soil.utils.SharedPrefsManager;
import timber.log.Timber;
import com.example.smart_soil.BuildConfig;

public class BaseActivity extends AppCompatActivity {
    
    protected SharedPrefsManager prefsManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Force Light Mode to fix color glitches on physical devices
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        
        super.onCreate(savedInstanceState);
        
        // Initialize SharedPrefs Manager
        prefsManager = new SharedPrefsManager(this);
        
        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
    
    public boolean isLoggedIn() {
        return prefsManager.isTokenAvailable();
    }
    
    public String getAuthToken() {
        String token = prefsManager.getToken();
        return "Bearer " + (token != null ? token : "");
    }
    
    public void logout() {
        prefsManager.clearAll();
    }
}
