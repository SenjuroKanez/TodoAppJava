package com.todoapp.desktop.ui.auth;

import com.todoapp.common.model.User;
import com.todoapp.desktop.service.ApiService;
import com.todoapp.desktop.ui.MainFrame;
import com.todoapp.desktop.ui.dashboard.DashboardPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * Panel for user registration.
 */
public class RegisterPanel extends JPanel {
    private final MainFrame mainFrame;
    
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JButton loginButton;
    
    public RegisterPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        
        setLayout(new MigLayout("fill, insets 0"));
        
        initializeUI();
    }
    
    private void initializeUI() {
        // Create main container with two panels (left for logo, right for register form)
        JPanel container = new JPanel(new MigLayout("fill, insets 0", "[50%][50%]"));
        
        // Left panel for logo/branding
        JPanel brandPanel = createBrandPanel();
        container.add(brandPanel, "cell 0 0, grow");
        
        // Right panel for register form
        JPanel formPanel = createFormPanel();
        container.add(formPanel, "cell 1 0, grow");
        
        add(container, "grow");
    }
    
    private JPanel createBrandPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 0"));
        panel.setBackground(new Color(118, 143, 255));
        
        JPanel contentPanel = new JPanel(new MigLayout("fill, insets 50"));
        contentPanel.setOpaque(false);
        
        // App logo
        JLabel logoLabel = new JLabel("TodoApp");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 36));
        logoLabel.setForeground(Color.WHITE);
        contentPanel.add(logoLabel, "top, center, wrap");
        
        // Tagline
        JLabel taglineLabel = new JLabel("Join the TodoApp Community");
        taglineLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        taglineLabel.setForeground(Color.WHITE);
        contentPanel.add(taglineLabel, "top, center, gaptop 10, wrap");
        
        // Add some benefits list
        JPanel benefitsPanel = new JPanel(new MigLayout("fillx, wrap 1, insets 0", "[]"));
        benefitsPanel.setOpaque(false);
        
        String[] benefits = {
            "✓ Sync across all your devices",
            "✓ Never lose your tasks again",
            "✓ Beautiful, intuitive interface",
            "✓ Completely free to use"
        };
        
        for (String benefit : benefits) {
            JLabel benefitLabel = new JLabel(benefit);
            benefitLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            benefitLabel.setForeground(Color.WHITE);
            benefitsPanel.add(benefitLabel, "gaptop 15");
        }
        
        contentPanel.add(benefitsPanel, "center, gaptop 50");
        
        panel.add(contentPanel, "grow");
        
        return panel;
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 0"));
        panel.setBackground(Color.WHITE);
        
        JPanel formContainer = new JPanel(new MigLayout("fillx, wrap 1, insets 50", "[grow]"));
        formContainer.setOpaque(false);
        
        // Title
        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        formContainer.add(titleLabel, "gaptop 30");
        
        JLabel subtitleLabel = new JLabel("Sign up to get started");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setForeground(Color.GRAY);
        formContainer.add(subtitleLabel, "gaptop 5, wrap 30");
        
        // Username field
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        formContainer.add(usernameLabel);
        
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        formContainer.add(usernameField, "growx, wrap 15");
        
        // Email field
        JLabel emailLabel = new JLabel("Email");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        formContainer.add(emailLabel);
        
        emailField = new JTextField(20);
        emailField.setFont(new Font("Arial", Font.PLAIN, 14));
        formContainer.add(emailField, "growx, wrap 15");
        
        // Password field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        formContainer.add(passwordLabel);
        
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        formContainer.add(passwordField, "growx, wrap 15");
        
        // Confirm Password field
        JLabel confirmPasswordLabel = new JLabel("Confirm Password");
        confirmPasswordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        formContainer.add(confirmPasswordLabel);
        
        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 14));
        formContainer.add(confirmPasswordField, "growx, wrap 25");
        
        // Register button
        registerButton = new JButton("Create Account");
        registerButton.setFont(new Font("Arial", Font.BOLD, 14));
        registerButton.setBackground(new Color(118, 143, 255));
        registerButton.setForeground(Color.WHITE);
        registerButton.addActionListener(this::handleRegister);
        formContainer.add(registerButton, "growx, height 40!, wrap 15");
        
        // Login link
        JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        loginPanel.setOpaque(false);
        
        JLabel loginLabel = new JLabel("Already have an account?");
        loginLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        loginPanel.add(loginLabel);
        
        loginButton = new JButton("Login");
        loginButton.setBorderPainted(false);
        loginButton.setContentAreaFilled(false);
        loginButton.setForeground(new Color(118, 143, 255));
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.addActionListener(e -> showLoginPanel());
        loginPanel.add(loginButton);
        
        formContainer.add(loginPanel, "center");
        
        panel.add(formContainer, "grow");
        
        return panel;
    }
    
    private void handleRegister(ActionEvent e) {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        // Validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "Please fill in all fields.", 
                    "Registration Error", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, 
                    "Passwords do not match.", 
                    "Registration Error", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, 
                    "Password must be at least 6 characters long.", 
                    "Registration Error", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, 
                    "Please enter a valid email address.", 
                    "Registration Error", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        registerButton.setEnabled(false);
        registerButton.setText("Creating Account...");
        
        // Use SwingWorker to perform the registration in a background thread
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private User user;
            private String token;
            private String errorMessage;
            
            @Override
            protected Void doInBackground() {
                try {
                    // In a real app, this would be a server URL
                    ApiService apiService = new ApiService("http://localhost:8080", null);
                    user = apiService.register(username, email, password);
                    token = "dummy-token"; // In a real app, this would come from the server
                } catch (IOException ex) {
                    errorMessage = "Registration failed: " + ex.getMessage();
                }
                return null;
            }
            
            @Override
            protected void done() {
                registerButton.setEnabled(true);
                registerButton.setText("Create Account");
                
                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(RegisterPanel.this, 
                            errorMessage, 
                            "Registration Error", 
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Show success message
                JOptionPane.showMessageDialog(RegisterPanel.this, 
                        "Account created successfully! You are now logged in.", 
                        "Registration Successful", 
                        JOptionPane.INFORMATION_MESSAGE);
                
                // Store the authentication token and user
                mainFrame.setAuthToken(token);
                mainFrame.setCurrentUser(user);
                
                // Set theme based on user preference
                mainFrame.setTheme(user.getThemePreference());
                
                // Navigate to the main dashboard
                mainFrame.setPanel(new DashboardPanel(mainFrame));
            }
        };
        
        worker.execute();
    }
    
    private boolean isValidEmail(String email) {
        // Simple email validation
        return email.contains("@") && email.contains(".") && email.length() > 5;
    }
    
    private void showLoginPanel() {
        mainFrame.setPanel(new LoginPanel(mainFrame));
    }
}