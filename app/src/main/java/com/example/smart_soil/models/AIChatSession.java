package com.example.smart_soil.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class AIChatSession implements Serializable {
    public Long id;
    public Long userId;
    public Long soilTestId;
    
    @SerializedName("contextSnapshot")
    public String contextSnapshot;
    
    @SerializedName("modelUsed")
    public String modelUsed;
    
    @SerializedName("createdAt")
    public String createdAt;
    
    public Boolean isActive;
}
