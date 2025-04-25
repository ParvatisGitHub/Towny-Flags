package com.psmountains.townyflags.listeners;

import com.palmergames.adventure.text.Component;
import com.palmergames.bukkit.towny.event.statusscreen.TownStatusScreenEvent;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.utils.TownyComponents;
import com.palmergames.adventure.text.event.HoverEvent;
import com.palmergames.adventure.text.event.ClickEvent;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.psmountains.townyflags.TownyFlags;
import com.psmountains.townyflags.AsciiFlagView;
import com.psmountains.townyflags.FlagStorage;

import java.io.File;
import java.util.List;

@RequiredArgsConstructor
public class TownFlagStatusListener implements Listener {

	private final TownyFlags plugin;
	private final MiniMessage mini = MiniMessage.miniMessage();

	@EventHandler
	public void onTownStatusScreen(TownStatusScreenEvent event) {
		Town town = event.getTown();

		// ---------- TOWN FLAG ----------
		String townFlag = FlagStorage.getTownFlag(town.getUUID());
		if (townFlag != null && !townFlag.isBlank()) {
			File flagFile = new File(plugin.getFlagImageFolder(), townFlag + ".png");
			if (flagFile.exists()) {
				addFlagComponentToScreen(event, "TownFlag", "Town Flag", flagFile, "&7[&aTown Flag&7]", "/t flag view");
			}
		}

		// ---------- NATION FLAG ----------
		if (town.hasNation()) {
			Nation nation = town.getNationOrNull();
			if (nation != null) {
				String nationFlag = FlagStorage.getNationFlag(nation.getUUID());
				if (nationFlag != null && !nationFlag.isBlank()) {
					File nationFlagFile = new File(plugin.getFlagImageFolder(), nationFlag + ".png");
					if (nationFlagFile.exists()) {
						addFlagComponentToScreen(event, "NationFlag", "Nation Flag", nationFlagFile, "&7[&aNation Flag&7]", "/n flag view");
					}
				}
			}
		}
	}

	private void addFlagComponentToScreen(TownStatusScreenEvent event, String key, String label, File flagFile, String clickableText, String command) {
		StringBuilder hoverText = new StringBuilder();

		String titleBar = centerText(" " + label + " ", 29, '─');
		hoverText.append(titleBar).append("<newline>");

		List<net.kyori.adventure.text.Component> previewLines = AsciiFlagView.generatePreviewRGB(flagFile, 24, 8);
		for (net.kyori.adventure.text.Component line : previewLines) {
			String lineString = mini.serialize(line);
			hoverText.append(lineString).append("<newline>");
		}

		String bottomBar = centerText("─", 24, '─');
		hoverText.append(bottomBar);

		// Unique key ensures both flags appear
		event.getStatusScreen().addComponentOf(
			key,
			clickableText,
			HoverEvent.showText(TownyComponents.miniMessage(hoverText.toString())),
			ClickEvent.runCommand(command)
		);

	}

	private String centerText(String content, int width, char fillChar) {
		String plain = content.replaceAll("<[^>]+>", "");
		int contentLength = plain.length();
		int totalPadding = Math.max(0, width - contentLength);
		int left = totalPadding / 2;
		int right = totalPadding - left;
		String leftFill = String.valueOf(fillChar).repeat(left);
		String rightFill = String.valueOf(fillChar).repeat(right);
		return "<gold>" + leftFill + content + rightFill;
	}
}
