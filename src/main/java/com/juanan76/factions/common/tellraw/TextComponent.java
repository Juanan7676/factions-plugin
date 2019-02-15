package com.juanan76.factions.common.tellraw;

public class TextComponent implements TellRawComponent {
	
	private String color;
	private String text;
	private boolean bold;
	
	public TextComponent(String text, String color, boolean bold)
	{
		this.color = color;
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
	public String getRepr() {
		String repr = "";
		repr += "{\"text\":\""+text+"\",\"color\":\""+color+"\"";
		repr += (bold) ? ",\"bold\":\"true\"}" : ",\"bold\":\"false\"}";
		return repr;
	}

}
