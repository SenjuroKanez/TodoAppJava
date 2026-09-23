package com.todoapp.security;

import com.todoapp.domain.Models.*;
import com.todoapp.domain.Problem;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final JdbcTemplate db;
    private final PasswordEncoder passwords;
    public AuthController(JdbcTemplate db, PasswordEncoder passwords) { this.db=db; this.passwords=passwords; }
    @GetMapping("/csrf") public Map<String,String> csrf(CsrfToken token) {
        return Map.of("token",token.getToken(), "parameterName",token.getParameterName(),"headerName",token.getHeaderName());
    }
    @PostMapping("/register") @Transactional
    public ResponseEntity<Void> register(@Valid @RequestBody Register input) {
        try { ZoneId.of(input.timezone()); } catch (Exception e) { throw new Problem("Choose a valid timezone."); }
        if (input.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) throw new Problem("Password must be at most 72 UTF-8 bytes.");
        String id=UUID.randomUUID().toString(), project=UUID.randomUUID().toString();
        db.update("INSERT INTO users(id,username,email,password_hash,timezone,created_at) VALUES(?,?,?,?,?,?)",id,
            input.username().toLowerCase(Locale.ROOT),input.email().toLowerCase(Locale.ROOT),passwords.encode(input.password()),input.timezone(),Instant.now().toString());
        db.update("INSERT INTO projects(id,owner_id,name,color,inbox) VALUES(?,?,?,?,true)",project,id,"Inbox","#6c63e8");
        db.update("INSERT INTO members(project_id,user_id,role) VALUES(?,?,'OWNER')",project,id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @ExceptionHandler({Problem.class, DataIntegrityViolationException.class})
    ResponseEntity<Map<String,String>> invalid(Exception e) {
        return ResponseEntity.badRequest().body(Map.of("message", e instanceof Problem ? e.getMessage() : "That username or email is unavailable."));
    }
}
