package com.todoapp.backend.controller;

import com.todoapp.backend.entity.UserEntity;
import com.todoapp.backend.repository.UserRepository;
import com.todoapp.backend.security.JwtTokenProvider;
import com.todoapp.backend.security.UserPrincipal;
import com.todoapp.common.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Controller for user authentication and registration.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        UserEntity user = userRepository.findByUsername(userPrincipal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + userPrincipal.getUsername()));
        
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        return ResponseEntity.ok(new AuthResponse(jwt, user.toModel()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Username is already taken!"));
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Email Address already in use!"));
        }

        // Create new user's account
        UserEntity user = UserEntity.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .createdAt(LocalDateTime.now())
                .themePreference(User.ThemePreference.LIGHT)
                .build();

        userRepository.save(user);

        // Authenticate the newly registered user to generate a token
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getUsername(),
                        registerRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        return ResponseEntity.ok(new AuthResponse(jwt, user.toModel()));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "Username is required")
        private String username;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        private String username;

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private User user;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiResponse {
        private boolean success;
        private String message;
    }
}
