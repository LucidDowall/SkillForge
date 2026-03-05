package com.luciddowall.skillforge.items;

import com.luciddowall.skillforge.SkillForgePlugin;
import com.luciddowall.skillforge.engine.SkillEngine;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class ItemSkillBinder implements Listener {

    private final SkillForgePlugin plugin;
    private final SkillEngine engine;

    public ItemSkillBinder(SkillForgePlugin plugin, SkillEngine engine) {
        this.plugin = plugin;
        this.engine = engine;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        Action a = e.getAction();
        if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = e.getItem();
        if (item == null || !item.hasItemMeta()) return;

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String skillId = pdc.get(plugin.KEY_SKILL_ID, PersistentDataType.STRING);
        if (skillId == null || skillId.isBlank()) return;

        Integer lvl = pdc.get(plugin.KEY_SKILL_LVL, PersistentDataType.INTEGER);
        int level = (lvl == null) ? 1 : Math.max(1, lvl);

        Player caster = e.getPlayer();
        engine.castFromItem(caster, item, skillId, level);
    }
}
