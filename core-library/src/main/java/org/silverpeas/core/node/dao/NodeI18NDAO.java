/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.node.dao;

import org.silverpeas.core.node.model.NodeI18NDetail;
import org.silverpeas.core.node.model.NodeI18NPK;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.StringUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the Node Data Access Object.
 *
 * @author Nicolas Eysseric
 */
public class NodeI18NDAO {

  static final private String SELECT_TRANSLATIONS = "SELECT id, nodeId, lang, nodeName, "
      + "nodeDescription FROM sb_node_nodeI18N WHERE nodeId = ?";
  static final private String REMOVE_TRANSLATION = "DELETE FROM sb_node_nodeI18N WHERE id = ?";
  static final private String REMOVE_TRANSLATIONS = "DELETE FROM sb_node_nodeI18N WHERE nodeId = ?";
  static final private String INSERT_TRANSLATION
      = "INSERT INTO sb_node_nodeI18N VALUES (?, ?, ?, ?, ?)";
  static final private String UPDATE_TRANSLATION
      = "UPDATE sb_node_nodeI18N SET lang = ?, nodeName =  ?, "
      + "nodeDescription = ?  WHERE id = ?";

  /**
   * This class must not be instanciated
   *
   * @since 1.0
   */
  public NodeI18NDAO() {
  }

  /**
   * Deletes all translations of publications linked to the component instance represented by the
   * given identifier.
   * @param componentInstanceId the identifier of the component instance for which the resources
   * must be deleted.
   * @throws SQLException
   */
  public static void deleteComponentInstanceData(String componentInstanceId) throws SQLException {
    JdbcSqlQuery.createDeleteFor("sb_node_nodeI18N").where("nodeId in (" +
        JdbcSqlQuery.createSelect("nodeId from sb_node_node").where("instanceId = ?")
            .getSqlQuery() + ")", componentInstanceId).execute();
  }

  /**
   * ********************* Database Routines ***********************
   */
  /**
   * Create a NodeI18N from a ResultSet
   *
   * @param rs the ResultSet which contains data
   * @return the NodeI18NDetail
   * @see NodeI18NDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  private static NodeI18NDetail resultSet2NodeDetail(ResultSet rs) throws SQLException {

    int id = rs.getInt(1);
    String lang = rs.getString("lang");
    String name = rs.getString("nodeName");
    String description = rs.getString("nodeDescription");
    if (!StringUtil.isDefined(description)) {
      description = "";
    }
    NodeI18NDetail nd = new NodeI18NDetail(id, lang, name, description);


    return (nd);
  }

  /**
   * Insert into the database the data of a node
   *
   * @return a NodeI18NPK which contains the new row id
   * @param nd the NodeI18NDetail which contains data
   * @see NodeI18NDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static NodeI18NPK saveTranslation(Connection con, NodeI18NDetail nd) throws SQLException {

    NodeI18NPK pk = new NodeI18NPK("useless");
    int newId = DBUtil.getNextId(nd.getTableName(), "id");
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

    return pk;
  }

  /**
   * Update into the database the translation
   *
   * @return a NodeI18NPK which contains the new row id
   * @param nd the NodeI18NDetail which contains data
   * @see NodeI18NDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static NodeI18NPK updateTranslation(Connection con, NodeI18NDetail nd)
      throws SQLException {

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

    return pk;
  }

  /**
   * Delete into the database a translation
   *
   * @param id translationId
   * @see NodeI18NDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static void removeTranslation(Connection con, int id) throws SQLException {

    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(REMOVE_TRANSLATION);
      prepStmt.setInt(1, id);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }

  }

  /**
   * Delete all translations of a node
   *
   * @param nodeId id of the node to delete
   * @param con the JDBC Connection
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static void removeTranslations(Connection con, int nodeId) throws SQLException {

    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(REMOVE_TRANSLATIONS);
      prepStmt.setInt(1, nodeId);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }

  }

  /**
   * Returns the rows described by the given query with one id parameter.
   *
   * @param con
   * @param nodeId
   * @return
   * @throws SQLException
   */
  public static List<NodeI18NDetail> getTranslations(Connection con, int nodeId) throws SQLException {
    ResultSet rs = null;
    PreparedStatement prepStmt = null;
    List<NodeI18NDetail> result = new ArrayList<>();
    try {
      prepStmt = con.prepareStatement(SELECT_TRANSLATIONS);
      prepStmt.setInt(1, nodeId);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        result.add(resultSet2NodeDetail(rs));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return result;
  }
}
