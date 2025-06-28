package com.todoapp.backend.entity;

import com.todoapp.common.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for Users.
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {
    @Id
    @GeneratedValue
    private UUID id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime lastLogin;
    
    @Enumerated(EnumType.STRING)
    private User.ThemePreference themePreference;
    
    // Convert entity to model
    public User toModel() {
        return User.builder()
                .id(id)
                .username(username)
                .email(email)
                .password("") // Don't expose the password
                .createdAt(createdAt)
                .lastLogin(lastLogin)
                .themePreference(themePreference)
                .build();
    }
    
    // Convert model to entity
    public static UserEntity fromModel(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .themePreference(user.getThemePreference())
                .build();
    }
}