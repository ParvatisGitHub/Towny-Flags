package com.psmountains.townyflags.listeners;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.psmountains.townyflags.FlagStorage;
import com.psmountains.townyflags.TownyFlags;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.dynmap.towny.events.BuildTownMarkerDescriptionEvent;

public class DynmapFlagPlaceholderListener implements Listener {
	private final TownyFlags plugin;
	
	public DynmapFlagPlaceholderListener(TownyFlags plugin) {
        this.plugin = plugin;
    }
	
    @EventHandler
    public void on(BuildTownMarkerDescriptionEvent event) {
        String desc = event.getDescription();
        Town town = event.getTown();
        //town placeholder
		if (desc.contains("%town_flag%")) {
            String flag = FlagStorage.getTownFlag(town.getUUID());
				if (flag == null || flag.isEmpty()) {
					flag = plugin.getConfig().getString("flags.default-town-flag", "");
				}
            String html = flag != null ? getImageHtml(flag, plugin.getTownFlagHeight()) : "";
            event.setDescription(desc.replace("%town_flag%", html));
        }
		//nation placeholder
		if (desc.contains("%nation_flag%")) {
			Nation nation = town.hasNation() ? town.getNationOrNull() : null;
			String flag = (nation != null) ? FlagStorage.getNationFlag(nation.getUUID()) : null;
			if ((flag == null || flag.isEmpty()) && nation != null) {
				flag = plugin.getConfig().getString("flags.default-nation-flag", "");
			}
			String html = flag != null ? getImageHtml(flag, plugin.getNationFlagHeight()) : "";
			event.setDescription(desc.replace("%nation_flag%", html));
		}
    }

    private String getImageHtml(String imageName, int height) {
		return "<img src='flags/" + imageName + ".png' alt='" + imageName + "' style='height:" + height + "px;'/>";
	}
}
