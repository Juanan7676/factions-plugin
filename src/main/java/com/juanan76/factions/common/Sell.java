package com.juanan76.factions.common;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.juanan76.factions.Main;
import com.juanan76.factions.factions.gens.Generator;

public class Sell implements CommandExecutor {
	public Map<Material,Long> serverPrices = new HashMap<Material,Long>();
	
	public Sell()
	{
		this.serverPrices.put(Material.COBBLESTONE, 1L);
		this.serverPrices.put(Material.STONE, 1L);
		this.serverPrices.put(Material.NETHERRACK, 1L);
		this.serverPrices.put(Material.OAK_PLANKS, 2L);
		this.serverPrices.put(Material.ACACIA_PLANKS, 2L);
		this.serverPrices.put(Material.BIRCH_PLANKS, 2L);
		this.serverPrices.put(Material.DARK_OAK_PLANKS, 2L);
		this.serverPrices.put(Material.JUNGLE_PLANKS, 2L);
		this.serverPrices.put(Material.SPRUCE_PLANKS, 2L);
		this.serverPrices.put(Material.SAND, 1L);
		this.serverPrices.put(Material.DIRT, 1L);
		this.serverPrices.put(Material.ANDESITE, 1L);
		this.serverPrices.put(Material.DIORITE, 1L);
		this.serverPrices.put(Material.GRANITE, 1L);
		this.serverPrices.put(Material.GRAVEL, 1L);
		this.serverPrices.put(Material.COAL, 3L);
		this.serverPrices.put(Material.IRON_INGOT, 5L);
		this.serverPrices.put(Material.GOLD_INGOT, 11L);
		this.serverPrices.put(Material.LAPIS_LAZULI, 12L);
		this.serverPrices.put(Material.REDSTONE,7L);
		this.serverPrices.put(Material.QUARTZ,10L);
		this.serverPrices.put(Material.ROTTEN_FLESH,2L);
		this.serverPrices.put(Material.GUNPOWDER, 2L);
		this.serverPrices.put(Material.SPIDER_EYE, 2L);
		this.serverPrices.put(Material.BONE, 2L);
		this.serverPrices.put(Material.ARROW, 2L);
		this.serverPrices.put(Material.ENDER_PEARL, 2L);
		this.serverPrices.put(Material.OBSIDIAN,6L);
		this.serverPrices.put(Material.DIAMOND, 20L);
		this.serverPrices.put(Material.CACTUS, 5L);
		this.serverPrices.put(Material.SUGAR_CANE, 5L);
		this.serverPrices.put(Material.BREAD, 6L);
		this.serverPrices.put(Material.WHEAT, 2L);
		this.serverPrices.put(Material.CARROT, 2L);
		this.serverPrices.put(Material.POTATO, 2L);
		this.serverPrices.put(Material.MELON_SLICE, 1L);
		this.serverPrices.put(Material.PUMPKIN, 2L);
		this.serverPrices.put(Material.WITHER_SKELETON_SKULL, 100L);
		this.serverPrices.put(Material.BLAZE_ROD, 7L);
		this.serverPrices.put(Material.STRING, 2L);
		this.serverPrices.put(Material.WHITE_WOOL, 1L);
		this.serverPrices.put(Material.COPPER_INGOT, 3L);
		this.serverPrices.put(Material.COBBLED_DEEPSLATE, 1L);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player)
		{
			ItemStack it = ((Player) sender).getInventory().getItemInMainHand();
			if (it != null)
			{
				if (it.getType()==Material.SPAWNER)
				{
					if (it.getEnchantmentLevel(Enchantment.ARROW_INFINITE)>0)
					{
						((Player) sender).getInventory().setItemInMainHand(null);
						long aw = (long)Math.floor(Generator.getStack(it.getEnchantmentLevel(Enchantment.ARROW_INFINITE)).getPurchasePrice(it.getAmount())*0.9);
						Main.players.get(sender).addMoney(aw);
						Main.players.get(sender).sendMessage(PluginPart.ECONOMY, ChatColor.GREEN+"You got "+Util.getMoney(aw)+"!");
						return true;
					}
					else
						Main.players.get(sender).sendMessage(PluginPart.ECONOMY, ChatColor.RED+"That item can't be sold to the server.");
				}
				if (!this.serverPrices.containsKey(it.getType()))
					Main.players.get(sender).sendMessage(PluginPart.ECONOMY, ChatColor.RED+"That item can't be sold to the server.");
				else
				{
					((Player) sender).getInventory().setItemInMainHand(null);
					long aw = it.getAmount()*this.serverPrices.get(it.getType());
					Main.players.get(sender).addMoney(aw);
					Main.players.get(sender).sendMessage(PluginPart.ECONOMY, ChatColor.GREEN+"You got "+Util.getMoney(aw)+"!");
				}
			}
		}
		return true;
	}
	
	
}
