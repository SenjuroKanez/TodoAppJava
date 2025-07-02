package com.todoapp.web.views;

//import com.todoapp.common.model.User;
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
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import lombok.extern.slf4j.Slf4j;

/**
 * Registration view for the Todo web application.
 */
@Route("register")
@PageTitle("TodoApp | Register")
@Slf4j
public class RegisterView extends HorizontalLayout {
    private final ApiService apiService;
    private final AuthService authService;
    
    private TextField usernameField;
    private EmailField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    
    public RegisterView(ApiService apiService, AuthService authService) {
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
        panel.addClassName("register-left-panel");
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
        
        Paragraph tagline = new Paragraph("Join the TodoApp Community");
        tagline.getStyle().set("font-size", "1.2em");
        tagline.getStyle().set("margin-top", "-15px");
        
        VerticalLayout benefitsLayout = new VerticalLayout();
        benefitsLayout.setSpacing(false);
        benefitsLayout.setPadding(false);
        benefitsLayout.setAlignItems(FlexComponent.Alignment.START);
        
        Span benefit1 = new Span("✓ Sync across all your devices");
        Span benefit2 = new Span("✓ Never lose your tasks again");
        Span benefit3 = new Span("✓ Beautiful, intuitive interface");
        Span benefit4 = new Span("✓ Completely free to use");
        
        benefitsLayout.add(benefit1, benefit2, benefit3, benefit4);
        
        panel.add(title, tagline, benefitsLayout);
        
        return panel;
    }
    
    private VerticalLayout createRightPanel() {
        VerticalLayout panel = new VerticalLayout();
        panel.addClassName("register-right-panel");
        panel.setHeightFull();
        panel.setWidth("60%");
        panel.setAlignItems(FlexComponent.Alignment.CENTER);
        panel.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        panel.setSpacing(true);
        
        VerticalLayout formContainer = new VerticalLayout();
        formContainer.setMaxWidth("400px");
        formContainer.setAlignItems(FlexComponent.Alignment.STRETCH);
        
        H3 header = new H3("Create Account");
        Paragraph subheader = new Paragraph("Sign up to get started");
        subheader.getStyle().set("color", "var(--lumo-secondary-text-color)");
        subheader.getStyle().set("margin-top", "-10px");
        
        FormLayout registerForm = new FormLayout();
        
        usernameField = new TextField("Username");
        usernameField.setRequired(true);
        
        emailField = new EmailField("Email");
        emailField.setRequired(true);
        
        passwordField = new PasswordField("Password");
        passwordField.setRequired(true);
        
        confirmPasswordField = new PasswordField("Confirm Password");
        confirmPasswordField.setRequired(true);
        
        Button registerButton = new Button("Create Account");
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidthFull();
        registerButton.addClickListener(e -> register());
        
        registerForm.add(usernameField, emailField, passwordField, confirmPasswordField, registerButton);
        
        HorizontalLayout loginLayout = new HorizontalLayout();
        loginLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        loginLayout.setWidthFull();
        
        Span loginText = new Span("Already have an account?");
        RouterLink loginLink = new RouterLink("Login", LoginView.class);
        
        loginLayout.add(loginText, loginLink);
        
        formContainer.add(header, subheader, registerForm, loginLayout);
        panel.add(formContainer);
        
        return panel;
    }
    
    private void register() {
        String username = usernameField.getValue().trim();
        String email = emailField.getValue().trim();
        String password = passwordField.getValue();
        String confirmPassword = confirmPasswordField.getValue();
        
        // Validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Notification.show("Please fill in all fields");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            Notification.show("Passwords do not match");
            return;
        }
        
        if (password.length() < 6) {
            Notification.show("Password must be at least 6 characters long");
            return;
        }
        
        try {
            ApiService.AuthResponse response = apiService.register(username, email, password);
            authService.setAuthenticated(response.getToken(), response.getUser());
            
            Notification.show("Account created successfully! Welcome to TodoApp!");
            UI.getCurrent().navigate("");
        } catch (Exception e) {
            log.error("Registration failed", e);
            Notification.show("Registration failed: " + e.getMessage());
        }
    }
}