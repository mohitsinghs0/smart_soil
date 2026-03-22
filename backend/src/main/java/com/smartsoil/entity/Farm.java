package com.smartsoil.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "farms")
public class Farm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long userId;
    private String name;
    private String village;
    private String city;
    private String district;
    private String cropType;
    private Double latitude;
    private Double longitude;
}
