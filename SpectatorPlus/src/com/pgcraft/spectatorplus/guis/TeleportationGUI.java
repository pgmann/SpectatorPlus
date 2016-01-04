/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.pgcraft.spectatorplus.guis;

import com.pgcraft.spectatorplus.SpectatorPlus;
import com.pgcraft.spectatorplus.Toggles;
import com.pgcraft.spectatorplus.arenas.Arena;
import com.pgcraft.spectatorplus.spectators.Spectator;
import fr.zcraft.zlib.components.gui.ExplorerGui;
import fr.zcraft.zlib.components.gui.Gui;
import fr.zcraft.zlib.components.gui.GuiUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TeleportationGUI extends ExplorerGui<Spectator>
{
	@Override
	protected void onUpdate()
	{
		List<Spectator> visiblePlayers = SpectatorPlus.get().getSpectatorsManager().getVisiblePlayersFor(SpectatorPlus.get().getPlayerData(getPlayer()));

		setData(visiblePlayers.toArray(new Spectator[visiblePlayers.size()]));
		setMode(Mode.READONLY);

		setTitle(ChatColor.BLACK + "Players " + ChatColor.RESET + "(" + visiblePlayers.size() + ")");
	}


	@Override
	protected ItemStack getEmptyViewItem()
	{
		String domain = "";
		switch (SpectatorPlus.get().getSpectatorsManager().getSpectatorsMode())
		{
			case ANY:
				domain = "server";
				break;

			case ARENA:
				domain = "arena";
				break;

			case WORLD:
				domain = "world";
				break;
		}

		return GuiUtils.makeItem(Material.BARRIER, ChatColor.RED + "There's no player to display!", Arrays.asList(
				ChatColor.GRAY + "Either all players on this " + domain,
				ChatColor.GRAY + "are currently spectating, or you're",
				ChatColor.GRAY + "not allowed to see some of them."
		));
	}

	@Override
	protected ItemStack getViewItem(Spectator sPlayer)
	{
		Player player = sPlayer.getPlayer();
		if (player == null)
			return GuiUtils.makeItem(Material.AIR);


		ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) head.getItemMeta();

		meta.setOwner(player.getName());

		if (sPlayer.isHiddenFromTp())
			meta.setDisplayName(ChatColor.DARK_GRAY + "[HIDDEN] " + ChatColor.RESET + player.getDisplayName());
		else
			meta.setDisplayName(ChatColor.RESET + player.getDisplayName());


		List<String> lore = new ArrayList<>();

		if (Toggles.TOOLS_TELEPORTER_HEALTH.get() || Toggles.TOOLS_TELEPORTER_LOCATION.get())
		{
			lore.add("");

			if (Toggles.TOOLS_TELEPORTER_HEALTH.get())
			{
				lore.add(ChatColor.GOLD + "" + ((int) player.getHealth()) + " " + ChatColor.WHITE + "hearts" + ChatColor.GRAY + " out of 20");
			}

			if (Toggles.TOOLS_TELEPORTER_LOCATION.get())
			{
				if (!player.getWorld().equals(getPlayer().getWorld()))
				{
					ChatColor worldColor = ChatColor.GRAY;
					switch (player.getWorld().getEnvironment())
					{
						case NORMAL:
							worldColor = ChatColor.DARK_GREEN;
							break;

						case NETHER:
							worldColor = ChatColor.RED;
							break;

						case THE_END:
							worldColor = ChatColor.LIGHT_PURPLE;
							break;
					}

					lore.add(ChatColor.GRAY + player.getName() + " is in the " + worldColor + player.getWorld().getName() + ChatColor.GRAY + " world.");
				}
				else
				{
					int distance = (int) player.getLocation().distance(getPlayer().getLocation());

					String direction = null;

					// The angle between a vector pointing to the North and a vector pointing
					// from the spectator to the player, converted in degrees, -180 to have 0° for North.
					double angle = (new Vector(0, 0, -1).angle(player.getLocation().toVector().setY(0).subtract(getPlayer().getLocation().toVector().setY(0)).multiply(-1)) * 180 / Math.PI - 180) % 360;
					if (angle < 0) angle += 360.0;

					// The calculated angle is the same for two positions symmetric of each other
					// relative to the N-S axis.
					// This lead to "west" displayed for both east and west.
					if (getPlayer().getLocation().getX() < player.getLocation().getX()
							&& 202.5 <= angle && angle < 337.5)
					{
						angle -= 180.0;
					}

					if (0 <= angle && angle < 22.5)
					{
						direction = "North";
					}
					else if (22.5 <= angle && angle < 67.5)
					{
						direction = "North-east";
					}
					else if (67.5 <= angle && angle < 112.5)
					{
						direction = "East";
					}
					else if (112.5 <= angle && angle < 157.5)
					{
						direction = "South-east";
					}
					else if (157.5 <= angle && angle < 202.5)
					{
						direction = "South";
					}
					else if (202.5 <= angle && angle < 247.5)
					{
						direction = "South-west";
					}
					else if (247.5 <= angle && angle < 292.5)
					{
						direction = "West";
					}
					else if (292.5 <= angle && angle < 337.5)
					{
						direction = "North-west";
					}
					else if (337.5 <= angle && angle <= 360.0)
					{
						direction = "North";
					}

					if (direction != null)
					{
						lore.add(ChatColor.WHITE + direction + ", " + distance + " meters");
					}
					else
					{
						lore.add(ChatColor.WHITE + "" + distance + " meters");
					}
				}
			}

			lore.add(""); // separator
		}

		lore.add(ChatColor.DARK_GRAY + "» " + ChatColor.WHITE + "Left click" + ChatColor.GRAY + " to be teleported");
		if (Toggles.TOOLS_TELEPORTER_INSPECTOR.get())
		{
			lore.add(ChatColor.DARK_GRAY + "» " + ChatColor.WHITE + "Right click" + ChatColor.GRAY + " to see the inventory");
		}

		meta.setLore(lore);
		head.setItemMeta(meta);

		return head;
	}


	@Override
	protected ItemStack getPickedUpItem(Spectator spectator)
	{
		Player clicked = spectator.getPlayer();

		if (clicked == null)
		{
			SpectatorPlus.get().sendMessage(getPlayer(), ChatColor.RED + "Cannot teleport you to that player because he cannot be found.", true);
			return null;
		}
		else
		{
			if (spectator.isSpectating())
			{
				SpectatorPlus.get().sendMessage(getPlayer(), ChatColor.RED + "Cannot teleport you to that player because he is currently spectating.", true);
				return null;
			}

			switch (SpectatorPlus.get().getSpectatorsManager().getSpectatorsMode())
			{
				case ARENA:
					Arena arena = SpectatorPlus.get().getPlayerData(getPlayer()).getArena();
					if (arena != null)
					{
						if (!arena.isInside(clicked.getLocation()))
						{
							SpectatorPlus.get().sendMessage(getPlayer(), ChatColor.RED + "Cannot teleport you to that player because he is not inside your arena.", true);
							return null;
						}
					}
					else
					{
						SpectatorPlus.get().sendMessage(getPlayer(), ChatColor.RED + "Cannot teleport you to that player because you are not in an arena.", true);
						return null;
					}
					break;

				case WORLD:
					if (!getPlayer().getWorld().equals(clicked.getWorld()))
					{
						SpectatorPlus.get().sendMessage(getPlayer(), ChatColor.RED + "Cannot teleport you to that player because he is not in the same world as you.", true);
						return null;
					}
					break;
			}
		}

		getPlayer().teleport(clicked);
		close();

		SpectatorPlus.get().sendMessage(getPlayer(), "Teleported you to " + ChatColor.RED + clicked.getDisplayName() + ChatColor.GOLD + ".");

		return null;
	}

	@Override
	protected void onRightClick(Spectator spectator)
	{
		final Player spectatorPlayer = spectator.getPlayer();

		if (spectatorPlayer != null && Toggles.TOOLS_TELEPORTER_INSPECTOR.get())
		{
			Gui.open(getPlayer(), new PlayerInventoryGUI(spectatorPlayer), this);
		}
	}
}
