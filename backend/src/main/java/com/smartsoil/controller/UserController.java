package com.smartsoil.controller;

import com.smartsoil.entity.User;
import com.smartsoil.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    private Optional<User> getUserByToken(String token) {
        String cleanToken = token.replace("Bearer ", "").trim();
        return userRepository.findByToken(cleanToken);
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(@RequestHeader("Authorization") String token) {
        return getUserByToken(token)
                .map(user -> {
                    user.setPassword(null); // Hide password in response
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.status(401).build());
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@RequestHeader("Authorization") String token, @RequestBody User userDetails) {
        return getUserByToken(token)
                .map(user -> {
                    user.setName(userDetails.getName());
                    user.setMobile(userDetails.getMobile());
                    user.setGender(userDetails.getGender());
                    User updatedUser = userRepository.save(user);
                    updatedUser.setPassword(null);
                    return ResponseEntity.ok(updatedUser);
                })
                .orElse(ResponseEntity.status(401).build());
    }
}
