package com.example.taskmanager.service;

import com.example.taskmanager.model.Category;
import com.example.taskmanager.model.Priority;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.CategoryRepository;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final Clock clock;

    public TaskService(TaskRepository taskRepository,
                       CategoryRepository categoryRepository,
                       Clock clock) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
        this.clock = clock;
    }

    @Transactional
    public Task createTask(String title, String description,
                           Priority priority, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Category not found with id: " + categoryId));

        Task task = new Task(title, description, priority, category);
        task.setCreatedAt(LocalDateTime.now(clock));
        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Task getTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @Transactional
    public Task updateTask(Long id, String title, String description,
                           Priority priority) {
        Task task = getTask(id);
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
        return taskRepository.save(task);
    }

    @Transactional
    public Task completeTask(Long id) {
        Task task = getTask(id);
        task.setCompleted(true);
        task.setCompletedAt(LocalDateTime.now(clock));
        return taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksByPriority(Priority priority) {
        return taskRepository.findByPriority(priority);
    }

    @Transactional(readOnly = true)
    public List<Task> getPendingTasks() {
        return taskRepository.findByCompletedFalseOrderByPriorityDesc();
    }
}