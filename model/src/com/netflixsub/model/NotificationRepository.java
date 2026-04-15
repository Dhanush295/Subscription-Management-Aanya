package com.netflixsub.model;

import com.netflixsub.notification.Notification;
import java.sql.*;
import java.util.*;

public class NotificationRepository {

    public void insert(Notification n) {
        try (PreparedStatement ps = Db.get().prepareStatement(
                "INSERT INTO notifications(id,user_id,type,message,created_at) VALUES (?,?,?,?,?)")) {
            ps.setString(1, n.id);
            ps.setString(2, n.userId);
            ps.setString(3, n.type);
            ps.setString(4, n.message);
            ps.setTimestamp(5, Timestamp.from(n.sentAt));
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public List<Notification> all() {
        List<Notification> out = new ArrayList<>();
        try (Statement s = Db.get().createStatement();
             ResultSet rs = s.executeQuery("SELECT id,user_id,type,message,created_at FROM notifications ORDER BY created_at")) {
            while (rs.next()) {
                out.add(new Notification(rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getTimestamp(5).toInstant()));
            }
            return out;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
