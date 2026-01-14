package com.sn0326.cicddemo.controller;

import com.sn0326.cicddemo.model.AppInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
public class HelloController {

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
