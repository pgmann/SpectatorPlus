/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.pgcraft.spectatorplus.guis;

import fr.zcraft.zlib.components.gui.ActionGui;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


public class InventoryGUI extends ActionGui
{
	private Inventory displayedInventory;

	public InventoryGUI(Inventory displayedInventory)
	{
		this.displayedInventory = displayedInventory;
	}


	@Override
	protected void onUpdate()
	{
		setSize(displayedInventory.getSize());
		setTitle(displayedInventory.getTitle().startsWith("container.") ? displayedInventory.getType().getDefaultTitle() : displayedInventory.getTitle());

		int slot = 0;
		for (ItemStack stack : displayedInventory)
		{
			action("", slot++, stack);
		}
	}
}
