package com.juanan76.factions.npc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.juanan76.factions.common.Util;

public class SellingItem {
	private Material m;
	private double price;
	private Map<Enchantment,Integer> ench;
	private String name;
	private List<String> lore;
	private boolean hideEnchants;
	
	public SellingItem(Material m, String name, double pricePerUnit, Map<Enchantment,Integer> ench, List<String> lore, boolean hideEnchants)
	{
		this.m = m;
		this.price = pricePerUnit;
		this.ench = ench;
		this.name = name;
		this.lore = lore;
		this.hideEnchants = hideEnchants;
	}
	
	public SellingItem(Material m, String name, double pricePerUnit)
	{
		this(m,name,pricePerUnit,new HashMap<Enchantment,Integer>(),Arrays.asList(new String[] {}),false);
	}
	
	public ItemStack getItemStack(int qty)
	{
		ItemStack i = new ItemStack(this.m,qty);
		if (this.ench != null)
			i.addUnsafeEnchantments(this.ench);
		ItemMeta it = i.getItemMeta();
		it.setDisplayName(this.name);
		if (this.lore != null)
			it.setLore(this.lore);
		if (this.hideEnchants)
			it.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		i.setItemMeta(it);
		
		return i;
	}
	
	public long getPurchasePrice(int qty)
	{
		return (long)Math.ceil(qty*price);
	}
	
	public ItemStack getShopItem()
	{
		ItemStack i = new ItemStack(this.m,1);
		
		i.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 10);
		ItemMeta it = i.getItemMeta();
		it.setDisplayName(ChatColor.DARK_PURPLE + ChatColor.BOLD.toString() + "Purchase " + ChatColor.stripColor(name));
		it.setLore(Arrays.asList(new String[] { "","Price per unit: "+Util.getMoney((long)(Math.ceil(this.price))),"","Click to purchase" }));
		it.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		i.setItemMeta(it);
		
		return i;
	}
}
