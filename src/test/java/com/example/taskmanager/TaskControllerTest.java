package com.example.taskmanager;


import com.example.taskmanager.model.Category;
import com.example.taskmanager.model.Priority;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.CategoryRepository;
import com.example.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class TaskControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    int port;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    CategoryRepository categoryRepository;

    RestClient restClient;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
        taskRepository.deleteAll();
        categoryRepository.deleteAll();
        testCategory = categoryRepository.save(
                new Category("Work", "Work tasks"));
    }

    @Test
    void shouldCreateTask() {
        String body = """
            {
                "title": "Finish report",
                "description": "Q4 quarterly report",
                "priority": "HIGH",
                "categoryId": %d
            }
            """.formatted(testCategory.getId());

        String response = restClient.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        assertThat(response).contains("Finish report");
        assertThat(response).contains("HIGH");
    }

    @Test
    void shouldReturn404ForNonExistentTask() {
        try {
            restClient.get()
                    .uri("/api/tasks/999")
                    .retrieve()
                    .body(String.class);
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound ex) {
            assertThat(ex.getStatusCode().value()).isEqualTo(404);
            assertThat(ex.getResponseBodyAsString()).contains("Task Not Found");
            return;
        }
        throw new AssertionError("Expected 404 Not Found");
    }

    @Test
    void shouldListAllTasks() {
        taskRepository.save(new Task("Task 1", "Desc 1",
                Priority.HIGH, testCategory));
        taskRepository.save(new Task("Task 2", "Desc 2",
                Priority.LOW, testCategory));

        String response = restClient.get()
                .uri("/api/tasks")
                .retrieve()
                .body(String.class);

        assertThat(response).contains("Task 1", "Task 2");
    }

    @Test
    void shouldDeleteTask() {
        Task task = taskRepository.save(new Task("To delete", "Bye",
                Priority.LOW, testCategory));

        restClient.delete()
                .uri("/api/tasks/" + task.getId())
                .retrieve()
                .toBodilessEntity();

        assertThat(taskRepository.findById(task.getId())).isEmpty();
    }
}
