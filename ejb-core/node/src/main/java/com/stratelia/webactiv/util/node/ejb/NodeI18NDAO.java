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
// TODO : reporter dans CVS (done)
package com.stratelia.webactiv.util.node.ejb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.silverpeas.util.i18n.Translation;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.node.model.NodeI18NDetail;
import com.stratelia.webactiv.util.node.model.NodeI18NPK;
import com.stratelia.webactiv.util.node.model.NodeRuntimeException;
import com.stratelia.webactiv.util.exception.*;
import com.stratelia.silverpeas.silvertrace.*;

/**
 * This is the Node Data Access Object.
 * @author Nicolas Eysseric
 */
public class NodeI18NDAO {
  static final private String COLUMNS = "id,nodeId,lang,nodeName,nodeDescription";
  static final private String SELECT_TRANSLATIONS = "select " + COLUMNS
      + " from Sb_Node_NodeI18N where nodeId = ?";
  static final private String REMOVE_TRANSLATION = "delete from Sb_Node_NodeI18N where id = ?";
  static final private String REMOVE_TRANSLATIONS = "delete from Sb_Node_NodeI18N where nodeId = ?";
  static final private String INSERT_TRANSLATION =
      "insert into Sb_Node_NodeI18N values (?, ?, ?, ?, ?)";
  static final private String UPDATE_TRANSLATION =
      "update Sb_Node_NodeI18N set lang = ? , nodeName =  ? , nodeDescription = ?  where id = ?";
  static final public String TABLE_NAME = "sb_node_nodei18n";

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
  private static NodeI18NDetail resultSet2NodeDetail(ResultSet rs)
      throws SQLException {
    SilverTrace.info("node", "NodeI18NDAO.resultSet2NodeDetail()",
        "root.MSG_GEN_ENTER_METHOD");
    /* Récupération des données depuis la BD */
    int id = rs.getInt(1);
    String lang = rs.getString(3);
    String name = rs.getString(4);
    String description = rs.getString(5);
    if (description == null)
      description = "";

    NodeI18NDetail nd = new NodeI18NDetail(id, lang, name, description);

    SilverTrace.info("node", "NodeI18NDAO.resultSet2NodeDetail()",
        "root.MSG_GEN_EXIT_METHOD");
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
  public static NodeI18NPK saveTranslation(Connection con, NodeI18NDetail nd)
      throws SQLException {
    SilverTrace.info("node", "NodeI18NDAO.saveTranslation()",
        "root.MSG_GEN_ENTER_METHOD");
    NodeI18NPK pk = new NodeI18NPK("useless");

    int newId = 0;

    int nodeId = nd.getNodeId();
    String lang = nd.getLanguage();
    String name = nd.getName();
    String description = nd.getDescription();

    try {
      /* Recherche de la nouvelle PK de la table */
      newId = DBUtil.getNextId(nd.getTableName(), new String("id"));
    } catch (Exception e) {
      throw new NodeRuntimeException("NodeI18NDAO.insertRow()",
          SilverpeasRuntimeException.ERROR, "root.EX_GET_NEXTID_FAILED", e);
    }

    StringBuffer insertQuery = new StringBuffer();
    insertQuery.append(INSERT_TRANSLATION);
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(insertQuery.toString());
      prepStmt.setInt(1, newId);
      prepStmt.setInt(2, nodeId);
      prepStmt.setString(3, lang);
      prepStmt.setString(4, name);
      prepStmt.setString(5, description);
      prepStmt.executeUpdate();
      pk.setId(new Integer(newId).toString());
    } finally {
      DBUtil.close(prepStmt);
    }
    SilverTrace.info("node", "NodeI18NDAO.saveTranslation()",
        "root.MSG_GEN_EXIT_METHOD");
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
    SilverTrace.info("node", "NodeI18NDAO.udpateTranslation()",
        "root.MSG_GEN_ENTER_METHOD");
    NodeI18NPK pk = new NodeI18NPK(new Integer(nd.getId()).toString());

    int id = nd.getId();
    String lang = nd.getLanguage();
    String name = nd.getName();
    String description = nd.getDescription();

    StringBuffer updateQuery = new StringBuffer();
    updateQuery.append(UPDATE_TRANSLATION);
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(updateQuery.toString());
      prepStmt.setString(1, lang);
      prepStmt.setString(2, name);
      prepStmt.setString(3, description);
      prepStmt.setInt(4, id);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
    SilverTrace.info("node", "NodeI18NDAO.udpateTranslation()",
        "root.MSG_GEN_EXIT_METHOD");
    return pk;
  }

  /**
   * Delete into the database a translation
   * @param the translationId
   * @see com.stratelia.webactiv.util.node.model.NodeI18NDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static void removeTranslation(Connection con, int id)
      throws SQLException {
    SilverTrace.info("node", "NodeI18NDAO.removeTranslation()",
        "root.MSG_GEN_ENTER_METHOD");
    StringBuffer deleteQuery = new StringBuffer();
    deleteQuery.append(REMOVE_TRANSLATION);

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(deleteQuery.toString());
      prepStmt.setInt(1, id);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
    SilverTrace.info("node", "NodeI18NDAO.removeTranslation()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Delete all translations of a node
   * @param the nodeI18NDetail of the node to delete
   * @see com.stratelia.webactiv.util.node.model.NodeI18NDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static void removeTranslations(Connection con, int nodeId)
      throws SQLException {
    SilverTrace.info("node", "NodeI18NDAO.removeTranslations()",
        "root.MSG_GEN_ENTER_METHOD");
    StringBuffer deleteQuery = new StringBuffer();
    deleteQuery.append(REMOVE_TRANSLATIONS);

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(deleteQuery.toString());
      prepStmt.setInt(1, nodeId);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
    SilverTrace.info("node", "NodeI18NDAO.removeTranslations()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Returns the rows described by the given query with one id parameter.
   */
  public static List<Translation> getTranslations(Connection con, int nodeId)
      throws SQLException {
    ResultSet rs = null;
    StringBuffer selectQuery = new StringBuffer();
    PreparedStatement prepStmt = null;
    SilverTrace.debug("node", "NodeI18NDAO.getTranslations", "root.MSG_QUERY",
        SELECT_TRANSLATIONS + "  nodeId: " + nodeId);
    selectQuery.append(SELECT_TRANSLATIONS);
    List<Translation> result = new ArrayList<Translation>();

    try {
      prepStmt = con.prepareStatement(selectQuery.toString());
      prepStmt.setInt(1, nodeId);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        result.add(resultSet2NodeDetail(rs));
      }
    } catch (SQLException e) {
      throw new NodeRuntimeException("NodeI18NDAO.getTranslations()",
          SilverpeasRuntimeException.ERROR, "root.EX_SQL_QUERY_FAILED",
          "selectQuery = " + selectQuery.toString());
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return result;
  }
}