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
import com.pgcraft.spectatorplus.Toggles;
import com.pgcraft.spectatorplus.spectators.Spectator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;


public class SpectatorsChatListener implements Listener
{
	private SpectatorPlus p;

	public SpectatorsChatListener()
	{
		p = SpectatorPlus.get();
	}


	/**
	 * Used to hide chat messages sent by spectators, if the spectator chat is enabled.
	 */
	// Ignore cancelled, so another plugin can implement a private chat without conflicts.
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	protected void onChatSend(AsyncPlayerChatEvent ev)
	{
		if (Toggles.CHAT_ENABLED.get() && p.getPlayerData(ev.getPlayer()).isSpectating())
		{
			ev.setCancelled(true);
			p.getSpectatorsManager().getChatManager().sendSpectatorsChatMessage(ev.getPlayer(), ev.getMessage(), false);
		}
	}

	/**
	 * Prevents a command to be executed if the player is a spectator and the option is set in the
	 * config; catches /me commands to show them into the spectator chat; allows specified commands
	 * from the whitelist section to be executed.
	 */
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	protected void onCommandPreprocessed(PlayerCommandPreprocessEvent ev)
	{
		final Spectator spectator = p.getPlayerData(ev.getPlayer());

		if (!spectator.isSpectating())
			return;


		if (Toggles.CHAT_ENABLED.get() && ev.getMessage().toLowerCase().startsWith("/me "))
		{
			ev.setCancelled(true);
			p.getSpectatorsManager().getChatManager().sendSpectatorsChatMessage(ev.getPlayer(), ev.getMessage().substring(4), true);
		}

		else if (Toggles.CHAT_BLOCKCOMMANDS_ENABLED.get() && !p.getSpectatorsManager().getChatManager().isCommandWhitelisted(ev.getMessage(), ev.getPlayer()))
		{
			ev.setCancelled(true);
			p.sendMessage(ev.getPlayer(), "You are not allowed to send this command while in spectator mode.", true);
		}
	}
}
