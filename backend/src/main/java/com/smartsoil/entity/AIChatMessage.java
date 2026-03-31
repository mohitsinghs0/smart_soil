package com.smartsoil.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ai_chat_messages")
public class AIChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long sessionId;
    
    private String role; // 'user' or 'assistant'
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "tokens_used")
    private Integer tokensUsed;
    
    @Column(name = "response_time_ms")
    private Long responseTimeMs;
    
    private LocalDateTime timestamp = LocalDateTime.now();
}
