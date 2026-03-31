package com.smartsoil.controller;

import com.smartsoil.entity.AIChatMessage;
import com.smartsoil.entity.AIChatSession;
import com.smartsoil.entity.SoilTest;
import com.smartsoil.entity.User;
import com.smartsoil.repository.AIChatMessageRepository;
import com.smartsoil.repository.AIChatSessionRepository;
import com.smartsoil.repository.SoilTestRepository;
import com.smartsoil.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/ai-chat")
public class AIChatController {

    @Autowired
    private AIChatSessionRepository sessionRepository;

    @Autowired
    private AIChatMessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SoilTestRepository soilTestRepository;

    private Optional<User> getUserByToken(String token) {
        if (token == null) return Optional.empty();
        String cleanToken = token.replace("Bearer ", "").trim();
        return userRepository.findByToken(cleanToken);
    }

    @PostMapping("/sessions")
    public ResponseEntity<?> createSession(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Long> payload) {
        
        Optional<User> userOpt = getUserByToken(token);
        if (userOpt.isEmpty()) return ResponseEntity.status(401).build();

        Long soilTestId = payload.get("soil_test_id");
        Optional<SoilTest> testOpt = soilTestRepository.findById(soilTestId);
        
        if (testOpt.isEmpty()) return ResponseEntity.badRequest().body("Soil test not found");

        AIChatSession session = new AIChatSession();
        session.setUserId(userOpt.get().getId());
        session.setSoilTestId(soilTestId);
        session.setModelUsed("gemini-1.5-flash"); // Default model
        
        // Simplified context snapshot - in real app, convert SoilTest to JSON
        session.setContextSnapshot("{\"ph\":" + testOpt.get().getPh() + "}"); 

        return ResponseEntity.ok(sessionRepository.save(session));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<AIChatSession>> getSessions(@RequestHeader("Authorization") String token) {
        return getUserByToken(token)
                .map(user -> ResponseEntity.ok(sessionRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(user.getId())))
                .orElse(ResponseEntity.status(401).build());
    }

    @PostMapping("/messages")
    public ResponseEntity<?> sendMessage(
            @RequestHeader("Authorization") String token,
            @RequestBody AIChatMessage message) {
        
        Optional<User> userOpt = getUserByToken(token);
        if (userOpt.isEmpty()) return ResponseEntity.status(401).build();

        // Save user message
        message.setRole("user");
        AIChatMessage savedUserMsg = messageRepository.save(message);

        // TODO: Call LLM API (Gemini/OpenAI) here
        // For now, return a mock response
        AIChatMessage aiResponse = new AIChatMessage();
        aiResponse.setSessionId(message.getSessionId());
        aiResponse.setRole("assistant");
        aiResponse.setContent("I am analyzing your soil data. Your pH level is " + 
                "optimal for most crops. (This is a mock response)");
        aiResponse.setTokensUsed(50);
        aiResponse.setResponseTimeMs(200L);
        
        messageRepository.save(aiResponse);

        return ResponseEntity.ok(aiResponse);
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<List<AIChatMessage>> getMessages(
            @RequestHeader("Authorization") String token,
            @PathVariable Long sessionId) {
        
        Optional<User> userOpt = getUserByToken(token);
        if (userOpt.isEmpty()) return ResponseEntity.status(401).build();

        return ResponseEntity.ok(messageRepository.findBySessionIdOrderByTimestampAsc(sessionId));
    }
}
