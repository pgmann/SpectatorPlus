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
 * <p>An arena is a 3D space, represented by two opposed
 * corners; the spectators inside an arena can teleport themselves only to the players inside this
 * arena.
 *
 * @since 2.0
 */
public class Arena
{
	private UUID id = null;
	private String name = null;

	private Location corner1 = null;
	private Location corner2 = null;
	private Location lobby = null;

	private Boolean registered = false;
	private Boolean enabled = true;

	/**
	 * Standard constructor.<br> This constructor is <em>only</em> used to create a new arena.
	 *
	 * @param name    The name of the arena.
	 * @param corner1 A corner of the arena.
	 * @param corner2 The other, opposite corner of the arena.
	 */
	public Arena(String name, Location corner1, Location corner2)
	{
		this.id = UUID.randomUUID();
		this.name = name;

		this.corner1 = corner1;
		this.corner2 = corner2;

		reequilibrateCorners();
	}

	/**
	 * From-config constructor.
	 *
	 * @param id Arena UUID.
	 * @param name Arena name.
	 * @param corner1 First corner.
	 * @param corner2 Other corner.
	 * @param lobby Lobby.
	 * @param registered Registered?
	 * @param enabled Enabled?
	 */
	public Arena(UUID id, String name, Location corner1, Location corner2, Location lobby, boolean registered, boolean enabled)
	{
		Validate.notNull(id, "Arena UUID can't be null");
		Validate.notNull(name, "Arena name can't be null");
		Validate.notNull(corner1, "Arena corner 1 can't be null");
		Validate.notNull(corner2, "Arena corner 2 can't be null");

		this.id = id;
		this.name = name;
		this.corner1 = corner1;
		this.corner2 = corner2;
		this.lobby = lobby;
		this.registered = registered;
		this.enabled = enabled;

		reequilibrateCorners();
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

		if(isRegistered())
			SpectatorPlus.get().getArenasManager().save();
	}

	/**
	 * Returns the location of the first corner of this arena. This will always be the corner with
	 * the lowest coordinates, not the object passed to a previous call of {@link #setCorner1(Location)}.
	 *
	 * @return the location of the first corner of this arena.
	 */
	public Location getCorner1()
	{
		return corner1;
	}

	/**
	 * Sets the location of the first corner of this arena.
	 *
	 * @param corner1 The first corner to set.
	 */
	public void setCorner1(Location corner1)
	{
		this.corner1 = corner1;
		reequilibrateCorners();

		if(isRegistered())
			SpectatorPlus.get().getArenasManager().save();
	}

	/**
	 * Returns the location of the second corner of this arena. This will always be the corner with
	 * the highest coordinates, not the object passed to a previous call of {@link #setCorner2(Location)}.
	 *
	 * @return the location of the second corner of this arena.
	 */
	public Location getCorner2()
	{
		return corner2;
	}

	/**
	 * Sets the location of the second corner of this arena.
	 *
	 * @param corner2 The second corner to set.
	 */
	public void setCorner2(Location corner2)
	{
		this.corner2 = corner2;
		reequilibrateCorners();

		if(isRegistered())
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

		if(isRegistered())
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
	 * @param registered
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
	 * @return {@code true} if inside.
	 */
	public boolean isInside(Location location)
	{
		return location != null
				&& location.getWorld().equals(corner1.getWorld())
				&& location.getX() > corner1.getX()
				&& location.getX() < corner2.getX()
				&& location.getY() > corner1.getY()
				&& location.getY() < corner2.getY()
				&& location.getZ() > corner1.getZ()
				&& location.getZ() < corner2.getZ();
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
	private void reequilibrateCorners()
	{
		World world = corner1.getWorld();

		Vector firstCorner  = corner1.toVector();
		Vector secondCorner = corner2.toVector();

		corner1 = new Location(
				world,
				Math.min(firstCorner.getX(), secondCorner.getX()),
				Math.min(firstCorner.getY(), secondCorner.getY()),
				Math.min(firstCorner.getZ(), secondCorner.getZ())
		);

		corner2 = new Location(
				world,
				Math.max(firstCorner.getX(), secondCorner.getX()),
				Math.max(firstCorner.getY(), secondCorner.getY()),
				Math.max(firstCorner.getZ(), secondCorner.getZ())
		);
	}
}
