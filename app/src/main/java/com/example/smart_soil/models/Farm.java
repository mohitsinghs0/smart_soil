package com.example.smart_soil.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Farm implements Serializable {
    public int id;
    
    @SerializedName("user_id")
    public String user_id;
    
    public String name;
    public String village;
    public String city;
    public String district;
    public String state;
    
    @SerializedName("soil_type")
    public String soil_type;
    
    @SerializedName("crop_type")
    public String crop_type;
    
    @SerializedName("lat")
    public Double latitude;
    
    @SerializedName("lng")
    public Double longitude;
    
    public Double area;

    public Farm() {}

    public Farm(String name, String village, String city, String district, String state,
                String soil_type, String crop_type, Double latitude, Double longitude, Double area) {
        this.name = name;
        this.village = village;
        this.city = city;
        this.district = district;
        this.state = state;
        this.soil_type = soil_type;
        this.crop_type = crop_type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.area = area;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getSoilType() { return soil_type; }
    public void setSoilType(String soil_type) { this.soil_type = soil_type; }

    public String getCropType() { return crop_type; }
    public void setCropType(String crop_type) { this.crop_type = crop_type; }

    public String getLocation() {
        return village + ", " + district;
    }

    @Override
    public String toString() {
        return name + " (" + village + ")";
    }
}
