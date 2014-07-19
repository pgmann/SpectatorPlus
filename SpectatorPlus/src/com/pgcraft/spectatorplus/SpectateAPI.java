package com.pgcraft.spectatorplus;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpectateAPI {
	private SpectatorPlus plugin;

	public SpectateAPI(SpectatorPlus plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Checks if a player is currently spectating.
	 * 
	 * @param player The player to check.
	 * @return True if the player is spectating.
	 */
	public boolean isSpectator(Player player) {
		return plugin.user.get(player.getName()).spectating;
	}
	
	/**
	 * Enables or disables the spectator mode on a player.
	 * 
	 * @param player The player.
	 * @param spectating True if the spectator mode needs to be enabled.
	 */
	public void setSpectating(Player player, boolean spectating) {
		// Defaults to console having enabled spectator mode
		setSpectating(player, spectating, plugin.console);
	}
	
	/**
	 * Enables or disables the spectator mode on a player.
	 * 
	 * @param player The player.
	 * @param spectating True if the spectator mode needs to be enabled.
	 * @param sender The player (or the console) who enabled spectate mode
	 */
	public void setSpectating(Player spectator, boolean spectating, CommandSender sender) {
		if (spectating) {
			plugin.enableSpectate(spectator, sender);
		} else {
			plugin.disableSpectate(spectator, sender);
		}
	}
	
	/**
	 * Teleports a spectator to a player.
	 * The teleportation fails if spectator is not spectating or if target is currently spectating.
	 * 
	 * @param spectator The spectator to be teleported.
	 * @param target The target.
	 * @return True if the player was effectively teleported.
	 */
	public boolean spectatePlayer(Player spectator, Player target) {
		if (plugin.user.get(spectator.getName()).spectating && !plugin.user.get(target.getName()).spectating) {
			plugin.choosePlayer(spectator, target);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Enables (or disables) the teleporter (compass).
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 */
	public void setCompass(boolean value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("compass", value);
			plugin.toggles.saveConfig();
		}
		
		plugin.compass = value;
	}
	
	/**
	 * Enables (or disables) the arena selector in arena mode (clock).
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 */
	public void setArenaClock(boolean value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("arenaclock", value);
			plugin.toggles.saveConfig();
		}
		
		plugin.clock = value;
	}
	
	/**
	 * Enables (or disables) spectator-only chat, invisible to non-specs.
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 */
	public void setSpectatorChatEnabled(boolean value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("specchat", value);
			plugin.toggles.saveConfig();
		}
		
		plugin.specChat = value;
	}
	
	/**
	 * Enables (or disables) SpectatorPlus plugin messages sent to spectators.
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 */
	public void setOutputMessages(boolean value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("outputmessages", value);
			plugin.toggles.saveConfig();
		}
		
		plugin.output = value;
	}
	
	/**
	 * Enables (or disables) spectate mode when a player dies.
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 */
	public void setSpectateOnDeath(boolean value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("deathspec", value);
			plugin.toggles.saveConfig();
		}
		
		plugin.death = value;
	}
	
	/**
	 * Enables (or disables) the prefix of the spectator names in the tab list
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 */
	public void setColouredTabList(boolean value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("colouredtablist", value);
			plugin.toggles.saveConfig();
		}
		
		plugin.scoreboard = value;
	}
	
	/**
	 * Enables (or disables) the availability to see other spectators when a player is spectating.
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 */
	public void setSeeSpectators(boolean value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("seespecs", value);
			plugin.toggles.saveConfig();
		}
		
		plugin.seeSpecs = value;
	}
	
	/**
	 * Enables (or disables) the blockage of the commands sent by a spectator.
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 */
	public void setBlockCommands(boolean value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("blockcmds", value);
			plugin.toggles.saveConfig();
		}
		
		plugin.blockCmds = value;
	}
	
	/**
	 * Enables (or disables) the availability to anyone with spectate.admin to bypass command blocking
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 */
	public void setAllowAdminBypassCommandBlocking(boolean value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("adminbypass", value);
			plugin.toggles.saveConfig();
		}
		
		plugin.adminBypass = value;
	}
}
