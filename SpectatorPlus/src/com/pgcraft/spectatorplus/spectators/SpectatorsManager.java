/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.pgcraft.spectatorplus.spectators;

import com.pgcraft.spectatorplus.Permissions;
import com.pgcraft.spectatorplus.SpectatorPlus;
import com.pgcraft.spectatorplus.Toggles;
import com.pgcraft.spectatorplus.arenas.Arena;
import com.pgcraft.spectatorplus.guis.ArenasSelectorGUI;
import com.pgcraft.spectatorplus.guis.TeleportationGUI;
import com.pgcraft.spectatorplus.guis.inventories.SpectatorsInventoryManager;
import com.pgcraft.spectatorplus.utils.ConfigAccessor;
import fr.zcraft.quartzlib.components.gui.Gui;
import fr.zcraft.quartzlib.tools.PluginLogger;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SpectatorsManager {
    private static final String SPECTATORS_TEAM_NAME = "spectators";
    private static final String SPECTATORS_HEALTH_OBJECTIVE_NAME = "health";
    private static final String SPECTATORS_TEAM_PREFIX = ChatColor.DARK_GRAY + "SPEC ▏ " + ChatColor.GRAY;
    private final SpectatorPlus p;
    private SpectatorsInventoryManager inventoryManager;
    private SpectatorsChatManager chatManager;
    private ConfigAccessor savedSpectatingPlayers;
    private ConfigAccessor spectatorsSetup;
    private SpectatorMode spectatorsMode;
    private Location spectatorsLobby = null;
    private Scoreboard spectatorsScoreboard;
    private Team spectatorsTeam;

    public SpectatorsManager(SpectatorPlus plugin) {
        p = plugin;

        savedSpectatingPlayers = new ConfigAccessor(p, "specs");
        spectatorsSetup = new ConfigAccessor(p, "setup");

        inventoryManager = new SpectatorsInventoryManager();
        chatManager = new SpectatorsChatManager();

        loadSpectatorsSetup();
        rebuildScoreboard();
    }

    public ConfigAccessor getSavedSpectatingPlayers() {
        return savedSpectatingPlayers;
    }

    public ConfigAccessor getSpectatorsSetup() {
        return spectatorsSetup;
    }

    public SpectatorsInventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public SpectatorsChatManager getChatManager() {
        return chatManager;
    }

    /**
     * Saves into file all things stored in a config file.
     */
    public void save() {
        saveSpectatorsSetup();
        savedSpectatingPlayers.saveConfig();
    }

    /* **  Spectators lobby  ** */
    private void loadSpectatorsSetup() {
        spectatorsSetup.saveDefaultConfig();

        boolean updated = false;

        // Spectating mode
        try {
            spectatorsMode = SpectatorMode.fromString(spectatorsSetup.getConfig().getString("mode", "ANY"));
        } catch (RuntimeException e) {
            spectatorsMode = SpectatorMode.ANY;
            updated = true;
        }

        // Spectators lobby
        spectatorsLobby = null;

        if (spectatorsSetup.getConfig().getBoolean("active", false)) {
            final String worldName = spectatorsSetup.getConfig().getString("world", "null");
            final World lobbyWorld = p.getServer().getWorld(worldName);

            if (lobbyWorld != null) {
                try {
                    double lobbyX = Double.parseDouble(spectatorsSetup.getConfig().getString("xPos"));
                    double lobbyY = Double.parseDouble(spectatorsSetup.getConfig().getString("yPos"));
                    double lobbyZ = Double.parseDouble(spectatorsSetup.getConfig().getString("zPos"));

                    float lobbyPitch = Float.parseFloat(spectatorsSetup.getConfig().getString("pitch"));
                    float lobbyYaw = Float.parseFloat(spectatorsSetup.getConfig().getString("yaw"));

                    // Values check
                    if (lobbyY > lobbyWorld.getMaxHeight()) lobbyY = lobbyWorld.getMaxHeight();
                    else if (lobbyY < 0) lobbyY = 0d;

                    lobbyPitch %= 360;
                    lobbyYaw %= 360;

                    if (lobbyPitch < 0) lobbyPitch += 360;
                    if (lobbyYaw < 0) lobbyYaw += 360;

                    spectatorsLobby = new Location(lobbyWorld, lobbyX, lobbyY, lobbyZ, lobbyPitch, lobbyYaw);
                } catch (NumberFormatException e) {
                    PluginLogger.warning("Invalid spectator lobby stored in setup.yml (invalid coordinates), removing the lobby.");
                }
            } else {
                // Error message only displayed if the world is not the null name
                if (!Objects.equals(worldName, "null"))
                    PluginLogger.warning("Invalid spectator lobby stored in setup.yml (unknown world), removing the lobby.");
            }

            if (spectatorsLobby == null) // If the lobby is still null, the location is invalid and not kept.
                updated = true;
        }


        if (updated)
            saveSpectatorsSetup();
    }

    private void saveSpectatorsSetup() {
        spectatorsSetup.getConfig().set("active", spectatorsLobby != null);

        spectatorsSetup.getConfig().set("xPos", spectatorsLobby != null ? spectatorsLobby.getX() : 0d);
        spectatorsSetup.getConfig().set("yPos", spectatorsLobby != null ? spectatorsLobby.getY() : 0d);
        spectatorsSetup.getConfig().set("zPos", spectatorsLobby != null ? spectatorsLobby.getZ() : 0d);
        spectatorsSetup.getConfig().set("pitch", spectatorsLobby != null ? spectatorsLobby.getPitch() : 0f);
        spectatorsSetup.getConfig().set("yaw", spectatorsLobby != null ? spectatorsLobby.getYaw() : 0f);

        spectatorsSetup.getConfig().set("world", spectatorsLobby != null ? spectatorsLobby.getWorld().getName() : "null");

        spectatorsSetup.getConfig().set("mode", spectatorsMode.toString());

        spectatorsSetup.saveConfig();
    }


    public boolean teleportToLobby(Spectator spectator) {
        Player player = spectator.getPlayer();
        if (player == null) return false;

        if (spectatorsLobby != null) {
            // We need a safe spot
            Location aboveLobby = spectatorsLobby.clone().add(0, 1, 0);
            Location belowLobby = spectatorsLobby.clone().add(0, -1, 0);

            while (spectatorsLobby.getBlock().getType() != Material.AIR || aboveLobby.getBlock().getType() != Material.AIR || belowLobby.getBlock().getType() == Material.AIR || belowLobby.getBlock().getType() == Material.LAVA) {
                spectatorsLobby.add(0, 1, 0);
                aboveLobby.add(0, 1, 0);
                belowLobby.add(0, 1, 0);

                if (spectatorsLobby.getY() > spectatorsLobby.getWorld().getHighestBlockYAt(spectatorsLobby)) {
                    spectatorsLobby.add(0, -2, 0);
                    aboveLobby.add(0, -2, 0);
                    belowLobby.add(0, -2, 0);

                    break;
                }
            }

            spectator.setTeleporting(true);
            player.teleport(spectatorsLobby);
            spectator.setTeleporting(false);

            return true;
        } else if (Toggles.ONSPECMODECHANGED_TELEPORTATION_TOSPAWN.get()) {
            if (Toggles.ONSPECMODECHANGED_TELEPORTATION_WITHSPAWNCMD.get() && Bukkit.getServer().getPluginCommand("spawn") != null) {
                return player.performCommand("spawn");
            } else {
                return player.teleport(player.getWorld().getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            }
        } else return false;
    }

    public SpectatorMode getSpectatorsMode() {
        return spectatorsMode;
    }

    public void setSpectatorsMode(SpectatorMode mode) {
        this.spectatorsMode = mode;
        saveSpectatorsSetup();

        // Needed to add (or remove) the arena selector
        getInventoryManager().equipSpectators();


        // Force-closes the arena selector if the new mode is not the arena one.
        if (mode != SpectatorMode.ARENA)
            Gui.close(ArenasSelectorGUI.class);

            // Force-closes the teleportation GUI if the new mode is the Arena one, as
            // players will have to select an arena.
        else
            Gui.close(TeleportationGUI.class);

        // Updates the teleportation GUIs because the displayed players may change.
        Gui.update(TeleportationGUI.class);
    }

    public Location getSpectatorsLobby() {
        return spectatorsLobby;
    }

    public void setSpectatorsLobby(Location lobby) {
        spectatorsLobby = lobby;
        saveSpectatorsSetup();
    }

    /* **  Spectators scoreboard  ** */

    /**
     * Rebuilds the scoreboard if enabled; removes it else.
     */
    public void rebuildScoreboard() {
        if (Toggles.SPECTATORS_TABLIST_PREFIX.get() || Toggles.SPECTATORS_TABLIST_HEALTH.get()) {
            resetScoreboard();

            spectatorsScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

            if (Toggles.SPECTATORS_TABLIST_HEALTH.get()) {
                spectatorsScoreboard.registerNewObjective(SPECTATORS_HEALTH_OBJECTIVE_NAME, "health", "Health")
                        .setDisplaySlot(DisplaySlot.PLAYER_LIST);
            }

            if (Toggles.SPECTATORS_TABLIST_PREFIX.get()) {
                spectatorsTeam = spectatorsScoreboard.registerNewTeam(SPECTATORS_TEAM_NAME);
                spectatorsTeam.setPrefix(SPECTATORS_TEAM_PREFIX);
                spectatorsTeam.setSuffix(ChatColor.RESET.toString());
            }

            spectatorsTeam.setCanSeeFriendlyInvisibles(Toggles.SPECTATORS_SEE_OTHERS.get());

            for (Player spectator : Bukkit.getOnlinePlayers()) {
                if (SpectatorPlus.get().getPlayerData(spectator).isSpectating()) {
                    spectator.setScoreboard(spectatorsScoreboard);
                    spectatorsTeam.addEntry(spectator.getName());
                }
            }
        } else if (spectatorsScoreboard != null) {
            resetScoreboard();

            for (Player spectator : Bukkit.getOnlinePlayers()) {
                SpectatorPlus.get().getPlayerData(spectator).resetScoreboard();
            }
        }
    }

    private void resetScoreboard() {
        if (spectatorsScoreboard == null)
            return;

        if (spectatorsTeam != null) {
            spectatorsTeam.unregister();
            spectatorsTeam = null;
        }

        final Objective objective = spectatorsScoreboard.getObjective(SPECTATORS_HEALTH_OBJECTIVE_NAME);
        if (objective != null) {
            objective.unregister();
        }

        spectatorsScoreboard = null;
    }

    /**
     * Sets the scoreboard to be used by the spectators.
     */
    public void setSpectatorsScoreboard(Spectator spectator) {
        if (spectatorsScoreboard == null) return;

        Player player = spectator.getPlayer();
        if (player == null) return;

        if (spectator.isSpectating())
            player.setScoreboard(spectatorsScoreboard);
        else
            spectator.resetScoreboard();
    }

    public void setSpectatingInScoreboard(Spectator spectator) {
        if (spectatorsTeam == null) return;

        Player player = spectator.getPlayer();
        if (player == null) return;

        if (spectator.isSpectating())
            spectatorsTeam.addEntry(player.getName());
        else
            spectatorsTeam.removeEntry(player.getName());
    }

    /* **  Spectators queries  ** */

    public List<Spectator> getVisiblePlayersFor(Spectator spectator) {
        Player player = spectator.getPlayer();
        if (player == null) return Collections.emptyList();

        List<Spectator> visiblePlayers = new ArrayList<>();

        for (Player viewedPlayer : Bukkit.getOnlinePlayers()) {
            Spectator viewedSpectator = SpectatorPlus.get().getPlayerData(viewedPlayer);

            if (viewedSpectator.isSpectating() || (viewedSpectator.isHiddenFromTp() && !Permissions.SEE_HIDDEN_PLAYERS.grantedTo(player)))
                continue;

            switch (spectatorsMode) {
                case ANY:
                    visiblePlayers.add(viewedSpectator);
                    break;

                case WORLD:
                    if (viewedPlayer.getWorld().equals(player.getWorld()))
                        visiblePlayers.add(viewedSpectator);

                    break;

                case ARENA:
                    Arena arena = spectator.getArena();
                    if (arena != null && arena.isInside(viewedPlayer.getLocation()))
                        visiblePlayers.add(viewedSpectator);

                    break;
            }
        }

        return visiblePlayers;
    }
}
