package com.smartsoil.controller;

import com.smartsoil.entity.User;
import com.smartsoil.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Email already exists");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Use the 'password' field from @Transient and encode it into 'passwordHash'
        if (user.getPassword() != null) {
            user.setPasswordHash(passwordEncoder.encode(user.getPassword()));
        } else if (user.getPasswordHash() != null) {
            // Fallback in case client sends passwordHash directly (though not recommended)
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }
        
        user.setToken(UUID.randomUUID().toString());
        User savedUser = userRepository.save(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("user_id", savedUser.getId());
        response.put("name", savedUser.getName());
        response.put("token", savedUser.getToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Check password against passwordHash
            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("user_id", user.getId());
                response.put("name", user.getName());
                response.put("token", user.getToken());
                return ResponseEntity.ok(response);
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Invalid email or password");
        return ResponseEntity.status(401).body(response);
    }
}
