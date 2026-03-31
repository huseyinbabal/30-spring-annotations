package com.example.taskmanager.service;

import org.springframework.stereotype.Component;

@Component("slackNotification")
public class SlackNotificationService implements NotificationService {
    @Override
    public void notify(String message) {
        System.out.println("[SLACK] " + message);
    }
}