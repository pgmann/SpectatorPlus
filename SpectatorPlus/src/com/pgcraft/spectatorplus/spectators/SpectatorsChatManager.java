/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.pgcraft.spectatorplus.spectators;

import com.pgcraft.spectatorplus.Permissions;
import com.pgcraft.spectatorplus.SpectatorPlus;
import com.pgcraft.spectatorplus.Toggles;
import com.pgcraft.spectatorplus.utils.SPUtils;
import fr.zcraft.zlib.components.rawtext.RawText;
import fr.zcraft.zlib.tools.text.MessageSender;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class SpectatorsChatManager
{
	private static final String SPECTATORS_CHAT_PREFIX = ChatColor.GRAY + "Spectators " + ChatColor.DARK_GRAY + ChatColor.BOLD + "\u2503 "; // box drawings heavy vertical

	private final SpectatorPlus p;

	private final List<String> internalWhitelist = new CopyOnWriteArrayList<>();
	private final List<String> commandsWhitelist = new CopyOnWriteArrayList<>();


	public SpectatorsChatManager()
	{
		p = SpectatorPlus.get();

		reloadCommandsWhitelist();
	}



	/* **  Commands whitelisting  ** */

	/**
	 * Checks if a command is whitelisted.
	 *
	 * @param rawCommand The raw command sent by the player.
	 * @param sender The command's sender.
	 *
	 * @return {@code true} if, supposing that this player is spectating, he can use this command.
	 */
	public boolean isCommandWhitelisted(String rawCommand, final Player sender)
	{
		if (Toggles.CHAT_BLOCKCOMMANDS_ADMINBYPASS.get() && Permissions.BYPASS_COMMANDS_WHITELIST.grantedTo(sender))
			return true;

		rawCommand = rawCommand.toLowerCase();

		for (String whitelistedCommand : internalWhitelist)
			if (rawCommand.startsWith(whitelistedCommand))
				return true;

		for (String whitelistedCommand : commandsWhitelist)
			if (rawCommand.startsWith(whitelistedCommand))
				return true;

		return false;
	}

	/**
	 * Reloads the whitelist from the stored configuration.
	 */
	public void reloadCommandsWhitelist()
	{
		internalWhitelist.clear();
		commandsWhitelist.clear();

		// Internal static whitelist
		internalWhitelist.addAll(Arrays.asList("spec", "spectate"));

		// Configurable whitelist
		for (String rawCommand : Toggles.CHAT_BLOCKCOMMANDS_WHITELIST)
			addCommandToWhitelist(rawCommand, false);
	}

	/**
	 * Adds a command to the whitelist.
	 *
	 * @param rawCommand The raw command to add.
	 * @param save {@code true} to save the command into the configuration file.
	 */
	public void addCommandToWhitelist(String rawCommand, boolean save)
	{
		if (rawCommand.startsWith("/"))
			rawCommand = rawCommand.substring(1);

		commandsWhitelist.add(rawCommand.toLowerCase());

		if (save)
			Toggles.CHAT_BLOCKCOMMANDS_WHITELIST.set(commandsWhitelist, true);
	}

	/**
	 * Removes a command from the whitelist.
	 *
	 * @param rawCommand The raw command to remove.
	 * @param save {@code true} to save the change into the configuration file.
	 */
	public void removeCommandFromWhitelist(String rawCommand, boolean save)
	{
		commandsWhitelist.remove(rawCommand);

		if (save)
			Toggles.CHAT_BLOCKCOMMANDS_WHITELIST.set(commandsWhitelist, true);
	}



	/* **  Private chat  ** */

	/**
	 * Sends a chat message on behalf of the given sender, in the spectators chat channel.
	 *
	 * @param sender   The message's sender.
	 * @param message  The message.
	 * @param isAction {@code true} if this is an action message (/me).
	 */
	public void sendSpectatorsChatMessage(final CommandSender sender, final String message, final boolean isAction)
	{
		if (isAction)
		{
			sendRawMessageToSpectators(
					new RawText(SPECTATORS_CHAT_PREFIX)
							.then("* ")
								.color(ChatColor.GRAY)
							.then(SPUtils.getName(sender))
								.color(ChatColor.WHITE)
							.then(" ")
							.then(message)
								.color(ChatColor.GRAY)
							.build()
			);
		}
		else
		{
			sendRawMessageToSpectators(
					new RawText(SPECTATORS_CHAT_PREFIX)
							.then(SPUtils.getName(sender))
								.color(ChatColor.WHITE)
							.then(" : ")
								.color(ChatColor.DARK_GRAY)
							.then(message)
								.color(ChatColor.GRAY)
							.build()
			);
		}
	}

	/**
	 * Broadcasts a message to all players with spectator mode enabled, and the sender.
	 *
	 * @param sender  The sender of the message to broadcast.
	 * @param message The message to broadcast.
	 */
	public void broadcastToSpectators(final CommandSender sender, final String message)
	{
		RawText broadcast = new RawText("")
				.then("[" + SPUtils.getName(sender))
					.color(ChatColor.GOLD)
				.then(" -> spectators] ")
					.color(ChatColor.GOLD)
				.then(message)
					.color(ChatColor.WHITE)
				.build();

		sendRawMessageToSpectators(broadcast);

		// If the sender don't receive the spectators chat
		if (sender instanceof Player && !p.getPlayerData(((Player) sender)).isSpectating())
		{
			MessageSender.sendChatMessage(((Player) sender), broadcast);
		}
	}

	/**
	 * Sends a message to all spectators and the console.
	 *
	 * @param message The message to be sent.
	 */
	private void sendRawMessageToSpectators(final RawText message)
	{
		for (Player player : p.getServer().getOnlinePlayers())
		{
			if (p.getPlayerData(player).isSpectating())
			{
				MessageSender.sendChatMessage(player, message);
			}
		}

		p.getServer().getConsoleSender().sendMessage(message.toFormattedText());
	}
}
