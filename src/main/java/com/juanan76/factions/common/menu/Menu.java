package com.juanan76.factions.common.menu;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import com.juanan76.factions.Main;
import com.juanan76.factions.common.FPlayer;

public abstract class Menu implements Listener {
	
	private int size;
	private FPlayer viewer;
	private Inventory view;
	
	protected List<MenuItem> contents;
	
	@EventHandler
	public void onClick(InventoryClickEvent e)
	{
		if (Main.players.get(e.getWhoClicked()).isLogged() && e.getInventory().equals(view))
		{
			int slot = e.getSlot();
			e.setCancelled(true);
			boolean flag = false;
			for (MenuItem i : this.contents)
				if(i.getSlot()==slot)
					flag = i.handleClick();
			
			if (flag)
			{
				final Player p = viewer.getPlayer();
				Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(Main.class), new Runnable () {

					@Override
					public void run() {
						p.closeInventory();
					}
					
				},1);
			}
		}
	}
	
	public void composeInv()
	{
		this.view = Bukkit.createInventory(viewer.getPlayer(), size);
		for (MenuItem i : this.contents)
			this.view.setItem(i.getSlot(), i.getItem());
			
	}
	
	public Inventory getInv()
	{
		return this.view;
	}

}
