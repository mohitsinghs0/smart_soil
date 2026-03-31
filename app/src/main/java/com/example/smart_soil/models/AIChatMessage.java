package com.example.smart_soil.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class AIChatMessage implements Serializable {
    public Long id;
    public Long sessionId;
    public String role; // 'user' or 'assistant'
    public String content;
    
    @SerializedName("tokensUsed")
    public Integer tokensUsed;
    
    @SerializedName("responseTimeMs")
    public Long responseTimeMs;
    
    public String timestamp;

    public AIChatMessage() {}

    public AIChatMessage(Long sessionId, String role, String content) {
        this.sessionId = sessionId;
        this.role = role;
        this.content = content;
    }
}
