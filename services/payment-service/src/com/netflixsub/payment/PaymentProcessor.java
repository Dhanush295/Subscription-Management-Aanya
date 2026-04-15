package com.netflixsub.payment;

import com.netflixsub.model.PaymentRepository;
import java.util.ArrayList;
import java.util.List;

public class PaymentProcessor {
    private final List<Payment> payments = new ArrayList<>();
    private final List<Invoice> invoices = new ArrayList<>();
    private final PaymentRepository repo = new PaymentRepository();

    public PaymentProcessor() {
        payments.addAll(repo.allPayments());
        invoices.addAll(repo.allInvoices());
    }

    public List<Payment> payments() { return payments; }
    public List<Invoice> invoices() { return invoices; }

    public List<Payment> paymentsForUser(String userId) {
        List<Payment> out = new ArrayList<>();
        for (Payment p : payments) if (p.userId.equals(userId)) out.add(p);
        return out;
    }

    public List<Invoice> invoicesForUser(String userId) {
        List<Invoice> out = new ArrayList<>();
        for (Invoice i : invoices) if (i.userId.equals(userId)) out.add(i);
        return out;
    }

    public Result charge(String userId, String subId, double amount, String cardLast4) {
        String last4 = cardLast4 == null ? "0000" : cardLast4;
        boolean fail = "0000".equals(last4) || "4444".equals(last4);
        String status = fail ? "FAILED" : "SUCCESS";
        String reason = fail ? "Card declined (test card)" : null;
        Payment p = new Payment(userId, subId, amount, last4, status, reason);
        payments.add(p);
        repo.insertPayment(p);
        Invoice inv = null;
        if (!fail) {
            inv = new Invoice(userId, subId, p.paymentId, amount);
            invoices.add(inv);
            repo.insertInvoice(inv);
        }
        return new Result(p, inv, !fail);
    }

    public static class Result {
        public final Payment payment;
        public final Invoice invoice;
        public final boolean success;
        public Result(Payment p, Invoice i, boolean ok) { this.payment = p; this.invoice = i; this.success = ok; }
    }
}
