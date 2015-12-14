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
				info("The spectators can no longer see you.");
			else
				info("You are no longer hidden from spectators.");
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
				info("The spectators can no longer see " + targetName + ".");
				SpectatorPlus.get().sendMessage(target, "You are now hidden from the spectators: they can no longer see you in the teleportation menu.", true);
			}
			else
			{
				info(targetName + " is no longer hidden from spectators.");
				SpectatorPlus.get().sendMessage(target, "You are no longer hidden from the spectators: they can teleport themselves to you, now.", true);
			}
		}
	}
}
