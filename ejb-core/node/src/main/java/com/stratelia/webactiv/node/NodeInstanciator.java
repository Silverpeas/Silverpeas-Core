/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * NewsInstanciator.java
 *
 * Created on 13 juillet 2000, 09:54
 */

package com.stratelia.webactiv.node;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.silverpeas.admin.components.InstanciationException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class NodeInstanciator extends SQLRequest {
  /** Creates new NewsInstanciator */
  public NodeInstanciator() {
  }

  public NodeInstanciator(String fullPathName) {
    super("com.stratelia.webactiv.util.node");
  }

  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {

  }

  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("node", "NodeInstanciator.delete()",
        "root.MSG_GEN_ENTER_METHOD", "spaceId = " + spaceId
        + ", componentId = " + componentId);

    // read the property file which contains all SQL queries to delete rows
    setDeleteQueries();

    deleteDataOfInstance(con, componentId, "Node");
    deleteFavorites(con, spaceId, componentId);
    SilverTrace.info("node", "NodeInstanciator.delete()",
        "root.MSG_GEN_EXIT_METHOD", "spaceId = " + spaceId + ", componentId = "
        + componentId);
  }

  private void deleteFavorites(Connection con, String spaceId,
      String componentId) throws InstanciationException {
    PreparedStatement prepStmt = null;
    String deleteStatement = "delete from favorit where componentName = '"
        + componentId + "'";
    try {
      prepStmt = con.prepareStatement(deleteStatement);
      prepStmt.executeUpdate();
    } catch (SQLException se) {
      throw new InstanciationException("NodeInstanciator.deleteFavorites()",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", se);
    } finally {
      try {
        prepStmt.close();
      } catch (SQLException err_closeStatement) {
        SilverTrace.error("node", "NodeInstanciator.deleteFavorites()",
            "root.EX_RESOURCE_CLOSE_FAILED", "", err_closeStatement);
      }
    }
  }

  /**
   * Delete all data of one forum instance from the forum table.
   * @param con (Connection) the connection to the data base
   * @param componentId (String) the instance id of the Silverpeas component forum.
   * @param suffixName (String) the suffixe of a Forum table
   */
  private void deleteDataOfInstance(Connection con, String componentId,
      String suffixName) throws InstanciationException {

    Statement stmt = null;

    // get the delete query from the external file
    String deleteQuery = getDeleteQuery(componentId, suffixName);
    // execute the delete query
    try {
      stmt = con.createStatement();
      stmt.executeUpdate(deleteQuery);
    } catch (SQLException se) {
      throw new InstanciationException(
          "NodeInstanciator.deleteDataOfInstance()", SilverpeasException.ERROR,
          "root.EX_SQL_QUERY_FAILED", se);
    } finally {
      try {
        stmt.close();
      } catch (SQLException err_closeStatement) {
        SilverTrace.error("node", "NodeInstanciator.deleteDataOfInstance()",
            "root.EX_RESOURCE_CLOSE_FAILED", "", err_closeStatement);
      }
    }
  }

}