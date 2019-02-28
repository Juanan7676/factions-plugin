package com.juanan76.factions.common.menu;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import com.juanan76.factions.Main;
import com.juanan76.factions.common.FPlayer;

public abstract class Menu implements Listener {
	
	private int size;
	protected FPlayer viewer;
	private Inventory view;
	private boolean closed;
	
	protected Map<Integer,MenuItem> contents;
	
	public Menu(FPlayer viewer, int size)
	{
		this.viewer = viewer;
		this.size = size;
		this.closed = false;
	}
	
	public abstract void initContents();
	
	public void onSwap(Menu m) { };
	public void onClose() { };
	
	@EventHandler
	public void onClick(InventoryClickEvent e)
	{
		if (Main.players.get(e.getWhoClicked()).isLogged() && e.getInventory().equals(view))
		{
			int slot = e.getSlot();
			e.setCancelled(true);
			boolean flag = false;
			if (this.contents.containsKey(slot))
				this.contents.get(slot).handleClick();
			
			if (flag)
			{
				this.viewer.closeMenu();
				this.closed = true;
				this.onClose();
			}
		}
	}
	
	public void composeInv()
	{
		if (this.view == null)
			this.view = Bukkit.createInventory(viewer.getPlayer(), size);
		
		this.view.clear();
		for (int s : this.contents.keySet())
			this.view.setItem(s,this.contents.get(s).getItem());
	}
	
	public Inventory getInv()
	{
		return this.view;
	}
	
	public boolean isClosed()
	{
		return this.closed;
	}
	
	public void swapMenu(Menu another)
	{
		this.viewer.openMenu(another);
		this.closed = true;
		this.onSwap(another);
	}

}