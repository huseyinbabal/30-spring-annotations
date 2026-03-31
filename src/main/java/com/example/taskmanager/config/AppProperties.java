package com.example.taskmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
    int defaultPageSize,
    int maxPageSize,
    String apiVersion
) {}