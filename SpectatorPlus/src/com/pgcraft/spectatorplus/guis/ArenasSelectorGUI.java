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

import com.pgcraft.spectatorplus.Permissions;
import com.pgcraft.spectatorplus.SpectatorPlus;
import com.pgcraft.spectatorplus.Toggles;
import com.pgcraft.spectatorplus.arenas.Arena;
import com.pgcraft.spectatorplus.spectators.Spectator;
import com.pgcraft.spectatorplus.utils.SPUtils;
import fr.zcraft.zlib.components.gui.ExplorerGui;
import fr.zcraft.zlib.components.gui.GuiUtils;
import fr.zcraft.zlib.tools.items.GlowEffect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class ArenasSelectorGUI extends ExplorerGui<Arena>
{
	@Override
	protected void onUpdate()
	{
		Collection<Arena> arenas = SpectatorPlus.get().getArenasManager().getArenas();

		setTitle(ChatColor.BLACK + "Arenas " + ChatColor.RESET + "(" + arenas.size() + ")");
		setData(arenas.toArray(new Arena[arenas.size()]));
	}

	@Override
	protected ItemStack getViewItem(Arena arena)
	{
		final Spectator spectator = SpectatorPlus.get().getPlayerData(getPlayer());
		final Boolean inThisArena = spectator.getArena() != null && spectator.getArena().equals(arena);

		final List<String> lore = new ArrayList<>();

		if (Toggles.TOOLS_ARENA_SELECTOR_PLAYERS_COUNT.get())
		{
			Integer playersCount = 0;
			for (Player player : Bukkit.getOnlinePlayers())
			{
				if (!SpectatorPlus.get().getPlayerData(player).isSpectating() && arena.isInside(player.getLocation()))
				{
					playersCount++;
				}
			}

			lore.add("");
			lore.add(ChatColor.WHITE + "" + playersCount + " players " + ChatColor.GRAY + "in this arena");
		}

		if (Toggles.TOOLS_ARENA_SELECTOR_TECH_INFOS.get() && Permissions.MANAGE_ARENAS.grantedTo(getPlayer()))
		{
			lore.add("");
			lore.add(ChatColor.BLUE + "About this arena");
			lore.add(ChatColor.WHITE + "UUID" + ChatColor.GRAY + ": " + arena.getUUID().toString().toUpperCase());
			lore.add(ChatColor.WHITE + "Corners" + ChatColor.GRAY + ":");
			lore.add(ChatColor.GRAY + "- " + SPUtils.userFriendlyLocation(arena.getLowestCorner()));
			lore.add(ChatColor.GRAY + "- " + SPUtils.userFriendlyLocation(arena.getHighestCorner()));
			lore.add(ChatColor.WHITE + "Lobby" + ChatColor.GRAY + ": " + (arena.getLobby() == null ? "not set" : SPUtils.userFriendlyLocation(arena.getLobby())));
		}

		lore.add("");
		lore.add(ChatColor.DARK_GRAY + "» " + ChatColor.WHITE + "Click" + ChatColor.GRAY + " to " + (inThisArena ? "leave" : "select") + " this arena");

		final ItemStack arenaButton = GuiUtils.makeItem(Material.BOOK, ChatColor.RESET + "" + ChatColor.BOLD + arena.getName(), lore);

		if (inThisArena && Toggles.TOOLS_TOOLS_GLOW.get())
			GlowEffect.addGlow(arenaButton);

		return arenaButton;
	}

	@Override
	protected ItemStack getEmptyViewItem()
	{
		return GuiUtils.makeItem(Material.BARRIER, ChatColor.RED + "No arena has been created yet.", Arrays.asList(
				ChatColor.GRAY + "Ask your administrator to create one.",
				"",
				ChatColor.GRAY + "If you are the administrator, check out",
				ChatColor.GRAY + "the " + ChatColor.WHITE + "/spec arena" + ChatColor.GRAY + " command."
		));
	}

	@Override
	protected ItemStack getPickedUpItem(Arena arena)
	{
		final Spectator spectator = SpectatorPlus.get().getPlayerData(getPlayer());

		if (spectator.getArena() != null && spectator.getArena().equals(arena))
			spectator.setArena(null);
		else
			spectator.setArena(arena);

		close();
		return null;
	}

	@Override
	protected void onRightClick(Arena arena)
	{
		getPickedUpItem(arena);
	}
}
