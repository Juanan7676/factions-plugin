package com.juanan76.factions.pvp;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.juanan76.factions.Main;
import com.juanan76.factions.common.PluginPart;

public class PvpPlayer {

	boolean onFight;
	long ticksleft;
	Player assoc;
	
	public PvpPlayer(Player p)
	{
		this.assoc = p;
		this.onFight = false;
		this.ticksleft = 0;
	}
	
	public void engageFight()
	{
		this.ticksleft = 1200;
		if (!this.onFight) Main.players.get(assoc).sendMessage(PluginPart.PVP, ChatColor.RED+"You're now in combat! You may not log out, or face the consequences.");
		this.onFight = true;
	}
	
	public void disengageFight()
	{
		this.onFight = false;
		if (this.assoc.getHealth()>0) Main.players.get(assoc).sendMessage(PluginPart.PVP, ChatColor.GREEN+"You're no longer in combat! You may now log out.");
	}
	
	public void update()
	{ // Please, call this method every server tick
		if (this.ticksleft > 0)
		{
			this.ticksleft--;
			if (this.ticksleft == 0)
				this.disengageFight();
		}
	}
}
