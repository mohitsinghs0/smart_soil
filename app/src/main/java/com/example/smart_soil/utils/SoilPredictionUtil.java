package com.example.smart_soil.utils;

import com.example.smart_soil.models.SoilTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SoilPredictionUtil {
    
    // Status thresholds
    public static final double SOC_LOW = 0.5, SOC_MED = 1.2;
    public static final double N_LOW = 120, N_MED = 240;
    public static final double P_LOW = 15, P_MED = 35;
    public static final double K_LOW = 120, K_MED = 280;
    public static final double PH_LOW = 6.0, PH_MED = 7.5;
    
    private static final Random random = new Random();
    
    /**
     * Generate mock soil test predictions
     */
    public static SoilTest predictSoilHealth() {
        SoilTest test = new SoilTest();
        
        // Mock prediction with realistic ranges
        test.soc = 0.3 + random.nextDouble() * 2.0;        // 0.3 - 2.3%
        test.nitrogen = 50 + random.nextDouble() * 300;    // 50 - 350 kg/ha
        test.phosphorus = 5 + random.nextDouble() * 60;    // 5 - 65 kg/ha
        test.potassium = 50 + random.nextDouble() * 400;   // 50 - 450 kg/ha
        test.ph = 5.0 + random.nextDouble() * 3.0;         // 5 - 8
        
        // Round to 2 decimal places
        test.soc = Math.round(test.soc * 100.0) / 100.0;
        test.nitrogen = Math.round(test.nitrogen * 100.0) / 100.0;
        test.phosphorus = Math.round(test.phosphorus * 100.0) / 100.0;
        test.potassium = Math.round(test.potassium * 100.0) / 100.0;
        test.ph = Math.round(test.ph * 100.0) / 100.0;
        
        // Get recommendations
        test.recommended_crops = getRecommendedCrops(test);
        
        return test;
    }
    
    /**
     * Get status (Low/Medium/High) for a parameter
     */
    public static String getStatus(String parameter, double value) {
        switch (parameter) {
            case "soc":
                return value < SOC_LOW ? "Low" : (value < SOC_MED ? "Medium" : "High");
            case "nitrogen":
                return value < N_LOW ? "Low" : (value < N_MED ? "Medium" : "High");
            case "phosphorus":
                return value < P_LOW ? "Low" : (value < P_MED ? "Medium" : "High");
            case "potassium":
                return value < K_LOW ? "Low" : (value < K_MED ? "Medium" : "High");
            case "ph":
                return value < PH_LOW ? "Low" : (value < PH_MED ? "Medium" : "High");
            default:
                return "Medium";
        }
    }
    
    /**
     * Get status color resource ID
     */
    public static int getStatusColor(String status) {
        switch (status) {
            case "Low":
                return android.R.color.holo_red_light;  // Will be mapped to actual color in activity
            case "Medium":
                return android.R.color.holo_orange_light;
            case "High":
                return android.R.color.holo_green_light;
            default:
                return android.R.color.darker_gray;
        }
    }
    
    /**
     * Recommend crops based on soil parameters
     */
    public static List<String> getRecommendedCrops(SoilTest test) {
        List<String> crops = new ArrayList<>();
        
        // Wheat: High N, Medium P, High K, slightly acidic to neutral pH
        if (test.nitrogen > 200 && test.phosphorus > 20 && test.ph >= 6.5 && test.ph <= 7.5) {
            crops.add("Wheat (Best)");
        } else if (test.nitrogen > 150) {
            crops.add("Wheat (Alternative)");
        }
        
        // Rice: High N, Medium P, neutral to slightly acidic
        if (test.nitrogen > 200 && test.ph >= 6.0 && test.ph <= 7.5) {
            crops.add("Rice (Best)");
        } else if (test.nitrogen > 150 && test.ph < 7.5) {
            crops.add("Rice (Alternative)");
        }
        
        // Soybean: Medium N, High P, neutral to slightly acidic
        if (test.phosphorus > 25 && test.ph >= 6.0 && test.ph <= 7.5) {
            crops.add("Soybean (Best)");
        } else if (test.phosphorus > 15) {
            crops.add("Soybean (Alternative)");
        }
        
        // Cotton: Medium to High K, slightly acidic pH
        if (test.potassium > 200 && test.ph >= 6.0 && test.ph <= 7.5) {
            crops.add("Cotton (Best)");
        } else if (test.potassium > 150) {
            crops.add("Cotton (Alternative)");
        }
        
        // Sugarcane: High N, Medium P, neutral pH
        if (test.nitrogen > 200 && test.phosphorus > 20 && test.ph >= 6.5 && test.ph <= 8.0) {
            crops.add("Sugarcane (Best)");
        } else if (test.nitrogen > 180) {
            crops.add("Sugarcane (Alternative)");
        }
        
        // Jowar (Sorghum): Tolerates low moisture, medium nutrients
        if (test.soc > 0.5 && test.ph >= 5.5 && test.ph <= 8.5) {
            crops.add("Jowar (Alternative)");
        }
        
        // Bajra (Pearl Millet): Tolerates poor soil, medium N
        if (test.nitrogen > 80 && test.ph >= 5.5) {
            crops.add("Bajra (Alternative)");
        }
        
        // If no specific crops found, recommend defaults
        if (crops.isEmpty()) {
            crops.add("Rice (Recommended)");
            crops.add("Wheat (Recommended)");
        }
        
        return crops;
    }
    
    /**
     * Calculate average value from multiple tests for trending
     */
    public static double getAverageValue(List<SoilTest> tests, String parameter) {
        if (tests == null || tests.isEmpty()) return 0;
        
        double sum = 0;
        for (SoilTest test : tests) {
            switch (parameter) {
                case "soc":
                    sum += test.soc;
                    break;
                case "nitrogen":
                    sum += test.nitrogen;
                    break;
                case "phosphorus":
                    sum += test.phosphorus;
                    break;
                case "potassium":
                    sum += test.potassium;
                    break;
                case "ph":
                    sum += test.ph;
                    break;
            }
        }
        return sum / tests.size();
    }
}
