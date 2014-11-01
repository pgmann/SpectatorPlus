package com.pgcraft.spectatorplus;

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
					if (arena != null) {
						if (target.getLocation().getX() >= arena.getCorner1().getX() && target.getLocation().getX() <= arena.getCorner2().getX()) {
							if (target.getLocation().getY() >= arena.getCorner1().getY() && target.getLocation().getY() <= arena.getCorner2().getY()) {
								if (target.getLocation().getZ() >= arena.getCorner1().getZ() && target.getLocation().getZ() <= arena.getCorner2().getZ()) {
									outOfBounds = false;
								}
							}
						}
					} else {
						// No arena, no movement.
						p.spawnPlayer(target);
						p.console.sendMessage("Returning to spawn...");
						return;
					}
					if (outOfBounds) {
						p.getServer().getLogger().info(target.getName() + " is out of bounds.");
					}
				}
			}
		}
	}

}
