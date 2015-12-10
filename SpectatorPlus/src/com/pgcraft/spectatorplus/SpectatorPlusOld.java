package com.pgcraft.spectatorplus;

import com.pgcraft.spectatorplus.arenas.Arena;
import com.pgcraft.spectatorplus.arenas.ArenasManager;
import com.pgcraft.spectatorplus.spectators.Spectator;
import com.pgcraft.spectatorplus.spectators.SpectatorMode;
import com.pgcraft.spectatorplus.tasks.SpectatorManagerTask;
import com.pgcraft.spectatorplus.utils.ConfigAccessor;
import fr.zcraft.zlib.core.ZPlugin;
import fr.zcraft.zlib.tools.items.GlowEffect;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("deprecation")
public class SpectatorPlusOld extends ZPlugin
{
	private static SpectatorPlusOld instance;

	protected HashMap <String, Spectator> user = new HashMap<>();

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
	public void onLoad()
	{
		super.onLoad();

		// Registers the Arena class as a serializable one.
		ConfigurationSerialization.registerClass(Arena.class);
	}
	
	/**
	 * This method is not meant for public use.
	 */
	@Override
	public void onEnable()
	{
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
			user.put(player.getName(), new Spectator());
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
			if (getPlayerData(player).isSpectating()) {
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
					if (user.containsKey(target.getName()) && user.get(target.getName()).isSpectating()) {
					}
				}
			}
			
			// Make sure the team is empty
			for (OfflinePlayer target : team.getPlayers()) {
				team.removePlayer(target);
			}
			
			// Add players who are spectating & set their scoreboard
			for (Player target : getServer().getOnlinePlayers()) {
				if (getPlayerData(target) != null && getPlayerData(target).isSpectating()) {
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
					if (getPlayerData(target) != null && getPlayerData(target).isSpectating()) {
						if (getPlayerData(target).getOldScoreboard() != null) {
							target.setScoreboard(getPlayerData(target).getOldScoreboard());
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
			if (getPlayerData(target) != null && getPlayerData(target).isSpectating()) {
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
	 * Lets a player select two points and set up an arena.
	 * 
	 * @param player The player involved in the setup process.
	 * @param block The block punched by the player.
	 * 
	 * @return True if the player was setting up an arena; false else.
	 */
	protected boolean arenaSetup(Player player, Block block) {
		if (getPlayerData(player).getSetup() == 2) {
			getPlayerData(player).setPos2(block.getLocation());
			getPlayerData(player).setSetup(0);

			Location lowPos, hiPos;
			lowPos = new Location(getPlayerData(player).getPos1().getWorld(), 0, 0, 0);
			hiPos = new Location(getPlayerData(player).getPos1().getWorld(), 0, 0, 0);

			// yPos
			if (Math.floor(getPlayerData(player).getPos1().getY()) > Math.floor(getPlayerData(player).getPos2().getY())) {
				hiPos.setY(Math.floor(getPlayerData(player).getPos1().getY()));
				lowPos.setY(Math.floor(getPlayerData(player).getPos2().getY()));
			} else {
				lowPos.setY(Math.floor(getPlayerData(player).getPos1().getY()));
				hiPos.setY(Math.floor(getPlayerData(player).getPos2().getY()));
			}

			// xPos
			if (Math.floor(getPlayerData(player).getPos1().getX()) > Math.floor(getPlayerData(player).getPos2().getX())) {
				hiPos.setX(Math.floor(getPlayerData(player).getPos1().getX()));
				lowPos.setX(Math.floor(getPlayerData(player).getPos2().getX()));
			} else {
				lowPos.setX(Math.floor(getPlayerData(player).getPos1().getX()));
				hiPos.setX(Math.floor(getPlayerData(player).getPos2().getX()));
			}

			// zPos
			if (Math.floor(getPlayerData(player).getPos1().getZ()) > Math.floor(getPlayerData(player).getPos2().getZ())) {
				hiPos.setZ(Math.floor(getPlayerData(player).getPos1().getZ()));
				lowPos.setZ(Math.floor(getPlayerData(player).getPos2().getZ()));
			} else {
				lowPos.setZ(Math.floor(getPlayerData(player).getPos1().getZ()));
				hiPos.setZ(Math.floor(getPlayerData(player).getPos2().getZ()));
			}
			
			arenasManager.registerArena(new Arena(getPlayerData(player).getArenaName(), hiPos, lowPos));
			player.sendMessage(prefix + "Arena " + ChatColor.RED + getPlayerData(player).getArenaName() + ChatColor.GOLD + " successfully set up!");

			// returns true: Cancels breaking of the block that was punched
			return true;
		}
		else {
			if (getPlayerData(player).getSetup() == 1) {
				getPlayerData(player).setPos1(block.getLocation());

				player.sendMessage(prefix + "Punch point " + ChatColor.RED + "#2" + ChatColor.GOLD + " - the opposite corner of the arena");

				getPlayerData(player).setSetup(2);

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
			if(getPlayerData(player).isSpectating()) {
				if(getPlayerData(player).getArena() != null && getPlayerData(player).getArena().equals(arenaToBeRemoved.getUUID())) {
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
		
		getPlayerData(player).setArena(arena.getUUID());
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

		getPlayerData(player).setArena(null);
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
			if(getPlayerData(player).isSpectating() || player.getName().equals(sender.getName())) {
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
			if(getPlayerData(player).isSpectating()) {
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
			if (getPlayerData(target).isSpectating()) {
				updateSpectatorInventory(target);
			}
		}
	}

	/**
	 * Get the Spectator object (data store) for the player. It is created on-the-fly if needed.
	 *
	 * @param target The player to get the Spectator object of.
	 *
	 * @since 2.0
	 */
	public Spectator getPlayerData(Player target)
	{
		Spectator data = user.get(target.getName());

		// Created on-the-fly if needed.
		if(data == null)
		{
			data = new Spectator();
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

	public static SpectatorPlusOld get()
	{
		return instance;
	}
}
