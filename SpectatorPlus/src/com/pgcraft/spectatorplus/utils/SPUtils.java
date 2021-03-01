/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.pgcraft.spectatorplus.utils;

import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public final class SPUtils {
    private SPUtils() {
    }

    /**
     * @param commandSender A command sender.
     * @return A name for this sender: the player name, or "Command block 'name'", or "Console".
     */
    public static String getName(CommandSender commandSender) {
        if (commandSender instanceof Player)
            return ((Player) commandSender).getDisplayName();

        else if (commandSender instanceof CommandBlock)
            return "Command block '" + commandSender.getName() + "'";

        else
            return "Console";
    }

    /**
     * Returns the user-friendly name of the given effect.
     *
     * <p>As example, "SLOW_DIGGING" becomes "Mining Fatigue".</p>
     *
     * @param type The potion effect type.
     * @return An user-friendly name.
     */
    public static String getEffectName(PotionEffectType type) {
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

        else if (type.equals(PotionEffectType.HARM))
            return "Instant Damage";

        else if (type.equals(PotionEffectType.JUMP))
            return "Jump Boost";

        else
            return WordUtils.capitalizeFully(type.getName().replace("_", " "));
    }

    /**
     * Converts a location into an user-friendly description: "world @ x,y,z".
     *
     * @param location The location to display.
     * @return An user-friendly representation of the location.
     */
    public static String userFriendlyLocation(Location location) {
        return location.getWorld().getName() + " @ " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    /**
     * Returns a {@link Player} object from his player name.
     *
     * @param playerName The player name.
     * @return The {@link Player} object.
     */
    public static Player getPlayer(String playerName) {
        for (Player player : Bukkit.getOnlinePlayers())
            if (player.getName().equals(playerName))
                return player;

        return null;
    }

    /**
     * Returns whether a {@link Material} is a type of regular door.
     *
     * @param material The {@link Material}.
     * @return Whether it's a regular door.
     */
    public static boolean isRegularDoor(Material material) {
        // an example material type is included here to help catch any future changes to these enums
        return material.data == Door.class;
    }

    /**
     * Returns whether a {@link Material} is a type of trapdoor.
     *
     * @param material The {@link Material}.
     * @return Whether it's a trapdoor.
     */
    public static boolean isTrapdoor(Material material) {
        return material.data == TrapDoor.class;
    }

    /**
     * Returns whether a {@link Material} is a type of fence gate.
     *
     * @param material The {@link Material}.
     * @return Whether it's a fence gate.
     */
    public static boolean isFenceGate(Material material) {
        return material.data == Gate.class;
    }
}
