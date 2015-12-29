/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.pgcraft.spectatorplus.spectators;

/**
 * Represents the current teleportation mode of Spectator Plus.
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
 * @since 2.0
 */
public enum SpectatorMode
{
	ANY("Spectators can teleport themselves to any non-spectating player in the server."),
	ARENA("Spectators needs to select an arena; then, they can only teleport themselves to the players inside the boundaries of this arena. An option is available to prevent the spectators from leaving the arena."),
	WORLD("Spectators can teleport themselves to the players in the same world only.");


	private String description;

	SpectatorMode(String description)
	{
		this.description = description;
	}

	public String getDescription()
	{
		return description;
	}


	/**
	 * Returns the mode with the specified name.
	 *
	 * @param mode The mode.
	 *
	 * @return {@link SpectatorMode}.
	 * @throws IllegalArgumentException if there isn't any mode with this name.
	 * @throws NullPointerException     if mode is null.
	 * @since 2.0
	 */
	static public SpectatorMode fromString(String mode)
	{
		return SpectatorMode.valueOf(mode.toUpperCase().trim());
	}
}
