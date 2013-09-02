/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.node;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.silverpeas.admin.components.InstanciationException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.node.control.AnonymousMethodOnNode;
import com.stratelia.webactiv.util.node.control.NodeDeletion;
import com.stratelia.webactiv.util.node.model.NodePK;

public class NodeInstanciator extends SQLRequest {

  private static AnonymousMethodOnNode NO_METHOD = null;

  /**
   * Creates new NewsInstanciator
   */
  public NodeInstanciator() {
  }

  public NodeInstanciator(String fullPathName) {
    super("com.stratelia.webactiv.util.node");
  }

  public void create(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
  }

  public void delete(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
    SilverTrace.info("node", "NodeInstanciator.delete()", "root.MSG_GEN_ENTER_METHOD",
        "spaceId = " + spaceId + ", componentId = " + componentId);
    deleteNodes(con, componentId);
    deleteFavorites(con, componentId);
    SilverTrace.info("node", "NodeInstanciator.delete()", "root.MSG_GEN_EXIT_METHOD",
        "spaceId = " + spaceId + ", componentId = " + componentId);
  }

  private void deleteNodes(Connection connection, String componentId) throws InstanciationException {
    try {
      NodePK pk = new NodePK("0", componentId);
      NodeDeletion.deleteNodes(pk, connection, NO_METHOD);
    } catch (Exception ex) {
      throw new InstanciationException("NodeInstanciator.deleteNodes()",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", ex);
    }
  }

  private void deleteFavorites(Connection con, String componentId) throws
      InstanciationException {
    PreparedStatement prepStmt = null;
    String deleteStatement = "delete from favorit where componentName = ?";
    try {
      prepStmt = con.prepareStatement(deleteStatement);
      prepStmt.setString(1, componentId);
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
}