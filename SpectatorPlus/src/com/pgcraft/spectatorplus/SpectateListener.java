package com.pgcraft.spectatorplus;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.Iterator;

@SuppressWarnings("deprecation")
public class SpectateListener implements Listener {
	
	private SpectatorPlusOld p; // Pointer to main class (see SpectatorPlusOld.java)

	protected SpectateListener(SpectatorPlusOld p) {
		this.p = p;
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
	 * Used to:<br>
	 *  - setup an arena (if the command was sent before by the sender).
	 * 
	 * @param e
	 */
	@EventHandler
	protected void onBlockDamage(BlockDamageEvent e) {
		// Set up mode
		if (p.arenaSetup(e.getPlayer(), e.getBlock())) {
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
}
