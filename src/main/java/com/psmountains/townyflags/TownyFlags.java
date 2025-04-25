package com.psmountains.townyflags;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import java.io.File;
import com.psmountains.townyflags.commands.*;
import com.psmountains.townyflags.listeners.*;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI.CommandType;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.TownyAPI;
import org.dynmap.towny.events.BuildTownMarkerDescriptionEvent;
import java.util.List;

@Slf4j
@Getter
public final class TownyFlags extends JavaPlugin {

    private File flagImageFolder;
    private boolean dynmapEnabled;

    @Override
    public void onEnable() {
        dynmapEnabled = Bukkit.getPluginManager().isPluginEnabled("dynmap")
            && Bukkit.getPluginManager().isPluginEnabled("Dynmap-Towny");
		log.info("TownyFlags Dynmap Integration Enabled.");
        createFlagImageDirectory();

        if (dynmapEnabled) {
            
			copyFlagsToDynmapWebDir();
        }

        TownyCommandAddonAPI.addSubCommand(CommandType.TOWN, "flag", new TownFlagCommand(this));
        TownyCommandAddonAPI.addSubCommand(CommandType.NATION, "flag", new NationFlagCommand(this));
        TownyCommandAddonAPI.addSubCommand(CommandType.TOWNYADMIN, "flagreload", new FlagReloadCommand(this));
        TownyCommandAddonAPI.addSubCommand(CommandType.TOWNYADMIN, "flag", new FlagAdminCommand(this));

        log.info("TownyFlagAddon enabled.");

        getServer().getPluginManager().registerEvents(new DynmapFlagPlaceholderListener(this), this);
        getServer().getPluginManager().registerEvents(new TownFlagStatusListener(this), this);
        getServer().getPluginManager().registerEvents(new NationFlagStatusListener(this), this);

        printSickASCIIArt();
        saveDefaultConfig();
    }

    public int getTownFlagHeight() {
        return getConfig().getInt("flags.town-height", 64);
    }

    public int getNationFlagHeight() {
        return getConfig().getInt("flags.nation-height", 80);
    }

    private void createFlagImageDirectory() {
        flagImageFolder = new File(getDataFolder(), "flag-images");

        if (!flagImageFolder.exists()) {
            if (flagImageFolder.mkdirs()) {
                log.info("Created flag-images directory.");

                // First-run: load default starter flags
                String[] defaults = {
                    "flag-images/blue.png",
                    "flag-images/red.png",
                    "flag-images/green.png",
                    "flag-images/checkered.png",
                    "flag-images/sunburst.png",
                    "flag-images/transparency.png"
                };
                for (String path : defaults) {
                    saveResource(path, false);
                }
                log.info("Loaded default starter flag set.");

                if (dynmapEnabled) {
                    patchDynmapTownyInfowindow();
                }
            }
        }
    }

    public void copyFlagsToDynmapWebDir() {
        if (!dynmapEnabled) return;

        File dynmapWebDir = new File("plugins/dynmap/web/flags");
        if (!dynmapWebDir.exists() && !dynmapWebDir.mkdirs()) {
            log.warn("Failed to create Dynmap web flags directory.");
            return;
        }

        File[] images = flagImageFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (images == null) return;

        for (File src : images) {
            File dst = new File(dynmapWebDir, src.getName());
            try {
                java.nio.file.Files.copy(src.toPath(), dst.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                log.warn("Failed to copy flag: " + src.getName());
            }
        }
    }

    private void patchDynmapTownyInfowindow() {
        if (!dynmapEnabled) return;

        File dynmapTownyConfig = new File("plugins/Dynmap-Towny/config.yml");
        if (!dynmapTownyConfig.exists()) return;

        try {
            List<String> lines = java.nio.file.Files.readAllLines(dynmapTownyConfig.toPath());
            boolean modified = false;

            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).trim().startsWith("infowindow:")) {
                    int insertIndex = i + 1;

                    if (lines.stream().noneMatch(line -> line.contains("%town_flag%"))) {
                        lines.add(insertIndex, "  - '%town_flag%'");
                        modified = true;
                    }
                    if (lines.stream().noneMatch(line -> line.contains("%nation_flag%"))) {
                        lines.add(insertIndex + 1, "  - '%nation_flag%'");
                        modified = true;
                    }
                    break;
                }
            }

            if (modified) {
                java.nio.file.Files.write(dynmapTownyConfig.toPath(), lines);
                getLogger().info("Injected %town_flag% and %nation_flag% into Dynmap-Towny config.");
            }
        } catch (IOException e) {
            getLogger().warning("Failed to inject into Dynmap-Towny config: " + e.getMessage());
        }
    }

    public void refreshDynmapMarkers() {
        if (!dynmapEnabled) return;

        Bukkit.getScheduler().runTask(this, () -> {
            for (Town town : TownyAPI.getInstance().getTowns()) {
                BuildTownMarkerDescriptionEvent event = new BuildTownMarkerDescriptionEvent(town);
                Bukkit.getPluginManager().callEvent(event);
            }
            getLogger().info("Triggered Dynmap marker description rebuilds.");
        });
    }

    private void printSickASCIIArt() {
        String gold = ChatColor.GOLD.toString();
        String reset = ChatColor.RESET.toString();
        Bukkit.getConsoleSender().sendMessage(gold + "   **      //\\                                                               **");
        Bukkit.getConsoleSender().sendMessage(gold + "   **      \\\\//                                                               **");
        Bukkit.getConsoleSender().sendMessage(gold + "   **       ||\\   ____________________________________________________\t      **");
        Bukkit.getConsoleSender().sendMessage(gold + "   **       || \\ /~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\      **");
        Bukkit.getConsoleSender().sendMessage(gold + "   **       ||   |~ _____                                         |~~~~\\     **");
        Bukkit.getConsoleSender().sendMessage(gold + "   **       ||   |~|_   _|   ___   __        __  _ __    _   _    |~~~~~\\    **");
        Bukkit.getConsoleSender().sendMessage(gold + "   **       ||   |~  | |    / _ \\  \\ \\ /\\  /  / | '_ \\  | | | |   |~~~~~~||  **");
        Bukkit.getConsoleSender().sendMessage(gold + "   **       ||   |~  | |   | (_) |  \\ V   V  /  | | | | | |_| |   |~~~~~~||  **");
        Bukkit.getConsoleSender().sendMessage(gold + "   **       ||   |~  |_|    \\___/    \\_/ \\_/    |_| |_|  \\__, |   |~~~~~~||  **");
        Bukkit.getConsoleSender().sendMessage(gold + "   **       ||   |~\t    _____   _                     |___/    |~~~~~~||  **");
        Bukkit.getConsoleSender().sendMessage(gold + "   **       ||   |~\t   |  ___| | |   __ _    __ _   ___        |~~~~~~||  **");
        Bukkit.getConsoleSender().sendMessage(gold + "   **       ||   |~\t   | |_    | |  / _` |  / _` | / __|       |~~~~~~||  **");
        Bukkit.getConsoleSender().sendMessage(gold + "   **       ||   |~\t   |  _|   | | | (_| | | (_| |  \\__\\       |~~~~~~||  **");
        Bukkit.getConsoleSender().sendMessage(gold + "   **       ||   |~\t   |_|     |_| \\___,_|  \\__, | |___/       |~~~~~//   **");
        Bukkit.getConsoleSender().sendMessage(gold + "   **       ||   |~\t\t                |___/\t\t   |~~~~//    **");
        Bukkit.getConsoleSender().sendMessage(gold + "   **       || // \\~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//      **");
        Bukkit.getConsoleSender().sendMessage(gold + "   **       ||//                                                             **");
        Bukkit.getConsoleSender().sendMessage(gold + "   **       ||                                                               **");
        Bukkit.getConsoleSender().sendMessage(gold + "   **       ||                                                               **");
        Bukkit.getConsoleSender().sendMessage(gold + "   **       ||                                                               **");
        Bukkit.getConsoleSender().sendMessage(gold + "   **       ||                                                               **");
        Bukkit.getConsoleSender().sendMessage(gold + "   **       ||                                                               **");
        Bukkit.getConsoleSender().sendMessage(gold + "   **       ||                                                               **");
        Bukkit.getConsoleSender().sendMessage(gold + "   **       ||                                                               **");
        Bukkit.getConsoleSender().sendMessage(gold + "   **       ||                                                               **");
        Bukkit.getConsoleSender().sendMessage(gold + "   **       ||                                                               **");
        Bukkit.getConsoleSender().sendMessage(gold + "   **      // \\                                                              **");
        Bukkit.getConsoleSender().sendMessage(gold + "   **     //   \\                                                             **");
    }
}
