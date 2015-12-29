/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.pgcraft.spectatorplus.utils;

import fr.zcraft.zlib.tools.PluginLogger;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;


public class SPUtils
{
	public static String getName(CommandSender commandSender)
	{
		if (commandSender instanceof Player)
			return ((Player) commandSender).getDisplayName();

		else if (commandSender instanceof CommandBlock)
			return "Command block '" + commandSender.getName() + "'";

		else
			return "Console";
	}

	public static String getEffectName(PotionEffectType type)
	{
		if (type.equals(PotionEffectType.CONFUSION))
			return "Nausea";

		else if (type.equals(PotionEffectType.FAST_DIGGING))
			return "Haste";

		else if (type.equals(PotionEffectType.SLOW_DIGGING))
			return "Mining Fatigue";

		else if (type.equals(PotionEffectType.INCREASE_DAMAGE))
			return "Strength";

		else if (type.equals(PotionEffectType.HEAL))
			return "Instant Health";

		else if (type.equals(PotionEffectType.INCREASE_DAMAGE))
			return "Instant Damage";

		else if (type.equals(PotionEffectType.JUMP))
			return "Jump Boost";

		else
			return WordUtils.capitalizeFully(type.getName().replace("_", " "));
	}

	public static String userFriendlyLocation(Location location)
	{
		return location.getWorld().getName() + " @ " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
	}

	public static Player getPlayer(String playerName)
	{
		for (Player player : Bukkit.getOnlinePlayers())
			if (player.getName().equals(playerName))
				return player;

		return null;
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
