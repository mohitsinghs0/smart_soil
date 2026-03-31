package com.example.smart_soil.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class SoilTest implements Serializable {
    public int id;
    
    @SerializedName("farmId")
    public int farm_id;
    
    @SerializedName("userId")
    public int user_id;
    
    @SerializedName("testDate")
    public String test_date;
    
    @SerializedName("imagePath")
    public String image_path;
    
    public double soc;
    public double nitrogen;
    public double phosphorus;
    public double potassium;
    public double ph;
    
    @SerializedName("recommendedCrops")
    public String recommended_crops;

    @SerializedName("overallScore")
    public Integer overallScore;

    @SerializedName("reportSummary")
    public String reportSummary;

    @SerializedName("aiAnalysisDone")
    public Boolean aiAnalysisDone;

    public SoilTest() {}

    public SoilTest(int farm_id, String image_path, double soc, double nitrogen, 
                    double phosphorus, double potassium, double ph, String recommended_crops) {
        this.farm_id = farm_id;
        this.image_path = image_path;
        this.soc = soc;
        this.nitrogen = nitrogen;
        this.phosphorus = phosphorus;
        this.potassium = potassium;
        this.ph = ph;
        this.recommended_crops = recommended_crops;
    }
}
