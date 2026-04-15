package com.netflixsub.payment;

import com.netflixsub.common.Http;
import com.netflixsub.common.Json;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class PaymentHandler implements HttpHandler {
    private final PaymentProcessor proc;
    public PaymentHandler(PaymentProcessor p) { this.proc = p; }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (Http.cors(ex)) return;
        String path = ex.getRequestURI().getPath();
        try {
            if (path.equals("/payments/charge"))           { charge(ex); return; }
            if (path.equals("/payments"))                  { Http.send(ex, 200, Json.array(proc.payments())); return; }
            if (path.startsWith("/payments/user/"))        { Http.send(ex, 200, Json.array(proc.paymentsForUser(path.substring("/payments/user/".length())))); return; }
            if (path.equals("/invoices"))                  { Http.send(ex, 200, Json.array(proc.invoices())); return; }
            if (path.startsWith("/invoices/user/"))        { Http.send(ex, 200, Json.array(proc.invoicesForUser(path.substring("/invoices/user/".length())))); return; }
            Http.send(ex, 404, Json.err("Not found"));
        } catch (Exception e) {
            Http.send(ex, 500, Json.err("payment error: " + e.getMessage()));
        }
    }

    private void charge(HttpExchange ex) throws IOException {
        String body = Json.readBody(ex);
        String userId = Json.field(body, "userId");
        String subId = Json.field(body, "subId");
        String cardLast4 = Json.field(body, "cardLast4");
        double amount = parseD(Json.field(body, "amount"));
        if (userId == null || subId == null || amount <= 0) { Http.send(ex, 400, Json.err("userId, subId, amount required")); return; }
        var res = proc.charge(userId, subId, amount, cardLast4);
        Http.send(ex, res.success ? 200 : 402, res.payment.toString());
    }

    private static double parseD(String v) {
        try { return v == null ? 0 : Double.parseDouble(v); } catch (Exception e) { return 0; }
    }
}
