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
/*
 * Aliaksei_Budnikau
 * Date: Oct 16, 2002
 */
package com.stratelia.silverpeas.versioning.ejb;

import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.Worker;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class WorkListDAO {

  public final static String workListTableName = "SB_Document_WorkList";

  public final static String ADD_WORKERS = "INSERT INTO "
      + workListTableName
      + " ( documentId , "
      + " userid, orderBy, writer, approval, instanceId, settype, saved, used, listtype ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";

  public final static String SAVE_WORKERS = "UPDATE " + workListTableName
      + " set saved = 1, listtype = ? "
      + " where instanceid = ? and documentId = ?";

  public final static String GET_WORKERS_ACCESS_LIST_USERS = "SELECT * from "
      + workListTableName
      + " where instanceid = ? and saved = 1 and settype='U' order by orderby";

  public final static String GET_WORKERS_ACCESS_LIST_GROUPS = "SELECT * from "
      + workListTableName
      + " where instanceid = ? and saved = 1 and settype='G' order by orderby";

  public final static String REMOVE_ALL_WORKERS = "delete from "
      + workListTableName + " where documentId = ? ";
  public final static String REMOVE_WORKERS_NOT_SAVED = "delete from "
      + workListTableName + " where documentId = ? and saved = 0";

  public final static String GET_WORKERS_QUERY = "SELECT a.documentId, a.userid, "
      + " a.orderBy, a.writer,  a.approval, a.instanceId, a.settype, a.saved, a.used, a.listtype"
      + " FROM "
      + workListTableName
      + " a WHERE a.documentId = ? order by orderby";

  public final static String GET_SAVED_LIST_TYPE = "SELECT listtype from "
      + workListTableName + " where instanceid = ? and saved = 1";

  public final static String REMOVE_SAVED_LIST = "delete from "
      + workListTableName + " where instanceid = ? and saved = 1 and used = 0";

  public final static String UPDATE_OLD_SAVED_LIST = "update "
      + workListTableName + " set saved=0 where instanceid = ?";

  /**
   * 
   * @param conn
   * @param workers
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static void addWorkers(Connection conn, ArrayList workers)
      throws SQLException, VersioningRuntimeException {
    if (conn == null) {
      throw new VersioningRuntimeException("WorkListDAO.addWorkers",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_NO_CONNECTION");
    }
    if (workers == null || ((workers != null) && workers.size() == 0)) {
      return;
    }
    PreparedStatement prepStmt = null;

    try {
      prepStmt = conn.prepareStatement(ADD_WORKERS);

      for (int i = 0; i < workers.size(); i++) {
        Worker worker = (Worker) workers.get(i);
        if (worker == null) {
          throw new VersioningRuntimeException("WorkListDAO.addWorkers",
              SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_NULL_VALUE_OBJECT");
        }
        prepStmt.setInt(1, worker.getDocumentId());
        prepStmt.setInt(2, worker.getId());
        prepStmt.setInt(3, worker.getOrder());

        String isWriter = (worker.isWriter() == true) ? "Y" : "N";
        String isApproval = (worker.isApproval() == true) ? "Y" : "N";
        prepStmt.setString(4, isWriter);
        prepStmt.setString(5, isApproval);
        prepStmt.setString(6, worker.getInstanceId());
        prepStmt.setString(7, worker.getType());
        prepStmt.setInt(8, (worker.isSaved()) ? 1 : 0);
        prepStmt.setInt(9, (worker.isUsed()) ? 1 : 0);
        prepStmt.setInt(10, (worker.getListType()));

        int rownum = prepStmt.executeUpdate();
        if (rownum < 1) {
          throw new VersioningRuntimeException("WorkListDAO.addWorkers",
              SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_RECORD_INSERTION_FAILED",
              worker);
        }
      }
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * 
   * @param conn
   * @param documentPK
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static void removeAllWorkers(Connection conn, DocumentPK documentPK)
      throws SQLException, VersioningRuntimeException {
    removeAllWorkers(conn, documentPK, false);
  }

  /**
   * 
   * @param conn
   * @param documentPK
   * @param keepSaved
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static void removeAllWorkers(Connection conn, DocumentPK documentPK,
      boolean keepSaved) throws SQLException, VersioningRuntimeException {
    if (conn == null) {
      throw new VersioningRuntimeException("WorkListDAO.removeAllWorkers",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_NO_CONNECTION");
    }
    if (documentPK == null) {
      throw new VersioningRuntimeException("WorkListDAO.removeAllWorkers",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_NULL_VALUE_OBJECT_OR_PK");
    }
    PreparedStatement prepStmt = null;

    try {
      if (keepSaved)
        prepStmt = conn.prepareStatement(REMOVE_WORKERS_NOT_SAVED);
      else
        prepStmt = conn.prepareStatement(REMOVE_ALL_WORKERS);

      try {
        prepStmt.setInt(1, Integer.parseInt(documentPK.getId()));
      } catch (NumberFormatException e) {
        throw new VersioningRuntimeException("WorkListDAO.removeAllWorkers",
            SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_WRONG_PK", documentPK
                .toString(), e);
      }

      prepStmt.executeUpdate();

    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * 
   * @param conn
   * @param documentPK
   * @return
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static ArrayList getWorkers(Connection conn, DocumentPK documentPK)
      throws SQLException, VersioningRuntimeException {
    if (conn == null) {
      throw new VersioningRuntimeException("WorkListDAO.getWorkers",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_NO_CONNECTION");
    }
    if (documentPK == null) {
      throw new VersioningRuntimeException("WorkListDAO.getWorkers",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_NULL_VALUE_OBJECT_OR_PK");
    }

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    ArrayList result = new ArrayList();

    try {
      prepStmt = conn.prepareStatement(GET_WORKERS_QUERY);
      try {
        prepStmt.setInt(1, Integer.parseInt(documentPK.getId()));
      } catch (NumberFormatException e) {
        throw new VersioningRuntimeException("WorkListDAO.getWorkers",
            SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_WRONG_PK", documentPK
                .toString(), e);
      }

      rs = prepStmt.executeQuery();

      while (rs.next()) {
        Worker worker = new Worker();

        worker.setDocumentId(rs.getInt(1));
        worker.setId(rs.getInt(2));
        worker.setOrder(rs.getInt(3));
        worker.setWriter(rs.getString(4).trim().equalsIgnoreCase("Y"));
        worker.setApproval(rs.getString(5).trim().equalsIgnoreCase("Y"));
        worker.setInstanceId(rs.getString(6));
        worker.setType(rs.getString(7));
        worker.setSaved(rs.getInt(8) == 1 ? true : false);
        worker.setUsed(rs.getInt(9) == 1 ? true : false);
        worker.setListType(rs.getInt(10));
        result.add(worker);
      }

    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return result;
  }

  /**
   * 
   * @param conn
   * @param componentId
   * @param documentId
   * @param listType
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static void saveWorkersAccessList(Connection conn, String componentId,
      String documentId, int listType) throws SQLException,
      VersioningRuntimeException {
    PreparedStatement prepStmt = null;
    try {
      prepStmt = conn.prepareStatement(SAVE_WORKERS);
      try {
        prepStmt.setInt(1, listType);
        prepStmt.setString(2, componentId);
        prepStmt.setInt(3, Integer.parseInt(documentId));
      } catch (NumberFormatException e) {
        throw new VersioningRuntimeException(
            "WorkListDAO.saveWorkersAccessList", SilverTrace.TRACE_LEVEL_DEBUG,
            "root.EX_WRONG_PK", componentId.toString(), e);
      }
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * 
   * @param con
   * @param componentId
   * @return * @return ArrayList of workers (users)
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static ArrayList getWorkersAccessListUsers(Connection con,
      String componentId) throws SQLException, VersioningRuntimeException {
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    ArrayList result = new ArrayList();

    try {
      prepStmt = con.prepareStatement(GET_WORKERS_ACCESS_LIST_USERS);
      try {
        prepStmt.setString(1, componentId);
      } catch (NumberFormatException e) {
        throw new VersioningRuntimeException(
            "WorkListDAO.getWorkersAccessListUsers",
            SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_WRONG_PK", componentId
                .toString(), e);
      }

      rs = prepStmt.executeQuery();

      while (rs.next()) {
        Worker worker = new Worker();
        worker.setDocumentId(rs.getInt(1));
        worker.setId(rs.getInt(2));
        worker.setOrder(rs.getInt(3));
        worker.setWriter(rs.getString(4).trim().equalsIgnoreCase("Y"));
        worker.setApproval(rs.getString(5).trim().equalsIgnoreCase("Y"));
        worker.setInstanceId(rs.getString(6));
        worker.setType(rs.getString(7));
        worker.setSaved(rs.getInt(8) == 1 ? true : false);
        worker.setUsed(rs.getInt(9) == 1 ? true : false);
        worker.setListType(rs.getInt(10));
        result.add(worker);
      }

    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return result;
  }

  /**
   * 
   * @param con
   * @param componentId
   * @return ArrayList of workers (groups)
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static ArrayList getWorkersAccessListGroups(Connection con,
      String componentId) throws SQLException, VersioningRuntimeException {
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    ArrayList result = new ArrayList();

    try {
      prepStmt = con.prepareStatement(GET_WORKERS_ACCESS_LIST_GROUPS);
      try {
        prepStmt.setString(1, componentId);
      } catch (NumberFormatException e) {
        throw new VersioningRuntimeException(
            "WorkListDAO.getWorkersAccessListUsers",
            SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_WRONG_PK", componentId
                .toString(), e);
      }

      rs = prepStmt.executeQuery();

      while (rs.next()) {
        Worker worker = new Worker();
        worker.setDocumentId(rs.getInt(1));
        worker.setId(rs.getInt(2));
        worker.setOrder(rs.getInt(3));
        worker.setWriter(rs.getString(4).trim().equalsIgnoreCase("Y"));
        worker.setApproval(rs.getString(5).trim().equalsIgnoreCase("Y"));
        worker.setInstanceId(rs.getString(6));
        worker.setType(rs.getString(7));
        worker.setSaved(rs.getInt(8) == 1 ? true : false);
        worker.setUsed(rs.getInt(9) == 1 ? true : false);
        worker.setListType(rs.getInt(10));
        result.add(worker);
      }

    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return result;
  }

  /**
   * Get saved list type
   * 
   * @param con
   * @param componentId
   * @return
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static int getSavedListType(Connection con, String componentId)
      throws SQLException, VersioningRuntimeException {
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    int listType = 0;
    try {
      prepStmt = con.prepareStatement(GET_SAVED_LIST_TYPE);
      try {
        prepStmt.setString(1, componentId);
      } catch (NumberFormatException e) {
        throw new VersioningRuntimeException("WorkListDAO.getSavedListType()",
            SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_WRONG_PK", componentId
                .toString(), e);
      }

      rs = prepStmt.executeQuery();
      if (rs.next())
        listType = rs.getInt(1);
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return listType;
  }

  public static void removeAccessList(Connection conn, String componentId)
      throws SQLException, VersioningRuntimeException {
    PreparedStatement prepStmt = null;
    try {
      // We update workers saved list used by documents
      prepStmt = conn.prepareStatement(UPDATE_OLD_SAVED_LIST);
      try {
        prepStmt.setString(1, componentId);
      } catch (NumberFormatException e) {
        throw new VersioningRuntimeException("WorkListDAO.removeAllWorkers",
            SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_WRONG_PK", componentId, e);
      }
      prepStmt.executeUpdate();

      // We remove workers saved list not used by documents
      prepStmt = conn.prepareStatement(REMOVE_SAVED_LIST);
      try {
        prepStmt.setString(1, componentId);
      } catch (NumberFormatException e) {
        throw new VersioningRuntimeException("WorkListDAO.removeAllWorkers",
            SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_WRONG_PK", componentId, e);
      }
      prepStmt.executeUpdate();

    } finally {
      DBUtil.close(prepStmt);
    }
  }

}
