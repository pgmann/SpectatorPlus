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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


/**
 * Loads & saves arenas.
 */
public final class ArenasIO
{
	private static final String STORAGE_KEY = "arenas";

	private ArenasIO() {}


	public static Set<Arena> loadArenas(ConfigurationSection configuration)
	{
		if (!configuration.isConfigurationSection(STORAGE_KEY))
			return Collections.emptySet();

		final ConfigurationSection arenasSection = configuration.getConfigurationSection(STORAGE_KEY);
		final Set<Arena> arenas = new HashSet<>();

		for (String arenaUUID : arenasSection.getKeys(false))
		{
			PluginLogger.info("Loading arena {0}...", arenaUUID);

			if (!arenasSection.isConfigurationSection(arenaUUID))
			{
				PluginLogger.error(" → Not a configuration section. Skipped.");
				continue;
			}

			try
			{
				ConfigurationSection arenaSection = arenasSection.getConfigurationSection(arenaUUID);
				String rawUUID = arenaSection.getString("uuid");

				if (rawUUID == null)
					throw new IllegalArgumentException("Arena UUID can't be null");

				Arena arena = new Arena(
						UUID.fromString(rawUUID),
						arenaSection.getString("name", null),
						string2Location(arenaSection.getString("corner1", null)),
						string2Location(arenaSection.getString("corner2", null)),
						string2Location(arenaSection.getString("lobby", null)),
						arenaSection.getBoolean("registered", true),
						arenaSection.getBoolean("enabled", true)
				);

				arenas.add(arena);
			}
			catch (IllegalArgumentException e)
			{
				PluginLogger.error("Cannot load arena {0}", e, arenaUUID);
			}
		}

		return arenas;
	}

	public static void saveArenas(ConfigurationSection configuration, Collection<Arena> arenas)
	{
		// The configuration is rewrote every time, to take deletions into account.
		if (configuration.isConfigurationSection(STORAGE_KEY))
		{
			configuration.set(STORAGE_KEY, null);
		}

		ConfigurationSection configArenas = configuration.createSection(STORAGE_KEY);

		for (Arena arena : arenas)
		{
			PluginLogger.info("Storing arena {0} (UUID {1})...", arena.getName(), arena.getUUID());
			ConfigurationSection storedArena = configArenas.createSection(arena.getUUID().toString());

			storedArena.set("name", arena.getName());
			storedArena.set("uuid", arena.getUUID().toString());
			storedArena.set("corner1", location2String(arena.getLowestCorner()));
			storedArena.set("corner2", location2String(arena.getHighestCorner()));
			storedArena.set("lobby", location2String(arena.getLobby()));
			storedArena.set("registered", arena.isRegistered());
			storedArena.set("enabled", arena.isEnabled());
		}
	}


	private static String location2String(Location location)
	{
		if (location == null)
			return null;

		return location.getWorld().getName()
				+ ';' + location.getX()
				+ ';' + location.getY()
				+ ';' + location.getZ()
				+ ';' + location.getPitch()
				+ ';' + location.getYaw();
	}

	private static Location string2Location(String rawLocation)
	{
		if (rawLocation == null)
			return null;

		String[] locationsPart = rawLocation.split(";");
		if (locationsPart.length < 4)
			return null;

		World world = Bukkit.getWorld(locationsPart[0]);
		if (world == null)
			return null;

		Location location;
		try
		{
			location = new Location(
					world,
					Double.valueOf(locationsPart[1]),
					Double.valueOf(locationsPart[2]),
					Double.valueOf(locationsPart[3])
			);

			if (locationsPart.length >= 5)
			{
				location.setPitch(Float.valueOf(locationsPart[4]));

				if (locationsPart.length >= 6)
				{
					location.setYaw(Float.valueOf(locationsPart[5]));
				}
			}
		}
		catch (NumberFormatException e)
		{
			return null;
		}

		return location;
	}
}
