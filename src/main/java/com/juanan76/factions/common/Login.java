package com.juanan76.factions.common;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.juanan76.factions.Main;

public class Login implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player)
		{
			if (args.length != 1)
				return false;
			
			if (Main.players.get(sender).isLogged())
				Main.players.get(sender).sendMessage(PluginPart.MAIN, ChatColor.DARK_RED + "You're already logged in!");
			else
			{
				try {
					ResultSet rst = DBManager.performSafeQuery("select * from users where nick=? and passwd=?","ss",sender.getName(),args[0]);
					if (!rst.next())
					{
						Main.players.get(sender).sendMessage(PluginPart.MAIN, ChatColor.DARK_RED + "Invalid password or not yet registered!");
					}
					else
					{
						Main.players.get(sender).login();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
			}
		}
		return true;
	}
	
}
