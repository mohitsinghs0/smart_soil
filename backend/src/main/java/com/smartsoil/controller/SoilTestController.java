package com.smartsoil.controller;

import com.smartsoil.entity.CropRecommendation;
import com.smartsoil.entity.SoilTest;
import com.smartsoil.entity.User;
import com.smartsoil.repository.CropRecommendationRepository;
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

    @Autowired
    private CropRecommendationRepository cropRecommendationRepository;

    private final String UPLOAD_DIR = "uploads/";

    private Optional<User> getUserByToken(String token) {
        if (token == null) return Optional.empty();
        String cleanToken = token.replace("Bearer ", "").trim();
        return userRepository.findByToken(cleanToken);
    }

    @GetMapping
    public ResponseEntity<List<SoilTest>> getSoilTests(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam("farm_id") Long farmId) {
        return getUserByToken(token)
                .map(user -> ResponseEntity.ok(soilTestRepository.findByFarmId(farmId)))
                .orElse(ResponseEntity.status(401).build());
    }

    @PostMapping
    public ResponseEntity<?> createSoilTest(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam("farm_id") Long farmId,
            @RequestParam("soc") Double soc,
            @RequestParam("nitrogen") Double nitrogen,
            @RequestParam("phosphorus") Double phosphorus,
            @RequestParam("potassium") Double potassium,
            @RequestParam("ph") Double ph,
            @RequestParam("recommended_crops") String recommendedCrops,
            @RequestParam(value = "overall_score", required = false) Integer overallScore,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        Optional<User> userOpt = getUserByToken(token);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Unauthorized: Invalid Token");
        }

        User user = userOpt.get();
        SoilTest soilTest = new SoilTest();
        soilTest.setUserId(user.getId());
        soilTest.setFarmId(farmId);
        soilTest.setSoc(soc);
        soilTest.setNitrogen(nitrogen);
        soilTest.setPhosphorus(phosphorus);
        soilTest.setPotassium(potassium);
        soilTest.setPh(ph);
        soilTest.setRecommendedCrops(recommendedCrops);
        soilTest.setOverallScore(overallScore);

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

        SoilTest savedTest = soilTestRepository.save(soilTest);

        // Save detailed crop recommendations
        if (recommendedCrops != null && !recommendedCrops.isEmpty()) {
            String[] crops = recommendedCrops.split(",");
            for (int i = 0; i < crops.length; i++) {
                CropRecommendation recommendation = new CropRecommendation();
                recommendation.setSoilTestId(savedTest.getId());
                recommendation.setCropName(crops[i].trim());
                recommendation.setPriorityRank(i + 1);
                // Default season could be set or extracted if provided in a specific format
                cropRecommendationRepository.save(recommendation);
            }
        }

        return ResponseEntity.ok(savedTest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSoilTest(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable Long id) {
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
