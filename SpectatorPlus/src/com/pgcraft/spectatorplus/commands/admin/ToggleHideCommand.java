/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.pgcraft.spectatorplus.commands.admin;

import com.pgcraft.spectatorplus.Permissions;
import com.pgcraft.spectatorplus.SpectatorPlus;
import com.pgcraft.spectatorplus.spectators.Spectator;
import com.pgcraft.spectatorplus.utils.SPUtils;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import org.bukkit.entity.Player;


@CommandInfo (name = "togglehide", usageParameters = "[player]")
public class ToggleHideCommand extends Command
{
	@Override
	protected void run() throws CommandException
	{
		// Toggled for self
		if (args.length == 0)
		{
			if (!Permissions.HIDE_SELF_FROM_SPECTATORS.grantedTo(playerSender()))
				throw new CommandException(this, CommandException.Reason.SENDER_NOT_AUTHORIZED);

			Spectator spectator = SpectatorPlus.get().getPlayerData(playerSender());
			spectator.setHiddenFromTp(!spectator.isHiddenFromTp());

			if (spectator.isHiddenFromTp())
				success("The spectators can no longer see you.");
			else
				success("You are no longer hidden from spectators.");
		}

		// Toggled for other
		else
		{
			if (!Permissions.HIDE_OTHERS_FROM_SPECTATORS.grantedTo(playerSender()))
				throw new CommandException(this, CommandException.Reason.SENDER_NOT_AUTHORIZED);

			String targetName = args[0];
			Player target = SPUtils.getPlayer(targetName);

			if (target == null || !target.isOnline())
			{
				error("The player " + targetName + " cannot be found or is offline.");
			}

			Spectator spectator = SpectatorPlus.get().getPlayerData(target);
			spectator.setHiddenFromTp(!spectator.isHiddenFromTp());

			if (spectator.isHiddenFromTp())
			{
				success("The spectators can no longer see " + targetName + ".");
				SpectatorPlus.get().sendMessage(target, "You are now hidden from the spectators: they can no longer see you in the teleportation menu.", true);
			}
			else
			{
				success(targetName + " is no longer hidden from spectators.");
				SpectatorPlus.get().sendMessage(target, "You are no longer hidden from the spectators: they can teleport themselves to you, now.", true);
			}
		}
	}
}
