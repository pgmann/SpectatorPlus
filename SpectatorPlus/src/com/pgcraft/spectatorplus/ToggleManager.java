package com.pgcraft.spectatorplus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;

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
		this.p = plugin;
		this.toggles = toggles;
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
	@SuppressWarnings("unchecked")
	public void set(Toggle toggle, Object value) {
		Validate.isTrue(toggle.getDataType().isAssignableFrom(value.getClass()), "Cannot cast this toggle to Boolean: ", toggle.getPath());
		
		toggles.getConfig().set(toggle.getPath(), value);
	}
	
	/**
	 * Saves the config file to the disk.
	 */
	public void save() {
		toggles.saveConfig();
	}
	
	
	/**
	 * Migrates the configuration from the old to the new one.
	 */
	protected void migrate() {
		if (getVersion() == p.version) {
			return; // Updated
		}
		
		HashMap<String, String> conversionTable = new HashMap<String, String>();
		
		if(getVersion() <= 2.0) {
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
			toggles.getConfig().set(conversion.getValue(), toggles.getConfig().get(conversion.getKey()));
			toggles.getConfig().set(conversion.getKey(), null);
		}
		
		setVersion(p.version);
		
		save();
	}
}
