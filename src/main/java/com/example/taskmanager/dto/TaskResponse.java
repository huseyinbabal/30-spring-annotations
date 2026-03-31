package com.example.taskmanager.dto;

import java.time.LocalDateTime;

public record TaskResponse(
    Long id,
    String title,
    String description,
    String priority,
    boolean completed,
    LocalDateTime createdAt,
    LocalDateTime completedAt,
    String categoryName
) {}