package com.netflixsub.auth;

import com.netflixsub.common.Http;
import com.netflixsub.common.Json;
import com.netflixsub.user.Profile;
import com.netflixsub.user.ProfileStore;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class AuthHandler implements HttpHandler {
    private final AuthStore store;
    private final ProfileStore profiles;

    public AuthHandler(AuthStore store, ProfileStore profiles) {
        this.store = store;
        this.profiles = profiles;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (Http.cors(ex)) return;
        String path = ex.getRequestURI().getPath();
        try {
            if (path.endsWith("/signup"))   { signup(ex); return; }
            if (path.endsWith("/login"))    { login(ex); return; }
            if (path.endsWith("/logout"))   { logout(ex); return; }
            if (path.endsWith("/validate")) { validate(ex); return; }
            Http.send(ex, 404, Json.err("Not found"));
        } catch (Exception e) {
            Http.send(ex, 500, Json.err("auth error: " + e.getMessage()));
        }
    }

    private void signup(HttpExchange ex) throws IOException {
        String body = Json.readBody(ex);
        String firstName = Json.field(body, "firstName");
        String lastName = Json.field(body, "lastName");
        String ageStr = Json.field(body, "age");
        String email = Json.field(body, "email");
        String password = Json.field(body, "password");
        if (firstName == null || lastName == null || email == null || password == null) {
            Http.send(ex, 400, Json.err("firstName, lastName, email and password required")); return;
        }
        if (store.findByEmail(email).isPresent() || profiles.emailTaken(email)) {
            Http.send(ex, 400, Json.err("Email already registered")); return;
        }
        int age = 0;
        try { if (ageStr != null) age = Integer.parseInt(ageStr); } catch (Exception ignored) {}

        Account a = store.register(email, password);
        Profile p = profiles.create(a.userId, firstName, lastName, age, a.email);
        String token = store.issueToken(a.userId);
        Http.send(ex, 200, "{\"token\":\"" + token + "\","
                + "\"userId\":\"" + a.userId + "\","
                + "\"firstName\":\"" + Json.esc(p.firstName) + "\","
                + "\"lastName\":\"" + Json.esc(p.lastName) + "\","
                + "\"email\":\"" + Json.esc(a.email) + "\","
                + "\"role\":\"" + Json.esc(a.role) + "\"}");
    }

    private void login(HttpExchange ex) throws IOException {
        String body = Json.readBody(ex);
        String email = Json.field(body, "email");
        String password = Json.field(body, "password");
        var acct = store.findByEmail(email);
        if (acct.isEmpty() || !acct.get().passwordHash.equals(AuthStore.hash(password))) {
            Http.send(ex, 401, Json.err("Invalid email or password")); return;
        }
        String token = store.issueToken(acct.get().userId);
        Http.send(ex, 200, "{\"token\":\"" + token + "\","
                + "\"userId\":\"" + acct.get().userId + "\","
                + "\"email\":\"" + Json.esc(acct.get().email) + "\","
                + "\"role\":\"" + Json.esc(acct.get().role) + "\"}");
    }

    private void logout(HttpExchange ex) throws IOException {
        String token = Http.authHeader(ex);
        if (token != null) store.revoke(token);
        Http.send(ex, 200, Json.ok("logged out"));
    }

    private void validate(HttpExchange ex) throws IOException {
        String token = Http.authHeader(ex);
        var uid = token == null ? java.util.Optional.<String>empty() : store.userIdForToken(token);
        if (uid.isEmpty()) { Http.send(ex, 401, Json.err("Invalid token")); return; }
        Http.send(ex, 200, "{\"userId\":\"" + uid.get() + "\"}");
    }
}
