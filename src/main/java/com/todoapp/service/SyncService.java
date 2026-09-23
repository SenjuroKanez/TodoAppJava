package com.todoapp.service;

import com.todoapp.domain.*;
import com.todoapp.domain.Models.*;
import com.todoapp.security.CurrentUser;
import com.vaadin.hilla.BrowserCallable;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@BrowserCallable @PermitAll
public class SyncService {
    private final Workspace w;private final CurrentUser user;private final TaskService tasks;
    public SyncService(Workspace w,CurrentUser user,TaskService tasks){this.w=w;this.user=user;this.tasks=tasks;}
    @Transactional public SyncResponse sync(@Valid SyncRequest request) {
        w.lock();var me=user.get();
        if(request.mutations().size()>100)throw new Problem("Sync up to 100 changes at a time.");
        var outcomes=new ArrayList<Outcome>();var blocked=new HashSet<String>();
        for(var mutation:request.mutations()){
            if(blocked.contains(mutation.task().id())){
                outcomes.add(new Outcome(mutation.operationId(),"REJECTED","An earlier edit conflicted. Review the preserved changes before continuing.",mutation.task().id()));
                continue;
            }
            var outcome=tasks.mutate(mutation,me);outcomes.add(outcome);
            if(!outcome.status().equals("APPLIED"))blocked.add(mutation.task().id());
        }
        var projects=w.projects(me.id());var delta=new ArrayList<Task>();var removed=new HashSet<String>();var members=new ArrayList<Member>();
        long cursor=w.db.queryForObject("SELECT revision FROM revision_clock WHERE id=1",Long.class);
        long since=request.cursor()>cursor?0:Math.max(0,request.cursor());
        for(var p:projects) {
            long floor=request.knownProjectIds().contains(p.id())?since:0;
            delta.addAll(w.db.query("SELECT data_json FROM tasks WHERE project_id=? AND revision>?",(r,n)->Json.read(r.getString(1),Task.class),p.id(),floor));
            removed.addAll(w.db.query("SELECT task_id FROM changes WHERE project_id=? AND revision>? AND removed=true",(r,n)->r.getString(1),p.id(),floor));
            members.addAll(w.db.query("SELECT m.user_id,u.username,m.role FROM members m JOIN users u ON u.id=m.user_id WHERE m.project_id=?",
                (r,n)->new Member(p.id(),r.getString(1),r.getString(2),r.getString(3)),p.id()));
        }
        delta.forEach(t->removed.remove(t.id()));
        var invitations=w.db.query("SELECT i.id,i.project_id,p.name,i.role FROM invitations i JOIN projects p ON p.id=i.project_id WHERE i.user_id=?",
            (r,n)->new Invitation(r.getString(1),r.getString(2),r.getString(3),r.getString(4)),me.id());
        var notices=w.db.query("SELECT n.* FROM notifications n JOIN members m ON m.project_id=n.project_id AND m.user_id=n.user_id WHERE n.user_id=? ORDER BY n.created_at DESC LIMIT 100",
            (r,n)->new Notice(r.getString("id"),r.getString("project_id"),r.getString("task_id"),r.getString("message"),r.getString("created_at"),!r.getString("read_at").isEmpty()),me.id());
        var filters=w.db.query("SELECT id,name,data_json FROM filters WHERE user_id=? ORDER BY name",(r,n)->new SavedFilter(r.getString(1),r.getString(2),r.getString(3)),me.id());
        return new SyncResponse(cursor,delta,new ArrayList<>(removed),projects,members,invitations,notices,filters,outcomes);
    }
    @Transactional public void saveFilter(String id,String name,String query) {
        Workspace.uuid(id);if(name.isBlank()||name.length()>80||query.length()>4000)throw new Problem("Invalid saved filter.");
        String uid=user.get().id();w.lock();
        w.db.update("DELETE FROM filters WHERE id=? AND user_id=?",id,uid);
        w.db.update("INSERT INTO filters(id,user_id,name,data_json) VALUES(?,?,?,?)",id,uid,name,query);
    }
    @Transactional public void deleteFilter(String id){w.db.update("DELETE FROM filters WHERE id=? AND user_id=?",id,user.get().id());}
}
