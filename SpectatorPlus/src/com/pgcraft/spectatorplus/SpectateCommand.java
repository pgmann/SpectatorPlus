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
		
		if(args.length == 0) {
			help(sender);
			return true;
		}
		
		String subcommandName = args[0].toLowerCase();
		
		// First: subcommand existence.
		if(!this.commands.contains(subcommandName)) {
			help(sender);
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
			help(sender);
			return true;
			
		} catch(SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			sender.sendMessage(p.prefix + "An error occured, see console for details. This is probably a bug, please report it!");
			e.printStackTrace();
			return false;
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
		
		sender.sendMessage(ChatColor.RED + "/spectate <on/off> [player]" + ChatColor.GOLD + ": Enables/disables spectator mode [for a certain player]");
		
		sender.sendMessage(ChatColor.RED + "/spectate arena <" + playerOnly + "add <name>/lobby <name>" + ChatColor.RED + "/remove <name>/reset/list>" + ChatColor.GOLD + ": Manages arenas");
		sender.sendMessage(ChatColor.RED + playerOnly + "/spectate lobby <set/del>" + ChatColor.GOLD + playerOnly + ": Adds/deletes the spectator lobby");		
		sender.sendMessage(ChatColor.RED + "/spectate mode <any/arena>" + ChatColor.GOLD + ": Sets who players can teleport to");
		
		sender.sendMessage(ChatColor.RED + playerOnly + "/spectate player <player>" + ChatColor.GOLD + playerOnly + ": Teleports the sender (spectator only) to <player>");
		
		sender.sendMessage(ChatColor.RED + "/spectate say <message>" + ChatColor.GOLD + ": Sends a message to spectator chat");

		sender.sendMessage(ChatColor.RED + "/spectate reload" + ChatColor.GOLD + ": Reloads configuration");
		
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
				case "mode":
				case "say":
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
					message = "You are not allowed to change the spectator mode of another player";
				}
				else {
					message = "You are not allowed to change your own spectator mode";
				}
				break;
			
			case "arena":
				message = "You don't have the permission to manage arenas.";
				break;
				
			case "lobby":
				message = "You don't have the permission to manage the main lobby.";
				break;
				
			case "reload":
				message = "You don't have the permission to reload the configuration.";
				break;
				
			case "mode":
				message = "You are not allowed to change the mode.";
				
			case "say":
				message = "You are not allowed to broadcast a message to the spectators' chat.";
				break;
		}
		
		sender.sendMessage(p.prefix + message);
	}
	
	
	/**
	 * This command enables the spectator mode on someone.
	 * Usage: /spec on [player]
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doOn(CommandSender sender, Command command, String label, String[] args) {
		
		if (args.length == 1) { // /spec on
			p.enableSpectate((Player) sender, sender);
		}
		
		else { // /spec on <player>
			Player player = p.getServer().getPlayer(args[1]);
			if (player != null) {
				p.enableSpectate(player, sender);
			}
			else {
				sender.sendMessage(p.prefix + ChatColor.RED + args[1] + ChatColor.GOLD + " isn't online");
			}
		}
	}
	
	/**
	 * This command disables the spectator mode on someone.
	 * Usage: /spec off [player]
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doOff(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 1) { // /spec off
			p.disableSpectate((Player) sender, sender);
		}
		
		else { // /spec off <player>
			Player player = p.getServer().getPlayer(args[1]);
			if (player != null) {
				p.disableSpectate(player, sender);
			}
			else {
				sender.sendMessage(p.prefix + ChatColor.RED + args[1] + ChatColor.GOLD + " isn't online");
			}
		}
	}
	
	/**
	 * Reloads the config from the files.
	 * Usage: /spec reload
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doReload(CommandSender sender, Command command, String label, String[] args) {
		p.setup.reloadConfig();
		p.toggles.reloadConfig();
		
		sender.sendMessage(p.prefix + "Config reloaded!");
	}
	
	/**
	 * Teleports a spectator to a player, just like picking a head in the teleportation GUI.
	 * Usage: /spec player <playerName>
	 * 
	 * TODO add argument to allow the console to teleport spectators to players.
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doPlayer(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) { // A player...
			if (p.user.get(sender.getName()).spectating) { // ...who is spectating...
				if (args.length > 1) { // ... and specified a name...
					Player target = p.getServer().getPlayer(args[1]);
					
					if (target != null && !p.user.get(target.getName()).spectating) { // ... of an online player
						p.choosePlayer((Player) sender, p.getServer().getPlayer(args[1]));
					}
					else {
						sender.sendMessage(p.prefix + ChatColor.WHITE + args[1] + ChatColor.GOLD + " isn't online!");
					}
					
				} else {
					sender.sendMessage(p.prefix + "Specify the player you want to spectate: /spec p <player>");
				}
			} else {
				sender.sendMessage(p.prefix + "You aren't spectating!");
			}
		} else {
			sender.sendMessage(p.prefix + "Cannot be executed from the console!");
		}
	}
	
	/**
	 * Teleports a spectator to a player, just like picking a head in the teleportation GUI.
	 * Usage: /spec p <playerName>
	 * 
	 * Alias of /spec player.
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doP(CommandSender sender, Command command, String label, String[] args) {
		doPlayer(sender, command, label, args);
	}
	
	/**
	 * This command can set or unset the main lobby.
	 * Usage: /spec lobby set|del|delete
	 * 
	 * This cannot be executed from the console.
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doLobby(CommandSender sender, Command command, String label, String[] args) {
		
		if(!(sender instanceof Player)) {
			sender.sendMessage(p.prefix + "Cannot be executed from the console!");
			return;
		}
		
		if(args.length == 1) { // /spec lobby
			sender.sendMessage(p.prefix + "Usage: " + ChatColor.RED + "/spectate lobby <set/del/delete>");
		}
		
		else {
			String subcommand = args[1];
			
			// /spec lobby set
			if(subcommand.equalsIgnoreCase("set")) {
				Location where = ((Player) sender).getLocation();
				
				p.setup.getConfig().set("xPos", Math.floor(where.getX())+0.5);
				p.setup.getConfig().set("yPos", Math.floor(where.getY()));
				p.setup.getConfig().set("zPos", Math.floor(where.getZ())+0.5);
				p.setup.getConfig().set("world", where.getWorld().getName());
				p.setup.getConfig().set("active", true);
				p.setup.saveConfig();
				
				sender.sendMessage(p.prefix + "Location saved! Players will be teleported here when they spectate");
			}
			
			// /spec lobby del|delete
			else if(subcommand.equalsIgnoreCase("del") || subcommand.equalsIgnoreCase("delete")) {
				p.setup.getConfig().set("xPos", 0);
				p.setup.getConfig().set("yPos", 0);
				p.setup.getConfig().set("zPos", 0);
				p.setup.getConfig().set("world", null);
				p.setup.getConfig().set("active", false);
				p.setup.saveConfig();
				
				sender.sendMessage(p.prefix + "Spectator lobby location removed! Players will be teleported to spawn when they spectate.");
			}
		}
	}
	
	/**
	 * This command changes the current mode:
	 *  - any: teleportation to any player;
	 *  - arena: teleportation to the players inside the current arena. Players outside an arena are unreachable.
	 * 
	 * Usage: /spec mode any|arena
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doMode(CommandSender sender, Command command, String label, String[] args) {
		
		if(args.length == 1) { // /spec mode
			sender.sendMessage(p.prefix + "Usage: " + ChatColor.RED + "/spectate mode <arena/any>");
		}
		
		else { // /spec mode <?>
			String mode = args[1];
			
			if(mode.equalsIgnoreCase("any") || mode.equalsIgnoreCase("arena")) {
				p.setup.getConfig().set("mode", mode.toLowerCase());
				p.setup.saveConfig();
				
				sender.sendMessage(p.prefix + "Mode set to " + ChatColor.RED + mode.toLowerCase());
				if(mode.equalsIgnoreCase("arena")) {
					sender.sendMessage(p.prefix + "Only players in arena regions can be teleported to by spectators.");
				}
			}
			else {
				sender.sendMessage(p.prefix + "The mode can be “arena” or “any”.");
			}
		}
	}
	
	
	/**
	 * This command manages the arenas.
	 * Usage: /spec arena add <name> | remove <name> | lobby <name> | reset | list
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doArena(CommandSender sender, Command command, String label, String[] args) {
		
		if(args.length == 1) { // /spec arena
			String playerOnly = "";
			if(!(sender instanceof Player)) {
				playerOnly = ChatColor.STRIKETHROUGH.toString();
			}
			
			sender.sendMessage(p.prefix + "Usage: " + ChatColor.RED + "/spectate arena <" + playerOnly +"add <name>/lobby <name>" + ChatColor.RED + "/remove <name>/reset/list>");
		}
		else {
			String subcommand = args[1];
			
			if(subcommand.equalsIgnoreCase("add")) { // /spec arena add ...
				
				if(!(sender instanceof Player)) {
					sender.sendMessage(p.prefix + "Cannot be executed from the console!");
					return;
				}
				
				if(args.length == 2) { // /spec arena add
					sender.sendMessage(p.prefix + "You need to specify a name for this arena.");
				}
				else { // /spec arena add <?>
					p.user.get(sender.getName()).arenaName = args[2];
					sender.sendMessage(p.prefix + "Punch point " + ChatColor.RED + "#1" + ChatColor.GOLD + " - a corner of the arena");
					p.user.get(sender.getName()).setup = 1;
				}
				
			}
			
			else if(subcommand.equalsIgnoreCase("remove")) { // spec arena remove ...
				
				if(args.length == 2) { // /spec arena remove
					sender.sendMessage(p.prefix + "You need to specify the name of the arena to remove.");
				}
				else { // /spec arena remove <?>
					if(p.removeArena(args[2])) {
						sender.sendMessage(p.prefix + "Arena " + ChatColor.RED + args[2] + ChatColor.GOLD + " removed.");
					}
					else {
						sender.sendMessage(p.prefix + "The arena " + ChatColor.RED + args[2] + ChatColor.GOLD + " does not exists!");
					}
				}
				
			}
			
			else if(subcommand.equalsIgnoreCase("list")) { // /spec arena list
				
				sender.sendMessage(ChatColor.GOLD + "          ~~ " + ChatColor.RED + "Arenas" + ChatColor.GOLD + " ~~          ");
				
				for (int i = 1; i < p.setup.getConfig().getInt("nextarena"); i++) {
					sender.sendMessage(ChatColor.RED + "(#" + i + ") " + p.setup.getConfig().getString("arena." + i + ".name") + ChatColor.GOLD + " Lobby x:" + p.setup.getConfig().getDouble("arena." + i + ".lobby.x") + " y:" + p.setup.getConfig().getDouble("arena." + i + ".lobby.y") + " z:" + p.setup.getConfig().getDouble("arena." + i + ".lobby.z"));
				}
				
			}
			
			else if(subcommand.equalsIgnoreCase("lobby")) { // /spec arena lobby
				
				if(!(sender instanceof Player)) {
					sender.sendMessage(p.prefix + "Cannot be executed from the console!");
					return;
				}
				
				p.setArenaLobbyLoc((Player) sender, args[2]);
				
			}
			
			else if(subcommand.equalsIgnoreCase("reset")) { // /spec arena reset
				
				p.setup.getConfig().set("arena", null);
				p.setup.getConfig().set("nextarena", 1);
				p.setup.saveConfig();
				
				sender.sendMessage(p.prefix + "All arenas removed.");
				
			}
		}
	}
	
	
	/**
	 * This command broadcasts a messages to the spectators.
	 * Usage: /spec say <message>
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doSay(CommandSender sender, Command command, String label, String[] args) {
		
		if(args.length == 1) {
			sender.sendMessage(p.prefix + "Usage: " + ChatColor.RED + "/spectate say <message>");
		}
		
		else {
			String message = "";
			for(int i = 1; i < args.length; i++) {
				message += args[i] + " ";
			}
			
			p.broadcastToSpectators(sender, message);
		}
		
	}
}
