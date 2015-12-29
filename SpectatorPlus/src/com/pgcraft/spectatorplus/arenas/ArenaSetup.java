/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.pgcraft.spectatorplus.arenas;

import com.pgcraft.spectatorplus.SpectatorPlus;
import com.pgcraft.spectatorplus.spectators.Spectator;
import fr.zcraft.zlib.tools.PluginLogger;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;


/**
 * Represents a setup session of an arena.
 */
public class ArenaSetup
{
	private UUID playerID;

	private String arenaName;
	private Integer step = -1;

	private Location corner1 = null;
	private Location corner2 = null;

	public ArenaSetup(Player player, String arenaName)
	{
		this.playerID = player.getUniqueId();
		this.arenaName = arenaName;

		next(null);
	}

	/**
	 * Switch to the next setup step.
	 *
	 * @param location The selected location for this step. {@code null} if it's the first step.
	 */
	public void next(Location location)
	{
		step++;

		Validate.isTrue(step <= 0 || location != null, "The location is required for the second and third steps.");

		Player player = Bukkit.getPlayer(playerID);
		Spectator spectator = SpectatorPlus.get().getPlayerData(playerID);

		if (player == null || !player.isOnline())
		{
			PluginLogger.warning("Arena setup was executed for an offline player (UUID {0})! This is not a normal thing. Aborting setup.", playerID);
			spectator.setArenaSetup(null);

			return;
		}

		switch (step)
		{
			case 0:
				SpectatorPlus.get().sendMessage(player, "You just entered arena setup mode for the arena " + ChatColor.RED + arenaName + ChatColor.GOLD + ".", true);
				player.sendMessage(ChatColor.GRAY + "You'll have to mark the two opposite corners of the arena, witch is a 3D rectangular structure.");
				player.sendMessage(ChatColor.GRAY + "Punch the " + ChatColor.RED + "first corner " + ChatColor.GOLD + " of the arena.");
				player.sendMessage(ChatColor.GRAY + "You can also use the command " + ChatColor.WHITE + "/spec arena corner" + ChatColor.GRAY + " to set the corner at your current location.");
				break;

			case 1:
				corner1 = location;
				SpectatorPlus.get().sendMessage(player, "Now, punch the " + ChatColor.RED + "second corner " + ChatColor.GOLD + " of the arena, in the opposite corner.", true);
				player.sendMessage(ChatColor.GRAY + "You can still use the " + ChatColor.WHITE + "/spec arena corner" + ChatColor.GRAY + " command.");
				break;

			case 2:
				corner2 = location;
				try
				{
					SpectatorPlus.get().getArenasManager().registerArena(new Arena(arenaName, corner1, corner2));
					SpectatorPlus.get().sendMessage(player, "The arena " + ChatColor.RED + arenaName + ChatColor.GOLD + " was successfully registered!", true);
				}
				catch (IllegalArgumentException e)
				{
					SpectatorPlus.get().sendMessage(player, ChatColor.RED + "Cannot register the arena.", true);
					player.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
				}
				spectator.setArenaSetup(null);
				break;
		}
	}
}
