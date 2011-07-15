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
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.UtilException;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Class declaration Get cumul datas from database to access and Volume
 * @author
 */
public class SilverStatisticsPeasDAOAccesVolume {

  public static final String TYPE_ACCES = "Acces";
  public static final String TYPE_VOLUME = "Volume";
  static AdminController myAdminController = new AdminController("");

  public static Collection<String> getVolumeYears() throws SQLException, UtilException {
    String selectQuery = "SELECT DISTINCT dateStat FROM sb_stat_volumecumul ORDER BY dateStat ASC";
    SilverTrace.debug("silverStatisticsPeas", "SilverStatisticsPeasDAOConnexion.getYearsFromQuery",
            "selectQuery=" + selectQuery);
    Statement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      stmt = myCon.createStatement();
      rs = stmt.executeQuery(selectQuery);
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
    String selectQuery = "SELECT DISTINCT dateStat FROM sb_stat_accesscumul ORDER BY dateStat";
    SilverTrace.debug("silverStatisticsPeas", "SilverStatisticsPeasDAOConnexion.getYearsFromQuery",
            "selectQuery=" + selectQuery);
    Statement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      stmt = myCon.createStatement();
      rs = stmt.executeQuery(selectQuery);
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
   * donne les stats sur le nombre d'accès
   * @return
   * @throws SQLException
   */
  public static Hashtable<String, String[]> getStatsUserVentil(String dateStat,
          String currentUserId, String filterIdGroup, String filterIdUser)
          throws SQLException {
    SilverTrace.info("silverStatisticsPeas",
            "SilverStatisticsPeasDAOAccessVolume.getStatsUserVentil",
            "root.MSG_GEN_ENTER_METHOD");

    Hashtable<String, String[]> resultat = new Hashtable<String, String[]>(); // key=componentId,
    // value=new
    // String[3] {tout, groupe, user}

    Hashtable<String, String> hashTout = selectAccessForAllComponents(dateStat);

    Iterator<String> it = hashTout.keySet().iterator();
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

    String selectQuery = null;
    if ("SPACE".equals(entite)) {

      // Query Tout
      selectQuery = "SELECT dateStat, SUM(countAccess)"
              + " FROM SB_Stat_accessCumul" + " WHERE spaceId ='" + entiteId + "'"
              + " GROUP BY dateStat" + " ORDER BY dateStat ASC";

      // Query Groupe
      if (StringUtil.isDefined(filterIdGroup)) {

        selectQuery = "SELECT A.dateStat, SUM(A.countAccess)"
                + " FROM SB_Stat_accessCumul A, ST_Group_User_Rel C"
                + " WHERE A.spaceId ='" + entiteId + "'"
                + " AND A.userId = C.userId" + " AND C.groupId ='" + filterIdGroup
                + "'" + " GROUP BY A.dateStat" + " ORDER BY A.dateStat ASC";
      }

      // Query User
      if (StringUtil.isDefined(filterIdUser)) {
        selectQuery = "SELECT dateStat, SUM(countAccess)"
                + " FROM SB_Stat_accessCumul" + " WHERE spaceId ='" + entiteId
                + "'" + " AND userId ='" + filterIdUser + "'"
                + " GROUP BY dateStat" + " ORDER BY dateStat ASC";
      }
    } else { // component

      // Query Tout
      selectQuery = "SELECT dateStat, SUM(countAccess)"
              + " FROM SB_Stat_accessCumul" + " WHERE componentId ='" + entiteId
              + "'" + " GROUP BY dateStat" + " ORDER BY dateStat ASC";

      // Query Groupe
       if (StringUtil.isDefined(filterIdGroup)) {

        selectQuery = "SELECT A.dateStat, SUM(A.countAccess)"
                + " FROM SB_Stat_accessCumul A, ST_Group_User_Rel C"
                + " WHERE A.componentId ='" + entiteId + "'"
                + " AND A.userId = C.userId" + " AND C.groupId ='" + filterIdGroup
                + "'" + " GROUP BY A.dateStat" + " ORDER BY A.dateStat ASC";
      }

      // Query User
      if (StringUtil.isDefined(filterIdUser)) {
        selectQuery = "SELECT dateStat, SUM(countAccess)"
                + " FROM SB_Stat_accessCumul" + " WHERE componentId ='" + entiteId
                + "'" + " AND userId ='" + filterIdUser + "'"
                + " GROUP BY dateStat" + " ORDER BY dateStat ASC";
      }
    }

    return getStatsUserFromQuery(selectQuery);
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
    String stat[] = null;
    String date;
    long count = 0;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String dateRef = null;
    Calendar calDateRef = null;

    while (rs.next()) {
      date = rs.getString(1);
      count = rs.getLong(2);

      if (calDateRef == null) {// initialisation
        dateRef = date;
        calDateRef = GregorianCalendar.getInstance();
        calDateRef.setTime(sdf.parse(date));
      }

      while (dateRef.compareTo(date) < 0) {
        stat = new String[2];
        stat[0] = dateRef; // date
        stat[1] = "0"; // nb Accès
        myList.add(stat);

        // ajoute un mois
        calDateRef.add(Calendar.MONTH, 1);
        dateRef = sdf.format(calDateRef.getTime());
      }

      stat = new String[2];
      stat[0] = date; // date
      stat[1] = Long.toString(count); // nb Accès
      myList.add(stat);

      // ajoute un mois
      calDateRef.add(Calendar.MONTH, 1);
      dateRef = sdf.format(calDateRef.getTime());
    }
    return myList;
  }

  /**
   * Method declaration
   * @param selectQuery
   * @return
   * @throws SQLException
   * @see
   */
  private static Hashtable<String, String> getHashtableFromQuery(String selectQuery)
          throws SQLException {
    SilverTrace.debug("silverStatisticsPeas",
            "SilverStatisticsPeasDAOAccessVolume.getHashtableFromQuery",
            "selectQuery=" + selectQuery);
    Statement stmt = null;
    ResultSet rs = null;
    Hashtable<String, String> ht = null;
    Connection myCon = null;

    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      stmt = myCon.createStatement();
      rs = stmt.executeQuery(selectQuery);
      ht = getHashtableFromResultset(rs);
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }

    return ht;
  }

  /**
   * Method declaration
   * @param rs
   * @return
   * @throws SQLException
   * @see
   */
  static Hashtable<String, String> getHashtableFromResultset(ResultSet rs) throws SQLException {
    Hashtable<String, String> result = new Hashtable<String, String>();
    while (rs.next()) {
      addNewStatistic(result, rs.getString(1), rs.getLong(2));
    }
    return result;
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
    Iterator<String> it = hashTout.keySet().iterator();
    while (it.hasNext()) {
      boolean ok = false;
      String cmpId = it.next();
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

  private static Collection<String> getYearsConnexion(ResultSet rs) throws SQLException {
    List<String> myList = new ArrayList<String>();
    String year = "";

    while (rs.next()) {
      if (!year.equals(rs.getString(1).substring(0, 4))) {
        year = rs.getString(1).substring(0, 4);
        myList.add(year);
      }
    }
    return myList;
  }

  private static Collection<String> getYearsFromQuery(String selectQuery) throws SQLException {
    Statement stmt = null;
    ResultSet rs = null;
    Connection myCon = null;
    try {
      myCon = DBUtil.makeConnection(JNDINames.SILVERSTATISTICS_DATASOURCE);
      stmt = myCon.createStatement();
      rs = stmt.executeQuery(selectQuery);
      return getYearsConnexion(rs);
    } finally {
      DBUtil.close(rs, stmt);
      DBUtil.close(myCon);
    }
  }
}
