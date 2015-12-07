package com.pgcraft.spectatorplus;

import java.util.Collection;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Scoreboard;

public class PlayerObject {	
	private boolean spectating;
	
	private boolean teleporting;
	
	private boolean gamemodeChangeAllowed = false;
	
	private UUID arena;
	
	private ItemStack[] inventory;
	
	private ItemStack[] armour;
	
	private Collection<PotionEffect> effects;
	
	private GameMode oldGameMode;
	
	private String lastDeathMessage = null;
	
	private Location deathLocation = null;
	
	private int setup;
	
	private String arenaName;
	
	private Location pos1;
	
	private Location pos2;
	
	private Scoreboard oldScoreboard;
	
	private boolean hideFromTp;
	
	private boolean wasSpectatorBeforeWorldChanged;
	
	
	protected PlayerObject() {
		setSpectating(false);
		setArenaName(null);
		setTeleporting(false);
		setSetup(0);
		setHideFromTp(false);
	}

    /**
     * True if the player is currently spectating.
     */
    public boolean isSpectating()
    {
        return spectating;
    }

    public void setSpectating(boolean spectating)
    {
        this.spectating = spectating;
    }

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
     * The saved inventory of the player.
     */
    public ItemStack[] getInventory()
    {
        return inventory;
    }

    public void setInventory(ItemStack[] inventory)
    {
        this.inventory = inventory;
    }

    /**
     * The saved armour of the player.
     */
    public ItemStack[] getArmour()
    {
        return armour;
    }

    public void setArmour(ItemStack[] armour)
    {
        this.armour = armour;
    }

    /**
     * The saved potion effects of the player.
     */
    public Collection<PotionEffect> getEffects()
    {
        return effects;
    }

    public void setEffects(Collection<PotionEffect> effects)
    {
        this.effects = effects;
    }

    /**
     * The saved gamemode of the player.
     */
    public GameMode getOldGameMode()
    {
        return oldGameMode;
    }

    public void setOldGameMode(GameMode oldGameMode)
    {
        this.oldGameMode = oldGameMode;
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
     * The original scoreboard the player had set before spectate mode was enabled (to restore after)
     */
    public Scoreboard getOldScoreboard()
    {
        return oldScoreboard;
    }

    public void setOldScoreboard(Scoreboard oldScoreboard)
    {
        this.oldScoreboard = oldScoreboard;
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
    public boolean isWasSpectatorBeforeWorldChanged()
    {
        return wasSpectatorBeforeWorldChanged;
    }

    public void setWasSpectatorBeforeWorldChanged(boolean wasSpectatorBeforeWorldChanged)
    {
        this.wasSpectatorBeforeWorldChanged = wasSpectatorBeforeWorldChanged;
    }
}
