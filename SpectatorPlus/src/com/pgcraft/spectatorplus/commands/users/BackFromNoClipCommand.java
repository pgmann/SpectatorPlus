/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.pgcraft.spectatorplus.commands.users;

import com.pgcraft.spectatorplus.SpectatorPlus;
import com.pgcraft.spectatorplus.Toggles;
import com.pgcraft.spectatorplus.spectators.Spectator;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;


@CommandInfo (name = "b")
public class BackFromNoClipCommand extends Command
{
	@Override
	protected void run() throws CommandException
	{
		final Spectator spectator = SpectatorPlus.get().getPlayerData(playerSender());

		if (spectator.isSpectating() && playerSender().getGameMode() == GameMode.SPECTATOR)
		{
			spectator.setGamemodeChangeAllowed(true);
			playerSender().setGameMode(GameMode.ADVENTURE);
			spectator.setGamemodeChangeAllowed(false);

			playerSender().setAllowFlight(true);
			playerSender().setFlying(true); // The player comes from the spectator mode, so he was flying.

			SpectatorPlus.get().getSpectatorsManager().getInventoryManager().equipSpectator(playerSender());

			success("No-clip mode disabled.");
		}
	}

	@Override
	public boolean canExecute(CommandSender sender)
	{
		// Allowed if the vanilla spectator mode is not forced. No permission, as this is a workaround.
		return !Toggles.VANILLA_SPECTATOR_MODE.get();
	}
}
