package com.juanan76.factions.common;

import java.sql.SQLException;
import java.util.Iterator;

import com.juanan76.factions.Main;
import com.juanan76.factions.economy.TradeRequest;
import com.juanan76.factions.factions.War;

import net.md_5.bungee.api.ChatColor;

public class BossInfo implements Runnable {
	
	public final String info = "    [FACTIONS SERVER v0.4] SUERTE AHI AFUERA!";
	
	private int currOffset = 0;
	private int tickCount = 0;
	
	@Override
	public void run() {
		if (++tickCount%10==0)
		{
			int l = info.length();
			currOffset++;
			if (currOffset == l)
				currOffset = 0;
			String t;
			
			if (26+currOffset < l)
				t = info.substring(currOffset,26+currOffset);
			else
				t = info.substring(currOffset,l)+info.substring(0,26-l+currOffset);
			Main.info.setTitle(ChatColor.GOLD+ChatColor.BOLD.toString()+t);
		}
		
		Iterator<TradeRequest> t = Main.traderequests.values().iterator();
		while (t.hasNext())
		{
			TradeRequest tr = t.next();
			if (tr.shouldRemove)
				t.remove();
		}
		
		Iterator<War> it = Main.wars.values().iterator();
		while (it.hasNext()) {
			War w = it.next();
			try {
				w.update();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
