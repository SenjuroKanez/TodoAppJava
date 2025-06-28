package com.todoapp.web.service;

import com.todoapp.common.model.User;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

/**
 * Service for managing authentication state.
 */
@Service
@SessionScope
public class AuthService {
    @Getter
    private String token;
    
    @Getter
    private User currentUser;
    
    public boolean isAuthenticated() {
        return token != null && currentUser != null;
    }
    
    public void setAuthenticated(String token, User user) {
        this.token = token;
        this.currentUser = user;
    }
    
    public void logout() {
        this.token = null;
        this.currentUser = null;
    }
}