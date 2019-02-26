package com.juanan76.factions.common.menu;

import org.bukkit.inventory.ItemStack;

public abstract class MenuItem {
	
	protected Menu menu;
	protected ItemStack repr;
	
	protected MenuItem(Menu m, ItemStack repr)
	{
		this.menu = m;
		this.repr = repr;
	}
	
	/**
	 * What to do when the user clicks this item.
	 * @return true if we need to keep in the menu, false if we need to close it.
	 */
	public abstract boolean handleClick();
	
	public ItemStack getItem() {
		return this.repr;
	}
}
