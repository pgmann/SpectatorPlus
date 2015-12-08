package com.pgcraft.spectatorplus.spectators;

import com.pgcraft.spectatorplus.SpectatorPlus;
import com.pgcraft.spectatorplus.utils.SPUtils;
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
    private UUID playerID;

	private boolean spectating = false;
	private boolean teleporting = false;
	private boolean gamemodeChangeAllowed = false;
    private boolean hideFromTp = false;
    private boolean wasSpectatorBeforeWorldChanged = false;

	private UUID arena = null;


	private ItemStack[] oldInventoryContent = null;
	private ItemStack[] oldArmourContent = null;
	private Collection<PotionEffect> oldEffects = null;

	private Scoreboard oldScoreboard = null;

	private GameMode oldGameMode = null;
	private Boolean oldFlyMode = null;
	private Float oldFlySpeed = null;


	private String lastDeathMessage = null;
	private Location deathLocation = null;
	
	private int setup = 0;
	private String arenaName = null;
	private Location pos1 = null;
	private Location pos2 = null;


	public Spectator(UUID id)
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
	    oldScoreboard       = player.getScoreboard() != null ? player.getScoreboard() : Bukkit.getScoreboardManager().getMainScoreboard();


	    // We clear the player
	    player.getInventory().clear();
	    player.getInventory().setArmorContents(null);

	    for (PotionEffect effect : player.getActivePotionEffects())
	    {
		    player.removePotionEffect(effect.getType());
	    }

	    // We add spectating capabilities to the player
	    player.setAllowFlight(true);
	    player.setFoodLevel(20);
	    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 15), true);

	    player.setFireTicks(0);

	    // TODO fill spectating inventory

	    // We disable all interactions if possible
	    player.setGameMode(/* FIXME vanillaSpectate */true ? GameMode.ADVENTURE : GameMode.SPECTATOR);
	    SPUtils.setCollidesWithEntities(player, false);

	    // We hide this player if seeSpecs mode is off and the target isn't spectating
	    for (Player other : Bukkit.getOnlinePlayers())
	    {
			if (!other.equals(player))
			{
				if (/* FIXME seeSpecs */false && SpectatorPlus.get().getPlayerData(other).isSpectating())
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
	    if (/* FIXME scoreboard enabled */false)
	    {
		    SpectatorPlus.get().getSpectatorsManager().setSpectatorsScoreboard(this);
		    SpectatorPlus.get().getSpectatorsManager().setSpectatingInScoreboard(this);
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

		SPUtils.setCollidesWithEntities(player, true);

		player.setAllowFlight(oldFlyMode);
		player.setGameMode(oldGameMode);

		player.getInventory().setContents(oldInventoryContent);
		player.getInventory().setArmorContents(oldArmourContent);

		// The arena is reset
		if (!worldChange)
		{
			setArena(null);
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
		if (/* FIXME scoreboard enabled */false)
		{
			SpectatorPlus.get().getSpectatorsManager().setSpectatorsScoreboard(this);
			SpectatorPlus.get().getSpectatorsManager().setSpectatingInScoreboard(this);
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
     * The ID of the current arena the spectator is in.
     */
    public UUID getArena()
    {
        return arena;
    }

    public void setArena(UUID arena)
    {
        this.arena = arena;
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
     * The setup step.
     *  - 0: no setup in progress;
     *  - 1: first corner set;
     *  - 2: second corner set.
     */
    public int getSetup()
    {
        return setup;
    }

    public void setSetup(int setup)
    {
        this.setup = setup;
    }

    /**
     * If the player setup an arena, the entered name of this arena is stored here.
     */
    public String getArenaName()
    {
        return arenaName;
    }

    public void setArenaName(String arenaName)
    {
        this.arenaName = arenaName;
    }

    /**
     * The location of the first corner of an arena (in setup mode).
     */
    public Location getPos1()
    {
        return pos1;
    }

    public void setPos1(Location pos1)
    {
        this.pos1 = pos1;
    }

    /**
     * The location of the second corner of an arena (in setup mode).
     */
    public Location getPos2()
    {
        return pos2;
    }

    public void setPos2(Location pos2)
    {
        this.pos2 = pos2;
    }

    /**
     * Whether the player is hidden from the spectator teleportation GUI.
     */
    public boolean isHideFromTp()
    {
        return hideFromTp;
    }

    public void setHideFromTp(boolean hideFromTp)
    {
        this.hideFromTp = hideFromTp;
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
