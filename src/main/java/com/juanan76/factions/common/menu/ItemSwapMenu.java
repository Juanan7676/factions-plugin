package com.juanan76.factions.common.menu;

import java.util.List;

import org.bukkit.inventory.ItemStack;

public class ItemSwapMenu extends MenuItem {
	
	private List<MenuItem> toSwap;
	
	public ItemSwapMenu(Menu m, List<MenuItem> swap)
	{
		super(m);
		this.toSwap = swap;
	}
	
	@Override
	public boolean handleClick() {
		super.menu.contents = toSwap;
		return false;
	}

	@Override
	public ItemStack getItem() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
