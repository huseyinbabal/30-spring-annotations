package com.example.taskmanager.controller;

import com.example.taskmanager.dto.*;
import com.example.taskmanager.model.Priority;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@Valid @RequestBody CreateTaskRequest request) {
        Task task = taskService.createTask(
                request.title(),
                request.description(),
                Priority.valueOf(request.priority()),
                request.categoryId()
        );
        return toResponse(task);
    }

    @GetMapping
    public List<TaskResponse> getAllTasks(
            @RequestParam(required = false) String priority) {
        List<Task> tasks;
        if (priority != null) {
            tasks = taskService.getTasksByPriority(
                    Priority.valueOf(priority));
        } else {
            tasks = taskService.getAllTasks();
        }
        return tasks.stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public TaskResponse getTask(@PathVariable Long id) {
        return toResponse(taskService.getTask(id));
    }

    @PutMapping("/{id}")
    public TaskResponse updateTask(@PathVariable Long id,
                                   @RequestBody UpdateTaskRequest request) {
        Task task = taskService.updateTask(
                id,
                request.title(),
                request.description(),
                Priority.valueOf(request.priority())
        );
        return toResponse(task);
    }

    @PatchMapping("/{id}/complete")
    public TaskResponse completeTask(@PathVariable Long id) {
        return toResponse(taskService.completeTask(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }

    @GetMapping("/pending")
    public List<TaskResponse> getPendingTasks() {
        return taskService.getPendingTasks()
                .stream().map(this::toResponse).toList();
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority().name(),
                task.isCompleted(),
                task.getCreatedAt(),
                task.getCompletedAt(),
                task.getCategory() != null ?
                    task.getCategory().getName() : null
        );
    }
}