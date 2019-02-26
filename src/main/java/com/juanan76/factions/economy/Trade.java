package com.juanan76.factions.economy;

import java.util.List;
import java.util.Vector;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.juanan76.factions.common.FPlayer;

public class Trade {
	private FPlayer p1;
	private FPlayer p2;
	
	private long money1;
	private long money2;
	
	private List<ItemStack> items1;
	private List<ItemStack> items2;
	
	private boolean ready1;
	private boolean ready2;
	
	private Inventory inv1;
	private Inventory inv2;
	
	public Trade(FPlayer p1, FPlayer p2)
	{
		this.p1 = p1;
		this.p2 = p2;
		this.money1=0;
		this.money2=0;
		this.items1 = new Vector<ItemStack>();
		this.items2 = new Vector<ItemStack>();
		this.ready1 = false;
		this.ready2 = false;
		this.inv1 = Bukkit.createInventory(p1.getPlayer(), 54);
		this.inv2 = Bukkit.createInventory(p2.getPlayer(), 54);
		p1.getPlayer().openInventory(this.inv1);
		p2.getPlayer().openInventory(this.inv2);
	}
	
	public List<ItemStack> getItemsTraded(FPlayer p)
	{
		if (p1.getID()==p.getID())
			return this.items1;
		else if(p2.getID()==p.getID())
			return this.items2;
		else
			return null;
	}
	
	public long getMoneyTraded(FPlayer p)
	{
		if (p1.getID()==p.getID())
			return this.money1;
		else if(p2.getID()==p.getID())
			return this.money2;
		else
			return 0;
	}
	
	public boolean isReady(FPlayer p)
	{
		if (p1.getID()==p.getID())
			return this.ready1;
		else if(p2.getID()==p.getID())
			return this.ready2;
		else
			return false;
	}
}
