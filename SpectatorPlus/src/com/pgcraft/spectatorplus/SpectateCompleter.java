package com.pgcraft.spectatorplus;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class SpectateCompleter implements TabCompleter {
	
	private SpectatorPlus p = null;
	
	
	public SpectateCompleter(SpectatorPlus plugin) {
		this.p = plugin;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (!command.getName().equalsIgnoreCase("spec") && !command.getName().equalsIgnoreCase("spectate")) {
			return null;
		}
		
		// Autocompletion for subcommands
		if(args.length == 1) {
			return getAutocompleteSuggestions(args[0], p.commands.getCommands());
		}
		
		else if(args[0].equalsIgnoreCase("arena")) {
			
			// Autocompletion for /spec arena subcommands
			if(args.length == 2) {
				return getAutocompleteSuggestions(args[1], Arrays.asList("add", "lobby", "remove", "reset", "list"));
			}
			
			// Autocompletion for /spec arena lobby|remove: arena name
			else if(args[1].equalsIgnoreCase("lobby") || args[1].equalsIgnoreCase("remove")) {
				ArrayList<String> suggestions = new ArrayList<String>();
				
				for(Arena arena : p.arenasManager.getArenas()) {
					suggestions.add(arena.getName());
				}
				
				return getAutocompleteSuggestions(args[2], suggestions);
			}
			
		}
		
		else if(args[0].equalsIgnoreCase("lobby")) {
			// Autocompletion for /spec lobby subcommands
			if(args.length == 2) {
				return getAutocompleteSuggestions(args[1], Arrays.asList("set", "delete"));
			}
		}
		
		else if(args[0].equalsIgnoreCase("mode")) {
			// Autocompletion for /spec mode: modes
			if(args.length == 2) {
				ArrayList<String> suggestions = new ArrayList<String>();
				suggestions.add("any");
				suggestions.add("arena");
				return getAutocompleteSuggestions(args[1], Arrays.asList("any", "arena"));
			}
		}
		
		else if(args[0].equalsIgnoreCase("config")) {
			// Autocompletion for /spec config: keys
			if(args.length == 2) {
				ArrayList<String> suggestions = new ArrayList<String>();
				
				for(String key : p.toggles.getConfig().getKeys(true)) {
					if(!key.equals("version")) suggestions.add(key);
				}
				
				return getAutocompleteSuggestions(args[1], suggestions);
			}
			
			else if(args.length == 3) {
				switch(args[1]) {
					case "compass":
					case "arenaclock":
					case "inspector":
					case "inspectPlayerFromTeleportationMenu":
					case "specchat":
					case "outputmessages":
					case "deathspec":
					case "colouredtablist":
					case "seespecs":
					case "blockcmds":
					case "adminbypass":
					case "tpToDeathTool":
					case "tpToDeathToolShowCause":
						return getAutocompleteSuggestions(args[2], Arrays.asList("true", "false"));
					case "compassItem":
					case "clockItem":
					case "inspectorItem":
					case "spectatorsToolsItem":
						ArrayList<String> suggestions = new ArrayList<String>();
						for(Material material : Material.values()) {
							suggestions.add(material.toString());
						}
						return getAutocompleteSuggestions(args[2], suggestions);
				}
			}
			
			else if(args.length == 4) {
				return getAutocompleteSuggestions(args[3], Arrays.asList("temp"));
			}
		}
		
		return null;
	}
	
	
	/**
	 * Returns a list of autocompletion suggestions based on what the user typed and on a list of
	 * available commands.
	 * 
	 * @param typed What the user typed. This string needs to include <em>all</em> the words typed.
	 * @param suggestionsList The list of the suggestions.
	 * @param numberOfWordsToIgnore If non-zero, this number of words will be ignored at the beginning of the string. This is used to handle multiple-words autocompletion.
	 * 
	 * @return The list of matching suggestions.
	 */
	private List<String> getAutocompleteSuggestions(String typed, List<String> suggestionsList, int numberOfWordsToIgnore) {
		List<String> list = new ArrayList<String>();
		
		// For each suggestion:
		//  - if there isn't any world to ignore, we just compare them;
		//  - else, we removes the correct number of words at the beginning of the string;
		//    then, if the raw suggestion matches the typed text, we adds to the suggestion list
		//    the filtered suggestion, because the Bukkit's autocompleter works on a “per-word” basis.
		
		for(String rawSuggestion : suggestionsList) {
			String suggestion = "";
			
			if(numberOfWordsToIgnore == 0) {
				suggestion = rawSuggestion;
			}
			else {
				// Not the primary use, but, hey! It works.
				suggestion = getStringFromArray(rawSuggestion.split(" "), numberOfWordsToIgnore);
			}
			
			if(rawSuggestion.toLowerCase().startsWith(typed.toLowerCase())) {
				list.add(suggestion);
			}
		}
		
		Collections.sort(list, Collator.getInstance());
		
		return list;
	}
	
	/**
	 * Returns a list of autocompletion suggestions based on what the user typed and on a list of
	 * available commands.
	 * 
	 * @param typed What the user typed.
	 * @param suggestionsList The list of the suggestions.
	 * 
	 * @return The list of matching suggestions.
	 */
	private List<String> getAutocompleteSuggestions(String typed, List<String> suggestionsList) {
		return getAutocompleteSuggestions(typed, suggestionsList, 0);
	}
	
	
	/**
	 * Extracts a string from an array, starting at the given index and separating each string with a space.
	 * 
	 * @param args The raw arguments.
	 * @param startIndex The index of the first item in the returned string (first argument given: 0).
	 * 
	 * @return The extracted string.
	 * 
	 * @throws IllegalArgumentException if the index of the first element is out of the bounds of the arguments' list.
	 */
	private String getStringFromArray(String[] args, int startIndex) {
		if(args.length < startIndex) {
			throw new IllegalArgumentException("The index of the first element is out of the bounds of the arguments' list.");
		}
		
		String text = "";
		
		for(int index = startIndex; index < args.length; index++) {
			if(index < args.length - 1) {
				text += args[index] + " ";
			}
			else {
				text += args[index];
			}
		}
		
		return text;
	}

}
