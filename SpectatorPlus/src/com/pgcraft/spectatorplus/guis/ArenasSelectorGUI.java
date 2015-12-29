/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
