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
    
    @Column(name = "overall_score")
    private Integer overallScore;
    
    @Column(name = "report_summary", columnDefinition = "TEXT")
    private String reportSummary;
    
    @Column(name = "ai_analysis_done")
    private Boolean aiAnalysisDone = false;
    
    private LocalDateTime testDate = LocalDateTime.now();
}
