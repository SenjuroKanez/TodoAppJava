package com.todoapp.desktop.service;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.todoapp.common.model.User;

import javax.swing.*;
import java.awt.*;

/**
 * Manages application theming.
 */
public class ThemeManager {
    private static boolean initialized = false;
    
    public static void initialize() {
        if (initialized) {
            return;
        }
        
        // Register custom colors
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("ProgressBar.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        
        // Set custom accent color
        UIManager.put("Component.focusColor", new Color(118, 143, 255));
        UIManager.put("Button.default.focusColor", new Color(118, 143, 255));
        
        initialized = true;
    }
    
    public static void setTheme(User.ThemePreference preference) {
        try {
            switch (preference) {
                case DARK:
                    UIManager.setLookAndFeel(new FlatDarkLaf());
                    break;
                    
                case LIGHT:
                case SYSTEM:
                default:
                    UIManager.setLookAndFeel(new FlatLightLaf());
                    break;
            }
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }
}