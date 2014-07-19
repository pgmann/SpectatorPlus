package com.pgcraft.spectatorplus;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpectateAPI {
	private SpectatorPlus plugin;

	public SpectateAPI(SpectatorPlus plugin) {
		this.plugin = plugin;
	}

	public boolean isSpectator(Player player) {
		return plugin.user.get(player.getName()).spectating;
	}

	public void setSpectating(Player player, boolean spectating) {
		// Defaults to console having enabled spectator mode
		setSpectating(player, spectating, plugin.console);
	}

	public void setSpectating(Player spectator, boolean spectating, CommandSender sender) {
		// sender: the player (or the console) who enabled spectate mode
		if (spectating) {
			plugin.enableSpectate(spectator, sender);
		} else {
			plugin.disableSpectate(spectator, sender);
		}
	}

	public boolean spectatePlayer(Player spectator, Player target) {
		if (plugin.user.get(spectator.getName()).spectating && !plugin.user.get(target.getName()).spectating) {
			plugin.choosePlayer(spectator, target);
			return true;
		} else {
			return false;
		}
	}
}
