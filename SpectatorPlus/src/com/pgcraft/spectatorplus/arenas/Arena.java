/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.pgcraft.spectatorplus.arenas;

import com.pgcraft.spectatorplus.SpectatorPlus;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.UUID;


/**
 * Represents an Arena in SpectatorPlus.
 *
 * <p>An arena is a 3D space, represented by two opposed corners; the spectators inside an arena can
 * teleport themselves only to the players inside this arena.
 *
 * @since 2.0
 */
public class Arena
{
	private UUID id = null;
	private String name = null;

	private Location lowestCorner = null;
	private Location highestCorner = null;
	private Location lobby = null;

	private Boolean registered = false;
	private Boolean enabled = true;

	/**
	 * Standard constructor.
	 *
	 * <p>This constructor is <em>only</em> used to create a new arena.</p>
	 *
	 * @param name    The name of the arena.
	 * @param corner1 A corner of the arena.
	 * @param corner2 The other, opposite corner of the arena.
	 *
	 * @throws IllegalArgumentException if both corners are not in the same world.
	 */
	public Arena(String name, Location corner1, Location corner2)
	{
		this.id = UUID.randomUUID();
		this.name = name;

		setCorners(corner1, corner2);
	}

	/**
	 * From-config constructor.
	 *
	 * @param id         Arena UUID.
	 * @param name       Arena name.
	 * @param corner1    First corner.
	 * @param corner2    Other corner.
	 * @param lobby      Lobby.
	 * @param registered Registered?
	 * @param enabled    Enabled?
	 *
	 * @throws IllegalArgumentException if both corners are not in the same world.
	 */
	public Arena(UUID id, String name, Location corner1, Location corner2, Location lobby, boolean registered, boolean enabled)
	{
		Validate.notNull(id, "Arena UUID can't be null");
		Validate.notNull(name, "Arena name can't be null");
		Validate.notNull(corner1, "Arena corner 1 can't be null");
		Validate.notNull(corner2, "Arena corner 2 can't be null");

		this.id = id;
		this.name = name;
		this.lobby = lobby;
		this.registered = registered;
		this.enabled = enabled;

		setCorners(corner1, corner2);
	}

	/**
	 * Returns the name of the arena.
	 *
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the name of the arena.
	 *
	 * @param name the name to set.
	 */
	public void setName(String name)
	{
		this.name = name;

		if (isRegistered())
			SpectatorPlus.get().getArenasManager().save();
	}

	/**
	 * @return The location of the corner of this arena with the lowest coordinates.
	 */
	public Location getLowestCorner()
	{
		return lowestCorner;
	}

	/**
	 * @return The location of the corner of this arena with the highest coordinates.
	 */
	public Location getHighestCorner()
	{
		return highestCorner;
	}


	/**
	 * Sets the location of the corners of this arena.
	 *
	 * @param corner1 One of the corners.
	 * @param corner2 The other corner.
	 *
	 * @throws IllegalArgumentException if both corners are not in the same world.
	 */
	public void setCorners(Location corner1, Location corner2)
	{
		Validate.isTrue(corner1.getWorld().equals(corner2.getWorld()), "The two corners of an arena must be in the same world.");

		this.lowestCorner = corner1;
		this.highestCorner = corner2;
		computeSortedCorners();

		// The arena manager is null if this is called from his constructor, by the arenas loader.
		if (isRegistered() && SpectatorPlus.get().getArenasManager() != null)
			SpectatorPlus.get().getArenasManager().save();
	}


	/**
	 * Returns the {@link java.util.UUID UUID} of this arena.
	 *
	 * @return the UUID.
	 */
	public UUID getUUID()
	{
		return id;
	}

	/**
	 * Returns the location of the lobby, or null if there isn't any lobby set.
	 *
	 * @return the lobby
	 */
	public Location getLobby()
	{
		return lobby;
	}

	/**
	 * Sets the location of the lobby.
	 *
	 * @param lobby the location of the lobby. Null to delete the lobby.
	 */
	public void setLobby(Location lobby)
	{
		this.lobby = lobby;

		if (isRegistered())
			SpectatorPlus.get().getArenasManager().save();
	}

	/**
	 * Returns true if the arena is registered into the {@link ArenasManager}, and is available to
	 * the players.
	 *
	 * @return true if the arena is registered.
	 */
	public boolean isRegistered()
	{
		return registered;
	}

	/**
	 * Sets the registration state of this {@link Arena}.
	 *
	 * @param registered The status.
	 */
	protected void setRegistered(boolean registered)
	{
		this.registered = registered;
	}

	/**
	 * Returns true if this arena is enabled.
	 *
	 * @return True if enabled.
	 */
	public Boolean isEnabled()
	{
		return enabled;
	}

	/**
	 * Enables or disables this arena.
	 *
	 * @param enabled The status.
	 */
	public void setEnabled(Boolean enabled)
	{
		this.enabled = enabled;
	}


	/**
	 * Checks if the given location is inside this arena.
	 *
	 * @param location The location.
	 *
	 * @return {@code true} if inside.
	 */
	public boolean isInside(Location location)
	{
		return location != null
				&& location.getWorld().equals(lowestCorner.getWorld())
				&& location.getX() > lowestCorner.getX()
				&& location.getX() < highestCorner.getX()
				&& location.getY() > lowestCorner.getY()
				&& location.getY() < highestCorner.getY()
				&& location.getZ() > lowestCorner.getZ()
				&& location.getZ() < highestCorner.getZ();
	}


	@Override
	public int hashCode()
	{
		return this.getUUID().hashCode();
	}

	@Override
	public boolean equals(Object other)
	{
		return other != null && other instanceof Arena && ((Arena) other).getUUID().equals(this.getUUID());
	}


	/**
	 * Updates the corners so the first one is the lowest, and the second one the highest.
	 */
	private void computeSortedCorners()
	{
		World world = lowestCorner.getWorld();

		Vector firstCorner = lowestCorner.toVector();
		Vector secondCorner = highestCorner.toVector();

		lowestCorner = new Location(
				world,
				Math.min(firstCorner.getX(), secondCorner.getX()),
				Math.min(firstCorner.getY(), secondCorner.getY()),
				Math.min(firstCorner.getZ(), secondCorner.getZ())
		);

		highestCorner = new Location(
				world,
				Math.max(firstCorner.getX(), secondCorner.getX()),
				Math.max(firstCorner.getY(), secondCorner.getY()),
				Math.max(firstCorner.getZ(), secondCorner.getZ())
		);
	}


	/**
	 * @deprecated Use {@link #getLowestCorner()} instead.
	 */
	@Deprecated
	public Location getCorner1() { return lowestCorner; }

	/**
	 * @deprecated Use {@link #getHighestCorner()} instead.
	 */
	@Deprecated
	public Location getCorner2() { return highestCorner; }

	/**
	 * @deprecated Use {@link #setCorners(Location, Location)} instead.
	 */
	@Deprecated
	public void setCorner1(Location corner1) { throw new UnsupportedOperationException("Cannot set only one corner at a time."); }

	/**
	 * @deprecated Use {@link #setCorners(Location, Location)} instead.
	 */
	@Deprecated
	public void setCorner2(Location corner2) { throw new UnsupportedOperationException("Cannot set only one corner at a time."); }
}
