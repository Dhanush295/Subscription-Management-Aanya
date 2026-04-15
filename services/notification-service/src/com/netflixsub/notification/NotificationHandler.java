package com.netflixsub.notification;

import com.netflixsub.common.Http;
import com.netflixsub.common.Json;
import com.netflixsub.model.NotificationRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NotificationHandler implements HttpHandler {
    private final List<Notification> log = new ArrayList<>();
    private final NotificationRepository repo = new NotificationRepository();

    public NotificationHandler() { log.addAll(repo.all()); }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (Http.cors(ex)) return;
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();
        try {
            if ("POST".equals(method) && path.equals("/notifications")) { send(ex); return; }
            if ("GET".equals(method)  && path.equals("/notifications")) { Http.send(ex, 200, Json.array(log)); return; }
            if ("GET".equals(method)  && path.startsWith("/notifications/user/")) {
                String uid = path.substring("/notifications/user/".length());
                List<Notification> out = new ArrayList<>();
                for (Notification n : log) if (n.userId.equals(uid)) out.add(n);
                Http.send(ex, 200, Json.array(out));
                return;
            }
            Http.send(ex, 404, Json.err("Not found"));
        } catch (Exception e) {
            Http.send(ex, 500, Json.err("notif error: " + e.getMessage()));
        }
    }

    private void send(HttpExchange ex) throws IOException {
        String body = Json.readBody(ex);
        String userId = Json.field(body, "userId");
        String type = Json.field(body, "type");
        String message = Json.field(body, "message");
        if (userId == null || type == null || message == null) { Http.send(ex, 400, Json.err("userId, type, message required")); return; }
        Notification n = new Notification(userId, type, message);
        log.add(n);
        repo.insert(n);
        System.out.println("[notification] " + type + " -> " + userId + ": " + message);
        Http.send(ex, 200, n.toString());
    }
}
