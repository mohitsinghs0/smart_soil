package com.smartsoil.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "soil_tests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoilTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long farmId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime testDate = LocalDateTime.now();

    @Column(nullable = true, length = 500)
    private String imagePath;

    @Column(nullable = true)
    private Double soc;

    @Column(nullable = true)
    private Double nitrogen;

    @Column(nullable = true)
    private Double phosphorus;

    @Column(nullable = true)
    private Double potassium;

    @Column(nullable = true)
    private Double ph;

    @Column(nullable = true, columnDefinition = "JSON")
    private String recommendedCrops;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
