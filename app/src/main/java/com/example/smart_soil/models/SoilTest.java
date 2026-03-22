package com.example.smart_soil.models;

import java.io.Serializable;
import java.util.List;

public class SoilTest implements Serializable {
    public int id;
    public int farm_id;
    public int user_id;
    public String test_date;
    public String image_path;
    public double soc;
    public double nitrogen;
    public double phosphorus;
    public double potassium;
    public double ph;
    public List<String> recommended_crops;

    public SoilTest() {}

    public SoilTest(int farm_id, String image_path, double soc, double nitrogen, 
                    double phosphorus, double potassium, double ph, List<String> recommended_crops) {
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
