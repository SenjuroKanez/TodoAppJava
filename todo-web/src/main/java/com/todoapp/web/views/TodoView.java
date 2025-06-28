package com.todoapp.web.views;

import com.todoapp.common.dto.TodoDto;
import com.todoapp.common.model.Category;
import com.todoapp.common.model.Todo;
import com.todoapp.web.service.ApiService;
import com.todoapp.web.service.AuthService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
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
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Main view for the Todo web application.
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("TodoApp | Tasks")
@UIScope
@Slf4j
public class TodoView extends VerticalLayout {
    private final ApiService apiService;
    private final AuthService authService;
    
    private Grid<TodoDto> grid;
    private List<TodoDto> todos = new ArrayList<>();
    
    private ComboBox<String> filterComboBox;
    private ComboBox<Category> categoryComboBox;
    
    public TodoView(ApiService apiService, AuthService authService) {
        this.apiService = apiService;
        this.authService = authService;
        
        if (!authService.isAuthenticated()) {
            getUI().ifPresent(ui -> ui.navigate("login"));
            return;
        }
        
        addClassName("todo-view");
        setSizeFull();
        setPadding(true);
        
        add(createHeader());
        add(createFilters());
        add(createGrid());
        add(createAddButton());
        
        loadTodos();
    }
    
    private Component createHeader() {
        H3 header = new H3("My Tasks");
        header.addClassName("view-header");
        
        return header;
    }
    
    private Component createFilters() {
        HorizontalLayout filters = new HorizontalLayout();
        filters.setWidthFull();
        filters.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        
        // Filter by status
        filterComboBox = new ComboBox<>("Show");
        filterComboBox.setWidth("150px");
        filterComboBox.setItems("All", "Active", "Completed");
        filterComboBox.setValue("All");
        filterComboBox.addValueChangeListener(e -> loadTodos());
        
        // Filter by category
        categoryComboBox = new ComboBox<>("Category");
        categoryComboBox.setWidth("200px");
        categoryComboBox.setItemLabelGenerator(Category::getName);
        loadCategories();
        categoryComboBox.addValueChangeListener(e -> loadTodos());
        
        filters.add(filterComboBox, categoryComboBox);
        
        return filters;
    }
    
    private Grid<TodoDto> createGrid() {
        grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeightFull();
        
        // Define columns
        grid.addColumn(new ComponentRenderer<>(this::createTodoCard))
                .setAutoWidth(true)
                .setFlexGrow(1);
        
        return grid;
    }
    
    private Component createTodoCard(TodoDto todo) {
        VerticalLayout cardLayout = new VerticalLayout();
        cardLayout.addClassName("todo-card");
        cardLayout.setSpacing(false);
        cardLayout.setPadding(true);
        
        // Top row with title and actions
        HorizontalLayout topRow = new HorizontalLayout();
        topRow.setWidthFull();
        topRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        topRow.setAlignItems(FlexComponent.Alignment.CENTER);
        
        // Checkbox and title
        HorizontalLayout titleLayout = new HorizontalLayout();
        Checkbox checkbox = new Checkbox();
        checkbox.setValue(todo.isCompleted());
        checkbox.addValueChangeListener(e -> toggleTodoCompletion(todo));
        
        Span titleSpan = new Span(todo.getTitle());
        titleSpan.getStyle().set("font-weight", "bold");
        if (todo.isCompleted()) {
            titleSpan.getStyle().set("text-decoration", "line-through");
            titleSpan.getStyle().set("color", "var(--lumo-contrast-60pct)");
        }
        
        titleLayout.add(checkbox, titleSpan);
        
        // Action buttons
        HorizontalLayout actions = new HorizontalLayout();
        
        Button editButton = new Button(new Icon(VaadinIcon.EDIT));
        editButton.addClassName("edit-button");
        editButton.addClickListener(e -> showEditDialog(todo));
        
        Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
        deleteButton.addClassName("delete-button");
        deleteButton.addClickListener(e -> deleteTodo(todo));
        
        actions.add(editButton, deleteButton);
        
        topRow.add(titleLayout, actions);
        cardLayout.add(topRow);
        
        // Description if available
        if (todo.getDescription() != null && !todo.getDescription().isEmpty()) {
            Span description = new Span(todo.getDescription());
            description.addClassName("todo-description");
            cardLayout.add(description);
        }
        
        // Bottom row with metadata
        HorizontalLayout metadataLayout = new HorizontalLayout();
        metadataLayout.setWidthFull();
        metadataLayout.setSpacing(true);
        
        // Priority badge
        if (todo.getPriority() != null) {
            Span priorityBadge = new Span(todo.getPriority().toString());
            priorityBadge.addClassName("priority-badge");
            priorityBadge.addClassName("priority-" + todo.getPriority().toString().toLowerCase());
            metadataLayout.add(priorityBadge);
        }
        
        // Category badge
        if (todo.getCategory() != null) {
            Span categoryBadge = new Span(todo.getCategory().getName());
            categoryBadge.addClassName("category-badge");
            categoryBadge.getStyle().set("background-color", todo.getCategory().getColor());
            metadataLayout.add(categoryBadge);
        }
        
        // Due date
        if (todo.getDueDate() != null) {
            Span dueDateSpan = new Span(formatDueDate(todo.getDueDate()));
            dueDateSpan.addClassName("due-date");
            
            if (todo.getDueDate().isBefore(LocalDateTime.now())) {
                dueDateSpan.addClassName("overdue");
            }
            
            metadataLayout.add(dueDateSpan);
        }
        
        cardLayout.add(metadataLayout);
        
        return cardLayout;
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
    
    private Component createAddButton() {
        Button addButton = new Button("Add Task", new Icon(VaadinIcon.PLUS));
        addButton.addClassName("add-todo-button");
        addButton.addClickListener(e -> showAddDialog());
        
        return addButton;
    }
    
    private void loadTodos() {
        try {
            String filter = filterComboBox.getValue();
            Category selectedCategory = categoryComboBox.getValue();
            
            if ("Active".equals(filter)) {
                todos = apiService.getActiveTodos();
            } else if ("Completed".equals(filter)) {
                todos = apiService.getCompletedTodos();
            } else if (selectedCategory != null) {
                todos = apiService.getTodosByCategory(selectedCategory.getId());
            } else {
                todos = apiService.getAllTodos();
            }
            
            grid.setItems(todos);
        } catch (Exception e) {
            log.error("Error loading todos", e);
            Notification.show("Error loading tasks: " + e.getMessage());
        }
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
    
    private void toggleTodoCompletion(TodoDto todo) {
        try {
            apiService.toggleTodoCompletion(todo.getId());
            loadTodos();
        } catch (Exception e) {
            log.error("Error toggling todo completion", e);
            Notification.show("Error updating task: " + e.getMessage());
        }
    }
    
    private void showAddDialog() {
        TodoDialog dialog = new TodoDialog(apiService, null);
        dialog.addOpenedChangeListener(event -> {
            if (!event.isOpened() && dialog.isSaved()) {
                loadTodos();
            }
        });
        dialog.open();
    }
    
    private void showEditDialog(TodoDto todo) {
        TodoDialog dialog = new TodoDialog(apiService, todo);
        dialog.addOpenedChangeListener(event -> {
            if (!event.isOpened() && dialog.isSaved()) {
                loadTodos();
            }
        });
        dialog.open();
    }
    
    private void deleteTodo(TodoDto todo) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirm Delete");
        
        Div content = new Div();
        content.setText("Are you sure you want to delete this task?");
        confirmDialog.add(content);
        
        Button cancelButton = new Button("Cancel", e -> confirmDialog.close());
        Button deleteButton = new Button("Delete", e -> {
            try {
                apiService.deleteTodo(todo.getId());
                loadTodos();
                confirmDialog.close();
                Notification.show("Task deleted");
            } catch (Exception ex) {
                log.error("Error deleting todo", ex);
                Notification.show("Error deleting task: " + ex.getMessage());
            }
        });
        deleteButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY, 
                com.vaadin.flow.component.button.ButtonVariant.LUMO_ERROR);
        
        confirmDialog.getFooter().add(cancelButton, deleteButton);
        confirmDialog.open();
    }
}