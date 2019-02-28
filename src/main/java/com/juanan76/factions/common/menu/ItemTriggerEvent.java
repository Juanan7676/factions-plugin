package com.juanan76.factions.common.menu;

import org.bukkit.inventory.ItemStack;

public class ItemTriggerEvent extends MenuItem {
	
	private String id;
	private Object[] args;
	
	public ItemTriggerEvent(Menu m, ItemStack repr, String id, Object... args) {
		super(m, repr);
		this.args = args;
	}

	@Override
	public boolean handleClick() {
		super.menu.onEvent(this.id,this.args);
		return false;
	}
	
}
