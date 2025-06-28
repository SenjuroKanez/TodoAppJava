package com.todoapp.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a user in the application.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    private UUID id;
    private String username;
    private String email;
    private String password; // This would be hashed in the database
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private ThemePreference themePreference;
    
    public enum ThemePreference {
        LIGHT, DARK, SYSTEM
    }
}