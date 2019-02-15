package com.juanan76.factions.common;

public enum PluginPart {
	MAIN("MAIN"),
	PVP("PVP"),
	ECONOMY("ECONOMY"),
	FACTIONS("FACTIONS");
	
	private String display;
	private PluginPart(String s)
	{
		this.display = s;
	}
	@Override
	public String toString()
	{
		return this.display;
	}
}
