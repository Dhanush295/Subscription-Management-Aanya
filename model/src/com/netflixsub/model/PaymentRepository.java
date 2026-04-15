package com.netflixsub.model;

import com.netflixsub.payment.Invoice;
import com.netflixsub.payment.Payment;
import java.sql.*;
import java.util.*;

public class PaymentRepository {

    public void insertPayment(Payment p) {
        try (PreparedStatement ps = Db.get().prepareStatement(
                "INSERT INTO payments(payment_id,user_id,sub_id,amount,card_last4,status,failure_reason,created_at) VALUES (?,?,?,?,?,?,?,?)")) {
            ps.setString(1, p.paymentId);
            ps.setString(2, p.userId);
            ps.setString(3, p.subId);
            ps.setDouble(4, p.amount);
            ps.setString(5, p.cardLast4);
            ps.setString(6, p.status);
            ps.setString(7, p.failureReason);
            ps.setTimestamp(8, Timestamp.from(p.createdAt));
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void insertInvoice(Invoice i) {
        try (PreparedStatement ps = Db.get().prepareStatement(
                "INSERT INTO invoices(invoice_id,user_id,sub_id,payment_id,amount,issued_at) VALUES (?,?,?,?,?,?)")) {
            ps.setString(1, i.invoiceId);
            ps.setString(2, i.userId);
            ps.setString(3, i.subId);
            ps.setString(4, i.paymentId);
            ps.setDouble(5, i.amount);
            ps.setTimestamp(6, Timestamp.from(i.issuedAt));
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public List<Payment> allPayments() {
        List<Payment> out = new ArrayList<>();
        try (Statement s = Db.get().createStatement();
             ResultSet rs = s.executeQuery("SELECT payment_id,user_id,sub_id,amount,card_last4,status,failure_reason,created_at FROM payments ORDER BY created_at")) {
            while (rs.next()) {
                out.add(new Payment(rs.getString(1), rs.getString(2), rs.getString(3), rs.getDouble(4),
                        rs.getString(5), rs.getString(6), rs.getString(7),
                        rs.getTimestamp(8).toInstant()));
            }
            return out;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public List<Invoice> allInvoices() {
        List<Invoice> out = new ArrayList<>();
        try (Statement s = Db.get().createStatement();
             ResultSet rs = s.executeQuery("SELECT invoice_id,user_id,sub_id,payment_id,amount,issued_at FROM invoices ORDER BY issued_at")) {
            while (rs.next()) {
                out.add(new Invoice(rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getDouble(5),
                        rs.getTimestamp(6).toInstant()));
            }
            return out;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
