package com.todoapp.common.dto;

import com.todoapp.common.model.Category;
import com.todoapp.common.model.Todo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for Todo items.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoDto {
    private UUID id;
    
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters")
    private String title;
    
    @Size(max = 500, message = "Description cannot be more than 500 characters")
    private String description;
    
    private boolean completed;
    private LocalDateTime dueDate;
    private Todo.Priority priority;
    private Category category;
    
    // Convert DTO to entity
    public Todo toEntity() {
        return Todo.builder()
                .id(id != null ? id : UUID.randomUUID())
                .title(title)
                .description(description)
                .completed(completed)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .dueDate(dueDate)
                .priority(priority != null ? priority : Todo.Priority.MEDIUM)
                .category(category)
                .build();
    }
    
    // Convert entity to DTO
    public static TodoDto fromEntity(Todo todo) {
        return TodoDto.builder()
                .id(todo.getId())
                .title(todo.getTitle())
                .description(todo.getDescription())
                .completed(todo.isCompleted())
                .dueDate(todo.getDueDate())
                .priority(todo.getPriority())
                .category(todo.getCategory())
                .build();
    }
}