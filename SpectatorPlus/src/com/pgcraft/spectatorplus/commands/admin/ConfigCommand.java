/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.pgcraft.spectatorplus.commands.admin;

import com.pgcraft.spectatorplus.Permissions;
import com.pgcraft.spectatorplus.Toggles;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.components.configuration.ConfigurationItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@CommandInfo(name = "config", usageParameters = "<path> [new value]")
public class ConfigCommand extends Command
{
	@Override
	@SuppressWarnings ({"unchecked"})
	protected void run() throws CommandException
	{
		if (args.length == 0)
			throwInvalidArgument("you have to provide a configuration path; use autocompletion if needed.");

		// Display
		else
		{
			ConfigurationItem<?> toggle = Toggles.getToggleFromPath(args[0]);
			if (toggle == null)
				error("There isn't any toggle at " + args[0]);

			// Display
			if (args.length == 1)
			{
				if (sender instanceof Player) sender.sendMessage("");
				sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Toggle " + toggle.getFieldName());
				sender.sendMessage(ChatColor.GOLD + "Value: " + ChatColor.RED + ChatColor.BOLD + toggle.get());
				sender.sendMessage(ChatColor.GOLD + "Default value: " + ChatColor.RED + toggle.getDefaultValue());
			}

			// Modification
			else if (args.length > 1)
			{
				// Materials
				if (toggle == Toggles.TOOLS_ARENA_SELECTOR_ITEM
						|| toggle == Toggles.TOOLS_INSPECTOR_ITEM
						|| toggle == Toggles.TOOLS_TELEPORTER_ITEM
						|| toggle == Toggles.TOOLS_TOOLS_ITEM)
				{
					if (Material.matchMaterial(args[1]) == null)
					{
						error("A valid material is required for this toggle.");
					}
					else
					{
						((ConfigurationItem<String>) toggle).set(args[1].toUpperCase());
					}
				}
				else if (toggle.getDefaultValue() instanceof String)
				{
					((ConfigurationItem<String>) toggle).set(args[1]);
				}
				else if (toggle.getDefaultValue() instanceof Boolean)
				{
					((ConfigurationItem<Boolean>) toggle).set(getBooleanParameter(1));
				}
				else if (toggle.getDefaultValue() instanceof Double)
				{
					((ConfigurationItem<Double>) toggle).set(getDoubleParameter(1));
				}
				else
				{
					error("Sorry, you cannot edit this kind of toggle from the game currently.");
				}

				success("Toggle " + args[0] + " successfully updated to " + toggle.get() + ".");
			}
		}
	}

	@Override
	protected List<String> complete() throws CommandException
	{
		if (args.length == 1)
		{
			List<String> paths = new ArrayList<>(Toggles.getPaths());
			Collections.sort(paths);
			return getMatchingSubset(paths, args[0]);
		}

		else if (args.length == 2)
		{
			ConfigurationItem<?> toggle = Toggles.getToggleFromPath(args[0]);
			if (toggle != null)
			{
				if (toggle == Toggles.TOOLS_ARENA_SELECTOR_ITEM
						|| toggle == Toggles.TOOLS_INSPECTOR_ITEM
						|| toggle == Toggles.TOOLS_TELEPORTER_ITEM
						|| toggle == Toggles.TOOLS_TOOLS_ITEM)
				{
					List<String> materialsNames = new ArrayList<>();
					for (Material material : Material.values())
					{
						materialsNames.add(material.name());
					}

					return getMatchingSubset(materialsNames, args[1]);
				}
				else if (toggle.getDefaultValue() instanceof Boolean)
				{
					return getMatchingSubset(Arrays.asList("yes", "no"), args[1]);
				}
			}
		}

		return null;
	}

	@Override
	public boolean canExecute(CommandSender sender)
	{
		return Permissions.CHANGE_CONFIG.grantedTo(sender);
	}
}
