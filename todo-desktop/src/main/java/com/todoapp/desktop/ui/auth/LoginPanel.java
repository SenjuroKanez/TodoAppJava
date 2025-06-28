package com.todoapp.desktop.ui.auth;

import com.todoapp.common.model.User;
import com.todoapp.desktop.service.ApiService;
import com.todoapp.desktop.ui.MainFrame;
// import com.todoapp.desktop.ui.dashboard.DashboardPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * Panel for user login.
 */
public class LoginPanel extends JPanel {
    private final MainFrame mainFrame;
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    
    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        
        setLayout(new MigLayout("fill, insets 0"));
        
        initializeUI();
    }
    
    private void initializeUI() {
        // Create main container with two panels (left for logo, right for login form)
        JPanel container = new JPanel(new MigLayout("fill, insets 0", "[50%][50%]"));
        
        // Left panel for logo/branding
        JPanel brandPanel = createBrandPanel();
        container.add(brandPanel, "cell 0 0, grow");
        
        // Right panel for login form
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
        JLabel taglineLabel = new JLabel("Beautifully Simple Task Management");
        taglineLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        taglineLabel.setForeground(Color.WHITE);
        contentPanel.add(taglineLabel, "top, center, gaptop 10, wrap");
        
        // Add some illustrations or features list
        JPanel featuresPanel = new JPanel(new MigLayout("fillx, wrap 1, insets 0", "[]"));
        featuresPanel.setOpaque(false);
        
        String[] features = {
            "✓ Organize tasks with categories",
            "✓ Set priorities and due dates",
            "✓ Sync across devices",
            "✓ Light and dark themes"
        };
        
        for (String feature : features) {
            JLabel featureLabel = new JLabel(feature);
            featureLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            featureLabel.setForeground(Color.WHITE);
            featuresPanel.add(featureLabel, "gaptop 15");
        }
        
        contentPanel.add(featuresPanel, "center, gaptop 50");
        
        panel.add(contentPanel, "grow");
        
        return panel;
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 0"));
        panel.setBackground(Color.WHITE);
        
        JPanel formContainer = new JPanel(new MigLayout("fillx, wrap 1, insets 50", "[grow]"));
        formContainer.setOpaque(false);
        
        // Title
        JLabel titleLabel = new JLabel("Welcome Back");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        formContainer.add(titleLabel, "gaptop 30");
        
        JLabel subtitleLabel = new JLabel("Login to your account");
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
        
        // Password field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        formContainer.add(passwordLabel);
        
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        formContainer.add(passwordField, "growx, wrap 25");
        
        // Login button
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBackground(new Color(118, 143, 255));
        loginButton.setForeground(Color.WHITE);
        loginButton.addActionListener(this::handleLogin);
        formContainer.add(loginButton, "growx, height 40!, wrap 15");
        
        // Register link
        JPanel registerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        registerPanel.setOpaque(false);
        
        JLabel registerLabel = new JLabel("Don't have an account?");
        registerLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        registerPanel.add(registerLabel);
        
        registerButton = new JButton("Register");
        registerButton.setBorderPainted(false);
        registerButton.setContentAreaFilled(false);
        registerButton.setForeground(new Color(118, 143, 255));
        registerButton.setFont(new Font("Arial", Font.BOLD, 14));
        registerButton.addActionListener(e -> showRegisterPanel());
        registerPanel.add(registerButton);
        
        formContainer.add(registerPanel, "center");
        
        // Demo mode button
        JButton demoButton = new JButton("Try Demo Mode");
        demoButton.setFont(new Font("Arial", Font.PLAIN, 12));
        demoButton.setBorderPainted(false);
        demoButton.setContentAreaFilled(false);
        demoButton.setForeground(Color.GRAY);
        demoButton.addActionListener(e -> loginWithDemo());
        formContainer.add(demoButton, "center, gaptop 30");
        
        panel.add(formContainer, "grow");
        
        return panel;
    }
    
    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "Please enter both username and password.", 
                    "Login Error", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");
        
        // Use SwingWorker to perform the login in a background thread
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private User user;
            private String token;
            private String errorMessage;
            
            @Override
            protected Void doInBackground() {
                try {
                    // In a real app, this would be a server URL
                    ApiService apiService = new ApiService("http://localhost:8080", null);
                    user = apiService.login(username, password);
                    token = "dummy-token"; // In a real app, this would come from the server
                } catch (IOException ex) {
                    errorMessage = "Login failed: " + ex.getMessage();
                }
                return null;
            }
            
            @Override
            protected void done() {
                loginButton.setEnabled(true);
                loginButton.setText("Login");
                
                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(LoginPanel.this, 
                            errorMessage, 
                            "Login Error", 
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Store the authentication token and user
                mainFrame.setAuthToken(token);
                mainFrame.setCurrentUser(user);
                
                // Set theme based on user preference
                mainFrame.setTheme(user.getThemePreference());
                
                // Navigate to the main dashboard
                // mainFrame.setPanel(new DashboardPanel(mainFrame)); // TODO: Implement dashboard panel or fix import
            }
        };
        
        worker.execute();
    }
    
    private void showRegisterPanel() {
        // mainFrame.setPanel(new RegisterPanel(mainFrame)); // TODO: Implement RegisterPanel or fix import
    }
    
    private void loginWithDemo() {
        // Create a demo user and go to dashboard
        User demoUser = User.builder()
                .id(java.util.UUID.randomUUID())
                .username("Demo User")
                .email("demo@example.com")
                .themePreference(User.ThemePreference.LIGHT)
                .build();
        
        mainFrame.setAuthToken("demo-token");
        mainFrame.setCurrentUser(demoUser);
        
        // Navigate to the main dashboard
        // mainFrame.setPanel(new DashboardPanel(mainFrame)); // TODO: Implement dashboard panel or fix import
    }
}