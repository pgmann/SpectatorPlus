/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.pgcraft.spectatorplus.guis.inventories;

import com.pgcraft.spectatorplus.Permissions;
import com.pgcraft.spectatorplus.SpectatorPlus;
import com.pgcraft.spectatorplus.guis.ArenasSelectorGUI;
import com.pgcraft.spectatorplus.guis.PlayerInventoryGUI;
import com.pgcraft.spectatorplus.guis.SpectatorsToolsGUI;
import com.pgcraft.spectatorplus.guis.TeleportationGUI;
import com.pgcraft.spectatorplus.spectators.SpectatorMode;
import fr.zcraft.zlib.components.gui.Gui;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public class SpectatorsInventoryListener implements Listener
{
	/* **  GUIs or action buttons  ** */

	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryClick(InventoryClickEvent ev)
	{
		if (!(ev.getWhoClicked() instanceof Player) || ev.getCurrentItem() == null)
			return;

		Player clicker = (Player) ev.getWhoClicked();

		// Player is spectating and the clicked slot is in his own inventory
		if (SpectatorPlus.get().getPlayerData(clicker).isSpectating() && (ev.getView().getTopInventory() == null || ev.getRawSlot() >= ev.getInventory().getSize()))
		{
			handleClick(clicker, ev.getCurrentItem());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerInteract(PlayerInteractEvent ev)
	{
		if (ev.getItem() != null && ev.getAction() != Action.PHYSICAL && SpectatorPlus.get().getPlayerData(ev.getPlayer()).isSpectating())
		{
			handleClick(ev.getPlayer(), ev.getItem());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerPunchEntity(PlayerInteractEntityEvent ev)
	{
		if (ev.getRightClicked() instanceof Player && SpectatorPlus.get().getPlayerData(ev.getPlayer()).isSpectating())
		{
			if (ev.getPlayer().getItemInHand() != null && ev.getPlayer().getItemInHand().getItemMeta().getDisplayName().startsWith(SpectatorsInventoryManager.INSPECTOR_TITLE))
			{
				handlePunch(ev.getPlayer(), (Player) ev.getRightClicked());
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerPunchEntity(EntityDamageByEntityEvent ev)
	{
		if (ev.getDamager() instanceof Player && ev.getEntity() instanceof Player
				&& ((Player) ev.getDamager()).getItemInHand() != null
				&& ((Player) ev.getDamager()).getItemInHand().hasItemMeta()
				&& ((Player) ev.getDamager()).getItemInHand().getItemMeta().hasDisplayName())
		{
			if (SpectatorPlus.get().getPlayerData(((Player) ev.getDamager())).isSpectating())
			{
				if (((Player) ev.getDamager()).getItemInHand().getItemMeta().getDisplayName().startsWith(SpectatorsInventoryManager.INSPECTOR_TITLE))
				{
					handlePunch((Player) ev.getDamager(), (Player) ev.getEntity());
				}
				else
				{
					handleClick(((Player) ev.getDamager()), ((Player) ev.getDamager()).getItemInHand());
				}
			}
		}
	}


	/* **  Update of the newbie tips when the inventory is opened or closed  ** */

	@EventHandler (priority = EventPriority.MONITOR)
	public void onPlayerOpensInventory(InventoryOpenEvent ev)
	{
		if (SpectatorPlus.get().getPlayerData(((Player) ev.getPlayer())).isSpectating())
		{
			SpectatorPlus.get().getSpectatorsManager().getInventoryManager().updateNewbieTips(((Player) ev.getPlayer()), true);
		}
	}

	@EventHandler (priority = EventPriority.MONITOR)
	public void onPlayerClosesInventory(InventoryCloseEvent ev)
	{
		if (SpectatorPlus.get().getPlayerData(((Player) ev.getPlayer())).isSpectating())
		{
			SpectatorPlus.get().getSpectatorsManager().getInventoryManager().updateNewbieTips(((Player) ev.getPlayer()), false);
		}
	}


	/* **  Generic methods to handle clicks & punches  ** */

	private void handleClick(Player player, ItemStack clicked)
	{
		if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName())
			return;

		final SpectatorsInventoryManager inventoryManager = SpectatorPlus.get().getSpectatorsManager().getInventoryManager();
		final String displayName = clicked.getItemMeta().getDisplayName();

		if (displayName.startsWith(SpectatorsInventoryManager.TELEPORTER_TITLE))
		{
			if (SpectatorPlus.get().getSpectatorsManager().getSpectatorsMode() == SpectatorMode.ARENA && SpectatorPlus.get().getPlayerData(player).getArena() == null)
			{
				player.sendMessage(ChatColor.RED + "You have to select an arena before teleporting to a player.");
				player.sendMessage(ChatColor.GRAY + "Use the arena selector, next to the teleporter, to do so.");

				return;
			}

			Gui.open(player, new TeleportationGUI());
		}
		else if (displayName.startsWith(SpectatorsInventoryManager.ARENA_SELECTOR_TITLE))
		{
			Gui.open(player, new ArenasSelectorGUI());
		}
		else if (displayName.startsWith(SpectatorsInventoryManager.TOOLS_TITLE))
		{
			Gui.open(player, new SpectatorsToolsGUI());
		}
		else if (displayName.startsWith(SpectatorsInventoryManager.NIGHT_VISION_ACTIVE_TITLE))
		{
			player.removePotionEffect(PotionEffectType.NIGHT_VISION);
			player.removePotionEffect(PotionEffectType.WATER_BREATHING);
			inventoryManager.equipSpectator(player);
		}
		else if (displayName.startsWith(SpectatorsInventoryManager.NIGHT_VISION_INACTIVE_TITLE))
		{
			player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, true, false), true);
			player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 1, true, false), true);

			inventoryManager.equipSpectator(player);
		}
		else if (displayName.startsWith(SpectatorsInventoryManager.QUIT_NOCLIP_TITLE))
		{
			SpectatorPlus.get().getPlayerData(player).setGamemodeChangeAllowed(true);
			player.setGameMode(GameMode.ADVENTURE);
			SpectatorPlus.get().getPlayerData(player).setGamemodeChangeAllowed(false);

			inventoryManager.equipSpectator(player);

			player.setAllowFlight(true);
			player.setFlying(true); // The player comes from the spectator mode, so he was flying.

			player.sendMessage(ChatColor.GREEN + "No-clip mode disabled.");
		}
		else if (displayName.startsWith(SpectatorsInventoryManager.LEAVE_TITLE))
		{
			if(Permissions.DISABLE_SPECTATOR_MODE.grantedTo(player))
			{
				SpectatorPlus.get().getPlayerData(player).setSpectating(false, player);
			}
			else
			{
				// Update the inventory, as player permissions have changed.
				SpectatorPlus.get().getSpectatorsManager().getInventoryManager().equipSpectator(player);
			}
		}
	}

	private void handlePunch(Player player, Player clicked)
	{
		if (!SpectatorPlus.get().getPlayerData(clicked).isSpectating())
		{
			Gui.open(player, new PlayerInventoryGUI(clicked));
		}
	}
}
