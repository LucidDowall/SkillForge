package com.luciddowall.skillforge;

import com.luciddowall.skillforge.commands.SfCommand;
import com.luciddowall.skillforge.engine.SkillEngine;
import com.luciddowall.skillforge.items.ItemSkillBinder;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class SkillForgePlugin extends JavaPlugin {

    public NamespacedKey KEY_SKILL_ID;
    public NamespacedKey KEY_SKILL_LVL;

    private SkillEngine engine;

    @Override
    public void onEnable() {
        KEY_SKILL_ID = new NamespacedKey(this, "skill_id");
        KEY_SKILL_LVL = new NamespacedKey(this, "skill_lvl");

        engine = new SkillEngine(this);
        engine.registerBuiltinSkills();

        getServer().getPluginManager().registerEvents(new ItemSkillBinder(this, engine), this);

        SfCommand cmd = new SfCommand(this, engine);
        if (getCommand("sf") != null) {
            getCommand("sf").setExecutor(cmd);
            getCommand("sf").setTabCompleter(cmd);
        }

        getLogger().info("SkillForge enabled");
    }

    @Override
    public void onDisable() {
        if (engine != null) engine.shutdown();
        getLogger().info("SkillForge disabled");
    }
}
