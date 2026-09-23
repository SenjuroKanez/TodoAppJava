package com.todoapp.service;

import com.todoapp.domain.*;
import com.todoapp.domain.Models.Task;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import java.security.Security;
import java.time.*;
import java.util.*;

@Component
public class ReminderScheduler {
    private static final Logger log=LoggerFactory.getLogger(ReminderScheduler.class);
    private final Workspace w;private final TaskService tasks;private final TransactionTemplate tx;
    private final PushService push;
    public ReminderScheduler(Workspace w,TaskService tasks,org.springframework.transaction.PlatformTransactionManager manager,
        @Value("${app.vapid.public-key:}") String publicKey,@Value("${app.vapid.private-key:}") String privateKey,@Value("${app.vapid.subject:}") String subject) throws Exception {
        this.w=w;this.tasks=tasks;this.tx=new TransactionTemplate(manager);
        if(!publicKey.isBlank()&&!privateKey.isBlank()) {Security.addProvider(new BouncyCastleProvider());push=new PushService(publicKey,privateKey,subject);}
        else push=null;
    }
    @Scheduled(fixedDelay=30000,initialDelay=30000)
    public void schedule() {
        tx.executeWithoutResult(status->{
            w.lock();Instant now=Instant.now();
            var values=w.db.query("SELECT t.data_json FROM tasks t JOIN projects p ON p.id=t.project_id WHERE t.deleted_at='' AND p.archived=false",(r,n)->Json.read(r.getString(1),Task.class));
            for(Task task:values) {
                if(task.status().equals("DONE"))continue;
                for(String reminder:task.reminders())if(!Instant.parse(reminder).isAfter(now)) {
                    List<String> recipients=task.assigneeId().isEmpty()?w.db.query("SELECT owner_id FROM projects WHERE id=?",(r,n)->r.getString(1),task.projectId()):List.of(task.assigneeId());
                    for(String uid:recipients)if(w.db.queryForObject("SELECT count(*) FROM members WHERE project_id=? AND user_id=?",Integer.class,task.projectId(),uid)>0)
                        tasks.notice(uid,task,"Reminder: "+task.title(),"reminder:"+task.id()+":"+reminder+":"+uid);
                }
            }
            if(push!=null) w.db.update("INSERT INTO push_deliveries(notification_id,subscription_id,next_attempt) SELECT n.id,s.id,? FROM notifications n JOIN subscriptions s ON s.user_id=n.user_id WHERE n.read_at='' AND NOT EXISTS(SELECT 1 FROM push_deliveries d WHERE d.notification_id=n.id AND d.subscription_id=s.id)",now.toString());
            // Tombstones live for 30 days. Changes retain deletion facts for older offline clients.
            String cutoff=now.minus(Duration.ofDays(30)).toString();
            var expired=w.db.query("SELECT id,project_id FROM tasks WHERE deleted_at<>'' AND deleted_at<?",(r,n)->List.of(r.getString(1),r.getString(2)),cutoff);
            for(var t:expired){long rev=w.revision();w.db.update("INSERT INTO changes(revision,task_id,project_id,removed) VALUES(?,?,?,true)",rev,t.get(0),t.get(1));w.db.update("DELETE FROM tasks WHERE id=?",t.get(0));}
        });
        if(push!=null)deliver();
    }
    private void deliver() {
        var jobs=tx.execute(status->{
            w.lock();
            var rows=w.db.queryForList("SELECT d.*,s.endpoint,s.p256dh,s.auth FROM push_deliveries d JOIN subscriptions s ON s.id=d.subscription_id JOIN notifications n ON n.id=d.notification_id JOIN members m ON m.project_id=n.project_id AND m.user_id=n.user_id WHERE d.delivered=false AND d.attempts<5 AND d.next_attempt<=? LIMIT 30",Instant.now().toString());
            rows.forEach(r->w.db.update("UPDATE push_deliveries SET attempts=attempts+1,next_attempt=? WHERE notification_id=? AND subscription_id=?",Instant.now().plusSeconds(300).toString(),r.get("notification_id"),r.get("subscription_id")));
            return rows;
        });
        if(jobs==null)return;
        for(var job:jobs) try {
            // Keep task content off lock-screen push payloads; details are loaded after authentication.
            byte[] payload=Json.write(Map.of("title","TodoApp","body","You have a new update in your workspace.","url","/")).getBytes(java.nio.charset.StandardCharsets.UTF_8);
            var response=push.send(new Notification(job.get("endpoint").toString(),job.get("p256dh").toString(),job.get("auth").toString(),payload));
            int code=response.getStatusLine().getStatusCode();
            if(code==404||code==410)w.db.update("DELETE FROM subscriptions WHERE id=?",job.get("subscription_id"));
            else if(code>=200&&code<300)w.db.update("UPDATE push_deliveries SET delivered=true WHERE notification_id=? AND subscription_id=?",job.get("notification_id"),job.get("subscription_id"));
        }catch(Exception e){log.warn("Push delivery failed; retry is scheduled ({})",e.getClass().getSimpleName());}
    }
}
