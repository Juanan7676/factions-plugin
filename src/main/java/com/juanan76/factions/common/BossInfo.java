package com.juanan76.factions.common;

import com.juanan76.factions.Main;

import net.md_5.bungee.api.ChatColor;

public class BossInfo implements Runnable {
	
	public final String info = "    [J76 FACTIONS PUBLIC BETA v0.2] NOW TRADES ARE AVAILABLE!";
	
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
			Main.info.setTitle(ChatColor.AQUA+ChatColor.BOLD.toString()+t);
		}
	}

}
