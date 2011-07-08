/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

package com.stratelia.silverpeas.silverStatisticsPeas.control;


import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.UtilException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

/**
 * Class declaration Get connections data from database
 * @author
 */
public class SilverStatisticsPeasDAOConnexion {

  private static final OrganizationController controller = new OrganizationController();
  public static final int INDICE_LIB = 0;
  public static final int INDICE_COUNTCONNEXION = 1;
  public static final int INDICE_DURATION = 2;
  public static final int INDICE_ID = 3;

  /**
   * donne les stats global pour l'enemble de tous les users cad 2 infos, la collection contient
   * donc un seul element
   *
   * @param startDate
   * @param endDate
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<String[]> getStatsConnexionAllAll(String startDate, String endDate)
      throws SQLException, UtilException {
    SilverTrace.info("silverStatisticsPeas", "SilverStatisticsPeasDAOConnexion.getStatsConnexionAllAll",
        "root.MSG_GEN_ENTER_METHOD");
    String selectQuery = " SELECT SUM(countConnection), SUM(duration) FROM sb_stat_connectioncumul WHERE dateStat BETWEEN ? AND ?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    List<String[]> result = new ArrayList<String[]>();
    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      stmt = myCon.prepareStatement(selectQuery);
      stmt.setString(1, startDate);
      stmt.setString(2, endDate);
      rs = stmt.executeQuery();
      while(rs.next()){
        result.add(buildConnectionStatistics("*", rs.getLong(1), rs.getLong(2),""));
      }
      return result;
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }

  /**
   * Method declaration
   * @param rs
   * @return
   * @throws SQLException
   * @throws ParseException
   * @see
   */
  private static Collection[] getCollectionArrayFromResultset(ResultSet rs,
      String dateBegin, String dateEnd) throws SQLException, ParseException {
    List<String> dates = new ArrayList<String>();
    List<String> counts = new ArrayList<String>();
    String dateRef = dateBegin;
    Calendar calDateRef = Calendar.getInstance();
    calDateRef.setTime(DateUtil.parseISO8601Date(dateRef));
    calDateRef.set(Calendar.HOUR_OF_DAY, 0);
    calDateRef.set(Calendar.MINUTE, 0);
    calDateRef.set(Calendar.SECOND, 0);
    calDateRef.set(Calendar.MILLISECOND, 0);
    while (rs.next()) {
      String date = rs.getString(1);
      long count = rs.getLong(2);
      while (dateRef.compareTo(date) < 0) {
        dates.add(dateRef);
        counts.add("0");
        calDateRef.add(Calendar.MONTH, 1);
        dateRef =  DateUtil.formatAsISO8601Day(calDateRef.getTime());
      }
      dates.add(date);
      counts.add(String.valueOf(count));
      calDateRef.add(Calendar.MONTH, 1);
      dateRef = DateUtil.formatAsISO8601Day(calDateRef.getTime());
    }

    while (dateRef.compareTo(dateEnd) <= 0) {
      dates.add(dateRef);
      counts.add("0");
      calDateRef.add(Calendar.MONTH, 1);
      dateRef =  DateUtil.formatAsISO8601Day(calDateRef.getTime());
    }
    return new Collection[] { dates, counts };
  }

  /**
   * Returns the number of distinct users connections per month.
   * @param startDate
   * @param endDate
   * @return the number of distinct users connections per month.
   * @throws SQLException
   * @throws ParseException
   */
  public static Collection[] getStatsUser(String startDate, String endDate) throws SQLException,
      ParseException, UtilException {
    SilverTrace.info("silverStatisticsPeas", "SilverStatisticsPeasDAOConnexion.getStatsUser",
        "root.MSG_GEN_ENTER_METHOD");
    String selectQuery = "SELECT datestat, count(distinct userid) FROM sb_stat_connectioncumul " +
        "WHERE dateStat BETWEEN ? AND ? GROUP BY dateStat ORDER BY dateStat";

    PreparedStatement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      stmt = myCon.prepareStatement(selectQuery);
      stmt.setString(1, startDate);
      stmt.setString(2, endDate);
      rs = stmt.executeQuery();
      return getCollectionArrayFromResultset(rs, startDate, endDate);
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }

  /**
   * Returns the total number of connections per month.
   * @param startDate
   * @param endDate
   * @return the total number of connections per month.
   * @throws SQLException
   * @throws ParseException
   */
  public static Collection[] getStatsConnexion(String startDate, String endDate)
      throws SQLException, ParseException, UtilException {
    SilverTrace.info("silverStatisticsPeas", "SilverStatisticsPeasDAOConnexion.getStatsConnexion",
        "root.MSG_GEN_ENTER_METHOD");
    String selectQuery = " SELECT datestat, SUM(countConnection) FROM sb_stat_connectioncumul " +
        "WHERE dateStat BETWEEN ? AND ? GROUP BY dateStat ORDER BY dateStat";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      stmt = myCon.prepareStatement(selectQuery);
      stmt.setString(1, startDate);
      stmt.setString(2, endDate);
      rs = stmt.executeQuery();
      return getCollectionArrayFromResultset(rs, startDate, endDate);
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }

  /**
   * donne les stats pour un user seulement cad 2 infos, la collection contient donc un seul element
   * @param dateBegin
   * @param dateEnd
   * @param idUser
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<String[]> getStatsConnexionAllUser(String dateBegin, String dateEnd,
      int idUser) throws SQLException, UtilException {
    SilverTrace.info("silverStatisticsPeas", "SilverStatisticsPeasDAOConnexion.getStatsConnexionAllUser",
        "root.MSG_GEN_ENTER_METHOD");
    String selectQuery = "SELECT B.lastName, SUM(A.countConnection), SUM(A.duration), B.id " +
        "FROM sb_stat_connectioncumul A, ST_User B WHERE A.dateStat BETWEEN ? AND ? " +
        "AND A.userId = B.id AND A.userID = ? GROUP BY B.lastName, B.id";
    SilverTrace.debug("silverStatisticsPeas", "SilverStatisticsPeasDAOConnexion.getStatsConnexionAllUser",
        "selectQuery=" + selectQuery);

    PreparedStatement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    List<String[]> result = new ArrayList<String[]>();
    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      stmt = myCon.prepareStatement(selectQuery);
      stmt.setString(1, dateBegin);
      stmt.setString(2, dateEnd);
      stmt.setInt(3, idUser);
      rs = stmt.executeQuery();
      while (rs.next()) {
        result.add(buildConnectionStatistics(rs.getString(1), rs.getLong(2), rs.getLong(3),
            rs.getString(4)));
      }
      return result;
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }

  /**
   * donne les stats pour un groupe
   * @return
   * @throws SQLException
   * @throws ParseException
   */
  public static Collection[] getStatsUserConnexion(String startDate, String endDate, String idUser)
      throws SQLException, ParseException, UtilException {
    SilverTrace.info("silverStatisticsPeas", "SilverStatisticsPeasDAOConnexion.getStatsUserConnexion",
        "root.MSG_GEN_ENTER_METHOD");
    String selectQuery = "SELECT datestat, SUM(countConnection) FROM sb_stat_connectioncumul " +
        "WHERE dateStat BETWEEN ? AND ? AND userId = ? GROUP BY dateStat ORDER BY dateStat";

    PreparedStatement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      stmt = myCon.prepareStatement(selectQuery);
      stmt.setString(1, startDate);
      stmt.setString(2, endDate);
      stmt.setInt(3, Integer.parseInt(idUser));
      rs = stmt.executeQuery();
      return getCollectionArrayFromResultset(rs, startDate, endDate);
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }

  /**
   * donne les stats pour un groupe seulement cad 2 info, la collection contient donc un seul
   * element
   * @param dateBegin
   * @param dateEnd
   * @param idGroup
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<String[]> getStatsConnexionAllGroup(String dateBegin, String dateEnd,
      int idGroup) throws SQLException, UtilException {
    SilverTrace.info("silverStatisticsPeas", "SilverStatisticsPeasDAOConnexion.getStatsConnexionAllGroup",
        "root.MSG_GEN_ENTER_METHOD");


    String selectQuery = "SELECT B.name, SUM(A.countConnection), SUM(A.duration), B.id"
        + " FROM sb_stat_connectioncumul A, ST_Group B, ST_Group_User_Rel C WHERE " +
        "A.dateStat BETWEEN ? AND ? AND A.userId = C.userId AND C.groupId= B.id AND B.id=?"
        + " GROUP BY B.name, B.id";
    SilverTrace.debug("silverStatisticsPeas", "SilverStatisticsPeasDAOConnexion.getStatsConnexionAllGroup",
        "selectQuery=" + selectQuery);
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;
      List<String[]> result = new ArrayList<String[]>();
    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      stmt = myCon.prepareStatement(selectQuery);
      stmt.setString(1, dateBegin);
      stmt.setString(2, dateEnd);
      stmt.setInt(3, idGroup);
      rs = stmt.executeQuery();
      while (rs.next()) {
        String[] stat = buildConnectionStatistics(rs.getString(1), rs.getLong(2), rs.getLong(3),
            rs.getString(4));
        result.add(stat);
      }
      return result;
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }

  /**
   * Returns the group stats : group name, number of connexions, mean connexion time and group id
   * for all the specified group.
   * @param startDate
   * @param endDate
   * @param groupId
   * @return the group stats : group name, number of connexions, mean connexion time and group id
   * for all the groups.
   * @throws SQLException
   * @throws UtilException
   */
  public static Collection[] getStatsGroupConnexion(String startDate, String endDate,
      String groupId) throws SQLException, ParseException, UtilException {
    SilverTrace.info("silverStatisticsPeas", "SilverStatisticsPeasDAOConnexion.getStatsGroupConnexion",
        "root.MSG_GEN_ENTER_METHOD");
    String selectQuery = " SELECT A.datestat, SUM(A.countConnection) FROM sb_stat_connectioncumul A," +
        " ST_Group_User_Rel C WHERE A.dateStat BETWEEN ? AND ? AND A.userId = C.userId AND " +
        "C.groupId = ? GROUP BY dateStat ORDER BY dateStat";

    PreparedStatement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      stmt = myCon.prepareStatement(selectQuery);
      stmt.setString(1, startDate);
      stmt.setString(2, endDate);
      stmt.setInt(3, Integer.parseInt(groupId));
      rs = stmt.executeQuery();
      return getCollectionArrayFromResultset(rs, startDate, endDate);
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }

  /**
   * Returns the groups stats : group name, number of connexions, mean connexion time and group id
   * for all the groups.
   * @param dateBegin
   * @param dateEnd
   * @return the group stats : group name, number of connexions, mean connexion time and group id
   * for all the groups.
   * @throws SQLException
   * @throws UtilException  
   */
  public static Collection<String[]> getStatsConnexionGroupAll(String dateBegin,
      String dateEnd) throws SQLException, UtilException {
    SilverTrace.info("silverStatisticsPeas",
        "SilverStatisticsPeasDAOConnexion.getStatsConnexionGroupAll",
        "root.MSG_GEN_ENTER_METHOD");
    String selectQuery = "SELECT B.name, SUM(A.countConnection), SUM(A.duration), B.id"
        + " FROM sb_stat_connectioncumul A, ST_Group B, ST_Group_User_Rel C WHERE A.dateStat" +
        " BETWEEN ? AND  ? AND A.userId = C.userId AND C.groupId= B.id GROUP BY B.name, B.id";

    PreparedStatement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    List<String[]> result = new ArrayList<String[]>();
    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      stmt = myCon.prepareStatement(selectQuery);
      stmt.setString(1, dateBegin);
      stmt.setString(2, dateEnd);
      rs = stmt.executeQuery();
      while (rs.next()) {
        result.add(buildConnectionStatistics(rs.getString(1), rs.getLong(2), rs.getLong(3),
            rs.getString(4)));
      }
      return result;
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }

  /**
   * Returns the user stats : User last name, number of connexions, mean connexion time and user id
   * for all the users of a group.
   * @param dateBegin .
   * @param dateEnd .
   * @param groupId .
   * @return the user stats : User last name, number of connexions, mean connexion time and user id
   * for all the users of a group.
   * @throws SQLException
   * @throws UtilException  
   */
  public static Collection<String[]> getStatsConnexionGroupUser(String dateBegin,
      String dateEnd, int groupId) throws SQLException, UtilException {
    String selectQuery = "SELECT B.name, SUM(A.countConnection), SUM(A.duration), B.id FROM " +
        "SB_Stat_ConnectionCumul A, ST_Group B, ST_Group_User_Rel C WHERE A.dateStat BETWEEN ? AND ? " +
        "AND A.userId = C.userId AND C.groupId= B.id AND C.groupId = ? GROUP BY B.name, B.id";

    //UserDetail[] users = controller.getAllUsersOfGroup(groupId);

    SilverTrace.debug("silverStatisticsPeas", "SilverStatisticsPeasDAOConnexion.getStatsConnexionGroupUser",
        "selectQuery=" + selectQuery);
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    List<String[]> result = new ArrayList<String[]>();
    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      stmt = myCon.prepareStatement(selectQuery);
      stmt.setString(1, dateBegin);
      stmt.setString(2, dateEnd);
      stmt.setInt(3, groupId);
      rs = stmt.executeQuery();
      while (rs.next()) {
        result.add(buildConnectionStatistics(rs.getString(1), rs.getLong(2), rs.getLong(3),
            rs.getString(4)));
      }
      return result;
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }

  /**
   * Returns the user stats : User last name, number of connexions, mean connexion time and user id
   * for all Silverpeas users.
   * @param dateBegin
   * @param dateEnd
   * @return the user stats : User last name, number of connexions, mean connexion time and user id
   * for all Silverpeas users.
   * @throws SQLException
   */
  public static Collection<String[]> getStatsConnexionUserAll(String dateBegin,
      String dateEnd) throws SQLException, UtilException {
    SilverTrace.info("silverStatisticsPeas",
        "SilverStatisticsPeasDAOConnexion.getStatsConnexionUserAll",
        "root.MSG_GEN_ENTER_METHOD");

    String selectQuery = "SELECT B.lastName, SUM(A.countConnection), SUM(A.duration), B.id " +
        "FROM SB_Stat_ConnectionCumul A,	ST_User B WHERE A.dateStat BETWEEN ? AND ? " +
        "AND A.userId = B.id GROUP BY B.lastName, B.id";

    PreparedStatement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    List<String[]> result = new ArrayList<String[]>();
    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      stmt = myCon.prepareStatement(selectQuery);
      stmt.setString(1, dateBegin);
      stmt.setString(2, dateEnd);
      rs = stmt.executeQuery();
      while (rs.next()) {
        result.add(buildConnectionStatistics(rs.getString(1), rs.getLong(2), rs.getLong(3),
            rs.getString(4)));
      }
      return result;
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }

  /**
   * Returns the user stats : User last name, number of connexions, mean connexion time and user id.
   * @param dateBegin
   * @param dateEnd
   * @param userId
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<String[]> getStatsConnexionUserUser(String dateBegin,
      String dateEnd, int userId) throws SQLException, UtilException {
    String selectQuery = "SELECT B.lastName, sum(A.countConnection), sum(A.duration), B.id"
        + " FROM SB_Stat_ConnectionCumul A,	ST_User B WHERE A.dateStat between ? AND ? " +
        "AND A.userId = B.Id and B.Id = ? GROUP BY B.lastName, B.id";

    SilverTrace.debug("silverStatisticsPeas",
        "SilverStatisticsPeasDAOConnexion.getStatsConnexionUserGroup",
        "selectQuery=" + selectQuery);

    PreparedStatement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    List<String[]> result = new ArrayList<String[]>();
    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      stmt = myCon.prepareStatement(selectQuery);
      stmt.setString(1, dateBegin);
      stmt.setString(2, dateEnd);
      stmt.setInt(3, userId);
      rs = stmt.executeQuery();
      while (rs.next()) {
        result.add(buildConnectionStatistics(rs.getString(1), rs.getLong(2), rs.getLong(3),
            rs.getString(4)));
      }
      return result;
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }

  }

  public static Collection<String> getYears() throws SQLException, UtilException {
    String selectQuery = "SELECT DISTINCT dateStat FROM SB_Stat_ConnectionCumul ORDER BY dateStat";
    SilverTrace.debug("silverStatisticsPeas", "SilverStatisticsPeasDAOConnexion.getYearsFromQuery",
        "selectQuery=" + selectQuery);
    Statement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      stmt = myCon.createStatement();
      rs = stmt.executeQuery(selectQuery);
      List<String> myList = new ArrayList<String>();
      String year = "";
      while (rs.next()) {
        if (!year.equals(rs.getString(1).substring(0, 4))) {
          year = rs.getString(1).substring(0, 4);
          myList.add(year);
        }
      }
      return myList;
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }

  /**
   * donne les stats d'accès par KM pour la date donnée
   * @return
   * @throws SQLException
   * @throws ParseException
   */
  public static Collection[] getStatsUserFq(String startDate, String endDate, int min, int max)
      throws SQLException, ParseException, UtilException {
    SilverTrace.info("silverStatisticsPeas", "SilverStatisticsPeasDAOConnexion.getStatsUserFq",
        "root.MSG_GEN_ENTER_METHOD");

    String selectQuery = "SELECT dateStat, COUNT(userId) FROM SB_Stat_ConnectionCumul WHERE " +
        "dateStat BETWEEN ? AND ? AND countConnection >= ?  AND countConnection< ? " +
        "GROUP BY dateStat ORDER BY dateStat";

    PreparedStatement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      stmt = myCon.prepareStatement(selectQuery);
      stmt.setString(1, startDate);
      stmt.setString(2, endDate);
      stmt.setInt(3, min);
      stmt.setInt(4, max);
      rs = stmt.executeQuery();
      return getCollectionArrayFromResultset(rs, startDate, endDate);
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }

  /**
   * Method declaration
   * @return
   * @throws SQLException
   * @see
   */
   static String[] buildConnectionStatistics(String name, long count, long duration, String id) throws SQLException {
      String[] stat = new String[4];
      stat[INDICE_LIB] = name;
      stat[INDICE_COUNTCONNEXION] = String.valueOf(count);
      if (count != 0) {
        // calcul durée moyenne = durée totale / nb connexions
        duration /= count;
      }
      stat[INDICE_DURATION] = String.valueOf(duration);
      stat[INDICE_ID] = id;
      return stat;
  }
}