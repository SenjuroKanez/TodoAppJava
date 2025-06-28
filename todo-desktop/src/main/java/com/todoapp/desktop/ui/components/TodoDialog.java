package com.todoapp.desktop.ui.components;

import com.todoapp.common.dto.TodoDto;
import com.todoapp.common.model.Category;
import com.todoapp.common.model.Todo;
import com.todoapp.desktop.service.ApiService;
import com.todoapp.desktop.ui.MainFrame;
import lombok.Getter;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Dialog for adding or editing a todo.
 */
public class TodoDialog extends JDialog {
    private final MainFrame mainFrame;
    private final ApiService apiService;
    private final TodoDto existingTodo;
    
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JComboBox<Todo.Priority> priorityComboBox;
    private JComboBox<Category> categoryComboBox;
    private JCheckBox hasDateCheckBox;
    private JSpinner dateSpinner;
    
    @Getter
    private boolean success = false;
    
    public TodoDialog(MainFrame mainFrame, ApiService apiService, TodoDto existingTodo) {
        super(mainFrame, existingTodo == null ? "Add New Task" : "Edit Task", true);
        this.mainFrame = mainFrame;
        this.apiService = apiService;
        this.existingTodo = existingTodo;
        
        initializeUI();
        
        // Set size and position
        setSize(500, 500);
        setLocationRelativeTo(mainFrame);
    }
    
    private void initializeUI() {
        JPanel contentPanel = new JPanel(new MigLayout("fillx, wrap 1, insets 20", "[fill]"));
        setContentPane(contentPanel);
        
        // Title field
        contentPanel.add(new JLabel("Title:"), "gaptop 10");
        titleField = new JTextField(existingTodo != null ? existingTodo.getTitle() : "");
        contentPanel.add(titleField);
        
        // Description field
        contentPanel.add(new JLabel("Description:"), "gaptop 10");
        descriptionArea = new JTextArea(existingTodo != null ? existingTodo.getDescription() : "", 5, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        contentPanel.add(descScrollPane);
        
        // Priority field
        contentPanel.add(new JLabel("Priority:"), "gaptop 10");
        priorityComboBox = new JComboBox<>(Todo.Priority.values());
        if (existingTodo != null && existingTodo.getPriority() != null) {
            priorityComboBox.setSelectedItem(existingTodo.getPriority());
        }
        contentPanel.add(priorityComboBox);
        
        // Category field
        contentPanel.add(new JLabel("Category:"), "gaptop 10");
        categoryComboBox = new JComboBox<>();
        loadCategories();
        if (existingTodo != null && existingTodo.getCategory() != null) {
            for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
                Category category = categoryComboBox.getItemAt(i);
                if (category != null && category.getId().equals(existingTodo.getCategory().getId())) {
                    categoryComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }
        
        JPanel categoryPanel = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
        categoryPanel.add(categoryComboBox, "grow");
        
        JButton newCategoryButton = new JButton("New Category");
        newCategoryButton.addActionListener(e -> showNewCategoryDialog());
        categoryPanel.add(newCategoryButton);
        
        contentPanel.add(categoryPanel);
        
        // Due date field
        contentPanel.add(new JLabel("Due Date:"), "gaptop 10");
        
        JPanel datePanel = new JPanel(new MigLayout("insets 0, fillx"));
        hasDateCheckBox = new JCheckBox("Set Due Date");
        hasDateCheckBox.setSelected(existingTodo != null && existingTodo.getDueDate() != null);
        
        // Create date spinner with current date
        SpinnerDateModel dateModel = new SpinnerDateModel();
        dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "MMM d, yyyy");
        dateSpinner.setEditor(dateEditor);
        
        // Set date if editing a todo with a due date
        if (existingTodo != null && existingTodo.getDueDate() != null) {
            LocalDateTime dueDate = existingTodo.getDueDate();
            java.util.Date date = java.util.Date.from(
                    dueDate.atZone(java.time.ZoneId.systemDefault()).toInstant()
            );
            dateSpinner.setValue(date);
        }
        
        dateSpinner.setEnabled(hasDateCheckBox.isSelected());
        hasDateCheckBox.addActionListener(e -> dateSpinner.setEnabled(hasDateCheckBox.isSelected()));
        
        datePanel.add(hasDateCheckBox, "cell 0 0");
        datePanel.add(dateSpinner, "cell 1 0, growx");
        
        contentPanel.add(datePanel);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        
        JButton saveButton = new JButton(existingTodo == null ? "Add Task" : "Save Changes");
        saveButton.addActionListener(e -> saveTask());
        
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(saveButton);
        
        contentPanel.add(buttonsPanel, "gaptop 20");
    }
    
    private void loadCategories() {
        try {
            List<Category> categories = apiService.getAllCategories();
            categoryComboBox.removeAllItems();
            categoryComboBox.addItem(null); // No category option
            
            for (Category category : categories) {
                categoryComboBox.addItem(category);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading categories: " + e.getMessage());
        }
    }
    
    private void showNewCategoryDialog() {
        String name = JOptionPane.showInputDialog(this, "Enter category name:");
        if (name != null && !name.trim().isEmpty()) {
            // Show color options
            String[] colorOptions = {
                    "Red", "Orange", "Yellow", "Green", "Blue", "Purple", "Pink"
            };
            int colorChoice = JOptionPane.showOptionDialog(
                    this, 
                    "Choose a color:", 
                    "Category Color", 
                    JOptionPane.DEFAULT_OPTION, 
                    JOptionPane.QUESTION_MESSAGE, 
                    null, 
                    colorOptions, 
                    colorOptions[0]
            );
            
            if (colorChoice >= 0) {
                String colorHex;
                switch (colorChoice) {
                    case 0: colorHex = Category.COLOR_RED; break;
                    case 1: colorHex = Category.COLOR_ORANGE; break;
                    case 2: colorHex = Category.COLOR_YELLOW; break;
                    case 3: colorHex = Category.COLOR_GREEN; break;
                    case 4: colorHex = Category.COLOR_BLUE; break;
                    case 5: colorHex = Category.COLOR_PURPLE; break;
                    default: colorHex = Category.COLOR_PINK; break;
                }
                
                try {
                    Category newCategory = Category.builder()
                            .name(name.trim())
                            .color(colorHex)
                            .build();
                    
                    Category savedCategory = apiService.createCategory(newCategory);
                    loadCategories();
                    
                    // Select the newly created category
                    categoryComboBox.setSelectedItem(savedCategory);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error creating category: " + e.getMessage());
                }
            }
        }
    }
    
    private void saveTask() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title is required");
            return;
        }
        
        try {
            TodoDto todoDto = new TodoDto();
            
            if (existingTodo != null) {
                todoDto.setId(existingTodo.getId());
            }
            
            todoDto.setTitle(title);
            todoDto.setDescription(descriptionArea.getText().trim());
            todoDto.setPriority((Todo.Priority) priorityComboBox.getSelectedItem());
            todoDto.setCategory((Category) categoryComboBox.getSelectedItem());
            
            // Handle due date
            if (hasDateCheckBox.isSelected()) {
                java.util.Date date = (java.util.Date) dateSpinner.getValue();
                LocalDate localDate = date.toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate();
                
                // Set the time to end of day (11:59 PM)
                LocalDateTime dueDateTime = LocalDateTime.of(localDate, LocalTime.of(23, 59));
                todoDto.setDueDate(dueDateTime);
            }
            
            if (existingTodo == null) {
                apiService.createTodo(todoDto);
            } else {
                apiService.updateTodo(todoDto);
            }
            
            success = true;
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving task: " + e.getMessage());
        }
    }
}