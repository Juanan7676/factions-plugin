package com.juanan76.factions.pvp;

import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.juanan76.factions.Main;
import com.juanan76.factions.common.FPlayer;
import com.juanan76.factions.common.PluginPart;
import com.juanan76.factions.common.Util;
import com.juanan76.factions.factions.Faction;

public class PvpListeners implements Listener {
	
	@EventHandler
	public void onTeleport(PlayerTeleportEvent e)
	{
		if (e.getCause() == TeleportCause.ENDER_PEARL)
		{ // Prevent player from logging out
			Main.pvp.get(e.getPlayer()).engageFight();
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e)
	{
		if (e.getDamager().getType() == EntityType.PLAYER && e.getEntity().getType() == EntityType.PLAYER)
		{ // Prevent player from logging out
			if (e.getEntity().getLocation().distanceSquared(new Location(e.getEntity().getLocation().getWorld(),0,0,0)) <= 40000 && Util.convertWorld(e.getEntity().getLocation().getWorld())==0)
				e.setCancelled(true);
			else
			{
				Main.pvp.get((Player)e.getEntity()).engageFight();
				Main.pvp.get((Player)e.getDamager()).engageFight();
			}
		}
		else if (e.getEntity().getType() == EntityType.ITEM_FRAME && e.getEntity().getLocation().distanceSquared(new Location(e.getEntity().getLocation().getWorld(),0,0,0)) <= 40000 && Util.convertWorld(e.getEntity().getLocation().getWorld())==0)
			e.setCancelled(true);
	}
	
	@EventHandler
	public void onLogout(PlayerQuitEvent e)
	{
		if (Main.pvp.get(e.getPlayer()).onFight)
		{
			PlayerInventory i = e.getPlayer().getInventory();
			for (ItemStack it : i.getStorageContents())
			{
				if (it != null)
					i.getLocation().getWorld().dropItemNaturally(e.getPlayer().getLocation(), it);
			}
			i.clear();
			Bukkit.getServer().broadcastMessage(ChatColor.DARK_PURPLE + "Player " + ChatColor.YELLOW + e.getPlayer().getDisplayName() + ChatColor.DARK_PURPLE + " logged out while in combat and dropped his whole inventory!");
		}
		if (Main.pvpUpdaters.containsKey(e.getPlayer()))
		{
			Bukkit.getScheduler().cancelTask(Main.pvpUpdaters.get(e.getPlayer()));
			Main.pvpUpdaters.remove(e.getPlayer());
		}
		Main.players.remove(e.getPlayer());
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e) throws SQLException
	{
		if (e.getEntity().getKiller() != null)
		{
			Player killer = e.getEntity().getKiller();
			Player killed = (Player)e.getEntity();
			if (Main.players.get(killer).getFactionObject().getID()!=-1)
			{
				Faction kFaction = Main.players.get(killed).getFactionObject();
				Faction wFaction = Main.players.get(killer).getFactionObject();
				if (kFaction.getID()==wFaction.getID())
					return;
				if (kFaction.getID() != -1)
				{
					long respectGained = (long)Math.ceil(kFaction.getRespect()*0.2);
					
					kFaction.addRespect(-respectGained);
					wFaction.addRespect(respectGained);
					
					killer.sendTitle("Killed: ["+Main.players.get(killed).getFactionObject().getShortName(Main.players.get(killer).getFactionObject())+"]"+ChatColor.WHITE+killed.getName(), ChatColor.GREEN+"+"+respectGained+" respect!", 20, 60, 20);
					for (FPlayer p : Main.players.values())
					{
						if (p.getFaction()==kFaction.getID())
							p.sendMessage(PluginPart.PVP, ChatColor.RED+"Your faction lost "+respectGained+" respect due to a member being slain!");
						else if (p.getFaction()==wFaction.getID())
							p.sendMessage(PluginPart.PVP, ChatColor.GREEN+"Your faction gained "+respectGained+" respect thanks to a player kill!");
					}
				}
				else
				{
					wFaction.addRespect(5);
					killer.sendTitle("Killed: ["+Main.players.get(killed).getFactionObject().getShortName(Main.players.get(killer).getFactionObject())+"]"+ChatColor.WHITE+killed.getName(), ChatColor.GREEN+"+5 respect!", 20, 60, 20);
					for (FPlayer p : Main.players.values())
					{
						if (p.getFaction()==wFaction.getID())
							p.sendMessage(PluginPart.PVP, ChatColor.GREEN+"Your faction gained 5 respect thanks to a player kill!");
					}
				}
			}
		}
	}
}
