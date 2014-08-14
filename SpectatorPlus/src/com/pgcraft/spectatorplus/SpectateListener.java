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
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

@SuppressWarnings("deprecation")
public class SpectateListener implements Listener {
	
	private SpectatorPlus plugin; // Pointer to main class (see SpectatorPlus.java)

	public SpectateListener(SpectatorPlus plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Used to:
	 *  - save the player in the internal list of players;
	 *  - set the internal scoreboard for this player;
	 *  - hide spectators from the joining player;
	 *  - enable the spectator mode if the player is registered as a spectator.
	 * 
	 * @param event
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		plugin.user.put(event.getPlayer().getName(), new PlayerObject());
		
		if (plugin.scoreboard) {
			event.getPlayer().setScoreboard(plugin.board);
		}
		
		for (Player target : plugin.getServer().getOnlinePlayers()) {
			if (plugin.user.get(target.getName()).spectating == true) {
				event.getPlayer().hidePlayer(target);
			}
		}
		
		if (plugin.specs.getConfig().contains(event.getPlayer().getName())) {
			plugin.enableSpectate(event.getPlayer(), (CommandSender) event.getPlayer());
		}
	}
	
	/**
	 * Used to hide chat messages sent by spectators, if the spectator chat is enabled.
	 * 
	 * @param event
	 */
	@EventHandler
	public void onChatSend(AsyncPlayerChatEvent event) {
		if (plugin.specChat) {
			if (plugin.user.get(event.getPlayer().getName()).spectating) {
				event.setCancelled(true);
				plugin.sendSpectatorMessage(event.getPlayer(), event.getMessage(), false);
			}
		}
	}
	
	/**
	 * Used to prevent spectators from placing blocks.
	 * 
	 * @param event
	 */
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (plugin.user.get(event.getPlayer().getName()).spectating) {
			event.setCancelled(true);
			if(plugin.output) {
				event.getPlayer().sendMessage(plugin.prefix + "You cannot place blocks while in spectate mode!");
			}
		}
	}
	
	/**
	 * Used to cancel any damage taken or caused by a spectator.
	 * 
	 * @param event
	 */
	@EventHandler
	public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
		// Manages spectators damaging players
		if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
			if (plugin.user.get(((Player) event.getDamager()).getName()).spectating || plugin.user.get(((Player) event.getEntity()).getName()).spectating) {
				event.setCancelled(true);
			}
		}
		
		// Manages spectators damaging mobs
		else if (event.getEntity() instanceof Player == false && event.getDamager() instanceof Player) {
			if (plugin.user.get(((Player) event.getDamager()).getName()).spectating == true) {
				event.setCancelled(true);
			}
		}
		
		// Manages mobs damaging spectators
		else if (event.getEntity() instanceof Player && event.getDamager() instanceof Player == false) {
			if (plugin.user.get(((Player) event.getEntity()).getName()).spectating == true) {
				event.setCancelled(true);
			}
		}
		
		// Otherwise both entities are mobs, ignore the event.
	}
	
	/**
	 * Used to:
	 *  - prevent spectator from breaking blocks;
	 *  - setup an arena, if the command was sent before by this player.
	 * 
	 * @param event
	 */
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		// Cancel if the player is a spectator. Fires only when the block is fully broken.
		if (plugin.user.get(event.getPlayer().getName()).spectating) {
			event.setCancelled(true);
			
			if(plugin.output) {
				event.getPlayer().sendMessage(plugin.prefix + "You cannot break blocks while in spectate mode!");
			}
		}
		
		// Set up mode
		if(plugin.arenaSetup(event.getPlayer(), event.getBlock())) {
			event.setCancelled(true);
		}
	}
	
	
	/**
	 * Used to prevent spectators from changing their gamemove whilst spectating.
	 * @param event
	 */
	@EventHandler(priority=EventPriority.HIGH)
	public void onGamemodeChange(PlayerGameModeChangeEvent event) {
		if (plugin.user.get(event.getPlayer().getName()).spectating && !event.getNewGameMode().equals(GameMode.ADVENTURE)) {
			event.setCancelled(true);
			event.getPlayer().setAllowFlight(true);
		}
	}
	
	
	/**
	 * Used to prevent spectators from dropping items on ground.
	 * 
	 * @param event
	 */
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		// On player drop item - Cancel if the player is a spectator
		if (plugin.user.get(event.getPlayer().getName()).spectating) {
			event.setCancelled(true);
		}
	}
	
	/**
	 * Used to prevent spectators from picking up items.
	 * 
	 * @param event
	 */
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if (plugin.user.get(event.getPlayer().getName()).spectating) {
			event.setCancelled(true);
		}
	}
	
	/**
	 * Used to prevent the mobs to be interested by (and aggressive against) spectators. 
	 * 
	 * @param event
	 */
	@EventHandler
	public void onEntityTarget(EntityTargetEvent event) {
		// On entity target - Stop mobs targeting spectators
		if (event.getTarget() != null && event.getTarget() instanceof Player && plugin.user.get(((Player) event.getTarget()).getName()).spectating) {
			event.setCancelled(true);
		}
	}
	
	/**
	 * Used to:
	 *  - prevent the damage block animation to be displayed, if the player is a spectator;
	 *  - setup an arena (if the command was sent before by the sender).
	 * 
	 * @param event
	 */
	@EventHandler
	public void onBlockDamage(BlockDamageEvent event) {
		// On block damage - Cancels the block damage animation
		if (plugin.user.get(event.getPlayer().getName()).spectating) {
			event.setCancelled(true);
			
			if(plugin.output) {
				event.getPlayer().sendMessage(plugin.prefix + "You cannot break blocks while in spectate mode!");
			}
		}
		
		// Set up mode
		if (plugin.arenaSetup(event.getPlayer(), event.getBlock())) {
			event.setCancelled(true);
		}
	}
	
	/**
	 * Used to:
	 *  - prevent players & mobs from damaging spectators;
	 *  - stop the fire display when a spectator go out of a fire/lava block.
	 * 
	 * @param event
	 */
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		// On entity damage - Stops users hitting players and mobs while spectating
		if (event.getEntity() instanceof Player && plugin.user.get(((Player) event.getEntity()).getName()).spectating) {
			event.setCancelled(true);
			event.getEntity().setFireTicks(0);
		}
		if (event.getEntity() instanceof Player && plugin.user.get(((Player) event.getEntity()).getName()).teleporting) {
			event.setCancelled(true);
			event.getEntity().setFireTicks(0);
		}
	}
	
	/**
	 * Used to prevent the food level to drop if the player is a spectator.
	 * 
	 * @param event
	 */
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (event.getEntityType() == EntityType.PLAYER && plugin.user.get(event.getEntity().getName()).spectating) {
			event.setCancelled(true);
			plugin.getServer().getPlayer(event.getEntity().getName()).setFoodLevel(20);
		}
	}
	
	/**
	 * Used to:
	 *  - removes the player from the internal scoreboard;
	 *  - disable the “hidden” state of the spectator;
	 *  - put the player back in the server's default gamemode;
	 *  - disable the spectator mode and reload the inventory, to avoid this inventory to be destroyed on server restart.
	 * 
	 * @param event
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
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
	
	/**
	 * Used to:
	 *  - display the various GUIs (teleportation, arenas) when the player right-click with the good item;
	 *  - prevent the player from opening chests, doors...
	 * 
	 * @param event
	 */
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (plugin.user.get(event.getPlayer().getName()).spectating == true && event.getMaterial() == Material.COMPASS && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			String mode = plugin.setup.getConfig().getString("mode"); 
			if (mode.equals("arena")) {
				int region = plugin.user.get(event.getPlayer().getName()).arenaNum;
				plugin.showGUI(event.getPlayer(), region);
			} else {
				plugin.showGUI(event.getPlayer(), 0);
			}
		}
		
		if (plugin.user.get(event.getPlayer().getName()).spectating == true && event.getMaterial() == Material.WATCH && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			event.setCancelled(true);
			plugin.showArenaGUI(event.getPlayer());
		}
		
		// Cancel chest opening, doors, anything when the player right clicks.
		if (plugin.user.get(event.getPlayer().getName()).spectating == true) {
			event.setCancelled(true);
		}
	}
	
	/**
	 * Used to:
	 *  - prevent a command to be executed if the player is a spectator and the option is set in the config;
	 *  - catch /me commands to show them into the spectator chat.
	 * 
	 * @param event
	 */
	@EventHandler
	public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if(plugin.specChat && event.getMessage().startsWith("/me") && plugin.user.get(event.getPlayer().getName()).spectating) {
			plugin.sendSpectatorMessage(event.getPlayer(), event.getMessage().substring(4), true);
			event.setCancelled(true);
			return;
		}
		
		if (plugin.blockCmds) {
			if (event.getPlayer().hasPermission("spectate.admin") && plugin.adminBypass) {
				// Do nothing
			} else if (!(event.getMessage().startsWith("/spec") || event.getMessage().startsWith("/spectate") || event.getMessage().startsWith("/me")) && plugin.user.get(event.getPlayer().getName()).spectating) {
				event.getPlayer().sendMessage(plugin.prefix+"Command blocked!");
				event.setCancelled(true);
			}
		}
	}
	
	/**
	 * Used to enable the spectator mode for dead players, if this option is enabled in the config.
	 * 
	 * @param event
	 */
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if(plugin.death) {
			// prevent murdering clients! (force close bug if spec mode is enabled instantly)
			new AfterRespawnTask(event.getPlayer(), plugin).runTaskLater(plugin, 20);
		}
	}
	
	/**
	 * Used to get the selected item in the various GUIs.
	 * 
	 * @param event
	 */
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (plugin.user.get(event.getWhoClicked().getName()).spectating) {
			// Teleportation GUI
			if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.SKULL_ITEM && event.getCurrentItem().getDurability() == 3) {
				ItemStack playerhead = event.getCurrentItem();
				SkullMeta meta = (SkullMeta)playerhead.getItemMeta();
				Player skullOwner = plugin.getServer().getPlayer(meta.getOwner());
				event.getWhoClicked().closeInventory();
				
				if (skullOwner != null && skullOwner.isOnline() && !plugin.user.get(skullOwner.getName()).spectating) {
					plugin.choosePlayer((Player) event.getWhoClicked(), skullOwner);
				}
				else {
					if (skullOwner == null) {
						OfflinePlayer offlineSkullOwner = plugin.getServer().getOfflinePlayer(meta.getOwner());
						((Player) event.getWhoClicked()).sendMessage(plugin.prefix + ChatColor.RED + offlineSkullOwner.getName() + ChatColor.GOLD + " is offline!");
					}
					else if (skullOwner.getAllowFlight() == true) {
						((Player) event.getWhoClicked()).sendMessage(plugin.prefix + ChatColor.RED + skullOwner.getName() + ChatColor.GOLD + " is currently spectating!");
					}
				}
			}
			
			// Manage showArenaGUI method selection
			if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.BOOK) {
				ItemStack arenaBook = event.getCurrentItem();
				ItemMeta meta = (ItemMeta)arenaBook.getItemMeta();
				String chosenArena = meta.getDisplayName();
				event.getWhoClicked().closeInventory();

				if (arenaBook != null) {
					plugin.setArenaForPlayer((Player) event.getWhoClicked(), chosenArena);
				}
			}
			
			// Cancel the event to prevent the item to be taken
			event.setCancelled(true);
		}
	}
}
