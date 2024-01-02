package com.juanan76.factions.economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.juanan76.factions.Main;
import com.juanan76.factions.common.FPlayer;
import com.juanan76.factions.common.PluginPart;
import com.juanan76.factions.common.Util;
import com.juanan76.factions.common.tellraw.ClickableComponent;
import com.juanan76.factions.common.tellraw.TextComponent;

public class TradeRequest implements CommandExecutor {
	
	private FPlayer source;
	private FPlayer dest;
	
	private int ticksLeft;
	public boolean shouldRemove;
	private int taskID;
	
	public TradeRequest()
	{
		
	}
	
	public TradeRequest(FPlayer source, FPlayer dest)
	{
		this.source = source;
		this.dest = dest;
		this.ticksLeft = 10*20;
		this.shouldRemove = false;
		
		final TradeRequest t = this;
		this.taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(Main.class), new Runnable() {
			@Override
			public void run() {
				t.update();
			}
		}, 1, 1);
	}
	
	public FPlayer getSource()
	{
		return this.source;
	}
	
	public void sendMessage()
	{
		Util.tellRaw(dest, new TextComponent("[","yellow"),
				new ClickableComponent(ChatColor.BOLD+this.source.getFactionObject().getShortName(this.dest.getFactionObject())+ChatColor.RESET+"]","yellow",true,"View this faction's info","/f info "+this.source.getFaction()),
				new TextComponent(source.getPlayer().getName(),"green"), new TextComponent(" te ha enviado una solicitud de trade. [","yellow"),
				new ClickableComponent("Aceptar","green",true,"Comenzar trade","/trade accept"),
				new TextComponent("] [","yellow"),
				new ClickableComponent("Rechazar","red",true,"Rechazar trade","/trade refuse"),
				new TextComponent("]","yellow"));
	}
	
	public FPlayer getDest()
	{
		return this.dest;
	}
	
	public void update()
	{
		this.ticksLeft--;
		if (this.ticksLeft <= 0)
			this.destroy();
	}
	
	public void destroy()
	{
		this.shouldRemove = true;
		Bukkit.getScheduler().cancelTask(this.taskID);
		this.getDest().sendMessage(PluginPart.ECONOMY, ChatColor.RED+"Trade request denied.");
		this.getSource().sendMessage(PluginPart.ECONOMY, ChatColor.RED+"Trade request denied.");
	}
	
	public void accept()
	{
		Trade t = new Trade(this.source,this.dest);
		Main.trades.add(t);
		this.destroy();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!Main.players.get(sender).isLogged())
			return true;
		if (args.length != 1)
			return true;
		
		if (args[0].equalsIgnoreCase("accept"))
		{
			if (Main.traderequests.containsKey(Main.players.get(sender)))
				Main.traderequests.get(Main.players.get(sender)).accept();
		}
		else
		{
			if (Main.traderequests.containsKey(Main.players.get(sender)))
				Main.traderequests.get(Main.players.get(sender)).destroy();
		}
		
		return true;
	}
	
}
