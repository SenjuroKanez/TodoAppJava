package com.todoapp.desktop;

import com.formdev.flatlaf.FlatLightLaf;
import com.todoapp.desktop.ui.MainFrame;
import com.todoapp.desktop.ui.auth.LoginPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Main application class for the Todo desktop client.
 */
public class TodoDesktopApplication {
    public static void main(String[] args) {
        // Set the look and feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }
        
        // Enable macOS-specific features if running on macOS
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.application.name", "TodoApp");
            System.setProperty("apple.awt.application.appearance", "system");
        }
        
        // Start the application on the EDT
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setPanel(new LoginPanel(mainFrame));
            mainFrame.setVisible(true);
            
            // Center the frame on the screen
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int x = (screenSize.width - mainFrame.getWidth()) / 2;
            int y = (screenSize.height - mainFrame.getHeight()) / 2;
            mainFrame.setLocation(x, y);
        });
    }
}