package com.juanan76.factions.pvp;

import org.bukkit.Location;

import com.juanan76.factions.common.FPlayer;

public class TeleportPlayer extends Teleport {
	
	private FPlayer to;
	
	@Override
	public String getLocationName() {
		return to.getPlayer().getName();
	}

	@Override
	public Location getTeleportLocation() {
		return to.getPlayer().getLocation();
	}
	
	public TeleportPlayer(FPlayer to, FPlayer teleported)
	{
		this.to = to;
		super.config(teleported);
		if (to.getFactionObject().getRelation(teleported.getFactionObject()).equalsIgnoreCase("a"))
		{ // Friendly faction: cost,time x 2
			super.cost *= 2;
			super.remainingTicks *= 2;
			super.amigo = true;
		}
	}
	
	public FPlayer getTo()
	{
		return this.to;
	}
	
	
	public FPlayer getTeleported()
	{
		return this.teleported;
	}
}
