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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.UtilException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class declaration Get cumul datas from database to access and Volume
 * @author
 */
public class SilverStatisticsPeasDAOAccesVolume {

  static AdminController myAdminController = new AdminController("");
  
  private static final String SELECT_VOLUME_YEARS = "SELECT DISTINCT dateStat "
          + "FROM sb_stat_volumecumul ORDER BY dateStat ASC";
  private static final String SELECT_ACCESS_YEARS = "SELECT DISTINCT dateStat " +
      "FROM sb_stat_accesscumul ORDER BY dateStat ASC";
  private static final String SELECT_ACCESS_EVOL_FOR_SPACE = "SELECT dateStat, SUM(countAccess) AS " +
      "accesses FROM sb_stat_accessCumul WHERE spaceId = ? GROUP BY dateStat ORDER BY dateStat ASC";
  public static Collection<String> getVolumeYears() throws SQLException, UtilException {
    
    SilverTrace.debug("silverStatisticsPeas", "SilverStatisticsPeasDAOConnexion.getVolumeYears",
            "selectQuery=" + SELECT_VOLUME_YEARS);
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      stmt = myCon.prepareStatement(SELECT_VOLUME_YEARS);
      rs = stmt.executeQuery();
      LinkedHashSet<String> years = new LinkedHashSet<String>();
      while (rs.next()) {
        String currentYear = extractYearFromDate(rs.getString("dateStat"));
        if (!years.contains(currentYear)) {
          years.add(currentYear);
        }
      }
      return years;
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }

  public static Collection<String> getAccessYears() throws SQLException, UtilException {
    SilverTrace.debug("silverStatisticsPeas", "SilverStatisticsPeasDAOConnexion.getAccessYears",
            "selectQuery=" + SELECT_ACCESS_YEARS);
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      stmt = myCon.prepareStatement(SELECT_ACCESS_YEARS);
      rs = stmt.executeQuery();
      LinkedHashSet<String> years = new LinkedHashSet<String>();
      while (rs.next()) {
        String currentYear = extractYearFromDate(rs.getString("dateStat"));
        if (!years.contains(currentYear)) {
          years.add(currentYear);
        }
      }
      return years;
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }

  private static String extractYearFromDate(String date) {
    return date.substring(0, 4);
  }

  /**
   * Returns the access statistics.
   * @param dateStat
   * @param currentUserId
   * @param filterIdGroup
   * @param filterIdUser
   * @return the access statistics.
   * @throws SQLException 
   */
  public static Hashtable<String, String[]> getStatsUserVentil(String dateStat,
          String currentUserId, String filterIdGroup, String filterIdUser)
          throws SQLException {
    SilverTrace.info("silverStatisticsPeas", "SilverStatisticsPeasDAOAccessVolume.getStatsUserVentil",
            "root.MSG_GEN_ENTER_METHOD");

    Hashtable<String, String[]> resultat = new Hashtable<String, String[]>(); // key=componentId,
    // value=new
    // String[3] {tout, groupe, user}

    Hashtable<String, String> hashTout = selectAccessForAllComponents(dateStat);
    filterVisibleComponents(currentUserId, resultat, hashTout);

    // Query Groupe
    if (StringUtil.isDefined(filterIdGroup)) {
      for(Map.Entry<String, String[]> componentStatistic : resultat.entrySet()){
        componentStatistic.getValue()[1] = "0";
      }

      Hashtable<String, String> hashGroupe = selectAccessForGroup(dateStat, filterIdGroup);

      for(Map.Entry<String, String[]> componentStatistic : resultat.entrySet()) {
        if (componentStatistic.getValue() != null) {
          componentStatistic.getValue()[1] = hashGroupe.get(componentStatistic.getKey());
        }
      }
    }

    // Query User
    if (StringUtil.isDefined(filterIdUser)) {

      for(Map.Entry<String, String[]> componentStatistic : resultat.entrySet()){
        componentStatistic.getValue()[2] = "0";
      }
      Hashtable<String, String> hashUser = selectAccessForUser(dateStat, filterIdUser);
      for(Map.Entry<String, String[]> componentStatistic : resultat.entrySet()) {
        if (componentStatistic.getValue() != null) {
          componentStatistic.getValue()[2] = hashUser.get(componentStatistic.getKey());
        }
      }
    }

    return resultat;
  }

  /**
   * donne les stats sur le nombre d'accès
   * @return
   * @throws SQLException
   * @throws ParseException
   */
  public static List<String[]> getStatsUserEvolution(String entite,
          String entiteId, String filterIdGroup, String filterIdUser)
          throws SQLException, ParseException {
    SilverTrace.info("silverStatisticsPeas",
            "SilverStatisticsPeasDAOAccessVolume.getStatsUserEvolution",
            "root.MSG_GEN_ENTER_METHOD");
    if ("SPACE".equals(entite)) {
      if (StringUtil.isDefined(filterIdUser)) {
        return selectUserAccessEvolutionForSpace(entiteId, filterIdUser);
      }
      if (StringUtil.isDefined(filterIdGroup)) {
        return selectGroupAccessEvolutionForSpace(entiteId, filterIdGroup);
      }
      return selectAccessEvolutionForSpace(entiteId);
    }
    if (StringUtil.isDefined(filterIdUser)) {
        return selectUserAccessEvolutionForComponent(entiteId, filterIdUser);
      }
      if (StringUtil.isDefined(filterIdGroup)) {
        return selectGroupAccessEvolutionForComponent(entiteId, filterIdGroup);
      }
      return selectAccessEvolutionForComponent(entiteId);
  }

  public static List<String[]> selectAccessEvolutionForSpace(String spaceId) throws SQLException, ParseException {
    SilverTrace.debug("silverStatisticsPeas", "SilverStatisticsPeasDAOAccessVolume.selectAccessEvolutionForSpace", "selectQuery=" + SELECT_ACCESS_EVOL_FOR_SPACE);
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      pstmt = myCon.prepareStatement(SELECT_ACCESS_EVOL_FOR_SPACE);
      pstmt.setString(1, spaceId);
      rs = pstmt.executeQuery();
      return getStatsUserFromResultSet(rs);
    } finally {
      DBUtil.close(rs, pstmt);
      DBUtil.close(myCon);
    }
  }

  public static List<String[]> selectUserAccessEvolutionForSpace(String spaceId, String userId) throws SQLException, ParseException {
     String selectQuery = "SELECT dateStat, SUM(countAccess) AS accesses FROM sb_stat_accessCumul WHERE spaceId = ? AND userId = ? GROUP BY dateStat ORDER BY dateStat ASC";
     SilverTrace.debug("silverStatisticsPeas", "SilverStatisticsPeasDAOAccessVolume.selectUserAccessEvolutionForSpace", "selectQuery=" + selectQuery);
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      pstmt = myCon.prepareStatement(selectQuery);
      pstmt.setString(1, spaceId);
      pstmt.setInt(2, Integer.parseInt(userId));
      rs = pstmt.executeQuery();
      return getStatsUserFromResultSet(rs);
    } finally {
      DBUtil.close(rs, pstmt);
      DBUtil.close(myCon);
    }
  }

  public static List<String[]> selectGroupAccessEvolutionForSpace(String spaceId, String groupId) throws SQLException, ParseException {
    UserDetail[] users = myAdminController.getAllUsersOfGroup(groupId);
    Map<String, String[]> allAccesses = new HashMap<String, String[]>();
    for(UserDetail user : users){
      List<String[]> userStats = selectUserAccessEvolutionForSpace(spaceId, user.getId());
      for(String[] stats : userStats) {
        if(allAccesses.containsKey(stats[0])) {
          String[] currentData = allAccesses.get(stats[0]);
          currentData[1] = String.valueOf(Integer.parseInt(currentData[1]) + Integer.parseInt(stats[1]));
        } else {
          allAccesses.put(stats[0], stats);
        }
      }
    }
    List<String> dates = new ArrayList<String>(allAccesses.keySet());
    Collections.sort(dates);
    List<String[]> result = new ArrayList<String[]>(dates.size());
    for(String date : dates){
      result.add(allAccesses.get(date));
    }
    return result;
  }


  public static List<String[]> selectAccessEvolutionForComponent(String componentId) throws SQLException, ParseException {
    String selectQuery = "SELECT dateStat, SUM(countAccess) AS accesses FROM sb_stat_accessCumul WHERE componentId = ? GROUP BY dateStat ORDER BY dateStat ASC";
    SilverTrace.debug("silverStatisticsPeas", "SilverStatisticsPeasDAOAccessVolume.selectAccessEvolutionForComponent", "selectQuery=" + selectQuery);
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      pstmt = myCon.prepareStatement(selectQuery);
      pstmt.setString(1, componentId);
      rs = pstmt.executeQuery();
      return getStatsUserFromResultSet(rs);
    } finally {
      DBUtil.close(rs, pstmt);
      DBUtil.close(myCon);
    }
  }

  public static List<String[]> selectUserAccessEvolutionForComponent(String componentId, String userId) throws SQLException, ParseException {
    String selectQuery = "SELECT dateStat, SUM(countAccess) AS accesses FROM sb_stat_accessCumul WHERE componentId = ? AND userId = ? GROUP BY dateStat ORDER BY dateStat ASC";
    SilverTrace.debug("silverStatisticsPeas", "SilverStatisticsPeasDAOAccessVolume.selectAccessEvolutionForComponent", "selectQuery=" + selectQuery);
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      pstmt = myCon.prepareStatement(selectQuery);
      pstmt.setString(1, componentId);
      pstmt.setInt(2, Integer.parseInt(userId));
      rs = pstmt.executeQuery();
      return getStatsUserFromResultSet(rs);
    } finally {
      DBUtil.close(rs, pstmt);
      DBUtil.close(myCon);
    }
  }

  public static List<String[]> selectGroupAccessEvolutionForComponent(String componentId, String groupId) throws SQLException, ParseException {
    UserDetail[] users = myAdminController.getAllUsersOfGroup(groupId);
    Map<String, String[]> allAccesses = new HashMap<String, String[]>();
    for(UserDetail user : users){
      List<String[]> userStats = selectUserAccessEvolutionForComponent(componentId, user.getId());
      for(String[] stats : userStats) {
        if(allAccesses.containsKey(stats[0])) {
          String[] currentData = allAccesses.get(stats[0]);
          currentData[1] = String.valueOf(Integer.parseInt(currentData[1]) + Integer.parseInt(stats[1]));
        } else {
          allAccesses.put(stats[0], stats);
        }
      }
    }
    List<String> dates = new ArrayList<String>(allAccesses.keySet());
    Collections.sort(dates);
    List<String[]> result = new ArrayList<String[]>(dates.size());
    for(String date : dates){
      result.add(allAccesses.get(date));
    }
    return result;
  }


  /**
   * Method declaration
   * @param selectQuery
   * @return
   * @throws SQLException
   * @throws ParseException
   * @see
   */
  private static List<String[]> getStatsUserFromQuery(String selectQuery)
          throws SQLException, ParseException {
    SilverTrace.debug("silverStatisticsPeas",
            "SilverStatisticsPeasDAOAccessVolume.getStatsUserFromQuery",
            "selectQuery=" + selectQuery);
    Statement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      stmt = myCon.createStatement();
      rs = stmt.executeQuery(selectQuery);
      return getStatsUserFromResultSet(rs);
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
  private static List<String[]> getStatsUserFromResultSet(ResultSet rs)
          throws SQLException, ParseException {
    List<String[]> myList = new ArrayList<String[]>();
    Calendar calDateRef = null;

    while (rs.next()) {
      String date = rs.getString(1);
      long count = rs.getLong(2);

      if (calDateRef == null) {// initialisation
        calDateRef = GregorianCalendar.getInstance();
        calDateRef.setTime(DateUtil.parseISO8601Date(date));
        calDateRef.set(Calendar.HOUR, 0);
        calDateRef.set(Calendar.MINUTE, 0);
        calDateRef.set(Calendar.SECOND, 0);
        calDateRef.set(Calendar.MILLISECOND, 0);
      }
      Calendar currentDate = GregorianCalendar.getInstance();
      currentDate.setTime(DateUtil.parseISO8601Date(date));
      currentDate.set(Calendar.HOUR, 0);
      currentDate.set(Calendar.MINUTE, 0);
      currentDate.set(Calendar.SECOND, 0);
      currentDate.set(Calendar.MILLISECOND, 0);

      while (calDateRef.before(currentDate)) {
        String[] stat = new String[2];
        stat[0] = DateUtil.formatAsISO8601Day(calDateRef.getTime());
        stat[1] = "0";
        myList.add(stat);
        calDateRef.add(Calendar.MONTH, 1);
      }
      String[] stat = new String[2];
      stat[0] = date;
      stat[1] = Long.toString(count);
      myList.add(stat);
      calDateRef.add(Calendar.MONTH, 1);
    }
    return myList;
  }

  static void addNewStatistic(Map<String, String> result, String date, long count) {
    result.put(date, String.valueOf(count));
  }

  /**
   * donne les stats sur le nombre de publications
   * @return
   * @throws SQLException
   */
  public static Hashtable<String, String[]> getStatsPublicationsVentil(String dateStat,
          String currentUserId, String filterIdGroup, String filterIdUser)
          throws SQLException {
    SilverTrace.info("silverStatisticsPeas",
            "SilverStatisticsPeasDAOAccessVolume.getStatsPublicationsVentil",
            "root.MSG_GEN_ENTER_METHOD");

    Hashtable<String, String[]> resultat = new Hashtable<String, String[]>(); // key=componentId, value=new
    // String[3] {tout, groupe, user}
    Hashtable<String, String> hashTout = selectVolumeForAllComponents(dateStat);
    filterVisibleComponents(currentUserId, resultat, hashTout);

    // Query Group
    if (StringUtil.isDefined(filterIdGroup)) {
      for(Map.Entry<String, String[]> componentStatistic : resultat.entrySet()){
        componentStatistic.getValue()[1] = "0";
      }

      Hashtable<String, String> hashGroupe = selectVolumeForGroup(dateStat, filterIdGroup);

      for(Map.Entry<String, String[]> componentStatistic : resultat.entrySet()) {
        if (componentStatistic.getValue() != null) {
          componentStatistic.getValue()[1] = hashGroupe.get(componentStatistic.getKey());
        }
      }
    }

    // Query User
    if (StringUtil.isDefined(filterIdUser)) {
      for(Map.Entry<String, String[]> componentStatistic : resultat.entrySet()){
        componentStatistic.getValue()[2] = "0";
      }

      Hashtable<String, String> hashUser = selectVolumeForUser(dateStat, filterIdUser);
      for(Map.Entry<String, String[]> componentStatistic : resultat.entrySet()) {
        if (componentStatistic.getValue() != null) {
          componentStatistic.getValue()[2] = hashUser.get(componentStatistic.getKey());
        }
      }
    }

    return resultat;
  }

  static Hashtable<String, String> selectVolumeForUser(String dateStat, String filterIdUser)
          throws SQLException {
    String selectQuery = "SELECT componentId, SUM(countVolume) AS volume FROM sb_stat_volumecumul "
            + "WHERE datestat= ? AND userId = ? GROUP BY datestat, componentId "
            + "ORDER BY datestat ASC, volume DESC";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;

    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      stmt = myCon.prepareStatement(selectQuery);
      stmt.setString(1, dateStat);
      stmt.setInt(2, Integer.parseInt(filterIdUser));
      rs = stmt.executeQuery();
      Hashtable<String, String> result = new Hashtable<String, String>();
      while (rs.next()) {
        addNewStatistic(result, rs.getString("componentId"), rs.getLong("volume"));
      }
      return result;
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }

  static Hashtable<String, String> selectAccessForAllComponents(String dateStat)
          throws SQLException {
    String selectQuery = "SELECT componentId, SUM(countAccess) AS accesses FROM sb_stat_accesscumul " +
        "WHERE datestat=? GROUP BY dateStat, componentId ORDER BY dateStat ASC, accesses DESC";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;

    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      stmt = myCon.prepareStatement(selectQuery);
      stmt.setString(1, dateStat);
      rs = stmt.executeQuery();
      Hashtable<String, String> result = new Hashtable<String, String>();
      while (rs.next()) {
        addNewStatistic(result, rs.getString("componentId"), rs.getLong("accesses"));
      }
      return result;
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }

  static Hashtable<String, String> selectAccessForUser(String dateStat, String filterIdUser)
          throws SQLException {
    String selectQuery = "SELECT componentId, SUM(countAccess) AS accesses  FROM sb_stat_accesscumul " +
        "WHERE dateStat= ? AND userId = ? GROUP BY dateStat, componentId ORDER BY dateStat ASC, accesses DESC";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;

    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      stmt = myCon.prepareStatement(selectQuery);
      stmt.setString(1, dateStat);
      stmt.setInt(2, Integer.parseInt(filterIdUser));
      rs = stmt.executeQuery();
      Hashtable<String, String> result = new Hashtable<String, String>();
      while (rs.next()) {
        addNewStatistic(result, rs.getString("componentId"), rs.getLong("accesses"));
      }
      return result;
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }

  static Hashtable<String, String> selectAccessForGroup(String dateStat,
          String filterIdGroup) throws SQLException {
    String selectQuery = "SELECT componentId, SUM(countAccess) AS accesses FROM sb_stat_accesscumul, ST_Group_User_Rel"
              + " WHERE dateStat = ? AND groupId=? AND sb_stat_accesscumul.userId=ST_Group_User_Rel.userId"
              + " GROUP BY dateStat, componentId"
              + " ORDER BY dateStat ASC, accesses DESC";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      stmt = myCon.prepareStatement(selectQuery);
      stmt.setString(1, dateStat);
      stmt.setInt(2, Integer.parseInt(filterIdGroup));
      rs = stmt.executeQuery();
      Hashtable<String, String> result = new Hashtable<String, String>();
      while (rs.next()) {
        addNewStatistic(result, rs.getString("componentId"), rs.getLong("accesses"));
      }
      return result;
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }

  static Hashtable<String, String> selectVolumeForGroup(String dateStat,
          String filterIdGroup) throws SQLException {
    String selectQuery = "SELECT componentId, SUM(countVolume) AS volume "
            + " FROM sb_stat_volumecumul, st_Group_User_Rel"
            + " WHERE dateStat=? AND groupId= ? AND SB_Stat_VolumeCumul.userId=ST_Group_User_Rel.userId"
            + " GROUP BY dateStat, componentId"
            + " ORDER BY dateStat ASC, volume DESC";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;

    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      stmt = myCon.prepareStatement(selectQuery);
      stmt.setString(1, dateStat);
      stmt.setInt(2, Integer.parseInt(filterIdGroup));
      rs = stmt.executeQuery();
      Hashtable<String, String> result = new Hashtable<String, String>();
      while (rs.next()) {
        addNewStatistic(result, rs.getString("componentId"), rs.getLong("volume"));
      }
      return result;
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }

  static Hashtable<String, String> selectVolumeForAllComponents(String dateStat)
          throws SQLException {
    // Query Tout
    String selectQuery = "SELECT componentId, SUM(countVolume) AS volume "
            + " FROM SB_Stat_VolumeCumul WHERE dateStat = ? GROUP BY dateStat, componentId"
            + " ORDER BY dateStat ASC, volume DESC";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;

    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      stmt = myCon.prepareStatement(selectQuery);
      stmt.setString(1, dateStat);
      rs = stmt.executeQuery();
      Hashtable<String, String> result = new Hashtable<String, String>();
      while (rs.next()) {
        addNewStatistic(result, rs.getString("componentId"), rs.getLong("volume"));
      }
      return result;
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }

  static void filterVisibleComponents(String currentUserId,
          Hashtable<String, String[]> resultat, Hashtable<String, String> hashTout) {
    for (String cmpId : hashTout.keySet()) {
      boolean ok = false;
      ComponentInst compInst = myAdminController.getComponentInst(cmpId);
      String spaceId = compInst.getDomainFatherId();

      String[] tabManageableSpaceIds = myAdminController.getUserManageableSpaceClientIds(
          currentUserId);
      for (String tabManageableSpaceId : tabManageableSpaceIds) {
        if (spaceId.equals(tabManageableSpaceId)) {
          ok = true;
          break;
        }
      }

      if (ok) {
        String[] values = new String[3];
        values[0] = hashTout.get(cmpId);
        resultat.put(cmpId, values);
      }
    }
  }

}
