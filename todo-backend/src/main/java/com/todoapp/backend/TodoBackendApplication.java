package com.todoapp.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main application class for the Todo backend.
 */
@SpringBootApplication
@EnableJpaAuditing
public class TodoBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(TodoBackendApplication.class, args);
    }
}