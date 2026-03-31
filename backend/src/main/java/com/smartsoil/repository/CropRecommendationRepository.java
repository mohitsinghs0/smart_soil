package com.smartsoil.repository;

import com.smartsoil.entity.CropRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CropRecommendationRepository extends JpaRepository<CropRecommendation, Long> {
    List<CropRecommendation> findBySoilTestIdOrderByPriorityRankAsc(Long soilTestId);
}
