package com.netflixsub.model;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

public final class Schema {
    private Schema() {}

    public static void applyAndSeed(String adminPasswordHash) throws Exception {
        runFile(Path.of("model", "sql", "schema.sql"));
        runFile(Path.of("model", "sql", "seed.sql"));
        try (var ps = Db.get().prepareStatement(
                "UPDATE accounts SET password_hash = ? WHERE user_id = ? AND password_hash = 'PLACEHOLDER'")) {
            ps.setString(1, adminPasswordHash);
            ps.setString(2, "admin000");
            ps.executeUpdate();
        }
    }

    private static void runFile(Path p) throws Exception {
        if (!Files.exists(p)) throw new IllegalStateException("Missing SQL file: " + p.toAbsolutePath());
        String sql = Files.readString(p);
        Connection c = Db.get();
        try (Statement s = c.createStatement()) {
            for (String stmt : splitStatements(sql)) {
                String t = stmt.trim();
                if (t.isEmpty()) continue;
                s.execute(t);
            }
        }
    }

    private static java.util.List<String> splitStatements(String sql) {
        java.util.List<String> out = new java.util.ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inSingle = false;
        for (int i = 0; i < sql.length(); i++) {
            char ch = sql.charAt(i);
            if (ch == '\'' && (i == 0 || sql.charAt(i - 1) != '\\')) inSingle = !inSingle;
            if (ch == '-' && i + 1 < sql.length() && sql.charAt(i + 1) == '-' && !inSingle) {
                while (i < sql.length() && sql.charAt(i) != '\n') i++;
                continue;
            }
            if (ch == ';' && !inSingle) { out.add(cur.toString()); cur.setLength(0); }
            else cur.append(ch);
        }
        if (cur.length() > 0) out.add(cur.toString());
        return out;
    }
}
