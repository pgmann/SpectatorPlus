/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.pgcraft.spectatorplus.listeners;

import com.pgcraft.spectatorplus.SpectatorPlus;
import com.pgcraft.spectatorplus.guis.InventoryGUI;
import com.pgcraft.spectatorplus.guis.PlayerInventoryGUI;
import fr.zcraft.quartzlib.components.gui.Gui;
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


public class GuiUpdatesListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent ev) {
        updateInventory(ev.getInventory());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent ev) {
        updateInventory(ev.getInventory());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemMove(InventoryMoveItemEvent ev) {
        updateInventory(ev.getDestination());
        updateInventory(ev.getSource());
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent ev) {
        if (ev.getEntity() instanceof Player) {
            updatePlayerInventoryGUI();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRegainHealth(EntityRegainHealthEvent ev) {
        if (ev.getEntity() instanceof Player) {
            updatePlayerInventoryGUI();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerXPChange(PlayerExpChangeEvent ev) {
        updatePlayerInventoryGUI();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFoodLevelChange(FoodLevelChangeEvent ev) {
        updatePlayerInventoryGUI();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent ev) {
        updatePlayerInventoryGUI();
    }

    private void updateInventory(final Inventory inventory) {
        Bukkit.getScheduler().runTaskLater(SpectatorPlus.get(), () -> {
            if (inventory.getType() == InventoryType.PLAYER || inventory.getType() == InventoryType.CREATIVE) {
                updatePlayerInventoryGUI();
            } else if (inventory.getType() == InventoryType.CHEST) {
                Gui.update(InventoryGUI.class);
            }
        }, 1L);
    }

    private void updatePlayerInventoryGUI() {
        Bukkit.getScheduler().runTaskLater(SpectatorPlus.get(), () -> Gui.update(PlayerInventoryGUI.class), 1L);
    }
}
