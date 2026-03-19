package com.smartsoil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.smartsoil")
public class SmartSoilApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartSoilApplication.class, args);
    }
}
