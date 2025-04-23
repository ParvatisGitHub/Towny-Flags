package com.psmountains.townyflags;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import lombok.experimental.UtilityClass;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@UtilityClass
public class FlagStorage {

	private final File pluginFolder = new File("plugins/TownyFlags");

	private final File townFile = new File(pluginFolder, "town-flags.yml");
	private final File nationFile = new File(pluginFolder, "nation-flags.yml");

	private final YamlConfiguration townConfig = YamlConfiguration.loadConfiguration(townFile);
	private final YamlConfiguration nationConfig = YamlConfiguration.loadConfiguration(nationFile);

	public void setTownFlag(Town town, String imageName) {
		townConfig.set(town.getUUID().toString(), imageName);
		save(townConfig, townFile);
	}

	public void setNationFlag(Nation nation, String imageName) {
		nationConfig.set(nation.getUUID().toString(), imageName);
		save(nationConfig, nationFile);
	}

	public String getTownFlag(UUID uuid) {
		return townConfig.getString(uuid.toString());
	}

	public String getNationFlag(UUID uuid) {
		return nationConfig.getString(uuid.toString());
	}

	public void removeTownFlag(Town town) {
		townConfig.set(town.getUUID().toString(), null);
		save(townConfig, townFile);
	}

	public void removeNationFlag(Nation nation) {
		nationConfig.set(nation.getUUID().toString(), null);
		save(nationConfig, nationFile);
	}

	private void save(YamlConfiguration config, File file) {
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
