package com.pgcraft.spectatorplus;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This is the API of SpectatorPlus.
 * <p>
 * Use the methods of this class to manage spectators, arenas and everything else.
 */
@SuppressWarnings("deprecation")
public class SpectateAPI {
	private SpectatorPlus plugin;
	
	protected SpectateAPI(SpectatorPlus plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Checks if a player is currently spectating.
	 * 
	 * @param player The player to check.
	 * 
	 * @return <b>true</b> if the player is spectating.
	 * 
	 * @since 1.9.2
	 */
	public boolean isSpectator(Player player) {
		return plugin.getPlayerData(player).spectating;
	}
	
	/**
	 * Enables or disables the spectator mode for a player.<br>
	 * Since this method provides no <i>sender</i>, the console is used by default.<br>
	 * Consider using {@link #setSpectating(Player player, boolean spectating, boolean silent)} instead to avoid outputting messages.
	 * 
	 * @param player The player.
	 * @param spectating True if the spectator mode needs to be enabled.
	 * 
	 * @since 1.9.2
	 */
	public void setSpectating(Player player, boolean spectating) {
		if(isSpectator(player) != spectating) {
			// Defaults to console having enabled spectator mode
			setSpectating(player, spectating, plugin.console);
		}
	}
	
	/**
	 * Enables or disables the spectator mode for a player.<br>
	 * Since this method provides no <i>sender</i>, the console is used by default.
	 * 
	 * @param player The player.
	 * @param spectating True if the spectator mode needs to be enabled.
	 * @param silent If true, will not output any messages - useful when using the API or command blocks.
	 * 
	 * @since 2.0
	 */
	public void setSpectating(Player player, boolean spectating, boolean silent) {
		if(isSpectator(player) != spectating) {
			// Defaults to console having enabled spectator mode
			setSpectating(player, spectating, plugin.console, silent);
		}
	}
	
	/**
	 * Enables or disables the spectator mode for a player.<br>
	 * Consider using {@link #setSpectating(Player player, boolean spectating, CommandSender sender, boolean silent)} instead to avoid outputting messages.
	 * 
	 * @param spectator The player.
	 * @param spectating True if the spectator mode needs to be enabled.
	 * @param sender The player (or the console) who enabled spectate mode
	 * 
	 * @since 1.9.2
	 */
	public void setSpectating(Player spectator, boolean spectating, CommandSender sender) {
		setSpectating(spectator, spectating, sender, false);
	}
	
	/**
	 * Enables or disables the spectator mode for a player.
	 * 
	 * @param spectator The player.
	 * @param spectating True if the spectator mode needs to be enabled.
	 * @param sender The player (or the console) who enabled spectate mode
	 * @param silent If true, will not output any messages - useful when using the API or command blocks.
	 * 
	 * @since 2.0
	 */
	public void setSpectating(Player spectator, boolean spectating, CommandSender sender, boolean silent) {
		if (spectating) {
			plugin.enableSpectate(spectator, sender, silent);
		} else {
			plugin.disableSpectate(spectator, sender, silent);
		}
	}
	
	/**
	 * Teleports a spectator to a player.<br>
	 * The teleportation fails if spectator is not spectating or if target is currently spectating.
	 * 
	 * @param spectator The spectator to be teleported.
	 * @param target The target.
	 * 
	 * @return True if the spectator was successfully teleported - <i>spectator</i> was spectating, and <i>target</i> was not.
	 * 
	 * @since 2.0
	 */
	public boolean teleportSpectatorToPlayer(Player spectator, Player target) {
		if (plugin.getPlayerData(spectator).spectating && !plugin.getPlayerData(spectator).spectating) {
			plugin.choosePlayer(spectator, target);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Teleports a spectator to a player.<br>
	 * The teleportation fails if spectator is not spectating or if target is currently spectating.
	 * 
	 * @param spectator The spectator to be teleported.
	 * @param target The target.
	 * 
	 * @return True if the spectator was successfully teleported - <i>spectator</i> was spectating, and <i>target</i> was not.
	 * 
	 * @since 1.9.2
	 * @deprecated Use {@link #teleportSpectatorToPlayer(Player spectator, Player target)} instead.
	 */
	@Deprecated
	public boolean spectatePlayer(Player spectator, Player target) {
		return teleportSpectatorToPlayer(spectator, target);
	}
	
	/**
	 * Enables (or disables) the teleporter (compass).
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 1.9.2
	 */
	public void setCompass(boolean value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("compass", value);
			plugin.toggles.saveConfig();
		}
		
		plugin.compass = value;
		plugin.reloadConfig(false);
	}
	
	/**
	 * Sets the item to be given as the teleporter (default: "compass").
	 * 
	 * @param value Item to be used instead of compass, invalid entries will default to "compass". Examples: "watch", "stone", "wool"
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 2.0
	 */
	public void setCompassItem(String value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("compassItem", value);
			plugin.toggles.saveConfig();
		}
		
		if(value != null) {
			plugin.compassItem = Material.matchMaterial(value);
		}
		
		if(plugin.compassItem == null) plugin.compassItem = Material.COMPASS;
		plugin.reloadConfig(false);
	}
	
	/**
	 * Enables (or disables) giving the arena selector in arena mode (clock).
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 1.9.2
	 */
	public void setArenaClock(boolean value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("arenaclock", value);
			plugin.toggles.saveConfig();
		}
		
		plugin.clock = value;
		plugin.reloadConfig(false);
	}
	
	/**
	 * Sets the item to be given as the arena chooser (default: "watch" [clock]).
	 * 
	 * @param value Item to be used instead of "watch", invalid entries will default to "watch". Examples: "compass", "stone", "wool"
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 2.0
	 */
	public void setClockItem(String value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("clockItem", value);
			plugin.toggles.saveConfig();
		}
		
		if(value != null) {
			plugin.clockItem = Material.matchMaterial(value);
		}
		
		if(plugin.clockItem == null) plugin.clockItem = Material.WATCH;
		plugin.reloadConfig(false);
	}
	
	/**
	 * Enables (or disables) the spectators' tools (magma cream).
	 * <p>
	 * These tools allows a spectator to change his speed, to enable night/underwater vision,
	 * and to teleport them to their death points (if enabled by the config).
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 2.0
	 */
	public void setSpectatorsTools(boolean value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("spectatorsTools", value);
			plugin.toggles.saveConfig();
		}
		
		plugin.spectatorsTools = value;
		plugin.reloadConfig(false);
	}
	
	/**
	 * Sets the item to be given as the spectator's tools (default: "magma_cream").
	 * 
	 * @param value Item to be used instead of magma cream, invalid entries will default to "magma_cream". Examples: "watch", "stone", "wool"
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 2.0
	 */
	public void setSpectatorsToolsItem(String value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("spectatorsToolsItem", value);
			plugin.toggles.saveConfig();
		}
		
		if(value != null) {
			plugin.spectatorsToolsItem = Material.matchMaterial(value);
		}
		
		if(plugin.spectatorsToolsItem == null) plugin.spectatorsToolsItem = Material.MAGMA_CREAM;
		plugin.reloadConfig(false);
	}
	
	/**
	 * Enables/disables the "teleport to death point" tool.
	 * 
	 * @param value True if enabled.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 2.0
	 */
	public void setTPToDeathTool(boolean value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("tpToDeathTool", value);
			plugin.toggles.saveConfig();
		}
		
		plugin.tpToDeathTool = value;
		plugin.reloadConfig(false);
		
		if(!value) {
			for(Player player : plugin.getServer().getOnlinePlayers()) {
				plugin.getPlayerData(player).deathLocation = null;
			}
		}
	}
	
	/**
	 * Enables/disables the display of the death cause in the
	 * "teleport to death point" tool.
	 * 
	 * @param value True if enabled.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 2.0
	 */
	public void setShowCauseInTPToDeathTool(boolean value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("tpToDeathToolShowCause", value);
			plugin.toggles.saveConfig();
		}
		
		plugin.tpToDeathToolShowCause = value;
		plugin.reloadConfig(false);
		
		if(!value) {
			for(Player player : plugin.getServer().getOnlinePlayers()) {
				plugin.getPlayerData(player).lastDeathMessage = null;
			}
		}
	}
	
	/**
	 * Enables (or disables) the inventory inspector (book).
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 2.0
	 */
	public void setInspector(boolean value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("inspector", value);
			plugin.toggles.saveConfig();
		}
		
		plugin.inspector = value;
		plugin.reloadConfig(false);
	}
	
	/**
	 * Sets the item to be given as the inspector (default: "book").
	 * 
	 * @param value Item to be used instead of "book", invalid entries will default to "book". Examples: "compass", "stone", "wool"
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 2.0
	 */
	public void setInspectorItem(String value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("inspectorItem", value);
			plugin.toggles.saveConfig();
		}
		
		if(value != null) {
			plugin.inspectorItem = Material.matchMaterial(value);
		}
		
		if(plugin.inspectorItem == null) plugin.inspectorItem = Material.BOOK;
		plugin.reloadConfig(false);
	}
	
	/**
	 * If set to true, the players will be able to see the inventory & state of the players by right-clicking
	 * their heads in the teleportation menu.
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 2.0
	 */
	public void setInspectPlayerFromTeleportationMenu(boolean value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("inspectPlayerFromTeleportationMenu", value);
			plugin.toggles.saveConfig();
		}
		
		plugin.inspectFromTPMenu = value;
		plugin.reloadConfig(false);
	}
	
	/**
	 * If set to true, the players will be able to see the players' health in the tooltip
	 * of the teleportation menu.
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 2.0
	 */
	public void setPlayersHealthInTeleportationMenu(boolean value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("playersHealthInTeleportationMenu", value);
			plugin.toggles.saveConfig();
		}
		
		plugin.playersHealthInTeleportationMenu = value;
		plugin.reloadConfig(false);
	}
	
	/**
	 * If set to true, the players will be able to see the player's relative location
	 * (distance + direction) in the tooltip of the teleportation menu (if both the player
	 * and the spectator are in the same world).
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 2.0
	 */
	public void setPlayersLocationInTeleportationMenu(boolean value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("playersLocationInTeleportationMenu", value);
			plugin.toggles.saveConfig();
		}
		
		plugin.playersLocationInTeleportationMenu = value;
		plugin.reloadConfig(false);
	}
	
	/**
	 * Enables (or disables) spectator-only chat, invisible to non-specs.
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 1.9.2
	 */
	public void setSpectatorChatEnabled(boolean value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("specchat", value);
			plugin.toggles.saveConfig();
		}
		
		plugin.specChat = value;
		plugin.reloadConfig(false);
	}
	
	/**
	 * Enables (or disables) SpectatorPlus plugin messages sent to spectators.
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 1.9.2
	 */
	public void setOutputMessages(boolean value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("outputmessages", value);
			plugin.toggles.saveConfig();
		}
		
		plugin.output = value;
		plugin.reloadConfig(false);
	}
	
	/**
	 * Enables (or disables) spectate mode when a player dies.
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 1.9.2
	 */
	public void setSpectateOnDeath(boolean value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("deathspec", value);
			plugin.toggles.saveConfig();
		}
		
		plugin.death = value;
		plugin.reloadConfig(false);
	}
	
	/**
	 * Enables (or disables) the prefix of the spectator names in the tab list
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 1.9.2
	 */
	public void setColouredTabList(boolean value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("colouredtablist", value);
			plugin.toggles.saveConfig();
		}
		
		plugin.scoreboard = value;
		plugin.reloadConfig(false);
	}
	
	/**
	 * Enables (or disables) the availability to see other spectators when a player is spectating.
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 1.9.2
	 */
	public void setSeeSpectators(boolean value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("seespecs", value);
			plugin.toggles.saveConfig();
		}
		
		plugin.seeSpecs = value;
		plugin.reloadConfig(false);
	}
	
	/**
	 * Enables (or disables) the blockage of the commands sent by a spectator.
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 1.9.2
	 */
	public void setBlockCommands(boolean value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("blockcmds", value);
			plugin.toggles.saveConfig();
		}
		
		plugin.blockCmds = value;
		plugin.reloadConfig(false);
	}
	
	/**
	 * Enables (or disables) the availability to anyone with spectate.admin to bypass command blocking
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 1.9.2
	 */
	public void setAllowAdminBypassCommandBlocking(boolean value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("adminbypass", value);
			plugin.toggles.saveConfig();
		}
		
		plugin.adminBypass = value;
		plugin.reloadConfig(false);
	}
	
	/**
	 * If enabled, "(Right-click)" will be added in the name of the spectators' tools (teleporter, etc.).
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 2.0
	 */
	public void setNewbieMode(boolean value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("newbieMode", value);
			plugin.toggles.saveConfig();
		}
		
		plugin.newbieMode = value;
		plugin.reloadConfig(false);
	}
	
	/**
	 * Teleport the players to the spawn, if there isn't any main lobby set, when the spectator
	 * mode is enabled/disabled?
	 * 
	 * @param value Enabled if true
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 2.0
	 */
	public void setTeleportToSpawnOnSpecChangeWithoutLobby(boolean value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("teleportToSpawnOnSpecChangeWithoutLobby", value);
			plugin.toggles.saveConfig();
		}
		
		plugin.teleportToSpawnOnSpecChangeWithoutLobby = value;
		plugin.reloadConfig(false);
	}
	
	/**
	 * When teleporting the players to the spawn (without main lobby), use the /spawn command, or
	 * the spawn point of the current world?
	 * 
	 * @param value Enabled if true
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 2.0
	 */
	public void setUseSpawnCommandToTeleport(boolean value, boolean temp) {
		if(!temp) {
			plugin.toggles.getConfig().set("useSpawnCommandToTeleport", value);
			plugin.toggles.saveConfig();
		}
		
		plugin.useSpawnCommandToTeleport = value;
		plugin.reloadConfig(false);
	}
	
	/**
	 * Returns the {@link ArenasManager Arenas manager}. Use this to manage the arenas.
	 * 
	 * @return The {@link ArenasManager}.
	 * 
	 * @since 2.0
	 */
	public ArenasManager getArenasManager() {
		return plugin.arenasManager;
	}
	
	/**
	 * Sets the arena for the given player.<br>
	 * Teleports the player to the lobby of that arena, if a lobby is available.
	 * 
	 * @param player The player.
	 * @param arena The arena.
	 * @param teleportToLobby If true the player will be teleported to the lobby (if a lobby is set).
	 * 
	 * @return True if the change was effective (i.e. the arena exists).
	 * 
	 * @since 2.0
	 */
	public boolean setArenaForPlayer(Player player, Arena arena, boolean teleportToLobby) {
		return plugin.setArenaForPlayer(player, arena.getName(), teleportToLobby);
	}
	
	/**
	 * Sets the arena for the given player.<br>
	 * Teleports the player to the lobby of that arena, if a lobby is available.
	 * 
	 * @param player The player.
	 * @param arena The arena.
	 * 
	 * @return True if the change was effective (i.e. the arena exists).
	 * 
	 * @since 2.0
	 */
	public boolean setArenaForPlayer(Player player, Arena arena) {
		return setArenaForPlayer(player, arena, true);
	}
	
	/**
	 * Removes a player from his arena.<br>
	 * The player is teleported to the main lobby, if such a lobby is set.
	 * 
	 * @param player The player to be removed from his arena.
	 * 
	 * @since 2.0
	 */
	public void removePlayerFromArena(Player player) {
		plugin.removePlayerFromArena(player);
	}
	
	
	
	/**
	 * Broadcasts a message to all players with spectator mode enabled, and the sender.
	 * 
	 * @param sender The sender of the message to be broadcasted.
	 * @param message The message to broadcast.
	 * 
	 * @since 2.0
	 */
	public void broadcastToSpectators(CommandSender sender, String message) {
		plugin.broadcastToSpectators(sender, message);
	}
	
	
	/**
	 * Sends a spectator chat message, from one spectator to all other spectators.<br> 
	 * Includes "/me" actions
	 * 
	 * @param sender The sender of the message.
	 * @param message The text of the message.
	 * @param isAction If true, the message will be displayed as an action message (like /me <message>).
	 * 
	 * @since 2.0
	 */
	public void sendSpectatorMessage(CommandSender sender, String message, Boolean isAction) {
		plugin.sendSpectatorMessage(sender, message, isAction);
	}
}
