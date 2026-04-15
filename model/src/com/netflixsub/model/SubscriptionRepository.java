package com.netflixsub.model;

import com.netflixsub.subscription.Subscription;
import java.sql.*;
import java.time.Instant;
import java.util.*;

public class SubscriptionRepository {

    public void upsert(Subscription s) {
        try (PreparedStatement ps = Db.get().prepareStatement(
                "INSERT INTO subscriptions(sub_id,user_id,plan_code,billing_cycle,status,started_at,expires_at,trial_ends_at,cancelled_at) " +
                "VALUES (?,?,?,?,?,?,?,?,?) " +
                "ON CONFLICT (sub_id) DO UPDATE SET plan_code=EXCLUDED.plan_code, billing_cycle=EXCLUDED.billing_cycle, status=EXCLUDED.status, " +
                "started_at=EXCLUDED.started_at, expires_at=EXCLUDED.expires_at, trial_ends_at=EXCLUDED.trial_ends_at, cancelled_at=EXCLUDED.cancelled_at")) {
            ps.setString(1, s.subId);
            ps.setString(2, s.userId);
            ps.setString(3, s.planCode);
            ps.setString(4, s.billingCycle);
            ps.setString(5, s.status);
            ps.setTimestamp(6, Timestamp.from(s.startedAt));
            ps.setTimestamp(7, Timestamp.from(s.expiresAt));
            ps.setTimestamp(8, s.trialEndsAt == null ? null : Timestamp.from(s.trialEndsAt));
            ps.setTimestamp(9, s.cancelledAt == null ? null : Timestamp.from(s.cancelledAt));
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void delete(String subId) {
        try (PreparedStatement ps = Db.get().prepareStatement("DELETE FROM subscriptions WHERE sub_id=?")) {
            ps.setString(1, subId);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public List<Subscription> all() {
        List<Subscription> out = new ArrayList<>();
        try (Statement s = Db.get().createStatement();
             ResultSet rs = s.executeQuery("SELECT sub_id,user_id,plan_code,billing_cycle,status,started_at,expires_at,trial_ends_at,cancelled_at FROM subscriptions")) {
            while (rs.next()) {
                Instant trial = rs.getTimestamp(8) == null ? null : rs.getTimestamp(8).toInstant();
                Instant cancel = rs.getTimestamp(9) == null ? null : rs.getTimestamp(9).toInstant();
                out.add(new Subscription(
                        rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getString(5),
                        rs.getTimestamp(6).toInstant(), rs.getTimestamp(7).toInstant(),
                        trial, cancel));
            }
            return out;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
