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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.util.StringUtil;

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.contribution.publication.social.SocialInformationPublication;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.contribution.publication.model.NodeTree;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.model.PublicationRuntimeException;
import org.silverpeas.core.contribution.publication.model.PublicationWithStatus;

/**
 * This is the Publication Data Access Object.
 * @author Nicolas Eysseric
 */
public class PublicationDAO {
  // if beginDate is null, it will be replace in database with it

  private static final String nullBeginDate = "0000/00/00";
  // if endDate is null, it will be replace in database with it
  private static final String nullEndDate = "9999/99/99";
  // if beginHour is null, it will be replace in database with it
  private static final String nullBeginHour = "00:00";
  // if endDate is null, it will be replace in database with it
  private static final String nullEndHour = "23:59";
  // this object caches last publications availables
  // used only for kmelia
  // keys : componentId
  // values : Collection of PublicationDetail
  private static Map<String, Collection<PublicationDetail>> lastPublis =
      new HashMap<String, Collection<PublicationDetail>>();
  static final String publicationTableName = "SB_Publication_Publi";
  private static final String UPDATE_PUBLICATION =
      "UPDATE SB_Publication_Publi SET infoId = ?, "
          + "pubName = ?, pubDescription = ?, pubCreationDate = ?, pubBeginDate = ?, pubEndDate = ?, "
          + "pubCreatorId = ?, pubImportance = ?, pubVersion = ?, pubKeywords = ?, pubContent = ?, "
          + "pubStatus = ?, pubUpdateDate = ?, pubUpdaterId = ?, "
          + "instanceId = ?, pubValidatorId = ?, pubValidateDate = ?, pubBeginHour = ?, pubEndHour = ?, "
          + "pubAuthor = ?, pubTargetValidatorId = ?, pubCloneId = ?, pubCloneStatus = ?, lang = ?, pubDraftOutDate = ?  "
          + "WHERE pubId = ? ";

  /**
   * This class must not be instanciated
   * @since 1.0
   */
  public PublicationDAO() {
  }

  /**
   * Deletes all publications linked to the component instance represented by the given identifier.
   * @param componentInstanceId the identifier of the component instance for which the resources
   * must be deleted.
   * @throws SQLException
   */
  public static void deleteComponentInstanceData(String componentInstanceId) throws SQLException {
    JdbcSqlQuery.createDeleteFor(publicationTableName).where("instanceId = ?", componentInstanceId)
        .execute();
  }

  /**
   * Invalidate last publications for a given instance
   * @param instanceId
   */
  public static void invalidateLastPublis(String instanceId) {
    lastPublis.remove(instanceId);
  }

  private static void cacheLastPublis(String instanceId,
      Collection<PublicationDetail> lastPublications) {
    lastPublis.put(instanceId, lastPublications);
  }

  private static Collection<PublicationDetail> getLastPublis(String instanceId) {
    Collection<PublicationDetail> listLastPublisCache = lastPublis.get(instanceId);
    if (listLastPublisCache != null && listLastPublisCache.size() > 0) {
      // removing not visible publications from the cache
      ArrayList<PublicationDetail> listLastPublisCacheMAJ = new ArrayList<PublicationDetail>();
      java.util.Date date = new java.util.Date();
      Calendar cDateHeureNow = Calendar.getInstance();
      cDateHeureNow.setTime(date);
      int hourNow = cDateHeureNow.get(Calendar.HOUR_OF_DAY) * 100
          + cDateHeureNow.get(Calendar.MINUTE);
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
      String sDate = simpleDateFormat.format(date);
      java.util.Date theDateFormat = null;
      try {
        theDateFormat = simpleDateFormat.parse(sDate);
      } catch (ParseException e) {
        theDateFormat = java.sql.Date.valueOf(cDateHeureNow.get(Calendar.YEAR)
            + "-"
            + cDateHeureNow.get(Calendar.MONTH)
            + "-" + cDateHeureNow.get(Calendar.DATE));
      }
      Calendar cDateNow = Calendar.getInstance();
      cDateNow.setTime(theDateFormat);
      Calendar cDateBegin = Calendar.getInstance();
      Calendar cDateEnd = Calendar.getInstance();
      String sBeginHour = null;
      String sEndHour = null;
      int beginHour = 0;
      int endHour = 0;
      for (PublicationDetail pubDetail : listLastPublisCache) {
        if (pubDetail.getBeginDate() != null) {
          cDateBegin.setTime(pubDetail.getBeginDate());
        } else {
          cDateBegin.setTime(java.sql.Date.valueOf("0000-01-01"));
        }
        if (pubDetail.getEndDate() != null) {
          cDateEnd.setTime(pubDetail.getEndDate());
        } else {
          cDateEnd.setTime(java.sql.Date.valueOf("9999-12-31"));
        }
        sBeginHour = pubDetail.getBeginHour();// exemple 10:04
        beginHour = Integer.parseInt(sBeginHour.substring(0, 2)) * 100;// 1000
        beginHour += Integer.parseInt(sBeginHour.substring(3));// 1004
        sEndHour = pubDetail.getEndHour();
        endHour = Integer.parseInt(sEndHour.substring(0, 2)) * 100;
        endHour += Integer.parseInt(sEndHour.substring(3));

        if ((cDateNow.after(cDateBegin) && cDateNow.before(cDateEnd))
            || (cDateNow.equals(cDateBegin) && cDateNow.before(cDateEnd) && hourNow > beginHour)
            || (cDateNow.after(cDateBegin) && cDateNow.equals(cDateEnd) && hourNow < endHour)
            || (cDateNow.equals(cDateBegin) && cDateNow.equals(cDateEnd)
            && hourNow > beginHour && hourNow < endHour)) {
          listLastPublisCacheMAJ.add(pubDetail);
        }
      }
      lastPublis.put(instanceId, listLastPublisCacheMAJ);
    }
    return lastPublis.get(instanceId);
  }

  /**
   * Method declaration
   * @param con
   * @param fatherPKs
   * @return
   * @throws SQLException
   * @see
   */
  public static int getNbPubInFatherPKs(Connection con, Collection<NodePK> fatherPKs)
      throws SQLException {
    ResultSet rs = null;
    int result = 0;
    NodePK nodePK = null;
    PublicationPK pubPK = null;
    String nodeId = null;

    if (fatherPKs.isEmpty()) {
      return 0;
    } else {
      Iterator<NodePK> iterator = fatherPKs.iterator();

      if (iterator.hasNext()) {
        nodePK = iterator.next();
        pubPK = new PublicationPK("unknown", nodePK);
        nodeId = nodePK.getId();
      }

      StringBuilder selectStatement = new StringBuilder(128);
      selectStatement.append("select count(F.pubId) from ").append(
          pubPK.getTableName()).append("Father F, ").append(
          pubPK.getTableName()).append(" P ");
      selectStatement.append(" where F.pubId = P.pubId ");
      selectStatement.append(" and (");
      selectStatement.append("( ? > pubBeginDate AND ? < pubEndDate ) OR ");
      selectStatement.append("( ? = pubBeginDate AND ? < pubEndDate AND ? > pubBeginHour ) OR ");
      selectStatement.append("( ? > pubBeginDate AND ? = pubEndDate AND ? < pubEndHour ) OR ");
      selectStatement.append(
          "( ? = pubBeginDate AND ? = pubEndDate AND ? > pubBeginHour AND ? < pubEndHour )");
      selectStatement.append(" ) ");
      selectStatement.append(" and ( F.nodeId = ").append(nodeId);

      while (iterator.hasNext()) {
        nodePK = iterator.next();
        nodeId = nodePK.getId();
        selectStatement.append(" or F.nodeId = ").append(nodeId);
      }
      selectStatement.append(" )");

      PreparedStatement prepStmt = null;
      try {
        java.util.Date now = new java.util.Date();
        String dateNow = null;
        dateNow = DateUtil.formatDate(now);
        String hourNow = null;
        hourNow = DateUtil.formatTime(now);
        prepStmt = con.prepareStatement(selectStatement.toString());

        prepStmt.setString(1, dateNow);
        prepStmt.setString(2, dateNow);
        prepStmt.setString(3, dateNow);
        prepStmt.setString(4, dateNow);
        prepStmt.setString(5, hourNow);
        prepStmt.setString(6, dateNow);
        prepStmt.setString(7, dateNow);
        prepStmt.setString(8, hourNow);
        prepStmt.setString(9, dateNow);
        prepStmt.setString(10, dateNow);
        prepStmt.setString(11, hourNow);
        prepStmt.setString(12, hourNow);

        rs = prepStmt.executeQuery();
        if (rs.next()) {
          result = rs.getInt(1);
        }
      } finally {
        DBUtil.close(rs, prepStmt);
      }
      return result;
    }
  }

  /**
   * Method declaration
   * @param con
   * @param fatherPK
   * @param fatherPath
   * @return
   * @throws SQLException
   * @see
   */
  public static int getNbPubByFatherPath(Connection con, NodePK fatherPK,
      String fatherPath) throws SQLException {
    int result = 0;
    PublicationPK pubPK = new PublicationPK("unknown", fatherPK);

    if (fatherPath.length() <= 0) {
      return 0;
    } else {
      StringBuilder selectStatement = new StringBuilder(128);
      selectStatement.append("select count(F.pubId) from ").append(
          pubPK.getTableName()).append("Father F, ").append(
          pubPK.getTableName()).append(" P, ").append(fatherPK.getTableName()).append(" N ");
      selectStatement.append(" where F.pubId = P.pubId ");
      selectStatement.append(" and F.nodeId = N.nodeId ");
      selectStatement.append(" and P.instanceId = ? ");
      selectStatement.append(" and N.instanceId  = ? ");
      selectStatement.append(" and (");
      selectStatement.append("( ? > P.pubBeginDate AND ? < P.pubEndDate ) OR ");
      selectStatement.append(
          "( ? = P.pubBeginDate AND ? < P.pubEndDate AND ? > P.pubBeginHour ) OR ");
      selectStatement
          .append("( ? > P.pubBeginDate AND ? = P.pubEndDate AND ? < P.pubEndHour ) OR ");
      selectStatement
          .append(
          "( ? = P.pubBeginDate AND ? = P.pubEndDate AND ? > P.pubBeginHour AND ? < P.pubEndHour )");
      selectStatement.append(" ) ");
      selectStatement.append(" and (N.nodePath like '").append(fatherPath).append("/").append(
          fatherPK.getId()).append("%' or N.nodeId = ").append(fatherPK.getId()).append(")");

      PreparedStatement prepStmt = null;
      ResultSet rs = null;
      try {
        java.util.Date now = new java.util.Date();
        String dateNow = DateUtil.formatDate(now);
        String hourNow = DateUtil.formatTime(now);
        prepStmt = con.prepareStatement(selectStatement.toString());

        prepStmt.setString(1, fatherPK.getComponentName());
        prepStmt.setString(2, fatherPK.getComponentName());

        prepStmt.setString(3, dateNow);
        prepStmt.setString(4, dateNow);
        prepStmt.setString(5, dateNow);
        prepStmt.setString(6, dateNow);
        prepStmt.setString(7, hourNow);
        prepStmt.setString(8, dateNow);
        prepStmt.setString(9, dateNow);
        prepStmt.setString(10, hourNow);
        prepStmt.setString(11, dateNow);
        prepStmt.setString(12, dateNow);
        prepStmt.setString(13, hourNow);
        prepStmt.setString(14, hourNow);

        rs = prepStmt.executeQuery();
        if (rs.next()) {
          result = rs.getInt(1);
        }
        return result;
      } finally {
        DBUtil.close(rs, prepStmt);
      }
    }
  }

  public static NodeTree getDistributionTree(Connection con, String instanceId,
      String statusSubQuery, boolean checkVisibility) throws SQLException {
    Map<String, NodeTree> nodes = new HashMap<String, NodeTree>();
    NodeTree rootNode = null;
    StringBuilder selectStatement = new StringBuilder(128);

    selectStatement
        .append("SELECT sb_node_node.nodeId, sb_node_node.nodefatherid, ")
        .append(
        "COUNT(sb_publication_publi.pubName) AS nbPubli FROM sb_node_node LEFT OUTER JOIN ")
        .append(
        "sb_publication_publifather ON sb_publication_publifather.nodeid = sb_node_node.nodeid ")
        .
        append(
        "AND sb_publication_publifather.instanceId = ? LEFT OUTER JOIN sb_publication_publi ").
        append("ON sb_publication_publifather.pubId = sb_publication_publi.pubId ");
    if (statusSubQuery != null) {
      selectStatement.append(statusSubQuery);
    }
    if (checkVisibility) {
      selectStatement
          .append(" AND (( ? > sb_publication_publi.pubBeginDate AND ")
          .append(
              "? < sb_publication_publi.pubEndDate ) OR ( ? = sb_publication_publi.pubBeginDate AND ")
          .
          append("? < sb_publication_publi.pubEndDate AND ? > sb_publication_publi.pubBeginHour ) ")
          .
          append("OR ( ? > sb_publication_publi.pubBeginDate AND ")
          .append(
              "? = sb_publication_publi.pubEndDate AND ? < sb_publication_publi.pubEndHour ) OR "
                  + "( ? = sb_publication_publi.pubBeginDate AND ? = sb_publication_publi.pubEndDate "
                  + "AND ? > sb_publication_publi.pubBeginHour AND ? < sb_publication_publi.pubEndHour )) ");
    }
    selectStatement
        .append(
        " WHERE sb_node_node.instanceId = ? GROUP BY sb_node_node.nodeId, sb_node_node.nodefatherid");

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      java.util.Date now = new java.util.Date();
      String dateNow = DateUtil.formatDate(now);


      String hourNow = DateUtil.formatTime(now);


      prepStmt = con.prepareStatement(selectStatement.toString());

      prepStmt.setString(1, instanceId);
      if (checkVisibility) {
        prepStmt.setString(2, dateNow);
        prepStmt.setString(3, dateNow);
        prepStmt.setString(4, dateNow);
        prepStmt.setString(5, dateNow);
        prepStmt.setString(6, hourNow);
        prepStmt.setString(7, dateNow);
        prepStmt.setString(8, dateNow);
        prepStmt.setString(9, hourNow);
        prepStmt.setString(10, dateNow);
        prepStmt.setString(11, dateNow);
        prepStmt.setString(12, hourNow);
        prepStmt.setString(13, hourNow);
        prepStmt.setString(14, instanceId);
      } else {
        prepStmt.setString(2, instanceId);
      }

      rs = prepStmt.executeQuery();
      while (rs.next()) {
        int nodeId = rs.getInt("nodeId");
        String nodeIdentifier = String.valueOf(nodeId);
        NodeTree nodeTree;
        if (nodes.containsKey(nodeIdentifier)) {
          nodeTree = nodes.get(nodeIdentifier);
        } else {
          nodeTree = new NodeTree(new NodePK(nodeIdentifier, instanceId));
          nodes.put(nodeIdentifier, nodeTree);
        }
        nodeTree.setNbPublications(rs.getInt("nbPubli"));
        String fatherId = rs.getString("nodefatherid");
        NodeTree father;
        if (nodes.containsKey(fatherId)) {
          father = nodes.get(fatherId);
        } else {
          if (Integer.parseInt(fatherId) >= 0) { // Not a root node
            father = new NodeTree(new NodePK(String.valueOf(fatherId), instanceId));
            if (NodePK.ROOT_NODE_ID.equals(String.valueOf(fatherId))) {
              rootNode = father;
            }
            nodes.put(fatherId, father);
          } else {
            father = null;
            if (NodePK.ROOT_NODE_ID.equals(nodeIdentifier)) {
              rootNode = nodeTree;
            }
          }
        }
        if (father != null) {
          father.addChild(nodeTree);
          nodeTree.setParent(father);
        }
      }
      return rootNode;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param detail
   * @throws SQLException
   * @see
   */
  public static void insertRow(Connection con, PublicationDetail detail)
      throws SQLException {
    StringBuilder insertStatement = new StringBuilder(128);
    insertStatement.append("insert into ").append(detail.getPK().getTableName());
    insertStatement.append(" (pubId, infoId, pubName, pubDescription, pubCreationDate,");
    insertStatement.append(
        " pubBeginDate, pubEndDate, pubCreatorId, pubImportance, pubVersion, pubKeywords,");
    insertStatement.append(" pubContent, pubStatus, pubUpdateDate,");
    insertStatement.append(
        " instanceId, pubUpdaterId, pubValidateDate, pubValidatorId, pubBeginHour, pubEndHour,");
    insertStatement.append(
        " pubAuthor, pubTargetValidatorId, pubCloneId, pubCloneStatus, lang, pubDraftOutDate) ");
    insertStatement
        .append(
        " values ( ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ?, ?, ? , ? , ? , ? , ? , ? , ? , ?, ?)");
    PreparedStatement prepStmt = null;

    try {
      int id = new Integer(detail.getPK().getId()).intValue();
      String infoId = "0";
      if (StringUtil.isDefined(detail.getInfoId())) {
        infoId = detail.getInfoId();
      }

      prepStmt = con.prepareStatement(insertStatement.toString());
      prepStmt.setInt(1, id);
      prepStmt.setString(2, infoId);

      prepStmt.setString(3, detail.getName());
      prepStmt.setString(4, detail.getDescription());
      if (detail.getCreationDate() == null) {
        prepStmt.setString(5, DateUtil.today2SQLDate());
      } else {
        prepStmt.setString(5, DateUtil.formatDate(detail.getCreationDate()));
      }
      if (detail.getBeginDate() == null) {
        prepStmt.setString(6, nullBeginDate);
      } else {
        prepStmt.setString(6, DateUtil.formatDate(detail.getBeginDate()));
      }
      if (detail.getEndDate() == null) {
        prepStmt.setString(7, nullEndDate);
      } else {
        prepStmt.setString(7, DateUtil.formatDate(detail.getEndDate()));
      }
      prepStmt.setString(8, detail.getCreatorId());
      prepStmt.setInt(9, detail.getImportance());
      prepStmt.setString(10, detail.getVersion());
      prepStmt.setString(11, detail.getKeywords());
      prepStmt.setString(12, detail.getContent());
      prepStmt.setString(13, detail.getStatus());
      if (detail.isUpdateDateMustBeSet() && detail.getUpdateDate() != null) {
        prepStmt.setString(14, DateUtil.formatDate(detail.getUpdateDate()));
      } else {
        if (detail.getCreationDate() == null) {
          prepStmt.setString(14, DateUtil.today2SQLDate());
        } else {
          prepStmt.setString(14, DateUtil.formatDate(detail.getCreationDate()));
        }
      }
      prepStmt.setString(15, detail.getPK().getComponentName());
      if (detail.isUpdateDateMustBeSet() && StringUtil.isDefined(detail.getUpdaterId())) {
        prepStmt.setString(16, detail.getUpdaterId());
      } else {
        prepStmt.setString(16, detail.getCreatorId());
      }
      if (detail.getValidateDate() == null) {
        prepStmt.setString(17, null);
      } else {
        prepStmt.setString(17, DateUtil.formatDate(detail.getValidateDate()));
      }
      prepStmt.setString(18, detail.getValidatorId());
      if (isUndefined(detail.getBeginHour())) {
        prepStmt.setString(19, nullBeginHour);
      } else {
        prepStmt.setString(19, detail.getBeginHour());
      }
      if (isUndefined(detail.getEndHour())) {
        prepStmt.setString(20, nullEndHour);
      } else {
        prepStmt.setString(20, detail.getEndHour());
      }
      if (isUndefined(detail.getAuthor())) {
        prepStmt.setString(21, null);
      } else {
        prepStmt.setString(21, detail.getAuthor());
      }

      prepStmt.setString(22, detail.getTargetValidatorId());

      if (isUndefined(detail.getCloneId())) {
        prepStmt.setInt(23, -1);
      } else {
        prepStmt.setInt(23, Integer.parseInt(detail.getCloneId()));
      }

      prepStmt.setString(24, detail.getCloneStatus());

      if (isUndefined(detail.getLanguage())) {
        prepStmt.setNull(25, Types.VARCHAR);
      } else {
        prepStmt.setString(25, detail.getLanguage());
      }

      prepStmt.setString(26, DateUtil.formatDate(detail.getDraftOutDate()));



      prepStmt.executeUpdate();
    } catch (Exception e) {
      SilverTrace.error("publication", "PublicationDAO.insertRow()",
          "root.MSG_GEN_PARAM_VALUE", e);
    } finally {
      DBUtil.close(prepStmt);
    }
    invalidateLastPublis(detail.getPK().getComponentName());
  }

  private static boolean isUndefined(String object) {
    return !StringUtil.isDefined(object);
  }

  /**
   * Method declaration
   * @param con
   * @param pk
   * @throws SQLException
   * @see
   */
  public static void deleteRow(Connection con, PublicationPK pk)
      throws SQLException {
    PublicationFatherDAO.removeAllFather(con, pk); // Delete associations
    // between pub and nodes
    StringBuilder deleteStatement = new StringBuilder(128);
    deleteStatement.append("delete from ").append(pk.getTableName()).append(
        " where pubId = ").append(pk.getId());
    Statement stmt = null;

    try {
      stmt = con.createStatement();
      stmt.executeUpdate(deleteStatement.toString());
    } finally {
      DBUtil.close(stmt);
    }

    invalidateLastPublis(pk.getComponentName());
  }

  public static PublicationDetail selectByPrimaryKey(Connection con,
      PublicationPK primaryKey) throws SQLException {

    try {
      return loadRow(con, primaryKey);
    } catch (PublicationRuntimeException e) {
      /*
       * NodeRuntimeException thrown by loadRow() should be replaced by returning null (not found)
       */
      return null;
    }
  }

  public static PublicationDetail selectByPublicationName(Connection con,
      PublicationPK primaryKey, String name) throws SQLException {
    return selectByName(con, primaryKey, name);
  }

  public static PublicationDetail selectByPublicationNameAndNodeId(Connection con,
      PublicationPK primaryKey, String name, int nodeId) throws SQLException {
    return selectByNameAndNodeId(con, primaryKey, name, nodeId);
  }

  private static PublicationDetail resultSet2PublicationDetail(ResultSet rs,
      PublicationPK pubPK) throws SQLException {
    return resultSet2PublicationDetail(rs, pubPK, false);
  }

  private static PublicationDetail resultSet2PublicationDetail(ResultSet rs,
      PublicationPK pubPK, boolean getSort) throws SQLException {
    // SilverTrace.debug("publication",
    // "PublicationDAO.resultSet2PublicationDetail()",
    // "root.MSG_GEN_ENTER_METHOD", "pubPK = " + pubPK.toString());
    PublicationDetail pub = null;
    int id = rs.getInt("pubid");
    String componentId = rs.getString(15);
    PublicationPK pk = new PublicationPK(String.valueOf(id), componentId);
    String infoId = rs.getString("infoid");
    String name = rs.getString("pubname");
    String description = rs.getString("pubDescription");
    if (description == null) {
      description = "";
    }
    Date creationDate;
    try {
      creationDate = DateUtil.parseDate(rs.getString("pubCreationDate"));
    } catch (java.text.ParseException e) {
      throw new SQLException(
          "PublicationDAO : resultSet2PublicationDetail() : internal error : "
          + "creationDate format unknown for publication.pk = " + pk
          + " : " + e.toString());
    }
    Date beginDate;
    String d = rs.getString("pubBeginDate");

    if (d.equals(nullBeginDate)) {
      beginDate = null;
    } else {
      try {
        beginDate = DateUtil.parseDate(d);
      } catch (java.text.ParseException e) {
        throw new SQLException(
            "PublicationDAO : resultSet2PublicationDetail() : internal error : "
            + "beginDate format unknown for publication.pk = "
            + pk.toString() + " : " + e.toString());
      }
    }
    Date endDate;

    d = rs.getString("pubEndDate");
    if (d.equals(nullEndDate)) {
      endDate = null;
    } else {
      try {
        endDate = DateUtil.parseDate(d);
      } catch (java.text.ParseException e) {
        throw new SQLException(
            "PublicationDAO : resultSet2PublicationDetail() : internal error : "
            + "endDate format unknown for publication.pk = "
            + pk.toString() + " : " + e.toString());
      }
    }
    String creatorId = rs.getString("pubCreatorId");
    int importance = rs.getInt("pubImportance");
    String version = rs.getString("pubVersion");
    String keywords = rs.getString("pubKeywords");
    String content = rs.getString("pubContent");
    String status = rs.getString("pubStatus");

    Date updateDate;
    String u = rs.getString("pubUpdateDate");
    if (u != null) {
      try {
        updateDate = DateUtil.parseDate(u);
      } catch (java.text.ParseException e) {
        throw new SQLException(
            "PublicationDAO : resultSet2PublicationDetail() : internal error : updateDate format unknown for publication.pk = "
                +
                pk + " : " + e.toString());
      }
    } else {
      updateDate = creationDate;
    }
    String updaterId;
    String v = rs.getString("pubUpdaterId");
    if (v != null) {
      updaterId = v;
    } else {
      updaterId = creatorId;
    }

    Date validateDate = null;
    String strValDate = rs.getString("pubValidateDate");
    try {
      validateDate = DateUtil.parseDate(strValDate);
    } catch (java.text.ParseException e) {
      throw new SQLException(
          "PublicationDAO : resultSet2PublicationDetail() : internal error : validateDate format unknown for publication.pk = "
              +
              pk + " : " + e.toString());
    }
    String validatorId = rs.getString("pubValidatorId");

    String beginHour = rs.getString("pubBeginHour");
    String endHour = rs.getString("pubEndHour");
    String author = rs.getString("pubAuthor");
    String targetValidatorId = rs.getString("pubTargetValidatorId");
    int tempPubId = rs.getInt("pubCloneId");
    String cloneStatus = rs.getString("pubCloneStatus");
    String lang = rs.getString("lang");

    Date draftOutDate = null;
    try {
      draftOutDate = DateUtil.parseDate(rs.getString("pubdraftoutdate"));
    } catch (java.text.ParseException e) {
      throw new SQLException(
          "PublicationDAO : resultSet2PublicationDetail() : internal error : draftOutDate format unknown for publication.pk = "
              +
              pk + " : " + e.toString());
    }
    int order = -1;
    if (getSort) {
      order = rs.getInt("pubOrder");
    }
    pub = new PublicationDetail(pk, name, description, creationDate, beginDate,
        endDate, creatorId, importance, version, keywords, content, status,
        updateDate, updaterId, validateDate, validatorId,
        author);

    pub.setInfoId(infoId);
    pub.setBeginHour(beginHour);
    pub.setEndHour(endHour);
    pub.setTargetValidatorId(targetValidatorId);
    pub.setCloneId(Integer.toString(tempPubId));
    pub.setCloneStatus(cloneStatus);
    pub.setLanguage(lang);
    pub.setDraftOutDate(draftOutDate);
    pub.setExplicitRank(order);

    return pub;
  }

  /**
   * Method declaration
   * @param con
   * @param fatherPK
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<PublicationDetail> selectByFatherPK(Connection con, NodePK fatherPK)
      throws SQLException {
    return selectByFatherPK(con, fatherPK, null);
  }

  public static Collection<PublicationDetail> selectByFatherPK(Connection con, NodePK fatherPK,
      boolean filterOnVisibilityPeriod) throws SQLException {
    return selectByFatherPK(con, fatherPK, null, filterOnVisibilityPeriod);
  }

  public static Collection<PublicationDetail> selectByFatherPK(Connection con, NodePK fatherPK,
      String sorting, boolean filterOnVisibilityPeriod) throws SQLException {
    return selectByFatherPK(con, fatherPK, sorting, filterOnVisibilityPeriod, null);
  }

  public static Collection<PublicationDetail> selectByFatherPK(Connection con, NodePK fatherPK,
      String sorting, boolean filterOnVisibilityPeriod, String userId)
      throws SQLException {
    PublicationPK pubPK = new PublicationPK("unknown", fatherPK);
    StringBuilder selectStatement = new StringBuilder(QueryStringFactory.getSelectByFatherPK(
        pubPK.getTableName(), filterOnVisibilityPeriod, userId));
    if (sorting != null) {
      selectStatement.append(", ").append(sorting);
    }
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(selectStatement.toString());
      prepStmt.setString(1, pubPK.getComponentName());
      prepStmt.setInt(2, Integer.parseInt(fatherPK.getId()));
      int index = 3;
      if (StringUtil.isDefined(userId)) {
        prepStmt.setString(3, userId);
        prepStmt.setString(4, userId);
        index = 5;
      }
      if (filterOnVisibilityPeriod) {
        java.util.Date now = new java.util.Date();
        String dateNow = DateUtil.formatDate(now);

        String hourNow = null;
        hourNow = DateUtil.formatTime(now);

        prepStmt.setString(index, dateNow);
        prepStmt.setString(++index, dateNow);
        prepStmt.setString(++index, dateNow);
        prepStmt.setString(++index, dateNow);
        prepStmt.setString(++index, hourNow);
        prepStmt.setString(++index, dateNow);
        prepStmt.setString(++index, dateNow);
        prepStmt.setString(++index, hourNow);
        prepStmt.setString(++index, dateNow);
        prepStmt.setString(++index, dateNow);
        prepStmt.setString(++index, hourNow);
        prepStmt.setString(++index, hourNow);
      }

      rs = prepStmt.executeQuery();
      List<PublicationDetail> list = new ArrayList<PublicationDetail>();
      PublicationDetail pub = null;
      while (rs.next()) {
        pub = resultSet2PublicationDetail(rs, pubPK, true);
        list.add(pub);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static Collection<PublicationDetail> selectByFatherPK(Connection con, NodePK fatherPK,
      String sorting) throws SQLException {
    return selectByFatherPK(con, fatherPK, sorting, true, null);
  }

  /**
   * Method declaration
   * @param con
   * @param fatherPK
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<PublicationDetail> selectNotInFatherPK(Connection con, NodePK fatherPK)
      throws SQLException {
    return selectNotInFatherPK(con, fatherPK, null);
  }

  public static Collection<PublicationDetail> selectNotInFatherPK(Connection con, NodePK fatherPK,
      String sorting) throws SQLException {
    PublicationPK pubPK = new PublicationPK("unknown", fatherPK);
    String selectStatement = QueryStringFactory.getSelectNotInFatherPK(pubPK.getTableName());

    if (sorting != null) {
      selectStatement += " order by " + sorting;
    }



    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      java.util.Date now = new java.util.Date();
      String dateNow = DateUtil.formatDate(now);

      String hourNow = DateUtil.formatTime(now);


      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, pubPK.getComponentName());
      prepStmt.setInt(2, new Integer(fatherPK.getId()).intValue());

      prepStmt.setString(3, dateNow);
      prepStmt.setString(4, dateNow);
      prepStmt.setString(5, dateNow);
      prepStmt.setString(6, dateNow);
      prepStmt.setString(7, hourNow);
      prepStmt.setString(8, dateNow);
      prepStmt.setString(9, dateNow);
      prepStmt.setString(10, hourNow);
      prepStmt.setString(11, dateNow);
      prepStmt.setString(12, dateNow);
      prepStmt.setString(13, hourNow);
      prepStmt.setString(14, hourNow);

      rs = prepStmt.executeQuery();
      ArrayList<PublicationDetail> list = new ArrayList<PublicationDetail>();
      PublicationDetail pub = null;
      while (rs.next()) {
        pub = resultSet2PublicationDetail(rs, pubPK);
        list.add(pub);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static ArrayList<PublicationDetail> selectByFatherIds(Connection con,
      List<String> fatherIds, PublicationPK pubPK, String sorting,
      List<String> status, boolean filterOnVisibilityPeriod) throws SQLException {

    ArrayList<PublicationDetail> list = new ArrayList<PublicationDetail>();
    ResultSet rs = null;
    PublicationDetail pub = null;

    String fatherId = "";
    StringBuilder whereClause = new StringBuilder(128);

    if (fatherIds != null) {
      Iterator<String> it = fatherIds.iterator();

      whereClause.append("(");
      while (it.hasNext()) {
        fatherId = it.next();
        whereClause.append(" F.nodeId = ").append(fatherId);
        if (it.hasNext()) {
          whereClause.append(" or ");
        } else {
          whereClause.append(" ) ");
        }
      }
    }

    StringBuilder selectStatement = new StringBuilder(128);
    selectStatement
        .append(
        "select  distinct P.pubId, P.infoId, P.pubName, P.pubDescription, P.pubCreationDate, P.pubBeginDate, ");
    selectStatement
        .append(
        "         P.pubEndDate, P.pubCreatorId, P.pubImportance, P.pubVersion, P.pubKeywords, P.pubContent, ");
    selectStatement
        .append(
        "		 P.pubStatus, P.pubUpdateDate, P.instanceId, P.pubUpdaterId, P.pubValidateDate, P.pubValidatorId, P.pubBeginHour, P.pubEndHour, P.pubAuthor, P.pubTargetValidatorId, P.pubCloneId, P.pubCloneStatus, P.lang, P.pubdraftoutdate, F.puborder ");
    selectStatement.append("from ").append(pubPK.getTableName()).append(" P, ").append(pubPK.
        getTableName()).append("Father F ");

    selectStatement.append("where ").append(whereClause.toString());

    if (filterOnVisibilityPeriod) {
      selectStatement.append(" and (");
      selectStatement.append("( ? > P.pubBeginDate AND ? < P.pubEndDate ) OR ");
      selectStatement.append(
          "( ? = P.pubBeginDate AND ? < P.pubEndDate AND ? > P.pubBeginHour ) OR ");
      selectStatement
          .append("( ? > P.pubBeginDate AND ? = P.pubEndDate AND ? < P.pubEndHour ) OR ");
      selectStatement
          .append(
          "( ? = P.pubBeginDate AND ? = P.pubEndDate AND ? > P.pubBeginHour AND ? < P.pubEndHour )");
      selectStatement.append(" ) ");
    }
    selectStatement.append(" and F.pubId = P.pubId ");
    selectStatement.append(" and F.instanceId='").append(
        pubPK.getComponentName()).append("'");

    if (status != null && status.size() > 0) {
      StringBuilder statusBuffer = new StringBuilder();
      Iterator<String> it = status.iterator();

      statusBuffer.append("(");
      String sStatus = null;
      while (it.hasNext()) {
        sStatus = it.next();
        statusBuffer.append(" P.pubStatus = '").append(sStatus).append("'");
        if (it.hasNext()) {
          statusBuffer.append(" or ");
        } else {
          statusBuffer.append(" ) ");
        }
      }
      selectStatement.append(" and ").append(statusBuffer.toString());
    }

    if (sorting != null) {
      selectStatement.append(" order by ").append(sorting);
    }

    PreparedStatement prepStmt = null;

    try {
      java.util.Date now = new java.util.Date();
      String dateNow = DateUtil.formatDate(now);

      String hourNow = DateUtil.formatTime(now);


      prepStmt = con.prepareStatement(selectStatement.toString());

      if (filterOnVisibilityPeriod) {
        prepStmt.setString(1, dateNow);
        prepStmt.setString(2, dateNow);
        prepStmt.setString(3, dateNow);
        prepStmt.setString(4, dateNow);
        prepStmt.setString(5, hourNow);
        prepStmt.setString(6, dateNow);
        prepStmt.setString(7, dateNow);
        prepStmt.setString(8, hourNow);
        prepStmt.setString(9, dateNow);
        prepStmt.setString(10, dateNow);
        prepStmt.setString(11, hourNow);
        prepStmt.setString(12, hourNow);
      }

      rs = prepStmt.executeQuery();

      while (rs.next()) {
        pub = resultSet2PublicationDetail(rs, pubPK);
        list.add(pub);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return list;
  }

  public static Collection<PublicationDetail> selectByPublicationPKs(Connection con,
      Collection<PublicationPK> publicationPKs) {
    ArrayList<PublicationDetail> publications = new ArrayList<PublicationDetail>();
    Iterator<PublicationPK> iterator = publicationPKs.iterator();

    while (iterator.hasNext()) {
      PublicationPK pubPK = iterator.next();
      PublicationDetail pub;
      try {
        pub = loadRow(con, pubPK);
        publications.add(pub);
      } catch (Exception e) {
        SilverTrace.error("publication", "PublicationDAO.selectByPublicationPKs",
            "publication.GETTING_PUBLICATION_FAILED", "pubPK = " + pubPK.toString());
      }
    }
    return publications;
  }

  /**
   * Method declaration
   * @param con
   * @param pubPK
   * @param status
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<PublicationDetail> selectByStatus(Connection con, PublicationPK pubPK,
      String status) throws SQLException {
    StringBuilder selectStatement = new StringBuilder(128);
    selectStatement.append("select * from ").append(pubPK.getTableName());
    selectStatement.append(" where pubStatus like '").append(status).append(
        "' ");
    selectStatement.append(" and instanceId ='").append(
        pubPK.getComponentName()).append(
        "' order by pubUpdateDate desc, pubId desc");
    Statement stmt = null;
    ResultSet rs = null;

    try {
      PublicationDetail pub = null;
      stmt = con.createStatement();
      rs = stmt.executeQuery(selectStatement.toString());
      List<PublicationDetail> list = new ArrayList<PublicationDetail>();
      while (rs.next()) {
        pub = resultSet2PublicationDetail(rs, pubPK);
        list.add(pub);
      }
      return list;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  public static Collection<PublicationDetail> selectByStatus(Connection con,
      List<String> componentIds,
      String status) throws SQLException {
    List<PublicationDetail> list = new ArrayList<PublicationDetail>();
    if (componentIds != null && componentIds.size() > 0) {
      StringBuilder selectStatement = new StringBuilder(128);
      selectStatement
          .append(
          "select  distinct P.pubId, P.infoId, P.pubName, P.pubDescription, P.pubCreationDate, P.pubBeginDate, ");
      selectStatement
          .append(
          "         P.pubEndDate, P.pubCreatorId, P.pubImportance, P.pubVersion, P.pubKeywords, P.pubContent, ");
      selectStatement
          .append(
          "		 P.pubStatus, P.pubUpdateDate, P.instanceId, P.pubUpdaterId, P.pubValidateDate, P.pubValidatorId, P.pubBeginHour, P.pubEndHour, P.pubAuthor, P.pubTargetValidatorId, P.pubCloneId, P.pubCloneStatus, P.lang, P.pubdraftoutdate ");
      selectStatement.append("from ").append(publicationTableName).append(
          " P, ").append(publicationTableName).append("Father F ");
      selectStatement.append("where P.pubStatus = '").append(status).append("'");
      selectStatement.append(" and F.nodeId <> 1 ");
      selectStatement.append(" and (");
      selectStatement.append("( ? > P.pubBeginDate AND ? < P.pubEndDate ) OR ");
      selectStatement.append(
          "( ? = P.pubBeginDate AND ? < P.pubEndDate AND ? > P.pubBeginHour ) OR ");
      selectStatement
          .append("( ? > P.pubBeginDate AND ? = P.pubEndDate AND ? < P.pubEndHour ) OR ");
      selectStatement
          .append(
          "( ? = P.pubBeginDate AND ? = P.pubEndDate AND ? > P.pubBeginHour AND ? < P.pubEndHour )");
      selectStatement.append(" ) ");
      selectStatement.append(" and F.pubId = P.pubId ");
      selectStatement.append(" and (");

      String componentId = null;
      for (int c = 0; c < componentIds.size(); c++) {
        componentId = componentIds.get(c);
        if (c != 0) {
          selectStatement.append(" OR ");
        }
        selectStatement.append("P.instanceId ='").append(componentId).append(
            "'");
      }
      selectStatement.append(")");
      selectStatement.append(" order by P.pubUpdateDate desc, P.pubId desc");

      PreparedStatement prepStmt = null;
      ResultSet rs = null;

      try {
        java.util.Date now = new java.util.Date();
        String dateNow = DateUtil.formatDate(now);

        String hourNow = DateUtil.formatTime(now);


        prepStmt = con.prepareStatement(selectStatement.toString());

        prepStmt.setString(1, dateNow);
        prepStmt.setString(2, dateNow);
        prepStmt.setString(3, dateNow);
        prepStmt.setString(4, dateNow);
        prepStmt.setString(5, hourNow);
        prepStmt.setString(6, dateNow);
        prepStmt.setString(7, dateNow);
        prepStmt.setString(8, hourNow);
        prepStmt.setString(9, dateNow);
        prepStmt.setString(10, dateNow);
        prepStmt.setString(11, hourNow);
        prepStmt.setString(12, hourNow);

        rs = prepStmt.executeQuery();

        PublicationPK pubPK = new PublicationPK("unknown", "useless", "unknown");
        PublicationDetail pub = null;
        while (rs.next()) {
          componentId = rs.getString(17);
          pubPK.setComponentName(componentId);

          pub = resultSet2PublicationDetail(rs, pubPK);
          list.add(pub);
        }
      } finally {
        DBUtil.close(rs, prepStmt);
      }
    }
    return list;
  }

  public static Collection<PublicationPK> selectPKsByStatus(Connection con,
      List<String> componentIds, String status) throws SQLException {
    List<PublicationPK> list = new ArrayList<PublicationPK>();
    if (componentIds != null && componentIds.size() > 0) {
      StringBuilder selectStatement = new StringBuilder(128);
      selectStatement.append("SELECT  DISTINCT(P.pubId), P.instanceId, P.pubUpdateDate ");
      selectStatement.append("FROM SB_Publication_Publi P, SB_Publication_PubliFather F ");
      selectStatement.append("WHERE P.pubStatus = ? AND F.nodeId <> 1 AND (");
      selectStatement.append("( ? > P.pubBeginDate AND ? < P.pubEndDate ) OR ");
      selectStatement.append(
          "( ? = P.pubBeginDate AND ? < P.pubEndDate AND ? > P.pubBeginHour ) OR ");
      selectStatement
          .append("( ? > P.pubBeginDate AND ? = P.pubEndDate AND ? < P.pubEndHour ) OR ");
      selectStatement
          .append(
          "( ? = P.pubBeginDate AND ? = P.pubEndDate AND ? > P.pubBeginHour AND ? < P.pubEndHour )");
      selectStatement.append(" ) ");
      selectStatement.append(" and F.pubId = P.pubId ");
      selectStatement.append(" and P.instanceId IN (");
      String componentId = null;
      for (int c = 0; c < componentIds.size(); c++) {
        componentId = componentIds.get(c);
        if (c != 0) {
          selectStatement.append(", ");
        }
        selectStatement.append("'").append(componentId).append("'");
      }
      selectStatement.append(")");
      selectStatement.append(" order by P.pubUpdateDate desc, P.pubId desc");

      PreparedStatement prepStmt = null;
      ResultSet rs = null;
      try {
        java.util.Date now = new java.util.Date();
        String dateNow = DateUtil.formatDate(now);

        String hourNow = DateUtil.formatTime(now);

        prepStmt = con.prepareStatement(selectStatement.toString());

        prepStmt.setString(1, status);
        prepStmt.setString(2, dateNow);
        prepStmt.setString(3, dateNow);
        prepStmt.setString(4, dateNow);
        prepStmt.setString(5, dateNow);
        prepStmt.setString(6, hourNow);
        prepStmt.setString(7, dateNow);
        prepStmt.setString(8, dateNow);
        prepStmt.setString(9, hourNow);
        prepStmt.setString(10, dateNow);
        prepStmt.setString(11, dateNow);
        prepStmt.setString(12, hourNow);
        prepStmt.setString(13, hourNow);
        rs = prepStmt.executeQuery();
        PublicationPK pubPK = null;
        while (rs.next()) {
          pubPK = new PublicationPK(rs.getString("pubId"), rs.getString("instanceId"));
          list.add(pubPK);
        }
      } finally {
        DBUtil.close(rs, prepStmt);
      }
    }
    return list;
  }

  public static Collection<PublicationPK> selectUpdatedPublicationsSince(Connection con,
      List<String> componentIds, String status, java.util.Date since, int maxSize) throws
      SQLException {
    List<PublicationPK> list = new ArrayList<PublicationPK>();
    if (componentIds != null && componentIds.size() > 0) {
      StringBuilder selectStatement = new StringBuilder(128);
      selectStatement.append("SELECT  DISTINCT(P.pubId), P.instanceId, P.pubUpdateDate ");
      selectStatement.append("FROM SB_Publication_Publi P, SB_Publication_PubliFather F ");
      selectStatement.append("WHERE P.pubStatus = ? AND F.nodeId <> 1 AND (");
      selectStatement.append("( ? > P.pubBeginDate AND ? < P.pubEndDate ) OR ");
      selectStatement.append(
          "( ? = P.pubBeginDate AND ? < P.pubEndDate AND ? > P.pubBeginHour ) OR ");
      selectStatement
          .append("( ? > P.pubBeginDate AND ? = P.pubEndDate AND ? < P.pubEndHour ) OR ");
      selectStatement
          .append(
          "( ? = P.pubBeginDate AND ? = P.pubEndDate AND ? > P.pubBeginHour AND ? < P.pubEndHour )");
      selectStatement.append(" ) ");
      selectStatement.append(" AND F.pubId = P.pubId AND P.pubupdatedate > ? ");
      selectStatement.append(" AND P.instanceId IN (");
      String componentId = null;
      for (int c = 0; c < componentIds.size(); c++) {
        componentId = componentIds.get(c);
        if (c != 0) {
          selectStatement.append(", ");
        }
        selectStatement.append("'").append(componentId).append("'");
      }
      selectStatement.append(")");
      selectStatement.append(" ORDER BY P.pubUpdateDate DESC, P.pubId DESC");

      PreparedStatement prepStmt = null;
      ResultSet rs = null;
      try {
        java.util.Date now = new java.util.Date();
        String dateNow = DateUtil.formatDate(now);

        String hourNow = DateUtil.formatTime(now);

        prepStmt = con.prepareStatement(selectStatement.toString());
        prepStmt.setFetchSize(maxSize);
        prepStmt.setString(1, status);
        prepStmt.setString(2, dateNow);
        prepStmt.setString(3, dateNow);
        prepStmt.setString(4, dateNow);
        prepStmt.setString(5, dateNow);
        prepStmt.setString(6, hourNow);
        prepStmt.setString(7, dateNow);
        prepStmt.setString(8, dateNow);
        prepStmt.setString(9, hourNow);
        prepStmt.setString(10, dateNow);
        prepStmt.setString(11, dateNow);
        prepStmt.setString(12, hourNow);
        prepStmt.setString(13, hourNow);
        prepStmt.setString(14, DateUtil.date2SQLDate(since));
        rs = prepStmt.executeQuery();
        PublicationPK pubPK = null;
        int i = 0;
        while (rs.next() && (maxSize <= 0 || i <= maxSize)) {
          pubPK = new PublicationPK(rs.getString("pubId"), rs.getString("instanceId"));
          list.add(pubPK);
          i++;
        }
      } finally {
        DBUtil.close(rs, prepStmt);
      }
    }
    return list;
  }

  /**
   * Method declaration
   * @param con
   * @param pubPK
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<PublicationDetail> selectAllPublications(Connection con,
      PublicationPK pubPK) throws SQLException {
    return selectAllPublications(con, pubPK, null);
  }

  public static Collection<PublicationDetail> selectAllPublications(Connection con,
      PublicationPK pubPK, String sorting) throws SQLException {
    StringBuilder selectStatement = new StringBuilder(128);
    selectStatement.append("select * from ").append(pubPK.getTableName()).append(
        " P where P.instanceId='").append(pubPK.getComponentName()).append("'");

    if (sorting != null) {
      selectStatement.append(" order by ").append(sorting);
    }

    Statement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(selectStatement.toString());

      ArrayList<PublicationDetail> list = new ArrayList<PublicationDetail>();
      PublicationDetail pub = null;
      while (rs.next()) {
        pub = resultSet2PublicationDetail(rs, pubPK);
        list.add(pub);
      }
      return list;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param pubPK
   * @param status
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<PublicationDetail> selectByBeginDateDescAndStatus(Connection con,
      PublicationPK pubPK, String status) throws SQLException {
    StringBuilder selectStatement = new StringBuilder(128);
    selectStatement.append("select * from SB_Publication_Publi where pubStatus like '");
    selectStatement.append(status).append("' ");
    selectStatement.append(" and instanceId = ? ");
    selectStatement.append(" and (");
    selectStatement.append("( ? > pubBeginDate AND ? < pubEndDate ) OR ");
    selectStatement.append("( ? = pubBeginDate AND ? < pubEndDate AND ? > pubBeginHour ) OR ");
    selectStatement.append("( ? > pubBeginDate AND ? = pubEndDate AND ? < pubEndHour ) OR ");
    selectStatement.append(
        "( ? = pubBeginDate AND ? = pubEndDate AND ? > pubBeginHour AND ? < pubEndHour )");
    selectStatement.append(" ) ");
    selectStatement.append(" order by pubCreationDate DESC, pubBeginDate DESC, pubId DESC");

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      java.util.Date now = new java.util.Date();
      String dateNow = DateUtil.formatDate(now);
      String hourNow = DateUtil.formatTime(now);

      prepStmt = con.prepareStatement(selectStatement.toString());

      prepStmt.setString(1, pubPK.getComponentName());

      prepStmt.setString(2, dateNow);
      prepStmt.setString(3, dateNow);
      prepStmt.setString(4, dateNow);
      prepStmt.setString(5, dateNow);
      prepStmt.setString(6, hourNow);
      prepStmt.setString(7, dateNow);
      prepStmt.setString(8, dateNow);
      prepStmt.setString(9, hourNow);
      prepStmt.setString(10, dateNow);
      prepStmt.setString(11, dateNow);
      prepStmt.setString(12, hourNow);
      prepStmt.setString(13, hourNow);

      rs = prepStmt.executeQuery();

      ArrayList<PublicationDetail> list = new ArrayList<PublicationDetail>();
      PublicationDetail pub = null;
      while (rs.next()) {
        pub = resultSet2PublicationDetail(rs, pubPK);
        list.add(pub);
      }

      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static Collection<PublicationDetail> selectByBeginDateDescAndStatusAndNotLinkedToFatherId(
      Connection con, PublicationPK pubPK, String status, String fatherId,
      int fetchSize) throws SQLException {

    Collection<PublicationDetail> thisLastPublis = getLastPublis(pubPK.getComponentName());
    if (thisLastPublis != null) {
      return thisLastPublis;
    } else {
      String selectStatement = QueryStringFactory.
          getSelectByBeginDateDescAndStatusAndNotLinkedToFatherId(pubPK.getTableName());

      PreparedStatement prepStmt = null;
      ResultSet rs = null;

      try {
        java.util.Date now = new java.util.Date();
        String dateNow = DateUtil.formatDate(now);
        String hourNow = DateUtil.formatTime(now);
        prepStmt = con.prepareStatement(selectStatement);

        prepStmt.setString(1, pubPK.getComponentName());
        prepStmt.setInt(2, new Integer(fatherId).intValue());
        prepStmt.setString(3, status);

        prepStmt.setString(4, dateNow);
        prepStmt.setString(5, dateNow);
        prepStmt.setString(6, dateNow);
        prepStmt.setString(7, dateNow);
        prepStmt.setString(8, hourNow);
        prepStmt.setString(9, dateNow);
        prepStmt.setString(10, dateNow);
        prepStmt.setString(11, hourNow);
        prepStmt.setString(12, dateNow);
        prepStmt.setString(13, dateNow);
        prepStmt.setString(14, hourNow);
        prepStmt.setString(15, hourNow);

        rs = prepStmt.executeQuery();

        int nbFetch = 0;
        List<PublicationDetail> list = new ArrayList<PublicationDetail>();
        PublicationDetail pub = null;
        while (rs.next() && nbFetch < fetchSize) {
          pub = resultSet2PublicationDetail(rs, pubPK);
          list.add(pub);
          nbFetch++;
        }
        cacheLastPublis(pubPK.getComponentName(), list);
        return list;
      } finally {
        DBUtil.close(rs, prepStmt);
      }
    }
  }

  /**
   * Method declaration
   * @param con
   * @param pubPK
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<PublicationDetail> selectByBeginDateDesc(Connection con,
      PublicationPK pubPK) throws SQLException {
    StringBuilder selectStatement = new StringBuilder(128);
    selectStatement.append("select * from SB_Publication_Publi where instanceId = ? ");
    selectStatement.append(" and (");
    selectStatement.append("( ? > pubBeginDate AND ? < pubEndDate ) OR ");
    selectStatement.append("( ? = pubBeginDate AND ? < pubEndDate AND ? > pubBeginHour ) OR ");
    selectStatement.append("( ? > pubBeginDate AND ? = pubEndDate AND ? < pubEndHour ) OR ");
    selectStatement.append(
        "( ? = pubBeginDate AND ? = pubEndDate AND ? > pubBeginHour AND ? < pubEndHour )");
    selectStatement.append(" ) ");
    selectStatement.append(" order by pubCreationDate DESC, pubBeginDate DESC");

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      java.util.Date now = new java.util.Date();
      String dateNow = DateUtil.formatDate(now);
      String hourNow = DateUtil.formatTime(now);

      prepStmt = con.prepareStatement(selectStatement.toString());

      prepStmt.setString(1, pubPK.getComponentName());
      prepStmt.setString(2, dateNow);
      prepStmt.setString(3, dateNow);
      prepStmt.setString(4, dateNow);
      prepStmt.setString(5, dateNow);
      prepStmt.setString(6, hourNow);
      prepStmt.setString(7, dateNow);
      prepStmt.setString(8, dateNow);
      prepStmt.setString(9, hourNow);
      prepStmt.setString(10, dateNow);
      prepStmt.setString(11, dateNow);
      prepStmt.setString(12, hourNow);
      prepStmt.setString(13, hourNow);

      rs = prepStmt.executeQuery();
      List<PublicationDetail> list = new ArrayList<PublicationDetail>();
      PublicationDetail pub = null;
      while (rs.next()) {
        pub = resultSet2PublicationDetail(rs, pubPK);
        list.add(pub);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param pubPK
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<PublicationDetail> getOrphanPublications(Connection con,
      PublicationPK pubPK) throws SQLException {
    StringBuilder selectStatement = new StringBuilder(128);
    selectStatement.append("select * from ").append(pubPK.getTableName());
    selectStatement.append(" where pubId NOT IN (Select pubId from ").append(
        pubPK.getTableName()).append("Father) ");
    selectStatement.append(" and instanceId='").append(pubPK.getComponentName()).append("'");

    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(selectStatement.toString());
      List<PublicationDetail> list = new ArrayList<PublicationDetail>();
      PublicationDetail pub = null;
      while (rs.next()) {
        pub = resultSet2PublicationDetail(rs, pubPK);
        list.add(pub);
      }
      return list;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param pubPK
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<PublicationDetail> getNotOrphanPublications(Connection con,
      PublicationPK pubPK) throws SQLException {
    StringBuilder selectStatement = new StringBuilder(128);
    selectStatement.append("select * from ").append(pubPK.getTableName());
    selectStatement.append(" where pubId IN (Select pubId from ").append(
        pubPK.getTableName()).append("Father) ");
    selectStatement.append(" and instanceId='").append(pubPK.getComponentName()).append("' ");
    selectStatement.append(" and (");
    selectStatement.append("( ? > pubBeginDate AND ? < pubEndDate ) OR ");
    selectStatement.append("( ? = pubBeginDate AND ? < pubEndDate AND ? > pubBeginHour ) OR ");
    selectStatement.append("( ? > pubBeginDate AND ? = pubEndDate AND ? < pubEndHour ) OR ");
    selectStatement.append(
        "( ? = pubBeginDate AND ? = pubEndDate AND ? > pubBeginHour AND ? < pubEndHour )");
    selectStatement.append(" ) ");
    selectStatement.append(" order by pubUpdateDate DESC ");

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      java.util.Date now = new java.util.Date();
      String dateNow = DateUtil.formatDate(now);
      String hourNow = DateUtil.formatTime(now);

      prepStmt = con.prepareStatement(selectStatement.toString());

      prepStmt.setString(1, dateNow);
      prepStmt.setString(2, dateNow);
      prepStmt.setString(3, dateNow);
      prepStmt.setString(4, dateNow);
      prepStmt.setString(5, hourNow);
      prepStmt.setString(6, dateNow);
      prepStmt.setString(7, dateNow);
      prepStmt.setString(8, hourNow);
      prepStmt.setString(9, dateNow);
      prepStmt.setString(10, dateNow);
      prepStmt.setString(11, hourNow);
      prepStmt.setString(12, hourNow);

      rs = prepStmt.executeQuery();
      List<PublicationDetail> list = new ArrayList<PublicationDetail>();
      PublicationDetail pub = null;
      while (rs.next()) {
        pub = resultSet2PublicationDetail(rs, pubPK);
        list.add(pub);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param pubPK
   * @param creatorId
   * @throws SQLException
   * @see
   */
  public static void deleteOrphanPublicationsByCreatorId(Connection con,
      PublicationPK pubPK, String creatorId) throws SQLException {
    StringBuilder deleteStatement = new StringBuilder(128);
    deleteStatement.append("delete from ").append(pubPK.getTableName());
    deleteStatement.append(" where pubCreatorId=").append(creatorId);
    deleteStatement.append(" and pubId NOT IN (Select pubId from ").append(
        pubPK.getTableName()).append("Father ) ");
    deleteStatement.append(" and instanceId='").append(pubPK.getComponentName()).append("'");

    Statement stmt = null;

    try {
      stmt = con.createStatement();
      stmt.executeUpdate(deleteStatement.toString());
    } finally {
      DBUtil.close(stmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param pubPK
   * @param publisherId
   * @param nodeId
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<PublicationDetail> getUnavailablePublicationsByPublisherId(
      Connection con, PublicationPK pubPK, String publisherId, String nodeId)
      throws SQLException {
    StringBuilder selectStatement = new StringBuilder(128);
    selectStatement.append("select * from ").append(pubPK.getTableName()).append(" P, ").append(
        pubPK.getTableName()).append("Father F ");
    selectStatement.append(" where P.instanceId = ? ");
    selectStatement.append(" and F.pubId = P.pubId ");
    selectStatement.append(" AND (F.nodeId = ").append(nodeId);
    selectStatement.append(" OR ( ? < pubBeginDate ) ");
    selectStatement.append(" OR ( pubBeginDate = ? AND ? < pubBeginHour )");
    selectStatement.append(" OR ( ? > pubEndDate ) ");
    selectStatement.append(" OR ( pubEndDate = ? AND ? > pubEndHour))");
    if (publisherId != null) {
      selectStatement.append(" and P.pubCreatorId = ? ");
    }

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(selectStatement.toString());
      prepStmt.setString(1, pubPK.getComponentName());
      java.util.Date now = new java.util.Date();
      String formattedDate = DateUtil.formatDate(now);
      String formattedHour = DateUtil.formatTime(now);
      prepStmt.setString(2, formattedDate);
      prepStmt.setString(3, formattedDate);
      prepStmt.setString(4, formattedHour);
      prepStmt.setString(5, formattedDate);
      prepStmt.setString(6, formattedDate);
      prepStmt.setString(7, formattedHour);
      if (publisherId != null) {
        prepStmt.setString(8, publisherId);
      }

      rs = prepStmt.executeQuery();
      List<PublicationDetail> list = new ArrayList<PublicationDetail>();
      PublicationDetail pub = null;
      while (rs.next()) {
        pub = resultSet2PublicationDetail(rs, pubPK);
        list.add(pub);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /*
   * @deprecated
   */
  /**
   * Method declaration
   * @param con
   * @param query
   * @param pubPK
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<PublicationDetail> searchByKeywords(Connection con, String query,
      PublicationPK pubPK) {
    List<PublicationDetail> result = new ArrayList<PublicationDetail>();
    return result;
  }

  /**
   * Method declaration
   * @param con
   * @param pk
   * @return
   * @throws SQLException
   * @see
   */
  public static PublicationDetail loadRow(Connection con, PublicationPK pk)
      throws SQLException {
    String selectStatement = QueryStringFactory.getLoadRow(pk.getTableName());


    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      PublicationDetail pub = null;
      stmt = con.prepareStatement(selectStatement);
      stmt.setInt(1, Integer.parseInt(pk.getId()));
      rs = stmt.executeQuery();
      if (rs.next()) {
        pub = resultSet2PublicationDetail(rs, pk);
        return pub;
      } else {
        throw new PublicationRuntimeException("PublicationDAO.loadRow()",
            SilverpeasRuntimeException.ERROR,
            "root.EX_CANT_LOAD_ENTITY_ATTRIBUTES", "PublicationId = "
            + pk.getId() + " not found in database !");
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  public static void changeInstanceId(Connection con, PublicationPK pubPK,
      String newInstanceId) throws SQLException {

    int rowCount = 0;

    StringBuilder updateQuery = new StringBuilder(128);
    updateQuery.append("update ").append(PublicationDAO.publicationTableName);
    updateQuery.append(" set instanceId = ? ");
    updateQuery.append(" where pubId = ? ");
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(updateQuery.toString());
      prepStmt.setString(1, newInstanceId);
      prepStmt.setInt(2, Integer.parseInt(pubPK.getId()));

      rowCount = prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }

    invalidateLastPublis(pubPK.getInstanceId());
    invalidateLastPublis(newInstanceId);

    if (rowCount == 0) {
      throw new PublicationRuntimeException(
          "PublicationDAO.changeInstanceId()",
          SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_STORE_ENTITY_ATTRIBUTES", "PublicationId = "
          + pubPK.getId());
    }
  }

  public static void storeRow(Connection con, PublicationDetail detail)
      throws SQLException {
    int rowCount = 0;
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(UPDATE_PUBLICATION);
      prepStmt.setString(1, detail.getInfoId());

      prepStmt.setString(2, detail.getName());
      prepStmt.setString(3, detail.getDescription());
      prepStmt.setString(4, DateUtil.formatDate(detail.getCreationDate()));
      if (detail.getBeginDate() == null) {
        prepStmt.setString(5, nullBeginDate);
      } else {
        prepStmt.setString(5, DateUtil.formatDate(detail.getBeginDate()));
      }
      if (detail.getEndDate() == null) {
        prepStmt.setString(6, nullEndDate);
      } else {
        prepStmt.setString(6, DateUtil.formatDate(detail.getEndDate()));
      }
      prepStmt.setString(7, detail.getCreatorId());
      prepStmt.setInt(8, detail.getImportance());
      prepStmt.setString(9, detail.getVersion());
      prepStmt.setString(10, detail.getKeywords());
      prepStmt.setString(11, detail.getContent());
      prepStmt.setString(12, detail.getStatus());
      if (detail.getUpdateDate() == null) {
        prepStmt.setString(13, DateUtil.formatDate(detail.getCreationDate()));
      } else {
        prepStmt.setString(13, DateUtil.formatDate(detail.getUpdateDate()));
      }
      if (detail.getUpdaterId() == null) {
        prepStmt.setString(14, detail.getCreatorId());
      } else {
        prepStmt.setString(14, detail.getUpdaterId());
      }
      prepStmt.setString(15, detail.getPK().getComponentName());

      prepStmt.setString(16, detail.getValidatorId());
      if (detail.getValidateDate() != null) {
        prepStmt.setString(17, DateUtil.formatDate(detail.getValidateDate()));
      } else {
        prepStmt.setString(17, null);
      }

      if (isUndefined(detail.getBeginHour())) {
        prepStmt.setString(18, nullBeginHour);
      } else {
        prepStmt.setString(18, detail.getBeginHour());
      }

      if (isUndefined(detail.getEndHour())) {
        prepStmt.setString(19, nullEndHour);
      } else {
        prepStmt.setString(19, detail.getEndHour());
      }

      if (isUndefined(detail.getAuthor())) {
        prepStmt.setString(20, null);
      } else {
        prepStmt.setString(20, detail.getAuthor());
      }

      prepStmt.setString(21, detail.getTargetValidatorId());

      if (isUndefined(detail.getCloneId())) {
        prepStmt.setInt(22, -1);
      } else {
        prepStmt.setInt(22, Integer.parseInt(detail.getCloneId()));
      }

      prepStmt.setString(23, detail.getCloneStatus());

      prepStmt.setString(24, detail.getLanguage());
      prepStmt.setString(25, DateUtil.formatDate(detail.getDraftOutDate()));

      prepStmt.setInt(26, Integer.parseInt(detail.getPK().getId()));

      rowCount = prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
    invalidateLastPublis(detail.getPK().getComponentName());

    if (rowCount == 0) {
      throw new PublicationRuntimeException("PublicationDAO.storeRow()",
          SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_STORE_ENTITY_ATTRIBUTES", "PublicationId = "
          + detail.getPK().getId());
    }
  }

  // Added by ney - 23/08/2001
  public static PublicationDetail selectByName(Connection con,
      PublicationPK pubPK, String name) throws SQLException {
    ResultSet rs = null;
    PublicationDetail pub = null;
    String selectStatement = QueryStringFactory.getSelectByName();
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, name);
      prepStmt.setString(2, pubPK.getComponentName());

      rs = prepStmt.executeQuery();
      if (rs.next()) {
        pub = resultSet2PublicationDetail(rs, pubPK);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return pub;
  }

  public static PublicationDetail selectByNameAndNodeId(Connection con,
      PublicationPK pubPK, String name, int nodeId) throws SQLException {
    ResultSet rs = null;
    PublicationDetail pub = null;
    String selectStatement = QueryStringFactory.getSelectByNameAndNodeId();
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, name);
      prepStmt.setString(2, pubPK.getComponentName());
      prepStmt.setInt(3, nodeId);

      rs = prepStmt.executeQuery();
      if (rs.next()) {
        pub = resultSet2PublicationDetail(rs, pubPK);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return pub;
  }

  public static Collection<PublicationDetail> selectBetweenDate(Connection con, String beginDate,
      String endDate, String instanceId) throws SQLException {
    StringBuilder selectStatement = new StringBuilder(128);
    selectStatement.append("select * from ").append(publicationTableName);
    selectStatement.append(" where instanceId = ? ");
    selectStatement.append(" and (");
    selectStatement.append("( ? <= pubCreationDate AND ? >= pubCreationDate ) ");
    selectStatement.append(" ) ");
    selectStatement.append(" order by pubCreationDate DESC, pubId DESC");

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      prepStmt = con.prepareStatement(selectStatement.toString());

      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, beginDate);
      prepStmt.setString(3, endDate);

      rs = prepStmt.executeQuery();
      List<PublicationDetail> list = new ArrayList<PublicationDetail>();
      PublicationDetail pub = null;
      while (rs.next()) {
        pub = resultSet2PublicationDetail(rs, null);
        list.add(pub);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * get my SocialInformationPublication accordint to the type of data base
   * used(PostgreSQL,Oracle,MMS) .
   * @param con
   * @param userId
   * @param begin
   * @param end
   * @return List<SocialInformationPublication>
   * @throws SQLException
   */
  public static List<SocialInformation> getAllPublicationsIDbyUserid(Connection con,
      String userId, Date begin, Date end) throws SQLException {

    List<SocialInformation> listPublications = new ArrayList<SocialInformation>();

    String query =
        "(SELECT pubcreationdate AS dateinformation, pubid, 'false' as type  FROM sb_publication_publi WHERE pubcreatorid = ? and pubstatus = 'Valid' and pubCreationDate >= ? and pubCreationDate <= ? )"
            + "UNION (SELECT pubupdatedate AS dateinformation, pubid, 'true' as type FROM sb_publication_publi WHERE pubupdaterid = ? and pubstatus = 'Valid' and pubupdatedate >= ? and pubupdatedate <= ? )"
            + "ORDER BY dateinformation DESC, pubid DESC";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, userId);
      prepStmt.setString(2, DateUtil.date2SQLDate(begin));
      prepStmt.setString(3, DateUtil.date2SQLDate(end));
      prepStmt.setString(4, userId);
      prepStmt.setString(5, DateUtil.date2SQLDate(begin));
      prepStmt.setString(6, DateUtil.date2SQLDate(end));
      rs = prepStmt.executeQuery();
      while (rs.next()) {

        PublicationDetail pd = loadRow(con, new PublicationPK(Integer.toString(rs.getInt(2))));
        PublicationWithStatus withStatus = new PublicationWithStatus(pd, rs.getBoolean(3));

        listPublications.add(new SocialInformationPublication(withStatus));
      }

    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return listPublications;
  }

  /**
   * get list of socialInformation of my contacts according to the type of data base
   * used(PostgreSQL,Oracle,MMS) .
   * @param con
   * @param myContactsIds
   * @param options list of Available Components name
   * @param begin
   * @param end
   * @return List <SocialInformation>
   */
  public static List<SocialInformationPublication> getSocialInformationsListOfMyContacts(
      Connection con, List<String> myContactsIds, List<String> options, Date begin, Date end)
      throws SQLException {
    List<SocialInformationPublication> listPublications =
        new ArrayList<SocialInformationPublication>();

    String query =
        "(SELECT pubcreationdate AS dateinformation, pubid, 'false' as type FROM sb_publication_publi WHERE pubcreatorid in(" +
            toSqlString(
            myContactsIds) +
            ") and instanceid in(" +
            toSqlString(options) +
            ") and pubstatus = 'Valid' and pubCreationDate >= ? and pubCreationDate <= ? )"
            +
            "UNION (SELECT  pubupdatedate AS dateinformation, pubid, 'true' as type FROM sb_publication_publi WHERE pubupdaterid in(" +
            toSqlString(
            myContactsIds) +
            ")  and instanceid in(" +
            toSqlString(options) +
            ")and pubstatus = 'Valid' and pubupdatedate >= ? and pubupdatedate <= ? )"
            + "ORDER BY dateinformation DESC, pubid DESC";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, DateUtil.date2SQLDate(begin));
      prepStmt.setString(2, DateUtil.date2SQLDate(end));
      prepStmt.setString(3, DateUtil.date2SQLDate(begin));
      prepStmt.setString(4, DateUtil.date2SQLDate(end));
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        PublicationDetail pd = loadRow(con, new PublicationPK(Integer.toString(rs.getInt(2))));
        PublicationWithStatus withStatus = new PublicationWithStatus(pd, rs.getBoolean(3));
        listPublications.add(new SocialInformationPublication(withStatus));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return listPublications;
  }

  /**
   * tronsform the list of string to String for using in query sql
   * @param list
   * @return String
   */
  private static String toSqlString(List<String> list) {
    StringBuilder result = new StringBuilder(100);
    if (list == null || list.isEmpty()) {
      return "''";
    }
    int i = 0;
    for (String var : list) {
      if (i != 0) {
        result.append(",");
      }
      result.append("'").append(var).append("'");
      i++;
    }
    return result.toString();
  }

  public static Collection<PublicationDetail> getPublicationsToDraftOut(Connection con,
      boolean useClone) throws SQLException {
    StringBuilder sb = new StringBuilder();
    sb.append("select * from sb_publication_publi ");
    sb.append("where pubdraftoutdate <= ? ");
    if (useClone) {
      sb.append("and pubcloneid <> -1 ");
      sb.append("and pubclonestatus is null ");
      sb.append("and pubstatus = 'Draft' ");
    }

    List<PublicationDetail> publications = new ArrayList<PublicationDetail>();
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(sb.toString());
      prepStmt.setString(1, DateUtil.today2SQLDate());
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        publications.add(resultSet2PublicationDetail(rs, null));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return publications;
  }

  public static Collection<PublicationDetail> getDraftsByUser(Connection con, String userId)
      throws SQLException {
    StringBuilder sb = new StringBuilder();
    sb.append("select * from sb_publication_publi ");
    sb.append("where pubUpdaterId = ? ");
    sb.append("and ((pubStatus = ? ");
    sb.append("and pubcloneid = -1 ");
    sb.append("and pubclonestatus is null) ");
    sb.append("or ");
    sb.append("(pubcloneid <> -1 ");
    sb.append("and pubclonestatus = ? )) ");
    sb.append("order by pubUpdateDate desc");

    List<PublicationDetail> publications = new ArrayList<PublicationDetail>();
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(sb.toString());
      prepStmt.setString(1, userId);
      prepStmt.setString(2, PublicationDetail.DRAFT);
      prepStmt.setString(3, PublicationDetail.DRAFT);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        publications.add(resultSet2PublicationDetail(rs, null));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return publications;
  }
}
