package com.pgcraft.spectatorplus.arenas;

import com.pgcraft.spectatorplus.SpectatorPlus;
import fr.zcraft.zlib.tools.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * Represents an Arena in SpectatorPlusOld. <p> An arena is a 3D space, represented by two opposed
 * corners; the spectators inside an arena can teleport themselves only to the players inside this
 * arena.
 *
 * @since 2.0
 */
public class Arena implements ConfigurationSerializable
{

	private UUID id = null;
	private String name = null;

	private Location corner1 = null;
	private Location corner2 = null;

	private Location lobby = null;

	private Boolean registered = false;

	private Boolean enabled = true;

	private String tempWorldName = "", tempLobbyWorldName = "";
	private Map<String, Object> tempSerialized;

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
	 * Constructs an object from the serialized version.<br> Error handling:<ul> <li>null world ->
	 * disable arena until fixed.</li> <li>null lobby world -> use arena world.</li> <li>can migrate
	 * old config if new values don't exist.</li> </ul>
	 *
	 * @param serialized The serialized version, returned by {@link #serialize()}.
	 */
	public Arena(Map<String, Object> serialized)
	{
		this.id = UUID.fromString((String) serialized.get("id"));
		this.name = (String) serialized.get("name");

		tempWorldName = (String) serialized.get("world");
		World world = Bukkit.getWorld(tempWorldName);

		if (world == null)
		{
			// Log to console, disable and return. Prevents loading arenas in a world where there isn't supposed to be an arena.
			PluginLogger.warning("Arena {0} is meant to be in a world called '{1}', but it couldn't be found. Disabling this arena.", name, tempWorldName);

			setEnabled(false);

			// Keep the old configuration, to make fixing easier.
			tempSerialized = serialized;

			return;
		}
		else
		{
			// If it's been fixed, make sure to reset tempSerialized to allow the arena to be saved in future.
			tempSerialized = null;
		}


		this.corner1 = ((Vector) serialized.get("corner1")).toLocation(world);
		this.corner2 = ((Vector) serialized.get("corner2")).toLocation(world);

		reequilibrateCorners();


		if (serialized.get("lobby.location") != null)
		{
			tempLobbyWorldName = (String) serialized.get("lobby.world");
			World worldLobby = Bukkit.getWorld(tempLobbyWorldName);

			if (worldLobby == null)
			{
				// Take an educated guess at where the lobby should be. In the arena's world is the best bet.
				PluginLogger.warning("Arena {0}'s lobby is meant to be in a world called '{1}', but it couldn't be found. Using the arena's world instead.", name, tempLobbyWorldName);
				worldLobby = world; // world cannot be null at this point or code would have exited.
			}

			// Get the coordinates of the lobby in the decided world
			this.lobby = ((Vector) serialized.get("lobby.location")).toLocation(worldLobby);
		}


		this.registered = (Boolean) serialized.get("registered");

		// Migration
		if (serialized.containsKey("enabled"))
		{
			this.enabled = (Boolean) serialized.get("enabled");
		}
		else
		{
			this.enabled = true;
		}
	}

	/**
	 * Returns a map representing this object. <p> Required by {@link org.bukkit.configuration.serialization.ConfigurationSerializable
	 * ConfigurationSerializable}.
	 *
	 * @return A representation of the object.
	 */
	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> serialized = new HashMap<>();

		if (tempSerialized != null)
		{
			// This is if the world is null - keep the exact same config for this arena as was loaded to ease fixing.
			serialized = tempSerialized;
			PluginLogger.warning("Remember to fix arena {0}'s world! No world called {1} exists!", name, tempWorldName);
			return serialized;
		}

		String worldName;
		if (corner1.getWorld() != null)
		{
			worldName = corner1.getWorld().getName();
		}
		else
		{
			// Just in case, to deal with worlds that have magically disappeared during runtime...
			worldName = tempWorldName;
			setEnabled(false);

			PluginLogger.warning("Remember to fix arena {0}'s world!", name);
		}

		serialized.put("id", id.toString());
		serialized.put("name", name);
		serialized.put("world", worldName);
		serialized.put("corner1", corner1.toVector());
		serialized.put("corner2", corner2.toVector());

		if (lobby != null)
		{
			// Deal with nonexistent lobby world, previously detected at load. Keep the old world name to make fixing easier.
			if (lobby.getWorld() != null && lobby.getWorld().getName().equals(tempLobbyWorldName))
			{
				worldName = lobby.getWorld().getName();
			}
			else
			{
				PluginLogger.warning("Remember to fix arena {0}'s lobby location! No world called {1} exists!", name, tempLobbyWorldName);
				worldName = tempLobbyWorldName;
			}

			serialized.put("lobby.location", lobby.toVector());
			serialized.put("lobby.world", worldName);
		}
		else
		{
			serialized.put("lobby.location", null);
			serialized.put("lobby.world", null);
		}

		serialized.put("registered", registered);
		serialized.put("enabled", enabled);

		return serialized;
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
