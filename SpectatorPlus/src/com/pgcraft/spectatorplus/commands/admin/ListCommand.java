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

import com.pgcraft.spectatorplus.SpectatorPlus;
import com.pgcraft.spectatorplus.arenas.Arena;
import com.pgcraft.spectatorplus.spectators.Spectator;
import com.pgcraft.spectatorplus.spectators.SpectatorMode;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


@CommandInfo(name = "list", usageParameters = "[arena name]")
public class ListCommand extends Command
{
	@Override
	protected void run() throws CommandException
	{
		List<Player> spectators = new ArrayList<>();
		String title;

		if (args.length == 0)
		{
			for (Player player : Bukkit.getOnlinePlayers())
				if (SpectatorPlus.get().getPlayerData(player).isSpectating())
					spectators.add(player);

			title = ChatColor.GOLD + "" + ChatColor.BOLD + "All spectating players " + ChatColor.RED + "(" + spectators.size() + ")";
		}
		else
		{
			Arena arena = SpectatorPlus.get().getArenasManager().getArena(args[0]);

			if (arena == null)
				error("The arena " + args[0] + " does not exists.");

			for (Player player : Bukkit.getOnlinePlayers())
			{
				Spectator spectator = SpectatorPlus.get().getPlayerData(player);

				if (spectator.isSpectating() && arena.equals(spectator.getArena()))
					spectators.add(player);
			}

			title = ChatColor.GOLD + "" + ChatColor.BOLD + "Spectators in the " + arena.getName() + " arena " + ChatColor.RED + "(" + spectators.size() + ")";
		}

		sender.sendMessage(title);

		if (spectators.size() == 0)
		{
			warning("(No player to display.)");
		}
		else
		{
			for (int i = 0; i < spectators.size(); i += 3)
			{
				String line = spectators.get(i).getDisplayName();
				if (i + 1 < spectators.size())
					line += ChatColor.GRAY + " - " + spectators.get(i + 1).getDisplayName();
				if (i + 2 < spectators.size())
					line += ChatColor.GRAY + " - " + spectators.get(i + 2).getDisplayName();

				sender.sendMessage(line);
			}
		}

		if (args.length == 0 && SpectatorPlus.get().getSpectatorsManager().getSpectatorsMode() == SpectatorMode.ARENA)
		{
			info("");
			info("Tip! Use /spec list <arenaName> to list the spectators in the given arena.");
		}
	}

	@Override
	protected List<String> complete() throws CommandException
	{
		if (args.length == 1)
		{
			List<String> suggestions = new ArrayList<>();

			for (Arena arena : SpectatorPlus.get().getArenasManager().getArenas())
				suggestions.add(arena.getName());

			return getMatchingSubset(suggestions, args[0]);
		}

		else return null;
	}
}
