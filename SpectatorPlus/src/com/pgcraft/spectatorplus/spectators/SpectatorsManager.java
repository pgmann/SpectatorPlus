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

import com.pgcraft.spectatorplus.ConfigAccessor;
import com.pgcraft.spectatorplus.SpectatorPlus;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;


public class SpectatorsManager
{
	private SpectatorPlus p;

	private ConfigAccessor savedSpectatingPlayers;

	private Scoreboard spectatorsScoreboard;
	private Team spectatorsTeam;

	private static final String SPECTATORS_TEAM_NAME = "spectators";
	private static final String SPECTATORS_HEALTH_OBJECTIVE_NAME = "health";
	private static final String SPECTATORS_TEAM_PREFIX = ChatColor.GRAY + "Spectator " + ChatColor.DARK_GRAY + " ▏ ";


	public SpectatorsManager(SpectatorPlus plugin)
	{
		p = plugin;

		savedSpectatingPlayers = new ConfigAccessor(p, "specs");
		rebuildScoreboard();
	}

	public ConfigAccessor getSavedSpectatingPlayers()
	{
		return savedSpectatingPlayers;
	}



	/* **  Spectators scoreboard  ** */

	/**
	 * Rebuilds the scoreboard if enabled; removes it else.
	 */
	public void rebuildScoreboard()
	{
		if (/* FIXME scoreboard enabled */false)
		{
			spectatorsScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

			spectatorsScoreboard.registerNewObjective(SPECTATORS_HEALTH_OBJECTIVE_NAME, "health")
					.setDisplaySlot(DisplaySlot.PLAYER_LIST);

			spectatorsTeam = spectatorsScoreboard.registerNewTeam(SPECTATORS_TEAM_NAME);
			spectatorsTeam.setPrefix(SPECTATORS_TEAM_PREFIX);
			spectatorsTeam.setSuffix(ChatColor.RESET.toString());
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
}
