package com.netflixsub.admin;

import com.netflixsub.auth.AuthStore;
import com.netflixsub.common.Http;
import com.netflixsub.common.Json;
import com.netflixsub.plan.Plan;
import com.netflixsub.plan.PlanStore;
import com.netflixsub.subscription.SubStore;
import com.netflixsub.subscription.Subscription;
import com.netflixsub.user.Profile;
import com.netflixsub.user.ProfileStore;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Optional;

public class AdminHandler implements HttpHandler {
    private final AuthStore auth;
    private final ProfileStore profiles;
    private final SubStore subs;
    private final PlanStore plans;

    public AdminHandler(AuthStore auth, ProfileStore profiles, SubStore subs, PlanStore plans) {
        this.auth = auth;
        this.profiles = profiles;
        this.subs = subs;
        this.plans = plans;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (Http.cors(ex)) return;
        String token = Http.authHeader(ex);
        Optional<String> uid = token == null ? Optional.empty() : auth.userIdForToken(token);
        if (uid.isEmpty() || !auth.isAdmin(uid.get())) {
            Http.send(ex, 401, Json.err("Admin privileges required"));
            return;
        }

        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();
        String[] parts = path.split("/");
        try {
            if ("GET".equals(method) && parts.length == 3 && "subscriptions".equals(parts[2])) { listSubs(ex); return; }
            if ("PUT".equals(method) && parts.length == 5 && "subscriptions".equals(parts[2]) && "cancel".equals(parts[4])) { cancelSub(ex, parts[3]); return; }
            if ("DELETE".equals(method) && parts.length == 4 && "subscriptions".equals(parts[2])) { deleteSub(ex, parts[3]); return; }
            if ("GET".equals(method) && parts.length == 3 && "users".equals(parts[2])) { listUsers(ex); return; }
            if ("DELETE".equals(method) && parts.length == 4 && "users".equals(parts[2])) { deleteUser(ex, parts[3]); return; }
            if ("GET".equals(method) && parts.length == 3 && "plans".equals(parts[2])) { Http.send(ex, 200, Json.array(toCollection(plans.all()))); return; }
            Http.send(ex, 404, Json.err("Not found"));
        } catch (Exception e) {
            Http.send(ex, 500, Json.err("admin error: " + e.getMessage()));
        }
    }

    private static java.util.Collection<?> toCollection(Iterable<?> it) {
        java.util.ArrayList<Object> out = new java.util.ArrayList<>();
        for (Object o : it) out.add(o);
        return out;
    }

    private String enrichedSub(Subscription s) {
        Optional<Profile> p = profiles.byId(s.userId);
        Optional<Plan> pl = plans.get(s.planCode);
        String fullName = p.map(Profile::fullName).orElse("");
        String email = p.map(x -> x.email).orElse("");
        String planName = pl.map(x -> x.name).orElse(s.planCode);
        String base = s.toString();
        String extra = ",\"userEmail\":\"" + Json.esc(email) + "\","
                + "\"userFullName\":\"" + Json.esc(fullName) + "\","
                + "\"planName\":\"" + Json.esc(planName) + "\"}";
        return base.substring(0, base.length() - 1) + extra;
    }

    private void listSubs(HttpExchange ex) throws IOException {
        subs.sweepExpired();
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Subscription s : subs.all()) {
            if (!first) sb.append(",");
            sb.append(enrichedSub(s));
            first = false;
        }
        sb.append("]");
        Http.send(ex, 200, sb.toString());
    }

    private void cancelSub(HttpExchange ex, String subId) throws IOException {
        var s = subs.get(subId);
        if (s.isEmpty()) { Http.send(ex, 404, Json.err("Subscription not found")); return; }
        if ("CANCELLED".equals(s.get().status)) { Http.send(ex, 400, Json.err("Already cancelled")); return; }
        s.get().cancel();
        subs.persist(s.get());
        Http.send(ex, 200, s.get().toString());
    }

    private void deleteSub(HttpExchange ex, String subId) throws IOException {
        var removed = subs.delete(subId);
        if (removed.isEmpty()) { Http.send(ex, 404, Json.err("Subscription not found")); return; }
        Http.send(ex, 200, Json.ok("deleted"));
    }

    private void listUsers(HttpExchange ex) throws IOException {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Profile p : profiles.all()) {
            if (auth.isAdmin(p.userId)) continue;
            Optional<Subscription> active = subs.activeFor(p.userId);
            String base = p.toString();
            String extra = ",\"activeSubStatus\":" + (active.isPresent() ? "\"" + active.get().status + "\"" : "null")
                    + ",\"activeSubPlan\":" + (active.isPresent() ? "\"" + active.get().planCode + "\"" : "null") + "}";
            if (!first) sb.append(",");
            sb.append(base, 0, base.length() - 1).append(extra);
            first = false;
        }
        sb.append("]");
        Http.send(ex, 200, sb.toString());
    }

    private void deleteUser(HttpExchange ex, String userId) throws IOException {
        if (auth.isAdmin(userId)) { Http.send(ex, 400, Json.err("Cannot delete admin account")); return; }
        Optional<Profile> p = profiles.byId(userId);
        if (p.isEmpty()) { Http.send(ex, 404, Json.err("User not found")); return; }
        for (Subscription s : subs.forUser(userId)) subs.delete(s.subId);
        auth.delete(userId, p.get().email);
        profiles.delete(userId);
        Http.send(ex, 200, Json.ok("user deleted"));
    }
}
