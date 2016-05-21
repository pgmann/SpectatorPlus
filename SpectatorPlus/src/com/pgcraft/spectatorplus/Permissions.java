/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
	LIST_SPECTATORS("spectate.admin.list"),
	MANAGE_ARENAS("spectate.admin.arena"),
	CHANGE_CONFIG("spectate.admin.config"),
	RELOAD_PLUGIN("spectate.admin.reload"),

	CHAT_SHOUT("spectate.admin.shout"),
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
