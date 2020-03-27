/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.silverstatistics.access.dao;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.silverstatistics.access.model.HistoryByUser;
import org.silverpeas.core.silverstatistics.access.model.HistoryCriteria;
import org.silverpeas.core.silverstatistics.access.model.HistoryObjectDetail;
import org.silverpeas.core.silverstatistics.access.model.StatisticRuntimeException;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.util.StringUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.silverpeas.core.contribution.model.ContributionIdentifier.from;
import static org.silverpeas.core.util.DateUtil.*;
import static org.silverpeas.core.util.StringUtil.isDefined;

public class HistoryObjectDAO {

  private static final String HISTORY_TABLE_NAME = "SB_Statistic_History";
  private static final String USER_ID = "userId";

  private static final String QUERY_STATISTIC_INSERT = "INSERT INTO SB_Statistic_History " +
      "(dateStat, heureStat, userId, resourceId, componentId, actionType, resourceType) " +
      "VALUES (?, ?, ?, ?, ?, ?, ?)";

  private static final String QUERY_STATISTIC_DELETE_BY_RESOURCE =
      "DELETE FROM SB_Statistic_History WHERE resourceId = ? AND componentId = ? AND resourceType" +
          " = ?";

  private static final String QUERY_STATISTIC_DELETE_BY_COMPONENT =
      "DELETE FROM SB_Statistic_History WHERE componentId = ?";

  private static final String QUERY_STATISTIC_COUNT =
      "SELECT COUNT(resourceId) FROM SB_Statistic_History WHERE resourceId=? AND ComponentId =? " +
          "AND resourceType = ?";

  private static final String QUERY_STATISTIC_COUNT_BY_PERIOD =
      "SELECT COUNT(resourceId) FROM SB_Statistic_History WHERE resourceId=? AND ComponentId =? " +
          "AND resourceType = ? AND datestat >= ? AND datestat <= ?";

  private HistoryObjectDAO() {
  }

  private static HistoryByUser getHistoryByUser(final ResultSet rs) throws SQLException {
    String userId = rs.getString(1);
    Date date;
    try {
      // First the date of the day is parsed
      final String[] dateTime = rs.getString(2).split("T");
      date = DateUtil.parse(dateTime[0]);
      // Then the hour is set
      date = DateUtil.getDate(date, dateTime[1]);
    } catch (java.text.ParseException e) {
      throw new StatisticRuntimeException(e);
    }
    int nbAccess = rs.getInt(3);
    return new HistoryByUser(userId, date, nbAccess);
  }

  private static HistoryObjectDetail getHistoryDetail(final ResultSet rs) throws SQLException {
    Date date;
    try {
      // First the date of the day is parsed
      date = DateUtil.parse(rs.getString(1));
      // Then the hour is set
      date = DateUtil.getDate(date, rs.getString(2));
    } catch (java.text.ParseException e) {
      throw new StatisticRuntimeException(e);
    }
    String userId = rs.getString(3);
    String foreignId = rs.getString(4);
    String componentId = rs.getString(5);
    ResourceReference resourceReference = new ResourceReference(foreignId, componentId);
    return new HistoryObjectDetail(date, userId, resourceReference);
  }

  /**
   * @param con the database connection
   * @param userId the user identifier
   * @param resourceReference
   * @param actionType
   * @param objectType
   * @throws SQLException
   */
  public static void add(Connection con, String userId, ResourceReference resourceReference, int actionType,
      String objectType) throws SQLException {

    PreparedStatement prepStmt = null;

    try {
      Date now = new Date();
      prepStmt = con.prepareStatement(QUERY_STATISTIC_INSERT);
      prepStmt.setString(1, date2SQLDate(now));
      prepStmt.setString(2, DateUtil.formatTime(now));
      prepStmt.setString(3, userId);
      prepStmt.setString(4, resourceReference.getId());
      prepStmt.setString(5, resourceReference.getInstanceId());
      prepStmt.setInt(6, actionType);
      prepStmt.setString(7, objectType);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Finds by user last access date and number of access.
   * @param con the database connection.
   * @param criteria search criteria.
   * @return a {@link SilverpeasList} of {@link HistoryByUser}.
   */
  public static SilverpeasList<HistoryByUser> findByUserByCriteria(Connection con,
      final HistoryCriteria criteria) throws SQLException  {
    final JdbcSqlQuery sqlQuery = JdbcSqlQuery.createSelect(
        "userId, max(concat(dateStat, concat('T', heureStat))) as lastAccess, count(userId) as nbAccess");
    applySqlCriteria(sqlQuery, criteria);
    return sqlQuery
        .groupBy(USER_ID)
        .orderBy("lastAccess desc, nbAccess desc")
        .withPagination(criteria.getPagination())
        .executeWith(con, HistoryObjectDAO::getHistoryByUser);
  }

  /**
   * Finds all history data satisfying the given criteria.
   * @param con the database connection.
   * @param criteria search criteria.
   * @return a {@link SilverpeasList} of {@link HistoryObjectDetail}.
   * @throws SQLException on technical error with database.
   */
  public static SilverpeasList<HistoryObjectDetail> findByCriteria(Connection con,
      final HistoryCriteria criteria)
      throws SQLException {
    final JdbcSqlQuery sqlQuery = JdbcSqlQuery
        .createSelect("dateStat, heureStat, userId, resourceId, componentId");
    applySqlCriteria(sqlQuery, criteria);
    if (!criteria.getOrderByList().isEmpty()) {
      sqlQuery.orderBy(criteria.getOrderByList().stream()
          .map(HistoryCriteria.QUERY_ORDER_BY::getClause)
          .collect(Collectors.joining(", ")));
    }
    return sqlQuery
        .withPagination(criteria.getPagination())
        .executeWith(con, HistoryObjectDAO::getHistoryDetail);
  }

  private static void applySqlCriteria(final JdbcSqlQuery sqlQuery,
      final HistoryCriteria criteria) {
    sqlQuery.from(HISTORY_TABLE_NAME).where("actionType = ?", criteria.getActionType());
    if (!criteria.getComponentInstanceIds().isEmpty()) {
      sqlQuery.and("componentId").in(criteria.getComponentInstanceIds());
    }
    if (!criteria.getResourceIds().isEmpty()) {
      sqlQuery.and("resourceId").in(criteria.getResourceIds());
    }
    if (isDefined(criteria.getResourceType())) {
      sqlQuery.and("resourceType = ?", criteria.getResourceType());
    }
    if (!criteria.getUserIds().isEmpty()) {
      sqlQuery.and(USER_ID).in(criteria.getUserIds());
    }
    if (!criteria.getExcludedUserIds().isEmpty()) {
      sqlQuery.and(USER_ID).notIn(criteria.getExcludedUserIds());
    }
  }

  /**
   * @param con the database connection
   * @param resourceReference
   * @param objectType
   * @throws SQLException
   */
  public static void deleteHistoryByObject(Connection con, ResourceReference resourceReference, String objectType)
      throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(QUERY_STATISTIC_DELETE_BY_RESOURCE);
      prepStmt.setString(1, resourceReference.getId());
      prepStmt.setString(2, resourceReference.getInstanceId());
      prepStmt.setString(3, objectType);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void deleteStatsOfComponent(Connection con, String componentId)
      throws SQLException {
    SilverTrace
        .info("statistic", "HistoryObjectDAO.deleteStatsOfComponent", "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(QUERY_STATISTIC_DELETE_BY_COMPONENT);
      prepStmt.setString(1, componentId);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static int getCount(Connection con, Collection<ResourceReference> resourceReferences, String objectType)
      throws SQLException {
    int nb = 0;
    for (ResourceReference pk : resourceReferences) {
      nb = nb + getCount(con, pk, objectType);
    }
    return nb;
  }

  public static int getCount(Connection con, ResourceReference resourceReference, String objectType)
      throws SQLException {
    int nb = 0;

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(QUERY_STATISTIC_COUNT);
      prepStmt.setString(1, resourceReference.getId());
      prepStmt.setString(2, resourceReference.getInstanceId());
      prepStmt.setString(3, objectType);
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        nb = rs.getInt(1);
      }
      return nb;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static int getCountByPeriod(Connection con, WAPrimaryKey primaryKey, String objectType,
      Date startDate, Date endDate) throws SQLException {
    int nb = 0;

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(QUERY_STATISTIC_COUNT_BY_PERIOD);
      prepStmt.setString(1, primaryKey.getId());
      prepStmt.setString(2, primaryKey.getInstanceId());
      prepStmt.setString(3, objectType);
      prepStmt.setString(4, date2SQLDate(startDate));
      prepStmt.setString(5, date2SQLDate(endDate));
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        nb = rs.getInt(1);
      }
      return nb;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static Stream<Pair<ContributionIdentifier, Integer>> countByPeriodAndUser(
      final Connection con, final Collection<ContributionIdentifier> contributionIds,
      final Date startDate, final Date endDate, final String userId) throws SQLException {
    final Set<String> ids = contributionIds.stream()
        .map(ContributionIdentifier::getLocalId)
        .collect(Collectors.toSet());
    final Set<String> instanceIds = contributionIds.stream()
        .map(ContributionIdentifier::getComponentInstanceId)
        .collect(Collectors.toSet());
    final Set<String> types = contributionIds.stream()
        .map(ContributionIdentifier::getType)
        .collect(Collectors.toSet());
    final Map<ContributionIdentifier, Integer> result = new HashMap<>(contributionIds.size());
    JdbcSqlQuery.executeBySplittingOn(ids, (idBatch, ignore) ->
        JdbcSqlQuery.executeBySplittingOn(instanceIds, (instanceIdBatch, ignoreToo) ->
            JdbcSqlQuery.executeBySplittingOn(types, (typeBatch, ignoreAlsoToo) -> JdbcSqlQuery
                .createSelect("resourceId, ComponentId, resourceType, count(*)")
                .from(HISTORY_TABLE_NAME)
                .where("resourceId").in(idBatch)
                .and("ComponentId").in(instanceIdBatch)
                .and("resourceType").in(typeBatch)
                .and("datestat >= ?", date2SQLDate(startDate != null ? startDate : MINIMUM_DATE))
                .and("datestat <= ?", date2SQLDate(endDate != null ? endDate : MAXIMUM_DATE))
                .and("userid = ?", userId)
                .groupBy("resourceId, ComponentId, resourceType")
                .executeWith(con, r -> {
                  final ContributionIdentifier cId = from(r.getString(2), r.getString(1), r.getString(3));
                  result.put(cId, r.getInt(4));
                  return null;
                }))));
    return contributionIds.stream().map(i -> Pair.of(i, result.getOrDefault(i, 0)));
  }

  public static void move(Connection con, ResourceReference toResourceReference, int actionType, String objectType)
      throws SQLException {


    String insertStatement = "update " + HISTORY_TABLE_NAME +
        " set componentId = ? where resourceId = ? and actionType = ? and resourceType = ?";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setString(1, toResourceReference.getInstanceId());
      prepStmt.setString(2, toResourceReference.getId());
      prepStmt.setInt(3, actionType);
      prepStmt.setString(4, objectType);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static List<String> getListObjectAccessByPeriod(Connection con,
      List<WAPrimaryKey> primaryKeys, String objectType, Date startDate, Date endDate)
      throws SQLException {
    StringBuilder query = new StringBuilder();
    query.append(
        "SELECT resourceId FROM SB_Statistic_History WHERE ComponentId =? AND resourceType = ? " +
            "AND datestat >= ? AND datestat <= ? ");
    String instanceId = null;
    if (primaryKeys != null && !primaryKeys.isEmpty()) {
      query.append("AND resourceId IN (");
      for (WAPrimaryKey pk : primaryKeys) {
        if (primaryKeys.indexOf(pk) != 0) {
          query.append(",");
        }
        query.append("'").append(pk.getId()).append("'");
      }
      query.append(")");
      instanceId = primaryKeys.get(0).getInstanceId();
    }

    List<String> results = new ArrayList<>();

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query.toString());
      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, objectType);
      prepStmt.setString(3, date2SQLDate(startDate));
      prepStmt.setString(4, date2SQLDate(endDate));
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        results.add(rs.getString(1));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return results;
  }

  public static List<String> getListObjectAccessByPeriodAndUser(Connection con,
      List<WAPrimaryKey> primaryKeys, String objectType, Date startDate, Date endDate,
      String userId) throws SQLException {
    StringBuilder query = new StringBuilder();
    query.append(
        "SELECT resourceId FROM SB_Statistic_History WHERE ComponentId =? AND resourceType = ? " +
            "AND datestat >= ? AND datestat <= ? ");
    String instanceId = null;
    if (CollectionUtil.isNotEmpty(primaryKeys)) {
      query.append("AND resourceId IN (");
      for (WAPrimaryKey pk : primaryKeys) {
        if (primaryKeys.indexOf(pk) != 0) {
          query.append(",");
        }
        query.append("'").append(pk.getId()).append("'");
      }
      query.append(")");
      instanceId = primaryKeys.get(0).getInstanceId();
    }
    if (StringUtil.isDefined(userId)) {
      query.append(" AND userId = ?");
    }

    List<String> results = new ArrayList<>();

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query.toString());
      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, objectType);
      prepStmt.setString(3, date2SQLDate(startDate));
      prepStmt.setString(4, date2SQLDate(endDate));
      prepStmt.setString(5, userId);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        results.add(rs.getString(1));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return results;
  }

  /**
   * Gets the last history detail of each object associated to a user. The result is sorted on the
   * datetime from the youngest to the oldest
   * @param con
   * @param userId
   * @param actionType
   * @param objectType
   * @param nbObjects
   * @return
   * @throws SQLException
   */
  public static Collection<HistoryObjectDetail> getLastHistoryDetailOfObjectsForUser(Connection con,
      String userId, int actionType, String objectType, int nbObjects) throws SQLException {


    String selectStatement =
        "select componentId, resourceId, datestat, heurestat" + " from SB_Statistic_History" +
            " where userId='" + userId + "'" + " and actionType=" + actionType +
            " and resourceType='" + objectType + "'" + " order by datestat desc, heurestat desc";

    Statement stmt = null;
    ResultSet rs = null;
    List<HistoryObjectDetail> result = new ArrayList<>();
    Set<ResourceReference> performedIds = new HashSet<>(nbObjects * 2);
    Date date;

    try {
      stmt = con.createStatement();
      // Setting a cursor to avoid performance problems
      stmt.setFetchSize(50);
      rs = stmt.executeQuery(selectStatement);

      while (rs.next() && performedIds.size() < nbObjects) {

        // Id
        String componentId = rs.getString(1);
        String foreignId = rs.getString(2);
        ResourceReference resourceReference = new ResourceReference(foreignId, componentId);

        // If id is already performed, then it is skiped
        if (performedIds.add(resourceReference)) {
          try {
            // First the date of the day is parsed
            date = DateUtil.parse(rs.getString(3));
            // Then the hour is set
            date = DateUtil.getDate(date, rs.getString(4));
          } catch (java.text.ParseException e) {
            throw new StatisticRuntimeException(e);
          }
          result.add(new HistoryObjectDetail(date, userId, resourceReference));
        }
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return result;
  }
}
