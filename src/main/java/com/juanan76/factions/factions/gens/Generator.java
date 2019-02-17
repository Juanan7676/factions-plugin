package com.juanan76.factions.factions.gens;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import com.juanan76.factions.common.DBManager;
import com.juanan76.factions.common.Util;
import com.juanan76.factions.factions.Faction;
import com.juanan76.factions.npc.SellingItem;

public class Generator {
	private Location genLoc;
	private Faction owner;
	private int lvl;
	private int progress;
	private double lvlUp;
	
	public static SellingItem getStack(int lvl)
	{
		Map<Enchantment,Integer> ench = new HashMap<Enchantment,Integer>();
		ench.put(Enchantment.ARROW_INFINITE, lvl);
		return new SellingItem(Material.SPAWNER, ChatColor.GRAY+ChatColor.BOLD.toString() + "Lvl "+lvl+" Generator", (long)Math.ceil(5000*Math.pow(2.75, lvl)), ench, Arrays.asList(new String[] {"",ChatColor.GOLD.toString()+ChatColor.UNDERLINE+"Respect generation:"+ChatColor.RESET+ChatColor.GOLD+" "+(int)Math.round(3600.0/(0.001*Math.pow(3, 11-lvl)*5))+" respect/hour"}), true);
	}
	
	public Generator(Faction faction, Location l, int lvl)
	{
		this.owner = faction;
		this.genLoc = l;
		this.lvl = lvl;
		this.progress = 0;
		this.lvlUp = 0.001*Math.pow(3, 11-this.lvl);
	}
	
	public Generator(Faction faction, Location l, int lvl, int progress)
	{
		this(faction,l,lvl);
		this.progress = progress;
	}
	
	/** Call every time the generator spawns a creature. **/
	public void update()
	{
		this.progress++;
		if (this.progress >= this.lvlUp)
		{
			try {
				owner.addRespect((long)Math.floor(this.progress/this.lvlUp));
				this.progress = 0;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Location getLoc()
	{
		return this.genLoc;
	}
	
	public int getProgress()
	{
		return this.progress;
	}
	
	public double getLvlUp()
	{
		return this.lvlUp;
	}
	
	public int getLvl()
	{
		return this.lvl;
	}
	
	public void create() throws SQLException
	{
		DBManager.performExecute("insert into generators values ("+this.owner.getID()+","+Util.convertWorld(this.genLoc.getWorld())+","+this.genLoc.getBlockX()+","+this.genLoc.getBlockY()+
			","+this.genLoc.getBlockZ()+","+this.lvl+",0)");
	}
	
	public void save() throws SQLException
	{
		DBManager.performExecute("update generators set progress="+this.progress+" where world="+Util.convertWorld(this.genLoc.getWorld())+" and x="+this.genLoc.getBlockX()+" and y="+this.genLoc.getBlockY()+" and z="+this.genLoc.getBlockZ());
	}
	
	public void delete() throws SQLException
	{
		DBManager.performExecute("delete from generators where world="+Util.convertWorld(this.genLoc.getWorld())+" and x="+this.genLoc.getBlockX()+" and y="+this.genLoc.getBlockY()+" and z="+this.genLoc.getBlockZ());
	}
}
