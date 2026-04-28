package com.constitution.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ConstitutionBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConstitutionBackendApplication.class, args);
    }
}
