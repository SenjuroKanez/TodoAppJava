package com.todoapp.security;

import com.todoapp.domain.Models;
import com.todoapp.domain.Problem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    private final JdbcTemplate db;
    public CurrentUser(JdbcTemplate db) { this.db=db; }
    public Models.User get() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new Problem("Please sign in again.");
        return db.query("SELECT id,username,email,theme,timezone FROM users WHERE username=?",
            (r,n) -> new Models.User(r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5)), auth.getName())
            .stream().findFirst().orElseThrow(() -> new Problem("Please sign in again."));
    }
}
