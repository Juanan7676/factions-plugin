package com.juanan76.factions.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import com.juanan76.factions.Main;
import com.juanan76.factions.factions.Plot;

public class PlayerScoreboard {
	
	private Player assoc;
	private Scoreboard scoreboard;
	private Objective dummy;
	private Score fecha;
	private Score espacio1;
	private Score espacio3;
	private Score balance;
	private Score dinero;
	private Score facTitulo;
	private Score fact;
	private Score mapTitulo;
	private Score[] map;
	
	public PlayerScoreboard(Player p)
	{
		this.assoc = p;
		this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		dummy = this.scoreboard.registerNewObjective("panel", "dummy", ChatColor.DARK_PURPLE + ChatColor.BOLD.toString() + "J76-Factions");
		dummy.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		LocalDateTime ldt = LocalDateTime.now();
		DateTimeFormatter frm = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);
		this.fecha = dummy.getScore(ChatColor.GRAY + ChatColor.ITALIC.toString() + frm.format(ldt));
		this.fecha.setScore(15);
		
		this.espacio1 = dummy.getScore("");
		this.espacio1.setScore(14);
		
		this.balance = dummy.getScore(ChatColor.RED+"Balance: ");
		this.balance.setScore(13);
		
		this.dinero = dummy.getScore(Util.getMoney(Main.players.get(assoc).getMoney()));
		this.dinero.setScore(12);
		
		this.facTitulo = dummy.getScore(ChatColor.RED+"Faction: ");
		this.facTitulo.setScore(11);
		
		this.updateFaction();
		
		this.espacio3 = dummy.getScore("  ");
		this.espacio3.setScore(9);
		
		this.mapTitulo = dummy.getScore(ChatColor.RED+"Faction Map:");
		this.mapTitulo.setScore(8);
		
		this.updateMap(p.getLocation().getBlockX()/16,p.getLocation().getBlockZ()/16,Main.players.get(this.assoc).getFaction());
		
		p.setScoreboard(scoreboard);
	}
	
	public void updateBalance(long amt)
	{
		this.scoreboard.resetScores(this.dinero.getEntry());
		this.dinero = this.dummy.getScore(Util.getMoney(amt));
		this.dinero.setScore(12);
	}
	
	public void updateMap(int x, int z, int faction)
	{
		
		ChatColor[][] colors = new ChatColor[7][7];
		for (int ox = -3; ox <= 3; ox++)
		{
			for (int oz = -3; oz <= 3; oz++)
			{
				if (ox==0 && oz==0)
				{
					colors[ox+3][oz+3]=ChatColor.YELLOW;
					continue;
				}
				Plot p = new Plot(x+ox,z+oz,Util.convertWorld(this.assoc.getWorld()));
				if (p.getFaction()==-1)
					colors[ox+3][oz+3]=ChatColor.GRAY;
				else if (p.getFaction() == faction)
					colors[ox+3][oz+3]=ChatColor.GREEN;
				else
				{
					String rel = Main.factions.get(p.getFaction()).getRelation(Main.factions.get(faction));
					if (rel.equalsIgnoreCase("a"))
						colors[ox+3][oz+3]=ChatColor.AQUA;
					else if (rel.equalsIgnoreCase("w"))
						colors[ox+3][oz+3]=ChatColor.DARK_RED;
					else
						colors[ox+3][oz+3]=ChatColor.RED;
				}
			}
		}
		
		String[] lineas = {"","","","","","",""};
		ChatColor[] stuff = {ChatColor.RED,ChatColor.BLACK,ChatColor.GREEN,ChatColor.DARK_GREEN,ChatColor.GOLD,ChatColor.GRAY,ChatColor.WHITE};
		
		for (int k=0; k<=6; k++)
		{
			lineas[k] += stuff[k];
			for (int j=0; j<=6; j++)
				lineas[k] += colors[j][k]+"█";
		}

		for (int k=0; k<=6; k++)
			if (this.map != null) this.scoreboard.resetScores(this.map[k].getEntry());
		
		this.map = new Score[]{dummy.getScore(lineas[0]),dummy.getScore(lineas[1]),dummy.getScore(lineas[2]),dummy.getScore(lineas[3]),dummy.getScore(lineas[4]),dummy.getScore(lineas[5]),dummy.getScore(lineas[6])};
		
		for (int k=0; k<=6; k++)
			this.map[k].setScore(7-k);
	}
	
	public void updateFaction()
	{
		try {
			if (this.fact != null) this.scoreboard.resetScores(this.fact.getEntry());
			this.fact = dummy.getScore(ChatColor.GREEN+Main.factions.get(Main.players.get(this.assoc).getFaction()).getName());
			this.fact.setScore(10);
		} catch (IllegalArgumentException | IllegalStateException e) {
			e.printStackTrace();
		}
	}
}
