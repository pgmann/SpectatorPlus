/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.pgcraft.spectatorplus.spectators;

import com.pgcraft.spectatorplus.SpectatorPlus;
import com.pgcraft.spectatorplus.Toggles;
import com.pgcraft.spectatorplus.arenas.Arena;
import com.pgcraft.spectatorplus.arenas.ArenaSetup;
import com.pgcraft.spectatorplus.guis.TeleportationGUI;
import com.pgcraft.spectatorplus.utils.Collisions;
import com.pgcraft.spectatorplus.utils.SPUtils;
import fr.zcraft.zlib.components.gui.Gui;
import fr.zcraft.zlib.tools.players.ReducedDebugInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Collection;
import java.util.UUID;

public class Spectator
{
    private final UUID playerID;

	private Boolean spectating = false;
	private Boolean teleporting = false;
	private Boolean gamemodeChangeAllowed = true;
    private Boolean hiddenFromTp = false;
    private Boolean wasSpectatorBeforeWorldChanged = false;

	private Arena arena = null;
	private ArenaSetup arenaSetup = null;


	private ItemStack[] oldInventoryContent = null;
	private ItemStack[] oldArmourContent = null;
	private Collection<PotionEffect> oldEffects = null;

	private Scoreboard oldScoreboard = null;

	private GameMode oldGameMode = null;
	private Boolean oldFlyMode = null;
	private Float oldFlySpeed = null;
	private Boolean oldCollision = null;


	private String lastDeathMessage = null;
	private Location deathLocation = null;


	public Spectator(final UUID id)
    {
        playerID = id;
	}


	/**
	 * Returns the Bukkit player object, or {@code null} if offline.
	 *
	 * @return The {@link Player} object.
	 */
	public Player getPlayer()
	{
		return Bukkit.getPlayer(playerID);
	}

	public UUID getPlayerUniqueId()
	{
		return playerID;
	}



	/* **  Methods to enable or disable the spectator mode  ** */


    /**
     * True if the player is currently spectating.
     */
    public boolean isSpectating()
    {
        return spectating;
    }

	/**
	 * Enables or disables the spectator mode on this player.
	 *
	 * @param spectating {@code true} to enable the spectator mode.
	 */
	public void setSpectating(boolean spectating)
	{
		setSpectating(spectating, false);
	}

	/**
	 * Enables or disables the spectator mode on this player.
	 *
	 * @param spectating {@code true} to enable the spectator mode.
	 * @param silent {@code true} to silently enable or disable the spectator mode.
	 */
	public void setSpectating(boolean spectating, boolean silent)
	{
		setSpectating(spectating, null, silent);
	}

	/**
	 * Enables or disables the spectator mode on this player.
	 *
	 * @param spectating {@code true} to enable the spectator mode.
	 * @param executor the player, console or command block ordering the spectator mode change.
	 */
	public void setSpectating(boolean spectating, CommandSender executor)
	{
		setSpectating(spectating, executor, false);
	}

	/**
	 * Enables or disables the spectator mode on this player.
	 *
	 * @param spectating {@code true} to enable the spectator mode.
	 * @param executor the player, console or command block ordering the spectator mode change.
	 * @param silent {@code true} to silently enable or disable the spectator mode.
	 */
	public void setSpectating(boolean spectating, CommandSender executor, boolean silent)
	{
		setSpectating(spectating, executor, silent, false);
	}

	/**
	 * Enables or disables the spectator mode on this player.
	 *
	 * @param spectating {@code true} to enable the spectator mode.
	 * @param executor the player, console or command block ordering the spectator mode change.
	 * @param silent {@code true} to silently enable or disable the spectator mode.
	 * @param worldChange {@code true} if the spectator mode is changed due to a world change. Internal use.
	 */
    public void setSpectating(boolean spectating, CommandSender executor, boolean silent, boolean worldChange)
    {
        if (spectating != this.spectating)
        {
            this.spectating = spectating;

	        if (executor == null)
		        executor = Bukkit.getConsoleSender();

            if (spectating)
	            enableSpectatorMode(executor, silent, worldChange);
	        else
	            disableSpectatorMode(executor, silent, worldChange);

	        if(!worldChange || SpectatorPlus.get().getSpectatorsManager().getSpectatorsMode() == SpectatorMode.WORLD)
		        Gui.update(TeleportationGUI.class);
        }
    }

	/**
	 * Enables the spectator mode on this player.
	 *
	 * @param executor the player, console or command block ordering the spectator mode change.
	 * @param silent {@code true} to silently enable the spectator mode.
	 * @param worldChange {@code true} if the spectator mode is changed due to a world change.
	 */
    private void enableSpectatorMode(CommandSender executor, boolean silent, boolean worldChange)
    {
		Player player = getPlayer();
	    if (player == null)
		    return;


	    // We save all the previous data to be able to restore it after
	    oldInventoryContent = player.getInventory().getContents();
	    oldArmourContent    = player.getInventory().getArmorContents();
	    oldEffects          = player.getActivePotionEffects();
	    oldGameMode         = player.getGameMode();
	    oldFlyMode          = player.getAllowFlight();
	    oldFlySpeed         = player.getFlySpeed();
	    oldCollision        = Collisions.collidesWithEntities(player);
	    oldScoreboard       = player.getScoreboard() != null ? player.getScoreboard() : Bukkit.getScoreboardManager().getMainScoreboard();


	    // We clear the player
	    for (PotionEffect effect : player.getActivePotionEffects())
	    {
		    player.removePotionEffect(effect.getType());
	    }

	    player.setFireTicks(0);

	    // We add spectating capabilities to the player
	    player.setAllowFlight(true);
	    player.setFoodLevel(20);
	    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 15), true);

	    // We disable all interactions if possible
	    player.setGameMode(Toggles.VANILLA_SPECTATOR_MODE.get() ? GameMode.SPECTATOR : GameMode.ADVENTURE);
	    Collisions.setCollidesWithEntities(player, false);
	    setGamemodeChangeAllowed(false);

	    // We update the spectator's inventory
	    SpectatorPlus.get().getSpectatorsManager().getInventoryManager().equipSpectator(player);

	    // We hide this player if seeSpecs mode is off and the target isn't spectating
	    for (Player other : Bukkit.getOnlinePlayers())
	    {
			if (!other.equals(player))
			{
				if (Toggles.SPECTATORS_SEE_OTHERS.get() && SpectatorPlus.get().getPlayerData(other).isSpectating())
				{
					player.showPlayer(other);
				}
				else
				{
					other.hidePlayer(player);
				}
			}
	    }

	    // We set the SpectatorPlus' scoreboard (if needed)
	    if (Toggles.SPECTATORS_TABLIST_PREFIX.get())
	    {
		    SpectatorPlus.get().getSpectatorsManager().setSpectatorsScoreboard(this);
		    SpectatorPlus.get().getSpectatorsManager().setSpectatingInScoreboard(this);
	    }

	    // We reduce debug infos (if needed)
	    if (Toggles.REDUCE_DEBUG_INFO.get())
	    {
		    ReducedDebugInfo.setForPlayer(player, true);
	    }

	    // We teleports the player to the spectating lobby, if needed
	    if (!worldChange)
	    {
		    SpectatorPlus.get().getSpectatorsManager().teleportToLobby(this);
	    }


	    // We save the fact that this player is spectating
	    saveSpectatorModeInFile(true);


	    // We notify the spectator and the sender if needed
	    if (!silent)
	    {
		    // Spectator mode enabled on itself
		    if (executor instanceof Player && ((Player) executor).getUniqueId().equals(playerID))
		    {
			    SpectatorPlus.get().sendMessage(player, "Spectator mode " + ChatColor.RED + "enabled");
		    }
		    else
		    {
			    SpectatorPlus.get().sendMessage(player, "Spectator mode " + ChatColor.RED + "enabled" + ChatColor.GOLD + " by " + ChatColor.DARK_RED + SPUtils.getName(executor));
			    SpectatorPlus.get().sendMessage(executor, "Spectator mode " + ChatColor.RED + "enabled" + ChatColor.GOLD + " for " + ChatColor.DARK_RED + player.getName());
		    }
	    }
    }

	/**
	 * Disables the spectator mode on this player.
	 *
	 * @param executor the player, console or command block ordering the spectator mode change.
	 * @param silent {@code true} to silently disable the spectator mode.
	 * @param worldChange {@code true} if the spectator mode is changed due to a world change.
	 */
	@SuppressWarnings("deprecation")
	private void disableSpectatorMode(CommandSender executor, boolean silent, boolean worldChange)
	{
		Player player = getPlayer();
		if (player == null)
			return;


		// We restore the player: effects, inventory, collisions...
		player.removePotionEffect(PotionEffectType.INVISIBILITY);
		player.removePotionEffect(PotionEffectType.SPEED);
		player.removePotionEffect(PotionEffectType.WATER_BREATHING);
		player.removePotionEffect(PotionEffectType.NIGHT_VISION);
		player.addPotionEffects(oldEffects);

		player.setFlySpeed(oldFlySpeed);

		Collisions.setCollidesWithEntities(player, oldCollision);
		setGamemodeChangeAllowed(true);

		player.setAllowFlight(oldFlyMode);
		player.setGameMode(oldGameMode);

		player.getInventory().setContents(oldInventoryContent);
		player.getInventory().setArmorContents(oldArmourContent);
		player.updateInventory();

		// The arena is reset (silently)
		if (!worldChange)
		{
			setArena(null, true);
		}

		// The player is shown
		for (Player other : Bukkit.getOnlinePlayers())
		{
			if(!other.equals(player))
			{
				if (SpectatorPlus.get().getPlayerData(other).isSpectating())
				{
					player.hidePlayer(other);
				}

				other.showPlayer(player);
			}
		}

		// The scoreboard is reset
		if (Toggles.SPECTATORS_TABLIST_PREFIX.get())
		{
			SpectatorPlus.get().getSpectatorsManager().setSpectatorsScoreboard(this);
			SpectatorPlus.get().getSpectatorsManager().setSpectatingInScoreboard(this);
		}

		// The debug infos are expanded (if needed)
		if (Toggles.REDUCE_DEBUG_INFO.get())
		{
			ReducedDebugInfo.setForPlayer(player, false);
		}

		// The player is teleported back to the spawn if needed
		if (!worldChange)
		{
			SpectatorPlus.get().getSpectatorsManager().teleportToLobby(this);
		}


		// The player is unmarked as spectator in file
		saveSpectatorModeInFile(false);


		// The saved data is cleared
		oldFlyMode          = null;
		oldFlySpeed         = null;
		oldGameMode         = null;
		oldInventoryContent = null;
		oldArmourContent    = null;
		oldEffects          = null;
		oldScoreboard       = null;


		// We notify the spectator and the sender if needed
		if (!silent)
		{
			// Spectator mode enabled on itself
			if (executor instanceof Player && ((Player) executor).getUniqueId().equals(playerID))
			{
				SpectatorPlus.get().sendMessage(player, "Spectator mode " + ChatColor.RED + "disabled");
			}
			else
			{
				SpectatorPlus.get().sendMessage(player, "Spectator mode " + ChatColor.RED + "disabled" + ChatColor.GOLD + " by " + ChatColor.DARK_RED + SPUtils.getName(executor));
				SpectatorPlus.get().sendMessage(executor, "Spectator mode " + ChatColor.RED + "disabled" + ChatColor.GOLD + " for " + ChatColor.DARK_RED + player.getName());
			}
		}
	}

	/**
	 * Saves in a file the spectating status of this player. The saved status will be used when the player log-ins.
	 * @param spectating {@code true} to save the spectator mode; {@code false} to clean it from the file.
	 */
	public void saveSpectatorModeInFile(boolean spectating)
	{
		SpectatorPlus.get().getSpectatorsManager().getSavedSpectatingPlayers().getConfig().set(playerID.toString(), spectating ? true : null);
		SpectatorPlus.get().getSpectatorsManager().getSavedSpectatingPlayers().saveConfig();
	}

	/**
	 * Resets the player's scoreboard to the one
	 */
	public void resetScoreboard()
	{
		if (oldScoreboard != null)
		{
			Player player = getPlayer();
			if (player != null) player.setScoreboard(oldScoreboard);
		}
	}



	/* **  Other methods  ** */


    /**
     * True if the player is currently teleporting.
     */
    public boolean isTeleporting()
    {
        return teleporting;
    }

    public void setTeleporting(boolean teleporting)
    {
        this.teleporting = teleporting;
    }

    /**
     * True if the player can change his gamemode.
     * <p>
     * Used to bypass the gamemode lock when using the no-clip mode.
     */
    public boolean isGamemodeChangeAllowed()
    {
        return gamemodeChangeAllowed;
    }

    public void setGamemodeChangeAllowed(boolean gamemodeChangeAllowed)
    {
        this.gamemodeChangeAllowed = gamemodeChangeAllowed;
    }

	/**
	 * Changes the gamemode of the player, bypassing the locked mode.
	 * @param mode The new gamemode.
	 */
	public void setGameMode(GameMode mode)
	{
		Player player = getPlayer();
		if (player != null)
		{
			setGamemodeChangeAllowed(true);
			player.setGameMode(mode);
			setGamemodeChangeAllowed(false);
		}
	}

    /**
     * The arena the spectator is in.
     */
    public Arena getArena()
    {
        return arena;
    }

    public void setArena(Arena arena)
    {
    	setArena(arena, false);
    }
    public void setArena(Arena arena, boolean silent)
    {
        this.arena = arena;

	    if (arena == null)
	    {
		    if(!silent) SpectatorPlus.get().sendMessage(this, "You were removed from your arena.");
		    SpectatorPlus.get().getSpectatorsManager().teleportToLobby(this);
	    }

	    else
	    {
		    if(!silent) SpectatorPlus.get().sendMessage(this, "You are now in the " + arena.getName() + " arena.");

		    if (arena.getLobby() != null)
		    {
			    Player player = getPlayer();
			    if (player != null && player.isOnline())
				    player.teleport(arena.getLobby());
		    }
	    }
    }

	public ArenaSetup getArenaSetup()
	{
		return arenaSetup;
	}

	public void setArenaSetup(ArenaSetup arenaSetup)
	{
		this.arenaSetup = arenaSetup;
	}


	/**
     * The last death message for this player, with his name replaced by "You",
     * and "Name was" replaced by "You were".
     * <p>
     * Example: « You starved to death ».
     * <p>
     * Null if the player was never dead.
     */
    public String getLastDeathMessage()
    {
        return lastDeathMessage;
    }

    public void setLastDeathMessage(String lastDeathMessage)
    {
        this.lastDeathMessage = lastDeathMessage;
    }

    /**
     * The location of the last death.
     *
     * Null if no death registered.
     */
    public Location getDeathLocation()
    {
        return deathLocation;
    }

    public void setDeathLocation(Location deathLocation)
    {
        this.deathLocation = deathLocation;
    }

	/**
     * Whether the player is hidden from the spectator teleportation GUI.
     */
    public boolean isHiddenFromTp()
    {
	    return hiddenFromTp;
    }

    public void setHiddenFromTp(boolean hideFromTp)
    {
        this.hiddenFromTp = hideFromTp;
    }

    /**
     * Whether the player was spectating before they changed worlds.
     */
    public boolean wasSpectatorBeforeWorldChanged()
    {
        return wasSpectatorBeforeWorldChanged;
    }

    public void setWasSpectatorBeforeWorldChanged(boolean wasSpectatorBeforeWorldChanged)
    {
        this.wasSpectatorBeforeWorldChanged = wasSpectatorBeforeWorldChanged;
    }
}
