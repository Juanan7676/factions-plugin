package com.juanan76.factions.factions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.juanan76.factions.common.tellraw.ClickableComponent;
import com.juanan76.factions.common.tellraw.TextComponent;
import com.juanan76.factions.factions.Faction.FactionRelation;

public class FactionCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player)
		{
			FPlayer fsender = Main.players.get(sender);
			if (!fsender.isLogged()) return true;
			if (args.length==0)
				return false;
			if (args[0].equalsIgnoreCase("c") || args[0].equalsIgnoreCase("create"))
			{ // Create a new faction
				
				if (!Main.perms.has(sender, "j76factions.create"))
				{
					fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You don't have permission to do that!");
					return true;
				}
				if (args.length<4)
				{
					fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Usage: /f c <faction name> <faction shortname> <description>");
					return true;
				}
				
				String desc = "";
				for (int k=3;k<args.length;k++)
					desc += args[k] + " ";
				desc = desc.substring(0,desc.length()-1);
				
				if (args[1].length()<4 || args[1].length() > 15)
					fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Faction names must be 4-15 characters in length!");
				else if (args[2].length()!=3)
					fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Faction shortnames must be exactly 3 characters in length!");
				else if (desc.length()>30)
					fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Faction description can't be longer than 30 characters!");
				else if (fsender.getFaction() != -1)
					fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You're already in a faction! Leave your current one first with /f l or /f leave.");
				else
				{
					long price = FactionUtils.getPrice();
					if (fsender.getMoney() < price)
						fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You don't have enough funds to cover the expenses! Current price to create a faction: "+Util.getMoney(price));
					else
					{
						Plot p = new Plot(fsender.getChunkX(),fsender.getChunkZ(),Util.convertWorld(((Player) sender).getWorld()));
						try {
							if (p.getFaction()!=-1 || (Math.abs(p.getX()) <= 20 && Math.abs(p.getZ()) <= 20 && Util.convertWorld(((Player) sender).getWorld()) == 0))
								fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You can't claim this territory to your new faction since it belongs to another one or is too close to spawn!");
							else
							{ // Everything is set up, create the faction
								int id = FactionUtils.getID();
								DBManager.performSafeExecute("insert into facciones values (?,?,?,0,0,?)", "issi", id,args[1],args[2].toUpperCase(),fsender.getID());
								new Plot(p.getX(),p.getZ(),Util.convertWorld(((Player) sender).getWorld()),args[1],desc,id);
								DBManager.performSafeExecute("insert into miembros values (?,?,-1,0)","ii",fsender.getID(),id);
								fsender.addMoney(-price);
								Main.factions.put(id, new Faction(id));
								fsender.sendMessage(PluginPart.FACTIONS, ChatColor.GREEN+"Faction succesfully created!");
								fsender.updateMap();
								fsender.updateFaction();
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
				
				if (!Main.perms.has(sender, "j76factions.join"))
					return true;
				
				if (args.length!=2)
					fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Usage: /f c <faction>");
				else if (fsender.getFaction() != -1)
					fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You're already in a faction! Leave your current one first with /f l or /f leave.");
				else
				{
					try {
						int f = FactionUtils.id(args[1]);
						if (f==-1)
							fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Faction not found!");
						else
						{
							DBManager.performSafeExecute("insert into peticiones values (?,?,'Can I join your faction?')","ii",f,fsender.getID());
							fsender.sendMessage(PluginPart.FACTIONS, ChatColor.GREEN+"Faction application sent succesfully!");
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
			else if (args[0].equalsIgnoreCase("a") || args[0].equalsIgnoreCase("applications"))
			{ // View application list
				try {
					ResultSet rst = DBManager.performQuery("select * from facciones where lider="+fsender.getID());
					if (!rst.next())
						fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You must be the leader of your faction in order to view the application list!");
					else
					{
						List<Integer> l = fsender.getFactionObject().getApplications();
						if (l.size() == 0)
							fsender.sendMessage(PluginPart.FACTIONS, "Your faction doesn't have any pending applications.");
						else 
						{
							fsender.sendMessage(PluginPart.FACTIONS, ChatColor.GREEN+"This is a list of pending applications to your faction:");
							Util.tellSeparator(fsender);
							int cont = 0;
							for (int playerID : l)
							{
								String playerName = FPlayer.nickfromID(playerID);
								Util.tellRaw(fsender, new TextComponent((++cont)+". ","white",true), new TextComponent(playerName+" ["), 
										new ClickableComponent("ACCEPT","green",true,"Accept this player into faction","/f accept "+playerID),
										new TextComponent("] ["), new ClickableComponent("REJECT","red",true,"Reject this applciation","/f refuse "+playerID),
										new TextComponent("]"));
							}
							Util.tellSeparator(fsender);
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			else if (args[0].equalsIgnoreCase("o")) {
				Faction f = fsender.getFactionObject();
				if (f == null) {
					fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You are not in a faction, thus cannot view your faction info!");
					return true;
				}
				try {
					f.showInfo(fsender);
				} catch (SQLException e) {
					e.printStackTrace();
					fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"An error ocurred while attempting this, contact an admin!");
					return true;
				}
			}
			else if (args[0].equalsIgnoreCase("accept"))
			{
				ResultSet rst;
				try {
					rst = DBManager.performQuery("select * from facciones where lider="+fsender.getID());
					if (!rst.next())
						fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You must be the leader of your faction in order to accept aplications!");
					else if (args.length != 2)
						fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Malformed command (contact server admin if you think this is an error!)");
					else
					{
						int id = Integer.parseInt(args[1]);
						rst = DBManager.performQuery("select * from peticiones where faccion="+fsender.getFaction()+" and usuario="+id);
						if (!rst.next())
							fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Trying to accept a non-existent application!");
						else
						{
							if (fsender.getFactionObject().getNMembers() >= fsender.getFactionObject().getMaxMembers())
								fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Your faction has reached the limit of members it can have (max. "+fsender.getFactionObject().getMaxMembers()+"), increase your faction respect to unlock more member slots.");
							else
							{
								fsender.getFactionObject().addMember(id);
								DBManager.performExecute("delete from peticiones where usuario="+id);
								fsender.sendMessage(PluginPart.FACTIONS, ChatColor.GREEN+"You accepted this user into your faction!");
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
					fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Malformed command (contact server admin if you think this is an error!)");
				}
			}
			else if (args[0].equalsIgnoreCase("refuse"))
			{
				ResultSet rst;
				try {
					rst = DBManager.performQuery("select * from facciones where lider="+fsender.getID());
					if (!rst.next())
						fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You must be the leader of your faction in order to refuse aplications!");
					else if (args.length != 2)
						fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Malformed command (contact server admin if you think this is an error!)");
					else
					{
						int id = Integer.parseInt(args[1]);
						rst = DBManager.performQuery("select * from peticiones where faccion="+fsender.getFaction()+" and usuario="+id);
						if (!rst.next())
							fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Trying to reject a non-existent application!");
						else
						{
							DBManager.performExecute("delete from peticiones where faccion="+fsender.getFaction()+" and usuario="+id);
							fsender.sendMessage(PluginPart.FACTIONS, ChatColor.YELLOW+"You refused this application.");
							if (FPlayer.fromID(id)!=null)
							{
								FPlayer.fromID(id).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Your request to join "+fsender.getFactionObject().getName(fsender.getFactionObject())+" was denied.");
								FPlayer.fromID(id).updateFaction();
							}
						}
					}
					
				} catch (SQLException e) {
					e.printStackTrace();
				}
				catch (NumberFormatException e)
				{
					fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Malformed command (contact server admin if you think this is an error!)");
				}
			}
			else if (args[0].equalsIgnoreCase("list"))
			{
				if (args.length != 2)
				{
					fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Malformed command!");
					return true;
				}
				int f = Integer.parseInt(args[1]);
				ResultSet rst;
				try {
					rst = DBManager.performQuery("select * from facciones where lider="+fsender.getID()+" and id="+f);
						
					boolean kick = rst.next();
					boolean tp = fsender.getFaction()==f || Main.factions.get(f).getRelation(fsender.getFactionObject()) == FactionRelation.ALLIANCE;
						
					fsender.sendMessage(PluginPart.FACTIONS, "Here's a list of the members of your faction: \n");
					rst = DBManager.performQuery("select usuario,rango from miembros where miembros.faccion="+f);
					int cont = 0;
					Util.tellSeparator(fsender);
					while (rst.next())
					{
						String color = (rst.getInt(2)==-1) ? "yellow":"white";
						if (FPlayer.fromID(rst.getInt(1))==null)
						{
							if (!kick || rst.getInt(1)==fsender.getID()) 
								Util.tellRaw(fsender, new TextComponent((++cont)+". ","white",true), new TextComponent(FPlayer.nickfromID(rst.getInt(1))+" ",color,true), new TextComponent("("), new TextComponent("Offline","red"), new TextComponent(")"));
							else
								Util.tellRaw(fsender, new TextComponent((++cont)+". ","white",true), new TextComponent(FPlayer.nickfromID(rst.getInt(1))+" ",color,true), new TextComponent("("), new TextComponent("Offline","red"), new TextComponent(") ["), new ClickableComponent("KICK","red",true,"Kick this player from faction","/f k "+rst.getInt(1)), new TextComponent("]"));
						}
						else
						{
							if (!kick && rst.getInt(1)!=fsender.getID()) 
								Util.tellRaw(fsender, new TextComponent((++cont)+". ","white",true), new TextComponent(FPlayer.nickfromID(rst.getInt(1))+" ",color,true), new TextComponent("("), new TextComponent("Online ","green"), (tp) ? new TextComponent(" [") : null, (tp) ? new ClickableComponent("TP","green",true,"Initiate a teleport to this player","/tele "+rst.getInt(1)) : null, (tp) ? new TextComponent("]") : null, new TextComponent(")"));
							else
								Util.tellRaw(fsender, new TextComponent((++cont)+". ","white",true), new TextComponent(FPlayer.nickfromID(rst.getInt(1))+" ",color,true), new TextComponent("("), new TextComponent("Online ","green"), new TextComponent(" ["), new ClickableComponent("TP","green",true,"Initiate a teleport to this player","/tele "+rst.getInt(1)), new TextComponent("]) ["),new ClickableComponent("KICK","red",true,"Kick this player from faction","/f k "+rst.getInt(1)), new TextComponent("]"));
						}
					}
					Util.tellSeparator(fsender);
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (NumberFormatException e)
				{
					fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Malformed command!");
				}
			}
			else if (args[0].equalsIgnoreCase("wars"))
			{
				if (args.length != 2)
				{
					fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Malformed command!");
					return true;
				}
				Faction f = Main.factions.get(Integer.parseInt(args[1]));
				if (f == null) {
					fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"No such faction!");
					return true;
				}
				fsender.sendMessage(PluginPart.FACTIONS, "Here's a list of active wars of this faction: \n");
				Util.tellSeparator(fsender);
				int cont = 0;
				for (War w : War.fromFaction(f)) {
					Util.tellRaw(fsender, new TextComponent((++cont)+". ", "white",true), new ClickableComponent(w.getFaction1().getRawName()+" vs. "+w.getFaction2().getRawName(),"red",true,"View war info","/f warInfo "+w.getID()));
				}
				Util.tellSeparator(fsender);
			}
			else if (args[0].equalsIgnoreCase("warInfo")) {
				if (args.length != 2) {
					fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Malformed command!");
					return true;
				}
				War w = Main.wars.get(Integer.parseInt(args[1]));
				if (w == null) {
					fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"No such war!");
					return true;
				}
				w.showInfo(Main.players.get(sender));
			}
			else if (args[0].equalsIgnoreCase("k"))
			{
				int f = fsender.getFaction();
				if (args.length!=2)
					fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Malformed command (contact server admin if you think this is an error!)");
				if (f==-1)
					fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You're not in a faction!");
				else
				{
					try {
						int k = Integer.parseInt(args[1]);
						if (k != fsender.getID())
						{
							if (!fsender.getFactionObject().isLeader(fsender.getID()))
								fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You must be the leader of your faction in order to kick someone!");
							else
							{
								fsender.getFactionObject().removeMember(k);
								fsender.sendMessage(PluginPart.FACTIONS, ChatColor.YELLOW+"User successfully kicked.");
								if (FPlayer.fromID(k)!=null)
								{
									FPlayer.fromID(k).sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You were kicked from your faction.");
									FPlayer.fromID(k).updateFaction();
								}
							}
						}
						else
						{
							ResultSet rst = DBManager.performQuery("select * from facciones where lider="+fsender.getID());
							if (!rst.next())
							{
								fsender.getFactionObject().removeMember(fsender.getID());
								fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You sucessfully left your faction.");
								fsender.updateFaction();
							}
							else
							{
								fsender.getFactionObject().removeMember(fsender.getID());
								ResultSet rstt = DBManager.performQuery("select usuario from miembros where faccion="+f+" order by rango desc");
								if (!rstt.next())
								{ // Delete faction (no more players in this faction)
									Main.factions.get(f).delete();
									fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You sucessfully left your faction. Since you were the only member left, the faction was destroyed.");
									fsender.updateFaction();
								}
								else
								{ // Transfer leadership to another member
									int newLeader = rstt.getInt(1);
									Main.factions.get(f).updateLeader(newLeader);
									fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You sucessfully left your faction. Leadership was transferred to another player.");
									fsender.updateFaction();
								}
							}
						}
					} catch (NumberFormatException e)
					{
						fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Malformed command (contact server admin if you think this is an error!)");
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
			else if (args[0].equalsIgnoreCase("info"))
			{
				try {
					if (args.length != 2)
						fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Malformed command (contact server admin if you think this is an error!)");
					else
					{
						int f = Integer.parseInt(args[1]);
						if (f == -1)
							return true;
						
						if (!Main.factions.containsKey(f))
							fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"No such faction!");
						else
						{
							Faction fact = Main.factions.get(f);
							fact.showInfo(fsender);
						}
					}
				} catch (NumberFormatException e)
				{
					fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Malformed command (contact server admin if you think this is an error!)");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			else if (args[0].equalsIgnoreCase("claim"))
			{
				if (fsender.getFaction()==-1)
					fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You're not in a faction!");
				else
				{
					Faction f = fsender.getFactionObject();
					if (!f.isLeader(fsender.getID()))
						fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You must be the leader of the faction to do this!");
					String desc = "";
					for (int k=1; k<args.length;k++)
						desc += args[k];
					
					if (f.isClaimable(fsender.getChunkX(), fsender.getChunkZ(),Util.convertWorld(((Player) sender).getWorld())))
						try {
							f.claimChunk(fsender.getChunkX(), fsender.getChunkZ(),Util.convertWorld(((Player) sender).getWorld()), desc);
							fsender.sendMessage(PluginPart.FACTIONS, ChatColor.GREEN+"Chunk claimed to your faction! Required respect to claim another chunk: "+ChatColor.YELLOW+(f.getNPlots()*f.getNPlots()*f.getNPlots()));
							fsender.updateTerritory();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					else
						fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Could not claim chunk! Is it too close to spawn, is it claimed by another faction or has your faction enough respect? (Required respect to claim another chunk: "+ChatColor.YELLOW+(f.getNPlots()*f.getNPlots()*f.getNPlots())+ChatColor.RED+")");
				}
			}
			else if (args[0].equalsIgnoreCase("unclaim"))
			{
				if (fsender.getFaction()==-1)
					fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You're not in a faction!");
				else
				{
					Faction f = fsender.getFactionObject();
					if (!f.isLeader(fsender.getID()))
						fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You must be the leader of the faction to do this!");
					Plot p = new Plot(fsender.getChunkX(), fsender.getChunkZ(),Util.convertWorld(((Player) sender).getWorld()));
					if (p.getFaction()==f.getID())
						try {
							f.unclaimChunk(p.getX(), p.getZ());
							fsender.updateTerritory();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					else
						fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You can only unclaim chunks that belong to your faction!");
				}
			}
			else if (args[0].equalsIgnoreCase("factions"))
			{
				
				if (!Main.perms.has(sender, "j76factions.list"))
					return true;
				fsender.sendMessage(PluginPart.FACTIONS, "Here's a list with the top 20 factions with more respect on the server:");
				Util.tellSeparator(fsender);
				int cont = 0;
				List<Faction> lista = new ArrayList<Faction>(Main.factions.values());
				Collections.sort(lista, new Comparator<Faction>() {
					@Override
					public int compare(Faction o2, Faction o1) {
						long diff = o1.getRespect() - o2.getRespect();
						if (diff > 0)
							return 1;
						else if (diff == 0)
							return 0;
						else
							return -1;
					}
				});
				for (Faction f : lista)
					if (f.getID()!=-1) {
						FactionRelation x = f.getRelation(fsender.getFactionObject());
						String color;
						switch (x) {
						case WAR:
							color = "dark_red";
							break;
						case OWN:
							color = "green";
							break;
						case ALLIANCE:
							color = "blue";
							break;
						default:
							color = "red";
						}
					Util.tellRaw(fsender, new TextComponent((++cont)+". ","white",true), new ClickableComponent(f.getRawName(),color,true,"View this faction's info","/f info "+f.getID()), new TextComponent(" ("+f.getRespect()+")", color));
					}
				Util.tellSeparator(fsender);
			}
			else if (args[0].equalsIgnoreCase("war"))
			{
				if (args.length != 2)
					fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Malformed command (contact server admin if you think this is an error!)");
				else
				{
					if (fsender.getFaction()==-1)
						fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You're not in a faction!");
					else
					{
						Faction f = fsender.getFactionObject();
						if (!f.isLeader(fsender.getID()))
							fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You must be the leader of your faction to do this!");
						else
						{
							try {
								Faction obj = Main.factions.get(Integer.parseInt(args[1]));
								if (obj == null || obj.getID() == -1)
									fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"No such faction!");
								else {
									if (f.getRelation(obj) == FactionRelation.WAR)
										return true;
									
									f.updateRelation(obj, FactionRelation.WAR);
									obj.updateRelation(f, FactionRelation.WAR);
									
									f.broadcastMessage(ChatColor.RED + ChatColor.BOLD.toString() + "A war has started between us and " + obj.getRawName() + "! You have 12 hours to prepare!");
									obj.broadcastMessage(ChatColor.RED + ChatColor.BOLD.toString() + "A war has started between us and " + f.getRawName() + "! You have 12 hours to prepare!");
									
									War w = new War(f, obj);
									w.save();
									Main.wars.put(w.getID(), w);
									fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+ChatColor.BOLD.toString()+"You declared war to the faction "+obj.getName(fsender.getFactionObject())+"!");
								}
							} catch (NumberFormatException | SQLException e) {
								e.printStackTrace();
								fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"There was an error while doing this!");
							}
						}
					}
				}
			}
			else if (args[0].equalsIgnoreCase("alliance"))
			{
				if (args.length != 2)
					fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Malformed command (contact server admin if you think this is an error!)");
				else
				{
					if (fsender.getFaction()==-1)
						fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You're not in a faction!");
					else
					{
						Faction f = fsender.getFactionObject();
						if (!f.isLeader(fsender.getID()))
							fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You must be the leader of your faction to do this!");
						else
						{
							try {
								Faction obj = Main.factions.get(Integer.parseInt(args[1]));
								if (obj == null || obj.getID() == -1)
									fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"No such faction!");
								else {
									f.updateRelation(obj, FactionRelation.ALLIANCE);
									fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+ChatColor.BLUE.toString()+"You proposed alliance to the faction "+obj.getName(fsender.getFactionObject())+". You will both complete your alliance when they do it back.");
								}
							} catch (NumberFormatException | SQLException e) {
								e.printStackTrace();
								fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"There was an error while doing this!");
							}
						}
					}
				}
				
			}
			else if (args[0].equalsIgnoreCase("neutral"))
			{
				if (args.length != 2)
					fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"Malformed command (contact server admin if you think this is an error!)");
				else
				{
					if (fsender.getFaction()==-1)
						fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You're not in a faction!");
					else
					{
						Faction f = fsender.getFactionObject();
						if (!f.isLeader(fsender.getID()))
							fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"You must be the leader of your faction to do this!");
						else
						{
							try {
								Faction obj = Main.factions.get(Integer.parseInt(args[1]));
								if (obj == null || obj.getID() == -1)
									fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"No such faction!");
								else {
									f.updateRelation(obj, FactionRelation.NEUTRAL);
									if (f.getRelation(obj) == FactionRelation.NEUTRAL) {
										War.fromFactions(f, obj).ifPresent(w -> {
											try {
												w.delete();
											} catch (SQLException e) {
												e.printStackTrace();
											}
										});
									} else
										fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+ChatColor.BLUE.toString()+"You proposed peace to the faction "+obj.getName(fsender.getFactionObject())+". If you were at war, it won't end until there is peace for both parts. If you were allies, you broke it.");
								}
							} catch (NumberFormatException | SQLException e) {
								e.printStackTrace();
								fsender.sendMessage(PluginPart.FACTIONS, ChatColor.RED+"There was an error while doing this!");
							}
						}
					}
				}
			}
			else
				return false;
		}
		return true;
	}

}
