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

package com.stratelia.silverpeas.silverStatisticsPeas.control;

import java.sql.Connection;
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

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;

/**
 * Class declaration Get cumul datas from database to access and Volume
 * @author
 */
public class SilverStatisticsPeasDAOAccesVolume {
  private static final String dbName = JNDINames.SILVERSTATISTICS_DATASOURCE;

  public static final String TYPE_ACCES = "Acces";
  public static final String TYPE_VOLUME = "Volume";

  public static Collection getYears(String typeReq) throws SQLException {
    // String selectQuery = " SELECT DISTINCT LEFT(dateStat,4)";
    // DLE
    String selectQuery = " SELECT DISTINCT dateStat";

    if (typeReq.equals(TYPE_ACCES)) {
      selectQuery += " FROM SB_Stat_AccessCumul";
    } else if (typeReq.equals(TYPE_VOLUME)) {
      selectQuery += " FROM SB_Stat_VolumeCumul";
    }
    selectQuery += " ORDER BY dateStat ASC";

    return getYearsFromQuery(selectQuery);
  }

  /**
   * donne les stats sur le nombre d'accès
   * @return
   * @throws SQLException
   */
  public static Hashtable getStatsUserVentil(String dateStat,
      String currentUserId, String filterIdGroup, String filterIdUser)
      throws SQLException {
    SilverTrace.info("silverStatisticsPeas",
        "SilverStatisticsPeasDAOAccessVolume.getStatsUserVentil",
        "root.MSG_GEN_ENTER_METHOD");

    Hashtable resultat = new Hashtable(); // key=componentId, value=new
    // String[3] {tout, groupe, user}

    // Query Tout
    String selectQuery = "SELECT componentId, SUM(countAccess)"
        + " FROM SB_Stat_accessCumul" + " WHERE dateStat='" + dateStat + "'"
        + " GROUP BY dateStat, componentId"
        + " ORDER BY dateStat ASC, SUM(countAccess) DESC";

    Hashtable hashTout = getHashtableFromQuery(selectQuery);

    Iterator it = hashTout.keySet().iterator();
    AdminController myAdminController = new AdminController("");

    String cmpId;
    ComponentInst compInst;
    String[] values;
    String[] tabManageableSpaceIds;
    String spaceId;
    boolean ok = false;

    while (it.hasNext()) {
      ok = false;
      cmpId = (String) it.next();

      compInst = myAdminController.getComponentInst(cmpId);
      spaceId = compInst.getDomainFatherId(); // ex : WA123

      tabManageableSpaceIds = myAdminController
          .getUserManageableSpaceClientIds(currentUserId);

      // filtre les composants autorisés selon les droits de l'utilisateur
      // (Admin ou Gestionnaire d'espace)
      for (int i = 0; i < tabManageableSpaceIds.length; i++) {
        if (spaceId.equals(tabManageableSpaceIds[i])) {
          ok = true;
          break;
        }
      }

      if (ok) {
        values = new String[3];
        values[0] = (String) hashTout.get(cmpId);
        resultat.put(cmpId, values);
      }
    }

    // Query Groupe
    if (!filterIdGroup.equals("")) {

      // préremplit tout avec 0
      it = resultat.keySet().iterator();
      while (it.hasNext()) {
        cmpId = (String) it.next();
        values = (String[]) resultat.get(cmpId);
        values[1] = "0";
        resultat.put(cmpId, values);
      }

      selectQuery = "SELECT componentId, SUM(countAccess)"
          + " FROM SB_Stat_AccessCumul, ST_Group_User_Rel"
          + " WHERE dateStat='" + dateStat + "'" + " AND groupId="
          + filterIdGroup
          + " AND SB_Stat_AccessCumul.userId=ST_Group_User_Rel.userId"
          + " GROUP BY dateStat, componentId"
          + " ORDER BY dateStat ASC, SUM(countAccess) DESC";

      Hashtable hashGroupe = getHashtableFromQuery(selectQuery);

      it = hashGroupe.keySet().iterator();
      while (it.hasNext()) {
        cmpId = (String) it.next();

        values = (String[]) resultat.get(cmpId);
        if (values != null) {
          values[1] = (String) hashGroupe.get(cmpId);
          resultat.put(cmpId, values);
        }
      }
    }

    // Query User
    if (!filterIdUser.equals("")) {

      // préremplit tout avec 0
      it = resultat.keySet().iterator();
      while (it.hasNext()) {
        cmpId = (String) it.next();
        values = (String[]) resultat.get(cmpId);
        values[2] = "0";
        resultat.put(cmpId, values);
      }

      selectQuery = "SELECT componentId, SUM(countAccess)"
          + " FROM SB_Stat_AccessCumul" + " WHERE dateStat='" + dateStat + "'"
          + " AND userId=" + filterIdUser + " GROUP BY dateStat, componentId"
          + " ORDER BY dateStat ASC, SUM(countAccess) DESC";

      Hashtable hashUser = getHashtableFromQuery(selectQuery);

      it = hashUser.keySet().iterator();
      while (it.hasNext()) {
        cmpId = (String) it.next();

        values = (String[]) resultat.get(cmpId);
        if (values != null) {
          values[2] = (String) hashUser.get(cmpId);
          resultat.put(cmpId, values);
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
  public static Collection getStatsUserEvolution(String entite,
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
      if (!filterIdGroup.equals("")) {

        selectQuery = "SELECT A.dateStat, SUM(A.countAccess)"
            + " FROM SB_Stat_accessCumul A, ST_Group_User_Rel C"
            + " WHERE A.spaceId ='" + entiteId + "'"
            + " AND A.userId = C.userId" + " AND C.groupId ='" + filterIdGroup
            + "'" + " GROUP BY A.dateStat" + " ORDER BY A.dateStat ASC";
      }

      // Query User
      if (!filterIdUser.equals("")) {
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
      if (!filterIdGroup.equals("")) {

        selectQuery = "SELECT A.dateStat, SUM(A.countAccess)"
            + " FROM SB_Stat_accessCumul A, ST_Group_User_Rel C"
            + " WHERE A.componentId ='" + entiteId + "'"
            + " AND A.userId = C.userId" + " AND C.groupId ='" + filterIdGroup
            + "'" + " GROUP BY A.dateStat" + " ORDER BY A.dateStat ASC";
      }

      // Query User
      if (!filterIdUser.equals("")) {
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
  private static Collection getStatsUserFromQuery(String selectQuery)
      throws SQLException, ParseException {
    SilverTrace.debug("silverStatisticsPeas",
        "SilverStatisticsPeasDAOAccessVolume.getStatsUserFromQuery",
        "selectQuery=" + selectQuery);
    Statement stmt = null;
    ResultSet rs = null;
    Collection list = null;
    Connection myCon = getConnection();

    try {
      stmt = myCon.createStatement();
      rs = stmt.executeQuery(selectQuery);
      list = getStatsUserFromResultSet(rs);
    } finally {
      DBUtil.close(rs, stmt);
      freeConnection(myCon);
    }

    return list;
  }

  /**
   * Method declaration
   * @param rs
   * @return
   * @throws SQLException
   * @throws ParseException
   * @see
   */
  private static Collection getStatsUserFromResultSet(ResultSet rs)
      throws SQLException, ParseException {
    ArrayList myList = new ArrayList();
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
  private static Hashtable getHashtableFromQuery(String selectQuery)
      throws SQLException {
    SilverTrace.debug("silverStatisticsPeas",
        "SilverStatisticsPeasDAOAccessVolume.getHashtableFromQuery",
        "selectQuery=" + selectQuery);
    Statement stmt = null;
    ResultSet rs = null;
    Hashtable ht = null;
    Connection myCon = getConnection();

    try {
      stmt = myCon.createStatement();
      rs = stmt.executeQuery(selectQuery);
      ht = getHashtableFromResultset(rs);
    } finally {
      DBUtil.close(rs, stmt);
      freeConnection(myCon);
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
  private static Hashtable getHashtableFromResultset(ResultSet rs)
      throws SQLException {
    Hashtable result = new Hashtable();
    long count = 0;

    while (rs.next()) {
      count = rs.getLong(2);
      result.put(rs.getString(1), String.valueOf(count));
    }
    return result;
  }

  /**
   * donne les stats sur le nombre de publications
   * @return
   * @throws SQLException
   */
  public static Hashtable getStatsPublicationsVentil(String dateStat,
      String currentUserId, String filterIdGroup, String filterIdUser)
      throws SQLException {
    SilverTrace.info("silverStatisticsPeas",
        "SilverStatisticsPeasDAOAccessVolume.getStatsPublicationsVentil",
        "root.MSG_GEN_ENTER_METHOD");

    Hashtable resultat = new Hashtable(); // key=componentId, value=new
    // String[3] {tout, groupe, user}

    // Query Tout
    String selectQuery = "SELECT componentId, SUM(countVolume)"
        + " FROM SB_Stat_VolumeCumul" + " WHERE dateStat='" + dateStat + "'"
        + " GROUP BY dateStat, componentId"
        + " ORDER BY dateStat ASC, SUM(countVolume) DESC";

    Hashtable hashTout = getHashtableFromQuery(selectQuery);

    Iterator it = hashTout.keySet().iterator();
    String cmpId;
    AdminController myAdminController = new AdminController("");
    String[] values;
    ComponentInst compInst;
    String spaceId;
    String[] tabManageableSpaceIds;
    boolean ok = false;

    while (it.hasNext()) {
      ok = false;
      cmpId = (String) it.next();

      compInst = myAdminController.getComponentInst(cmpId);
      spaceId = compInst.getDomainFatherId(); // ex : WA123

      tabManageableSpaceIds = myAdminController
          .getUserManageableSpaceClientIds(currentUserId);

      // filtre les composants autorisés selon les droits de l'utilisateur
      // (Admin ou Gestionnaire d'espace)
      for (int i = 0; i < tabManageableSpaceIds.length; i++) {
        if (spaceId.equals(tabManageableSpaceIds[i])) {
          ok = true;
          break;
        }
      }

      if (ok) {
        values = new String[3];
        values[0] = (String) hashTout.get(cmpId);
        resultat.put(cmpId, values);
      }
    }

    // Query Groupe
    if (!filterIdGroup.equals("")) {

      // préremplit tout avec 0
      it = resultat.keySet().iterator();
      while (it.hasNext()) {
        cmpId = (String) it.next();
        values = (String[]) resultat.get(cmpId);
        values[1] = "0";
        resultat.put(cmpId, values);
      }

      selectQuery = "SELECT componentId, SUM(countVolume)"
          + " FROM SB_Stat_VolumeCumul, ST_Group_User_Rel"
          + " WHERE dateStat='" + dateStat + "'" + " AND groupId="
          + filterIdGroup
          + " AND SB_Stat_VolumeCumul.userId=ST_Group_User_Rel.userId"
          + " GROUP BY dateStat, componentId"
          + " ORDER BY dateStat ASC, SUM(countVolume) DESC";

      Hashtable hashGroupe = getHashtableFromQuery(selectQuery);

      it = hashGroupe.keySet().iterator();
      while (it.hasNext()) {
        cmpId = (String) it.next();

        values = (String[]) resultat.get(cmpId);
        if (values != null) {
          values[1] = (String) hashGroupe.get(cmpId);
          resultat.put(cmpId, values);
        }
      }
    }

    // Query User
    if (!filterIdUser.equals("")) {

      // préremplit tout avec 0
      it = resultat.keySet().iterator();
      while (it.hasNext()) {
        cmpId = (String) it.next();
        values = (String[]) resultat.get(cmpId);
        values[2] = "0";
        resultat.put(cmpId, values);
      }

      selectQuery = "SELECT componentId, SUM(countVolume)"
          + " FROM SB_Stat_VolumeCumul" + " WHERE dateStat='" + dateStat + "'"
          + " AND userId=" + filterIdUser + " GROUP BY dateStat, componentId"
          + " ORDER BY dateStat ASC, SUM(countVolume) DESC";

      Hashtable hashUser = getHashtableFromQuery(selectQuery);

      it = hashUser.keySet().iterator();
      while (it.hasNext()) {
        cmpId = (String) it.next();

        values = (String[]) resultat.get(cmpId);
        if (values != null) {
          values[2] = (String) hashUser.get(cmpId);
          resultat.put(cmpId, values);
        }
      }
    }

    return resultat;
  }

  private static Collection getYearsConnexion(ResultSet rs) throws SQLException {
    ArrayList myList = new ArrayList();
    String year = "";

    while (rs.next()) {
      if (!year.equals(rs.getString(1).substring(0, 4))) {
        year = rs.getString(1).substring(0, 4);
        myList.add(year);
      }
    }
    return myList;
  }

  private static Collection getYearsFromQuery(String selectQuery)
      throws SQLException {
    Statement stmt = null;
    ResultSet rs = null;
    Collection years = null;
    Connection myCon = getConnection();

    try {
      stmt = myCon.createStatement();
      rs = stmt.executeQuery(selectQuery);
      years = getYearsConnexion(rs);
    } finally {
      DBUtil.close(rs, stmt);
      freeConnection(myCon);
    }

    return years;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  private static Connection getConnection() {
    SilverTrace.info("silverStatisticsPeas",
        "SilverStatisticsPeasDAOAccesVolume.getConnection()",
        "root.MSG_GEN_ENTER_METHOD");

    Connection con = null;

    try {
      con = DBUtil.makeConnection(dbName);
    } catch (Exception e) {
      SilverTrace.error("silverStatisticsPeas",
          "SilverStatisticsPeasDAOAccesVolume.freeConnection()",
          "root.EX_CONNECTION_CLOSE_FAILED", "", e);
    }
    return con;
  }

  /**
   * Method declaration
   * @param con
   * @see
   */
  private static void freeConnection(Connection con) {
    SilverTrace.info("silverStatisticsPeas",
        "SilverStatisticsPeasDAOAccesVolume.freeConnection()",
        "root.MSG_GEN_ENTER_METHOD");

    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverTrace.error("silverStatisticsPeas",
            "SilverStatisticsPeasDAOAccesVolume.freeConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

}
