package com.pgcraft.spectatorplus;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Handle players trying to leave the arena in arena mode.<br>
 * Players outside the arena borders will have the move event cancelled;<br>
 * Players more than 5 blocks away from the global lobby will also have their move event cancelled.
 * 
 * @param event
 */
@SuppressWarnings("deprecation")
public class SpectatorManagerTask extends BukkitRunnable {
	SpectatorPlus p;
	
	protected SpectatorManagerTask(SpectatorPlus p) {
		this.p = p;
	}
	
	@Override
	public void run() {
		boolean enforceBoundary = true;
		if (p.setup.getConfig().getString("mode").equals("arena") && enforceBoundary) {
			for (Player target:p.getServer().getOnlinePlayers()) {
				if (p.getPlayerData(target).spectating) {
					// Check if the spectator is not in any arenas/the global lobby
					boolean outOfBounds = true;
					Arena arena = p.arenasManager.getArena(p.getPlayerData(target).arena);
					// [Spawn allows free movement (before choosing an arena).]
					if (arena != null) {
						Location pos1 = arena.getCorner1();
						Location pos2 = arena.getCorner2();
						if (pos1.getX() > pos2.getX()) {
							Location tempPos = new Location(pos1.getWorld(), pos1.getX(),pos1.getY(),pos1.getZ()); // pos1 temp
							pos1 = new Location(pos2.getWorld(), pos2.getX(),pos2.getY(),pos2.getZ());
							pos2 = new Location(tempPos.getWorld(), tempPos.getX(),tempPos.getY(),tempPos.getZ());
						}
						
						if (target.getLocation().getX() >= pos1.getX() && target.getLocation().getX() <= pos2.getX()) {
							if (target.getLocation().getY() >= pos1.getY() && target.getLocation().getY() <= pos2.getY()) {
								if (target.getLocation().getZ() >= pos1.getZ() && target.getLocation().getZ() <= pos2.getZ()) {
									outOfBounds = false;
								}
							}
						}
					} else {outOfBounds = false;}
					if (outOfBounds) {
						p.getServer().getLogger().info(target.getName() + " is out of bounds.");
					}
				}
			}
		}
	}

}
