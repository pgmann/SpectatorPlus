/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.pgcraft.spectatorplus.commands.admin;

import com.pgcraft.spectatorplus.Permissions;
import com.pgcraft.spectatorplus.SpectatorPlus;
import com.pgcraft.spectatorplus.arenas.Arena;
import com.pgcraft.spectatorplus.spectators.Spectator;
import com.pgcraft.spectatorplus.spectators.SpectatorMode;
import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


@CommandInfo(name = "list", usageParameters = "[arena name]")
public class ListCommand extends Command {
    @Override
    protected void run() throws CommandException {
        List<Player> spectators = new ArrayList<>();
        String title;

        if (args.length == 0) {
            for (Player player : Bukkit.getOnlinePlayers())
                if (SpectatorPlus.get().getPlayerData(player).isSpectating())
                    spectators.add(player);

            title = ChatColor.GOLD + "" + ChatColor.BOLD + "All spectating players " + ChatColor.RED + "(" + spectators.size() + ")";
        } else {
            Arena arena = SpectatorPlus.get().getArenasManager().getArena(args[0]);

            if (arena == null)
                error("The arena " + args[0] + " does not exist.");

            for (Player player : Bukkit.getOnlinePlayers()) {
                Spectator spectator = SpectatorPlus.get().getPlayerData(player);

                if (spectator.isSpectating() && arena.equals(spectator.getArena()))
                    spectators.add(player);
            }

            title = ChatColor.GOLD + "" + ChatColor.BOLD + "Spectators in the " + arena.getName() + " arena " + ChatColor.RED + "(" + spectators.size() + ")";
        }

        sender.sendMessage(title);

        if (spectators.size() == 0) {
            warning("(No players to display.)");
        } else {
            final String separator = ChatColor.GRAY + " - " + ChatColor.RESET;

            for (int i = 0; i < spectators.size(); i += 3) {
                String line = spectators.get(i).getDisplayName();
                if (i + 1 < spectators.size()) line += separator + spectators.get(i + 1).getDisplayName();
                if (i + 2 < spectators.size()) line += separator + spectators.get(i + 2).getDisplayName();

                sender.sendMessage(line);
            }
        }

        if (args.length == 0 && SpectatorPlus.get().getSpectatorsManager().getSpectatorsMode() == SpectatorMode.ARENA) {
            info("");
            info("Tip! Use /spec list <arenaName> to list the spectators in the given arena.");
        }
    }

    @Override
    protected List<String> complete() {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();

            for (Arena arena : SpectatorPlus.get().getArenasManager().getArenas())
                suggestions.add(arena.getName());

            return getMatchingSubset(suggestions, args[0]);
        } else return null;
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return Permissions.LIST_SPECTATORS.grantedTo(sender);
    }
}
