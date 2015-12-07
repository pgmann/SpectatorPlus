package com.pgcraft.spectatorplus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class SpectatorPlus extends JavaPlugin
{
	private static SpectatorPlus instance;

	protected HashMap <String, PlayerObject> user = new HashMap<>();

	public final static String basePrefix = ChatColor.BLUE + "Spectator" + ChatColor.DARK_BLUE + "Plus";
	public final static String prefix = ChatColor.GOLD + "[" + basePrefix + ChatColor.GOLD + "] ";

	public double version = 2.1; // Plugin version

	public ConsoleCommandSender console;

	public ConfigAccessor setup = null;
	public ConfigAccessor specs = null;

	public ToggleManager toggles = null;

	public SpectateCommand commands = null;

	public ArenasManager arenasManager = null;
	
	private SpectateAPI api = null;

	public DecimalFormat format;

	// Manage toggles
	public Boolean compass;
	public Material compassItem;
	public Boolean clock;
	public Material clockItem;
	public Boolean spectatorsTools;
	public Material spectatorsToolsItem;
	public Boolean inspector;
	public Material inspectorItem;
	public Boolean tpToDeathTool, tpToDeathToolShowCause, divingSuitTool, nightVisionTool, noClipTool, speedTool, glowOnActiveTools, inspectFromTPMenu, playersHealthInTeleportationMenu, playersLocationInTeleportationMenu, specChat, scoreboard, vanillaSpectate, output, death, seeSpecs, blockCmds, adminBypass, newbieMode, teleportToSpawnOnSpecChangeWithoutLobby, useSpawnCommandToTeleport, enforceArenaBoundary, skriptInt;

	public SpectatorMode mode = SpectatorMode.ANY;

	public ScoreboardManager manager = null;
	public Scoreboard board = null;
	public Team team = null;
	
	// Constants for inventory title names
	public final static String TELEPORTER_ANY_TITLE = ChatColor.BLACK + "Teleporter";
	public final static String TELEPORTER_ARENA_TITLE = ChatColor.BLACK + "Arena "; // (Prefix only)
	public final static String ARENA_SELECTOR_TITLE = basePrefix;
	public final static String PLAYER_STATE_TITLE = ChatColor.RESET + "'s state"; // (Suffix only)
	public final static String SPEC_TOOLS_TITLE = ChatColor.BLACK + "Spectators' tools";
	
	// Constants used for identification of the spectators' tools in the listener
	public final static String TOOL_NORMAL_SPEED_NAME = ChatColor.DARK_AQUA + "Normal speed";
	public final static String TOOL_SPEED_I_NAME   = ChatColor.AQUA + "Speed I";
	public final static String TOOL_SPEED_II_NAME  = ChatColor.AQUA + "Speed II";
	public final static String TOOL_SPEED_III_NAME = ChatColor.AQUA + "Speed III";
	public final static String TOOL_SPEED_IV_NAME  = ChatColor.AQUA + "Speed IV";
	public final static String TOOL_NIGHT_VISION_INACTIVE_NAME = ChatColor.GOLD + "Enable night vision";
	public final static String TOOL_NIGHT_VISION_ACTIVE_NAME = ChatColor.DARK_PURPLE + "Disable night vision";
	public final static String TOOL_DIVING_SUIT_NAME = ChatColor.BLUE + "Diving Suit";
	public final static String TOOL_NOCLIP_NAME = ChatColor.LIGHT_PURPLE + "No-clip mode";
	public final static String TOOL_NOCLIP_QUIT_NAME = ChatColor.DARK_GREEN + "Go back to the real "; //... spectator's name
	public final static String TOOL_TP_TO_DEATH_POINT_NAME = ChatColor.YELLOW + "Go to your death point";

	/**
	 * This method is not meant for public use.
	 */
	@Override
	public void onLoad() {
		// Registers the Arena class as a serializable one.
		ConfigurationSerialization.registerClass(Arena.class);
	}
	
	/**
	 * This method is not meant for public use.
	 */
	@Override
	public void onEnable() {
		setup = new ConfigAccessor(this, "setup");
		toggles = new ToggleManager(this, new ConfigAccessor(this, "toggles"));
		specs = new ConfigAccessor(this, "spectators");
		
		console = getServer().getConsoleSender();
		
		arenasManager = new ArenasManager(this);
		api = new SpectateAPI(this);
		
		try {
			mode = SpectatorMode.fromString(setup.getConfig().getString("mode"));
		} catch(IllegalArgumentException e) {
			getLogger().warning("The spectator mode set in the config (" + setup.getConfig().getString("mode") + ") is invalid; using the ANY mode instead!");
			setSpectatorMode(SpectatorMode.ANY);
		}
		
		reloadConfig(true); // Load config values.
		
		// Add players already online to this plugin's database
		for (Player player : getServer().getOnlinePlayers()) {
			user.put(player.getName(), new PlayerObject());
		}
		
		// Re-enable spectate mode if necessary
		for(Player player : getServer().getOnlinePlayers()) {
			if (specs.getConfig().contains(player.getName())) {
				enableSpectate(player, player, true);
			}
		}

		// Register event listeners
		getServer().getPluginManager().registerEvents(new SpectateListener(this), this);
		new SpectatorManagerTask(this).runTaskTimer(this, 20, 20);

		if(output) {
			console.sendMessage(prefix + "Version " + ChatColor.RED + version + ChatColor.GOLD + " is enabled!");
		}

		this.commands = new SpectateCommand(this);
		this.getCommand("spectate").setExecutor(commands);
		this.getCommand("spec").setExecutor(commands);
		
		SpectateCompleter completer = new SpectateCompleter(this);
		this.getCommand("spectate").setTabCompleter(completer);
		this.getCommand("spec").setTabCompleter(completer);
		format = new DecimalFormat("0.0");
	}

	/**
	 * This method is not meant for public use.
	 */
	@Override
	public void onDisable() {
		if(output) {
			console.sendMessage(prefix + "Disabling...");
		}
		for (Player player : getServer().getOnlinePlayers()) {
			for (Player target : getServer().getOnlinePlayers()) {
				target.showPlayer(player);
			}
			// Disable spectate mode temporarily.
			if (getPlayerData(player).spectating) {
				disableSpectate(player, console, true, true);
			}
		}
		
		// Just in case
		arenasManager.save();
	}

	// ---------------
	//     METHODS
	// ---------------

	/**
	 * Teleports the player to the global lobby location.
	 * 
	 * @param player
	 * @return true if the player was  teleported, false else.
	 */
	protected boolean spawnPlayer(Player player) {
		player.setFireTicks(0);
		if (setup.getConfig().getBoolean("active")) {
			Location where = new Location(getServer().getWorld(setup.getConfig().getString("world")), setup.getConfig().getDouble("xPos"), setup.getConfig().getDouble("yPos"), setup.getConfig().getDouble("zPos"));
			Location aboveWhere = new Location(getServer().getWorld(setup.getConfig().getString("world")), setup.getConfig().getDouble("xPos"), setup.getConfig().getDouble("yPos") + 1, setup.getConfig().getDouble("zPos"));
			Location belowWhere = new Location(getServer().getWorld(setup.getConfig().getString("world")), setup.getConfig().getDouble("xPos"), setup.getConfig().getDouble("yPos") - 1, setup.getConfig().getDouble("zPos"));
			if (where.getBlock().getType() != Material.AIR || aboveWhere.getBlock().getType() != Material.AIR || (belowWhere.getBlock().getType() == Material.AIR || belowWhere.getBlock().getType() == Material.LAVA || belowWhere.getBlock().getType() == Material.WATER)) {
				while (where.getBlock().getType() != Material.AIR || aboveWhere.getBlock().getType() != Material.AIR || (belowWhere.getBlock().getType() == Material.AIR || belowWhere.getBlock().getType() == Material.LAVA || belowWhere.getBlock().getType() == Material.WATER)) {
					where.setY(where.getY()+1);
					aboveWhere.setY(aboveWhere.getY()+1);
					belowWhere.setY(belowWhere.getY()+1);
					if (where.getY() > getServer().getWorld(setup.getConfig().getString("world")).getHighestBlockYAt(where)) {
						where.setY(where.getY()-2);
						aboveWhere.setY(aboveWhere.getY()-2);
						belowWhere.setY(belowWhere.getY()-2);
					}
				}
			}
			getPlayerData(player).teleporting = true;
			player.teleport(where);
			getPlayerData(player).teleporting = false;
			return true;
		} else {
			if(teleportToSpawnOnSpecChangeWithoutLobby) {
				if(useSpawnCommandToTeleport) {
					if(getServer().getPluginCommand("spawn") != null) {
						return player.performCommand("spawn");
					}
					return false;
				}
				else {
					return player.teleport(player.getWorld().getSpawnLocation(), TeleportCause.PLUGIN);
				}
			}
			return false;
		}
	}


	/**
	 * Generates a player head ItemStack to be displayed in the teleportation GUI.
	 * 
	 * @param player The player
	 * @param inventoryViewer The spectator viewing the inventory (position relative to that player).
	 * @param hidden True if the player is a displayed hidden player.
	 * @return The head.
	 */
	protected ItemStack generatePlayerHeadItem(Player player, Player inventoryViewer, boolean hidden) {
		ItemStack playerhead = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta)playerhead.getItemMeta();
		
		meta.setOwner(player.getName());
		
		if(hidden) meta.setDisplayName(ChatColor.DARK_GRAY + "[HIDDEN] " + ChatColor.RESET + player.getDisplayName());
		else       meta.setDisplayName(ChatColor.RESET + player.getDisplayName());
		
		
		List<String> lore = new ArrayList<String>();
		
		if(playersHealthInTeleportationMenu || playersLocationInTeleportationMenu) {
			if(playersHealthInTeleportationMenu) {
				lore.add(ChatColor.GOLD + "" + ((int) ((Damageable) player).getHealth()) + " " + ChatColor.WHITE + "hearts" + ChatColor.GRAY + " out of 20");
			}
			
			if(playersLocationInTeleportationMenu) {
				if(!player.getWorld().equals(inventoryViewer.getWorld())) {
					lore.add(ChatColor.GRAY + "You and " + player.getName() + " are not in the same world.");
				}
				else {
					int distance = (int) player.getLocation().distance(inventoryViewer.getLocation());
					
					String direction = null;
					// The angle between a vector pointing to the North and a vector pointing
					// from the spectator to the player, converted in degrees, -180 to have 0° for North.
					double angle = (new Vector(0, 0, -1).angle(player.getLocation().toVector().setY(0).subtract(inventoryViewer.getLocation().toVector().setY(0)).multiply(-1)) * 180/Math.PI - 180) % 360;
					if(angle < 0) angle += 360.0;
					
					// The calculated angle is the same for two positions symmetric of each other
					// relative to the N-S axis.
					// This lead to "west" displayed for both east and west.
					if(inventoryViewer.getLocation().getX() < player.getLocation().getX()
							&& 202.5 <= angle && angle < 337.5) {
						angle -= 180.0;
					}
					
					if (0 <= angle && angle < 22.5) {
						direction = "North";
					} else if (22.5 <= angle && angle < 67.5) {
						direction =  "North-east";
					} else if (67.5 <= angle && angle < 112.5) {
						direction =  "East";
					} else if (112.5 <= angle && angle < 157.5) {
						direction =  "South-east";
					} else if (157.5 <= angle && angle < 202.5) {
						direction =  "South";
					} else if (202.5 <= angle && angle < 247.5) {
						direction =  "South-west";
					} else if (247.5 <= angle && angle < 292.5) {
						direction =  "West";
					} else if (292.5 <= angle && angle < 337.5) {
						direction =  "North-west";
					} else if (337.5 <= angle && angle <= 360.0) {
						direction =  "North";
					}
					
					if(direction != null) {
						lore.add(ChatColor.WHITE + direction + ", " + distance + " meters");
					}
					else {
						lore.add(ChatColor.WHITE + "" + distance + " meters");
					}
				}
			}
			
			lore.add(" "); // separator
		}
		
		lore.add(ChatColor.GOLD+""+ChatColor.ITALIC+"Left click"+ ChatColor.DARK_GRAY+ChatColor.ITALIC +" to be teleported");
		if(this.inspectFromTPMenu) {
			lore.add(ChatColor.GOLD+""+ChatColor.ITALIC+"Right click"+ ChatColor.DARK_GRAY+ChatColor.ITALIC +" to see inventory");
		}
		
		meta.setLore(lore);
		
		
		playerhead.setItemMeta(meta);
		return playerhead;
	}
	
	/**
	 * Opens the player head GUI, to allow spectators to choose a player to teleport to.
	 * 
	 * @param spectator The GUI will be open for this spectator.
	 * @param region The UUID of the arena to use to choose the players to display on the GUI. Null if there isn't any arena set for this player.
	 */
	protected void showGUI(Player spectator, UUID region) {
		
		if (mode == SpectatorMode.ARENA && region == null) {
			if(output) {
				spectator.sendMessage(prefix + "Pick an arena first using the arena selector!");
			}
			return;
		}
		
		Inventory gui = null;
		
		LinkedList<Player> displayedSpectators = new LinkedList<Player>();
		LinkedList<Player> displayedSpectatorsHidden = new LinkedList<Player>();
		
		for (Player player : getServer().getOnlinePlayers()) {
			if (mode == SpectatorMode.ARENA) {
				if (region == null) {
					if(output) {spectator.sendMessage(prefix + "Pick an arena first using the arena selector!");}
					return;
				} else {
					Location where = player.getLocation();
					Arena currentArena = arenasManager.getArena(region);
					int pos1y = currentArena.getCorner1().getBlockY();
					int pos2y = currentArena.getCorner2().getBlockY();
					int pos1x = currentArena.getCorner1().getBlockX();
					int pos2x = currentArena.getCorner2().getBlockX();
					int pos1z = currentArena.getCorner1().getBlockZ();
					int pos2z = currentArena.getCorner2().getBlockZ();
					// pos1 should have the highest co-ords of the arena, pos2 the lowest
					if (!getPlayerData(player).spectating && player.getWorld().equals(spectator.getWorld())) {
						if (Math.floor(where.getY()) < Math.floor(pos1y) && Math.floor(where.getY()) > Math.floor(pos2y)) {
							if (Math.floor(where.getX()) < pos1x && Math.floor(where.getX()) > pos2x) {
								if (Math.floor(where.getZ()) < pos1z && Math.floor(where.getZ()) > pos2z) {
									if(getPlayerData(player).hideFromTp) {
										displayedSpectatorsHidden.add(player);
									} else {
										displayedSpectators.add(player);
									}
								}
							}
						}
					}
				}
			}
			else if(mode == SpectatorMode.ANY
					|| (mode == SpectatorMode.WORLD && player.getWorld().equals(spectator.getWorld()))) {
				
				if (!getPlayerData(player).hideFromTp && !getPlayerData(player).spectating) {
					displayedSpectators.add(player);
				}
				
				// Admins will still be able to see players who have used '/spec hide':
				else if (spectator.hasPermission("spectate.admin.hide.see") && !getPlayerData(player).spectating) {
					displayedSpectatorsHidden.add(player);
				}
				
			}
		}
		
		Integer inventorySize = (int) Math.ceil(Double.valueOf(displayedSpectators.size()+displayedSpectatorsHidden.size())/9) * 9;
		if(inventorySize == 0) {
			inventorySize = 9; // Avoids an empty inventory.
		}
		
		if(mode == SpectatorMode.ARENA) {
			gui = Bukkit.getServer().createInventory(spectator, inventorySize, TELEPORTER_ARENA_TITLE + ChatColor.ITALIC + arenasManager.getArena(region).getName());
		}
		else { // WORLD and ANY
			gui = Bukkit.getServer().createInventory(spectator, inventorySize, TELEPORTER_ANY_TITLE);
		}
		
		// Display hidden players first (people who have used '/spec hide')
		for(Player displayedSpectatorHidden : displayedSpectatorsHidden) {
			gui.addItem(generatePlayerHeadItem(displayedSpectatorHidden, spectator, true));
		}
		
		// Display normal players
		for(Player displayedSpectator : displayedSpectators) {
			gui.addItem(generatePlayerHeadItem(displayedSpectator, spectator, false));
		}

		spectator.openInventory(gui);
	}


	/**
	 * Shows the arena selection GUI, full of books with the name of valid arenas.
	 * 
	 * @param spectator The GUI will be open for this spectator.
	 */
	protected void showArenaGUI(Player spectator) {
		Inventory gui = Bukkit.getServer().createInventory(spectator, 27, ARENA_SELECTOR_TITLE);


		for (Arena arena : arenasManager.getArenas()) {
			if(arena.isEnabled()) {
				ItemStack arenaBook = new ItemStack(Material.BOOK, 1);

				ItemMeta meta = arenaBook.getItemMeta();
				meta.setDisplayName(arena.getName());
				arenaBook.setItemMeta(meta);

				gui.addItem(arenaBook);
			}
		}

		spectator.openInventory(gui);
	}
	
	/**
	 * Shows a representation of the inventory, the armour, the health, the XP, the potion effects
	 * and the feed state of the player.
	 * 
	 * @param spectator The GUI will be open for this spectator.
	 * @param inventoryOwner The analyzed player is this player.
	 */
	protected void showPlayerInventoryGUI(Player spectator, Player inventoryOwner) {
		
		PlayerInventory inventory = inventoryOwner.getInventory();
		
		// Remove item name from the inventory separator.
		ItemStack separator = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
		ItemMeta separatorMeta = separator.getItemMeta();
		separatorMeta.setDisplayName(ChatColor.RESET+"");
		separator.setItemMeta(separatorMeta);
		
		// + 18: a separator row, and a row with armor, XP, potion effects, health and feed level.
		Inventory gui = Bukkit.getServer().createInventory(spectator, inventory.getSize() + 18, (inventoryOwner.getDisplayName().length() > 22) ? inventoryOwner.getName() : inventoryOwner.getDisplayName() + PLAYER_STATE_TITLE);
		ItemStack[] GUIContent = gui.getContents();
		
		// Player's inventory
		// The hotbar is 0-8
		// The inventory is 9-35
		for(int i = 9; i < inventory.getSize(); i++) {
			GUIContent[i - 9] = inventory.getItem(i);
		}
		for(int i = 0; i < 9; i++) {
			GUIContent[i + 27] = inventory.getItem(i);
		}
		
		// Separator
		for(int i = inventory.getSize(); i < inventory.getSize() + 9; i++) {
			GUIContent[i] = separator;
		}
		
		// Armor
		GUIContent[inventory.getSize() +  9] = inventory.getHelmet();
		GUIContent[inventory.getSize() + 10] = inventory.getChestplate();
		GUIContent[inventory.getSize() + 11] = inventory.getLeggings();
		GUIContent[inventory.getSize() + 12] = inventory.getBoots();
		
		// Separator
		GUIContent[inventory.getSize() + 13] = separator;
		
		// XP
		if(inventoryOwner.getLevel() > 0) {
			GUIContent[inventory.getSize() + 14] = new ItemStack(Material.EXP_BOTTLE, inventoryOwner.getLevel());
		}
		else {
			GUIContent[inventory.getSize() + 14] = new ItemStack(Material.EXP_BOTTLE, 1);
		}
		
		ItemMeta xpMeta = GUIContent[inventory.getSize() + 14].getItemMeta();
			xpMeta.setDisplayName(ChatColor.GREEN +""+ ChatColor.BOLD + "Experience");
			List<String> lore = new ArrayList<String>();
				lore.add(ChatColor.DARK_GRAY +""+ ChatColor.ITALIC + "Level " + ChatColor.GOLD + ChatColor.ITALIC + inventoryOwner.getLevel() + ChatColor.DARK_GRAY + ChatColor.ITALIC + " (" + ChatColor.GOLD + ChatColor.ITALIC + ((int) Math.floor(inventoryOwner.getExp()*100)) + ChatColor.DARK_GRAY + ChatColor.ITALIC + "% towards level " + ChatColor.GOLD + ChatColor.ITALIC + (inventoryOwner.getLevel()+1) + ChatColor.DARK_GRAY + ChatColor.ITALIC + ")");
			xpMeta.setLore(lore);
		GUIContent[inventory.getSize() + 14].setItemMeta(xpMeta);
		
		// Potion effects
		if(inventoryOwner.getActivePotionEffects().size() == 0) {
			GUIContent[inventory.getSize() + 15] = new ItemStack(Material.GLASS_BOTTLE, 1);
			ItemMeta meta = GUIContent[inventory.getSize() + 15].getItemMeta();
			meta.setLore(Arrays.asList(ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + "No active effects"));
			GUIContent[inventory.getSize() + 15].setItemMeta(meta);
		}
		else {
			GUIContent[inventory.getSize() + 15] = new Potion(PotionType.FIRE_RESISTANCE).toItemStack(1);
			PotionMeta effectsMeta = (PotionMeta) GUIContent[inventory.getSize() + 15].getItemMeta();
				effectsMeta.clearCustomEffects();
				lore = new ArrayList<String>();
					lore.add(ChatColor.GOLD +""+ ChatColor.ITALIC + inventoryOwner.getActivePotionEffects().size() + ChatColor.DARK_GRAY + ChatColor.ITALIC + " active effects");
				xpMeta.setLore(lore);
				for(PotionEffect effect : inventoryOwner.getActivePotionEffects()) {
					effectsMeta.addCustomEffect(effect, true);
				}
			GUIContent[inventory.getSize() + 15].setItemMeta(effectsMeta);
		}
		
		ItemMeta effectsMeta = GUIContent[inventory.getSize() + 15].getItemMeta();
			effectsMeta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Potion effects  ");
		GUIContent[inventory.getSize() + 15].setItemMeta(effectsMeta);
		
		// Health
		if(((Damageable) inventoryOwner).getHealth() > 0) {
			GUIContent[inventory.getSize() + 16] = new ItemStack(Material.GOLDEN_APPLE, (int) Math.ceil(((Damageable) inventoryOwner).getHealth()));
			ItemMeta healthMeta = GUIContent[inventory.getSize() + 16].getItemMeta();
				healthMeta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Health ");
				lore = new ArrayList<String>();
					lore.add(ChatColor.GOLD +"" + ChatColor.ITALIC + (int) Math.ceil(((Damageable) inventoryOwner).getHealth()) + ChatColor.DARK_GRAY + ChatColor.ITALIC + "/20");
				healthMeta.setLore(lore);
			GUIContent[inventory.getSize() + 16].setItemMeta(healthMeta);
		}
		
		// Food level
		if(inventoryOwner.getFoodLevel() > 0) {
			GUIContent[inventory.getSize() + 17] = new ItemStack(Material.COOKIE, inventoryOwner.getFoodLevel());
			ItemMeta foodMeta = GUIContent[inventory.getSize() + 17].getItemMeta();
				foodMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Food");
				lore = new ArrayList<String>();
					lore.add(ChatColor.DARK_GRAY +""+ ChatColor.ITALIC + "Food level: " + ChatColor.GOLD + ChatColor.ITALIC + inventoryOwner.getFoodLevel() + ChatColor.DARK_GRAY + ChatColor.ITALIC + "/20");
					lore.add(ChatColor.DARK_GRAY +""+ ChatColor.ITALIC + "Saturation: " + ChatColor.GOLD + ChatColor.ITALIC + format.format(inventoryOwner.getSaturation()));
				foodMeta.setLore(lore);
			GUIContent[inventory.getSize() + 17].setItemMeta(foodMeta);
		}
		
		gui.setContents(GUIContent);
		
		spectator.openInventory(gui);
	}
	
	
	protected void showSpectatorsOptionsGUI(Player spectator) {
		// We first need to know what is the size of the inventory
		
		// If a death location is registered for this player, and if every tool is
		// enabled, a line will have to be added.
		// That's why this is defined here, not below.
		// If the "tp to death" tool is disabled, the death location is not set. So it's useless to
		// check this here.
		Location deathPoint = getPlayerData(spectator).deathLocation;
		
		int height = 0, offset = 0;
		if(speedTool) {
			height++;
			offset = 9;
		}
		if(divingSuitTool || nightVisionTool || noClipTool || (tpToDeathTool && deathPoint != null)) height++;
		if(divingSuitTool && nightVisionTool && noClipTool && tpToDeathTool && deathPoint != null) height++;
		
		Inventory gui = Bukkit.getServer().createInventory(spectator, height * 9, SPEC_TOOLS_TITLE);
		ItemStack[] GUIContent = gui.getContents();
		
		// Retrieves the current speed level, and the other enabled effects
		// 0 = no speed; 1 = speed I, etc.
		Integer speedLevel = 0;
		Boolean nightVisionActive = false;
		for(PotionEffect effect : spectator.getActivePotionEffects()) {
			if(effect.getType().equals(PotionEffectType.SPEED)) {
				speedLevel = effect.getAmplifier() + 1; // +1 because Speed I = amplifier 0.
			}
			else if(effect.getType().equals(PotionEffectType.NIGHT_VISION)) {
				nightVisionActive = true;
			}
		}
		
		Boolean divingSuitEquiped = false;
		if(spectator.getInventory().getBoots() != null && spectator.getInventory().getBoots().getType() == Material.DIAMOND_BOOTS) {
			divingSuitEquiped = true;
		}
		
		List<String> activeLore = new ArrayList<String>();
		activeLore.add("" + ChatColor.GRAY + ChatColor.ITALIC + "Active");
		
		ItemMeta meta;
		
		if(speedTool) {
			
			// Normal speed
			ItemStack normalSpeed = new ItemStack(Material.STRING);
			meta = normalSpeed.getItemMeta();
			meta.setDisplayName(TOOL_NORMAL_SPEED_NAME);
			normalSpeed.setItemMeta(meta);
			
			if(speedLevel == 0) {
				meta.setLore(activeLore);
				normalSpeed.setItemMeta(meta);
				
				if(glowOnActiveTools) {
					GlowEffect.addGlow(normalSpeed);
				}
			}
			
			GUIContent[2] = normalSpeed;
			
			
			// Speed I
			ItemStack speedI = new ItemStack(Material.FEATHER);
			meta = speedI.getItemMeta();
			meta.setDisplayName(TOOL_SPEED_I_NAME);
			speedI.setItemMeta(meta);
			
			if(speedLevel == 1) {
				meta.setLore(activeLore);
				speedI.setItemMeta(meta);
				
				if(glowOnActiveTools) {
					GlowEffect.addGlow(speedI);
				}
			}
			
			GUIContent[3] = speedI;
			
			
			// Speed II
			ItemStack speedII = new ItemStack(Material.FEATHER, 2);
			meta = speedII.getItemMeta();
			meta.setDisplayName(TOOL_SPEED_II_NAME);
			speedII.setItemMeta(meta);
			
			if(speedLevel == 2) {
				meta.setLore(activeLore);
				speedII.setItemMeta(meta);
				
				if(glowOnActiveTools) {
					GlowEffect.addGlow(speedII);
				}
			}
			
			GUIContent[4] = speedII;
			
			
			// Speed III
			ItemStack speedIII = new ItemStack(Material.FEATHER, 3);
			meta = speedIII.getItemMeta();
			meta.setDisplayName(TOOL_SPEED_III_NAME);
			speedIII.setItemMeta(meta);
			
			if(speedLevel == 3) {
				meta.setLore(activeLore);
				speedIII.setItemMeta(meta);
				
				if(glowOnActiveTools) {
					GlowEffect.addGlow(speedIII);
				}
			}
			
			GUIContent[5] = speedIII;
			
			
			// Speed IV
			ItemStack speedIV = new ItemStack(Material.FEATHER, 4);
			meta = speedIV.getItemMeta();
			meta.setDisplayName(TOOL_SPEED_IV_NAME);
			speedIV.setItemMeta(meta);
			
			if(speedLevel == 4) {
				meta.setLore(activeLore);
				speedIV.setItemMeta(meta);
				
				if(glowOnActiveTools) {
					GlowEffect.addGlow(speedIV);
				}
			}
			
			GUIContent[6] = speedIV;
		}
		
		ArrayList<ItemStack> toolsOnLine2 = new ArrayList<ItemStack>();
		
		// No-clip mode
		if(noClipTool) {
			ItemStack noClip = new ItemStack(Material.BARRIER);
			meta = noClip.getItemMeta();
			meta.setDisplayName(TOOL_NOCLIP_NAME);
			meta.setLore(Arrays.asList(
					ChatColor.GRAY + "Allows you to go through all the blocks.",
					//ChatColor.GRAY + "blocks.",
					"",
					ChatColor.GRAY + "You can also first-spectate a player",
					ChatColor.GRAY + "by left-clicking on him",
					ChatColor.DARK_GRAY + "Use Shift to quit the first-person",
					ChatColor.DARK_GRAY + "spectator mode.",
					"",
					ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + "In this mode, open your inventory",
					ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + "to access controls!"));
			
			noClip.setItemMeta(meta);
			
			toolsOnLine2.add(noClip);
		}
		
		// Night vision
		if(nightVisionTool) {
			ItemStack nightVision = new ItemStack(Material.EYE_OF_ENDER);
			meta = nightVision.getItemMeta();
			if(nightVisionActive) {
				nightVision.setType(Material.ENDER_PEARL);
				meta.setDisplayName(TOOL_NIGHT_VISION_ACTIVE_NAME);
			}
			else {
				meta.setDisplayName(TOOL_NIGHT_VISION_INACTIVE_NAME);
			}
			nightVision.setItemMeta(meta);
			
			toolsOnLine2.add(nightVision);
		}
		
		// Diving suit (Depth-Strider-III boots)
		if(divingSuitTool) {
			ItemStack divingSuit = new ItemStack(Material.DIAMOND_BOOTS);
			meta = divingSuit.getItemMeta();
			meta.setDisplayName(TOOL_DIVING_SUIT_NAME);
			if(divingSuitEquiped) {
				meta.setLore(activeLore);
			}
			List<String> lore = meta.getLore();
			if(lore == null) lore = new ArrayList<String>();
			lore.add(ChatColor.GRAY + "Get a pair of Depth Strider III boots");
			meta.setLore(lore);
			divingSuit.setItemMeta(meta);
			
			if(divingSuitEquiped && glowOnActiveTools) {
				GlowEffect.addGlow(divingSuit);
			}
			
			toolsOnLine2.add(divingSuit);
		}
		
		// Teleportation to the death point
		ItemStack tpToDeathPoint = null;
		if(deathPoint != null) {
			tpToDeathPoint = new ItemStack(Material.NETHER_STAR);
			meta = tpToDeathPoint.getItemMeta();
			meta.setDisplayName(TOOL_TP_TO_DEATH_POINT_NAME);
			
			// The death message is never set if it is disabled: check useless (same as above).
			if(getPlayerData(spectator).lastDeathMessage != null) {
				List<String> lore = new ArrayList<String>();
				lore.add("" + ChatColor.GRAY + getPlayerData(spectator).lastDeathMessage);
				meta.setLore(lore);
			}
			
			tpToDeathPoint.setItemMeta(meta);
		}
		
		
		// Line 2 (and 3): display
		int lineSize = toolsOnLine2.size();
		if(lineSize == 0 && deathPoint != null) {
			GUIContent[offset + 4] = tpToDeathPoint;
		}
		else if(lineSize == 1) {
			if(deathPoint != null) {
				GUIContent[offset + 2] = toolsOnLine2.get(0);
				GUIContent[offset + 6] = tpToDeathPoint;
			}
			else {
				GUIContent[offset + 4] = toolsOnLine2.get(0);
			}
		}
		else if(lineSize == 2) {
			if(deathPoint != null) {
				GUIContent[offset + 2] = toolsOnLine2.get(0);
				GUIContent[offset + 4] = toolsOnLine2.get(1);
				GUIContent[offset + 6] = tpToDeathPoint;
			}
			else {
				GUIContent[offset + 2] = toolsOnLine2.get(0);
				GUIContent[offset + 6] = toolsOnLine2.get(1);
			}
		}
		else if(lineSize == 3) {
			GUIContent[offset + 2] = toolsOnLine2.get(0);
			GUIContent[offset + 4] = toolsOnLine2.get(1);
			GUIContent[offset + 6] = toolsOnLine2.get(2);
			
			if(deathPoint != null) {
				GUIContent[offset + 13] = tpToDeathPoint;
			}
		}
		
		
		
		gui.setContents(GUIContent);
		spectator.openInventory(gui);
	}


	/**
	 * Teleports the spectator to the player they have chosen using "/spec p &lt;target>"
	 * 
	 * @param spectator The spectator to teleport.
	 * @param target The spectator will be teleported at the current location of this player.
	 */
	protected void choosePlayer(Player spectator, Player target) {
		spectator.teleport(target);

		if(output) {
			spectator.sendMessage(prefix + "Teleported you to " + ChatColor.RED + target.getName());
		}
	}
	
	/**
	 * Sets whether the player collides with entities.
	 * 
	 * @param player The player.
	 * @param collides Whether the player should collide with entities or not.
	 * 
	 * @return true if the change was successful (compatible server, i.e. Spigot currently); false else.
	 */
	private boolean setCollidesWithEntities(Player player, boolean collides) {
		try {
			// We need to call player.spigot.setCollidesWithEntities(collides) .
			
			Field playerSpigotField = player.getClass().getDeclaredField("spigot");
			playerSpigotField.setAccessible(true);

			Class<?> playerSpigotClazz = playerSpigotField.getType();
			Object playerSpigotObject = playerSpigotField.get(player);


			playerSpigotClazz.getDeclaredMethod("setCollidesWithEntities", boolean.class)
			                 .invoke(playerSpigotObject, collides);

			return true;
			
		} catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ignored) {
			// Cannot enable/disable collisions :(
			return false;
		}
	}

	/**
	 * Checks for problems and enables spectator mode for spectator, on behalf of sender.
	 * 
	 * @param spectator The player that will be a spectator.
	 * @param sender The sender of the /spec on [player] command.
	 */
	protected void enableSpectate(Player spectator, CommandSender sender) {
		enableSpectate(spectator, sender, false);
	}
	
	/**
	 * Checks for problems and enables spectator mode for spectator, on behalf of sender.
	 * 
	 * @param spectator The player that will be a spectator.
	 * @param sender The sender of the /spec on [player] command.
	 * @param silent Will not output any messages - useful when using the API or command blocks.
	 * 
	 * @since 2.0
	 */
	protected void enableSpectate(Player spectator, CommandSender sender, boolean silent) {
		enableSpectate(spectator, sender, silent, false);
	}
	
	/**
	 * Checks for problems and enables spectator mode for spectator, on behalf of sender.
	 * 
	 * @param spectator The player that will be a spectator.
	 * @param sender The sender of the /spec on [player] command.
	 * @param silent Will not output any messages - useful when using the API or command blocks.
	 * @param worldChange Was the enable spectate caused by a world change?
	 * 
	 * @since 2.0
	 */
	protected void enableSpectate(Player spectator, CommandSender sender, boolean silent, boolean worldChange) {
		if (user.get(spectator.getName()).spectating) {
			if (!silent) {
				// Spectator mode was already on
				if (sender instanceof Player && spectator.getName().equals(sender.getName())) {
					spectator.sendMessage(prefix + "You are already spectating!");
				}
				else {
					sender.sendMessage(prefix + ChatColor.RED + spectator.getDisplayName() + ChatColor.GOLD + " is already spectating!");
				}
			}
		}

		else {
			// Hide them from everyone
			for (Player target : getServer().getOnlinePlayers()) {
				if(seeSpecs && getPlayerData(target).spectating) {
					spectator.showPlayer(target);
				}
				else {
					target.hidePlayer(spectator); // Hide the spectator from non-specs: if seeSpecs mode is off and the target isn't spectating
				}
			}

			// Gamemode, 'ghost' and inventory
			getPlayerData(spectator).oldGameMode = spectator.getGameMode();
			GameMode gm = (vanillaSpectate)? GameMode.SPECTATOR : GameMode.ADVENTURE;
			spectator.setGameMode(gm);
			
			savePlayerInv(spectator);
			getPlayerData(spectator).effects = spectator.getActivePotionEffects();
			for (PotionEffect pe : spectator.getActivePotionEffects()) {
				spectator.removePotionEffect(pe.getType());
			}
			
			spectator.setAllowFlight(true);
			spectator.setFoodLevel(20);
			spectator.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 15), true);

			// Disable interaction
			getPlayerData(spectator).spectating = true;
			setCollidesWithEntities(spectator, false);

			updateSpectatorInventory(spectator);

			// Set the prefix in the tab list if the toggle is on
			if (scoreboard) {
				if (spectator.getScoreboard() != null) getPlayerData(spectator).oldScoreboard = spectator.getScoreboard(); else user.get(spectator.getName()).oldScoreboard = getServer().getScoreboardManager().getMainScoreboard();
				spectator.setScoreboard(board);
				team.addPlayer(spectator);
			}

			// Teleport them to the global lobby (not if world change)
			if (!worldChange) spawnPlayer(spectator);

			// Manage messages if spectator mode was enabled
			if (!silent) {
				if (sender instanceof Player && spectator.getName().equals(sender.getName())) {
					if(output) {
						spectator.sendMessage(prefix + "Spectator mode " + ChatColor.RED + "enabled");
					}
				} 
				else if (sender instanceof Player && !spectator.getName().equals(sender.getName())) {
					if(output) {
						spectator.sendMessage(prefix + "Spectator mode " + ChatColor.RED + "enabled" + ChatColor.GOLD + " by " + ChatColor.RED + ((Player) sender).getDisplayName());
					}
					sender.sendMessage(prefix + "Spectator mode " + ChatColor.RED + "enabled" + ChatColor.GOLD + " for " + ChatColor.RED + spectator.getDisplayName());
				} 
				else {
					if(output) {
						spectator.sendMessage(prefix + "Spectator mode " + ChatColor.RED + "enabled" + ChatColor.GOLD + " by " + ChatColor.DARK_RED + "Console");
					}
					sender.sendMessage(prefix + "Spectator mode " + ChatColor.RED + "enabled" + ChatColor.GOLD + " for " + ChatColor.RED + spectator.getDisplayName());
				}
			}

			specs.getConfig().set(spectator.getName(), true);
			specs.saveConfig();
			
			
		}
	}
	
	/**
	 * Checks for problems and disables spectator mode for spectator, on behalf of sender.
	 * Convenience method for {@link #disableSpectate(Player spectator, CommandSender sender, boolean silent)}
	 * 
	 * @param spectator The spectator that will be a normal player.
	 * @param sender The sender of the /spec off [player] command.
	 */
	protected void disableSpectate(Player spectator, CommandSender sender) {
		disableSpectate(spectator, sender, false);
	}
	
	/**
	 * Checks for problems and disables spectator mode for spectator, on behalf of sender.
	 * Convenience method for {@link #disableSpectate(Player spectator, CommandSender sender, boolean silent, boolean temp)}
	 * 
	 * @param spectator The spectator that will be a normal player.
	 * @param sender The sender of the /spec off [player] command.
	 * @param silent Will not output any messages - useful when using the API or command blocks.
	 * 
	 * @since 2.0
	 */
	protected void disableSpectate(Player spectator, CommandSender sender, boolean silent) {
		disableSpectate(spectator, sender, silent, false);
	}
	
	/**
	 * Checks for problems and disables spectator mode for spectator, on behalf of sender.
	 * 
	 * @param spectator The spectator that will be a normal player.
	 * @param sender The sender of the /spec off [player] command.
	 * @param silent Will not output any messages - useful when using the API or command blocks.
	 * @param temp If true, the next time the player re-logs, spectator mode will be re-enabled.
	 * 
	 * @since 2.0
	 */
	protected void disableSpectate(Player spectator, CommandSender sender, boolean silent, boolean temp) {
		disableSpectate(spectator, sender, silent, temp, false);
	}
	
	/**
	 * Checks for problems and disables spectator mode for spectator, on behalf of sender.
	 * 
	 * @param spectator The spectator that will be a normal player.
	 * @param sender The sender of the /spec off [player] command.
	 * @param silent Will not output any messages - useful when using the API or command blocks.
	 * @param temp If true, the next time the player re-logs, spectator mode will be re-enabled.
	 * @param worldChange Was the enable spectate caused by a world change?
	 * 
	 * @since 2.0
	 */
	protected void disableSpectate(Player spectator, CommandSender sender, boolean silent, boolean temp, boolean worldChange) {
		if (getPlayerData(spectator).spectating) {
			// Show them to everyone
			for (Player target : getServer().getOnlinePlayers()) {
				if (seeSpecs && getPlayerData(target).spectating) {
					spectator.hidePlayer(target);
				}
				target.showPlayer(spectator);
			}
			
			// Allow interaction
			getPlayerData(spectator).spectating = false;
			setCollidesWithEntities(spectator, true);
			
			spectator.setAllowFlight(false);
			spectator.setGameMode(getPlayerData(spectator).oldGameMode);
			
			loadPlayerInv(spectator);
			
			// Restore effects
			spectator.removePotionEffect(PotionEffectType.INVISIBILITY);
			spectator.removePotionEffect(PotionEffectType.SPEED);
			spectator.removePotionEffect(PotionEffectType.WATER_BREATHING);
			spectator.removePotionEffect(PotionEffectType.NIGHT_VISION);
			spectator.addPotionEffects(getPlayerData(spectator).effects);
			
			spectator.setFlySpeed(0.1f);
			
			// Remove from spec team
			if (scoreboard) {
				if(getPlayerData(spectator).oldScoreboard != null) spectator.setScoreboard(getPlayerData(spectator).oldScoreboard);
				team.removePlayer(spectator);
			}
			
			if (!worldChange) {
				removePlayerFromArena(spectator, true); // Clear the arena they were spectating in
				spawnPlayer(spectator); // Teleport to spawn
			}
			
			if (!silent) {
				if (sender instanceof Player && spectator.getName().equals(sender.getName())) {
					if(output) {
						spectator.sendMessage(prefix + "Spectator mode " + ChatColor.RED + "disabled");
					}
				}
				else if (sender instanceof Player && !spectator.getName().equals(sender.getName())) {
					if(output) {
						spectator.sendMessage(prefix + "Spectator mode " + ChatColor.RED + "disabled" + ChatColor.GOLD + " by " + ChatColor.RED + ((Player) sender).getDisplayName());
					}
					sender.sendMessage(prefix + "Spectator mode " + ChatColor.RED + "disabled" + ChatColor.GOLD + " for " + ChatColor.RED + spectator.getDisplayName());
				}
				else {
					if(output) {
						spectator.sendMessage(prefix + "Spectator mode " + ChatColor.RED + "disabled" + ChatColor.GOLD + " by " + ChatColor.DARK_RED + "Console");
					}
					sender.sendMessage(prefix + "Spectator mode " + ChatColor.RED + "disabled" + ChatColor.GOLD + " for " + ChatColor.RED + spectator.getDisplayName());
				}
			}

			if (!temp) {
				specs.getConfig().set(spectator.getName(), null);
				specs.saveConfig();
			}
		} 
		else {
			// Spectate mode wasn't on
			if (!silent) {
				if (sender instanceof Player && spectator.getName().equals(sender.getName())) {
					spectator.sendMessage(prefix + "You aren't spectating!");
				} 
				else {
					sender.sendMessage(prefix + ChatColor.RED + spectator.getDisplayName() + ChatColor.GOLD + " isn't spectating!");
				}
			}
		}
	}

	/**
	 * Reload the configuration.
	 * 
	 * @param hardReload If true, the configuration will be reloaded from the disk.
	 */
	protected void reloadConfig(boolean hardReload) {
		// A hard reload will reload the config values from file.
		if (hardReload) {			
			setup.saveDefaultConfig();
			toggles.getConfigAccessor().saveDefaultConfig();
			specs.saveDefaultConfig();
			
			setup.reloadConfig();
			toggles.getConfigAccessor().reloadConfig();
			specs.reloadConfig();
			
			toggles.upgrade();	
		}
		
		compass = toggles.getBoolean(Toggle.TOOLS_TELEPORTER_ENABLED);
		clock = toggles.getBoolean(Toggle.TOOLS_ARENACHOOSER_ENABLED);
		spectatorsTools = toggles.getBoolean(Toggle.TOOLS_TOOLS_ENABLED);
		divingSuitTool = toggles.getBoolean(Toggle.TOOLS_TOOLS_DIVINGSUIT);
		nightVisionTool = toggles.getBoolean(Toggle.TOOLS_TOOLS_NIGHTVISION);
		noClipTool = toggles.getBoolean(Toggle.TOOLS_TOOLS_NOCLIP);
		speedTool = toggles.getBoolean(Toggle.TOOLS_TOOLS_SPEED);
		tpToDeathTool = toggles.getBoolean(Toggle.TOOLS_TOOLS_TPTODEATH_ENABLED);
		tpToDeathToolShowCause = toggles.getBoolean(Toggle.TOOLS_TOOLS_TPTODEATH_DISPLAYCAUSE);
		glowOnActiveTools = toggles.getBoolean(Toggle.TOOLS_TOOLS_GLOW);
		inspector = toggles.getBoolean(Toggle.TOOLS_INSPECTOR_ENABLED);
		inspectFromTPMenu = toggles.getBoolean(Toggle.TOOLS_TELEPORTER_INSPECTOR);
		playersHealthInTeleportationMenu = toggles.getBoolean(Toggle.TOOLS_TELEPORTER_HEALTH);
		playersLocationInTeleportationMenu = toggles.getBoolean(Toggle.TOOLS_TELEPORTER_LOCATION);
		specChat = toggles.getBoolean(Toggle.CHAT_SPECTATORCHAT);
		output = toggles.getBoolean(Toggle.OUTPUT_MESSAGES);
		death = toggles.getBoolean(Toggle.SPECTATOR_MODE_ON_DEATH);
		scoreboard = toggles.getBoolean(Toggle.SPECTATORS_TABLIST_PREFIX);
		vanillaSpectate = toggles.getBoolean(Toggle.SPECTATORS_USE_VANILLA);
		seeSpecs = toggles.getBoolean(Toggle.SPECTATORS_SEE_OTHERS);
		skriptInt = toggles.getBoolean(Toggle.SKRIPT_INTEGRATION);
		blockCmds = toggles.getBoolean(Toggle.CHAT_BLOCKCOMMANDS_ENABLED);
		adminBypass = toggles.getBoolean(Toggle.CHAT_BLOCKCOMMANDS_ADMINBYPASS);
		newbieMode = toggles.getBoolean(Toggle.TOOLS_NEWBIEMODE);
		teleportToSpawnOnSpecChangeWithoutLobby = toggles.getBoolean(Toggle.ONSPECMODECHANGED_TELEPORTATION_TOSPAWN);
		useSpawnCommandToTeleport = toggles.getBoolean(Toggle.ONSPECMODECHANGED_TELEPORTATION_WITHSPAWNCMD);
		enforceArenaBoundary = toggles.getBoolean(Toggle.ENFORCE_ARENA_BOUNDARIES);
		
		compassItem = toggles.getMaterial(Toggle.TOOLS_TELEPORTER_ITEM);
		clockItem = toggles.getMaterial(Toggle.TOOLS_ARENACHOOSER_ITEM);
		spectatorsToolsItem = toggles.getMaterial(Toggle.TOOLS_TOOLS_ITEM);
		inspectorItem = toggles.getMaterial(Toggle.TOOLS_INSPECTOR_ITEM);
		
		try {
			setSpectatorMode(SpectatorMode.fromString(setup.getConfig().getString("mode")));
		} catch(IllegalArgumentException e) {
			getLogger().warning("The SpectatorPlus' mode set in the config (" + setup.getConfig().getString("mode") + ") is invalid; using the ANY mode.");
			setSpectatorMode(SpectatorMode.ANY);
		}
		
		if (scoreboard) {
			if (manager==null) { // After a reload, if 'scoreboard' is kept on, the same scoreboard will be used.
				manager = getServer().getScoreboardManager();
				board = manager.getNewScoreboard();
				board.registerNewObjective("health", "health").setDisplaySlot(DisplaySlot.PLAYER_LIST);
				team = board.registerNewTeam("spec");
				team.setPrefix(ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Spec" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY);
				team.setSuffix(ChatColor.RESET.toString());
				for (Player target : getServer().getOnlinePlayers()) {
					if (user.containsKey(target.getName()) && user.get(target.getName()).spectating) {
					}
				}
			}
			
			// Make sure the team is empty
			for (OfflinePlayer target : team.getPlayers()) {
				team.removePlayer(target);
			}
			
			// Add players who are spectating & set their scoreboard
			for (Player target : getServer().getOnlinePlayers()) {
				if (getPlayerData(target) != null && getPlayerData(target).spectating) {
					target.setScoreboard(board);
					team.addPlayer(target);
				}
			}
			
			// Incase seeSpecs was previously disabled...
			seeSpecs = toggles.getBoolean(Toggle.SPECTATORS_SEE_OTHERS);
		} else {
			// seeSpecs relies on using scoreboard teams. Force-disable seeSpecs if scoreboard is disabled.
			seeSpecs = false;
			// Do we need to worry about the scoreboard being previously enabled?
			if (manager != null) {
				// Remove all players from spectator team
				for (OfflinePlayer target : team.getPlayers()) {
					team.removePlayer(target);
				}
				// Reset each spectator's scoreboard to default/previous
				for (Player target : getServer().getOnlinePlayers()) {
					if (getPlayerData(target) != null && getPlayerData(target).spectating) {
						if (getPlayerData(target).oldScoreboard != null) {
							target.setScoreboard(getPlayerData(target).oldScoreboard);
						} else {
							target.setScoreboard(getServer().getScoreboardManager().getMainScoreboard());
						}
					}
				}
			}
		}

		if(team != null) team.setCanSeeFriendlyInvisibles(seeSpecs);
		
		// Update all spectators' inventories
		updateSpectatorInventories();
		
		for (Player target : getServer().getOnlinePlayers()) {
			if (getPlayerData(target) != null && getPlayerData(target).spectating) {
				if(vanillaSpectate) {
					// Set all spectators to SPECTATOR gamemode.
					target.setGameMode(GameMode.SPECTATOR);
				} else {
					// Set all spectators to ADVENTURE gamemode.
					target.setGameMode(GameMode.ADVENTURE);
				}
			}
		}
		
		// Update arenas.
		arenasManager.reload();
	}

	/**
	 * Sets the current SpectatorPlus' mode.
	 * <p>
	 * <ul>
	 *   <li>{@code ANY}: the spectators can teleports themselves to any player in the server.</li>
	 *   <li>{@code ARENA}: the spectators will have to choose an arena; then they will be able 
	 *   to teleport themselves only to the players in this arena. An option is available to prevent 
	 *   the spectators from leaving the arena.</li>
	 *   <li>{@code WORLD}: the spectators will be able to teleport themselves to the players in the same world.</li>
	 * </ul>
	 * 
	 * @param mode The mode.
	 * @see SpectatorMode
	 * 
	 * @since 2.0
	 */
	protected void setSpectatorMode(SpectatorMode mode) {
		this.mode = mode;
		
		setup.getConfig().set("mode", mode.toString());
		setup.saveConfig();
		
		// Needed if the mode is changed from/to the arena mode.
		updateSpectatorInventories();
	}
	
	/**
	 * Lets a player select two points and set up an arena.
	 * 
	 * @param player The player involved in the setup process.
	 * @param block The block punched by the player.
	 * 
	 * @return True if the player was setting up an arena; false else.
	 */
	protected boolean arenaSetup(Player player, Block block) {
		if (getPlayerData(player).setup == 2) {
			getPlayerData(player).pos2 = block.getLocation();
			getPlayerData(player).setup = 0;

			Location lowPos, hiPos;
			lowPos = new Location(getPlayerData(player).pos1.getWorld(), 0, 0, 0);
			hiPos = new Location(getPlayerData(player).pos1.getWorld(), 0, 0, 0);

			// yPos
			if (Math.floor(getPlayerData(player).pos1.getY()) > Math.floor(getPlayerData(player).pos2.getY())) {
				hiPos.setY(Math.floor(getPlayerData(player).pos1.getY()));
				lowPos.setY(Math.floor(getPlayerData(player).pos2.getY()));
			} else {
				lowPos.setY(Math.floor(getPlayerData(player).pos1.getY()));
				hiPos.setY(Math.floor(getPlayerData(player).pos2.getY()));
			}

			// xPos
			if (Math.floor(getPlayerData(player).pos1.getX()) > Math.floor(getPlayerData(player).pos2.getX())) {
				hiPos.setX(Math.floor(getPlayerData(player).pos1.getX()));
				lowPos.setX(Math.floor(getPlayerData(player).pos2.getX()));
			} else {
				lowPos.setX(Math.floor(getPlayerData(player).pos1.getX()));
				hiPos.setX(Math.floor(getPlayerData(player).pos2.getX()));
			}

			// zPos
			if (Math.floor(getPlayerData(player).pos1.getZ()) > Math.floor(getPlayerData(player).pos2.getZ())) {
				hiPos.setZ(Math.floor(getPlayerData(player).pos1.getZ()));
				lowPos.setZ(Math.floor(getPlayerData(player).pos2.getZ()));
			} else {
				lowPos.setZ(Math.floor(getPlayerData(player).pos1.getZ()));
				hiPos.setZ(Math.floor(getPlayerData(player).pos2.getZ()));
			}
			
			arenasManager.registerArena(new Arena(getPlayerData(player).arenaName, hiPos, lowPos));
			player.sendMessage(prefix + "Arena " + ChatColor.RED + getPlayerData(player).arenaName + ChatColor.GOLD + " successfully set up!");

			// returns true: Cancels breaking of the block that was punched
			return true;
		}
		else {
			if (getPlayerData(player).setup == 1) {
				getPlayerData(player).pos1 = block.getLocation();

				player.sendMessage(prefix + "Punch point " + ChatColor.RED + "#2" + ChatColor.GOLD + " - the opposite corner of the arena");

				getPlayerData(player).setup = 2;

				// returns true: Cancels breaking of the block that was punched
				return true;
			}
			else {
				// returns false: The player was not setting up an arena.
				return false;
			}
		}
	}


	/**
	 * Removes an arena.
	 * 
	 * @param arenaName
	 * @return True if the arena was removed; false else (non-existant arena).
	 */
	protected boolean removeArena(String arenaName) {
		
		Arena arenaToBeRemoved = arenasManager.getArena(arenaName);
		if(arenaToBeRemoved == null) {
			return false;
		}

		arenasManager.unregisterArena(arenaToBeRemoved);
		
		// The players in the deleted arena are removed to the arena
		for(Player player : this.getServer().getOnlinePlayers()) {
			if(getPlayerData(player).spectating) {
				if(getPlayerData(player).arena != null && getPlayerData(player).arena.equals(arenaToBeRemoved.getUUID())) {
					removePlayerFromArena(player);
				}
			}
		}
		
		return true;
	}


	/**
	 * Sets the arena for the given player.
	 * Teleports the player to the lobby of that arena, if a lobby is available.
	 * 
	 * @param player The player.
	 * @param arenaName The name of the arena.
	 * @param teleportToLobby If true the player will be teleported to the lobby (if a lobby is set).
	 * @return True if the change was effective (i.e. the arena exists).
	 */
	protected boolean setArenaForPlayer(Player player, String arenaName, boolean teleportToLobby) {
		Arena arena = arenasManager.getArena(arenaName);
		
		getPlayerData(player).arena = arena.getUUID();
		if(teleportToLobby) {
			Location lobbyLocation = arena.getLobby();
			
			if(lobbyLocation == null) { // No lobby set
				player.sendMessage(prefix + "No lobby location set for " + ChatColor.RED + arenaName);
				return true;
			}

			if(output) {
				player.sendMessage(prefix + "Teleported you to " + ChatColor.RED + arenaName);
			}

			player.teleport(lobbyLocation);
		}

		return true;
	}


	/**
	 * Sets the arena for the given player.
	 * Teleports the player to the lobby of that arena, if a lobby is available.
	 * 
	 * @param player The player.
	 * @param arenaName The name of the arena.
	 * @return True if the change was effective (i.e. the arena exists).
	 */
	protected boolean setArenaForPlayer(Player player, String arenaName) {
		return setArenaForPlayer(player, arenaName, true);
	}

	/**
	 * Removes a player from his arena.
	 * The player is teleported to the main lobby, if such a lobby is set.
	 * 
	 * @param player
	 */
	protected void removePlayerFromArena(Player player) {
		removePlayerFromArena(player, false);
	}
	
	/**
	 * Removes a player from his arena.
	 * The player is teleported to the main lobby, if such a lobby is set.
	 * 
	 * @param player
	 * @param silent
	 */
	protected void removePlayerFromArena(Player player, boolean silent) {

		getPlayerData(player).arena = null;
		boolean teleported = spawnPlayer(player);

		if(output && !silent) {
			if(teleported) {
				player.sendMessage(prefix + "You were removed from your current arena and teleported to the main lobby.");
			}
			else {
				player.sendMessage(prefix + "You were removed from your current arena.");
			}
		}
	}

	/**
	 * Saves the player's inventory and clears it before enabling spectator mode.
	 * 
	 * @param player The concerned player.
	 */
	protected void savePlayerInv(Player player) {
		getPlayerData(player).inventory = player.getInventory().getContents();
		getPlayerData(player).armour = player.getInventory().getArmorContents();

		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
	}

	/**
	 * Loads the player's inventory after disabling the spectate mode.
	 * 
	 * @param player The concerned player.
	 */
	protected void loadPlayerInv(Player player) {
		player.getInventory().clear();
		player.getInventory().setContents(getPlayerData(player).inventory);
		player.getInventory().setArmorContents(getPlayerData(player).armour);

		getPlayerData(player).inventory = null;
		getPlayerData(player).armour = null;

		player.updateInventory(); // yes, it's deprecated. But it still works!
	}

	/**
	 * Broadcasts a message to all players with spectator mode enabled, and the sender.
	 * 
	 * @param sender The sender of the message to be broadcasted.
	 * @param message The message to broadcast.
	 */
	protected void broadcastToSpectators(CommandSender sender, String message) {
		String senderName = null;
		if(sender instanceof Player) {
			senderName = ChatColor.WHITE + ((Player) sender).getDisplayName();
		}
		else {
			senderName = ChatColor.DARK_RED + "CONSOLE";
		}

		String formattedMessage = ChatColor.GOLD + "[" + senderName + ChatColor.GOLD + " -> spectators] " + ChatColor.RESET + message;

		for (Player player : getServer().getOnlinePlayers()) {
			if(getPlayerData(player).spectating || player.getName().equals(sender.getName())) {
				player.sendMessage(formattedMessage);
			}
		}

		console.sendMessage(formattedMessage);
	}

	/**
	 * Sends a spectator chat message, from one spectator to all other spectators. 
	 * Includes "/me" actions
	 * 
	 * @param sender The sender of the message.
	 * @param message The text of the message.
	 * @param isAction If true, the message will be displayed as an action message (like /me <message>).
	 */
	protected void sendSpectatorMessage(CommandSender sender, String message, Boolean isAction) {
		String playerName = null;
		if(sender instanceof Player) {
			playerName = ChatColor.WHITE + ((Player) sender).getDisplayName();
		} else {
			playerName = ChatColor.DARK_RED + "CONSOLE";
		}

		String invite = null;
		if(isAction) {
			invite = "* " + playerName + " " + ChatColor.GRAY;
		} else {
			invite = playerName + ChatColor.GRAY + ": ";
		}

		for (Player player : getServer().getOnlinePlayers()) {
			if(getPlayerData(player).spectating) {
				player.sendMessage(ChatColor.GRAY + "[SPEC] " + invite + message);
			}
		}
		console.sendMessage(ChatColor.GRAY + "[SPEC] " + invite + message);
	}

	/**
	 * Updates the spectator inventory for a certain player.
	 * 
	 * @param spectator The player whose inventory will be updated
	 * 
	 * @since 2.0
	 */	
	protected void updateSpectatorInventory(Player spectator) {
		// Empty the inventory first...
		spectator.getInventory().clear();
		
		/* Classic spectator inventory */
		if(spectator.getGameMode() == GameMode.ADVENTURE) {
			
			String rightClick = "", rightClickPlayer = "";
			if(newbieMode) {
				rightClick = ChatColor.GRAY + " (Right-click)";
				rightClickPlayer = ChatColor.GRAY + " (Right-click a player)";
			}
	
			// Give them compass if the toggle is on
			if (compass) {
				ItemStack compass = new ItemStack(compassItem, 1);
				ItemMeta compassMeta = (ItemMeta)compass.getItemMeta();
				compassMeta.setDisplayName(ChatColor.AQUA +""+ ChatColor.BOLD + "Teleporter" + rightClick);
				List<String> lore = new ArrayList<String>();
					lore.add(ChatColor.GOLD +""+ ChatColor.ITALIC + "Right click" + ChatColor.DARK_GRAY + ChatColor.ITALIC + " to choose a player");
					lore.add(ChatColor.DARK_GRAY +""+ ChatColor.ITALIC + "to teleport to");
				compassMeta.setLore(lore);
				compass.setItemMeta(compassMeta);
				spectator.getInventory().setItem(0, compass);
			}
	
			// Give them clock (only for arena mode and if the toggle is on)
			if (clock && mode == SpectatorMode.ARENA) {
				ItemStack watch = new ItemStack(clockItem, 1);
				ItemMeta watchMeta = (ItemMeta)watch.getItemMeta();
				watchMeta.setDisplayName(ChatColor.DARK_RED +""+ ChatColor.BOLD + "Arena selector" + rightClick);
				List<String> lore = new ArrayList<String>();
					lore.add(ChatColor.GOLD +""+ ChatColor.ITALIC + "Right click" + ChatColor.DARK_GRAY + ChatColor.ITALIC + " to choose an arena");
					lore.add(ChatColor.DARK_GRAY +""+ ChatColor.ITALIC + "to spectate in");
				watchMeta.setLore(lore);
				watch.setItemMeta(watchMeta);
				spectator.getInventory().setItem(1, watch);
			}
	
			// Give them magma cream (spectators tools) if the toggle is on
			if(spectatorsTools) {
				ItemStack tools = new ItemStack(spectatorsToolsItem, 1);
				ItemMeta toolsMeta = tools.getItemMeta();
				toolsMeta.setDisplayName(ChatColor.DARK_GREEN +""+ ChatColor.BOLD + "Spectators' tools" + rightClick);
				List<String> lore = new ArrayList<String>();
					lore.add(ChatColor.GOLD +""+ ChatColor.ITALIC + "Right click" + ChatColor.DARK_GRAY + ChatColor.ITALIC + " to open the spectator");
					lore.add(ChatColor.DARK_GRAY +""+ ChatColor.ITALIC + "tools menu");
				toolsMeta.setLore(lore);
				tools.setItemMeta(toolsMeta);
				spectator.getInventory().setItem(4, tools);
			}
	
			// Give them book if the toggle is on
			if(inspector) {
				ItemStack book = new ItemStack(inspectorItem, 1);
				ItemMeta bookMeta = (ItemMeta)book.getItemMeta();
				bookMeta.setDisplayName(ChatColor.DARK_AQUA +""+ ChatColor.BOLD + "Inspector" + rightClickPlayer);
				List<String> lore = new ArrayList<String>();
					lore.add(ChatColor.GOLD +""+ ChatColor.ITALIC + "Right click" + ChatColor.DARK_GRAY + ChatColor.ITALIC + " a player to see their");
					lore.add(ChatColor.DARK_GRAY +""+ ChatColor.ITALIC + "inventory, armour, health & more!");
				bookMeta.setLore(lore);
				book.setItemMeta(bookMeta);
				spectator.getInventory().setItem(8, book);
			}
		}
		
		/* No-clip spectator inventory */
		else if(spectator.getGameMode() == GameMode.SPECTATOR) {
			
			ItemStack quitNoClip = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
			SkullMeta sMeta = (SkullMeta) quitNoClip.getItemMeta();
				String disableExit="";
				if(vanillaSpectate) {
					disableExit = ChatColor.RED+""+ChatColor.BOLD+" DISABLED";
				}
				sMeta.setDisplayName(TOOL_NOCLIP_QUIT_NAME + spectator.getName() +disableExit);
				List<String> lore = new ArrayList<String>();
				lore.add(ChatColor.GRAY + "Leave no-clip mode");
				lore.add(ChatColor.DARK_GRAY + "You can also use /spec b");
				sMeta.setLore(lore);
				sMeta.setOwner(spectator.getName());
			quitNoClip.setItemMeta(sMeta);
			
			
			if(nightVisionTool) {
				Boolean nightVisionActive = false;
				for(PotionEffect effect : spectator.getActivePotionEffects()) {
					if(effect.getType().equals(PotionEffectType.NIGHT_VISION)) {
						nightVisionActive = true;
						break;
					}
				}
				
				ItemStack nightVision = new ItemStack(Material.EYE_OF_ENDER);
				ItemMeta iMeta = nightVision.getItemMeta();
				if(nightVisionActive) {
					nightVision.setType(Material.ENDER_PEARL);
					iMeta.setDisplayName(TOOL_NIGHT_VISION_ACTIVE_NAME);
				}
				else {
					iMeta.setDisplayName(TOOL_NIGHT_VISION_INACTIVE_NAME);
				}
				nightVision.setItemMeta(iMeta);
				
				spectator.getInventory().setItem(20, nightVision);
				spectator.getInventory().setItem(24, quitNoClip);
			}
			
			else {
				spectator.getInventory().setItem(22, quitNoClip);
			}
		}

		spectator.updateInventory();
	}
	
	/**
	 * Updates the spectator inventories for all currently spectating players.
	 * 
	 * @since 2.0
	 */	
	protected void updateSpectatorInventories() {
		for (Player target : getServer().getOnlinePlayers()) {
			if (getPlayerData(target).spectating) {
				updateSpectatorInventory(target);
			}
		}
	}
	
	/**
	 * Get the PlayerObject (data store) for the player. It is created on-the-fly if needed.
	 * 
	 * @param target The player to get the PlayerObject of.
	 * 
	 * @since 2.0
	 */	
	protected PlayerObject getPlayerData(Player target)
	{
		PlayerObject data = user.get(target.getName());
		
		// Created on-the-fly if needed.
		if(data == null)
		{
			data = new PlayerObject();
			user.put(target.getName(), data);
		}
		
		return data;
	}
	
	/**
	 * Returns the API.
	 * 
	 * @see SpectateAPI
	 * 
	 * @return The API.
	 */
	public final SpectateAPI getAPI() {
		return api;
	}
	
	protected Boolean parseBoolean(String input) {
		if (input.equalsIgnoreCase("on") || input.equalsIgnoreCase("yes") || input.equalsIgnoreCase("y") || input.equalsIgnoreCase("true")) {
			return true;
		} else if (input.equalsIgnoreCase("off") || input.equalsIgnoreCase("no") || input.equalsIgnoreCase("n") || input.equalsIgnoreCase("false")) {
			return false;
		} else {
			return null; 
		}
	}
}
