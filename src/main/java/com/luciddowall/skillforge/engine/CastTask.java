package com.luciddowall.skillforge.engine;

import org.bukkit.Location;

import java.util.UUID;

public final class CastTask {
    private final UUID casterId;
    private final Location origin;
    private final Plan plan;
    private final int level;

    public CastTask(UUID casterId, Location origin, Plan plan, int level) {
        this.casterId = casterId;
        this.origin = origin;
        this.plan = plan;
        this.level = level;
    }

    public UUID casterId() { return casterId; }
    public Location origin() { return origin; }
    public Plan plan() { return plan; }
    public int level() { return level; }
}
