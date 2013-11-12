package com.pgcraft.spectatorplus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpectatorPlus extends JavaPlugin {
	String[] arenaNameStore = new String[100000];
	boolean[] specStore = new boolean[100000];
	boolean[] tpStore = new boolean[100000];
	int[] setupStore = new int[100000];
	int[] arenaStore = new int[100000];
	Location[] pos1store = new Location[100000];
	Location[] pos2store = new Location[100000];
	String basePrefix = ChatColor.BLUE + "Spectator" + ChatColor.DARK_BLUE + "Plus";
	String prefix = ChatColor.GOLD + "[" + basePrefix + ChatColor.GOLD + "] ";
	@Override
	public void onEnable() {
		this.saveDefaultConfig(); // save default config if none is there
		getServer().getPluginManager().registerEvents(new Listener() { 
            @EventHandler
            public void onPlayerJoin(PlayerJoinEvent event) {
            	// On player join - hide spectators from the joining player
    			for (Player target : getServer().getOnlinePlayers()) {
					if (specStore[target.getEntityId()] == true) {
						event.getPlayer().hidePlayer(target);
					}
				}
            }
            @EventHandler
            public void onBlockPlace(BlockPlaceEvent event) {
                // On block place - cancel if the player is a spectator
            	if (specStore[event.getPlayer().getEntityId()] == true) {
            		event.setCancelled(true);
            		event.getPlayer().sendMessage(prefix + "You cannot place blocks while in spectate mode!");
            	}
            }
            @EventHandler
            public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
                // On entity hit - are they a spectator or were they hit by a spectator?
            	if (specStore[event.getDamager().getEntityId()] == true) {
            		event.setCancelled(true);
            	}
            	if (specStore[event.getEntity().getEntityId()] == true) {
            		event.setCancelled(true);
            	}
            }
            @EventHandler
            public void onBlockBreak(BlockBreakEvent event) {
                // On block break - Cancel if the player is a spectator. Fires only when the block is fully broken
            	if (specStore[event.getPlayer().getEntityId()] == true) {
            		event.setCancelled(true);
            		event.getPlayer().sendMessage(prefix + "You cannot break blocks while in spectate mode!");
            	}
            	// Set up mode
            	if(modeSetup(event.getPlayer(), event.getBlock()) == true) {
            		event.setCancelled(true);
            	}
            }
            @EventHandler
            public void onPlayerDropItem(PlayerDropItemEvent event) {
                // On player drop item - Cancel if the player is a spectator
            	if (specStore[event.getPlayer().getEntityId()] == true) {
            		event.setCancelled(true);
            	}
            }
            @EventHandler
            public void onPlayerPickupItem(PlayerPickupItemEvent event) {
                // On player pickup item - Cancel if the player is a spectator
            	if (specStore[event.getPlayer().getEntityId()] == true) {
            		event.setCancelled(true);
            	}
            }
            @EventHandler
            public void onEntityTarget(EntityTargetEvent event) {
                // On entity target - Stop mobs targeting spectators
            	if (event.getTarget() != null && specStore[event.getTarget().getEntityId()] == true) {
            		event.setCancelled(true);
            	}
            }
            @EventHandler
            public void onBlockDamage(BlockDamageEvent event) {
                // On block damage - Cancels the block damage animation
            	if (specStore[event.getPlayer().getEntityId()] == true) {
            		event.setCancelled(true);
            		event.getPlayer().sendMessage(prefix + "You cannot break blocks while in spectate mode!");
            	}
            	// Set up mode
            	if (modeSetup(event.getPlayer(), event.getBlock()) == true) {
            		event.setCancelled(true);
            	}
            }
            @EventHandler
            public void onEntityDamage(EntityDamageEvent event) {
                // On block damage - Cancels the block damage animation
            	if (event.getEntity() instanceof Player && specStore[getServer().getPlayer(((Player) event.getEntity()).getName()).getEntityId()] == true) {
            		event.setCancelled(true);
            	}
            	if (event.getEntity() instanceof Player && tpStore[getServer().getPlayer(((Player) event.getEntity()).getName()).getEntityId()] == true) {
            		event.setCancelled(true);
            	}
            }
            @EventHandler
            public void onFoodLevelChange(FoodLevelChangeEvent event) {
                // On food loss - Cancels the food loss
            	if (event.getEntityType() == EntityType.PLAYER && specStore[event.getEntity().getEntityId()] == true) {
            		event.setCancelled(true);
            		getServer().getPlayer(event.getEntity().getName()).setFoodLevel(20);
            	}
            }
            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent event) {
        		for (Player target : getServer().getOnlinePlayers()) {
        			target.showPlayer(event.getPlayer()); // show the leaving player to everyone
        			event.getPlayer().showPlayer(target); // show the leaving player everyone
        			event.getPlayer().removePotionEffect(PotionEffectType.HEAL);
        			
        		}
        		Player spectator = event.getPlayer();
        		if (specStore[spectator.getEntityId()] == true) {
        			spawnPlayer(spectator);
        			spectator.setGameMode(getServer().getDefaultGameMode());
        			spectator.getInventory().clear();
        			specStore[spectator.getEntityId()] = false;
        		}
            }
            @EventHandler
            public void onPlayerInteract(PlayerInteractEvent event) {
        		if (specStore[event.getPlayer().getEntityId()] == true && event.getMaterial() == Material.COMPASS && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
        			String mode = getConfig().getString("mode"); 
        			if (mode.equals("arena")) {
        				int region = arenaStore[event.getPlayer().getEntityId()];
        				tpPlayer(event.getPlayer(), region);
        			} else {
        				tpPlayer(event.getPlayer(), 0);
        			}
        		}
        		if (specStore[event.getPlayer().getEntityId()] == true && event.getMaterial() == Material.WRITTEN_BOOK && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
        			event.setCancelled(true);
        			arenaSelect(event.getPlayer());
        		}
        		if (specStore[event.getPlayer().getEntityId()] == true) {
        			event.setCancelled(true);
        		}
            }
            @EventHandler
            public void onInventoryClick(InventoryClickEvent event) {
        		if (specStore[event.getWhoClicked().getEntityId()] == true) {
        			if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.SKULL_ITEM && event.getCurrentItem().getDurability() == 3) {
						ItemStack playerhead = event.getCurrentItem();
						SkullMeta meta = (SkullMeta)playerhead.getItemMeta();
						Player skullOwner = getServer().getPlayer(meta.getOwner());
						event.getWhoClicked().closeInventory();
						if (skullOwner != null && skullOwner.isOnline() == true && skullOwner.getAllowFlight() == false) {
							event.getWhoClicked().teleport(skullOwner);
		                   	((Player)event.getWhoClicked()).sendMessage(prefix + "Teleported you to " + ChatColor.RED + skullOwner.getName());
						} else {
							if (skullOwner == null) {
								OfflinePlayer offlineSkullOwner = getServer().getOfflinePlayer(meta.getOwner());
								((Player) event.getWhoClicked()).sendMessage(prefix + ChatColor.RED + offlineSkullOwner.getName() + ChatColor.GOLD + " is not online!");
							} else if (skullOwner.getAllowFlight() == true) {
								((Player) event.getWhoClicked()).sendMessage(prefix + ChatColor.RED + skullOwner.getName() + ChatColor.GOLD + " is currently spectating!");
							}
						}
        			}
        			//manage arenaSelect choice
        			if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.WRITTEN_BOOK) {
						ItemStack arenaBook = event.getCurrentItem();
						ItemMeta meta = (ItemMeta)arenaBook.getItemMeta();
						String chosenArena = meta.getDisplayName();
						event.getWhoClicked().closeInventory();

						if (arenaBook != null) {
							// find out the arena id #
							for (int i=1; i<getConfig().getInt("nextarena"); i++) {
								if (getConfig().getString("arena." + i + ".name") == chosenArena) {
									arenaStore[event.getWhoClicked().getEntityId()] = i;
								}
							}
							double tpPosY = getConfig().getDouble("arena." + arenaStore[event.getWhoClicked().getEntityId()] + ".lobby.y", event.getWhoClicked().getLocation().getY());
							double tpPosX = getConfig().getDouble("arena." + arenaStore[event.getWhoClicked().getEntityId()] + ".lobby.x", event.getWhoClicked().getLocation().getX());
							double tpPosZ = getConfig().getDouble("arena." + arenaStore[event.getWhoClicked().getEntityId()] + ".lobby.z", event.getWhoClicked().getLocation().getZ());
							World tpWorld = getServer().getWorld(getConfig().getString("arena." + arenaStore[event.getWhoClicked().getEntityId()] + ".lobby.world", event.getWhoClicked().getWorld().getName()));
							Location where = new Location(tpWorld, tpPosX, tpPosY, tpPosZ);

							if (tpWorld == event.getWhoClicked().getWorld()) {
								double dist = where.distance(event.getWhoClicked().getLocation());
								if (dist <= 1) {
									((Player)event.getWhoClicked()).sendMessage(prefix + "No lobby location set for " + ChatColor.RED + chosenArena);
								} else {
									((Player)event.getWhoClicked()).sendMessage(prefix + "Teleported you to " + ChatColor.RED + chosenArena);
									event.getWhoClicked().teleport(where);
								}
							} else {
								((Player)event.getWhoClicked()).sendMessage(prefix + "Teleported you to " + ChatColor.RED + chosenArena);
								event.getWhoClicked().teleport(where);
							}
							

						}
        			}
        			event.setCancelled(true);
        		}
            }
		}, this);
 
        this.getCommand("spectate").setExecutor(commands);
        this.getCommand("spec").setExecutor(commands);
	}
	
	public void onDisable() {
		for (Player isSpec : getServer().getOnlinePlayers()) { // treat everyone as if they're spectator
			for (Player target : getServer().getOnlinePlayers()) { // show everyone that player
				target.showPlayer(isSpec);
			}
		}
		for (Player player: getServer().getOnlinePlayers()) {
			if (specStore[player.getEntityId()] == true) {
				spawnPlayer(player);
				player.removePotionEffect(PotionEffectType.HEAL);
				player.setAllowFlight(false);
				player.setGameMode(getServer().getDefaultGameMode());
				player.getInventory().clear();

			}
		}
	}
	
	// --------------
	// CUSTOM METHODS
	// --------------
	
	// custom method to spawn the player in the lobby
	void spawnPlayer(Player player) {
		player.setFireTicks(0);
		if (getConfig().getBoolean("active") == true) {
			Location where = new Location(getServer().getWorld(getConfig().getString("world")), getConfig().getDouble("xPos"), getConfig().getDouble("yPos"), getConfig().getDouble("zPos"));
			Location aboveWhere = new Location(getServer().getWorld(getConfig().getString("world")), getConfig().getDouble("xPos"), getConfig().getDouble("yPos") + 1, getConfig().getDouble("zPos"));
			Location belowWhere = new Location(getServer().getWorld(getConfig().getString("world")), getConfig().getDouble("xPos"), getConfig().getDouble("yPos") - 1, getConfig().getDouble("zPos"));
			if (where.getBlock().getType() != Material.AIR || aboveWhere.getBlock().getType() != Material.AIR || (belowWhere.getBlock().getType() == Material.AIR || belowWhere.getBlock().getType() == Material.LAVA || belowWhere.getBlock().getType() == Material.WATER)) {
				while (where.getBlock().getType() != Material.AIR || aboveWhere.getBlock().getType() != Material.AIR || (belowWhere.getBlock().getType() == Material.AIR || belowWhere.getBlock().getType() == Material.LAVA || belowWhere.getBlock().getType() == Material.WATER)) {
					where.setY(where.getY()+1);
					aboveWhere.setY(aboveWhere.getY()+1);
					belowWhere.setY(belowWhere.getY()+1);
					if (where.getY() > getServer().getWorld(getConfig().getString("world")).getHighestBlockYAt(where)) {
						where.setY(where.getY()-2);
						aboveWhere.setY(aboveWhere.getY()-2);
						belowWhere.setY(belowWhere.getY()-2);
					}
				}
			}
			tpStore[player.getEntityId()] = true;
			player.teleport(where);

			tpStore[player.getEntityId()] = false;
		} else {
			player.performCommand("spawn");
		}
	}
	
	// player head inventory method 
	void tpPlayer(Player spectator, int region) {
		Inventory gui = Bukkit.getServer().createInventory(spectator, 27, basePrefix);
		for (Player player : getServer().getOnlinePlayers()) {
			if (getConfig().getString("mode").equals("any")) {
				if (player.hasPermission("spectate.hide") == false && specStore[player.getEntityId()] == false) {
					ItemStack playerhead = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
					SkullMeta meta = (SkullMeta)playerhead.getItemMeta();
					meta.setOwner(player.getName());
					meta.setDisplayName(player.getName());
					playerhead.setItemMeta(meta);
					gui.addItem(playerhead);
				}
			} else if (getConfig().getString("mode").equals("arena")) {
				Location where = player.getLocation();
				int pos1y = getConfig().getInt("arena." + region + ".1.y");
				int pos2y = getConfig().getInt("arena." + region + ".2.y");
				int pos1x = getConfig().getInt("arena." + region + ".1.x");
				int pos2x = getConfig().getInt("arena." + region + ".2.x");
				int pos1z = getConfig().getInt("arena." + region + ".1.z");
				int pos2z = getConfig().getInt("arena." + region + ".2.z");
				// pos1 should have the highest co-ords of the arena, pos2 the lowest
				if (player.hasPermission("spectate.hide") == false && specStore[player.getEntityId()] == false) {
					if (Math.floor(where.getY()) < Math.floor(pos1y) && Math.floor(where.getY()) > Math.floor(pos2y)) {
						if (Math.floor(where.getX()) < pos1x && Math.floor(where.getX()) > pos2x) {
							if (Math.floor(where.getZ()) < pos1z && Math.floor(where.getZ()) > pos2z) {
								ItemStack playerhead = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
								SkullMeta meta = (SkullMeta)playerhead.getItemMeta();
								meta.setOwner(player.getName());
								meta.setDisplayName(player.getName());
								playerhead.setItemMeta(meta);
								gui.addItem(playerhead);
							}
						}
					}
				}
			}
			spectator.openInventory(gui);
		}

	}
	
	void arenaSelect(Player spectator) {
		Inventory gui = Bukkit.getServer().createInventory(spectator, 27, basePrefix);
		for (int i=1; i<getConfig().getInt("nextarena"); i++) {
			ItemStack arenaBook = new ItemStack(Material.WRITTEN_BOOK, 1);
			ItemMeta meta = (ItemMeta)arenaBook.getItemMeta();
			meta.setDisplayName(getConfig().getString("arena." + i + ".name"));
			arenaBook.setItemMeta(meta);
			gui.addItem(arenaBook);
		}
		spectator.openInventory(gui);
	}

	
	// command
	CommandExecutor commands = new CommandExecutor() {
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            // On command: to the sender of the command
        	if (sender instanceof Player && sender.hasPermission("spectate.use")) {
        		Player spectator = getServer().getPlayer(sender.getName());
        		// on spectate disable
        		if (args.length == 1 && args[0].equals("off")) {
        			// show them to everyone
        			for (Player target : getServer().getOnlinePlayers()) { 
        				target.showPlayer(spectator);
        			}
        			
        			// teleport to spawn
        			spawnPlayer(spectator);
        			
        			// allow interaction
        			specStore[spectator.getEntityId()] = false;
        			spectator.setGameMode(getServer().getDefaultGameMode());
        			spectator.setAllowFlight(false);
        			spectator.removePotionEffect(PotionEffectType.HEAL);
        			spectator.getInventory().clear();

        			spectator.sendMessage(prefix + "Spectator mode " + ChatColor.RED + "disabled");
        		} else if (args.length == 1 && args[0].equals("lobby")) {
        			if (sender.hasPermission("spectate.set")) {
        				spectator.sendMessage(prefix + "Usage: " + ChatColor.RED + "/spectate lobby <set/del>");
        			} else {
        				spectator.sendMessage(prefix + "You do not have permission to change the mode!");
        			}
        		} else if (args.length == 2 && args[0].equals("lobby") && args[1].equals("set")) {
        			if (sender.hasPermission("spectate.set")) {
        				Location where = spectator.getLocation();
        				getConfig().set("xPos", Math.floor(where.getX())+0.5);
        				getConfig().set("yPos", Math.floor(where.getY()));
        				getConfig().set("zPos", Math.floor(where.getZ())+0.5);
        				getConfig().set("world", where.getWorld().getName());
        				getConfig().set("active", true);
        				SpectatorPlus.this.saveConfig();
        				spectator.sendMessage(prefix + "Location saved! Players will be teleported here when they spectate");
        			} else {
        				spectator.sendMessage(prefix + "You do not have permission to set the lobby location!");
        			}
        		} else if (args.length == 2 && args[0].equals("lobby") && (args[1].equals("del") || args[1].equals("delete"))) {
        			if (sender.hasPermission("spectate.set")) {
        				getConfig().set("xPos", 0);
        				getConfig().set("yPos", 0);
        				getConfig().set("zPos", 0);
        				getConfig().set("world", null);
        				getConfig().set("active", false);
        				SpectatorPlus.this.saveConfig();
        				spectator.sendMessage(prefix + "Spectator lobby location removed! Players will be teleported to spawn when they spectate");
        			} else {
        				spectator.sendMessage(prefix + "You do not have permission to set the lobby location!");
        			}
        		} else if (args.length == 1 && args[0].equals("mode")) {
        			if (sender.hasPermission("spectate.set")) {
        				spectator.sendMessage(prefix + "Usage: " + ChatColor.RED + "/spectate mode <arena/any>");
        			} else {
        				spectator.sendMessage(prefix + "You do not have permission to change the mode!");
        			}
        		} else if (args.length == 2 && args[0].equals("mode") && args[1].equals("any")) {
        			if (sender.hasPermission("spectate.set")) {
        				getConfig().set("mode", "any");
        				SpectatorPlus.this.saveConfig();
        				spectator.sendMessage(prefix + "Mode set to " + ChatColor.RED + "any");
        			} else {
        				spectator.sendMessage(prefix + "You do not have permission to change the mode!");
        			}
        		} else if (args.length == 2 && args[0].equals("mode") && args[1].equals("arena")) {
        			if (sender.hasPermission("spectate.set")) {
        				getConfig().set("mode", "arena");
        				SpectatorPlus.this.saveConfig();
        				spectator.sendMessage(prefix + "Mode set to " + ChatColor.RED + "arena" + ChatColor.GOLD + ". Only players in arena regions can be teleported to by spectators.");
        			} else {
        				spectator.sendMessage(prefix + "You do not have permission to change the mode!");
        			}
        		} else if ((args.length == 1 && args[0].equals("arena")) || (args.length == 2 && args[0].equals("arena") && args[1].equals("add"))) {
        			if (sender.hasPermission("spectate.set")) {
        				spectator.sendMessage(prefix + "Usage: " + ChatColor.RED + "/spectate arena <add <name>/reset/lobby <id>/list>");
        			} else {
        				spectator.sendMessage(prefix + "You do not have permission to change the mode!");
        			}
        		} else if (args.length == 3 && args[0].equals("arena") && args[1].equals("add")) {
        			if (sender.hasPermission("spectate.set")) {
        				arenaNameStore[spectator.getEntityId()] = args[2];
        				spectator.sendMessage(prefix + "Punch point " + ChatColor.RED + "#1" + ChatColor.GOLD + " - the corner with the highest co-ordinates");
        				setupStore[spectator.getEntityId()] = 1;
        			} else {
        				spectator.sendMessage(prefix + "You do not have permission to change the mode!");
        			}
        		} else if (args.length == 2 && args[0].equals("arena") && args[1].equals("list")) {
        			if (sender.hasPermission("spectate.set")) {
    					spectator.sendMessage(ChatColor.GOLD + "          ~~ " + ChatColor.RED + "Arenas" + ChatColor.GOLD + " ~~          ");
        				for (int i=1; i<getConfig().getInt("nextarena"); i++) {
        					spectator.sendMessage(ChatColor.RED + "(#" + i + ") " + getConfig().getString("arena." + i + ".name") + ChatColor.GOLD + " Lobby x:" + getConfig().getDouble("arena." + i + ".lobby.x") + " y:" + getConfig().getDouble("arena." + i + ".lobby.y") + " z:" + getConfig().getDouble("arena." + i + ".lobby.z"));
        				}
        			} else {
        				spectator.sendMessage(prefix + "You do not have permission to change the mode!");
        			}
        		} else if (args.length == 3 && args[0].equals("arena") && args[1].equals("lobby")) {
        			if (sender.hasPermission("spectate.set")) {
        				int region = Integer.parseInt(args[2]);
        				lobbySetup(spectator, region);
        			} else {
        				spectator.sendMessage(prefix + "You do not have permission to change the mode!");
        			}
        		} else if (args.length == 2 && args[0].equals("arena") && args[1].equals("reset")) {
        			if (sender.hasPermission("spectate.set")) {
        				getConfig().set("arena", null);
        				getConfig().set("nextarena", 1);
        				SpectatorPlus.this.saveConfig();
        				spectator.sendMessage(prefix + "All arenas removed.");
        			} else {
        				spectator.sendMessage(prefix + "You do not have permission to change the mode!");
        			}
        		} else if (args.length == 0) {
        			// teleport them to the global lobby
    				spawnPlayer(spectator);
    				// tell them spectator mode is enabled
        			spectator.sendMessage(prefix + "Spectator mode " + ChatColor.RED + "enabled");
        			// hide them from everyone
					for (Player target : getServer().getOnlinePlayers()) {
						target.hidePlayer(spectator);
					}
					// gamemode and inventory
					spectator.setGameMode(GameMode.ADVENTURE);
					/*invStore[spectator.getEntityId()] = Bukkit.getServer().createInventory(spectator, 36);
					invStore[spectator.getEntityId()].addItem(spectator.getInventory().getContents());*/
					spectator.getInventory().clear();
					spectator.setAllowFlight(true);
					spectator.setFoodLevel(20);
					// disable interaction
					specStore[spectator.getEntityId()] = true;
					PotionEffect heal = new PotionEffect(PotionEffectType.HEAL, Integer.MAX_VALUE, 1000, true);
					spectator.addPotionEffect(heal);
					// give them compass
					ItemStack compass = new ItemStack(Material.COMPASS, 1);
					ItemMeta compassMeta = (ItemMeta)compass.getItemMeta();
					compassMeta.setDisplayName(ChatColor.BLUE + "Teleporter");
					compass.setItemMeta(compassMeta);
					spectator.getInventory().addItem(compass);
					// give them book (only for arena mode)
					String mode = getConfig().getString("mode");
					if (mode.equals("arena")) {
						ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
						ItemMeta bookMeta = (ItemMeta)book.getItemMeta();
						bookMeta.setDisplayName(ChatColor.DARK_RED + "Arena chooser");
						book.setItemMeta(bookMeta);
						spectator.getInventory().addItem(book);
					}
        		} else {
    				spectator.sendMessage(ChatColor.GOLD + "            ~~ " + ChatColor.BLUE + "Spectator" + ChatColor.DARK_BLUE + "Plus" + ChatColor.GOLD + " ~~            ");
    				spectator.sendMessage(ChatColor.RED + "/spectate [off]" + ChatColor.GOLD + ": Enables/disables spectator mode");
    				spectator.sendMessage(ChatColor.RED + "/spectate arena <add <name>/reset/lobby <id>/list>" + ChatColor.GOLD + ": Adds/deletes arenas");
    				spectator.sendMessage(ChatColor.RED + "/spectate lobby <set/del>" + ChatColor.GOLD + ": Adds/deletes the spectator lobby");
    				spectator.sendMessage(ChatColor.RED + "/spectate mode <any/arena>" + ChatColor.GOLD + ": Sets who players can teleport to");
        		}
        	} else {
        		if (sender instanceof Player) {
        			sender.sendMessage(prefix + "You don't have permission to spectate!");
        		} else {
        			sender.sendMessage(prefix + "Only players can exectute this command!");
        		}
        	}
        	return true; // return true: to stop usage showing
        } // end of onCommand
	};
	boolean modeSetup(Player player, Block block) {
    	if (setupStore[player.getEntityId()] == 2) {
    		pos2store[player.getEntityId()] = block.getLocation();
    		setupStore[player.getEntityId()] = 0;
    		
			getConfig().set("arena." + getConfig().getInt("nextarena") + ".1.y", Math.floor(pos1store[player.getEntityId()].getY()));
			getConfig().set("arena." + getConfig().getInt("nextarena") + ".1.x", Math.floor(pos1store[player.getEntityId()].getX()));
			getConfig().set("arena." + getConfig().getInt("nextarena") + ".1.z", Math.floor(pos1store[player.getEntityId()].getZ()));
			getConfig().set("arena." + getConfig().getInt("nextarena") + ".2.y", Math.floor(pos2store[player.getEntityId()].getY()));
			getConfig().set("arena." + getConfig().getInt("nextarena") + ".2.x", Math.floor(pos2store[player.getEntityId()].getX()));
			getConfig().set("arena." + getConfig().getInt("nextarena") + ".2.z", Math.floor(pos2store[player.getEntityId()].getZ()));
			getConfig().set("arena." + getConfig().getInt("nextarena") + ".name", arenaNameStore[player.getEntityId()]);
			getConfig().set("nextarena", getConfig().getInt("nextarena") + 1);
			SpectatorPlus.this.saveConfig();
			player.sendMessage(prefix + "Arena " + ChatColor.RED + arenaNameStore[player.getEntityId()] + " (#" + (getConfig().getInt("nextarena")-1) + ")" + ChatColor.GOLD + " successfully set up!");
			return true;
    	} else {
    		if (setupStore[player.getEntityId()] == 1) {
    		pos1store[player.getEntityId()] = block.getLocation();
			player.sendMessage(prefix + "Punch point " + ChatColor.RED + "#2" + ChatColor.GOLD + " - the corner with the lowest co-ordinates");
    		setupStore[player.getEntityId()] = 2;
    		return true;
    		} else {
    			return false;
    		}
    	}
	}
	void lobbySetup(Player player, int arenaNum) {
		getConfig().set("arena." + arenaNum + ".lobby.y", Math.floor(player.getLocation().getY()));
		getConfig().set("arena." + arenaNum + ".lobby.x", Math.floor(player.getLocation().getX()));
		getConfig().set("arena." + arenaNum + ".lobby.z", Math.floor(player.getLocation().getZ()));
		getConfig().set("arena." + arenaNum + ".lobby.world", player.getWorld().getName());
		player.sendMessage(prefix + "Arena " + ChatColor.RED + "#" + arenaNum + ChatColor.GOLD + "'s lobby location set to your location");
	}
}
