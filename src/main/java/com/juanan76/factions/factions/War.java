package com.juanan76.factions.factions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;

import com.juanan76.factions.Main;
import com.juanan76.factions.common.DBManager;
import com.juanan76.factions.common.FPlayer;
import com.juanan76.factions.common.Util;
import com.juanan76.factions.common.font.FontUtil;
import com.juanan76.factions.common.tellraw.TextComponent;
import com.juanan76.factions.factions.Faction.FactionRelation;

public class War {
	public static final int WAR_START_DURATION = 12*3600*20;
	public static final int WAR_DURATION = 24*3600*20;
	
	public static enum WarStatus {
		STARTING(0),
		IN_PROGRESS(1);
		private int sqlRep;
		private WarStatus(int r) {
			this.sqlRep = r;
		}
		public int toInt() {
			return this.sqlRep;
		}
		public static WarStatus fromInt(int r) {
			return Arrays.stream(WarStatus.values()).filter(x -> x.toInt() == r).findFirst().get();
		}
	}
	
	private Faction f1;
	private Faction f2;
	private int id;
	private WarStatus status;
	private int casualties1;
	private int casualties2;
	private int territory_lost1;
	private int territory_lost2;
	private int ticksToTransition;
	
	public static Optional<War> fromFactions(Faction f1, Faction f2) {
		return Main.wars.values().stream().filter(w -> (
					(w.f1.getID()==f1.getID() && w.f2.getID()==f2.getID()) ||
					(w.f1.getID() == f2.getID() && w.f2.getID() == f1.getID())
				)).findFirst();
	}
	
	public static War[] fromFaction(Faction f) {
		return Main.wars.values().stream().filter(w -> w.f1.getID() == f.getID() || w.f2.getID() == f.getID()).toArray(War[]::new);
	}
	
	public War(Faction aggressor, Faction defensor) {
		this.f1 = aggressor;
		this.f2 = defensor;
		this.status = WarStatus.STARTING;
		this.ticksToTransition = WAR_START_DURATION;
		this.casualties1 = 0;
		this.casualties2 = 0;
		this.territory_lost1 = 0;
		this.territory_lost2 = 0;
		this.id = -1;
	}
	
	public War(int id) throws SQLException {
		ResultSet rst = DBManager.performQuery("select * from wars where id="+id);
		if (!rst.next()) throw new SQLException("No such war!");
		this.id = id;
		this.f1 = Main.factions.get(rst.getInt("faction1"));
		this.f2 = Main.factions.get(rst.getInt("faction2"));
		this.status = WarStatus.fromInt(rst.getInt("status"));
		this.casualties1 = rst.getInt("casualties1");
		this.casualties2 = rst.getInt("casualties2");
		this.territory_lost1 = rst.getInt("territory_lost_1");
		this.territory_lost2 = rst.getInt("territory_lost_2");
	}
	
	
	public void save() throws SQLException {
		ResultSet rst = DBManager.performQuery("select 1 from wars where id="+this.id);
		if (!rst.next()) {
			this.id = Util.getNextID("wars","id");
			DBManager.performSafeExecute(
					"insert into wars (id,faction1,faction2,status,ticksToTransition,casualties1,casualties2,territory_lost_1,territory_lost_2) values (?,?,?,?,?,?,?,?,?)",
					"iiiiiiii",
					this.id,
					this.f1.getID(),
					this.f2.getID(),
					this.status.toInt(),
					this.ticksToTransition,
					this.casualties1,
					this.casualties2,
					this.territory_lost1,
					this.territory_lost2);
		}
		else
			DBManager.performSafeExecute("update wars set faction1=?, faction2=?, status=?, ticksToTransition=?, casualties1=?, casualties2=?, territory_lost_1=?, territory_lost_2=? where id=?",
					"iiiiiiii",
					this.f1.getID(),
					this.f2.getID(),
					this.status.toInt(),
					this.ticksToTransition,
					this.casualties1,
					this.casualties2,
					this.territory_lost1,
					this.territory_lost2,
					this.id);
	}
	
	public void update() throws SQLException {
		this.ticksToTransition--;
		if (this.ticksToTransition <= 0) {
			if (this.status == WarStatus.STARTING) {
				this.status = WarStatus.IN_PROGRESS;
				this.ticksToTransition = War.WAR_DURATION;
				this.save();
				this.f1.broadcastMessage("The war with the faction " + this.f2.getRawName() + " has started!");
				this.f2.broadcastMessage("The war with the faction " + this.f1.getRawName() + " has started!");
			}
			else {
				this.f1.updateRelation(this.f2, FactionRelation.NEUTRAL);
				this.f2.updateRelation(this.f1, FactionRelation.NEUTRAL);
				this.delete();
			}
		} else if (this.ticksToTransition%600 == 0)
			this.save();
	}
	
	public void delete() throws SQLException {
		this.f1.broadcastMessage("The war with the faction " + this.f2.getRawName() + " has ended! Our relation with them has been set to neutral.");
		this.f2.broadcastMessage("The war with the faction " + this.f1.getRawName() + " has ended! Our relation with them has been set to neutral.");
		DBManager.performExecute("delete from wars where id="+this.id);
		Main.wars.remove(this.id);
	}
	
	public int getID() {
		return this.id;
	}
	
	public Faction getFaction1() {
		return this.f1;
	}
	
	public Faction getFaction2() {
		return this.f2;
	}
	
	public void showInfo(FPlayer s) {
		Util.tellSeparator(s);
		Util.tellRaw(s, new TextComponent(FontUtil.getCenteredMessage("War &4"+this.f1.getRawName()+" &fvs. &2"+this.f2.getRawName())));
		Util.tellRaw(s, new TextComponent(""));
		Util.tellRaw(s,
				new TextComponent("► Casualties: ","gold"),
				new TextComponent(this.casualties1 + "","dark_red"),
				new TextComponent(" / ","white"),
				new TextComponent(this.casualties2 + "","dark_green")
				);
		Util.tellRaw(s,
				new TextComponent("► Territory lost: ","gold"),
				new TextComponent(this.territory_lost1 + "","dark_red"),
				new TextComponent(" / ","white"),
				new TextComponent(this.territory_lost1 + "","dark_green")
				);
		Util.tellRaw(s, new TextComponent(""));
		Util.tellRaw(s, new TextComponent("► Status: ","gold"), new TextComponent((this.status == WarStatus.IN_PROGRESS) ? "IN PROGRESS" : "STARTING", "white", true));
		Util.tellRaw(s, new TextComponent("► Time until " + ((this.status == WarStatus.IN_PROGRESS) ? "war ends: " : "war starts: "),"gold"), new TextComponent(Util.readableTimeDiff(this.ticksToTransition/20*1000), "white", true));
		Util.tellSeparator(s);
	}
	
	public void addCasualty(Faction f) {
		if (f.getID() == this.f1.getID())
			this.casualties1++;
		else if (f.getID() == this.f2.getID())
			this.casualties2++;
	}
}
