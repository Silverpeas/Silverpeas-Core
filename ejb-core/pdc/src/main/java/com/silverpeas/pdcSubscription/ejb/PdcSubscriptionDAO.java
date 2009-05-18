// TODO : reporter dans CVS (done)
/*
 * Aliaksei_Budnikau
 * Date: Oct 24, 2002
 */
package com.silverpeas.pdcSubscription.ejb;

import com.silverpeas.pdcSubscription.model.PDCSubscription;

import java.util.ArrayList;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.silverpeas.pdcSubscription.PdcSubscriptionRuntimeException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.classifyEngine.Criteria;
import com.stratelia.webactiv.util.DBUtil;

public class PdcSubscriptionDAO
{

	public final static String PDC_SUBSRIPTION_TABLE_NAME = "SB_PDC_Subscription";
	public final static String PDC_SUBSRIPTION_AXIS_TABLE_NAME = "SB_PDC_Subscription_Axis";

	public static final String GET_SUBSCRIPTION_BY_USERID_QUERY =
		"SELECT id, name, ownerId "
			+ " FROM "
			+ PDC_SUBSRIPTION_TABLE_NAME
			+ " WHERE ownerId = ? ";

	public static ArrayList getPDCSubscriptionByUserId(Connection conn, int userId)
		throws PdcSubscriptionRuntimeException, SQLException
	{
		if (conn == null)
		{
			throw new PdcSubscriptionRuntimeException(
				"PdcSubscriptionDAO.getPDCSubscriptionByUserId",
				SilverTrace.TRACE_LEVEL_DEBUG,
				"root.EX_NO_CONNECTION");
		}
		if (userId < 0)
		{
			throw new PdcSubscriptionRuntimeException(
				"PdcSubscriptionDAO.getPDCSubscriptionByUserId",
				SilverTrace.TRACE_LEVEL_DEBUG,
				"root.EX_NULL_VALUE_OBJECT_OR_PK");
		}
		ArrayList result = new ArrayList();
		PreparedStatement prepStmt = null;
		ResultSet rs = null;

		try
		{
			prepStmt = conn.prepareStatement(GET_SUBSCRIPTION_BY_USERID_QUERY);
			prepStmt.setInt(1, userId);

			rs = prepStmt.executeQuery();

			while (rs.next())
			{
				PDCSubscription sc = getSubScFromRS(rs);
				sc.setPdcContext(getCriteriasBySubscriptionID(conn, sc.getId()));
				result.add(sc);
			}
		}
		finally
		{
			DBUtil.close(rs, prepStmt);
		}

		return result;
	}

	public static final String GET_ALL_SUBSCRIPTIONS_QUERY =
		"SELECT id, name, ownerId FROM " + PDC_SUBSRIPTION_TABLE_NAME;

	public static ArrayList getAllPDCSubscriptions(Connection conn)
		throws PdcSubscriptionRuntimeException, SQLException
	{
		if (conn == null)
		{
			throw new PdcSubscriptionRuntimeException(
				"PdcSubscriptionDAO.getAllPDCSubscriptions",
				SilverTrace.TRACE_LEVEL_DEBUG,
				"root.EX_NO_CONNECTION");
		}
		ArrayList result = new ArrayList();
		PreparedStatement prepStmt = null;
		ResultSet rs = null;

		try
		{
			prepStmt = conn.prepareStatement(GET_ALL_SUBSCRIPTIONS_QUERY);

			rs = prepStmt.executeQuery();

			while (rs.next())
			{
				PDCSubscription sc = getSubScFromRS(rs);
				sc.setPdcContext(getCriteriasBySubscriptionID(conn, sc.getId()));
				result.add(sc);
			}
		}
		finally
		{
			DBUtil.close(rs, prepStmt);
		}

		return result;
	}

	private static PDCSubscription getSubScFromRS(ResultSet rs) throws SQLException, PdcSubscriptionRuntimeException
	{
		PDCSubscription result =
			new PDCSubscription(rs.getInt("id"), rs.getString("name"), null, rs.getInt("ownerId"));
		return result;
	}

	public final static String GET_CRITERIAS_BY_SC_ID_QUERY =
		"SELECT id, pdcSubscriptionId, axisId, value FROM "
			+ PDC_SUBSRIPTION_AXIS_TABLE_NAME
			+ " WHERE pdcSubscriptionId = ? ";

	private static ArrayList getCriteriasBySubscriptionID(Connection conn, int scId)
		throws PdcSubscriptionRuntimeException, SQLException
	{
		if (conn == null)
		{
			throw new PdcSubscriptionRuntimeException(
				"PdcSubscriptionDAO.getCriteriasBySubscriptionID",
				SilverTrace.TRACE_LEVEL_DEBUG,
				"root.EX_NO_CONNECTION");
		}
		ArrayList result = new ArrayList();
		PreparedStatement prepStmt = null;
		ResultSet rs = null;

		if (scId < 0)
		{
			return result;
		}

		try
		{
			prepStmt = conn.prepareStatement(GET_CRITERIAS_BY_SC_ID_QUERY);
			prepStmt.setInt(1, scId);

			rs = prepStmt.executeQuery();

			while (rs.next())
			{
				Criteria sc = getSCFromRS(rs);
				result.add(sc);
			}

		}
		finally
		{
			DBUtil.close(rs, prepStmt);
		}

		return result;
	}

	private static Criteria getSCFromRS(ResultSet rs) throws SQLException, PdcSubscriptionRuntimeException
	{
		Criteria result = new Criteria(rs.getInt("axisId"), rs.getString("value"));
		return result;
	}

	public static final String GET_SUBSCRIPTION_BY_ID_QUERY =
		"SELECT id, name, ownerId FROM "
			+ PDC_SUBSRIPTION_TABLE_NAME
			+ " WHERE id = ? ";

	public static PDCSubscription getPDCSubsriptionById(Connection conn, int id)
		throws PdcSubscriptionRuntimeException, SQLException
	{
		if (conn == null)
		{
			throw new PdcSubscriptionRuntimeException(
				"PdcSubscriptionDAO.PDCSubsriptionById",
				SilverTrace.TRACE_LEVEL_DEBUG,
				"root.EX_NO_CONNECTION");
		}
		if (id < 0)
		{
			throw new PdcSubscriptionRuntimeException(
				"PdcSubscriptionDAO.PDCSubsriptionById",
				SilverTrace.TRACE_LEVEL_DEBUG,
				"root.EX_WRONG_PK");
		}
		PDCSubscription result = null;
		PreparedStatement prepStmt = null;
		ResultSet rs = null;

		try
		{
			prepStmt = conn.prepareStatement(GET_SUBSCRIPTION_BY_ID_QUERY);
			prepStmt.setInt(1, id);

			rs = prepStmt.executeQuery();

			if (rs.next())
			{
				result = getSubScFromRS(rs);
				result.setPdcContext(getCriteriasBySubscriptionID(conn, result.getId()));
			}

		}
		finally
		{
			DBUtil.close(rs, prepStmt);
		}
		return result;
	}

	public final static String CREATE_PDCSUBSCR_QUERY =
		"INSERT INTO " + PDC_SUBSRIPTION_TABLE_NAME + " (id, name, ownerId ) VALUES (?, ?, ?)";

	public static int createPDCSubscription(Connection conn, PDCSubscription subscription)
		throws PdcSubscriptionRuntimeException, SQLException
	{
		if (conn == null)
		{
			throw new PdcSubscriptionRuntimeException(
				"PdcSubscriptionDAO.createPDCSubscription",
				SilverTrace.TRACE_LEVEL_DEBUG,
				"root.EX_NO_CONNECTION");
		}
		if (subscription == null)
		{
			throw new PdcSubscriptionRuntimeException(
				"PdcSubscriptionDAO.createPDCSubscription",
				SilverTrace.TRACE_LEVEL_DEBUG,
				"root.EX_NULL_VALUE_OBJECT_OR_PK");
		}
		PreparedStatement prepStmt = null;
		int newId = -1;

		try
		{
			newId = DBUtil.getNextId(PDC_SUBSRIPTION_TABLE_NAME, new String("id"));
		}
		catch (Exception e)
		{
			throw new PdcSubscriptionRuntimeException(
				"PdcSubscriptionDAO.createPDCSubscription",
				SilverTrace.TRACE_LEVEL_DEBUG,
				"root.EX_GET_NEXTID_FAILED",
				PDC_SUBSRIPTION_TABLE_NAME,
				e);
		}

		try
		{
			prepStmt = conn.prepareStatement(CREATE_PDCSUBSCR_QUERY);

			prepStmt.setInt(1, newId);
			prepStmt.setString(2, subscription.getName());
			prepStmt.setInt(3, subscription.getOwnerId());

			int rownum = prepStmt.executeUpdate();
			if (rownum < 1)
			{
				throw new PdcSubscriptionRuntimeException(
					"PdcSubscriptionDAO.createPDCSubscription",
					SilverTrace.TRACE_LEVEL_DEBUG,
					"root.EX_RECORD_INSERTION_FAILED",
					subscription);
			}

			ArrayList ctx = subscription.getPdcContext();

			if (ctx != null && ctx.size() != 0)
			{
				createSearchCriterias(conn, ctx, newId);
			}

		}
		finally
		{
			DBUtil.close(prepStmt);
		}

		return newId;
	}

	public final static String CREATE_PDC_SEARCHCRITERIA_QUERY =
		"INSERT INTO "
			+ PDC_SUBSRIPTION_AXIS_TABLE_NAME
			+ " (id, pdcSubscriptionId, axisId, value) VALUES (?, ?, ?, ?)";

	private static void createSearchCriterias(Connection conn, ArrayList searchCriterias, int subscriptionId)
		throws PdcSubscriptionRuntimeException, SQLException
	{
		if (conn == null)
		{
			throw new PdcSubscriptionRuntimeException(
				"PdcSubscriptionDAO.createSearchCriterias",
				SilverTrace.TRACE_LEVEL_DEBUG,
				"root.EX_NO_CONNECTION");
		}
		if (searchCriterias == null)
		{
			throw new PdcSubscriptionRuntimeException(
				"PdcSubscriptionDAO.createSearchCriterias",
				SilverTrace.TRACE_LEVEL_DEBUG,
				"root.EX_NULL_VALUE_OBJECT_OR_PK");
		}
		if (searchCriterias.size() == 0)
		{
			return;
		}

		PreparedStatement prepStmt = null;
		int newId = -1;

		try
		{
			prepStmt = conn.prepareStatement(CREATE_PDC_SEARCHCRITERIA_QUERY);

			for (int i = 0; i < searchCriterias.size(); i++)
			{
				Criteria sc = (Criteria) searchCriterias.get(i);

				try
				{
					newId = DBUtil.getNextId(PDC_SUBSRIPTION_AXIS_TABLE_NAME, new String("id"));
				}
				catch (Exception e)
				{
					throw new PdcSubscriptionRuntimeException(
						"PdcSubscriptionDAO.createSearchCriterias",
						SilverTrace.TRACE_LEVEL_DEBUG,
						"root.EX_GET_NEXTID_FAILED",
						PDC_SUBSRIPTION_AXIS_TABLE_NAME,
						e);
				}

				prepStmt.setInt(1, newId);
				prepStmt.setInt(2, subscriptionId);
				prepStmt.setInt(3, sc.getAxisId());
				prepStmt.setString(4, sc.getValue());

				int rownum = prepStmt.executeUpdate();
				if (rownum < 1)
				{
					throw new PdcSubscriptionRuntimeException(
						"PdcSubscriptionDAO.createSearchCriterias",
						SilverTrace.TRACE_LEVEL_DEBUG,
						"root.EX_RECORD_INSERTION_FAILED",
						sc);
				}
			}
		}
		finally
		{
			DBUtil.close(prepStmt);
		}
	}

	public final static String UPDATE_PDC_SUBSCR_QUERY =
		"UPDATE " + PDC_SUBSRIPTION_TABLE_NAME + " SET name = ? , ownerId = ? WHERE id = ? ";

	public static void updatePDCSubscription(Connection conn, PDCSubscription subscription)
		throws PdcSubscriptionRuntimeException, SQLException
	{
		if (conn == null)
		{
			throw new PdcSubscriptionRuntimeException(
				"PdcSubscriptionDAO.updatePDCSubscription",
				SilverTrace.TRACE_LEVEL_DEBUG,
				"root.EX_NO_CONNECTION");
		}
		if (subscription == null)
		{
			throw new PdcSubscriptionRuntimeException(
				"PdcSubscriptionDAO.updatePDCSubscription",
				SilverTrace.TRACE_LEVEL_DEBUG,
				"root.EX_NULL_VALUE_OBJECT_OR_PK");
		}
		PreparedStatement prepStmt = null;

		try
		{
			prepStmt = conn.prepareStatement(UPDATE_PDC_SUBSCR_QUERY);
			prepStmt.setString(1, subscription.getName());
			prepStmt.setInt(2, subscription.getOwnerId());
			prepStmt.setInt(3, subscription.getId());

			int rownum = prepStmt.executeUpdate();
			if (rownum < 1)
			{
				throw new PdcSubscriptionRuntimeException(
					"PdcSubscriptionDAO.updatePDCSubscription",
					SilverTrace.TRACE_LEVEL_DEBUG,
					"root.EX_RECORD_UPDATE_FAILED",
					subscription);
			}

			ArrayList ctx = subscription.getPdcContext();
			removeSearchCriterias(conn, subscription.getId());
			if (ctx != null && ctx.size() != 0)
			{
				createSearchCriterias(conn, ctx, subscription.getId());
			}

		}
		finally
		{
			DBUtil.close(prepStmt);
		}
	}

	public final static String REMOVE_SUBSCR_BYID_QUERY = "delete from " + PDC_SUBSRIPTION_TABLE_NAME + " where id = ? ";

	public static void removePDCSubscriptionById(Connection conn, int id)
		throws PdcSubscriptionRuntimeException, SQLException
	{
		if (conn == null)
		{
			throw new PdcSubscriptionRuntimeException(
				"PdcSubscriptionDAO.removePDCSubscriptionById",
				SilverTrace.TRACE_LEVEL_DEBUG,
				"root.EX_NO_CONNECTION");
		}
		if (id < 0)
		{
			throw new PdcSubscriptionRuntimeException(
				"PdcSubscriptionDAO.removePDCSubscriptionById",
				SilverTrace.TRACE_LEVEL_DEBUG,
				"root.EX_WRONG_PK");
		}
		PreparedStatement prepStmt = null;

		try
		{
			prepStmt = conn.prepareStatement(REMOVE_SUBSCR_BYID_QUERY);

			prepStmt.setInt(1, id);

			int rownum = prepStmt.executeUpdate();
			removeSearchCriterias(conn, id);

			if (rownum < 1)
			{
				throw new PdcSubscriptionRuntimeException(
					"PdcSubscriptionDAO.removePDCSubscriptionById",
					SilverTrace.TRACE_LEVEL_DEBUG,
					"root.EX_RECORD_NOTFOUND",
					String.valueOf(id));
			}

		}
		finally
		{
			DBUtil.close(prepStmt);
		}
	}

	public final static String REMOVE_SCS_QUERY =
		"delete from " + PDC_SUBSRIPTION_AXIS_TABLE_NAME + " where pdcSubscriptionId = ? ";

	private static void removeSearchCriterias(Connection conn, int subscrID)
		throws PdcSubscriptionRuntimeException, SQLException
	{
		if (conn == null)
		{
			throw new PdcSubscriptionRuntimeException(
				"PdcSubscriptionDAO.removeSearchCriterias",
				SilverTrace.TRACE_LEVEL_DEBUG,
				"root.EX_NO_CONNECTION");
		}
		if (subscrID < 0)
		{
			throw new PdcSubscriptionRuntimeException(
				"PdcSubscriptionDAO.removeSearchCriterias",
				SilverTrace.TRACE_LEVEL_DEBUG,
				"root.EX_WRONG_PK");
		}
		PreparedStatement prepStmt = null;

		try
		{
			prepStmt = conn.prepareStatement(REMOVE_SCS_QUERY);
			prepStmt.setInt(1, subscrID);

			prepStmt.executeUpdate();
		}
		finally
		{
			DBUtil.close(prepStmt);
		}
	}

	public static void removePDCSubscriptionById(Connection conn, int[] ids)
		throws PdcSubscriptionRuntimeException, SQLException
	{
		if (conn == null)
		{
			throw new PdcSubscriptionRuntimeException(
				"PdcSubscriptionDAO.removePDCSubscriptionById",
				SilverTrace.TRACE_LEVEL_DEBUG,
				"root.EX_NO_CONNECTION");
		}
		if (ids == null)
		{
			throw new PdcSubscriptionRuntimeException(
				"PdcSubscriptionDAO.removePDCSubscriptionById",
				SilverTrace.TRACE_LEVEL_DEBUG,
				"root.EX_NULL_VALUE_OBJECT_OR_PK");
		}

		for (int i = 0; i < ids.length; i++)
		{
			int id = ids[i];
			removePDCSubscriptionById(conn, id);
		}
	}

	public final static String FIND_SUBSCRIPTION_BY_AXIS_QUERY =
		"SELECT pdcSubscriptionId FROM " + PDC_SUBSRIPTION_AXIS_TABLE_NAME + " WHERE axisId = ? ";

	public static ArrayList getPDCSubscriptionByUsedAxis(Connection conn, int axisId)
		throws PdcSubscriptionRuntimeException, SQLException
	{
		if (conn == null)
		{
			throw new PdcSubscriptionRuntimeException(
				"PdcSubscriptionDAO.getPDCSubscriptionByUsedAxis",
				SilverTrace.TRACE_LEVEL_DEBUG,
				"root.EX_NO_CONNECTION");
		}
		if (axisId < 0)
		{
			throw new PdcSubscriptionRuntimeException(
				"PdcSubscriptionDAO.getPDCSubscriptionByUsedAxis",
				SilverTrace.TRACE_LEVEL_DEBUG,
				"root.EX_NULL_VALUE_OBJECT_OR_PK");
		}
		ArrayList result = new ArrayList();

		ArrayList ids = new ArrayList();

		PreparedStatement prepStmt = null;
		ResultSet rs = null;

		try
		{
			prepStmt = conn.prepareStatement(FIND_SUBSCRIPTION_BY_AXIS_QUERY);
			prepStmt.setInt(1, axisId);
			rs = prepStmt.executeQuery();

			while (rs.next())
			{
				Integer subscrId = new Integer(rs.getInt(1));
				if (!ids.contains(subscrId))
				{
					ids.add(subscrId);
				}
			}

			for (int i = 0; i < ids.size(); i++)
			{
				int subscrId = ((Integer) ids.get(i)).intValue();
				PDCSubscription subscription = getPDCSubsriptionById(conn, subscrId);
				result.add(subscription);
			}
		}
		finally
		{
			DBUtil.close(rs, prepStmt);
		}

		return result;
	}
}
