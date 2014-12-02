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
	 * Sets a toggle.
	 * 
	 * @param toggle The toggle to set.
	 * @param value The new value.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @throws IllegalArgumentException if the type of the toggle is not compatible with the type of the value.
	 * 
	 * @since 2.1
	 */
	public void setConfig(Toggle toggle, Object value, boolean temp) {
		if(!temp) {
			p.toggles.set(toggle, value);
			p.toggles.save();
		}
		
		switch(toggle) {
			case CHAT_BLOCKCOMMANDS_ADMINBYPASS:
				p.adminBypass = (Boolean) value;
				break;
				
			case CHAT_BLOCKCOMMANDS_ENABLED:
				p.blockCmds = (Boolean) value;
				break;
				
			case CHAT_BLOCKCOMMANDS_WHITELIST:
				// Unimplemented
				break;
				
			case CHAT_SPECTATORCHAT:
				p.specChat = (Boolean) value;
				break;
				
			case ENFORCE_ARENA_BOUNDARIES:
				p.enforceArenaBoundary = (Boolean) value;
				break;
				
			case ONSPECMODECHANGED_TELEPORTATION_TOSPAWN:
				p.teleportToSpawnOnSpecChangeWithoutLobby = (Boolean) value;
				break;
				
			case ONSPECMODECHANGED_TELEPORTATION_WITHSPAWNCMD:
				p.useSpawnCommandToTeleport = (Boolean) value;
				break;
				
			case OUTPUT_MESSAGES:
				p.output = (Boolean) value;
				break;
				
			case SPECTATORS_SEE_OTHERS:
				p.seeSpecs = (Boolean) value;
				break;
				
			case SPECTATORS_TABLIST_PREFIX:
				p.scoreboard = (Boolean) value;
				break;
				
			case SPECTATOR_MODE_ON_DEATH:
				p.death = (Boolean) value;
				break;
				
			case TOOLS_ARENACHOOSER_ENABLED:
				p.clock = (Boolean) value;
				break;
				
			case TOOLS_ARENACHOOSER_ITEM:
				p.clockItem = (Material) value;
				break;
				
			case TOOLS_INSPECTOR_ENABLED:
				p.inspector = (Boolean) value;
				break;
				
			case TOOLS_INSPECTOR_ITEM:
				p.inspectorItem = (Material) value;
				break;
				
			case TOOLS_NEWBIEMODE:
				p.newbieMode = (Boolean) value;
				break;
				
			case TOOLS_TELEPORTER_ENABLED:
				p.compass = (Boolean) value;
				break;
				
			case TOOLS_TELEPORTER_HEALTH:
				p.playersHealthInTeleportationMenu = (Boolean) value;
				break;
				
			case TOOLS_TELEPORTER_INSPECTOR:
				p.inspectFromTPMenu = (Boolean) value;
				break;
				
			case TOOLS_TELEPORTER_ITEM:
				p.compassItem = (Material) value;
				break;
				
			case TOOLS_TELEPORTER_LOCATION:
				p.playersLocationInTeleportationMenu = (Boolean) value;
				break;
				
			case TOOLS_TOOLS_ENABLED:
				p.spectatorsTools = (Boolean) value;
				break;
				
			case TOOLS_TOOLS_ITEM:
				p.spectatorsToolsItem = (Material) value;
				break;
				
			case TOOLS_TOOLS_TPTODEATH_DISPLAYCAUSE:
				p.tpToDeathToolShowCause = (Boolean) value;
				break;
				
			case TOOLS_TOOLS_TPTODEATH_ENABLED:
				p.tpToDeathTool = (Boolean) value;
				break;
				
			default:
				break;
		}
	}
	
	/**
	 * Enables (or disables) the teleporter (compass).
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 1.9.2
	 * @deprecated Use {@link #setConfig(Toggle, Object, boolean)} instead.
	 */
	@Deprecated
	public void setCompass(boolean value, boolean temp) {
		setConfig(Toggle.TOOLS_TELEPORTER_ENABLED, value, temp);
	}
	
	/**
	 * Sets the item to be given as the teleporter (default: "compass").
	 * 
	 * @param value Item to be used instead of compass.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 2.0
	 * @deprecated Use {@link #setConfig(Toggle, Object, boolean)} instead.
	 */
	@Deprecated
	public void setCompassItem(String value, boolean temp) {
		setConfig(Toggle.TOOLS_TELEPORTER_ITEM, Material.matchMaterial(value), temp);
	}
	
	/**
	 * Enables (or disables) giving the arena selector in arena mode (clock).
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 1.9.2
	 * @deprecated Use {@link #setConfig(Toggle, Object, boolean)} instead.
	 */
	@Deprecated
	public void setArenaClock(boolean value, boolean temp) {
		setConfig(Toggle.TOOLS_ARENACHOOSER_ENABLED, value, temp);
	}
	
	/**
	 * Sets the item to be given as the arena chooser (default: "watch" [clock]).
	 * 
	 * @param value Item to be used instead of "watch", invalid entries will default to "watch". Examples: "compass", "stone", "wool"
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 2.0
	 * @deprecated Use {@link #setConfig(Toggle, Object, boolean)} instead.
	 */
	@Deprecated
	public void setClockItem(String value, boolean temp) {
		setConfig(Toggle.TOOLS_ARENACHOOSER_ITEM, Material.matchMaterial(value), temp);
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
	 * @deprecated Use {@link #setConfig(Toggle, Object, boolean)} instead.
	 */
	@Deprecated
	public void setSpectatorsTools(boolean value, boolean temp) {
		setConfig(Toggle.TOOLS_TOOLS_ENABLED, value, temp);
	}
	
	/**
	 * Sets the item to be given as the spectator's tools (default: "magma_cream").
	 * 
	 * @param value Item to be used instead of magma cream, invalid entries will default to "magma_cream". Examples: "watch", "stone", "wool"
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 2.0
	 * @deprecated Use {@link #setConfig(Toggle, Object, boolean)} instead.
	 */
	@Deprecated
	public void setSpectatorsToolsItem(String value, boolean temp) {
		setConfig(Toggle.TOOLS_TOOLS_ITEM, Material.matchMaterial(value), temp);
	}
	
	/**
	 * Enables/disables the "teleport to death point" tool.
	 * 
	 * @param value True if enabled.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 2.0
	 * @deprecated Use {@link #setConfig(Toggle, Object, boolean)} instead.
	 */
	@Deprecated
	public void setTPToDeathTool(boolean value, boolean temp) {
		setConfig(Toggle.TOOLS_TOOLS_TPTODEATH_ENABLED, value, temp);
	}
	
	/**
	 * Enables/disables the display of the death cause in the
	 * "teleport to death point" tool.
	 * 
	 * @param value True if enabled.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 2.0
	 * @deprecated Use {@link #setConfig(Toggle, Object, boolean)} instead.
	 */
	@Deprecated
	public void setShowCauseInTPToDeathTool(boolean value, boolean temp) {
		setConfig(Toggle.TOOLS_TOOLS_TPTODEATH_DISPLAYCAUSE, value, temp);
	}
	
	/**
	 * Enables (or disables) the inventory inspector (book).
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 2.0
	 * @deprecated Use {@link #setConfig(Toggle, Object, boolean)} instead.
	 */
	@Deprecated
	public void setInspector(boolean value, boolean temp) {
		setConfig(Toggle.TOOLS_INSPECTOR_ENABLED, value, temp);
	}
	
	/**
	 * Sets the item to be given as the inspector (default: "book").
	 * 
	 * @param value Item to be used instead of "book", invalid entries will default to "book". Examples: "compass", "stone", "wool"
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 2.0
	 * @deprecated Use {@link #setConfig(Toggle, Object, boolean)} instead.
	 */
	@Deprecated
	public void setInspectorItem(String value, boolean temp) {
		setConfig(Toggle.TOOLS_INSPECTOR_ITEM, Material.matchMaterial(value), temp);
	}
	
	/**
	 * If set to true, the players will be able to see the inventory & state of the players by right-clicking
	 * their heads in the teleportation menu.
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 2.0
	 * @deprecated Use {@link #setConfig(Toggle, Object, boolean)} instead.
	 */
	@Deprecated
	public void setInspectPlayerFromTeleportationMenu(boolean value, boolean temp) {
		setConfig(Toggle.TOOLS_TELEPORTER_INSPECTOR, value, temp);
	}
	
	/**
	 * If set to true, the players will be able to see the players' health in the tooltip
	 * of the teleportation menu.
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 2.0
	 * @deprecated Use {@link #setConfig(Toggle, Object, boolean)} instead.
	 */
	@Deprecated
	public void setPlayersHealthInTeleportationMenu(boolean value, boolean temp) {
		setConfig(Toggle.TOOLS_TELEPORTER_HEALTH, value, temp);
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
	 * @deprecated Use {@link #setConfig(Toggle, Object, boolean)} instead.
	 */
	@Deprecated
	public void setPlayersLocationInTeleportationMenu(boolean value, boolean temp) {
		setConfig(Toggle.TOOLS_TELEPORTER_LOCATION, value, temp);
	}
	
	/**
	 * Enables (or disables) spectator-only chat, invisible to non-specs.
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 1.9.2
	 * @deprecated Use {@link #setConfig(Toggle, Object, boolean)} instead.
	 */
	@Deprecated
	public void setSpectatorChatEnabled(boolean value, boolean temp) {
		setConfig(Toggle.CHAT_SPECTATORCHAT, value, temp);
	}
	
	/**
	 * Enables (or disables) SpectatorPlus plugin messages sent to spectators.
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 1.9.2
	 * @deprecated Use {@link #setConfig(Toggle, Object, boolean)} instead.
	 */
	@Deprecated
	public void setOutputMessages(boolean value, boolean temp) {
		setConfig(Toggle.OUTPUT_MESSAGES, value, temp);
	}
	
	/**
	 * Enables (or disables) spectate mode when a player dies.
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 1.9.2
	 * @deprecated Use {@link #setConfig(Toggle, Object, boolean)} instead.
	 */
	@Deprecated
	public void setSpectateOnDeath(boolean value, boolean temp) {
		setConfig(Toggle.SPECTATOR_MODE_ON_DEATH, value, temp);
	}
	
	/**
	 * Enables (or disables) the prefix of the spectator names in the tab list
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 1.9.2
	 * @deprecated Use {@link #setConfig(Toggle, Object, boolean)} instead.
	 */
	@Deprecated
	public void setColouredTabList(boolean value, boolean temp) {
		setConfig(Toggle.SPECTATORS_TABLIST_PREFIX, value, temp);
	}
	
	/**
	 * Enables (or disables) the availability to see other spectators when a player is spectating.
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 1.9.2
	 * @deprecated Use {@link #setConfig(Toggle, Object, boolean)} instead.
	 */
	@Deprecated
	public void setSeeSpectators(boolean value, boolean temp) {
		setConfig(Toggle.SPECTATORS_SEE_OTHERS, value, temp);
	}
	
	/**
	 * Enables (or disables) the blockage of the commands sent by a spectator.
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 1.9.2
	 * @deprecated Use {@link #setConfig(Toggle, Object, boolean)} instead.
	 */
	@Deprecated
	public void setBlockCommands(boolean value, boolean temp) {
		setConfig(Toggle.CHAT_BLOCKCOMMANDS_ENABLED, value, temp);
	}
	
	/**
	 * Enables (or disables) the availability to anyone with spectate.admin to bypass command blocking
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 1.9.2
	 * @deprecated Use {@link #setConfig(Toggle, Object, boolean)} instead.
	 */
	@Deprecated
	public void setAllowAdminBypassCommandBlocking(boolean value, boolean temp) {
		setConfig(Toggle.CHAT_BLOCKCOMMANDS_ADMINBYPASS, value, temp);
	}
	
	/**
	 * If enabled, "(Right-click)" will be added in the name of the spectators' tools (teleporter, etc.).
	 * 
	 * @param value Enabled if true.
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 2.0
	 * @deprecated Use {@link #setConfig(Toggle, Object, boolean)} instead.
	 */
	@Deprecated
	public void setNewbieMode(boolean value, boolean temp) {
		setConfig(Toggle.TOOLS_NEWBIEMODE, value, temp);
	}
	
	/**
	 * Teleport the players to the spawn, if there isn't any main lobby set, when the spectator
	 * mode is enabled/disabled?
	 * 
	 * @param value Enabled if true
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 2.0
	 * @deprecated Use {@link #setConfig(Toggle, Object, boolean)} instead.
	 */
	@Deprecated
	public void setTeleportToSpawnOnSpecChangeWithoutLobby(boolean value, boolean temp) {
		setConfig(Toggle.ONSPECMODECHANGED_TELEPORTATION_TOSPAWN, value, temp);
	}
	
	/**
	 * When teleporting the players to the spawn (without main lobby), use the /spawn command, or
	 * the spawn point of the current world?
	 * 
	 * @param value Enabled if true
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 2.0
	 * @deprecated Use {@link #setConfig(Toggle, Object, boolean)} instead.
	 */
	@Deprecated
	public void setUseSpawnCommandToTeleport(boolean value, boolean temp) {
		setConfig(Toggle.ONSPECMODECHANGED_TELEPORTATION_WITHSPAWNCMD, value, temp);
	}
	
	/**
	 * When teleporting the players to the spawn (without main lobby), use the /spawn command, or
	 * the spawn point of the current world?
	 * 
	 * @param value Enabled if true
	 * @param temp If true this change will not be saved in the config file.
	 * 
	 * @since 2.0
	 * @deprecated Use {@link #setConfig(Toggle, Object, boolean)} instead.
	 */
	@Deprecated
	public void setEnforceArenaBoundary(boolean value, boolean temp) {
		setConfig(Toggle.ENFORCE_ARENA_BOUNDARIES, value, temp);
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
