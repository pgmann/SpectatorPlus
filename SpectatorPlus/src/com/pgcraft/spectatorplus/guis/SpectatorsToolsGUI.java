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

import com.pgcraft.spectatorplus.SpectatorPlus;
import com.pgcraft.spectatorplus.Toggles;
import com.pgcraft.spectatorplus.spectators.Spectator;
import fr.zcraft.zlib.components.gui.ActionGui;
import fr.zcraft.zlib.components.gui.GuiAction;
import fr.zcraft.zlib.components.gui.GuiUtils;
import fr.zcraft.zlib.tools.items.GlowEffect;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class SpectatorsToolsGUI extends ActionGui
{
	@Override
	protected void onUpdate()
	{
		Spectator spectator = SpectatorPlus.get().getPlayerData(getPlayer());



		/* **  -----   Size   -----  ** */


		// We first need to know what is the size of the inventory

		// If a death location is registered for this player, and if every tool is
		// enabled, a line will have to be added.
		// That's why this is defined here, not below.
		// If the "tp to death" tool is disabled, the death location is not set. So it's useless to
		// check this here.

		Location deathPoint = spectator.getDeathLocation();

		int height = 0, offset = 0;
		if (Toggles.TOOLS_TOOLS_SPEED.get())
		{
			height++;
			offset = 9;
		}

		if (Toggles.TOOLS_TOOLS_DIVINGSUIT.get() || Toggles.TOOLS_TOOLS_NIGHTVISION.get() || Toggles.TOOLS_TOOLS_NOCLIP.get() || (Toggles.TOOLS_TOOLS_TPTODEATH_ENABLED.get() && deathPoint != null))
			height++;
		if (Toggles.TOOLS_TOOLS_DIVINGSUIT.get() && Toggles.TOOLS_TOOLS_NIGHTVISION.get() && Toggles.TOOLS_TOOLS_NOCLIP.get() && Toggles.TOOLS_TOOLS_TPTODEATH_ENABLED.get() && deathPoint != null)
			height++;

		setSize(height * 9);
		setTitle(ChatColor.BLACK + "Spectators' tools");



		/* **  -----   Active tools & effects   -----  ** */


		// Retrieves the current speed level, and the other enabled effects
		// 0 = no speed; 1 = speed I, etc.
		Integer speedLevel = 0;
		Boolean nightVisionActive = false;

		for (PotionEffect effect : getPlayer().getActivePotionEffects())
		{
			if (effect.getType().equals(PotionEffectType.SPEED))
			{
				speedLevel = effect.getAmplifier() + 1; // +1 because Speed I = amplifier 0.
			}
			else if (effect.getType().equals(PotionEffectType.NIGHT_VISION))
			{
				nightVisionActive = true;
			}
		}

		Boolean divingSuitEquipped = false;
		if (getPlayer().getInventory().getBoots() != null && getPlayer().getInventory().getBoots().getType() == Material.DIAMOND_BOOTS)
		{
			divingSuitEquipped = true;
		}

		List<String> activeLore = Collections.singletonList("" + ChatColor.GRAY + ChatColor.ITALIC + "Active");



		/* **  -----   Speed tools   -----  ** */


		if (Toggles.TOOLS_TOOLS_SPEED.get())
		{
			// Normal speed

			ItemStack normalSpeed = GuiUtils.makeItem(Material.STRING, ChatColor.DARK_AQUA + "Normal speed", speedLevel == 0 ? activeLore : null);
			if (speedLevel == 0) GlowEffect.addGlow(normalSpeed);

			action("speed_0", 2, normalSpeed);


			// Speed I

			ItemStack speedI = GuiUtils.makeItem(Material.FEATHER, ChatColor.AQUA + "Speed I", speedLevel == 1 ? activeLore : null);
			if (speedLevel == 1) GlowEffect.addGlow(speedI);

			action("speed_1", 3, speedI);


			// Speed II

			ItemStack speedII = GuiUtils.makeItem(Material.FEATHER, ChatColor.AQUA + "Speed II", speedLevel == 2 ? activeLore : null);
			speedII.setAmount(2);
			if (speedLevel == 2) GlowEffect.addGlow(speedII);

			action("speed_2", 4, speedII);


			// Speed III

			ItemStack speedIII = GuiUtils.makeItem(Material.FEATHER, ChatColor.AQUA + "Speed III", speedLevel == 3 ? activeLore : null);
			speedIII.setAmount(3);
			if (speedLevel == 3) GlowEffect.addGlow(speedIII);

			action("speed_3", 5, speedIII);


			// Speed IV

			ItemStack speedIV = GuiUtils.makeItem(Material.FEATHER, ChatColor.AQUA + "Speed IV", speedLevel == 4 ? activeLore : null);
			speedIV.setAmount(4);
			if (speedLevel == 4) GlowEffect.addGlow(speedIV);

			action("speed_4", 6, speedIV);
		}



		/* **  -----   Lines 2 & 3: content   -----  ** */

		List<Pair<String, ItemStack>> toolsOnLine2 = new ArrayList<>();

		// No-clip
		if (Toggles.TOOLS_TOOLS_NOCLIP.get())
		{
			ItemStack noClip = GuiUtils.makeItem(Material.BARRIER, ChatColor.LIGHT_PURPLE + "No-clip mode", Arrays.asList(
					ChatColor.GRAY + "Allows you to go through all the blocks.",
					"",
					ChatColor.GRAY + "You can also first-spectate a player",
					ChatColor.GRAY + "by left-clicking on him",
					ChatColor.DARK_GRAY + "Use Shift to quit the first-person",
					ChatColor.DARK_GRAY + "spectator mode.",
					"",
					ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + "In this mode, open your inventory",
					ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + "to access controls!"
			));

			toolsOnLine2.add(Pair.of("noClip", noClip));
		}

		// Night vision
		if (Toggles.TOOLS_TOOLS_NIGHTVISION.get())
		{
			ItemStack nightVision = GuiUtils.makeItem(
					nightVisionActive ? Material.EYE_OF_ENDER : Material.ENDER_PEARL,
					nightVisionActive ? ChatColor.DARK_PURPLE + "Disable night vision" : ChatColor.GOLD + "Enable night vision"
			);

			toolsOnLine2.add(Pair.of("nightVision", nightVision));
		}

		// Diving suit
		if (Toggles.TOOLS_TOOLS_DIVINGSUIT.get())
		{
			ItemStack divingSuit = GuiUtils.makeItem(Material.DIAMOND_BOOTS, ChatColor.BLUE + "Diving suit", Collections.singletonList(
					ChatColor.GRAY + "Get a pair of Depth Strider III boots"
			));

			if (divingSuitEquipped)
			{
				ItemMeta meta = divingSuit.getItemMeta();
				List<String> lore = meta.getLore();
				lore.add(activeLore.get(0));
				meta.setLore(lore);
				divingSuit.setItemMeta(meta);

				GlowEffect.addGlow(divingSuit);
			}

			toolsOnLine2.add(Pair.of("divingSuit", divingSuit));
		}

		// Teleportation to the death point
		ItemStack tpToDeathPoint = null;
		if (Toggles.TOOLS_TOOLS_TPTODEATH_ENABLED.get() && deathPoint != null)
		{
			tpToDeathPoint = GuiUtils.makeItem(
					Material.NETHER_STAR,
					ChatColor.YELLOW + "Go to your death point",
					Toggles.TOOLS_TOOLS_TPTODEATH_DISPLAYCAUSE.get() && spectator.getLastDeathMessage() != null ?
							Collections.singletonList(ChatColor.GRAY + spectator.getLastDeathMessage()) : null
			);
		}



		/* **  -----   Lines 2 & 3: display   -----  ** */

		int lineSize = toolsOnLine2.size();
		if (lineSize == 0 && deathPoint != null)
		{
			action("deathPoint", offset + 4, tpToDeathPoint);
		}
		else if (lineSize == 1)
		{
			if (deathPoint != null)
			{
				final Pair<String, ItemStack> toolZero = toolsOnLine2.get(0);

				action(toolZero.getKey(), offset + 2, toolZero.getValue());
				action("deathPoint", offset + 6, tpToDeathPoint);
			}
			else
			{
				final Pair<String, ItemStack> toolZero = toolsOnLine2.get(0);
				action(toolZero.getKey(), offset + 4, toolZero.getValue());
			}
		}
		else if (lineSize == 2)
		{
			if (deathPoint != null)
			{
				final Pair<String, ItemStack> toolZero = toolsOnLine2.get(0);
				final Pair<String, ItemStack> toolOne = toolsOnLine2.get(1);

				action(toolZero.getKey(), offset + 2, toolZero.getValue());
				action(toolOne.getKey(), offset + 4, toolOne.getValue());
				action("deathPoint", offset + 6, tpToDeathPoint);
			}
			else
			{
				final Pair<String, ItemStack> toolZero = toolsOnLine2.get(0);
				final Pair<String, ItemStack> toolOne = toolsOnLine2.get(1);

				action(toolZero.getKey(), offset + 2, toolZero.getValue());
				action(toolOne.getKey(), offset + 6, toolOne.getValue());
			}
		}
		else if (lineSize == 3)
		{
			final Pair<String, ItemStack> toolZero = toolsOnLine2.get(0);
			final Pair<String, ItemStack> toolOne = toolsOnLine2.get(1);
			final Pair<String, ItemStack> toolTwo = toolsOnLine2.get(2);

			action(toolZero.getKey(), offset + 2, toolZero.getValue());
			action(toolOne.getKey(), offset + 4, toolOne.getValue());
			action(toolTwo.getKey(), offset + 6, toolTwo.getValue());

			if (deathPoint != null)
			{
				action("deathPoint", offset + 6, tpToDeathPoint);
			}
		}
	}


	@GuiAction (value = "speed_0")
	protected void normalSpeed()
	{
		getPlayer().removePotionEffect(PotionEffectType.SPEED);
		getPlayer().setFlySpeed(0.10f);

		update();
	}

	@GuiAction (value = "speed_1")
	protected void speedI()
	{
		getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0), true);
		getPlayer().setFlySpeed(0.13f);

		update();
	}

	@GuiAction (value = "speed_2")
	protected void speedII()
	{
		getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1), true);
		getPlayer().setFlySpeed(0.16f);

		update();
	}

	@GuiAction (value = "speed_3")
	protected void speedIII()
	{
		getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2), true);
		getPlayer().setFlySpeed(0.19f);

		update();
	}

	@GuiAction (value = "speed_4")
	protected void speedIV()
	{
		getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3), true);
		getPlayer().setFlySpeed(0.22f);

		update();
	}

	@GuiAction (value = "noClip")
	protected void noClip()
	{
		SpectatorPlus.get().getPlayerData(getPlayer()).setGamemodeChangeAllowed(true);
		getPlayer().setGameMode(GameMode.SPECTATOR);
		SpectatorPlus.get().getPlayerData(getPlayer()).setGamemodeChangeAllowed(false);

		// TODO update inventory

		getPlayer().sendMessage(ChatColor.GREEN + "No-clip mode enabled");
		getPlayer().sendMessage(ChatColor.GRAY + "Open your inventory to access controls or to quit the no-clip mode");

		close();
	}

	@GuiAction (value = "nightVision")
	protected void nightVision()
	{
		if (getPlayer().hasPotionEffect(PotionEffectType.NIGHT_VISION))
		{
			getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
			getPlayer().removePotionEffect(PotionEffectType.WATER_BREATHING);
		}
		else
		{
			getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0), true);
			getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0), true);
		}

		update();
	}

	@GuiAction (value = "divingSuit")
	protected void divingSuit()
	{
		if (getPlayer().getInventory().getBoots() != null && getPlayer().getInventory().getBoots().getType() == Material.DIAMOND_BOOTS)
		{
			getPlayer().getInventory().setBoots(null);
		}
		else
		{
			ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
			boots.addEnchantment(Enchantment.DEPTH_STRIDER, 3);
			getPlayer().getInventory().setBoots(boots);
		}

		update();
	}

	@GuiAction (value = "deathPoint")
	protected void deathPoint()
	{
		getPlayer().teleport(SpectatorPlus.get().getPlayerData(getPlayer()).getDeathLocation().setDirection(getPlayer().getLocation().getDirection()));

		close();
	}
}
