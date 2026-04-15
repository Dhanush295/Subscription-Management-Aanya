package com.netflixsub.plan;

import com.netflixsub.model.PlanRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class PlanStore {
    private final Map<String, Plan> plans = new LinkedHashMap<>();

    public PlanStore() {
        for (Plan p : new PlanRepository().all()) plans.put(p.code, p);
    }

    public Iterable<Plan> all() { return plans.values(); }
    public Optional<Plan> get(String code) {
        return Optional.ofNullable(code == null ? null : plans.get(code.toUpperCase()));
    }
}
