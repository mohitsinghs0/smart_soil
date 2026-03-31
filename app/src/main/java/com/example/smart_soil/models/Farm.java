package com.example.smart_soil.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Farm implements Serializable {
    public int id;
    
    @SerializedName("userId")
    public int user_id;
    
    public String name;
    public String village;
    public String city;
    public String district;
    
    @SerializedName("cropType")
    public String crop_type;
    
    @SerializedName("lat")
    public double latitude;
    
    @SerializedName("lng")
    public double longitude;
    
    public double area;

    public Farm() {}

    public Farm(String name, String village, String city, String district, 
                String crop_type, double latitude, double longitude, double area) {
        this.name = name;
        this.village = village;
        this.city = city;
        this.district = district;
        this.crop_type = crop_type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.area = area;
    }

    @Override
    public String toString() {
        return name + " (" + village + ")";
    }
}
