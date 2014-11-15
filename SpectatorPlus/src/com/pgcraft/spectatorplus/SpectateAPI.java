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
	private SpectatorPlus p;
	
	protected SpectateAPI(SpectatorPlus p) {
		this.p = p;
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
		if(p.getPlayerData(player) != null) {
			return p.getPlayerData(player).spectating;
		}
		
		return false;
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
			setSpectating(player, spectating, p.console);
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
			setSpectating(player, spectating, p.console, silent);
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
			p.enableSpectate(spectator, sender, silent);
		} else {
			p.disableSpectate(spectator, sender, silent);
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
		if (p.getPlayerData(spectator).spectating && !p.getPlayerData(spectator).spectating) {
			p.choosePlayer(spectator, target);
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
	 * Sets the current SpectatorPlus' mode.
	 * <p>
	 * <ul>
	 *   <li>{@code ANY}: the spectators can teleports themselves to any player in the server.</li>
	 *   <li>{@code ARENA}: the spectators will have to choose an arena; then they will be able 
	 *   to teleport themselves only to the players in this arena. An option is available to prevent 
	 *   the spectators from leaving the arena.</li>
	 *   <li>{@code WORLD}: the spectators will be able to teleport themselves to the players in the same world.</li>
	 * </ul>
	 * 
	 * @param mode The mode.
	 * @see SpectatorPlusMode
	 * 
	 * @since 2.0
	 */
	public void setSpectatorPlusMode(SpectatorMode mode) {
		p.setSpectatorMode(mode);
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
			p.toggles.getConfig().set("compass", value);
			p.toggles.saveConfig();
		}
		
		p.compass = value;
		p.reloadConfig(false);
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
			p.toggles.getConfig().set("compassItem", value);
			p.toggles.saveConfig();
		}
		
		if(value != null) {
			p.compassItem = Material.matchMaterial(value);
		}
		
		if(p.compassItem == null) p.compassItem = Material.COMPASS;
		p.reloadConfig(false);
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
			p.toggles.getConfig().set("arenaclock", value);
			p.toggles.saveConfig();
		}
		
		p.clock = value;
		p.reloadConfig(false);
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
			p.toggles.getConfig().set("clockItem", value);
			p.toggles.saveConfig();
		}
		
		if(value != null) {
			p.clockItem = Material.matchMaterial(value);
		}
		
		if(p.clockItem == null) p.clockItem = Material.WATCH;
		p.reloadConfig(false);
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
			p.toggles.getConfig().set("spectatorsTools", value);
			p.toggles.saveConfig();
		}
		
		p.spectatorsTools = value;
		p.reloadConfig(false);
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
			p.toggles.getConfig().set("spectatorsToolsItem", value);
			p.toggles.saveConfig();
		}
		
		if(value != null) {
			p.spectatorsToolsItem = Material.matchMaterial(value);
		}
		
		if(p.spectatorsToolsItem == null) p.spectatorsToolsItem = Material.MAGMA_CREAM;
		p.reloadConfig(false);
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
			p.toggles.getConfig().set("tpToDeathTool", value);
			p.toggles.saveConfig();
		}
		
		p.tpToDeathTool = value;
		p.reloadConfig(false);
		
		if(!value) {
			for(Player player : p.getServer().getOnlinePlayers()) {
				p.getPlayerData(player).deathLocation = null;
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
			p.toggles.getConfig().set("tpToDeathToolShowCause", value);
			p.toggles.saveConfig();
		}
		
		p.tpToDeathToolShowCause = value;
		p.reloadConfig(false);
		
		if(!value) {
			for(Player player : p.getServer().getOnlinePlayers()) {
				p.getPlayerData(player).lastDeathMessage = null;
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
			p.toggles.getConfig().set("inspector", value);
			p.toggles.saveConfig();
		}
		
		p.inspector = value;
		p.reloadConfig(false);
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
			p.toggles.getConfig().set("inspectorItem", value);
			p.toggles.saveConfig();
		}
		
		if(value != null) {
			p.inspectorItem = Material.matchMaterial(value);
		}
		
		if(p.inspectorItem == null) p.inspectorItem = Material.BOOK;
		p.reloadConfig(false);
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
			p.toggles.getConfig().set("inspectPlayerFromTeleportationMenu", value);
			p.toggles.saveConfig();
		}
		
		p.inspectFromTPMenu = value;
		p.reloadConfig(false);
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
			p.toggles.getConfig().set("playersHealthInTeleportationMenu", value);
			p.toggles.saveConfig();
		}
		
		p.playersHealthInTeleportationMenu = value;
		p.reloadConfig(false);
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
			p.toggles.getConfig().set("playersLocationInTeleportationMenu", value);
			p.toggles.saveConfig();
		}
		
		p.playersLocationInTeleportationMenu = value;
		p.reloadConfig(false);
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
			p.toggles.getConfig().set("specchat", value);
			p.toggles.saveConfig();
		}
		
		p.specChat = value;
		p.reloadConfig(false);
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
			p.toggles.getConfig().set("outputmessages", value);
			p.toggles.saveConfig();
		}
		
		p.output = value;
		p.reloadConfig(false);
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
			p.toggles.getConfig().set("deathspec", value);
			p.toggles.saveConfig();
		}
		
		p.death = value;
		p.reloadConfig(false);
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
			p.toggles.getConfig().set("colouredtablist", value);
			p.toggles.saveConfig();
		}
		
		p.scoreboard = value;
		p.reloadConfig(false);
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
			p.toggles.getConfig().set("seespecs", value);
			p.toggles.saveConfig();
		}
		
		p.seeSpecs = value;
		p.reloadConfig(false);
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
			p.toggles.getConfig().set("blockcmds", value);
			p.toggles.saveConfig();
		}
		
		p.blockCmds = value;
		p.reloadConfig(false);
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
			p.toggles.getConfig().set("adminbypass", value);
			p.toggles.saveConfig();
		}
		
		p.adminBypass = value;
		p.reloadConfig(false);
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
			p.toggles.getConfig().set("newbieMode", value);
			p.toggles.saveConfig();
		}
		
		p.newbieMode = value;
		p.reloadConfig(false);
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
			p.toggles.getConfig().set("teleportToSpawnOnSpecChangeWithoutLobby", value);
			p.toggles.saveConfig();
		}
		
		p.teleportToSpawnOnSpecChangeWithoutLobby = value;
		p.reloadConfig(false);
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
			p.toggles.getConfig().set("useSpawnCommandToTeleport", value);
			p.toggles.saveConfig();
		}
		
		p.useSpawnCommandToTeleport = value;
		p.reloadConfig(false);
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
	public void setEnforceArenaBoundary(boolean value, boolean temp) {
		if(!temp) {
			p.toggles.getConfig().set("enforceArenaBoundary", value);
			p.toggles.saveConfig();
		}
		
		p.enforceArenaBoundary = value;
		p.reloadConfig(false);
	}
	
	/**
	 * Returns the {@link ArenasManager Arenas manager}. Use this to manage the arenas.
	 * 
	 * @return The {@link ArenasManager}.
	 * 
	 * @since 2.0
	 */
	public ArenasManager getArenasManager() {
		return p.arenasManager;
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
		return p.setArenaForPlayer(player, arena.getName(), teleportToLobby);
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
		p.removePlayerFromArena(player);
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
		p.broadcastToSpectators(sender, message);
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
		p.sendSpectatorMessage(sender, message, isAction);
	}
}
