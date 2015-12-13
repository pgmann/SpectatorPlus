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

import org.bukkit.permissions.Permissible;


public enum Permissions
{
	ENABLE_SPECTATOR_MODE("spectate.use.on"),
	DISABLE_SPECTATOR_MODE("spectate.use.off"),
	CHANGE_SPECTATOR_MODE_FOR_OTHERS("spectate.use.others"),

	CHANGE_SPECTATORS_LOBBY("spectate.admin.lobby"),
	CHANGE_SPECTATING_MODE("spectate.admin.mode"),
	MANAGE_ARENAS("spectate.admin.arena"),
	CHANGE_CONFIG("spectate.admin.config"),
	RELOAD_PLUGIN("spectate.admin.reload"),

	BYPASS_COMMANDS_WHITELIST("spectate.admin.bypasswhitelist"),

	HIDE_SELF_FROM_SPECTATORS("spectate.admin.hide.self"),
	HIDE_OTHERS_FROM_SPECTATORS("spectate.admin.hide.others"),
	AUTO_HIDE_FROM_SPECTATORS("spectate.admin.hide.auto"),
	SEE_HIDDEN_PLAYERS("spectate.admin.hide.see"),

	BROADCAST_MESSAGES_TO_SPECTATORS("spectate.admin.say")
	;


	private String permission;

	Permissions(String permission)
	{
		this.permission = permission;
	}

	public String get()
	{
		return permission;
	}

	@Override
	public String toString()
	{
		return permission;
	}

	public boolean grantedTo(Permissible permissible)
	{
		return permissible.hasPermission(permission);
	}
}
