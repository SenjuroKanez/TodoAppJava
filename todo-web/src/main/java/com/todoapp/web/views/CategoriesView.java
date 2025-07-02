package com.todoapp.web.views;

import com.todoapp.common.model.Category;
import com.todoapp.web.service.ApiService;
import com.todoapp.web.service.AuthService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * View for managing categories.
 */
@Route(value = "categories", layout = MainLayout.class)
@PageTitle("Categories")
@Slf4j
public class CategoriesView extends VerticalLayout {
    private final ApiService apiService;
    private final AuthService authService;
    
    private Grid<Category> grid;
    private List<Category> categories = new ArrayList<>();
    
    public CategoriesView(ApiService apiService, AuthService authService) {
        this.apiService = apiService;
        this.authService = authService;
        
        if (!authService.isAuthenticated()) {
            getUI().ifPresent(ui -> ui.navigate("login"));
            return;
        }
        
        addClassName("categories-view");
        setSizeFull();
        setPadding(true);
        
        add(createHeader());
        add(createGrid());
        add(createAddButton());
        
        loadCategories();
    }
    
    private Component createHeader() {
        H3 header = new H3("Categories");
        header.addClassName("view-header");
        
        return header;
    }
    
    private Grid<Category> createGrid() {
        grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeightFull();
        
        grid.addColumn(new ComponentRenderer<>(this::createCategoryCard))
                .setAutoWidth(true)
                .setFlexGrow(1);
        
        return grid;
    }
    
    private Component createCategoryCard(Category category) {
        HorizontalLayout cardLayout = new HorizontalLayout();
        cardLayout.addClassName("category-card");
        cardLayout.setWidthFull();
        cardLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        cardLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        cardLayout.setPadding(true);
        
        // Category info
        HorizontalLayout infoLayout = new HorizontalLayout();
        infoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        
        // Color indicator
        Div colorIndicator = new Div();
        colorIndicator.setWidth("20px");
        colorIndicator.setHeight("20px");
        colorIndicator.getStyle().set("background-color", category.getColor());
        colorIndicator.getStyle().set("border-radius", "50%");
        colorIndicator.getStyle().set("margin-right", "10px");
        
        Span nameSpan = new Span(category.getName());
        nameSpan.getStyle().set("font-weight", "bold");
        
        infoLayout.add(colorIndicator, nameSpan);
        
        // Action buttons
        HorizontalLayout actions = new HorizontalLayout();
        
        Button editButton = new Button(new Icon(VaadinIcon.EDIT));
        editButton.addClassName("edit-button");
        editButton.addClickListener(e -> showEditDialog(category));
        
        Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
        deleteButton.addClassName("delete-button");
        deleteButton.addClickListener(e -> deleteCategory(category));
        
        actions.add(editButton, deleteButton);
        
        cardLayout.add(infoLayout, actions);
        
        return cardLayout;
    }
    
    private Component createAddButton() {
        Button addButton = new Button("Add Category", new Icon(VaadinIcon.PLUS));
        addButton.addClassName("add-category-button");
        addButton.addClickListener(e -> showAddDialog());
        
        return addButton;
    }
    
    private void loadCategories() {
        try {
            categories = apiService.getAllCategories();
            grid.setItems(categories);
        } catch (Exception e) {
            log.error("Error loading categories", e);
            Notification.show("Error loading categories: " + e.getMessage());
        }
    }
    
    private void showAddDialog() {
        CategoryDialog dialog = new CategoryDialog(apiService, null);
        dialog.addOpenedChangeListener(event -> {
            if (!event.isOpened() && dialog.isSaved()) {
                loadCategories();
            }
        });
        dialog.open();
    }
    
    private void showEditDialog(Category category) {
        CategoryDialog dialog = new CategoryDialog(apiService, category);
        dialog.addOpenedChangeListener(event -> {
            if (!event.isOpened() && dialog.isSaved()) {
                loadCategories();
            }
        });
        dialog.open();
    }
    
    private void deleteCategory(Category category) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirm Delete");
        
        Div content = new Div();
        content.setText("Are you sure you want to delete this category? This action cannot be undone.");
        confirmDialog.add(content);
        
        Button cancelButton = new Button("Cancel", e -> confirmDialog.close());
        Button deleteButton = new Button("Delete", e -> {
            try {
                // Note: In a real application, you would have a delete category API endpoint
                Notification.show("Category deletion not implemented in demo");
                confirmDialog.close();
            } catch (Exception ex) {
                log.error("Error deleting category", ex);
                Notification.show("Error deleting category: " + ex.getMessage());
            }
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        
        confirmDialog.getFooter().add(cancelButton, deleteButton);
        confirmDialog.open();
    }
    
    /**
     * Dialog for adding or editing a category.
     */
    private static class CategoryDialog extends Dialog {
        private final ApiService apiService;
        private final Category existingCategory;
        
        private TextField nameField;
        private ComboBox<String> colorComboBox;
        
        private boolean saved = false;
        
        public CategoryDialog(ApiService apiService, Category existingCategory) {
            this.apiService = apiService;
            this.existingCategory = existingCategory;
            
            setHeaderTitle(existingCategory == null ? "Add New Category" : "Edit Category");
            setWidth("400px");
            
            add(createForm());
            
            Button cancelButton = new Button("Cancel", e -> close());
            Button saveButton = new Button(existingCategory == null ? "Add Category" : "Save Changes", e -> saveCategory());
            saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            
            getFooter().add(cancelButton, saveButton);
        }
        
        private VerticalLayout createForm() {
            VerticalLayout layout = new VerticalLayout();
            layout.setSpacing(true);
            layout.setPadding(false);
            
            // Name field
            nameField = new TextField("Name");
            nameField.setWidthFull();
            nameField.setRequired(true);
            
            // Color field
            colorComboBox = new ComboBox<>("Color");
            colorComboBox.setItems("Red", "Orange", "Yellow", "Green", "Blue", "Purple", "Pink");
            colorComboBox.setWidthFull();
            colorComboBox.setValue("Blue");
            
            // Fill values if editing
            if (existingCategory != null) {
                nameField.setValue(existingCategory.getName());
                colorComboBox.setValue(getColorName(existingCategory.getColor()));
            }
            
            layout.add(nameField, colorComboBox);
            
            return layout;
        }
        
        private String getColorName(String colorHex) {
            switch (colorHex) {
                case Category.COLOR_RED: return "Red";
                case Category.COLOR_ORANGE: return "Orange";
                case Category.COLOR_YELLOW: return "Yellow";
                case Category.COLOR_GREEN: return "Green";
                case Category.COLOR_PURPLE: return "Purple";
                case Category.COLOR_PINK: return "Pink";
                case Category.COLOR_BLUE:
                default: return "Blue";
            }
        }
        
        private String getColorHex(String colorName) {
            switch (colorName) {
                case "Red": return Category.COLOR_RED;
                case "Orange": return Category.COLOR_ORANGE;
                case "Yellow": return Category.COLOR_YELLOW;
                case "Green": return Category.COLOR_GREEN;
                case "Purple": return Category.COLOR_PURPLE;
                case "Pink": return Category.COLOR_PINK;
                case "Blue":
                default: return Category.COLOR_BLUE;
            }
        }
        
        private void saveCategory() {
            String name = nameField.getValue().trim();
            if (name.isEmpty()) {
                Notification.show("Category name is required");
                return;
            }
            
            try {
                Category category = Category.builder()
                        .id(existingCategory != null ? existingCategory.getId() : null)
                        .name(name)
                        .color(getColorHex(colorComboBox.getValue()))
                        .build();
                
                apiService.createCategory(category);
                
                saved = true;
                close();
                Notification.show(existingCategory == null ? "Category added" : "Category updated");
            } catch (Exception e) {
                log.error("Error saving category", e);
                Notification.show("Error saving category: " + e.getMessage());
            }
        }
        
        public boolean isSaved() {
            return saved;
        }
    }
}