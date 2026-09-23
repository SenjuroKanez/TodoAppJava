package com.todoapp.service;

import com.todoapp.domain.*;
import com.todoapp.domain.Models.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class Workspace {
    public final JdbcTemplate db;
    public Workspace(JdbcTemplate db) { this.db=db; }
    /** All writes acquire this lock before changing sync-visible state, so cursors cannot skip commits. */
    public long lock() { return db.queryForObject("SELECT revision FROM revision_clock WHERE id=1 FOR UPDATE",Long.class); }
    public long revision() {
        long next=lock()+1;
        db.update("UPDATE revision_clock SET revision=? WHERE id=1",next);
        return next;
    }
    public String role(String project,String user) {
        return db.query("SELECT role FROM members WHERE project_id=? AND user_id=?",(r,n)->r.getString(1),project,user)
            .stream().findFirst().orElseThrow(()->new Problem("You no longer have access to this project."));
    }
    public void edit(String project,String user) {
        if (role(project,user).equals("VIEWER")) throw new Problem("This project is read-only.");
        if(Boolean.TRUE.equals(db.queryForObject("SELECT archived FROM projects WHERE id=?",Boolean.class,project))) throw new Problem("Restore this archived project before editing.");
    }
    public void owner(String project,String user) {
        if (!role(project,user).equals("OWNER")) throw new Problem("Only the project owner can do that.");
    }
    public void ordinaryProject(String project) {
        if(Boolean.TRUE.equals(db.queryForObject("SELECT inbox FROM projects WHERE id=?",Boolean.class,project))) throw new Problem("Your Inbox cannot be shared, archived, or deleted.");
    }
    public List<Project> projects(String user) {
        return db.query("SELECT p.*,m.role FROM projects p JOIN members m ON m.project_id=p.id WHERE m.user_id=? ORDER BY p.inbox DESC,p.name",
            (r,n)->new Project(r.getString("id"),r.getString("name"),r.getString("color"),r.getBoolean("archived"),r.getBoolean("inbox"),r.getString("role"),r.getLong("version")),user);
    }
    public Task task(String id) {
        return db.query("SELECT data_json FROM tasks WHERE id=?",(r,n)->Json.read(r.getString(1),Task.class),id).stream().findFirst().orElse(null);
    }
    public void save(Task task, Task previous) {
        long revision=revision();
        if(previous==null) db.update("INSERT INTO tasks(id,project_id,title,description,data_json,version,revision,deleted_at) VALUES(?,?,?,?,?,?,?,?)",
            task.id(),task.projectId(),task.title(),task.description(),Json.write(task),task.version(),revision,task.deletedAt());
        else db.update("UPDATE tasks SET project_id=?,title=?,description=?,data_json=?,version=?,revision=?,deleted_at=? WHERE id=?",
            task.projectId(),task.title(),task.description(),Json.write(task),task.version(),revision,task.deletedAt(),task.id());
        db.update("INSERT INTO changes(revision,task_id,project_id,removed) VALUES(?,?,?,false)",revision,task.id(),task.projectId());
        if(previous!=null && !previous.projectId().equals(task.projectId())) db.update("INSERT INTO changes(revision,task_id,project_id,removed) VALUES(?,?,?,true)",revision,task.id(),previous.projectId());
    }
    public static String uuid(String id) {
        try { return UUID.fromString(id).toString(); } catch(Exception e) { throw new Problem("Invalid identifier."); }
    }
}
