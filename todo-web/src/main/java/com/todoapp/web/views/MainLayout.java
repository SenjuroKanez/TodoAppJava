package com.todoapp.web.views;

import com.todoapp.web.service.AuthService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIcon;

/**
 * Main layout for the Todo web application.
 */
public class MainLayout extends AppLayout {
    private final AuthService authService;
    
    private H2 viewTitle;
    
    public MainLayout(AuthService authService) {
        this.authService = authService;
        
        // Create the top navigation bar
        setPrimarySection(Section.DRAWER);
        addToNavbar(createHeaderContent());
        
        // Create the side navigation drawer
        addToDrawer(createDrawerContent());
    }
    
    private Component createHeaderContent() {
        Header header = new Header();
        header.addClassNames(
                LumoUtility.BoxSizing.BORDER,
                LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Width.FULL
        );
        
        // Top row with toggle and title
        HorizontalLayout topRow = new HorizontalLayout();
        topRow.addClassNames(
                LumoUtility.AlignItems.CENTER,
                LumoUtility.Padding.Horizontal.MEDIUM,
                LumoUtility.Padding.Vertical.SMALL,
                LumoUtility.Width.FULL
        );
        
        DrawerToggle toggle = new DrawerToggle();
        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        
        topRow.add(toggle, viewTitle);
        topRow.expand(viewTitle);
        
        // User menu
        if (authService.isAuthenticated()) {
            Button logoutButton = new Button("Logout", new Icon(VaadinIcon.SIGN_OUT));
            logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            logoutButton.addClickListener(e -> {
                authService.logout();
                getUI().ifPresent(ui -> ui.navigate("login"));
            });
            
            topRow.add(logoutButton);
        }
        
        header.add(topRow);
        
        return header;
    }
    
    private Component createDrawerContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.addClassNames(
                LumoUtility.Padding.Horizontal.SMALL,
                LumoUtility.Padding.Vertical.MEDIUM
        );
        layout.setSpacing(false);
        
        // App title
        HorizontalLayout logoLayout = new HorizontalLayout();
        logoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        
        H1 appName = new H1("TodoApp");
        appName.addClassNames(
                LumoUtility.FontSize.XLARGE,
                LumoUtility.Margin.NONE
        );
        
        logoLayout.add(appName);
        
        // Navigation items
        layout.add(
                logoLayout,
                createNavItem("Tasks", TodoView.class, LineAwesomeIcon.TASKS_SOLID.create()),
                createNavItem("Categories", CategoriesView.class, LineAwesomeIcon.TAGS_SOLID.create()),
                createNavItem("Settings", SettingsView.class, LineAwesomeIcon.COG_SOLID.create())
        );
        
        return layout;
    }
    
    private Component createNavItem(String text, Class<? extends Component> navigationTarget, Component icon) {
        RouterLink link = new RouterLink();
        link.addClassNames(
                LumoUtility.Display.FLEX,
                LumoUtility.AlignItems.CENTER,
                LumoUtility.Padding.Horizontal.SMALL,
                LumoUtility.Padding.Vertical.XSMALL,
                LumoUtility.Width.FULL
        );
        
        link.setRoute(navigationTarget);
        
        HorizontalLayout layout = new HorizontalLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setSpacing(true);
        
        if (icon != null) {
            layout.add(icon);
        }
        
        layout.add(new com.vaadin.flow.component.html.Span(text));
        link.add(layout);
        
        return link;
    }
    
    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        
        // Set the view title based on the PageTitle annotation
        viewTitle.setText(getCurrentPageTitle());
        
        // Redirect to login if not authenticated
        if (!authService.isAuthenticated() && 
                !getContent().getClass().equals(LoginView.class) &&
                !getContent().getClass().equals(RegisterView.class)) {
            getUI().ifPresent(ui -> ui.navigate("login"));
        }
    }
    
    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        if (title != null) {
            String pageTitle = title.value();
            // Strip the application name part if present
            int separatorIndex = pageTitle.indexOf(" | ");
            if (separatorIndex > 0) {
                return pageTitle.substring(0, separatorIndex);
            }
            return pageTitle;
        }
        return "";
    }
}