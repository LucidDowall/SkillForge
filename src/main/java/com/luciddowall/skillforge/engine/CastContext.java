package com.luciddowall.skillforge.engine;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class CastContext {
    public Player caster;
    public World world;
    public Location origin;
    public Vector lookDir;
    public final ObjectArrayList<Entity> targets = new ObjectArrayList<>(8);

    public void reset(Player caster, Location origin) {
        this.caster = caster;
        this.world = caster.getWorld();
        this.origin = origin;
        this.lookDir = caster.getLocation().getDirection();
        this.targets.clear();
    }
}
