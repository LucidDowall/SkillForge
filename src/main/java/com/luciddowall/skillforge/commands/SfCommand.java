package com.luciddowall.skillforge.commands;

import com.luciddowall.skillforge.SkillForgePlugin;
import com.luciddowall.skillforge.engine.SkillEngine;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class SfCommand implements CommandExecutor, TabCompleter {

    private final SkillForgePlugin plugin;
    private final SkillEngine engine;

    public SfCommand(SkillForgePlugin plugin, SkillEngine engine) {
        this.plugin = plugin;
        this.engine = engine;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("sf.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(ChatColor.GOLD + "SkillForge commands:");
            sender.sendMessage(ChatColor.YELLOW + "/sf give <skillId> [level]" + ChatColor.GRAY + " - give bound item");
            sender.sendMessage(ChatColor.YELLOW + "/sf test <skillId> [level]" + ChatColor.GRAY + " - cast directly");
            sender.sendMessage(ChatColor.YELLOW + "/sf budget <ms>" + ChatColor.GRAY + " - set tick budget (default " + engine.getTickBudgetMillis() + "ms)");
            sender.sendMessage(ChatColor.YELLOW + "/sf reload" + ChatColor.GRAY + " - reload built-in skills");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                engine.registerBuiltinSkills();
                sender.sendMessage(ChatColor.GREEN + "Reloaded built-in skills.");
                return true;
            }

            case "budget" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /sf budget <ms>");
                    return true;
                }
                try {
                    double ms = Double.parseDouble(args[1]);
                    engine.setTickBudgetMillis(ms);
                    sender.sendMessage(ChatColor.GREEN + "Tick budget set to " + engine.getTickBudgetMillis() + "ms");
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid number.");
                }
                return true;
            }

            case "give" -> {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /sf give <skillId> [level]");
                    return true;
                }

                String skillId = args[1];
                int level = 1;
                if (args.length >= 3) {
                    try { level = Math.max(1, Integer.parseInt(args[2])); } catch (NumberFormatException ignored) {}
                }

                ItemStack wand = new ItemStack(Material.BLAZE_ROD);
                ItemMeta meta = wand.getItemMeta();
                meta.setDisplayName(ChatColor.AQUA + "Skill: " + skillId + " " + ChatColor.GRAY + "(Lv." + level + ")");
                meta.getPersistentDataContainer().set(plugin.KEY_SKILL_ID, PersistentDataType.STRING, skillId);
                meta.getPersistentDataContainer().set(plugin.KEY_SKILL_LVL, PersistentDataType.INTEGER, level);
                wand.setItemMeta(meta);

                p.getInventory().addItem(wand);
                p.sendMessage(ChatColor.GREEN + "Given item-bound skill: " + skillId + " (Lv." + level + ")");
                return true;
            }

            case "test" -> {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /sf test <skillId> [level]");
                    return true;
                }
                String skillId = args[1];
                int level = 1;
                if (args.length >= 3) {
                    try { level = Math.max(1, Integer.parseInt(args[2])); } catch (NumberFormatException ignored) {}
                }
                engine.castDirect(p, skillId, level);
                return true;
            }

            default -> {
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /sf help");
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("sf.admin")) return List.of();

        if (args.length == 1) {
            return filter(List.of("help", "give", "test", "budget", "reload"), args[0]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("test"))) {
            Set<String> ids = engine.plans().ids();
            return filter(new ArrayList<>(ids), args[1]);
        }
        return List.of();
    }

    private List<String> filter(List<String> base, String prefix) {
        String p = prefix.toLowerCase();
        List<String> out = new ArrayList<>();
        for (String s : base) if (s.toLowerCase().startsWith(p)) out.add(s);
        return out;
    }
}
