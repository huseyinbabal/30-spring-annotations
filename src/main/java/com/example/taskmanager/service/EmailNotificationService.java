package com.example.taskmanager.service;

import org.springframework.stereotype.Component;

@Component("emailNotification")
public class EmailNotificationService implements NotificationService {
    @Override
    public void notify(String message) {
        System.out.println("[EMAIL] " + message);
    }
}