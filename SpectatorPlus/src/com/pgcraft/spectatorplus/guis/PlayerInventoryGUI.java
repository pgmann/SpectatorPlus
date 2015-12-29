/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.pgcraft.spectatorplus.guis;

import com.pgcraft.spectatorplus.utils.RomanNumber;
import com.pgcraft.spectatorplus.utils.SPUtils;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class PlayerInventoryGUI extends ActionGui
{
	private static final DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("0.0");
	private static final DecimalFormat INTEGER_FORMATTER = new DecimalFormat("00");

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
						+ " (" + ChatColor.WHITE + ((int) Math.floor(displayedInventoryOwner.getExp() * 100)) + "%" + ChatColor.GRAY + " towards level " + (displayedInventoryOwner.getLevel() + 1) + ")"
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

			List<String> lore = new ArrayList<>();
			lore.add(ChatColor.GOLD + "" + activePotionEffects.size() + ChatColor.WHITE + " active potion effect" + (activePotionEffects.size() > 1 ? "s" : "") + ".");
			lore.add("");

			meta.clearCustomEffects();
			for (PotionEffect potionEffect : activePotionEffects)
			{
				final String effectName = SPUtils.getEffectName(potionEffect.getType());
				final String effectAmplifier = potionEffect.getAmplifier() == 0 ? "" : " " + RomanNumber.toRoman(potionEffect.getAmplifier() + 1);
				final String effectDuration = INTEGER_FORMATTER.format(Math.floor(potionEffect.getDuration() / (60 * 20))) + ":" + INTEGER_FORMATTER.format((potionEffect.getDuration() / 20) % 60);

				lore.add(ChatColor.DARK_GRAY + "- " + ChatColor.WHITE + effectName + effectAmplifier + ChatColor.GRAY + " (" + effectDuration + ")");
			}

			meta.setLore(lore);
			GuiUtils.hideItemAttributes(meta);

			effects.setItemMeta(meta);
			effects.setAmount(activePotionEffects.size());
		}

		action("", size + 15, effects);


		// Health

		final ItemStack health = GuiUtils.makeItem(Material.GOLDEN_APPLE, ChatColor.DARK_RED + "" + ChatColor.BOLD + "Health", Collections.singletonList(
				ChatColor.GOLD + "" + ((int) displayedInventoryOwner.getHealth()) + ChatColor.WHITE + " life points " + ChatColor.GRAY + "(out of 20)"
		));
		health.setAmount((int) displayedInventoryOwner.getHealth());

		action("", size + 16, health);


		// Food level

		final ItemStack food = GuiUtils.makeItem(Material.COOKIE, ChatColor.GOLD + "" + ChatColor.BOLD + "Food level", Arrays.asList(
				ChatColor.GRAY + "Food level: " + ChatColor.GOLD + displayedInventoryOwner.getFoodLevel() + ChatColor.WHITE + " food points " + ChatColor.GRAY + "(out of 20)",
				ChatColor.GRAY + "Saturation: " + ChatColor.GOLD + DECIMAL_FORMATTER.format(displayedInventoryOwner.getSaturation()) + ChatColor.WHITE + " points"
		));
		food.setAmount(displayedInventoryOwner.getFoodLevel());

		action("", size + 17, food);
	}
}
