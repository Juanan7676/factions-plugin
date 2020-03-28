package com.juanan76.factions.economy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.juanan76.factions.Main;
import com.juanan76.factions.common.FPlayer;
import com.juanan76.factions.common.PluginPart;
import com.juanan76.factions.common.Util;
import com.juanan76.factions.common.menu.ItemDummy;
import com.juanan76.factions.common.menu.ItemExitMenu;
import com.juanan76.factions.common.menu.ItemSwapMenu;
import com.juanan76.factions.common.menu.ItemTriggerEvent;
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
	
	private static interface ItemsUpdateable {
		public void updateContents();
	}
	
	private static interface MoneyUpdateable {
		public void updateMoney();
	}
	
	private static class MenuMain extends Menu implements ItemsUpdateable,MoneyUpdateable {
		
		private Trade t;
		
		public MenuMain(FPlayer viewer, Trade t) {
			super(viewer, 54);
			this.t = t;
			super.title = ChatColor.BLACK+ChatColor.BOLD.toString()+"Trading with "+ChatColor.GREEN+ChatColor.BOLD.toString()+((t.p1.getID()==viewer.getID())?t.p2.getPlayer().getName():t.p1.getPlayer().getName());
		}
		
		@Override
		public void initContents() {
			super.contents = new HashMap<Integer,MenuItem>();
			
			// Aesthetics
			for (int k = 36; k < 48; k++)
				super.contents.put(k,new ItemDummy(this,new ItemStack(Material.LIME_STAINED_GLASS_PANE,1)));
			for (int k = 51; k <= 53; k++)
				super.contents.put(k,new ItemDummy(this,new ItemStack(Material.LIME_STAINED_GLASS_PANE,1)));
			super.contents.put(49,new ItemDummy(this, new ItemStack(Material.LIME_STAINED_GLASS_PANE,1)));
			
			ItemStack exit = new ItemStack(Material.BARRIER,1);
			ItemMeta it = exit.getItemMeta();
			it.setDisplayName(ChatColor.DARK_RED+ChatColor.BOLD.toString()+"Cancel trade");
			exit.setItemMeta(it);
			
			ItemStack ok = new ItemStack(Material.FERN,1);
			it = ok.getItemMeta();
			it.setDisplayName(ChatColor.DARK_GREEN+ChatColor.BOLD.toString()+"Ready");
			ok.setItemMeta(it);
			
			super.contents.put(50, new ItemExitMenu(this,exit));
			super.contents.put(48, new ItemSwapMenu(this,ok,new MenuMainAccepting(viewer,this.t)));
			
			this.updateContents();
			this.updateMoney();
		}
		
		public void updateContents()
		{
			List<ItemStack> items1 = (this.viewer.equals(this.t.p1)) ? this.t.items1 : this.t.items2;
			List<ItemStack> items2 = (this.viewer.equals(this.t.p1)) ? this.t.items2 : this.t.items1;

			for (int k = 0; k < 12; k++)
			{
				this.contents.remove(9+(k/4*9)+(k%4));
				this.contents.remove(9+(k/4*9)+(k%4+5));
			}
			
			int counter = 0;
			for (ItemStack i : items1)
			{
				this.contents.put(9+(counter/4*9)+(counter%4),new ItemTriggerEvent(this, i, "rem",9+(counter/4*9)+(counter%4),i));
				counter++;
			}
			
			counter = 0;
			for (ItemStack i : items2)
			{
				this.contents.put(9+(counter/4*9)+(counter%4+5),new ItemDummy(this, i));
				counter++;
			}
			this.composeInv();
		}
		
		public void updateMoney()
		{
			// Owner money in trade
			
			FPlayer p1 = this.viewer;
			FPlayer p2 = (p1.equals(this.t.p1)) ? this.t.p2 : this.t.p1;
			
			ItemStack om = new ItemStack(Material.GREEN_WOOL,1);
			ItemMeta it = om.getItemMeta();
			it.setDisplayName(ChatColor.DARK_GREEN+ChatColor.BOLD.toString()+"Money in trade:");
			it.setLore(Arrays.asList(new String[] {"",Util.getMoney(this.t.getMoneyTraded(p1)),"","Click to add/remove money from trade"}));
			om.setItemMeta(it);
			
			if (this.canAdd()) super.contents.put(0,new ItemSwapMenu(this,om,new MenuMoney(super.viewer,this.t)));
			else super.contents.put(0,new ItemDummy(this,om));
			
			// Trader money in trade
			ItemStack tm = new ItemStack(Material.GREEN_WOOL,1);
			it = tm.getItemMeta();
			it.setDisplayName(ChatColor.DARK_GREEN+ChatColor.BOLD.toString()+"Money in trade:");
			it.setLore(Arrays.asList(new String[] {"",Util.getMoney(this.t.getMoneyTraded(p2))}));
			tm.setItemMeta(it);
			super.contents.put(8,new ItemDummy(this,tm));
			this.composeInv();
		}
		
		protected boolean canAdd()
		{
			return true;
		}
		
		@Override
		@EventHandler
		public void onClick(InventoryClickEvent e)
		{
			super.onClick(e);
			if (e.getClickedInventory().equals(e.getView().getBottomInventory()) && Main.players.get(e.getWhoClicked()).isLogged() && e.getInventory().equals(super.view) && this.canAdd())
			{
				ItemStack i = e.getView().getItem(e.getRawSlot());
				if (i!=null)
					this.t.addItem(Main.players.get(e.getWhoClicked()), i);
			}
		}
		
		
		
		@Override
		public void onSwap(Menu another)
		{
			if (another instanceof MenuMainAccepting)
				this.t.setReady(this.viewer, true);
		}
		
		@Override
		public void onEvent(String id, Object... args)
		{
			if (id.equals("rem") && this.canAdd())
			{
				this.contents.remove(args[0]);
				this.t.removeItem(this.viewer, (ItemStack)args[1]);
			}
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
			ItemStack cancel = new ItemStack(Material.BARRIER,1);
			ItemMeta it = cancel.getItemMeta();
			it.setDisplayName(ChatColor.RED+ChatColor.BOLD.toString()+"Not ready");
			cancel.setItemMeta(it);
			super.contents.put(48, new ItemSwapMenu(this,cancel,new MenuMain(viewer,super.t)));
			super.contents.put(50, new ItemDummy(this, new ItemStack(Material.LIME_STAINED_GLASS_PANE,1)));
		}
		
		@Override
		protected boolean canAdd()
		{
			return false;
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
		
		@Override
		protected boolean canAdd()
		{
			return false;
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
			if (this.ticksToComplete <= 0)
				this.swapMenu(new MenuFinalComplete(this.viewer,super.t));
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
			
			ItemStack complete = new ItemStack(Material.FERN,1);
			ItemMeta i = complete.getItemMeta();
			i.setDisplayName(ChatColor.DARK_GREEN+ChatColor.BOLD.toString()+"Complete trade");
			complete.setItemMeta(i);
			
			super.contents.put(50, new ItemTriggerEvent(this, complete,"complete"));
			
			ItemStack om = new ItemStack(Material.GREEN_WOOL,1);
			ItemMeta it = om.getItemMeta();
			it.setDisplayName(ChatColor.DARK_GREEN+ChatColor.BOLD.toString()+"Money in trade:");
			it.setLore(Arrays.asList(new String[] {"",Util.getMoney(super.t.getMoneyTraded(this.viewer))}));
			om.setItemMeta(it);
			super.contents.put(0,new ItemDummy(this,om));
		}
		
		@Override
		protected boolean canAdd()
		{
			return false;
		}
		
		@Override
		public void onEvent(String id, Object...args)
		{
			if (id.equals("complete"))
				super.t.setComplete(super.viewer);
		}
		
	}
	
	private static class MenuMoney extends Menu implements MoneyUpdateable
	{
		private Trade t;
		
		public MenuMoney(FPlayer viewer, Trade t) {
			super(viewer, 54);
			this.t = t;
			super.title = ChatColor.BLACK+ChatColor.BOLD.toString()+"Trading with "+ChatColor.GREEN+ChatColor.BOLD.toString()+((t.p1.getID()==viewer.getID())?t.p2.getPlayer().getName():t.p1.getPlayer().getName());
		}
		
		private ItemStack getSlot(long amt)
		{
			ItemStack ret = new ItemStack((amt>0)?Material.GREEN_CONCRETE:Material.RED_CONCRETE,1);
			ItemMeta r = ret.getItemMeta();
			r.setDisplayName(ChatColor.GREEN + ((amt>0)?"Add "+Util.getMoney(amt)+ChatColor.GREEN+" to trade":"Remove "+Util.getMoney(amt)+ChatColor.GREEN+" from trade"));
			ret.setItemMeta(r);
			return ret;
		}
		
		@Override
		public void initContents() {
			int c = 0;
			for (long k = 1; k <= 100000000; k *= 10, c++)
			{
				this.contents.put(9+(c/4)*9+(c%4), new ItemTriggerEvent(this,getSlot(k),"m",k));
				this.contents.put(9+(c/4)*9+(c%4+5), new ItemTriggerEvent(this,getSlot(-k),"m",-k));
			}
			
			ItemStack back = new ItemStack(Material.BARRIER,1);
			ItemMeta i = back.getItemMeta();
			i.setDisplayName(ChatColor.RED+ChatColor.BOLD.toString()+"Back to menu");
			back.setItemMeta(i);
			
			this.contents.put(49, new ItemSwapMenu(this, back,new MenuMain(this.viewer,this.t)));
			
			this.updateMoney();
		}
		
		public void updateMoney()
		{
			ItemStack i = new ItemStack(Material.GREEN_WOOL,1);
			ItemMeta it = i.getItemMeta();
			it.setDisplayName(ChatColor.GREEN+ChatColor.BOLD.toString()+"Money in trade: "+Util.getMoney(this.t.getMoneyTraded(this.viewer)));
			i.setItemMeta(it);
			this.contents.put(4, new ItemDummy(this,i));
			this.composeInv();
		}
		
		@Override
		public void onEvent(String id, Object... args)
		{
			if (id.equals("m"))
			{
				this.t.addMoneyToTrade(this.viewer, (long)args[0]);
			}
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
		this.p1.openMenu(new MenuMain(this.p1,this));
		this.p2.openMenu(new MenuMain(this.p2,this));
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
			this.money1+=((amt < 0 && this.money1 < amt) || (amt > 0 && p.getMoney() < this.money1+amt)) ? 0 : amt;
		else if(p2.getID()==p.getID())
			this.money2+=((amt < 0 && this.money2 < amt) || (amt > 0 && p.getMoney() < this.money2+amt)) ? 0 : amt;
		
		if (this.p1.getMenu() instanceof MoneyUpdateable) ((MoneyUpdateable)this.p1.getMenu()).updateMoney();
		if (this.p2.getMenu() instanceof MoneyUpdateable) ((MoneyUpdateable)this.p2.getMenu()).updateMoney();
	}
	
	public void addItem(FPlayer p, ItemStack i)
	{
		if (p1.getID()==p.getID())
		{
			if (!this.items1.contains(i) && this.items1.size() < 12) this.items1.add(i);
		}
		
		else if (p2.getID()==p.getID())
		{
			if (!this.items2.contains(i) && this.items2.size() < 12) this.items2.add(i);
		}
		
		if (this.p1.getMenu() instanceof ItemsUpdateable) ((ItemsUpdateable)this.p1.getMenu()).updateContents();
		if (this.p2.getMenu() instanceof ItemsUpdateable) ((ItemsUpdateable)this.p2.getMenu()).updateContents();
	}
	
	public void removeItem(FPlayer p, ItemStack i)
	{
		if (p1.getID()==p.getID())
			this.items1.remove(i);
		else if(p2.getID()==p.getID())
			this.items2.remove(i);
		
		if (this.p1.getMenu() instanceof ItemsUpdateable) ((ItemsUpdateable)this.p1.getMenu()).updateContents();
		if (this.p2.getMenu() instanceof ItemsUpdateable) ((ItemsUpdateable)this.p2.getMenu()).updateContents();
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
		
		if (this.ready1 && this.ready2)
			this.initComplete();
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
		if (p1.getID()==p.getID())
			this.complete1=true;
		else if(p2.getID()==p.getID())
			this.complete2=true;
		
		if (this.complete1 && this.complete2)
			this.complete();
	}
	
	public void complete()
	{
		for (ItemStack i : items1)
		{
			HashMap<Integer,ItemStack> failed = p2.getPlayer().getInventory().addItem(i);
			for (ItemStack o : failed.values())
			{
				p2.getPlayer().getWorld().dropItem(p2.getPlayer().getLocation(), o);
			}
			
			p1.getPlayer().getInventory().remove(i);
		}
		for (ItemStack i : items2)
		{
			HashMap<Integer,ItemStack> failed = p1.getPlayer().getInventory().addItem(i);
			for (ItemStack o : failed.values())
			{
				p1.getPlayer().getWorld().dropItem(p1.getPlayer().getLocation(), o);
			}
			
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
		Main.trades.remove(this);
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
