package com.netflixsub.subscription;

import com.netflixsub.common.Http;
import com.netflixsub.common.Json;
import com.netflixsub.payment.PaymentProcessor;
import com.netflixsub.plan.Plan;
import com.netflixsub.plan.PlanStore;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.time.Instant;

public class SubHandler implements HttpHandler {
    private final SubStore store;
    private final PlanStore plans;
    private final PaymentProcessor payments;

    public SubHandler(SubStore s, PlanStore p, PaymentProcessor pay) {
        this.store = s; this.plans = p; this.payments = pay;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (Http.cors(ex)) return;
        store.sweepExpired();
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();
        String[] parts = path.split("/");
        try {
            if ("GET".equals(method) && parts.length == 2)               { Http.send(ex, 200, Json.array(store.all())); return; }
            if ("GET".equals(method) && parts.length == 4 && "user".equals(parts[2])) { Http.send(ex, 200, Json.array(store.forUser(parts[3]))); return; }
            if ("POST".equals(method) && parts.length == 2)              { create(ex); return; }
            if ("PUT".equals(method) && parts.length == 4 && "cancel".equals(parts[3]))  { cancel(ex, parts[2]); return; }
            if ("PUT".equals(method) && parts.length == 4 && "change".equals(parts[3]))  { change(ex, parts[2]); return; }
            if ("PUT".equals(method) && parts.length == 4 && "renew".equals(parts[3]))   { renew(ex, parts[2]); return; }
            Http.send(ex, 400, Json.err("Bad request"));
        } catch (Exception e) {
            Http.send(ex, 500, Json.err("sub error: " + e.getMessage()));
        }
    }

    private double priceFor(Plan p, String cycle) {
        return "YEARLY".equalsIgnoreCase(cycle) ? p.yearlyInr : p.monthlyInr;
    }

    private void create(HttpExchange ex) throws IOException {
        String body = Json.readBody(ex);
        String userId = Json.field(body, "userId");
        String planCode = Json.field(body, "planCode");
        String cycle = firstNonNull(Json.field(body, "billingCycle"), "MONTHLY");
        String cardLast4 = Json.field(body, "cardLast4");
        if (userId == null || planCode == null) { Http.send(ex, 400, Json.err("userId and planCode required")); return; }
        if (store.activeFor(userId).isPresent()) { Http.send(ex, 400, Json.err("User already has an active subscription")); return; }
        var plan = plans.get(planCode);
        if (plan.isEmpty()) { Http.send(ex, 400, Json.err("Unknown plan")); return; }

        Subscription s = new Subscription(userId, plan.get().code, cycle, plan.get().trialDays);
        if (plan.get().trialDays == 0) {
            var res = payments.charge(userId, s.subId, priceFor(plan.get(), cycle), cardLast4);
            if (!res.success) { Http.send(ex, 402, Json.err("Payment failed: " + res.payment.failureReason)); return; }
        }
        store.put(s);
        Http.send(ex, 200, s.toString());
    }

    private void cancel(HttpExchange ex, String subId) throws IOException {
        var s = store.get(subId);
        if (s.isEmpty()) { Http.send(ex, 404, Json.err("Subscription not found")); return; }
        if ("CANCELLED".equals(s.get().status)) { Http.send(ex, 400, Json.err("Already cancelled")); return; }
        s.get().cancel();
        store.persist(s.get());
        Http.send(ex, 200, s.get().toString());
    }

    private void change(HttpExchange ex, String subId) throws IOException {
        var s = store.get(subId);
        if (s.isEmpty()) { Http.send(ex, 404, Json.err("Subscription not found")); return; }
        String body = Json.readBody(ex);
        String newPlan = Json.field(body, "planCode");
        String newCycle = firstNonNull(Json.field(body, "billingCycle"), s.get().billingCycle);
        var p = plans.get(newPlan);
        if (p.isEmpty()) { Http.send(ex, 400, Json.err("Unknown plan")); return; }
        s.get().changePlan(p.get().code, newCycle);
        store.persist(s.get());
        Http.send(ex, 200, s.get().toString());
    }

    private void renew(HttpExchange ex, String subId) throws IOException {
        var s = store.get(subId);
        if (s.isEmpty()) { Http.send(ex, 404, Json.err("Subscription not found")); return; }
        var p = plans.get(s.get().planCode);
        if (p.isEmpty()) { Http.send(ex, 400, Json.err("Plan no longer available")); return; }
        var res = payments.charge(s.get().userId, subId, priceFor(p.get(), s.get().billingCycle), "0000");
        if (!res.success) { Http.send(ex, 402, Json.err("Payment failed on renewal")); return; }
        s.get().renewFrom(Instant.now());
        store.persist(s.get());
        Http.send(ex, 200, s.get().toString());
    }

    private static String firstNonNull(String a, String b) { return a == null ? b : a; }
}
