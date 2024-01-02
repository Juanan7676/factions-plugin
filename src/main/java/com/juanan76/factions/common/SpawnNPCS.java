package com.juanan76.factions.common;

import java.sql.SQLException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class SpawnNPCS implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof ConsoleCommandSender)
		{
			try {
				Util.spawnNPCS();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

}

