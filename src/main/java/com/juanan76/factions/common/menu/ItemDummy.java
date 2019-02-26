package com.juanan76.factions.common.menu;

import org.bukkit.inventory.ItemStack;

public class ItemDummy extends MenuItem {

	public ItemDummy(Menu m, ItemStack repr) {
		super(m, repr);
	}

	@Override
	public boolean handleClick() {
		return false;
	}

}
