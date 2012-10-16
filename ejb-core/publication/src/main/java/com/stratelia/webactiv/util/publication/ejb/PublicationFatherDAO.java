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

package com.stratelia.webactiv.util.publication.ejb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.Alias;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

/**
 * This is the Publication Father Data Access Object.
 * @author Nicolas Eysseric
 */
public class PublicationFatherDAO {
  private static String publicationFatherTableName = "SB_Publication_PubliFather";

  /**
   * This class must not be instanciated
   * @since 1.0
   */
  public PublicationFatherDAO() {
  }

  /**
   * Add a new father to this publication
   * @param con Connection to database
   * @param pubPK the publication PublicationPK
   * @param fatherPK the father NodePK to add
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @see com.stratelia.webactiv.util.publication.model.PublicationPK
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static void addFather(Connection con, PublicationPK pubPK,
      NodePK fatherPK) throws SQLException {
    StringBuffer insertStatement = new StringBuffer(128);
    insertStatement.append("insert into ").append(publicationFatherTableName)
        .append(" values (?, ?, ?, ?, ?, ?)");
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement.toString());
      prepStmt.setInt(1, new Integer(pubPK.getId()).intValue());
      prepStmt.setInt(2, new Integer(fatherPK.getId()).intValue());
      prepStmt.setString(3, pubPK.getInstanceId());
      prepStmt.setNull(4, Types.INTEGER);
      prepStmt.setNull(5, Types.VARCHAR);
      prepStmt.setInt(6, 0);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void updateOrder(Connection con, PublicationPK pubPK,
      NodePK fatherPK, int order) throws SQLException {
    StringBuffer statement = new StringBuffer(128);
    statement.append("update ").append(publicationFatherTableName).append(
        " set pubOrder = ? where pubId = ? and nodeId = ? and instanceId = ? ");
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(statement.toString());
      prepStmt.setInt(1, order);
      prepStmt.setInt(2, Integer.parseInt(pubPK.getId()));
      prepStmt.setInt(3, Integer.parseInt(fatherPK.getId()));
      prepStmt.setString(4, pubPK.getInstanceId());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void addAlias(Connection con, PublicationPK pubPK, Alias alias)
      throws SQLException {

    StringBuffer insertStatement = new StringBuffer(128);
    insertStatement.append("insert into ").append(publicationFatherTableName).
        append(" values (?, ?, ?, ?, ?, ?)");
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement.toString());
      prepStmt.setInt(1, new Integer(pubPK.getId()).intValue());
      prepStmt.setInt(2, new Integer(alias.getId()).intValue());
      prepStmt.setString(3, alias.getInstanceId());
      if (alias.getUserId() != null) {
        prepStmt.setInt(4, Integer.parseInt(alias.getUserId()));
      } else {
        prepStmt.setNull(4, Types.INTEGER);
      }
      prepStmt.setString(5, Long.toString(new Date().getTime()));
      prepStmt.setInt(6, alias.getPubOrder());
      prepStmt.executeUpdate();

    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void removeAlias(Connection con, PublicationPK pubPK,
      Alias alias) throws SQLException {
    StringBuffer statement = new StringBuffer(128);
    statement.append("delete from ").append(publicationFatherTableName).append(
        " where pubId = ? and nodeId = ? and instanceId = ? ");
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(statement.toString());

      prepStmt.setInt(1, Integer.parseInt(pubPK.getId()));
      prepStmt.setInt(2, Integer.parseInt(alias.getId()));
      prepStmt.setString(3, alias.getInstanceId());

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static List<Alias> getAlias(Connection con, PublicationPK pubPK)
      throws SQLException {
    List<Alias> list = null;

    StringBuffer selectQuery = new StringBuffer(128);
    selectQuery.append(
        "select nodeId, instanceId, aliasUserId, aliasDate, pubOrder from ").
        append(publicationFatherTableName).append(" where pubId = ").append(pubPK.
        getId());
    Statement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(selectQuery.toString());
      String id = null;
      String instanceId = null;
      String userId = null;
      Date date = null;
      list = new ArrayList<Alias>();
      Alias alias = null;

      while (rs.next()) {
        id = Integer.toString(rs.getInt(1));
        instanceId = rs.getString(2);
        int iUserId = rs.getInt(3);
        userId = Integer.toString(iUserId);
        String sDate = rs.getString(4);
        if (StringUtil.isDefined(sDate)) {
          date = new Date(Long.parseLong(sDate));
        }
        int pubOrder = rs.getInt(5);

        alias = new Alias(id, instanceId);
        alias.setUserId(userId);
        alias.setDate(date);
        alias.setPubOrder(pubOrder);
        list.add(alias);
      }

      return list;

    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  /**
   * Remove a father to this publication
   * @param con Connection to database
   * @param pubPK the publication PublicationPK
   * @param fatherPK the father NodePK to delete
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @see com.stratelia.webactiv.util.publication.model.PublicationPK
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static void removeFather(Connection con, PublicationPK pubPK,
      NodePK fatherPK) throws SQLException {
    StringBuffer deleteStatement = new StringBuffer(128);
    deleteStatement.append("delete from ").append(publicationFatherTableName)
        .append(" where pubId = ? and nodeId = ? ");
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(deleteStatement.toString());
      prepStmt.setInt(1, new Integer(pubPK.getId()).intValue());
      prepStmt.setInt(2, new Integer(fatherPK.getId()).intValue());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param pubPK
   * @param fatherPK
   * @throws SQLException
   * @see
   */
  public static void removeFatherToPublications(Connection con,
      PublicationPK pubPK, NodePK fatherPK) throws SQLException {
    SilverTrace.info("publication",
        "PublicationDAO.removeFatherToPublications()",
        "root.MSG_GEN_ENTER_METHOD", "pubPK = " + pubPK.toString()
        + ", fatherPK = " + fatherPK.toString());

    PublicationPK publicationPK = null;

    // get all publications linked to fatherPK
    List<PublicationPK> pubPKs = (List<PublicationPK>) getPubPKsInFatherPK(con, fatherPK);

    // for each publication, remove link into table
    for (int i = 0; i < pubPKs.size(); i++) {
      publicationPK = (PublicationPK) pubPKs.get(i);
      removeLink(con, publicationPK, fatherPK);
    }
  }

  private static void removeLink(Connection con, PublicationPK pubPK,
      NodePK fatherPK) throws SQLException {
    SilverTrace.info("publication", "PublicationDAO.removeLink()",
        "root.MSG_GEN_ENTER_METHOD", "pubPK = " + pubPK.toString()
        + ", fatherPK = " + fatherPK.toString());

    StringBuffer deleteStatement = new StringBuffer(128);
    deleteStatement.append("delete from ").append(publicationFatherTableName)
        .append(" where nodeId = ? and pubId = ? ");
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(deleteStatement.toString());
      prepStmt.setInt(1, new Integer(fatherPK.getId()).intValue());
      prepStmt.setInt(2, new Integer(pubPK.getId()).intValue());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param pubPK
   * @param fatherIds
   * @throws SQLException
   * @see
   */
  public static void removeFathersToPublications(Connection con,
      PublicationPK pubPK, Collection<String> fatherIds) throws SQLException {
    SilverTrace.info("publication",
        "PublicationDAO.removeFathersToPublications()",
        "root.MSG_GEN_ENTER_METHOD", "pubPK = " + pubPK.toString()
        + ", fatherIds = " + fatherIds.toString());

    if (fatherIds != null && fatherIds.size() > 0) {
      String fatherId = "";
      NodePK fatherPK = null;
      Iterator<String> it = fatherIds.iterator();

      while (it.hasNext()) {
        fatherId = it.next();
        fatherPK = new NodePK(fatherId, pubPK);
        removeFatherToPublications(con, pubPK, fatherPK);
      }
    }
  }

  /**
   * Delete all fathers to this publication
   * @param con Connection to database
   * @param pubPK the publication PublicationPK
   * @see com.stratelia.webactiv.util.publication.model.PublicationPK
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static void removeAllFather(Connection con, PublicationPK pubPK)
      throws SQLException {
    StringBuffer deleteStatement = new StringBuffer(128);
    deleteStatement.append("delete from ").append(publicationFatherTableName)
        .append(" where pubId = ? ");
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(deleteStatement.toString());
      prepStmt.setInt(1, new Integer(pubPK.getId()).intValue());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Delete links between publication and father when publications are linked to a father which is a
   * descendant of a node
   * @param con Connection to database
   * @param pubPK the publication PublicationPK
   * @param originPK the node which is deleted
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @see com.stratelia.webactiv.util.publication.model.PublicationPK
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static Collection<NodePK> getAllFatherPK(Connection con, PublicationPK pubPK)
      throws SQLException {
    return getAllFatherPK(con, pubPK, null);
  }

  public static Collection<NodePK> getAllFatherPK(Connection con, PublicationPK pubPK,
      String order) throws SQLException {
    List<NodePK> list = null;

    StringBuffer selectQuery = new StringBuffer(128);
    selectQuery.append("select nodeId from ")
        .append(publicationFatherTableName).append(" where pubId = ").append(
        pubPK.getId()).append(
        " and instanceId = '" + pubPK.getInstanceId() + "'");
    if (order != null) {
      selectQuery.append(" order by ").append(order);
    }
    Statement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(selectQuery.toString());
      String id = "";
      list = new ArrayList<NodePK>();
      while (rs.next()) {
        id = new Integer(rs.getInt(1)).toString();
        NodePK nodePK = new NodePK(id, pubPK);
        list.add(nodePK);
      }
      return list;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param fatherPKs
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<PublicationPK> getPubPKsInFatherPKs(Connection con,
      Collection<WAPrimaryKey> fatherPKs) throws SQLException {
    WAPrimaryKey fatherPK = null;
    PublicationPK pubPK = null;
    String fatherId = null;
    ArrayList<PublicationPK> list = new ArrayList<PublicationPK>();

    if (fatherPKs.isEmpty()) {
      return list;
    } else {
      Iterator<WAPrimaryKey> iterator = fatherPKs.iterator();

      if (iterator.hasNext()) {
        fatherPK = iterator.next();
        pubPK = new PublicationPK("unknown", fatherPK);
        fatherId = fatherPK.getId();
      }

      StringBuffer selectStatement = new StringBuffer(128);
      selectStatement.append("select F.pubId from ").append(
          publicationFatherTableName).append(" F, ").append(
          pubPK.getTableName()).append(" P ");
      selectStatement.append(" where F.pubId = P.pubId ");
      selectStatement.append(" and ( F.nodeId = ").append(fatherId);

      while (iterator.hasNext()) {
        fatherPK = (WAPrimaryKey) iterator.next();
        fatherId = fatherPK.getId();
        selectStatement.append(" or F.nodeId = ").append(fatherId);
      }
      selectStatement.append(" )");

      Statement stmt = null;
      ResultSet rs = null;

      try {
        stmt = con.createStatement();
        rs = stmt.executeQuery(selectStatement.toString());
        String id = "";

        while (rs.next()) {
          id = new Integer(rs.getInt(1)).toString();
          pubPK = new PublicationPK(id, fatherPK);
          list.add(pubPK);
        }
        return list;
      } finally {
        DBUtil.close(rs, stmt);
      }
    }
  }

  /**
   * Method declaration
   * @param con
   * @param fatherPK
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<PublicationPK> getPubPKsInFatherPK(Connection con, NodePK fatherPK)
      throws SQLException {
    PublicationPK pubPK = new PublicationPK("unknown", fatherPK);
    StringBuffer selectStatement = new StringBuffer(128);
    selectStatement.append("select P.pubId, P.instanceId from ").append(
        publicationFatherTableName).append(" F, ").append(pubPK.getTableName())
        .append(" P ");
    selectStatement.append(" where F.instanceId = ? ");
    selectStatement.append(" and F.pubId = P.pubId ");
    selectStatement.append(" and F.nodeId = ? ");

    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      ArrayList<PublicationPK> list = new ArrayList<PublicationPK>();
      stmt = con.prepareStatement(selectStatement.toString());

      stmt.setString(1, fatherPK.getInstanceId());
      stmt.setInt(2, Integer.parseInt(fatherPK.getId()));

      rs = stmt.executeQuery();
      String id = "";
      String instanceId = "";
      while (rs.next()) {
        id = Integer.toString(rs.getInt(1));
        instanceId = rs.getString(2);
        pubPK = new PublicationPK(id, instanceId);
        list.add(pubPK);
      }
      return list;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }
}