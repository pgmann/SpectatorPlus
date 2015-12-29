/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

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
