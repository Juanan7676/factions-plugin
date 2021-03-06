package com.juanan76.factions.common;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.juanan76.factions.Main;
import com.juanan76.factions.common.tellraw.TellRawComponent;

public class Util {
	public static String getMoney(long amt)
	{
		String ret = (amt < 0) ? ChatColor.RED + "-$" : ChatColor.GREEN + "$";
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
	
	public static int getNextID() throws SQLException
	{
		ResultSet rst = DBManager.performQuery("select id from users order by id desc");
		if (!rst.next()) return 0;
		else return rst.getInt(1)+1;
	}
	
	public static void tellRaw(String user, TellRawComponent... stuff)
	{
		String t = "tellraw "+user+" [";
		String json = "";
		for (TellRawComponent c : stuff)
		{
			if (c != null) json += ","+c.getRepr();
		}
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), t+json.substring(1)+"]");
	}
	
	public static void tellSeparator(String user)
	{
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw "+user+" {\"text\":\"-----------------------------------------------\",\"color\":\"yellow\"}");
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
}
