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
package com.pgcraft.spectatorplus.guis.inventories;

import com.pgcraft.spectatorplus.SpectatorPlus;
import com.pgcraft.spectatorplus.guis.ArenasSelectorGUI;
import com.pgcraft.spectatorplus.guis.PlayerInventoryGUI;
import com.pgcraft.spectatorplus.guis.SpectatorsToolsGUI;
import com.pgcraft.spectatorplus.guis.TeleportationGUI;
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

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent ev)
	{
		if (!(ev.getWhoClicked() instanceof Player) || ev.getCurrentItem() == null)
			return;

		Player clicker = (Player) ev.getWhoClicked();

		if (ev.getInventory().equals(clicker.getInventory()) && SpectatorPlus.get().getPlayerData(clicker).isSpectating())
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
		if (ev.getDamager() instanceof Player && ev.getEntity() instanceof Player && ((Player) ev.getDamager()).getItemInHand() != null)
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
			if (ev.getInventory().equals(ev.getPlayer().getInventory()))
			{
				SpectatorPlus.get().getSpectatorsManager().getInventoryManager().updateNewbieTips(((Player) ev.getPlayer()), true);
			}
		}
	}

	@EventHandler (priority = EventPriority.MONITOR)
	public void onPlayerClosesInventory(InventoryCloseEvent ev)
	{
		if (SpectatorPlus.get().getPlayerData(((Player) ev.getPlayer())).isSpectating())
		{
			if (ev.getInventory().equals(ev.getPlayer().getInventory()))
			{
				SpectatorPlus.get().getSpectatorsManager().getInventoryManager().updateNewbieTips(((Player) ev.getPlayer()), false);
			}
		}
	}


	/* **  Generic methods to handle clicks & punches  ** */

	private void handleClick(Player player, ItemStack clicked)
	{
		final SpectatorsInventoryManager inventoryManager = SpectatorPlus.get().getSpectatorsManager().getInventoryManager();
		final String displayName = clicked.getItemMeta().getDisplayName();

		if (displayName.startsWith(SpectatorsInventoryManager.TELEPORTER_TITLE))
		{
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
			player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, true, false), true);
			player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0, true, false), true);

			inventoryManager.equipSpectator(player);
		}
		else if (displayName.startsWith(SpectatorsInventoryManager.QUIT_NOCLIP_TITLE))
		{
			SpectatorPlus.get().getPlayerData(player).setGamemodeChangeAllowed(true);
			player.setGameMode(GameMode.ADVENTURE);
			SpectatorPlus.get().getPlayerData(player).setGamemodeChangeAllowed(false);

			inventoryManager.equipSpectator(player);

			player.setAllowFlight(true);

			player.sendMessage(ChatColor.GREEN + "No-clip mode disabled.");
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
