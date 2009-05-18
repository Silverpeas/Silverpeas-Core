// TODO : reporter dans CVS (done)
package com.stratelia.webactiv.util.statistic.ejb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.statistic.model.HistoryNodePublicationActorDetail;
import com.stratelia.webactiv.util.statistic.model.StatisticResultDetail;
import com.stratelia.webactiv.util.statistic.model.StatisticRuntimeException;

/*
 * CVS Informations
 * 
 * $Id: HistoryNodePublicationActorDAO.java,v 1.4 2007/06/14 08:37:55 neysseri Exp $
 * 
 * $Log: HistoryNodePublicationActorDAO.java,v $
 * Revision 1.4  2007/06/14 08:37:55  neysseri
 * no message
 *
 * Revision 1.3.6.1  2007/06/14 08:22:38  neysseri
 * no message
 *
 * Revision 1.3  2003/11/25 08:42:38  cbonin
 * no message
 *
 * Revision 1.2  2003/11/24 13:26:30  cbonin
 * no message
 *
 * Revision 1.1.1.1  2002/08/06 14:47:53  nchaix
 * no message
 *
 * Revision 1.10  2002/01/22 09:25:48  mguillem
 * Stabilisation Lot2
 * Réorganisation des Router et SessionController
 * Suppression dans les fichiers *Exception de 'implements FromModule'
 *
 * Revision 1.9  2001/12/26 12:01:47  nchaix
 * no message
 *
 */
 
/**
 * Class declaration
 *
 *
 * @author
 */
public class HistoryNodePublicationActorDAO
{
	static SimpleDateFormat formater = new SimpleDateFormat("yyyy/MM/dd");

	/**
	 * Method declaration
	 *
	 *
	 * @param rs
	 * @param space
	 * @param componentName
	 *
	 * @return
	 *
	 * @throws SQLException
	 *
	 * @see
	 */
	public static Collection getHistoryDetails(ResultSet rs, String space, String componentName) throws SQLException
	{
		ArrayList      list = new ArrayList();
		java.util.Date date;
		String         actorId = "";
		String         nodeId = "";
		String         pubId = "";

		while (rs.next())
		{
			try
			{
				date = formater.parse(rs.getString(1));
			}
			catch (java.text.ParseException e)
			{
		throw new StatisticRuntimeException("HistoryNodePublicationActorDAO.getHistoryDetails()", SilverpeasRuntimeException.ERROR, "statistic.INCORRECT_DATE", e);
			}
			actorId = rs.getString(2);
			nodeId = String.valueOf(rs.getInt(3));
			pubId = String.valueOf(rs.getInt(4));
			NodePK                            nodePK = new NodePK(nodeId, space, componentName);
			PublicationPK                     pubPK = new PublicationPK(pubId, space, componentName);
			HistoryNodePublicationActorDetail detail = new HistoryNodePublicationActorDetail(date, actorId, nodePK, pubPK);

			list.add(detail);
		}
		return list;
	}

	/* cette classe ne devrait jamais etre instanciee */

	/**
	 * Constructor declaration
	 *
	 *
	 * @see
	 */
	public HistoryNodePublicationActorDAO() {}


	/**
	 * Get descendant node PKs of a node
	 * @return A collection of NodePK
	 * @param con A connection to the database
	 * @param nodePK A NodePK
	 * @see com.stratelia.webactiv.util.node.model.NodePK
	 * @exception java.sql.SQLException
	 * @since 1.0
	 */
	private static Collection getDescendantPKs(Connection con, NodePK nodePK) throws SQLException
	{

		String            path = null;
		ArrayList         a = new ArrayList();
		String            selectQuery = "select nodePath from " + nodePK.getTableName() + "  where nodeId = ? and instanceId = ?";

		PreparedStatement prepStmt = null;
		ResultSet         rs = null;

		try
		{
			prepStmt = con.prepareStatement(selectQuery);
			prepStmt.setInt(1, new Integer(nodePK.getId()).intValue());
			prepStmt.setString(2, nodePK.getComponentName());
			rs = prepStmt.executeQuery();
			if (rs.next())
			{
				path = rs.getString(1);
			}
		}
		finally
		{
			DBUtil.close(rs, prepStmt);
		}


		if (path != null)
		{
			path = path + "/" + "%";

			selectQuery = "select nodeId " + "from " + nodePK.getTableName() + " where nodePath like '" + path + "'" + " and instanceId = ? order by nodeId";

			try
			{
				prepStmt = con.prepareStatement(selectQuery);
				prepStmt.setString(1, nodePK.getComponentName());
				rs = prepStmt.executeQuery();
				String nodeId = "";

				while (rs.next())
				{
					nodeId = new Integer(rs.getInt(1)).toString();
					NodePK n = new NodePK(nodeId, nodePK);

					a.add(n);  /* Stockage du sous thème */
				}				
			}
			finally
			{
				DBUtil.close(rs, prepStmt);
			}
		}

		return a;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param con
	 * @param tableName
	 * @param fatherPK
	 *
	 * @return
	 *
	 * @throws SQLException
	 *
	 * @see
	 */
	public static Collection getNodesUsage(Connection con, String tableName, NodePK fatherPK) throws SQLException
	{
		SilverTrace.info("statistic", "HistoryNodePublicationActorDAO.getNodesUsage", "root.MSG_GEN_ENTER_METHOD");

		Collection        sonPK_list = null;
		ResultSet         rs = null;
		PreparedStatement prepStmt = null;
		String            selectQuery = "select count(nodeId) from " + tableName + " where nodeId=?";

		String            nodeId = "";
		
		try
		{
			// get all descendant of a one NodePK
			sonPK_list = getDescendantPKs(con, fatherPK);
			// verify that the Collection object return is not empty;
			ArrayList nodesUsage = new ArrayList();
			if (!sonPK_list.isEmpty())
			{
				Iterator iterator = sonPK_list.iterator();

				prepStmt = con.prepareStatement(selectQuery);
				// for each descendant, we compute the number of them in the SB_Publication_Histaory table
				for (; iterator.hasNext(); )
				{
					nodeId = ((NodePK) iterator.next()).getId();
					prepStmt.setInt(1, (new Integer(nodeId)).intValue());
					rs = prepStmt.executeQuery();
					// get the result
					if (rs.next())
					{
						NodePK                nodePK = new NodePK(nodeId, fatherPK);
						StatisticResultDetail detail = new StatisticResultDetail(nodePK, String.valueOf(rs.getInt(1)));

						nodesUsage.add(detail);
					}
				}				
			}
			return nodesUsage;
		}
		finally
		{
			DBUtil.close(rs, prepStmt);
		}	
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param con
	 * @param tableName
	 * @param userId
	 * @param nodePK
	 * @param pubPK
	 *
	 * @throws SQLException
	 *
	 * @see
	 */
	public static void add(Connection con, String tableName, String userId, NodePK nodePK, PublicationPK pubPK) throws SQLException
	{
		SilverTrace.info("statistic", "HistoryNodePublicationActorDAO.add", "root.MSG_GEN_ENTER_METHOD");

		String            insertStatement = "insert into " + tableName + " values (?, ?, ?, ? )";
		PreparedStatement prepStmt = null;

		try
		{
			prepStmt = con.prepareStatement(insertStatement);
			prepStmt.setString(1, formater.format(new java.util.Date()));
			prepStmt.setString(2, userId);
			prepStmt.setInt(3, new Integer(nodePK.getId()).intValue());
			prepStmt.setInt(4, new Integer(pubPK.getId()).intValue());
			prepStmt.executeUpdate();
		}
		finally
		{
			DBUtil.close(prepStmt);
		}
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param con
	 * @param tableName
	 * @param pubPK
	 *
	 * @return
	 *
	 * @throws SQLException
	 *
	 * @see
	 */
	public static Collection getHistoryDetailByPublication(Connection con, String tableName, PublicationPK pubPK) throws SQLException
	{
		SilverTrace.info("statistic", "HistoryNodePublicationActorDAO.getHistoryDetailByPublication", "root.MSG_GEN_ENTER_METHOD");
		String     space = pubPK.getSpace();
		String     componentName = pubPK.getComponentName();
		String     selectStatement = "select * " + "from " + tableName + " where pubId=" + pubPK.getId();

		Statement  stmt = null;
		ResultSet  rs = null;		
		try
		{
			stmt = con.createStatement();
			rs = stmt.executeQuery(selectStatement);
			Collection list = getHistoryDetails(rs, space, componentName);
			return list;
		}
		finally
		{
			DBUtil.close(rs, stmt);
		}
	}
}
