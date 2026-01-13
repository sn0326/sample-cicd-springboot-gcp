package com.sn0326.cicddemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

@RestController
class HelloController {
    
    @GetMapping("/")
    public String hello() {
        return "🚀 Sample CI/CD: Spring Boot on GCP Cloud Run!";
    }
    
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
    
    @GetMapping("/info")
    public AppInfo info() {
        return new AppInfo(
            "sample-cicd-springboot-gcp",
            "0.0.1-SNAPSHOT",
            "Sample CI/CD Spring Boot Application on GCP",
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}

record AppInfo(String name, String version, String description, String timestamp) {}