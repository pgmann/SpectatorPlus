package com.pgcraft.spectatorplus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.Vector;

public class Arena implements ConfigurationSerializable {

	private UUID id = null;
	private String name = null;
	
	private Location corner1 = null;
	private Location corner2 = null;
	
	private Location lobby = null;
	
	private Boolean registered = false;
	
	/**
	 * Standard constructor.
	 * This constructor is <em>only</em> used to create a new arena.
	 * 
	 * @param name The name of the arena.
	 * @param corner1 A corner of the arena.
	 * @param corner2 The other corner of the arena.
	 */
	public Arena(String name, Location corner1, Location corner2) {
		
		this.id = UUID.randomUUID();
		this.name = name;
		
		this.corner1 = corner1;
		this.corner2 = corner2;
		
	}
	
	/**
	 * Constructs an object from the serialized version.
	 * 
	 * @param serialized The serialized version, returned by {@link #serialize()}.
	 */
	public Arena(Map<String,Object> serialized) {
		
		this.id = UUID.fromString((String) serialized.get("id"));
		this.name = (String) serialized.get("name");
		
		World world = Bukkit.getWorld((String) serialized.get("world"));
		this.corner1 = ((Vector) serialized.get("corner1")).toLocation(world);
		this.corner2 = ((Vector) serialized.get("corner2")).toLocation(world);
		
		if(serialized.get("lobby.vector") != null) {
			World worldLobby = Bukkit.getWorld((String) serialized.get("lobby.world"));
			this.lobby = ((Vector) serialized.get("lobby.vector")).toLocation(worldLobby);
		}
		
		this.registered = (Boolean) serialized.get("registered");
		
	}
	
	/**
	 * Returns a map representing this object.
	 * Required by ConfigurationSerializable.
	 * 
	 * @return A representation of the object.
	 */
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> serialized = new HashMap<String,Object>();
		
		serialized.put("id", id.toString());
		serialized.put("name", name);
		serialized.put("world", corner1.getWorld().getName());
		serialized.put("corner1", corner1.toVector());
		serialized.put("corner2", corner2.toVector());
		
		if(lobby != null) {
			serialized.put("lobby.vector", lobby.toVector());
			serialized.put("lobby.world", lobby.getWorld().getName());
		}
		else {
			serialized.put("lobby.vector", null);
			serialized.put("lobby.world", null);
		}
		
		serialized.put("registered", registered);
		
		return serialized;
	}

	/**
	 * Returns the name of the arena.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the arena.
	 * 
	 * @param name the name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the location of the first corner of this arena.
	 * 
	 * @return the location of the first corner of this arena.
	 */
	public Location getCorner1() {
		return corner1;
	}

	/**
	 * Sets the location of the first corner of this arena.
	 * 
	 * @param corner1 the corner1 to set
	 */
	public void setCorner1(Location corner1) {
		this.corner1 = corner1;
	}

	/**
	 * Returns the location of the second corner of this arena.
	 * 
	 * @return the location of the second corner of this arena.
	 */
	public Location getCorner2() {
		return corner2;
	}

	/**
	 * Sets the location of the second corner of this arena.
	 * 
	 * @param corner2 the corner2 to set
	 */
	public void setCorner2(Location corner2) {
		this.corner2 = corner2;
	}

	/**
	 * Returns the UUID of this arena.
	 * 
	 * @return the UUID.
	 */
	public UUID getUUID() {
		return id;
	}

	/**
	 * Returns the location of the lobby.
	 * 
	 * @return the lobby
	 */
	public Location getLobby() {
		return lobby;
	}
	
	/**
	 * Sets the location of the lobby.
	 * 
	 * @param lobby the location of the lobby.
	 */
	public void setLobby(Location lobby) {
		this.lobby = lobby;
	}
	
	/**
	 * Returns true if the arena is registered into the ArenasManager, and is available to the players.
	 * 
	 * @return true if the arena is registered.
	 */
	public boolean isRegistered() {
		return registered;
	}
	
	/**
	 * Sets the registration state of this Arena.
	 * 
	 * @param registered
	 */
	protected void setRegistered(boolean registered) {
		this.registered = registered;
	}
}
