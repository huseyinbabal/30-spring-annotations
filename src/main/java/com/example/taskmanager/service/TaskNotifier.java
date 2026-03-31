package com.example.taskmanager.service;

import com.example.taskmanager.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class TaskNotifier {

    private final NotificationService notificationService;

    @Autowired
    public TaskNotifier(@Qualifier("slackNotification") NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void notifyTaskCompleted(Task task) {
        notificationService.notify("Task completed: " + task.getTitle());
    }
}