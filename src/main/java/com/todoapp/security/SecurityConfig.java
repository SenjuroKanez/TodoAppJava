package com.todoapp.security;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.Locale;

@Configuration
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }
    @Bean UserDetailsService userDetailsService(JdbcTemplate db) {
        return login -> db.query("SELECT username,password_hash FROM users WHERE username=? OR email=?",
            (r,n) -> User.withUsername(r.getString(1)).password(r.getString(2)).roles("USER").build(),
            login.toLowerCase(Locale.ROOT), login.toLowerCase(Locale.ROOT)).stream().findFirst()
            .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
    }
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(a -> a.requestMatchers("/auth/csrf", "/auth/register", "/manifest.webmanifest", "/sw.js", "/icons/**", "/actuator/health/**").permitAll());
        http.with(VaadinSecurityConfigurer.vaadin(), c -> c.loginView("/login", "/login"));
        http.formLogin(f -> f.successHandler((req,res,auth) -> res.setStatus(200))
            .failureHandler((req,res,e) -> res.sendError(401, "Invalid credentials")));
        http.logout(l -> l.logoutSuccessHandler((req,res,a) -> res.setStatus(204)));
        // Return the denial itself; an /error dispatch must not turn it into a login-page 200.
        http.exceptionHandling(e -> e.accessDeniedHandler((req,res,error) -> res.setStatus(403)));
        http.addFilterBefore(new LoginRateLimiter(), UsernamePasswordAuthenticationFilter.class);
        http.headers(h -> h.contentTypeOptions(c -> {}).referrerPolicy(c -> c.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN)));
        var chain=http.build();
        // Vaadin installs its own expired-session redirect during configuration.
        // API and form CSRF failures must remain explicit denials, including anonymous requests.
        chain.getFilters().stream().filter(org.springframework.security.web.csrf.CsrfFilter.class::isInstance)
            .map(org.springframework.security.web.csrf.CsrfFilter.class::cast)
            .forEach(filter -> filter.setAccessDeniedHandler((req,res,error) -> res.setStatus(403)));
        return chain;
    }
}
