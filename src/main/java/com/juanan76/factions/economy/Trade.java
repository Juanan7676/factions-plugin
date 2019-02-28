package com.juanan76.factions.economy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.juanan76.factions.Main;
import com.juanan76.factions.common.FPlayer;
import com.juanan76.factions.common.PluginPart;
import com.juanan76.factions.common.Util;
import com.juanan76.factions.common.menu.ItemDummy;
import com.juanan76.factions.common.menu.ItemExitMenu;
import com.juanan76.factions.common.menu.ItemSwapMenu;
import com.juanan76.factions.common.menu.Menu;
import com.juanan76.factions.common.menu.MenuItem;


public class Trade {
	private FPlayer p1;
	private FPlayer p2;
	
	private long money1;
	private long money2;
	
	private List<ItemStack> items1;
	private List<ItemStack> items2;
	
	private boolean ready1;
	private boolean ready2;
	
	private int tickstoComplete;
	
	private boolean complete1;
	private boolean complete2;
	
	private static class MenuMain extends Menu {
		
		private Trade t;
		
		public MenuMain(FPlayer viewer, Trade t) {
			super(viewer, 54);
			this.t = t;
		}

		@Override
		public void initContents() {
			super.contents = new HashMap<Integer,MenuItem>();
			
			// Owner money in trade
			ItemStack om = new ItemStack(Material.GREEN_WOOL,1);
			ItemMeta it = om.getItemMeta();
			it.setDisplayName(ChatColor.DARK_GREEN+ChatColor.BOLD.toString()+"Money in trade:");
			it.setLore(Arrays.asList(new String[] {"",Util.getMoney(this.t.money1)}));
			super.contents.put(0,new ItemDummy(this,om));
			
			// Trader money in trade
			ItemStack tm = new ItemStack(Material.GREEN_WOOL,1);
			it = tm.getItemMeta();
			it.setDisplayName(ChatColor.DARK_GREEN+ChatColor.BOLD.toString()+"Money in trade:");
			it.setLore(Arrays.asList(new String[] {"",Util.getMoney(this.t.money2)}));
			super.contents.put(8,new ItemDummy(this,tm));
			
			// Aesthetics
			for (int k = 36; k < 48; k++)
				super.contents.put(k,new ItemDummy(this,new ItemStack(Material.LIME_STAINED_GLASS_PANE,1)));
			for (int k = 51; k <= 54; k++)
				super.contents.put(k,new ItemDummy(this,new ItemStack(Material.LIME_STAINED_GLASS_PANE,1)));
			super.contents.put(49,new ItemDummy(this, new ItemStack(Material.LIME_STAINED_GLASS_PANE,1)));
			
			super.contents.put(50, new ItemExitMenu(this,new ItemStack(Material.BARRIER,1)));
			super.contents.put(48, new ItemSwapMenu(this,new ItemStack(Material.FERN,1),new MenuMainAccepting(viewer,this.t)));
			
			super.composeInv();
		}
		
		public void updateContents()
		{
			List<ItemStack> items1 = (this.viewer.equals(this.t.p1)) ? this.t.items1 : this.t.items2;
			List<ItemStack> items2 = (this.viewer.equals(this.t.p1)) ? this.t.items2 : this.t.items1;
			
			int counter = 0;
			for (ItemStack i : items1)
			{
				counter++;
			}
		}
		
		@Override
		public void onSwap(Menu another)
		{
			if (another instanceof MenuMainAccepting)
				this.t.setReady(this.viewer, true);
		}
		
		@Override
		public void onClose() {
			this.t.closeTrade(true);
		}
	};
	
	private static class MenuMainAccepting extends MenuMain
	{
		public MenuMainAccepting(FPlayer viewer, Trade t) {
			super(viewer, t);
		}
		
		@Override
		public void initContents()
		{
			super.initContents();
			super.contents.put(48, new ItemSwapMenu(this,new ItemStack(Material.BARRIER,1),new MenuMain(viewer,super.t)));
			super.contents.put(50, new ItemDummy(this, new ItemStack(Material.LIME_STAINED_GLASS_PANE,1)));
		}
		
		@Override
		public void onSwap(Menu m)
		{
			if (m instanceof MenuMain)
			{
				super.t.setReady(super.viewer,false);
			}
		}
	}
	
	private static class MenuWaitComplete extends MenuMain
	{
		
		private int taskID;
		private int ticksToComplete;
		
		public MenuWaitComplete(FPlayer viewer, Trade t, int taskID) {
			super(viewer, t);
			this.ticksToComplete = 100;
			this.taskID = taskID;
		}
		
		public void updateCountdown()
		{
			ItemStack it = new ItemStack(Material.PAPER,(int)Math.ceil(this.ticksToComplete/20.0));
			ItemMeta i = it.getItemMeta();
			i.setDisplayName(ChatColor.GREEN+"Check everything is OK!");
			it.setItemMeta(i);
			super.contents.put(50, new ItemDummy(this,it));
			super.composeInv();
		}
		
		@Override
		public void initContents()
		{
			super.initContents();
			super.contents.put(48, new ItemDummy(this, new ItemStack(Material.LIME_STAINED_GLASS_PANE,1)));
			this.updateCountdown();
		}
		
		@Override
		public void onClose()
		{
			super.onClose();
			Bukkit.getScheduler().cancelTask(this.taskID);
		}
		
		@Override
		public void onSwap(Menu another)
		{
			if (another instanceof MenuFinalComplete)
				Bukkit.getScheduler().cancelTask(this.taskID);
		}
		
		public void updateTicks()
		{
			this.ticksToComplete--;
			this.updateCountdown();
		}
	}
	
	private static class MenuFinalComplete extends MenuMain
	{

		public MenuFinalComplete(FPlayer viewer, Trade t) {
			super(viewer, t);
		}
		
		@Override
		public void initContents()
		{
			super.initContents();
			super.contents.put(48, new ItemDummy(this, new ItemStack(Material.LIME_STAINED_GLASS_PANE,1)));
			super.contents.put(48, new ItemSwapMenu(this, new ItemStack(Material.FERN,1),null));
		}
		
		@Override
		public void onSwap(Menu m)
		{
			if (m==null)
				super.t.setComplete(super.viewer);
		}
		
	}
	
	public Trade(FPlayer p1, FPlayer p2)
	{
		this.p1 = p1;
		this.p2 = p2;
		this.money1=0;
		this.money2=0;
		this.items1 = new Vector<ItemStack>();
		this.items2 = new Vector<ItemStack>();
		this.ready1 = false;
		this.ready2 = false;
		
	}
	
	public List<ItemStack> getItemsTraded(FPlayer p)
	{
		if (p1.getID()==p.getID())
			return this.items1;
		else if(p2.getID()==p.getID())
			return this.items2;
		else
			return null;
	}
	
	public long getMoneyTraded(FPlayer p)
	{
		if (p1.getID()==p.getID())
			return this.money1;
		else if(p2.getID()==p.getID())
			return this.money2;
		else
			return 0;
	}
	
	public void addMoneyToTrade(FPlayer p, long amt)
	{
		if (p1.getID()==p.getID())
			this.money1+=amt;
		else if(p2.getID()==p.getID())
			this.money2+=amt;
	}
	
	public void addItem(FPlayer p, ItemStack i)
	{
		if (p1.getID()==p.getID())
			this.items1.add(i);
		else if(p2.getID()==p.getID())
			this.items2.add(i);
	}
	
	public void removeItem(FPlayer p, ItemStack i)
	{
		if (p1.getID()==p.getID())
			this.items1.remove(i);
		else if(p2.getID()==p.getID())
			this.items2.remove(i);
	}
	
	public boolean isReady(FPlayer p)
	{
		if (p1.getID()==p.getID())
			return this.ready1;
		else if(p2.getID()==p.getID())
			return this.ready2;
		else
			return false;
	}
	
	public void setReady(FPlayer p, boolean val)
	{
		if (p1.getID()==p.getID())
			this.ready1=val;
		else if(p2.getID()==p.getID())
			this.ready2=val;
	}
	
	public void initComplete()
	{
		int i = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(Main.class), new Runnable() {
			@Override
			public void run()
			{
				Menu m1 = p1.getMenu();
				Menu m2 = p2.getMenu();
				
				if (m1 instanceof MenuWaitComplete)
					((MenuWaitComplete)p1.getMenu()).updateTicks();
				if (m2 instanceof MenuWaitComplete)
					((MenuWaitComplete)p2.getMenu()).updateTicks();
			}
		}, 1, 1);

		p1.getMenu().swapMenu(new MenuWaitComplete(p1,this,i));
		p2.getMenu().swapMenu(new MenuWaitComplete(p2,this,i));
	}
	
	public void updateComplete()
	{
		this.tickstoComplete--;
		if (this.tickstoComplete <= 0)
		{
			p1.getMenu().swapMenu(new MenuFinalComplete(p1,this));
			p2.getMenu().swapMenu(new MenuFinalComplete(p1,this));
		}
	}
	
	public void setComplete(FPlayer p)
	{
		if (this.ready1 && this.ready2)
		{
			if (p1.getID()==p.getID())
				this.complete1=true;
			else if(p2.getID()==p.getID())
				this.complete2=true;
			
			if (this.complete1 && this.complete2)
				this.complete();
		}
	}
	
	public void complete()
	{
		for (ItemStack i : items1)
		{
			p2.getPlayer().getWorld().dropItem(p1.getPlayer().getLocation(), i);
			p1.getPlayer().getInventory().remove(i);
		}
		for (ItemStack i : items2)
		{
			p1.getPlayer().getWorld().dropItem(p1.getPlayer().getLocation(), i);
			p2.getPlayer().getInventory().remove(i);
		}
		p2.addMoney(this.money1);
		p1.addMoney(-this.money1);
		p1.addMoney(this.money2);
		p2.addMoney(-this.money2);
		this.closeTrade(false);
	}
	
	public void closeTrade(boolean cancel)
	{
		p1.closeMenu();
		p2.closeMenu();
		if (cancel)
		{
			p1.sendMessage(PluginPart.ECONOMY, ChatColor.RED+"Trade was cancelled.");
			p2.sendMessage(PluginPart.ECONOMY, ChatColor.RED+"Trade was cancelled.");
		}
		else
		{
			p1.sendMessage(PluginPart.ECONOMY, ChatColor.GREEN+"Trade complete!");
			p2.sendMessage(PluginPart.ECONOMY, ChatColor.GREEN+"Trade complete!");
		}
	}
}
