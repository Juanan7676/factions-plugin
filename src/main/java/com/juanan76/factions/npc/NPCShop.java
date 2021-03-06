package com.juanan76.factions.npc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import com.juanan76.factions.Main;
import com.juanan76.factions.common.FPlayer;

public class NPCShop extends NPC {
	
	private String name;
	private Location l;
	private Map<FPlayer,Inventory> interactions;
	private List<SellingItem> stock;
	
	public NPCShop(String name, Location l, List<SellingItem> stock)
	{
		super.interacters = new HashSet<FPlayer>();
		this.name = name;
		this.l = l;
		this.interactions = new HashMap<FPlayer,Inventory>();
		this.stock = stock;
	}
	
	@Override
	public void interact(FPlayer p) {
		Inventory i = Bukkit.createInventory(p.getPlayer(), 27);
		
		int c = 0;
		for (SellingItem si : stock)
			i.setItem(c += 2, si.getShopItem());
		
		p.openShop(i);
		this.interactions.put(p, i);
	}
	
	@Override
	public void handleClick(InventoryClickEvent e)
	{
		if (this.interactions.containsKey(Main.players.get(e.getWhoClicked())))
		{
			if (this.interactions.get(Main.players.get(e.getWhoClicked())).equals(e.getInventory()))
			{
				final FPlayer purchaser = Main.players.get(e.getWhoClicked());
				e.setCancelled(true);
				if ((e.getSlot()/2-1) >= stock.size() || (e.getSlot()/2-1) < 0 || e.getSlot()%2!=0)
					return;
				SellingItem purchase = stock.get(e.getSlot()/2-1);
				boolean result = purchaser.purchaseItem(purchase, 1);
				if (!result)
				{
					Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(Main.class), new Runnable() {
						@Override
						public void run()
						{
							purchaser.getPlayer().closeInventory();
						}
					}, 1);
				}
			}
		}
	}
	
	@Override
	public void handleClose(InventoryCloseEvent e)
	{
		if (this.interactions.containsKey(Main.players.get(e.getPlayer())) && e.getInventory().equals(this.interactions.get(Main.players.get(e.getPlayer()))))
		{
			this.interactions.remove(Main.players.get(e.getPlayer()));
			super.stopInteraction(Main.players.get(e.getPlayer()));
			Main.players.get(e.getPlayer()).closeShop();
		}
	}

	@Override
	public boolean isMultiple() {
		return true;
	}

	@Override
	public Location getLoc() {
		return this.l;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public EntityType getType() {
		return EntityType.VILLAGER;
	}

	@Override
	public boolean isInvulnerable() {
		return true;
	}

	@Override
	public boolean isUnpusheable() {
		return true;
	}

}
