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

    private Optional<User> getUserByToken(String token) {
        String cleanToken = token.replace("Bearer ", "").trim();
        return userRepository.findByToken(cleanToken);
    }

    @GetMapping
    public ResponseEntity<List<Farm>> getFarms(@RequestHeader("Authorization") String token) {
        return getUserByToken(token)
                .map(user -> ResponseEntity.ok(farmRepository.findByUserId(user.getId())))
                .orElse(ResponseEntity.status(401).build());
    }

    @PostMapping
    public ResponseEntity<Farm> createFarm(@RequestHeader("Authorization") String token, @RequestBody Farm farm) {
        return getUserByToken(token)
                .map(user -> {
                    farm.setUserId(user.getId());
                    return ResponseEntity.ok(farmRepository.save(farm));
                })
                .orElse(ResponseEntity.status(401).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Farm> updateFarm(@RequestHeader("Authorization") String token, @PathVariable Long id, @RequestBody Farm farmDetails) {
        return getUserByToken(token)
                .map(user -> {
                    Optional<Farm> farmOpt = farmRepository.findById(id);
                    if (farmOpt.isPresent() && farmOpt.get().getUserId().equals(user.getId())) {
                        Farm farm = farmOpt.get();
                        farm.setName(farmDetails.getName());
                        farm.setVillage(farmDetails.getVillage());
                        farm.setCity(farmDetails.getCity());
                        farm.setDistrict(farmDetails.getDistrict());
                        farm.setCropType(farmDetails.getCropType());
                        farm.setLat(farmDetails.getLat());
                        farm.setLng(farmDetails.getLng());
                        return ResponseEntity.ok(farmRepository.save(farm));
                    }
                    return ResponseEntity.notFound().<Farm>build();
                })
                .orElse(ResponseEntity.status(401).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFarm(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        return getUserByToken(token)
                .map(user -> {
                    Optional<Farm> farmOpt = farmRepository.findById(id);
                    if (farmOpt.isPresent() && farmOpt.get().getUserId().equals(user.getId())) {
                        farmRepository.delete(farmOpt.get());
                        return ResponseEntity.ok().<Void>build();
                    }
                    return ResponseEntity.notFound().<Void>build();
                })
                .orElse(ResponseEntity.status(401).build());
    }
}
