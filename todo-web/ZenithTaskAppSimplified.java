import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;

public class ZenithTaskAppSimplified {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}

class MainFrame extends JFrame {
    private TaskService service = new TaskService();
    private DefaultListModel<Project> projectModel = new DefaultListModel<>();
    private JList<Project> projectList = new JList<>(projectModel);
    private JTextField taskField = new JTextField();
    private JTextField dueField = new JTextField("YYYY-MM-DD");
    private JPanel taskPanel = new JPanel();
    private Project current;

    MainFrame() {
        setTitle("ZenithTask");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        JPanel left = new JPanel(new BorderLayout());
        left.setPreferredSize(new Dimension(200, 600));
        JButton addP = new JButton("+P");
        JButton delP = new JButton("-P");
        JPanel pBtns = new JPanel(new GridLayout(1, 2));
        pBtns.add(addP); pBtns.add(delP);
        left.add(new JScrollPane(projectList), BorderLayout.CENTER);
        left.add(pBtns, BorderLayout.SOUTH);
        add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new GridLayout(1, 3));
        JButton addT = new JButton("+T");
        top.add(taskField); top.add(dueField); top.add(addT);
        right.add(top, BorderLayout.NORTH);
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(taskPanel);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        right.add(scroll, BorderLayout.CENTER);
        add(right, BorderLayout.CENTER);

        addP.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "New Project Name");
            if (name != null && !name.isBlank()) {
                Project p = new Project(name.trim());
                service.addProject(p);
                refreshProjects();
                projectList.setSelectedValue(p, true);
            }
        });

        delP.addActionListener(e -> {
            if (current != null && !"Inbox".equals(current.getName())) {
                service.deleteProject(current.getId());
                refreshProjects();
            }
        });

        addT.addActionListener(e -> {
            if (current != null && !taskField.getText().isBlank()) {
                Task t = new Task(taskField.getText().trim(), current.getId());
                try {
                    t.setDueDate(LocalDate.parse(dueField.getText().trim()));
                } catch (DateTimeParseException ignored) {}
                service.addTask(t);
                taskField.setText("");
                dueField.setText("YYYY-MM-DD");
                refreshTasks();
            }
        });

        projectList.addListSelectionListener(e -> {
            current = projectList.getSelectedValue();
            refreshTasks();
        });

        refreshProjects();
    }

    void refreshProjects() {
        projectModel.clear();
        for (Project p : service.getAllProjects()) projectModel.addElement(p);
        if (!projectModel.isEmpty()) {
            projectList.setSelectedIndex(0);
            current = projectList.getSelectedValue();
            refreshTasks();
        }
    }

    void refreshTasks() {
        taskPanel.removeAll();
        if (current == null) return;
        for (Task t : service.getTasksByProjectId(current.getId())) {
            JPanel row = new JPanel(new BorderLayout());
            JCheckBox check = new JCheckBox();
            check.setSelected(t.isCompleted());
            JLabel title = new JLabel(t.getTitle());
            JLabel due = new JLabel(t.getDueDate() != null ? t.getDueDate().toString() : "");
            JButton del = new JButton("X");
            row.add(check, BorderLayout.WEST);
            row.add(title, BorderLayout.CENTER);
            JPanel right = new JPanel();
            right.add(due); right.add(del);
            row.add(right, BorderLayout.EAST);
            taskPanel.add(row);

            check.addActionListener(e -> {
                t.setCompleted(check.isSelected());
                service.updateTask(t);
            });
            del.addActionListener(e -> {
                service.deleteTask(t.getId());
                refreshTasks();
            });
        }
        taskPanel.revalidate();
        taskPanel.repaint();
    }
}

class TaskService {
    private final File file = new File("zenithtask.data");
    private List<Task> tasks;
    private List<Project> projects;

    public TaskService() {
        load();
        if (projects.isEmpty()) {
            Project inbox = new Project("Inbox");
            projects.add(inbox);
        }
    }

    void save() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(new Object[]{tasks, projects});
        } catch (IOException ignored) {}
    }

    void load() {
        if (!file.exists()) {
            tasks = new ArrayList<>();
            projects = new ArrayList<>();
            return;
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Object[] data = (Object[]) in.readObject();
            tasks = (List<Task>) data[0];
            projects = (List<Project>) data[1];
        } catch (IOException | ClassNotFoundException e) {
            tasks = new ArrayList<>();
            projects = new ArrayList<>();
        }
    }

    void addTask(Task t) {
        tasks.add(t); save();
    }

    void updateTask(Task t) {
        deleteTask(t.getId());
        tasks.add(t); save();
    }

    void deleteTask(String id) {
        tasks.removeIf(t -> t.getId().equals(id)); save();
    }

    List<Task> getTasksByProjectId(String id) {
        return tasks.stream().filter(t -> t.getProjectId().equals(id)).toList();
    }

    void addProject(Project p) {
        projects.add(p); save();
    }

    void deleteProject(String id) {
        if (projects.size() <= 1) return;
        projects.removeIf(p -> p.getId().equals(id));
        String newId = projects.get(0).getId();
        for (Task t : tasks) if (t.getProjectId().equals(id)) t.setProjectId(newId);
        save();
    }

    List<Project> getAllProjects() {
        return new ArrayList<>(projects);
    }
}

class Task implements Serializable {
    private String id, title, projectId;
    private boolean completed;
    private LocalDate dueDate;
    public Task(String title, String projectId) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.projectId = projectId;
    }
    public String getId() { return id; }
    public String getTitle() { return title; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean c) { completed = c; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate d) { dueDate = d; }
    public String getProjectId() { return projectId; }
    public void setProjectId(String id) { this.projectId = id; }
}

class Project implements Serializable {
    private String id, name;
    public Project(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public String toString() { return name; }
}
