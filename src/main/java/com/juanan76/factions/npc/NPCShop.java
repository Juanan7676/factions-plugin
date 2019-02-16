package com.juanan76.factions.npc;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.juanan76.factions.common.FPlayer;

public class NPCShop extends NPC {
	
	private String name;
	private Location l;
	private Map<FPlayer,Inventory> interactions;
	private Map<ItemStack,Integer> stock;
	
	@Override
	public void interact(FPlayer p) {
		Inventory i = Bukkit.createInventory(p.getPlayer(), 27);
		
		
		
		p.getPlayer().openInventory(i);
		this.interactions.put(p, i);
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
