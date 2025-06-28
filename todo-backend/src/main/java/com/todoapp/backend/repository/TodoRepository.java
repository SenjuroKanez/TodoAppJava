package com.todoapp.backend.repository;

import com.todoapp.backend.entity.TodoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for accessing Todo entities.
 */
@Repository
public interface TodoRepository extends JpaRepository<TodoEntity, UUID> {
    List<TodoEntity> findByUserIdOrderByCreatedAtDesc(String userId);
    List<TodoEntity> findByUserIdAndCompletedOrderByCreatedAtDesc(String userId, boolean completed);
    List<TodoEntity> findByUserIdAndCategoryIdOrderByCreatedAtDesc(String userId, UUID categoryId);
}