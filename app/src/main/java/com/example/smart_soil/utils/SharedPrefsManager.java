package com.example.smart_soil.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsManager {
    private static final String PREFS_NAME = "smart_soil_prefs";
    private static final String PREF_TOKEN = "auth_token";
    private static final String PREF_USER_ID = "user_id";
    private static final String PREF_USER_NAME = "user_name";
    private static final String PREF_USER_EMAIL = "user_email";
    
    private SharedPreferences prefs;
    
    public SharedPrefsManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    // Token Management
    public void saveToken(String token) {
        prefs.edit().putString(PREF_TOKEN, token).apply();
    }
    
    public String getToken() {
        return prefs.getString(PREF_TOKEN, null);
    }
    
    public boolean isTokenAvailable() {
        return getToken() != null && !getToken().isEmpty();
    }
    
    // User Info Management
    public void saveUserId(int userId) {
        prefs.edit().putInt(PREF_USER_ID, userId).apply();
    }
    
    public int getUserId() {
        return prefs.getInt(PREF_USER_ID, -1);
    }
    
    public void saveUserName(String name) {
        prefs.edit().putString(PREF_USER_NAME, name).apply();
    }
    
    public String getUserName() {
        return prefs.getString(PREF_USER_NAME, "User");
    }
    
    public void saveUserEmail(String email) {
        prefs.edit().putString(PREF_USER_EMAIL, email).apply();
    }
    
    public String getUserEmail() {
        return prefs.getString(PREF_USER_EMAIL, "");
    }
    
    // Clear All (Logout)
    public void clearAll() {
        prefs.edit().clear().apply();
    }
}
