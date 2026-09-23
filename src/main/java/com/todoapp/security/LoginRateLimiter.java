package com.todoapp.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/** Bounded, per-process throttle. Remote address comes from the trusted proxy configuration. */
public class LoginRateLimiter extends OncePerRequestFilter {
    private record Window(long start, int count) {}
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        if (req.getMethod().equals("POST") && (req.getRequestURI().equals("/login") || req.getRequestURI().equals("/auth/register"))) {
            long now = System.currentTimeMillis();
            if (windows.size() > 10000) windows.entrySet().removeIf(e -> now - e.getValue().start > 60000);
            if (windows.size() > 10000) { res.sendError(429); return; }
            Window w = windows.compute(req.getRemoteAddr(), (key, old) -> old == null || now - old.start > 60000 ? new Window(now,1) : new Window(old.start, old.count+1));
            if (w.count > 20) { res.setHeader("Retry-After", "60"); res.sendError(429, "Try again in a minute"); return; }
        }
        chain.doFilter(req,res);
    }
}
