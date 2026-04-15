package com.netflixsub.common;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Json {
    private Json() {}

    public static String readBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static String field(String body, String key) {
        if (body == null) return null;
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
        Matcher m = p.matcher(body);
        if (m.find()) return unescape(m.group(1));
        Pattern p2 = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?|true|false|null)");
        Matcher m2 = p2.matcher(body);
        return m2.find() ? m2.group(1) : null;
    }

    public static String array(Collection<?> items) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object o : items) {
            if (!first) sb.append(",");
            sb.append(o.toString());
            first = false;
        }
        return sb.append("]").toString();
    }

    public static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static String unescape(String s) {
        return s.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    public static String err(String msg) {
        return "{\"error\":\"" + esc(msg) + "\"}";
    }

    public static String ok(String msg) {
        return "{\"ok\":true,\"message\":\"" + esc(msg) + "\"}";
    }
}
