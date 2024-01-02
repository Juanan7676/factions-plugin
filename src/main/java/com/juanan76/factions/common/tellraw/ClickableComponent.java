package com.juanan76.factions.common.tellraw;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class ClickableComponent extends TextComponent {

	private String desc;
	private String onClick;
	
	public ClickableComponent(String text, String color,boolean bold, String desc, String onClick)
	{
		super(text,color,bold);
		this.desc = desc;
		this.onClick = onClick;
	}

	@Override
	public BaseComponent toBukkit() {
		BaseComponent ret = super.toBukkit();
		ret.setClickEvent(new ClickEvent(Action.RUN_COMMAND, this.onClick));
		ret.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.desc).create()));
		return ret;
	}

}
