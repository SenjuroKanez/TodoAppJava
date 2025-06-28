package com.todoapp.backend.service;

import com.todoapp.backend.entity.TodoEntity;
import com.todoapp.backend.exception.ResourceNotFoundException;
import com.todoapp.backend.repository.TodoRepository;
import com.todoapp.common.dto.TodoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for Todo operations.
 */
@Service
@RequiredArgsConstructor
public class TodoService {
    private final TodoRepository todoRepository;
    
    /**
     * Get all todos for a user.
     */
    public List<TodoDto> getAllTodos(String userId) {
        return todoRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(entity -> TodoDto.fromEntity(entity.toModel()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get a specific todo by ID.
     */
    public TodoDto getTodoById(UUID id, String userId) {
        TodoEntity todoEntity = todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + id));
        
        // Security check
        if (!todoEntity.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Todo not found with id: " + id);
        }
        
        return TodoDto.fromEntity(todoEntity.toModel());
    }
    
    /**
     * Create a new todo.
     */
    @Transactional
    public TodoDto createTodo(TodoDto todoDto, String userId) {
        TodoEntity todoEntity = todoDto.toModel().toEntity();
        todoEntity.setUserId(userId);
        todoEntity.setCreatedAt(LocalDateTime.now());
        todoEntity.setUpdatedAt(LocalDateTime.now());
        TodoEntity savedEntity = todoRepository.save(todoEntity);
        return TodoDto.fromEntity(savedEntity.toModel());
    }
    
    /**
     * Update an existing todo.
     */
    @Transactional
    public TodoDto updateTodo(UUID id, TodoDto todoDto, String userId) {
        TodoEntity todoEntity = todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + id));
        
        // Security check
        if (!todoEntity.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Todo not found with id: " + id);
        }
        
        todoEntity.setTitle(todoDto.getTitle());
        todoEntity.setDescription(todoDto.getDescription());
        todoEntity.setCompleted(todoDto.isCompleted());
        todoEntity.setDueDate(todoDto.getDueDate());
        todoEntity.setPriority(todoDto.getPriority());
        todoEntity.setUpdatedAt(LocalDateTime.now());
        
        if (todoDto.getCategory() != null) {
            // Assuming setCategory accepts a Category entity and todoDto.getCategory() returns a Category entity or you have a mapper
            todoEntity.setCategory(todoDto.getCategory());
        }
        
        TodoEntity updatedEntity = todoRepository.save(todoEntity);
        return TodoDto.fromEntity(updatedEntity.toModel());
    }
    
    /**
     * Delete a todo.
     */
    @Transactional
    public void deleteTodo(UUID id, String userId) {
        TodoEntity todoEntity = todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + id));
        
        // Security check
        if (!todoEntity.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Todo not found with id: " + id);
        }
        
        todoRepository.delete(todoEntity);
    }
    
    /**
     * Toggle the completion status of a todo.
     */
    @Transactional
    public TodoDto toggleCompletion(UUID id, String userId) {
        TodoEntity todoEntity = todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + id));
        
        // Security check
        if (!todoEntity.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Todo not found with id: " + id);
        }
        
        todoEntity.setCompleted(!todoEntity.isCompleted());
        todoEntity.setUpdatedAt(LocalDateTime.now());
        
        TodoEntity updatedEntity = todoRepository.save(todoEntity);
        return TodoDto.fromEntity(updatedEntity.toModel());
    }
    
    /**
     * Get all completed todos for a user.
     */
    public List<TodoDto> getCompletedTodos(String userId) {
        return todoRepository.findByUserIdAndCompletedOrderByCreatedAtDesc(userId, true).stream()
                .map(entity -> TodoDto.fromEntity(entity.toModel()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get all active (not completed) todos for a user.
     */
    public List<TodoDto> getActiveTodos(String userId) {
        return todoRepository.findByUserIdAndCompletedOrderByCreatedAtDesc(userId, false).stream()
                .map(entity -> TodoDto.fromEntity(entity.toModel()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get all todos for a specific category.
     */
    public List<TodoDto> getTodosByCategory(UUID categoryId, String userId) {
        return todoRepository.findByUserIdAndCategoryIdOrderByCreatedAtDesc(userId, categoryId).stream()
                .map(entity -> TodoDto.fromEntity(entity.toModel()))
                .collect(Collectors.toList());
    }
}