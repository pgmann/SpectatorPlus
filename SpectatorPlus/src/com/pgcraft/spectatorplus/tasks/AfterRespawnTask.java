package com.pgcraft.spectatorplus.tasks;

import com.pgcraft.spectatorplus.SpectatorPlusOld;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AfterRespawnTask extends BukkitRunnable {
	
	private SpectatorPlusOld p;
	private Player player;
	
	public AfterRespawnTask(Player player, SpectatorPlusOld p) {
		this.p = p;
		this.player = player;
	}
	
	@Override
	public void run() {
		p.enableSpectate(player, player);
		this.cancel();
	}
}
