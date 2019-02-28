package com.juanan76.factions.common.menu;

import org.bukkit.inventory.ItemStack;

public class ItemRunFunction extends MenuItem {
	
	private String func;
	
	protected ItemRunFunction(Menu m, ItemStack repr, String func) {
		super(m, repr);
	}

	@Override
	public boolean handleClick() {
		return false;
	}
	
}
