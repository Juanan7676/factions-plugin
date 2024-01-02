package com.juanan76.factions.common;

import java.sql.SQLException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class SQLExecuter implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof ConsoleCommandSender)
		{
			if (args.length == 0)
				return false;
			try {
				DBManager.performExecute(String.join(" ", args));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

}
