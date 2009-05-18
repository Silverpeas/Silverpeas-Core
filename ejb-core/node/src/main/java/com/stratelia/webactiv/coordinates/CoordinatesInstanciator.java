/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.coordinates;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.util.exception.SilverpeasException;


/**
 * @author
 * update by  Sébastien Antonio - Externalisation of the SQL request
 */
public class CoordinatesInstanciator extends SQLRequest
{

	/**
	 * Constructor declaration
	 *
	 *
	 * @see
	 */
	public CoordinatesInstanciator() {}

	// add by sébastien

	/**
	 * Constructor declaration
	 *
	 *
	 * @param fullPathName
	 *
	 * @see
	 */
	public CoordinatesInstanciator(String fullPathName)
	{
		super("com.stratelia.webactiv.util.coordinates");
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param con
	 * @param spaceId
	 * @param componentId
	 * @param userId
	 *
	 * @throws InstanciationException
	 *
	 * @see
	 */
	public void create(Connection con, String spaceId, String componentId, String userId) throws InstanciationException {}

	/**
	 * Method declaration
	 *
	 *
	 * @param con
	 * @param spaceId
	 * @param componentId
	 * @param userId
	 *
	 * @throws InstanciationException
	 *
	 * @see
	 */
	public void delete(Connection con, String spaceId, String componentId, String userId) throws InstanciationException
	{
		SilverTrace.info("coordinates", "CoordinatesInstanciator.delete()", "root.MSG_GEN_PARAM_VALUE", "delete called with space = " + spaceId + " and component = " + componentId);

		setDeleteQueries();

		deleteDataOfInstance(con, componentId, "Coordinates");
	}

	/**
	 * Delete all data of one forum instance from the forum table.
	 * @param con (Connection) the connection to the data base
	 * @param componentId (String) the instance id of the Silverpeas component forum.
	 * @param suffixName (String) the suffixe of a Forum table
	 */
	private void deleteDataOfInstance(Connection con, String componentId, String suffixName) throws InstanciationException
	{

		Statement stmt = null;

		// get the delete query from the external file
		String	  deleteQuery = getDeleteQuery(componentId, suffixName);

		// execute the delete query
		try
		{
			stmt = con.createStatement();
			stmt.executeUpdate(deleteQuery);
		}
		catch (SQLException se)
		{
			throw new InstanciationException("CoordinatesInstanciator.deleteDataOfInstance()", SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", se);
		}
		finally
		{
			try
			{
				stmt.close();
			}
			catch (SQLException err_closeStatement)
			{
				SilverTrace.error("coordinates", "CoordinatesInstanciator.deleteDataOfInstance()", "root.EX_RESOURCE_CLOSE_FAILED", "", err_closeStatement);
			}
		}

	}

}
