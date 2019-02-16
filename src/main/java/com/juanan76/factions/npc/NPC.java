package com.juanan76.factions.npc;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
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
	
	public NPC()
	{
		this.interacters = new HashSet<FPlayer>();
		this.spawnEntity();
	}
	
	protected void stopInteraction(FPlayer p)
	{
		this.interacters.remove(p);
	}
	
	protected void handleClick(InventoryClickEvent e)
	{
		return;
	}
	
	protected void handleClose(FPlayer closer)
	{
		return;
	}
	
	@EventHandler
	public void onInteract(PlayerInteractAtEntityEvent e)
	{
		if (e.getRightClicked().equals(this.npc))
		{
			if (!this.isMultiple() && this.interacters.size()>0)
				e.setCancelled(true);
			else if (!this.interacters.contains(Main.players.get(e.getPlayer())))
			{
				this.interact(Main.players.get(e.getPlayer()));
				this.interacters.add(Main.players.get(e.getPlayer()));
			}
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent e)
	{
		if (this.isInvulnerable() && e.getEntity().equals(this.npc))
			e.setCancelled(true);
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
			this.npc = (LivingEntity)w.spawnEntity(this.getLoc(), this.getType());
			this.npc.setSilent(true);
			this.npc.setInvulnerable(true);
			this.npc.setCollidable(false);
			this.npc.teleport(this.getLoc());
		}
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent e)
	{
		this.handleClick(e);
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent e)
	{
		if (this.interacters.contains(Main.players.get(e.getPlayer())))
			this.interacters.remove(Main.players.get(e.getPlayer()));
		
		this.handleClose(Main.players.get(e.getPlayer()));
		
	}
}
