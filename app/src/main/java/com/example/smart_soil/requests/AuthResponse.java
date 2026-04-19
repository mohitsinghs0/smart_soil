package com.example.smart_soil.requests;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("access_token")
    public String accessToken;
    
    @SerializedName("refresh_token")
    public String refreshToken;
    
    @SerializedName("user")
    public User user;

    public boolean success; // Kept for compatibility with existing UI logic if needed
    public String message; // Kept for compatibility

    public static class User {
        public String id; // UUID from Supabase
        public String email;
    }

    public AuthResponse() {}
}
