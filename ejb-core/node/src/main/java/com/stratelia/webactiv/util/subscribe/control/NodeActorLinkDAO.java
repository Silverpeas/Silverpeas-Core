/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

// TODO : reporter dans CVS (done)
package com.stratelia.webactiv.util.subscribe.control;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.node.model.NodePK;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Class declaration
 *
 * @author
 */
public class NodeActorLinkDAO {

  public static final String ADD_SUBSCRIPTION =
      "INSERT INTO subscribe (actorId, nodeId, space, componentName) VALUES (?, ?, ?, ? )";
  public static final String REMOVE_SUBSCRIPTION =
      "DELETE FROM subscribe WHERE actorId = ? AND nodeId = ? AND componentName = ?";
  public static final String REMOVE_USER_SUBSCRIPTIONS = "DELETE FROM subscribe WHERE actorId = ?";
  public static final String SELECT_SUBSCRIPTIONS_BY_USER = "SELECT nodeId, componentName FROM subscribe WHERE actorId = ?";
  public static final String SELECT_SUBSCRIBERS_FOR_NODE = "SELECT actorId FROM subscribe WHERE nodeId = ? AND componentName = ?";

  /**
   * Constructor declaration
   *
   * @see
   */
  private NodeActorLinkDAO() {
  }

  /**
   * Method declaration
   *
   * @param con
   * @param userId
   * @param node
   * @throws SQLException
   * @see
   */
  public static void add(Connection con, String userId, NodePK node) throws SQLException {
    SilverTrace.info("subscribe", "NodeActorLinkDAO.add", "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(ADD_SUBSCRIPTION);
      prepStmt.setString(1, userId);
      prepStmt.setInt(2, Integer.parseInt(node.getId()));
      prepStmt.setString(3, node.getSpace());
      prepStmt.setString(4, node.getComponentName());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   *
   * @param con
   * @param userId
   * @param node
   * @throws SQLException
   * @see
   */
  public static void remove(Connection con, String userId, NodePK node) throws SQLException {
    SilverTrace.info("subscribe", "NodeActorLinkDAO.remove", "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(REMOVE_SUBSCRIPTION);
      prepStmt.setString(1, userId);
      prepStmt.setInt(2, Integer.parseInt(node.getId()));
      prepStmt.setString(3, node.getComponentName());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   *
   * @param con
   * @param userId
   * @throws SQLException
   * @see
   */
  public static void removeByUser(Connection con, String tableName, String userId)
      throws SQLException {
    SilverTrace.info("subscribe", "NodeActorLinkDAO.removeByUser",
        "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(REMOVE_USER_SUBSCRIPTIONS);
      prepStmt.setString(1, userId);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   *
   * @param con
   * @param tableName
   * @param node
   * @param path
   * @throws SQLException
   * @see
   */
  public static void removeByNodePath(Connection con, String tableName,
      NodePK node, String path) throws SQLException {
    SilverTrace.info("subscribe", "NodeActorLinkDAO.removeByNodePath",
        "root.MSG_GEN_ENTER_METHOD");

    String insertStatement = "delete from " + tableName
        + " where componentName = ? " + "	and nodeId in ( "
        + "	select  nodeId from " + node.getTableName()
        + " where nodePath like '" + path + "%' and " + " instanceId = ? )";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setString(1, node.getComponentName());
      prepStmt.setString(2, node.getComponentName());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   *
   * @param con
   * @param userId
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<NodePK> getNodePKsByActor(Connection con, String userId)
      throws SQLException {
    SilverTrace.info("subscribe", "NodeActorLinkDAO.getNodePKsByActor",
        "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(SELECT_SUBSCRIPTIONS_BY_USER);
      prepStmt.setString(1, userId);
      rs = prepStmt.executeQuery();
      List<NodePK> list = new ArrayList<NodePK>();
      while (rs.next()) {
        String id = String.valueOf(rs.getInt(1));
        String componentName = rs.getString(2);
        NodePK nodePK = new NodePK(id, null, componentName);
        list.add(nodePK);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * Method declaration
   *
   * @param con
   * @param tableName
   * @param userId
   * @param componentName
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection getNodePKsByActorComponent(Connection con,
      String tableName, String userId, String componentName)
      throws SQLException {
    SilverTrace.info("subscribe", "NodeActorLinkDAO.getNodePKsByActorComponent",
        "root.MSG_GEN_ENTER_METHOD");
    String selectStatement = "select nodeId from " + tableName
        + " where actorId = ? " + " and componentName = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, userId);
      prepStmt.setString(2, componentName);
      rs = prepStmt.executeQuery();
      ArrayList list = new ArrayList();
      while (rs.next()) {
        String id = String.valueOf(rs.getInt(1));
        NodePK nodePK = new NodePK(id, null, componentName);
        list.add(nodePK);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * Method declaration
   *
   * @param con
   * @param node
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<String> getActorPKsByNodePK(Connection con, NodePK node)
      throws SQLException {
    SilverTrace.info("subscribe", "NodeActorLinkDAO.getActorPKsByNodePK",
        "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(SELECT_SUBSCRIBERS_FOR_NODE);
      prepStmt.setInt(1, Integer.parseInt(node.getId()));
      prepStmt.setString(2, node.getComponentName());
      rs = prepStmt.executeQuery();
      List<String> list = new ArrayList<String>();
      while (rs.next()) {
        list.add(rs.getString(1));
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

    /*
    TODO measure performances
    public static void getActorPKsByNodePK(Connection con, NodePK node, Collection<String> result)
      throws SQLException {
    SilverTrace.info("subscribe", "NodeActorLinkDAO.getActorPKsByNodePK",
        "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(SELECT_SUBSCRIBERS_FOR_NODE);
      prepStmt.setInt(1, Integer.parseInt(node.getId()));
      prepStmt.setString(2, node.getComponentName());
      rs = prepStmt.executeQuery();
      List<String> list = new ArrayList<String>();
      while (rs.next()) {
        result.add(rs.getString(1));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }*/

  public static Collection getActorPKsByNodePKs(Connection con,
      String tableName, Collection nodePKs) throws SQLException {
    SilverTrace.info("subscribe", "NodeActorLinkDAO.getActorPKsByNodePKs",
        "root.MSG_GEN_ENTER_METHOD");
    ArrayList list = new ArrayList();
    if (nodePKs != null && nodePKs.size() > 0) {
      String whereClause = "(";
      Iterator it = nodePKs.iterator();
      NodePK nodePK = null;
      while (it.hasNext()) {
        nodePK = (NodePK) it.next();
        whereClause += "nodeId = " + nodePK.getId();
        if (it.hasNext()) {
          whereClause += " or ";
        }
      }
      whereClause += ")";
      String selectStatement = "select distinct(actorId) from " + tableName
          + " where " + whereClause + " and componentName = ?";

      PreparedStatement prepStmt = null;
      ResultSet rs = null;
      try {
        prepStmt = con.prepareStatement(selectStatement);
        prepStmt.setString(1, nodePK.getComponentName());
        rs = prepStmt.executeQuery();
        while (rs.next()) {
          String id = rs.getString(1);
          list.add(id);
        }
      } finally {
        DBUtil.close(rs, prepStmt);
      }
    }
    return list;
  }
}