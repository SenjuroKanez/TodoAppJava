package com.todoapp.web.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.todoapp.common.dto.TodoDto;
import com.todoapp.common.model.Category;
import com.todoapp.common.model.User;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Service for communicating with the backend API.
 */
@Service
@SessionScope
@RequiredArgsConstructor
public class ApiService {
    @Value("${api.base-url:http://localhost:8080}")
    private String baseUrl;
    
    private final AuthService authService;
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    
    // Todo operations
    
    public List<TodoDto> getAllTodos() throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/todos")
                .header("Authorization", "Bearer " + authService.getToken())
                .get()
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to get todos: " + response.code());
            }
            
            return objectMapper.readValue(
                    response.body().string(),
                    new TypeReference<List<TodoDto>>() {}
            );
        }
    }
    
    public List<TodoDto> getCompletedTodos() throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/todos/completed")
                .header("Authorization", "Bearer " + authService.getToken())
                .get()
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to get completed todos: " + response.code());
            }
            
            return objectMapper.readValue(
                    response.body().string(),
                    new TypeReference<List<TodoDto>>() {}
            );
        }
    }
    
    public List<TodoDto> getActiveTodos() throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/todos/active")
                .header("Authorization", "Bearer " + authService.getToken())
                .get()
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to get active todos: " + response.code());
            }
            
            return objectMapper.readValue(
                    response.body().string(),
                    new TypeReference<List<TodoDto>>() {}
            );
        }
    }
    
    public List<TodoDto> getTodosByCategory(UUID categoryId) throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/todos/category/" + categoryId)
                .header("Authorization", "Bearer " + authService.getToken())
                .get()
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to get todos by category: " + response.code());
            }
            
            return objectMapper.readValue(
                    response.body().string(),
                    new TypeReference<List<TodoDto>>() {}
            );
        }
    }
    
    public TodoDto createTodo(TodoDto todoDto) throws IOException {
        String json = objectMapper.writeValueAsString(todoDto);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request request = new Request.Builder()
                .url(baseUrl + "/api/todos")
                .header("Authorization", "Bearer " + authService.getToken())
                .post(body)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to create todo: " + response.code());
            }
            
            return objectMapper.readValue(
                    response.body().string(),
                    TodoDto.class
            );
        }
    }
    
    public TodoDto updateTodo(TodoDto todoDto) throws IOException {
        String json = objectMapper.writeValueAsString(todoDto);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request request = new Request.Builder()
                .url(baseUrl + "/api/todos/" + todoDto.getId())
                .header("Authorization", "Bearer " + authService.getToken())
                .put(body)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to update todo: " + response.code());
            }
            
            return objectMapper.readValue(
                    response.body().string(),
                    TodoDto.class
            );
        }
    }
    
    public TodoDto toggleTodoCompletion(UUID id) throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/todos/" + id + "/complete")
                .header("Authorization", "Bearer " + authService.getToken())
                .patch(RequestBody.create("", MediaType.parse("text/plain")))
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to toggle todo completion: " + response.code());
            }
            
            return objectMapper.readValue(
                    response.body().string(),
                    TodoDto.class
            );
        }
    }
    
    public void deleteTodo(UUID id) throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/todos/" + id)
                .header("Authorization", "Bearer " + authService.getToken())
                .delete()
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to delete todo: " + response.code());
            }
        }
    }
    
    // Category operations
    
    public List<Category> getAllCategories() throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/categories")
                .header("Authorization", "Bearer " + authService.getToken())
                .get()
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to get categories: " + response.code());
            }
            
            return objectMapper.readValue(
                    response.body().string(),
                    new TypeReference<List<Category>>() {}
            );
        }
    }
    
    public Category createCategory(Category category) throws IOException {
        String json = objectMapper.writeValueAsString(category);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request request = new Request.Builder()
                .url(baseUrl + "/api/categories")
                .header("Authorization", "Bearer " + authService.getToken())
                .post(body)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to create category: " + response.code());
            }
            
            return objectMapper.readValue(
                    response.body().string(),
                    Category.class
            );
        }
    }
    
    // Authentication
    
    public AuthResponse login(String username, String password) throws IOException {
        LoginRequest loginRequest = new LoginRequest(username, password);
        String json = objectMapper.writeValueAsString(loginRequest);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request request = new Request.Builder()
                .url(baseUrl + "/api/auth/login")
                .post(body)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Login failed: " + response.code());
            }
            
            return objectMapper.readValue(
                    response.body().string(),
                    AuthResponse.class
            );
        }
    }
    
    public AuthResponse register(String username, String email, String password) throws IOException {
        RegisterRequest registerRequest = new RegisterRequest(username, email, password);
        String json = objectMapper.writeValueAsString(registerRequest);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request request = new Request.Builder()
                .url(baseUrl + "/api/auth/register")
                .post(body)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Registration failed: " + response.code());
            }
            
            return objectMapper.readValue(
                    response.body().string(),
                    AuthResponse.class
            );
        }
    }
    
    // Data classes
    
    public static class LoginRequest {
        private final String username;
        private final String password;
        
        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
        
        public String getUsername() { return username; }
        public String getPassword() { return password; }
    }
    
    public static class RegisterRequest {
        private final String username;
        private final String email;
        private final String password;
        
        public RegisterRequest(String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.password = password;
        }
        
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
    }
    
    public static class AuthResponse {
        private String token;
        private User user;
        
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        
        public User getUser() { return user; }
        public void setUser(User user) { this.user = user; }
    }
}