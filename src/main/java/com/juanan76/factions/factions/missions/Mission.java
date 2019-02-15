package com.juanan76.factions.factions.missions;

import java.sql.SQLException;

public interface Mission {
	
	public String getName();
	public String getDesc();
	public void reward();
	public int getLimit();
	public boolean isCompleted();
	public long getRequiredRespect();
	
	public void create() throws SQLException;
	public void update() throws SQLException;
	public void delete() throws SQLException;
}
