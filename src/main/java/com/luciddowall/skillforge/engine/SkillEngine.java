package com.luciddowall.skillforge.engine;

import com.luciddowall.skillforge.SkillForgePlugin;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class SkillEngine {

    private final SkillForgePlugin plugin;
    private final PlanRegistry registry = new PlanRegistry();
    private final Executor executor = new Executor();

    private final ObjectArrayFIFOQueue<CastTask> queue = new ObjectArrayFIFOQueue<>();
    private final Cooldowns cooldowns = new Cooldowns();

    // Default: 1.5ms per tick for skill execution
    private volatile long tickBudgetNanos = 1_500_000L;

    public SkillEngine(SkillForgePlugin plugin) {
        this.plugin = plugin;

        plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    public PlanRegistry plans() { return registry; }

    public void setTickBudgetMillis(double ms) {
        this.tickBudgetNanos = Math.max(200_000L, (long) (ms * 1_000_000.0));
    }

    public double getTickBudgetMillis() {
        return tickBudgetNanos / 1_000_000.0;
    }

    public void castFromItem(Player caster, ItemStack item, String skillId, int level) {
        Plan plan = registry.get(skillId);
        if (plan == null) return;

        long now = System.nanoTime();
        long cdNanos = cooldownFor(skillId, level);
        if (!cooldowns.tryConsume(caster.getUniqueId(), skillId, now, cdNanos)) return;

        // Keep origin minimal: use player location snapshot
        Location origin = caster.getLocation().clone();
        queue.enqueue(new CastTask(caster.getUniqueId(), origin, plan, level));
    }

    public void castDirect(Player caster, String skillId, int level) {
        Plan plan = registry.get(skillId);
        if (plan == null) return;
        Location origin = caster.getLocation().clone();
        queue.enqueue(new CastTask(caster.getUniqueId(), origin, plan, level));
    }

    private void tick() {
        long start = System.nanoTime();
        long deadline = start + tickBudgetNanos;

        while (!queue.isEmpty()) {
            if (System.nanoTime() >= deadline) break;

            CastTask task = queue.dequeue();
            Player caster = plugin.getServer().getPlayer(task.casterId());
            if (caster == null || !caster.isOnline()) continue;

            executor.execute(task.plan(), caster, task.origin(), task.level());
        }
    }

    private long cooldownFor(String skillId, int level) {
        // Simple, predictable: higher level reduces cooldown slightly
        // firebolt: 1.5s base, dash: 2.0s base, shock: 3.0s base
        long baseMs = switch (skillId) {
            case "firebolt" -> 1500L;
            case "dash" -> 2000L;
            case "shock" -> 3000L;
            default -> 1500L;
        };
        double scale = Math.max(0.70, 1.0 - (level - 1) * 0.05);
        return (long) (baseMs * scale) * 1_000_000L;
    }

    public void registerBuiltinSkills() {
        registry.clear();

        // firebolt: raycast -> damage -> particle line -> sound
        registry.put("firebolt", new Plan(
                "firebolt",
                new int[]{
                        Opcodes.TARGET_RAYCAST,
                        Opcodes.ACTION_DAMAGE,
                        Opcodes.EFFECT_PARTICLE_LINE,
                        Opcodes.EFFECT_SOUND
                },
                new int[]{1, 2, 3, -1},
                new int[]{0, 1, 2, 3},
                new float[]{24.0f, 6.0f, 0f, 1.0f, 1.2f},
                new int[]{0, 0, 2, 0},
                new Particle[]{null, null, Particle.FLAME, null},
                new Sound[]{null, null, null, Sound.ENTITY_BLAZE_SHOOT}
        ));

        // dash: dash -> particle burst -> sound
        registry.put("dash", new Plan(
                "dash",
                new int[]{
                        Opcodes.ACTION_DASH,
                        Opcodes.EFFECT_PARTICLE_BURST,
                        Opcodes.EFFECT_SOUND
                },
                new int[]{1, 2, -1},
                new int[]{0, 1, 2},
                new float[]{1.25f, 0f, 1.0f, 1.6f},
                new int[]{0, 18, 0},
                new Particle[]{null, Particle.CLOUD, null},
                new Sound[]{null, null, Sound.ENTITY_ENDERMAN_TELEPORT}
        ));

        // shock: radius -> damage -> burst -> sound
        registry.put("shock", new Plan(
                "shock",
                new int[]{
                        Opcodes.TARGET_RADIUS,
                        Opcodes.ACTION_DAMAGE,
                        Opcodes.EFFECT_PARTICLE_BURST,
                        Opcodes.EFFECT_SOUND
                },
                new int[]{1, 2, 3, -1},
                new int[]{0, 1, 2, 3},
                new float[]{4.0f, 4.0f, 0f, 1.0f, 0.8f},
                new int[]{0, 0, 28, 0},
                new Particle[]{null, null, Particle.ELECTRIC_SPARK, null},
                new Sound[]{null, null, null, Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED}
        ));
    }

    public void shutdown() {
        queue.clear();
        cooldowns.clear();
    }
}
