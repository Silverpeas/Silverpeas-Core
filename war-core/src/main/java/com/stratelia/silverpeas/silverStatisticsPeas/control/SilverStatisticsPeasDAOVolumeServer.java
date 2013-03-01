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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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
import java.util.List;
import java.util.Map;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import java.util.HashMap;

/**
 * Class declaration Get stat size directory data from database
 * <p/>
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
    Connection myCon = null;
    try {
      myCon = DBUtil.makeConnection(DB_NAME);
      stmt = myCon.createStatement();
      rs = stmt.executeQuery(selectQuery);
      return getStatsVolumeServerFromResultSet(rs);
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
    return Long.toString(kiloBytes);
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
    // value=new String[3] {nb, null, null}

    String selectQuery = "SELECT instanceId, COUNT(*) "
        + "FROM SB_Attachment_Attachment " + "GROUP BY instanceId "
        + "ORDER BY COUNT(*) DESC";

    Map<String, String> intermedHash = getHashtableFromQuery(selectQuery);
    AdminController myAdminController = new AdminController("");
    boolean isAdmin = UserDetail.getById(currentUserId).isAccessAdmin();
    for (Map.Entry<String, String> entry : intermedHash.entrySet()) {
      String cmpId = entry.getKey();
      // filtre les composants autorisés selon les droits de l'utilisateur
      // (Admin ou Gestionnaire d'espace)
      if (isAdmin || myAdminController.isComponentAvailable(cmpId, currentUserId)) {
        String[] values = new String[3];
        values[0] = entry.getValue();
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
    SilverTrace.info(
        "silverStatisticsPeas",
        "SilverStatisticsPeasDAOVolumeServer.getStatsVersionnedAttachmentsVentil",
        "root.MSG_GEN_ENTER_METHOD");
    
    String selectQuery = "SELECT v.instanceId, COUNT(*) "
        + "FROM SB_Version_Version v , SB_Version_Document d "
        + "WHERE v.documentId = d.documentId " + "GROUP BY v.instanceId "
        + "ORDER BY COUNT(*) DESC";

    return extractResults(currentUserId, selectQuery);
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

    String selectQuery = "SELECT instanceId, SUM(CAST(attachmentSize AS decimal)) "
        + "FROM SB_Attachment_Attachment "
        + "GROUP BY instanceId "
        + "ORDER BY SUM(CAST(attachmentSize AS decimal)) DESC";

    return extractResults(currentUserId, selectQuery);
  }

  /**
   * donne les stats sur la taille des pièces jointes versionnées
   * @return
   * @throws SQLException
   */
  public static Hashtable<String, String[]> getStatsVersionnedAttachmentsSizeVentil(
      String currentUserId) throws SQLException {
    SilverTrace.info("silverStatisticsPeas",
        "SilverStatisticsPeasDAOVolumeServer.getStatsVersionnedAttachmentsSizeVentil",
        "root.MSG_GEN_ENTER_METHOD");

    String selectQuery = "SELECT v.instanceId, SUM(versionSize) "
        + "FROM SB_Version_Version v , SB_Version_Document d "
        + "WHERE v.documentId = d.documentId " + "GROUP BY v.instanceId "
        + "ORDER BY SUM(versionSize) DESC";

    return extractResults(currentUserId, selectQuery);
  }

  private static Hashtable<String, String[]> extractResults(String currentUserId,
      String selectQuery) throws SQLException {
    Hashtable<String, String[]> resultat = new Hashtable<String, String[]>(); // key=componentId,
    // value=new String[3] {nb, null, null}
    Map<String, String> intermedHash = getHashtableFromQuery(selectQuery);
    AdminController myAdminController = new AdminController("");
    boolean isAdmin = UserDetail.getById(currentUserId).isAccessAdmin();
    String[] tabManageableSpaceIds = myAdminController.getUserManageableSpaceClientIds(
        currentUserId);
    for (Map.Entry<String, String> entry : intermedHash.entrySet()) {
      boolean ok = false;
      String cmpId = entry.getKey();

      ComponentInst compInst = myAdminController.getComponentInst(cmpId);
      String spaceId = compInst.getDomainFatherId(); // ex : WA123

      // filtre les composants autorisés selon les droits de l'utilisateur
      // (Admin ou Gestionnaire d'espace)
      if (isAdmin) {
        ok = true;
      } else {
        for (String tabManageableSpaceId : tabManageableSpaceIds) {
          if (spaceId.equals(tabManageableSpaceId)) {
            ok = true;
            break;
          }
        }
      }
      if (ok) {
        String[] values = new String[3];
        values[0] = entry.getValue();
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
  private static Map<String, String> getHashtableFromQuery(String selectQuery)
      throws SQLException {
    SilverTrace.debug("silverStatisticsPeas",
        "SilverStatisticsPeasDAOVolumeServer.getHashtableFromQuery",
        "selectQuery=" + selectQuery);
    Statement stmt = null;
    ResultSet rs = null;
    Map<String, String> ht = null;
    Connection myCon = null;
    try {
      myCon = DBUtil.makeConnection(DB_NAME);
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
   * Transform a ResultSet into a Hashtable
   * @param rs the ResultSet
   * @return a hashtable from database query result set
   * @throws SQLException
   */
  private static Map<String, String> getHashtableFromResultset(ResultSet rs)
      throws SQLException {
    Map<String, String> result = new HashMap<String, String>();
    long count = 0;

    while (rs.next()) {
      count = rs.getLong(2);
      result.put(rs.getString(1), String.valueOf(count));
    }
    return result;
  }
}