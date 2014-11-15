package com.pgcraft.spectatorplus;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AfterRespawnTask extends BukkitRunnable {
	
	private SpectatorPlus p;
	private Player player;
	
	public AfterRespawnTask(Player player, SpectatorPlus p) {
		this.p = p;
		this.player = player;
	}
	
	@Override
	public void run() {
		p.enableSpectate(player, player);
		this.cancel();
	}
}
