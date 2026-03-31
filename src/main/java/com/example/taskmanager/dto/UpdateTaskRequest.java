package com.example.taskmanager.dto;

public record UpdateTaskRequest(
    String title,
    String description,
    String priority
) {}