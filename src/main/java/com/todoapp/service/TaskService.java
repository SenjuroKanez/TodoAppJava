package com.todoapp.service;

import com.todoapp.domain.*;
import com.todoapp.domain.Models.*;
import com.todoapp.security.CurrentUser;
import org.springframework.stereotype.Service;
import jakarta.validation.Validator;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** All task commands, including online commands, pass through SyncService. */
@Service
public class TaskService {
    private final Workspace w; private final Validator validator;
    public TaskService(Workspace w,Validator validator){this.w=w;this.validator=validator;}
    public Outcome mutate(Mutation m,User user) {
        Workspace.uuid(m.operationId());
        var cached=w.db.query("SELECT result_json FROM operations WHERE user_id=? AND operation_id=?",(r,n)->Json.read(r.getString(1),Outcome.class),user.id(),m.operationId());
        if(!cached.isEmpty())return cached.getFirst();
        Outcome outcome;
        try { outcome=apply(m,user); }
        catch(Problem e){outcome=new Outcome(m.operationId(),"REJECTED",e.getMessage(),m.task().id());}
        w.db.update("INSERT INTO operations(user_id,operation_id,result_json,created_at) VALUES(?,?,?,?)",user.id(),m.operationId(),Json.write(outcome),Instant.now().toString());
        return outcome;
    }
    private Outcome apply(Mutation m,User user) {
        var violations=validator.validate(m);
        if(!violations.isEmpty())throw new Problem("Check task fields: "+violations.iterator().next().getMessage());
        if(!Set.of("SAVE","DELETE","RESTORE","COMMENT").contains(m.kind()))throw new Problem("Unsupported task action.");
        Task input=m.task();Workspace.uuid(input.id());Workspace.uuid(input.projectId());
        if(input.tags().size()>20||input.reminders().size()>10||input.steps().size()>100)throw new Problem("Limit tasks to 20 tags, 10 reminders, and 100 steps.");
        Task old=w.task(input.id());
        if(old!=null)w.edit(old.projectId(),user.id());
        w.edit(input.projectId(),user.id());
        if(!Set.of("OPEN","DONE").contains(input.status()) || !Set.of("LOW","MEDIUM","HIGH","URGENT").contains(input.priority()))throw new Problem("Invalid task status or priority.");
        Recurrences.validate(input);
        if(!input.assigneeId().isEmpty())w.role(input.projectId(),input.assigneeId());
        if(old==null&&!m.kind().equals("SAVE"))throw new Problem("This task no longer exists.");
        if(old!=null && !old.deletedAt().isEmpty()&&!Set.of("RESTORE","DELETE").contains(m.kind()))throw new Problem("This task is in Trash. Restore it first.");
        if(old!=null && !old.projectId().equals(input.projectId()) &&
            (w.db.queryForObject("SELECT count(*) FROM members WHERE project_id=?",Integer.class,old.projectId())>1 ||
             w.db.queryForObject("SELECT count(*) FROM members WHERE project_id=?",Integer.class,input.projectId())>1)) {
            w.owner(old.projectId(),user.id());w.owner(input.projectId(),user.id());
        }
        String now=Instant.now().toString();
        // Comments append independently. Client authors and activity are never trusted.
        if(m.kind().equals("COMMENT")) {
            if(input.comments().isEmpty())throw new Problem("Write a comment first.");
            String body=input.comments().getLast().text().strip();
            if(body.isEmpty()||body.length()>2000)throw new Problem("Comments must be 1–2000 characters.");
            TaskDraft draft=new TaskDraft(old);
            draft.comments.add(new Comment(m.operationId(),user.id(),user.username(),body,now));
            draft.version=old.version()+1;draft.updatedAt=now;draft.activity.add(activity(user,"Added a comment",now));w.save(draft.build(),old);
            notifyMembers(old,user,"New comment on “"+old.title()+"”",m.operationId());
            return new Outcome(m.operationId(),"APPLIED","",old.id());
        }
        if(old!=null&&old.version()!=m.baseVersion() || old==null&&m.baseVersion()!=-1) {
            TaskDraft copy=new TaskDraft(input);copy.id=UUID.randomUUID().toString();copy.title=("Conflict: "+input.title()).substring(0,Math.min(200,10+input.title().length()));
            copy.conflictOf=input.id();copy.createdAt=now;copy.updatedAt=now;copy.completedAt="";copy.deletedAt="";copy.status="OPEN";copy.version=0;
            copy.comments=new ArrayList<>();copy.activity=new ArrayList<>(List.of(activity(user,"Offline edit preserved as a conflict copy",now)));
            copy.recurrence=new Recurrence("NONE",1,List.of(),"",0);copy.reminders=List.of();w.save(copy.build(),null);
            return new Outcome(m.operationId(),"CONFLICT","Someone changed this task. Your work was saved as a conflict copy.",copy.id);
        }
        TaskDraft draft=new TaskDraft(input);draft.title=input.title().strip();draft.tags=input.tags().stream().map(String::strip).distinct().toList();
        draft.createdAt=old==null?now:old.createdAt();draft.updatedAt=now;draft.version=old==null?0:old.version()+1;
        draft.comments=old==null?new ArrayList<>():new ArrayList<>(old.comments());draft.activity=old==null?new ArrayList<>():new ArrayList<>(old.activity());
        draft.conflictOf=old==null?"":old.conflictOf();draft.deletedAt=m.kind().equals("DELETE")?now:"";
        draft.completedAt=input.status().equals("DONE")?(old!=null&&old.status().equals("DONE")?old.completedAt():now):"";
        String action=old==null?"Created task":m.kind().equals("DELETE")?"Moved to Trash":m.kind().equals("RESTORE")?"Restored task":!old.status().equals(input.status())?(input.status().equals("DONE")?"Completed task":"Reopened task"):"Updated task";
        draft.activity.add(activity(user,action,now));
        Task saved=draft.build();w.save(saved,old);
        if(!saved.assigneeId().isEmpty()&&(old==null||!saved.assigneeId().equals(old.assigneeId())))notice(saved.assigneeId(),saved,"Assigned to you: "+saved.title(),m.operationId()+":assign");
        if(saved.deletedAt().isEmpty() && saved.status().equals("DONE")&&(old==null||!old.status().equals("DONE")))nextOccurrence(saved,user,now);
        return new Outcome(m.operationId(),"APPLIED","",saved.id());
    }
    private void nextOccurrence(Task task,User user,String now) {
        var date=Recurrences.next(task,Clock.systemUTC());if(date.isEmpty())return;
        String id=UUID.nameUUIDFromBytes((task.id()+":next").getBytes(StandardCharsets.UTF_8)).toString();
        if(w.task(id)!=null)return;
        TaskDraft next=new TaskDraft(task);next.id=id;next.status="OPEN";next.version=0;next.createdAt=now;next.updatedAt=now;next.completedAt="";next.myDay="";
        next.dueDate=date.get().toString();next.steps=task.steps().stream().map(s->new Step(UUID.randomUUID().toString(),s.title(),false)).toList();
        long days=ChronoUnit.DAYS.between(LocalDate.parse(task.dueDate()),date.get());
        next.reminders=task.reminders().stream().map(r->Instant.parse(r).atZone(ZoneId.of(task.timezone())).plusDays(days).toInstant().toString()).toList();
        next.comments=new ArrayList<>();next.activity=new ArrayList<>(List.of(activity(user,"Created next recurring occurrence",now)));
        var r=task.recurrence();next.recurrence=new Recurrence(r.frequency(),r.interval(),r.weekdays(),r.endDate(),r.remaining()>0?r.remaining()-1:0);w.save(next.build(),null);
    }
    private Activity activity(User u,String action,String now){return new Activity(UUID.randomUUID().toString(),u.username(),action,now);}
    private void notifyMembers(Task task,User actor,String message,String key){
        w.db.query("SELECT user_id FROM members WHERE project_id=? AND user_id<>?",(r,n)->r.getString(1),task.projectId(),actor.id())
            .forEach(uid->notice(uid,task,message,key+":"+uid));
    }
    public void notice(String uid,Task task,String message,String key){
        if(w.db.queryForObject("SELECT count(*) FROM notifications WHERE dedup_key=?",Integer.class,key)>0)return;
        w.db.update("INSERT INTO notifications(id,user_id,project_id,task_id,message,created_at,dedup_key) VALUES(?,?,?,?,?,?,?)",
            UUID.randomUUID().toString(),uid,task.projectId(),task.id(),message.substring(0,Math.min(500,message.length())),Instant.now().toString(),key);
    }
}
