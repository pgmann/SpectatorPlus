package com.pgcraft.spectatorplus;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AfterRespawnTask extends BukkitRunnable {
	
	SpectatorPlus plugin;
	Player player;
	
	public AfterRespawnTask(Player player, SpectatorPlus plugin) {
		this.plugin = plugin;
		this.player = player;
	}
	
	@Override
	public void run() {
		plugin.enableSpectate(player, player);
		this.cancel();
	}
}
