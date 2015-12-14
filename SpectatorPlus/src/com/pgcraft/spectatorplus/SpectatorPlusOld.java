package com.pgcraft.spectatorplus;

import com.pgcraft.spectatorplus.arenas.Arena;
import com.pgcraft.spectatorplus.arenas.ArenasManager;
import com.pgcraft.spectatorplus.spectators.Spectator;
import com.pgcraft.spectatorplus.spectators.SpectatorMode;
import com.pgcraft.spectatorplus.tasks.SpectatorManagerTask;
import com.pgcraft.spectatorplus.utils.ConfigAccessor;
import fr.zcraft.zlib.core.ZPlugin;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.text.DecimalFormat;
import java.util.ArrayList;
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
