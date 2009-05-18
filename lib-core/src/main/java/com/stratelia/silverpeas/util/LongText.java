package com.stratelia.silverpeas.util;

/**
 * Title: userPanelPeas
 * Description: this is an object pair of pair object 
 * Copyright:    Copyright (c) 2002
 * Company:      Silverpeas
 * @author J-C Groccia
 * @version 1.0
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;

public class  LongText
{
	public final static int PART_SIZE_MAX = 1998;

	static public int addLongText(String theText) throws UtilException
	{
		int theId = DBUtil.getNextId("ST_LongText","id");
		PreparedStatement stmt = null;
		Connection privateConnection = null;
		int orderNum = 0;
		String partText = null;
		String theQuery = "insert into ST_LongText (id, orderNum, bodyContent) values (?, ?, ?)";
		
		try
		{
			privateConnection = openConnection();
			stmt = privateConnection.prepareStatement(theQuery);
			if ((theText == null) || (theText.length() <= 0))
			{
				stmt.setInt(1, theId);
				stmt.setInt(2, orderNum);
				stmt.setString(3, "");
				stmt.executeUpdate();
			}
			else
			{
				while (orderNum * PART_SIZE_MAX < theText.length())
				{
					if ((orderNum+1) * PART_SIZE_MAX < theText.length())
						partText = theText.substring(orderNum * PART_SIZE_MAX, (orderNum+1) * PART_SIZE_MAX);
					else
						partText = theText.substring(orderNum * PART_SIZE_MAX);
					stmt.setInt(1, theId);
					stmt.setInt(2, orderNum);
					stmt.setString(3, partText);
					stmt.executeUpdate();
					orderNum++;
				}
			}
		}
		catch (Exception e)
		{
			throw new UtilException("LongText.addLongText()",SilverpeasException.WARNING, "root.MSG_PARAM_VALUE", theText, e);
		}
		finally
		{
			DBUtil.close(stmt);
			closeConnection(privateConnection);
		}
		return theId;
	}
	
	static public String getLongText(int longTextId) throws UtilException
	{
		PreparedStatement stmt = null;
		Connection privateConnection = null;
		ResultSet rs = null;
		StringBuffer valret = new StringBuffer();
		String theQuery = "select bodyContent from ST_LongText where id = ? order by orderNum";

		try
		{
			privateConnection = openConnection();
			stmt = privateConnection.prepareStatement(theQuery);
			stmt.setInt(1, longTextId);
			rs = stmt.executeQuery();
			while (rs.next())
			{
				valret.append(rs.getString(1));
			}
			return valret.toString();
		}
		catch (Exception e)
		{
			throw new UtilException("LongText.getLongText()",SilverpeasException.WARNING, "root.MSG_PARAM_VALUE", Integer.toString(longTextId), e);
		}
		finally
		{
			DBUtil.close(rs,stmt);
			closeConnection(privateConnection);
		}
	}
	
	static public void removeLongText(int longTextId) throws UtilException
	{
		PreparedStatement stmt = null;
		Connection privateConnection = null;
		String theQuery = "delete from ST_LongText where id = ?";

		try
		{
			privateConnection = openConnection();
			stmt = privateConnection.prepareStatement(theQuery);
			stmt.setInt(1, longTextId);
			stmt.executeUpdate();
		}
		catch (Exception e)
		{
			throw new UtilException("LongText.removeLongText()",SilverpeasException.WARNING, "root.MSG_PARAM_VALUE", Integer.toString(longTextId), e);
		}
		finally
		{
			DBUtil.close(stmt);
			closeConnection(privateConnection);
		}
	}
	
	static protected Connection openConnection() throws UtilException 
	{
		Connection con = null;
		try 
		{
			con = DBUtil.makeConnection(JNDINames.SILVERPEAS_DATASOURCE);
		}
		catch (Exception e) 
		{
			throw new UtilException("LongText.openConnection()",SilverpeasException.WARNING, "root.MSG_PARAM_VALUE", e);
		}

		return con;
	}

	static protected void closeConnection(Connection con)
	{
		try 
		{
			con.close();
		}
		catch (Exception e) 
		{
			SilverTrace.error("util", "LongText.closeConnection()", "root.EX_CONNECTION_CLOSE_FAILED", e);
		}
	}
}
