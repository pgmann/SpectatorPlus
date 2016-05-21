/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.pgcraft.spectatorplus.listeners;

import com.pgcraft.spectatorplus.Permissions;
import com.pgcraft.spectatorplus.SpectatorPlus;
import com.pgcraft.spectatorplus.Toggles;
import com.pgcraft.spectatorplus.spectators.Spectator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;


public class SpectatorsChatListener implements Listener
{
	private SpectatorPlus p;

	public SpectatorsChatListener()
	{
		p = SpectatorPlus.get();
	}


	/**
	 * Used to hide chat messages sent by spectators, if the spectator chat is enabled.<br>
	 * Spectators with appropriate permission can shout to bypass spectator chat by using a configurable message prefix.
	 */
	// Ignore cancelled, so another plugin can implement a private chat without conflicts.
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChatSend(AsyncPlayerChatEvent ev)
	{
		if (Toggles.CHAT_ENABLED.get() && p.getPlayerData(ev.getPlayer()).isSpectating())
		{
			if(Toggles.CHAT_SHOUT_ENABLED.get()
					&& ev.getMessage().startsWith(Toggles.CHAT_SHOUT_PREFIX.get())
					&& Permissions.CHAT_SHOUT.grantedTo(ev.getPlayer()))
			{
				// Spectator SHOUT - trim off the SHOUT_PREFIX leaving the actual message
				ev.setMessage(ev.getMessage().substring(Toggles.CHAT_SHOUT_PREFIX.get().length()));
			} else {
				// Spectator CHAT
				ev.setCancelled(true);
				p.getSpectatorsManager().getChatManager().sendSpectatorsChatMessage(ev.getPlayer(), ev.getMessage(), false);
			}
		}
	}

	/**
	 * <ul>
	 * <li>Prevents a command being executed if the player is a spectator and the option is set in the config;</li>
	 * <li>Catches /me commands to show them in the spectator chat (if the user isn't shouting);</li>
	 * <li>Allows specified commands from the whitelist section to be executed.</li>
	 */
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onCommandPreprocessed(PlayerCommandPreprocessEvent ev)
	{
		final Spectator spectator = p.getPlayerData(ev.getPlayer());

		if (!spectator.isSpectating())
			return;


		if (Toggles.CHAT_ENABLED.get() && ev.getMessage().toLowerCase().startsWith("/me "))
		{
			// Spectators with the appropriate permission can shout /me messages too (e.g. "/me !hi")
			if(Toggles.CHAT_SHOUT_ENABLED.get()
					&& ev.getMessage().startsWith("/me " + Toggles.CHAT_SHOUT_PREFIX.get())
					&& Permissions.CHAT_SHOUT.grantedTo(ev.getPlayer()))
			{
				// Spectator SHOUT - trim off the SHOUT_PREFIX leaving the actual message
				ev.setMessage(ev.getMessage().replaceFirst(Toggles.CHAT_SHOUT_PREFIX.get(), ""));
			}
			else
			{
				// Spectator CHAT (action)
				ev.setCancelled(true);
				p.getSpectatorsManager().getChatManager().sendSpectatorsChatMessage(ev.getPlayer(), ev.getMessage().substring(4), true);
			}
		}

		else if (Toggles.CHAT_BLOCKCOMMANDS_ENABLED.get() && !p.getSpectatorsManager().getChatManager().isCommandWhitelisted(ev.getMessage(), ev.getPlayer()))
		{
			ev.setCancelled(true);
			p.sendMessage(ev.getPlayer(), "You are not allowed to send this command while in spectator mode.", true);
		}
	}

	/**
	 * Adds autocompletion for spectators even if the player can't see them.
	 */
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onTabComplete(PlayerChatTabCompleteEvent ev)
	{
		if ((Toggles.AUTOCOMPLETE_SPECTATORS_FOR_PLAYERS.get() && !SpectatorPlus.get().getPlayerData(ev.getPlayer()).isSpectating()) || Toggles.AUTOCOMPLETE_SPECTATORS_FOR_SPECTATORS.get())
		{
			final String lowerCaseLastToken = ev.getLastToken().toLowerCase();

			for (Player player : Bukkit.getOnlinePlayers())
			{
				if (SpectatorPlus.get().getPlayerData(player).isSpectating() && !ev.getTabCompletions().contains(player.getName()))
				{
					if (player.getName().toLowerCase().startsWith(lowerCaseLastToken))
					{
						ev.getTabCompletions().add(player.getName());
					}
				}
			}
		}
	}
}
