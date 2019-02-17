package com.juanan76.factions.pvp;

import org.bukkit.Location;

import com.juanan76.factions.common.FPlayer;

public class TeleportLocation extends Teleport {

	private Location dest;
	
	public TeleportLocation(FPlayer u, Location dest)
	{
		this.dest = dest;
		super.config(u);
	}
	
	@Override
	public String getLocationName() {
		return "("+this.dest.getBlockX()+","+this.dest.getBlockY()+","+this.dest.getBlockZ()+")";
	}

	@Override
	public Location getTeleportLocation() {
		return this.dest;
	}
}
