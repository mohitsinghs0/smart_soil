package com.smartsoil.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ai_chat_sessions")
public class AIChatSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long userId;
    private Long soilTestId;
    
    @Column(name = "context_snapshot", columnDefinition = "TEXT")
    private String contextSnapshot; // JSON snapshot of soil test data
    
    @Column(name = "model_used")
    private String modelUsed;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "is_active")
    private Boolean isActive = true;
}
