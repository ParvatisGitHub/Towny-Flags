package com.psmountains.townyflags.listeners;

import com.palmergames.adventure.text.Component;
import com.palmergames.bukkit.towny.event.statusscreen.NationStatusScreenEvent;
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
public class NationFlagStatusListener implements Listener {

	private final TownyFlags plugin;
	private final MiniMessage mini = MiniMessage.miniMessage();

	@EventHandler
	public void onNationStatusScreen(NationStatusScreenEvent event) {
		Nation nation = event.getNation();
		String imageName = FlagStorage.getNationFlag(nation.getUUID());
		if (imageName == null || imageName.isBlank())
			return;

		File flagFile = new File(plugin.getFlagImageFolder(), imageName + ".png");
		if (!flagFile.exists())
			return;

		// Build hover with gold bars
		StringBuilder hoverText = new StringBuilder();

		String titleBar = centerText(" Nation Flag ", 30, '─');
		hoverText.append(titleBar).append("<newline>");

		List<net.kyori.adventure.text.Component> previewLines = AsciiFlagView.generatePreviewRGB(flagFile, 24, 8);
		for (net.kyori.adventure.text.Component line : previewLines) {
			String lineString = mini.serialize(line);
			hoverText.append(lineString).append("<newline>");
		}

		String bottomBar = centerText("─", 24, '─');
		hoverText.append(bottomBar);

		// Add to /n info screen
		event.getStatusScreen().addComponentOf(
			"TownyFlags",
			"&7[&aNation Flag&7]",
			HoverEvent.showText(TownyComponents.miniMessage(hoverText.toString())),
			ClickEvent.runCommand("/n flag view")
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
