package com.smartsoil.repository;

import com.smartsoil.entity.AIChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AIChatSessionRepository extends JpaRepository<AIChatSession, Long> {
    List<AIChatSession> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId);
    List<AIChatSession> findBySoilTestIdOrderByCreatedAtDesc(Long soilTestId);
}
