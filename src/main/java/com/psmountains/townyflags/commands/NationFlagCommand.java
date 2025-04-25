// NationFlagCommand.java with added permission checks for preview/view/list

package com.psmountains.townyflags.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.psmountains.townyflags.FlagStorage;
import com.psmountains.townyflags.TownyFlags;
import com.psmountains.townyflags.AsciiFlagView;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class NationFlagCommand implements CommandExecutor, TabCompleter {
    private final TownyFlags plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("§cThis command can only be run by a player.");
            return true;
        }

        if (!plugin.getConfig().getBoolean("flags.enable-nation", true)) {
            p.sendMessage("§cNation flags are disabled.");
            return true;
        }

        if (args.length < 1) {
            p.sendMessage("§cUsage: /n flag <set|remove|info|preview|view|list>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "set" -> {
                if (args.length < 2) {
                    p.sendMessage("§cUsage: /n flag set <image>");
                    return true;
                }

                boolean allowed = isAllowed(p, "flags.nation-allowed-roles", "nation") || p.hasPermission("townyflags.nation.flag.set");
                if (!allowed) {
                    p.sendMessage("§cYou don’t have permission to set the nation flag.");
                    return true;
                }

                String imageName = args[1];
                File imageFile = new File(plugin.getFlagImageFolder(), imageName + ".png");
                if (!imageFile.exists()) {
                    p.sendMessage("§cImage not found: " + imageName + ".png");
                    return true;
                }

                Nation nation = TownyAPI.getInstance().getNation(p);
                if (nation == null) {
                    p.sendMessage("§cYou are not in a nation.");
                    return true;
                }

                FlagStorage.setNationFlag(nation, imageName);
                p.sendMessage("§aNation flag set to " + imageName + ".png");
                return true;
            }
            case "remove" -> {
                boolean allowed = isAllowed(p, "flags.nation-allowed-roles", "nation") || p.hasPermission("townyflags.nation.flag.remove");
                if (!allowed) {
                    p.sendMessage("§cYou don’t have permission to remove the nation flag.");
                    return true;
                }

                Nation nation = TownyAPI.getInstance().getNation(p);
                if (nation == null) {
                    p.sendMessage("§cYou are not in a nation.");
                    return true;
                }

                FlagStorage.removeNationFlag(nation);
                p.sendMessage("§aNation flag removed.");
                return true;
            }
            case "info" -> {
                Nation nation = TownyAPI.getInstance().getNation(p);
                if (nation == null) {
                    p.sendMessage("§cYou are not in a nation.");
                    return true;
                }

                String flag = FlagStorage.getNationFlag(nation.getUUID());
                if (flag == null) {
                    p.sendMessage("§7No nation flag set.");
                } else {
                    p.sendMessage("§7Nation flag: §e" + flag + ".png");
                }
                return true;
            }
            case "list" -> {
                if (!p.hasPermission("townyflags.nation.flag.list")) {
                    p.sendMessage("§cYou don’t have permission to list flag images.");
                    return true;
                }
                File[] files = plugin.getFlagImageFolder().listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
                if (files == null || files.length == 0) {
                    p.sendMessage("§7No flag images available.");
                    return true;
                }

                p.sendMessage("§7Available nation flag images:");
                for (File f : files) {
                    p.sendMessage("§8 - §f" + f.getName().replace(".png", ""));
                }
                return true;
            }
            case "preview" -> {
                if (!p.hasPermission("townyflags.nation.flag.preview")) {
                    p.sendMessage("§cYou don’t have permission to preview flags.");
                    return true;
                }
                if (args.length != 2) {
                    p.sendMessage("§cUsage: /n flag preview <image>");
                    return true;
                }
                String imageName = args[1];
                File file = new File(plugin.getFlagImageFolder(), imageName + ".png");
                if (!file.exists()) {
                    p.sendMessage("§cFlag not found: " + imageName + ".png");
                    return true;
                }
                p.sendMessage("§7Previewing: §e" + imageName + ".png");
                for (Component line : AsciiFlagView.generatePreviewRGB(file)) {
                    p.sendMessage(line);
                }
                return true;
            }
            case "view" -> {
                if (!p.hasPermission("townyflags.nation.flag.view")) {
                    p.sendMessage("§cYou don’t have permission to view nation flags.");
                    return true;
                }
                Nation nation = TownyAPI.getInstance().getNation(p);
                if (nation == null) {
                    p.sendMessage("§cYou are not in a nation.");
                    return true;
                }
                String imageName = FlagStorage.getNationFlag(nation.getUUID());
                if (imageName == null || imageName.isEmpty()) {
                    imageName = plugin.getConfig().getString("flags.default-nation-flag", "");
                }
                if (imageName.isEmpty()) {
                    p.sendMessage("§7No flag set.");
                    return true;
                }
                File file = new File(plugin.getFlagImageFolder(), imageName + ".png");
                if (!file.exists()) {
                    p.sendMessage("§cFlag file not found: " + imageName + ".png");
                    return true;
                }
                p.sendMessage("§7Current nation flag: §e" + imageName + ".png");
                for (Component line : AsciiFlagView.generatePreviewRGB(file)) {
                    p.sendMessage(line);
                }
                return true;
            }
            default -> {
                p.sendMessage("§cUnknown flag subcommand. Use /n flag set/remove/info/view/preview/list");
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("set", "remove", "info", "view", "preview", "list");

        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            File[] files = plugin.getFlagImageFolder().listFiles((dir, name) -> name.endsWith(".png"));
            if (files == null) return List.of();
            List<String> names = new ArrayList<>();
            for (File f : files) names.add(f.getName().replace(".png", ""));
            return names;
        }

        return List.of();
    }

    private boolean isAllowed(Player p, String configKey, String type) {
        var resident = TownyAPI.getInstance().getResident(p);
        if (resident == null) return false;

        List<String> allowed = plugin.getConfig().getStringList(configKey);
        if (allowed == null || allowed.isEmpty()) {
            allowed = type.equalsIgnoreCase("town") ? List.of("mayor", "assistant") : List.of("king", "assistant");
        }

        if (type.equalsIgnoreCase("town")) {
            var town = TownyAPI.getInstance().getTown(p);
            if (town == null) return false;
            for (String role : allowed) {
                if (role.equalsIgnoreCase("mayor") && town.isMayor(resident)) return true;
                if (resident.hasTownRank(role)) return true;
            }
        }

        if (type.equalsIgnoreCase("nation")) {
            var nation = TownyAPI.getInstance().getNation(p);
            if (nation == null) return false;
            for (String role : allowed) {
                if (role.equalsIgnoreCase("king") && nation.isKing(resident)) return true;
                if (resident.hasNationRank(role)) return true;
            }
        }

        return false;
    }
}
