package com.todoapp.web.views;

import com.todoapp.common.dto.TodoDto;
import com.todoapp.common.model.Category;
import com.todoapp.common.model.Todo;
import com.todoapp.web.service.ApiService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Dialog for adding or editing a todo.
 */
@Slf4j
public class TodoDialog extends Dialog {
    private final ApiService apiService;
    private final TodoDto existingTodo;
    
    private TextField titleField;
    private TextArea descriptionArea;
    private ComboBox<Todo.Priority> priorityComboBox;
    private ComboBox<Category> categoryComboBox;
    private Checkbox hasDateCheckbox;
    private DatePicker dueDatePicker;
    
    @Getter
    private boolean saved = false;
    
    public TodoDialog(ApiService apiService, TodoDto existingTodo) {
        this.apiService = apiService;
        this.existingTodo = existingTodo;
        
        setHeaderTitle(existingTodo == null ? "Add New Task" : "Edit Task");
        setWidth("500px");
        
        add(createForm());
        
        Button cancelButton = new Button("Cancel", e -> close());
        Button saveButton = new Button(existingTodo == null ? "Add Task" : "Save Changes", e -> saveTask());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        getFooter().add(cancelButton, saveButton);
    }
    
    private VerticalLayout createForm() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);
        
        // Title field
        titleField = new TextField("Title");
        titleField.setWidthFull();
        titleField.setRequired(true);
        
        // Description field
        descriptionArea = new TextArea("Description");
        descriptionArea.setWidthFull();
        descriptionArea.setHeight("100px");
        
        // Priority field
        priorityComboBox = new ComboBox<>("Priority");
        priorityComboBox.setItems(Todo.Priority.values());
        priorityComboBox.setWidthFull();
        
        // Category field with New Category button
        HorizontalLayout categoryLayout = new HorizontalLayout();
        categoryLayout.setWidthFull();
        categoryLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        
        categoryComboBox = new ComboBox<>("Category");
        categoryComboBox.setWidthFull();
        categoryComboBox.setItemLabelGenerator(Category::getName);
        loadCategories();
        
        Button newCategoryButton = new Button("New", e -> showNewCategoryDialog());
        newCategoryButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        
        categoryLayout.add(categoryComboBox, newCategoryButton);
        categoryLayout.expand(categoryComboBox);
        
        // Due date field
        HorizontalLayout dateLayout = new HorizontalLayout();
        dateLayout.setWidthFull();
        dateLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        
        hasDateCheckbox = new Checkbox("Set Due Date");
        
        dueDatePicker = new DatePicker();
        dueDatePicker.setWidthFull();
        dueDatePicker.setEnabled(false);
        
        hasDateCheckbox.addValueChangeListener(e -> dueDatePicker.setEnabled(e.getValue()));
        
        dateLayout.add(hasDateCheckbox, dueDatePicker);
        dateLayout.expand(dueDatePicker);
        
        // Fill values if editing
        if (existingTodo != null) {
            titleField.setValue(existingTodo.getTitle());
            
            if (existingTodo.getDescription() != null) {
                descriptionArea.setValue(existingTodo.getDescription());
            }
            
            if (existingTodo.getPriority() != null) {
                priorityComboBox.setValue(existingTodo.getPriority());
            }
            
            if (existingTodo.getCategory() != null) {
                categoryComboBox.setValue(existingTodo.getCategory());
            }
            
            if (existingTodo.getDueDate() != null) {
                hasDateCheckbox.setValue(true);
                dueDatePicker.setValue(existingTodo.getDueDate().toLocalDate());
                dueDatePicker.setEnabled(true);
            }
        }
        
        layout.add(titleField, descriptionArea, priorityComboBox, categoryLayout, dateLayout);
        
        return layout;
    }
    
    private void loadCategories() {
        try {
            List<Category> categories = apiService.getAllCategories();
            categoryComboBox.setItems(categories);
        } catch (Exception e) {
            log.error("Error loading categories", e);
            Notification.show("Error loading categories: " + e.getMessage());
        }
    }
    
    private void showNewCategoryDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("New Category");
        
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);
        
        TextField nameField = new TextField("Name");
        nameField.setWidthFull();
        nameField.setRequired(true);
        
        ComboBox<String> colorComboBox = new ComboBox<>("Color");
        colorComboBox.setItems(
                "Red", "Orange", "Yellow", "Green", "Blue", "Purple", "Pink"
        );
        colorComboBox.setWidthFull();
        colorComboBox.setValue("Blue");
        
        layout.add(nameField, colorComboBox);
        dialog.add(layout);
        
        Button cancelButton = new Button("Cancel", e -> dialog.close());
        Button saveButton = new Button("Create", e -> {
            String name = nameField.getValue().trim();
            if (name.isEmpty()) {
                Notification.show("Category name is required");
                return;
            }
            
            String color = getColorHex(colorComboBox.getValue());
            
            try {
                Category newCategory = Category.builder()
                        .name(name)
                        .color(color)
                        .build();
                
                Category savedCategory = apiService.createCategory(newCategory);
                loadCategories();
                categoryComboBox.setValue(savedCategory);
                dialog.close();
            } catch (Exception ex) {
                log.error("Error creating category", ex);
                Notification.show("Error creating category: " + ex.getMessage());
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
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
    
    private void saveTask() {
        String title = titleField.getValue().trim();
        if (title.isEmpty()) {
            Notification.show("Title is required");
            return;
        }
        
        try {
            TodoDto todoDto = new TodoDto();
            
            if (existingTodo != null) {
                todoDto.setId(existingTodo.getId());
            }
            
            todoDto.setTitle(title);
            todoDto.setDescription(descriptionArea.getValue().trim());
            todoDto.setPriority(priorityComboBox.getValue());
            todoDto.setCategory(categoryComboBox.getValue());
            
            // Handle due date
            if (hasDateCheckbox.getValue() && dueDatePicker.getValue() != null) {
                LocalDate localDate = dueDatePicker.getValue();
                // Set the time to end of day (11:59 PM)
                LocalDateTime dueDateTime = LocalDateTime.of(localDate, LocalTime.of(23, 59));
                todoDto.setDueDate(dueDateTime);
            }
            
            if (existingTodo == null) {
                apiService.createTodo(todoDto);
                Notification.show("Task added");
            } else {
                apiService.updateTodo(todoDto);
                Notification.show("Task updated");
            }
            
            saved = true;
            close();
        } catch (Exception e) {
            log.error("Error saving task", e);
            Notification.show("Error saving task: " + e.getMessage());
        }
    }
}