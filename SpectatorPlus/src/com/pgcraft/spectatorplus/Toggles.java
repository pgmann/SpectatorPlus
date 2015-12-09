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

import fr.zcraft.zlib.components.configuration.ConfigurationItem;
import org.bukkit.Material;

import java.util.Collections;
import java.util.List;


public class Toggles
{
	/* Miscellaneous toggles */

	public static final ConfigurationItem<Boolean> OUTPUT_MESSAGES = ConfigurationItem.item("outputMessages", true, "outputmessages");
	public static final ConfigurationItem<Boolean> VANILLA_SPECTATOR_MODE = ConfigurationItem.item("spectators.useVanillaMode", false);

	public static final ConfigurationItem<Boolean> SKRIPT_INTEGRATION = ConfigurationItem.item("skriptIntegration", false);

	public static final ConfigurationItem<Boolean> SPECTATOR_MODE_ON_DEATH = ConfigurationItem.item("spectatorModeOnDeath", false, "deathspec");
	public static final ConfigurationItem<Boolean> ENFORCE_ARENA_BOUNDARIES = ConfigurationItem.item("enforceArenaBoundaries", false, "enforceArenaBoundary");


	/* Spectators lobby */

	public static final ConfigurationItem<Boolean> ONSPECMODECHANGED_TELEPORTATION_TOSPAWN = ConfigurationItem.item("onSpectatorModeChanged.teleportation.toSpawnWithoutLobby", false, "teleportToSpawnOnSpecChangeWithoutLobby");
	public static final ConfigurationItem<Boolean> ONSPECMODECHANGED_TELEPORTATION_WITHSPAWNCMD = ConfigurationItem.item("onSpectatorModeChanged.teleportation.usingSpawnCommand", false, "useSpawnCommandToTeleport");


	/* Scoreboards-related */

	public static final ConfigurationItem<Boolean> SPECTATORS_TABLIST_PREFIX = ConfigurationItem.item("spectators.tabListPrefix", false, "colouredtablist");
	public static final ConfigurationItem<Boolean> SPECTATORS_SEE_OTHERS = ConfigurationItem.item("spectators.spectatorsSeeSpectators", false, "seespecs");


	/* Spectators tools */

	public static final ConfigurationItem<Boolean>  TOOLS_NEWBIES_MODE = ConfigurationItem.item("tools.newbieMode", true, "newbieMode");

	public static final ConfigurationItem<Boolean>  TOOLS_TELEPORTER_ENABLED = ConfigurationItem.item("tools.teleporter.enabled", true, "compass");
	public static final ConfigurationItem<Material> TOOLS_TELEPORTER_ITEM = ConfigurationItem.item("tools.teleporter.item", Material.COMPASS, "compassItem");
	public static final ConfigurationItem<Boolean>  TOOLS_TELEPORTER_INSPECTOR = ConfigurationItem.item("tools.teleporter.inspector", true, "inspectPlayerFromTeleportationMenu");
	public static final ConfigurationItem<Boolean>  TOOLS_TELEPORTER_HEALTH = ConfigurationItem.item("tools.teleporter.health", true, "playersHealthInTeleportationMenu");
	public static final ConfigurationItem<Boolean>  TOOLS_TELEPORTER_LOCATION = ConfigurationItem.item("tools.teleporter.location", true, "playersLocationInTeleportationMenu");

	public static final ConfigurationItem<Boolean>  TOOLS_ARENACHOOSER_ENABLED = ConfigurationItem.item("tools.arenaChooser.enabled", true, "arenaclock");
	public static final ConfigurationItem<Material> TOOLS_ARENACHOOSER_ITEM = ConfigurationItem.item("tools.arenaChooser.item", Material.WATCH, "clockItem");

	public static final ConfigurationItem<Boolean>  TOOLS_TOOLS_ENABLED = ConfigurationItem.item("tools.tools.enabled", true, "spectatorsTools");
	public static final ConfigurationItem<Material> TOOLS_TOOLS_ITEM = ConfigurationItem.item("tools.tools.item", Material.MAGMA_CREAM, "spectatorsToolsItem");
	public static final ConfigurationItem<Boolean>  TOOLS_TOOLS_SPEED = ConfigurationItem.item("tools.tools.speed", true);
	public static final ConfigurationItem<Boolean>  TOOLS_TOOLS_NIGHTVISION = ConfigurationItem.item("tools.tools.nightVision", true);
	public static final ConfigurationItem<Boolean>  TOOLS_TOOLS_DIVINGSUIT = ConfigurationItem.item("tools.tools.divingSuit", true);
	public static final ConfigurationItem<Boolean>  TOOLS_TOOLS_NOCLIP = ConfigurationItem.item("tools.tools.noClipMode", true);
	public static final ConfigurationItem<Boolean>  TOOLS_TOOLS_TPTODEATH_ENABLED = ConfigurationItem.item("tools.tools.tpToDeath.enabled", true, "tpToDeathTool");
	public static final ConfigurationItem<Boolean>  TOOLS_TOOLS_TPTODEATH_DISPLAYCAUSE = ConfigurationItem.item("tools.tools.tpToDeath.displayCause", true, "tpToDeathToolShowCause");

	public static final ConfigurationItem<Boolean>  TOOLS_TOOLS_GLOW = ConfigurationItem.item("tools.tools.glowEffectIfActive", true);

	public static final ConfigurationItem<Boolean>  TOOLS_INSPECTOR_ENABLED = ConfigurationItem.item("tools.inspector.enabled", false, "inspector");
	public static final ConfigurationItem<Material> TOOLS_INSPECTOR_ITEM = ConfigurationItem.item("tools.inspector.item", Material.FEATHER, "inspectorItem");


	/* Spectators chat */

	public static final ConfigurationItem<Boolean> CHAT_ENABLED = ConfigurationItem.item("chat.spectatorChat", true, "specchat");
	public static final ConfigurationItem<Boolean> CHAT_BLOCKCOMMANDS_ENABLED = ConfigurationItem.item("chat.blockCommands.enabled", true, "blockcmds");
	public static final ConfigurationItem<Boolean> CHAT_BLOCKCOMMANDS_ADMINBYPASS = ConfigurationItem.item("chat.blockCommands.adminBypass", true, "adminbypass");

	public static final ConfigurationItem<List<String>> CHAT_BLOCKCOMMANDS_WHITELIST = ConfigurationItem.item("chat.blockCommands.whitelist", Collections.<String>emptyList());
}
