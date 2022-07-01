/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
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

import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.RemovedSpaceAndComponentInstanceChecker;
import org.silverpeas.core.contribution.publication.dao.PublicationCriteria.QUERY_ORDER_BY;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.model.PublicationRuntimeException;
import org.silverpeas.core.contribution.publication.model.PublicationWithStatus;
import org.silverpeas.core.contribution.publication.social.SocialInformationPublication;
import org.silverpeas.core.date.TemporalConverter;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.persistence.datasource.repository.PaginationCriterion;
import org.silverpeas.core.persistence.jdbc.AbstractDAO;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.MapUtil;
import org.silverpeas.core.util.SilverpeasArrayList;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.time.OffsetDateTime.now;
import static java.util.Collections.emptyList;
import static java.util.Optional.of;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static org.silverpeas.core.contribution.publication.dao.PublicationFatherDAO.PUBLICATION_FATHER_TABLE_NAME;
import static org.silverpeas.core.util.DateUtil.formatDate;
import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

/**
 * This is the Publication Data Access Object.
 * @author Nicolas Eysseric
 */
public class PublicationDAO extends AbstractDAO {
  // if beginDate is null, it will be replace in database with it
  private static final String NULL_BEGIN_DATE = "0000/00/00";
  // if endDate is null, it will be replace in database with it
  private static final String NULL_END_DATE = "9999/99/99";
  // if beginHour is null, it will be replace in database with it
  private static final String NULL_BEGIN_HOUR = "00:00";
  // if endDate is null, it will be replace in database with it
  private static final String NULL_END_HOUR = "23:59";

  private static final String UNDEFINED_ID = "unknown";
  private static final String PUB_ID = "pubId";
  private static final String SB_PUBLICATION_PUBLI_TABLE = "sb_publication_publi";
  private static final String PUBSTATUS_VALID_CRITERION = "pubstatus = 'Valid'";
  private static final String SELECT_FROM_SB_PUBLICATION_PUBLI =
      "select * from sb_publication_publi ";
  private static final String FATHER_F = "Father F ";
  private static final String P_PUB_BEGIN_DATE_AND_P_PUB_END_DATE_OR =
      "( ? > P.pubBeginDate AND ? < P.pubEndDate ) OR ";
  private static final String P_PUB_BEGIN_DATE_AND_P_PUB_END_DATE_AND_P_PUB_BEGIN_HOUR_OR =
      "( ? = P.pubBeginDate AND ? < P.pubEndDate AND ? > P.pubBeginHour ) OR ";
  private static final String P_PUB_BEGIN_DATE_AND_P_PUB_END_DATE_AND_P_PUB_END_HOUR_OR =
      "( ? > P.pubBeginDate AND ? = P.pubEndDate AND ? < P.pubEndHour ) OR ";
  private static final String PUBLICATION_DAO_RESULT_SET_2_PUBLICATION_DETAIL_INTERNAL_ERROR =
      "PublicationDAO : resultSet2PublicationDetail() : internal error : ";
  private static final String ORDER_BY = " order by ";
  private static final String AND_F_PUB_ID_EQUAL_P_PUB_ID = " and F.pubId = P.pubId ";
  private static final String SELECT_FROM = "select * from ";
  private static final String AND = " and (";

  static final String PUBLICATION_TABLE_NAME = "SB_Publication_Publi";
  private static final String UPDATE_PUBLICATION =
      "UPDATE SB_Publication_Publi SET infoId = ?, "
          + "pubName = ?, pubDescription = ?, pubCreationDate = ?, pubBeginDate = ?, pubEndDate = ?, "
          + "pubCreatorId = ?, pubImportance = ?, pubVersion = ?, pubKeywords = ?, pubContent = ?, "
          + "pubStatus = ?, pubUpdateDate = ?, pubUpdaterId = ?, "
          + "instanceId = ?, pubValidatorId = ?, pubValidateDate = ?, pubBeginHour = ?, pubEndHour = ?, "
          + "pubAuthor = ?, pubTargetValidatorId = ?, pubCloneId = ?, pubCloneStatus = ?, lang = ? "
          + "WHERE pubId = ? ";
  private static final String WHERE_CONJUNCTION = "WHERE ";
  private static final String AND_CONJUNCTION = "AND ";

  private PublicationDAO() {
  }

  /**
   * Deletes all publications linked to the component instance represented by the given identifier.
   * @param componentInstanceId the identifier of the component instance for which the resources
   * must be deleted.
   * @throws SQLException on SQL error
   */
  public static void deleteComponentInstanceData(String componentInstanceId) throws SQLException {
    JdbcSqlQuery.createDeleteFor(PUBLICATION_TABLE_NAME).where("instanceId = ?", componentInstanceId)
        .execute();
  }

  public static int getNbPubByFatherPath(Connection con, NodePK fatherPK,
      String fatherPath) throws SQLException {
    int result = 0;
    PublicationPK pubPK = new PublicationPK(UNDEFINED_ID, fatherPK);

    if (fatherPath.length() > 0) {
      final String selectStatement =
          "select count(F.pubId) from " + pubPK.getTableName() + "Father F, " +
              pubPK.getTableName() + " P, " + fatherPK.getTableName() + " N " +
              " where F.pubId = P.pubId " + " and F.nodeId = N.nodeId " + " and P.instanceId = ? " +
              " and N.instanceId  = ? " + AND + P_PUB_BEGIN_DATE_AND_P_PUB_END_DATE_OR +
              P_PUB_BEGIN_DATE_AND_P_PUB_END_DATE_AND_P_PUB_BEGIN_HOUR_OR +
              P_PUB_BEGIN_DATE_AND_P_PUB_END_DATE_AND_P_PUB_END_HOUR_OR +
              "( ? = P.pubBeginDate AND ? = P.pubEndDate AND ? > P.pubBeginHour AND ? < P" +
              ".pubEndHour)" +
              " ) " + " and (N.nodePath like '" + fatherPath + "/" + fatherPK.getId() +
              "%' or N.nodeId = " + fatherPK.getId() + ")";

      try (PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
        java.util.Date now = new java.util.Date();
        String dateNow = formatDate(now);
        String hourNow = DateUtil.formatTime(now);

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

        try (ResultSet rs = prepStmt.executeQuery()) {
          if (rs.next()) {
            result = rs.getInt(1);
          }
        }
      }
    }
    return result;
  }

  public static Map<String, Integer> getDistributionTree(final Connection con,
      final DistributionTreeCriteria criteria) throws SQLException {
    final DistributionTreeCriteria completedCriteria = new DistributionTreeCriteria(criteria)
        .ignoringInstanceIds(getRemovedInstanceIdsLinkedToInstance(con, criteria));
    return loadDistributionTree(con, completedCriteria);
  }

  private static List<String> getRemovedInstanceIdsLinkedToInstance(final Connection con,
      final DistributionTreeCriteria criteria) throws SQLException {
    final String instanceId = criteria.getInstanceId();
    final JdbcSqlQuery query = JdbcSqlQuery
        .createSelect("distinct sb_publication_publi.instanceId AS instanceId")
        .from("sb_node_node")
          .join("sb_publication_publifather")
          .on("sb_publication_publifather.nodeid = sb_node_node.nodeid")
          .and("sb_publication_publifather.instanceId = ?", instanceId)
        .join(SB_PUBLICATION_PUBLI_TABLE)
          .on("sb_publication_publifather.pubId = sb_publication_publi.pubId");
    criteria.getStatusSubQuery().ifPresent(query::addSqlPart);
    if (criteria.visibilityCheckRequired()) {
      visibleFilter(query, AND_CONJUNCTION, now(), SB_PUBLICATION_PUBLI_TABLE);
    }
    final RemovedSpaceAndComponentInstanceChecker checker = RemovedSpaceAndComponentInstanceChecker.create();
    return query.where("sb_publication_publi.instanceId <> ?", instanceId)
                .executeWith(con, r -> of(r.getString("instanceId"))
                    .filter(checker::isRemovedComponentInstanceById)
                    .orElse(null));
  }

  private static Map<String, Integer> loadDistributionTree(final Connection con,
      final DistributionTreeCriteria criteria) throws SQLException {
    final String instanceId = criteria.getInstanceId();
    final JdbcSqlQuery query = JdbcSqlQuery
        .createSelect("sb_node_node.nodeId, sb_node_node.nodefatherid, COUNT(sb_publication_publi.pubName) AS nbPubli")
        .from("sb_node_node")
        .outerJoin("sb_publication_publifather")
          .on("sb_publication_publifather.nodeid = sb_node_node.nodeid")
          .and("sb_publication_publifather.instanceId = ?", instanceId)
        .outerJoin(SB_PUBLICATION_PUBLI_TABLE)
          .on("sb_publication_publifather.pubId = sb_publication_publi.pubId");
    final Collection<String> instanceIdsToIgnore = criteria.getInstanceIdsToIgnore();
    if (!instanceIdsToIgnore.isEmpty()) {
      query.and("sb_publication_publi.instanceId").notIn(instanceIdsToIgnore);
    }
    criteria.getStatusSubQuery().ifPresent(query::addSqlPart);
    if (criteria.visibilityCheckRequired()) {
      visibleFilter(query, AND_CONJUNCTION, now(), SB_PUBLICATION_PUBLI_TABLE);
    }
    final Map<String, Integer> nodes = new HashMap<>();
    query
        .where("sb_node_node.instanceId = ?", instanceId)
        .groupBy("sb_node_node.nodeId, sb_node_node.nodefatherid")
        .executeWith(con, r -> {
      final int nodeId = r.getInt("nodeId");
      final String nodeIdentifier = String.valueOf(nodeId);
      nodes.put(nodeIdentifier, r.getInt("nbPubli"));
      return null;
    });
    return nodes;
  }

  public static void insertRow(Connection con, PublicationDetail detail) {
    final String insertStatement = "insert into " + detail.getPK()
        .getTableName() + " (pubId, infoId, pubName, pubDescription, pubCreationDate," +
        " pubBeginDate, pubEndDate, pubCreatorId, pubImportance, pubVersion, pubKeywords," +
        " pubContent, pubStatus, pubUpdateDate," +
        " instanceId, pubUpdaterId, pubValidateDate, pubValidatorId, pubBeginHour, pubEndHour," +
        " pubAuthor, pubTargetValidatorId, pubCloneId, pubCloneStatus, lang) " +
        " values ( ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ?, ?, ? , ? , ? , ? , ? , ? , ? , ?)";

    try (PreparedStatement prepStmt = con.prepareStatement(insertStatement)) {
      prepStmt.setInt(1, Integer.parseInt(detail.getPK().getId()));
      setStringParameter(prepStmt, 2, detail.getInfoId(), "0");
      prepStmt.setString(3, detail.getName());
      prepStmt.setString(4, detail.getDescription());
      setDateParameter(prepStmt, 5, detail.getCreationDate(), DateUtil.today2SQLDate());
      setDateParameter(prepStmt, 6, detail.getBeginDate(), NULL_BEGIN_DATE);
      setDateParameter(prepStmt, 7, detail.getEndDate(), NULL_END_DATE);
      prepStmt.setString(8, detail.getCreatorId());
      prepStmt.setInt(9, detail.getImportance());
      prepStmt.setString(10, detail.getVersion());
      prepStmt.setString(11, detail.getKeywords());
      prepStmt.setString(12, detail.getContentPagePath());
      prepStmt.setString(13, detail.getStatus());
      if (detail.isUpdateDataMustBeSet() && detail.getLastUpdateDate() != null) {
        prepStmt.setString(14, formatDate(detail.getLastUpdateDate()));
      } else {
        setDateParameter(prepStmt, 14, detail.getCreationDate(), DateUtil.today2SQLDate());
      }
      prepStmt.setString(15, detail.getPK().getComponentName());
      if (detail.isUpdateDataMustBeSet() && StringUtil.isDefined(detail.getUpdaterId())) {
        prepStmt.setString(16, detail.getUpdaterId());
      } else {
        prepStmt.setString(16, detail.getCreatorId());
      }
      setDateParameter(prepStmt, 17, detail.getValidateDate(), null);
      prepStmt.setString(18, detail.getValidatorId());
      setStringParameter(prepStmt, 19, detail.getBeginHour(), NULL_BEGIN_HOUR);
      setStringParameter(prepStmt, 20, detail.getEndHour(), NULL_END_HOUR);
      setStringParameter(prepStmt, 21, detail.getAuthor(), null);
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

      prepStmt.executeUpdate();
    } catch (Exception e) {
      SilverLogger.getLogger(PublicationDAO.class).error(e);
    }
  }

  private static boolean isUndefined(String object) {
    return !StringUtil.isDefined(object);
  }

  public static void deleteRow(Connection con, PublicationPK pk)
      throws SQLException {
    PublicationFatherDAO.removeAllFathers(con, pk); // Delete associations
    // between pub and nodes
    StringBuilder deleteStatement = new StringBuilder(128);
    deleteStatement.append("delete from ").append(pk.getTableName()).append(
        " where pubId = ").append(pk.getId());

    try (Statement stmt = con.createStatement()) {
      stmt.executeUpdate(deleteStatement.toString());
    }
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

  private static PublicationDetail resultSet2PublicationDetail(ResultSet rs) throws SQLException {
    return resultSet2PublicationDetail(rs, false);
  }

  private static PublicationDetail resultSet2PublicationDetail(ResultSet rs, boolean getSort)
      throws SQLException {
    PublicationDetail pub;
    PublicationPK pk = null;
    try {
      int id = rs.getInt("pubid");
      String componentId = rs.getString("instanceId");
      pk = new PublicationPK(String.valueOf(id), componentId);
      String infoId = rs.getString("infoid");
      String name = rs.getString("pubname");
      String description = defaultStringIfNotDefined(rs.getString("pubDescription"));
      Date creationDate = DateUtil.parseDate(rs.getString("pubCreationDate"));
      final Date beginDate = asDate(rs.getString("pubBeginDate"), NULL_BEGIN_DATE);
      final Date endDate = asDate(rs.getString("pubEndDate"), NULL_END_DATE);
      String creatorId = rs.getString("pubCreatorId");
      int importance = rs.getInt("pubImportance");
      String version = rs.getString("pubVersion");
      String keywords = rs.getString("pubKeywords");
      String content = rs.getString("pubContent");
      String status = rs.getString("pubStatus");

      Date updateDate;
      String u = rs.getString("pubUpdateDate");
      if (u != null) {
        updateDate = DateUtil.parseDate(u);
      } else {
        updateDate = creationDate;
      }
      String updaterId = defaultStringIfNotDefined(rs.getString("pubUpdaterId"), creatorId);

      Date validateDate;
      String strValDate = rs.getString("pubValidateDate");
      validateDate = DateUtil.parseDate(strValDate);
      String validatorId = rs.getString("pubValidatorId");

      String beginHour = rs.getString("pubBeginHour");
      String endHour = rs.getString("pubEndHour");
      String author = rs.getString("pubAuthor");
      String targetValidatorId = rs.getString("pubTargetValidatorId");
      int tempPubId = rs.getInt("pubCloneId");
      String cloneStatus = rs.getString("pubCloneStatus");
      String lang = rs.getString("lang");

      int order = -1;
      if (getSort) {
        order = rs.getInt("pubOrder");
      }
      pub = PublicationDetail.builder(lang)
          .setPk(pk)
          .setNameAndDescription(name, description)
          .created(creationDate, creatorId)
          .updated(updateDate, updaterId)
          .validated(validateDate, validatorId)
          .setBeginDateTime(beginDate, beginHour)
          .setEndDateTime(endDate, endHour)
          .setImportance(importance)
          .setVersion(version)
          .setKeywords(keywords)
          .setContentPagePath(content)
          .build();
      pub.setStatus(status);
      pub.setAuthor(author);
      pub.setInfoId(infoId);
      pub.setTargetValidatorId(targetValidatorId);
      pub.setCloneId(Integer.toString(tempPubId));
      pub.setCloneStatus(cloneStatus);
      pub.setExplicitRank(order);
    } catch (ParseException e) {
      throw new SQLException(PUBLICATION_DAO_RESULT_SET_2_PUBLICATION_DETAIL_INTERNAL_ERROR +
          "a date format unknown for publication.pk = " + pk + " : " + e.toString());
    }

    return pub;
  }

  public static Collection<PublicationDetail> selectByFatherPK(Connection con, NodePK fatherPK)
      throws SQLException {
    return selectByFatherPK(con, fatherPK, null);
  }

  public static Collection<PublicationDetail> selectByFatherPK(Connection con, NodePK fatherPK,
      String sorting, boolean filterOnVisibilityPeriod) throws SQLException {
    return selectByFatherPK(con, fatherPK, sorting, filterOnVisibilityPeriod, null);
  }

  public static Collection<PublicationDetail> selectByFatherPK(Connection con, NodePK fatherPK,
      String sorting, boolean filterOnVisibilityPeriod, String userId)
      throws SQLException {
    PublicationPK pubPK = new PublicationPK(UNDEFINED_ID, fatherPK);
    StringBuilder selectStatement = new StringBuilder(QueryStringFactory.getSelectByFatherPK(
        pubPK.getTableName(), filterOnVisibilityPeriod, userId));
    if (sorting != null) {
      selectStatement.append(", ").append(sorting);
    }
    try (PreparedStatement prepStmt = con.prepareStatement(selectStatement.toString())) {
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
        String dateNow = formatDate(now);

        String hourNow;
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

      List<PublicationDetail> list = new ArrayList<>();
      try (ResultSet rs = prepStmt.executeQuery()) {
        PublicationDetail pub;
        while (rs.next()) {
          pub = resultSet2PublicationDetail(rs, true);
          list.add(pub);
        }
      }
      return list;
    }
  }

  public static Collection<PublicationDetail> selectByFatherPK(Connection con, NodePK fatherPK,
      String sorting) throws SQLException {
    return selectByFatherPK(con, fatherPK, sorting, true, null);
  }

  public static Collection<PublicationDetail> selectNotInFatherPK(Connection con, NodePK fatherPK,
      String sorting) throws SQLException {
    PublicationPK pubPK = new PublicationPK(UNDEFINED_ID, fatherPK);
    String selectStatement = QueryStringFactory.getSelectNotInFatherPK(pubPK.getTableName());

    if (sorting != null) {
      selectStatement += ORDER_BY + sorting;
    }

    try (PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
      java.util.Date now = new java.util.Date();
      String dateNow = formatDate(now);
      String hourNow = DateUtil.formatTime(now);

      prepStmt.setString(1, pubPK.getComponentName());
      prepStmt.setInt(2, Integer.parseInt(fatherPK.getId()));
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

      ArrayList<PublicationDetail> list = new ArrayList<>();
      try (ResultSet rs = prepStmt.executeQuery()) {
        PublicationDetail pub;
        while (rs.next()) {
          pub = resultSet2PublicationDetail(rs);
          list.add(pub);
        }
      }
      return list;
    }
  }

  public static List<PublicationDetail> selectByFatherIds(Connection con,
      List<String> fatherIds, String instanceId, String sorting,
      List<String> status, boolean filterOnVisibilityPeriod) throws SQLException {

    ArrayList<PublicationDetail> list = new ArrayList<>();
    PublicationDetail pub;
    StringBuilder whereClause = new StringBuilder(128);
    if (fatherIds != null) {
      whereClause.append("(");
      for (String fatherId : fatherIds) {
        whereClause.append(" F.nodeId = ").append(fatherId);
          whereClause.append(" or ");
      }
      whereClause.replace(whereClause.length() - 3, whereClause.length(), " ) ");
    }

    StringBuilder selectStatement = new StringBuilder(128);
    selectStatement.append(
        "select distinct P.pubId, P.infoId, P.pubName, P.pubDescription, P.pubCreationDate, P.pubBeginDate, ");
    selectStatement.append(
        "P.pubEndDate, P.pubCreatorId, P.pubImportance, P.pubVersion, P.pubKeywords, P.pubContent, ");
    selectStatement.append(
        "P.pubStatus, P.pubUpdateDate, P.instanceId, P.pubUpdaterId, P.pubValidateDate, ");
    selectStatement.append(
        "P.pubValidatorId, P.pubBeginHour, P.pubEndHour, P.pubAuthor, P.pubTargetValidatorId, ");
    selectStatement.append(
        "P.pubCloneId, P.pubCloneStatus, P.lang, F.puborder ");
    selectStatement.append("from ").append(PUBLICATION_TABLE_NAME).append(" P, ").append(PUBLICATION_TABLE_NAME).append(FATHER_F);

    selectStatement.append("where ").append(whereClause.toString());

    if (filterOnVisibilityPeriod) {
      selectStatement.append(AND);
      selectStatement.append(P_PUB_BEGIN_DATE_AND_P_PUB_END_DATE_OR);
      selectStatement.append(P_PUB_BEGIN_DATE_AND_P_PUB_END_DATE_AND_P_PUB_BEGIN_HOUR_OR);
      selectStatement.append(P_PUB_BEGIN_DATE_AND_P_PUB_END_DATE_AND_P_PUB_END_HOUR_OR);
      selectStatement.append(
          "( ? = P.pubBeginDate AND ? = P.pubEndDate AND ? > P.pubBeginHour AND ? < P" +
              ".pubEndHour )");
      selectStatement.append(" ) ");
    }
    selectStatement.append(AND_F_PUB_ID_EQUAL_P_PUB_ID);
    selectStatement.append(" and F.instanceId='").append(
        instanceId).append("'");

    if (status != null && !status.isEmpty()) {
      StringBuilder statusBuffer = new StringBuilder();
      Iterator<String> it = status.iterator();

      statusBuffer.append("(");
      String sStatus;
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
      selectStatement.append(ORDER_BY).append(sorting);
    }

    try (PreparedStatement prepStmt = con.prepareStatement(selectStatement.toString())) {
      java.util.Date now = new java.util.Date();
      String dateNow = formatDate(now);
      String hourNow = DateUtil.formatTime(now);

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

      try (ResultSet rs = prepStmt.executeQuery()) {
        while (rs.next()) {
          pub = resultSet2PublicationDetail(rs);
          list.add(pub);
        }
      }
    }

    return list;
  }

  public static List<PublicationDetail> getByIds(final Connection con,
      final Collection<String> publicationIds, final Set<PublicationPK> indexedPks) {
    try {
      final Map<String, PublicationDetail> result = new HashMap<>(publicationIds.size());
      JdbcSqlQuery.executeBySplittingOn(publicationIds, (idBatch, ignore) -> JdbcSqlQuery
        .createSelect(QueryStringFactory.getLoadRowFields())
        .from(SB_PUBLICATION_PUBLI_TABLE)
        .where(PUB_ID).in(idBatch.stream().map(Integer::parseInt).collect(Collectors.toList()))
        .executeWith(con, r -> {
          final PublicationDetail publicationDetail = resultSet2PublicationDetail(r);
          if (indexedPks == null || indexedPks.contains(publicationDetail.getPK())) {
            result.put(publicationDetail.getId(), publicationDetail);
          }
          return null;
        }));
      return publicationIds.stream()
          .map(result::get)
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
    } catch (SQLException e) {
      SilverLogger.getLogger(PublicationDAO.class)
          .error("failing getting publications from PK list");
    }
    return new ArrayList<>();
  }

  /**
   * Selects massively simple data about publications.
   * <p>
   * For now, only the following data are retrieved:
   *   <ul>
   *     <li>pubId</li>
   *     <li>pubStatus</li>
   *     <li>pubCloneId</li>
   *     <li>pubCloneStatus</li>
   *     <li>instanceId</li>
   *     <li>pubBeginDate</li>
   *     <li>pubEndDate</li>
   *     <li>pubBeginHour</li>
   *     <li>pubEndHour</li>
   *     <li>pubcreatorid</li>
   *     <li>pubupdaterid</li>
   *   </ul>
   *   This method is designed for process performance needs.<br/>
   *   The result is not necessarily into same ordering as the one of given parameter.
   * </p>
   * @param con the database connection.
   * @param ids the instance ids aimed.
   * @return a list of {@link PublicationDetail} instances.
   * @throws SQLException on database error.
   */
  public static List<PublicationDetail> getMinimalDataByIds(Connection con,
      Collection<PublicationPK> ids) throws SQLException {
    final Map<PublicationPK, Integer> indexedPubPks = ids.stream()
        .collect(toMap(p -> p, p -> Integer.parseInt(p.getId())));
    final List<PublicationDetail> result = new ArrayList<>(ids.size());
    JdbcSqlQuery.executeBySplittingOn(indexedPubPks.values(), (idBatch, ignore) ->
        JdbcSqlQuery.createSelect("pubId, instanceId, pubStatus, pubCloneId, pubCloneStatus,")
        .addSqlPart("pubBeginDate, pubEndDate, pubBeginHour, pubEndHour,")
        .addSqlPart("pubcreatorid, pubupdaterid")
        .from(SB_PUBLICATION_PUBLI_TABLE)
        .where(PUB_ID).in(idBatch)
        .executeWith(con, r -> {
          final PublicationPK pk = new PublicationPK(Integer.toString(r.getInt(1)), r.getString(2));
          if (indexedPubPks.containsKey(pk)) {
            final PublicationDetail pubDetail = PublicationDetail.builder().setPk(pk).build();
            pubDetail.setStatus(r.getString(3));
            pubDetail.setCloneId(Integer.toString(r.getInt(4)));
            pubDetail.setCloneStatus(r.getString(5));
            try {
              pubDetail.setBeginDate(asDate(r.getString(6), NULL_BEGIN_DATE));
              pubDetail.setEndDate(asDate(r.getString(7), NULL_END_DATE));
            } catch (ParseException e) {
              throw new SQLException(e);
            }
            pubDetail.setBeginHour(r.getString(8));
            pubDetail.setEndHour(r.getString(9));
            pubDetail.setCreatorId(r.getString(10));
            pubDetail.setUpdaterId(r.getString(11));
            result.add(pubDetail);
          }
          return null;
        }));
    return result;
  }

  public static SilverpeasList<PublicationPK> selectPksByCriteria(final Connection con,
      final PublicationCriteria criteria) throws SQLException {
    if (!criteria.emptyResultWhenNoFilteringOnComponentInstances()) {
      final JdbcSqlQuery query = prepareSelectPksByCriteria(criteria);
      configureSelectByCriteria(query, criteria);
      configureFromByCriteria(query, criteria);
      configureClausesByCriteria(query, criteria);
      configureOrderingByCriteria(query, criteria);
      configureExecution(query, criteria);
      return query.executeWith(con, r -> new PublicationPK(r.getString(1), r.getString(2)));
    }
    return new SilverpeasArrayList<>(0);
  }

  public static SilverpeasList<PublicationDetail> selectPublicationsByCriteria(final Connection con,
      final PublicationCriteria criteria) throws SQLException {
    if (!criteria.emptyResultWhenNoFilteringOnComponentInstances()) {
      final JdbcSqlQuery query = prepareSelectPublicationsByCriteria(criteria);
      configureSelectByCriteria(query, criteria);
      configureFromByCriteria(query, criteria);
      configureClausesByCriteria(query, criteria);
      configureOrderingByCriteria(query, criteria);
      configureExecution(query, criteria);
      return query.executeWith(con, PublicationDAO::resultSet2PublicationDetail);
    }
    return new SilverpeasArrayList<>(0);
  }

  private static JdbcSqlQuery prepareSelectPublicationsByCriteria(final PublicationCriteria criteria) {
    if (criteria.mustJoinOnNodeFatherTable()) {
      return JdbcSqlQuery.createSelect("DISTINCT P.*");
    } else {
      return JdbcSqlQuery.createSelect("P.*");
    }
  }

  private static JdbcSqlQuery prepareSelectPksByCriteria(final PublicationCriteria criteria) {
    final JdbcSqlQuery query;
    if (criteria.mustJoinOnNodeFatherTable()) {
      query = JdbcSqlQuery.createSelect("DISTINCT(P.pubId)");
    } else {
      query = JdbcSqlQuery.createSelect("P.pubId");
    }
    if (!criteria.getComponentInstanceIds().isEmpty()) {
      query.addSqlPart(", P.instanceId");
    }
    criteria.getOrderByList().stream()
        .filter(o -> o != QUERY_ORDER_BY.BEGIN_VISIBILITY_DATE_ASC && o != QUERY_ORDER_BY.BEGIN_VISIBILITY_DATE_DESC)
        .filter(o -> o != QUERY_ORDER_BY.CREATION_DATE_ASC && o != QUERY_ORDER_BY.CREATION_DATE_DESC)
        .forEach(o -> query.addSqlPart(", P." + o.getPropertyName()));
    return query;
  }

  private static void configureSelectByCriteria(final JdbcSqlQuery query,
      final PublicationCriteria criteria) {
    criteria.getOrderByList().stream()
        .filter(o -> o == QUERY_ORDER_BY.BEGIN_VISIBILITY_DATE_ASC || o == QUERY_ORDER_BY.BEGIN_VISIBILITY_DATE_DESC)
        .findFirst()
        .ifPresent(o -> query.addSqlPart(
          ", CASE WHEN P.pubUpdateDate > P.pubBeginDate THEN P.pubUpdateDate ELSE P.pubBeginDate END AS " + o.getPropertyName())
        );
  }

  private static void configureFromByCriteria(final JdbcSqlQuery query,
      final PublicationCriteria criteria) {
    query.from(PUBLICATION_TABLE_NAME + " P");
    if (criteria.mustJoinOnNodeFatherTable()) {
      query.join(PUBLICATION_FATHER_TABLE_NAME + " F").on("F.pubId = P.pubId");
    }
    if (!criteria.getComponentInstanceIds().isEmpty()) {
      final String instanceSelector = criteria.isAliasesTakenIntoAccount() ? "F" : "P";
      query.join("ST_ComponentInstance I")
           .on(instanceSelector + ".instanceid = CONCAT(I.componentname , CAST(I.id AS VARCHAR(20)))");
    }
  }

  private static void configureClausesByCriteria(final JdbcSqlQuery query,
      final PublicationCriteria criteria) {
    final List<Integer> componentIds = criteria.getComponentInstanceIds().stream()
        .map(ComponentInst::getComponentLocalId)
        .collect(Collectors.toList());
    final Set<Integer> includedNodeIds = criteria.getIncludedNodeIds();
    final Set<Integer> excludedNodeIds = criteria.getExcludedNodeIds();
    final OffsetDateTime visibilityDate = criteria.getVisibilityDate();
    final OffsetDateTime invisibilityDate = criteria.getInvisibilityDate();
    final OffsetDateTime updatedSince = criteria.getLastUpdatedSince();
    String conjunction = WHERE_CONJUNCTION;
    if (!criteria.getStatuses().isEmpty()) {
      query.addSqlPart(conjunction + "P.pubStatus").in(criteria.getStatuses());
      conjunction = AND_CONJUNCTION;
    }
    if (!includedNodeIds.isEmpty()) {
      query.addSqlPart(conjunction + "F.nodeId").in(includedNodeIds);
      conjunction = AND_CONJUNCTION;
    }
    if (!excludedNodeIds.isEmpty()) {
      query.addSqlPart(conjunction + "F.nodeId").notIn(excludedNodeIds);
      conjunction = AND_CONJUNCTION;
    }
    if (updatedSince != null) {
      query.addSqlPart(conjunction + "P.pubupdatedate > ?", formatDate(updatedSince.toLocalDate()));
      conjunction = AND_CONJUNCTION;
    }
    if (!componentIds.isEmpty()) {
      query.addSqlPart(conjunction + "I.id").in(componentIds);
      conjunction = AND_CONJUNCTION;
    }
    if (visibilityDate != null) {
      visibleFilter(query, conjunction, visibilityDate, "P");
    } else if (invisibilityDate != null) {
      nonVisibleFilter(query, conjunction, invisibilityDate);
    }
  }

  private static void visibleFilter(final JdbcSqlQuery sqlQuery, final String conjunction,
    final OffsetDateTime visibilityDate, final String target) {
    final java.util.Date asDateType = TemporalConverter.asDate(visibilityDate);
    final String dateNow =  formatDate(asDateType);
    final String hourNow = DateUtil.formatTime(asDateType);
    final String p = target + ".";
    sqlQuery
      .addSqlPart(conjunction + f("((? > %spubBeginDate", p), dateNow).and(f("? < %spubEndDate)", p), dateNow)
      .or(f("(? = %spubBeginDate", p), dateNow).and(f("? < %spubEndDate", p), dateNow).and(f("? > %spubBeginHour)", p), hourNow)
      .or(f("(? > %spubBeginDate", p), dateNow).and(f("? = %spubEndDate", p), dateNow).and(f("? < %spubEndHour)", p), hourNow)
      .or(f("(? = %spubBeginDate", p), dateNow).and(f("? = %spubEndDate", p), dateNow).and(f("? > %spubBeginHour", p), hourNow).and(f("? < %spubEndHour))", p), hourNow);
  }

  private static String f(final String sqlToFormat, final String prefix) {
    return format(sqlToFormat, prefix);
  }

  private static void nonVisibleFilter(final JdbcSqlQuery sqlQuery, final String conjunction,
      final OffsetDateTime invisibilityDate) {
    final java.util.Date asDateType = TemporalConverter.asDate(invisibilityDate);
    final String dateNow =  formatDate(asDateType);
    final String hourNow = DateUtil.formatTime(asDateType);
    sqlQuery
        .addSqlPart(conjunction + "((? < P.pubBeginDate", dateNow).or("? > P.pubEndDate)", dateNow)
        .or("(? = P.pubBeginDate", dateNow).and("? < P.pubBeginHour)", hourNow)
        .or("(? = P.pubEndDate", dateNow).and("? > P.pubEndHour))", hourNow);
  }

  private static void configureOrderingByCriteria(final JdbcSqlQuery query,
      final PublicationCriteria criteria) {
    final List<QUERY_ORDER_BY> orderBies = criteria.getOrderByList();
    if (!orderBies.isEmpty()) {
      query.orderBy(orderBies.stream()
          .map(o -> (o.isComplex() ? "" : "P.") + o.getPropertyName() + " " + (o.isAsc() ? "asc" : "desc"))
          .collect(joining(",")));
    }
  }

  private static void configureExecution(final JdbcSqlQuery query,
      final PublicationCriteria criteria) {
    final PaginationPage pagination = criteria.getPagination();
    if (pagination != null) {
      if (criteria.getOrderByList().isEmpty()) {
        throw new IllegalArgumentException(
            "it is not possible to paginate without order by clauses : " + criteria);
      }
      query.withPagination(pagination.asCriterion());
    }
  }

  public static Collection<PublicationDetail> selectAllPublications(Connection con,
      String instanceId, String sorting) throws SQLException {
    StringBuilder selectStatement = new StringBuilder(128);
    selectStatement.append(SELECT_FROM).append(PUBLICATION_TABLE_NAME).append(
        " P where P.instanceId='").append(instanceId).append("'");

    if (sorting != null) {
      selectStatement.append(ORDER_BY).append(sorting);
    }

    try (Statement stmt = con.createStatement();
         ResultSet rs = stmt.executeQuery(selectStatement.toString())) {
      ArrayList<PublicationDetail> list = new ArrayList<>();
      PublicationDetail pub;
      while (rs.next()) {
        pub = resultSet2PublicationDetail(rs);
        list.add(pub);
      }
      return list;
    }
  }

  public static Collection<PublicationDetail> selectByBeginDateDescAndStatus(Connection con,
      PublicationPK pubPK, String status) throws SQLException {
    final String selectStatement =
        "select * from SB_Publication_Publi where pubStatus like '" + status + "' " +
            " and instanceId = ? " + AND + "( ? > pubBeginDate AND ? < pubEndDate ) OR " +
            "( ? = pubBeginDate AND ? < pubEndDate AND ? > pubBeginHour ) OR " +
            "( ? > pubBeginDate AND ? = pubEndDate AND ? < pubEndHour ) OR " +
            "( ? = pubBeginDate AND ? = pubEndDate AND ? > pubBeginHour AND ? < pubEndHour )" +
            " ) " + " order by pubCreationDate DESC, pubBeginDate DESC, pubId DESC";

    try (PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
      java.util.Date now = new java.util.Date();
      String dateNow = formatDate(now);
      String hourNow = DateUtil.formatTime(now);

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

      ArrayList<PublicationDetail> list = new ArrayList<>();
      try (ResultSet rs = prepStmt.executeQuery()) {
        PublicationDetail pub;
        while (rs.next()) {
          pub = resultSet2PublicationDetail(rs);
          list.add(pub);
        }
      }
      return list;
    }
  }

  public static Collection<PublicationDetail> getOrphanPublications(Connection con,
      final String componentId) throws SQLException {
    StringBuilder selectStatement = new StringBuilder(128);
    selectStatement.append(SELECT_FROM).append(PUBLICATION_TABLE_NAME);
    selectStatement.append(" where pubId NOT IN (Select pubId from ").append(
        "SB_Publication_PubliFather").append(") ");
    selectStatement.append(" and instanceId='").append(componentId).append("'");

    try (Statement stmt = con.createStatement();
         ResultSet rs = stmt.executeQuery(selectStatement.toString())) {
      List<PublicationDetail> list = new ArrayList<>();
      PublicationDetail pub;
      while (rs.next()) {
        pub = resultSet2PublicationDetail(rs);
        list.add(pub);
      }
      return list;
    }
  }

  public static Collection<PublicationDetail> getUnavailablePublicationsByPublisherId(
      Connection con, PublicationPK pubPK, String publisherId, String nodeId)
      throws SQLException {
    StringBuilder selectStatement = new StringBuilder(128);
    selectStatement.append(SELECT_FROM)
        .append(pubPK.getTableName())
        .append(" P, ")
        .append(pubPK.getTableName())
        .append(FATHER_F);
    selectStatement.append(" where P.instanceId = ? ");
    selectStatement.append(AND_F_PUB_ID_EQUAL_P_PUB_ID);
    selectStatement.append(" AND (F.nodeId = ").append(nodeId);
    selectStatement.append(" OR ( ? < pubBeginDate ) ");
    selectStatement.append(" OR ( pubBeginDate = ? AND ? < pubBeginHour )");
    selectStatement.append(" OR ( ? > pubEndDate ) ");
    selectStatement.append(" OR ( pubEndDate = ? AND ? > pubEndHour))");
    if (publisherId != null) {
      selectStatement.append(" and P.pubCreatorId = ? ");
    }

    try (PreparedStatement prepStmt = con.prepareStatement(selectStatement.toString())) {
      prepStmt.setString(1, pubPK.getComponentName());
      java.util.Date now = new java.util.Date();
      String formattedDate = formatDate(now);
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

      List<PublicationDetail> list = new ArrayList<>();
      try (ResultSet rs = prepStmt.executeQuery()) {
        PublicationDetail pub;
        while (rs.next()) {
          pub = resultSet2PublicationDetail(rs);
          list.add(pub);
        }
      }
      return list;
    }
  }

  public static PublicationDetail loadRow(Connection con, PublicationPK pk)
      throws SQLException {
    String selectStatement = QueryStringFactory.getLoadRow(pk.getTableName());

    try (PreparedStatement stmt = con.prepareStatement(selectStatement)) {
      PublicationDetail pub;
      stmt.setInt(1, Integer.parseInt(pk.getId()));
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          pub = resultSet2PublicationDetail(rs);
          return pub;
        } else {
          throw new PublicationRuntimeException(
              "Publication with id = " + pk.getId() + " not found in database!");
        }
      }
    }
  }

  public static void changeInstanceId(Connection con, PublicationPK pubPK,
      String newInstanceId) throws SQLException {

    int rowCount;

    StringBuilder updateQuery = new StringBuilder(128);
    updateQuery.append("update ").append(PublicationDAO.PUBLICATION_TABLE_NAME);
    updateQuery.append(" set instanceId = ? ");
    updateQuery.append(" where pubId = ? ");

    try (PreparedStatement prepStmt = con.prepareStatement(updateQuery.toString())) {
      prepStmt.setString(1, newInstanceId);
      prepStmt.setInt(2, Integer.parseInt(pubPK.getId()));
      rowCount = prepStmt.executeUpdate();
    }

    if (rowCount == 0) {
      throw new PublicationRuntimeException(
          "The update of the publication with id = " + pubPK.getId() + " failed!");
    }
  }

  public static void storeRow(Connection con, PublicationDetail detail)
      throws SQLException {
    int rowCount;
    try (PreparedStatement prepStmt = con.prepareStatement(UPDATE_PUBLICATION)) {
      prepStmt.setString(1, detail.getInfoId());
      prepStmt.setString(2, detail.getName());
      prepStmt.setString(3, detail.getDescription());
      prepStmt.setString(4, formatDate(detail.getCreationDate()));
      setDateParameter(prepStmt, 5, detail.getBeginDate(), NULL_BEGIN_DATE);
      setDateParameter(prepStmt, 6, detail.getEndDate(), NULL_END_DATE);
      prepStmt.setString(7, detail.getCreatorId());
      prepStmt.setInt(8, detail.getImportance());
      prepStmt.setString(9, detail.getVersion());
      prepStmt.setString(10, detail.getKeywords());
      prepStmt.setString(11, detail.getContentPagePath());
      prepStmt.setString(12, detail.getStatus());
      setDateParameter(prepStmt, 13, detail.getLastUpdateDate(), formatDate(detail.getCreationDate()));
      if (detail.getUpdaterId() == null) {
        prepStmt.setString(14, detail.getCreatorId());
      } else {
        prepStmt.setString(14, detail.getUpdaterId());
      }
      prepStmt.setString(15, detail.getPK().getComponentName());

      prepStmt.setString(16, detail.getValidatorId());
      if (detail.getValidateDate() != null) {
        prepStmt.setString(17, formatDate(detail.getValidateDate()));
      } else {
        prepStmt.setString(17, null);
      }

      if (isUndefined(detail.getBeginHour())) {
        prepStmt.setString(18, NULL_BEGIN_HOUR);
      } else {
        prepStmt.setString(18, detail.getBeginHour());
      }

      if (isUndefined(detail.getEndHour())) {
        prepStmt.setString(19, NULL_END_HOUR);
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

      prepStmt.setInt(25, Integer.parseInt(detail.getPK().getId()));

      rowCount = prepStmt.executeUpdate();
    }

    if (rowCount == 0) {
      throw new PublicationRuntimeException(
          "The update of the publication with id = " + detail.getPK().getId() + " failed!");
    }
  }

  // Added by ney - 23/08/2001
  public static PublicationDetail selectByName(Connection con,
      PublicationPK pubPK, String name) throws SQLException {
    PublicationDetail pub = null;
    String selectStatement = QueryStringFactory.getSelectByName();
    try (PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
      prepStmt.setString(1, name);
      prepStmt.setString(2, pubPK.getComponentName());

      try (ResultSet rs = prepStmt.executeQuery()) {
        if (rs.next()) {
          pub = resultSet2PublicationDetail(rs);
        }
      }
    }

    return pub;
  }

  public static PublicationDetail selectByNameAndNodeId(Connection con,
      PublicationPK pubPK, String name, int nodeId) throws SQLException {
    PublicationDetail pub = null;
    String selectStatement = QueryStringFactory.getSelectByNameAndNodeId();
    try (PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
      prepStmt.setString(1, name);
      prepStmt.setString(2, pubPK.getComponentName());
      prepStmt.setInt(3, nodeId);

      try (ResultSet rs = prepStmt.executeQuery()) {
        if (rs.next()) {
          pub = resultSet2PublicationDetail(rs);
        }
      }
    }

    return pub;
  }

  public static Collection<PublicationDetail> selectBetweenDate(Connection con, String beginDate,
      String endDate, String instanceId) throws SQLException {
    StringBuilder selectStatement = new StringBuilder(128);
    selectStatement.append(SELECT_FROM).append(PUBLICATION_TABLE_NAME);
    selectStatement.append(" where instanceId = ? ");
    selectStatement.append(AND);
    selectStatement.append("( ? <= pubCreationDate AND ? >= pubCreationDate ) ");
    selectStatement.append(" ) ");
    selectStatement.append(" order by pubCreationDate DESC, pubId DESC");

    try (PreparedStatement prepStmt = con.prepareStatement(selectStatement.toString())) {
      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, beginDate);
      prepStmt.setString(3, endDate);

      List<PublicationDetail> list = new ArrayList<>();
      try (ResultSet rs = prepStmt.executeQuery()) {
        PublicationDetail pub;
        while (rs.next()) {
          pub = resultSet2PublicationDetail(rs);
          list.add(pub);
        }
      }
      return list;
    }
  }

  @SuppressWarnings("unchecked")
  public static List<SocialInformation> getAllPublicationsIDbyUserid(Connection con,
      String userId, Date begin, Date end) throws SQLException {
  final PaginationCriterion pagination = new PaginationCriterion(1, 500).setOriginalSizeRequired(false);
  final Map<String, List<Boolean>> statusMapping = new HashMap<>(pagination.getItemCount());
  final List<String> pubIds = JdbcSqlQuery
      .create("(SELECT pubcreationdate AS dateinformation, pubid, 'false' as type")
      .from(SB_PUBLICATION_PUBLI_TABLE)
      .where("pubcreatorid = ?", userId)
      .and(PUBSTATUS_VALID_CRITERION)
      .and("pubCreationDate >= ?", DateUtil.date2SQLDate(begin))
      .and("pubCreationDate <= ?)", DateUtil.date2SQLDate(end))
      .union()
      .addSqlPart("(SELECT pubupdatedate AS dateinformation, pubid, 'true' as type")
      .from(SB_PUBLICATION_PUBLI_TABLE)
      .where("pubupdaterid = ?", userId)
      .and(PUBSTATUS_VALID_CRITERION)
      .and("pubupdatedate >= ?", DateUtil.date2SQLDate(begin))
      .and("pubupdatedate <= ?)", DateUtil.date2SQLDate(end))
      .orderBy("dateinformation DESC, pubid DESC, type")
      .withPagination(pagination)
      .executeWith(con, r -> {
        final String pubId = Integer.toString(r.getInt(2));
        MapUtil.putAddList(statusMapping, pubId, r.getBoolean(3));
        return pubId;
      });
    return (SilverpeasList) buildSocialInformationResult(con, pubIds, statusMapping);
  }

  private static SilverpeasList<SocialInformationPublication> buildSocialInformationResult(
      final Connection con, final List<String> pubIds,
      final Map<String, List<Boolean>> statusMapping) {
    final Map<String, PublicationDetail> publications = new HashMap<>(pubIds.size());
    getByIds(con, pubIds, null).forEach(p -> publications.put(p.getId(), p));
    return pubIds
        .stream()
        .map(i -> {
          final PublicationDetail p = publications.get(i);
          final Boolean s = statusMapping.get(i).remove(0);
          final PublicationWithStatus withStatus = new PublicationWithStatus(p, s);
          return new SocialInformationPublication(withStatus);
        })
        .collect(SilverpeasList.collector(pubIds));
  }

  public static List<SocialInformationPublication> getSocialInformationsListOfMyContacts(
      Connection con, List<String> myContactsIds, List<String> options, Date begin, Date end)
      throws SQLException {
    if (options.isEmpty()) {
      return emptyList();
    }
    final PaginationCriterion pagination = new PaginationCriterion(1, 500).setOriginalSizeRequired(false);
    final Map<String, List<Boolean>> statusMapping = new HashMap<>(pagination.getItemCount());
    final List<String> pubIds = JdbcSqlQuery
        .create("(SELECT pubcreationdate AS dateinformation, pubid, 'false' as type")
        .from(SB_PUBLICATION_PUBLI_TABLE)
        .where("pubcreatorid").in(myContactsIds)
        .and("instanceid").in(options)
        .and(PUBSTATUS_VALID_CRITERION)
        .and("pubCreationDate >= ?", DateUtil.date2SQLDate(begin))
        .and("pubCreationDate <= ?)", DateUtil.date2SQLDate(end))
        .union()
        .addSqlPart("(SELECT pubupdatedate AS dateinformation, pubid, 'true' as type")
        .from(SB_PUBLICATION_PUBLI_TABLE)
        .where("pubupdaterid").in(myContactsIds)
        .and("instanceid").in(options)
        .and(PUBSTATUS_VALID_CRITERION)
        .and("pubupdatedate >= ?", DateUtil.date2SQLDate(begin))
        .and("pubupdatedate <= ?)", DateUtil.date2SQLDate(end))
        .orderBy("dateinformation DESC, pubid DESC, type")
        .withPagination(pagination)
        .executeWith(con, r -> {
          final String pubId = Integer.toString(r.getInt(2));
          MapUtil.putAddList(statusMapping, pubId, r.getBoolean(3));
          return pubId;
        });
    return buildSocialInformationResult(con, pubIds, statusMapping);
  }

  public static Collection<PublicationDetail> getDraftsByUser(Connection con, String userId)
      throws SQLException {
    StringBuilder sb = new StringBuilder();
    sb.append(SELECT_FROM_SB_PUBLICATION_PUBLI);
    sb.append("where pubUpdaterId = ? ");
    sb.append("and ((pubStatus = ? ");
    sb.append("and pubcloneid = -1 ");
    sb.append("and pubclonestatus is null) ");
    sb.append("or ");
    sb.append("(pubcloneid <> -1 ");
    sb.append("and pubclonestatus = ? )) ");
    sb.append("order by pubUpdateDate desc");

    List<PublicationDetail> publications = new ArrayList<>();
    try (PreparedStatement prepStmt = con.prepareStatement(sb.toString())) {
      prepStmt.setString(1, userId);
      prepStmt.setString(2, PublicationDetail.DRAFT_STATUS);
      prepStmt.setString(3, PublicationDetail.DRAFT_STATUS);
      try (ResultSet rs = prepStmt.executeQuery()) {
        while (rs.next()) {
          publications.add(resultSet2PublicationDetail(rs));
        }
      }
    }
    return publications;
  }

  public static List<PublicationDetail> getByTargetValidatorId(Connection con, String userId)
      throws SQLException {
    StringBuilder sb = new StringBuilder();
    sb.append(SELECT_FROM_SB_PUBLICATION_PUBLI);
    sb.append("where pubTargetValidatorId like ?");

    List<PublicationDetail> publications = new ArrayList<>();
    try (PreparedStatement prepStmt = con.prepareStatement(sb.toString())) {
      prepStmt.setString(1, "%" + userId + "%");
      try (ResultSet rs = prepStmt.executeQuery()) {
        while (rs.next()) {
          String targetValidatorIds = rs.getString("pubTargetValidatorId");
          String[] userIds = StringUtil.split(targetValidatorIds, ',');
          if (ArrayUtil.contains(userIds, userId)) {
            publications.add(resultSet2PublicationDetail(rs));
          }
        }
      }
    }
    return publications;
  }

  public static void updateTargetValidatorIds(Connection con, PublicationDetail detail)
      throws SQLException {
    try (PreparedStatement prepStmt = con.prepareStatement(
        "update SB_Publication_Publi set pubtargetvalidatorid = ? where pubid = ? and instanceid " +
            "= ? ")) {
      prepStmt.setString(1, detail.getTargetValidatorId());
      prepStmt.setInt(2, Integer.parseInt(detail.getPK().getId()));
      prepStmt.setString(3, detail.getPK().getInstanceId());

      prepStmt.executeUpdate();
    }
  }
}
