package com.smartsoil.controller;

import com.smartsoil.entity.Farm;
import com.smartsoil.entity.User;
import com.smartsoil.repository.FarmRepository;
import com.smartsoil.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/farms")
public class FarmController {

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Farm>> getFarms(@RequestHeader("Authorization") String token) {
        String cleanToken = token.replace("Bearer ", "").trim();
        Optional<User> userOpt = userRepository.findAll().stream()
                .filter(u -> cleanToken.equals(u.getToken()))
                .findFirst();
        
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(farmRepository.findByUserId(userOpt.get().getId()));
        }
        return ResponseEntity.status(401).build();
    }

    @PostMapping
    public ResponseEntity<Farm> createFarm(@RequestHeader("Authorization") String token, @RequestBody Farm farm) {
        String cleanToken = token.replace("Bearer ", "").trim();
        Optional<User> userOpt = userRepository.findAll().stream()
                .filter(u -> cleanToken.equals(u.getToken()))
                .findFirst();

        if (userOpt.isPresent()) {
            farm.setUserId(userOpt.get().getId());
            return ResponseEntity.ok(farmRepository.save(farm));
        }
        return ResponseEntity.status(401).build();
    }
}
