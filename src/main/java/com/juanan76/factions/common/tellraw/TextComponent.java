package com.juanan76.factions.common.tellraw;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;

public class TextComponent implements TellRawComponent {
	
	private ChatColor color;
	private String text;
	private boolean bold;
	
	public TextComponent(String text, String color, boolean bold)
	{
		this.color = ChatColor.of(color);
		this.text = text;
		this.bold = bold;
	}
	
	public TextComponent(String text)
	{
		this(text,"white",false);
	}
	
	public TextComponent(String text, String color)
	{
		this(text,color,false);
	}
	
	@Override
	public BaseComponent toBukkit() {
		net.md_5.bungee.api.chat.TextComponent ret =  new net.md_5.bungee.api.chat.TextComponent(this.text);
		ret.setBold(this.bold);
		ret.setColor(this.color);
		return ret;
	}

	public void setColor(String newColor){
		color = ChatColor.of(newColor);
	}

	public void setText(String text){
		this.text = text;
	}

}
