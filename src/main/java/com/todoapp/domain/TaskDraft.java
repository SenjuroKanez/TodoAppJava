package com.todoapp.domain;

import com.todoapp.domain.Models.*;
import java.util.*;

/** Internal mutable working copy; never exposed as a browser endpoint. */
public final class TaskDraft {
    public String id,projectId,title,description,status,priority,assigneeId,dueDate,dueTime,timezone,myDay;
    public int estimateMinutes;
    public Recurrence recurrence;
    public List<String> tags,reminders;
    public List<Step> steps;
    public List<Comment> comments;
    public List<Activity> activity;
    public double position;
    public long version;
    public String createdAt,updatedAt,completedAt,deletedAt,conflictOf;
    public TaskDraft(Task t) {
        id=t.id();projectId=t.projectId();title=t.title();description=t.description();status=t.status();priority=t.priority();
        assigneeId=t.assigneeId();tags=t.tags();dueDate=t.dueDate();dueTime=t.dueTime();timezone=t.timezone();estimateMinutes=t.estimateMinutes();
        myDay=t.myDay();recurrence=t.recurrence();reminders=t.reminders();steps=t.steps();comments=new ArrayList<>(t.comments());
        activity=new ArrayList<>(t.activity());position=t.position();version=t.version();createdAt=t.createdAt();updatedAt=t.updatedAt();
        completedAt=t.completedAt();deletedAt=t.deletedAt();conflictOf=t.conflictOf();
    }
    public Task build() {return new Task(id,projectId,title,description,status,priority,assigneeId,tags,dueDate,dueTime,timezone,
        estimateMinutes,myDay,recurrence,reminders,steps,comments,activity,position,version,createdAt,updatedAt,completedAt,deletedAt,conflictOf);}
}
