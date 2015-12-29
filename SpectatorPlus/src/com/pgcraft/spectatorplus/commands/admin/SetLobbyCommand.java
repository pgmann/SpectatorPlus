/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.pgcraft.spectatorplus.commands.admin;


import com.pgcraft.spectatorplus.Permissions;
import com.pgcraft.spectatorplus.SpectatorPlus;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;

import java.util.Arrays;
import java.util.List;


@CommandInfo (name = "lobby", usageParameters = "<set|delete>")
public class SetLobbyCommand extends Command
{
	@Override
	protected void run() throws CommandException
	{
		if (!Permissions.CHANGE_SPECTATORS_LOBBY.grantedTo(sender))
			throw new CommandException(this, CommandException.Reason.SENDER_NOT_AUTHORIZED);

		if (args.length < 1)
			throwInvalidArgument("No action provided.");

		switch (args[0].toLowerCase().trim())
		{
			case "set":
				SpectatorPlus.get().getSpectatorsManager().setSpectatorsLobby(playerSender().getLocation());
				success("The spectators lobby has been set on your current location.");
				break;

			case "delete":
				SpectatorPlus.get().getSpectatorsManager().setSpectatorsLobby(null);
				success("The spectators lobby was successfully deleted. The spectators will no longer be teleported to it when they enter spectating mode.");
				break;

			default:
				throwInvalidArgument("We don't understand « " + args[0] + " ».");
				break;
		}
	}

	@Override
	protected List<String> complete() throws CommandException
	{
		if (args.length == 1)
		{
			return getMatchingSubset(Arrays.asList("set", "delete"), args[0]);
		}

		else return null;
	}
}
