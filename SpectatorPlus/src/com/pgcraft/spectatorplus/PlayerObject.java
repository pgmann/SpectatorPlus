package com.pgcraft.spectatorplus;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class PlayerObject {	
	/**
	 * True if the player is currently spectating.
	 */
	public boolean spectating;
	
	/**
	 * True if the player is currently teleporting.
	 */
	public boolean teleporting;
	
	/**
	 * The UUID of the current arena the spectator is in.
	 */
	public UUID arena;
	
	/**
	 * The saved inventory of the player.
	 */
	public ItemStack[] inventory;
	
	/**
	 * The saved armour of the player.
	 */
	public ItemStack[] armour;
	
	/**
	 * The setup step.
	 *  - 0: no setup in progress;
	 *  - 1: first corner set;
	 *  - 2: second corner set.
	 */
	public int setup;
	
	/**
	 * If the player setup an arena, the entered name of this arena is stored here.
	 */
	public String arenaName;
	
	/**
	 * The location of the first corner of an arena (in setup mode).
	 */
	public Location pos1;
	
	/**
	 * The location of the second corner of an arena (in setup mode).
	 */
	public Location pos2;
	
	
	public PlayerObject() {
		spectating = false;
		arenaName = null;
		teleporting = false;
		setup = 0;
	}
}
