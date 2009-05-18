package com.stratelia.silverpeas.domains.sqldriver;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.*;

import java.sql.*;
import java.util.ArrayList;

/**
 * A GroupTable object manages the DomainSQL_Group table.
 */
public class SQLGroupUserRelTable
{
	SQLSettings drvSettings = new SQLSettings();
    
	public SQLGroupUserRelTable(SQLSettings ds)
    {
		drvSettings = ds;
    }

   /**
    * Returns all the User ids which compose a group.
    */
   public ArrayList getDirectUserIdsOfGroup(Connection c, int groupId) throws AdminException
   {
	    ResultSet rs = null;
	    PreparedStatement statement = null;
	    ArrayList theResult = new ArrayList();
	    String theQuery = "select " + drvSettings.getRelUIDColumnName() + 
						  " from " + drvSettings.getRelTableName() + 
						  " where " + drvSettings.getRelGIDColumnName() + " = ?";
	    
	    try
	    {
 		   SilverTrace.debug("admin", "SQLGroupUserRelTable.getDirectUserIdsOfGroup", "root.MSG_QUERY", theQuery);
	       statement = c.prepareStatement(theQuery);
	       statement.setInt(1,groupId);
           rs = statement.executeQuery();
           while (rs.next())
           {
           	theResult.add(Integer.toString(rs.getInt(1)));
           }
	    }
	    catch (SQLException e)
	    {
	       throw new AdminException("SQLGroupUserRelTable.getDirectUserIdsOfGroup", SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", "Query = " + theQuery, e);
	    }
	    finally
	    {
	         DBUtil.close(rs,statement);
	    }
	    return theResult;
   }

   /**
    * Returns all the groups in a given userRole (not recursive).
    */
   public ArrayList getDirectGroupIdsOfUser(Connection c, int userId) throws AdminException
   {
		ResultSet rs = null;
		PreparedStatement statement = null;
	    ArrayList theResult = new ArrayList();
		String theQuery = "select " + drvSettings.getRelGIDColumnName() + 
						  " from " + drvSettings.getRelTableName() + 
						  " where " + drvSettings.getRelUIDColumnName() + " = ?";
		
		try
		{
			SilverTrace.debug("admin", "SQLGroupUserRelTable.getDirectGroupIdsOfUser", "root.MSG_QUERY", theQuery);
			statement = c.prepareStatement(theQuery);
	        statement.setInt(1,userId);
			rs = statement.executeQuery();
			while (rs.next())
			{
				theResult.add(Integer.toString(rs.getInt(1)));
			}
		}
		catch (SQLException e)
		{
		   throw new AdminException("SQLGroupUserRelTable.getDirectGroupIdsOfUser", SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", "Query = " + theQuery, e);
		}
		finally
		{
		     DBUtil.close(rs,statement);
		}
	    return theResult;
   }

   /**
    * Insert a new group row.
    */
   public int createGroupUserRel(Connection c, int groupId, int userId) throws AdminException
   {
		PreparedStatement statement = null;
		String theQuery = "insert into " + drvSettings.getRelTableName() + "(" 
						  + drvSettings.getRelGIDColumnName() + "," + drvSettings.getRelUIDColumnName() + ") " 
						  + " values (?,?)";
		
		try
		{
			SilverTrace.debug("admin", "SQLGroupUserRelTable.createGroupUserRel", "root.MSG_QUERY", theQuery);
			statement = c.prepareStatement(theQuery);
            statement.setInt(1,groupId);
            statement.setInt(2,userId);
			return statement.executeUpdate(); 
		}
		catch (SQLException e)
		{
		   throw new AdminException("SQLGroupUserRelTable.createGroupUserRel", SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", "Query = " + theQuery, e);
		}
		finally
		{
		     DBUtil.close(statement);
		}
   }

   /**
    * Insert a new group row.
    */
   public int removeGroupUserRel(Connection c, int groupId, int userId) throws AdminException
   {
		PreparedStatement statement = null;
		String theQuery = "delete from " + drvSettings.getRelTableName() 
			+ " where " + drvSettings.getRelGIDColumnName() + " = ?" 
			+ " and " + drvSettings.getRelUIDColumnName() + " = ?";

		try
		{
			SilverTrace.debug("admin", "SQLGroupUserRelTable.removeGroupUserRel", "root.MSG_QUERY", theQuery);
			statement = c.prepareStatement(theQuery);
            statement.setInt(1,groupId);
            statement.setInt(2,userId);
			return statement.executeUpdate(); 
		}
		catch (SQLException e)
		{
		   throw new AdminException("SQLGroupUserRelTable.removeGroupUserRel", SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", "Query = " + theQuery, e);
		}
		finally
		{
		     DBUtil.close(statement);
		}
   }

   /**
    * Insert a new group row.
    */
   public int removeAllUserRel(Connection c, int userId) throws AdminException
   {
		PreparedStatement statement = null;
		String theQuery = "delete from " + drvSettings.getRelTableName() 
			+ " where " + drvSettings.getRelUIDColumnName() + " = ?";

		try
		{
			SilverTrace.debug("admin", "SQLGroupUserRelTable.removeAllUserRel", "root.MSG_QUERY", theQuery);
			statement = c.prepareStatement(theQuery);
            statement.setInt(1,userId);
			return statement.executeUpdate(); 
		}
		catch (SQLException e)
		{
		   throw new AdminException("SQLGroupUserRelTable.removeAllUserRel", SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", "Query = " + theQuery, e);
		}
		finally
		{
		     DBUtil.close(statement);
		}
   }

   /**
    * Insert a new group row.
    */
   public int removeAllGroupRel(Connection c, int groupId) throws AdminException
   {
		PreparedStatement statement = null;
		String theQuery = "delete from " + drvSettings.getRelTableName() 
			+ " where " + drvSettings.getRelGIDColumnName() + " = ?";

		try
		{
			SilverTrace.debug("admin", "SQLGroupUserRelTable.removeAllGroupRel", "root.MSG_QUERY", theQuery);
			statement = c.prepareStatement(theQuery);
            statement.setInt(1,groupId);
			return statement.executeUpdate(); 
		}
		catch (SQLException e)
		{
		   throw new AdminException("SQLGroupUserRelTable.removeAllGroupRel", SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", "Query = " + theQuery, e);
		}
		finally
		{
		     DBUtil.close(statement);
		}
   }

   /**
    * Tests if a user is in given group (not recursive).
    */
   public boolean isUserDirectlyInGroup(Connection c, int userId, int groupId) throws AdminException
   {
	    ResultSet rs = null;
	    PreparedStatement statement = null;
	    String theQuery = "select " + drvSettings.getRelUIDColumnName() + 
						  " from " + drvSettings.getRelTableName() + 
						  " where " + drvSettings.getRelGIDColumnName() + " = ? AND " + drvSettings.getRelUIDColumnName() + " = ?";
	    
	    try
	    {
			SilverTrace.debug("admin", "SQLGroupUserRelTable.isUserDirectlyInGroup", "root.MSG_QUERY", theQuery);
	        statement = c.prepareStatement(theQuery);
            statement.setInt(1,groupId);
            statement.setInt(2,userId);
	        rs = statement.executeQuery();
	        return rs.next();
	    }
	    catch (SQLException e)
	    {
	    	 throw new AdminException("SQLGroupUserRelTable.isUserDirectlyInGroup", SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", "Query = " + theQuery, e);
	    }
	    finally
	    {
	         DBUtil.close(rs,statement);
	    }
   }
}
