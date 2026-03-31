package com.smartsoil.repository;

import com.smartsoil.entity.AIChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AIChatMessageRepository extends JpaRepository<AIChatMessage, Long> {
    List<AIChatMessage> findBySessionIdOrderByTimestampAsc(Long sessionId);
}
