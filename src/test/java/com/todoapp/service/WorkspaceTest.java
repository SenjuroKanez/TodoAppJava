package com.todoapp.service;

import com.todoapp.domain.*;
import com.todoapp.domain.Models.*;
import com.todoapp.security.CurrentUser;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.*;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import javax.sql.DataSource;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(WorkspaceTest.Config.class)
class WorkspaceTest {
    @org.springframework.boot.test.context.TestConfiguration @EnableTransactionManagement
    static class Config {
        @Bean DataSource dataSource(){return new EmbeddedDatabaseBuilder().generateUniqueName(true).setType(EmbeddedDatabaseType.H2).addScript("db/migration/V1__workspace.sql").build();}
        @Bean JdbcTemplate jdbc(DataSource ds){return new JdbcTemplate(ds);}
        @Bean DataSourceTransactionManager transactionManager(DataSource ds){return new DataSourceTransactionManager(ds);}
        @Bean Validator validator(){return Validation.buildDefaultValidatorFactory().getValidator();}
        @Bean Workspace workspace(JdbcTemplate db){return new Workspace(db);}
        @Bean CurrentUser user(JdbcTemplate db){return new CurrentUser(db);}
        @Bean TaskService task(Workspace w,Validator v){return new TaskService(w,v);}
        @Bean SyncService sync(Workspace w,CurrentUser u,TaskService t){return new SyncService(w,u,t);}
        @Bean ProjectService project(Workspace w,CurrentUser u){return new ProjectService(w,u);}
    }
    @Autowired Workspace w;@Autowired SyncService sync;@Autowired ProjectService projects;
    String alice,bob,aliceProject,bobProject;
    @BeforeEach void setup(){
        for(String table:List.of("push_deliveries","notifications","subscriptions","operations","changes","tasks","invitations","members","projects","filters","users"))w.db.update("DELETE FROM "+table);
        w.db.update("UPDATE revision_clock SET revision=0 WHERE id=1");
        alice=addUser("alice");bob=addUser("bob");as("alice");aliceProject=projects.create(new ProjectInput("Personal","#6a59ca"));as("bob");bobProject=projects.create(new ProjectInput("Bob private","#446677"));as("alice");
    }
    @AfterEach void clear(){SecurityContextHolder.clearContext();}
    private String addUser(String name){String id=UUID.randomUUID().toString();w.db.update("INSERT INTO users(id,username,email,password_hash,created_at) VALUES(?,?,?,?,?)",id,name,name+"@test.local","not-a-real-password",Instant.now().toString());return id;}
    private void as(String name){SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(name,"", "ROLE_USER"));}
    static Task task(String project){return new Task(UUID.randomUUID().toString(),project,"Plan a better day","", "OPEN","MEDIUM","",List.of(),"","","UTC",0,"",new Recurrence("NONE",1,List.of(),"",0),List.of(),List.of(),List.of(),List.of(),1,-1,"","","","","");}
    private SyncResponse apply(Task t,long version,String kind){return sync.sync(new SyncRequest(0,List.of(new Mutation(UUID.randomUUID().toString(),kind,version,t)),List.of()));}
    private Task saved(Task t){apply(t,-1,"SAVE");return w.task(t.id());}
    @Test void createAndEditAreDurableAndVersioned(){Task t=saved(task(aliceProject));assertEquals(0,t.version());TaskDraft edit=new TaskDraft(t);edit.title="Updated";apply(edit.build(),0,"SAVE");assertEquals("Updated",w.task(t.id()).title());assertEquals(1,w.task(t.id()).version());}
    @Test void duplicateOperationIsAppliedOnce(){Task t=task(aliceProject);var m=new Mutation(UUID.randomUUID().toString(),"SAVE",-1,t);var req=new SyncRequest(0,List.of(m),List.of());assertEquals(sync.sync(req).outcomes(),sync.sync(req).outcomes());assertEquals(1,w.db.queryForObject("SELECT count(*) FROM tasks",Integer.class));}
    @Test void guessedTaskAndProjectIdsCannotCrossUserBoundary(){as("bob");Task other=saved(task(bobProject));as("alice");assertEquals("REJECTED",apply(other,other.version(),"DELETE").outcomes().getFirst().status());assertEquals("REJECTED",apply(task(bobProject),-1,"SAVE").outcomes().getFirst().status());assertTrue(w.task(other.id()).deletedAt().isEmpty());assertTrue(sync.sync(new SyncRequest(0,List.of(),List.of())).tasks().isEmpty());}
    @Test void staleEditPreservesServerAndConflictCopy(){Task original=saved(task(aliceProject));TaskDraft first=new TaskDraft(original);first.title="Server change";apply(first.build(),0,"SAVE");TaskDraft offline=new TaskDraft(original);offline.title="Offline work";var response=apply(offline.build(),0,"SAVE");assertEquals("CONFLICT",response.outcomes().getFirst().status());assertEquals("Server change",w.task(original.id()).title());assertTrue(response.tasks().stream().anyMatch(t->t.conflictOf().equals(original.id())&&t.title().contains("Offline work")));}
    @Test void viewersCannotWriteAndRevocationRejectsOfflineChanges(){projects.invite(aliceProject,"bob","VIEWER");as("bob");var invite=sync.sync(new SyncRequest(0,List.of(),List.of())).invitations().getFirst();projects.respond(invite.id(),true);assertEquals("REJECTED",apply(task(aliceProject),-1,"SAVE").outcomes().getFirst().status());as("alice");projects.membership(aliceProject,bob,"EDITOR");as("bob");Task t=saved(task(aliceProject));as("alice");projects.membership(aliceProject,bob,"REMOVE");as("bob");var response=apply(t,t.version(),"SAVE");assertEquals("REJECTED",response.outcomes().getFirst().status());assertFalse(response.projects().stream().anyMatch(p->p.id().equals(aliceProject)));}
    @Test void removedMemberIsUnassignedAndChangeIsSynced(){
        projects.invite(aliceProject,"bob","EDITOR");as("bob");projects.respond(sync.sync(new SyncRequest(0,List.of(),List.of())).invitations().getFirst().id(),true);as("alice");
        TaskDraft d=new TaskDraft(task(aliceProject));d.assigneeId=bob;Task t=saved(d.build());long cursor=w.lock();
        projects.membership(aliceProject,bob,"REMOVE");assertEquals("",w.task(t.id()).assigneeId());assertEquals(1,w.task(t.id()).version());
        assertEquals(1,sync.sync(new SyncRequest(cursor,List.of(),List.of(aliceProject))).tasks().size());
    }
    @Test void forgedAssignmentIsRejected(){TaskDraft d=new TaskDraft(task(aliceProject));d.assigneeId=bob;assertEquals("REJECTED",apply(d.build(),-1,"SAVE").outcomes().getFirst().status());}
    @Test void deletingAndRestoringKeepsTaskAndHistory(){Task t=saved(task(aliceProject));apply(t,0,"DELETE");Task deleted=w.task(t.id());assertFalse(deleted.deletedAt().isEmpty());apply(deleted,1,"RESTORE");assertTrue(w.task(t.id()).deletedAt().isEmpty());assertEquals(3,w.task(t.id()).activity().size());}
    @Test void clientCannotForgeCommentsOrAuditTrail(){TaskDraft d=new TaskDraft(task(aliceProject));d.comments=List.of(new Comment("fake",bob,"bob","Forged",""));d.activity=List.of(new Activity("fake","bob","Forged",""));Task t=saved(d.build());assertTrue(t.comments().isEmpty());assertEquals("alice",t.activity().getFirst().actor());TaskDraft comment=new TaskDraft(t);comment.comments=List.of(new Comment("fake",bob,"bob","Hello",""));apply(comment.build(),0,"COMMENT");assertEquals("alice",w.task(t.id()).comments().getFirst().author());}
    @Test void recurrenceAndRepeatedCompletionDoNotDuplicateNextTask(){TaskDraft d=new TaskDraft(task(aliceProject));d.dueDate=LocalDate.now().minusDays(3).toString();d.recurrence=new Recurrence("DAILY",1,List.of(),"",0);Task original=saved(d.build());TaskDraft done=new TaskDraft(original);done.status="DONE";apply(done.build(),0,"SAVE");assertEquals(2,w.db.queryForObject("SELECT count(*) FROM tasks",Integer.class));Task current=w.task(original.id());TaskDraft reopen=new TaskDraft(current);reopen.status="OPEN";apply(reopen.build(),current.version(),"SAVE");current=w.task(original.id());done=new TaskDraft(current);done.status="DONE";apply(done.build(),current.version(),"SAVE");assertEquals(2,w.db.queryForObject("SELECT count(*) FROM tasks",Integer.class));}
    @Test void deltaDoesNotRepeatUnchangedTasks(){Task t=saved(task(aliceProject));var first=sync.sync(new SyncRequest(0,List.of(),List.of()));var second=sync.sync(new SyncRequest(first.cursor(),List.of(),List.of(aliceProject)));assertTrue(second.tasks().isEmpty());TaskDraft d=new TaskDraft(t);d.title="Next revision";apply(d.build(),0,"SAVE");var third=sync.sync(new SyncRequest(first.cursor(),List.of(),List.of(aliceProject)));assertEquals(1,third.tasks().size());}
    @Test void movingTaskCreatesRemovalForPreviousProject(){String dest=projects.create(new ProjectInput("Second","#aa3344"));Task t=saved(task(aliceProject));long cursor=w.db.queryForObject("SELECT revision FROM revision_clock WHERE id=1",Long.class);TaskDraft d=new TaskDraft(t);d.projectId=dest;apply(d.build(),0,"SAVE");assertEquals(1,w.db.queryForObject("SELECT count(*) FROM changes WHERE task_id=? AND project_id=? AND removed=true AND revision>?",Integer.class,t.id(),aliceProject,cursor));}
    @Test void invalidTaskDoesNotPartiallyWrite(){TaskDraft d=new TaskDraft(task(aliceProject));d.title=" ";assertEquals("REJECTED",apply(d.build(),-1,"SAVE").outcomes().getFirst().status());assertEquals(0,w.db.queryForObject("SELECT count(*) FROM tasks",Integer.class));}
    @Test void ownerCannotBeDemoted(){assertThrows(Problem.class,()->projects.membership(aliceProject,alice,"VIEWER"));}
    @Test void maliciousPushEndpointRejected(){assertThrows(Problem.class,()->NotificationService.validateEndpoint("http://127.0.0.1/admin"));assertThrows(Problem.class,()->NotificationService.validateEndpoint("https://fcm.googleapis.com.evil.invalid/a"));assertDoesNotThrow(()->NotificationService.validateEndpoint("https://fcm.googleapis.com/fcm/send/example"));}
}
