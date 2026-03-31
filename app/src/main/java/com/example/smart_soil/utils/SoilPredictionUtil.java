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
        
        // Calculate Overall Score (0-100)
        test.overallScore = calculateOverallScore(test);
        
        // Get recommendations
        List<String> recommendations = getRecommendedCrops(test);
        
        // Join list into comma separated string (compatible with API 24)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < recommendations.size(); i++) {
            sb.append(recommendations.get(i));
            if (i < recommendations.size() - 1) {
                sb.append(",");
            }
        }
        test.recommended_crops = sb.toString();
        
        test.reportSummary = generateSummary(test);
        test.aiAnalysisDone = false;
        
        return test;
    }

    private static int calculateOverallScore(SoilTest test) {
        double score = 0;
        score += (test.soc / 2.0) * 20; // Max 2.0 SOC contributes 20 pts
        score += (test.nitrogen / 300.0) * 20; // Max 300 N contributes 20 pts
        score += (test.phosphorus / 50.0) * 20; // Max 50 P contributes 20 pts
        score += (test.potassium / 400.0) * 20; // Max 400 K contributes 20 pts
        
        // pH Score (Ideal 6.5-7.5)
        double phScore = 0;
        if (test.ph >= 6.5 && test.ph <= 7.5) phScore = 20;
        else if (test.ph >= 6.0 && test.ph <= 8.0) phScore = 15;
        else phScore = 10;
        score += phScore;

        return (int) Math.min(100, score);
    }

    private static String generateSummary(SoilTest test) {
        StringBuilder summary = new StringBuilder("Your soil health is ");
        if (test.overallScore > 80) summary.append("Excellent! ");
        else if (test.overallScore > 60) summary.append("Good. ");
        else if (test.overallScore > 40) summary.append("Average. ");
        else summary.append("Poor. Needs improvement. ");

        summary.append("Key findings: ");
        if (test.soc < SOC_LOW) summary.append("Organic Carbon is very low. ");
        if (test.nitrogen < N_LOW) summary.append("Nitrogen deficiency detected. ");
        if (test.ph < PH_LOW) summary.append("Soil is acidic. ");
        else if (test.ph > PH_MED) summary.append("Soil is alkaline. ");
        
        return summary.toString();
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
