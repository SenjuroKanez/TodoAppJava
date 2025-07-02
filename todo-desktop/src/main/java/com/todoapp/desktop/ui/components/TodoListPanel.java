package com.todoapp.desktop.ui.components;

import com.todoapp.common.dto.TodoDto;
import com.todoapp.common.model.Category;
//import com.todoapp.common.model.Todo;
import com.todoapp.desktop.service.ApiService;
import com.todoapp.desktop.ui.MainFrame;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for displaying the list of todos.
 */
public class TodoListPanel extends JPanel {
    private final MainFrame mainFrame;
    private final ApiService apiService;
    
    private final JPanel listContainer;
    private List<TodoDto> todos = new ArrayList<>();
    
    private JComboBox<String> filterComboBox;
    private JComboBox<Category> categoryComboBox;
    
    public TodoListPanel(MainFrame mainFrame, ApiService apiService) {
        this.mainFrame = mainFrame;
        this.apiService = apiService;
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create toolbar at the top
        JPanel toolbarPanel = createToolbarPanel();
        add(toolbarPanel, BorderLayout.NORTH);
        
        // Create the list container with a scroll pane
        listContainer = new JPanel();
        listContainer.setLayout(new MigLayout("fillx, wrap 1", "[fill]", "[]"));
        
        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Create the add task button at the bottom
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Load todos
        loadTodos();
    }
    
    private JPanel createToolbarPanel() {
        JPanel toolbarPanel = new JPanel(new MigLayout("fillx, insets 0", "[][grow][]"));
        
        JLabel titleLabel = new JLabel("My Tasks");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 24f));
        toolbarPanel.add(titleLabel, "cell 0 0");
        
        // Add filter dropdown
        filterComboBox = new JComboBox<>(new String[]{"All", "Active", "Completed"});
        filterComboBox.addActionListener(e -> loadTodos());
        
        // Add category filter dropdown
        categoryComboBox = new JComboBox<>();
        categoryComboBox.addItem(null); // No category filter
        loadCategories();
        categoryComboBox.addActionListener(e -> loadTodos());
        
        JPanel filterPanel = new JPanel(new MigLayout("insets 0"));
        filterPanel.add(new JLabel("Show:"), "gapright 5");
        filterPanel.add(filterComboBox, "gapright 10");
        filterPanel.add(new JLabel("Category:"), "gapright 5");
        filterPanel.add(categoryComboBox);
        
        toolbarPanel.add(filterPanel, "cell 2 0, align right");
        
        return toolbarPanel;
    }
    
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton addButton = new JButton("+ New Task");
        addButton.setFont(addButton.getFont().deriveFont(Font.BOLD));
        addButton.addActionListener(e -> showAddTodoDialog());
        
        bottomPanel.add(addButton);
        
        return bottomPanel;
    }
    
    private void loadTodos() {
        String filter = (String) filterComboBox.getSelectedItem();
        Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
        
        try {
            if ("Active".equals(filter)) {
                todos = apiService.getActiveTodos();
            } else if ("Completed".equals(filter)) {
                todos = apiService.getCompletedTodos();
            } else if (selectedCategory != null) {
                todos = apiService.getTodosByCategory(selectedCategory.getId());
            } else {
                todos = apiService.getAllTodos();
            }
            
            refreshTodoList();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading todos: " + e.getMessage());
        }
    }
    
    private void loadCategories() {
        try {
            List<Category> categories = apiService.getAllCategories();
            categoryComboBox.removeAllItems();
            categoryComboBox.addItem(null); // "All" option
            
            for (Category category : categories) {
                categoryComboBox.addItem(category);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading categories: " + e.getMessage());
        }
    }
    
    private void refreshTodoList() {
        listContainer.removeAll();
        
        if (todos.isEmpty()) {
            JLabel emptyLabel = new JLabel("No tasks to display. Add one!");
            emptyLabel.setFont(emptyLabel.getFont().deriveFont(Font.ITALIC));
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            listContainer.add(emptyLabel, "gapy 20");
        } else {
            for (TodoDto todo : todos) {
                listContainer.add(createTodoPanel(todo), "gapy 5");
            }
        }
        
        listContainer.revalidate();
        listContainer.repaint();
    }
    
    private JPanel createTodoPanel(TodoDto todo) {
        JPanel todoPanel = new JPanel(new MigLayout("fillx, insets 10", "[][grow][]"));
        todoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        todoPanel.setBackground(Color.WHITE);
        
        // Add completion checkbox
        JCheckBox checkBox = new JCheckBox();
        checkBox.setSelected(todo.isCompleted());
        checkBox.addActionListener(e -> toggleTodoCompletion(todo));
        todoPanel.add(checkBox, "cell 0 0");
        
        // Add title and description
        JPanel textPanel = new JPanel(new MigLayout("insets 0, fillx, wrap 1", "[grow]"));
        textPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(todo.getTitle());
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        if (todo.isCompleted()) {
            titleLabel.setText("<html><strike>" + todo.getTitle() + "</strike></html>");
        }
        textPanel.add(titleLabel);
        
        if (todo.getDescription() != null && !todo.getDescription().isEmpty()) {
            JLabel descLabel = new JLabel(todo.getDescription());
            descLabel.setFont(descLabel.getFont().deriveFont(Font.PLAIN, 12f));
            descLabel.setForeground(Color.GRAY);
            textPanel.add(descLabel);
        }
        
        // Add due date if present
        if (todo.getDueDate() != null) {
            JLabel dateLabel = new JLabel(formatDueDate(todo.getDueDate()));
            dateLabel.setFont(dateLabel.getFont().deriveFont(Font.PLAIN, 12f));
            dateLabel.setForeground(todo.getDueDate().isBefore(LocalDateTime.now()) 
                    ? new Color(217, 83, 79) : new Color(91, 192, 222));
            textPanel.add(dateLabel);
        }
        
        // Add category badge if present
        if (todo.getCategory() != null) {
            JLabel categoryLabel = new JLabel(todo.getCategory().getName());
            categoryLabel.setFont(categoryLabel.getFont().deriveFont(Font.PLAIN, 11f));
            categoryLabel.setOpaque(true);
            categoryLabel.setBackground(Color.decode(todo.getCategory().getColor()));
            categoryLabel.setForeground(Color.WHITE);
            categoryLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            textPanel.add(categoryLabel, "gaptop 3");
        }
        
        todoPanel.add(textPanel, "cell 1 0, grow");
        
        // Add action buttons
        JPanel actionsPanel = new JPanel(new MigLayout("insets 0"));
        actionsPanel.setOpaque(false);
        
        JButton editButton = new JButton("Edit");
        editButton.setFont(editButton.getFont().deriveFont(Font.PLAIN, 12f));
        editButton.addActionListener(e -> showEditTodoDialog(todo));
        actionsPanel.add(editButton, "gapright 5");
        
        JButton deleteButton = new JButton("Delete");
        deleteButton.setFont(deleteButton.getFont().deriveFont(Font.PLAIN, 12f));
        deleteButton.addActionListener(e -> deleteTodo(todo));
        actionsPanel.add(deleteButton);
        
        todoPanel.add(actionsPanel, "cell 2 0, align right");
        
        return todoPanel;
    }
    
    private String formatDueDate(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        
        String formattedDate = dateTime.format(formatter);
        
        if (dateTime.toLocalDate().isEqual(now.toLocalDate())) {
            return "Due Today";
        } else if (dateTime.toLocalDate().isEqual(now.toLocalDate().plusDays(1))) {
            return "Due Tomorrow";
        } else if (dateTime.isBefore(now)) {
            return "Overdue: " + formattedDate;
        } else {
            return "Due: " + formattedDate;
        }
    }
    
    private void toggleTodoCompletion(TodoDto todo) {
        try {
            apiService.toggleTodoCompletion(todo.getId());
            loadTodos(); // Reload todos after toggling
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating todo: " + e.getMessage());
        }
    }
    
    private void showAddTodoDialog() {
        TodoDialog dialog = new TodoDialog(mainFrame, apiService, null);
        dialog.setVisible(true);
        
        // Reload todos if a new one was added
        if (dialog.isSuccess()) {
            loadTodos();
        }
    }
    
    private void showEditTodoDialog(TodoDto todo) {
        TodoDialog dialog = new TodoDialog(mainFrame, apiService, todo);
        dialog.setVisible(true);
        
        // Reload todos if updated
        if (dialog.isSuccess()) {
            loadTodos();
        }
    }
    
    private void deleteTodo(TodoDto todo) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this task?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                apiService.deleteTodo(todo.getId());
                loadTodos(); // Reload todos after deletion
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deleting todo: " + e.getMessage());
            }
        }
    }
}