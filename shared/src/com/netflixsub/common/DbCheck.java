package com.netflixsub.common;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class DbCheck {
    public static void main(String[] args) {
        try {
            Map<String, String> env = loadEnv(Path.of(".env"));
            String url      = env.get("SUPABASE_DB_URL");
            String user     = env.getOrDefault("SUPABASE_DB_USER", "postgres");
            String password = env.get("SUPABASE_DB_PASSWORD");

            if (url != null && !url.startsWith("jdbc:")) {
                url = "jdbc:" + url;
            }
            if (url != null && !url.contains("sslmode=")) {
                url += (url.contains("?") ? "&" : "?") + "sslmode=require";
            }

            if (url == null || password == null) {
                System.err.println("FAIL: .env is missing SUPABASE_DB_URL or SUPABASE_DB_PASSWORD");
                System.exit(2);
            }

            System.out.println("Connecting to: " + url);
            System.out.println("User         : " + user);
            System.out.println();

            long t0 = System.currentTimeMillis();
            try (Connection c = DriverManager.getConnection(url, user, password);
                 Statement s = c.createStatement();
                 ResultSet rs = s.executeQuery("SELECT current_user, version()")) {
                if (rs.next()) {
                    long ms = System.currentTimeMillis() - t0;
                    System.out.println("Connected in " + ms + " ms.");
                    System.out.println("  user   : " + rs.getString(1));
                    System.out.println("  version: " + rs.getString(2));
                    System.exit(0);
                }
                System.err.println("FAIL: query returned no rows");
                System.exit(3);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("FAIL: Postgres JDBC driver not on classpath.");
            System.err.println("  Drop postgresql-42.7.4.jar into lib/ and retry.");
            System.exit(4);
        } catch (java.net.UnknownHostException e) {
            System.err.println("FAIL: UnknownHostException -> " + e.getMessage());
            System.err.println("  Hint: network cannot resolve the Supabase host.");
            System.err.println("  Try the Session Pooler instead (Supabase dashboard > Database > Connection string > Session pooler).");
            System.exit(5);
        } catch (java.sql.SQLException e) {
            System.err.println("FAIL: " + e.getClass().getSimpleName() + " -> " + e.getMessage());
            Throwable root = e;
            while (root.getCause() != null && root.getCause() != root) root = root.getCause();
            if (root != e) {
                System.err.println("  root: " + root.getClass().getName() + " -> " + root.getMessage());
            }
            String msg = ((e.getMessage() == null ? "" : e.getMessage()) + " " + (root.getMessage() == null ? "" : root.getMessage())).toLowerCase();
            if (msg.contains("password authentication failed")) {
                System.err.println("  Hint: password mismatch. Reset in Supabase dashboard and update .env.");
            } else if (msg.contains("ssl")) {
                System.err.println("  Hint: SSL negotiation failed. Remove sslmode=require from the URL, or check your network proxy.");
            } else if (msg.contains("timeout") || msg.contains("refused")) {
                System.err.println("  Hint: port 5432 appears blocked. Switch to the Session Pooler on 6543.");
            } else if (msg.contains("tenant or user not found")) {
                System.err.println("  Hint: when using the pooler, the user must be postgres.<project-ref>, e.g. postgres.lodeplislsmkvjewisvi.");
            }
            System.exit(1);
        } catch (Exception e) {
            System.err.println("FAIL: " + e.getClass().getSimpleName() + " -> " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static Map<String, String> loadEnv(Path path) throws Exception {
        Map<String, String> out = new HashMap<>();
        if (!Files.exists(path)) return out;
        for (String line : Files.readAllLines(path)) {
            String t = line.trim();
            if (t.isEmpty() || t.startsWith("#")) continue;
            int eq = t.indexOf('=');
            if (eq <= 0) continue;
            String k = t.substring(0, eq).trim();
            String v = t.substring(eq + 1).trim();
            if (v.length() >= 2 && (v.startsWith("\"") && v.endsWith("\"") || v.startsWith("'") && v.endsWith("'"))) {
                v = v.substring(1, v.length() - 1);
            }
            out.put(k, v);
        }
        return out;
    }
}
