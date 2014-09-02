package com.pgcraft.spectatorplus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

public class ArenasManager {
	
	private SpectatorPlus p = null;
	private Map<UUID,Arena> arenas = new HashMap<UUID,Arena>();
	
	private static final String STORAGE_KEY = "arenas";
	
	public ArenasManager(SpectatorPlus plugin) {
		this.p = plugin;
		
		// Registers the Arena class as a serializable one.
		ConfigurationSerialization.registerClass(Arena.class);
		
		// Load the arenas from the config
		reload();
	}
	
	/**
	 * (Re)loads the arenas from the configuration file.
	 */
	public void reload() {
		if(p.setup.getConfig().isConfigurationSection(STORAGE_KEY)) {
			ConfigurationSection configArenas = p.setup.getConfig().getConfigurationSection(STORAGE_KEY);
			
			for(String key : configArenas.getKeys(false)) {
				arenas.put(UUID.fromString(key), (Arena) configArenas.get(key));
			}
		}
	}
	
	/**
	 * Saves all registered arenas in the configuration file.
	 */
	public void save() {
		// The configuration is rewrite every time, to take deletions into account.
		if(p.setup.getConfig().isConfigurationSection(STORAGE_KEY)) {
			p.setup.getConfig().set(STORAGE_KEY, null);
		}
		
		ConfigurationSection configArenas = p.setup.getConfig().createSection(STORAGE_KEY);
		
		for(UUID id : arenas.keySet()) {
			configArenas.set(id.toString(), arenas.get(id));
		}
		
		p.setup.saveConfig();
	}
	
	/**
	 * Returns an Arena from his UUID.
	 * 
	 * @param id The UUID of the Arena.
	 * @return The Arena, or null if there isn't any Arena with this UUID.
	 */
	public Arena getArena(UUID id) {
		return arenas.get(id);
	}
	
	/**
	 * Returns an Arena from his name.
	 * 
	 * Case sensitive.
	 * 
	 * @param name The name of the Arena.
	 * @return The Arena, or null if there isn't any Arena with this name.
	 */
	public Arena getArena(String name) {
		for(Arena arena : arenas.values()) {
			if(arena.getName().equals(name)) {
				return arena;
			}
		}
		
		return null;
	}
	
	/**
	 * Registers an arena.
	 * 
	 * This arena will be saved into the configuration file when {@link #save()} will be called.
	 * 
	 * @param arena The arena to register.
	 * @throws IllegalArgumentException if an arena with the same UUID is already registered.
	 * @throws IllegalArgumentException if an arena with the same name is already registered.
	 */
	public void registerArena(Arena arena) {
		if(arenas.containsKey(arena.getUUID())) {
			throw new IllegalArgumentException("An arena with the UUID " + arena.getUUID().toString() + " already exists!");
		}
		
		if(this.getArena(arena.getName()) != null) {
			throw new IllegalArgumentException("An arena with the name " + arena.getName() + " already exists!");
		}
		
		arenas.put(arena.getUUID(), arena);
		
		arena.setRegistered(true);
	}
	
	/**
	 * Unregisters an arena.
	 * 
	 * This arena will be deleted from the configuration file when {@link #save()} will be called.
	 * 
	 * @param arena The arena to unregister.
	 */
	public void unregisterArena(Arena arena) {
		arenas.remove(arena.getUUID());
		arena.setRegistered(false);
	}
}
