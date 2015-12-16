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
package com.pgcraft.spectatorplus.listeners;

import com.pgcraft.spectatorplus.SpectatorPlus;
import com.pgcraft.spectatorplus.guis.InventoryGUI;
import com.pgcraft.spectatorplus.guis.PlayerInventoryGUI;
import fr.zcraft.zlib.components.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.Inventory;


public class GuiUpdatesListener implements Listener
{
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent ev)
	{
		updateInventory(ev.getInventory());
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryDrag(InventoryDragEvent ev)
	{
		updateInventory(ev.getInventory());
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemMove(InventoryMoveItemEvent ev)
	{
		updateInventory(ev.getDestination());
		updateInventory(ev.getSource());
	}


	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerDamage(EntityDamageEvent ev)
	{
		if (ev.getEntity() instanceof Player)
		{
			updatePlayerInventoryGUI();
		}
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerRegainHealth(EntityRegainHealthEvent ev)
	{
		if (ev.getEntity() instanceof Player)
		{
			updatePlayerInventoryGUI();
		}
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerXPChange(PlayerExpChangeEvent ev)
	{
		updatePlayerInventoryGUI();
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerFoodLevelChange(FoodLevelChangeEvent ev)
	{
		updatePlayerInventoryGUI();
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPotionSplash(PotionSplashEvent ev)
	{
		updatePlayerInventoryGUI();
	}


	private void updateInventory(final Inventory inventory)
	{
		Bukkit.getScheduler().runTaskLater(SpectatorPlus.get(), new Runnable()
		{
			@Override
			public void run()
			{
				if (inventory.getType() == InventoryType.PLAYER || inventory.getType() == InventoryType.CREATIVE)
				{
					updatePlayerInventoryGUI();
				}
				else if (inventory.getType() == InventoryType.CHEST)
				{
					Gui.update(InventoryGUI.class);
				}
			}
		}, 1l);
	}

	private void updatePlayerInventoryGUI()
	{
		Bukkit.getScheduler().runTaskLater(SpectatorPlus.get(), new Runnable()
		{
			@Override
			public void run()
			{
				Gui.update(PlayerInventoryGUI.class);
			}
		}, 1l);
	}
}
