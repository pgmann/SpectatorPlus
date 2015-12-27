package com.pgcraft.spectatorplus.arenas;

import com.pgcraft.spectatorplus.SpectatorPlus;
import com.pgcraft.spectatorplus.arenas.io.ArenasIO;
import com.pgcraft.spectatorplus.arenas.io.ArenasMigrator;
import com.pgcraft.spectatorplus.spectators.Spectator;
import com.pgcraft.spectatorplus.utils.ConfigAccessor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * This class manages the arenas of SpectatorPlusOld. <p> For an {@link Arena} to be recognized by
 * SpectatorPlus, the object needs to be registered using {@link #registerArena(Arena)}.
 *
 * The Arena class is registered as a serializable one in the onLoad method of the main class.
 *
 * @since 2.0
 */
public class ArenasManager
{

	private Map<UUID, Arena> arenas = new HashMap<>();
	private ConfigAccessor storageConfig;


	public ArenasManager(SpectatorPlus plugin)
	{
		ArenasMigrator.prepareMigration(new File(SpectatorPlus.get().getDataFolder().getPath() + File.separator + "setup.yml"));
		storageConfig = plugin.getSpectatorsManager().getSpectatorsSetup();

		// Migrates the old arenas to the new storage
		for (Arena arena : ArenasMigrator.migrate(storageConfig.getConfig()))
			registerArena(arena);

		// Loads the arenas from the config
		reload();

		// Save, to store in file the migrated arenas
		save();
	}


	/**
	 * (Re)loads the arenas from the configuration file.
	 *
	 * @since 2.0
	 */
	public void reload()
	{
		for (Arena arena : ArenasIO.loadArenas(storageConfig.getConfig()))
		{
			arenas.put(arena.getUUID(), arena);
		}
	}

	/**
	 * Saves all registered arenas in the configuration file.
	 *
	 * @since 2.0
	 */
	public void save()
	{
		ArenasIO.saveArenas(storageConfig.getConfig(), arenas.values());
		storageConfig.saveConfig();
	}

	/**
	 * Removes all the registered arenas.
	 *
	 * WARNING - CANNOT BE CANCELLED.
	 *
	 * @since 2.0
	 */
	public void reset()
	{
		for (Arena arena : getArenas())
		{
			arena.setRegistered(false);
		}

		arenas = new HashMap<>();

		save();
	}

	/**
	 * Returns an Arena from his UUID.
	 *
	 * @param id The UUID of the Arena.
	 *
	 * @return The Arena, or null if there isn't any Arena with this UUID.
	 * @since 2.0
	 */
	public Arena getArena(UUID id)
	{
		return arenas.get(id);
	}

	/**
	 * Returns an Arena from his name.
	 *
	 * Case sensitive.
	 *
	 * @param name The name of the Arena.
	 *
	 * @return The Arena, or null if there isn't any Arena with this name.
	 * @since 2.0
	 */
	public Arena getArena(String name)
	{
		for (Arena arena : arenas.values())
		{
			if (arena.getName().equals(name))
			{
				return arena;
			}
		}

		return null;
	}

	/**
	 * Returns a collection of the registered arenas.
	 *
	 * @return the collection.
	 * @since 2.0
	 */
	public Collection<Arena> getArenas()
	{
		return arenas.values();
	}

	/**
	 * Registers an arena.
	 *
	 * @param arena The arena to register.
	 *
	 * @throws IllegalArgumentException if an arena with the same UUID is already registered.
	 * @throws IllegalArgumentException if an arena with the same name is already registered.
	 * @since 2.0
	 */
	public void registerArena(Arena arena)
	{
		if (arenas.containsKey(arena.getUUID()))
		{
			throw new IllegalArgumentException("An arena with the UUID " + arena.getUUID().toString() + " already exists!");
		}

		if (this.getArena(arena.getName()) != null)
		{
			throw new IllegalArgumentException("An arena with the name " + arena.getName() + " already exists!");
		}

		arenas.put(arena.getUUID(), arena);
		arena.setRegistered(true);

		save();
	}

	/**
	 * Unregisters an arena.
	 *
	 * @param arena The arena to unregister.
	 *
	 * @since 2.0
	 */
	public void unregisterArena(Arena arena)
	{
		arenas.remove(arena.getUUID());
		arena.setRegistered(false);

		for (Player player : Bukkit.getOnlinePlayers())
		{
			Spectator spectator = SpectatorPlus.get().getPlayerData(player);

			if (spectator.isSpectating() && spectator.getArena() != null && spectator.getArena().equals(arena))
			{
				spectator.setArena(null);
			}
		}

		save();
	}
}
