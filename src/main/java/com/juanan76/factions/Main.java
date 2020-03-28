package com.juanan76.factions;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.juanan76.factions.common.BossInfo;
import com.juanan76.factions.common.DBManager;
import com.juanan76.factions.common.FListeners;
import com.juanan76.factions.common.FPlayer;
import com.juanan76.factions.common.Login;
import com.juanan76.factions.common.Pay;
import com.juanan76.factions.common.Register;
import com.juanan76.factions.common.Sell;
import com.juanan76.factions.common.Util;
import com.juanan76.factions.economy.Trade;
import com.juanan76.factions.factions.Faction;
import com.juanan76.factions.factions.FactionCommand;
import com.juanan76.factions.factions.gens.Generator;
import com.juanan76.factions.npc.NPC;
import com.juanan76.factions.pvp.Home;
import com.juanan76.factions.pvp.PvpListeners;
import com.juanan76.factions.pvp.PvpPlayer;
import com.juanan76.factions.pvp.Sethome;
import com.juanan76.factions.pvp.Spawn;
import com.juanan76.factions.pvp.Tele;
import com.juanan76.factions.pvp.Teleport;

import net.milkbowl.vault.permission.Permission;


public class Main extends JavaPlugin 
{
	
	public static final Map<Player,PvpPlayer> pvp = new HashMap<Player,PvpPlayer>();
	public static final Map<Player,Integer> pvpUpdaters = new HashMap<Player,Integer>();
	public static final Map<Player,FPlayer> players = new HashMap<Player,FPlayer>();
	public static final Map<Integer,Faction> factions = new HashMap<Integer,Faction>();
	public static final Map<Integer,Teleport> teleports = new HashMap<Integer,Teleport>();
	public static final List<NPC> spawnShops = new Vector<NPC>();
	public static final List<Trade> trades = new Vector<Trade>();
	public static final Map<Location,Generator> gens = new HashMap<Location,Generator>();
	public static final BossBar info = Bukkit.createBossBar("", BarColor.PURPLE , BarStyle.SOLID);
	public static final Map<String,String> prefixes = new HashMap<String,String>();
	public static Permission perms = null;
	
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
		this.getCommand("spawn").setExecutor(new Spawn());
		this.getCommand("sethome").setExecutor(new Sethome());
		this.getCommand("home").setExecutor(new Home());
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new BossInfo(), 1, 1);
		
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
			DBManager.performExecute("create table if not exists homes ("
					+ "usuario integer primary key,"
					+ "world integer,"
					+ "x integer,"
					+ "y integer,"
					+ "z integer,"
					+ "foreign key (usuario) references users(id) on delete cascade)");
			DBManager.performExecute("create table if not exists generators ("
					+ "faccion integer,"
					+ "world integer,"
					+ "x integer,"
					+ "y integer,"
					+ "z integer,"
					+ "lvl integer,"
					+ "progress integer,"
					+ "primary key (world,x,y,z),"
					+ "foreign key (faccion) references facciones(id) on delete cascade)");
			// Load factions
			ResultSet rst = DBManager.performQuery("select id from facciones");
			while (rst.next())
				Main.factions.put(rst.getInt(1), new Faction(rst.getInt(1)));
			// Load the 'none faction (id -1)
				Main.factions.put(-1,new Faction(-1));
			
			// Load generators
			rst = DBManager.performQuery("select * from generators");
			while (rst.next())
			{
				Location l = new Location(Util.iconvertWorld(rst.getInt("world")),rst.getInt("x"),rst.getInt("y"),rst.getInt("z"));
				Main.gens.put(l, new Generator(Main.factions.get(rst.getInt("faccion")), l, rst.getInt("lvl"), rst.getInt("progress")));
			}
			
			
			YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/Essentials/config.yml"));
			Set<String> rangos = config.getConfigurationSection("chat.group-formats").getKeys(false);
			
			Pattern formato = Pattern.compile("^(.*) \\{DISPLAYNAME\\}.*");
			for (String r : rangos)
			{
				String str = config.getString("chat.group-formats."+r);
				System.out.println(str);
				Matcher m = formato.matcher(str);
				if (m.matches()) {
					prefixes.put(r, m.group(1));
					System.out.println(m.group(1));
				}
				
				
			}
			setupPermissions();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	private void setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
    }
	
	@Override
	public void onDisable()
	{
		try {
			// Save generators
			for (Generator g : Main.gens.values())
				g.save();
			DBManager.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
