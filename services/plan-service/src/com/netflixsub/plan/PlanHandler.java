package com.netflixsub.plan;

import com.netflixsub.common.Http;
import com.netflixsub.common.Json;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlanHandler implements HttpHandler {
    private final PlanStore store;
    public PlanHandler(PlanStore s) { this.store = s; }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (Http.cors(ex)) return;
        String path = ex.getRequestURI().getPath();
        String[] parts = path.split("/");
        try {
            if (parts.length == 2) {
                List<Plan> list = new ArrayList<>();
                store.all().forEach(list::add);
                Http.send(ex, 200, Json.array(list));
                return;
            }
            if (parts.length == 3) {
                var p = store.get(parts[2]);
                if (p.isEmpty()) { Http.send(ex, 404, Json.err("Plan not found")); return; }
                Http.send(ex, 200, p.get().toString());
                return;
            }
            Http.send(ex, 400, Json.err("Bad request"));
        } catch (Exception e) {
            Http.send(ex, 500, Json.err("plan error: " + e.getMessage()));
        }
    }
}
