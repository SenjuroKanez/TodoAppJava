package com.todoapp.backend.controller;

import com.todoapp.backend.security.UserPrincipal;
import com.todoapp.common.dto.TodoDto;
import com.todoapp.backend.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for Todo operations.
 */
@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoController {
    private final TodoService todoService;
    
    @GetMapping
    public ResponseEntity<List<TodoDto>> getAllTodos(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(todoService.getAllTodos(userPrincipal.getId()));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TodoDto> getTodoById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(todoService.getTodoById(id, userPrincipal.getId()));
    }
    
    @PostMapping
    public ResponseEntity<TodoDto> createTodo(
            @Valid @RequestBody TodoDto todoDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return new ResponseEntity<>(todoService.createTodo(todoDto, userPrincipal.getId()), HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TodoDto> updateTodo(
            @PathVariable UUID id,
            @Valid @RequestBody TodoDto todoDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(todoService.updateTodo(id, todoDto, userPrincipal.getId()));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        todoService.deleteTodo(id, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/complete")
    public ResponseEntity<TodoDto> toggleTodoCompletion(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(todoService.toggleCompletion(id, userPrincipal.getId()));
    }
    
    @GetMapping("/completed")
    public ResponseEntity<List<TodoDto>> getCompletedTodos(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(todoService.getCompletedTodos(userPrincipal.getId()));
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<TodoDto>> getActiveTodos(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(todoService.getActiveTodos(userPrincipal.getId()));
    }
    
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<TodoDto>> getTodosByCategory(
            @PathVariable UUID categoryId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(todoService.getTodosByCategory(categoryId, userPrincipal.getId()));
    }
}