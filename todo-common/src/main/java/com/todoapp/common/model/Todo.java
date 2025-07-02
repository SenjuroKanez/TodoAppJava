package com.todoapp.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a Todo item in the application.
 * This class is shared between all modules.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Todo implements Serializable {
    private UUID id;
    private String title;
    private String description;
    private boolean completed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime dueDate;
    private Priority priority;
    private Category category;
    private String userId;
    
    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }
    
    
}