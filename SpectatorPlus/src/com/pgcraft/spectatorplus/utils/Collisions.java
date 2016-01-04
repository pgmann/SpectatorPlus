/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.pgcraft.spectatorplus.utils;

import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.reflection.Reflection;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;


public final class Collisions
{
    private Collisions() {}

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
            // We need to call player.spigot().setCollidesWithEntities(collides).

            Object playerSpigotObject = Reflection.call(player.getClass(), player, "spigot");
            Reflection.call(playerSpigotObject, "setCollidesWithEntities", collides);

            return true;
        }
        catch (NoSuchMethodException ignored)
        {
            // Cannot enable/disable collisions :(
        }
        catch (IllegalAccessException | IllegalArgumentException | SecurityException e)
        {
            PluginLogger.error("Reflection exception caught while trying to change collisions status for " + player.getName(), e);
        }
        catch (InvocationTargetException e)
        {
            PluginLogger.error("Exception caught while trying to change collisions status for " + player.getName(), e.getCause());
        }

        return false;
    }

    /**
     * Checks whether the player collides with entities.
     *
     * @param player The player.
     *
     * @return true if the player collides with entities; false else or if the server is not
     * compatible (i.e. non-Spigot, currently).
     */
    public static boolean collidesWithEntities(Player player)
    {
        try
        {
            // We need to call player.spigot().getCollidesWithEntities().
            Object playerSpigotObject = Reflection.call(player.getClass(), player, "spigot");
            return (boolean) Reflection.call(playerSpigotObject, "getCollidesWithEntities");
        }
        catch (NoSuchMethodException ignored)
        {
            // Cannot check collisions :(
        }
        catch (IllegalAccessException | IllegalArgumentException | SecurityException e)
        {
            PluginLogger.error("Reflection exception caught while trying to check collisions status for " + player.getName(), e);
        }
        catch (InvocationTargetException e)
        {
            PluginLogger.error("Exception caught while trying to check collisions status for " + player.getName(), e.getCause());
        }

        return false;
    }
}
