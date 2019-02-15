package com.juanan76.factions.factions.missions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import com.juanan76.factions.Main;
import com.juanan76.factions.common.DBManager;
import com.juanan76.factions.common.Util;
import com.juanan76.factions.factions.Faction;

public class MissionSell implements Mission {
	
	private Material mat;
	private int qt;
	private int left;
	private int id;
	private Faction f;
	
	private final List<Material> conversion = Arrays.asList(new Material[]{
			Material.COBBLESTONE,
			Material.OAK_PLANKS,
			Material.BREAD,
			Material.CACTUS,
			Material.SUGAR_CANE,
			Material.IRON_INGOT,
			Material.DIAMOND
	});
	
	public MissionSell(int id) throws SQLException
	{
		ResultSet rst = DBManager.performQuery("select * from misiones where id="+id);
		if (!rst.next()) throw new IllegalArgumentException("No such missionID "+id);
		
		this.id = id;
		this.mat = this.conversion.get(rst.getInt("d1"));
		this.qt = rst.getInt("d2");
		this.left = rst.getInt("d3");
		this.f = Main.factions.get(rst.getInt("faccion"));
	}
	
	public MissionSell(Material mat, int qt, Faction f) throws SQLException
	{
		this.id = MissionSell.getNewID();
		this.mat = mat;
		this.qt = qt;
		this.left = qt;
		this.f = f;
	}
	
	private static int getNewID() throws SQLException
	{
		ResultSet rst = DBManager.performQuery("select id from misiones order by id desc");
		if (!rst.next())
			return 0;
		else
			return rst.getInt(1)+1;
	}
	
	private String getMatName()
	{
		switch (this.mat)
		{
		case COBBLESTONE:
			return "Cobblestone";
		case OAK_PLANKS:
			return "Wood";
		case BREAD:
			return "Bread";
		case CACTUS:
			return "Cactus";
		case SUGAR_CANE:
			return "Sugar Cane";
		case IRON_INGOT:
			return "Iron";
		case  DIAMOND:
			return "Diamonds";
		default:
			return "";
		}
	}
	
	@Override
	public String getName() {
		return "Get "+ ChatColor.AQUA+this.getMatName();
	}
	
	private long getMoneyReward()
	{
		double modifier;
		switch (this.mat)
		{
		case COBBLESTONE:
			modifier=0.1;
		case OAK_PLANKS:
			modifier=1;
		case BREAD:
			modifier=2;
		case CACTUS:
			modifier=1.25;
		case SUGAR_CANE:
			modifier=1.5;
		case IRON_INGOT:
			modifier=5;
		case  DIAMOND:
			modifier=10;
		default:
			modifier=0;
		}
		return (long)Math.ceil(this.qt * modifier);
	}
	
	private long getRespectReward()
	{
		return (long)Math.ceil(this.getMoneyReward()/10);
	}
	
	@Override
	public void reward() {
		try {
			this.f.addMoney(this.getMoneyReward());
			this.f.addRespect(this.getRespectReward());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String getDesc() {
		String r = "Get to your faction "+ ChatColor.YELLOW + this.qt + " " + ChatColor.GREEN+this.getMatName();
		r += ChatColor.DARK_GREEN+"\n\n► +"+Util.getMoney(this.getMoneyReward());
		r += ChatColor.DARK_GREEN+"\n► +"+Util.getRespect(this.getRespectReward());
		return r;
	}

	@Override
	public int getLimit() {
		return 168; // 1 semana
	}

	@Override
	public boolean isCompleted() {
		return left <= 0;
	}

	@Override
	public long getRequiredRespect() {
		return (long)Math.ceil(Math.pow(this.qt-64,1.5D)/500);
	}

	@Override
	public void update() throws SQLException {
		DBManager.performSafeExecute("update misiones set d1=?,d2=?,d3=? where id=?", "iiii", this.conversion.indexOf(this.mat),this.qt,this.left,this.id);
	}

	@Override
	public void delete() throws SQLException {
		DBManager.performExecute("delete from misiones where id="+this.id);
		
	}

	@Override
	public void create() throws SQLException {
		DBManager.performSafeExecute("insert into misiones (faccion,id,tipo,d1,d2,d3) values (?,?,?,?,?,?)","iiiiii",this.f.getID(),this.id,0,this.conversion.indexOf(this.mat),this.qt,this.qt);
	}

}
