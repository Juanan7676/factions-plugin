package com.juanan76.factions.npc;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import com.juanan76.factions.Main;
import com.juanan76.factions.common.FPlayer;

public abstract class NPC implements Listener {
	
	protected LivingEntity npc;
	protected Set<FPlayer> interacters;
	
	public abstract void interact(FPlayer p);
	public abstract boolean isMultiple();
	
	public abstract Location getLoc();
	public abstract String getName();
	public abstract EntityType getType();

	public abstract boolean isInvulnerable();
	public abstract boolean isUnpusheable();
	
	protected void stopInteraction(FPlayer p)
	{
		this.interacters.remove(p);
	}
	
	protected void handleClick(InventoryClickEvent e)
	{
		return;
	}
	
	protected void handleClose(InventoryCloseEvent e)
	{
		return;
	}
	
	@EventHandler
	public void onInteract(PlayerInteractAtEntityEvent e)
	{
		if (!Main.players.get(e.getPlayer()).isLogged())
			e.setCancelled(true);
		else if (e.getRightClicked().getUniqueId().equals(this.npc.getUniqueId()))
		{
			if (!this.isMultiple() && this.interacters.size()>0)
				e.setCancelled(true);
			else if (!this.interacters.contains(Main.players.get(e.getPlayer())))
			{
				e.getPlayer().closeInventory();
				this.interact(Main.players.get(e.getPlayer()));
				this.interacters.add(Main.players.get(e.getPlayer()));
				e.setCancelled(true);
			}
		}
	}
	
	public void spawnEntity()
	{
		World w = this.getLoc().getWorld();
		boolean flag = true;
		for (Entity e : w.getEntities())
		{
			if (e.getLocation().distanceSquared(this.getLoc()) <= 0.5)
			{
				flag = false;
				this.npc = (LivingEntity)e;
			}
		}
		if (flag)
		{
			this.npc = (LivingEntity) w.spawnEntity(this.getLoc(), this.getType());
			this.npc.setAI(false);
			this.npc.setSilent(true);
			this.npc.setInvulnerable(true);
			this.npc.setCollidable(false);
			this.npc.setCustomNameVisible(true);
			this.npc.setCustomName(this.getName());
			this.npc.teleport(this.getLoc());
		}
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent e)
	{
		if (!Main.players.get(e.getWhoClicked()).isLogged())
			e.setCancelled(true);
		else this.handleClick(e);
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent e)
	{
		this.handleClose(e);
	}
}
