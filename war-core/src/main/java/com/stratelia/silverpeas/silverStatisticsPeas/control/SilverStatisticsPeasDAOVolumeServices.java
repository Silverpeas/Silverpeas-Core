package com.stratelia.silverpeas.silverStatisticsPeas.control;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.instance.control.WAComponent;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

/**
 * @author BERTINL
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SilverStatisticsPeasDAOVolumeServices 
{
	private static final String DB_NAME = JNDINames.ADMIN_DATASOURCE;

	/**
	* donne les stats global pour l'enemble de tous les users cad 2 infos, la collection contient donc un seul element
	*
	* @return
	*
	* @throws SQLException
	*/
	public static Collection[] getStatsInstancesServices() throws SQLException
	{
		SilverTrace.info(
			"silverStatisticsPeas",
			"SilverStatisticsPeasDAOVolumeServices.getStatsInstancesServices",
			"root.MSG_GEN_ENTER_METHOD");
		String selectQuery =
			" SELECT componentname, count(*)"
				+ " FROM st_componentinstance"
				+ " group by componentname"
				+ " order by count(*) desc";

		return getCollectionArrayFromQuery(selectQuery);
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param rs
	 *
	 * @return
	 *
	 * @throws SQLException
	 *
	 * @see
	 */
	private static Collection[] getCollectionArrayFromResultset(ResultSet rs) throws SQLException
	{
		Vector dates = new Vector();
		Vector counts = new Vector();
		long count = 0;
		Admin admin = new Admin();
		Hashtable components = admin.getAllComponents();
		String label = null;

		while (rs.next())
		{
			WAComponent compo = (WAComponent) components.get(rs.getString(1));
			if (compo != null)
			{
				label = (compo.getLabel().indexOf("-")==-1) ? compo.getLabel() : compo.getLabel().substring(compo.getLabel().indexOf("-")+1);
				dates.add(label);
			
				count = rs.getLong(2);
				counts.add( Long.toString(count) );
			}
		}
		return new Collection[]{dates, counts};
	}
	
	/**
	 * Method declaration
	 *
	 *
	 * @param selectQuery
	 *
	 * @return
	 *
	 * @throws SQLException
	 *
	 * @see
	 */
	private static Collection[] getCollectionArrayFromQuery(String selectQuery) throws SQLException
	{
		SilverTrace.debug(
			"silverStatisticsPeas",
			"SilverStatisticsPeasDAOVolumeServices.getCollectionArrayFromQuery",
			"selectQuery=" + selectQuery);
		Statement stmt = null;
		ResultSet rs = null;
		Collection[] list = null;
		Connection myCon = getConnection();

		try
		{
			stmt = myCon.createStatement();
			rs = stmt.executeQuery(selectQuery);
			list = getCollectionArrayFromResultset(rs);
		}
		finally
		{
			DBUtil.close(rs, stmt);
			freeConnection(myCon);
		}

		return list;
	}
	
	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	private static Connection getConnection()
	{
		try
		{
			Connection con = DBUtil.makeConnection(DB_NAME);

			return con;
		}
		catch (Exception e)
		{
			throw new SilverStatisticsPeasRuntimeException(
				"SilverStatisticsPeasDAOVolumeServices.getConnection()",
				SilverpeasRuntimeException.ERROR,
				"root.EX_CONNECTION_OPEN_FAILED",
				"DbName=" + DB_NAME,
				e);
		}
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param con
	 *
	 * @see
	 */
	private static void freeConnection(Connection con)
	{
		if (con != null)
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
				SilverTrace.error(
					"silverStatisticsPeas",
					"SilverStatisticsPeasDAOVolumeServices.freeConnection()",
					"root.EX_CONNECTION_CLOSE_FAILED",
					"",
					e);
			}
		}
	}
}

