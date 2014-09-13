package com.pgcraft.spectatorplus;

import java.util.Collection;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Scoreboard;

public class PlayerObject {	
	/**
	 * True if the player is currently spectating.
	 */
	protected boolean spectating;
	
	/**
	 * True if the player is currently teleporting.
	 */
	protected boolean teleporting;
	
	/**
	 * The ID of the current arena the spectator is in.
	 */
	protected UUID arena;
	
	/**
	 * The saved inventory of the player.
	 */
	protected ItemStack[] inventory;
	
	/**
	 * The saved armour of the player.
	 */
	protected ItemStack[] armour;
	
	/**
	 * The saved potion effects of the player.
	 */
	protected Collection<PotionEffect> effects;
	
	/**
	 * The setup step.
	 *  - 0: no setup in progress;
	 *  - 1: first corner set;
	 *  - 2: second corner set.
	 */
	protected int setup;
	
	/**
	 * If the player setup an arena, the entered name of this arena is stored here.
	 */
	protected String arenaName;
	
	/**
	 * The location of the first corner of an arena (in setup mode).
	 */
	protected Location pos1;
	
	/**
	 * The location of the second corner of an arena (in setup mode).
	 */
	protected Location pos2;
	
	/**
	 * The original scoreboard the player had set before spectate mode was enabled (to restore after)
	 */
	protected Scoreboard oldScoreboard;
	
	
	protected PlayerObject() {
		spectating = false;
		arenaName = null;
		teleporting = false;
		setup = 0;
	}
}
