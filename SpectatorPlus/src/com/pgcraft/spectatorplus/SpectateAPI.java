/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.pgcraft.spectatorplus;

import com.pgcraft.spectatorplus.arenas.Arena;
import com.pgcraft.spectatorplus.arenas.ArenasManager;
import com.pgcraft.spectatorplus.spectators.Spectator;
import com.pgcraft.spectatorplus.spectators.SpectatorMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;


/**
 * This is the API of SpectatorPlus.
 *
 * <p> Use the methods of this class to manage spectators, arenas and everything else.</p>
 */
public class SpectateAPI
{
	private static SpectateAPI instance;

	private SpectatorPlus p;

	protected SpectateAPI()
	{
		this.p = SpectatorPlus.get();
		instance = this;
	}


	/**
	 * Returns the representation of this player for Spectator Plus, with
	 * methods to manage spectating-related things around this player.
	 *
	 * The object is created on-the-fly if needed: this never returns {@code null}.
	 *
	 * @param id The player's UUID.
	 * @return The {@link Spectator} object.
	 *
	 * @see Spectator
	 */
	public Spectator getSpectator(UUID id)
	{
		return p.getPlayerData(id);
	}

	/**
	 * Returns the representation of this player for Spectator Plus, with
	 * methods to manage spectating-related things around this player.
	 *
	 * The object is created on-the-fly if needed: this never returns {@code null}.
	 *
	 * @param player The player.
	 * @return The {@link Spectator} object.
	 *
	 * @see Spectator
	 */
	public Spectator getSpectator(Player player)
	{
		return p.getPlayerData(player);
	}


	/**
	 * Checks if a player is currently spectating.
	 *
	 * @param player The player to check.
	 *
	 * @return <b>true</b> if the player is spectating.
	 * @since 1.9.2
	 */
	public boolean isSpectator(Player player)
	{
		return p.getPlayerData(player).isSpectating();
	}

	/**
	 * Enables or disables the spectator mode for a player.<br> Since this method provides no
	 * <i>sender</i>, the console is used by default.<br> Consider using {@link
	 * #setSpectating(Player player, boolean spectating, boolean silent)} instead to avoid
	 * outputting messages.
	 *
	 * @param player     The player.
	 * @param spectating True if the spectator mode needs to be enabled.
	 *
	 * @since 1.9.2
	 */
	public void setSpectating(Player player, boolean spectating)
	{
		p.getPlayerData(player).setSpectating(spectating);
	}

	/**
	 * Enables or disables the spectator mode for a player.<br> Since this method provides no
	 * <i>sender</i>, the console is used by default.
	 *
	 * @param player     The player.
	 * @param spectating True if the spectator mode needs to be enabled.
	 * @param silent     If true, will not output any messages - useful when using the API or
	 *                   command blocks.
	 *
	 * @since 2.0
	 */
	public void setSpectating(Player player, boolean spectating, boolean silent)
	{
		p.getPlayerData(player).setSpectating(spectating, silent);
	}

	/**
	 * Enables or disables the spectator mode for a player.<br> Consider using {@link
	 * #setSpectating(Player player, boolean spectating, CommandSender sender, boolean silent)}
	 * instead to avoid outputting messages.
	 *
	 * @param player  The player.
	 * @param spectating True if the spectator mode needs to be enabled.
	 * @param sender     The player (or the console) who enabled spectate mode
	 *
	 * @since 1.9.2
	 */
	public void setSpectating(Player player, boolean spectating, CommandSender sender)
	{
		p.getPlayerData(player).setSpectating(spectating, sender);
	}

	/**
	 * Enables or disables the spectator mode for a player.
	 *
	 * @param player  The player.
	 * @param spectating True if the spectator mode needs to be enabled.
	 * @param sender     The player (or the console) who enabled spectate mode
	 * @param silent     If true, will not output any messages - useful when using the API or
	 *                   command blocks.
	 *
	 * @since 2.0
	 */
	public void setSpectating(Player player, boolean spectating, CommandSender sender, boolean silent)
	{
		p.getPlayerData(player).setSpectating(spectating, sender, silent);
	}

	/**
	 * Teleports a spectator to a player.<br> The teleportation fails if spectator is not spectating
	 * or if target is currently spectating.
	 *
	 * @param spectator The spectator to be teleported.
	 * @param target    The target.
	 *
	 * @return True if the spectator was successfully teleported - <i>spectator</i> was spectating,
	 * and <i>target</i> was not.
	 * @since 2.0
	 */
	public boolean teleportSpectatorToPlayer(Player spectator, Player target)
	{
		if (p.getPlayerData(spectator).isSpectating() && !p.getPlayerData(target).isSpectating())
		{
			spectator.teleport(target);
			return true;
		}

		else return false;
	}

	/**
	 * Sets the current SpectatorPlus mode.
	 *
	 * <ul>
	 *     <li>
	 *         {@code ANY}: the spectators can teleports themselves to any player in the server.
	 *     </li>
	 *     <li>
	 *         {@code ARENA}: the spectators will have to choose an arena; then they will be able
	 *         to teleport themselves only to the players in this arena. An option is available
	 *         to prevent the spectators from leaving the arena.
	 *     </li>
	 *     <li>
	 *         {@code WORLD}: the spectators will be able to teleport themselves to the players in
	 *         the same world.
	 *     </li>
	 * </ul>
	 *
	 * @param mode The mode.
	 *
	 * @see SpectatorMode
	 * @since 2.0
	 */
	public void setSpectatorPlusMode(SpectatorMode mode)
	{
		p.getSpectatorsManager().setSpectatorsMode(mode);
	}

	/**
	 * Sets a toggle.
	 *
	 * DEPRECATED — does nothing.
	 *
	 * @param toggle The toggle to set.
	 * @param value  The new value.
	 * @param temp   If true this change will not be saved in the config file.
	 *
	 * @throws NullPointerException     if the toggle is null.
	 * @throws IllegalArgumentException if the type of the toggle is not compatible with the type of
	 *                                  the value.
	 * @since 2.1
	 * @deprecated Use {@link Toggles} directly. This method does nothing.
	 * @see Toggles
	 */
	@Deprecated
	public void setConfig(Object toggle, Object value, boolean temp) {}

	/**
	 * Returns the {@link ArenasManager Arenas manager}. Use this to manage the arenas.
	 *
	 * @return The {@link ArenasManager}.
	 * @since 2.0
	 */
	public ArenasManager getArenasManager()
	{
		return p.getArenasManager();
	}

	/**
	 * Sets the arena for the given player.<br> Teleports the player to the lobby of that arena, if
	 * a lobby is available.
	 *
	 * @param player          The player.
	 * @param arena           The arena.
	 * @param teleportToLobby If true the player will be teleported to the lobby (if a lobby is
	 *                        set).
	 *
	 * @return True if the change was effective (i.e. the arena exists).
	 * @since 2.0
	 *
	 * @deprecated Use {@link Spectator#setArena(Arena)}.
	 */
	@Deprecated
	public boolean setArenaForPlayer(Player player, Arena arena, boolean teleportToLobby)
	{
		if (arena.isRegistered())
		{
			p.getPlayerData(player).setArena(arena);
			return true;
		}

		else return false;
	}

	/**
	 * Sets the arena for the given player.<br> Teleports the player to the lobby of that arena, if
	 * a lobby is available.
	 *
	 * @param player The player.
	 * @param arena  The arena.
	 *
	 * @return True if the change was effective (i.e. the arena exists).
	 * @since 2.0
	 *
	 * @deprecated Use {@link Spectator#setArena(Arena)}.
	 */
	@Deprecated
	public boolean setArenaForPlayer(Player player, Arena arena)
	{
		return setArenaForPlayer(player, arena, true);
	}

	/**
	 * Removes a player from his arena.<br> The player is teleported to the main lobby, if such a
	 * lobby is set.
	 *
	 * @param player The player to be removed from his arena.
	 *
	 * @since 2.0
	 * @deprecated Use {@link Spectator#setArena(Arena)} and pass {@code null} as argument.
	 */
	@Deprecated
	public void removePlayerFromArena(Player player)
	{
		p.getPlayerData(player).setArena(null);
	}


	/**
	 * Broadcasts a message to all players with spectator mode enabled, and the sender.
	 *
	 * @param sender  The sender of the message to be broadcasted.
	 * @param message The message to broadcast.
	 *
	 * @since 2.0
	 */
	public void broadcastToSpectators(CommandSender sender, String message)
	{
		p.getSpectatorsManager().getChatManager().broadcastToSpectators(sender, message);
	}


	/**
	 * Sends a spectator chat message, from one spectator to all other spectators.<br> Includes
	 * "/me" actions
	 *
	 * @param sender   The sender of the message.
	 * @param message  The text of the message.
	 * @param isAction If true, the message will be displayed as an action message (like /me
	 *                 <message>).
	 *
	 * @since 2.0
	 */
	public void sendSpectatorMessage(CommandSender sender, String message, Boolean isAction)
	{
		p.getSpectatorsManager().getChatManager().sendSpectatorsChatMessage(sender, message, isAction);
	}

	/**
	 * Used to make access to the API much easier.
	 *
	 * {@code SpectateAPI.getAPI()} can now be used to get a reference to the running instance.
	 *
	 * @return the running instance of {@link SpectateAPI}.
	 */
	public static SpectateAPI getAPI()
	{
		return instance;
	}
}
