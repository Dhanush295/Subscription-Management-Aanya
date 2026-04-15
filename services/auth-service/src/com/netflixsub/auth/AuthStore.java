package com.netflixsub.auth;

import com.netflixsub.common.Ids;
import com.netflixsub.model.AccountRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AuthStore {
    private final Map<String, Account> byEmail = new HashMap<>();
    private final Map<String, Account> byId    = new HashMap<>();
    private final Map<String, String>  tokens  = new HashMap<>();
    private final AccountRepository repo = new AccountRepository();

    public AuthStore() {
        for (Account a : repo.all()) {
            byEmail.put(a.email, a);
            byId.put(a.userId, a);
        }
        tokens.putAll(repo.loadTokens());
    }

    public Optional<Account> findByEmail(String email) {
        return Optional.ofNullable(byEmail.get(email == null ? "" : email.toLowerCase()));
    }

    public Optional<Account> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public Account register(String email, String password) {
        Account a = new Account(email.toLowerCase(), hash(password));
        byEmail.put(a.email, a);
        byId.put(a.userId, a);
        repo.upsert(a);
        return a;
    }

    public Account registerAdmin(String email, String password) {
        Account existing = byEmail.get(email.toLowerCase());
        if (existing != null) {
            existing.role = "ADMIN";
            repo.upsert(existing);
            return existing;
        }
        Account a = register(email, password);
        a.role = "ADMIN";
        repo.upsert(a);
        return a;
    }

    public boolean isAdmin(String userId) {
        Account a = byId.get(userId);
        return a != null && "ADMIN".equals(a.role);
    }

    public void delete(String userId, String email) {
        byId.remove(userId);
        if (email != null) byEmail.remove(email.toLowerCase());
        repo.delete(userId);
    }

    public String issueToken(String userId) {
        String t = Ids.token();
        tokens.put(t, userId);
        repo.saveToken(t, userId);
        return t;
    }

    public Optional<String> userIdForToken(String token) {
        return Optional.ofNullable(tokens.get(token));
    }

    public void revoke(String token) {
        tokens.remove(token);
        repo.revokeToken(token);
    }

    public static String hash(String password) {
        return Integer.toHexString((password + "netflix-salt").hashCode());
    }
}
