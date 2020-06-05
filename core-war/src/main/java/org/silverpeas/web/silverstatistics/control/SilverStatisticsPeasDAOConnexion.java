/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.web.silverstatistics.control;

import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.DateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Class declaration Get connections data from database
 * @author
 */
public class SilverStatisticsPeasDAOConnexion {

  private static final int INDICE_LIB = 0;
  private static final int INDICE_COUNTCONNEXION = 1;
  private static final int INDICE_DURATION = 2;
  private static final int INDICE_ID = 3;
  private static final String SELECT_GLOBAL_NB_USER = "SELECT datestat, COUNT(DISTINCT userid) " +
      "AS nbUser FROM sb_stat_connectioncumul WHERE dateStat BETWEEN ? AND ? " +
      "GROUP BY dateStat ORDER BY dateStat";
  private static final String SELECT_GLOBAL_STATISTICS = "SELECT datestat, SUM(countConnection) " +
      "AS connectionCount FROM sb_stat_connectioncumul WHERE dateStat BETWEEN ? AND ? " +
      "GROUP BY dateStat ORDER BY dateStat";
  private static final String SELECT_USER_NB_CONNECTION =
      "SELECT datestat, SUM(countConnection) AS " +
          "connectionCount FROM sb_stat_connectioncumul WHERE dateStat BETWEEN ? AND ? AND userId" +
          " = ? " +
          "GROUP BY dateStat ORDER BY dateStat";
  private static final String SELECT_STATISTICS_FOR_USER = "SELECT B.lastName, " +
      "SUM(A.countConnection) AS connectionCount, SUM(A.duration) AS connectionTime, B.id " +
      "FROM sb_stat_connectioncumul A, ST_User B WHERE A.dateStat BETWEEN ? AND ? " +
      "AND A.userId = B.id AND A.userID = ? GROUP BY B.lastName, B.id";
  private static final String SELECT_STATISTICS_FOR_ALL_USERS = "SELECT B.lastName, " +
      "SUM(A.countConnection) AS connectionCount, SUM(A.duration) AS connectionTime, B.id " +
      "FROM SB_Stat_ConnectionCumul A, ST_User B WHERE A.dateStat BETWEEN ? AND ? " +
      "AND A.userId = B.id GROUP BY B.lastName, B.id";
  private static final String SELECT_GLOBAL_COUNTS =
      "SELECT SUM(countConnection) AS connectionCount, " +
          "SUM(duration) AS connectionTime FROM sb_stat_connectioncumul " +
          "WHERE dateStat BETWEEN ? AND ?";
  private static final String SELECT_COUNTS_FOR_USER =
      "SELECT SUM(countConnection) AS connectionCount, " +
          "SUM(duration) AS connectionTime FROM  sb_stat_connectioncumul WHERE dateStat " +
          "BETWEEN ? AND ? AND userId = ? GROUP BY userId";
  private static final String SELECT_NB_USER_BY_USAGE =
      "SELECT dateStat, COUNT(userId) AS nbUser " +
          "FROM sb_stat_connectioncumul WHERE dateStat BETWEEN ? AND ? AND countConnection >= ?  " +
          "AND countConnection< ? GROUP BY dateStat ORDER BY dateStat";
  private static final String DATESTAT_COLUMN = "datestat";
  private static final String CONNECTION_COUNT_COLUMN = "connectionCount";
  private static final String CONNECTION_TIME_COLUMN = "connectionTime";

  private SilverStatisticsPeasDAOConnexion() {

  }

  /**
   * donne les stats global pour l'enemble de tous les users cad 2 infos, la collection contient
   * donc un seul element
   * @param startDate
   * @param endDate
   * @return
   * @throws SQLException
   */
  public static Collection<String[]> getStatsConnexionAllAll(String startDate, String endDate)
      throws SQLException {
    List<String[]> result = new ArrayList<>();
    try (final Connection myCon = DBUtil.openConnection();
         final PreparedStatement stmt = myCon.prepareStatement(SELECT_GLOBAL_COUNTS)) {
      stmt.setString(1, startDate);
      stmt.setString(2, endDate);
      try (final ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          result.add(buildConnectionStatistics("*", rs.getLong(CONNECTION_COUNT_COLUMN),
              rs.getLong(CONNECTION_TIME_COLUMN), ""));
        }
      }
    }
    return result;
  }

  static LinkedHashMap<String, Long> prepareStatisticsArray(String dateBegin, String dateEnd)
      throws ParseException {
    LinkedHashMap<String, Long> result = new LinkedHashMap<>(48);
    Calendar endDate = Calendar.getInstance();
    endDate.setTime(DateUtil.parseISO8601Date(dateEnd));
    endDate.set(Calendar.HOUR_OF_DAY, 0);
    endDate.set(Calendar.MINUTE, 0);
    endDate.set(Calendar.SECOND, 0);
    endDate.set(Calendar.MILLISECOND, 0);

    Calendar date = Calendar.getInstance();
    date.setTime(DateUtil.parseISO8601Date(dateBegin));
    date.set(Calendar.HOUR_OF_DAY, 0);
    date.set(Calendar.MINUTE, 0);
    date.set(Calendar.SECOND, 0);
    date.set(Calendar.MILLISECOND, 0);
    while (date.before(endDate)) {
      result.put(DateUtil.formatAsISO8601Day(date.getTime()), 0L);
      date.add(Calendar.MONTH, 1);
    }
    result.put(dateEnd, 0L);
    return result;
  }

  static Collection[] convertStatisticsArray(LinkedHashMap<String, Long> statistics) {
    List<String> dates = new ArrayList<>(statistics.size());
    List<String> counts = new ArrayList<>(statistics.size());
    for (Map.Entry<String, Long> data : statistics.entrySet()) {
      dates.add(data.getKey());
      counts.add(String.valueOf(data.getValue()));
    }
    return new Collection[]{dates, counts};
  }

  static void addStatisticsToArray(Map<String, Long> statistics, String date, long value) {
    statistics.put(date, statistics.get(date) + value);
  }

  /**
   * Returns the number of distinct users connections per month.
   * @param startDate
   * @param endDate
   * @return the number of distinct users connections per month.
   * @throws SQLException
   * @throws ParseException
   */
  public static Collection[] getStatsUser(String startDate, String endDate)
      throws SQLException, ParseException {
    try (final Connection myCon = DBUtil.openConnection();
         final PreparedStatement stmt = myCon.prepareStatement(SELECT_GLOBAL_NB_USER)) {
      stmt.setString(1, startDate);
      stmt.setString(2, endDate);
      return fetchUserStatistics(startDate, endDate, stmt);
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
      throws SQLException, ParseException {
    try (final Connection myCon = DBUtil.openConnection();
         final PreparedStatement stmt = myCon.prepareStatement(SELECT_GLOBAL_STATISTICS)) {
      stmt.setString(1, startDate);
      stmt.setString(2, endDate);
      return fetchConnexionStatistics(startDate, endDate, stmt);
    }
  }

  /**
   * Get user connection statistics
   * @param startDate the start date
   * @param endDate the end date
   * @param idUser the user identifier
   * @return an array of date and number of connections of a user between startDate and endDate
   * @throws SQLException
   * @throws ParseException
   */
  public static Collection[] getStatsUserConnexion(String startDate, String endDate, String idUser)
      throws SQLException, ParseException {
    try (final Connection myCon = DBUtil.openConnection();
         final PreparedStatement stmt = myCon.prepareStatement(SELECT_USER_NB_CONNECTION)) {
      stmt.setString(1, startDate);
      stmt.setString(2, endDate);
      stmt.setInt(3, Integer.parseInt(idUser));
      return fetchConnexionStatistics(startDate, endDate, stmt);
    }
  }

  private static Collection[] fetchConnexionStatistics(final String startDate, final String endDate,
      final PreparedStatement stmt) throws SQLException, ParseException {
    try (final ResultSet rs = stmt.executeQuery()) {
      LinkedHashMap<String, Long> statistics = prepareStatisticsArray(startDate, endDate);
      while (rs.next()) {
        addStatisticsToArray(statistics, rs.getString(DATESTAT_COLUMN), rs.getLong(
            CONNECTION_COUNT_COLUMN));
      }
      return convertStatisticsArray(statistics);
    }
  }

  /**
   * Returns the group stats : group name, number of connexions, mean connexion time and group id
   * for all the specified group.
   * @param startDate the start date
   * @param endDate the end date
   * @param groupId the group identifier
   * @return the group stats : group name, number of connexions, mean connexion time and group id
   * for all the groups.
   * @throws SQLException
   */
  public static Collection[] getStatsGroupConnexion(String startDate, String endDate,
      String groupId) throws SQLException, ParseException {
    LinkedHashMap<String, Long> result = prepareStatisticsArray(startDate, endDate);
    try (final Connection myCon = DBUtil.openConnection();
         final PreparedStatement stmt = myCon.prepareStatement(SELECT_USER_NB_CONNECTION)) {
      UserDetail[] users =
          OrganizationControllerProvider.getOrganisationController().getAllUsersOfGroup(groupId);
      for (UserDetail userDetail : users) {
        stmt.setString(1, startDate);
        stmt.setString(2, endDate);
        stmt.setInt(3, Integer.parseInt(userDetail.getId()));
        try (final ResultSet rs = stmt.executeQuery()) {
          while (rs.next()) {
            addStatisticsToArray(result, rs.getString(DATESTAT_COLUMN), rs.getLong(
                CONNECTION_COUNT_COLUMN));
          }
        }
      }
      return convertStatisticsArray(result);
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
   */
  public static Collection<String[]> getStatsConnexionGroupAll(String dateBegin, String dateEnd)
      throws SQLException {
    List<String[]> result = new ArrayList<>();
    Group[] groups = OrganizationControllerProvider.getOrganisationController().getAllGroups();
    for (Group group : groups) {
      result.addAll(getStatsConnexionGroupUser(dateBegin, dateEnd, group));
    }
    return result;
  }

  /**
   * Returns the group stats : Group name, number of connexions, mean connexion time and group id
   * for all the users of a group.
   * @param dateBegin .
   * @param dateEnd .
   * @param groupId .
   * @return the group stats : Group last name, number of connexions, mean connexion time and group
   * id for all the users of a group.
   * @throws SQLException
   */
  public static Collection<String[]> getStatsConnexionAllGroup(String dateBegin, String dateEnd,
      int groupId) throws SQLException {
    return getStatsConnexionGroupUser(dateBegin, dateEnd,
        OrganizationControllerProvider.getOrganisationController()
            .getGroup(String.valueOf(groupId)));
  }

  static Collection<String[]> getStatsConnexionGroupUser(String dateBegin, String dateEnd,
      Group group) throws SQLException {
    List<String[]> result = new ArrayList<>();
    try (final Connection myCon = DBUtil.openConnection();
         final PreparedStatement stmt = myCon.prepareStatement(SELECT_COUNTS_FOR_USER)) {
      long countConnection = 0L;
      long duration = 0L;
      UserDetail[] users = OrganizationControllerProvider.getOrganisationController()
          .getAllUsersOfGroup(group.getId());
      for (UserDetail userDetail : users) {
        stmt.setString(1, dateBegin);
        stmt.setString(2, dateEnd);
        stmt.setInt(3, Integer.parseInt(userDetail.getId()));
        try (final ResultSet rs = stmt.executeQuery()) {
          while (rs.next()) {
            countConnection = countConnection + rs.getLong(CONNECTION_COUNT_COLUMN);
            duration = duration + rs.getLong(CONNECTION_TIME_COLUMN);
          }
        }
      }
      result.add(
          buildConnectionStatistics(group.getName(), countConnection, duration, group.getId()));
      return result;
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
  public static Collection<String[]> getStatsConnexionUserAll(String dateBegin, String dateEnd)
      throws SQLException {
    List<String[]> result = new ArrayList<>();
    try (final Connection myCon = DBUtil.openConnection();
         final PreparedStatement stmt = myCon.prepareStatement(SELECT_STATISTICS_FOR_ALL_USERS)) {
      stmt.setString(1, dateBegin);
      stmt.setString(2, dateEnd);
      try (final ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          result.add(
              buildConnectionStatistics(rs.getString("lastName"), rs.getLong(
                  CONNECTION_COUNT_COLUMN),
                  rs.getLong(CONNECTION_TIME_COLUMN), rs.getString("id")));
        }
      }
    }
    return result;
  }

  /**
   * Returns the user stats : User last name, number of connexions, mean connexion time and user
   * id.
   * @param dateBegin
   * @param dateEnd
   * @param userId
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<String[]> getStatsConnexionUser(String dateBegin, String dateEnd,
      int userId) throws SQLException {
    List<String[]> result = new ArrayList<>();
    try (final Connection myCon = DBUtil.openConnection();
         final PreparedStatement stmt = myCon.prepareStatement(SELECT_STATISTICS_FOR_USER)) {
      stmt.setString(1, dateBegin);
      stmt.setString(2, dateEnd);
      stmt.setInt(3, userId);
      try (final ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          result.add(
              buildConnectionStatistics(rs.getString("lastName"), rs.getLong(
                  CONNECTION_COUNT_COLUMN),
                  rs.getLong(CONNECTION_TIME_COLUMN), rs.getString("id")));
        }
      }
    }
    return result;
  }

  /**
   * @return if no statistic exist return current year, else return a collection of years which are
   * loaded inside SB_Stat_ConnectionCumul table
   * @throws SQLException
   */
  public static Collection<String> getYears() throws SQLException {
    String selectQuery = "SELECT DISTINCT dateStat FROM SB_Stat_ConnectionCumul ORDER BY dateStat";
    try (final Connection myCon = DBUtil.openConnection();
         final Statement stmt = myCon.createStatement();
         final ResultSet rs = stmt.executeQuery(selectQuery)) {
      LinkedHashSet<String> years = new LinkedHashSet<>();
      while (rs.next()) {
        String currentYear = extractYearFromDate(rs.getString("dateStat"));
        if (!years.contains(currentYear)) {
          years.add(currentYear);
        }
      }
      if (years.isEmpty()) {
        years.add(Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
      }
      return years;
    }
  }

  private static String extractYearFromDate(String date) {
    return date.substring(0, 4);
  }

  /**
   * @param startDate
   * @param endDate
   * @param min
   * @param max
   * @return
   * @throws SQLException
   * @throws ParseException
   */
  public static Collection[] getStatsUserFq(String startDate, String endDate, int min, int max)
      throws SQLException, ParseException {
    try (Connection myCon = DBUtil.openConnection();
         PreparedStatement stmt = myCon.prepareStatement(SELECT_NB_USER_BY_USAGE)) {
      stmt.setString(1, startDate);
      stmt.setString(2, endDate);
      stmt.setInt(3, min);
      stmt.setInt(4, max);
      return fetchUserStatistics(startDate, endDate, stmt);
    }
  }

  private static Collection[] fetchUserStatistics(final String startDate, final String endDate,
      final PreparedStatement stmt) throws SQLException, ParseException {
    try (final ResultSet rs = stmt.executeQuery()) {
      LinkedHashMap<String, Long> statistics = prepareStatisticsArray(startDate, endDate);
      while (rs.next()) {
        addStatisticsToArray(statistics, rs.getString(DATESTAT_COLUMN), rs.getLong("nbUser"));
      }
      return convertStatisticsArray(statistics);
    }
  }

  /**
   * Method declaration
   * @return
   * @throws SQLException
   * @see
   */
  static String[] buildConnectionStatistics(String name, long count, long duration, String id) {
    String[] stat = new String[4];
    stat[INDICE_LIB] = name;
    stat[INDICE_COUNTCONNEXION] = String.valueOf(count);
    long meanDuration = duration;
    if (count != 0) {
      // calcul durée moyenne = durée totale / nb connexions
      meanDuration /= count;
    }
    stat[INDICE_DURATION] = String.valueOf(meanDuration);
    stat[INDICE_ID] = id;
    return stat;
  }
}