package com.todoapp.backend.controller;

import com.todoapp.backend.entity.CategoryEntity;
import com.todoapp.backend.repository.CategoryRepository;
import com.todoapp.backend.security.UserPrincipal;
import com.todoapp.common.model.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for Category operations.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<Category> categories = categoryRepository.findByUserIdOrderByNameAsc(userPrincipal.getId())
                .stream()
                .map(CategoryEntity::toModel)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(
            @Valid @RequestBody Category category,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        CategoryEntity categoryEntity = CategoryEntity.fromModel(category);
        // Ensure the category is associated with the authenticated user
        categoryEntity.setUserId(userPrincipal.getId());
        
        // Generate a new ID if not present
        if (categoryEntity.getId() == null) {
            categoryEntity.setId(UUID.randomUUID());
        }

        CategoryEntity savedCategory = categoryRepository.save(categoryEntity);
        return new ResponseEntity<>(savedCategory.toModel(), HttpStatus.CREATED);
    }
}
