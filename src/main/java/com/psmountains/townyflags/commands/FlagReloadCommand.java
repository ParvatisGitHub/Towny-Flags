package com.psmountains.townyflags.commands;

import com.psmountains.townyflags.TownyFlags;
import com.psmountains.townyflags.FlagStorage;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public class FlagReloadCommand implements CommandExecutor {

    private final TownyFlags plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("townyflags.admin.reload")) {
            sender.sendMessage("§cYou do not have permission to reload the config.");
            return true;
        }

        plugin.reloadConfig();
        plugin.copyFlagsToDynmapWebDir();
        plugin.refreshDynmapMarkers();
        sender.sendMessage("§aFlag config reloaded.");
        return true;
    }
}
