package com.juanan76.factions.common.menu;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import com.juanan76.factions.Main;
import com.juanan76.factions.common.FPlayer;

public abstract class Menu implements Listener {
	
	private int size;
	protected FPlayer viewer;
	protected Inventory view;
	protected boolean closed;
	protected String title;
	
	protected Map<Integer,MenuItem> contents;
	
	public Menu(FPlayer viewer, int size)
	{
		this.contents = new HashMap<Integer,MenuItem>();
		this.viewer = viewer;
		this.size = size;
		this.closed = false;
		Bukkit.getPluginManager().registerEvents(this, Main.getPlugin(Main.class));
	}
	
	public abstract void initContents();
	
	public void onSwap(Menu m) { };
	public void onClose() { };
	
	protected void onEvent(String id, Object... args) { };
	
	@EventHandler
	public void onClick(InventoryClickEvent e)
	{
		if (Main.players.get(e.getWhoClicked()).isLogged() && e.getInventory().equals(this.view))
		{
			e.setCancelled(true);
			if (e.getClickedInventory().equals(this.view))
			{
				int slot = e.getView().convertSlot(e.getRawSlot());
				boolean flag = false;
				if (this.contents.containsKey(slot))
					flag = this.contents.get(slot).handleClick();
			
				if (flag)
				{
					this.viewer.closeMenu();
					this.closed = true;
					HandlerList.unregisterAll(this);
					this.onClose();
				}
			}
		}
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent e)
	{
		if (Main.players.containsKey(e.getPlayer()))
			if (Main.players.get(e.getPlayer()).isLogged() && e.getInventory().equals(view) && Main.players.get(e.getPlayer()).getMenu()!=null)
			{
				if (!this.closed)
				{
					this.closed = true;
					HandlerList.unregisterAll(this);
					this.viewer.closeMenu();
					this.onClose();
				}
			}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e)
	{
		if (e.getPlayer().equals(this.viewer.getPlayer()))
		{
			if (!this.closed)
			{
				this.closed = true;
				HandlerList.unregisterAll(this);
				this.viewer.closeMenu();
				this.onClose();
			}
		}
	}
	
	public void composeInv()
	{
		if (this.view == null)
		{
			if (this.title == null)
				this.view = Bukkit.createInventory(null, size);
			else
				this.view = Bukkit.createInventory(null, size,this.title);
		}
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
		HandlerList.unregisterAll(this);
		this.onSwap(another);
	}

}
