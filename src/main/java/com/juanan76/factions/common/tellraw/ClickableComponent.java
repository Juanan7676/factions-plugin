package com.juanan76.factions.common.tellraw;

public class ClickableComponent extends TextComponent {

	private String desc;
	private String onClick;
	
	public ClickableComponent(String text,String color,boolean bold, String desc, String onClick)
	{
		super(text,color,bold);
		this.desc = desc;
		this.onClick = onClick;
	}

	@Override
	public String getRepr() {
		String repr = super.getRepr();
		repr = repr.substring(0,repr.length()-1);
		repr += ",\"clickEvent\":{\"action\":\"run_command\",\"value\":\""+this.onClick+"\"}";
		repr += ",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\""+desc+"\"}}";
		return repr;
	}

}
