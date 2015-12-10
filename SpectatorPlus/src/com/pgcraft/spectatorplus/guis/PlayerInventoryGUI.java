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
package com.pgcraft.spectatorplus.guis;

import fr.zcraft.zlib.components.gui.ActionGui;
import fr.zcraft.zlib.components.gui.GuiUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;


public class PlayerInventoryGUI extends ActionGui
{
	private static final DecimalFormat FORMATTER = new DecimalFormat("0.0");

	private Player displayedInventoryOwner;
	private PlayerInventory displayedInventory;

	public PlayerInventoryGUI(Player player)
	{
		displayedInventoryOwner = player;
		displayedInventory = displayedInventoryOwner.getInventory();
	}


	@Override
	protected void onUpdate()
	{
		final int size = displayedInventory.getSize();

		setSize(size + 18);  // + 18: a separator row, and a row with armor, XP, potion effects, health and feed level.
		setTitle(ChatColor.BLACK + displayedInventoryOwner.getDisplayName());


		// The separator between the inventory's content and the other player infos.

		final ItemStack separator = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
		final ItemMeta separatorMeta = separator.getItemMeta();
		separatorMeta.setDisplayName(ChatColor.GRAY + "Above: player's inventory");
		separatorMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Below: armor and others"));
		separator.setItemMeta(separatorMeta);


		// Player's inventory: the hotbar is 0-8; the inventory is 9-35.

		for (int i = 9; i < size; i++)
		{
			action("", i - 9, displayedInventory.getItem(i));
		}
		for (int i = 0; i < 9; i++)
		{
			action("", i + 27, displayedInventory.getItem(i));
		}


		// Separator

		for(int i = size; i < size + 9; i++) {
			action("", i, separator);
		}


		// Armor

		action("", size +  9, displayedInventory.getHelmet());
		action("", size + 10, displayedInventory.getChestplate());
		action("", size + 11, displayedInventory.getLeggings());
		action("", size + 12, displayedInventory.getBoots());


		// Separator

		action("", size + 13, separator);


		// Experience level

		final ItemStack xp = GuiUtils.makeItem(Material.EXP_BOTTLE, ChatColor.GREEN + "" + ChatColor.BOLD + "Experience", Collections.singletonList(
				ChatColor.GRAY + "Level " + ChatColor.WHITE + displayedInventoryOwner.getLevel() + ChatColor.GRAY
						+ "(" + ChatColor.WHITE + ((int) Math.floor(displayedInventoryOwner.getExp() * 100)) + "%" + ChatColor.GRAY + " towards level " + (displayedInventoryOwner.getLevel() + 1) + ")"
		));

		xp.setAmount(displayedInventoryOwner.getLevel());
		action("", size + 14, xp);


		// Potion effects

		final ItemStack effects;
		final Collection<PotionEffect> activePotionEffects = displayedInventoryOwner.getActivePotionEffects();

		if (activePotionEffects.size() == 0)
		{
			effects = GuiUtils.makeItem(Material.GLASS_BOTTLE, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Potion effects", Collections.singletonList(
					ChatColor.GRAY + "No active potion effects."
			));
		}
		else
		{
			effects = new Potion(PotionType.FIRE_RESISTANCE).toItemStack(1);
			PotionMeta meta = (PotionMeta) effects.getItemMeta();

			meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Potion effects");
			meta.setLore(Arrays.asList(
					ChatColor.GRAY + "" + activePotionEffects.size() + " active potion effect(s).",
					""
			));

			meta.clearCustomEffects();
			for (PotionEffect potionEffect : activePotionEffects)
			{
				meta.addCustomEffect(potionEffect, true);
			}

			effects.setItemMeta(meta);
			effects.setAmount(activePotionEffects.size());
		}

		action("", size + 15, effects);


		// Health

		final ItemStack health = GuiUtils.makeItem(Material.GOLDEN_APPLE, ChatColor.DARK_RED + "" + ChatColor.BOLD + "Health", Collections.singletonList(
				ChatColor.WHITE + "" + ((int) displayedInventoryOwner.getHealth()) + ChatColor.GRAY + " life points (out of 20)"
		));
		health.setAmount((int) displayedInventoryOwner.getHealth());

		action("", size + 16, health);


		// Food level

		final ItemStack food = GuiUtils.makeItem(Material.COOKIE, ChatColor.GOLD + "" + ChatColor.BOLD + "Food level", Arrays.asList(
						ChatColor.GRAY + "Food level: " + ChatColor.WHITE + displayedInventoryOwner.getFoodLevel() + ChatColor.GRAY + " food points (out of 20)",
						ChatColor.GRAY + "Saturation: " + ChatColor.WHITE + FORMATTER.format(displayedInventoryOwner.getSaturation())
		));
		food.setAmount(displayedInventoryOwner.getFoodLevel());

		action("", size + 17, food);
	}
}
