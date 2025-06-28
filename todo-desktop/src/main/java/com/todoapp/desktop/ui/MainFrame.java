package com.todoapp.desktop.ui;

import com.todoapp.common.model.User;
import com.todoapp.desktop.service.ThemeManager;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window.
 */
public class MainFrame extends JFrame {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 700;
    
    private final JPanel contentPanel;
    
    @Getter @Setter
    private String authToken;
    
    @Getter @Setter
    private User currentUser;
    
    public MainFrame() {
        setTitle("TodoApp");
        setSize(WIDTH, HEIGHT);
        setMinimumSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Set the application icon
        ImageIcon icon = new ImageIcon(getClass().getResource("/icons/app_icon.png"));
        setIconImage(icon.getImage());
        
        // Initialize the content panel
        contentPanel = new JPanel(new BorderLayout());
        setContentPane(contentPanel);
        
        // Initialize theme manager
        ThemeManager.initialize();
    }
    
    /**
     * Set the main panel of the application.
     */
    public void setPanel(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    /**
     * Set the theme based on user preference.
     */
    public void setTheme(User.ThemePreference preference) {
        ThemeManager.setTheme(preference);
        SwingUtilities.updateComponentTreeUI(this);
    }
}