/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.pgcraft.spectatorplus.listeners;

import com.pgcraft.spectatorplus.Permissions;
import com.pgcraft.spectatorplus.SpectatorPlus;
import com.pgcraft.spectatorplus.Toggles;
import com.pgcraft.spectatorplus.spectators.Spectator;
import com.pgcraft.spectatorplus.tasks.AfterRespawnTask;
import fr.zcraft.zlib.tools.runners.RunTask;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;


public class ServerActionsListener implements Listener
{
	private SpectatorPlus p;

	public ServerActionsListener()
	{
		p = SpectatorPlus.get();
	}


	/**
	 * - Hides player with the permission when they join;
	 * - hides the spectating players from the joining player;
	 * - re-enables the spectator mode if it was enabled before.
	 */
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(final PlayerJoinEvent ev)
	{
		if (Permissions.AUTO_HIDE_FROM_SPECTATORS.grantedTo(ev.getPlayer()))
		{
			p.getPlayerData(ev.getPlayer()).setHiddenFromTp(true);
		}

		for (Player target : p.getServer().getOnlinePlayers())
		{
			if (p.getPlayerData(target).isSpectating())
			{
				ev.getPlayer().hidePlayer(target);
			}
		}

		if (Toggles.ONSPECMODECHANGED_SAVESPECTATORS.get() && p.getSpectatorsManager().getSavedSpectatingPlayers().getConfig().contains(ev.getPlayer().getUniqueId().toString()))
		{
			p.getPlayerData(ev.getPlayer()).setSpectating(true, true);
		}
	}

	/**
	 * If the player was spectating:
	 * - the spectator mode is disabled, so the in-game inventory is saved;
	 * - the fact that this player was spectating is stored into a file, to
	 *   re-enable the spectator mode on his next join.
	 */
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerQuit(final PlayerQuitEvent ev)
	{
		final Spectator spectator = p.getPlayerData(ev.getPlayer());

		if (spectator.isSpectating())
		{
			spectator.setSpectating(false, true);
			spectator.saveSpectatorModeInFile(true);
		}
	}


	/**
	 * Saves the death message & location, if the “teleportation to the death point”
	 * tool is enabled.
	 */
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerDeath(final PlayerDeathEvent ev)
	{
		if (Toggles.TOOLS_TOOLS_TPTODEATH_ENABLED.get())
		{
			final Player killed = ev.getEntity();
			final Spectator spectator = p.getPlayerData(killed);

			spectator.setDeathLocation(killed.getLocation());

			if (Toggles.TOOLS_TOOLS_TPTODEATH_DISPLAYCAUSE.get())
			{
				String deathMessage = ChatColor.stripColor(ev.getDeathMessage());
				String noColorsDisplayName = ChatColor.stripColor(killed.getDisplayName());

				if (deathMessage == null)
				{
					deathMessage = "";
				}
				else
				{
					deathMessage = deathMessage.replace(killed.getName() + " was", "You were")
							.replace(killed.getName(), "You")
							.replace(noColorsDisplayName + " was", "You were")
							.replace(noColorsDisplayName, "You");
				}

				spectator.setLastDeathMessage(ChatColor.stripColor(deathMessage));
			}
		}
	}

	/**
	 * Used to enable the spectator mode for dead players, if this option is enabled
	 * in the config.
	 */
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerRespawn(final PlayerRespawnEvent ev)
	{
		if (Toggles.SPECTATOR_MODE_ON_DEATH.get())
		{
			// Prevent murdering clients! (force close bug if spec mode is enabled instantly)
			RunTask.nextTick(new AfterRespawnTask(ev.getPlayer()));
		}
	}


	/**
	 * Handles MultiverseInventories & other similar plugins.
	 *
	 * Disables spectate mode to restore proper inventory before world change; then
	 * re-enables spectate mode to restore spectator inventory after world change.
	 */
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onWorldChange(final PlayerChangedWorldEvent ev)
	{
		final Spectator spectator = p.getPlayerData(ev.getPlayer());

		if (spectator.isSpectating())
		{
			spectator.setWasSpectatorBeforeWorldChanged(true);
			spectator.setSpectating(false, null, true, true);

			RunTask.later(new Runnable()
			{
				@Override
				public void run()
				{
					if (spectator.wasSpectatorBeforeWorldChanged())
					{
						spectator.setSpectating(true, null, true, true);
						spectator.setWasSpectatorBeforeWorldChanged(false);
					}
				}
			}, 5l);
		}
	}


	/**
	 * Used to prevent spectators from changing their gamemode whilst spectating.
	 */
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onGamemodeChange(final PlayerGameModeChangeEvent e)
	{
		final Spectator spectator = p.getPlayerData(e.getPlayer());

		if (spectator.isSpectating() && e.getNewGameMode() != GameMode.ADVENTURE && !spectator.isGamemodeChangeAllowed())
		{
			e.setCancelled(true);
			e.getPlayer().setAllowFlight(true);
		}
	}


	/**
	 * Used to prevent the food level to drop if the player is a spectator.
	 */
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onFoodLevelChange(final FoodLevelChangeEvent e)
	{
		if (e.getEntity() instanceof Player && !e.getEntity().hasMetadata("NPC") && p.getPlayerData((Player) e.getEntity()).isSpectating())
		{
			e.setCancelled(true);

			((Player) e.getEntity()).setFoodLevel(20);
			((Player) e.getEntity()).setSaturation(20);
		}
	}
}
