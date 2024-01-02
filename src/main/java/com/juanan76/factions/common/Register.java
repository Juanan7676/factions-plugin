package com.juanan76.factions.common;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.juanan76.factions.Main;

public class Register implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player)
		{
			if (args.length != 1)
				return false;
			if (args[0].length() < 4 || args[0].length() > 20)
			{
				Main.players.get(sender).sendMessage(PluginPart.MAIN, ChatColor.RED + "Password must be between 4-20 characters in length!");
			}
			else if (Main.players.get(sender).isLogged())
			{
				Main.players.get(sender).sendMessage(PluginPart.MAIN, ChatColor.RED + "You're already logged in!");
			}
			else
			{
				try {
					ResultSet rst = DBManager.performSafeQuery("select * from users where nick=?","s",sender.getName());
					if (rst.next())
					{
						Main.players.get(sender).sendMessage(PluginPart.MAIN, ChatColor.RED + "You're already registered! Please login with /login <password>");
					}
					else
					{
						int id = Util.getNextID("users","id");
						DBManager.performSafeExecute("insert into users values (?,?,?)","iss",id,sender.getName(),args[0]);
						DBManager.performSafeExecute("insert into dinero values (?,?)","ii",id,0);
						Main.players.get(sender).sendMessage(PluginPart.MAIN, ChatColor.GREEN + "You're now registered to the server!");
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
