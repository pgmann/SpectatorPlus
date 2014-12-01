package com.pgcraft.spectatorplus;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;

/**
 * This class manages the SpectatorPlus' toggles.
 * 
 * @author Amaury Carrade
 * @since 2.1
 */
public class ToggleManager {
	
	private ConfigAccessor toggles = null;
	
	/**
	 * Constructor.
	 * 
	 * @param toggles The toggles.
	 */
	public ToggleManager(ConfigAccessor toggles) {
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
}
