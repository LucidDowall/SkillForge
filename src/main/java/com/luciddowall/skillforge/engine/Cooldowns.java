package com.luciddowall.skillforge.engine;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import java.util.UUID;

/**
 * Cooldown store with low GC pressure.
 * UUID keys are boxed objects, but map internals are primitive longs.
 */
public final class Cooldowns {
    private final Object2LongOpenHashMap<UUID> nextReadyNanos = new Object2LongOpenHashMap<>();

    public Cooldowns() {
        nextReadyNanos.defaultReturnValue(0L);
    }

    public boolean tryConsume(UUID casterId, String skillId, long nowNanos, long cooldownNanos) {
        // Combine caster + skill into one UUID-keyed map would need composite key.
        // For MVP: cooldown per (caster,skill) stored via synthetic UUID based on hash.
        UUID key = new UUID(casterId.getMostSignificantBits() ^ skillId.hashCode(), casterId.getLeastSignificantBits());
        long ready = nextReadyNanos.getLong(key);
        if (nowNanos < ready) return false;
        nextReadyNanos.put(key, nowNanos + cooldownNanos);
        return true;
    }

    public long remainingNanos(UUID casterId, String skillId, long nowNanos) {
        UUID key = new UUID(casterId.getMostSignificantBits() ^ skillId.hashCode(), casterId.getLeastSignificantBits());
        long ready = nextReadyNanos.getLong(key);
        return Math.max(0L, ready - nowNanos);
    }

    public void clear() {
        nextReadyNanos.clear();
    }
}
