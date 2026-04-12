package com.example.smart_soil.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "soil_tests")
public class SoilTestEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String serverId;
    private int farmId; // Local FK to farms table
    private String farmServerId; // Server FK
    
    // Nutrients
    private double nitrogen; // N
    private double phosphorus; // P
    private double potassium; // K
    private double phLevel;
    private double moisture;
    private double temperature;
    
    private String recommendation;
    private long testDate;
    private boolean isSynced;

    public SoilTestEntity(int farmId, double nitrogen, double phosphorus, double potassium, double phLevel) {
        this.farmId = farmId;
        this.nitrogen = nitrogen;
        this.phosphorus = phosphorus;
        this.potassium = potassium;
        this.phLevel = phLevel;
        this.testDate = System.currentTimeMillis();
        this.isSynced = false;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getServerId() { return serverId; }
    public void setServerId(String serverId) { this.serverId = serverId; }

    public int getFarmId() { return farmId; }
    public void setFarmId(int farmId) { this.farmId = farmId; }

    public String getFarmServerId() { return farmServerId; }
    public void setFarmServerId(String farmServerId) { this.farmServerId = farmServerId; }

    public double getNitrogen() { return nitrogen; }
    public void setNitrogen(double nitrogen) { this.nitrogen = nitrogen; }

    public double getPhosphorus() { return phosphorus; }
    public void setPhosphorus(double phosphorus) { this.phosphorus = phosphorus; }

    public double getPotassium() { return potassium; }
    public void setPotassium(double potassium) { this.potassium = potassium; }

    public double getPhLevel() { return phLevel; }
    public void setPhLevel(double phLevel) { this.phLevel = phLevel; }

    public double getMoisture() { return moisture; }
    public void setMoisture(double moisture) { this.moisture = moisture; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public long getTestDate() { return testDate; }
    public void setTestDate(long testDate) { this.testDate = testDate; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }
}
