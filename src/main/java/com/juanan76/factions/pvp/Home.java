package com.juanan76.factions.pvp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.juanan76.factions.Main;

public class Home implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player)
		{
			if (Main.players.get(sender).isLogged())
			{
				Teleport t = new TeleportLocation(Main.players.get(sender),Main.players.get(sender).getHome());
				t.start();
			}
		}
		return true;
	}

}
