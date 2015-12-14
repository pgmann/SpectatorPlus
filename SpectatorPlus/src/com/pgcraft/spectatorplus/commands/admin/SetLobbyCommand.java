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
				info("The spectators lobby has been set on your current location.");
				break;

			case "delete":
				SpectatorPlus.get().getSpectatorsManager().setSpectatorsLobby(null);
				info("The spectators lobby was successfully deleted. The spectators will no longer be teleported to it when they enter spectating mode.");
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
