package com.example.smart_soil.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "farms")
public class FarmEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String serverId; // ID from backend
    private String name;
    private String location;
    private double latitude;
    private double longitude;
    private String cropType;
    private long createdAt;
    private boolean isSynced;

    public FarmEntity(String name, String location, String cropType) {
        this.name = name;
        this.location = location;
        this.cropType = cropType;
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

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getCropType() { return cropType; }
    public void setCropType(String cropType) { this.cropType = cropType; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }
}
