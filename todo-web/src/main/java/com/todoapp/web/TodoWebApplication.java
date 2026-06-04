package com.todoapp.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
public class TodoWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(TodoWebApplication.class, args);
    }

    @Configuration
    public static class CorsConfig implements WebMvcConfigurer {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**")
                    .allowedOriginPatterns("*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .exposedHeaders("*")
                    .allowCredentials(false);
        }
    }
}