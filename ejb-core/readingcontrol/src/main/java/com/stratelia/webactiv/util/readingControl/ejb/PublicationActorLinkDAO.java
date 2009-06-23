// TODO : reporter dans CVS (done)
package com.stratelia.webactiv.util.readingControl.ejb;

import java.sql.*;
import java.util.*;

import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.publication.model.*;
import com.stratelia.webactiv.util.readingControl.model.PublicationActorLinkDetail;
import com.stratelia.silverpeas.silvertrace.*;

public class PublicationActorLinkDAO
{

	/* cette classe ne devrait jamais etre instanciee */
	public PublicationActorLinkDAO()
	{
	}

	public static void add(Connection con, String tableName, String userId, PublicationPK pubPK) throws SQLException
	{
		String insertStatement = "insert into " + tableName + " values (? , ? , ? , ? )";
		PreparedStatement prepStmt = con.prepareStatement(insertStatement);
		try
		{
			prepStmt.setInt(1, new Integer(pubPK.getId()).intValue());
			prepStmt.setString(2, userId);
			prepStmt.setString(3, pubPK.getSpace());
			prepStmt.setString(4, pubPK.getComponentName());
			prepStmt.executeUpdate();
		}
		catch (SQLException se)
		{
			SilverTrace.error(
				"readingControl",
				"PublicationActorLinkDAO.add()",
				"root.EX_RECORD_INSERTION_FAILED",
				"pubPK = " + pubPK.toString(),
				se);
			throw se;
		}
		finally
		{
			DBUtil.close(prepStmt);
		}
	}

	public static Collection getReadingControlDetails(ResultSet rs) throws SQLException
	{
		ArrayList list = new ArrayList();
		while (rs.next())
		{
			String pubId = String.valueOf(rs.getInt(1));
			String actorId = rs.getString(2);
			String space = rs.getString(3);
			String componentName = rs.getString(4);
			PublicationPK pubPK = new PublicationPK(pubId, space, componentName);
			PublicationActorLinkDetail detail = new PublicationActorLinkDetail(actorId, pubPK);
			list.add(detail);
		}
		return list;
	}

	public static Collection getReadingControls(Connection con, String tableName, PublicationPK pubPK)
		throws SQLException
	{
		SilverTrace.info("readingControl", "PublicationActorLinkDAO.getReadingControls()", "root.MSG_GEN_ENTER_METHOD");
		//String space = pubPK.getSpace();
		String componentName = pubPK.getComponentName();
		String selectStatement = "select actorId " + " from " + tableName + " where pubId = ? and componentName = ?";

		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try
		{
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, new Integer(pubPK.getId()).intValue());
			prepStmt.setString(2, componentName);
			rs = prepStmt.executeQuery();
			ArrayList list = new ArrayList();
			while (rs.next())
			{
				list.add(rs.getString(1));
			}
			SilverTrace.info("readingControl", "PublicationActorLinkDAO.getReadingControls()", "root.MSG_GEN_EXIT_METHOD");
			return list;
		}
		catch (SQLException se)
		{
			SilverTrace.error(
				"readingControl",
				"PublicationActorLinkDAO.getReadingControls()",
				"root.EX_RECORD_NOT_FOUND",
				"pubPK = " + pubPK.toString(),
				se);
			throw se;
		}
		finally
		{
			DBUtil.close(rs, prepStmt);
		}
	}

	public static void addReadingControls(Connection con, String tableName, Collection userIds, PublicationPK pubPK)
		throws SQLException
	{
		SilverTrace.info("readingControl", "PublicationActorLinkDAO.addReadingControls()", "root.MSG_GEN_ENTER_METHOD");
		Iterator iterator = userIds.iterator();
		while (iterator.hasNext())
		{
			add(con, tableName, (String) iterator.next(), pubPK);
		}
		SilverTrace.info("readingControl", "PublicationActorLinkDAO.addReadingControls()", "root.MSG_GEN_EXIT_METHOD");
	}

	public static void remove(Connection con, String tableName, Collection userIds, PublicationPK pubPK)
		throws SQLException
	{
		SilverTrace.info("readingControl", "PublicationActorLinkDAO.remove()", "root.MSG_GEN_ENTER_METHOD");
		SilverTrace.info("readingControl", "PublicationActorLinkDAO.remove()", "root.MSG_GEN_EXIT_METHOD");
	}

	public static void removeByUser(Connection con, String tableName, String userId) throws SQLException
	{
		SilverTrace.info("readingControl", "PublicationActorLinkDAO.removeByUser()", "root.MSG_GEN_ENTER_METHOD");
		String insertStatement = "delete from " + tableName + " where actorId = ?";
		PreparedStatement prepStmt = null;
		try
		{
			prepStmt = con.prepareStatement(insertStatement);
			prepStmt.setString(1, userId);
			prepStmt.executeUpdate();
		}
		catch (SQLException se)
		{
			SilverTrace.error(
				"readingControl",
				"PublicationActorLinkDAO.removeByUser()",
				"root.EX_RECORD_DELETE_FAILED",
				"userId = " + userId,
				se);
			throw se;
		}
		finally
		{
			DBUtil.close(prepStmt);
		}
		SilverTrace.info("readingControl", "PublicationActorLinkDAO.removeByUser()", "root.MSG_GEN_EXIT_METHOD");
	}

	public static void removeByPublication(Connection con, String tableName, PublicationPK pubPK) throws SQLException
	{
		SilverTrace.info(
			"readingControl",
			"PublicationActorLinkDAO.removeByPublication()",
			"root.MSG_GEN_ENTER_METHOD");
		String insertStatement = "delete from " + tableName + " where pubId =  ? and " + " componentName = ?";

		PreparedStatement prepStmt = null;
		try
		{
			prepStmt = con.prepareStatement(insertStatement);
			prepStmt.setInt(1, new Integer(pubPK.getId()).intValue());
			prepStmt.setString(2, pubPK.getComponentName());
			prepStmt.executeUpdate();
		}
		catch (SQLException se)
		{
			SilverTrace.error(
				"readingControl",
				"PublicationActorLinkDAO.removeByPublication()",
				"root.EX_RECORD_DELETE_FAILED",
				"pubPK = " + pubPK.toString(),
				se);
			throw se;
		}
		finally
		{
			DBUtil.close(prepStmt);
		}
		SilverTrace.info("readingControl", "PublicationActorLinkDAO.removeByPublication()", "root.MSG_GEN_EXIT_METHOD");
	}
}