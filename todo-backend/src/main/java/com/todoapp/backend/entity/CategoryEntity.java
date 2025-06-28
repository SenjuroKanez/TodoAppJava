package com.todoapp.backend.entity;

import com.todoapp.common.model.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * JPA Entity for Categories.
 */
@Entity
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryEntity {
    @Id
    @GeneratedValue
    private UUID id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String color;
    
    @Column(nullable = false)
    private String userId;
    
    // Convert entity to model
    public Category toModel() {
        return Category.builder()
                .id(id)
                .name(name)
                .color(color)
                .userId(userId)
                .build();
    }
    
    // Convert model to entity
    public static CategoryEntity fromModel(Category category) {
        return CategoryEntity.builder()
                .id(category.getId())
                .name(category.getName())
                .color(category.getColor())
                .userId(category.getUserId())
                .build();
    }
}