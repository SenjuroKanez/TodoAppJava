package com.todoapp.backend.entity;

import com.todoapp.common.model.Todo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for Todo items.
 */
@Entity
@Table(name = "todos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TodoEntity {
    @Id
    @GeneratedValue
    private UUID id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 500)
    private String description;
    
    private boolean completed;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    private LocalDateTime dueDate;
    
    @Enumerated(EnumType.STRING)
    private Todo.Priority priority;
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryEntity category;
    
    @Column(nullable = false)
    private String userId;
    
    // Convert entity to model
    public Todo toModel() {
        return Todo.builder()
                .id(id)
                .title(title)
                .description(description)
                .completed(completed)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .dueDate(dueDate)
                .priority(priority)
                .category(category != null ? category.toModel() : null)
                .userId(userId)
                .build();
    }
    
    // Convert model to entity
    public static TodoEntity fromModel(Todo todo) {
        return TodoEntity.builder()
                .id(todo.getId())
                .title(todo.getTitle())
                .description(todo.getDescription())
                .completed(todo.isCompleted())
                .createdAt(todo.getCreatedAt())
                .updatedAt(todo.getUpdatedAt())
                .dueDate(todo.getDueDate())
                .priority(todo.getPriority())
                .category(todo.getCategory() != null ? CategoryEntity.fromModel(todo.getCategory()) : null)
                .userId(todo.getUserId())
                .build();
    }
}