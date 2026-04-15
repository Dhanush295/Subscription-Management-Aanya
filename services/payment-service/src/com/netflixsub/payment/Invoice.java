package com.netflixsub.payment;

import com.netflixsub.common.Ids;
import java.time.Instant;

public class Invoice {
    public final String invoiceId;
    public final String userId;
    public final String subId;
    public final String paymentId;
    public final double amount;
    public Instant issuedAt = Instant.now();

    public Invoice(String userId, String subId, String paymentId, double amount) {
        this.invoiceId = "INV-" + Ids.shortId().toUpperCase();
        this.userId = userId;
        this.subId = subId;
        this.paymentId = paymentId;
        this.amount = amount;
    }

    public Invoice(String invoiceId, String userId, String subId, String paymentId, double amount, Instant issuedAt) {
        this.invoiceId = invoiceId;
        this.userId = userId;
        this.subId = subId;
        this.paymentId = paymentId;
        this.amount = amount;
        if (issuedAt != null) this.issuedAt = issuedAt;
    }

    @Override
    public String toString() {
        return "{\"invoiceId\":\"" + invoiceId + "\","
             + "\"userId\":\"" + userId + "\","
             + "\"subId\":\"" + subId + "\","
             + "\"paymentId\":\"" + paymentId + "\","
             + "\"amount\":" + amount + ","
             + "\"issuedAt\":\"" + issuedAt + "\"}";
    }
}
