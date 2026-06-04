package com.todoapp.backend.security;

import com.todoapp.backend.entity.UserEntity;
import com.todoapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Custom UserDetailsService implementation for loading user data.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Allow login by username or email
        UserEntity user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username or email : " + usernameOrEmail)
                );

        return UserPrincipal.create(user);
    }

    /**
     * Load user by their UUID. Used by JWT authentication filter.
     */
    @Transactional
    public UserDetails loadUserById(String id) {
        UserEntity user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id : " + id));

        return UserPrincipal.create(user);
    }
}
