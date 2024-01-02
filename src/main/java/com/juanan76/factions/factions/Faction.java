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
import com.juanan76.factions.common.FListeners;
import com.juanan76.factions.common.FPlayer;
import com.juanan76.factions.common.PluginPart;
import com.juanan76.factions.common.Util;
import com.juanan76.factions.common.font.FontUtil;
import com.juanan76.factions.common.tellraw.ClickableComponent;
import com.juanan76.factions.common.tellraw.TextComponent;
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
	private Map<Integer,FactionRelation> relations;
	
	public enum FactionRelation {
		WAR("w"),
		ALLIANCE("a"),
		NEUTRAL("n"),
		OWN("o");
		
		String sqlRepr;
		
		private FactionRelation(String sqlRepr) {
			this.sqlRepr = sqlRepr;
		}
		
		public String getSQLRepr() {
			return this.sqlRepr;
		}
		
		public static FactionRelation fromSQLRepr(String repr) {
			for (FactionRelation r : FactionRelation.values())
				if (r.getSQLRepr().equals(repr))
					return r;
			
			return null;
		}
		
		@Override
		public String toString() {
			return this.getSQLRepr();
		}
		
	}
	
	public Faction(int id) throws SQLException
	{
		this.relations = new HashMap<Integer, FactionRelation>();
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
	
	private FactionRelation calculate(FactionRelation ours, FactionRelation theirs) {
		if (ours == FactionRelation.WAR || theirs == FactionRelation.WAR)
			return FactionRelation.WAR;
		else if (ours == FactionRelation.ALLIANCE && theirs == FactionRelation.ALLIANCE)
			return FactionRelation.ALLIANCE;
		else
			return FactionRelation.NEUTRAL;
	}
	
	public void loadRelations() throws SQLException
	{
		ResultSet rst = DBManager.performQuery("SELECT t1.faccion2 as faccion, t1.relacion as r1, t2.relacion as r2 FROM relaciones t1, relaciones t2 where t1.faccion1=t2.faccion2 and t1.faccion2=t2.faccion1 and t1.faccion1="+id+" "
				+ "UNION SELECT t.faccion2 as faccion, t.relacion as r1, null as r2 from relaciones t where t.faccion1="+id+" and not exists(select * from relaciones tmp where tmp.faccion2=t.faccion1 and tmp.faccion1=t.faccion2) "
				+ "UNION SELECT t.faccion1 as faccion, null as r1, t.relacion as r2 from relaciones t where t.faccion2="+id+" and not exists(select * from relaciones tmp where tmp.faccion2=t.faccion1 and tmp.faccion1=t.faccion2);");
		while (rst.next()) {
			this.relations.put(rst.getInt("faccion"), this.calculate(FactionRelation.fromSQLRepr(rst.getString("r1")), FactionRelation.fromSQLRepr(rst.getString("r2"))));
		}
	}
	
	public void delete() throws SQLException
	{
		DBManager.performExecute("delete from facciones where id="+this.id);
		
		// Update faction's relations, in case this one had relations with them
		for (Faction f : Main.factions.values())
			f.loadRelations();
		
		Main.factions.remove(this.id);
	}
	
	public FactionRelation getRelation(Faction f)
	{
		if (f == null)
			return FactionRelation.NEUTRAL;
		
		if (f.getID()==this.getID())
			return FactionRelation.OWN;
		else if (!this.relations.containsKey(f.getID()))
			return FactionRelation.NEUTRAL;
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
		if ((Math.abs(x-FListeners.spawnX/16)<=20 && Math.abs(z-FListeners.spawnZ/16)<=20) && world==0)
			return false; // Chunk too close to spawn
		
		try {
			ResultSet rst = DBManager.performQuery("select * from plots where x="+x+" and y="+z+" and world="+world);
			if (rst.next()) // Chunk already claimed
				return false;
			else
				return this.getRespect() >= this.getNPlots()*this.getNPlots()*this.getNPlots();
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
	
	public boolean isLeader(int id)
	{
		return this.getLeader() == id;
	}
	
	public long getRespect()
	{
		return this.respect;
	}
	
	public long getMoney()
	{
		return this.dinero;
	}

	public String getName(Faction viewer)
	{
		if (this.id==-1) return ChatColor.RESET + ChatColor.DARK_RED.toString() + "None";
		else {
			FactionRelation rel = this.getRelation(viewer);
			ChatColor color = ChatColor.RED;
			switch (rel) {
			case WAR:
				color = ChatColor.DARK_RED;
				break;
			case ALLIANCE:
				color = ChatColor.BLUE;
				break;
			case OWN:
				color = ChatColor.GREEN;
				break;
			default:
				color = ChatColor.RED;
			}
			return color + this.name + " ("+Util.getRespect(this.respect)+ color + ")" + ChatColor.RESET;
		}
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
			FactionRelation rel = this.getRelation(viewer);
			if (rel == FactionRelation.ALLIANCE)
				color = ChatColor.BLUE;
			else if (rel == FactionRelation.WAR)
				color = ChatColor.DARK_RED;
			else if (rel == FactionRelation.OWN)
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
	
	public void updateRelation(Faction target, FactionRelation nuevo) throws SQLException
	{
		ResultSet rst = DBManager.performSafeQuery("select * from relaciones where faccion1=? and faccion2=?","ii",this.getID(),target.getID());
		if (rst.next())
			DBManager.performSafeExecute("update relaciones set relacion=? where faccion1=? and faccion2=?","sii",nuevo.toString(),this.getID(),target.getID());
		else
			DBManager.performSafeExecute("insert into relaciones values (?,?,?)", "iis", this.getID(),target.getID(),nuevo.toString());

		this.loadRelations();
		target.loadRelations();
	}
	
	public void broadcastMessage(String message) {
		for (FPlayer p : Main.players.values()) {
			if (p.isLogged() && p.getFaction() == this.id) {
				p.sendMessage(PluginPart.FACTIONS, message);
			}
		}
	}
	
	public void showInfo(FPlayer fsender) throws SQLException {
		Util.tellSeparator(fsender);
		Util.tellRaw(fsender, new TextComponent(FontUtil.getCenteredMessage("Faction &2"+this.getRawName())));
		Util.tellRaw(fsender, new TextComponent(FontUtil.getCenteredMessage("&eMembers: &b"+this.getNMembers()+"/"+this.getMaxMembers()+"    &aRespect: "+ChatColor.stripColor(Util.getRespect(this.getRespect())))));
		Util.tellRaw(fsender, new TextComponent("        "));
		Util.tellRaw(fsender, new TextComponent("► Leader: ","red"), new TextComponent(FPlayer.nickfromID(this.getLeader()),"aqua"));
		if (!this.isMember(fsender.getID()))
			Util.tellRaw(fsender, new TextComponent("► ["),new ClickableComponent("APPLY","green",true,"Send an application to this faction","/f j "+this.getRawName()),
					new TextComponent("]"));
		Util.tellRaw(fsender, new TextComponent("► [","red"), new ClickableComponent("View member list","gold",false,"View this faction's members","/f list "+this.getID()), new TextComponent("]","red"));
		Util.tellRaw(fsender, new TextComponent("► [","red"), new ClickableComponent("View active wars","gold",false,"View this faction's wars","/f wars "+this.getID()), new TextComponent("]","red"));
		Util.tellRaw(fsender, new TextComponent(""));
		if (this.getID()!= fsender.getFaction() && fsender.getFaction() != -1) {
			String ourRelation, color;
			switch (this.getRelation(fsender.getFactionObject())) {
			case WAR:
				ourRelation = "WAR";
				color = "dark_red";
				break;
			case ALLIANCE:
				ourRelation = "ALLIANCE";
				color = "blue";
				break;
			default:
				ourRelation = "NEUTRAL";
				color = "white";
			}
			Util.tellRaw(fsender, new TextComponent("! Our relation to this faction: ","gold"), new TextComponent(ourRelation,color,true));
		}
		if (this.getID()!= fsender.getFaction() && fsender.getFactionObject().isLeader(fsender.getID())) {
			if (this.getRelation(fsender.getFactionObject()) != FactionRelation.WAR)
				Util.tellRaw(fsender, new TextComponent("► [","red"), new ClickableComponent("Declare War","red",true,"Declare war to this faction!","/f war "+this.getID()),new TextComponent("]","red") );
			if (this.getRelation(fsender.getFactionObject()) != FactionRelation.NEUTRAL)
				Util.tellRaw(fsender, new TextComponent("► [","white"), new ClickableComponent("Set Peace","white",true,"Set Peace to this faction.","/f neutral "+this.getID()),new TextComponent("]","white") );
			if (this.getRelation(fsender.getFactionObject()) != FactionRelation.ALLIANCE)
				Util.tellRaw(fsender, new TextComponent("► [","blue"), new ClickableComponent("Propose Alliance","blue",true,"Propose an alliance to this faction.","/f alliance "+this.getID()),new TextComponent("]","blue") );
		}
		Util.tellSeparator(fsender);
	}
}
