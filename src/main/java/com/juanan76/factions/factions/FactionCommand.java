package com.juanan76.factions.factions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.juanan76.factions.Main;
import com.juanan76.factions.common.DBManager;
import com.juanan76.factions.common.FPlayer;
import com.juanan76.factions.common.PluginPart;
import com.juanan76.factions.common.Util;
import com.juanan76.factions.common.font.FontUtil;
import com.juanan76.factions.common.tellraw.ClickableComponent;
import com.juanan76.factions.common.tellraw.TextComponent;

public class FactionCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player)
		{
			if (!Main.players.get(sender).isLogged()) return true;
			if (args.length==0)
				return false;
			if (args[0].equalsIgnoreCase("c") || args[0].equalsIgnoreCase("create"))
			{ // Create a new faction
				if (args.length<4)
				{
					Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Usage: /f c <faction name> <faction shortname> <description>");
					return true;
				}
				
				String desc = "";
				for (int k=3;k<args.length;k++)
					desc += args[k] + " ";
				desc = desc.substring(0,desc.length()-1);
				
				if (args[1].length()<4 || args[1].length() > 15)
					Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Faction names must be 4-15 characters in length!");
				else if (args[2].length()!=3)
					Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Faction shortnames must be exactly 3 characters in length!");
				else if (desc.length()>30)
					Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Faction description can't be longer than 30 characters!");
				else if (Main.players.get(sender).getFaction() != -1)
					Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You're already in a faction! Leave your current one first with /f l or /f leave.");
				else
				{
					long price = FactionUtils.getPrice();
					if (Main.players.get(sender).getMoney() < price)
						Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You don't have enough funds to cover the expenses! Current price to create a faction: "+Util.getMoney(price));
					else
					{
						Plot p = new Plot(Main.players.get(sender).getChunkX(),Main.players.get(sender).getChunkZ(),Util.convertWorld(((Player) sender).getWorld()));
						try {
							if (p.getFaction()!=-1 || (Math.abs(p.getX()) <= 20 && Math.abs(p.getZ()) <= 20 && Util.convertWorld(((Player) sender).getWorld()) == 0))
								Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You can't claim this territory to your new faction since it belongs to another one or is too close to spawn!");
							else
							{ // Everything is set up, create the faction
								int id = FactionUtils.getID();
								DBManager.performSafeExecute("insert into facciones values (?,?,?,0,0,?)", "issi", id,args[1],args[2].toUpperCase(),Main.players.get(sender).getID());
								new Plot(p.getX(),p.getZ(),Util.convertWorld(((Player) sender).getWorld()),args[1],desc,id);
								DBManager.performSafeExecute("insert into miembros values (?,?,-1,0)","ii",Main.players.get(sender).getID(),id);
								Main.factions.put(id, new Faction(id));
								Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.GREEN+"Faction succesfully created!");
								Main.players.get(sender).updateMap();
								Main.players.get(sender).updateFaction();
							}
						} catch (SQLException e) {
							e.printStackTrace();
							return true;
						}
					}
				}
			}
			else if (args[0].equalsIgnoreCase("j") || args[0].equalsIgnoreCase("join"))
			{ // Join a faction
				if (args.length!=2)
					Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Usage: /f c <faction>");
				else if (Main.players.get(sender).getFaction() != -1)
					Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You're already in a faction! Leave your current one first with /f l or /f leave.");
				else
				{
					try {
						int f = FactionUtils.id(args[1]);
						if (f==-1)
							Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Faction not found!");
						else
						{
							DBManager.performSafeExecute("insert into peticiones values (?,?,'Can I join your faction?')","ii",f,Main.players.get(sender).getID());
							Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.GREEN+"Faction application sent succesfully!");
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
			else if (args[0].equalsIgnoreCase("a") || args[0].equalsIgnoreCase("applications"))
			{ // View application list
				try {
					ResultSet rst = DBManager.performQuery("select * from facciones where lider="+Main.players.get(sender).getID());
					if (!rst.next())
						Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You must be the leader of your faction in order to view the application list!");
					else
					{
						List<Integer> l = Main.players.get(sender).getFactionObject().getApplications();
						if (l.size() == 0)
							Main.players.get(sender).sendMessage(PluginPart.FACTIONS, "Your faction doesn't have any pending applications.");
						else 
						{
							Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.GREEN+"This is a list of pending applications to your faction:");
							Util.tellSeparator(sender.getName());
							int cont = 0;
							for (int playerID : l)
							{
								String playerName = FPlayer.nickfromID(playerID);
								Util.tellRaw(sender.getName(), new TextComponent((++cont)+". ","white",true), new TextComponent(playerName+" ["), 
										new ClickableComponent("ACCEPT","green",true,"Accept this player into faction","/f accept "+playerID),
										new TextComponent("] ["), new ClickableComponent("REJECT","red",true,"Reject this applciation","/f refuse "+playerID),
										new TextComponent("]"));
							}
							Util.tellSeparator(sender.getName());
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			else if (args[0].equalsIgnoreCase("accept"))
			{
				ResultSet rst;
				try {
					rst = DBManager.performQuery("select * from facciones where lider="+Main.players.get(sender).getID());
					if (!rst.next())
						Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You must be the leader of your faction in order to accept aplications!");
					else if (args.length != 2)
						Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Malformed command (contact server admin if you think this is an error!)");
					else
					{
						int id = Integer.parseInt(args[1]);
						rst = DBManager.performQuery("select * from peticiones where faccion="+Main.players.get(sender).getFaction()+" and usuario="+id);
						if (!rst.next())
							Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Trying to accept a non-existent application!");
						else
						{
							if (Main.players.get(sender).getFactionObject().getNMembers() >= Main.players.get(sender).getFactionObject().getMaxMembers())
								Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Your faction has reached the limit of members it can have (max. "+Main.players.get(sender).getFactionObject().getMaxMembers()+"), increase your faction respect to unlock more member slots.");
							else
							{
								Main.players.get(sender).getFactionObject().addMember(id);
								DBManager.performExecute("delete from peticiones where usuario="+id);
								Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.GREEN+"You accepted this user into your faction!");
								if (FPlayer.fromID(id)!=null)
								{
									FPlayer.fromID(id).sendMessage(PluginPart.FACTIONS, ChatColor.GREEN+"Your request was accepted! You're now inside a faction.");
									FPlayer.fromID(id).updateFaction();
								}
							}
						}
					}
					
				} catch (SQLException e) {
					e.printStackTrace();
				}
				catch (NumberFormatException e)
				{
					Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Malformed command (contact server admin if you think this is an error!)");
				}
			}
			else if (args[0].equalsIgnoreCase("refuse"))
			{
				ResultSet rst;
				try {
					rst = DBManager.performQuery("select * from facciones where lider="+Main.players.get(sender).getID());
					if (!rst.next())
						Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You must be the leader of your faction in order to refuse aplications!");
					else if (args.length != 2)
						Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Malformed command (contact server admin if you think this is an error!)");
					else
					{
						int id = Integer.parseInt(args[1]);
						rst = DBManager.performQuery("select * from peticiones where faccion="+Main.players.get(sender).getFaction()+" and usuario="+id);
						if (!rst.next())
							Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Trying to reject a non-existent application!");
						else
						{
							DBManager.performExecute("delete from peticiones where faccion="+Main.players.get(sender).getFaction()+" and usuario="+id);
							Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.YELLOW+"You refused this application.");
							if (FPlayer.fromID(id)!=null)
							{
								FPlayer.fromID(id).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Your request to join "+Main.players.get(sender).getFactionObject().getName()+" was denied.");
								FPlayer.fromID(id).updateFaction();
							}
						}
					}
					
				} catch (SQLException e) {
					e.printStackTrace();
				}
				catch (NumberFormatException e)
				{
					Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Malformed command (contact server admin if you think this is an error!)");
				}
			}
			else if (args[0].equalsIgnoreCase("list"))
			{
				int f = Main.players.get(sender).getFaction();
				if (f==-1)
					Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You're not in a faction!");
				else
				{
					ResultSet rst;
					try {
						rst = DBManager.performQuery("select * from facciones where lider="+Main.players.get(sender).getID());
						
						boolean kick = rst.next();
						
						Main.players.get(sender).sendMessage(PluginPart.FACTIONS, "Here's a list of the members of your faction: \n");
						rst = DBManager.performQuery("select usuario,rango from miembros where miembros.faccion="+f);
						int cont = 0;
						Util.tellSeparator(sender.getName());
						while (rst.next())
						{
							String color = (rst.getInt(2)==-1) ? "yellow":"white";
							if (FPlayer.fromID(rst.getInt(1))==null)
							{
								if (!kick || rst.getInt(1)==Main.players.get(sender).getID()) 
									Util.tellRaw(sender.getName(), new TextComponent((++cont)+". ","white",true), new TextComponent(FPlayer.nickfromID(rst.getInt(1))+" ",color,true), new TextComponent("("), new TextComponent("Offline","red"), new TextComponent(")"));
								else
									Util.tellRaw(sender.getName(), new TextComponent((++cont)+". ","white",true), new TextComponent(FPlayer.nickfromID(rst.getInt(1))+" ",color,true), new TextComponent("("), new TextComponent("Offline","red"), new TextComponent(") ["), new ClickableComponent("KICK","red",true,"Kick this player from faction","/f k "+rst.getInt(1)), new TextComponent("]"));
							}
							else
							{
								if (!kick && rst.getInt(1)!=Main.players.get(sender).getID()) 
									Util.tellRaw(sender.getName(), new TextComponent((++cont)+". ","white",true), new TextComponent(FPlayer.nickfromID(rst.getInt(1))+" ",color,true), new TextComponent("("), new TextComponent("Online ","green"), new TextComponent(" ["), new ClickableComponent("TP","green",true,"Initiate a teleport to this player","/tele "+rst.getInt(1)), new TextComponent("])"));
								else
									Util.tellRaw(sender.getName(), new TextComponent((++cont)+". ","white",true), new TextComponent(FPlayer.nickfromID(rst.getInt(1))+" ",color,true), new TextComponent("("), new TextComponent("Online ","green"), new TextComponent(" ["), new ClickableComponent("TP","green",true,"Initiate a teleport to this player","/tele "+rst.getInt(1)), new TextComponent("]) ["),new ClickableComponent("KICK","red",true,"Kick this player from faction","/f k "+rst.getInt(1)), new TextComponent("]"));
							}
								
						}
						Util.tellSeparator(sender.getName());
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
			else if (args[0].equalsIgnoreCase("k"))
			{
				int f = Main.players.get(sender).getFaction();
				if (args.length!=2)
					Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Malformed command (contact server admin if you think this is an error!)");
				if (f==-1)
					Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You're not in a faction!");
				else
				{
					try {
						int k = Integer.parseInt(args[1]);
						if (k != Main.players.get(sender).getID())
						{
							ResultSet rst = DBManager.performQuery("select * from facciones where lider="+Main.players.get(sender).getID());
							if (!rst.next())
								Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You must be the leader of your faction in order to kick someone!");
							else
							{
								Main.players.get(sender).getFactionObject().removeMember(k);
								Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.YELLOW+"User successfully kicked.");
								if (FPlayer.fromID(k)!=null)
								{
									FPlayer.fromID(k).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You were kicked from your faction.");
									FPlayer.fromID(k).updateFaction();
								}
							}
						}
						else
						{
							ResultSet rst = DBManager.performQuery("select * from facciones where lider="+Main.players.get(sender).getID());
							if (!rst.next())
							{
								Main.players.get(sender).getFactionObject().removeMember(Main.players.get(sender).getID());
								Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You sucessfully left your faction.");
								Main.players.get(sender).updateFaction();
							}
							else
							{
								Main.players.get(sender).getFactionObject().removeMember(Main.players.get(sender).getID());
								ResultSet rstt = DBManager.performQuery("select usuario from miembros where faccion="+f+" order by rango desc");
								if (!rstt.next())
								{ // Delete faction (no more players in this faction)
									Main.factions.get(f).delete();
									Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You sucessfully left your faction. Since you were the only member left, the faction was destroyed.");
									Main.players.get(sender).updateFaction();
								}
								else
								{ // Transfer leadership to another member
									int newLeader = rstt.getInt(1);
									Main.factions.get(f).updateLeader(newLeader);
									Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You sucessfully left your faction. Leadership was transferred to another player.");
									Main.players.get(sender).updateFaction();
								}
							}
						}
					} catch (NumberFormatException e)
					{
						Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Malformed command (contact server admin if you think this is an error!)");
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
			else if (args[0].equalsIgnoreCase("info"))
			{
				try {
					if (args.length != 2)
						Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Malformed command (contact server admin if you think this is an error!)");
					else
					{
						int f = Integer.parseInt(args[1]);
						if (f == -1)
							return true;
						
						if (!Main.factions.containsKey(f))
							Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"No such faction!");
						else
						{
							Faction fact = Main.factions.get(f);
							String user = sender.getName();
							Util.tellSeparator(user);
							Util.tellRaw(user, new TextComponent(FontUtil.getCenteredMessage("Faction &2"+fact.getRawName())));
							Util.tellRaw(user, new TextComponent(FontUtil.getCenteredMessage("&eMembers: &b"+fact.getNMembers()+"/"+fact.getMaxMembers()+"    &aRespect: "+ChatColor.stripColor(Util.getRespect(fact.getRespect())))));
							Util.tellRaw(user, new TextComponent("        "));
							Util.tellRaw(user, new TextComponent("► Leader: ","red"), new TextComponent(FPlayer.nickfromID(fact.getLeader()),"aqua"));
							if (!fact.isMember(Main.players.get(sender).getID()))
								Util.tellRaw(user, new TextComponent("► ["),new ClickableComponent("APPLY","green",true,"Send an application to this faction","/f j "+fact.getRawName()),
										new TextComponent("]"));
							Util.tellSeparator(user);
						}
					}
				} catch (NumberFormatException e)
				{
					Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Malformed command (contact server admin if you think this is an error!)");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			else if (args[0].equalsIgnoreCase("claim"))
			{
				if (Main.players.get(sender).getFaction()==-1)
					Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You're not in a faction!");
				else
				{
					Faction f = Main.players.get(sender).getFactionObject();
					
					String desc = "";
					for (int k=1; k<args.length;k++)
						desc += args[k];
					
					if (f.isClaimable(Main.players.get(sender).getChunkX(), Main.players.get(sender).getChunkZ(),Util.convertWorld(((Player) sender).getWorld())))
						try {
							f.claimChunk(Main.players.get(sender).getChunkX(), Main.players.get(sender).getChunkZ(),Util.convertWorld(((Player) sender).getWorld()), desc);
							Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.GREEN+"Chunk claimed to your faction! Required respect to claim another chunk: "+ChatColor.YELLOW+(f.getNPlots()*f.getNPlots()*f.getNPlots()*f.getNPlots()));
							Main.players.get(sender).updateTerritory();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					else
						Main.players.get(sender).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Could not claim chunk! Is it too close to spawn, is it claimed by another faction or has your faction enough respect? (Required respect to claim another chunk: "+ChatColor.YELLOW+(f.getNPlots()*f.getNPlots()*f.getNPlots()*f.getNPlots())+ChatColor.RED+")");
				}
			}
			else if (args[0].equalsIgnoreCase("factions"))
			{
				Main.players.get(sender).sendMessage(PluginPart.FACTIONS, "Here's a list with the top 20 factions with more respect on the server:");
				Util.tellSeparator(sender.getName());
				int cont = 0;
				for (Faction f : Main.factions.values())
					Util.tellRaw(sender.getName(), new TextComponent((++cont)+". ","white",true), new ClickableComponent(f.getRawName(),"yellow",false,"View this faction's info","/f info "+f.getID()));
				Util.tellSeparator(sender.getName());
			}
			else
				return false;
		}
		return true;
	}

}
