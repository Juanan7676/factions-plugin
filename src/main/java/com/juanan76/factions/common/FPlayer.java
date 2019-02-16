package com.juanan76.factions.common;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.juanan76.factions.Main;
import com.juanan76.factions.factions.Faction;
import com.juanan76.factions.factions.Plot;
import com.juanan76.factions.npc.SellingItem;

public class FPlayer {
	private Player assoc;
	private PlayerScoreboard scoreboard;
	private int playerID;
	private int faction;
	private long money;
	private boolean isLogged;
	private int xchunk;
	private int zchunk;
	private int currTerritory;
	private Inventory currShop;
	
	public static FPlayer fromID(int id)
	{
		for (FPlayer p : Main.players.values())
		{
			if (p.getID()==id)
				return p;
		}
		return null;
	}
	
	
	public static FPlayer fromNick(String nick)
	{
		for (FPlayer p : Main.players.values())
		{
			if (p.getPlayer().getName().equals(nick))
				return p;
		}
		return null;
	}
	
	public static String nickfromID(int id) throws SQLException
	{
		ResultSet rst = DBManager.performQuery("select nick from users where id="+id);
		if (!rst.next())
			return "";
		else
			return rst.getString(1);
	}
	
	public FPlayer(Player p)
	{
		this.assoc = p;
		this.playerID = -1;
		this.money = 0;
		this.isLogged = false;
		this.scoreboard = null;
		this.xchunk = p.getLocation().getBlockX()/16;
		this.zchunk = p.getLocation().getBlockZ()/16;
	}
	
	public void sendMessage(PluginPart sender, String msg)
	{
		this.assoc.sendMessage(ChatColor.WHITE + ChatColor.BOLD.toString() + ">>[" + ChatColor.RESET + ChatColor.AQUA + sender + ChatColor.WHITE + ChatColor.BOLD.toString() + "] " + ChatColor.RESET + msg);
	}

	public void login() throws SQLException {
		ResultSet rst = DBManager.performSafeQuery("select dinero.id,dinero.dinero from users,dinero where nick=? and dinero.id=users.id","s",assoc.getName());
		
		assert rst.next();
		
		this.playerID = rst.getInt(1);
		this.money = rst.getLong(2);
		this.updateFaction();
		this.isLogged = true;
		
		this.sendMessage(PluginPart.MAIN,ChatColor.GREEN + "Login sucessful!");
		
		
		this.scoreboard = new PlayerScoreboard(this.assoc);
		
		
		Player p = this.assoc;
		
		int id = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(Main.class), new Runnable() {
			@Override
			public void run() {
				Main.pvp.get(p).update();
			}
			
		}, 0, 1);
		
		
		Main.pvpUpdaters.put(this.assoc,id);
	}
	/**
	 * Call this every time a player moves.
	 */
	public void move()
	{
		Location l = this.assoc.getLocation();
		if ((int)Math.floor(l.getX()/16) != this.xchunk || (int)Math.floor(l.getZ()/16) != this.zchunk)
		{
			
			this.xchunk = (int)Math.floor(l.getX()/16);
			this.zchunk = (int)Math.floor(l.getZ()/16);
			this.updateTerritory();
		}
	}

	public void updateTerritory() {
		Plot p = new Plot(this.xchunk,this.zchunk,Util.convertWorld(this.assoc.getWorld()));
		this.updateMap();
		if (this.currTerritory != p.getFaction())
		{
			this.currTerritory = p.getFaction();
			this.assoc.sendTitle(p.getTitle(Main.factions.get(this.faction)), p.getDesc(),40,10,40);
		}
	}
	
	public Player getPlayer()
	{
		return this.assoc;
	}
	
	public void updateMap()
	{
		this.scoreboard.updateMap(this.xchunk, this.zchunk, this.faction);
	}
	
	public void updateFaction() throws SQLException
	{
		ResultSet rst = DBManager.performSafeQuery("select faccion from miembros where usuario=?", "i", this.playerID);
		if (!rst.next()) this.faction = -1;
		else this.faction = rst.getInt(1);
		if (this.scoreboard != null) this.scoreboard.updateFaction();
	}
	
	public long getMoney()
	{
		return this.money;
	}
	
	public void addMoney(long amt)
	{
		try {
			DBManager.performExecute("update dinero set dinero=dinero+("+amt+") where id="+this.playerID);
			this.money += amt;
			this.scoreboard.updateBalance(this.money);
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public int getChunkX()
	{
		return this.xchunk;
	}
	
	public int getChunkZ()
	{
		return this.zchunk;
	}
	
	public int getFaction()
	{
		return this.faction;
	}
	
	public Faction getFactionObject()
	{
		return Main.factions.get(this.faction);
	}
	
	public boolean isLogged()
	{
		return this.isLogged;
	}
	
	public int getID()
	{
		return this.playerID;
	}
	
	public int getCurrTerritory()
	{
		return this.currTerritory;
	}
	
	public Inventory getShop()
	{
		return this.currShop;
	}
	
	public void openShop(Inventory i)
	{
		this.currShop = i;
		this.assoc.openInventory(i);
	}
	public void closeShop()
	{
		this.assoc.closeInventory();
		this.currShop = null;
	}
	
	public boolean purchaseItem(SellingItem i, int qty)
	{
		if (this.money < i.getPurchasePrice(qty))
		{
			this.sendMessage(PluginPart.ECONOMY, ChatColor.RED + "You don't have enough money to purchase this item!");
			this.assoc.playSound(this.assoc.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
			return false;
		}
		else if (Util.isFull(this.assoc.getInventory()))
		{
			this.sendMessage(PluginPart.ECONOMY, ChatColor.RED + "You don't have enough space in your inventory to purchase this item!");
			this.assoc.playSound(this.assoc.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
			return false;
		}
		else
		{
			this.assoc.getInventory().addItem(i.getItemStack(qty));
			this.addMoney(-i.getPurchasePrice(qty));
			this.sendMessage(PluginPart.ECONOMY, ChatColor.GREEN + "Purchase successful!");
			this.assoc.playSound(this.assoc.getLocation(), Sound.ENTITY_VILLAGER_TRADE, 1.0F, 1.0F);
			return true;
		}
	}
}
