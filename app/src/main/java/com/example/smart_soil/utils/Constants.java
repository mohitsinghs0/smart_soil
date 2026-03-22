package com.example.smart_soil.utils;

public class Constants {
    // API Configuration
    public static final String BASE_URL = "http://10.0.2.2:8080"; // Emulator
    // For physical device, use: "http://192.168.x.x:8080"
    
    // Request/Response codes
    public static final int REQUEST_IMAGE_PICK = 1001;
    public static final int REQUEST_CAMERA = 1002;
    public static final int REQUEST_LOCATION = 1003;
    
    // Soil status thresholds
    public static final double SOC_LOW = 0.5;
    public static final double SOC_MEDIUM = 1.2;
    public static final double N_LOW = 120;
    public static final double N_MEDIUM = 240;
    public static final double P_LOW = 15;
    public static final double P_MEDIUM = 35;
    public static final double K_LOW = 120;
    public static final double K_MEDIUM = 280;
    public static final double PH_LOW = 6.0;
    public static final double PH_MEDIUM = 7.5;
    
    // Maharashtra coordinates (for random farm location generation)
    public static final double MAHARASHTRA_LAT = 18.5;
    public static final double MAHARASHTRA_LNG = 73.8;
    public static final double LAT_OFFSET = 1.5;  // Approx 170 km
    public static final double LNG_OFFSET = 1.5;  // Approx 120 km (varies by latitude)
    
    // UI Constants
    public static final int ANIMATION_DURATION = 300; // ms
    public static final int SPLASH_DURATION = 2000;   // 2 seconds
    
    // Crop Types
    public static final String[] CROP_TYPES = {
        "Rice", "Wheat", "Soybean", "Cotton", "Sugarcane", "Jowar", "Bajra", "Maize"
    };
    
    // Gender Options
    public static final String[] GENDER_OPTIONS = {
        "Male", "Female", "Other"
    };
}
