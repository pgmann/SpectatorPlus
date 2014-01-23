package com.pgcraft.spectatorplus;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
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
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffectType;

public class SpectateListener implements Listener {
	private SpectatorPlus plugin; // pointer to your main class, unrequired if you don't need methods from the main class
	 
	public SpectateListener(SpectatorPlus plugin) {
		this.plugin = plugin;
	}
	@EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
    	// On player join - hide spectators from the joining player
    	plugin.user.put(event.getPlayer().getName(), new PlayerObject());
		for (Player target : plugin.getServer().getOnlinePlayers()) {
			if (plugin.user.get(target.getName()).spectating == true) {
				event.getPlayer().hidePlayer(target);
			}
		}
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // On block place - cancel if the player is a spectator
    	if (plugin.user.get(event.getPlayer().getName()).spectating == true) {
    		event.setCancelled(true);
    		event.getPlayer().sendMessage(plugin.prefix + "You cannot place blocks while in spectate mode!");
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
    		event.getPlayer().sendMessage(plugin.prefix + "You cannot break blocks while in spectate mode!");
    	}
    	// Set up mode
    	if(plugin.modeSetup(event.getPlayer(), event.getBlock()) == true) {
    		event.setCancelled(true);
    	}
    }
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onGamemodeChange(PlayerGameModeChangeEvent event) {
    	if (plugin.user.get(event.getPlayer().getName()).spectating && !event.getNewGameMode().equals(GameMode.ADVENTURE)) {
    		event.setCancelled(true);
    		event.getPlayer().setAllowFlight(true);
    	}
    }
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
    	if (plugin.user.get(event.getPlayer().getName()).spectating == true) {
    		event.getPlayer().setAllowFlight(true);
    		event.getPlayer().setGameMode(GameMode.ADVENTURE);
    	}
    }
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        // On player drop item - Cancel if the player is a spectator
    	if (plugin.user.get(event.getPlayer().getName()).spectating == true) {
    		event.setCancelled(true);
    	}
    }
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        // On player pickup item - Cancel if the player is a spectator
    	if (plugin.user.get(event.getPlayer().getName()).spectating == true) {
    		event.setCancelled(true);
    	}
    }
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        // On entity target - Stop mobs targeting spectators
    	if (event.getTarget() != null && event.getTarget() instanceof Player && plugin.user.get(((Player) event.getTarget()).getName()).spectating) {
    		event.setCancelled(true);
    	}
    }
    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        // On block damage - Cancels the block damage animation
    	if (plugin.user.get(event.getPlayer().getName()).spectating == true) {
    		event.setCancelled(true);
    		event.getPlayer().sendMessage(plugin.prefix + "You cannot break blocks while in spectate mode!");
    	}
    	// Set up mode
    	if (plugin.modeSetup(event.getPlayer(), event.getBlock()) == true) {
    		event.setCancelled(true);
    	}
    }
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        // On block damage - Cancels the block damage animation
    	if (event.getEntity() instanceof Player && plugin.user.get(((Player) event.getEntity()).getName()).spectating == true) {
    		event.setCancelled(true);
    		event.getEntity().setFireTicks(0);
    	}
    	if (event.getEntity() instanceof Player && plugin.user.get(((Player) event.getEntity()).getName()).teleporting == true) {
    		event.setCancelled(true);
    		event.getEntity().setFireTicks(0);
    	}
    }
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        // On food loss - Cancels the food loss
    	if (event.getEntityType() == EntityType.PLAYER && plugin.user.get(event.getEntity().getName()).spectating == true) {
    		event.setCancelled(true);
    		plugin.getServer().getPlayer(event.getEntity().getName()).setFoodLevel(20);
    	}
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
    	Player spectator = event.getPlayer();
		for (Player target : plugin.getServer().getOnlinePlayers()) {
			target.showPlayer(event.getPlayer()); // show the leaving player to everyone
			event.getPlayer().showPlayer(target); // show the leaving player everyone
		}
		if (plugin.user.get(spectator.getName()).spectating) {
			plugin.user.get(spectator.getName()).spectating = false;
			spectator.setGameMode(plugin.getServer().getDefaultGameMode());
			plugin.spawnPlayer(spectator);
			event.getPlayer().removePotionEffect(PotionEffectType.HEAL);
			plugin.loadPlayerInv(spectator);
			plugin.user.remove(spectator.getName());
		}
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
		if (plugin.user.get(event.getPlayer().getName()).spectating == true && event.getMaterial() == Material.COMPASS && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			String mode = plugin.getConfig().getString("mode"); 
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
    public void onInventoryClick(InventoryClickEvent event) {
		if (plugin.user.get(event.getWhoClicked().getName()).spectating == true) {
			if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.SKULL_ITEM && event.getCurrentItem().getDurability() == 3) {
				ItemStack playerhead = event.getCurrentItem();
				SkullMeta meta = (SkullMeta)playerhead.getItemMeta();
				Player skullOwner = plugin.getServer().getPlayer(meta.getOwner());
				event.getWhoClicked().closeInventory();
				if (skullOwner != null && skullOwner.isOnline() == true && skullOwner.getAllowFlight() == false) {
					event.getWhoClicked().teleport(skullOwner);
                   	((Player)event.getWhoClicked()).sendMessage(plugin.prefix + "Teleported you to " + ChatColor.RED + skullOwner.getName());
				} else {
					if (skullOwner == null) {
						OfflinePlayer offlineSkullOwner = plugin.getServer().getOfflinePlayer(meta.getOwner());
						((Player) event.getWhoClicked()).sendMessage(plugin.prefix + ChatColor.RED + offlineSkullOwner.getName() + ChatColor.GOLD + " is not online!");
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
					for (int i=1; i<plugin.getConfig().getInt("nextarena"); i++) {
						if (plugin.getConfig().getString("arena." + i + ".name") == chosenArena) {
							plugin.user.get(event.getWhoClicked().getName()).arenaNum = i;
						}
					}
					double tpPosY = plugin.getConfig().getDouble("arena." + plugin.user.get(event.getWhoClicked().getName()).arenaNum + ".lobby.y", event.getWhoClicked().getLocation().getY());
					double tpPosX = plugin.getConfig().getDouble("arena." + plugin.user.get(event.getWhoClicked().getName()).arenaNum + ".lobby.x", event.getWhoClicked().getLocation().getX());
					double tpPosZ = plugin.getConfig().getDouble("arena." + plugin.user.get(event.getWhoClicked().getName()).arenaNum + ".lobby.z", event.getWhoClicked().getLocation().getZ());
					World tpWorld = plugin.getServer().getWorld(plugin.getConfig().getString("arena." + plugin.user.get(event.getWhoClicked().getName()).arenaNum + ".lobby.world", event.getWhoClicked().getWorld().getName()));
					Location where = new Location(tpWorld, tpPosX, tpPosY, tpPosZ);

					if (tpWorld == event.getWhoClicked().getWorld()) {
						double dist = where.distance(event.getWhoClicked().getLocation());
						if (dist <= 1) {
							((Player)event.getWhoClicked()).sendMessage(plugin.prefix + "No lobby location set for " + ChatColor.RED + chosenArena);
						} else {
							((Player)event.getWhoClicked()).sendMessage(plugin.prefix + "Teleported you to " + ChatColor.RED + chosenArena);
							event.getWhoClicked().teleport(where);
						}
					} else {
						((Player)event.getWhoClicked()).sendMessage(plugin.prefix + "Teleported you to " + ChatColor.RED + chosenArena);
						event.getWhoClicked().teleport(where);
					}
					

				}
			}
			event.setCancelled(true);
		}
    }
}
