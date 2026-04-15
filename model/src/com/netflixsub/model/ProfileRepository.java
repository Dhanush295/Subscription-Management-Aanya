package com.netflixsub.model;

import com.netflixsub.user.Profile;
import java.sql.*;
import java.util.*;

public class ProfileRepository {

    public void upsert(Profile p) {
        try (PreparedStatement ps = Db.get().prepareStatement(
                "INSERT INTO profiles(user_id,first_name,last_name,age,email,created_at) VALUES (?,?,?,?,?,?) " +
                "ON CONFLICT (user_id) DO UPDATE SET first_name=EXCLUDED.first_name, last_name=EXCLUDED.last_name, age=EXCLUDED.age, email=EXCLUDED.email")) {
            ps.setString(1, p.userId);
            ps.setString(2, p.firstName);
            ps.setString(3, p.lastName);
            ps.setInt(4, p.age);
            ps.setString(5, p.email);
            ps.setTimestamp(6, Timestamp.from(p.createdAt));
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void delete(String userId) {
        try (PreparedStatement ps = Db.get().prepareStatement("DELETE FROM profiles WHERE user_id=?")) {
            ps.setString(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public List<Profile> all() {
        List<Profile> out = new ArrayList<>();
        try (Statement s = Db.get().createStatement();
             ResultSet rs = s.executeQuery("SELECT user_id,first_name,last_name,age,email,created_at FROM profiles")) {
            while (rs.next()) {
                out.add(new Profile(
                        rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getInt(4), rs.getString(5),
                        rs.getTimestamp(6).toInstant()));
            }
            return out;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
