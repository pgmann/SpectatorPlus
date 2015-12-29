/*
 * Copyright or © or Copr. AmauryCarrade (2015)
 * 
 * http://amaury.carrade.eu
 * 
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 * 
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
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
				SpectatorPlus.get().sendMessage(player, "You'll have to mark the two opposite corners of the arena, witch is a 3D rectangular structure.", true);
				SpectatorPlus.get().sendMessage(player, "Punch the " + ChatColor.RED + "first corner " + ChatColor.GOLD + " of the arena.", true);
				SpectatorPlus.get().sendMessage(player, "You can also use the command " + ChatColor.RED + "/spec arena corner" + ChatColor.GOLD + " to set the corner at your current location.", true);
				break;

			case 1:
				corner1 = location;
				SpectatorPlus.get().sendMessage(player, "Now, punch the " + ChatColor.RED + "second corner " + ChatColor.GOLD + " of the arena, in the opposite corner.", true);
				SpectatorPlus.get().sendMessage(player, "You can still use the " + ChatColor.RED + "/spec arena corner" + ChatColor.GOLD + " command.", true);
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
					SpectatorPlus.get().sendMessage(player, ChatColor.RED + "Error: " + e.getMessage(), true);
				}

				spectator.setArenaSetup(null);
				break;
		}
	}
}
