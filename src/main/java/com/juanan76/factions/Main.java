package com.juanan76.factions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.juanan76.factions.common.DBManager;
import com.juanan76.factions.common.FListeners;
import com.juanan76.factions.common.FPlayer;
import com.juanan76.factions.common.Login;
import com.juanan76.factions.common.Pay;
import com.juanan76.factions.common.Register;
import com.juanan76.factions.common.Sell;
import com.juanan76.factions.factions.Faction;
import com.juanan76.factions.factions.FactionCommand;
import com.juanan76.factions.npc.NPC;
import com.juanan76.factions.pvp.PvpListeners;
import com.juanan76.factions.pvp.PvpPlayer;
import com.juanan76.factions.pvp.Tele;
import com.juanan76.factions.pvp.Teleport;


public class Main extends JavaPlugin 
{
	
	public static final Map<Player,PvpPlayer> pvp = new HashMap<Player,PvpPlayer>();
	public static final Map<Player,Integer> pvpUpdaters = new HashMap<Player,Integer>();
	public static final Map<Player,FPlayer> players = new HashMap<Player,FPlayer>();
	public static final Map<Integer,Faction> factions = new HashMap<Integer,Faction>();
	public static final Map<Integer,Teleport> teleports = new HashMap<Integer,Teleport>();
	public static final List<NPC> spawnShops = new Vector<NPC>();
	
	@Override
	public void onEnable()
	{
		Bukkit.getPluginManager().registerEvents(new PvpListeners(),this);
		Bukkit.getPluginManager().registerEvents(new FListeners(), this);
		this.getCommand("login").setExecutor(new Login());
		this.getCommand("register").setExecutor(new Register());
		this.getCommand("f").setExecutor(new FactionCommand());
		this.getCommand("tele").setExecutor(new Tele());
		this.getCommand("pay").setExecutor(new Pay());
		this.getCommand("sell").setExecutor(new Sell());
		
		try {
			DBManager.performExecute("pragma foreign_keys=ON");
			DBManager.performExecute("create table if not exists users ("
					+ "id integer primary key not null,"
					+ "nick varchar(50),"
					+ "passwd varchar(50))");
			DBManager.performExecute("create table if not exists dinero ("
					+ "id integer primary key not null,"
					+ "dinero integer,"
					+ "foreign key(id) references users(id) on delete cascade)");
			DBManager.performExecute("create table if not exists facciones ("
					+ "id integer primary key not null,"
					+ "nombre varchar(20),"
					+ "nombrecorto char(3) unique,"
					+ "dinero integer,"
					+ "respeto integer,"
					+ "lider integer,"
					+ "foreign key(lider) references users(id) on delete cascade)");
			DBManager.performExecute("create table if not exists miembros ("
					+ "usuario integer primary key not null,"
					+ "faccion integer,"
					+ "rango integer,"
					+ "dias integer,"
					+ "foreign key (usuario) references users(id) on delete cascade,"
					+ "foreign key (faccion) references facciones(id) on delete cascade)");
			DBManager.performExecute("create table if not exists plots ("
					+ "faccion integer,"
					+ "world integer,"
					+ "x integer,"
					+ "y integer,"
					+ "title varchar(20),"
					+ "desc varchar(50),"
					+ "primary key (x,y),"
					+ "foreign key (faccion) references facciones (id) on delete cascade)");
			DBManager.performExecute("create table if not exists relaciones ("
					+ "faccion1 integer,"
					+ "faccion2 integer check (faccion1 != faccion2),"
					+ "relacion char(1) check (relacion in ('a','w')),"
					+ "primary key (faccion1,faccion2),"
					+ "foreign key (faccion1) references facciones(id) on delete cascade,"
					+ "foreign key (faccion2) references facciones(id) on delete cascade)");
			DBManager.performExecute("create table if not exists peticiones ("
					+ "faccion integer,"
					+ "usuario integer,"
					+ "texto varchar(100),"
					+ "primary key (faccion,usuario),"
					+ "foreign key (faccion) references facciones(id) on delete cascade,"
					+ "foreign key (usuario) references users(id) on delete cascade)");
			DBManager.performExecute("create table if not exists correos ("
					+ "destino integer,"
					+ "remitente integer,"
					+ "texto varchar(100),"
					+ "primary key (destino,remitente),"
					+ "foreign key (destino) references users(id) on delete cascade,"
					+ "foreign key (remitente) references users(id) on delete cascade)");
			DBManager.performExecute("create table if not exists misiones ("
					+ "faccion integer,"
					+ "id integer primary key,"
					+ "tipo integer,"
					+ "d1 integer default null,"
					+ "d2 integer default null,"
					+ "d3 integer default null,"
					+ "d4 integer default null,"
					+ "foreign key (faccion) references facciones(id) on delete cascade)");
			DBManager.performExecute("create table if not exists u_mis ("
					+ "usuario integer primary key,"
					+ "mision integer,"
					+ "foreign key (usuario) references miembros(usuario) on delete cascade,"
					+ "foreign key (mision) references misiones(id) on delete cascade)");
			// Load factions
			ResultSet rst = DBManager.performQuery("select id from facciones");
			while (rst.next())
				Main.factions.put(rst.getInt(1), new Faction(rst.getInt(1)));
			// Load the 'none faction (id -1)
				Main.factions.put(-1,new Faction(-1));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void onDisable()
	{
		try {
			DBManager.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
