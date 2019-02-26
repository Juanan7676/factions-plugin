package com.juanan76.factions.common.menu;

import org.bukkit.inventory.ItemStack;

public class ItemSwapMenu extends MenuItem {
	
	private Menu toSwap;
	
	public ItemSwapMenu(Menu m, ItemStack repr, Menu swap)
	{
		super(m,repr);
		this.toSwap = swap;
	}
	
	@Override
	public boolean handleClick() {
		super.menu.swapMenu(this.toSwap);
		return false;
	}
	
}
