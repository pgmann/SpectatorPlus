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

import com.pgcraft.spectatorplus.Permissions;
import com.pgcraft.spectatorplus.SpectatorPlus;
import com.pgcraft.spectatorplus.Toggles;
import com.pgcraft.spectatorplus.utils.SPUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class SpectatorsChatManager
{
	private static final String SPECTATORS_CHAT_PREFIX = ChatColor.GRAY + "Spectators " + ChatColor.DARK_GRAY + " ▏ ";

	private static final String SPECTATORS_CHAT_FORMAT = SPECTATORS_CHAT_PREFIX + ChatColor.RESET + "{name}" + ChatColor.GRAY + " : {message}";
	private static final String SPECTATORS_ACTION_FORMAT = SPECTATORS_CHAT_PREFIX + "* " + ChatColor.RESET + "{name}" + ChatColor.GRAY + " {message}";
	private static final String SPECTATORS_BROADCAST_MESSAGE_FORMAT = ChatColor.GOLD + "[{name}" + ChatColor.GOLD + " -> spectators] " + ChatColor.RESET + "{message}";


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
		for (String rawCommand : Toggles.CHAT_BLOCKCOMMANDS_WHITELIST.get())
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
		sendRawMessageToSpectators(formatMessage(isAction ? SPECTATORS_ACTION_FORMAT : SPECTATORS_CHAT_FORMAT, sender, message));
	}

	/**
	 * Broadcasts a message to all players with spectator mode enabled, and the sender.
	 *
	 * @param sender  The sender of the message to broadcast.
	 * @param message The message to broadcast.
	 */
	public void broadcastToSpectators(final CommandSender sender, final String message)
	{
		String formattedMessage = formatMessage(SPECTATORS_BROADCAST_MESSAGE_FORMAT, sender, message);

		sendRawMessageToSpectators(formattedMessage);

		// If the sender don't receive the spectators chat
		if (sender instanceof Player && !p.getPlayerData(((Player) sender)).isSpectating())
		{
			sender.sendMessage(formattedMessage);
		}
	}


	/**
	 * Formats a message: replaces {name} by the display name of the sender, and {message} by the
	 * message.
	 *
	 * @param format  The format.
	 * @param sender  The sender.
	 * @param message The message.
	 *
	 * @return The formatted message.
	 */
	private String formatMessage(final String format, final CommandSender sender, final String message)
	{
		return format
				.replace("{name}", SPUtils.getName(sender))
				.replace("{message}", message);
	}

	/**
	 * Sends a message to all spectators and the console.
	 *
	 * @param message The message to be sent.
	 */
	private void sendRawMessageToSpectators(final String message)
	{
		for (Player player : p.getServer().getOnlinePlayers())
		{
			if (p.getPlayerData(player).isSpectating())
			{
				player.sendMessage(message);
			}
		}

		p.getServer().getConsoleSender().sendMessage(message);
	}
}
