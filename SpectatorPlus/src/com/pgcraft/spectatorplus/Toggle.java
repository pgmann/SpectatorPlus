package com.pgcraft.spectatorplus;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;


/**
 * Represents a toggle.
 * <p>
 * If the path cannot be guessed from the constant's name, you will have to use the second constructor.
 * 
 * @author Amaury Carrade
 * @since 2.1
 *
 */
@SuppressWarnings("rawtypes")
public enum Toggle {
	
	// Tools-related toggles
	
	TOOLS_TELEPORTER_ENABLED(Boolean.class, true, "If true the teleportation menu will be available for spectators."),
	TOOLS_TELEPORTER_ITEM(Material.class, Material.COMPASS, "The item used as the teleportation tool"),
	TOOLS_TELEPORTER_INSPECTOR(Boolean.class, true, "Right-click on a head on the TP menu to see the inventory of this player?"),
	TOOLS_TELEPORTER_HEALTH(Boolean.class, true, "Display the health of the players in the tooltip, on the TP menu?"),
	TOOLS_TELEPORTER_LOCATION(Boolean.class, true, "Display the relative location of the players in the tooltip, on the TP menu?"),
	
	TOOLS_ARENACHOOSER_ENABLED("tools.arenaChooser.enabled", Boolean.class, true, "Enable the arena selector in arena mode (clock)? Tip: don't disable that except if you're using a plugin to put players into arenas automatically."),
	TOOLS_ARENACHOOSER_ITEM("tools.arenaChooser.item", Material.class, Material.WATCH, "The item used as the arena chooser."),
	
	TOOLS_TOOLS_ENABLED(Boolean.class, true, "Speed, night or underwater vision, teleportation to death location"),
	TOOLS_TOOLS_ITEM(Material.class, Material.MAGMA_CREAM, "The item to use as the spectators' tools"),
	TOOLS_TOOLS_TPTODEATH_ENABLED("tools.tools.tpToDeath.enabled", Boolean.class, true, "Add a tool to allow dead spectators to teleport themselves to their death point?"),
	TOOLS_TOOLS_TPTODEATH_DISPLAYCAUSE("tools.tools.tpToDeath.displayCause", Boolean.class, true, "Display the cause of the death in the tooltip? (Example: « You starved to death ».)"),
	TOOLS_TOOLS_GLOW("tools.tools.glowEffectIfActive", Boolean.class, true, "Add an enchantment-like effect on the currently active tools?"),
	
	TOOLS_INSPECTOR_ENABLED(Boolean.class, true, "If true, spectators will be able to see the players' inventories using an item in their hotbar, right-clicking the player."),
	TOOLS_INSPECTOR_ITEM(Material.class, Material.FEATHER, "The item used as the inspector."),
	
	TOOLS_NEWBIEMODE("tools.newbieMode", Boolean.class, true, "Add '(Right-click)' in the name of the spectators' tools (teleporter, etc.)?"),
	
	// Chat-related toggles
	
	CHAT_SPECTATORCHAT("chat.spectatorChat", Boolean.class, true, "Enable spectator-only chat, invisible to non-specs?"),
	CHAT_BLOCKCOMMANDS_ENABLED("chat.blockCommands.enabled", Boolean.class, true, "Block commands while spectating?"),
	CHAT_BLOCKCOMMANDS_ADMINBYPASS("chat.blockCommands.adminBypass", Boolean.class, true, "Allow anyone with spectate.admin to bypass command blocking?"),
	CHAT_BLOCKCOMMANDS_WHITELIST("chat.blockCommands.whitelist", List.class, new ArrayList(), "Commands allowed even without the bypass permission. The /me command is always available. Type the beginning of the command – the plugin will accept all commands starting with that. Initial / is required."),
	
	// Spectators-related toggles
	
	SPECTATORS_TABLIST_PREFIX("spectators.tabListPrefix", Boolean.class, true, "Prefix spectator names in the tab list? This will change the Scoreboard used, and restore the old one when spectator mode is disabled. If you see another plugin's sidebar/infos on players list disappearing when you enable the spectator mode, try to disable this."),
	SPECTATORS_SEE_OTHERS("spectators.spectatorsSeeSpectators", Boolean.class, true, "See other spectators when you're spectating? (*requires spectators.tabListPrefix to be true*)"),
	
	// What to do when the spectator mode is changed (enabled or disabled)?
	
	ONSPECMODECHANGED_TELEPORTATION_TOSPAWN("onSpectatorModeChanged.teleportation.toSpawnWithoutLobby", Boolean.class, true, "Teleport the player to the spawn if there isn't any Main Lobby set?"),
	ONSPECMODECHANGED_TELEPORTATION_WITHSPAWNCMD("onSpectatorModeChanged.teleportation.usingSpawnCommand", Boolean.class, true, "When teleporting the players to the spawn (without main lobby), use the /spawn command, or the spawn point of the current world?"),
	
	// Miscellaneous toggles
	
	OUTPUT_MESSAGES("outputMessages", Boolean.class, true, "Show spectatorplus plugin messages to spectators?"),
	
	SPECTATOR_MODE_ON_DEATH("spectatorModeOnDeath", Boolean.class, false, "Enable spectate mode when a player dies?"),
	
	ENFORCE_ARENA_BOUNDARIES("enforceArenaBoundaries", Boolean.class, true, "Should spectators be stopped from going out of the boundary of the arena they're in? Stops them from generating far-off chunks."),
	
	;
	
	
	private String path;
	private Class dataType;
	private Object defaultValue;
	private String description;
	
	/**
	 * A toggle with guessed path.
	 * <p>
	 * The name of the enum constant <strong>MUST</strong> be the path of the toggle in the toggles.yml file,
	 * with "_" instead of "." and in upper case.<br />
	 * This constructor only supports fully-lowercase paths.
	 * 
	 * @param dataType The type of this toggle (Boolean.class, etc.).
	 * @param defaultValue The default value of this toggle.
	 * @param description A description for this toggle.
	 * 
	 * @since 2.1
	 */
	@SuppressWarnings("unchecked")
	Toggle(Class dataType, Object defaultValue, String description) {
		Validate.isTrue(dataType.isAssignableFrom(defaultValue.getClass()), "Invalid type for the default value of the toggle " + name(), defaultValue.getClass().getCanonicalName());
		
		this.path = this.name().replace("_", ".").toLowerCase();
		this.dataType = dataType;
		this.defaultValue = defaultValue;
		this.description = description;
	}
	
	/**
	 * A toggle with an explicit path.
	 * 
	 * @param path The path.
	 * @param dataType The type of this toggle (Boolean.class, etc.).
	 * @param defaultValue The default value of this toggle.
	 * @param description A description for this toggle.
	 * 
	 * @since 2.1
	 */
	@SuppressWarnings("unchecked")
	Toggle(String path, Class dataType, Object defaultValue, String description) {
		Validate.isTrue(dataType.isAssignableFrom(defaultValue.getClass()), "Invalid type for the default value of the toggle " + name(), defaultValue.getClass().getCanonicalName());
		
		this.path = path;
		this.dataType = dataType;
		this.defaultValue = defaultValue;
		this.description = description;
	}
	
	/**
	 * Returns a Toggle from the path in the toggles.yml file.
	 * 
	 * @param path The path. Case-sensitive.
	 * @return The Toggle (null if not found).
	 */
	public static Toggle fromPath(String path) {
		for(Toggle toggle : values()) {
			if(toggle.getPath().equals(path)) return toggle;
		}
		
		return null;
	}
	
	/**
	 * Returns the path of this toggle in the toggles.yml file.
	 * 
	 * @return The path.
	 * 
	 * @since 2.1
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * Returns the data type of this toggle.
	 * 
	 * @return The type.
	 * 
	 * @since 2.1
	 */
	public Class getDataType() {
		return dataType;
	}
	
	/**
	 * Returns the default value of this toggle.
	 * 
	 * @return The default value.
	 * 
	 * @since 2.1
	 */
	public Object getDefaultValue() {
		return defaultValue;
	}
	
	/**
	 * Returns an user-friendly description of this toggle.
	 * 
	 * @return The description.
	 * 
	 * @since 2.1
	 */
	public String getDescription() {
		return description;
	}
}
