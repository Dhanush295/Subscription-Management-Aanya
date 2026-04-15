package com.netflixsub.subscription;

import com.netflixsub.model.SubscriptionRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SubStore {
    private final Map<String, Subscription> byId = new HashMap<>();
    private final SubscriptionRepository repo = new SubscriptionRepository();

    public SubStore() {
        for (Subscription s : repo.all()) byId.put(s.subId, s);
    }

    public Subscription put(Subscription s) {
        byId.put(s.subId, s);
        repo.upsert(s);
        return s;
    }
    public Optional<Subscription> get(String id) { return Optional.ofNullable(byId.get(id)); }
    public Optional<Subscription> delete(String id) {
        Subscription s = byId.remove(id);
        repo.delete(id);
        return Optional.ofNullable(s);
    }
    public List<Subscription> all() { return new ArrayList<>(byId.values()); }

    public Optional<Subscription> activeFor(String userId) {
        return byId.values().stream()
                .filter(s -> s.userId.equals(userId) && !"CANCELLED".equals(s.status))
                .findFirst();
    }

    public List<Subscription> forUser(String userId) {
        List<Subscription> out = new ArrayList<>();
        for (Subscription s : byId.values()) if (s.userId.equals(userId)) out.add(s);
        return out;
    }

    public void sweepExpired() {
        for (Subscription s : byId.values()) {
            if (!"CANCELLED".equals(s.status) && !"EXPIRED".equals(s.status) && s.isExpired()) {
                s.status = "EXPIRED";
                repo.upsert(s);
            }
        }
    }

    public void persist(Subscription s) { repo.upsert(s); }
}
