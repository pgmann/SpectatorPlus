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
		ev.setCancelled(handleSetup(ev.getPlayer(), ev.getBlock().getLocation()));
	}

	/**
	 * Used to setup an arena (if the command was sent before by the sender).
	 */
	@EventHandler
	protected void onBlockDamage(BlockDamageEvent ev)
	{
		ev.setCancelled(handleSetup(ev.getPlayer(), ev.getBlock().getLocation()));
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
