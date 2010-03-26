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
package com.stratelia.webactiv.util.statistic.ejb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.statistic.model.HistoryNodePublicationActorDetail;
import com.stratelia.webactiv.util.statistic.model.StatisticResultDetail;
import com.stratelia.webactiv.util.statistic.model.StatisticRuntimeException;

public class HistoryNodePublicationActorDAO {
  static SimpleDateFormat formater = new SimpleDateFormat("yyyy/MM/dd");

  /**
   * Method declaration
   * @param rs
   * @param space
   * @param componentName
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<HistoryNodePublicationActorDetail> getHistoryDetails(ResultSet rs,
      String space,
      String componentName) throws SQLException {
    List<HistoryNodePublicationActorDetail> list =
        new ArrayList<HistoryNodePublicationActorDetail>();
    java.util.Date date;
    String actorId = "";
    String nodeId = "";
    String pubId = "";

    while (rs.next()) {
      try {
        date = formater.parse(rs.getString(1));
      } catch (java.text.ParseException e) {
        throw new StatisticRuntimeException(
            "HistoryNodePublicationActorDAO.getHistoryDetails()",
            SilverpeasRuntimeException.ERROR, "statistic.INCORRECT_DATE", e);
      }
      actorId = rs.getString(2);
      nodeId = String.valueOf(rs.getInt(3));
      pubId = String.valueOf(rs.getInt(4));
      NodePK nodePK = new NodePK(nodeId, space, componentName);
      PublicationPK pubPK = new PublicationPK(pubId, space, componentName);
      HistoryNodePublicationActorDetail detail = new HistoryNodePublicationActorDetail(
          date, actorId, nodePK, pubPK);

      list.add(detail);
    }
    return list;
  }

  /* cette classe ne devrait jamais etre instanciee */

  /**
   * Constructor declaration
   * @see
   */
  public HistoryNodePublicationActorDAO() {
  }

  /**
   * Get descendant node PKs of a node
   * @return A collection of NodePK
   * @param con A connection to the database
   * @param nodePK A NodePK
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @exception java.sql.SQLException
   * @since 1.0
   */
  private static Collection<NodePK> getDescendantPKs(Connection con, NodePK nodePK)
      throws SQLException {

    String path = null;
    List<NodePK> a = new ArrayList<NodePK>();
    String selectQuery = "select nodePath from " + nodePK.getTableName()
        + "  where nodeId = ? and instanceId = ?";

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      prepStmt = con.prepareStatement(selectQuery);
      prepStmt.setInt(1, new Integer(nodePK.getId()).intValue());
      prepStmt.setString(2, nodePK.getComponentName());
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        path = rs.getString(1);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    if (path != null) {
      path = path + "/" + "%";

      selectQuery = "select nodeId " + "from " + nodePK.getTableName()
          + " where nodePath like '" + path + "'"
          + " and instanceId = ? order by nodeId";

      try {
        prepStmt = con.prepareStatement(selectQuery);
        prepStmt.setString(1, nodePK.getComponentName());
        rs = prepStmt.executeQuery();
        String nodeId = "";

        while (rs.next()) {
          nodeId = new Integer(rs.getInt(1)).toString();
          NodePK n = new NodePK(nodeId, nodePK);

          a.add(n); /* Stockage du sous thème */
        }
      } finally {
        DBUtil.close(rs, prepStmt);
      }
    }

    return a;
  }

  /**
   * Method declaration
   * @param con
   * @param tableName
   * @param fatherPK
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<StatisticResultDetail> getNodesUsage(Connection con, String tableName,
      NodePK fatherPK) throws SQLException {
    SilverTrace.info("statistic",
        "HistoryNodePublicationActorDAO.getNodesUsage",
        "root.MSG_GEN_ENTER_METHOD");

    Collection<NodePK> sonPK_list = null;
    ResultSet rs = null;
    PreparedStatement prepStmt = null;
    String selectQuery = "select count(nodeId) from " + tableName
        + " where nodeId=?";

    String nodeId = "";

    try {
      // get all descendant of a one NodePK
      sonPK_list = getDescendantPKs(con, fatherPK);
      // verify that the Collection object return is not empty;
      ArrayList<StatisticResultDetail> nodesUsage = new ArrayList<StatisticResultDetail>();
      if (!sonPK_list.isEmpty()) {
        Iterator<NodePK> iterator = sonPK_list.iterator();

        prepStmt = con.prepareStatement(selectQuery);
        // for each descendant, we compute the number of them in the
        // SB_Publication_Histaory table
        for (; iterator.hasNext();) {
          nodeId = (iterator.next()).getId();
          prepStmt.setInt(1, (new Integer(nodeId)).intValue());
          rs = prepStmt.executeQuery();
          // get the result
          if (rs.next()) {
            NodePK nodePK = new NodePK(nodeId, fatherPK);
            StatisticResultDetail detail = new StatisticResultDetail(nodePK,
                String.valueOf(rs.getInt(1)));

            nodesUsage.add(detail);
          }
        }
      }
      return nodesUsage;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param tableName
   * @param userId
   * @param nodePK
   * @param pubPK
   * @throws SQLException
   * @see
   */
  public static void add(Connection con, String tableName, String userId,
      NodePK nodePK, PublicationPK pubPK) throws SQLException {
    SilverTrace.info("statistic", "HistoryNodePublicationActorDAO.add",
        "root.MSG_GEN_ENTER_METHOD");

    String insertStatement = "insert into " + tableName
        + " values (?, ?, ?, ? )";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setString(1, formater.format(new java.util.Date()));
      prepStmt.setString(2, userId);
      prepStmt.setInt(3, new Integer(nodePK.getId()).intValue());
      prepStmt.setInt(4, new Integer(pubPK.getId()).intValue());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param tableName
   * @param pubPK
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<HistoryNodePublicationActorDetail> getHistoryDetailByPublication(
      Connection con,
      String tableName, PublicationPK pubPK) throws SQLException {
    SilverTrace.info("statistic",
        "HistoryNodePublicationActorDAO.getHistoryDetailByPublication",
        "root.MSG_GEN_ENTER_METHOD");
    String space = pubPK.getSpace();
    String componentName = pubPK.getComponentName();
    String selectStatement = "select * " + "from " + tableName
        + " where pubId=" + pubPK.getId();

    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(selectStatement);
      return getHistoryDetails(rs, space, componentName);
    } finally {
      DBUtil.close(rs, stmt);
    }
  }
}
