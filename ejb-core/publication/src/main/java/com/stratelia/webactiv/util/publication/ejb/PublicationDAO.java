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
package com.stratelia.webactiv.util.publication.ejb;

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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.publication.socialNetwork.SocialInformationPublication;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.publication.model.PublicationRuntimeException;
import com.stratelia.webactiv.util.publication.model.PublicationWithStatus;

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
  private static Map<String, Collection<PublicationDetail>> lastPublis = new HashMap<String, Collection<PublicationDetail>>();
  private static final String publicationTableName = "SB_Publication_Publi";
  private static final String publicationFatherTableName = "SB_Publication_PubliFather";
  private static final String nodeTableName = "SB_Node_Node";
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
        SilverTrace.info("publication",
                "PublicationDAO.getNbPubInFatherPKs()",
                "root.MSG_GEN_PARAM_VALUE", "dateNow = " + dateNow);
        String hourNow = null;
        hourNow = DateUtil.formatTime(now);
        SilverTrace.info("publication",
                "PublicationDAO.getNbPubInFatherPKs()",
                "root.MSG_GEN_PARAM_VALUE", "hourNow = " + hourNow);
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
      selectStatement.append("( ? > P.pubBeginDate AND ? = P.pubEndDate AND ? < P.pubEndHour ) OR ");
      selectStatement.append(
              "( ? = P.pubBeginDate AND ? = P.pubEndDate AND ? > P.pubBeginHour AND ? < P.pubEndHour )");
      selectStatement.append(" ) ");
      selectStatement.append(" and (N.nodePath like '").append(fatherPath).append("/").append(
              fatherPK.getId()).append("%' or N.nodeId = ").append(fatherPK.getId()).append(")");

      PreparedStatement prepStmt = null;
      ResultSet rs = null;
      try {
        java.util.Date now = new java.util.Date();
        String dateNow = DateUtil.formatDate(now);
        SilverTrace.info("publication",
                "PublicationDAO.getNbPubByFatherPath()",
                "root.MSG_GEN_PARAM_VALUE", "dateNow = " + dateNow);
        String hourNow = DateUtil.formatTime(now);
        SilverTrace.info("publication",
                "PublicationDAO.getNbPubByFatherPath()",
                "root.MSG_GEN_PARAM_VALUE", "hourNow = " + hourNow);
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

  public static Hashtable<String, Integer> getDistribution(Connection con, String instanceId,
          String statusSubQuery, boolean checkVisibility) throws SQLException {
    Hashtable<String, Integer> result = new Hashtable<String, Integer>();

    StringBuilder selectStatement = new StringBuilder(128);

    selectStatement.append("SELECT N.nodeId, N.nodePath, COUNT(P.pubName) ");
    selectStatement.append("FROM ").append(publicationFatherTableName).append(
            " F, ").append(publicationTableName).append(" P, ").append(
            nodeTableName).append(" N ");
    selectStatement.append("Where F.pubId = P.pubId AND F.nodeId = N.nodeId ");
    selectStatement.append("AND N.instanceId = ? ");
    selectStatement.append("AND F.instanceId = ? ");
    if (statusSubQuery != null) {
      selectStatement.append(statusSubQuery);
    }
    if (checkVisibility) {
      selectStatement.append(" and (");
      selectStatement.append("( ? > P.pubBeginDate AND ? < P.pubEndDate ) OR ");
      selectStatement.append(
              "( ? = P.pubBeginDate AND ? < P.pubEndDate AND ? > P.pubBeginHour ) OR ");
      selectStatement.append("( ? > P.pubBeginDate AND ? = P.pubEndDate AND ? < P.pubEndHour ) OR ");
      selectStatement.append(
              "( ? = P.pubBeginDate AND ? = P.pubEndDate AND ? > P.pubBeginHour AND ? < P.pubEndHour )");
      selectStatement.append(" ) ");
    }
    selectStatement.append(" GROUP BY N.nodeId, N.nodePath");

    SilverTrace.info("publication", "PublicationDAO.getDistribution()",
            "root.MSG_GEN_PARAM_VALUE", "sqlStatement = "
            + selectStatement.toString());

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      java.util.Date now = new java.util.Date();
      String dateNow = DateUtil.formatDate(now);
      SilverTrace.info("publication", "PublicationDAO.getDistribution()",
              "root.MSG_GEN_PARAM_VALUE", "dateNow = " + dateNow);

      String hourNow = DateUtil.formatTime(now);
      SilverTrace.info("publication", "PublicationDAO.getDistribution()",
              "root.MSG_GEN_PARAM_VALUE", "hourNow = " + hourNow);

      prepStmt = con.prepareStatement(selectStatement.toString());

      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, instanceId);

      if (checkVisibility) {
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
      }

      rs = prepStmt.executeQuery();
      while (rs.next()) {
        int nodeId = rs.getInt(1);
        String nodePath = rs.getString(2);
        int nbPublis = rs.getInt(3);

        // This is a fix ! Some path may be not correct...
        if (!nodePath.endsWith("/")) {
          nodePath += "/";
        }

        result.put(nodePath + nodeId + "/", new Integer(nbPublis));
      }
      return result;
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
    insertStatement.append(" pubAuthor, pubTargetValidatorId, pubCloneId, pubCloneStatus, lang, pubDraftOutDate) ");
    insertStatement.append(
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
      SilverTrace.info("publication", "PublicationDAO.insertRow()",
              "root.MSG_GEN_PARAM_VALUE", "InfoId = " + infoId);
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
      if (detail.getCreationDate() == null) {
        prepStmt.setString(14, DateUtil.today2SQLDate());
      } else {
        prepStmt.setString(14, DateUtil.formatDate(detail.getCreationDate()));
      }
      prepStmt.setString(15, detail.getPK().getComponentName());
      prepStmt.setString(16, detail.getCreatorId());
      if ("Valid".equals(detail.getStatus())) {
        prepStmt.setString(18, detail.getCreatorId());
        prepStmt.setString(17, DateUtil.formatDate(detail.getCreationDate()));
      } else {
        prepStmt.setString(17, null);
        prepStmt.setString(18, null);
      }
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

      SilverTrace.info("publication", "PublicationDAO.insertRow()",
              "root.MSG_GEN_PARAM_VALUE", "pubDetail = " + detail.toString());

      prepStmt.executeUpdate();
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

  public static PublicationPK selectByPrimaryKey(Connection con,
          PublicationPK primaryKey) throws SQLException {

    try {
      PublicationDetail detail = loadRow(con, primaryKey);
      PublicationPK primary = new PublicationPK(primaryKey.getId(), detail.getPK().
              getInstanceId());

      primary.pubDetail = detail;
      return primary;

    } catch (PublicationRuntimeException e) {
      /*
       * NodeRuntimeException thrown by loadRow() should be replaced by
       * returning null (not found)
       */
      return null;
    }
  }

  public static PublicationPK selectByPublicationName(Connection con,
          PublicationPK primaryKey, String name) throws SQLException {

    PublicationPK primary = null;
    PublicationDetail detail = selectByName(con, primaryKey, name);

    if (detail != null) {
      primary = new PublicationPK(detail.getPK().getId(), primaryKey);
      primary.pubDetail = detail;
    }

    return primary;
  }

  public static PublicationPK selectByPublicationNameAndNodeId(Connection con,
          PublicationPK primaryKey, String name, int nodeId) throws SQLException {

    PublicationPK primary = null;
    PublicationDetail detail = selectByNameAndNodeId(con, primaryKey, name,
            nodeId);

    if (detail != null) {
      primary = new PublicationPK(detail.getPK().getId(), primaryKey);
      primary.pubDetail = detail;
    }

    return primary;
  }

  private static PublicationDetail resultSet2PublicationDetail(ResultSet rs,
          PublicationPK pubPK) throws SQLException {
    // SilverTrace.debug("publication",
    // "PublicationDAO.resultSet2PublicationDetail()",
    // "root.MSG_GEN_ENTER_METHOD", "pubPK = " + pubPK.toString());
    PublicationDetail pub = null;
    int id = rs.getInt("pubid");
    String componentId = rs.getString(15);
    PublicationPK pk = new PublicationPK(String.valueOf(id), componentId);
    String infoId = rs.getString("infoid");
    SilverTrace.info("publication",
            "PublicationDAO.resultSet2PublicationDetail()",
            "root.MSG_GEN_PARAM_VALUE", "InfoId = " + infoId);
    String name = rs.getString("pubname");
    SilverTrace.info("publication",
            "PublicationDAO.resultSet2PublicationDetail()",
            "root.MSG_GEN_PARAM_VALUE", "name = " + name);
    String description = rs.getString("pubDescription");
    if (description == null) {
      description = "";
    }
    java.util.Date creationDate;
    try {
      creationDate = DateUtil.parseDate(rs.getString("pubCreationDate"));
    } catch (java.text.ParseException e) {
      throw new SQLException(
              "PublicationDAO : resultSet2PublicationDetail() : internal error : "
              + "creationDate format unknown for publication.pk = " + pk
              + " : " + e.toString());
    }
    java.util.Date beginDate;
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
    java.util.Date endDate;

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
    
    java.util.Date updateDate;
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

    java.util.Date validateDate = null;
    String strValDate = rs.getString("pubValidateDate");
    try {
      validateDate = DateUtil.parseDate(strValDate);
    } catch (java.text.ParseException e) {
      throw new SQLException(
              "PublicationDAO : resultSet2PublicationDetail() : internal error : validateDate format unknown for publication.pk = "
              + pk + " : " + e.toString());
    }
    String validatorId = rs.getString("pubValidatorId");

    String beginHour = rs.getString("pubBeginHour");
    String endHour = rs.getString("pubEndHour");
    String author = rs.getString("pubAuthor");
    String targetValidatorId = rs.getString("pubTargetValidatorId");
    int tempPubId = rs.getInt("pubCloneId");
    String cloneStatus = rs.getString("pubCloneStatus");
    String lang = rs.getString("lang");
    
    java.util.Date draftOutDate = null;
    try {
      draftOutDate = DateUtil.parseDate(rs.getString("pubdraftoutdate"));
    } catch (java.text.ParseException e) {
      throw new SQLException(
          "PublicationDAO : resultSet2PublicationDetail() : internal error : draftOutDate format unknown for publication.pk = "
          + pk + " : " + e.toString());
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
            pubPK.getTableName(), filterOnVisibilityPeriod));
    if (userId != null) {
      selectStatement.append(" AND (P.pubUpdaterId = ? OR P.pubCreatorId = ? )");
    }
    selectStatement.append(" ORDER BY F.pubOrder ASC");
    if (sorting != null) {
      selectStatement.append(", ").append(sorting);
    }
    SilverTrace.info("publication", "PublicationDAO.selectByFatherPK()", "root.MSG_GEN_PARAM_VALUE", 
            "selectStatement = " + selectStatement);
            
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(selectStatement.toString());
      prepStmt.setString(1, pubPK.getComponentName());
      prepStmt.setInt(2, Integer.parseInt(fatherPK.getId()));
      int index = 3;
      if (filterOnVisibilityPeriod) {
        java.util.Date now = new java.util.Date();
        String dateNow = DateUtil.formatDate(now);
        SilverTrace.info("publication", "PublicationDAO.selectByFatherPK()",
                "root.MSG_GEN_PARAM_VALUE", "dateNow = " + dateNow);
        String hourNow = null;
        hourNow = DateUtil.formatTime(now);
        SilverTrace.info("publication", "PublicationDAO.selectByFatherPK()",
                "root.MSG_GEN_PARAM_VALUE", "hourNow = " + hourNow);
        prepStmt.setString(3 , dateNow);
        prepStmt.setString(4 , dateNow);
        prepStmt.setString(5 , dateNow);
        prepStmt.setString(6 , dateNow);
        prepStmt.setString(7 , hourNow);
        prepStmt.setString(8 , dateNow);
        prepStmt.setString(9 , dateNow);
        prepStmt.setString(10 , hourNow);
        prepStmt.setString(11 , dateNow);
        prepStmt.setString(12 , dateNow);
        prepStmt.setString(13 , hourNow);
        prepStmt.setString(14 , hourNow);
        index = 15;
      }
      if (userId != null) {
        prepStmt.setString(index, userId);
        prepStmt.setString(index + 1, userId);
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

    SilverTrace.info("publication", "PublicationDAO.selectNotInFatherPK()",
            "root.MSG_GEN_PARAM_VALUE", "selectStatement = " + selectStatement);

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      java.util.Date now = new java.util.Date();
      String dateNow = DateUtil.formatDate(now);
      SilverTrace.info("publication", "PublicationDAO.selectNotInFatherPK()",
              "root.MSG_GEN_PARAM_VALUE", "dateNow = " + dateNow);
      String hourNow = DateUtil.formatTime(now);
      SilverTrace.info("publication", "PublicationDAO.selectNotInFatherPK()",
              "root.MSG_GEN_PARAM_VALUE", "hourNow = " + hourNow);

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
    selectStatement.append(
            "select  distinct P.pubId, P.infoId, P.pubName, P.pubDescription, P.pubCreationDate, P.pubBeginDate, ");
    selectStatement.append(
            "         P.pubEndDate, P.pubCreatorId, P.pubImportance, P.pubVersion, P.pubKeywords, P.pubContent, ");
    selectStatement
        .append(
        "		 P.pubStatus, P.pubUpdateDate, P.instanceId, P.pubUpdaterId, P.pubValidateDate, P.pubValidatorId, P.pubBeginHour, P.pubEndHour, P.pubAuthor, P.pubTargetValidatorId, P.pubCloneId, P.pubCloneStatus, P.lang, P.pubdraftoutdate, F.puborder ");
    selectStatement.append("from ").append(pubPK.getTableName()).append(" P, ").append(pubPK.getTableName()).append("Father F ");

    selectStatement.append("where ").append(whereClause.toString());

    if (filterOnVisibilityPeriod) {
      selectStatement.append(" and (");
      selectStatement.append("( ? > P.pubBeginDate AND ? < P.pubEndDate ) OR ");
      selectStatement.append(
              "( ? = P.pubBeginDate AND ? < P.pubEndDate AND ? > P.pubBeginHour ) OR ");
      selectStatement.append("( ? > P.pubBeginDate AND ? = P.pubEndDate AND ? < P.pubEndHour ) OR ");
      selectStatement.append(
              "( ? = P.pubBeginDate AND ? = P.pubEndDate AND ? > P.pubBeginHour AND ? < P.pubEndHour )");
      selectStatement.append(" ) ");
    }
    selectStatement.append(" and F.pubId = P.pubId ");
    selectStatement.append(" and P.instanceId='").append(
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
      SilverTrace.info("publication", "PublicationDAO.selectByFatherIds()",
              "root.MSG_GEN_PARAM_VALUE", "dateNow = " + dateNow);
      String hourNow = DateUtil.formatTime(now);
      SilverTrace.info("publication", "PublicationDAO.selectByFatherIds()",
              "root.MSG_GEN_PARAM_VALUE", "hourNow = " + hourNow);

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
      selectStatement.append(
              "select  distinct P.pubId, P.infoId, P.pubName, P.pubDescription, P.pubCreationDate, P.pubBeginDate, ");
      selectStatement.append(
              "         P.pubEndDate, P.pubCreatorId, P.pubImportance, P.pubVersion, P.pubKeywords, P.pubContent, ");
      selectStatement.append(
              "		 P.pubStatus, P.pubUpdateDate, P.instanceId, P.pubUpdaterId, P.pubValidateDate, P.pubValidatorId, P.pubBeginHour, P.pubEndHour, P.pubAuthor, P.pubTargetValidatorId, P.pubCloneId, P.pubCloneStatus, P.lang, P.pubdraftoutdate ");
      selectStatement.append("from ").append(publicationTableName).append(
              " P, ").append(publicationTableName).append("Father F ");
      selectStatement.append("where P.pubStatus = '").append(status).append("'");
      selectStatement.append(" and F.nodeId <> 1 ");
      selectStatement.append(" and (");
      selectStatement.append("( ? > P.pubBeginDate AND ? < P.pubEndDate ) OR ");
      selectStatement.append(
              "( ? = P.pubBeginDate AND ? < P.pubEndDate AND ? > P.pubBeginHour ) OR ");
      selectStatement.append("( ? > P.pubBeginDate AND ? = P.pubEndDate AND ? < P.pubEndHour ) OR ");
      selectStatement.append(
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

      SilverTrace.info("publication", "PublicationDAO.selectByStatus()",
              "root.MSG_GEN_PARAM_VALUE", "selectStatement = "
              + selectStatement.toString());

      PreparedStatement prepStmt = null;
      ResultSet rs = null;

      try {
        java.util.Date now = new java.util.Date();
        String dateNow = DateUtil.formatDate(now);
        SilverTrace.info("publication", "PublicationDAO.selectByStatus()",
                "root.MSG_GEN_PARAM_VALUE", "dateNow = " + dateNow);
        String hourNow = DateUtil.formatTime(now);
        SilverTrace.info("publication", "PublicationDAO.selectByStatus()",
                "root.MSG_GEN_PARAM_VALUE", "hourNow = " + hourNow);

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
      selectStatement.append("( ? > P.pubBeginDate AND ? = P.pubEndDate AND ? < P.pubEndHour ) OR ");
      selectStatement.append(
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

      SilverTrace.info("publication", "PublicationDAO.selectByStatus()",
              "root.MSG_GEN_PARAM_VALUE", "selectStatement = "
              + selectStatement.toString());

      PreparedStatement prepStmt = null;
      ResultSet rs = null;
      try {
        java.util.Date now = new java.util.Date();
        String dateNow = DateUtil.formatDate(now);
        SilverTrace.info("publication", "PublicationDAO.selectByStatus()",
                "root.MSG_GEN_PARAM_VALUE", "dateNow = " + dateNow);
        String hourNow = DateUtil.formatTime(now);
        SilverTrace.info("publication", "PublicationDAO.selectByStatus()",
                "root.MSG_GEN_PARAM_VALUE", "hourNow = " + hourNow);
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
          List<String> componentIds, String status, java.util.Date since, int maxSize) throws SQLException {
    List<PublicationPK> list = new ArrayList<PublicationPK>();
    if (componentIds != null && componentIds.size() > 0) {
      StringBuilder selectStatement = new StringBuilder(128);
      selectStatement.append("SELECT  DISTINCT(P.pubId), P.instanceId, P.pubUpdateDate ");
      selectStatement.append("FROM SB_Publication_Publi P, SB_Publication_PubliFather F ");
      selectStatement.append("WHERE P.pubStatus = ? AND F.nodeId <> 1 AND (");
      selectStatement.append("( ? > P.pubBeginDate AND ? < P.pubEndDate ) OR ");
      selectStatement.append(
              "( ? = P.pubBeginDate AND ? < P.pubEndDate AND ? > P.pubBeginHour ) OR ");
      selectStatement.append("( ? > P.pubBeginDate AND ? = P.pubEndDate AND ? < P.pubEndHour ) OR ");
      selectStatement.append(
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

      SilverTrace.info("publication", "PublicationDAO.selectByStatus()",
              "root.MSG_GEN_PARAM_VALUE", "selectStatement = "
              + selectStatement.toString());

      PreparedStatement prepStmt = null;
      ResultSet rs = null;
      try {
        java.util.Date now = new java.util.Date();
        String dateNow = DateUtil.formatDate(now);
        SilverTrace.info("publication", "PublicationDAO.selectByStatus()",
                "root.MSG_GEN_PARAM_VALUE", "dateNow = " + dateNow);
        String hourNow = DateUtil.formatTime(now);
        SilverTrace.info("publication", "PublicationDAO.selectByStatus()",
                "root.MSG_GEN_PARAM_VALUE", "hourNow = " + hourNow);
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
        int i =0;
        while (rs.next() && (maxSize <= 0 || i <= maxSize )) {
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
      SilverTrace.info("publication",
              "PublicationDAO.selectByBeginDateDescAndStatus()",
              "root.MSG_GEN_PARAM_VALUE", "dateNow = " + dateNow);
      String hourNow = DateUtil.formatTime(now);
      SilverTrace.info("publication",
              "PublicationDAO.selectByBeginDateDescAndStatus()",
              "root.MSG_GEN_PARAM_VALUE", "hourNow = " + hourNow);

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
      String selectStatement = QueryStringFactory.getSelectByBeginDateDescAndStatusAndNotLinkedToFatherId(pubPK.getTableName());

      PreparedStatement prepStmt = null;
      ResultSet rs = null;

      try {
        java.util.Date now = new java.util.Date();
        String dateNow = DateUtil.formatDate(now);
        SilverTrace.info(
                "publication",
                "PublicationDAO.selectByBeginDateDescAndStatusAndNotLinkedToFatherId()",
                "root.MSG_GEN_PARAM_VALUE", "dateNow = " + dateNow);
        String hourNow = DateUtil.formatTime(now);
        SilverTrace.info(
                "publication",
                "PublicationDAO.selectByBeginDateDescAndStatusAndNotLinkedToFatherId()",
                "root.MSG_GEN_PARAM_VALUE", "hourNow = " + hourNow);
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
      SilverTrace.info("publication",
              "PublicationDAO.selectByBeginDateDesc()",
              "root.MSG_GEN_PARAM_VALUE", "dateNow = " + dateNow);
      String hourNow = DateUtil.formatTime(now);
      SilverTrace.info("publication",
              "PublicationDAO.selectByBeginDateDesc()",
              "root.MSG_GEN_PARAM_VALUE", "hourNow = " + hourNow);

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
      SilverTrace.info("publication",
              "PublicationDAO.selectByBeginDateDescAndStatus()",
              "root.MSG_GEN_PARAM_VALUE", "dateNow = " + dateNow);
      String hourNow = DateUtil.formatTime(now);
      SilverTrace.info("publication",
              "PublicationDAO.selectByBeginDateDescAndStatus()",
              "root.MSG_GEN_PARAM_VALUE", "hourNow = " + hourNow);

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
          PublicationPK pubPK) throws SQLException {
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
    SilverTrace.info("publication", "PublicationDAO.loadRow()",
            "root.MSG_GEN_PARAM_VALUE", "selectStatement = " + selectStatement);

    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      PublicationDetail pub = null;
      stmt = con.prepareStatement(selectStatement);
      stmt.setInt(1, new Integer(pk.getId()).intValue());
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

    SilverTrace.info("publication", "PublicationDAO.changeInstanceId()",
            "root.MSG_GEN_ENTER_METHOD", "pubPK = " + pubPK.toString()
            + ", newInstanceId = " + newInstanceId);
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
      SilverTrace.info("publication", "PublicationDAO.storeRow()",
              "root.MSG_GEN_PARAM_VALUE", "InfoId = " + detail.getInfoId());
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
    SilverTrace.info("publication", "PublicationDAO.selectByNameAndNodeId()",
            "root.MSG_GEN_ENTER_METHOD", "componentId = "
            + pubPK.getComponentName() + ", name = '" + name + "', nodeId = "
            + nodeId);
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
   * get my SocialInformationPublication  accordint to
   * the type of data base used(PostgreSQL,Oracle,MMS) .
   * @param con
   * @param userId
   * @param firstIndex
   * @param nbElement
   * @return List<SocialInformationPublication>
   * @throws SQLException
   */
  public static List<SocialInformationPublication> getAllPublicationsIDbyUserid(Connection con,
          String userId, int firstIndex, int nbElement) throws SQLException {
    String DatabaseProductName = con.getMetaData().getDatabaseProductName().toUpperCase();
    if (DatabaseProductName.contains("POSTGRESQL")) {
      return getAllPublicationsIDbyUserid_PostgreSQL(con, userId, firstIndex, nbElement);

    } else if (DatabaseProductName.contains("ORACLE")) {
      return getAllPublicationsIDbyUserid_Oracle(con, userId, firstIndex, nbElement);
    }
    //MSSQL
    return getAllPublicationsIDbyUserid_MMSQL(con, userId, firstIndex, nbElement);

  }

  /**
   * get my SocialInformationPublication  accordint to number of elements and the first index
   * when data base is PostgreSQL
   * @param con
   * @param userId
   * @param firstIndex
   * @param nbElement
   * @return List<SocialInformationPublication>
   * @throws SQLException
   */
  public static List<SocialInformationPublication> getAllPublicationsIDbyUserid_PostgreSQL(
          Connection con,
          String userId, int firstIndex, int nbElement) throws SQLException {
    List<SocialInformationPublication> listPublications = new ArrayList<SocialInformationPublication>();

    String query = "(SELECT pubcreationdate AS dateinformation, pubid, 'false' as type  FROM sb_publication_publi  WHERE pubcreatorid = ? and pubstatus = 'Valid')"
            + "UNION (SELECT  pubupdatedate AS dateinformation, pubid, 'true' as type FROM sb_publication_publi  WHERE  pubupdaterid = ? and pubstatus = 'Valid')"
            + "ORDER BY dateinformation DESC, type DESC limit ? offset ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, userId);
      prepStmt.setString(2, userId);
      prepStmt.setInt(3, nbElement);
      prepStmt.setInt(4, firstIndex);
      rs = prepStmt.executeQuery();
      while (rs.next()) {

        PublicationDetail pd = getPublication(con, rs.getInt(2));
        PublicationWithStatus withStatus = new PublicationWithStatus(pd, rs.getBoolean(3));

        listPublications.add(new SocialInformationPublication(withStatus));
      }

    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return listPublications;
  }

  /**
   * get my SocialInformationPublication  accordint to number of elements and the first index
   * when data base is Oracle
   * @param con
   * @param userId
   * @param firstIndex
   * @param nbElement
   * @return List<SocialInformationPublication>
   * @throws SQLException
   */
  public static List<SocialInformationPublication> getAllPublicationsIDbyUserid_Oracle(
          Connection con,
          String userId, int firstIndex, int nbElement) throws SQLException {
    List<SocialInformationPublication> listPublications = new ArrayList<SocialInformationPublication>();
    String queryOracle = " select * from (ROWNUM num , pubs_oracle.* from (";
    String query = "(SELECT pubcreationdate AS dateinformation, pubid, 'false' as type  FROM sb_publication_publi  WHERE pubcreatorid = ? and pubstatus = 'Valid')"
            + "UNION (SELECT  pubupdatedate AS dateinformation, pubid, 'true' as type FROM sb_publication_publi  WHERE  pubupdaterid = ? and pubstatus = 'Valid')"
            + "ORDER BY dateinformation DESC, type DESC  ";
    queryOracle = queryOracle + " " + query;
    queryOracle = queryOracle + " ) pubs_oracle) where num between ? and ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, userId);
      prepStmt.setString(2, userId);
      prepStmt.setInt(3, nbElement);
      prepStmt.setInt(4, firstIndex);
      rs = prepStmt.executeQuery();
      while (rs.next()) {

        PublicationDetail pd = getPublication(con, rs.getInt(2));
        PublicationWithStatus withStatus = new PublicationWithStatus(pd, rs.getBoolean(3));

        listPublications.add(new SocialInformationPublication(withStatus));
      }

    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return listPublications;
  }

  /**
   * get my SocialInformationPublication  accordint to number of elements and the first index
   * when data base is MMS
   * @param con
   * @param userId
   * @param firstIndex
   * @param nbElement
   * @return List<SocialInformationPublication>
   * @throws SQLException
   */
  public static List<SocialInformationPublication> getAllPublicationsIDbyUserid_MMSQL(Connection con,
          String userId, int firstIndex, int nbElement) throws SQLException {
    List<SocialInformationPublication> listPublications = new ArrayList<SocialInformationPublication>();

    String query = "(SELECT pubcreationdate AS dateinformation, pubid, 'false' as type  FROM sb_publication_publi  WHERE pubcreatorid = ? and pubstatus = 'Valid')"
            + "UNION (SELECT  pubupdatedate AS dateinformation, pubid, 'true' as type FROM sb_publication_publi  WHERE  pubupdaterid = ? and pubstatus = 'Valid')"
            + "ORDER BY dateinformation DESC, type DESC ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, userId);
      prepStmt.setString(2, userId);
      rs = prepStmt.executeQuery();
      int index = 0;
      while (rs.next()) {
        // limit the searche
        if (index >= firstIndex && index < nbElement + firstIndex) {
          PublicationDetail pd = getPublication(con, rs.getInt(2));
          PublicationWithStatus withStatus = new PublicationWithStatus(pd, rs.getBoolean(3));

          listPublications.add(new SocialInformationPublication(withStatus));
        }
        index++;
      }

    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return listPublications;
  }

  /**
   *  get publication by her id
   * @param con
   * @param pubId
   * @return
   * @throws SQLException
   */
  public static PublicationDetail getPublication(Connection con, int pubId) throws SQLException {
    PublicationDetail pub = new PublicationDetail();
    String query = "select * from sb_publication_publi where pubid = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, pubId);
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        pub = resultSet2PublicationDetail(rs, null);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return pub;

  }

  /**
   * gets the available component for a given users list
   * @param:Connection con, String myId,List<String> myContactsId
   * @return a list of ComponentName
   *
   */
  public static List<String> getAvailableComponents(Connection con, String myId,
          List<String> myContactsId) throws SQLException {
    String query = "SELECT  distinct  instanceid"
            + " FROM sb_publication_publi ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<String> listAvailableComponents = new ArrayList<String>();
    OrganizationController oc = new OrganizationController();
    try {
      prepStmt = con.prepareStatement(query);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        String componentId = rs.getString(1);
        if (oc.isComponentAvailable(componentId, myId)) {
          listAvailableComponents.add(componentId);
        }

      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return listAvailableComponents;
  }

  /**
   * get list of socialInformation of my contacts according to
   * the type of data base used(PostgreSQL,Oracle,MMS) .
   * @return: List <SocialInformation>
   * @param : String myId
   * @param :List<String> myContactsIds
   * @param :List<String> options list of Available Components name
   * @param int numberOfElement, int firstIndex
   */
  public static List<SocialInformationPublication> getSocialInformationsListOfMyContacts(
          Connection con,
          List<String> myContactsIds,
          List<String> options, int numberOfElement, int firstIndex) throws SQLException {
    String DatabaseProductName = con.getMetaData().getDatabaseProductName().toUpperCase();
    if (DatabaseProductName.contains("POSTGRESQL")) {
      return getSocialInformationsListOfMyContacts_PostgreSQL(con, myContactsIds,
              options, numberOfElement, firstIndex);

    } else if (DatabaseProductName.contains("ORACLE")) {
      return getSocialInformationsListOfMyContacts_Oracle(con, myContactsIds,
              options, numberOfElement, firstIndex);
    }
    //MSSQL
    return getSocialInformationsListOfMyContacts_MMSQL(con, myContactsIds,
            options, numberOfElement, firstIndex);
  }

  /**
   * When data base is PostgreSQL get list of socialInformation of my contacts
   * according to options and number of Item and the first Index
   * @return: List <SocialInformation>
   * @param : String myId
   * @param :List<String> myContactsIds
   * @param :List<String> options list of Available Components name
   * @param int numberOfElement, int firstIndex
   */
  private static List<SocialInformationPublication> getSocialInformationsListOfMyContacts_PostgreSQL(
          Connection con,
          List<String> myContactsIds,
          List<String> options, int numberOfElement, int firstIndex) throws SQLException {
    List<SocialInformationPublication> listPublications = new ArrayList<SocialInformationPublication>();

    String query = "(SELECT pubcreationdate AS dateinformation, pubid, 'false' as type  FROM sb_publication_publi  WHERE pubcreatorid in(" + toSqlString(
            myContactsIds) + ") and instanceid in(" + toSqlString(options) + ") and pubstatus = 'Valid')"
            + "UNION (SELECT  pubupdatedate AS dateinformation, pubid, 'true' as type FROM sb_publication_publi  WHERE  pubupdaterid in(" + toSqlString(
            myContactsIds) + ")  and instanceid in(" + toSqlString(options) + ")and pubstatus = 'Valid')"
            + "ORDER BY dateinformation DESC, type DESC limit ? offset ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {

      prepStmt = con.prepareStatement(query);

      prepStmt.setInt(1, numberOfElement);
      prepStmt.setInt(2, firstIndex);
      rs = prepStmt.executeQuery();
      while (rs.next()) {

        PublicationDetail pd = getPublication(con, rs.getInt(2));
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
    String result = "";
    if (list == null || list.size() == 0) {
      return "''";
    }
    int size = list.size();
    int i = 0;
    for (String var : list) {
      i++;
      result += "'" + var + "'";
      if (i != size) {
        result += ",";
      }
    }


    return result;
  }

  /**
   * When data base is Oracle get list of socialInformation of my contacts according to options and number of Item and the first Index
   * @return: List <SocialInformation>
   * @param : String myId
   * @param :List<String> myContactsIds
   * @param :List<String> options list of Available Components name
   * @param int numberOfElement, int firstIndex
   */
  private static List<SocialInformationPublication> getSocialInformationsListOfMyContacts_Oracle(
          Connection con,
          List<String> myContactsIds,
          List<String> options, int numberOfElement, int firstIndex) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  /**
   * When data base is MMSQL get list of socialInformation of my contacts according to options and number of Item and the first Index
   * @return: List <SocialInformation>
   * @param : String myId
   * @param :List<String> myContactsIds
   * @param :List<String> options list of Available Components name
   * @param int numberOfElement, int firstIndex
   */
  private static List<SocialInformationPublication> getSocialInformationsListOfMyContacts_MMSQL(
          Connection con,
          List<String> myContactsIds,
          List<String> options, int numberOfElement, int firstIndex) {
    throw new UnsupportedOperationException("Not yet implemented");
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
}