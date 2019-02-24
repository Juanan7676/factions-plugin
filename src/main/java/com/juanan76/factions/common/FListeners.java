package com.juanan76.factions.common;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
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
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.ItemStack;

import com.juanan76.factions.Main;
import com.juanan76.factions.common.tellraw.ClickableComponent;
import com.juanan76.factions.common.tellraw.TextComponent;
import com.juanan76.factions.factions.Faction;
import com.juanan76.factions.factions.Plot;
import com.juanan76.factions.factions.gens.Generator;
import com.juanan76.factions.npc.NPC;
import com.juanan76.factions.npc.NPCShop;
import com.juanan76.factions.npc.SellingItem;
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
			Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(Main.class), new Runnable() {
				@Override
				public void run() {
					for (FPlayer p : Main.players.values())
						Util.tellRaw(p.getPlayer().getName(), new TextComponent("<["), new ClickableComponent(ChatColor.BOLD+chatterFaction.getShortName(p.getFactionObject())+ChatColor.RESET+"]","white",true,"View this faction's info","/f info "+chatterFaction.getID()), new TextComponent(e.getPlayer().getName()+"> "+e.getMessage()));
				}
				
			}, 1);
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
			if (e.getEntity().getLocation().distanceSquared(new Location(e.getEntity().getWorld(), 0, 0, 0)) <= 40000 && Util.convertWorld(e.getEntity().getWorld())==0 && e.getEntityType() != EntityType.DROPPED_ITEM
					 && e.getEntityType() != EntityType.VILLAGER)
				e.setCancelled(true);
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent e)
	{
		if (!Main.players.get(e.getPlayer()).isLogged())
		{
			e.setCancelled(true);
			return;
		}
		
		if (e.getBlock().getType()==Material.SPAWNER)
		{
			Location l = e.getBlock().getLocation();
			if (Main.gens.containsKey(l))
			{
				try {
					Generator g = Main.gens.get(l);
					int lvl = g.getLvl();
					g.delete();
					Main.gens.remove(l);
					if (e.getPlayer().getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH))
					{ // Drop spawner into world
						l.getWorld().dropItem(l, Generator.getStack(lvl).getItemStack(1));
					}
					e.setExpToDrop(0);
				} catch (SQLException ex)
				{
					ex.printStackTrace();
				}
				
			}
		}
		
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
		if (!Main.players.get(e.getPlayer()).isLogged())
		{
			e.setCancelled(true);
			return;
		}
		
		if (e.getBlock().getType()==Material.COMMAND_BLOCK)
			e.getPlayer().sendMessage(e.getPlayer().getLocation().getWorld().getName());
		
		if (e.getBlock().getType()==Material.SPAWNER)
		{
			ItemStack i = e.getItemInHand();
			if (i.containsEnchantment(Enchantment.ARROW_INFINITE))
			{
				Plot p = new Plot(e.getBlock().getChunk().getX(),e.getBlock().getChunk().getZ(),Util.convertWorld(e.getBlock().getWorld()));
				if (p.getFaction()!=-1)
				{
					if (p.getFaction()==Main.players.get(e.getPlayer()).getFaction())
					{
						try {
						int lvl = i.getEnchantmentLevel(Enchantment.ARROW_INFINITE);
						Generator g = new Generator(Main.players.get(e.getPlayer()).getFactionObject(), e.getBlock().getLocation(), lvl);
						Main.gens.put(e.getBlock().getLocation(), g);
						g.create();
						Main.players.get(e.getPlayer()).sendMessage(PluginPart.FACTIONS, ChatColor.GREEN+"Generator placed.");
						CreatureSpawner sp = (CreatureSpawner)e.getBlock().getState();
						sp.setMinSpawnDelay(80);
						sp.setMaxSpawnDelay(120);
						sp.setSpawnCount(1);
						sp.setSpawnedType(EntityType.THROWN_EXP_BOTTLE);
						sp.update();
						} catch (SQLException ex)
						{
							ex.printStackTrace();
						}
					}
					else
					{
						Main.players.get(e.getPlayer()).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You can't place respect generators on a territory claimed by another faction!");
						e.setCancelled(true);
						return;
					}
				}
				else
				{
					Main.players.get(e.getPlayer()).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You can't place respect generators on wilderness!");
					e.setCancelled(true);
					return;
				}
			}
		}
		
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
	public void onLoad(ServerLoadEvent e)
	{
		// Load server NPCs
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(Main.class), new Runnable() {
			@Override
			public void run() {
				NPC shopGeneradores = new NPCShop(ChatColor.DARK_RED+ChatColor.BOLD.toString()+"Faction shop", new Location(Bukkit.getWorld("world"),32,66,13),
					Arrays.asList(new SellingItem[] { Generator.getStack(1),Generator.getStack(2),Generator.getStack(3),Generator.getStack(4),Generator.getStack(5),Generator.getStack(6),Generator.getStack(7),Generator.getStack(8),Generator.getStack(9),Generator.getStack(10) }));
				Main.spawnShops.add(shopGeneradores);
				Bukkit.getPluginManager().registerEvents(shopGeneradores, Main.getPlugin(Main.class));
				shopGeneradores.spawnEntity();
			}
		},1);
	}
	
	@EventHandler
	public void onSpawner(SpawnerSpawnEvent e)
	{
		if (Main.gens.containsKey(e.getSpawner().getLocation()))
		{
			CreatureSpawner sp = e.getSpawner();
			sp.setDelay(-1);
			sp.update();
			Main.gens.get(e.getSpawner().getLocation()).update();
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBucket(PlayerBucketEmptyEvent e)
	{
		if (Math.abs(e.getBlockClicked().getLocation().getX()) <= 100 && Math.abs(e.getBlockClicked().getLocation().getZ()) <= 100 && Util.convertWorld(e.getBlockClicked().getWorld())==0) // Trying to break spawn
			e.setCancelled(true);
	}
	
	@EventHandler
	public void onBucket(PlayerBucketFillEvent e)
	{
		if (Math.abs(e.getBlockClicked().getLocation().getX()) <= 100 && Math.abs(e.getBlockClicked().getLocation().getZ()) <= 100 && Util.convertWorld(e.getBlockClicked().getWorld())==0) // Trying to break spawn
			e.setCancelled(true);
	}
}
