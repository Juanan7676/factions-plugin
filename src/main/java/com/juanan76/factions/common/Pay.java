package com.juanan76.factions.common;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.juanan76.factions.Main;

public class Pay implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player)
		{
			if (!Main.players.get(sender).isLogged())
				return true;
			if (args.length!=2)
				return false;
			try {
				FPlayer payee = FPlayer.fromNick(args[0]);
				if (payee==null)
					Main.players.get(sender).sendMessage(PluginPart.ECONOMY, ChatColor.RED+"User not found or not online!");
				else
				{
					long qty = Long.parseLong(args[1]);
					if (Main.players.get(sender).getMoney() < qty)
						Main.players.get(sender).sendMessage(PluginPart.ECONOMY, ChatColor.RED + "You don't have enough money!");
					if (qty <= 0)
						Main.players.get(sender).sendMessage(PluginPart.ECONOMY, ChatColor.RED + "Invalid amount! Please enter a positive amount distinct from zero.");
					else
					{
						Main.players.get(sender).addMoney(-qty);
						payee.addMoney(qty);
						Main.players.get(sender).sendMessage(PluginPart.ECONOMY, ChatColor.GREEN+"Sucessfully sent "+Util.getMoney(qty)+" to "+ChatColor.YELLOW+args[0]);
						payee.sendMessage(PluginPart.ECONOMY, ChatColor.GREEN+"You received "+Util.getMoney(qty)+" from "+ChatColor.YELLOW+sender.getName());
					}
				}
			} catch (NumberFormatException e)
			{
				return false;
			}
		}
		else if (sender instanceof ConsoleCommandSender) {
			FPlayer payee = FPlayer.fromNick(args[0]);
			long qty = Long.parseLong(args[1]);
			payee.addMoney(qty);
			payee.sendMessage(PluginPart.ECONOMY, ChatColor.GREEN+"You received "+Util.getMoney(qty)+" from "+ChatColor.YELLOW+"Server");
		}
		return true;
	}

}
