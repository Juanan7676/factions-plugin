package com.juanan76.factions.pvp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;

import com.juanan76.factions.Main;
import com.juanan76.factions.common.FPlayer;
import com.juanan76.factions.common.PluginPart;
import com.juanan76.factions.common.Util;
import com.juanan76.factions.common.tellraw.TextComponent;

public class Teleport {
	private FPlayer to;
	private FPlayer teleported;
	private int remainingTicks;
	private long cost;
	private long rawCost;
	private int rawTime;
	private double distance;
	private boolean amigo;
	private boolean wilderness;
	private boolean started;
	private int taskID;
	private int id;
	
	public Teleport(FPlayer to, FPlayer teleported)
	{
		this.to = to;
		this.teleported = teleported;
		
		if (!to.getPlayer().getLocation().getWorld().equals(teleported.getPlayer().getLocation().getWorld()))
			this.distance = 100000000;
		else
			this.distance = to.getPlayer().getLocation().distanceSquared(teleported.getPlayer().getLocation());
		
		this.cost = (long)Math.ceil(0.0001*this.distance);
		this.rawCost = this.cost;
		this.remainingTicks = 200;
		this.rawTime = this.remainingTicks;
		this.amigo = false;
		this.wilderness = false;
		if (to.getFactionObject().getRelation(teleported.getFactionObject()).equalsIgnoreCase("a"))
		{ // Teleporting to friendly faction: cost,time x 2
			this.cost *= 2;
			this.remainingTicks *= 2;
			this.amigo = true;
		}
		if (teleported.getCurrTerritory()==-1)
		{ // Teleporting from wilderness: time x 3
			this.remainingTicks *= 3;
			this.wilderness = true;
		}
		this.started = false;
		this.taskID = 0;
	}

	public void start()
	{
		this.started = true;
		this.teleported.sendMessage(PluginPart.PVP, ChatColor.GREEN+"Initiating teleport:");
		Util.tellSeparator(this.teleported.getPlayer().getName());
		Util.tellRaw(this.teleported.getPlayer().getName(), new TextComponent("► Teleporting to: ","yellow"), new TextComponent(this.to.getPlayer().getName(),"green"));
		Util.tellRaw(this.teleported.getPlayer().getName(), new TextComponent(" "));
		Util.tellRaw(this.teleported.getPlayer().getName(), new TextComponent("► Cost: ","red"), new TextComponent(String.format("%.2f", Math.sqrt(this.distance))+" blocks = ","yellow"),
				new TextComponent(Util.getMoney(this.rawCost)));
		Util.tellRaw(this.teleported.getPlayer().getName(), new TextComponent("► Modifiers: ","red"));
		int time = this.rawTime;
		if (this.amigo)
		{
			Util.tellRaw(this.teleported.getPlayer().getName(), new TextComponent("    ► Teleporting to friendly faction: ","blue"), new TextComponent(Util.getMoney(this.rawCost)), new TextComponent(" x 2 = ","red"),
					new TextComponent(Util.getMoney(this.rawCost*2)), new TextComponent(" "+time+"s x 2 = ","red"), new TextComponent(Integer.toString(time*2)+"s","red"));
			time *= 2;
		}
		if (this.wilderness)
		Util.tellRaw(this.teleported.getPlayer().getName(), new TextComponent("    ► Teleporting from wilderness: ","blue"), new TextComponent(" "+time/20+"s x 3 = ","red"), new TextComponent(Integer.toString(time*3/20)+"s","red"));
		
		Util.tellRaw(this.teleported.getPlayer().getName(), new TextComponent(" "));
		Util.tellRaw(this.teleported.getPlayer().getName(), new TextComponent("► Final cost: ","dark_red"), new TextComponent(Util.getMoney(this.cost)));
		Util.tellRaw(this.teleported.getPlayer().getName(), new TextComponent("► Final time: ","dark_red"), new TextComponent(this.remainingTicks/20+"s","yellow"));
		Util.tellSeparator(this.teleported.getPlayer().getName());
		if (this.teleported.getMoney() < this.cost)
			this.teleported.sendMessage(PluginPart.PVP, ChatColor.RED+"Teleport failed: you don't have enough money");
		else
		{
			this.started = true;
			this.to.sendMessage(PluginPart.PVP, ChatColor.AQUA+"Warning! A friendly player is teleporting to your location..."+ChatColor.YELLOW+" (teleport time: "+this.remainingTicks/20+"s)");
			this.id = Util.getTeleID();
			final int id = this.id;
			Main.teleports.put(this.id,this);
			this.taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(Main.class), new Runnable() {
				@Override
				public void run() {
					Main.teleports.get(id).update();
				}
				
			}, 1, 1);
		}
	}
	
	public void update()
	{
		if (this.started)
		{
			if (this.remainingTicks <= 0)
			{
				this.started = false;
				this.finish();
			}
			this.remainingTicks--;
			this.teleported.getPlayer().spawnParticle(Particle.PORTAL, this.teleported.getPlayer().getLocation(), 2);
		}
	}
	
	public void finish()
	{
		this.started = false;
		this.teleported.addMoney(-this.cost);
		this.teleported.getPlayer().teleport(this.to.getPlayer().getLocation());
		this.teleported.getPlayer().playSound(this.teleported.getPlayer().getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1.0F, 1.0F);
		Bukkit.getScheduler().cancelTask(this.taskID);
		Main.teleports.remove(this.id);
	}
	
	public void cancel()
	{
		this.started = false;
		this.teleported.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Teleport cancelled.");
		this.to.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Teleport cancelled.");
		Bukkit.getScheduler().cancelTask(this.taskID);
		Main.teleports.remove(this.id);
	}
	
	public FPlayer getTo()
	{
		return this.to;
	}
	
	public FPlayer getTeleported()
	{
		return this.teleported;
	}
}
