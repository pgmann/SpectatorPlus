/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.pgcraft.spectatorplus.commands.admin;

import com.pgcraft.spectatorplus.Permissions;
import com.pgcraft.spectatorplus.SpectatorPlus;
import com.pgcraft.spectatorplus.spectators.SpectatorMode;
import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;


@CommandInfo(name = "mode", usageParameters = "<ANY|WORLD|ARENA>")
public class SetSpectatingModeCommand extends Command {
    @Override
    protected void run() throws CommandException {
        if (args.length < 1)
            throwInvalidArgument("Spectating mode missing.");

        final SpectatorMode mode = getEnumParameter(0, SpectatorMode.class);

        SpectatorPlus.get().getSpectatorsManager().setSpectatorsMode(mode);
        success("Spectating mode changed to " + mode + ".");
        info(mode.getDescription());
    }

    @Override
    protected List<String> complete() {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();

            for (SpectatorMode mode : SpectatorMode.values())
                suggestions.add(mode.name().toUpperCase());

            return getMatchingSubset(suggestions, args[0].toUpperCase());
        } else return null;
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return Permissions.CHANGE_SPECTATING_MODE.grantedTo(sender);
    }
}
