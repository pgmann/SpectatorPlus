package com.pgcraft.spectatorplus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.Gate;
import org.bukkit.material.TrapDoor;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

@SuppressWarnings("deprecation")
public class SpectateListener implements Listener {
	
	private SpectatorPlus plugin; // Pointer to main class (see SpectatorPlus.java)

	protected SpectateListener(SpectatorPlus plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Used to:<br>
	 *  - save the player in the internal list of players;<br>
	 *  - set the internal scoreboard for this player;<br>
	 *  - hide spectators from the joining player;<br>
	 *  - enable the spectator mode if the player is registered as a spectator.
	 * 
	 * @param event
	 */
	@EventHandler(priority=EventPriority.HIGH)
	protected void onPlayerJoin(PlayerJoinEvent event) {
		
		if(!plugin.user.containsKey(event.getPlayer().getName())) {
			plugin.user.put(event.getPlayer().getName(), new PlayerObject());
		}
		
		for (Player target : plugin.getServer().getOnlinePlayers()) {
			if (plugin.getPlayerData(target).spectating) {
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
	// Ignore cancelled, so another plugin can implement a private chat without conflicts.
	@EventHandler(ignoreCancelled = true)
	protected void onChatSend(AsyncPlayerChatEvent event) {
		if (plugin.specChat) {
			if (plugin.getPlayerData(event.getPlayer()).spectating) {
				event.setCancelled(true);
				plugin.sendSpectatorMessage(event.getPlayer(), event.getMessage(), false);
			}
		}
	}
	
	/**
	 * Used to prevent spectators from blocking players from placing blocks.
	 * 
	 * @param event
	 */
	@EventHandler
	protected void onBlockCanBuild(BlockCanBuildEvent event) {
		if (!event.isBuildable()) {
			
			// Get location of the block that is going to be placed 
			Location blockL = event.getBlock().getLocation(); // event.getBlock() returns the block to be placed -1 y
			
			boolean allowed = false; // If there are any actual players there, the event should not be over-wrote.
			
			for (Player target : plugin.getServer().getOnlinePlayers()) {
				if (target.getWorld().equals(event.getBlock().getWorld())) { // Player in same world?
					Location playerL = target.getLocation();
					
					if (playerL.getX() > blockL.getBlockX()-1 && playerL.getX() < blockL.getBlockX()+1) { //2d...
						if (playerL.getZ() > blockL.getBlockZ()-1 && playerL.getZ() < blockL.getBlockZ()+1) { // 2d (x & z)
							if (playerL.getY() > blockL.getBlockY()-2 && playerL.getY() < blockL.getBlockY()+1) { // 3d (y): for feet & head height
								if (plugin.getPlayerData(target).spectating) allowed = true;
								else {
									allowed = false;
									break;
								}
							}
						}
					}
					
				}
			}
			event.setBuildable(allowed);
		}
	}
	
	/**
	 * Used to prevent spectators from placing blocks, and teleport spectators blocking players from placing blocks.
	 * 
	 * @param event
	 */
	@EventHandler(priority=EventPriority.HIGHEST) // This event is the last to be executed, as lower priorities are executed first.
	protected void onBlockPlace(BlockPlaceEvent event) {
		if (plugin.getPlayerData(event.getPlayer()).spectating) {
			event.setCancelled(true);
			if(plugin.output) {
				event.getPlayer().sendMessage(SpectatorPlus.prefix + "You cannot place blocks while in spectate mode!");
			}
		}
		
		// Get location of the block that is going to be placed 
		Location blockL = event.getBlock().getLocation();

		for (Player target : plugin.getServer().getOnlinePlayers()) {
			if (plugin.getPlayerData(target).spectating && target.getWorld().equals(event.getBlock().getWorld())) { // Player spectating & in same world?
				Location playerL = target.getLocation();

				if (playerL.getX() > blockL.getBlockX()-1 && playerL.getX() < blockL.getBlockX()+1) { //2d...
					if (playerL.getZ() > blockL.getBlockZ()-1 && playerL.getZ() < blockL.getBlockZ()+1) { // 2d (x & z)
						if (playerL.getY() > blockL.getBlockY()-2 && playerL.getY() < blockL.getBlockY()+1) { // 3d (y): for feet & head height
							target.teleport(event.getPlayer(), TeleportCause.PLUGIN);
							target.sendMessage(SpectatorPlus.prefix + "You were teleported away from a placed block.");
						}
					}
				}
				
			}
		}
	}
	
	/**
	 * Used to:<br>
	 *  - cancel any damage taken or caused by a spectator;<br>
	 *  - make non-potions projectiles flew by the spectators.
	 * 
	 * @param event
	 */
	@EventHandler
	protected void onEntityDamageEvent(final EntityDamageByEntityEvent event) {		
		
		/** Cancels damages involving spectators **/
		
		// Manages spectators damaging players
		if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
			if ((!event.getDamager().hasMetadata("NPC") && plugin.getPlayerData(((Player) event.getDamager())).spectating) || (!event.getEntity().hasMetadata("NPC") && plugin.getPlayerData(((Player) event.getEntity())).spectating)) {
				event.setCancelled(true);
			}
		// Manage spectators damaging mobs
		} else if (!(event.getEntity() instanceof Player) && event.getDamager() instanceof Player) {
			if (!event.getDamager().hasMetadata("NPC") && plugin.getPlayerData(((Player) event.getDamager())).spectating == true) {
				event.setCancelled(true);
			}
		// Manage mobs damaging spectators
		} else if (event.getEntity() instanceof Player && !(event.getDamager() instanceof Player)) {
			if (!event.getEntity().hasMetadata("NPC") && plugin.getPlayerData(((Player) event.getEntity())).spectating == true) {
				event.setCancelled(true);
			}
		}
		
		// Otherwise both entities are mobs, ignore the event.
		
		
		/** Make projectiles fly through spectators **/
		
		if(event.getDamager() instanceof Projectile
				&& !(event.getDamager() instanceof ThrownPotion) // splash potions are cancelled in PotionSplashEvent
				&& event.getEntity() instanceof Player
				&& !event.getEntity().hasMetadata("NPC") // Check for NPC's, as they are instances of Players sometimes...
				&& plugin.getPlayerData(((Player) event.getEntity())).spectating) {
			
			event.setCancelled(true);
			
			final Player spectatorInvolved = (Player) event.getEntity();
			final boolean wasFlying = spectatorInvolved.isFlying();
			final Location initialSpectatorLocation = spectatorInvolved.getLocation();
			
			final Vector initialProjectileVelocity = event.getDamager().getVelocity();
			final Location initialProjectileLocation = event.getDamager().getLocation();
			
			spectatorInvolved.setFlying(true);
			spectatorInvolved.teleport(initialSpectatorLocation.clone().add(0, 6, 0), TeleportCause.PLUGIN);
			
			// Prevents the arrow from bouncing on the entity
			Bukkit.getScheduler().runTaskLater(plugin, new BukkitRunnable() {
				@Override
				public void run() {
					event.getDamager().teleport(initialProjectileLocation);
					event.getDamager().setVelocity(initialProjectileVelocity);
				}
			}, 1L);
			
			// Teleports back the spectator
			Bukkit.getScheduler().runTaskLater(plugin, new BukkitRunnable() {
				@Override
				public void run() {
					spectatorInvolved.teleport(new Location(initialSpectatorLocation.getWorld(), initialSpectatorLocation.getX(), initialSpectatorLocation.getY(), initialSpectatorLocation.getZ(), spectatorInvolved.getLocation().getYaw(), spectatorInvolved.getLocation().getPitch()), TeleportCause.PLUGIN);
					spectatorInvolved.setFlying(wasFlying);
				}
			}, 5L);
		}
	}
	
	/**
	 * Used to make splash potions flew by the spectators.
	 * 
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	protected void onPotionSplash(final PotionSplashEvent event) {
		
		final ArrayList<UUID> spectatorsAffected = new ArrayList<UUID>();
		
		for(LivingEntity player : event.getAffectedEntities()) {
			if(player instanceof Player && !player.hasMetadata("NPC") && plugin.getPlayerData(((Player) player)).spectating) {
				spectatorsAffected.add(player.getUniqueId());
			}
		}
		
		// If there isn't any spectator affected, it's a splash on players only and the spectators cannot
		// affect the behavior of the potion.
		// So, in this case, we don't care about the event.
		if(!spectatorsAffected.isEmpty()) {
			
			// If there are some spectators involved, we try to find how they are involved.
			// If all the spectators involved are far away from the impact point, there isn't
			// any needed action.
			// Else, if a spectator is the impact point, he perturbed the launch of the potion, and
			// the same thing is done as for the non-potions projectiles (teleport the spectators up, etc.).
			// In all cases, the effect is removed from the spectators.
			
			Boolean teleportationNeeded = false;
			
			for(Entity entity : event.getEntity().getNearbyEntities(2, 2, 2)) {
				if(entity instanceof Player && !entity.hasMetadata("NPC") && plugin.getPlayerData(((Player) entity)).spectating) {
					// The potion hits a spectator
					teleportationNeeded = true;
				}
			}
			
			final HashMap<UUID,Boolean> oldFlyMode = new HashMap<UUID,Boolean>(); 
			
			for(UUID spectatorUUID : spectatorsAffected) {
				
				Player spectator = plugin.getServer().getPlayer(spectatorUUID);
				
				// The effect is removed
				event.setIntensity(spectator, 0);
				
				if(teleportationNeeded) {
					oldFlyMode.put(spectator.getUniqueId(), spectator.isFlying());
					spectator.setFlying(true);

					// High teleportation because the potions can be thrown up
					spectator.teleport(spectator.getLocation().add(0, 10, 0));
				}
			}
			
			if(teleportationNeeded) {
				
				final Location initialProjectileLocation = event.getEntity().getLocation();
				final Vector initialProjectileVelocity = event.getEntity().getVelocity();
				
				// Prevents the potion from splashing on the entity
				plugin.getServer().getScheduler().runTaskLater(plugin, new BukkitRunnable() {
					@Override
					public void run() {
						// Because the original entity is, one tick later, destroyed, we need to spawn a new one
						// Cancelling the event only cancels the effect.
						ThrownPotion clonedEntity = (ThrownPotion) event.getEntity().getWorld().spawnEntity(initialProjectileLocation, event.getEntity().getType()); 
						
						// For other plugins (may be used)
						clonedEntity.setShooter(event.getEntity().getShooter());
						clonedEntity.setTicksLived(event.getEntity().getTicksLived());
						clonedEntity.setFallDistance(event.getEntity().getFallDistance());
						clonedEntity.setBounce(event.getEntity().doesBounce());
						if(event.getEntity().getPassenger() != null) {
							clonedEntity.setPassenger(event.getEntity().getPassenger()); // hey, why not
						}
						
						// Clones the effects
						clonedEntity.setItem(event.getEntity().getItem());
						
						// Clones the speed/direction
						clonedEntity.setVelocity(initialProjectileVelocity);
						
						// Just in case
						event.getEntity().remove();
					}
				}, 1L);
				
				// Teleports back the spectators
				plugin.getServer().getScheduler().runTaskLater(plugin, new BukkitRunnable() {
					@Override
					public void run() {
						for(UUID spectatorUUID : spectatorsAffected) {
							Player spectator = plugin.getServer().getPlayer(spectatorUUID);
							
							spectator.teleport(spectator.getLocation().add(0, -10, 0));
							spectator.setFlying(oldFlyMode.get(spectatorUUID));
						}
					}
				}, 5L);
				
				// Cancels the effect for everyone (because the thrown potion is re-spawned,
				// avoids a double effect for some players).
				event.setCancelled(true);
				
				// Side note: there is a visual glitch (the players will see a double splash,
				// the real one plus the splash on the spectator), but the behavior is preserved and
				// the effect is applied once, on the players.
			}	
		}
	}
	
	/**
	 * Used to:<br>
	 *  - prevent spectator from breaking blocks;<br>
	 *  - setup an arena, if the command was sent before by this player.
	 * 
	 * @param event
	 */
	@EventHandler
	protected void onBlockBreak(BlockBreakEvent event) {
		// Cancel if the player is a spectator. Fires only when the block is fully broken.
		if (plugin.getPlayerData(event.getPlayer()).spectating) {
			event.setCancelled(true);
			
			if(plugin.output) {
				event.getPlayer().sendMessage(SpectatorPlus.prefix + "You cannot break blocks while in spectate mode!");
			}
		}
		
		// Set up mode
		if(plugin.arenaSetup(event.getPlayer(), event.getBlock())) {
			event.setCancelled(true);
		}
	}
	
	
	/**
	 * Used to prevent spectators from changing their gamemode whilst spectating.
	 * 
	 * @param event
	 */
	@EventHandler(priority=EventPriority.HIGH)
	protected void onGamemodeChange(PlayerGameModeChangeEvent event) {
		if (plugin.getPlayerData(event.getPlayer()) != null && plugin.getPlayerData(event.getPlayer()).spectating && !event.getNewGameMode().equals(GameMode.ADVENTURE)) {
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
	protected void onPlayerDropItem(PlayerDropItemEvent event) {
		// On player drop item - Cancel if the player is a spectator
		if (plugin.getPlayerData(event.getPlayer()).spectating) {
			event.setCancelled(true);
		}
	}
	
	/**
	 * Used to prevent spectators from picking up items.
	 * 
	 * @param event
	 */
	@EventHandler
	protected void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if (plugin.getPlayerData(event.getPlayer()).spectating) {
			event.setCancelled(true);
		}
	}
	
	/**
	 * Used to prevent the mobs to be interested by (and aggressive against) spectators. 
	 * 
	 * @param event
	 */
	@EventHandler
	protected void onEntityTarget(EntityTargetEvent event) {
		// On entity target - Stop mobs targeting spectators
		// Check to make sure it isn't an NPC (Citizens NPC's will be detectable using 'entity.hasMetadata("NPC")')
		if (event.getTarget() != null && event.getTarget() instanceof Player && !event.getTarget().hasMetadata("NPC") && plugin.getPlayerData(((Player) event.getTarget())).spectating) {
			event.setCancelled(true);
		}
	}
	
	/**
	 * Used to:<br>
	 *  - prevent the damage block animation to be displayed, if the player is a spectator;<br>
	 *  - setup an arena (if the command was sent before by the sender).
	 * 
	 * @param event
	 */
	@EventHandler
	protected void onBlockDamage(BlockDamageEvent event) {
		// On block damage - Cancels the block damage animation
		if (plugin.getPlayerData(event.getPlayer()).spectating) {
			event.setCancelled(true);
			
			if(plugin.output) {
				event.getPlayer().sendMessage(SpectatorPlus.prefix + "You cannot break blocks while in spectate mode!");
			}
		}
		
		// Set up mode
		if (plugin.arenaSetup(event.getPlayer(), event.getBlock())) {
			event.setCancelled(true);
		}
	}
	
	/**
	 * Used to:<br>
	 *  - prevent players & mobs from damaging spectators;<br>
	 *  - stop the fire display when a spectator go out of a fire/lava block.
	 * 
	 * @param event
	 */
	@EventHandler
	protected void onEntityDamage(EntityDamageEvent event) {
		// On entity damage - Stops users hitting players and mobs while spectating
		// Check to make sure it isn't an NPC (Citizens NPC's will be detectable using 'entity.hasMetadata("NPC")')
		if (event.getEntity() instanceof Player && !event.getEntity().hasMetadata("NPC") && plugin.getPlayerData((Player) event.getEntity()).spectating) {
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
	protected void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player && !event.getEntity().hasMetadata("NPC") && plugin.getPlayerData((Player) event.getEntity()).spectating) {
			event.setCancelled(true);
			((Player) event.getEntity()).setFoodLevel(20);
			((Player) event.getEntity()).setSaturation(20);
		}
	}
	
	/**
	 * Used to:<br>
	 *  - disable the spectator mode and reload the inventory, to avoid this inventory to be destroyed on server restart;<br>
	 *  - save the spectator mode on a file to restore it on the next login.
	 * 
	 * @param event
	 */
	@EventHandler
	protected void onPlayerQuit(PlayerQuitEvent event) {
		Player spectator = event.getPlayer();
		if (plugin.getPlayerData(spectator).spectating) {
			plugin.disableSpectate(spectator, plugin.console);
			
			// The spectator mode needs to be re-enabled on the next login
			plugin.specs.getConfig().set(spectator.getName(), true);
			plugin.specs.saveConfig();
		}
	}
	
	/**
	 * Used to:<br>
	 *  - display the various GUIs (teleportation, arenas) when the player right-click with the good item;<br>
	 *  - open a read-only GUI for the chests, etc.;<br>
	 *  - cancel the use of the doors, etc.
	 * 
	 * @param event
	 */
	@EventHandler
	protected void onPlayerInteract(PlayerInteractEvent event) {
		if (plugin.getPlayerData(event.getPlayer()).spectating && event.getMaterial() == plugin.compassItem && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			String mode = plugin.setup.getConfig().getString("mode"); 
			if (mode.equals("arena")) {
				UUID region = plugin.getPlayerData(event.getPlayer()).arena;
				plugin.showGUI(event.getPlayer(), region);
			} else {
				plugin.showGUI(event.getPlayer(), null);
			}
		}
		
		if (plugin.getPlayerData(event.getPlayer()).spectating && event.getMaterial() == plugin.clockItem && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			event.setCancelled(true);
			plugin.showArenaGUI(event.getPlayer());
		}
		
		if (plugin.getPlayerData(event.getPlayer()).spectating && event.getMaterial() == plugin.spectatorsToolsItem && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			event.setCancelled(true);
			plugin.showSpectatorsOptionsGUI(event.getPlayer());
		}
		
		// Cancel chest opening animation, doors, anything when the player right clicks.
		if (plugin.getPlayerData(event.getPlayer()).spectating) {
			event.setCancelled(true);
			
			if(event.hasBlock()) {
				
				// Opens the inventory of the blocks with an inventory without the opening animation
				// The players are unable to take anything due to the InventoryClickEvent & InventoryDragEvent being cancelled.
				if(event.getClickedBlock().getState() instanceof InventoryHolder) {
						Inventory original = ((InventoryHolder) event.getClickedBlock().getState()).getInventory();
						Inventory copy = null;
						
						if(original.getType().equals(InventoryType.CHEST) && original.getSize() > 27) {
							// Double chest. Using the same method lead to an exception (because InventoryType.CHEST is limited to 27 items).
							copy = plugin.getServer().createInventory(event.getPlayer(), original.getSize(), original.getTitle());
						}
						else {
							copy = plugin.getServer().createInventory(event.getPlayer(), original.getType(), original.getTitle());
						}
						
						copy.setContents(original.getContents());
						event.getPlayer().openInventory(copy);
				}
				
				// Allows spectators to pass through doors.
				else if(event.getClickedBlock().getType() == Material.WOODEN_DOOR
						|| event.getClickedBlock().getType() == Material.IRON_DOOR_BLOCK
						|| event.getClickedBlock().getType() == Material.FENCE_GATE) {
					
					Player spectator = event.getPlayer();
					Location doorLocation = event.getClickedBlock()
					                             .getLocation()
					                             .setDirection(spectator.getLocation().getDirection());
					
					int relativeHeight = 0;
					if(event.getClickedBlock().getType() == Material.WOODEN_DOOR
							|| event.getClickedBlock().getType() == Material.IRON_DOOR_BLOCK) {
						
						Material belowBlockType = event.getClickedBlock()
						                               .getLocation().add(0, -1, 0)
						                               .getBlock().getType();
						
						if(belowBlockType == Material.WOODEN_DOOR || belowBlockType == Material.IRON_DOOR_BLOCK) {
							// The spectator clicked the top part of the door.
							relativeHeight = -1;
						}
					}
					
					/*
					 * North: small Z
					 * South: big Z
					 * East:  big X
					 * West:  small X
					 */
					switch(event.getBlockFace()) {
						case EAST:
							spectator.teleport(doorLocation.add(-0.5, relativeHeight, 0.5), TeleportCause.PLUGIN);
							break;
						case NORTH:
							spectator.teleport(doorLocation.add(0.5, relativeHeight, 1.5), TeleportCause.PLUGIN);
							break;
						case SOUTH:
							spectator.teleport(doorLocation.add(0.5, relativeHeight, -0.5), TeleportCause.PLUGIN);
							break;
						case WEST:
							spectator.teleport(doorLocation.add(1.5, relativeHeight, 0.5), TeleportCause.PLUGIN);
							break;
						case UP:
							// If it's a fence gate, we uses the relative position of the player and the
							// gate.
							if(event.getClickedBlock().getState().getData() instanceof Gate) {
								Gate fenceGate = (Gate) event.getClickedBlock().getState().getData();
								// The BlockFace represents the block in the direction of the "line" of
								// the gate. So we needs to invert the relative teleportation.
								switch(fenceGate.getFacing()) {
									case NORTH:
									case SOUTH:
										if(spectator.getLocation().getX() > doorLocation.getX()) {
											spectator.teleport(doorLocation.add(-0.5, relativeHeight, 0.5), TeleportCause.PLUGIN);
										}
										else {
											spectator.teleport(doorLocation.add(1.5, relativeHeight, 0.5), TeleportCause.PLUGIN);
										}
										break;
									case EAST:
									case WEST:
										if(spectator.getLocation().getZ() > doorLocation.getZ()) {
											spectator.teleport(doorLocation.add(0.5, relativeHeight, -0.5), TeleportCause.PLUGIN);
										}
										else {
											spectator.teleport(doorLocation.add(0.5, relativeHeight, 1.5), TeleportCause.PLUGIN);
										}
										break;
									default:
										// Should never happens.
										break;
								}
							}
							break;
							
						default:
							// Nothing
							break;
					}
					
				}
				
				// Allows spectators to pass through trap doors
				else if(event.getClickedBlock().getType() == Material.TRAP_DOOR) {
					if(!((TrapDoor) event.getClickedBlock().getState().getData()).isOpen()) {
						Player spectator = event.getPlayer();
						Location doorLocation = event.getClickedBlock()
						                             .getLocation()
						                             .setDirection(spectator.getLocation().getDirection());
						
						switch(event.getBlockFace()) {
							case UP:
								spectator.teleport(doorLocation.add(0.5, -1, 0.5), TeleportCause.PLUGIN);
								break;
							
							case DOWN:
								spectator.teleport(doorLocation.add(0.5, 1, 0.5), TeleportCause.PLUGIN);
								break;
							
							default:
								break;
						}
					}
				}
			}
		}
	}
	
	/**
	 * Used to display the inventory of a player when the spectator right-clicks.
	 * 
	 * @param event
	 */
	@EventHandler
	protected void onPlayerInteractEntity(PlayerInteractEntityEvent event) {		
		if(plugin.getPlayerData(event.getPlayer()).spectating && event.getRightClicked() instanceof Player && !event.getRightClicked().hasMetadata("NPC")) {
			if(event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getType().equals(plugin.inspectorItem) && !plugin.getPlayerData((Player) event.getRightClicked()).spectating) {
				plugin.showPlayerInventoryGUI(event.getPlayer(), (Player) event.getRightClicked());
			}
			
			event.setCancelled(true);
		}
	}
	
	/**
	 * Used to:<br>
	 *  - prevent a command to be executed if the player is a spectator and the option is set in the config;<br>
	 *  - catch /me commands to show them into the spectator chat.
	 * 
	 * @param event
	 */
	@EventHandler
	protected void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if(plugin.specChat && event.getMessage().startsWith("/me") && plugin.getPlayerData(event.getPlayer()).spectating) {
			plugin.sendSpectatorMessage(event.getPlayer(), event.getMessage().substring(4), true);
			event.setCancelled(true);
			return;
		}
		
		if (plugin.blockCmds) {
			if (event.getPlayer().hasPermission("spectate.admin") && plugin.adminBypass) {
				// Do nothing
			} else if (!(event.getMessage().startsWith("/spec") || event.getMessage().startsWith("/spectate") || event.getMessage().startsWith("/me")) && plugin.getPlayerData(event.getPlayer()).spectating) {
				event.getPlayer().sendMessage(SpectatorPlus.prefix+"Command blocked!");
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
	protected void onPlayerRespawn(PlayerRespawnEvent event) {
		if(plugin.death) {
			// Prevent murdering clients! (force close bug if spec mode is enabled instantly)
			new AfterRespawnTask(event.getPlayer(), plugin).runTaskLater(plugin, 20);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	protected void onPlayerDeath(PlayerDeathEvent event) {
		if(plugin.tpToDeathTool) {
			Player killed = event.getEntity();
			
			plugin.getPlayerData(killed).deathLocation = killed.getLocation();
			
			if(plugin.tpToDeathToolShowCause) {
				String deathMessage = ChatColor.stripColor(event.getDeathMessage());
				String noColorsDisplayName = ChatColor.stripColor(killed.getDisplayName());
				
				if(deathMessage == null) deathMessage = "";
				
				deathMessage = deathMessage.replace(killed.getName() + " was", "You were")
				                           .replace(killed.getName(), "You")
				                           .replace(noColorsDisplayName + " was", "You were")
				                           .replace(noColorsDisplayName, "You");
				
				plugin.getPlayerData(killed).lastDeathMessage = ChatColor.stripColor(deathMessage);
			}
		}
	}
	
	/**
	 * Used to get the selected item in the various GUIs.
	 * 
	 * @param event
	 */
	@EventHandler
	protected void onInventoryClick(InventoryClickEvent event) {
		if (plugin.getPlayerData((Player) event.getWhoClicked()).spectating) {
			
			// Cancel the event to prevent the item from being taken
			event.setCancelled(true);
			
			// Teleportation GUI
			if ((event.getInventory().getTitle().equals(SpectatorPlus.TELEPORTER_ANY_TITLE) || event.getInventory().getTitle().startsWith(SpectatorPlus.TELEPORTER_ARENA_TITLE)) && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.SKULL_ITEM && event.getCurrentItem().getDurability() == 3) {
				ItemStack playerhead = event.getCurrentItem();
				SkullMeta meta = (SkullMeta)playerhead.getItemMeta();
				Player skullOwner = plugin.getServer().getPlayer(meta.getOwner());
				event.getWhoClicked().closeInventory();
				
				if (skullOwner != null && skullOwner.isOnline() && !plugin.user.get(skullOwner.getName()).spectating) {
					if(event.isLeftClick()) {
						plugin.choosePlayer((Player) event.getWhoClicked(), skullOwner);
					}
					else if(plugin.inspectFromTPMenu) {
						plugin.showPlayerInventoryGUI((Player) event.getWhoClicked(), skullOwner);
					}
				}
				
				else {
					if (skullOwner == null) {
						OfflinePlayer offlineSkullOwner = plugin.getServer().getOfflinePlayer(meta.getOwner());
						((Player) event.getWhoClicked()).sendMessage(SpectatorPlus.prefix + ChatColor.RED + offlineSkullOwner.getName() + ChatColor.GOLD + " is offline!");
					}
					else if (skullOwner.getAllowFlight() == true) {
						((Player) event.getWhoClicked()).sendMessage(SpectatorPlus.prefix + ChatColor.RED + skullOwner.getName() + ChatColor.GOLD + " is currently spectating!");
					}
				}
				return;
			}
			
			// Manage showArenaGUI method selection
			if (event.getInventory().getTitle().equals(SpectatorPlus.ARENA_SELECTOR_TITLE) && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.BOOK) {
				ItemStack arenaBook = event.getCurrentItem();
				ItemMeta meta = (ItemMeta)arenaBook.getItemMeta();
				String chosenArena = meta.getDisplayName();
				if (!meta.hasLore()) { // If the book has lore, it is the inspector book, and should not be used.
					event.getWhoClicked().closeInventory();

					if (arenaBook != null) {
						plugin.setArenaForPlayer((Player) event.getWhoClicked(), chosenArena);
					}
				}
				return;
			}
			
			// Manage spectators' tools
			if(event.getInventory().getTitle().equals(SpectatorPlus.SPEC_TOOLS_TITLE) && event.getCurrentItem() != null) {
				ItemStack toolSelected = event.getCurrentItem();
				Player spectator = (Player) event.getWhoClicked();
				try {
					// The fly speed values are experimental; the difference between the fly speed and the run speed
					// matches approximately the vanilla difference.
					if(toolSelected.getItemMeta().getDisplayName().equalsIgnoreCase(SpectatorPlus.TOOL_NORMAL_SPEED_NAME)) {
						spectator.removePotionEffect(PotionEffectType.SPEED);
						spectator.setFlySpeed(0.10f); // default fly speed
					}
					else if(toolSelected.getItemMeta().getDisplayName().equalsIgnoreCase(SpectatorPlus.TOOL_SPEED_I_NAME)) {
						spectator.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0), true);
						spectator.setFlySpeed(0.13f);
					}
					else if(toolSelected.getItemMeta().getDisplayName().equalsIgnoreCase(SpectatorPlus.TOOL_SPEED_II_NAME)) {
						spectator.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1), true);
						spectator.setFlySpeed(0.16f);
					}
					else if(toolSelected.getItemMeta().getDisplayName().equalsIgnoreCase(SpectatorPlus.TOOL_SPEED_III_NAME)) {
						spectator.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2), true);
						spectator.setFlySpeed(0.19f);
					}
					else if(toolSelected.getItemMeta().getDisplayName().equalsIgnoreCase(SpectatorPlus.TOOL_SPEED_IV_NAME)) {
						spectator.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3), true);
						spectator.setFlySpeed(0.22f);
					}
					else if(toolSelected.getItemMeta().getDisplayName().equalsIgnoreCase(SpectatorPlus.TOOL_NIGHT_VISION_ACTIVE_NAME)
							|| toolSelected.getItemMeta().getDisplayName().equalsIgnoreCase(SpectatorPlus.TOOL_NIGHT_VISION_INACTIVE_NAME)) {
						if(spectator.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
							spectator.removePotionEffect(PotionEffectType.NIGHT_VISION);
							spectator.removePotionEffect(PotionEffectType.WATER_BREATHING);
						}
						else {
							spectator.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0), true);
							spectator.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0), true);
						}
					}
					else if(toolSelected.getItemMeta().getDisplayName().equalsIgnoreCase(SpectatorPlus.TOOL_TP_TO_DEATH_POINT_NAME)) {
						spectator.teleport(plugin.getPlayerData(spectator).deathLocation.setDirection(spectator.getLocation().getDirection()));
					}
					
					spectator.closeInventory();
				} catch(NullPointerException e) {
					// This happens if there isn't any meta, aka here if the spectator
					// clicks on an empty slot.
					// In this case, nothing happens, and the inventory is not closed.
				}
			}
		}
	}
	
	/**
	 * Used to cancel an item to be moved from/to an inventory if the player is spectating.<br>
	 * This method is required <b>as well as</b> InventoryClickEvent in order to completely prevent the player from moving items.
	 * 
	 * @param event
	 */
	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		if (plugin.getPlayerData((Player) event.getWhoClicked()).spectating) {
			event.setCancelled(true);
		}
	}
	
	/**
	 * Handle MultiverseInventories & other similar plugins<br>
	 * Disables spectate mode to restore proper inventory before world change; then<br>
	 * Re-enables spectate mode to restore spectator inventory after world change.
	 * 
	 * @param event
	 */
	@EventHandler(priority=EventPriority.LOWEST)
	public void onWorldChange(final PlayerChangedWorldEvent event) {
		if (plugin.getPlayerData((Player) event.getPlayer()).spectating) {
			plugin.getPlayerData((Player) event.getPlayer()).wasSpectatorBeforeWorldChanged = true;
			plugin.disableSpectate(event.getPlayer(), plugin.console, true);
			new BukkitRunnable() {
				@Override
				public void run() {
					// What you want to schedule goes here
					if (plugin.getPlayerData((Player) event.getPlayer()).wasSpectatorBeforeWorldChanged) {
						plugin.enableSpectate(event.getPlayer(), plugin.console, true);
						plugin.getPlayerData((Player) event.getPlayer()).wasSpectatorBeforeWorldChanged = false;
					}
				}
			}.runTaskLater(plugin, 1);
		}
	}
}
