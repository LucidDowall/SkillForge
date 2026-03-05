package com.luciddowall.skillforge.engine;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Collection;

public final class Executor {

    private final ContextPool pool = new ContextPool();

    public void execute(Plan plan, Player caster, Location origin, int level) {
        CastContext ctx = pool.borrow();
        try {
            ctx.reset(caster, origin);

            int pc = 0;
            while (pc >= 0 && pc < plan.opcodes.length) {
                int op = plan.opcodes[pc];
                int ai = plan.argIndex[pc];

                switch (op) {
                    case Opcodes.TARGET_RAYCAST -> {
                        float range = plan.fArgs[ai];
                        RayTraceResult r = ctx.world.rayTrace(ctx.caster.getEyeLocation(), ctx.lookDir, range,
                                FluidCollisionMode.NEVER, true, 0.2,
                                (Entity ent) -> ent != ctx.caster);

                        ctx.targets.clear();
                        if (r != null && r.getHitEntity() != null) {
                            ctx.targets.add(r.getHitEntity());
                        }
                        pc = plan.nextPc[pc];
                    }

                    case Opcodes.TARGET_RADIUS -> {
                        float radius = plan.fArgs[ai];
                        ctx.targets.clear();
                        Collection<Entity> nearby = ctx.world.getNearbyEntities(ctx.origin, radius, radius, radius,
                                (e) -> e != ctx.caster);
                        for (Entity e : nearby) ctx.targets.add(e);
                        pc = plan.nextPc[pc];
                    }

                    case Opcodes.ACTION_DAMAGE -> {
                        float base = plan.fArgs[ai];
                        float amount = base + (level - 1) * (base * 0.15f);
                        for (Entity t : ctx.targets) {
                            if (t instanceof org.bukkit.entity.Damageable d) {
                                d.damage(amount, ctx.caster);
                            }
                        }
                        pc = plan.nextPc[pc];
                    }

                    case Opcodes.ACTION_DASH -> {
                        float strength = plan.fArgs[ai];
                        Vector v = ctx.lookDir.clone().multiply(strength);
                        // keep y modest so it doesn't become a jump exploit
                        v.setY(Math.min(0.25, Math.max(v.getY(), 0.05)));
                        ctx.caster.setVelocity(v);
                        pc = plan.nextPc[pc];
                    }

                    case Opcodes.EFFECT_PARTICLE_LINE -> {
                        Particle p = plan.particles[ai];
                        int count = plan.iArgs[ai];
                        Location a = ctx.caster.getEyeLocation();
                        Location b;
                        if (!ctx.targets.isEmpty()) {
                            b = ctx.targets.get(0).getLocation().add(0, 1.0, 0);
                        } else {
                            b = a.clone().add(ctx.lookDir.clone().multiply(10));
                        }
                        spawnLine(ctx.world, a, b, p, count);
                        pc = plan.nextPc[pc];
                    }

                    case Opcodes.EFFECT_PARTICLE_BURST -> {
                        Particle p = plan.particles[ai];
                        int count = plan.iArgs[ai];
                        ctx.world.spawnParticle(p, ctx.origin, count, 0.35, 0.35, 0.35, 0.02);
                        pc = plan.nextPc[pc];
                    }

                    case Opcodes.EFFECT_SOUND -> {
                        Sound s = plan.sounds[ai];
                        float vol = plan.fArgs[ai];
                        float pitch = plan.fArgs[ai + 1];
                        ctx.world.playSound(ctx.origin, s, vol, pitch);
                        pc = plan.nextPc[pc];
                    }

                    default -> pc = -1;
                }
            }

        } finally {
            pool.release(ctx);
        }
    }

    private void spawnLine(World world, Location a, Location b, Particle particle, int countPerStep) {
        Vector dir = b.toVector().subtract(a.toVector());
        double len = dir.length();
        if (len < 0.01) return;
        dir.multiply(1.0 / len);

        int steps = (int) Math.min(48, Math.ceil(len));
        Location cur = a.clone();
        for (int i = 0; i <= steps; i++) {
            world.spawnParticle(particle, cur, countPerStep, 0, 0, 0, 0);
            cur.add(dir);
        }
    }
}
