package com.example.smart_soil.models;

import com.google.gson.annotations.SerializedName;

public class SoilPredictionResponse {
    @SerializedName("soc")
    private double soc;
    @SerializedName("nitrogen")
    private double nitrogen;
    @SerializedName("phosphorus")
    private double phosphorus;
    @SerializedName("potassium")
    private double potassium;
    @SerializedName("ph")
    private double ph;
    @SerializedName("recommended_crop")
    private String recommendedCrop;

    // Getters
    public double getSoc() { return soc; }
    public double getNitrogen() { return nitrogen; }
    public double getPhosphorus() { return phosphorus; }
    public double getPotassium() { return potassium; }
    public double getPh() { return ph; }
    public String getRecommendedCrop() { return recommendedCrop; }
}
