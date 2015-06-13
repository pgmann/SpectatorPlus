package com.pgcraft.spectatorplus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

/**
 * This class manages the SpectatorPlus' toggles.
 * 
 * @author Amaury Carrade
 * @since 2.1
 */
public class ToggleManager {
	
	private SpectatorPlus p = null;
	private ConfigAccessor toggles = null;
	
	/**
	 * Constructor.
	 * 
	 * @param plugin The main class of the plugin
	 * @param toggles The toggles.
	 */
	public ToggleManager(SpectatorPlus plugin, ConfigAccessor toggles) {
		p = plugin;
		this.toggles = toggles;
		
		migrate();
	}
	
	/**
	 * Returns the version of the toggles.yml file.
	 * 
	 * @return The version.
	 */
	public double getVersion() {
		return toggles.getConfig().getDouble("version");
	}
	
	/**
	 * Sets the version of the toggles.yml file.
	 * 
	 * @param version The version.
	 */
	private void setVersion(double version) {
		toggles.getConfig().set("version", version);
	}
	
	/**
	 * Returns the value of the given toggle.
	 * 
	 * @param toggle The toggle.
	 * @return The value.
	 */
	public Object get(Toggle toggle) {
		return toggles.getConfig().get(toggle.getPath(), toggle.getDefaultValue());
	}
	
	/**
	 * Returns the value of the given toggle casted as a boolean.
	 * 
	 * @param toggle The toggle.
	 * @return The value.
	 * 
	 * @throws IllegalArgumentException if the type of the toggle is not compatible.
	 */
	public Boolean getBoolean(Toggle toggle) {
		if(toggle == null) return null;
		
		Validate.isTrue(Boolean.class.isAssignableFrom(toggle.getDataType()), "Cannot cast this toggle to Boolean: ", toggle.getPath());
		
		return toggles.getConfig().getBoolean(toggle.getPath(), (boolean) toggle.getDefaultValue());
	}
	
	/**
	 * Returns the value of the given toggle casted as a string.
	 * 
	 * @param toggle The toggle.
	 * @return The value.
	 * 
	 * @throws IllegalArgumentException if the type of the toggle is not compatible.
	 */
	public String getString(Toggle toggle) {
		if(toggle == null) return null;
		
		Validate.isTrue(String.class.isAssignableFrom(toggle.getDataType()), "Cannot cast this toggle to String: ", toggle.getPath());
		
		return toggles.getConfig().getString(toggle.getPath(), (String) toggle.getDefaultValue());
	}
	
	/**
	 * Returns the value of the given toggle casted as a List.
	 * 
	 * @param toggle The toggle.
	 * @return The value.
	 * 
	 * @throws IllegalArgumentException if the type of the toggle is not compatible.
	 */
	@SuppressWarnings("rawtypes")
	public List getList(Toggle toggle) {
		if(toggle == null) return null;
		
		Validate.isTrue(List.class.isAssignableFrom(toggle.getDataType()), "Cannot cast this toggle to List: ", toggle.getPath());
		
		return toggles.getConfig().getList(toggle.getPath(), (List) toggle.getDefaultValue());
	}
	
	/**
	 * Returns the value of the given toggle casted as a Material.
	 * 
	 * @param toggle The toggle.
	 * @return The value.
	 * 
	 * @throws IllegalArgumentException if the type of the toggle is not compatible.
	 */
	public Material getMaterial(Toggle toggle) {
		if(toggle == null) return null;
		
		Validate.isTrue(Material.class.isAssignableFrom(toggle.getDataType()), "Cannot cast this toggle to Material: ", toggle.getPath());
		
		Material material = Material.matchMaterial(toggles.getConfig().getString(toggle.getPath(), ((Material) toggle.getDefaultValue()).toString()));
		
		if(material == null) {
			return (Material) toggle.getDefaultValue();
		}
		else {
			return material;
		}
	}
	
	
	/**
	 * Sets the value of the given toggle.
	 * 
	 * @param toggle The toggle.
	 * @param value The value.
	 * 
	 * @throws IllegalArgumentException if the type of the toggle is not compatible with the type of the value.
	 */
	public void set(Toggle toggle, Object value) {
		set(toggle, value, false);
	}
	
	/**
	 * Sets the value of the given toggle.
	 * 
	 * @param toggle The toggle.
	 * @param value The value. If null, the default value for this toggle is used.
	 * @param temp If true, this will only be reflected in the memory, not saved in the toggles.yml file.
	 * 
	 * @throws NullPointerException if the toggle is null.
	 * @throws IllegalArgumentException if the type of the toggle is not compatible with the type of the value.
	 */
	@SuppressWarnings("unchecked")
	public void set(Toggle toggle, Object value, boolean temp) {
		if(toggle == null) {
			throw new NullPointerException("The toggle cannot be null");
		}
		if(value == null) {
			value = toggle.getDefaultValue();
		}
		
		Validate.isTrue(toggle.getDataType().isAssignableFrom(value.getClass()), "Cannot cast the value of this toggle to the correct data type: ", toggle.getPath());
		
		
		// The value of the toggle is updated in memory
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
				
			case SPECTATORS_USE_VANILLA:
				p.vanillaSpectate = (Boolean) value;
				GameMode gm = (p.vanillaSpectate)? GameMode.SPECTATOR : GameMode.ADVENTURE;
				for (Player target : p.getServer().getOnlinePlayers()) {
					if (p.getPlayerData(target) != null && p.getPlayerData(target).spectating) {
						// Update each player to reflect the new gamemode.
						p.getPlayerData(target).gamemodeChangeAllowed=true;
						target.setGameMode(gm);
						p.getPlayerData(target).gamemodeChangeAllowed=false;
					}
				}
				p.updateSpectatorInventories();
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
				
			case TOOLS_TOOLS_GLOW:
				p.glowOnActiveTools = (Boolean) value;
				break;
				
			case TOOLS_TOOLS_DIVINGSUIT:
				p.divingSuitTool = (Boolean) value;
				break;
				
			case TOOLS_TOOLS_NIGHTVISION:
				p.nightVisionTool = (Boolean) value;
				break;
				
			case TOOLS_TOOLS_NOCLIP:
				p.noClipTool = (Boolean) value;
				break;
				
			case TOOLS_TOOLS_SPEED:
				p.speedTool = (Boolean) value;
				break;
				
			default:
				break;
		}
		
		// If we want to keep this value, the configuration file is updated.
		if(!temp) {
			toggles.getConfig().set(toggle.getPath(), value.toString());
			save();
		}
	}
	
	/**
	 * Saves the config file to the disk.
	 */
	public void save() {
		toggles.saveConfig();
	}
	
	/**
	 * Returns the internal configuration.
	 * 
	 * @return
	 */
	public Configuration getConfiguration() {
		return toggles.getConfig();
	}
	
	/**
	 * Returns the ConfigAccessor.
	 * 
	 * @return The ConfigAccessor.
	 * @see {@link ConfigAccessor}.
	 */
	public ConfigAccessor getConfigAccessor() {
		return toggles;
	}
	
	
	/**
	 * Upgrade the configuration, populating the configuration file with new keys and the
	 * appropriate default values.
	 */
	protected void upgrade() {
		Set<String> togglesND = getConfiguration().getKeys(true); // ND = no defaults
		
		for(Toggle toggle : Toggle.values()) {
			if(!togglesND.contains(toggle.getPath())) {
				set(toggle, toggle.getDefaultValue());
				p.getLogger().info("Added " + toggle.getPath() + ": " + toggle.getDefaultValue().toString() + " to the toggles");
			}
		}
		
		setVersion(p.version);
		save();
	}
	
	/**
	 * Migrates the configuration from the old to the new one, if some keys changed.
	 */
	protected void migrate() {
		if (getVersion() == p.version) {
			return; // Updated
		}
		
		HashMap<String, String> conversionTable = new HashMap<String, String>();
		
		if(getVersion() <= 2.0) {
			conversionTable.put("enforceArenaBoundary", "enforceArenaBoundaries");
			conversionTable.put("compass", "tools.teleporter.enabled");
			conversionTable.put("compassItem", "tools.teleporter.item");
			conversionTable.put("arenaclock", "tools.arenaChooser.enabled");
			conversionTable.put("clockItem", "tools.arenaChooser.item");
			conversionTable.put("spectatorsTools", "tools.tools.enabled");
			conversionTable.put("spectatorsToolsItem", "tools.tools.item");
			conversionTable.put("tpToDeathTool", "tools.tools.tpToDeath.enabled");
			conversionTable.put("tpToDeathToolShowCause", "tools.tools.tpToDeath.displayCause");
			conversionTable.put("inspector", "tools.inspector.enabled");
			conversionTable.put("inspectorItem", "tools.inspector.item");
			conversionTable.put("inspectPlayerFromTeleportationMenu", "tools.teleporter.inspector");
			conversionTable.put("playersHealthInTeleportationMenu", "tools.teleporter.health");
			conversionTable.put("playersLocationInTeleportationMenu", "tools.teleporter.location");
			conversionTable.put("specchat", "chat.spectatorChat");
			conversionTable.put("outputmessages", "outputMessages");
			conversionTable.put("deathspec", "spectatorModeOnDeath");
			conversionTable.put("colouredtablist", "spectators.tabListPrefix");
			conversionTable.put("seespecs", "spectators.spectatorsSeeSpectators");
			conversionTable.put("blockcmds", "chat.blockCommands.enabled");
			conversionTable.put("adminbypass", "chat.blockCommands.adminBypass");
			conversionTable.put("newbieMode", "tools.newbieMode");
			conversionTable.put("teleportToSpawnOnSpecChangeWithoutLobby", "onSpectatorModeChanged.teleportation.toSpawnWithoutLobby");
			conversionTable.put("useSpawnCommandToTeleport", "onSpectatorModeChanged.teleportation.usingSpawnCommand");
		}
		
		for(Map.Entry<String, String> conversion : conversionTable.entrySet()) {
			p.getLogger().info("Migrating " + conversion.getKey() + " to " + conversion.getValue() + "...");
			toggles.getConfig().set(conversion.getValue(), toggles.getConfig().get(conversion.getKey()));
			toggles.getConfig().set(conversion.getKey(), null);
		}
		
		setVersion(p.version);
		
		save();
	}
}
