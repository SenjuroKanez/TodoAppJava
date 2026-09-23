package com.todoapp.service;

import com.todoapp.domain.Models.*;
import com.todoapp.domain.Problem;
import com.todoapp.domain.Json;
import com.todoapp.domain.TaskDraft;
import java.time.Instant;
import com.todoapp.security.CurrentUser;
import com.vaadin.hilla.BrowserCallable;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@BrowserCallable @PermitAll
public class ProjectService {
    private final Workspace w; private final CurrentUser user;
    public ProjectService(Workspace w,CurrentUser user) {this.w=w;this.user=user;}
    public List<Project> list(){return w.projects(user.get().id());}
    @Transactional public String create(@Valid ProjectInput input) {
        w.lock(); String id=UUID.randomUUID().toString(), uid=user.get().id();
        if(input.name().isBlank()) throw new Problem("Name is required.");
        w.db.update("INSERT INTO projects(id,owner_id,name,color) VALUES(?,?,?,?)",id,uid,input.name().strip(),input.color());
        w.db.update("INSERT INTO members(project_id,user_id,role) VALUES(?,?,'OWNER')",id,uid);return id;
    }
    @Transactional public void update(String id,@Valid ProjectInput input,long version,boolean archived) {
        w.lock();w.owner(id,user.get().id());w.ordinaryProject(id);
        if(w.db.update("UPDATE projects SET name=?,color=?,archived=?,version=version+1 WHERE id=? AND version=?",input.name().strip(),input.color(),archived,id,version)!=1)
            throw new Problem("This project changed. Refresh and try again.");
    }
    @Transactional public void delete(String id) {
        w.lock();w.owner(id,user.get().id());w.ordinaryProject(id);w.db.update("DELETE FROM projects WHERE id=?",id);
    }
    @Transactional public void invite(String id,String login,String role) {
        w.lock();w.owner(id,user.get().id());w.ordinaryProject(id);checkRole(role);
        var users=w.db.query("SELECT id FROM users WHERE username=? OR email=?",(r,n)->r.getString(1),login.strip().toLowerCase(Locale.ROOT),login.strip().toLowerCase(Locale.ROOT));
        if(users.isEmpty()) throw new Problem("Ask your collaborator to register first, then use their exact username or email.");
        String uid=users.getFirst();
        if(w.db.queryForObject("SELECT count(*) FROM members WHERE project_id=? AND user_id=?",Integer.class,id,uid)>0)throw new Problem("This person is already a member.");
        w.db.update("DELETE FROM invitations WHERE project_id=? AND user_id=?",id,uid);
        w.db.update("INSERT INTO invitations(id,project_id,user_id,role) VALUES(?,?,?,?)",UUID.randomUUID().toString(),id,uid,role);
    }
    @Transactional public void respond(String invitationId,boolean accept) {
        w.lock();String uid=user.get().id();
        var values=w.db.query("SELECT project_id,role FROM invitations WHERE id=? AND user_id=?",(r,n)->List.of(r.getString(1),r.getString(2)),invitationId,uid);
        if(values.isEmpty())throw new Problem("Invitation is no longer available.");
        if(accept)w.db.update("INSERT INTO members(project_id,user_id,role) VALUES(?,?,?)",values.getFirst().get(0),uid,values.getFirst().get(1));
        w.db.update("DELETE FROM invitations WHERE id=? AND user_id=?",invitationId,uid);
    }
    @Transactional public void membership(String id,String memberId,String role) {
        w.lock();w.owner(id,user.get().id());
        if(w.role(id,memberId).equals("OWNER"))throw new Problem("The owner cannot be removed or demoted.");
        if(role.equals("REMOVE")) {
            w.db.update("DELETE FROM members WHERE project_id=? AND user_id=?",id,memberId);
            w.db.update("DELETE FROM notifications WHERE project_id=? AND user_id=?",id,memberId);
            var assigned=w.db.query("SELECT data_json FROM tasks WHERE project_id=?",(r,n)->Json.read(r.getString(1),Task.class),id);
            for(var task:assigned)if(task.assigneeId().equals(memberId)) {
                var draft=new TaskDraft(task);draft.assigneeId="";draft.version++;draft.updatedAt=Instant.now().toString();
                draft.activity.add(new Activity(UUID.randomUUID().toString(),user.get().username(),"Unassigned removed member",draft.updatedAt));
                w.save(draft.build(),task);
            }
        } else {checkRole(role);w.db.update("UPDATE members SET role=? WHERE project_id=? AND user_id=?",role,id,memberId);}
    }
    private static void checkRole(String role){if(!Set.of("EDITOR","VIEWER").contains(role))throw new Problem("Choose Editor or Viewer.");}
}
