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
package com.pgcraft.spectatorplus.utils;

import fr.zcraft.zlib.tools.PluginLogger;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;


public class SPUtils
{
	public static String getName(CommandSender commandSender)
	{
		if (commandSender instanceof Player)
			return commandSender.getName();

		else if (commandSender instanceof CommandBlock)
			return "Command block '" + commandSender.getName() + "'";

		else
			return "Console";
	}

	/**
	 * Sets whether the player collides with entities.
	 *
	 * @param player   The player.
	 * @param collides Whether the player should collide with entities or not.
	 *
	 * @return true if the change was successful (compatible server, i.e. Spigot currently); false
	 * else.
	 */
	public static boolean setCollidesWithEntities(Player player, boolean collides)
	{
		try
		{
			// We need to call player.spigot.setCollidesWithEntities(collides) .
			Field playerSpigotField = player.getClass().getDeclaredField("spigot");
			playerSpigotField.setAccessible(true);

			Class<?> playerSpigotClazz = playerSpigotField.getType();
			Object playerSpigotObject = playerSpigotField.get(player);


			playerSpigotClazz.getDeclaredMethod("setCollidesWithEntities", boolean.class)
					.invoke(playerSpigotObject, collides);

			return true;
		}

		// Cannot enable/disable collisions :(
		catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException e)
		{
			PluginLogger.error("Reflection exception caught while trying to change collisions status for " + player.getName(), e);
			return false;
		}
		catch (InvocationTargetException e)
		{
			PluginLogger.error("Exception caught while trying to change collisions status for " + player.getName(), e.getCause());
			return false;
		}
	}
}
