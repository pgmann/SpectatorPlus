package com.pgcraft.spectatorplus;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class PlayerObject {
	public String arenaName;
	public boolean spectating;
	public boolean teleporting;
	public int setup;
	public int arenaNum;
	public Location pos1;
	public Location pos2;
	public ItemStack[] inventory;
	public ItemStack[] armour;
	PlayerObject () {
		spectating = false;
		arenaName = null;
		teleporting = false;
		setup = 0;
	}
}
