/*
 * Copyright or © or Copr. AmauryCarrade (2015)
 * 
 * http://amaury.carrade.eu
 * 
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 * 
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package com.pgcraft.spectatorplus;

import com.pgcraft.spectatorplus.arenas.Arena;
import com.pgcraft.spectatorplus.arenas.ArenasManager;
import com.pgcraft.spectatorplus.commands.admin.BroadcastCommand;
import com.pgcraft.spectatorplus.commands.admin.ListCommand;
import com.pgcraft.spectatorplus.commands.admin.ManageArenasCommand;
import com.pgcraft.spectatorplus.commands.admin.SetLobbyCommand;
import com.pgcraft.spectatorplus.commands.admin.SetSpectatingModeCommand;
import com.pgcraft.spectatorplus.commands.admin.ToggleHideCommand;
import com.pgcraft.spectatorplus.commands.users.BackFromNoClipCommand;
import com.pgcraft.spectatorplus.commands.users.DisableSpectatorModeCommand;
import com.pgcraft.spectatorplus.commands.users.EnableSpectatorModeCommand;
import com.pgcraft.spectatorplus.guis.inventories.SpectatorsInventoryListener;
import com.pgcraft.spectatorplus.listeners.ArenaSetupListener;
import com.pgcraft.spectatorplus.listeners.GuiUpdatesListener;
import com.pgcraft.spectatorplus.listeners.ServerActionsListener;
import com.pgcraft.spectatorplus.listeners.SpectatorsChatListener;
import com.pgcraft.spectatorplus.listeners.SpectatorsInteractionsListener;
import com.pgcraft.spectatorplus.spectators.Spectator;
import com.pgcraft.spectatorplus.spectators.SpectatorsManager;
import com.pgcraft.spectatorplus.tasks.SpectatorManagerTask;
import fr.zcraft.zlib.components.commands.Commands;
import fr.zcraft.zlib.components.configuration.Configuration;
import fr.zcraft.zlib.components.gui.Gui;
import fr.zcraft.zlib.core.ZPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class SpectatorPlus extends ZPlugin
{
	public final static double VERSION = 3.0;

	private static SpectatorPlus instance;

	public final static String BASE_PREFIX = ChatColor.BLUE + "Spectator" + ChatColor.DARK_BLUE + "Plus";
	public final static String PREFIX = ChatColor.GOLD + "[" + BASE_PREFIX + ChatColor.GOLD + "] ";

	private SpectateAPI api;
	private SpectatorsManager spectatorsManager;
	private ArenasManager arenasManager;

	private Map<UUID, Spectator> spectators = new HashMap<>();


	@Override
	public void onLoad()
	{
		instance = this;

		// ZLib requirement
		super.onLoad();

		// Registers the Arena class as a serializable one.
		ConfigurationSerialization.registerClass(Arena.class);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onEnable()
	{
		// Loading zLib components
		loadComponents(Gui.class, Commands.class);

		// Loading config
		saveDefaultConfig();
		Configuration.init(Toggles.class);

		// Loading managers
		spectatorsManager = new SpectatorsManager(this);
		arenasManager = new ArenasManager(this);

		// Registering listeners
		final PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new ServerActionsListener(), this);
		pm.registerEvents(new SpectatorsInteractionsListener(), this);
		pm.registerEvents(new SpectatorsInventoryListener(), this);
		pm.registerEvents(new SpectatorsChatListener(), this);
		pm.registerEvents(new GuiUpdatesListener(), this);
		pm.registerEvents(new ArenaSetupListener(), this);

		// Registering commands
		Commands.register(
				new String[]{"spec", "spectate"},

				EnableSpectatorModeCommand.class,
				DisableSpectatorModeCommand.class,

				SetSpectatingModeCommand.class,
				SetLobbyCommand.class,
				ManageArenasCommand.class,
				ListCommand.class,

				ToggleHideCommand.class,
				BackFromNoClipCommand.class,

				BroadcastCommand.class
		);

		// Loading checking task
		getServer().getScheduler().runTaskTimer(this, new SpectatorManagerTask(), 20l, 20l);

		// Loading API
		api = new SpectateAPI();


		// Re-enable spectator mode if necessary
		for(Player player : getServer().getOnlinePlayers())
		{
			if (spectatorsManager.getSavedSpectatingPlayers().getConfig().contains(player.getUniqueId().toString()))
			{
				getPlayerData(player).setSpectating(true, true);
			}
		}
	}

	@Override
	public void onDisable()
	{
		// Disabling spectator mode for every spectator, so the inventories, etc., are saved by the server.
		for (Player player : getServer().getOnlinePlayers())
		{
			Spectator spectator = getPlayerData(player);

			if (spectator.isSpectating())
			{
				spectator.setSpectating(false, true);
				spectator.saveSpectatorModeInFile(true);
			}
		}

		// Just to be sure...
		arenasManager.save();
		spectatorsManager.save();

		// zLib requirement
		super.onDisable();
	}



	/* **  Data methods  ** */

	/**
	 * Returns the object representing a player inside SpectatorPlus.
	 *
	 * @param id The player's UUID.
	 *
	 * @return The object. It is created on-the-fly if not already instanced, so this never returns
	 * {@code null}.
	 */
	public Spectator getPlayerData(UUID id)
	{
		Spectator spectator = spectators.get(id);

		if (spectator == null)
		{
			spectator = new Spectator(id);
			spectators.put(id, spectator);
		}

		return spectator;
	}

	/**
	 * Returns the object representing a player inside SpectatorPlus.
	 *
	 * @param player The player.
	 *
	 * @return The object. It is created on-the-fly if not already instanced, so this never returns
	 * {@code null}.
	 */
	public Spectator getPlayerData(Player player)
	{
		return getPlayerData(player.getUniqueId());
	}



	/* **  Notifications methods  ** */

	/**
	 * Sends a message to the payer if the messages are enabled in the config.
	 *
	 * @param message The message to be sent. It will be prefixed by the Spectator Plus prefix.
	 * @param force {@code true} to send the message even if messages are not enabled.
	 */
	public void sendMessage(CommandSender receiver, String message, boolean force)
	{
		if (receiver != null && (!(receiver instanceof Player) || Toggles.OUTPUT_MESSAGES.get() || force))
		{
			receiver.sendMessage(SpectatorPlus.PREFIX + message);
		}
	}

	/**
	 * Sends a message to the payer if the messages are enabled in the config.
	 *
	 * @param message The message to be sent. It will be prefixed by the Spectator Plus prefix.
	 */
	public void sendMessage(CommandSender receiver, String message)
	{
		sendMessage(receiver, message, false);
	}

	public void sendMessage(Spectator receiver, String message)
	{
		sendMessage(receiver.getPlayerUniqueId(), message);
	}

	public void sendMessage(UUID id, String message)
	{
		if (id == null)
			return;

		Player player = getServer().getPlayer(id);
		if (player != null && player.isOnline())
			sendMessage(player, message);
	}



	/* **  Accessors  ** */

	public SpectatorsManager getSpectatorsManager()
	{
		return spectatorsManager;
	}

	public ArenasManager getArenasManager()
	{
		return arenasManager;
	}

	public SpectateAPI getAPI()
	{
		return api;
	}

	public static SpectatorPlus get()
	{
		return instance;
	}
}
