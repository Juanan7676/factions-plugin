package com.juanan76.factions.economy;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.bukkit.OfflinePlayer;

import com.juanan76.factions.common.DBManager;
import com.juanan76.factions.common.FPlayer;
import com.juanan76.factions.common.Util;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

public class EconomyProvider implements Economy {

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getName() {
		return "FEconomy";
	}

	@Override
	public boolean hasBankSupport() {
		return false;
	}

	@Override
	public int fractionalDigits() {
		return 0;
	}

	@Override
	public String format(double amount) {
		return Util.getMoney((long)amount);
	}

	@Override
	public String currencyNamePlural() {
		return "Farlandios";
	}

	@Override
	public String currencyNameSingular() {
		return "Farlandio";
	}

	@Override
	public boolean hasAccount(String playerName) {
		try {
			return FPlayer.IDfromnick(playerName) != -1;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean hasAccount(OfflinePlayer player) {
		return this.hasAccount(player.getName());
	}

	@Override
	public boolean hasAccount(String playerName, String worldName) {
		return this.hasAccount(playerName);
	}

	@Override
	public boolean hasAccount(OfflinePlayer player, String worldName) {
		return this.hasAccount(player);
	}

	@Override
	public double getBalance(String playerName) {
		try {
			int id = FPlayer.IDfromnick(playerName);
			ResultSet rst = DBManager.performSafeQuery("select dinero from dinero where id=?", "i", id);
			if (!rst.next()) throw new IllegalStateException("No such id: "+ id);
			return rst.getLong("dinero");
		} catch (SQLException | IllegalStateException e) {
			e.printStackTrace();
			return -1;
		}
		
	}

	@Override
	public double getBalance(OfflinePlayer player) {
		return this.getBalance(player.getName());
	}

	@Override
	public double getBalance(String playerName, String world) {
		return this.getBalance(playerName);
	}

	@Override
	public double getBalance(OfflinePlayer player, String world) {
		return this.getBalance(player);
	}

	@Override
	public boolean has(String playerName, double amount) {
		return this.getBalance(playerName) >= amount;
	}

	@Override
	public boolean has(OfflinePlayer player, double amount) {
		return this.has(player.getName(),amount);
	}

	@Override
	public boolean has(String playerName, String worldName, double amount) {
		return this.has(playerName, amount);
	}

	@Override
	public boolean has(OfflinePlayer player, String worldName, double amount) {
		return this.has(player.getName(), amount);
	}

	@Override
	public EconomyResponse withdrawPlayer(String playerName, double amount) {
		
		if (!this.has(playerName, amount)) return new EconomyResponse(0,this.getBalance(playerName),ResponseType.FAILURE,"Not enough funds!");
		
		FPlayer player;
		if ((player = (FPlayer.fromNick(playerName))) != null)
		{
			player.addMoney(-(long)amount);
			return new EconomyResponse(amount, this.getBalance(playerName), ResponseType.SUCCESS, null);
		}
		
		try {
			DBManager.performSafeExecute("UPDATE dinero SET dinero=dinero-"+(long)amount+" WHERE id=?", "i", FPlayer.IDfromnick(playerName));
		} catch (SQLException e) {
			e.printStackTrace();
			return new EconomyResponse(0,this.getBalance(playerName),ResponseType.FAILURE,"SQL error!");
		}
		
		return new EconomyResponse(amount, this.getBalance(playerName), ResponseType.SUCCESS, null);
	}

	@Override
	public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
		return this.withdrawPlayer(player.getName(), amount);
	}

	@Override
	public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
		return this.withdrawPlayer(playerName,amount);
	}

	@Override
	public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
		return this.withdrawPlayer(player,amount);
	}

	@Override
	public EconomyResponse depositPlayer(String playerName, double amount) {
		
		FPlayer player;
		if ((player = (FPlayer.fromNick(playerName))) != null)
		{
			player.addMoney((long)amount);
			return new EconomyResponse(amount, this.getBalance(playerName), ResponseType.SUCCESS, null);
		}
		
		try {
			DBManager.performSafeExecute("UPDATE dinero SET dinero=dinero+"+(long)amount+" WHERE id=?", "i", FPlayer.IDfromnick(playerName));
		} catch (SQLException e) {
			e.printStackTrace();
			return new EconomyResponse(0,this.getBalance(playerName),ResponseType.FAILURE,"SQL error!");
		}
		
		return new EconomyResponse(amount, this.getBalance(playerName), ResponseType.SUCCESS, null);
	}

	@Override
	public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
		return this.depositPlayer(player.getName(), amount);
	}

	@Override
	public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
		return this.depositPlayer(playerName, amount);
	}

	@Override
	public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
		return this.depositPlayer(player, amount);
	}

	@Override
	public EconomyResponse createBank(String name, String player) {
		return new EconomyResponse(0,0,ResponseType.NOT_IMPLEMENTED,null);
	}

	@Override
	public EconomyResponse createBank(String name, OfflinePlayer player) {
		return new EconomyResponse(0,0,ResponseType.NOT_IMPLEMENTED,null);
	}

	@Override
	public EconomyResponse deleteBank(String name) {
		return new EconomyResponse(0,0,ResponseType.NOT_IMPLEMENTED,null);
	}

	@Override
	public EconomyResponse bankBalance(String name) {
		return new EconomyResponse(0,0,ResponseType.NOT_IMPLEMENTED,null);
	}

	@Override
	public EconomyResponse bankHas(String name, double amount) {
		return new EconomyResponse(0,0,ResponseType.NOT_IMPLEMENTED,null);
	}

	@Override
	public EconomyResponse bankWithdraw(String name, double amount) {
		return new EconomyResponse(0,0,ResponseType.NOT_IMPLEMENTED,null);
	}

	@Override
	public EconomyResponse bankDeposit(String name, double amount) {
		return new EconomyResponse(0,0,ResponseType.NOT_IMPLEMENTED,null);
	}

	@Override
	public EconomyResponse isBankOwner(String name, String playerName) {
		return new EconomyResponse(0,0,ResponseType.NOT_IMPLEMENTED,null);
	}

	@Override
	public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
		return new EconomyResponse(0,0,ResponseType.NOT_IMPLEMENTED,null);
	}

	@Override
	public EconomyResponse isBankMember(String name, String playerName) {
		return new EconomyResponse(0,0,ResponseType.NOT_IMPLEMENTED,null);
	}

	@Override
	public EconomyResponse isBankMember(String name, OfflinePlayer player) {
		return new EconomyResponse(0,0,ResponseType.NOT_IMPLEMENTED,null);
	}

	@Override
	public List<String> getBanks() {
		return null;
	}

	@Override
	public boolean createPlayerAccount(String playerName) {
		return false;
	}

	@Override
	public boolean createPlayerAccount(OfflinePlayer player) {
		return false;
	}

	@Override
	public boolean createPlayerAccount(String playerName, String worldName) {
		return false;
	}

	@Override
	public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
		return false;
	}

}
