/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.pgcraft.spectatorplus.commands.admin;

import com.pgcraft.spectatorplus.Permissions;
import com.pgcraft.spectatorplus.SpectatorPlus;
import com.pgcraft.spectatorplus.arenas.Arena;
import com.pgcraft.spectatorplus.arenas.ArenaSetup;
import com.pgcraft.spectatorplus.arenas.ArenasManager;
import com.pgcraft.spectatorplus.utils.SPUtils;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.tools.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


@CommandInfo (name = "arena", usageParameters = "< add <name> | remove <name> | list | lobby <name> [remove] | reset >")
public class ManageArenasCommand extends Command
{
	@Override
	protected void run() throws CommandException
	{
		if (args.length == 0)
			throwInvalidArgument("Action missing.");

		switch (args[0].toLowerCase().trim())
		{
			case "add":
				add();
				break;

			case "remove":
				remove();
				break;

			case "list":
				list();
				break;

			case "lobby":
				lobby();
				break;

			case "reset":
				reset();
				break;

			case "corner":
				corner();
				break;

			default:
				throwInvalidArgument("We don't understand « " + args[0] + " ».");
		}
	}


	private void add() throws CommandException
	{
		if (args.length == 1 || (args.length > 2 && args.length < 6) || (args.length > 7 && args.length < 8))
		{
			warning(ChatColor.BOLD + "Usage:");
			warning("/spec arena add <name> - interactive arena setup (player only);");
			warning("/spec arena add <name> <x1> <z1> <x2> <z2> [world] - arena from the bottom to the top of the map inside these coordinates;");
			warning("/spec arena add <name> <x1> <y1> <z1> <x2> <y2> <z2> [world] - arena inside these coordinates.");
			info("In the two commands above, the world is required if the sender is the console; else (command block or player) the current world is used if missing.");

			return;
		}

		String arenaName = args[1].trim();

		Arena exists = SpectatorPlus.get().getArenasManager().getArena(arenaName);
		if (exists != null)
		{
			error("An arena with this name (" + arenaName + ") already exists!");
		}


		Location corner1 = null;
		Location corner2 = null;

		// Interactive setup
		if (args.length == 2)
		{
			SpectatorPlus.get().getPlayerData(playerSender()).setArenaSetup(new ArenaSetup(playerSender(), arenaName));
			return;
		}

		// 4 coordinates
		else if (args.length == 6 || args.length == 7)
		{
			World world = getWorld(6);

			corner1 = new Location(
					world,
					getDoubleParameter(2),
					0d,
					getDoubleParameter(3)
			);

			corner2 = new Location(
					world,
					getDoubleParameter(4),
					world.getMaxHeight(),
					getDoubleParameter(5)
			);
		}

		// 6 coordinates
		else if (args.length == 8 || args.length == 9)
		{
			World world = getWorld(8);

			corner1 = new Location(
					world,
					getDoubleParameter(2),
					getDoubleParameter(3),
					getDoubleParameter(4)
			);

			corner2 = new Location(
					world,
					getDoubleParameter(5),
					getDoubleParameter(6),
					getDoubleParameter(7)
			);
		}


		if (corner1 != null && corner2 != null)
		{
			try
			{
				SpectatorPlus.get().getArenasManager().registerArena(new Arena(arenaName, corner1, corner2));
				success("Successfully registered the arena " + arenaName + "!");
			}
			catch (IllegalArgumentException e)
			{
				PluginLogger.error("An error occurred while adding the arena {0}.", e, arenaName);
				error("An error occurred while adding the arena " + arenaName + "; see console for details.");
			}
		}
		else
		{
			error("An unknown error occurred while registering the " + arenaName + " arena. Error code: NULL_CORNER.");
		}
	}

	private World getWorld(int argIndex) throws CommandException
	{
		World world = null;

		if (args.length <= argIndex && sender instanceof ConsoleCommandSender)
			throwInvalidArgument("The world is required from the console.");

		if (args.length > argIndex)
		{
			world = Bukkit.getWorld(args[argIndex]);

			if (world == null)
				throwInvalidArgument("Unknown world " + args[argIndex] + ".");
		}
		else
		{
			if (sender instanceof Player)
				world = ((Player) sender).getWorld();
			else if (sender instanceof BlockCommandSender)
				world = ((BlockCommandSender) sender).getBlock().getWorld();
		}

		if (world == null)
			error("Unknown error occurred while looking for the world " + (args.length > argIndex ? args[argIndex] : "of the sender") + ". Error code: NULL_WORLD.");

		return world;
	}


	private void remove() throws CommandException
	{
		if (args.length == 1)
			throwInvalidArgument("Missing arena name.");

		final ArenasManager arenasManager = SpectatorPlus.get().getArenasManager();

		final String arenaName = args[1].trim();
		final Arena toDelete = arenasManager.getArena(arenaName);

		if (toDelete == null)
			error("We can't found an arena with the name « " + arenaName + " ».");

		arenasManager.unregisterArena(toDelete);
		info("The arena " + arenaName + " was successfully deleted. 410 Gone.");
	}


	private void list() throws CommandException
	{
		Collection<Arena> arenas = SpectatorPlus.get().getArenasManager().getArenas();

		info(ChatColor.GOLD + "" + ChatColor.BOLD + "Registered arenas " + ChatColor.RED + "(" + arenas.size() + ")");

		for (Arena arena : arenas)
		{
			String description = ChatColor.GOLD + " - " + ChatColor.RED + arena.getName() + ChatColor.GOLD + " ⋅ ";

			if (arena.getLobby() != null)
			{
				description += "Lobby configured " + ChatColor.GRAY;
				description += "(" + SPUtils.userFriendlyLocation(arena.getLobby()) + ")";
				description += ChatColor.GOLD + ".";
			}
			else
			{
				description += "Lobby not configured.";
			}

			info(description);
		}

		info(ChatColor.GRAY + "The end.");
	}


	@SuppressWarnings ("ConstantConditions")
	private void lobby() throws CommandException
	{
		if (args.length == 1)
			throwInvalidArgument("Arena name missing.");

		final String arenaName = args[1].trim();
		final Arena arena = SpectatorPlus.get().getArenasManager().getArena(arenaName);

		if (arena == null)
			error("We can't found an arena with the name « " + arenaName + " ».");


		if (args.length == 2)
		{
			arena.setLobby(playerSender().getLocation());
			info("Arena " + arenaName + "'s lobby location set to your current location.");
		}
		else if (args[2].trim().equalsIgnoreCase("remove"))
		{
			arena.setLobby(null);
			info("Arena " + arenaName + "'s lobby was removed.");
		}
		else
		{
			throwInvalidArgument("unknown sub-command; « remove » or nothing required.");
		}
	}


	private void reset() throws CommandException
	{
		if (args.length == 1 || (args.length >= 2 && !args[1].trim().equalsIgnoreCase("confirm")))
		{
			warning("You are about to remove all the arenas. " + ChatColor.BOLD + "This cannot be undone.");
			warning("To confirm, please execute " + ChatColor.DARK_RED + "/spec arena reset confirm");
		}
		else
		{
			SpectatorPlus.get().getArenasManager().reset();
			info("All arenas were successfully destroyed.");
		}
	}


	private void corner() throws CommandException
	{
		ArenaSetup setup = SpectatorPlus.get().getPlayerData(playerSender()).getArenaSetup();

		if (setup != null)
			setup.next(playerSender().getLocation());
		else
			error("You are not in an arena set-up.");
	}



	@Override
	protected List<String> complete() throws CommandException
	{
		if (args.length == 1)
		{
			return getMatchingSubset(Arrays.asList("add", "remove", "list", "lobby", "reset", "corner"), args[0]);
		}

		else if (args.length == 2)
		{
			List<String> suggestions = null;

			switch (args[0].trim().toLowerCase())
			{
				case "remove":
				case "lobby":
					suggestions = new ArrayList<>();
					for (Arena arena : SpectatorPlus.get().getArenasManager().getArenas())
						suggestions.add(arena.getName());

					break;

				case "reset":
					suggestions = Collections.singletonList("confirm");
					break;
			}

			if (suggestions != null)
				return getMatchingSubset(suggestions, args[1]);
			else
				return null;
		}

		else if (args[0].trim().equalsIgnoreCase("lobby") && args.length == 3)
		{
			return getMatchingSubset(Collections.singletonList("remove"), args[2]);
		}

		else if (args[0].trim().equalsIgnoreCase("add") && (args.length == 7 || args.length == 9))
		{
			List<String> suggestions = new ArrayList<>();

			for (World world : Bukkit.getWorlds())
				suggestions.add(world.getName());

			return getMatchingSubset(suggestions, args[args.length - 1]);
		}

		else return null;
	}

	@Override
	public boolean canExecute(CommandSender sender)
	{
		return Permissions.MANAGE_ARENAS.grantedTo(sender);
	}
}
