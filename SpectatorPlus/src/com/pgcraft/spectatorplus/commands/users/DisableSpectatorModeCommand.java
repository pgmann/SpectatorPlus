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
package com.pgcraft.spectatorplus.commands.users;

import com.pgcraft.spectatorplus.Permissions;
import com.pgcraft.spectatorplus.SpectatorPlus;
import com.pgcraft.spectatorplus.spectators.Spectator;
import com.pgcraft.spectatorplus.utils.SPUtils;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


@CommandInfo (name = "off", usageParameters = "[player name]")
public class DisableSpectatorModeCommand extends Command
{
	@Override
	protected void run() throws CommandException
	{
		// Disabled for self
		if (args.length == 0)
		{
			if (!Permissions.DISABLE_SPECTATOR_MODE.grantedTo(sender))
				throw new CommandException(this, CommandException.Reason.SENDER_NOT_AUTHORIZED);

			Spectator spectator = SpectatorPlus.get().getPlayerData(playerSender());

			if (spectator.isSpectating())
				spectator.setSpectating(false, playerSender());
			else
				warning("You are not spectating.");
		}

		// Disabled for another player
		else
		{
			if (!Permissions.CHANGE_SPECTATOR_MODE_FOR_OTHERS.grantedTo(sender))
				throw new CommandException(this, CommandException.Reason.SENDER_NOT_AUTHORIZED);

			String targetName = args[0];
			Player target = SPUtils.getPlayer(targetName);

			if (target == null || !target.isOnline())
			{
				error("The player " + targetName + " cannot be found or is offline.");
			}

			SpectatorPlus.get().getPlayerData(target).setSpectating(false, sender);
		}
	}

	@Override
	protected List<String> complete() throws CommandException
	{
		if (args.length == 1)
		{
			List<Player> candidates = new ArrayList<>();

			for (Player player : Bukkit.getOnlinePlayers())
				if (SpectatorPlus.get().getPlayerData(player).isSpectating())
					candidates.add(player);

			return getMatchingPlayerNames(candidates, args[0]);
		}

		else return null;
	}
}
