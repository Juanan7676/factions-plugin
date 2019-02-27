package com.juanan76.factions.economy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.juanan76.factions.common.FPlayer;
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
			super.contents.put(48, new ItemSwapMenu(this,new ItemStack(Material.FERN,1),new MenuMain(viewer,super.t)));
		}
		
		@Override
		public void onSwap(Menu m)
		{
			if (m instanceof MenuMain)
			{
				super.t.setReady(super.viewer);
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
	
	public boolean isReady(FPlayer p)
	{
		if (p1.getID()==p.getID())
			return this.ready1;
		else if(p2.getID()==p.getID())
			return this.ready2;
		else
			return false;
	}
	
	public void setReady(FPlayer p)
	{
		if (p1.getID()==p.getID())
			this.ready1=true;
		else if(p2.getID()==p.getID())
			this.ready2=true;
	}
}
