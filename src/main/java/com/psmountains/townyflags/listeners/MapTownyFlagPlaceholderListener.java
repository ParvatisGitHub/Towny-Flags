package com.psmountains.townyflags.listeners;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.psmountains.townyflags.FlagStorage;
import com.psmountains.townyflags.TownyFlags;
import me.silverwolfg11.maptowny.MapTownyPlugin;
import me.silverwolfg11.maptowny.events.MapReloadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MapTownyFlagPlaceholderListener implements Listener {
	private final TownyFlags plugin;
	
	public MapTownyFlagPlaceholderListener(TownyFlags plugin) {
        this.plugin = plugin;
    }
	
    @EventHandler
    public void on(MapReloadEvent event) {
        // Register replacements when MapTowny reloads
        registerReplacements();
    }
    
    public void registerReplacements() {
        Object mapTownyPlugin = plugin.getServer().getPluginManager().getPlugin("MapTowny");
        
        if (!(mapTownyPlugin instanceof MapTownyPlugin)) {
            return;
        }
        
        MapTownyPlugin maptowny = (MapTownyPlugin) mapTownyPlugin;
        
        // Register town flag replacement
        maptowny.getLayerManager().registerReplacement("%town_flag%", town -> {
            String flag = FlagStorage.getTownFlag(town.getUUID());
            if (flag == null || flag.isEmpty()) {
                flag = plugin.getConfig().getString("flags.default-town-flag", "");
            }
            return flag != null ? getImageHtml(flag, plugin.getTownFlagHeight()) : "";
        });

        // Compatibility alias used in older tooltip templates.
        maptowny.getLayerManager().registerReplacement("%towny_flags%", town -> {
            String flag = FlagStorage.getTownFlag(town.getUUID());
            if (flag == null || flag.isEmpty()) {
                flag = plugin.getConfig().getString("flags.default-town-flag", "");
            }
            return flag != null ? getImageHtml(flag, plugin.getTownFlagHeight()) : "";
        });
        
        // Register nation flag replacement
        maptowny.getLayerManager().registerReplacement("%nation_flag%", town -> {
            Nation nation = town.hasNation() ? town.getNationOrNull() : null;
            String flag = (nation != null) ? FlagStorage.getNationFlag(nation.getUUID()) : null;
            if ((flag == null || flag.isEmpty()) && nation != null) {
                flag = plugin.getConfig().getString("flags.default-nation-flag", "");
            }
            return flag != null ? getImageHtml(flag, plugin.getNationFlagHeight()) : "";
        });
    }

    private String getImageHtml(String imageName, int height) {
		return "<img src='flags/" + imageName + ".png' alt='" + imageName + "' style='height:" + height + "px;'/>";
	}
}
