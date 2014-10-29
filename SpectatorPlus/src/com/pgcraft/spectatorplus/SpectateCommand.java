package com.pgcraft.spectatorplus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@SuppressWarnings({"deprecation","unused"})
public class SpectateCommand implements CommandExecutor {

	private SpectatorPlus p = null;
	private ArrayList<String> commands = new ArrayList<String>();


	public SpectateCommand(SpectatorPlus plugin) {
		this.p = plugin;

		commands.add("on");
		commands.add("off");
		commands.add("arena");
		commands.add("lobby");
		commands.add("player");
		commands.add("p");
		commands.add("reload");
		commands.add("mode");
		commands.add("say");
		commands.add("config");
		commands.add("hide");
	}


	/**
	 * Handles a command.
	 * 
	 * @param sender The sender
	 * @param command The executed command
	 * @param label The alias used for this command
	 * @param args The arguments given to the command
	 * 
	 * @author Amaury Carrade
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!command.getName().equalsIgnoreCase("spectate") && !command.getName().equalsIgnoreCase("spec")) {
			return false;
		}

		if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
			help(sender);
			return true;
		}

		String subcommandName = args[0].toLowerCase();

		// First: subcommand existence.
		if(!this.commands.contains(subcommandName)) {
			sender.sendMessage(SpectatorPlus.prefix+ChatColor.DARK_RED+"Invalid command. Use "+ChatColor.RED+"/spec"+ChatColor.DARK_RED+" for a list of commands.");
			return true;
		}

		// Second: is the sender allowed?
		if(!isAllowed(sender, command, args)) {
			unauthorized(sender, command, args);
			return true;
		}

		// Third: instantiation
		try {
			Class<? extends SpectateCommand> cl = this.getClass();
			Class[] parametersTypes = new Class[]{CommandSender.class, Command.class, String.class, String[].class};

			Method doMethod = cl.getDeclaredMethod("do" + WordUtils.capitalize(subcommandName), parametersTypes);

			doMethod.invoke(this, new Object[]{sender, command, label, args});

			return true;

		} catch (NoSuchMethodException e) {
			// Unknown method => unknown subcommand.
			sender.sendMessage(SpectatorPlus.prefix+ChatColor.DARK_RED+"Invalid command. Use "+ChatColor.RED+"/spec"+ChatColor.DARK_RED+" for a list of commands.");
			return true;

		} catch(SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			sender.sendMessage(SpectatorPlus.prefix + ChatColor.DARK_RED + "An error occured, see console for details. This is probably a bug, please report it!");
			e.printStackTrace();
			return true; // An error message has been printed, so command was technically handled.
		}
	}

	/**
	 * Prints the plugin main help page.
	 * 
	 * @param sender The help will be displayer for this sender.
	 */
	private void help(CommandSender sender) {

		String playerOnly = "";
		if(!(sender instanceof Player)) {
			playerOnly = ChatColor.STRIKETHROUGH.toString();
		}


		sender.sendMessage(ChatColor.GOLD + "            ~~ " + ChatColor.BLUE + "Spectator" + ChatColor.DARK_BLUE + "Plus" + ChatColor.GOLD + " ~~            ");

		sender.sendMessage(ChatColor.RED + "/spec <on/off> [player]" + ChatColor.GOLD + ": Enables/disables spectator mode [for a certain player]");

		sender.sendMessage(ChatColor.RED + "/spec arena <" + playerOnly + "add <name>/lobby <name>" + ChatColor.RED + "/remove <name>/reset/list>" + ChatColor.GOLD + ": Manages arenas");
		sender.sendMessage(ChatColor.RED + playerOnly + "/spec lobby <set/del>" + ChatColor.GOLD + playerOnly + ": Adds/deletes the spectator lobby");		
		sender.sendMessage(ChatColor.RED + "/spec mode <any/arena>" + ChatColor.GOLD + ": Sets who players can teleport to");

		sender.sendMessage(ChatColor.RED + playerOnly + "/spec player <player>" + ChatColor.GOLD + playerOnly + ": Teleports the sender (spectator only) to <player>");

		sender.sendMessage(ChatColor.RED + "/spec say <message>" + ChatColor.GOLD + ": Sends a message to spectator chat");

		sender.sendMessage(ChatColor.RED + "/spec config" + ChatColor.GOLD + ": Edit configuration from ingame");
		sender.sendMessage(ChatColor.RED + "/spec reload" + ChatColor.GOLD + ": Reloads configuration");
		
		sender.sendMessage(ChatColor.RED + "/spec hide [player]" + ChatColor.GOLD + ": Toggles whether you are shown in the spectator GUI");

		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.DARK_AQUA + "Strikethrough commands can only be executed as a player.");
		}
	}

	/**
	 * This method checks if an user is allowed to send a command.
	 * 
	 * @param sender
	 * @param subcommand
	 * @param args
	 * 
	 * @return boolean The allowance status.
	 */
	private boolean isAllowed(CommandSender sender, Command command, String[] args) {

		// The console is always allowed
		if(!(sender instanceof Player)) {
			return true;
		}

		else {

			if(sender.isOp()) {
				return true;
			}

			if(args.length == 0) { // Help
				return true;
			}

			// Centralized way to manage permissions
			String permission = null;

			switch(args[0]) {
			case "on":
			case "off":
				permission = (args.length >= 2) ? "spectate.use.others" : "spectate.use"; 
				break;

			case "arena":
			case "lobby":			
			case "reload":
			case "config":
			case "mode":
			case "say":
			case "hide":
				permission = "spectate.admin." + args[0];
				break;

			case "player":
			case "p":
				return true; // always allowed

			default:
				permission = "spectate"; // Should never happens. But, just in case...
				break;
			}

			return ((Player) sender).hasPermission(permission);
		}
	}

	/**
	 * This method sends a message to a player who try to use a command without the permission.
	 * 
	 * @param sender
	 * @param command
	 * @param args
	 */
	private void unauthorized(CommandSender sender, Command command, String[] args) {
		if(args.length == 0) {
			return; // will never happens, but just in case of a mistake...
		}

		String message = null;
		switch(args[0]) {
		case "on":
		case "off":
			if(args.length >= 2) {
				message = "You can't change the spectate mode of others!";
			}
			else {
				message = "You can't change your spectate mode!";
			}
			break;

		case "arena":
			message = "You can't manage arenas!";
			break;

		case "lobby":
			message = "You can't manage the global lobby.";
			break;

		case "reload":
			message = "You can't reload the configuration.";
			break;

		case "config":
			message = "You can't edit the configuration.";
			break;

		case "mode":
			message = "You can't change the plugin mode.";

		case "say":
			message = "You can't broadcast a message to the spectators' chat.";
			break;
		
		case "hide":
			message = "You can't toggle hide mode!";
			break;
		}

		sender.sendMessage(SpectatorPlus.prefix + ChatColor.DARK_RED + message);
	}


	/**
	 * This command enables the spectator mode on someone.<br>
	 * Usage: /spec on [player]
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	private void doOn(CommandSender sender, Command command, String label, String[] args) {

		if (args.length == 1) { // /spec on
			if(sender instanceof Player) {
				p.enableSpectate((Player) sender, sender);
			}
			else {
				sender.sendMessage(SpectatorPlus.prefix + "Usage: "+ChatColor.RED+"/spec on <player>");
			}
		}

		else { // /spec on <player>
			Player player = p.getServer().getPlayer(args[1]);
			if (player != null) {
				p.enableSpectate(player, sender);
			}
			else {
				sender.sendMessage(SpectatorPlus.prefix + ChatColor.RED + args[1] + ChatColor.GOLD + " isn't online!");
			}
		}
	}

	/**
	 * This command disables the spectator mode on someone.<br>
	 * Usage: /spec off [player]
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	private void doOff(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 1) { // /spec off
			if(sender instanceof Player) {
				p.disableSpectate((Player) sender, sender);
			}
			else {
				sender.sendMessage(SpectatorPlus.prefix + "Usage: "+ChatColor.RED+"/spec off <player>");
			}
		}

		else { // /spec off <player>
			Player player = p.getServer().getPlayer(args[1]);
			if (player != null) {
				p.disableSpectate(player, sender);
			}
			else {
				sender.sendMessage(SpectatorPlus.prefix + ChatColor.RED + args[1] + ChatColor.GOLD + " isn't online!");
			}
		}
	}

	/**
	 * Reloads the config from the files.<br>
	 * Usage: /spec reload
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	private void doReload(CommandSender sender, Command command, String label, String[] args) {
		p.reloadConfig(true);

		sender.sendMessage(SpectatorPlus.prefix + "Config reloaded!");
	}

	/**
	 * Edits the config from ingame.<br>
	 * Usage: /spec config &lt;toggle> &lt;value> [temp=false]
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	private void doConfig(CommandSender sender, Command command, String label, String[] args) {
		if (args.length >= 3) {
			String entry = args[1];
			boolean temp = (args.length > 3 && args[3] != null && args[3].equalsIgnoreCase("temp")) ? true : false;
			boolean success = true;
			String displayValue;
			String displayTemp = (temp) ? " until next reload" : "";
			// Booleans
			if (p.parseBoolean(args[2]) != null) {
				boolean value = (boolean) p.parseBoolean(args[2]);
				displayValue = String.valueOf(value);
				
				switch(entry) {
				case "compass":
					p.getAPI().setCompass(value, temp);
					break;
				case "arenaclock":
					p.getAPI().setArenaClock(value, temp);
					break;
				case "inspector":
					p.getAPI().setInspector(value, temp);
					break;
				case "inspectPlayerFromTeleportationMenu":
					p.getAPI().setInspectPlayerFromTeleportationMenu(value, temp);
					break;
				case "specchat":
					p.getAPI().setSpectatorChatEnabled(value, temp);
					break;
				case "outputmessages":
					p.getAPI().setOutputMessages(value, temp);
					break;
				case "deathspec":
					p.getAPI().setSpectateOnDeath(value, temp);
					break;
				case "colouredtablist":
					p.getAPI().setColouredTabList(value, temp);
					break;
				case "seespecs":
					p.getAPI().setSeeSpectators(value, temp);
					break;
				case "blockcmds":
					p.getAPI().setBlockCommands(value, temp);
					break;
				case "adminbypass":
					p.getAPI().setAllowAdminBypassCommandBlocking(value, temp);
					break;
				case "tpToDeathTool":
					p.getAPI().setTPToDeathTool(value, temp);
					break;
				case "tpToDeathToolShowCause":
					p.getAPI().setShowCauseInTPToDeathTool(value, temp);
					break;
				case "newbieMode":
					p.getAPI().setNewbieMode(value, temp);
					break;
				case "teleportToSpawnOnSpecChangeWithoutLobby":
					p.getAPI().setTeleportToSpawnOnSpecChangeWithoutLobby(value, temp);
					break;
				case "useSpawnCommandToTeleport":
					p.getAPI().setUseSpawnCommandToTeleport(value, temp);
					break;
				default:
					success = false;
					break;
				}
				
			} else /* Strings */ {
				String value = args[2];
				displayValue = value;
				
				switch(entry) {
				case "compassItem":
					p.getAPI().setCompassItem(value, temp);
					break;
				case "clockItem":
					p.getAPI().setClockItem(value, temp);
					break;
				case "inspectorItem":
					p.getAPI().setInspectorItem(value, temp);
					break;
				case "spectatorsToolsItem":
					p.getAPI().setSpectatorsToolsItem(value, temp);
					break;
				default:
					success = false;
					break;
				}
				
			}
			if (success) {
				sender.sendMessage(SpectatorPlus.prefix+"Set "+ChatColor.RED+entry+ChatColor.GOLD+" to "+ChatColor.RED+displayValue+ChatColor.GOLD+displayTemp);
			} else {
				sender.sendMessage(SpectatorPlus.prefix+ChatColor.DARK_RED+"Toggle "+ChatColor.RED+entry+ChatColor.DARK_RED+" does not exist!");
			}
		} else {
			sender.sendMessage(SpectatorPlus.prefix+"Usage: "+ChatColor.RED+"/spec config <toggle> <value> [temp]");
		}
	}

	/**
	 * Teleports a spectator to a player, just like picking a head in the teleportation GUI.<br>
	 * Usage: /spec player &lt;playerName>
	 * <p>
	 * <i>(TODO: add argument to allow the console to teleport spectators to players.)</i>
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	private void doPlayer(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) { // A player...
			if (p.getPlayerData((Player) sender).spectating) { // ...who is spectating...
				if (args.length > 1) { // ... and specified a name...
					Player target = p.getServer().getPlayer(args[1]);

					if (target != null && !p.getPlayerData(target).spectating) { // ... of an online player
						p.choosePlayer((Player) sender, p.getServer().getPlayer(args[1]));
					}
					else {
						sender.sendMessage(SpectatorPlus.prefix + ChatColor.RED + args[1] + ChatColor.GOLD + " isn't online or is spectating!");
					}

				} else {
					sender.sendMessage(SpectatorPlus.prefix + "Usage: "+ChatColor.RED+"/spec p <player>");
				}
			} else {
				sender.sendMessage(SpectatorPlus.prefix + "You aren't spectating!");
			}
		} else {
			sender.sendMessage(SpectatorPlus.prefix + "Cannot be executed from the console!");
		}
	}

	/**
	 * Teleports a spectator to a player, just like picking a head in the teleportation GUI.<br>
	 * Usage: /spec p &lt;playerName>
	 * <p>
	 * Alias of /spec player.
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	private void doP(CommandSender sender, Command command, String label, String[] args) {
		doPlayer(sender, command, label, args);
	}

	/**
	 * This command can set or unset the main lobby.<br>
	 * Usage: /spec lobby &lt;set|del|delete>
	 * 
	 * This cannot be executed from the console.
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	private void doLobby(CommandSender sender, Command command, String label, String[] args) {
		boolean isEmptyCommand = false;
		String subcommand = null;

		if(!(sender instanceof Player)) {
			sender.sendMessage(SpectatorPlus.prefix + "Cannot be executed from the console!");
			return;
		}

		if(args.length == 1) { // /spec lobby
			isEmptyCommand = true;
		} else {
			subcommand = args[1];
		}

		// /spec lobby set
		if(!isEmptyCommand && subcommand.equalsIgnoreCase("set")) {
			Location where = ((Player) sender).getLocation();

			p.setup.getConfig().set("xPos", Math.floor(where.getX())+0.5);
			p.setup.getConfig().set("yPos", Math.floor(where.getY()));
			p.setup.getConfig().set("zPos", Math.floor(where.getZ())+0.5);
			p.setup.getConfig().set("world", where.getWorld().getName());
			p.setup.getConfig().set("active", true);
			p.setup.saveConfig();

			sender.sendMessage(SpectatorPlus.prefix + "Global spectator lobby location set!");
		}

		// /spec lobby del|delete
		else if(!isEmptyCommand && (subcommand.equalsIgnoreCase("del") || subcommand.equalsIgnoreCase("delete"))) {
			p.setup.getConfig().set("xPos", 0);
			p.setup.getConfig().set("yPos", 0);
			p.setup.getConfig().set("zPos", 0);
			p.setup.getConfig().set("world", null);
			p.setup.getConfig().set("active", false);
			p.setup.saveConfig();

			sender.sendMessage(SpectatorPlus.prefix + "Global spectator lobby location removed! Using "+ChatColor.WHITE+"/spawn"+ChatColor.GOLD+" instead.");
		}

		else {
			sender.sendMessage(SpectatorPlus.prefix + "Usage: " + ChatColor.RED + "/spec lobby <set/del[ete]>");
		}
	}

	/**
	 * This command changes the current mode:<br>
	 *  - any: teleportation to any player;<br>
	 *  - arena: teleportation to the players inside the current arena. Players outside an arena are unreachable.
	 * <p>
	 * Usage: /spec mode &lt;any|arena>
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	private void doMode(CommandSender sender, Command command, String label, String[] args) {

		if(args.length == 1) { // /spec mode
			sender.sendMessage(SpectatorPlus.prefix + "Usage: " + ChatColor.RED + "/spec mode <arena/any>");
		}

		else { // /spec mode <?>
			String mode = args[1];

			if(mode.equalsIgnoreCase("any") || mode.equalsIgnoreCase("arena")) {
				p.setup.getConfig().set("mode", mode.toLowerCase());
				p.setup.saveConfig();

				sender.sendMessage(SpectatorPlus.prefix + "Mode set to " + ChatColor.RED + mode.toLowerCase());
				if(mode.equalsIgnoreCase("arena")) {
					sender.sendMessage(SpectatorPlus.prefix + "Only players in arena regions can be teleported to by spectators.");
				}
			}
			else {
				sender.sendMessage(SpectatorPlus.prefix + "The mode can be \"arena\" or \"any\".");
			}
		}
	}


	/**
	 * This command manages the arenas.<br>
	 * Usage: /spec arena &lt;add &lt;name> | remove &lt;name> | lobby &lt;name> | reset | list>
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	private void doArena(CommandSender sender, Command command, String label, String[] args) {
		boolean isEmptyCommand = false;
		String subcommand = null;

		if(args.length == 1) { // /spec arena
			isEmptyCommand = true;
		} else {
			subcommand = args[1];
		}

		if(!isEmptyCommand && subcommand.equalsIgnoreCase("add")) { // /spec arena add ...

			if(!(sender instanceof Player)) {
				sender.sendMessage(SpectatorPlus.prefix + "Cannot be executed from the console!");
				return;
			}

			if(args.length == 2) { // /spec arena add
				sender.sendMessage(SpectatorPlus.prefix + "Usage: "+ChatColor.RED+"/spec arena add <arenaName>");
			}
			else { // /spec arena add <?>
				p.getPlayerData((Player) sender).arenaName = args[2];
				sender.sendMessage(SpectatorPlus.prefix + "Punch point " + ChatColor.RED + "#1" + ChatColor.GOLD + " - a corner of the arena");
				p.getPlayerData((Player) sender).setup = 1;
			}

		}

		else if(!isEmptyCommand && subcommand.equalsIgnoreCase("remove")) { // spec arena remove ...

			if(args.length == 2) { // /spec arena remove
				sender.sendMessage(SpectatorPlus.prefix + "Usage: "+ChatColor.RED+"/spec arena remove <arenaName>");
			}
			else { // /spec arena remove <?>
				if(p.removeArena(args[2])) {
					sender.sendMessage(SpectatorPlus.prefix + "Arena " + ChatColor.RED + args[2] + ChatColor.GOLD + " removed.");
				}
				else {
					sender.sendMessage(SpectatorPlus.prefix + "The arena " + ChatColor.RED + args[2] + ChatColor.GOLD + " does not exist!");
				}
			}

		}

		else if(!isEmptyCommand && subcommand.equalsIgnoreCase("list")) { // /spec arena list

			sender.sendMessage(ChatColor.GOLD + "          ~~ " + ChatColor.RED + "Arenas" + ChatColor.GOLD + " ~~          ");

			for(Arena arena : p.arenasManager.getArenas()) {
				String arenaDescription = ChatColor.RED + arena.getName();
				if(arena.getLobby() != null) {
					arenaDescription += ChatColor.GOLD + " - Lobby: " + arena.getLobby().getBlockX() + ";" + arena.getLobby().getBlockY() + ";" + arena.getLobby().getBlockZ();  
				}
				else {
					arenaDescription += ChatColor.GOLD + " - Lobby not configured";
				}
				sender.sendMessage(arenaDescription);
			}

		}

		else if(!isEmptyCommand && subcommand.equalsIgnoreCase("lobby")) { // /spec arena lobby

			if(!(sender instanceof Player)) {
				sender.sendMessage(SpectatorPlus.prefix + "Cannot be executed from the console!");
				return;
			}

			Arena arena = p.arenasManager.getArena(args[2]);
			if(arena != null) {
				arena.setLobby(((Player) sender).getLocation());
				p.arenasManager.save();

				sender.sendMessage(SpectatorPlus.prefix + "Arena " + ChatColor.RED + args[2] + ChatColor.GOLD + "'s lobby location set to your location");
			}
			else {
				sender.sendMessage(SpectatorPlus.prefix + "Arena " + ChatColor.RED + args[2] + ChatColor.GOLD + " doesn't exist!");
			}

		}

		else if(!isEmptyCommand && subcommand.equalsIgnoreCase("reset")) { // /spec arena reset

			p.arenasManager.reset();

			sender.sendMessage(SpectatorPlus.prefix + "All arenas removed.");

		}

		else {
			String playerOnly = "";
			if(!(sender instanceof Player)) playerOnly = ChatColor.DARK_RED+""+ChatColor.STRIKETHROUGH;

			sender.sendMessage(SpectatorPlus.prefix + "Usage: " + ChatColor.RED + "/spec arena <" + playerOnly +"add <name>/lobby <name>" + ChatColor.RED + "/remove <name>/reset/list>");
		}
	}


	/**
	 * This command broadcasts a message to the spectators.<br>
	 * Usage: /spec say &lt;message>
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	private void doSay(CommandSender sender, Command command, String label, String[] args) {

		if(args.length == 1) {
			sender.sendMessage(SpectatorPlus.prefix + "Usage: " + ChatColor.RED + "/spec say <message>");
		}

		else {
			String message = "";
			for(int i = 1; i < args.length; i++) {
				message += args[i] + " ";
			}

			p.broadcastToSpectators(sender, message);
		}

	}
	
	/**
	 * This command hide a player from the spectators.<br>
	 * Usage: /spec hide [player]
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	private void doHide(CommandSender sender, Command command, String label, String[] args) {

		if(args.length == 0) {
			sender.sendMessage(SpectatorPlus.prefix + "Usage: " + ChatColor.RED + "/spec hide [player]");
		} else {
			// Set the target...
			Player target;
			if (args.length <= 1) {
				if(sender instanceof Player)
					target = (Player) sender;
				else {
					sender.sendMessage(SpectatorPlus.prefix + "Please specify a player: " + ChatColor.RED + "/spec hide <player>");
					return;
				}
			} else if (p.getServer().getPlayer(args[1]) != null) {
				target = p.getServer().getPlayer(args[1]);
			} else {
				sender.sendMessage(SpectatorPlus.prefix + ChatColor.RED + args[1] + ChatColor.GOLD + " isn't online!");
				return;
			}
			
			// Toggle hide mode for them.
			p.getPlayerData(target).hideFromTp = !p.user.get(target.getName()).hideFromTp;
			
			// Notify the sender.
			String state = (p.getPlayerData(target).hideFromTp) ? ChatColor.GREEN+"enabled" : ChatColor.DARK_RED+"disabled";
			sender.sendMessage(SpectatorPlus.prefix + "Hide mode for " + target.getName() + " is now " + state);
		}

	}
	
	
	/**
	 * Returns a list of the commands.
	 * @return
	 */
	protected ArrayList<String> getCommands() {
		return commands;
	}
}
