package com.example.taskmanager.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class AppConfig {

    @Value("${spring.application.name}")
    private String appName;

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}