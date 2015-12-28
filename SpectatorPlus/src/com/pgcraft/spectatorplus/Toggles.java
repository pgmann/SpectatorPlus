/*
 * Copyright or © or Copr. AmauryCarrade (2015)
 * 
 * http://amaury.carrade.eu
 * 
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 * 
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package com.pgcraft.spectatorplus;

import com.pgcraft.spectatorplus.guis.ArenasSelectorGUI;
import com.pgcraft.spectatorplus.guis.PlayerInventoryGUI;
import com.pgcraft.spectatorplus.guis.SpectatorsToolsGUI;
import com.pgcraft.spectatorplus.guis.TeleportationGUI;
import com.pgcraft.spectatorplus.spectators.Spectator;
import fr.zcraft.zlib.components.configuration.Configuration;
import fr.zcraft.zlib.components.configuration.ConfigurationItem;
import fr.zcraft.zlib.components.gui.Gui;
import fr.zcraft.zlib.tools.Callback;
import fr.zcraft.zlib.tools.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Toggles
{
	/* Miscellaneous toggles */

	public static final ConfigurationItem<Boolean> OUTPUT_MESSAGES = ConfigurationItem.item("outputMessages", true, "outputmessages");
	public static final ConfigurationItem<Boolean> VANILLA_SPECTATOR_MODE = ConfigurationItem.item("spectators.useVanillaMode", false);

	public static final ConfigurationItem<Boolean> SKRIPT_INTEGRATION = ConfigurationItem.item("skriptIntegration", false);

	public static final ConfigurationItem<Boolean> SPECTATOR_MODE_ON_DEATH = ConfigurationItem.item("spectatorModeOnDeath", false, "deathspec");

	public static final ConfigurationItem<Boolean> ENFORCE_ARENA_BOUNDARIES = ConfigurationItem.item("enforceBoundaries.arenas", false, "enforceArenaBoundary", "enforceArenaBoundaries");
	public static final ConfigurationItem<Double>  ENFORCE_LOBBY_BOUNDARIES = ConfigurationItem.item("enforceBoundaries.lobby", 0d);


	/* Spectators lobby */

	public static final ConfigurationItem<Boolean> ONSPECMODECHANGED_TELEPORTATION_TOSPAWN = ConfigurationItem.item("onSpectatorModeChanged.teleportation.toSpawnWithoutLobby", false, "teleportToSpawnOnSpecChangeWithoutLobby");
	public static final ConfigurationItem<Boolean> ONSPECMODECHANGED_TELEPORTATION_WITHSPAWNCMD = ConfigurationItem.item("onSpectatorModeChanged.teleportation.usingSpawnCommand", false, "useSpawnCommandToTeleport");


	/* Scoreboards-related */

	public static final ConfigurationItem<Boolean> SPECTATORS_TABLIST_PREFIX = ConfigurationItem.item("spectators.tabList.prefix", false, "colouredtablist", "spectators.tabListPrefix");
	public static final ConfigurationItem<Boolean> SPECTATORS_TABLIST_HEALTH = ConfigurationItem.item("spectators.tabList.health", false);
	public static final ConfigurationItem<Boolean> SPECTATORS_SEE_OTHERS = ConfigurationItem.item("spectators.spectatorsSeeSpectators", false, "seespecs");


	/* Spectators tools */

	public static final ConfigurationItem<Boolean> TOOLS_NEWBIES_MODE = ConfigurationItem.item("tools.newbieMode", true, "newbieMode");

	public static final ConfigurationItem<Boolean> TOOLS_TELEPORTER_ENABLED = ConfigurationItem.item("tools.teleporter.enabled", true, "compass");
	public static final ConfigurationItem<String>  TOOLS_TELEPORTER_ITEM = ConfigurationItem.item("tools.teleporter.item", Material.COMPASS.toString(), "compassItem");
	public static final ConfigurationItem<Boolean> TOOLS_TELEPORTER_INSPECTOR = ConfigurationItem.item("tools.teleporter.inspector", true, "inspectPlayerFromTeleportationMenu");
	public static final ConfigurationItem<Boolean> TOOLS_TELEPORTER_HEALTH = ConfigurationItem.item("tools.teleporter.health", true, "playersHealthInTeleportationMenu");
	public static final ConfigurationItem<Boolean> TOOLS_TELEPORTER_LOCATION = ConfigurationItem.item("tools.teleporter.location", true, "playersLocationInTeleportationMenu");

	public static final ConfigurationItem<Boolean> TOOLS_ARENA_SELECTOR_ENABLED = ConfigurationItem.item("tools.arenaChooser.enabled", true, "arenaclock");
	public static final ConfigurationItem<String>  TOOLS_ARENA_SELECTOR_ITEM = ConfigurationItem.item("tools.arenaChooser.item", Material.WATCH.toString(), "clockItem");
	public static final ConfigurationItem<Boolean> TOOLS_ARENA_SELECTOR_PLAYERS_COUNT = ConfigurationItem.item("tools.arenaChooser.playersCount", true);
	public static final ConfigurationItem<Boolean> TOOLS_ARENA_SELECTOR_TECH_INFOS = ConfigurationItem.item("tools.arenaChooser.technicalInfos", true);

	public static final ConfigurationItem<Boolean> TOOLS_TOOLS_ENABLED = ConfigurationItem.item("tools.tools.enabled", true, "spectatorsTools");
	public static final ConfigurationItem<String>  TOOLS_TOOLS_ITEM = ConfigurationItem.item("tools.tools.item", Material.MAGMA_CREAM.toString(), "spectatorsToolsItem");
	public static final ConfigurationItem<Boolean> TOOLS_TOOLS_SPEED = ConfigurationItem.item("tools.tools.speed", true);
	public static final ConfigurationItem<Boolean> TOOLS_TOOLS_NIGHTVISION = ConfigurationItem.item("tools.tools.nightVision", true);
	public static final ConfigurationItem<Boolean> TOOLS_TOOLS_DIVINGSUIT = ConfigurationItem.item("tools.tools.divingSuit", true);
	public static final ConfigurationItem<Boolean> TOOLS_TOOLS_NOCLIP = ConfigurationItem.item("tools.tools.noClipMode", true);
	public static final ConfigurationItem<Boolean> TOOLS_TOOLS_TPTODEATH_ENABLED = ConfigurationItem.item("tools.tools.tpToDeath.enabled", true, "tpToDeathTool");
	public static final ConfigurationItem<Boolean> TOOLS_TOOLS_TPTODEATH_DISPLAYCAUSE = ConfigurationItem.item("tools.tools.tpToDeath.displayCause", true, "tpToDeathToolShowCause");

	public static final ConfigurationItem<Boolean> TOOLS_TOOLS_GLOW = ConfigurationItem.item("tools.tools.glowEffectIfActive", true);

	public static final ConfigurationItem<Boolean> TOOLS_INSPECTOR_ENABLED = ConfigurationItem.item("tools.inspector.enabled", false, "inspector");
	public static final ConfigurationItem<String>  TOOLS_INSPECTOR_ITEM = ConfigurationItem.item("tools.inspector.item", Material.FEATHER.toString(), "inspectorItem");


	/* Spectators chat */

	public static final ConfigurationItem<Boolean> CHAT_ENABLED = ConfigurationItem.item("chat.spectatorChat", true, "specchat");
	public static final ConfigurationItem<Boolean> CHAT_BLOCKCOMMANDS_ENABLED = ConfigurationItem.item("chat.blockCommands.enabled", true, "blockcmds");
	public static final ConfigurationItem<Boolean> CHAT_BLOCKCOMMANDS_ADMINBYPASS = ConfigurationItem.item("chat.blockCommands.adminBypass", true, "adminbypass");

	public static final ConfigurationItem<List<String>> CHAT_BLOCKCOMMANDS_WHITELIST = ConfigurationItem.item("chat.blockCommands.whitelist", Collections.<String>emptyList());

	public static final ConfigurationItem<Boolean> AUTOCOMPLETE_SPECTATORS_FOR_PLAYERS = ConfigurationItem.item("chat.autocompleteSpectators.forPlayers", false);
	public static final ConfigurationItem<Boolean> AUTOCOMPLETE_SPECTATORS_FOR_SPECTATORS = ConfigurationItem.item("chat.autocompleteSpectators.forSpectators", true);



	private static Map<String, ConfigurationItem<?>> ITEMS_PER_PATH = new HashMap<>();

	static
	{
		/* **  Storage of the fields in a searchable map  ** */

		for (Field field : Toggles.class.getDeclaredFields())
		{
			try
			{
				field.setAccessible(true);
				Object item = field.get(null);

				if (item instanceof ConfigurationItem)
				{
					ITEMS_PER_PATH.put(((ConfigurationItem<?>) item).getFieldName(), (ConfigurationItem<?>) item);
				}
			}
			catch (IllegalAccessException e)
			{
				PluginLogger.error("Cannot access a field from the Toggle class, something weird just happened.", e);
			}
		}



		/* **  Update callback  ** */

		Configuration.registerConfigurationUpdateCallback(new Callback<ConfigurationItem<?>>()
		{
			@Override
			@SuppressWarnings ("unchecked")
			public void call(ConfigurationItem<?> toggle)
			{
				if (toggle == TOOLS_ARENA_SELECTOR_ITEM || toggle == TOOLS_INSPECTOR_ITEM || toggle == TOOLS_TELEPORTER_ITEM || toggle == TOOLS_TOOLS_ITEM
						|| toggle == TOOLS_ARENA_SELECTOR_ENABLED || toggle == TOOLS_INSPECTOR_ENABLED || toggle == TOOLS_TELEPORTER_ENABLED || toggle == TOOLS_TOOLS_ENABLED)
				{
					SpectatorPlus.get().getSpectatorsManager().getInventoryManager().updateSpectatorsInventoriesConfig();

					if (!TOOLS_TELEPORTER_ENABLED.get())
						Gui.close(TeleportationGUI.class);

					if (!TOOLS_ARENA_SELECTOR_ENABLED.get())
						Gui.close(ArenasSelectorGUI.class);

					if (!TOOLS_TOOLS_ENABLED.get())
						Gui.close(SpectatorsToolsGUI.class);

					if (!TOOLS_INSPECTOR_ENABLED.get())
						Gui.close(PlayerInventoryGUI.class);
				}

				else if (toggle == TOOLS_TELEPORTER_HEALTH || toggle == TOOLS_TELEPORTER_LOCATION || toggle == TOOLS_TELEPORTER_INSPECTOR)
				{
					Gui.update(TeleportationGUI.class);
				}

				else if (toggle == TOOLS_NEWBIES_MODE)
				{
					SpectatorPlus.get().getSpectatorsManager().getInventoryManager().equipSpectators();
				}

				else if (toggle == VANILLA_SPECTATOR_MODE)
				{
					if(VANILLA_SPECTATOR_MODE.get())
					{
						for (Player player : Bukkit.getOnlinePlayers())
						{
							final Spectator spectator = SpectatorPlus.get().getPlayerData(player);
							if (spectator.isSpectating())
							{
								spectator.setGameMode(GameMode.ADVENTURE);
								SpectatorPlus.get().getSpectatorsManager().getInventoryManager().equipSpectator(player);
							}
						}
					}

					SpectatorPlus.get().getSpectatorsManager().getInventoryManager().equipSpectators();
				}

				else if (toggle == SPECTATORS_TABLIST_PREFIX || toggle == SPECTATORS_TABLIST_HEALTH || toggle == SPECTATORS_SEE_OTHERS)
				{
					SpectatorPlus.get().getSpectatorsManager().rebuildScoreboard();
				}

				else if (toggle == TOOLS_ARENA_SELECTOR_PLAYERS_COUNT || toggle == TOOLS_ARENA_SELECTOR_TECH_INFOS
						|| toggle == TOOLS_TOOLS_SPEED || toggle == TOOLS_TOOLS_NIGHTVISION || toggle == TOOLS_TOOLS_DIVINGSUIT || toggle == TOOLS_TOOLS_NOCLIP
						|| toggle == TOOLS_TOOLS_TPTODEATH_ENABLED || toggle == TOOLS_TOOLS_TPTODEATH_DISPLAYCAUSE
						|| toggle == TOOLS_TOOLS_GLOW)
				{
					Gui.update(SpectatorsToolsGUI.class);
					Gui.update(ArenasSelectorGUI.class);

					if (toggle == TOOLS_TOOLS_NOCLIP && !TOOLS_TOOLS_NOCLIP.get())
					{
						for (Player player : Bukkit.getOnlinePlayers())
						{
							final Spectator spectator = SpectatorPlus.get().getPlayerData(player);
							if (spectator.isSpectating())
							{
								spectator.setGameMode(GameMode.ADVENTURE);
								SpectatorPlus.get().getSpectatorsManager().getInventoryManager().equipSpectator(player);
							}
						}
					}
					else if (toggle == TOOLS_TOOLS_DIVINGSUIT && !TOOLS_TOOLS_DIVINGSUIT.get())
					{
						for (Player player : Bukkit.getOnlinePlayers())
						{
							if (SpectatorPlus.get().getPlayerData(player).isSpectating())
							{
								player.getInventory().setArmorContents(null);
							}
						}
					}
					else if (toggle == TOOLS_TOOLS_SPEED && !TOOLS_TOOLS_SPEED.get())
					{
						for (Player player : Bukkit.getOnlinePlayers())
						{
							if (SpectatorPlus.get().getPlayerData(player).isSpectating())
							{
								player.removePotionEffect(PotionEffectType.SPEED);
								player.setFlySpeed(0.1f);
							}
						}
					}
					else if (toggle == TOOLS_TOOLS_NIGHTVISION && !TOOLS_TOOLS_NIGHTVISION.get())
					{
						for (Player player : Bukkit.getOnlinePlayers())
						{
							if (SpectatorPlus.get().getPlayerData(player).isSpectating())
							{
								player.removePotionEffect(PotionEffectType.NIGHT_VISION);
								player.removePotionEffect(PotionEffectType.WATER_BREATHING);
							}
						}
					}
				}
			}
		});
	}

	/**
	 * @return all the toggles.
	 */
	public static Collection<ConfigurationItem<?>> getToggles()
	{
		return Collections.unmodifiableCollection(ITEMS_PER_PATH.values());
	}

	/**
	 * @return all the toggles' paths.
	 */
	public static Set<String> getPaths()
	{
		return Collections.unmodifiableSet(ITEMS_PER_PATH.keySet());
	}

	/**
	 * @param path A toggle's path.
	 * @return The toggle; {@code null} if there isn't any toggle at this path.
	 */
	public static ConfigurationItem<?> getToggleFromPath(String path)
	{
		return ITEMS_PER_PATH.get(path);
	}
}
