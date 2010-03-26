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
// TODO : reporter dans CVS (done)
/*
 * Aliaksei_Budnikau
 * Date: Oct 14, 2002
 */
package com.silverpeas.interestCenter.ejb;

import com.silverpeas.interestCenter.model.InterestCenter;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.silverpeas.pdc.model.SearchCriteria;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

import java.util.ArrayList;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class InterestCenterDAO {

  /** DB Interest_Center table name */
  public final static String ICENTER_TABLE_NAME = "SB_Interest_Center";
  /** Interest_Center_Axes table name */
  public final static String ICENTER_AXES_TABLE_NAME = "SB_Interest_Center_Axis";
  /** Date format pattern constatnt. This patters is used in db operations */
  public final static String DATE_FORMAT = "yyyy/MM/dd";

  /** getICByUserID sql query constant */
  public final static String GET_IC_BY_USERID_QUERY =
      "SELECT a.id, a.name, a.criteria, a.workSpaceId, a.peasId, "
          + " a.authorId, a.afterDate, a.beforeDate, a.ownerId FROM "
          + ICENTER_TABLE_NAME + " a WHERE a.ownerId = ? ";

  /**
   * @return a list of <code>InterestCenter</code>s by user id provided
   */
  public static ArrayList getICByUserID(Connection con, int userid)
      throws SQLException, DAOException {
    if (con == null) {
      throw new DAOException("InterestCenterDAO.getICByUserID",
          "InterestCenter.EX_NO_CONNECTION");
    }

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    ArrayList result = null;

    try {
      prepStmt = con.prepareStatement(GET_IC_BY_USERID_QUERY);
      prepStmt.setInt(1, userid);

      result = new ArrayList();
      rs = prepStmt.executeQuery();

      while (rs.next()) {
        InterestCenter ic = getICformRS(rs, con);

        result.add(ic);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return result;
  }

  /** getICByPK sql query constant */
  public final static String GET_IC_BY_PK_QUERY =
      "SELECT a.id, a.name, a.criteria, a.workSpaceId, a.peasId, "
          + " a.authorId, a.afterDate, a.beforeDate, a.ownerId FROM "
          + ICENTER_TABLE_NAME + " a WHERE a.id = ? ";

  /**
   * @param icPK <code>InterestCenter</code> id
   * @return InterestCenter by its id
   */
  public static InterestCenter getICByPK(Connection con, int icID)
      throws SQLException, DAOException {
    if (con == null) {
      throw new DAOException("InterestCenterDAO.getICByPK",
          "InterestCenter.EX_NO_CONNECTION");
    }
    PreparedStatement prepStmt = null;
    InterestCenter result = null;
    ResultSet rs = null;

    try {
      prepStmt = con.prepareStatement(GET_IC_BY_PK_QUERY);
      prepStmt.setInt(1, icID);

      rs = prepStmt.executeQuery();

      if (rs.next()) {
        result = getICformRS(rs, con);
      }

    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return result;
  }

  /**
   * @param rs ResultSet to Create <code>InterestCentre</code> from. ResultSet shoud be already
   * positioned
   */
  private static InterestCenter getICformRS(ResultSet rs,
      java.sql.Connection con) throws SQLException, DAOException {
    SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);

    InterestCenter ic = new InterestCenter();
    ic.setId(rs.getInt(1));
    ic.setName(rs.getString(2));
    ic.setQuery(rs.getString(3));
    ic.setWorkSpaceID(rs.getString(4));
    ic.setPeasID(rs.getString(5));
    ic.setAuthorID(rs.getString(6));

    try {
      String afterDate = rs.getString(7);
      String beforeDate = (rs.getString(8));

      if (afterDate != null && !afterDate.equals("")) {
        ic.setAfterDate(formatter.parse(afterDate));
      } else {
        ic.setAfterDate(null);
      }
      if (beforeDate != null && !beforeDate.equals("")) {
        ic.setBeforeDate(formatter.parse(beforeDate));
      } else {
        ic.setBeforeDate(null);
      }
    } catch (ParseException e) {
      throw new DAOException("InterestCenterDAO.getICByUserID",
          "InterestCenter.EX_CANNOT_PARSE_DATE", e);
    }

    ic.setOwnerID(rs.getInt(9));

    ArrayList pdcContext = loadPdcContext(con, ic.getId());
    ic.setPdcContext(pdcContext);
    return ic;
  }

  /** createIC sql query constant */
  public final static String CREATE_IC_QUERY =
      "INSERT  INTO "
          +
          ICENTER_TABLE_NAME
          +
          " (id, name, criteria, "
          +
          " workSpaceId, peasId, authorId, afterDate, beforeDate, ownerId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ";

  /**
   * @return id of <code>InterestCenter</code> created
   */
  public static int createIC(Connection con, InterestCenter interestCenter)
      throws SQLException, DAOException {
    if (con == null) {
      throw new DAOException("InterestCenterDAO.createIC",
          "InterestCenter.EX_NO_CONNECTION");
    }
    if (interestCenter == null) {
      throw new DAOException("InterestCenterDAO.createIC",
          "InterestCenter.EX_CANNOT_INSERT_NULL_IC");
    }
    PreparedStatement prepStmt = null;
    SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);

    int newId = -1;
    try {
      newId = DBUtil.getNextId(ICENTER_TABLE_NAME, "Id");
    } catch (Exception e) {
      throw new DAOException("InterestCenterDAO.createIC",
          "root.EX_PK_GENERATION_FAILED", ICENTER_TABLE_NAME, e);
    }

    try {
      prepStmt = con.prepareStatement(CREATE_IC_QUERY);
      prepStmt.setInt(1, newId);
      prepStmt.setString(2, interestCenter.getName());
      prepStmt.setString(3, interestCenter.getQuery());
      prepStmt.setString(4, interestCenter.getWorkSpaceID());
      prepStmt.setString(5, interestCenter.getPeasID());
      prepStmt.setString(6, interestCenter.getAuthorID());
      if (interestCenter.getAfterDate() != null) {
        prepStmt.setString(7, formatter.format(interestCenter.getAfterDate()));
      } else {
        prepStmt.setString(7, "");
      }
      if (interestCenter.getBeforeDate() != null) {
        prepStmt.setString(8, formatter.format(interestCenter.getBeforeDate()));
      } else {
        prepStmt.setString(8, "");
      }

      prepStmt.setInt(9, interestCenter.getOwnerID());

      int result = prepStmt.executeUpdate();
      if (result < 1) {
        throw new DAOException("InterestCenterDAO.createIC",
            "InterestCenter.WRONG_CREATED_ROW_NUMBER", "ID: " + newId
            + ". DataObject: " + interestCenter);
      }

      ArrayList list = interestCenter.getPdcContext();
      if (list != null && list.size() != 0) {
        appendPdcContext(con, list, newId);
      }

      interestCenter.setId(newId);
    } finally {
      DBUtil.close(prepStmt);
    }

    return newId;
  }

  /** updateIC sql query constant */
  public final static String UPDATE_IC_QUERY =
      "UPDATE  "
          +
          ICENTER_TABLE_NAME
          +
          " SET name = ?, criteria = ?, "
          +
          " workSpaceId = ?, peasId = ?, authorId = ?, afterDate = ?, beforeDate = ?, ownerId = ? WHERE id = ?";

  /**
   * perform updates of provided InterestCenter
   */
  public static void updateIC(Connection con, InterestCenter interestCenter)
      throws SQLException, DAOException {
    if (con == null) {
      throw new DAOException("InterestCenterDAO.updateIC",
          "InterestCenter.EX_NO_CONNECTION");
    }
    PreparedStatement prepStmt = null;
    SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);

    try {
      prepStmt = con.prepareStatement(UPDATE_IC_QUERY);
      prepStmt.setInt(9, interestCenter.getId());
      prepStmt.setString(1, interestCenter.getName());
      prepStmt.setString(2, interestCenter.getQuery());
      prepStmt.setString(3, interestCenter.getWorkSpaceID());
      prepStmt.setString(4, interestCenter.getPeasID());
      prepStmt.setString(5, interestCenter.getAuthorID());
      if (interestCenter.getAfterDate() != null) {
        prepStmt.setString(6, formatter.format(interestCenter.getAfterDate()));
      } else {
        prepStmt.setString(6, "");
      }
      if (interestCenter.getBeforeDate() != null) {
        prepStmt.setString(7, formatter.format(interestCenter.getBeforeDate()));
      } else {
        prepStmt.setString(7, "");
      }
      prepStmt.setInt(8, interestCenter.getOwnerID());

      int result = prepStmt.executeUpdate();
      if (result < 1) {
        throw new DAOException("InterestCenterDAO.updateIC",
            "InterestCenter.WRONG_UPDATED_ROW_NUMBER", interestCenter
            .toString());
      }

      ArrayList list = interestCenter.getPdcContext();
      if (list != null && list.size() != 0) {
        updatePdcContext(con, list, interestCenter.getId());
      }

    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * @param pks ArrayList of <code>java.lang.Integer</code> - id's of <code>InterestCenter</code>s
   * to be deleted
   */
  public static void removeICByPK(Connection con, ArrayList removePKList)
      throws SQLException, DAOException {
    for (int i = 0; i < removePKList.size(); i++) {
      Integer pk = (Integer) removePKList.get(i);
      removeICByPK(con, pk.intValue());
    }
  }

  /** removeICByPK sql query constant */
  public final static String REMOVE_IC_BY_PKS_LIST_QUERY = "delete from "
      + ICENTER_TABLE_NAME + " where id = ?";

  /**
   * @param pk an id of <code>InterestCenter</code> to be deleted
   */
  public static void removeICByPK(Connection con, int removeID)
      throws SQLException, DAOException {
    if (con == null) {
      throw new DAOException("InterestCenterDAO.removeICByPK",
          "InterestCenter.EX_NO_CONNECTION");
    }
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(REMOVE_IC_BY_PKS_LIST_QUERY);
      prepStmt.setInt(1, removeID);

      int result = prepStmt.executeUpdate();
      if (result < 1) {
        throw new DAOException("InterestCenterDAO.removeICByPK",
            "InterestCenter.WRONG_UPDATED_ROW_NUMBER", "ID=" + removeID);
      }

      removePdcContext(con, removeID);

    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /** loadPdcContext sql query constant */
  public final static String LOAD_PDC_PK_QUERY = "SELECT a.axisId, a.value "
      + "  FROM " + ICENTER_AXES_TABLE_NAME + " a WHERE a.icId = ? ";

  /**
   * @return list of SearchCriteria
   */
  public static ArrayList loadPdcContext(Connection con, int icId)
      throws SQLException, DAOException {
    if (con == null) {
      throw new DAOException("InterestCenterDAO.loadPdcContext",
          "InterestCenter.EX_NO_CONNECTION");
    }
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    ArrayList result = null;

    try {
      prepStmt = con.prepareStatement(LOAD_PDC_PK_QUERY);
      prepStmt.setInt(1, icId);

      result = new ArrayList();
      rs = prepStmt.executeQuery();

      while (rs.next()) {
        int axisId = rs.getInt(1);
        String value = rs.getString(2);
        SearchCriteria sc = new SearchCriteria(axisId, value);

        result.add(sc);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return result;
  }

  /** appendPdcContext sql query constant */
  public final static String CREATE_PDC_CONTEXT_QUERY = "INSERT  INTO "
      + ICENTER_AXES_TABLE_NAME + " ( "
      + " id, icId, axisId, value) VALUES (?, ?, ?, ?) ";

  /**
   * Appends a list of SearchCriteria to the InterestCenter by InterestCenterID
   * @param icId InterestCenterID
   */
  public static int[] appendPdcContext(Connection con, ArrayList pdcContext,
      int icId) throws SQLException, DAOException {
    if (con == null) {
      throw new DAOException("InterestCenterDAO.appendPdcContext",
          "InterestCenter.EX_NO_CONNECTION");
    }
    if (pdcContext == null) {
      return null;
    }
    PreparedStatement prepStmt = null;
    int[] generatedPKs = null;

    try {
      prepStmt = con.prepareStatement(CREATE_PDC_CONTEXT_QUERY);
      generatedPKs = new int[pdcContext.size()];

      for (int i = 0; i < pdcContext.size(); i++) {
        SearchCriteria criteria = (SearchCriteria) pdcContext.get(i);

        int newId = -1;
        try {
          newId = DBUtil.getNextId(ICENTER_AXES_TABLE_NAME, "Id");
        } catch (Exception e) {
          throw new DAOException("InterestCenterDAO.appendPdcContext",
              "root.EX_PK_GENERATION_FAILED", ICENTER_AXES_TABLE_NAME, e);
        }

        prepStmt.setInt(1, newId);
        prepStmt.setInt(2, icId);
        prepStmt.setInt(3, criteria.getAxisId());
        prepStmt.setString(4, criteria.getValue());

        int result = prepStmt.executeUpdate();
        if (result < 1) {
          throw new DAOException("InterestCenterDAO.appendPdcContext",
              "InterestCenter.WRONG_CREATED_ROW_NUMBER", "Criteria: "
              + criteria + ". ID = " + newId + ". For InterestCenter ID = "
              + icId);
        }
        generatedPKs[i] = newId;
      }

    } finally {
      DBUtil.close(prepStmt);
    }

    return generatedPKs;
  }

  /**
   * Updates SearchCriterias list for interestCenter by InterestCenterID
   * @param icId InterestCenterID
   */
  public static void updatePdcContext(Connection con, ArrayList list, int icId)
      throws SQLException, DAOException {
    if (con == null) {
      throw new DAOException("InterestCenterDAO.updatePdcContext",
          "InterestCenter.EX_NO_CONNECTION");
    }
    if (icId < 0) {
      throw new DAOException("InterestCenterDAO.updatePdcContext",
          "InterestCenter.INCORRECT_ID_NUMBER", String.valueOf(icId));
    }
    removePdcContext(con, icId);
    appendPdcContext(con, list, icId);
  }

  /** removePdcContext sql query constant */
  public final static String REMOVE_IC_CONTEXT_QUERY = "delete from "
      + ICENTER_AXES_TABLE_NAME + " where icId = ?";

  /**
   * Remove all SearchCriterias for provided interestCenterID
   * @param icId InterestCenterID
   */
  public static void removePdcContext(Connection con, int icId)
      throws SQLException, DAOException {
    if (con == null) {
      throw new DAOException("InterestCenterDAO.removePdcContext",
          "InterestCenter.EX_NO_CONNECTION");
    }
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(REMOVE_IC_CONTEXT_QUERY);
      prepStmt.setInt(1, icId);

      int result = prepStmt.executeUpdate();

      SilverTrace.debug("InterestCenter", "InterestCenter.removePdcContext",
          "Number of deleted rows = ", String.valueOf(result));

    } finally {
      DBUtil.close(prepStmt);
    }
  }

}