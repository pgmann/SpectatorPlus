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


@CommandInfo (name = "say", usageParameters = "<message>")
public class BroadcastCommand extends Command
{
	@Override
	protected void run() throws CommandException
	{
		if (!Permissions.BROADCAST_MESSAGES_TO_SPECTATORS.grantedTo(sender))
			throw new CommandException(this, CommandException.Reason.SENDER_NOT_AUTHORIZED);

		String message = "";
		for (String part : args)
			message += part + " ";

		SpectatorPlus.get().getSpectatorsManager().getChatManager().broadcastToSpectators(sender, message.trim());
	}
}
