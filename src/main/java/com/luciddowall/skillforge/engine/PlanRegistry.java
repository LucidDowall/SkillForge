package com.luciddowall.skillforge.engine;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class PlanRegistry {
    private final Map<String, Plan> plans = new ConcurrentHashMap<>();

    public Plan get(String id) {
        return plans.get(id);
    }

    public void put(String id, Plan plan) {
        plans.put(id, plan);
    }

    public void clear() {
        plans.clear();
    }

    public Set<String> ids() {
        return Collections.unmodifiableSet(plans.keySet());
    }
}
