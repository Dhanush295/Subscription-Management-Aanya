package com.netflixsub.user;

import com.netflixsub.common.Http;
import com.netflixsub.common.Json;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class UserHandler implements HttpHandler {
    private final ProfileStore store;
    public UserHandler(ProfileStore s) { this.store = s; }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (Http.cors(ex)) return;
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();
        String tail = path.substring("/users".length());
        try {
            if ("GET".equals(method) && tail.isEmpty())       { Http.send(ex, 200, Json.array(store.all())); return; }
            if ("GET".equals(method) && tail.startsWith("/")) { getOne(ex, tail.substring(1)); return; }
            if ("POST".equals(method) && tail.isEmpty())      { create(ex); return; }
            if ("PUT".equals(method) && tail.startsWith("/")) { update(ex, tail.substring(1)); return; }
            Http.send(ex, 400, Json.err("Bad request"));
        } catch (Exception e) {
            Http.send(ex, 500, Json.err("user error: " + e.getMessage()));
        }
    }

    private void getOne(HttpExchange ex, String id) throws IOException {
        var p = store.byId(id);
        if (p.isEmpty()) { Http.send(ex, 404, Json.err("User not found")); return; }
        Http.send(ex, 200, p.get().toString());
    }

    private void create(HttpExchange ex) throws IOException {
        String body = Json.readBody(ex);
        String uid = Json.field(body, "userId");
        String first = Json.field(body, "firstName");
        String last = Json.field(body, "lastName");
        String email = Json.field(body, "email");
        String ageStr = Json.field(body, "age");
        if (uid == null || first == null || last == null || email == null) {
            Http.send(ex, 400, Json.err("userId, firstName, lastName, email required")); return;
        }
        if (store.emailTaken(email)) {
            Http.send(ex, 400, Json.err("A profile with this email already exists")); return;
        }
        int age = 0;
        try { if (ageStr != null) age = Integer.parseInt(ageStr); } catch (Exception ignored) {}
        Profile p = store.create(uid, first, last, age, email);
        Http.send(ex, 200, p.toString());
    }

    private void update(HttpExchange ex, String id) throws IOException {
        var opt = store.byId(id);
        if (opt.isEmpty()) { Http.send(ex, 404, Json.err("User not found")); return; }
        Profile p = opt.get();
        String body = Json.readBody(ex);
        String first = Json.field(body, "firstName");
        String last = Json.field(body, "lastName");
        String email = Json.field(body, "email");
        String ageStr = Json.field(body, "age");
        if (first != null) p.firstName = first;
        if (last  != null) p.lastName  = last;
        if (ageStr != null) { try { p.age = Integer.parseInt(ageStr); } catch (Exception ignored) {} }
        if (email != null && !email.equalsIgnoreCase(p.email)) {
            if (store.emailTaken(email)) { Http.send(ex, 400, Json.err("Email already in use")); return; }
            store.rebindEmail(p, email);
        } else {
            store.persist(p);
        }
        Http.send(ex, 200, p.toString());
    }
}
