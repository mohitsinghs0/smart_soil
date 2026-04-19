package com.example.smart_soil.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class SoilTest implements Serializable {
    public long id;
    
    @SerializedName("user_id")
    public String user_id;
    
    @SerializedName("farm_id")
    public Long farm_id;
    
    public double soc;
    public double nitrogen;
    public double phosphorus;
    public double potassium;
    public double ph;
    
    @SerializedName("recommended_crops")
    public String recommended_crops;

    @SerializedName("overall_score")
    public Integer overallScore;

    @SerializedName("image_url")
    public String image_url;
    
    // Compatibility fields for legacy code
    @SerializedName("test_date")
    public String test_date;
    
    @SerializedName("image_path")
    public String image_path;
    
    @SerializedName("report_summary")
    public String reportSummary;
    
    @SerializedName("ai_analysis_done")
    public Boolean aiAnalysisDone;

    @SerializedName("created_at")
    public String created_at;

    public SoilTest() {}
}
