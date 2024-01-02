package com.juanan76.factions.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBManager {
	
	private static Connection conn = null;
	
	public static void getConnection() throws SQLException
	{
		if (conn == null)
		{
			String DB = "jdbc:sqlite:data.db";
			conn = DriverManager.getConnection(DB);
		}
	}
	
	public static void closeConnection() throws SQLException
	{
		if (conn != null)
		{
			conn.close();
		}
	}
	
	public static ResultSet performQuery(String sql) throws SQLException
	{
		getConnection();
		
		return conn.createStatement().executeQuery(sql);
	}
	
	public static void performExecute(String sql) throws SQLException
	{
		getConnection();
		conn.createStatement().executeUpdate(sql);
	}
	
	public static void performSafeExecute(String sql, String types, Object... args) throws SQLException
	{
		PreparedStatement pstmt = conn.prepareStatement(sql);
		int counter = 0;
		for (String t : types.split(""))
		{
			if (t.equalsIgnoreCase("i")) pstmt.setInt(counter+1, (Integer)args[counter]);
			else if (t.equalsIgnoreCase("s")) pstmt.setString(counter+1, (String)args[counter]);
			else if (t.equalsIgnoreCase("l")) pstmt.setLong(counter+1, (Long)args[counter]);
			else throw new IllegalArgumentException("Invalid SafeExceute!");
			counter++;
		}
		pstmt.execute();
		pstmt.close();
	}
	
	public static ResultSet performSafeQuery(String sql, String types, Object... args) throws SQLException
	{
		PreparedStatement pstmt = conn.prepareStatement(sql);
		int counter = 0;
		for (String t : types.split(""))
		{
			if (t.equalsIgnoreCase("i")) pstmt.setInt(counter+1, (Integer)args[counter]);
			else if (t.equalsIgnoreCase("s")) pstmt.setString(counter+1, (String)args[counter]);
			else if (t.equalsIgnoreCase("l")) pstmt.setLong(counter+1, (Long)args[counter]);
			else throw new IllegalArgumentException("Invalid SafeQuery!");
			counter++;
		}
		return pstmt.executeQuery();
	}
	/**
	 * In case you need to execute more than one query, in order to greatly improve performance it's better to prepare a statement and then execute it lots of times.
	 * @param sql Prepared SQL statement to be executed
	 * @return a PreparedStatement object
	 * @throws SQLException 
	 */
	public static PreparedStatement prepareQuery(String sql) throws SQLException
	{
		return conn.prepareStatement(sql);
	}
}
