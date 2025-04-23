package com.psmountains.townyflags.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.psmountains.townyflags.FlagStorage;
import com.psmountains.townyflags.TownyFlags;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class FlagAdminCommand implements CommandExecutor, TabCompleter {

	private final TownyFlags plugin;

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length < 3) {
			sender.sendMessage("§cUsage:");
			sender.sendMessage("§c/ta flag set <town|nation> <name> <image>");
			sender.sendMessage("§c/ta flag remove <town|nation> <name>");
			return true;
		}

		String action = args[0].toLowerCase();
		String type = args[1].toLowerCase();
		String name = args[2];

		switch (action) {
			case "set" -> {
				if (args.length != 4) {
					sender.sendMessage("§cUsage: /ta flag set <town|nation> <name> <image>");
					return true;
				}
				String image = args[3];
				File imageFile = new File(plugin.getFlagImageFolder(), image + ".png");
				if (!imageFile.exists()) {
					sender.sendMessage("§cImage not found: " + image + ".png");
					return true;
				}

				if (type.equals("town")) {
					Town town = TownyAPI.getInstance().getTown(name);
					if (town == null) {
						sender.sendMessage("§cTown not found: " + name);
						return true;
					}
					FlagStorage.setTownFlag(town, image);
					sender.sendMessage("§aFlag for town §e" + name + "§a set to §e" + image + ".png");
				} else if (type.equals("nation")) {
					Nation nation = TownyAPI.getInstance().getNation(name);
					if (nation == null) {
						sender.sendMessage("§cNation not found: " + name);
						return true;
					}
					FlagStorage.setNationFlag(nation, image);
					sender.sendMessage("§aFlag for nation §e" + name + "§a set to §e" + image + ".png");
				} else {
					sender.sendMessage("§cType must be 'town' or 'nation'");
				}
			}
			case "remove" -> {
				if (type.equals("town")) {
					Town town = TownyAPI.getInstance().getTown(name);
					if (town == null) {
						sender.sendMessage("§cTown not found: " + name);
						return true;
					}
					FlagStorage.removeTownFlag(town);
					sender.sendMessage("§aFlag removed for town §e" + name);
				} else if (type.equals("nation")) {
					Nation nation = TownyAPI.getInstance().getNation(name);
					if (nation == null) {
						sender.sendMessage("§cNation not found: " + name);
						return true;
					}
					FlagStorage.removeNationFlag(nation);
					sender.sendMessage("§aFlag removed for nation §e" + name);
				} else {
					sender.sendMessage("§cType must be 'town' or 'nation'");
				}
			}
			default -> sender.sendMessage("§cUnknown subcommand. Use 'set' or 'remove'.");
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 1) return List.of("set", "remove");

		if (args.length == 2) return List.of("town", "nation");

		if (args.length == 3) {
			if (args[1].equalsIgnoreCase("town")) {
				return TownyAPI.getInstance().getTowns().stream()
						.map(Town::getName)
						.collect(Collectors.toList());
			} else if (args[1].equalsIgnoreCase("nation")) {
				return TownyAPI.getInstance().getNations().stream()
						.map(Nation::getName)
						.collect(Collectors.toList());
			}
		}

		if (args.length == 4 && args[0].equalsIgnoreCase("set")) {
			File[] files = plugin.getFlagImageFolder().listFiles((dir, name) -> name.endsWith(".png"));
			if (files == null) return List.of();
			return Arrays.stream(files)
					.map(f -> f.getName().replace(".png", ""))
					.collect(Collectors.toList());
		}

		return List.of();
	}
}
