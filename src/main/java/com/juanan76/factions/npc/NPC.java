package com.juanan76.factions.npc;

import java.sql.SQLException;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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
	
	protected String uuid;
	protected Set<FPlayer> interacters;
	
	public abstract void interact(FPlayer p);
	public abstract boolean isMultiple();
	
	public abstract Location getLoc();
	public abstract String getName();
	public abstract EntityType getType();

	public abstract boolean isInvulnerable();
	public abstract boolean isUnpusheable();
	
	public abstract void save(String npc_name) throws SQLException;
	
	protected void stopInteraction(FPlayer p)
	{
		this.interacters.remove(p);
	}
	
	protected void handleClick(InventoryClickEvent e)
	{
		return;
	}
	
	protected abstract void handleClose(FPlayer p);
	
	@EventHandler
	public void onInteract(PlayerInteractAtEntityEvent e)
	{
		if (!Main.players.get(e.getPlayer()).isLogged())
			e.setCancelled(true);
		else if (e.getRightClicked().getUniqueId().toString().equals(this.uuid))
		{
			if (!this.isMultiple() && this.interacters.size()>0)
				e.setCancelled(true);
			else if (!this.interacters.contains(Main.players.get(e.getPlayer())))
			{
				e.getPlayer().closeInventory();
				this.interact(Main.players.get(e.getPlayer()));
				this.interacters.add(Main.players.get(e.getPlayer()));
				final NPC self = this;
				Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(Main.class), new Runnable() {
					public void run() {
						self.handleClose(Main.players.get(e.getPlayer()));
					}
				}, 30*20);
				e.setCancelled(true);
			}
		}
	}
	
	public void spawnEntity()
	{
		this.getLoc().setYaw(270);
		World w = this.getLoc().getWorld();
		LivingEntity e = (LivingEntity) w.spawnEntity(this.getLoc(), this.getType());
		this.uuid = e.getUniqueId().toString();
		e.setAI(false);
		e.setSilent(true);
		e.setInvulnerable(true);
		e.setCollidable(false);
		e.setCustomNameVisible(true);
		e.setCustomName(this.getName());
		e.teleport(this.getLoc());
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
		this.handleClose(Main.players.get(e.getPlayer()));
	}
}
