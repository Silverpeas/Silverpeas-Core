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
 * "http://www.silverpeas.org/legal/licensing"
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.statistic.model.HistoryNodePublicationActorDetail;
import com.stratelia.webactiv.util.statistic.model.HistoryObjectDetail;
import com.stratelia.webactiv.util.statistic.model.StatisticRuntimeException;

/**
 * Class declaration
 * @author
 */
public class HistoryObjectDAO {

  private static final String QUERY_STATISTIC_INSERT =
      "INSERT INTO SB_Statistic_History VALUES (?, ?, ?, ?, ?, ?, ?)";

  private static final String QUERY_STATISTIC_DELETE =
      "DELETE FROM SB_Statistic_History WHERE objectId = ? AND componentId = ? AND objectType = ?";

  private static final String QUERY_STATISTIC_COUNT =
      "SELECT COUNT(objectId) FROM SB_Statistic_History WHERE ObjectId=? AND ComponentId =? AND objectType = ?";

  /**
   * Method declaration
   * @param rs
   * @param space
   * @param componentName
   * @deprecated : fonction pour récupérer les publications
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<HistoryNodePublicationActorDetail> getHistoryPublicationDetails(
      ResultSet rs,
      String space, String componentName) throws SQLException {
    List<HistoryNodePublicationActorDetail> list =
        new ArrayList<HistoryNodePublicationActorDetail>();
    Date date;
    String actorId = "";
    String nodeId = "";
    String pubId = "";

    while (rs.next()) {
      try {
        date = DateUtil.parse(rs.getString(1));
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

  /**
   * Method declaration
   * @param rs
   * @param space
   * @param componentName
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<HistoryObjectDetail> getHistoryDetails(ResultSet rs, String space,
      String componentName) throws SQLException {
    List<HistoryObjectDetail> list = new ArrayList<HistoryObjectDetail>();
    Date date;
    String userId = "";
    String foreignId = "";

    while (rs.next()) {
      try {
        date = DateUtil.parse(rs.getString(1));
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String hhmm = rs.getString(2);
        String hh = hhmm.substring(0, 2);
        String mm = hhmm.substring(3, 5);
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hh));
        cal.set(Calendar.MINUTE, Integer.parseInt(mm));
        date = cal.getTime();
      } catch (java.text.ParseException e) {
        throw new StatisticRuntimeException(
            "HistoryObjectDAO.getHistoryDetails()",
            SilverpeasRuntimeException.ERROR, "statistic.INCORRECT_DATE", e);
      }
      userId = rs.getString(3);
      foreignId = String.valueOf(rs.getInt(4));
      ForeignPK foreignPK = new ForeignPK(foreignId, componentName);
      HistoryObjectDetail detail = new HistoryObjectDetail(date, userId, foreignPK);

      list.add(detail);
    }
    return list;
  }

  /* cette classe ne devrait jamais etre instanciee */

  /**
   * Constructor declaration
   * @see
   */
  public HistoryObjectDAO() {
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
      ForeignPK foreignPK, int actionType, String objectType)
      throws SQLException {
    SilverTrace.info("statistic", "HistoryObjectDAO.add", "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(QUERY_STATISTIC_INSERT);
      prepStmt.setString(1, DateUtil.today2SQLDate());
      Calendar calendar = Calendar.getInstance(Locale.FRENCH);
      int hourInt = calendar.get(Calendar.HOUR_OF_DAY);
      String hour = Integer.toString(hourInt);
      if (hourInt < 10) {
        hour = "0" + hour;
      }
      int minuteInt = calendar.get(Calendar.MINUTE);
      String minute = Integer.toString(minuteInt);
      if (minuteInt < 10) {
        minute = "0" + minute;
      }
      String time = hour + ":" + minute;
      prepStmt.setString(2, time);
      prepStmt.setString(3, userId);
      prepStmt.setInt(4, Integer.parseInt(foreignPK.getId()));
      prepStmt.setString(5, foreignPK.getInstanceId());
      prepStmt.setInt(6, actionType);
      prepStmt.setString(7, objectType);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param tableName
   * @param foreignPK
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<HistoryObjectDetail> getHistoryDetailByObject(Connection con,
      String tableName, ForeignPK foreignPK, String objectType)
      throws SQLException {
    SilverTrace.info("statistic", "HistoryObjectDAO.getHistoryDetailByObject",
        "root.MSG_GEN_ENTER_METHOD");
    String space = foreignPK.getSpace();
    String componentName = foreignPK.getComponentName();
    String selectStatement = "select * from " + tableName
        + " where objectId=" + foreignPK.getId() + " and componentId='"
        + foreignPK.getInstanceId() + "'" + " and objectType='" + objectType
        + "'";

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

  public static Collection<HistoryObjectDetail> getHistoryDetailByObjectAndUser(Connection con,
      String tableName, ForeignPK foreignPK, String objectType, String userId)
      throws SQLException {
    SilverTrace.info("statistic",
        "HistoryObjectDAO.getHistoryDetailByObjectAndUser",
        "root.MSG_GEN_ENTER_METHOD");
    String space = foreignPK.getSpace();
    String componentName = foreignPK.getComponentName();
    String selectStatement = "select * from " + tableName
        + " where objectId=" + foreignPK.getId() + " and componentId='"
        + foreignPK.getInstanceId() + "'" + " and objectType='" + objectType
        + "'" + " and userId ='" + userId + "'"
        + " order by dateStat desc, heureStat desc";

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

  /**
   * Method declaration
   * @param con
   * @param tableName
   * @param foreignPK
   * @return
   * @throws SQLException
   * @see
   */
  public static void deleteHistoryByObject(Connection con, String tableName, ForeignPK foreignPK,
      String objectType) throws SQLException {
    SilverTrace.info("statistic", "HistoryObjectDAO.deleteHistoryByObject",
        "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(QUERY_STATISTIC_DELETE);
      prepStmt.setInt(1, Integer.parseInt(foreignPK.getId()));
      prepStmt.setString(2, foreignPK.getInstanceId());
      prepStmt.setString(3, objectType);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static int getCount(Connection con, Collection<ForeignPK> foreignPKs, int action,
      String tableName, String objectType) throws SQLException {
    int nb = 0;
    if (!foreignPKs.isEmpty()) {
      Iterator<ForeignPK> iterator = foreignPKs.iterator();
      for (; iterator.hasNext();) {
        nb = nb + getCount(con, iterator.next(), action, tableName, objectType);
      }
    }
    return nb;
  }

  public static int getCount(Connection con, ForeignPK foreignPK, int action,
      String tableName, String objectType) throws SQLException {
    int nb = 0;
    SilverTrace.info("statistic", "HistoryObjectDAO.getCount", "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String foreignId = foreignPK.getId();
    String instanceId = foreignPK.getInstanceId();
    try {
      prepStmt = con.prepareStatement(QUERY_STATISTIC_COUNT);
      prepStmt.setInt(1, Integer.parseInt(foreignId));
      prepStmt.setString(2, instanceId);
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

  private static final String QUERY_STATISTIC_COUNT_BY_PERIOD =
      "SELECT COUNT(objectId) FROM SB_Statistic_History WHERE ObjectId=? AND ComponentId =? AND objectType = ? AND datestat >= ? AND datestat <= ?";

  public static int getCountByPeriod(Connection con, WAPrimaryKey primaryKey, String objectType,
      Date startDate, Date endDate) throws SQLException {
    int nb = 0;
    SilverTrace.info("statistic", "HistoryObjectDAO.getCountBYPeriod", "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String foreignId = primaryKey.getId();
    String instanceId = primaryKey.getInstanceId();
    try {
      prepStmt = con.prepareStatement(QUERY_STATISTIC_COUNT_BY_PERIOD);
      prepStmt.setInt(1, Integer.parseInt(foreignId));
      prepStmt.setString(2, instanceId);
      prepStmt.setString(3, objectType);
      prepStmt.setString(4, DateUtil.date2SQLDate(startDate));
      prepStmt.setString(5, DateUtil.date2SQLDate(endDate));
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        nb = rs.getInt(1);
      }
      return nb;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  private static final String QUERY_STATISTIC_COUNT_BY_PERIOD_AND_USER =
      "SELECT COUNT(objectId) FROM SB_Statistic_History WHERE ObjectId=? AND ComponentId =? AND objectType = ? AND datestat >= ? AND datestat <= ? AND userid = ?";

  public static int getCountByPeriodAndUser(Connection con, WAPrimaryKey primaryKey,
      String objectType,
      Date startDate, Date endDate, String userId) throws SQLException {
    int nb = 0;
    SilverTrace.info("statistic", "HistoryObjectDAO.getCountByPeriodAndUser",
        "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String foreignId = primaryKey.getId();
    String instanceId = primaryKey.getInstanceId();
    try {
      prepStmt = con.prepareStatement(QUERY_STATISTIC_COUNT_BY_PERIOD_AND_USER);
      prepStmt.setInt(1, Integer.parseInt(foreignId));
      prepStmt.setString(2, instanceId);
      prepStmt.setString(3, objectType);
      prepStmt.setString(4, DateUtil.date2SQLDate(startDate));
      prepStmt.setString(5, DateUtil.date2SQLDate(endDate));
      prepStmt.setString(6, userId);
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        nb = rs.getInt(1);
      }
      return nb;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static void move(Connection con, String tableName, ForeignPK toForeignPK, int actionType,
      String objectType) throws SQLException {
    SilverTrace.info("statistic", "HistoryObjectDAO.move", "root.MSG_GEN_ENTER_METHOD");

    String insertStatement =
        "update " + tableName +
            " set componentId = ? where objectId = ? and actionType = ? and objectType = ?";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setString(1, toForeignPK.getInstanceId());
      prepStmt.setInt(2, Integer.parseInt(toForeignPK.getId()));
      prepStmt.setInt(3, actionType);
      prepStmt.setString(4, objectType);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static List<Integer> getListObjectAccessByPeriod(Connection con,
      List<WAPrimaryKey> primaryKeys, String objectType, Date startDate, Date endDate)
      throws SQLException {
    StringBuilder query = new StringBuilder();
    query
        .append("SELECT objectId FROM SB_Statistic_History WHERE ComponentId =? AND objectType = ? AND datestat >= ? AND datestat <= ? ");
    String instanceId = null;
    if (primaryKeys != null && primaryKeys.size() > 0) {
      query.append("AND objectId IN (");
      for (WAPrimaryKey pk : primaryKeys) {
        if (primaryKeys.indexOf(pk) != 0) {
          query.append(",");
        }
        query.append(pk.getId());
      }
      query.append(")");
      instanceId = primaryKeys.get(0).getInstanceId();
    }

    List<Integer> results = new ArrayList<Integer>();
    SilverTrace.info("statistic", "HistoryObjectDAO.getListObjectAccessByPeriod",
        "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query.toString());
      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, objectType);
      prepStmt.setString(3, DateUtil.date2SQLDate(startDate));
      prepStmt.setString(4, DateUtil.date2SQLDate(endDate));
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        Integer objectId = rs.getInt(1);
        results.add(objectId);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return results;
  }

  public static List<Integer> getListObjectAccessByPeriodAndUser(Connection con,
      List<WAPrimaryKey> primaryKeys, String objectType, Date startDate, Date endDate,
      String userId)
      throws SQLException {
    StringBuilder query = new StringBuilder();
    query
        .append("SELECT objectId FROM SB_Statistic_History WHERE ComponentId =? AND objectType = ? AND datestat >= ? AND datestat <= ? ");
    String instanceId = null;
    if (primaryKeys != null && primaryKeys.size() > 0) {
      query.append("AND objectId IN (");
      for (WAPrimaryKey pk : primaryKeys) {
        if (primaryKeys.indexOf(pk) != 0) {
          query.append(",");
        }
        query.append(pk.getId());
      }
      query.append(")");
      instanceId = primaryKeys.get(0).getInstanceId();
    }
    if (StringUtil.isDefined(userId)) {
      query.append(" AND userId = ?");
    }

    List<Integer> results = new ArrayList<Integer>();
    SilverTrace.info("statistic", "HistoryObjectDAO.getListObjectAccessByPeriod",
        "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query.toString());
      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, objectType);
      prepStmt.setString(3, DateUtil.date2SQLDate(startDate));
      prepStmt.setString(4, DateUtil.date2SQLDate(endDate));
      prepStmt.setString(5, userId);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        Integer objectId = rs.getInt(1);
        results.add(objectId);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return results;
  }

}
