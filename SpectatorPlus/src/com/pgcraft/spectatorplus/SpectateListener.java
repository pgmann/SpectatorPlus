package com.pgcraft.spectatorplus;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;

@SuppressWarnings("deprecation")
public class SpectateListener implements Listener {
	
	private SpectatorPlusOld p; // Pointer to main class (see SpectatorPlusOld.java)

	protected SpectateListener(SpectatorPlusOld p) {
		this.p = p;
	}

	/**
	 * Used to setup an arena, if the command was sent before by this player.
	 * 
	 * @param e
	 */
	@EventHandler
	protected void onBlockBreak(BlockBreakEvent e) {
		// Set up mode
		if(p.arenaSetup(e.getPlayer(), e.getBlock())) {
			e.setCancelled(true);
		}
	}

	
	/**
	 * Used to:<br>
	 *  - setup an arena (if the command was sent before by the sender).
	 * 
	 * @param e
	 */
	@EventHandler
	protected void onBlockDamage(BlockDamageEvent e) {
		// Set up mode
		if (p.arenaSetup(e.getPlayer(), e.getBlock())) {
			e.setCancelled(true);
		}
	}
}
