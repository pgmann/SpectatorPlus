/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.pgcraft.spectatorplus;

import com.pgcraft.spectatorplus.guis.ArenasSelectorGUI;
import com.pgcraft.spectatorplus.guis.PlayerInventoryGUI;
import com.pgcraft.spectatorplus.guis.SpectatorsToolsGUI;
import com.pgcraft.spectatorplus.guis.TeleportationGUI;
import com.pgcraft.spectatorplus.spectators.Spectator;
import fr.zcraft.zlib.components.configuration.Configuration;
import fr.zcraft.zlib.components.configuration.ConfigurationItem;
import fr.zcraft.zlib.components.configuration.ConfigurationList;
import fr.zcraft.zlib.components.gui.Gui;
import fr.zcraft.zlib.tools.Callback;
import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.players.ReducedDebugInfo;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static fr.zcraft.zlib.components.configuration.ConfigurationItem.item;
import static fr.zcraft.zlib.components.configuration.ConfigurationItem.list;

public class Toggles
{
	/* Miscellaneous toggles */

	public static final ConfigurationItem<Boolean> OUTPUT_MESSAGES = item("outputMessages", true, "outputmessages");
	public static final ConfigurationItem<Boolean> VANILLA_SPECTATOR_MODE = item("spectators.useVanillaMode", false);

	public static final ConfigurationItem<Boolean> REDUCE_DEBUG_INFO = item("spectators.reduceDebugInfo", false);

	public static final ConfigurationItem<Boolean> SKRIPT_INTEGRATION = item("skriptIntegration", false);

	public static final ConfigurationItem<Boolean> SPECTATOR_MODE_ON_DEATH = item("spectatorModeOnDeath", false, "deathspec");

	public static final ConfigurationItem<Boolean> ENFORCE_ARENA_BOUNDARIES = item("enforceBoundaries.arenas", false, "enforceArenaBoundary", "enforceArenaBoundaries");
	public static final ConfigurationItem<Double>  ENFORCE_LOBBY_BOUNDARIES = item("enforceBoundaries.lobby", 0d);


	/* Spectators lobby and spectator mode persistence */

	public static final ConfigurationItem<Boolean> ONSPECMODECHANGED_TELEPORTATION_TOSPAWN = item("onSpectatorModeChanged.teleportation.toSpawnWithoutLobby", false, "teleportToSpawnOnSpecChangeWithoutLobby");
	public static final ConfigurationItem<Boolean> ONSPECMODECHANGED_TELEPORTATION_WITHSPAWNCMD = item("onSpectatorModeChanged.teleportation.usingSpawnCommand", false, "useSpawnCommandToTeleport");
	public static final ConfigurationItem<Boolean> ONSPECMODECHANGED_SAVESPECTATORS = item("onSpectatorModeChanged.saveSpectators", true);

	/* Scoreboards-related */

	public static final ConfigurationItem<Boolean> SPECTATORS_TABLIST_PREFIX = item("spectators.tabList.prefix", false, "colouredtablist", "spectators.tabListPrefix");
	public static final ConfigurationItem<Boolean> SPECTATORS_TABLIST_HEALTH = item("spectators.tabList.health", false);
	public static final ConfigurationItem<Boolean> SPECTATORS_SEE_OTHERS = item("spectators.spectatorsSeeSpectators", false, "seespecs");


	/* Spectators tools */

	public static final ConfigurationItem<Boolean> TOOLS_NEWBIES_MODE = item("tools.newbieMode", true, "newbieMode");

	// TELEPORTER
	public static final ConfigurationItem<Boolean> TOOLS_TELEPORTER_ENABLED = item("tools.teleporter.enabled", true, "compass");

	public static final ConfigurationItem<Material> TOOLS_TELEPORTER_ITEM = item("tools.teleporter.item", Material.COMPASS, "compassItem");
	public static final ConfigurationItem<Boolean> TOOLS_TELEPORTER_INSPECTOR = item("tools.teleporter.inspector", true, "inspectPlayerFromTeleportationMenu");
	public static final ConfigurationItem<Boolean> TOOLS_TELEPORTER_HEALTH = item("tools.teleporter.health", true, "playersHealthInTeleportationMenu");
	public static final ConfigurationItem<Boolean> TOOLS_TELEPORTER_LOCATION = item("tools.teleporter.location", true, "playersLocationInTeleportationMenu");

	// ARENA SELECTOR
	public static final ConfigurationItem<Boolean> TOOLS_ARENA_SELECTOR_ENABLED = item("tools.arenaChooser.enabled", true, "arenaclock");

	public static final ConfigurationItem<Material> TOOLS_ARENA_SELECTOR_ITEM = item("tools.arenaChooser.item", Material.WATCH, "clockItem");
	public static final ConfigurationItem<Boolean> TOOLS_ARENA_SELECTOR_PLAYERS_COUNT = item("tools.arenaChooser.playersCount", true);
	public static final ConfigurationItem<Boolean> TOOLS_ARENA_SELECTOR_TECH_INFOS = item("tools.arenaChooser.technicalInfos", true);

	// TOOLS
	public static final ConfigurationItem<Boolean> TOOLS_TOOLS_ENABLED = item("tools.tools.enabled", true, "spectatorsTools");

	public static final ConfigurationItem<Material> TOOLS_TOOLS_ITEM = item("tools.tools.item", Material.MAGMA_CREAM, "spectatorsToolsItem");
	public static final ConfigurationItem<Boolean> TOOLS_TOOLS_SPEED = item("tools.tools.speed", true);
	public static final ConfigurationItem<Boolean> TOOLS_TOOLS_NIGHTVISION = item("tools.tools.nightVision", true);
	public static final ConfigurationItem<Boolean> TOOLS_TOOLS_DIVINGSUIT = item("tools.tools.divingSuit", true);
	public static final ConfigurationItem<Boolean> TOOLS_TOOLS_NOCLIP = item("tools.tools.noClipMode", true);
	public static final ConfigurationItem<Boolean> TOOLS_TOOLS_TPTODEATH_ENABLED = item("tools.tools.tpToDeath.enabled", true, "tpToDeathTool");
	public static final ConfigurationItem<Boolean> TOOLS_TOOLS_TPTODEATH_DISPLAYCAUSE = item("tools.tools.tpToDeath.displayCause", true, "tpToDeathToolShowCause");

	public static final ConfigurationItem<Boolean> TOOLS_TOOLS_GLOW = item("tools.tools.glowEffectIfActive", true);

	// INSPECTOR
	public static final ConfigurationItem<Boolean> TOOLS_INSPECTOR_ENABLED = item("tools.inspector.enabled", true, "inspector");
	public static final ConfigurationItem<Material> TOOLS_INSPECTOR_ITEM = item("tools.inspector.item", Material.FEATHER, "inspectorItem");
	
	// LEAVE SPECTATE MODE
	public static final ConfigurationItem<Boolean> TOOLS_LEAVE_ENABLED = item("tools.leave.enabled", true);
	public static final ConfigurationItem<Material> TOOLS_LEAVE_ITEM = item("tools.leave.item", Material.BED);


	/* Spectators chat */

	public static final ConfigurationItem<Boolean> CHAT_ENABLED = item("chat.spectatorChat", true, "specchat");
	public static final ConfigurationItem<Boolean> CHAT_SHOUT_ENABLED = item("chat.shout.enabled", true);
	public static final ConfigurationItem<String> CHAT_SHOUT_PREFIX = item("chat.shout.prefix", "!");
	public static final ConfigurationItem<Boolean> CHAT_BLOCKCOMMANDS_ENABLED = item("chat.blockCommands.enabled", true, "blockcmds");
	public static final ConfigurationItem<Boolean> CHAT_BLOCKCOMMANDS_ADMINBYPASS = item("chat.blockCommands.adminBypass", true, "adminbypass");

	public static final ConfigurationList<String> CHAT_BLOCKCOMMANDS_WHITELIST = list("chat.blockCommands.whitelist", String.class);

	public static final ConfigurationItem<Boolean> AUTOCOMPLETE_SPECTATORS_FOR_PLAYERS = item("chat.autocompleteSpectators.forPlayers", false);
	public static final ConfigurationItem<Boolean> AUTOCOMPLETE_SPECTATORS_FOR_SPECTATORS = item("chat.autocompleteSpectators.forSpectators", true);



	private static Map<String, ConfigurationItem<?>> ITEMS_BY_PATH = new HashMap<>();

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
					ITEMS_BY_PATH.put(((ConfigurationItem<?>) item).getFieldName(), (ConfigurationItem<?>) item);
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
			public void call(ConfigurationItem<?> toggle)
			{
				if (toggle == TOOLS_ARENA_SELECTOR_ITEM || toggle == TOOLS_INSPECTOR_ITEM || toggle == TOOLS_TELEPORTER_ITEM || toggle == TOOLS_TOOLS_ITEM || toggle == TOOLS_LEAVE_ITEM
						|| toggle == TOOLS_ARENA_SELECTOR_ENABLED || toggle == TOOLS_INSPECTOR_ENABLED || toggle == TOOLS_TELEPORTER_ENABLED || toggle == TOOLS_TOOLS_ENABLED || toggle == TOOLS_LEAVE_ENABLED)
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

				else if (toggle == REDUCE_DEBUG_INFO)
				{
					for (Player player : Bukkit.getOnlinePlayers())
						if (SpectatorPlus.get().getPlayerData(player).isSpectating())
							ReducedDebugInfo.setForPlayer(player, REDUCE_DEBUG_INFO.get());

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
		return Collections.unmodifiableCollection(ITEMS_BY_PATH.values());
	}

	/**
	 * @return all the toggles' paths.
	 */
	public static Set<String> getPaths()
	{
		return Collections.unmodifiableSet(ITEMS_BY_PATH.keySet());
	}

	/**
	 * @param path A toggle's path.
	 * @return The toggle; {@code null} if there isn't any toggle at this path.
	 */
	public static ConfigurationItem<?> getToggleFromPath(String path)
	{
		return ITEMS_BY_PATH.get(path);
	}
}
