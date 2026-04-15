package com.netflixsub.auth;

import com.netflixsub.common.Ids;
import com.netflixsub.common.Json;

public class Account {
    public final String userId;
    public final String email;
    public final String passwordHash;
    public String role = "USER";

    public Account(String email, String passwordHash) {
        this.userId = Ids.shortId();
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public Account(String userId, String email, String passwordHash, String role) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    @Override
    public String toString() {
        return "{\"userId\":\"" + Json.esc(userId) + "\",\"email\":\"" + Json.esc(email) + "\",\"role\":\"" + Json.esc(role) + "\"}";
    }
}
