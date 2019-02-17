package com.juanan76.factions.pvp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.juanan76.factions.Main;

public class Spawn implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player)
		{
			if (Main.players.get(sender).isLogged())
			{
				Teleport t = new TeleportLocation(Main.players.get(sender), new Location(Bukkit.getWorld("world"),10,68,-6));
				t.start();
			}
		}
		return true;
	}

}
