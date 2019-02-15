package com.juanan76.factions.common;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;

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
}
