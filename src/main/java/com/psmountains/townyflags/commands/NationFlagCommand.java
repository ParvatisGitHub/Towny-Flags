package com.psmountains.townyflags.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.psmountains.townyflags.FlagStorage;
import com.psmountains.townyflags.TownyFlags;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

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
			p.sendMessage("§cUsage: /n flag <set|remove|info>");
			return true;
		}

		switch (args[0].toLowerCase()) {
			case "set" -> {
				if (args.length < 2) {
					p.sendMessage("§cUsage: /n flag set <image>");
					return true;
				}

				if (!isAllowed(p, "flags.nation-allowed-roles", "nation")) {
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
				if (!isAllowed(p, "flags.nation-allowed-roles", "nation")) {
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
			default -> {
				p.sendMessage("§cUnknown flag subcommand. Use /n flag set/remove/info");
				return true;
			}
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 1) return List.of("set", "remove", "info", "list");

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
			// Default to mayor/king if config list is empty or missing
			allowed = type.equalsIgnoreCase("town")
				? List.of("mayor", "assistant")
				: List.of("king", "assistant");
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
