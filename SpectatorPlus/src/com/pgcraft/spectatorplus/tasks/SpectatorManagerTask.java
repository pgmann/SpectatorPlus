package com.pgcraft.spectatorplus.tasks;

import com.pgcraft.spectatorplus.SpectatorPlus;
import com.pgcraft.spectatorplus.Toggles;
import com.pgcraft.spectatorplus.arenas.Arena;
import com.pgcraft.spectatorplus.spectators.Spectator;
import com.pgcraft.spectatorplus.spectators.SpectatorMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;


/**
 * Handle players trying to leave the arena in arena mode.
 *
 * Players outside the arena borders will have the move event cancelled; players more than x blocks
 * away (set in config) from the global lobby will also have their move event cancelled.
 *
 * This also ensures spectators are always able to fly.
 */
public class SpectatorManagerTask implements Runnable
{
	SpectatorPlus p;

	public SpectatorManagerTask()
	{
		p = SpectatorPlus.get();
	}

	@Override
	public void run()
	{
		final Boolean ARENA_MODE = p.getSpectatorsManager().getSpectatorsMode() == SpectatorMode.ARENA;
		final Boolean ENFORCE_ARENA_BOUNDARIES = Toggles.ENFORCE_ARENA_BOUNDARIES.get();
		final Boolean ENFORCE_LOBBY_BOUNDARIES = Toggles.ENFORCE_LOBBY_BOUNDARIES.get() > 0d;
		final Double MAX_LOBBY_DISTANCE_SQUARED = Math.pow(Toggles.ENFORCE_LOBBY_BOUNDARIES.get(), 2d);

		for (Player target : p.getServer().getOnlinePlayers())
		{
			final Spectator spectator = p.getPlayerData(target);

			if (spectator.isSpectating())
			{
				// Spectators should always be able to fly.
				// to help prevent glitches when enabling/disabling flight, only set allow flight when it's not already on.
				if (!target.getAllowFlight()) target.setAllowFlight(true);

				// In arena mode, if boundaries are enforced, check if spectators are not inside the boundary.
				// [Spawn allows free movement (before choosing an arena).]
				if (ARENA_MODE && ENFORCE_ARENA_BOUNDARIES)
				{
					final Arena arena = spectator.getArena();
					Boolean outOfBounds = false;

					if (arena != null)
					{
						// ignore players not in an arena.
						if (!arena.isEnabled() || !arena.isRegistered() || arena.getCorner1() == null || arena.getCorner2() == null)
						{
							spectator.setArena(null);
							p.getSpectatorsManager().teleportToLobby(spectator);

							p.sendMessage(target, "The arena you were in was removed.");

							outOfBounds = false;
						}
						else
						{
							outOfBounds = !arena.isInside(target.getLocation());
						}
					}
					else
					{
						if (ENFORCE_LOBBY_BOUNDARIES)
						{
							final Location lobby = p.getSpectatorsManager().getSpectatorsLobby();
							outOfBounds = !lobby.getWorld().equals(target.getWorld()) || lobby.distanceSquared(target.getLocation()) > MAX_LOBBY_DISTANCE_SQUARED;
						}
					}

					if (outOfBounds)
					{
						p.sendMessage(target, "Stay inside the arena!");
						target.teleport(getValidPos(target.getLocation(), arena));
					}
				}
			}
		}
	}

	private Location getValidPos(Location target, Arena arena)
	{
		final Location lobby = p.getSpectatorsManager().getSpectatorsLobby();

		Location safePos = new Location(
				arena != null ? arena.getCorner1().getWorld() : lobby.getWorld(),
				target.getX(),
				target.getY(),
				target.getZ()
		);

		Location lowestCorner  = arena != null ? arena.getCorner1() : lobby.clone().add(-5, -5, -5);
		Location highestCorner = arena != null ? arena.getCorner2() : lobby.clone().add(5, 5, 5);

		if (target.getX() > highestCorner.getX()) safePos.setX(highestCorner.getX() - 0.01);
		if (target.getX() < lowestCorner.getX()) safePos.setX(lowestCorner.getX() + 0.01);

		if (target.getY() > highestCorner.getY()) safePos.setY(highestCorner.getY() - 0.01);
		if (target.getY() < lowestCorner.getY()) safePos.setY(lowestCorner.getY() + 0.01);

		if (target.getZ() > highestCorner.getZ()) safePos.setZ(highestCorner.getZ() - 0.01);
		if (target.getZ() < lowestCorner.getZ()) safePos.setZ(lowestCorner.getZ() + 0.01);

		// Must have the same pitch and yaw to possibly equal the player's current position. Also, looks better.
		safePos.setDirection(target.getDirection());

		return safePos;
	}
}
