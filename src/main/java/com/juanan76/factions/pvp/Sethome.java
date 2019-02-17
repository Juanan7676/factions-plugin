package com.juanan76.factions.pvp;

import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.juanan76.factions.Main;
import com.juanan76.factions.common.PluginPart;

public class Sethome implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player)
		{
			if (Main.players.get(sender).isLogged())
			{
				try {
					Main.players.get(sender).updateHome(((Player) sender).getLocation());
					Main.players.get(sender).sendMessage(PluginPart.PVP, ChatColor.GREEN+"New home set sucessfully!");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}

}
