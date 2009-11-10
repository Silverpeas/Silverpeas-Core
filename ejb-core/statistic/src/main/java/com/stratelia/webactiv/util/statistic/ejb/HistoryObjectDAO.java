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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.statistic.model.HistoryNodePublicationActorDetail;
import com.stratelia.webactiv.util.statistic.model.HistoryObjectDetail;
import com.stratelia.webactiv.util.statistic.model.StatisticRuntimeException;

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class HistoryObjectDAO {
  /**
   * Method declaration
   * 
   * @param rs
   * @param space
   * @param componentName
   * @deprecated : fonction pour récupérer les publications
   * @return
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static Collection getHistoryPublicationDetails(ResultSet rs,
      String space, String componentName) throws SQLException {
    ArrayList list = new ArrayList();
    java.util.Date date;
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
   * 
   * @param rs
   * @param space
   * @param componentName
   * 
   * @return
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static Collection getHistoryDetails(ResultSet rs, String space,
      String componentName) throws SQLException {
    ArrayList list = new ArrayList();
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
      HistoryObjectDetail detail = new HistoryObjectDetail(date, userId,
          foreignPK);

      list.add(detail);
    }
    return list;
  }

  /* cette classe ne devrait jamais etre instanciee */

  /**
   * Constructor declaration
   * 
   * @see
   */
  public HistoryObjectDAO() {
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param tableName
   * @param userId
   * @param nodePK
   * @param pubPK
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static void add(Connection con, String tableName, String userId,
      ForeignPK foreignPK, int actionType, String objectType)
      throws SQLException {
    SilverTrace.info("statistic", "HistoryObjectDAO.add",
        "root.MSG_GEN_ENTER_METHOD");

    String insertStatement = "insert into " + tableName
        + " values (?, ?, ?, ?, ?, ?, ?)";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setString(1, DateUtil.today2SQLDate());
      Calendar calendar = Calendar.getInstance(Locale.FRENCH);
      int hourInt = calendar.get(Calendar.HOUR_OF_DAY);
      String hour = Integer.toString(hourInt);
      if (hourInt < 10)
        hour = "0" + hour;
      int minuteInt = calendar.get(Calendar.MINUTE);
      String minute = Integer.toString(minuteInt);
      if (minuteInt < 10)
        minute = "0" + minute;
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
   * 
   * @param con
   * @param tableName
   * @param foreignPK
   * 
   * @return
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static Collection getHistoryDetailByObject(Connection con,
      String tableName, ForeignPK foreignPK, String objectType)
      throws SQLException {
    SilverTrace.info("statistic", "HistoryObjectDAO.getHistoryDetailByObject",
        "root.MSG_GEN_ENTER_METHOD");
    String space = foreignPK.getSpace();
    String componentName = foreignPK.getComponentName();
    String selectStatement = "select * " + "from " + tableName
        + " where objectId=" + foreignPK.getId() + " and componentId='"
        + foreignPK.getInstanceId() + "'" + " and objectType='" + objectType
        + "'";
    // + " order by dateStat desc, heureStat desc";

    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(selectStatement);
      Collection list = getHistoryDetails(rs, space, componentName);
      return list;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  public static Collection getHistoryDetailByObjectAndUser(Connection con,
      String tableName, ForeignPK foreignPK, String objectType, String userId)
      throws SQLException {
    SilverTrace.info("statistic",
        "HistoryObjectDAO.getHistoryDetailByObjectAndUser",
        "root.MSG_GEN_ENTER_METHOD");
    String space = foreignPK.getSpace();
    String componentName = foreignPK.getComponentName();
    String selectStatement = "select * " + "from " + tableName
        + " where objectId=" + foreignPK.getId() + " and componentId='"
        + foreignPK.getInstanceId() + "'" + " and objectType='" + objectType
        + "'" + " and userId ='" + userId + "'"
        + " order by dateStat desc, heureStat desc";

    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(selectStatement);
      Collection list = getHistoryDetails(rs, space, componentName);
      return list;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  /**
   * Method declaration
   * 
   * @param con
   * @param tableName
   * @param foreignPK
   * 
   * @return
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static void deleteHistoryByObject(Connection con, String tableName,
      ForeignPK foreignPK, String objectType) throws SQLException {
    SilverTrace.info("statistic", "HistoryObjectDAO.deleteHistoryByObject",
        "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    try {
      String query = "delete from " + tableName
          + " where objectId = ? and componentId = ? and objectType = ?";

      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(foreignPK.getId()));
      prepStmt.setString(2, foreignPK.getInstanceId());
      prepStmt.setString(3, objectType);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * 
   * @param con
   * @param tableName
   * @param foreignPK
   * @deprecated : fonction pour récupérer les publications
   * @return
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static Collection getHistoryDetailByPublication(Connection con,
      String tableName, ForeignPK foreignPK) throws SQLException {
    SilverTrace.info("statistic", "HistoryObjectDAO.getHistoryDetailByObject",
        "root.MSG_GEN_ENTER_METHOD");
    String space = foreignPK.getSpace();
    String componentName = foreignPK.getComponentName();
    String selectStatement = "select dateStat, userId, '-1', objectId "
        + "from " + tableName + " where objectId=" + foreignPK.getId()
        + " and componentId='" + foreignPK.getInstanceId() + "'";

    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(selectStatement);
      Collection list = getHistoryPublicationDetails(rs, space, componentName);
      return list;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  public static int getCount(Connection con, Collection foreignPKs, int action,
      String tableName, String objectType) throws SQLException {
    int nb = 0;
    if (!foreignPKs.isEmpty()) {
      Iterator iterator = foreignPKs.iterator();
      for (; iterator.hasNext();) {
        nb = nb
            + getCount(con, (ForeignPK) iterator.next(), action, tableName,
                objectType);
      }
    }
    return nb;
  }

  public static int getCount(Connection con, ForeignPK foreignPK, int action,
      String tableName, String objectType) throws SQLException {
    int nb = 0;
    SilverTrace.info("statistic", "HistoryObjectDAO.getCount",
        "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String selectQuery = "select count(objectId) from " + tableName
        + " where ObjectId=? and ComponentId=? and objectType = ?";
    String foreignId = foreignPK.getId();
    String instanceId = foreignPK.getInstanceId();
    try {
      prepStmt = con.prepareStatement(selectQuery);
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
}
