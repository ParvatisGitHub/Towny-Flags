package com.psmountains.townyflags;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import java.io.File;
import com.psmountains.townyflags.commands.*;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI.CommandType;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.TownyAPI;
import org.dynmap.towny.events.BuildTownMarkerDescriptionEvent;

@Slf4j
@Getter
public final class TownyFlags extends JavaPlugin {

    private File flagImageFolder;

    @Override
    public void onEnable() {
        createFlagImageDirectory();
		copyFlagsToDynmapWebDir();
		TownyCommandAddonAPI.addSubCommand(CommandType.TOWN, "flag", new TownFlagCommand(this));
		TownyCommandAddonAPI.addSubCommand(CommandType.NATION, "flag", new NationFlagCommand(this));
		TownyCommandAddonAPI.addSubCommand(CommandType.TOWNYADMIN, "flagreload", new FlagReloadCommand(this));
        TownyCommandAddonAPI.addSubCommand(CommandType.TOWNYADMIN, "flag", new FlagAdminCommand(this));
		log.info("TownyFlagAddon enabled.");
		getServer().getPluginManager().registerEvents(new DynmapFlagPlaceholderListener(this), this);
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
			}
		}
	}

	public void copyFlagsToDynmapWebDir() {
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

	public void refreshDynmapMarkers() {
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
		Bukkit.getConsoleSender().sendMessage(gold + "   **      \\//                                                               **");
		Bukkit.getConsoleSender().sendMessage(gold + "   **       ||\\   ____________________________________________________	      **");
		Bukkit.getConsoleSender().sendMessage(gold + "   **       || \\ /~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\      **");
		Bukkit.getConsoleSender().sendMessage(gold + "   **       ||   |~ _____                                         |~~~~\\     **");
		Bukkit.getConsoleSender().sendMessage(gold + "   **       ||   |~|_   _|   ___   __        __  _ __    _   _    |~~~~~\\    **");
		Bukkit.getConsoleSender().sendMessage(gold + "   **       ||   |~  | |    / _ \\  \\ \\ /\\  /  / | '_ \\  | | | |   |~~~~~~||  **");
		Bukkit.getConsoleSender().sendMessage(gold + "   **       ||   |~  | |   | (_) |  \\ V   V  /  | | | | | |_| |   |~~~~~~||  **");
		Bukkit.getConsoleSender().sendMessage(gold + "   **       ||   |~  |_|    \\___/    \\_/ \\_/    |_| |_|  \\__, |   |~~~~~~||  **");
		Bukkit.getConsoleSender().sendMessage(gold + "   **       ||   |~	    _____   _                     |___/    |~~~~~~||  **");
		Bukkit.getConsoleSender().sendMessage(gold + "   **       ||   |~	   |  ___| | |   __ _    __ _   ___        |~~~~~~||  **");
		Bukkit.getConsoleSender().sendMessage(gold + "   **       ||   |~	   | |_    | |  / _` |  / _` | / __|       |~~~~~~||  **");
		Bukkit.getConsoleSender().sendMessage(gold + "   **       ||   |~	   |  _|   | | | (_| | | (_| |  \\__\\       |~~~~~~||  **");
		Bukkit.getConsoleSender().sendMessage(gold + "   **       ||   |~	   |_|     |_| \\___,_|  \\__, | |___/       |~~~~~//   **");
		Bukkit.getConsoleSender().sendMessage(gold + "   **       ||   |~		                |___/		   |~~~~//    **");
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
		Bukkit.getConsoleSender().sendMessage(gold + "   **       ||                                                               **");
		Bukkit.getConsoleSender().sendMessage(gold + "   **       ||                                                               **");
		Bukkit.getConsoleSender().sendMessage(gold + "   **       ||                                                               **");
		Bukkit.getConsoleSender().sendMessage(gold + "   **      // \\                                                              **");
		Bukkit.getConsoleSender().sendMessage(gold + "   **     //   \\                                                             **");                
	}
}










