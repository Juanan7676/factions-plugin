package com.juanan76.factions.factions;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.juanan76.factions.common.DBManager;

public class FactionUtils {
	public static int id(String name) throws SQLException
	{
		ResultSet rst = DBManager.performSafeQuery("select id from facciones where nombre=?","s",name);
		if (!rst.next()) return -1;
		else return rst.getInt(1);
	}
	
	public static long getPrice()
	{
		try {
			ResultSet rst = DBManager.performQuery("select count(*) from facciones");
			long v = rst.getLong(1);
			return 100*v*v*v*v*v*v;
		} catch (SQLException e) {
			// Something really bad should happen in order to arrive here... but anyway
			e.printStackTrace();
		}
		return Long.MAX_VALUE;
	}
	
	public static int getID() throws SQLException
	{
		ResultSet rst = DBManager.performQuery("select id from facciones order by id desc");
		if (!rst.next()) return 0;
		else return rst.getInt(1)+1;
	}
}
