package com.juanan76.factions.common;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.juanan76.factions.Main;
import com.juanan76.factions.common.tellraw.TellRawComponent;
import com.juanan76.factions.common.tellraw.TextComponent;
import com.juanan76.factions.factions.gens.Generator;
import com.juanan76.factions.npc.NPC;
import com.juanan76.factions.npc.NPCShop;
import com.juanan76.factions.npc.SellingItem;

import net.md_5.bungee.api.chat.BaseComponent;

public class Util {
	public static String getMoney(long amt)
	{
		String ret = (amt < 0) ? ChatColor.RED + "-₽" : ChatColor.GREEN + "₽";
		if (Math.abs(amt) < 1000)
			return ret + amt;
		else
		{
			String quantity = Long.toString(amt);
			if (Math.abs(amt) >= 1000000000000L)
			{
				quantity = quantity.substring(0,quantity.length()-11);
				ret += quantity.substring(0,quantity.length()-1)+"."+quantity.substring(quantity.length()-1);
				ret += "B";
			}
			else if (Math.abs(amt) >= 1000000L)
			{
				quantity = quantity.substring(0,quantity.length()-5);
				ret += quantity.substring(0,quantity.length()-1)+"."+quantity.substring(quantity.length()-1);
				ret += "M";
			}
			else if (Math.abs(amt) >= 1000L)
			{
				quantity = quantity.substring(0,quantity.length()-2);
				ret += quantity.substring(0,quantity.length()-1)+"."+quantity.substring(quantity.length()-1);
				ret += "K";
			}
			else
				ret += "";
		}
		return ret + ChatColor.WHITE;
	}
	public static String getRespect(long amt)
	{
		String ret = (amt < 0) ? ChatColor.RED + "-" : ChatColor.GREEN + "";
		if (Math.abs(amt) < 1000)
			return ret + amt + ChatColor.RESET;
		else
		{
			String quantity = Long.toString(amt);
			if (Math.abs(amt) >= 1000000000000L)
			{
				quantity = quantity.substring(0,quantity.length()-11);
				ret += quantity.substring(0,quantity.length()-1)+"."+quantity.substring(quantity.length()-1);
				ret += "B";
			}
			else if (Math.abs(amt) >= 1000000L)
			{
				quantity = quantity.substring(0,quantity.length()-5);
				ret += quantity.substring(0,quantity.length()-1)+"."+quantity.substring(quantity.length()-1);
				ret += "M";
			}
			else if (Math.abs(amt) >= 1000L)
			{
				quantity = quantity.substring(0,quantity.length()-2);
				ret += quantity.substring(0,quantity.length()-1)+"."+quantity.substring(quantity.length()-1);
				ret += "K";
			}
			else
				ret += "";
		}
		return ret + ChatColor.RESET;
	}
	
	public static int convertWorld(World w)
	{
		if (w.getName().equalsIgnoreCase("world"))
			return 0;
		else if (w.getName().equalsIgnoreCase("world_nether"))
			return -1;
		else
			return 1;		
	}
	
	public static World iconvertWorld(int i)
	{
		switch (i)
		{
		case 0:
			return Bukkit.getWorld("world");
		case -1:
			return Bukkit.getWorld("world_nether");
		case 1:
			return Bukkit.getWorld("world_the_end");
		default:
			return null;
		}
	}
	
	public static int getTeleID()
	{
		int i=0;
		while (!Main.teleports.containsKey(i) && i <= Main.teleports.size())
			i++;
		return i;
	}
	
	public static int getNextID(String table, String primaryKey) throws SQLException
	{
		ResultSet rst = DBManager.performQuery("select id from "+table+" order by "+primaryKey+" desc");
		if (!rst.next()) return 0;
		else return rst.getInt(1)+1;
	}
	
	public static void tellRaw(FPlayer p, TellRawComponent... stuff)
	{
		BaseComponent[] components = Arrays.stream(stuff).filter(c -> c != null).map(c -> c.toBukkit()).toArray(BaseComponent[]::new);
		
		p.getPlayer().spigot().sendMessage(components);
	}
	
	public static void tellSeparator(FPlayer user)
	{
		Util.tellRaw(user, new TextComponent("-----------------------------------------------", "yellow"));
	}
	
	public static boolean isFull(Inventory i)
	{
		ItemStack[] contents = i.getStorageContents();
		for (ItemStack it : contents)
		{
			if (it == null)
				return false;
		}
		return true;
	}
	
	public static String readableTimeDiff(long l) {
		TimeUnit[] units = new TimeUnit[] { TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS };
		
		long remaining = l;
		Map<TimeUnit,Long> result = new HashMap<TimeUnit, Long>();
		for (TimeUnit u: units) {
			long val = u.convert(remaining, TimeUnit.MILLISECONDS);
			result.put(u, val);
			remaining = remaining - u.toMillis(val);
		}
		
		return result.get(TimeUnit.HOURS) + "h " + result.get(TimeUnit.MINUTES) + "m " + result.get(TimeUnit.SECONDS) + "s";
	}
	
	public static void spawnNPCS() throws SQLException {
		NPC shopGeneradores = new NPCShop(ChatColor.DARK_RED+ChatColor.BOLD.toString()+"Faction shop", new Location(Bukkit.getWorld("world"),-56,72,50),
			Arrays.asList(new SellingItem[] { Generator.getStack(1),Generator.getStack(2),Generator.getStack(3),Generator.getStack(4),Generator.getStack(5),Generator.getStack(6),Generator.getStack(7),Generator.getStack(8),Generator.getStack(9),Generator.getStack(10) }));
		Main.spawnShops.add(shopGeneradores);
		Bukkit.getPluginManager().registerEvents(shopGeneradores, Main.getPlugin(Main.class));
		shopGeneradores.spawnEntity();
		shopGeneradores.save("shop_generadores");
		NPC shopMagic = new NPCShop(ChatColor.AQUA+ChatColor.BOLD.toString()+"Magic shop", new Location(Bukkit.getWorld("world"),-56,72,53),
				Arrays.asList(new SellingItem[] {
						new SellingItem(Material.EXPERIENCE_BOTTLE, ChatColor.RED+"[LL]"+ChatColor.WHITE+" Experience bottle", 250),
						new SellingItem(Material.ENCHANTED_GOLDEN_APPLE,ChatColor.RED+"[LL]"+ChatColor.WHITE+"Gapple",15000),
						new SellingItem(Material.TOTEM_OF_UNDYING,ChatColor.RED+"[LL]"+ChatColor.WHITE+"Lifesaver",50000)
						}));
		Main.spawnShops.add(shopMagic);
		Bukkit.getPluginManager().registerEvents(shopMagic, Main.getPlugin(Main.class));
		shopMagic.spawnEntity();
		shopMagic.save("shop_magic");
	}
	
	public static void loadNPCS() throws SQLException {
		NPC shopGeneradores = new NPCShop("shop_generadores",ChatColor.DARK_RED+ChatColor.BOLD.toString()+"Faction shop", new Location(Bukkit.getWorld("world"),-56,72,50),
				Arrays.asList(new SellingItem[] { Generator.getStack(1),Generator.getStack(2),Generator.getStack(3),Generator.getStack(4),Generator.getStack(5),Generator.getStack(6),Generator.getStack(7),Generator.getStack(8),Generator.getStack(9),Generator.getStack(10) }));
		Main.spawnShops.add(shopGeneradores);
		Bukkit.getPluginManager().registerEvents(shopGeneradores, Main.getPlugin(Main.class));
		
		NPC shopMagic = new NPCShop("shop_magic", ChatColor.AQUA+ChatColor.BOLD.toString()+"Magic shop", new Location(Bukkit.getWorld("world"),-56,72,53),
				Arrays.asList(new SellingItem[] {
						new SellingItem(Material.EXPERIENCE_BOTTLE, ChatColor.RED+"[LL]"+ChatColor.WHITE+" Experience bottle", 250),
						new SellingItem(Material.ENCHANTED_GOLDEN_APPLE,ChatColor.RED+"[LL]"+ChatColor.WHITE+"Gapple",15000),
						new SellingItem(Material.TOTEM_OF_UNDYING,ChatColor.RED+"[LL]"+ChatColor.WHITE+"Lifesaver",50000)
						}));
		Main.spawnShops.add(shopMagic);
		Bukkit.getPluginManager().registerEvents(shopMagic, Main.getPlugin(Main.class));
	}
}
