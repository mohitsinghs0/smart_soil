package com.example.smart_soil.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "farms")
public class FarmEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String serverId; // ID from backend
    private String name;
    private String village;
    private String city;
    private String district;
    private String state;
    private String soilType;
    private String cropType;
    private double latitude;
    private double longitude;
    private double area;
    private long createdAt;
    private boolean isSynced;

    public FarmEntity(String name, String village, String city, String district, String state, 
                      String soilType, String cropType, double latitude, double longitude, double area) {
        this.name = name;
        this.village = village;
        this.city = city;
        this.district = district;
        this.state = state;
        this.soilType = soilType;
        this.cropType = cropType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.area = area;
        this.createdAt = System.currentTimeMillis();
        this.isSynced = false;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getServerId() { return serverId; }
    public void setServerId(String serverId) { this.serverId = serverId; }

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

    public String getSoilType() { return soilType; }
    public void setSoilType(String soilType) { this.soilType = soilType; }

    public String getCropType() { return cropType; }
    public void setCropType(String cropType) { this.cropType = cropType; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getArea() { return area; }
    public void setArea(double area) { this.area = area; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }

    // Compatibility method for old code
    public String getLocation() {
        return village + ", " + district;
    }
}
