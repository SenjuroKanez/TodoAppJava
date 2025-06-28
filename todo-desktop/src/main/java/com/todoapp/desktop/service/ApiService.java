package com.todoapp.desktop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.todoapp.common.dto.TodoDto;
import com.todoapp.common.model.Category;
import com.todoapp.common.model.User;
import lombok.RequiredArgsConstructor;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Service for communicating with the backend API.
 */
@RequiredArgsConstructor
public class ApiService {
    private final String baseUrl;
    private final String authToken;
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    
    // User operations
    
    public User login(String username, String password) throws IOException {
        String json = objectMapper.writeValueAsString(
                new LoginRequest(username, password)
        );
        
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(baseUrl + "/api/auth/login")
                .post(body)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Login failed: " + response.code());
            }
            
            AuthResponse authResponse = objectMapper.readValue(
                    response.body().string(), 
                    AuthResponse.class
            );
            
            return authResponse.getUser();
        }
    }
    
    public User register(String username, String email, String password) throws IOException {
        String json = objectMapper.writeValueAsString(
                new RegisterRequest(username, email, password)
        );
        
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(baseUrl + "/api/auth/register")
                .post(body)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Registration failed: " + response.code());
            }
            
            AuthResponse authResponse = objectMapper.readValue(
                    response.body().string(), 
                    AuthResponse.class
            );
            
            return authResponse.getUser();
        }
    }
    
    // Todo operations
    
    public List<TodoDto> getAllTodos() throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/todos")
                .header("Authorization", "Bearer " + authToken)
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
                .header("Authorization", "Bearer " + authToken)
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
                .header("Authorization", "Bearer " + authToken)
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
                .header("Authorization", "Bearer " + authToken)
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
                .header("Authorization", "Bearer " + authToken)
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
                .header("Authorization", "Bearer " + authToken)
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
                .header("Authorization", "Bearer " + authToken)
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
                .header("Authorization", "Bearer " + authToken)
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
                .header("Authorization", "Bearer " + authToken)
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
                .header("Authorization", "Bearer " + authToken)
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
    
    // Data classes for authentication
    
    private static class LoginRequest {
        private final String username;
        private final String password;
        
        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
        
        public String getUsername() { return username; }
        public String getPassword() { return password; }
    }
    
    private static class RegisterRequest {
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
    
    private static class AuthResponse {
        private String token;
        private User user;
        
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        
        public User getUser() { return user; }
        public void setUser(User user) { this.user = user; }
    }
}