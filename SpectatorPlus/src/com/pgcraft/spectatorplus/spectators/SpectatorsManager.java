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
package com.pgcraft.spectatorplus.spectators;

import com.pgcraft.spectatorplus.SpectatorPlus;
import com.pgcraft.spectatorplus.Toggles;
import com.pgcraft.spectatorplus.arenas.Arena;
import com.pgcraft.spectatorplus.guis.ArenasSelectorGUI;
import com.pgcraft.spectatorplus.guis.inventories.SpectatorsInventoryManager;
import com.pgcraft.spectatorplus.utils.ConfigAccessor;
import fr.zcraft.zlib.components.gui.Gui;
import fr.zcraft.zlib.tools.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class SpectatorsManager
{
	private SpectatorPlus p;

	private SpectatorsInventoryManager inventoryManager;

	private ConfigAccessor savedSpectatingPlayers;
	private ConfigAccessor spectatorsSetup;

	private SpectatorMode spectatorsMode;
	private Location spectatorsLobby = null;

	private Scoreboard spectatorsScoreboard;
	private Team spectatorsTeam;

	private static final String SPECTATORS_TEAM_NAME = "spectators";
	private static final String SPECTATORS_HEALTH_OBJECTIVE_NAME = "health";
	private static final String SPECTATORS_TEAM_PREFIX = ChatColor.GRAY + "Spectator " + ChatColor.DARK_GRAY + " ▏ ";


	public SpectatorsManager(SpectatorPlus plugin)
	{
		p = plugin;

		savedSpectatingPlayers = new ConfigAccessor(p, "specs");
		spectatorsSetup = new ConfigAccessor(p, "setup");

		inventoryManager = new SpectatorsInventoryManager();

		loadSpectatorsSetup();
		rebuildScoreboard();
	}

	public ConfigAccessor getSavedSpectatingPlayers()
	{
		return savedSpectatingPlayers;
	}

	public SpectatorsInventoryManager getInventoryManager()
	{
		return inventoryManager;
	}


	/* **  Spectators lobby  ** */

	private void loadSpectatorsSetup()
	{
		spectatorsSetup.saveDefaultConfig();

		boolean updated = false;


		// Spectating mode
		try
		{
			spectatorsMode = SpectatorMode.fromString(spectatorsSetup.getConfig().getString("mode", "ANY"));
		}
		catch(RuntimeException e)
		{
			spectatorsMode = SpectatorMode.ANY;
			updated = true;
		}


		// Spectators lobby
		spectatorsLobby = null;

		if (spectatorsSetup.getConfig().getBoolean("active", false))
		{
			final String worldName = spectatorsSetup.getConfig().getString("world", "null");
			final World lobbyWorld = p.getServer().getWorld(worldName);

			if (lobbyWorld != null)
			{
				try
				{
					Double lobbyX = Double.valueOf(spectatorsSetup.getConfig().getString("xPos"));
					Double lobbyY = Double.valueOf(spectatorsSetup.getConfig().getString("yPos"));
					Double lobbyZ = Double.valueOf(spectatorsSetup.getConfig().getString("zPos"));

					Float lobbyPitch = Float.valueOf(spectatorsSetup.getConfig().getString("pitch"));
					Float lobbyYaw   = Float.valueOf(spectatorsSetup.getConfig().getString("yaw"));

					// Values check
					if (lobbyY > lobbyWorld.getMaxHeight()) lobbyY = (double) lobbyWorld.getMaxHeight();
					else if (lobbyY < 0) lobbyY = 0d;

					lobbyPitch %= 360;
					lobbyYaw %= 360;

					if (lobbyPitch < 0) lobbyPitch += 360;
					if (lobbyYaw < 0)   lobbyYaw   += 360;

					spectatorsLobby = new Location(lobbyWorld, lobbyX, lobbyY, lobbyZ, lobbyPitch, lobbyYaw);
				}
				catch (NumberFormatException e)
				{
					PluginLogger.warning("Invalid spectator lobby stored in setup.yml (invalid coordinates), removing the lobby.");
				}
			}
			else
			{
				// Error message only displayed if the world is not the null name
				if (!Objects.equals(worldName, "null"))
					PluginLogger.warning("Invalid spectator lobby stored in setup.yml (unknown world), removing the lobby.");
			}

			if (spectatorsLobby == null) // If the lobby is still null, the location is invalid and not kept.
				updated = true;
		}


		if (updated)
			saveSpectatorsSetup();
	}

	private void saveSpectatorsSetup()
	{
		spectatorsSetup.getConfig().set("active", spectatorsLobby != null);

		spectatorsSetup.getConfig().set("xPos",  spectatorsLobby != null ? spectatorsLobby.getX()     : 0d);
		spectatorsSetup.getConfig().set("yPos",  spectatorsLobby != null ? spectatorsLobby.getY()     : 0d);
		spectatorsSetup.getConfig().set("zPos",  spectatorsLobby != null ? spectatorsLobby.getZ()     : 0d);
		spectatorsSetup.getConfig().set("pitch", spectatorsLobby != null ? spectatorsLobby.getPitch() : 0f);
		spectatorsSetup.getConfig().set("yaw",   spectatorsLobby != null ? spectatorsLobby.getYaw()   : 0f);

		spectatorsSetup.getConfig().set("world", spectatorsLobby != null ? spectatorsLobby.getWorld().getName() : "null");

		spectatorsSetup.getConfig().set("mode", spectatorsMode.toString());

		spectatorsSetup.saveConfig();
	}


	public boolean teleportToLobby(Spectator spectator)
	{
		Player player = spectator.getPlayer();
		if (player == null) return false;

		if (spectatorsLobby != null)
		{
			// We need a safe spot
			Location aboveLobby = spectatorsLobby.clone().add(0, 1, 0);
			Location belowLobby = spectatorsLobby.clone().add(0, -1, 0);

			while (spectatorsLobby.getBlock().getType() != Material.AIR || aboveLobby.getBlock().getType() != Material.AIR || belowLobby.getBlock().getType() == Material.AIR || belowLobby.getBlock().getType() == Material.LAVA || belowLobby.getBlock().getType() == Material.STATIONARY_LAVA)
			{
				spectatorsLobby.add(0, 1, 0);
				aboveLobby.add(0, 1, 0);
				belowLobby.add(0, 1, 0);

				if (spectatorsLobby.getY() > spectatorsLobby.getWorld().getHighestBlockYAt(spectatorsLobby))
				{
					spectatorsLobby.add(0, -2, 0);
					aboveLobby.add(0, -2, 0);
					belowLobby.add(0, -2, 0);

					break;
				}
			}

			spectator.setTeleporting(true);
			player.teleport(spectatorsLobby);
			spectator.setTeleporting(false);

			return true;
		}

		else if (Toggles.ONSPECMODECHANGED_TELEPORTATION_TOSPAWN.get())
		{
			if (Toggles.ONSPECMODECHANGED_TELEPORTATION_WITHSPAWNCMD.get() && Bukkit.getServer().getPluginCommand("spawn") != null)
			{
				return player.performCommand("spawn");
			}
			else
			{
				return player.teleport(player.getWorld().getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
			}
		}

		else return false;
	}

	public void setSpectatorsMode(SpectatorMode mode)
	{
		this.spectatorsMode = mode;
		saveSpectatorsSetup();

		// Needed to add (or remove) the arena selector
		getInventoryManager().equipSpectators();

		// Force-close the arena selector if the new mode is not the arena one.
		if (mode != SpectatorMode.ARENA)
			Gui.close(ArenasSelectorGUI.class);
	}

	public void setSpectatorsLobby(Location lobby)
	{
		spectatorsLobby = lobby;
		saveSpectatorsSetup();
	}

	public SpectatorMode getSpectatorsMode()
	{
		return spectatorsMode;
	}

	public Location getSpectatorsLobby()
	{
		return spectatorsLobby;
	}



	/* **  Spectators scoreboard  ** */

	/**
	 * Rebuilds the scoreboard if enabled; removes it else.
	 */
	public void rebuildScoreboard()
	{
		if (Toggles.SPECTATORS_TABLIST_PREFIX.get())
		{
			spectatorsScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

			spectatorsScoreboard.registerNewObjective(SPECTATORS_HEALTH_OBJECTIVE_NAME, "health")
					.setDisplaySlot(DisplaySlot.PLAYER_LIST);

			spectatorsTeam = spectatorsScoreboard.registerNewTeam(SPECTATORS_TEAM_NAME);
			spectatorsTeam.setPrefix(SPECTATORS_TEAM_PREFIX);
			spectatorsTeam.setSuffix(ChatColor.RESET.toString());

			if (Toggles.SPECTATORS_SEE_OTHERS.get())
				spectatorsTeam.setCanSeeFriendlyInvisibles(true);


			for (Player spectator : Bukkit.getOnlinePlayers())
			{
				if (SpectatorPlus.get().getPlayerData(spectator).isSpectating())
				{
					spectator.setScoreboard(spectatorsScoreboard);
					spectatorsTeam.addPlayer(spectator);
				}
			}
		}
		else if (spectatorsScoreboard != null)
		{
			spectatorsTeam.unregister();

			spectatorsTeam = null;
			spectatorsScoreboard = null;

			for (Player spectator : Bukkit.getOnlinePlayers())
			{
				SpectatorPlus.get().getPlayerData(spectator).resetScoreboard();
			}
		}
	}

	/**
	 * Sets the scoreboard to be used by the spectators.
	 */
	public void setSpectatorsScoreboard(Spectator spectator)
	{
		if (spectatorsScoreboard == null) return;

		Player player = spectator.getPlayer();
		if (player == null) return;

		if (spectator.isSpectating())
			player.setScoreboard(spectatorsScoreboard);
		else
			spectator.resetScoreboard();
	}

	public void setSpectatingInScoreboard(Spectator spectator)
	{
		if (spectatorsTeam == null) return;

		Player player = spectator.getPlayer();
		if (player == null) return;

		if (spectator.isSpectating())
			spectatorsTeam.addPlayer(player);
		else
			spectatorsTeam.removePlayer(player);
	}



	/* **  Spectators queries  ** */

	public List<Spectator> getVisiblePlayersFor(Spectator spectator)
	{
		Player player = spectator.getPlayer();
		if (player == null) return Collections.emptyList();


		List<Spectator> visiblePlayers = new ArrayList<>();

		for (Player viewedPlayer : Bukkit.getOnlinePlayers())
		{
			Spectator viewedSpectator = SpectatorPlus.get().getPlayerData(player);

			if (!viewedSpectator.isSpectating() || (viewedSpectator.isHiddenFromTp() && !player.hasPermission("spectate.admin.hide.see")))
				continue;

			switch (spectatorsMode)
			{
				case ANY:
					visiblePlayers.add(viewedSpectator);
					break;

				case WORLD:
					if (viewedPlayer.getWorld().equals(player.getWorld()))
						visiblePlayers.add(viewedSpectator);

					break;

				case ARENA:
					Arena arena = spectator.getArena();
					if (arena != null && arena.isInside(viewedPlayer.getLocation()))
						visiblePlayers.add(viewedSpectator);

					break;
			}
		}

		return visiblePlayers;
	}
}
