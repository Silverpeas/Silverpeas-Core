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

package com.stratelia.webactiv.util.node.control.dao;

import com.silverpeas.util.StringUtil;
import java.util.ArrayList;
import java.util.List;

import com.silverpeas.util.i18n.Translation;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodeI18NDetail;
import com.stratelia.webactiv.util.node.model.NodeI18NPK;
import com.stratelia.webactiv.util.node.model.NodeRuntimeException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This is the Node Data Access Object.
 * @author Nicolas Eysseric
 */
public class NodeI18NDAO {

  static final private String SELECT_TRANSLATIONS = "SELECT id, nodeId, lang, nodeName, "
      + "nodeDescription FROM sb_node_nodeI18N WHERE nodeId = ?";
  static final private String REMOVE_TRANSLATION = "DELETE FROM sb_node_nodeI18N WHERE id = ?";
  static final private String REMOVE_TRANSLATIONS = "DELETE FROM sb_node_nodeI18N WHERE nodeId = ?";
  static final private String INSERT_TRANSLATION =
      "INSERT INTO sb_node_nodeI18N VALUES (?, ?, ?, ?, ?)";
  static final private String UPDATE_TRANSLATION =
      "UPDATE sb_node_nodeI18N SET lang = ?, nodeName =  ?, "
      + "nodeDescription = ?  WHERE id = ?";

  /**
   * This class must not be instanciated
   * @since 1.0
   */
  public NodeI18NDAO() {
  }

  /**
   * ********************* Database Routines ***********************
   */
  /**
   * Create a NodeI18N from a ResultSet
   * @param rs the ResultSet which contains data
   * @return the NodeI18NDetail
   * @see com.stratelia.webactiv.util.node.model.NodeI18NDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  private static NodeI18NDetail resultSet2NodeDetail(ResultSet rs) throws SQLException {
    SilverTrace.info("node", "NodeI18NDAO.resultSet2NodeDetail()", "root.MSG_GEN_ENTER_METHOD");
    int id = rs.getInt(1);
    String lang = rs.getString("lang");
    String name = rs.getString("nodeName");
    String description = rs.getString("nodeDescription");
    if (!StringUtil.isDefined(description)) {
      description = "";
    }
    NodeI18NDetail nd = new NodeI18NDetail(id, lang, name, description);

    SilverTrace.info("node", "NodeI18NDAO.resultSet2NodeDetail()", "root.MSG_GEN_EXIT_METHOD");
    return (nd);
  }

  /**
   * Insert into the database the data of a node
   * @return a NodeI18NPK which contains the new row id
   * @param nd the NodeI18NDetail which contains data
   * @see com.stratelia.webactiv.util.node.model.NodeI18NDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static NodeI18NPK saveTranslation(Connection con, NodeI18NDetail nd) throws SQLException {
    SilverTrace.info("node", "NodeI18NDAO.saveTranslation()", "root.MSG_GEN_ENTER_METHOD");
    NodeI18NPK pk = new NodeI18NPK("useless");
    int newId = 0;
    try {
      newId = DBUtil.getNextId(nd.getTableName(), "id");
    } catch (Exception e) {
      throw new NodeRuntimeException("NodeI18NDAO.insertRow()",
          SilverpeasRuntimeException.ERROR, "root.EX_GET_NEXTID_FAILED", e);
    }
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(INSERT_TRANSLATION);
      prepStmt.setInt(1, newId);
      prepStmt.setInt(2, nd.getNodeId());
      prepStmt.setString(3, nd.getLanguage());
      prepStmt.setString(4, nd.getName());
      prepStmt.setString(5, nd.getDescription());
      prepStmt.executeUpdate();
      pk.setId(String.valueOf(newId));
    } finally {
      DBUtil.close(prepStmt);
    }
    SilverTrace.info("node", "NodeI18NDAO.saveTranslation()", "root.MSG_GEN_EXIT_METHOD");
    return pk;
  }

  /**
   * Update into the database the translation
   * @return a NodeI18NPK which contains the new row id
   * @param nd the NodeI18NDetail which contains data
   * @see com.stratelia.webactiv.util.node.model.NodeI18NDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static NodeI18NPK updateTranslation(Connection con, NodeI18NDetail nd)
      throws SQLException {
    SilverTrace.info("node", "NodeI18NDAO.udpateTranslation()", "root.MSG_GEN_ENTER_METHOD");
    NodeI18NPK pk = new NodeI18NPK(String.valueOf(nd.getId()));
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(UPDATE_TRANSLATION);
      prepStmt.setString(1, nd.getLanguage());
      prepStmt.setString(2, nd.getName());
      prepStmt.setString(3, nd.getDescription());
      prepStmt.setInt(4, nd.getId());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
    SilverTrace.info("node", "NodeI18NDAO.udpateTranslation()", "root.MSG_GEN_EXIT_METHOD");
    return pk;
  }

  /**
   * Delete into the database a translation
   * @param the translationId
   * @see com.stratelia.webactiv.util.node.model.NodeI18NDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static void removeTranslation(Connection con, int id) throws SQLException {
    SilverTrace.info("node", "NodeI18NDAO.removeTranslation()", "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(REMOVE_TRANSLATION);
      prepStmt.setInt(1, id);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
    SilverTrace.info("node", "NodeI18NDAO.removeTranslation()", "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Delete all translations of a node
   * @param nodeId id of the node to delete
   * @param con the JDBC Connection
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static void removeTranslations(Connection con, int nodeId) throws SQLException {
    SilverTrace.info("node", "NodeI18NDAO.removeTranslations()", "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(REMOVE_TRANSLATIONS);
      prepStmt.setInt(1, nodeId);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
    SilverTrace.info("node", "NodeI18NDAO.removeTranslations()", "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Returns the rows described by the given query with one id parameter.
   * @param con
   * @param nodeId
   * @return
   * @throws SQLException
   */
  public static List<Translation> getTranslations(Connection con, int nodeId) throws SQLException {
    ResultSet rs = null;
    PreparedStatement prepStmt = null;
    SilverTrace.debug("node", "NodeI18NDAO.getTranslations", "root.MSG_QUERY",
        SELECT_TRANSLATIONS + "  nodeId: " + nodeId);
    List<Translation> result = new ArrayList<Translation>();
    try {
      prepStmt = con.prepareStatement(SELECT_TRANSLATIONS);
      prepStmt.setInt(1, nodeId);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        result.add(resultSet2NodeDetail(rs));
      }
    } catch (SQLException e) {
      throw new NodeRuntimeException("NodeI18NDAO.getTranslations()",
          SilverpeasRuntimeException.ERROR, "root.EX_SQL_QUERY_FAILED",
          "selectQuery = " + SELECT_TRANSLATIONS);
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return result;
  }
}