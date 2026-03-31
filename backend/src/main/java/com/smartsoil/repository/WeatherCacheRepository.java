package com.smartsoil.repository;

import com.smartsoil.entity.WeatherCache;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WeatherCacheRepository extends JpaRepository<WeatherCache, Long> {
    Optional<WeatherCache> findByCity(String city);
    Optional<WeatherCache> findByLatAndLng(Double lat, Double lng);
}
