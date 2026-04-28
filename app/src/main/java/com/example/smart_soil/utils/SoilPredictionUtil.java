package com.example.smart_soil.utils;

import com.example.smart_soil.R;
import com.example.smart_soil.models.SoilTest;
import java.util.ArrayList;
import java.util.List;

public class SoilPredictionUtil {
    
    // Status thresholds for SOC per requirements
    public static final double SOC_LOW_LIMIT = 0.5;
    public static final double SOC_SUFFICIENT_LIMIT = 1.0;

    // Other parameter thresholds (kept for fallback/consistency)
    public static final double N_LOW = 120, N_MED = 240;
    public static final double PH_LOW = 6.0, PH_MED = 7.5;
    
    /**
     * Get status for a parameter. 
     * Specifically handles SOC per the new production requirements.
     */
    public static String getStatus(String parameter, double value) {
        if ("soc".equalsIgnoreCase(parameter)) {
            if (value < SOC_LOW_LIMIT) return "Low";
            if (value <= SOC_SUFFICIENT_LIMIT) return "Medium";
            return "Sufficient";
        }
        
        switch (parameter.toLowerCase()) {
            case "nitrogen":
                return value < N_LOW ? "Low" : (value < N_MED ? "Medium" : "High");
            case "ph":
                return value < PH_LOW ? "Low" : (value < PH_MED ? "Medium" : "High");
            default:
                return "Normal";
        }
    }
    
    /**
     * Get status color specifically for SOC based on new logic.
     * Low -> yellow/amber, Medium -> orange/red, Sufficient -> green.
     */
    public static int getSOCColor(String status) {
        switch (status) {
            case "Low":
                return 0xFFFFC107; // Amber
            case "Medium":
                return 0xFFFF5722; // Deep Orange / Red-ish
            case "Sufficient":
                return 0xFF4CAF50; // Green
            default:
                return 0xFF9E9E9E; // Grey
        }
    }

    public static List<String> getRecommendedCrops(double nitrogen, double ph, double soc) {
        List<String> crops = new ArrayList<>();
        if (nitrogen > 200 && ph >= 6.5 && ph <= 7.5) crops.add("Wheat");
        if (nitrogen > 150 && ph < 7.5) crops.add("Rice");
        if (soc > 1.0) crops.add("Sugarcane");
        if (ph >= 6.0 && ph <= 7.0) crops.add("Soybean");
        if (crops.isEmpty()) {
            crops.add("Millets");
            crops.add("Pulses");
        }
        return crops;
    }

    public static int calculateOverallScore(double nitrogen, double ph, double soc) {
        double score = 0;
        score += Math.min(20, (soc / 1.5) * 20);
        score += Math.min(40, (nitrogen / 300.0) * 40);
        if (ph >= 6.5 && ph <= 7.5) score += 40;
        else if (ph >= 6.0 && ph <= 8.0) score += 20;
        return (int) score;
    }
}
