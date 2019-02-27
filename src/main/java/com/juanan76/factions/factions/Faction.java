package com.juanan76.factions.factions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.bukkit.ChatColor;

import com.juanan76.factions.Main;
import com.juanan76.factions.common.DBManager;
import com.juanan76.factions.common.FPlayer;
import com.juanan76.factions.common.Util;
import com.juanan76.factions.factions.missions.Mission;
import com.juanan76.factions.factions.missions.MissionSell;

public class Faction {
	private int id;
	private String name;
	private String shortname;
	private long respect;
	private long dinero;
	private int members;
	private int leader;
	private List<Plot> claimedChunks;
	private List<Mission> missions;
	private Map<Integer,String> relations;
	
	public Faction(int id) throws SQLException
	{
		ResultSet rst = DBManager.performQuery("select * from facciones where id="+id);
		if (rst.next())
		{
			this.id = id;
			this.name = rst.getString("nombre");
			this.shortname = rst.getString("nombrecorto");
			this.respect = rst.getLong("respeto");
			this.leader = rst.getInt("lider");
			this.dinero = rst.getLong("dinero");
			
			rst = DBManager.performQuery("select count(*) from miembros where faccion="+id);
			this.members = (rst.next()) ? rst.getInt(1) : 0;
			
			this.claimedChunks = new Vector<Plot>();
			rst = DBManager.performQuery("select * from plots where faccion="+id);
			while (rst.next())
				this.claimedChunks.add(new Plot(rst.getInt("x"),rst.getInt("y"),rst.getInt("world")));
			this.loadRelations();
			
			this.missions = new Vector<Mission>();
			rst = DBManager.performQuery("select id,tipo from misiones where faccion="+id);
			while (rst.next())
			{
				if (rst.getInt(2) == 0) // Mision de vender
					this.missions.add(new MissionSell(rst.getInt(1)));
			}
		}
		else
		{
			this.id = -1;
			this.name = ChatColor.RED+"None";
			this.shortname = "PSN";
		}
	}
	
	public void loadRelations() throws SQLException
	{
		this.relations = new HashMap<Integer,String>();
		ResultSet rst = DBManager.performQuery("select faccion2,relacion from relaciones where faccion1="+id);
		while (rst.next())
			this.relations.put(rst.getInt(1), rst.getString(2));
		
		rst = DBManager.performQuery("select faccion1,relacion from relaciones where faccion2="+id);
		while (rst.next())
			this.relations.put(rst.getInt(1), rst.getString(2));
	}
	
	public void delete() throws SQLException
	{
		DBManager.performExecute("delete from facciones where id="+this.id);
		
		// Update faction's relations, in case this one had relations with them
		for (Faction f : Main.factions.values())
			f.loadRelations();
		
		Main.factions.remove(this.id);
	}
	
	public String getRelation(Faction f)
	{
		if (f.getID()==this.getID())
			return "o";
		else if (!this.relations.containsKey(f.getID()))
			return "n";
		else
			return this.relations.get(f.getID());
	}
	
	public void claimChunk(int x, int z, int world, String desc) throws SQLException
	{
		if (this.isClaimable(x, z, world))
			this.claimedChunks.add(new Plot(x,z,world,this.name,desc,this.id));
	}
	
	public void unclaimChunk(int x, int z) throws SQLException
	{
		Iterator<Plot> it = this.claimedChunks.iterator();
		while (it.hasNext())
		{
			Plot p = it.next();
			if (p.getX()==x && p.getZ()==z)
			{
				p.remove();
				it.remove();
			}
		}
	}
	

	
	public boolean isClaimable(int x, int z, int world)
	{
		if ((Math.abs(x)<=20 && Math.abs(z)<=20) && world==0)
			return false; // Chunk too close to spawn
		
		try {
			ResultSet rst = DBManager.performQuery("select * from plots where x="+x+" and y="+z+" and world="+world);
			if (rst.next()) // Chunk already claimed
				return false;
			else
				return this.getRespect() >= this.getNPlots()*this.getNPlots()*this.getNPlots()*this.getNPlots();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Unsafe method; call only in case the member doen't have a faction.
	 * @param id
	 * @throws SQLException
	 */
	public void addMember(int id) throws SQLException
	{
		this.members++;
		DBManager.performExecute("insert into miembros values ("+id+","+this.id+",0,0)");
	}
	
	/**
	 * Unsafe method; call only in case the member is in this faction.
	 * @param id
	 * @throws SQLException
	 */
	public void removeMember(int id) throws SQLException
	{
		this.members--;
		DBManager.performExecute("delete from miembros where usuario="+id);
	}
	
	public boolean isMember(int id) throws SQLException
	{
		ResultSet rst = DBManager.performQuery("select * from miembros where usuario="+id+" and faccion="+this.id);
		return rst.next();
	}
	
	public long getRespect()
	{
		return this.respect;
	}
	
	public long getMoney()
	{
		return this.dinero;
	}

	public String getName()
	{
		if (this.id==-1) return ChatColor.RESET + ChatColor.DARK_RED.toString() + "None";
		else return this.name + " ("+Util.getRespect(this.respect)+")";
	}
	
	public String getRawName()
	{
		return this.name;
	}
	
	public int getNMembers()
	{
		return this.members;
	}
	
	public int getMaxMembers()
	{
		return (int)Math.floor(Math.log(this.respect+8));
	}
	
	public int getLeader()
	{
		return this.leader;
	}
	
	public int getNPlots()
	{
		return this.claimedChunks.size();
	}
	
	public int getID()
	{
		return this.id;
	}
	
	public String getRawShortName()
	{
		return this.shortname;
	}
	
	public String getShortName(Faction viewer)
	{
		ChatColor color;
		if (this.id == -1)
			color = ChatColor.DARK_GRAY;
		else
		{
			String rel = this.getRelation(viewer);
			if (rel.equals("a"))
				color = ChatColor.BLUE;
			else if (rel.equals("w"))
				color = ChatColor.DARK_RED;
			else if (rel.equals("o"))
				color = ChatColor.GREEN;
			else
				color = ChatColor.RED;
			
		}
		return color + this.getRawShortName();
	}
	
	public void updateLeader(int newLeader) throws SQLException
	{
		this.leader = newLeader;
		DBManager.performExecute("update facciones set lider="+newLeader+" where faccion="+this.id);
		DBManager.performExecute("update miembros set rango=-1 where usuario="+newLeader);
	}
	
	public List<Integer> getApplications() throws SQLException
	{
		List<Integer> l = new Vector<Integer>();
		ResultSet rst = DBManager.performQuery("select usuario from peticiones where faccion="+this.id);
		while (rst.next())
			l.add(rst.getInt(1));
		return l;
	}
	
	public void addMoney(long amt) throws SQLException
	{
		DBManager.performExecute("update facciones set dinero="+(this.dinero+amt)+" where id="+this.id);
		this.dinero+=amt;
	}
	
	public void addRespect(long amt) throws SQLException
	{
		DBManager.performExecute("update facciones set respeto="+(this.respect+amt)+" where id="+this.id);
		this.respect+=amt;
		for (FPlayer p : Main.players.values())
			if (p.getFaction()==this.id)
				p.updateFaction();
	}
	
	public void updateMissions()
	{ // TODO missions system
		
	}
}
