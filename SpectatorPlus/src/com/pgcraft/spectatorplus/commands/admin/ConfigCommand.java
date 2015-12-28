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
import com.pgcraft.spectatorplus.Toggles;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.components.configuration.ConfigurationItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@CommandInfo(name = "config", usageParameters = "<path> [new value]")
public class ConfigCommand extends Command
{
	@Override
	@SuppressWarnings ({"ConstantConditions", "unchecked"})
	protected void run() throws CommandException
	{
		if (!Permissions.CHANGE_CONFIG.grantedTo(sender))
			throw new CommandException(this, CommandException.Reason.SENDER_NOT_AUTHORIZED);

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
}
