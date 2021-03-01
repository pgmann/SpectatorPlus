/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.pgcraft.spectatorplus.commands.users;

import com.pgcraft.spectatorplus.Permissions;
import com.pgcraft.spectatorplus.SpectatorPlus;
import com.pgcraft.spectatorplus.spectators.Spectator;
import com.pgcraft.spectatorplus.utils.SPUtils;
import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


@CommandInfo(name = "on", usageParameters = "[player name]")
public class EnableSpectatorModeCommand extends Command {
    @Override
    protected void run() throws CommandException {
        // Enabled for self
        if (args.length == 0) {
            Spectator spectator = SpectatorPlus.get().getPlayerData(playerSender());

            if (!spectator.isSpectating())
                spectator.setSpectating(true, playerSender());
            else
                warning("You are already spectating.");
        }

        // Enable for another player
        else {
            String targetName = args[0];
            Player target = SPUtils.getPlayer(targetName);

            if (target == null || !target.isOnline()) {
                error("The player " + targetName + " cannot be found or is offline.");
            }

            SpectatorPlus.get().getPlayerData(target).setSpectating(true, sender);
        }
    }

    @Override
    protected List<String> complete() {
        if (args.length == 1) {
            List<Player> candidates = new ArrayList<>();

            for (Player player : Bukkit.getOnlinePlayers())
                if (!SpectatorPlus.get().getPlayerData(player).isSpectating())
                    candidates.add(player);

            return getMatchingPlayerNames(candidates, args[0]);
        } else return null;
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return (args != null && args.length == 0 && Permissions.ENABLE_SPECTATOR_MODE.grantedTo(sender)) || Permissions.CHANGE_SPECTATOR_MODE_FOR_OTHERS.grantedTo(sender);
    }
}
