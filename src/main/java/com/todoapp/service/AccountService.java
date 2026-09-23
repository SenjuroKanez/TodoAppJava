package com.todoapp.service;

import com.todoapp.domain.Models.User;
import com.todoapp.domain.Problem;
import com.todoapp.security.CurrentUser;
import com.vaadin.hilla.BrowserCallable;
import jakarta.annotation.security.PermitAll;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZoneId;
import java.util.Set;

@BrowserCallable @PermitAll
public class AccountService {
    private final Workspace w; private final CurrentUser user;
    public AccountService(Workspace w,CurrentUser user) { this.w=w;this.user=user; }
    public User current() { return user.get(); }
    @Transactional public User preferences(String theme,String timezone) {
        if(!Set.of("LIGHT","DARK","SYSTEM").contains(theme)) throw new Problem("Invalid theme.");
        try{ZoneId.of(timezone);}catch(Exception e){throw new Problem("Invalid timezone.");}
        w.db.update("UPDATE users SET theme=?,timezone=? WHERE id=?",theme,timezone,user.get().id());
        return user.get();
    }
}
