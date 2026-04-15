package com.netflixsub.model;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

public final class Db {
    private static Connection conn;
    private static String url, user, password;

    private Db() {}

    public static synchronized void init() throws Exception {
        Map<String, String> env = loadEnv(Path.of(".env"));
        url      = env.get("SUPABASE_DB_URL");
        user     = env.getOrDefault("SUPABASE_DB_USER", "postgres");
        password = env.get("SUPABASE_DB_PASSWORD");
        if (url == null || password == null) {
            throw new IllegalStateException(".env missing SUPABASE_DB_URL or SUPABASE_DB_PASSWORD");
        }
        if (!url.startsWith("jdbc:")) url = "jdbc:" + url;
        if (!url.contains("sslmode=")) url += (url.contains("?") ? "&" : "?") + "sslmode=require";
        Class.forName("org.postgresql.Driver");
        conn = DriverManager.getConnection(url, user, password);
        conn.setAutoCommit(true);
    }

    public static synchronized Connection get() {
        try {
            if (conn == null || conn.isClosed() || !conn.isValid(2)) {
                conn = DriverManager.getConnection(url, user, password);
                conn.setAutoCommit(true);
            }
            return conn;
        } catch (Exception e) {
            throw new RuntimeException("DB unavailable: " + e.getMessage(), e);
        }
    }

    public static void close() {
        try { if (conn != null) conn.close(); } catch (Exception ignored) {}
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
