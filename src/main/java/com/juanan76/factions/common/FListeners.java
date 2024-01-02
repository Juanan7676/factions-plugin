package com.juanan76.factions.common;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.ItemStack;

import com.juanan76.factions.Main;
import com.juanan76.factions.common.tellraw.ClickableComponent;
import com.juanan76.factions.common.tellraw.TellRawComponent;
import com.juanan76.factions.common.tellraw.TextComponent;
import com.juanan76.factions.economy.TradeRequest;
import com.juanan76.factions.factions.Faction;
import com.juanan76.factions.factions.Plot;
import com.juanan76.factions.factions.Faction.FactionRelation;
import com.juanan76.factions.factions.gens.Generator;
import com.juanan76.factions.npc.NPC;
import com.juanan76.factions.npc.NPCShop;
import com.juanan76.factions.npc.SellingItem;
import com.juanan76.factions.pvp.PvpPlayer;
import com.juanan76.factions.pvp.Teleport;

import net.md_5.bungee.api.ChatMessageType;

public class FListeners implements Listener {
	
	public static final int spawnX = -63;
	public static final int spawnY = 72;
	public static final int spawnZ = 52;
	
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
				ResultSet rst2 = DBManager.performSafeQuery("select timestamp from deaths where usuario=?", "i", rst.getInt("id"));
				if (rst2.next()) {
					if (rst2.getLong("timestamp") >= new Date().getTime()) {
						p.kickPlayer("You're dead! Time remaining until you can play again:\n\n" + Util.readableTimeDiff(rst2.getLong("timestamp") - new Date().getTime()));
						return;
					}
					else
						DBManager.performSafeExecute("delete from deaths where usuario=?", "i", rst.getInt("id"));
				}
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
		FPlayer player = Main.players.get(e.getPlayer());
		if (!player.isLogged()) e.setCancelled(true);
		else
		{
			e.setCancelled(true);
			Faction chatterFaction = player.getFactionObject();
			Pattern pat = Pattern.compile("(.*)(\\{DISPLAYNAME\\})(.*?)(\\{MESSAGE\\})(.*)");
			Matcher m = pat.matcher(Main.formats.get(Main.perms.getPrimaryGroup(e.getPlayer())));
			m.matches();
			Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(Main.class), new Runnable() {
				
				@Override
				public void run() {
					String message = e.getMessage();
					for (FPlayer p : Main.players.values()) {
						if (!p.isLogged()) continue;
						if (p.getPlayer().getLocation().distanceSquared(player.getPlayer().getLocation()) > 30*30 && message.charAt(0) != '!') continue;
						List<TellRawComponent> l = new Vector<TellRawComponent>();
						
						String input1 = ChatColor.translateAlternateColorCodes('&', m.group(1));
						
						if (m.group(1) != null)
							if(message.charAt(0) == '!'){
								l.add(new TextComponent("["));
								l.add(new TextComponent("GLOBAL", "red", true));
								l.add(new TextComponent("]"));
							}
							l.add(new TextComponent(input1));
						if (m.group(2) != null)
						{
							l.add(new TextComponent(ChatColor.getLastColors(input1)+"["));
							l.add(new ClickableComponent(ChatColor.BOLD+chatterFaction.getShortName(p.getFactionObject())+ChatColor.RESET+ChatColor.getLastColors(input1),"white",true,"View this faction's info","/f info "+chatterFaction.getID()));
							l.add(new TextComponent(ChatColor.getLastColors(input1)+"]"));

							if (p.getID() != player.getID() && (p.getFactionObject().getRelation(chatterFaction) == FactionRelation.OWN || p.getFactionObject().getRelation(chatterFaction) == FactionRelation.ALLIANCE))
								l.add(new ClickableComponent(ChatColor.getLastColors(input1) + e.getPlayer().getDisplayName(), "white",false,"Teleport to this player's location","/tele "+player.getID()));
							else
								l.add(new TextComponent(ChatColor.getLastColors(input1) + e.getPlayer().getDisplayName()));
						}
						if (m.group(3) != null)
							l.add(new TextComponent(ChatColor.translateAlternateColorCodes('&', m.group(3))));
						if (m.group(4) != null)
						{
							String input2 = ChatColor.translateAlternateColorCodes('&', m.group(3));
							TextComponent text = new TextComponent(ChatColor.getLastColors(input2) + message);

							if(message.charAt(0) == '!')
								text.setColor("light_blue");
							l.add(text);
						}
						if (m.group(5) != null)
							l.add(new TextComponent(ChatColor.translateAlternateColorCodes('&', m.group(5))));
						
						Util.tellRaw(p, l.toArray(new TextComponent[0]));
					}
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
	public void onInteractAtEntity(PlayerInteractEntityEvent e)
	{
		if (e.getRightClicked().getType()==EntityType.PLAYER && Main.players.get(e.getPlayer()).isLogged())
		{
			if (Main.players.get(e.getRightClicked()).isLogged())
			{
				if(Main.players.get(e.getPlayer()).getMenu()==null && Main.players.get(e.getRightClicked()).getMenu()==null && Main.perms.has(e.getPlayer(), "j76factions.requesttrade"))
				{
					if (Main.traderequests.containsKey(Main.players.get(e.getRightClicked())))
						return;
					
					if (Util.convertWorld(e.getRightClicked().getWorld()) == 0 && e.getRightClicked().getLocation().distanceSquared(new Location(e.getRightClicked().getWorld(), FListeners.spawnX, FListeners.spawnY, FListeners.spawnZ)) <= 300*300) {
						Main.players.get(e.getPlayer()).sendMessage(PluginPart.ECONOMY, "You are too close to spawn to trade!");
						return;
					}
					
					TradeRequest t = new TradeRequest(Main.players.get(e.getPlayer()),Main.players.get(e.getRightClicked()));
					Main.traderequests.put(Main.players.get(e.getRightClicked()), t);
					t.sendMessage();
				}
			}
		}
	}
	
	@EventHandler
	public void onSpawn(EntitySpawnEvent e)
	{
		if (e.getEntityType() != EntityType.PLAYER)
			if (e.getEntity().getLocation().distanceSquared(new Location(e.getEntity().getWorld(), spawnX,spawnY,spawnZ)) <= 40000 && Util.convertWorld(e.getEntity().getWorld())==0 && e.getEntityType() != EntityType.DROPPED_ITEM
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
		
		if (e.getPlayer().isOp() || Main.perms.has(e.getPlayer(), "j76factions.breakall"))
			return;
		
		if (Math.abs(e.getBlock().getLocation().getX() - spawnX) <= 100 && Math.abs(e.getBlock().getLocation().getZ() - spawnZ) <= 100 && Util.convertWorld(e.getBlock().getWorld())==0) // Trying to break spawn
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
		
		if (e.getPlayer().isOp() || Main.perms.has(e.getPlayer(), "j76factions.placeall"))
			return;
		if (Math.abs(e.getBlock().getLocation().getX() - spawnX) <= 100 && Math.abs(e.getBlock().getLocation().getZ() - spawnZ) <= 100 && Util.convertWorld(e.getBlock().getWorld())==0) // Trying to break spawn
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
		if (Math.abs(e.getBlock().getLocation().getX() - spawnX) <= 100 && Math.abs(e.getBlock().getLocation().getZ() - spawnZ) <= 100 && Util.convertWorld(e.getBlock().getWorld())==0) // Trying to break spawn
			e.setCancelled(true);
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
		if (e.getPlayer().isOp() || Main.perms.has(e.getPlayer(), "j76factions.placeall")) return;
		if (Math.abs(e.getBlockClicked().getLocation().getX()-spawnX) <= 100 && Math.abs(e.getBlockClicked().getLocation().getZ()-spawnZ) <= 100 && Util.convertWorld(e.getBlockClicked().getWorld())==0) // Trying to break spawn
			e.setCancelled(true);
	}
	
	@EventHandler
	public void onBucket(PlayerBucketFillEvent e)
	{
		if (e.getPlayer().isOp() || Main.perms.has(e.getPlayer(), "j76factions.breakall")) return;
		if (Math.abs(e.getBlockClicked().getLocation().getX()-spawnX) <= 100 && Math.abs(e.getBlockClicked().getLocation().getZ()-spawnZ) <= 100 && Util.convertWorld(e.getBlockClicked().getWorld())==0) // Trying to break spawn
			e.setCancelled(true);
	}
}
