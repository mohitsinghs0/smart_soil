package com.smartsoil.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "crop_recommendations")
public class CropRecommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long soilTestId;
    private String cropName;
    
    @Column(name = "priority_rank")
    private Integer priorityRank;
    
    private String season; // Kharif, Rabi, Zaid
    
    @Column(columnDefinition = "TEXT")
    private String description;
}
