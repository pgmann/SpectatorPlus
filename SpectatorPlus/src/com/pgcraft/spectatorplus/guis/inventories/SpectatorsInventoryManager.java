/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.pgcraft.spectatorplus.guis.inventories;

import com.pgcraft.spectatorplus.SpectatorPlus;
import com.pgcraft.spectatorplus.Toggles;
import com.pgcraft.spectatorplus.arenas.Arena;
import com.pgcraft.spectatorplus.spectators.Spectator;
import com.pgcraft.spectatorplus.spectators.SpectatorMode;
import fr.zcraft.zlib.components.gui.GuiUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;


@SuppressWarnings("deprecation")
public class SpectatorsInventoryManager
{
	// Titles of the tools in the inventory
	final static String TELEPORTER_TITLE = ChatColor.AQUA + "" + ChatColor.BOLD + "Teleporter";
	final static String ARENA_SELECTOR_TITLE = ChatColor.DARK_RED + "" + ChatColor.BOLD + "Arena selector";
	final static String TOOLS_TITLE = ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Spectators' tools";
	final static String INSPECTOR_TITLE = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Inspector";

	// Titles of the tools in the no-clip inventory
	final static String QUIT_NOCLIP_TITLE = ChatColor.DARK_GREEN + "Go back to the real "; //... spectator's name
	final static String NIGHT_VISION_ACTIVE_TITLE = ChatColor.DARK_PURPLE + "Disable night vision";
	final static String NIGHT_VISION_INACTIVE_TITLE = ChatColor.GOLD + "Enable night vision";

	// Newbie mode texts
	private final static String RIGHT_CLICK = ChatColor.GRAY + "(Right-click)";
	private final static String CLICK = ChatColor.GRAY + "(Click)";
	private final static String PUNCH_A_PLAYER = ChatColor.GRAY + "(Punch a player)";

	// Items used
	private static Material TELEPORTER_ITEM;
	private static Material ARENA_SELECTOR_ITEM;
	private static Material TOOLS_ITEM;
	private static Material INSPECTOR_ITEM;

	private final static Material QUIT_NOCLIP_ITEM = Material.SKULL_ITEM;
	private final static Material NIGHT_VISION_ACTIVE_ITEM = Material.EYE_OF_ENDER;
	private final static Material NIGHT_VISION_INACTIVE_ITEM = Material.ENDER_PEARL;


	public SpectatorsInventoryManager()
	{
		updateSpectatorsInventoriesConfig();
	}


	@SuppressWarnings("incomplete-switch")
	public void equipSpectator(Player player)
	{
		Spectator spectator = SpectatorPlus.get().getPlayerData(player);

		if (!spectator.isSpectating())
			return;

		player.getInventory().clear();
		player.getInventory().setArmorContents(null);

		switch (player.getGameMode())
		{
			case ADVENTURE:
				fillAdventureGamemodeInventory(player);
				break;

			case SPECTATOR:
				fillSpectatorGamemodeInventory(player);
				break;
		}

		player.updateInventory();
	}

	public void equipSpectators()
	{
		for (Player player : Bukkit.getOnlinePlayers())
		{
			equipSpectator(player);
		}
	}


	private void fillAdventureGamemodeInventory(Player player)
	{
		Spectator spectator = SpectatorPlus.get().getPlayerData(player);
		Inventory inventory = player.getInventory();

		if (Toggles.TOOLS_TELEPORTER_ENABLED.get())
		{
			ItemStack teleporter = GuiUtils.makeItem(TELEPORTER_ITEM, getItemTitle(TELEPORTER_TITLE, RIGHT_CLICK), Arrays.asList(
					ChatColor.GRAY + "Click to choose a player",
					ChatColor.GRAY + "to teleport to."
			));

			inventory.setItem(0, teleporter);
		}

		if (Toggles.TOOLS_ARENA_SELECTOR_ENABLED.get() && SpectatorPlus.get().getSpectatorsManager().getSpectatorsMode() == SpectatorMode.ARENA)
		{
			Arena spectatorArena = spectator.getArena();

			ItemStack arenaSelector = GuiUtils.makeItem(ARENA_SELECTOR_ITEM, getItemTitle(ARENA_SELECTOR_TITLE, RIGHT_CLICK), Arrays.asList(
					ChatColor.GRAY + "Click to choose an arena",
					ChatColor.GRAY + "to spectate in.",
					"",
					ChatColor.GRAY + "Current arena: " + ChatColor.WHITE + (spectatorArena == null ? "none" : spectatorArena.getName()) + ChatColor.GRAY + "."
			));

			inventory.setItem(Toggles.TOOLS_TELEPORTER_ENABLED.get() ? 1 : 0, arenaSelector);
		}

		if (Toggles.TOOLS_TOOLS_ENABLED.get())
		{
			ItemStack tools = GuiUtils.makeItem(TOOLS_ITEM, getItemTitle(TOOLS_TITLE, RIGHT_CLICK), Arrays.asList(
					ChatColor.GRAY + "Click to open the spectators'",
					ChatColor.GRAY + "tools menu."
			));


			// Where will this item be placed?

			// If there is only the inspector apart this, the item is placed on the left, for symmetry.
			// Else, if the inspector is not present but one of the other are (or both of them), the item is placed
			// on the right (same reason).

			int slot = 4;

			if (!Toggles.TOOLS_TELEPORTER_ENABLED.get() && !Toggles.TOOLS_ARENA_SELECTOR_ENABLED.get() && Toggles.TOOLS_INSPECTOR_ENABLED.get())
				slot = 0;

			else if (!Toggles.TOOLS_INSPECTOR_ENABLED.get() && (Toggles.TOOLS_TELEPORTER_ENABLED.get() || Toggles.TOOLS_ARENA_SELECTOR_ENABLED.get()))
				slot = 8;


			inventory.setItem(slot, tools);
		}

		if (Toggles.TOOLS_INSPECTOR_ENABLED.get())
		{
			ItemStack inspector = GuiUtils.makeItem(INSPECTOR_ITEM, getItemTitle(INSPECTOR_TITLE, PUNCH_A_PLAYER), Arrays.asList(
					ChatColor.GRAY + "Punch a player to see their",
					ChatColor.GRAY + "inventory, armour, health and more!"
			));

			inventory.setItem(8, inspector);
		}
	}

	private void fillSpectatorGamemodeInventory(Player player)
	{
		Inventory inventory = player.getInventory();

		ItemStack exit = null;
		ItemStack nightVision = null;

		if (!Toggles.VANILLA_SPECTATOR_MODE.get())
		{
			exit = GuiUtils.makeItem(new ItemStack(QUIT_NOCLIP_ITEM, 1, (short) 3), getItemTitle(QUIT_NOCLIP_TITLE + player.getName(), CLICK), Arrays.asList(
					ChatColor.GRAY + "Leave no-clip mode",
					ChatColor.DARK_GRAY + "You can also use /spec b"
			));
		}

		if (Toggles.TOOLS_TOOLS_NIGHTVISION.get())
		{
			Boolean nightVisionActive = false;
			for (PotionEffect effect : player.getActivePotionEffects())
			{
				if (effect.getType().equals(PotionEffectType.NIGHT_VISION))
				{
					nightVisionActive = true;
					break;
				}
			}

			nightVision = GuiUtils.makeItem(
					nightVisionActive ? NIGHT_VISION_ACTIVE_ITEM : NIGHT_VISION_INACTIVE_ITEM,
					getItemTitle(nightVisionActive ? NIGHT_VISION_ACTIVE_TITLE : NIGHT_VISION_INACTIVE_TITLE, CLICK)
			);
		}

		if (exit != null || nightVision != null)
		{
			if (exit != null && nightVision != null)
			{
				inventory.setItem(20, nightVision);
				inventory.setItem(24, exit);
			}
			else if (exit != null)
			{
				inventory.setItem(22, exit);
			}
			else
			{
				inventory.setItem(22, nightVision);
			}
		}
	}

	/**
	 * Updates the newbie tips to display “(Right-click)” if the inventory is closed and “(Click)” if it is open.
	 *
	 * @param player The player to update.
	 * @param isOpen {@code true} if the inventory is open.
	 */
	public void updateNewbieTips(Player player, boolean isOpen)
	{
		if (Toggles.TOOLS_NEWBIES_MODE.get())
		{
			String newbieTip = isOpen ? CLICK : RIGHT_CLICK;

			Inventory inventory = player.getInventory();

			// Initialized to the opposite of the enabled status, so if the button is not displayed,
			// this returns false and the button is marked as updated.
			Boolean teleporterUpdated    = !Toggles.TOOLS_TELEPORTER_ENABLED.get();
			Boolean arenaSelectorUpdated = !Toggles.TOOLS_ARENA_SELECTOR_ENABLED.get();
			Boolean toolsUpdated         = !Toggles.TOOLS_TOOLS_ENABLED.get();
			Boolean inspectorUpdated     = !Toggles.TOOLS_INSPECTOR_ENABLED.get();

			for (int slot = 0; slot < inventory.getSize(); slot++)
			{
				final ItemStack item = inventory.getItem(slot);

				if (item == null || item.getType() == Material.AIR)
					continue;

				final ItemMeta meta = item.getItemMeta();
				final String displayName = meta.getDisplayName();

				if (!teleporterUpdated && displayName.startsWith(TELEPORTER_TITLE))
				{
					meta.setDisplayName(getItemTitle(TELEPORTER_TITLE, newbieTip));
					teleporterUpdated = true;
				}
				else if (!arenaSelectorUpdated && displayName.startsWith(ARENA_SELECTOR_TITLE))
				{
					meta.setDisplayName(getItemTitle(ARENA_SELECTOR_TITLE, newbieTip));
					arenaSelectorUpdated = true;
				}
				else if (!toolsUpdated && displayName.startsWith(TOOLS_TITLE))
				{
					meta.setDisplayName(getItemTitle(TOOLS_TITLE, newbieTip));
					toolsUpdated = true;
				}
				else if (!inspectorUpdated && displayName.startsWith(INSPECTOR_TITLE))
				{
					meta.setDisplayName(getItemTitle(INSPECTOR_TITLE, newbieTip));
					inspectorUpdated = true;
				}

				if (teleporterUpdated && arenaSelectorUpdated && toolsUpdated && inspectorUpdated)
					break;
			}

			player.updateInventory();
		}
	}


	private String getItemTitle(String baseTitle, String newbieHelp)
	{
		return baseTitle + (Toggles.TOOLS_NEWBIES_MODE.get() ? " " + newbieHelp : "");
	}

	public void updateSpectatorsInventoriesConfig()
	{
		TELEPORTER_ITEM = Toggles.TOOLS_TELEPORTER_ITEM.get();
		ARENA_SELECTOR_ITEM = Toggles.TOOLS_ARENA_SELECTOR_ITEM.get();
		TOOLS_ITEM = Toggles.TOOLS_TOOLS_ITEM.get();
		INSPECTOR_ITEM = Toggles.TOOLS_INSPECTOR_ITEM.get();

		equipSpectators();
	}
}
