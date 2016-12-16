/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.pgcraft.spectatorplus.listeners;

import com.pgcraft.spectatorplus.SpectatorPlus;
import com.pgcraft.spectatorplus.arenas.ArenaSetup;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;


public class ArenaSetupListener implements Listener
{
	private SpectatorPlus p;

	public ArenaSetupListener()
	{
		this.p = SpectatorPlus.get();
	}


	/**
	 * Used to setup an arena, if the command was sent before by this player.
	 */
	@EventHandler
	protected void onBlockBreak(BlockBreakEvent ev)
	{
		if(handleSetup(ev.getPlayer(), ev.getBlock().getLocation()))
			ev.setCancelled(true);
	}

	/**
	 * Used to setup an arena (if the command was sent before by the sender).
	 */
	@EventHandler
	protected void onBlockDamage(BlockDamageEvent ev)
	{
		if(handleSetup(ev.getPlayer(), ev.getBlock().getLocation()))
			ev.setCancelled(true);
	}


	/**
	 * Used to setup an arena (if the command was sent before by the sender).
	 *
	 * @return {@code true} if the underlying event needs to be cancelled.
	 */
	private boolean handleSetup(Player player, Location location)
	{
		ArenaSetup setup = p.getPlayerData(player).getArenaSetup();

		if (setup != null)
		{
			setup.next(location);
			return true;
		}

		else return false;
	}
}
