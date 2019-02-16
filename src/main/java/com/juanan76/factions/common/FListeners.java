package com.juanan76.factions.common;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.juanan76.factions.Main;
import com.juanan76.factions.common.tellraw.ClickableComponent;
import com.juanan76.factions.common.tellraw.TextComponent;
import com.juanan76.factions.factions.Faction;
import com.juanan76.factions.factions.Plot;
import com.juanan76.factions.pvp.PvpPlayer;
import com.juanan76.factions.pvp.Teleport;

public class FListeners implements Listener {
	@EventHandler
	public void onJoin(PlayerJoinEvent e)
	{
		Player p = e.getPlayer();
		Main.pvp.put(p, new PvpPlayer(p));
		Main.players.put(p, new FPlayer(p));
		try {
			ResultSet rst = DBManager.performQuery("select * from users where nick='"+p.getName()+"'");
			if (!rst.next())
			{
				Main.players.get(p).sendMessage(PluginPart.MAIN, ChatColor.YELLOW + "Welcome to J76-Factions Server! Please, use /register <password> to register into the server.");
			}
			else
			{
				Main.players.get(p).sendMessage(PluginPart.MAIN, ChatColor.YELLOW + "Welcome back! Please, use /login <password> with your password to log in into the server.");
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent e)
	{
		if (!Main.players.get(e.getPlayer()).isLogged()) e.setCancelled(true);
		else
		{
			Main.players.get(e.getPlayer()).move();
			for (Teleport t : Main.teleports.values())
			{
				if (t.getTeleported().getPlayer().equals(e.getPlayer()))
				{
					t.cancel();
				}
			}
		}
	}
	
	@EventHandler
	public void onPickup(EntityPickupItemEvent e)
	{
		if (e.getEntityType() == EntityType.PLAYER)
		{
			Player p = (Player)e.getEntity();
			if (!Main.players.get(p).isLogged()) e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onDrop(EntityDropItemEvent e)
	{
		if (e.getEntityType() == EntityType.PLAYER)
		{
			Player p = (Player)e.getEntity();
			if (!Main.players.get(p).isLogged()) e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e)
	{
		if (!Main.players.get(e.getPlayer()).isLogged()) e.setCancelled(true);
		else
		{
			e.setCancelled(true);
			Faction chatterFaction = Main.players.get(e.getPlayer()).getFactionObject();
			for (FPlayer p : Main.players.values())
				Util.tellRaw(p.getPlayer().getName(), new TextComponent("<["), new ClickableComponent(ChatColor.BOLD+chatterFaction.getShortName(p.getFactionObject())+ChatColor.RESET+"]","white",true,"View this faction's info","/f info "+chatterFaction.getID()), new TextComponent(e.getPlayer().getName()+"> "+e.getMessage()));
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e)
	{
		if (!Main.players.get(e.getPlayer()).isLogged()) e.setCancelled(true);
	}
	
	@EventHandler
	public void onSpawn(EntitySpawnEvent e)
	{
		if (e.getEntityType() != EntityType.PLAYER)
			if (e.getEntity().getLocation().distanceSquared(new Location(e.getEntity().getWorld(), 0, 0, 0)) <= 40000 && Util.convertWorld(e.getEntity().getWorld())==0 && e.getEntityType() != EntityType.DROPPED_ITEM)
				e.setCancelled(true);
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent e)
	{
		if (e.getPlayer().isOp())
			return;
		if (Math.abs(e.getBlock().getLocation().getX()) <= 100 && Math.abs(e.getBlock().getLocation().getZ()) <= 100 && Util.convertWorld(e.getBlock().getWorld())==0) // Trying to break spawn
			e.setCancelled(true);
		else
		{
			Plot p = new Plot(e.getBlock().getChunk().getX(),e.getBlock().getChunk().getZ(),Util.convertWorld(e.getBlock().getWorld()));
			if (p.getFaction()!=-1)
			{
				if (p.getFaction()!=Main.players.get(e.getPlayer()).getFaction())
				{
					Main.players.get(e.getPlayer()).sendMessage(PluginPart.FACTIONS, ChatColor.DARK_RED+"You don't have permission to do that!");
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlace(BlockPlaceEvent e)
	{
		if (e.getBlock().getType()==Material.COMMAND_BLOCK)
			e.getPlayer().sendMessage(e.getPlayer().getLocation().getWorld().getName());
		if (e.getPlayer().isOp())
			return;
		if (Math.abs(e.getBlock().getLocation().getX()) <= 100 && Math.abs(e.getBlock().getLocation().getZ()) <= 100 && Util.convertWorld(e.getBlock().getWorld())==0) // Trying to break spawn
			e.setCancelled(true);
		else
		{
			Plot p = new Plot(e.getBlock().getChunk().getX(),e.getBlock().getChunk().getZ(),Util.convertWorld(e.getBlock().getWorld()));
			if (p.getFaction()!=-1)
			{
				if (p.getFaction()!=Main.players.get(e.getPlayer()).getFaction())
				{
					Main.players.get(e.getPlayer()).sendMessage(PluginPart.FACTIONS, ChatColor.DARK_RED+"You don't have permission to do that!");
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onFire(BlockBurnEvent e)
	{
		if (Math.abs(e.getBlock().getLocation().getX()) <= 100 && Math.abs(e.getBlock().getLocation().getZ()) <= 100 && Util.convertWorld(e.getBlock().getWorld())==0) // Trying to break spawn
			e.setCancelled(true);
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent e)
	{
		FPlayer clicker = Main.players.get(e.getWhoClicked());
		if (!clicker.isLogged())
			e.setCancelled(true);
		else
		{
			if (clicker.getShop()!=null)
			{
				
			}
		}
	}
}
