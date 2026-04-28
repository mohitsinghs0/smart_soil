package com.example.smart_soil.models;

import com.google.gson.annotations.SerializedName;

public class WeatherResponse {
    @SerializedName("main")
    public Main main;
    
    @SerializedName("wind")
    public Wind wind;

    public static class Main {
        @SerializedName("temp")
        public double temp;
        
        @SerializedName("humidity")
        public int humidity;
    }

    public static class Wind {
        @SerializedName("speed")
        public double speed;
    }
}
