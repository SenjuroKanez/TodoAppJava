package com.todoapp.desktop.ui.dashboard;

import com.todoapp.desktop.service.ApiService;
import com.todoapp.desktop.ui.MainFrame;
import com.todoapp.desktop.ui.components.TodoListPanel;
import net.miginfocom.swing.MigLayout;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

/**
 * Main dashboard panel shown after login.
 * This panel serves as the primary interface after user authentication.
 */
@Slf4j
public class DashboardPanel extends JPanel {
    private final MainFrame mainFrame;
    private ApiService apiService; // Removed final
    private TodoListPanel todoListPanel;

    public DashboardPanel(MainFrame mainFrame) {
        if (mainFrame == null) {
            throw new IllegalArgumentException("MainFrame cannot be null");
        }

        this.mainFrame = mainFrame;
        
        // Validate authentication token
        String authToken = mainFrame.getAuthToken();
        if (authToken == null || authToken.trim().isEmpty()) {
            log.error("Authentication token is missing");
            this.apiService = null; // Initialize to null
            showError("Authentication error", "Please log in again");
            mainFrame.setPanel(new com.todoapp.desktop.ui.auth.LoginPanel(mainFrame));
            return;
        }

        // Initialize apiService with valid token
        this.apiService = new ApiService("http://localhost:8080", authToken);

        try {
            initializeUI();
        } catch (Exception e) {
            log.error("Failed to initialize dashboard UI", e);
            showError("Initialization Error", "Failed to load dashboard. Please try again.");
            mainFrame.setPanel(new com.todoapp.desktop.ui.auth.LoginPanel(mainFrame));
        }
    }

    private void initializeUI() {
        // Set up the layout
        setLayout(new MigLayout("fill, insets 0", "[grow]", "[grow]"));

        // Create the main content panel
        JPanel contentPanel = new JPanel(new MigLayout("fill, insets 20", "[grow]", "[]10[grow]"));
        contentPanel.setBackground(new Color(245, 247, 250));

        // Add welcome message with null check for user
        String username = mainFrame.getCurrentUser() != null ? 
            mainFrame.getCurrentUser().getUsername() : "Guest";
        JLabel welcomeLabel = new JLabel("Welcome, " + username);
        welcomeLabel.setFont(welcomeLabel.getFont().deriveFont(Font.BOLD, 24f));
        contentPanel.add(welcomeLabel, "wrap");

        try {
            // Create and add the todo list panel - add null check for apiService
            if (apiService != null) {
                todoListPanel = new TodoListPanel(mainFrame, apiService);
                contentPanel.add(todoListPanel, "grow");
            } else {
                JLabel errorLabel = new JLabel("Service unavailable. Please log in again.");
                errorLabel.setForeground(Color.RED);
                contentPanel.add(errorLabel, "grow");
            }
        } catch (Exception e) {
            log.error("Failed to create TodoListPanel", e);
            JLabel errorLabel = new JLabel("Failed to load tasks. Please refresh the page.");
            errorLabel.setForeground(Color.RED);
            contentPanel.add(errorLabel, "grow");
        }

        // Add the content panel to the dashboard
        add(contentPanel, "grow");

        // Set background color
        setBackground(new Color(245, 247, 250));

        // Add refresh functionality
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshDashboard());
        contentPanel.add(refreshButton, "dock south, alignx right, gaptop 10");
    }

    private void refreshDashboard() {
        try {
            if (todoListPanel != null && apiService != null) { // Add null check for apiService
                remove(todoListPanel);
                todoListPanel = new TodoListPanel(mainFrame, apiService);
                add(todoListPanel, "grow");
                revalidate();
                repaint();
            }
        } catch (Exception e) {
            log.error("Failed to refresh dashboard", e);
            showError("Refresh Error", "Failed to refresh dashboard. Please try again.");
        }
    }

    private void showError(String title, String message) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(this, 
                message, 
                title, 
                JOptionPane.ERROR_MESSAGE)
        );
    }
}