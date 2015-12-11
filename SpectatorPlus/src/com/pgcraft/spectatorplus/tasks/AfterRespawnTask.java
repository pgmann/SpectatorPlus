package com.pgcraft.spectatorplus.tasks;

import com.pgcraft.spectatorplus.SpectatorPlus;
import org.bukkit.entity.Player;

public class AfterRespawnTask implements Runnable
{
	private Player player;
	
	public AfterRespawnTask(Player player)
	{
		this.player = player;
	}
	
	@Override
	public void run()
	{
		SpectatorPlus.get().getPlayerData(player).setSpectating(true);
	}
}
