package com.todoapp;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@PWA(name="TodoApp — Your daily workspace",shortName="TodoApp",offlineResources={"/"})
public class Application implements AppShellConfigurator {
    public static void main(String[] args) { SpringApplication.run(Application.class, args); }
}
