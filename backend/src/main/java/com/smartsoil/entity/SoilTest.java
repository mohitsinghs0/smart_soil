package com.smartsoil.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "soil_tests")
public class SoilTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long farmId;
    private Long userId;
    private String imagePath;
    
    private Double soc;
    private Double nitrogen;
    private Double phosphorus;
    private Double potassium;
    private Double ph;
    
    @Column(columnDefinition = "TEXT")
    private String recommendedCrops;
    
    private LocalDateTime testDate = LocalDateTime.now();
}
