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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

/**
 * Class declaration Get stat size directory data from database
 * @author
 */
public class SilverStatisticsPeasDAOVolumeServer {

  public static final int INDICE_DATE = 0;
  public static final int INDICE_LIB = 1;
  public static final int INDICE_SIZE = 2;

  // private static final String tableName = "SB_Stat_SizeDirCumul";
  private static final String DB_NAME = JNDINames.SILVERSTATISTICS_DATASOURCE;

  /**
   * donne les stats global pour l'enemble de tous les users cad 2 infos, la collection contient
   * donc un seul element
   * @param dateBegin
   * @param dateEnd
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<String[]> getStatsVolumeServer() throws SQLException {
    SilverTrace.info("silverStatisticsPeas",
        "SilverStatisticsPeasDAOVolumeServer.getStatsVolumeServer",
        "root.MSG_GEN_ENTER_METHOD");
    String selectQuery = " SELECT dateStat, fileDir, sizeDir"
        + " FROM SB_Stat_SizeDirCumul" + " ORDER BY dateStat";
    return getStatsVolumeServerFromQuery(selectQuery);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  private static Connection getConnection() {
    try {
      Connection con = DBUtil.makeConnection(DB_NAME);

      return con;
    } catch (Exception e) {
      throw new SilverStatisticsPeasRuntimeException(
          "SilverStatisticsPeasDAOVolumeServer.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED",
          "DbName=" + DB_NAME, e);
    }
  }

  /**
   * Method declaration
   * @param con
   * @see
   */
  private static void freeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverTrace.error("silverStatisticsPeas",
            "SilverStatisticsPeasDAOVolumeServer.freeConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  /**
   * Method declaration
   * @param selectQuery
   * @return
   * @throws SQLException
   * @see
   */
  private static Collection<String[]> getStatsVolumeServerFromQuery(String selectQuery)
      throws SQLException {
    SilverTrace.debug("silverStatisticsPeas",
        "SilverStatisticsPeasDAOVolumeServer.getStatsVolumeServerFromQuery",
        "selectQuery=" + selectQuery);
    Statement stmt = null;
    ResultSet rs = null;
    Collection<String[]> list = null;
    Connection myCon = getConnection();

    try {
      stmt = myCon.createStatement();
      rs = stmt.executeQuery(selectQuery);
      list = getStatsVolumeServerFromResultSet(rs);
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
   * @see
   */
  private static Collection<String[]> getStatsVolumeServerFromResultSet(ResultSet rs)
      throws SQLException {
    List<String[]> myList = new ArrayList<String[]>();
    String stat[] = null;

    while (rs.next()) {
      stat = new String[3];
      stat[INDICE_DATE] = rs.getString(1);
      stat[INDICE_LIB] = rs.getString(2);
      stat[INDICE_SIZE] = formatVolumeServer(rs.getLong(3));

      myList.add(stat);
    }
    return myList;
  }

  private static String formatVolumeServer(long size) {
    long kiloBytes = size / 1024;

    return new String(Long.toString(kiloBytes));
  }

  /**
   * donne les stats sur le nombre de pièces jointes non versionnées
   * @return
   * @throws SQLException
   */
  public static Hashtable<String, String[]> getStatsAttachmentsVentil(String currentUserId)
      throws SQLException {
    SilverTrace.info("silverStatisticsPeas",
        "SilverStatisticsPeasDAOVolumeServer.getStatsAttachmentsVentil",
        "root.MSG_GEN_ENTER_METHOD");

    Hashtable<String, String[]> resultat = new Hashtable<String, String[]>(); // key=componentId,
    // value=new
    // String[3] {nb, null, null}

    String selectQuery = "SELECT instanceId, COUNT(*) "
        + "FROM SB_Attachment_Attachment " + "GROUP BY instanceId "
        + "ORDER BY COUNT(*) DESC";

    Hashtable<String, String> intermedHash = getHashtableFromQuery(selectQuery);

    Iterator<String> it = intermedHash.keySet().iterator();
    String cmpId;
    AdminController myAdminController = new AdminController("");
    String[] values;

    while (it.hasNext()) {
      cmpId = it.next();

      // filtre les composants autorisés selon les droits de l'utilisateur
      // (Admin ou Gestionnaire d'espace)
      if (myAdminController.isComponentAvailable(cmpId, currentUserId)) {
        values = new String[3];
        values[0] = (String) intermedHash.get(cmpId);
        resultat.put(cmpId, values);
      }
    }

    return resultat;
  }

  /**
   * donne les stats sur le nombre de pièces jointes versionnées
   * @return
   * @throws SQLException
   */
  public static Hashtable<String, String[]> getStatsVersionnedAttachmentsVentil(
      String currentUserId) throws SQLException {
    SilverTrace
        .info(
            "silverStatisticsPeas",
            "SilverStatisticsPeasDAOVolumeServer.getStatsVersionnedAttachmentsVentil",
            "root.MSG_GEN_ENTER_METHOD");
    // key=componentId, value=new
    // String[3] {nb, null, null}
    Hashtable<String, String[]> resultat = new Hashtable<String, String[]>();

    String selectQuery = "SELECT v.instanceId, COUNT(*) "
        + "FROM SB_Version_Version v , SB_Version_Document d "
        + "WHERE v.documentId = d.documentId " + "GROUP BY v.instanceId "
        + "ORDER BY COUNT(*) DESC";

    Hashtable<String, String> intermedHash = getHashtableFromQuery(selectQuery);

    Iterator<String> it = intermedHash.keySet().iterator();
    String cmpId;
    AdminController myAdminController = new AdminController("");
    String[] values;
    boolean ok = false;
    ComponentInst compInst;
    String spaceId;
    String[] tabManageableSpaceIds;

    while (it.hasNext()) {
      ok = false;
      cmpId = it.next();

      compInst = myAdminController.getComponentInst(cmpId);
      spaceId = compInst.getDomainFatherId(); // ex : WA123

      tabManageableSpaceIds = myAdminController.getUserManageableSpaceClientIds(currentUserId);

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
        values[0] = (String) intermedHash.get(cmpId);
        resultat.put(cmpId, values);
      }
    }

    return resultat;
  }

  /**
   * donne les stats sur la taille des pièces jointes non versionnées
   * @return
   * @throws SQLException
   */
  public static Hashtable<String, String[]> getStatsAttachmentsSizeVentil(String currentUserId)
      throws SQLException {
    SilverTrace.info("silverStatisticsPeas",
        "SilverStatisticsPeasDAOVolumeServer.getStatsAttachmentsSizeVentil",
        "root.MSG_GEN_ENTER_METHOD");

    // key = componentId,
    // value = new String[3] {nb, null, null}
    Hashtable<String, String[]> resultat = new Hashtable<String, String[]>();

    String selectQuery = "SELECT instanceId, SUM(CAST(attachmentSize AS decimal)) "
        + "FROM SB_Attachment_Attachment "
        + "GROUP BY instanceId "
        + "ORDER BY SUM(CAST(attachmentSize AS decimal)) DESC";

    Hashtable<String, String> intermedHash = getHashtableFromQuery(selectQuery);

    Iterator<String> it = intermedHash.keySet().iterator();
    String cmpId;
    AdminController myAdminController = new AdminController("");
    String[] values;
    boolean ok = false;
    ComponentInst compInst;
    String spaceId;
    String[] tabManageableSpaceIds;

    while (it.hasNext()) {
      ok = false;
      cmpId = (String) it.next();

      compInst = myAdminController.getComponentInst(cmpId);
      spaceId = compInst.getDomainFatherId(); // ex : WA123

      tabManageableSpaceIds = myAdminController.getUserManageableSpaceClientIds(currentUserId);

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
        values[0] = (String) intermedHash.get(cmpId);
        resultat.put(cmpId, values);
      }
    }

    return resultat;
  }

  /**
   * donne les stats sur la taille des pièces jointes versionnées
   * @return
   * @throws SQLException
   */
  public static Hashtable<String, String[]> getStatsVersionnedAttachmentsSizeVentil(
      String currentUserId) throws SQLException {
    SilverTrace
        .info(
            "silverStatisticsPeas",
            "SilverStatisticsPeasDAOVolumeServer.getStatsVersionnedAttachmentsSizeVentil",
            "root.MSG_GEN_ENTER_METHOD");

    Hashtable<String, String[]> resultat = new Hashtable<String, String[]>(); // key=componentId,
    // value=new
    // String[3] {nb, null, null}

    String selectQuery = "SELECT v.instanceId, SUM(versionSize) "
        + "FROM SB_Version_Version v , SB_Version_Document d "
        + "WHERE v.documentId = d.documentId " + "GROUP BY v.instanceId "
        + "ORDER BY SUM(versionSize) DESC";

    Hashtable<String, String> intermedHash = getHashtableFromQuery(selectQuery);

    Iterator<String> it = intermedHash.keySet().iterator();
    String cmpId;
    AdminController myAdminController = new AdminController("");
    String[] values;
    boolean ok = false;
    ComponentInst compInst;
    String spaceId;
    String[] tabManageableSpaceIds;

    while (it.hasNext()) {
      ok = false;
      cmpId = it.next();

      compInst = myAdminController.getComponentInst(cmpId);
      spaceId = compInst.getDomainFatherId(); // ex : WA123

      tabManageableSpaceIds = myAdminController.getUserManageableSpaceClientIds(currentUserId);

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
        values[0] = (String) intermedHash.get(cmpId);
        resultat.put(cmpId, values);
      }
    }

    return resultat;
  }

  /**
   * Retrieve a hashtable of result from select database query
   * @param selectQuery the select SQL query
   * @return
   * @throws SQLException
   */
  private static Hashtable<String, String> getHashtableFromQuery(String selectQuery)
      throws SQLException {
    SilverTrace.debug("silverStatisticsPeas",
        "SilverStatisticsPeasDAOVolumeServer.getHashtableFromQuery",
        "selectQuery=" + selectQuery);
    Statement stmt = null;
    ResultSet rs = null;
    Hashtable<String, String> ht = null;
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
   * Transform a ResultSet into a Hashtable
   * @param rs the ResultSet
   * @return a hashtable from database query result set
   * @throws SQLException
   */
  private static Hashtable<String, String> getHashtableFromResultset(ResultSet rs)
      throws SQLException {
    Hashtable<String, String> result = new Hashtable<String, String>();
    long count = 0;

    while (rs.next()) {
      count = rs.getLong(2);
      result.put(rs.getString(1), String.valueOf(count));
    }
    return result;
  }

}