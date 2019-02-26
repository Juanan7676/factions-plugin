package com.juanan76.factions.common.menu;

import org.bukkit.inventory.ItemStack;

public class ItemExitMenu extends MenuItem {
	
	ItemStack i;
	
	public ItemExitMenu(Menu m, ItemStack repr) {
		super(m,repr);
	}

	@Override
	public boolean handleClick() {
		return true;
	}

}
