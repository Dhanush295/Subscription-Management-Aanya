package com.netflixsub.payment;

import com.netflixsub.common.Ids;
import com.netflixsub.common.Json;
import java.time.Instant;

public class Payment {
    public final String paymentId;
    public final String userId;
    public final String subId;
    public final double amount;
    public final String cardLast4;
    public Instant createdAt = Instant.now();
    public final String status;
    public final String failureReason;

    public Payment(String userId, String subId, double amount, String cardLast4, String status, String reason) {
        this.paymentId = Ids.shortId();
        this.userId = userId;
        this.subId = subId;
        this.amount = amount;
        this.cardLast4 = cardLast4;
        this.status = status;
        this.failureReason = reason;
    }

    public Payment(String paymentId, String userId, String subId, double amount,
                   String cardLast4, String status, String reason, Instant createdAt) {
        this.paymentId = paymentId;
        this.userId = userId;
        this.subId = subId;
        this.amount = amount;
        this.cardLast4 = cardLast4;
        this.status = status;
        this.failureReason = reason;
        if (createdAt != null) this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "{\"paymentId\":\"" + paymentId + "\","
             + "\"userId\":\"" + userId + "\","
             + "\"subId\":\"" + subId + "\","
             + "\"amount\":" + amount + ","
             + "\"cardLast4\":\"" + Json.esc(cardLast4) + "\","
             + "\"status\":\"" + status + "\","
             + "\"failureReason\":" + (failureReason == null ? "null" : "\"" + Json.esc(failureReason) + "\"") + ","
             + "\"createdAt\":\"" + createdAt + "\"}";
    }
}
