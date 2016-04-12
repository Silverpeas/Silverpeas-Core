/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.contribution.publication.dao;

import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.contribution.publication.model.Alias;
import org.silverpeas.core.contribution.publication.model.PublicationPK;

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
   * Deletes all locations of publications linked to the component instance represented by the
   * given identifier.
   * @param componentInstanceId the identifier of the component instance for which the resources
   * must be deleted.
   * @throws SQLException
   */
  public static void deleteComponentInstanceData(String componentInstanceId) throws SQLException {
    JdbcSqlQuery.createDeleteFor(publicationFatherTableName).where("pubId in (" +
        JdbcSqlQuery.createSelect("pubId from " + PublicationDAO.publicationTableName)
            .where("instanceId = ?").getSqlQuery() + ")", componentInstanceId).execute();
    JdbcSqlQuery.createDeleteFor(publicationFatherTableName)
        .where("instanceId = ?", componentInstanceId).execute();
  }

  /**
   * Add a new father to this publication
   * @param con Connection to database
   * @param pubPK the publication PublicationPK
   * @param fatherPK the father NodePK to add
   * @see NodePK
   * @see org.silverpeas.core.contribution.publication.model.PublicationPK
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static void addFather(Connection con, PublicationPK pubPK,
      NodePK fatherPK) throws SQLException {
    StringBuilder insertStatement = new StringBuilder(128);
    insertStatement.append("insert into ").append(publicationFatherTableName)
        .append(" values (?, ?, ?, ?, ?, ?)");
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement.toString());
      prepStmt.setInt(1, new Integer(pubPK.getId()));
      prepStmt.setInt(2, new Integer(fatherPK.getId()));
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
    StringBuilder statement = new StringBuilder(128);
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

    StringBuilder insertStatement = new StringBuilder(128);
    insertStatement.append("insert into ").append(publicationFatherTableName).
        append(" values (?, ?, ?, ?, ?, ?)");
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement.toString());
      prepStmt.setInt(1, new Integer(pubPK.getId()));
      prepStmt.setInt(2, new Integer(alias.getId()));
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
    StringBuilder statement = new StringBuilder(128);
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

    StringBuilder selectQuery = new StringBuilder(128);
    selectQuery.append(
        "select nodeId, instanceId, aliasUserId, aliasDate, pubOrder from ").
        append(publicationFatherTableName).append(" where pubId = ").append(pubPK.
        getId());
    Statement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(selectQuery.toString());
      List<Alias>  list = new ArrayList<Alias>();

      while (rs.next()) {
        String id = Integer.toString(rs.getInt(1));
        String instanceId = rs.getString(2);
        String userId = Integer.toString(rs.getInt(3));
        String sDate = rs.getString(4);
        Date date = null;
        if (StringUtil.isDefined(sDate)) {
          date = new Date(Long.parseLong(sDate));
        }
        int pubOrder = rs.getInt(5);

        Alias alias = new Alias(id, instanceId);
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
   * @see NodePK
   * @see org.silverpeas.core.contribution.publication.model.PublicationPK
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static void removeFather(Connection con, PublicationPK pubPK,
      NodePK fatherPK) throws SQLException {
    StringBuilder deleteStatement = new StringBuilder(128);
    deleteStatement.append("delete from ").append(publicationFatherTableName)
        .append(" where pubId = ? and nodeId = ? ");
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(deleteStatement.toString());
      prepStmt.setInt(1, new Integer(pubPK.getId()));
      prepStmt.setInt(2, new Integer(fatherPK.getId()));
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
    // get all publications linked to fatherPK
    List<PublicationPK> pubPKs = (List<PublicationPK>) getPubPKsInFatherPK(con, fatherPK);

    // for each publication, remove link into table
    for (PublicationPK publicationPK : pubPKs) {
      removeLink(con, publicationPK, fatherPK);
    }
  }

  private static void removeLink(Connection con, PublicationPK pubPK,
      NodePK fatherPK) throws SQLException {
    StringBuilder deleteStatement = new StringBuilder(128);
    deleteStatement.append("delete from ").append(publicationFatherTableName)
        .append(" where nodeId = ? and pubId = ? ");
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(deleteStatement.toString());
      prepStmt.setInt(1, new Integer(fatherPK.getId()));
      prepStmt.setInt(2, new Integer(pubPK.getId()));
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
    for (final String fatherId : fatherIds) {
      NodePK fatherPK = new NodePK(fatherId, pubPK);
      removeFatherToPublications(con, pubPK, fatherPK);
    }
  }

  /**
   * Delete all fathers to this publication
   * @param con Connection to database
   * @param pubPK the publication PublicationPK
   * @see org.silverpeas.core.contribution.publication.model.PublicationPK
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static void removeAllFather(Connection con, PublicationPK pubPK)
      throws SQLException {
    StringBuilder deleteStatement = new StringBuilder(128);
    deleteStatement.append("delete from ").append(publicationFatherTableName)
        .append(" where pubId = ? ");
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(deleteStatement.toString());
      prepStmt.setInt(1, new Integer(pubPK.getId()));
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
   * @see NodePK
   * @see org.silverpeas.core.contribution.publication.model.PublicationPK
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static Collection<NodePK> getAllFatherPK(Connection con, PublicationPK pubPK)
      throws SQLException {
    return getAllFatherPK(con, pubPK, null);
  }

  public static Collection<NodePK> getAllFatherPK(Connection con, PublicationPK pubPK,
      String order) throws SQLException {

    StringBuilder selectQuery = new StringBuilder(128);
    selectQuery.append("select nodeId from ").append(publicationFatherTableName)
        .append(" where pubId = ").append(pubPK.getId())
        .append(" and instanceId = '" + pubPK.getInstanceId() + "'");
    if (order != null) {
      selectQuery.append(" order by ").append(order);
    }
    Statement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(selectQuery.toString());
      List<NodePK> list = new ArrayList<NodePK>();
      while (rs.next()) {
        String id = Integer.toString(rs.getInt(1));
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

      StringBuilder selectStatement = new StringBuilder(128);
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
          id = Integer.toString(rs.getInt(1));
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
    StringBuilder selectStatement = new StringBuilder(128);
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