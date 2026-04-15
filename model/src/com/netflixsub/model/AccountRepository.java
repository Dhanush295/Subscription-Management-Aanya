package com.netflixsub.model;

import com.netflixsub.auth.Account;
import java.sql.*;
import java.util.*;

public class AccountRepository {

    public void upsert(Account a) {
        try (PreparedStatement ps = Db.get().prepareStatement(
                "INSERT INTO accounts(user_id,email,password_hash,role) VALUES (?,?,?,?) " +
                "ON CONFLICT (user_id) DO UPDATE SET email=EXCLUDED.email, password_hash=EXCLUDED.password_hash, role=EXCLUDED.role")) {
            ps.setString(1, a.userId);
            ps.setString(2, a.email);
            ps.setString(3, a.passwordHash);
            ps.setString(4, a.role);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void delete(String userId) {
        try (PreparedStatement ps = Db.get().prepareStatement("DELETE FROM accounts WHERE user_id=?")) {
            ps.setString(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public List<Account> all() {
        List<Account> out = new ArrayList<>();
        try (Statement s = Db.get().createStatement();
             ResultSet rs = s.executeQuery("SELECT user_id,email,password_hash,role FROM accounts")) {
            while (rs.next()) {
                out.add(new Account(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)));
            }
            return out;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void saveToken(String token, String userId) {
        try (PreparedStatement ps = Db.get().prepareStatement(
                "INSERT INTO sessions(token,user_id) VALUES (?,?) ON CONFLICT (token) DO NOTHING")) {
            ps.setString(1, token);
            ps.setString(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void revokeToken(String token) {
        try (PreparedStatement ps = Db.get().prepareStatement("DELETE FROM sessions WHERE token=?")) {
            ps.setString(1, token);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public Map<String, String> loadTokens() {
        Map<String, String> out = new HashMap<>();
        try (Statement s = Db.get().createStatement();
             ResultSet rs = s.executeQuery("SELECT token,user_id FROM sessions")) {
            while (rs.next()) out.put(rs.getString(1), rs.getString(2));
            return out;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

}
