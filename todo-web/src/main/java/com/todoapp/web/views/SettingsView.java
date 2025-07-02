package com.todoapp.web.views;

import com.todoapp.common.model.User;
import com.todoapp.web.service.AuthService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;

/**
 * View for user settings and preferences.
 */
@Route(value = "settings", layout = MainLayout.class)
@PageTitle("Settings")
@Slf4j
public class SettingsView extends VerticalLayout {
    private final AuthService authService;
    
    private TextField usernameField;
    private EmailField emailField;
    private ComboBox<User.ThemePreference> themeComboBox;
    
    public SettingsView(AuthService authService) {
        this.authService = authService;
        
        if (!authService.isAuthenticated()) {
            getUI().ifPresent(ui -> ui.navigate("login"));
            return;
        }
        
        addClassName("settings-view");
        setSizeFull();
        setPadding(true);
        
        add(createHeader());
        add(createProfileSection());
        add(new Hr());
        add(createPreferencesSection());
        add(new Hr());
        add(createAccountSection());
    }
    
    private Component createHeader() {
        H3 header = new H3("Settings");
        header.addClassName("view-header");
        
        return header;
    }
    
    private Component createProfileSection() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(false);
        
        H4 sectionTitle = new H4("Profile Information");
        section.add(sectionTitle);
        
        FormLayout formLayout = new FormLayout();
        
        // Username field
        usernameField = new TextField("Username");
        usernameField.setReadOnly(true); // Username typically can't be changed
        
        // Email field
        emailField = new EmailField("Email");
        emailField.setReadOnly(true); // Email typically can't be changed in demo
        
        // Fill current user data
        User currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            usernameField.setValue(currentUser.getUsername());
            emailField.setValue(currentUser.getEmail());
        }
        
        formLayout.add(usernameField, emailField);
        
        Button updateProfileButton = new Button("Update Profile");
        updateProfileButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        updateProfileButton.addClickListener(e -> {
            Notification.show("Profile update not implemented in demo");
        });
        
        section.add(formLayout, updateProfileButton);
        
        return section;
    }
    
    private Component createPreferencesSection() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(false);
        
        H4 sectionTitle = new H4("Preferences");
        section.add(sectionTitle);
        
        FormLayout formLayout = new FormLayout();
        
        // Theme preference
        themeComboBox = new ComboBox<>("Theme");
        themeComboBox.setItems(User.ThemePreference.values());
        themeComboBox.setItemLabelGenerator(theme -> {
            switch (theme) {
                case LIGHT: return "Light";
                case DARK: return "Dark";
                case SYSTEM: return "System Default";
                default: return theme.toString();
            }
        });
        
        // Set current theme
        User currentUser = authService.getCurrentUser();
        if (currentUser != null && currentUser.getThemePreference() != null) {
            themeComboBox.setValue(currentUser.getThemePreference());
        } else {
            themeComboBox.setValue(User.ThemePreference.LIGHT);
        }
        
        themeComboBox.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                // In a real app, this would update the user's preference in the backend
                Notification.show("Theme changed to " + e.getValue());
                // Apply theme change (this would require additional implementation)
            }
        });
        
        formLayout.add(themeComboBox);
        
        section.add(formLayout);
        
        return section;
    }
    
    private Component createAccountSection() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(false);
        
        H4 sectionTitle = new H4("Account");
        section.add(sectionTitle);
        
        Paragraph accountInfo = new Paragraph("Manage your account settings and data.");
        section.add(accountInfo);
        
        Button logoutButton = new Button("Logout");
        logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        logoutButton.addClickListener(e -> {
            authService.logout();
            getUI().ifPresent(ui -> ui.navigate("login"));
            Notification.show("Logged out successfully");
        });
        
        Button deleteAccountButton = new Button("Delete Account");
        deleteAccountButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        deleteAccountButton.addClickListener(e -> {
            Notification.show("Account deletion not implemented in demo");
        });
        
        section.add(logoutButton, deleteAccountButton);
        
        return section;
    }
}