package com.smartsoil.controller;

import com.smartsoil.entity.SoilTest;
import com.smartsoil.entity.User;
import com.smartsoil.repository.SoilTestRepository;
import com.smartsoil.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/soil-tests")
public class SoilTestController {

    @Autowired
    private SoilTestRepository soilTestRepository;

    @Autowired
    private UserRepository userRepository;

    private final String UPLOAD_DIR = "uploads/";

    private Optional<User> getUserByToken(String token) {
        String cleanToken = token.replace("Bearer ", "").trim();
        return userRepository.findByToken(cleanToken);
    }

    @GetMapping
    public ResponseEntity<List<SoilTest>> getSoilTests(
            @RequestHeader("Authorization") String token,
            @QueryParam("farm_id") Long farmId) {
        return getUserByToken(token)
                .map(user -> ResponseEntity.ok(soilTestRepository.findByFarmId(farmId)))
                .orElse(ResponseEntity.status(401).build());
    }

    @PostMapping
    public ResponseEntity<SoilTest> createSoilTest(
            @RequestHeader("Authorization") String token,
            @RequestParam("farm_id") Long farmId,
            @RequestParam("soc") Double soc,
            @RequestParam("nitrogen") Double nitrogen,
            @RequestParam("phosphorus") Double phosphorus,
            @RequestParam("potassium") Double potassium,
            @RequestParam("ph") Double ph,
            @RequestParam("recommended_crops") String recommendedCrops,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        return getUserByToken(token)
                .map(user -> {
                    SoilTest soilTest = new SoilTest();
                    soilTest.setUserId(user.getId());
                    soilTest.setFarmId(farmId);
                    soilTest.setSoc(soc);
                    soilTest.setNitrogen(nitrogen);
                    soilTest.setPhosphorus(phosphorus);
                    soilTest.setPotassium(potassium);
                    soilTest.setPh(ph);
                    soilTest.setRecommendedCrops(recommendedCrops);

                    if (image != null && !image.isEmpty()) {
                        try {
                            String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                            Path path = Paths.get(UPLOAD_DIR + fileName);
                            Files.createDirectories(path.getParent());
                            Files.write(path, image.getBytes());
                            soilTest.setImagePath(fileName);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    return ResponseEntity.ok(soilTestRepository.save(soilTest));
                })
                .orElse(ResponseEntity.status(401).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSoilTest(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        return getUserByToken(token)
                .map(user -> {
                    Optional<SoilTest> testOpt = soilTestRepository.findById(id);
                    if (testOpt.isPresent() && testOpt.get().getUserId().equals(user.getId())) {
                        soilTestRepository.delete(testOpt.get());
                        return ResponseEntity.ok().<Void>build();
                    }
                    return ResponseEntity.notFound().<Void>build();
                })
                .orElse(ResponseEntity.status(401).build());
    }
}
