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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
//TODO : reporter dans CVS (done)

package com.stratelia.webactiv.util.favorit.ejb;

import java.sql.*;
import java.util.*;

import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.silverpeas.silvertrace.*;

/**
 * Class declaration
 * 
 * 
 * @author
 * @version %I%, %G%
 */
public class NodeActorLinkDAO {

  /* cette classe ne devrait jamais etre instanciee */

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public NodeActorLinkDAO() {
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param tableName
   * 
   * @see
   */
  /*
   * public static void createTable(Connection con, String tableName) throws
   * SQLException { SilverTrace.info( "favorit", "NodeActorLinkDAO.createTable",
   * "root.MSG_GEN_ENTER_METHOD", "tableName = " + tableName); String
   * createStatement = "CREATE TABLE " + tableName + " ( " +
   * "actorId        VARCHAR(50)   NOT NULL, " +
   * "nodeId         INT           NOT NULL, " +
   * "space          VARCHAR(50)   NOT NULL, " +
   * "componentName  VARCHAR(50)   NOT NULL" +
   * "CONSTRAINT ID_PK PRIMARY KEY (actorID, nodeId, space, componentName));";
   * 
   * PreparedStatement prepStmt = null;
   * 
   * try { prepStmt = con.prepareStatement(createStatement);
   * prepStmt.executeUpdate(); } finally { DBUtil.close(prepStmt); } }
   */

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param tableName
   * @param userId
   * @param node
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static void add(Connection con, String tableName, String userId,
      NodePK node) throws SQLException {
    SilverTrace.info("favorit", "NodeActorLinkDAO.add",
        "root.MSG_GEN_ENTER_METHOD", "tableName = " + tableName + ", userId = "
            + userId + ", node = " + node.toString());

    String insertStatement = "insert into " + tableName
        + " (actorId, nodeId, space, componentName) values (?, ?, ?, ? )";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setString(1, userId);
      prepStmt.setInt(2, new Integer(node.getId()).intValue());
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
   * 
   * @param con
   * @param tableName
   * @param userId
   * @param node
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static void remove(Connection con, String tableName, String userId,
      NodePK node) throws SQLException {
    SilverTrace.info("favorit", "NodeActorLinkDAO.remove",
        "root.MSG_GEN_ENTER_METHOD", "tableName = " + tableName + ", userId = "
            + userId + ", node = " + node.toString());

    // NEWD DLE
    // String insertStatement = "delete from " + tableName +
    // " where actorId = ? and nodeId = ? and space = ? and componentName = ?";
    String insertStatement = "delete from " + tableName
        + " where actorId = ? and nodeId = ? and componentName = ?";
    // NEWF DLE
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setString(1, userId);
      prepStmt.setInt(2, new Integer(node.getId()).intValue());
      // NEWD DLE
      // prepStmt.setString(3, node.getSpace());
      // prepStmt.setString(4, node.getComponentName());
      prepStmt.setString(3, node.getComponentName());
      // NEWF DLE
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param tableName
   * @param userId
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static void removeByUser(Connection con, String tableName,
      String userId) throws SQLException {
    SilverTrace.info("favorit", "NodeActorLinkDAO.removeByUser",
        "root.MSG_GEN_ENTER_METHOD", "tableName = " + tableName + ", userId = "
            + userId);

    String insertStatement = "delete from " + tableName + " where actorId = ?";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setString(1, userId);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param tableName
   * @param node
   * @param path
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static void removeByNodePath(Connection con, String tableName,
      NodePK node, String path) throws SQLException {
    SilverTrace.info("favorit", "NodeActorLinkDAO.removeByNodePath",
        "root.MSG_GEN_ENTER_METHOD", "tableName = " + tableName + ", node = "
            + node.toString() + ", path = " + path);

    // NEWD DLE
    /*
     * String deleteStatement = "delete from " + tableName +
     * " where space = ? and " + " componentName = ? " + " and nodeId in ( " +
     * " select  nodeId from " + node.getTableName() + " where nodePath like '"
     * + path + "%' and " + " instanceId = ? )";
     */
    String deleteStatement = "delete from " + tableName
        + " where componentName = ? " + " and nodeId in ( "
        + " select  nodeId from " + node.getTableName()
        + " where nodePath like '" + path + "%' and " + " instanceId = ? )";
    // NEWF DLE

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(deleteStatement);
      // NEWD DLE
      // prepStmt.setString(1, node.getSpace());
      // prepStmt.setString(2, node.getComponentName());
      // prepStmt.setString(3, node.getComponentName());
      prepStmt.setString(1, node.getComponentName());
      prepStmt.setString(2, node.getComponentName());
      // NEWF DLE
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param tableName
   * @param userId
   * 
   * @return
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static Collection getNodePKsByActor(Connection con, String tableName,
      String userId) throws SQLException {
    SilverTrace.info("favorit", "NodeActorLinkDAO.getNodePKsByActor",
        "root.MSG_GEN_ENTER_METHOD", "tableName = " + tableName + ", userId = "
            + userId);
    // NEWD DLE
    // String selectStatement = "select nodeId, space, componentName from " +
    // tableName + " where actorId = ?";
    String selectStatement = "select nodeId, componentName from " + tableName
        + " where actorId = ?";
    // NEWF DLE
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, userId);
      rs = prepStmt.executeQuery();
      ArrayList list = new ArrayList();
      while (rs.next()) {
        String id = String.valueOf(rs.getInt(1));
        // NEWD DLE
        // String space = rs.getString(2);
        // String componentName = rs.getString(3);
        String componentName = rs.getString(2);
        // NodePK nodePK = new NodePK(id, space, componentName);
        NodePK nodePK = new NodePK(id, null, componentName);
        // NEWF DLE
        list.add(nodePK);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  // NEWD DLE
  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param tableName
   * @param userId
   * @param space
   * @param componentName
   * 
   * @return
   * 
   * @throws SQLException
   * 
   * @see
   */
  /*
   * public static Collection getNodePKsByActorSpaceAndComponent( Connection
   * con, String tableName, String userId, String space, String componentName)
   * throws SQLException { SilverTrace.info( "favorit",
   * "NodeActorLinkDAO.getNodePKsByActorSpaceAndComponent",
   * "root.MSG_GEN_ENTER_METHOD", "tableName = " + tableName + ", userId = " +
   * userId + ", space = " + space + ", componentName = " + componentName);
   * 
   * String selectStatement = "select nodeId, space, componentName from " +
   * tableName + " where actorId = ? and space = ? and componentName = ?";
   * 
   * PreparedStatement prepStmt = null; ResultSet rs = null; try { prepStmt =
   * con.prepareStatement(selectStatement); prepStmt.setString(1, userId);
   * prepStmt.setString(2, space); prepStmt.setString(3, componentName); rs =
   * prepStmt.executeQuery(); ArrayList list = new ArrayList(); while
   * (rs.next()) { String id = String.valueOf(rs.getInt(1)); String sp =
   * rs.getString(2); String cptName = rs.getString(3); NodePK nodePK = new
   * NodePK(id, sp, cptName); list.add(nodePK); } return list; } finally {
   * DBUtil.close(rs, prepStmt); } }
   */

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param tableName
   * @param userId
   * @param space
   * @param componentName
   * 
   * @return
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static Collection getNodePKsByActorComponent(Connection con,
      String tableName, String userId, String componentName)
      throws SQLException {
    SilverTrace.info("favorit",
        "NodeActorLinkDAO.getNodePKsByActorSpaceAndComponent",
        "root.MSG_GEN_ENTER_METHOD", "tableName = " + tableName + ", userId = "
            + userId + ", componentName = " + componentName);

    String selectStatement = "select nodeId, componentName from " + tableName
        + " where actorId = ? and componentName = ?";

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
        String cptName = rs.getString(2);
        NodePK nodePK = new NodePK(id, null, cptName);
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
   * 
   * @param con
   * @param tableName
   * @param node
   * 
   * @return
   * 
   * @throws SQLException
   * 
   * @see
   */
  /*
   * public static Collection getActorPKsByNodePK(Connection con, String
   * tableName, NodePK node) throws SQLException { SilverTrace.info( "favorit",
   * "NodeActorLinkDAO.getActorPKsByNodePK", "root.MSG_GEN_ENTER_METHOD",
   * "tableName = " + tableName + ", node = " + node.toString());
   * 
   * String selectStatement = "select actorId from " + tableName +
   * " where nodeId = ? and space = ? and componentName = ?"; PreparedStatement
   * prepStmt = null; ResultSet rs = null; try { prepStmt =
   * con.prepareStatement(selectStatement); prepStmt.setInt(1, new
   * Integer(node.getId()).intValue()); prepStmt.setString(2, node.getSpace());
   * prepStmt.setString(3, node.getComponentName()); rs =
   * prepStmt.executeQuery(); ArrayList list = new ArrayList(); while
   * (rs.next()) { String id = rs.getString(1);
   * 
   * list.add(id); } return list; } finally { DBUtil.close(rs, prepStmt); } }
   */

  // NEWD DLE
  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param tableName
   * @param node
   * 
   * @return
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static Collection getActorPKsByNodePK(Connection con,
      String tableName, NodePK node) throws SQLException {
    SilverTrace.info("favorit", "NodeActorLinkDAO.getActorPKsByNodePK",
        "root.MSG_GEN_ENTER_METHOD", "tableName = " + tableName + ", node = "
            + node.toString());

    String selectStatement = "select actorId from " + tableName
        + " where nodeId = ? and componentName = ?";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, new Integer(node.getId()).intValue());
      prepStmt.setString(2, node.getComponentName());
      rs = prepStmt.executeQuery();
      ArrayList list = new ArrayList();
      while (rs.next()) {
        String id = rs.getString(1);

        list.add(id);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }
  // NEWF DLE

}
