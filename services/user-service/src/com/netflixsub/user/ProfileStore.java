package com.netflixsub.user;

import com.netflixsub.model.ProfileRepository;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ProfileStore {
    private final Map<String, Profile> byId    = new HashMap<>();
    private final Map<String, Profile> byEmail = new HashMap<>();
    private final ProfileRepository repo = new ProfileRepository();

    public ProfileStore() {
        for (Profile p : repo.all()) {
            byId.put(p.userId, p);
            byEmail.put(p.email.toLowerCase(), p);
        }
    }

    public Collection<Profile> all()                   { return byId.values(); }
    public Optional<Profile> byId(String id)           { return Optional.ofNullable(byId.get(id)); }
    public boolean emailTaken(String email)            { return email != null && byEmail.containsKey(email.toLowerCase()); }

    public Profile create(String userId, String first, String last, int age, String email) {
        Profile p = new Profile(userId, first, last, age, email);
        byId.put(userId, p);
        byEmail.put(email.toLowerCase(), p);
        repo.upsert(p);
        return p;
    }

    public Optional<Profile> delete(String userId) {
        Profile p = byId.remove(userId);
        if (p != null) byEmail.remove(p.email.toLowerCase());
        repo.delete(userId);
        return Optional.ofNullable(p);
    }

    public void rebindEmail(Profile p, String newEmail) {
        byEmail.remove(p.email.toLowerCase());
        p.email = newEmail;
        byEmail.put(newEmail.toLowerCase(), p);
        repo.upsert(p);
    }

    public void persist(Profile p) { repo.upsert(p); }
}
