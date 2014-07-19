package com.pgcraft.spectatorplus;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

@SuppressWarnings("deprecation")
public class SpectateListener implements Listener {
	private SpectatorPlus plugin; // pointer to your main class, unrequired if you don't need methods from the main class

	public SpectateListener(SpectatorPlus plugin) {
		this.plugin = plugin;
	}
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		// On player join - hide spectators from the joining player
		plugin.user.put(event.getPlayer().getName(), new PlayerObject());
		if (plugin.scoreboard) {event.getPlayer().setScoreboard(plugin.board);}
		for (Player target : plugin.getServer().getOnlinePlayers()) {
			if (plugin.user.get(target.getName()).spectating == true) {
				event.getPlayer().hidePlayer(target);
			}
		}
		if (plugin.specs.getConfig().contains(event.getPlayer().getName())) {
			plugin.enableSpectate(event.getPlayer(), (CommandSender) event.getPlayer());
		}
	}
	@EventHandler
	public void onChatSend(AsyncPlayerChatEvent event) {
		if (plugin.specChat) {
			if (plugin.user.get(event.getPlayer().getName()).spectating) {
				event.setCancelled(true);
				for (Player player : plugin.getServer().getOnlinePlayers()) {
					if(plugin.user.get(player.getName()).spectating) {
						player.sendMessage(ChatColor.GRAY + "[SPEC] " + event.getPlayer().getDisplayName() + ChatColor.GRAY + ": " + event.getMessage());
					}
				}
				plugin.console.sendMessage(ChatColor.GRAY + "[SPEC] " + event.getPlayer().getDisplayName() + ChatColor.GRAY + ": " + event.getMessage());
			}
		}
	}
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		// On block place - cancel if the player is a spectator
		if (plugin.user.get(event.getPlayer().getName()).spectating == true) {
			event.setCancelled(true);
			if(plugin.output) {event.getPlayer().sendMessage(plugin.prefix + "You cannot place blocks while in spectate mode!");}
		}
	}
	@EventHandler
	public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
		// On entity hit - are they a spectator or were they hit by a spectator?
		if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
			if (plugin.user.get(((Player) event.getDamager()).getName()).spectating || plugin.user.get(((Player) event.getEntity()).getName()).spectating) {
				event.setCancelled(true);
			}
			// Manage spectators damaging mobs
		} else if (event.getEntity() instanceof Player == false && event.getDamager() instanceof Player) {
			if (plugin.user.get(((Player) event.getDamager()).getName()).spectating == true) {
				event.setCancelled(true);
			}
			// Manage mobs damaging spectators
		} else if (event.getEntity() instanceof Player && event.getDamager() instanceof Player == false) {
			if (plugin.user.get(((Player) event.getEntity()).getName()).spectating == true) {
				event.setCancelled(true);
			}
		}
		// Otherwise both entities are mobs, ignore the event.
	}
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		// On block break - Cancel if the player is a spectator. Fires only when the block is fully broken
		if (plugin.user.get(event.getPlayer().getName()).spectating == true) {
			event.setCancelled(true);
			if(plugin.output) {event.getPlayer().sendMessage(plugin.prefix + "You cannot break blocks while in spectate mode!");}
		}
		// Set up mode
		if(plugin.modeSetup(event.getPlayer(), event.getBlock()) == true) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority=EventPriority.HIGH)
	public void onGamemodeChange(PlayerGameModeChangeEvent event) {
		if (plugin.user.get(event.getPlayer().getName()).spectating && !event.getNewGameMode().equals(GameMode.ADVENTURE)) {
			event.setCancelled(true);
			event.getPlayer().setAllowFlight(true);
		}
	}
	@EventHandler(priority=EventPriority.HIGHEST)
	void onPlayerMove(PlayerMoveEvent event) {
		if (plugin.user.get(event.getPlayer().getName()).spectating) {
			event.getPlayer().setAllowFlight(true);
			event.getPlayer().setGameMode(GameMode.ADVENTURE);
		}
	}
	@EventHandler
	void onPlayerDropItem(PlayerDropItemEvent event) {
		// On player drop item - Cancel if the player is a spectator
		if (plugin.user.get(event.getPlayer().getName()).spectating) {
			event.setCancelled(true);
		}
	}
	@EventHandler
	void onPlayerPickupItem(PlayerPickupItemEvent event) {
		// On player pickup item - Cancel if the player is a spectator
		if (plugin.user.get(event.getPlayer().getName()).spectating) {
			event.setCancelled(true);
		}
	}
	@EventHandler
	void onEntityTarget(EntityTargetEvent event) {
		// On entity target - Stop mobs targeting spectators
		if (event.getTarget() != null && event.getTarget() instanceof Player && plugin.user.get(((Player) event.getTarget()).getName()).spectating) {
			event.setCancelled(true);
		}
	}
	@EventHandler
	void onBlockDamage(BlockDamageEvent event) {
		// On block damage - Cancels the block damage animation
		if (plugin.user.get(event.getPlayer().getName()).spectating) {
			event.setCancelled(true);
			if(plugin.output) {event.getPlayer().sendMessage(plugin.prefix + "You cannot break blocks while in spectate mode!");}
		}
		// Set up mode
		if (plugin.modeSetup(event.getPlayer(), event.getBlock())) {
			event.setCancelled(true);
		}
	}
	@EventHandler
	void onEntityDamage(EntityDamageEvent event) {
		// On block damage - Cancels the block damage animation
		if (event.getEntity() instanceof Player && plugin.user.get(((Player) event.getEntity()).getName()).spectating) {
			event.setCancelled(true);
			event.getEntity().setFireTicks(0);
		}
		if (event.getEntity() instanceof Player && plugin.user.get(((Player) event.getEntity()).getName()).teleporting) {
			event.setCancelled(true);
			event.getEntity().setFireTicks(0);
		}
	}
	@EventHandler
	void onFoodLevelChange(FoodLevelChangeEvent event) {
		// On food loss - Cancels the food loss
		if (event.getEntityType() == EntityType.PLAYER && plugin.user.get(event.getEntity().getName()).spectating) {
			event.setCancelled(true);
			plugin.getServer().getPlayer(event.getEntity().getName()).setFoodLevel(20);
		}
	}
	@EventHandler
	void onPlayerQuit(PlayerQuitEvent event) {
		Player spectator = event.getPlayer();
		if (plugin.scoreboard) {
			plugin.team.removePlayer(spectator);
		}
		for (Player target : plugin.getServer().getOnlinePlayers()) {
			target.showPlayer(event.getPlayer()); // show the leaving player to everyone
			event.getPlayer().showPlayer(target); // show the leaving player everyone
		}
		if (plugin.user.get(spectator.getName()).spectating) {
			plugin.user.get(spectator.getName()).spectating = false;
			spectator.setGameMode(plugin.getServer().getDefaultGameMode());
			plugin.spawnPlayer(spectator);
			plugin.loadPlayerInv(spectator);
			plugin.user.remove(spectator.getName());
		}
	}
	@EventHandler
	void onPlayerInteract(PlayerInteractEvent event) {
		if (plugin.user.get(event.getPlayer().getName()).spectating == true && event.getMaterial() == Material.COMPASS && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			String mode = plugin.setup.getConfig().getString("mode"); 
			if (mode.equals("arena")) {
				int region = plugin.user.get(event.getPlayer().getName()).arenaNum;
				plugin.tpPlayer(event.getPlayer(), region);
			} else {
				plugin.tpPlayer(event.getPlayer(), 0);
			}
		}
		if (plugin.user.get(event.getPlayer().getName()).spectating == true && event.getMaterial() == Material.WATCH && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			event.setCancelled(true);
			plugin.arenaSelect(event.getPlayer());
		}
		// Cancel chest opening, doors, anything when the player right clicks.
		if (plugin.user.get(event.getPlayer().getName()).spectating == true) {
			event.setCancelled(true);
		}
	}
	@EventHandler
	void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (plugin.blockCmds) {
			if (event.getPlayer().hasPermission("spectate.admin")&&plugin.adminBypass) {
				// Do nothing
			} else if (!(event.getMessage().startsWith("/spec")||event.getMessage().startsWith("/spectate")) && plugin.user.get(event.getPlayer().getName()).spectating) {
				event.getPlayer().sendMessage(plugin.prefix+"Command blocked!");
				event.setCancelled(true);
			}
		}
	}
	@EventHandler
	void onPlayerRespawn(PlayerRespawnEvent event) {
		if(plugin.death) {
			// prevent murdering clients! (force close bug if spec mode is enabled instantly)
			new AfterRespawnTask(event.getPlayer(), plugin).runTaskLater(plugin, 20);
		}
	}
	@EventHandler
	void onInventoryClick(InventoryClickEvent event) {
		if (plugin.user.get(event.getWhoClicked().getName()).spectating) {
			if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.SKULL_ITEM && event.getCurrentItem().getDurability() == 3) {
				ItemStack playerhead = event.getCurrentItem();
				SkullMeta meta = (SkullMeta)playerhead.getItemMeta();
				Player skullOwner = plugin.getServer().getPlayer(meta.getOwner());
				event.getWhoClicked().closeInventory();
				if (skullOwner != null && skullOwner.isOnline() && !plugin.user.get(skullOwner.getName()).spectating) {
					plugin.choosePlayer((Player) event.getWhoClicked(), skullOwner);
				} else {
					if (skullOwner == null) {
						OfflinePlayer offlineSkullOwner = plugin.getServer().getOfflinePlayer(meta.getOwner());
						((Player) event.getWhoClicked()).sendMessage(plugin.prefix + ChatColor.RED + offlineSkullOwner.getName() + ChatColor.GOLD + " is offline!");
					} else if (skullOwner.getAllowFlight() == true) {
						((Player) event.getWhoClicked()).sendMessage(plugin.prefix + ChatColor.RED + skullOwner.getName() + ChatColor.GOLD + " is currently spectating!");
					}
				}
			}
			//manage arenaSelect choice
			if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.BOOK) {
				ItemStack arenaBook = event.getCurrentItem();
				ItemMeta meta = (ItemMeta)arenaBook.getItemMeta();
				String chosenArena = meta.getDisplayName();
				event.getWhoClicked().closeInventory();

				if (arenaBook != null) {
					// find out the arena id #
					for (int i=1; i<plugin.setup.getConfig().getInt("nextarena"); i++) {
						if (plugin.setup.getConfig().getString("arena." + i + ".name") == chosenArena) {
							plugin.user.get(event.getWhoClicked().getName()).arenaNum = i;
						}
					}
					double tpPosY = plugin.setup.getConfig().getDouble("arena." + plugin.user.get(event.getWhoClicked().getName()).arenaNum + ".lobby.y", event.getWhoClicked().getLocation().getY());
					double tpPosX = plugin.setup.getConfig().getDouble("arena." + plugin.user.get(event.getWhoClicked().getName()).arenaNum + ".lobby.x", event.getWhoClicked().getLocation().getX());
					double tpPosZ = plugin.setup.getConfig().getDouble("arena." + plugin.user.get(event.getWhoClicked().getName()).arenaNum + ".lobby.z", event.getWhoClicked().getLocation().getZ());
					World tpWorld = plugin.getServer().getWorld(plugin.setup.getConfig().getString("arena." + plugin.user.get(event.getWhoClicked().getName()).arenaNum + ".lobby.world", event.getWhoClicked().getWorld().getName()));
					Location where = new Location(tpWorld, tpPosX, tpPosY, tpPosZ);

					if (tpWorld == event.getWhoClicked().getWorld()) {
						double dist = where.distance(event.getWhoClicked().getLocation());
						if (dist <= 1) {
							((Player)event.getWhoClicked()).sendMessage(plugin.prefix + "No lobby location set for " + ChatColor.RED + chosenArena);
						} else {
							if(plugin.output) {((Player)event.getWhoClicked()).sendMessage(plugin.prefix + "Teleported you to " + ChatColor.RED + chosenArena);}
							event.getWhoClicked().teleport(where);
						}
					} else {
						if(plugin.output) {((Player)event.getWhoClicked()).sendMessage(plugin.prefix + "Teleported you to " + ChatColor.RED + chosenArena);}
						event.getWhoClicked().teleport(where);
					}


				}
			}
			event.setCancelled(true);
		}
	}
}
