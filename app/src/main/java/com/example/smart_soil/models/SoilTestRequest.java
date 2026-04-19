package com.example.smart_soil.models;

import com.google.gson.annotations.SerializedName;

public class SoilTestRequest {
    @SerializedName("farm_id")
    public Long farm_id;
    
    @SerializedName("user_id")
    public String user_id;
    
    public double soc;
    public double nitrogen;
    public double phosphorus;
    public double potassium;
    public double ph;
    
    @SerializedName("recommended_crops")
    public String recommended_crops;

    @SerializedName("overall_score")
    public Integer overall_score;

    @SerializedName("image_url")
    public String image_url;

    public SoilTestRequest(Long farm_id, String user_id, double soc, double nitrogen, double phosphorus, 
                          double potassium, double ph, String recommended_crops, Integer overall_score) {
        this.farm_id = farm_id;
        this.user_id = user_id;
        this.soc = soc;
        this.nitrogen = nitrogen;
        this.phosphorus = phosphorus;
        this.potassium = potassium;
        this.ph = ph;
        this.recommended_crops = recommended_crops;
        this.overall_score = overall_score;
    }
}
