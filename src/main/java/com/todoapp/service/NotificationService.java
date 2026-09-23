package com.todoapp.service;

import com.todoapp.domain.Problem;
import com.todoapp.security.CurrentUser;
import com.vaadin.hilla.BrowserCallable;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.time.Instant;
import java.util.*;

@BrowserCallable @PermitAll
public class NotificationService {
    private final Workspace w;private final CurrentUser user;private final String publicKey;
    public NotificationService(Workspace w,CurrentUser user,@Value("${app.vapid.public-key:}") String key){this.w=w;this.user=user;this.publicKey=key;}
    public String publicKey(){user.get();return publicKey;}
    @Transactional public void read(String id){w.db.update("UPDATE notifications SET read_at=? WHERE id=? AND user_id=?",Instant.now().toString(),id,user.get().id());}
    @Transactional public void subscribe(String endpoint,String p256dh,String auth) {
        if(publicKey.isBlank())throw new Problem("Browser notifications are not configured on this server.");
        validateEndpoint(endpoint);
        try {if(Base64.getUrlDecoder().decode(p256dh).length!=65||Base64.getUrlDecoder().decode(auth).length!=16)throw new IllegalArgumentException();}
        catch(Exception e){throw new Problem("Invalid browser subscription keys.");}
        w.lock();w.db.update("DELETE FROM subscriptions WHERE endpoint=?",endpoint);
        w.db.update("INSERT INTO subscriptions(id,user_id,endpoint,p256dh,auth) VALUES(?,?,?,?,?)",UUID.randomUUID().toString(),user.get().id(),endpoint,p256dh,auth);
    }
    @Transactional public void unsubscribe(String endpoint){w.db.update("DELETE FROM subscriptions WHERE endpoint=? AND user_id=?",endpoint,user.get().id());}
    static void validateEndpoint(String endpoint) {
        try {
            URI uri=URI.create(endpoint);String host=uri.getHost();
            boolean vendor=host!=null&&(host.equals("fcm.googleapis.com")||host.equals("updates.push.services.mozilla.com")||host.endsWith(".notify.windows.com")||host.equals("web.push.apple.com"));
            if(!vendor||!uri.getScheme().equals("https")||uri.getUserInfo()!=null||uri.getPort()!=-1||endpoint.length()>2048)throw new IllegalArgumentException();
        }catch(Exception e){throw new Problem("Unsupported browser push endpoint.");}
    }
}
