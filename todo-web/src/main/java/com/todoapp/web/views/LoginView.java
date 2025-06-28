package com.todoapp.web.views;

import com.todoapp.web.service.ApiService;
import com.todoapp.web.service.AuthService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import lombok.extern.slf4j.Slf4j;

/**
 * Login view for the Todo web application.
 */
@Route("login")
@PageTitle("TodoApp | Login")
@Slf4j
public class LoginView extends HorizontalLayout {
    private final ApiService apiService;
    private final AuthService authService;
    
    private TextField usernameField;
    private PasswordField passwordField;
    
    public LoginView(ApiService apiService, AuthService authService) {
        this.apiService = apiService;
        this.authService = authService;
        
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        
        // Create left and right panels
        VerticalLayout leftPanel = createLeftPanel();
        VerticalLayout rightPanel = createRightPanel();
        
        add(leftPanel, rightPanel);
        
        // If already authenticated, redirect to main view
        if (authService.isAuthenticated()) {
            UI.getCurrent().navigate("");
        }
    }
    
    private VerticalLayout createLeftPanel() {
        VerticalLayout panel = new VerticalLayout();
        panel.addClassName("login-left-panel");
        panel.setHeightFull();
        panel.setWidth("40%");
        panel.setAlignItems(FlexComponent.Alignment.CENTER);
        panel.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        panel.getStyle().set("background-color", "#766BFF");
        panel.getStyle().set("color", "white");
        panel.setSpacing(true);
        
        H1 title = new H1("TodoApp");
        title.getStyle().set("font-size", "3em");
        title.getStyle().set("font-weight", "700");
        
        Paragraph tagline = new Paragraph("Beautifully Simple Task Management");
        tagline.getStyle().set("font-size", "1.2em");
        tagline.getStyle().set("margin-top", "-15px");
        
        VerticalLayout featuresLayout = new VerticalLayout();
        featuresLayout.setSpacing(false);
        featuresLayout.setPadding(false);
        featuresLayout.setAlignItems(FlexComponent.Alignment.START);
        
        Span feature1 = new Span("✓ Organize tasks with categories");
        Span feature2 = new Span("✓ Set priorities and due dates");
        Span feature3 = new Span("✓ Sync across devices");
        Span feature4 = new Span("✓ Light and dark themes");
        
        featuresLayout.add(feature1, feature2, feature3, feature4);
        
        panel.add(title, tagline, featuresLayout);
        
        return panel;
    }
    
    private VerticalLayout createRightPanel() {
        VerticalLayout panel = new VerticalLayout();
        panel.addClassName("login-right-panel");
        panel.setHeightFull();
        panel.setWidth("60%");
        panel.setAlignItems(FlexComponent.Alignment.CENTER);
        panel.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        panel.setSpacing(true);
        
        VerticalLayout formContainer = new VerticalLayout();
        formContainer.setMaxWidth("400px");
        formContainer.setAlignItems(FlexComponent.Alignment.STRETCH);
        
        H3 header = new H3("Welcome Back");
        Paragraph subheader = new Paragraph("Login to your account");
        subheader.getStyle().set("color", "var(--lumo-secondary-text-color)");
        subheader.getStyle().set("margin-top", "-10px");
        
        FormLayout loginForm = new FormLayout();
        
        usernameField = new TextField("Username");
        usernameField.setRequired(true);
        
        passwordField = new PasswordField("Password");
        passwordField.setRequired(true);
        
        Button loginButton = new Button("Login");
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.setWidthFull();
        loginButton.addClickListener(e -> login());
        
        loginForm.add(usernameField, passwordField, loginButton);
        
        HorizontalLayout registerLayout = new HorizontalLayout();
        registerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        registerLayout.setWidthFull();
        
        Span registerText = new Span("Don't have an account?");
        RouterLink registerLink = new RouterLink("Register", RegisterView.class);
        
        registerLayout.add(registerText, registerLink);
        
        // Demo mode button
        Button demoButton = new Button("Try Demo Mode");
        demoButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        demoButton.addClickListener(e -> loginWithDemo());
        
        formContainer.add(header, subheader, loginForm, registerLayout, demoButton);
        panel.add(formContainer);
        
        return panel;
    }
    
    private void login() {
        String username = usernameField.getValue();
        String password = passwordField.getValue();
        
        if (username.isEmpty() || password.isEmpty()) {
            Notification.show("Please enter both username and password");
            return;
        }
        
        try {
            ApiService.AuthResponse response = apiService.login(username, password);
            authService.setAuthenticated(response.getToken(), response.getUser());
            UI.getCurrent().navigate("");
        } catch (Exception e) {
            log.error("Login failed", e);
            Notification.show("Login failed: " + e.getMessage());
        }
    }
    
    private void loginWithDemo() {
        // For demo purposes, use a mock token and user
        com.todoapp.common.model.User demoUser = com.todoapp.common.model.User.builder()
                .id(java.util.UUID.randomUUID())
                .username("Demo User")
                .email("demo@example.com")
                .themePreference(com.todoapp.common.model.User.ThemePreference.LIGHT)
                .build();
        
        authService.setAuthenticated("demo-token", demoUser);
        UI.getCurrent().navigate("");
    }
}