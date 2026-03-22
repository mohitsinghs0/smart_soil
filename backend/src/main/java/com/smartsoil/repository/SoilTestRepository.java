package com.smartsoil.repository;

import com.smartsoil.entity.SoilTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SoilTestRepository extends JpaRepository<SoilTest, Long> {
    List<SoilTest> findByFarmId(Long farmId);
    List<SoilTest> findByUserId(Long userId);
}
