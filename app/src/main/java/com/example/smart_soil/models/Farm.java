package com.example.smart_soil.models;

import java.io.Serializable;

public class Farm implements Serializable {
    public int id;
    public int user_id;
    public String name;
    public String village;
    public String city;
    public String district;
    public String crop_type;
    public double latitude;
    public double longitude;
    public long created_at;

    public Farm() {}

    public Farm(String name, String village, String city, String district, 
                String crop_type, double latitude, double longitude) {
        this.name = name;
        this.village = village;
        this.city = city;
        this.district = district;
        this.crop_type = crop_type;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return name + " (" + village + ")";
    }
}
