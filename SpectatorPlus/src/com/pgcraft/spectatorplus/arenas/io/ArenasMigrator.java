/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.pgcraft.spectatorplus.arenas.io;

import com.pgcraft.spectatorplus.arenas.Arena;
import fr.zcraft.zlib.tools.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public final class ArenasMigrator
{
	private static final String V1_STORAGE_KEY = "arena";
	private static final String V1_NEXT_ARENA_KEY = "nextarena";

	private static final String V2_STORAGE_KEY = "arenas";
	private static final String V2_ARENA_SERIALIZATION_TOKEN = "==";
	private static final String V2_ARENA_SERIALIZATION_TOKEN_INTERNAL_REPLACEMENT = "sp_migrator_v3";
	private static final String V2_ARENA_SERIALIZATION_TOKEN_PGCRAFT = "==: com.pgcraft";
	private static final String V2_ARENA_SERIALIZATION_TOKEN_VECTOR = "==: Vector";

	private ArenasMigrator() {}


	/**
	 * Prepares the migration of this config file, removing the serialisation tokens to prevent the
	 * YAML loader from loading removed classes.
	 *
	 * @param configurationFile The file to prepare.
	 */
	public static void prepareMigration(File configurationFile)
	{
		if (!configurationFile.exists())
			return;

		StringBuilder fixedConfigurationFile = new StringBuilder();

		try
		{
			Boolean fileUpdated = false;

			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configurationFile)));
			for (String line; (line = reader.readLine()) != null; )
			{
				if (line.contains(V2_ARENA_SERIALIZATION_TOKEN_PGCRAFT))
				{
					fixedConfigurationFile
							.append(line.replace(V2_ARENA_SERIALIZATION_TOKEN, V2_ARENA_SERIALIZATION_TOKEN_INTERNAL_REPLACEMENT))
							.append('\n');

					fileUpdated = true;
				}
				else if (line.contains(V2_ARENA_SERIALIZATION_TOKEN_VECTOR))
				{
					// Line removed: not added in the new file content
					fileUpdated = true;
				}
				else
				{
					fixedConfigurationFile.append(line).append('\n');
				}
			}

			if (fileUpdated)
			{
				OutputStreamWriter writer = null;
				try
				{
					writer = new OutputStreamWriter(new FileOutputStream(configurationFile));
					writer.write(fixedConfigurationFile.toString());
				}
				finally
				{
					if (writer != null) writer.close();
				}
			}
		}
		catch (IOException e)
		{
			PluginLogger.error("Cannot prepare migration of the {0} file", e, configurationFile.getPath());
		}
	}

	/**
	 * Migrates the old arenas to the last storage system.
	 *
	 * @param config The configuration section containing the arenas (whole one, the representation of the {@code setup.yml} file).
	 * @return A set containing the arenas to import from these old configuration files.
	 */
	public static Set<Arena> migrate(ConfigurationSection config)
	{
		// No data, no migration
		if (config.getKeys(false).size() == 0)
			return Collections.emptySet();

		final Set<Arena> arenas = new HashSet<>();

		// Data migration from V1
		if (config.isConfigurationSection(V1_STORAGE_KEY))
			arenas.addAll(migrateFromV1(config));

		// Data migration from V2
		if (config.isConfigurationSection(V2_STORAGE_KEY))
			arenas.addAll(migrateFromV2(config));

		return arenas;
	}

	/**
	 * Migration from V1
	 *
	 * @return Arenas to import
	 */
	private static Set<Arena> migrateFromV1(ConfigurationSection config)
	{
		final Set<Arena> importedArenas = new HashSet<>();

		final int lastNumericID = config.getInt(V1_NEXT_ARENA_KEY);

		for (int i = 1; i < lastNumericID; i++)
		{
			String name = config.getString(V1_STORAGE_KEY + "." + i + ".name");

			World defaultWorld = Bukkit.getWorlds().get(0);
			Location corner1 = new Location(defaultWorld,
					config.getDouble(V1_STORAGE_KEY + "." + i + ".1.x"),
					config.getDouble(V1_STORAGE_KEY + "." + i + ".1.y"),
					config.getDouble(V1_STORAGE_KEY + "." + i + ".1.z"));
			Location corner2 = new Location(defaultWorld,
					config.getDouble(V1_STORAGE_KEY + "." + i + ".2.x"),
					config.getDouble(V1_STORAGE_KEY + "." + i + ".2.y"),
					config.getDouble(V1_STORAGE_KEY + "." + i + ".2.z"));

			Arena importedArena = new Arena(name, corner1, corner2);

			// Is a lobby registered?
			if (config.isConfigurationSection(V1_STORAGE_KEY + "." + i + ".lobby"))
			{
				Location lobby = new Location(Bukkit.getWorld(config.getString(V1_STORAGE_KEY + "." + i + ".lobby.world")),
						config.getDouble(V1_STORAGE_KEY + "." + i + ".lobby.x"),
						config.getDouble(V1_STORAGE_KEY + "." + i + ".lobby.y"),
						config.getDouble(V1_STORAGE_KEY + "." + i + ".lobby.z"));
				importedArena.setLobby(lobby);
			}

			PluginLogger.info("Imported arena {0} from SpectatorPlus 1.", importedArena.getName());
			importedArenas.add(importedArena);
		}

		// The old configuration is removed
		config.set(V1_STORAGE_KEY, null);
		config.set(V1_NEXT_ARENA_KEY, null);

		return importedArenas;
	}

	/**
	 * Migration from V2
	 *
	 * Format:
	 * arenas:
	 *	 7958b31e-d4c9-4ea0-9b99-708ade3b9ac3:
	 *		 ==: com.pgcraft.spectatorplus.arenas.Arena
	 *		 corner1:
	 *			 ==: Vector
	 *			 x: -91.65321905131621
	 *			 y: 102.0
	 *			 z: -198.2704094739675
	 *		 corner2:
	 *			 ==: Vector
	 *			 x: -81.58574333405973
	 *			 y: 114.61435641343124
	 *			 z: -192.74193356620611
	 *		 lobby.world: null
	 *		 world: world_nether
	 *		 name: Toast
	 *		 registered: true
	 *		 id: 7958b31e-d4c9-4ea0-9b99-708ade3b9ac3
	 *		 lobby.location: null
	 *		 enabled: true
	 *
	 * @return Arenas to import
	 */
	private static Set<Arena> migrateFromV2(ConfigurationSection config)
	{
		final ConfigurationSection arenasSection = config.getConfigurationSection(V2_STORAGE_KEY);
		final Set<Arena> importedArenas = new HashSet<>();

		for (String arenaUUID : arenasSection.getKeys(false))
		{
			if (!arenasSection.isConfigurationSection(arenaUUID))
				continue;

			final ConfigurationSection arenaSection = arenasSection.getConfigurationSection(arenaUUID);

			// The V2 format contains a serialization key containing the name of the
			// serialized class
			if (!arenaSection.contains(V2_ARENA_SERIALIZATION_TOKEN_INTERNAL_REPLACEMENT))
				continue;


			try
			{
				String name = arenaSection.getString("name");
				Boolean enabled = arenaSection.getBoolean("enabled");


				World world = Bukkit.getWorld(arenaSection.getString("world"));
				if (world == null)
				{
					PluginLogger.warning("Skipped import of arena {0} from V2 as the world {1} no longer exists.", name, arenaSection.getString("world"));
					continue;
				}

				Location corner1 = new Location(
						world,
						arenaSection.getDouble("corner1.x"),
						arenaSection.getDouble("corner1.y"),
						arenaSection.getDouble("corner1.z")
				);
				Location corner2 = new Location(
						world,
						arenaSection.getDouble("corner2.x"),
						arenaSection.getDouble("corner2.y"),
						arenaSection.getDouble("corner2.z")
				);


				Location lobby = null;

				String lobbyWorldName = arenaSection.getString("lobby.world");
				if (lobbyWorldName != null && !lobbyWorldName.equals("null"))
				{
					World lobbyWorld = Bukkit.getWorld(lobbyWorldName);

					if (lobbyWorld == null)
					{
						PluginLogger.warning("Skipped import of arena {0}'s lobby from V2 as the world {1} no longer exists.", name, lobbyWorldName);
					}
					else
					{
						lobby = new Location(
								lobbyWorld,
								arenaSection.getDouble("lobby.location.x"),
								arenaSection.getDouble("lobby.location.y"),
								arenaSection.getDouble("lobby.location.z")
						);
					}
				}


				Arena importedArena = new Arena(name, corner1, corner2);
				importedArena.setEnabled(enabled);

				if (lobby != null)
					importedArena.setLobby(lobby);

				PluginLogger.info("Imported arena {0} from SpectatorPlus 2.", importedArena.getName());
				importedArenas.add(importedArena);
			}
			finally
			{
				arenasSection.set(arenaUUID, null);  // The old arena is removed.
			}
		}

		return importedArenas;
	}
}
