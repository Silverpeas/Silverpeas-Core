/*
 * NewsInstanciator.java
 *
 * Created on 13 juillet 2000, 09:54
 */

package com.stratelia.webactiv.questionContainer;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.stratelia.webactiv.beans.admin.SQLRequest;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 *
 * @author  squere
 * @version
 * update by  Sébastien Antonio - Externalisation of the SQL request
 */
public class QuestionContainerInstanciator extends SQLRequest
{
    /** Creates new QuestionContainerInstanciator */
    public QuestionContainerInstanciator(){}
    public QuestionContainerInstanciator(String fullPathName){
		super("com.stratelia.webactiv.util.questionContainer");
    }

    public void create(Connection con, String spaceId, String componentId, String userId)
      throws InstanciationException
    {
        
    }

    public void delete(Connection con, String spaceId, String componentId, String userId)
      throws InstanciationException
    {
		// read the property file which contains all SQL queries to delete rows
		setDeleteQueries();

		deleteDataOfInstance(con, componentId, "Comment");
		deleteDataOfInstance(con, componentId, "QuestionContainer");
    }

 	/**
	* Delete all data of one forum instance from the forum table.
	* @param con (Connection) the connection to the data base
	* @param componentId (String) the instance id of the Silverpeas component forum.
	* @param suffixName (String) the suffixe of a Forum table
	*/
	private void deleteDataOfInstance(Connection con, String componentId, String suffixName) throws InstanciationException {
		
		Statement stmt = null;
		

		// get the delete query from the external file
		String deleteQuery = getDeleteQuery(componentId,suffixName);
		
		// execute the delete query
		try {
			stmt = con.createStatement();
		    stmt.executeUpdate(deleteQuery);
		} catch (SQLException se) {
			throw new InstanciationException("QuestionContainerInstanciator.deleteDataOfInstance()", SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", se);
		}
		finally {
			try{
				stmt.close();
			} catch (SQLException err_closeStatement){
				throw new InstanciationException("QuestionContainerInstanciator.deleteDataOfInstance()", SilverpeasException.ERROR, "root.EX_RESOURCE_CLOSE_FAILED", err_closeStatement);
			}
		}		
	}

}