package com.pgcraft.spectatorplus;

import com.pgcraft.spectatorplus.spectators.SpectatorMode;
import com.pgcraft.spectatorplus.tasks.AfterRespawnTask;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
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
import org.bukkit.event.vehicle.VehicleEnterEvent;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class SpectateListener implements Listener {
	
	private SpectatorPlusOld p; // Pointer to main class (see SpectatorPlusOld.java)

	protected SpectateListener(SpectatorPlusOld p) {
		this.p = p;
	}
	
	/**
	 * Used to:<br>
	 *  - save the player in the internal list of players;<br>
	 *  - set the internal scoreboard for this player;<br>
	 *  - hide spectators from the joining player;<br>
	 *  - enable the spectator mode if the player is registered as a spectator.
	 * 
	 * @param e
	 */
	@EventHandler(priority=EventPriority.HIGHEST)
	protected void onPlayerJoin(PlayerJoinEvent e) {
		
		if(e.getPlayer().hasPermission("spectate.admin.hide.auto")) {
			p.getPlayerData(e.getPlayer()).setHiddenFromTp(true);
		}
		
		for (Player target : p.getServer().getOnlinePlayers()) {
			if (p.getPlayerData(target).isSpectating()) {
				e.getPlayer().hidePlayer(target);
			}
		}
		
		if (p.specs.getConfig().contains(e.getPlayer().getName())) {
			p.enableSpectate(e.getPlayer(), e.getPlayer());
		}
	}
	
	/**
	 * Used to hide chat messages sent by spectators, if the spectator chat is enabled.
	 * 
	 * @param e
	 */
	// Ignore cancelled, so another plugin can implement a private chat without conflicts.
	@EventHandler(ignoreCancelled = true)
	protected void onChatSend(AsyncPlayerChatEvent e) {
		if (p.specChat) {
			if (p.getPlayerData(e.getPlayer()).isSpectating()) {
				e.setCancelled(true);
				p.sendSpectatorMessage(e.getPlayer(), e.getMessage(), false);
			}
		}
	}
	
	/**
	 * Used to prevent spectators from blocking players from placing blocks.
	 * 
	 * @param e
	 */
	@EventHandler
	protected void onBlockCanBuild(BlockCanBuildEvent e) {
		if (!e.isBuildable()) {
			
			// Get location of the block that is going to be placed 
			Location blockL = e.getBlock().getLocation(); // event.getBlock() returns the block to be placed -1 y
			
			boolean allowed = false; // If there are any actual players there, the event should not be over-wrote.
			
			for (Player target : p.getServer().getOnlinePlayers()) {
				if (target.getWorld().equals(e.getBlock().getWorld())) { // Player in same world?
					Location playerL = target.getLocation();
					
					if (playerL.getX() > blockL.getBlockX()-1 && playerL.getX() < blockL.getBlockX()+1) { //2d...
						if (playerL.getZ() > blockL.getBlockZ()-1 && playerL.getZ() < blockL.getBlockZ()+1) { // 2d (x & z)
							if (playerL.getY() > blockL.getBlockY()-2 && playerL.getY() < blockL.getBlockY()+1) { // 3d (y): for feet & head height
								if (p.getPlayerData(target).isSpectating()) allowed = true;
								else {
									allowed = false;
									break;
								}
							}
						}
					}
					
				}
			}
			e.setBuildable(allowed);
		}
	}
	
	/**
	 * Used to teleport spectators blocking players from placing blocks.
	 * 
	 * @param e
	 */
	@EventHandler(priority=EventPriority.HIGHEST) // This event is the last to be executed, as lower priorities are executed first.
	protected void onBlockPlace(BlockPlaceEvent e) {
		
		// Get location of the block that is going to be placed 
		Location blockL = e.getBlock().getLocation();

		for (Player target : p.getServer().getOnlinePlayers()) {
			if (p.getPlayerData(target).isSpectating() && target.getWorld().equals(e.getBlock().getWorld())) { // Player spectating & in same world?
				Location playerL = target.getLocation();

				if (playerL.getX() > blockL.getBlockX()-1 && playerL.getX() < blockL.getBlockX()+1) { //2d...
					if (playerL.getZ() > blockL.getBlockZ()-1 && playerL.getZ() < blockL.getBlockZ()+1) { // 2d (x & z)
						if (playerL.getY() > blockL.getBlockY()-2 && playerL.getY() < blockL.getBlockY()+1) { // 3d (y): for feet & head height
							target.teleport(e.getPlayer(), TeleportCause.PLUGIN);
							target.sendMessage(SpectatorPlusOld.prefix + "You were teleported away from a placed block.");
						}
					}
				}
				
			}
		}
	}
	
	/**
	 * Used to:<br>
	 *  - cancel any damage taken or caused by a spectator;<br>
	 *  - make non-potions projectiles fly through the spectators.
	 * 
	 * @param e
	 */
	@EventHandler
	protected void onEntityDamageEvent(final EntityDamageByEntityEvent e) {		
		
		/** Cancels damages involving spectators **/
		
		// Manages spectators damaging players
		if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
			if ((!e.getDamager().hasMetadata("NPC") && p.getPlayerData(((Player) e.getDamager())).isSpectating()) || (!e.getEntity().hasMetadata("NPC") && p.getPlayerData(((Player) e.getEntity())).isSpectating())) {
				e.setCancelled(true);
			}
		// Manage spectators damaging mobs
		} else if (!(e.getEntity() instanceof Player) && e.getDamager() instanceof Player) {
			if (!e.getDamager().hasMetadata("NPC") && p.getPlayerData(((Player) e.getDamager())).isSpectating() == true) {
				e.setCancelled(true);
			}
		// Manage mobs damaging spectators
		} else if (e.getEntity() instanceof Player && !(e.getDamager() instanceof Player)) {
			if (!e.getEntity().hasMetadata("NPC") && p.getPlayerData(((Player) e.getEntity())).isSpectating() == true) {
				e.setCancelled(true);
			}
		}
		
		// Otherwise both entities are mobs, ignore the event.
		
		
		/** Make projectiles fly through spectators **/
		
		if(e.getDamager() instanceof Projectile
				&& !(e.getDamager() instanceof ThrownPotion) // splash potions are cancelled in PotionSplashEvent
				&& e.getEntity() instanceof Player
				&& !e.getEntity().hasMetadata("NPC") // Check for NPC's, as they are instances of Players sometimes...
				&& p.getPlayerData(((Player) e.getEntity())).isSpectating()) {
			
			e.setCancelled(true);
			
			final Player spectatorInvolved = (Player) e.getEntity();
			final boolean wasFlying = spectatorInvolved.isFlying();
			final Location initialSpectatorLocation = spectatorInvolved.getLocation();
			
			final Vector initialProjectileVelocity = e.getDamager().getVelocity();
			final Location initialProjectileLocation = e.getDamager().getLocation();
			
			spectatorInvolved.setFlying(true);
			spectatorInvolved.teleport(initialSpectatorLocation.clone().add(0, 6, 0), TeleportCause.PLUGIN);
			
			// Prevents the arrow from bouncing on the entity
			Bukkit.getScheduler().runTaskLater(p, new BukkitRunnable() {
				@Override
				public void run() {
					e.getDamager().teleport(initialProjectileLocation);
					e.getDamager().setVelocity(initialProjectileVelocity);
				}
			}, 1L);
			
			// Teleports back the spectator
			Bukkit.getScheduler().runTaskLater(p, new BukkitRunnable() {
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
	 * @param e
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	protected void onPotionSplash(final PotionSplashEvent e) {
		
		final ArrayList<UUID> spectatorsAffected = new ArrayList<UUID>();
		
		for(LivingEntity player : e.getAffectedEntities()) {
			if(player instanceof Player && !player.hasMetadata("NPC") && p.getPlayerData(((Player) player)).isSpectating()) {
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
			
			for(Entity entity : e.getEntity().getNearbyEntities(2, 2, 2)) {
				if(entity instanceof Player && !entity.hasMetadata("NPC") && p.getPlayerData(((Player) entity)).isSpectating()) {
					// The potion hits a spectator
					teleportationNeeded = true;
				}
			}
			
			final HashMap<UUID,Boolean> oldFlyMode = new HashMap<UUID,Boolean>(); 
			
			for(UUID spectatorUUID : spectatorsAffected) {
				
				Player spectator = p.getServer().getPlayer(spectatorUUID);
				
				// The effect is removed
				e.setIntensity(spectator, 0);
				
				if(teleportationNeeded) {
					oldFlyMode.put(spectator.getUniqueId(), spectator.isFlying());
					spectator.setFlying(true);

					// High teleportation because the potions can be thrown up
					spectator.teleport(spectator.getLocation().add(0, 10, 0));
				}
			}
			
			if(teleportationNeeded) {
				
				final Location initialProjectileLocation = e.getEntity().getLocation();
				final Vector initialProjectileVelocity = e.getEntity().getVelocity();
				
				// Prevents the potion from splashing on the entity
				p.getServer().getScheduler().runTaskLater(p, new BukkitRunnable() {
					@Override
					public void run() {
						// Because the original entity is, one tick later, destroyed, we need to spawn a new one
						// Cancelling the event only cancels the effect.
						ThrownPotion clonedEntity = (ThrownPotion) e.getEntity().getWorld().spawnEntity(initialProjectileLocation, e.getEntity().getType()); 
						
						// For other plugins (may be used)
						clonedEntity.setShooter(e.getEntity().getShooter());
						clonedEntity.setTicksLived(e.getEntity().getTicksLived());
						clonedEntity.setFallDistance(e.getEntity().getFallDistance());
						clonedEntity.setBounce(e.getEntity().doesBounce());
						if(e.getEntity().getPassenger() != null) {
							clonedEntity.setPassenger(e.getEntity().getPassenger()); // hey, why not
						}
						
						// Clones the effects
						clonedEntity.setItem(e.getEntity().getItem());
						
						// Clones the speed/direction
						clonedEntity.setVelocity(initialProjectileVelocity);
						
						// Just in case
						e.getEntity().remove();
					}
				}, 1L);
				
				// Teleports back the spectators
				p.getServer().getScheduler().runTaskLater(p, new BukkitRunnable() {
					@Override
					public void run() {
						for(UUID spectatorUUID : spectatorsAffected) {
							Player spectator = p.getServer().getPlayer(spectatorUUID);
							
							spectator.teleport(spectator.getLocation().add(0, -10, 0));
							spectator.setFlying(oldFlyMode.get(spectatorUUID));
						}
					}
				}, 5L);
				
				// Cancels the effect for everyone (because the thrown potion is re-spawned,
				// avoids a double effect for some players).
				e.setCancelled(true);
				
				// Side note: there is a visual glitch (the players will see a double splash,
				// the real one plus the splash on the spectator), but the behavior is preserved and
				// the effect is applied once, on the players.
			}	
		}
	}
	
	/**
	 * Used to setup an arena, if the command was sent before by this player.
	 * 
	 * @param e
	 */
	@EventHandler
	protected void onBlockBreak(BlockBreakEvent e) {
		// Set up mode
		if(p.arenaSetup(e.getPlayer(), e.getBlock())) {
			e.setCancelled(true);
		}
	}
	
	
	/**
	 * Used to prevent spectators from changing their gamemode whilst spectating.
	 * 
	 * @param e
	 */
	@EventHandler(priority=EventPriority.HIGH)
	protected void onGamemodeChange(PlayerGameModeChangeEvent e) {
		if (p.getPlayerData(e.getPlayer()) != null
				&& p.getPlayerData(e.getPlayer()).isSpectating()
				&& !e.getNewGameMode().equals(GameMode.ADVENTURE)
				&& !p.getPlayerData(e.getPlayer()).isGamemodeChangeAllowed()) {
			
			e.setCancelled(true);
			e.getPlayer().setAllowFlight(true);
		}
	}
	
	
	/**
	 * Used to prevent spectators from dropping items on ground.
	 * 
	 * @param e
	 */
	@EventHandler
	protected void onPlayerDropItem(PlayerDropItemEvent e) {
		// On player drop item - Cancel if the player is a spectator
		if (p.getPlayerData(e.getPlayer()).isSpectating()) {
			e.setCancelled(true);
		}
	}
	
	/**
	 * Used to prevent spectators from picking up items.
	 * 
	 * @param e
	 */
	@EventHandler
	protected void onPlayerPickupItem(PlayerPickupItemEvent e) {
		if (p.getPlayerData(e.getPlayer()).isSpectating()) {
			e.setCancelled(true);
		}
	}
	
	/**
	 * Used to prevent the mobs to be interested by (and aggressive against) spectators. 
	 * 
	 * @param e
	 */
	@EventHandler
	protected void onEntityTarget(EntityTargetEvent e) {
		// On entity target - Stop mobs targeting spectators
		// Check to make sure it isn't an NPC (Citizens NPC's will be detectable using 'entity.hasMetadata("NPC")')
		if (e.getTarget() != null && e.getTarget() instanceof Player && !e.getTarget().hasMetadata("NPC") && p.getPlayerData(((Player) e.getTarget())).isSpectating()) {
			e.setCancelled(true);
		}
	}
	
	/**
	 * Used to:<br>
	 *  - prevent the damage block animation to be displayed, if the player is a spectator;<br>
	 *  - setup an arena (if the command was sent before by the sender).
	 * 
	 * @param e
	 */
	@EventHandler
	protected void onBlockDamage(BlockDamageEvent e) {
		// On block damage - Cancels the block damage animation
		if (p.getPlayerData(e.getPlayer()).isSpectating()) {
			e.setCancelled(true);
			
			if(p.output) {
				e.getPlayer().sendMessage(SpectatorPlusOld.prefix + "You cannot break blocks while in spectate mode!");
			}
		}
		
		// Set up mode
		if (p.arenaSetup(e.getPlayer(), e.getBlock())) {
			e.setCancelled(true);
		}
	}
	
	/**
	 * Used to:<br>
	 *  - prevent players & mobs from damaging spectators;<br>
	 *  - stop the fire display when a spectator go out of a fire/lava block.
	 * 
	 * @param e
	 */
	@EventHandler
	protected void onEntityDamage(EntityDamageEvent e) {
		// On entity damage - Stops users hitting players and mobs while spectating
		// Check to make sure it isn't an NPC (Citizens NPC's will be detectable using 'entity.hasMetadata("NPC")')
		if (e.getEntity() instanceof Player && !e.getEntity().hasMetadata("NPC") && p.getPlayerData((Player) e.getEntity()).isSpectating()) {
			e.setCancelled(true);
			e.getEntity().setFireTicks(0);
		}
	}
	
	/**
	 * Used to prevent the food level to drop if the player is a spectator.
	 * 
	 * @param e
	 */
	@EventHandler
	protected void onFoodLevelChange(FoodLevelChangeEvent e) {
		if (e.getEntity() instanceof Player && !e.getEntity().hasMetadata("NPC") && p.getPlayerData((Player) e.getEntity()).isSpectating()) {
			e.setCancelled(true);
			((Player) e.getEntity()).setFoodLevel(20);
			((Player) e.getEntity()).setSaturation(20);
		}
	}
	
	/**
	 * Used to:<br>
	 *  - disable the spectator mode and reload the inventory, to avoid this inventory to be destroyed on server restart;<br>
	 *  - save the spectator mode on a file to restore it on the next login.
	 * 
	 * @param e
	 */
	@EventHandler
	protected void onPlayerQuit(PlayerQuitEvent e) {
		Player spectator = e.getPlayer();
		if (p.getPlayerData(spectator).isSpectating()) {
			p.disableSpectate(spectator, p.console, true, true);
		}
	}
	
	/**
	 * Used to:<br>
	 *  - display the various GUIs (teleportation, arenas) when the player right-click with the good item;<br>
	 *  - open a read-only GUI for the chests, etc.;<br>
	 *  - cancel the use of the doors, etc.
	 * 
	 * @param e
	 */
	@EventHandler(priority=EventPriority.HIGH)
	protected void onPlayerInteract(PlayerInteractEvent e) {
		// Right-click with teleporter
		if (p.getPlayerData(e.getPlayer()).isSpectating() && e.getMaterial() == p.compassItem && (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (p.mode == SpectatorMode.ARENA) {
				UUID region = p.getPlayerData(e.getPlayer()).getArena();
				p.showGUI(e.getPlayer(), region);
			} else {
				p.showGUI(e.getPlayer(), null);
			}
		}
		
		// Right-click with arena selector
		if (p.getPlayerData(e.getPlayer()).isSpectating() && e.getMaterial() == p.clockItem && (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			e.setCancelled(true);
			p.showArenaGUI(e.getPlayer());
		}
		
		// Right-click with spectators' tools
		if (p.getPlayerData(e.getPlayer()).isSpectating() && e.getMaterial() == p.spectatorsToolsItem && (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			e.setCancelled(true);
			p.showSpectatorsOptionsGUI(e.getPlayer());
		}
		
		// Cancel chest opening animation, doors, anything when the player right clicks.
		if (p.getPlayerData(e.getPlayer()).isSpectating()) {
			if(!p.skriptInt){
				e.setCancelled(true);
			}
			if(e.hasBlock()) {
				
				// Opens the inventory of the blocks with an inventory without the opening animation
				// The players are unable to take anything due to the InventoryClickEvent & InventoryDragEvent being cancelled.
				if(e.getClickedBlock().getState() instanceof InventoryHolder) {
						Inventory original = ((InventoryHolder) e.getClickedBlock().getState()).getInventory();
						Inventory copy = null;
						
						if(original.getType().equals(InventoryType.CHEST) && original.getSize() > 27) {
							// Double chest. Using the same method lead to an exception (because InventoryType.CHEST is limited to 27 items).
							// Change title from "container.chest" to "Chest" if necessary.
							String title;
							if (original.getTitle().startsWith("container.")) title = WordUtils.capitalizeFully(original.getType().toString());
							else title = original.getTitle();
							
							copy = p.getServer().createInventory(e.getPlayer(), original.getSize(), title);
						}
						else {
							// Change title from "container.chest" to "Chest", "Furnace", etc, if necessary.
							String title;
							if (original.getTitle().startsWith("container.")) title = WordUtils.capitalizeFully(original.getType().toString());
							else title = original.getTitle();
							
							copy = p.getServer().createInventory(e.getPlayer(), original.getType(), title);
						}
						
						copy.setContents(original.getContents());
						e.getPlayer().openInventory(copy);
				}
				
				// Allows spectators to pass through doors.
				else if(e.getClickedBlock().getType() == Material.WOODEN_DOOR
						|| e.getClickedBlock().getType() == Material.IRON_DOOR_BLOCK
						|| e.getClickedBlock().getType() == Material.FENCE_GATE) {
					
					Player spectator = e.getPlayer();
					Location doorLocation = e.getClickedBlock()
					                             .getLocation()
					                             .setDirection(spectator.getLocation().getDirection());
					
					int relativeHeight = 0;
					if(e.getClickedBlock().getType() == Material.WOODEN_DOOR
							|| e.getClickedBlock().getType() == Material.IRON_DOOR_BLOCK) {
						
						Material belowBlockType = e.getClickedBlock()
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
					switch(e.getBlockFace()) {
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
							if(e.getClickedBlock().getState().getData() instanceof Gate) {
								Gate fenceGate = (Gate) e.getClickedBlock().getState().getData();
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
				else if(e.getClickedBlock().getType() == Material.TRAP_DOOR) {
					if(!((TrapDoor) e.getClickedBlock().getState().getData()).isOpen()) {
						Player spectator = e.getPlayer();
						Location doorLocation = e.getClickedBlock()
						                             .getLocation()
						                             .setDirection(spectator.getLocation().getDirection());
						
						switch(e.getBlockFace()) {
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
	 * @param e
	 */
	@EventHandler
	protected void onPlayerInteractEntity(PlayerInteractEntityEvent e) {		
		if(p.getPlayerData(e.getPlayer()).isSpectating() && e.getRightClicked() instanceof Player && !e.getRightClicked().hasMetadata("NPC")) {
			if(e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().getType().equals(p.inspectorItem) && !p.getPlayerData((Player) e.getRightClicked()).isSpectating()) {
				p.showPlayerInventoryGUI(e.getPlayer(), (Player) e.getRightClicked());
			}
			
			e.setCancelled(true);
		}
	}
	
	/**
	 * Used to:<br>
	 *  - prevent a command to be executed if the player is a spectator and the option is set in the config;<br>
	 *  - catch /me commands to show them into the spectator chat;<br>
	 *  - allow specified commands from the whitelist section to be executed.
	 * 
	 * @param e
	 */
	@SuppressWarnings("unchecked")
	@EventHandler
	protected void onCommandPreprocess(PlayerCommandPreprocessEvent e) {
		if(p.specChat && e.getMessage().startsWith("/me ") && p.getPlayerData(e.getPlayer()).isSpectating()) {
			p.sendSpectatorMessage(e.getPlayer(), e.getMessage().substring(4), true);
			e.setCancelled(true);
			return;
		}
		
		if (p.blockCmds) {
			if (e.getPlayer().hasPermission("spectate.admin") && p.adminBypass) {
				// Do nothing
			} else if (!(e.getMessage().startsWith("/spec ") || e.getMessage().equalsIgnoreCase("/spec") || e.getMessage().startsWith("/spectate ") || e.getMessage().equalsIgnoreCase("/spectate") || e.getMessage().startsWith("/me ") || e.getMessage().equalsIgnoreCase("/me")) && p.getPlayerData(e.getPlayer()).isSpectating()) {
				// Command whitelist
				try {
					Iterator<String> iter = ((ArrayList<String>) p.toggles.get(Toggle.CHAT_BLOCKCOMMANDS_WHITELIST)).iterator();
					boolean allowed = false;
					while (iter.hasNext()) {
						String compare = iter.next();
						if (e.getMessage().startsWith(compare+" ") || e.getMessage().equalsIgnoreCase(compare)) {
							allowed = true;
						}
					}
					if (!allowed) {
						e.getPlayer().sendMessage(SpectatorPlusOld.prefix+"Command blocked!");
						e.setCancelled(true);
					}
				} catch (ClassCastException err) { // caused by casting to ArrayList<String> error
					p.console.sendMessage(SpectatorPlusOld.prefix+ChatColor.DARK_RED+"The command whitelist section isn't formatted correctly, ignoring it!");
					// cancel the command.
					e.getPlayer().sendMessage(SpectatorPlusOld.prefix+"Command blocked!");
					e.setCancelled(true);
				}
			}
		}
	}
	
	/**
	 * Used to enable the spectator mode for dead players, if this option is enabled in the config.
	 * 
	 * @param e
	 */
	@EventHandler
	protected void onPlayerRespawn(PlayerRespawnEvent e) {
		if(p.death) {
			// Prevent murdering clients! (force close bug if spec mode is enabled instantly)
			new AfterRespawnTask(e.getPlayer(), p).runTaskLater(p, 20);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	protected void onPlayerDeath(PlayerDeathEvent e) {
		if(p.tpToDeathTool) {
			Player killed = e.getEntity();
			
			p.getPlayerData(killed).setDeathLocation(killed.getLocation());
			
			if(p.tpToDeathToolShowCause) {
				String deathMessage = ChatColor.stripColor(e.getDeathMessage());
				String noColorsDisplayName = ChatColor.stripColor(killed.getDisplayName());
				
				if(deathMessage == null) deathMessage = "";
				
				deathMessage = deathMessage.replace(killed.getName() + " was", "You were")
				                           .replace(killed.getName(), "You")
				                           .replace(noColorsDisplayName + " was", "You were")
				                           .replace(noColorsDisplayName, "You");
				
				p.getPlayerData(killed).setLastDeathMessage(ChatColor.stripColor(deathMessage));
			}
		}
	}
	
	/**
	 * Used to get the selected item in the various GUIs.
	 * 
	 * @param e
	 */
	@EventHandler
	protected void onInventoryClick(InventoryClickEvent e) {
		if (p.getPlayerData((Player) e.getWhoClicked()).isSpectating()) {
			
			// Cancel the event to prevent the item from being taken
			e.setCancelled(true);
			
			// Teleportation GUI
			if ((e.getInventory().getTitle().equals(SpectatorPlusOld.TELEPORTER_ANY_TITLE) || e.getInventory().getTitle().startsWith(SpectatorPlusOld.TELEPORTER_ARENA_TITLE)) && e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.SKULL_ITEM && e.getCurrentItem().getDurability() == 3) {
				ItemStack playerhead = e.getCurrentItem();
				SkullMeta meta = (SkullMeta)playerhead.getItemMeta();
				Player skullOwner = p.getServer().getPlayer(meta.getOwner());
				e.getWhoClicked().closeInventory();
				
				if (skullOwner != null && skullOwner.isOnline() && !p.user.get(skullOwner.getName()).isSpectating()) {
					if(e.isLeftClick()) {
						p.choosePlayer((Player) e.getWhoClicked(), skullOwner);
					}
					else if(p.inspectFromTPMenu) {
						p.showPlayerInventoryGUI((Player) e.getWhoClicked(), skullOwner);
					}
				}
				
				else {
					if (skullOwner == null) {
						OfflinePlayer offlineSkullOwner = p.getServer().getOfflinePlayer(meta.getOwner());
						((Player) e.getWhoClicked()).sendMessage(SpectatorPlusOld.prefix + ChatColor.RED + offlineSkullOwner.getName() + ChatColor.GOLD + " is offline!");
					}
					else if (skullOwner.getAllowFlight() == true) {
						((Player) e.getWhoClicked()).sendMessage(SpectatorPlusOld.prefix + ChatColor.RED + skullOwner.getName() + ChatColor.GOLD + " is currently spectating!");
					}
				}
				return;
			}
			
			// Manage showArenaGUI method selection
			else if (e.getInventory().getTitle().equals(SpectatorPlusOld.ARENA_SELECTOR_TITLE) && e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.BOOK) {
				ItemStack arenaBook = e.getCurrentItem();
				ItemMeta meta = (ItemMeta)arenaBook.getItemMeta();
				String chosenArena = meta.getDisplayName();
				if (!meta.hasLore()) { // If the book has lore, it is the inspector book, and should not be used.
					e.getWhoClicked().closeInventory();

					if (arenaBook != null) {
						p.setArenaForPlayer((Player) e.getWhoClicked(), chosenArena);
					}
				}
				return;
			}
			
			// Manage spectators' tools
			else if(e.getInventory().getTitle().equals(SpectatorPlusOld.SPEC_TOOLS_TITLE) && e.getCurrentItem() != null) {
				ItemStack toolSelected = e.getCurrentItem();
				Player spectator = (Player) e.getWhoClicked();
				try {
					// The fly speed values are experimental; the difference between the fly speed and the run speed
					// matches approximately the vanilla difference.
					if(toolSelected.getItemMeta().getDisplayName().equalsIgnoreCase(SpectatorPlusOld.TOOL_NORMAL_SPEED_NAME)) {
						spectator.removePotionEffect(PotionEffectType.SPEED);
						spectator.setFlySpeed(0.10f); // default fly speed
					}
					else if(toolSelected.getItemMeta().getDisplayName().equalsIgnoreCase(SpectatorPlusOld.TOOL_SPEED_I_NAME)) {
						spectator.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0), true);
						spectator.setFlySpeed(0.13f);
					}
					else if(toolSelected.getItemMeta().getDisplayName().equalsIgnoreCase(SpectatorPlusOld.TOOL_SPEED_II_NAME)) {
						spectator.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1), true);
						spectator.setFlySpeed(0.16f);
					}
					else if(toolSelected.getItemMeta().getDisplayName().equalsIgnoreCase(SpectatorPlusOld.TOOL_SPEED_III_NAME)) {
						spectator.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2), true);
						spectator.setFlySpeed(0.19f);
					}
					else if(toolSelected.getItemMeta().getDisplayName().equalsIgnoreCase(SpectatorPlusOld.TOOL_SPEED_IV_NAME)) {
						spectator.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3), true);
						spectator.setFlySpeed(0.22f);
					}
					else if(toolSelected.getItemMeta().getDisplayName().equalsIgnoreCase(SpectatorPlusOld.TOOL_NIGHT_VISION_ACTIVE_NAME)
							|| toolSelected.getItemMeta().getDisplayName().equalsIgnoreCase(SpectatorPlusOld.TOOL_NIGHT_VISION_INACTIVE_NAME)) {
						if(spectator.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
							spectator.removePotionEffect(PotionEffectType.NIGHT_VISION);
							spectator.removePotionEffect(PotionEffectType.WATER_BREATHING);
						}
						else {
							spectator.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0), true);
							spectator.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0), true);
						}
					}
					else if(toolSelected.getItemMeta().getDisplayName().equalsIgnoreCase(SpectatorPlusOld.TOOL_DIVING_SUIT_NAME)) {
						if(spectator.getInventory().getBoots() != null && spectator.getInventory().getBoots().getType() == Material.DIAMOND_BOOTS) {
							spectator.getInventory().setBoots(null);
						}
						else {
							ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
							boots.addEnchantment(Enchantment.DEPTH_STRIDER, 3);
							spectator.getInventory().setBoots(boots);
						}
					}
					else if(toolSelected.getItemMeta().getDisplayName().equalsIgnoreCase(SpectatorPlusOld.TOOL_NOCLIP_NAME)) {
						p.getPlayerData(spectator).setGamemodeChangeAllowed(true);
						spectator.setGameMode(GameMode.SPECTATOR);
						p.getPlayerData(spectator).setGamemodeChangeAllowed(false);

						p.updateSpectatorInventory(spectator);

						spectator.sendMessage(ChatColor.GREEN + "No-clip mode enabled");
						spectator.sendMessage(ChatColor.GRAY + "Open your inventory to access controls or to quit the no-clip mode");
					}
					else if(toolSelected.getItemMeta().getDisplayName().equalsIgnoreCase(SpectatorPlusOld.TOOL_TP_TO_DEATH_POINT_NAME)) {
						spectator.teleport(p.getPlayerData(spectator).getDeathLocation().setDirection(spectator.getLocation().getDirection()));
					}
					
					spectator.closeInventory();
				} catch(NullPointerException ex) {
					// This happens if there isn't any meta, aka here if the spectator
					// clicks on an empty slot.
					// In this case, nothing happens, and the inventory is not closed.
				}
				
				return;
			}
			
			// No-clip inventory
			else if(e.getWhoClicked().getGameMode() == GameMode.SPECTATOR) {
				ItemStack toolSelected = e.getCurrentItem();
				Player spectator = (Player) e.getWhoClicked();
				
				try {
					if(toolSelected.getItemMeta().getDisplayName().equalsIgnoreCase(SpectatorPlusOld.TOOL_NIGHT_VISION_ACTIVE_NAME)
							|| toolSelected.getItemMeta().getDisplayName().equalsIgnoreCase(SpectatorPlusOld.TOOL_NIGHT_VISION_INACTIVE_NAME)) {
						if(spectator.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
							spectator.removePotionEffect(PotionEffectType.NIGHT_VISION);
							spectator.removePotionEffect(PotionEffectType.WATER_BREATHING);
						}
						else {
							spectator.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0), true);
							spectator.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0), true);
						}
						
						spectator.closeInventory();
						p.updateSpectatorInventory(spectator);
					}
					
					else if(toolSelected.getItemMeta().getDisplayName().startsWith(SpectatorPlusOld.TOOL_NOCLIP_QUIT_NAME)) {
						// Take care of the vanilla spectate mode - spectators should always be in SPECTATOR gamemode then.
						// (this item is removed from the GUI in this case, so technically this should never be needed)
						if(!p.vanillaSpectate) {
							spectator.setGameMode(GameMode.ADVENTURE);

							spectator.setAllowFlight(true);
							spectator.setFlying(true);

							spectator.closeInventory();
							p.updateSpectatorInventory(spectator);
						} else {
							spectator.sendMessage(SpectatorPlusOld.prefix+ChatColor.DARK_RED+"Exiting no-clip mode is disabled.");
						}
					}
				} catch(NullPointerException ex) {
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
	 * @param e
	 */
	@EventHandler
	public void onInventoryDrag(InventoryDragEvent e) {
		if (p.getPlayerData((Player) e.getWhoClicked()).isSpectating()) {
			e.setCancelled(true);
		}
	}
	
	/**
	 * Handle MultiverseInventories & other similar plugins<br>
	 * Disables spectate mode to restore proper inventory before world change; then<br>
	 * Re-enables spectate mode to restore spectator inventory after world change.
	 * 
	 * @param e
	 */
	@EventHandler(priority=EventPriority.LOWEST)
	public void onWorldChange(final PlayerChangedWorldEvent e) {
		if (p.getPlayerData((Player) e.getPlayer()).isSpectating()) {
			p.getPlayerData((Player) e.getPlayer()).setWasSpectatorBeforeWorldChanged(true);
			p.disableSpectate(e.getPlayer(), p.console, true, false, true);
			new BukkitRunnable() {
				@Override
				public void run() {
					// What you want to schedule goes here
					if (p.getPlayerData((Player) e.getPlayer()).wasSpectatorBeforeWorldChanged()) {
						p.enableSpectate(e.getPlayer(), p.console, true, true);
						p.getPlayerData((Player) e.getPlayer()).setWasSpectatorBeforeWorldChanged(false);
					}
				}
			}.runTaskLater(p, 1);
		}
	}
	
	/**
	 * Stops spectators riding horses, minecarts, etc.
	 * 
	 * @param e
	 */
	@EventHandler
	public void onVehicleEnter(VehicleEnterEvent e) {
		if (e.getEntered() instanceof Player && p.getPlayerData((Player) e.getEntered()).isSpectating()) {
			e.setCancelled(true);
		}
	}
}
