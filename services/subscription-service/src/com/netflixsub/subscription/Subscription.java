package com.netflixsub.subscription;

import com.netflixsub.common.Ids;
import com.netflixsub.common.Json;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Subscription {
    public final String subId;
    public final String userId;
    public String planCode;
    public String billingCycle;
    public Instant startedAt;
    public Instant expiresAt;
    public Instant trialEndsAt;
    public String status;
    public Instant cancelledAt;

    public Subscription(String userId, String planCode, String billingCycle, int trialDays) {
        this.subId = Ids.shortId();
        this.userId = userId;
        this.planCode = planCode;
        this.billingCycle = billingCycle;
        this.startedAt = Instant.now();
        this.trialEndsAt = trialDays > 0 ? startedAt.plus(trialDays, ChronoUnit.DAYS) : null;
        this.expiresAt = computeExpiry(startedAt, billingCycle);
        this.status = trialDays > 0 ? "TRIAL" : "ACTIVE";
    }

    public Subscription(String subId, String userId, String planCode, String billingCycle,
                        String status, Instant startedAt, Instant expiresAt,
                        Instant trialEndsAt, Instant cancelledAt) {
        this.subId = subId;
        this.userId = userId;
        this.planCode = planCode;
        this.billingCycle = billingCycle;
        this.status = status;
        this.startedAt = startedAt;
        this.expiresAt = expiresAt;
        this.trialEndsAt = trialEndsAt;
        this.cancelledAt = cancelledAt;
    }

    public static Instant computeExpiry(Instant from, String cycle) {
        return "YEARLY".equalsIgnoreCase(cycle)
                ? from.plus(365, ChronoUnit.DAYS)
                : from.plus(30, ChronoUnit.DAYS);
    }

    public void changePlan(String newCode, String newCycle) {
        this.planCode = newCode;
        this.billingCycle = newCycle;
        this.expiresAt = computeExpiry(Instant.now(), newCycle);
        this.status = "ACTIVE";
    }

    public void cancel() {
        this.status = "CANCELLED";
        this.cancelledAt = Instant.now();
    }

    public boolean isExpired() { return Instant.now().isAfter(expiresAt); }

    public void renewFrom(Instant at) {
        this.startedAt = at;
        this.expiresAt = computeExpiry(at, billingCycle);
        this.status = "ACTIVE";
    }

    @Override
    public String toString() {
        return "{\"subId\":\"" + subId + "\","
             + "\"userId\":\"" + userId + "\","
             + "\"planCode\":\"" + planCode + "\","
             + "\"billingCycle\":\"" + billingCycle + "\","
             + "\"status\":\"" + status + "\","
             + "\"startedAt\":\"" + startedAt + "\","
             + "\"expiresAt\":\"" + expiresAt + "\","
             + "\"trialEndsAt\":" + (trialEndsAt == null ? "null" : "\"" + trialEndsAt + "\"") + ","
             + "\"cancelledAt\":" + (cancelledAt == null ? "null" : "\"" + cancelledAt + "\"") + "}";
    }
}
