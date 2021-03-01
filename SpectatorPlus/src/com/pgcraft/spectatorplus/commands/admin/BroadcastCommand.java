/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.pgcraft.spectatorplus.commands.admin;

import com.pgcraft.spectatorplus.Permissions;
import com.pgcraft.spectatorplus.SpectatorPlus;
import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import org.bukkit.command.CommandSender;


@CommandInfo(name = "say", usageParameters = "<message>")
public class BroadcastCommand extends Command {
    @Override
    protected void run() {
        StringBuilder message = new StringBuilder();
        for (String part : args)
            message.append(part).append(" ");

        SpectatorPlus.get().getSpectatorsManager().getChatManager().broadcastToSpectators(sender, message.toString().trim());
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return Permissions.BROADCAST_MESSAGES_TO_SPECTATORS.grantedTo(sender);
    }
}
