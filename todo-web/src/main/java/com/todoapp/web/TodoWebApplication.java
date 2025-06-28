package com.todoapp.web;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Todo web client.
 */
@SpringBootApplication
@Theme(value = "todoapp")
public class TodoWebApplication implements AppShellConfigurator {
    public static void main(String[] args) {
        SpringApplication.run(TodoWebApplication.class, args);
    }
}