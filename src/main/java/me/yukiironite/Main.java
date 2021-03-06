package me.yukiironite;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.generator.ChunkGenerator;

public class Main extends JavaPlugin {
	private static Main instance;

	public Main() {
			instance = this;
	}

	public static Main getInstance() {
			return instance;
	}

	@Override
	public void onEnable() {
		this.saveDefaultConfig();
	}

	@Override
	public void onDisable() {
		CraterPopulator.saveAll();
	}
		
	@Override
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
		return new MoonChunkGenerator();
	}
}
