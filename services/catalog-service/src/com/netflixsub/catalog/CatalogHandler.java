package com.netflixsub.catalog;

import com.netflixsub.common.Http;
import com.netflixsub.common.Json;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class CatalogHandler implements HttpHandler {
    private final CatalogStore store;
    public CatalogHandler(CatalogStore s) { this.store = s; }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (Http.cors(ex)) return;
        String path = ex.getRequestURI().getPath();
        String[] parts = path.split("/");
        Map<String, String> q = parseQuery(ex.getRequestURI().getRawQuery());
        try {
            if (parts.length == 2) {
                Http.send(ex, 200, Json.array(store.filter(q.get("language"), q.get("genre"))));
                return;
            }
            if (parts.length == 3) {
                Movie m = store.findById(parts[2]);
                if (m == null) { Http.send(ex, 404, Json.err("Movie not found")); return; }
                Http.send(ex, 200, m.toString());
                return;
            }
            Http.send(ex, 400, Json.err("Bad request"));
        } catch (Exception e) {
            Http.send(ex, 500, Json.err("catalog error: " + e.getMessage()));
        }
    }

    private static Map<String, String> parseQuery(String q) {
        Map<String, String> m = new HashMap<>();
        if (q == null) return m;
        for (String pair : q.split("&")) {
            int i = pair.indexOf('=');
            if (i < 0) continue;
            m.put(URLDecoder.decode(pair.substring(0, i), StandardCharsets.UTF_8),
                  URLDecoder.decode(pair.substring(i + 1), StandardCharsets.UTF_8));
        }
        return m;
    }
}
