package com.example.smart_soil.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smart_soil.utils.SharedPrefsManager;
import timber.log.Timber;
import com.example.smart_soil.BuildConfig;

public class BaseActivity extends AppCompatActivity {
    
    protected SharedPrefsManager prefsManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize SharedPrefs Manager
        prefsManager = new SharedPrefsManager(this);
        
        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
    
    /**
     * Check if user is logged in
     */
    protected boolean isLoggedIn() {
        return prefsManager.isTokenAvailable();
    }
    
    /**
     * Get auth token for API calls
     */
    protected String getAuthToken() {
        String token = prefsManager.getToken();
        return "Bearer " + (token != null ? token : "");
    }
    
    /**
     * Log out user
     */
    protected void logout() {
        prefsManager.clearAll();
    }
}
