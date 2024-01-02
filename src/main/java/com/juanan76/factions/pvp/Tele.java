package com.juanan76.factions.pvp;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.juanan76.factions.Main;
import com.juanan76.factions.common.FPlayer;
import com.juanan76.factions.common.PluginPart;
import com.juanan76.factions.factions.Faction.FactionRelation;

public class Tele implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player)
		{
			if (!Main.players.get(sender).isLogged()) return true;
			if (args.length==0)
				return false;
			
			int user = Integer.parseInt(args[0]);
			FPlayer fuser = FPlayer.fromID(user);
			if (fuser==null)
				Main.players.get(sender).sendMessage(PluginPart.PVP, ChatColor.RED+"User is not online!");
			else if (fuser.getID()==Main.players.get(sender).getID())
				return true;
			else
			{
				FactionRelation rel = fuser.getFactionObject().getRelation(Main.players.get(sender).getFactionObject());
				if (rel == FactionRelation.ALLIANCE || rel == FactionRelation.OWN)
				{
					Teleport t = new TeleportPlayer(fuser,Main.players.get(sender));
					t.start();
				}
				else
					Main.players.get(sender).sendMessage(PluginPart.PVP, ChatColor.RED+"You can only teleport to members of your own faction or friendly ones!");
			}
			try {
				
			} catch (NumberFormatException e)
			{
				Main.players.get(sender).sendMessage(PluginPart.PVP, ChatColor.RED+"Malformed command! Please contact a server admin in case you think this is an error.");
			}
		}
		return true;
	}

}
