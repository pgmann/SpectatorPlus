package com.pgcraft.spectatorplus;

import com.pgcraft.spectatorplus.arenas.Arena;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Handle players trying to leave the arena in arena mode.<br>
 * Players outside the arena borders will have the move event cancelled;<br>
 * Players more than 5 blocks away from the global lobby will also have their move event cancelled.
 * 
 * @param event
 */
public class SpectatorManagerTask extends BukkitRunnable {
	SpectatorPlus p;
	
	protected SpectatorManagerTask(SpectatorPlus p) {
		this.p = p;
	}
	
	@Override
	public void run() {
		
		for (Player target:p.getServer().getOnlinePlayers()) {
			if (p.getPlayerData(target).spectating) {
				// Spectators should always be able to fly.
				// to help prevent glitches when enabling/disabling flight, only set allow flight when it's not already on.
				if (!target.getAllowFlight()) target.setAllowFlight(true);
				
				// In arena mode, if boundaries are enforced, check if spectators are not inside the boundary.
				// [Spawn allows free movement (before choosing an arena).]
				if (p.mode.equals(SpectatorMode.ARENA) && p.enforceArenaBoundary) {
					boolean outOfBounds = true;
					
					Arena arena = p.arenasManager.getArena(p.getPlayerData(target).arena);
					if (arena != null) { // ignore players not in an arena.
						if(!arena.isEnabled() || !arena.isRegistered() || arena.getCorner1() == null || arena.getCorner2() == null) {
							p.getPlayerData(target).arena=null;
							target.sendMessage(SpectatorPlus.prefix+"The arena you were in was removed.");
							p.spawnPlayer(target);
							outOfBounds = false;
						} else if (isValidPos(target, arena)) {
							outOfBounds = false;
						}
					} else {
						outOfBounds = false;
					}
					
					if (outOfBounds) {
						if (p.output) target.sendMessage(SpectatorPlus.prefix + "Stay inside the arena!"); 
						target.teleport(getValidPos(target.getLocation(), arena));
					}
				}
			}
		}
	}

	boolean isValidPos(Player target, Arena arena) {
		Location loc1 = getLargestCorner(arena.getCorner1(), arena.getCorner2());
		Location loc2 = getSmallestCorner(arena.getCorner1(), arena.getCorner2());
		
		if (target.getLocation().getX() <= loc1.getX() && target.getLocation().getX() >= loc2.getX()) {
			if (target.getLocation().getY() <= loc1.getY() && target.getLocation().getY() >= loc2.getY()) {
				if (target.getLocation().getZ() <= loc1.getZ() && target.getLocation().getZ() >= loc2.getZ()) {
					return true;
				}
			}
		}
		
		return false;
	}
	Location getValidPos(Location target, Arena arena) {
		Location safePos = new Location(arena.getCorner1().getWorld(), target.getX(),target.getY(),target.getZ());
		Location loc1 = getLargestCorner(arena.getCorner1(), arena.getCorner2());
		Location loc2 = getSmallestCorner(arena.getCorner1(), arena.getCorner2());
		
		if (target.getX() > loc1.getX()) safePos.setX(loc1.getX()-0.01);
		if (target.getX() < loc2.getX()) safePos.setX(loc2.getX()+0.01);
		
		if (target.getY() > loc1.getY()) safePos.setY(loc1.getY()-0.01);
		if (target.getY() < loc2.getY()) safePos.setY(loc2.getY()+0.01);
		
		if (target.getZ() > loc1.getZ()) safePos.setZ(loc1.getZ()-0.01);
		if (target.getZ() < loc2.getZ()) safePos.setZ(loc2.getZ()+0.01);
		
		// Must have the same pitch and yaw to possibly equal the player's current position. Also, looks better.
		safePos.setDirection(target.getDirection());
		
		return safePos;
		
	}
	
	Location getSafePos(Player target) {
		Location where = target.getLocation();
		Location aboveWhere = target.getLocation().add(0,1,0);
		Location belowWhere = target.getLocation().subtract(0,1,0);
		if (where.getBlock().getType() != Material.AIR || aboveWhere.getBlock().getType() != Material.AIR || (belowWhere.getBlock().getType() == Material.AIR || belowWhere.getBlock().getType() == Material.LAVA || belowWhere.getBlock().getType() == Material.WATER)) {
			while (where.getBlock().getType() != Material.AIR || aboveWhere.getBlock().getType() != Material.AIR || (belowWhere.getBlock().getType() == Material.AIR || belowWhere.getBlock().getType() == Material.LAVA || belowWhere.getBlock().getType() == Material.WATER)) {
				where.setY(where.getY()+1);
				aboveWhere.setY(aboveWhere.getY()+1);
				belowWhere.setY(belowWhere.getY()+1);
				if (where.getY() > target.getLocation().getWorld().getHighestBlockYAt(where)) {
					where.setY(where.getY()-2);
					aboveWhere.setY(aboveWhere.getY()-2);
					belowWhere.setY(belowWhere.getY()-2);
				}
			}
		}
		return where;
	}
	
	Location getLargestCorner(Location loc1, Location loc2) {
		if (!loc1.getWorld().equals(loc2.getWorld())) {
			return null;
		}
		Location largest = new Location(loc1.getWorld(), loc1.getX(), loc1.getY(), loc1.getZ());
		if(loc1.getX() < loc2.getX()) largest.setX(loc2.getX());
		if(loc1.getY() < loc2.getY()) largest.setY(loc2.getY());
		if(loc1.getZ() < loc2.getZ()) largest.setZ(loc2.getZ());
		return largest;
	}
	Location getSmallestCorner(Location loc1, Location loc2) {
		if (!loc1.getWorld().equals(loc2.getWorld())) {
			return null;
		}
		Location smallest = new Location(loc1.getWorld(), loc1.getX(), loc1.getY(), loc1.getZ());
		if(loc1.getX() > loc2.getX()) smallest.setX(loc2.getX());
		if(loc1.getY() > loc2.getY()) smallest.setY(loc2.getY());
		if(loc1.getZ() > loc2.getZ()) smallest.setZ(loc2.getZ());
		return smallest;
	}
}
