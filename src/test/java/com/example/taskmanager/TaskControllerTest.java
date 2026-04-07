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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
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
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> "https://issuer.example.com");
    }

    @Autowired
    MockMvcTester mvc;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    CategoryRepository categoryRepository;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        categoryRepository.deleteAll();
        testCategory = categoryRepository.save(
                new Category("Work", "Work tasks"));
    }

    @Test
    void shouldCreateTaskWithUserRole() {
        assertThat(mvc.post().uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Finish report",
                        "description": "Q4 report",
                        "priority": "HIGH",
                        "categoryId": %d
                    }
                    """.formatted(testCategory.getId()))
                .with(jwt().authorities(
                        new SimpleGrantedAuthority("ROLE_USER"))))
                .hasStatus(HttpStatus.CREATED);
    }

    @Test
    void shouldDenyDeleteForUserRole() {
        Task task = taskRepository.save(
                new Task("To delete", "Bye", Priority.LOW, testCategory));

        assertThat(mvc.delete().uri("/api/tasks/" + task.getId())
                .with(jwt().authorities(
                        new SimpleGrantedAuthority("ROLE_USER"))))
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldAllowDeleteForAdminRole() {
        Task task = taskRepository.save(
                new Task("To delete", "Bye", Priority.LOW, testCategory));

        assertThat(mvc.delete().uri("/api/tasks/" + task.getId())
                .with(jwt().authorities(
                        new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldReturn401WithoutToken() {
        assertThat(mvc.get().uri("/api/tasks"))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }
}
