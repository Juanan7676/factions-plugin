package com.juanan76.factions.factions;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.ChatColor;

import com.juanan76.factions.Main;
import com.juanan76.factions.common.DBManager;

public class Plot {
	private int x;
	private int z;
	private int world;
	private String title;
	private String desc;
	private int f;
	
	public Plot(int x, int z, int world)
	{
		try {
		this.x = x;
		this.z = z;
		this.world = world;
		ResultSet rst = DBManager.performQuery("select * from plots where x="+x+" and y="+z+" and world="+world);
		if (!rst.next())
		{
			this.title = ChatColor.GRAY+"Wilderness";
			this.desc = "Be careful out there, traveler.";
			this.f = -1;
		}
		else
		{
			this.title = rst.getString("title");
			this.desc = rst.getString("desc");
			this.f = rst.getInt("faccion");
		}
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Construct & push to DB
	 * @throws SQLException if a database error occurs.
	 */
	public Plot(int x, int z, int world, String title, String desc, int f) throws SQLException
	{
		this.x = x;
		this.z = z;
		this.title = title;
		this.desc = desc;
		this.f = f;
		this.world = world;
		DBManager.performSafeExecute("insert into plots values (?,?,?,?,?,?)","iiiiss",f,world,x,z,title,desc);
	}
	
	public String getTitle()
	{
		return this.title;
	}
	
	public String getTitle(Faction user)
	{
		if (this.f == -1)
			return ChatColor.GRAY+this.getTitle();
		else
		{
			String rel = Main.factions.get(this.f).getRelation(user);
			if (rel=="a")
				return ChatColor.BLUE+this.getTitle();
			else if (rel=="o")
				return ChatColor.GREEN+this.getTitle();
			else if (rel=="n")
				return ChatColor.RED+this.getTitle();
			else
				return ChatColor.DARK_RED+this.getTitle();
		}
	}
	
	public String getDesc()
	{
		return ChatColor.YELLOW + ChatColor.ITALIC.toString() + this.desc;
	}
	
	public int getFaction()
	{
		return this.f;
	}
	
	public int getX()
	{
		return this.x;
	}
	
	public int getZ()
	{
		return this.z;
	}
	
	public void save() throws SQLException
	{
		DBManager.performSafeExecute("update plots set title=?,desc=?,faccion=? where x=? and y=? and world=?","ssiiii",this.title,this.desc,this.f,this.x,this.z,this.world);
	}
	
	public void remove() throws SQLException
	{
		DBManager.performExecute("delete from plots where x="+this.x+" and y="+this.z+" and world="+this.world);
	}
}
