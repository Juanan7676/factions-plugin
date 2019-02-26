package com.juanan76.factions.common.menu;

import org.bukkit.inventory.ItemStack;

public abstract class MenuItem {
	
	protected Menu menu;
	protected int slot;
	
	protected MenuItem(Menu m)
	{
		this.menu = m;
	}
	
	public int getSlot() {
		return this.slot;
	}
	
	/**
	 * What to do when the user clicks this item.
	 * @return true if we need to keep in the menu, false if we need to close it.
	 */
	public abstract boolean handleClick();
	
	public abstract ItemStack getItem();
}
