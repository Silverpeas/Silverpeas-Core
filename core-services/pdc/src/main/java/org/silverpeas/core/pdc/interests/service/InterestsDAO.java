/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Aliaksei_Budnikau
 * Date: Oct 14, 2002
 */
package org.silverpeas.core.pdc.interests.service;

import org.silverpeas.core.pdc.interests.model.Interests;
import org.silverpeas.core.pdc.classification.Criteria;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class InterestsDAO {

  /**
   * Interest_Center_Axes table name
   */
  public final static String INTERESTS_AXES_TABLE_NAME = "SB_Interests_Axis";
  /**
   * Date format pattern constatnt. This patters is used in db operations
   */
  public final static String DATE_FORMAT = "yyyy/MM/dd";
  /**
   * getInterestsByUserId sql query constant
   */
  public final static String GET_IC_BY_USERID_QUERY =
      "SELECT a.id, a.name, a.criteria, a.workSpaceId, a.peasId, "
      + " a.authorId, a.afterDate, a.beforeDate, a.ownerId FROM SB_Interests a WHERE a.ownerId = ? ";

  /**
   * @param con
   * @param userid
   * @return a list of <code>Interests</code>s by user id provided
   * @throws SQLException
   * @throws InterestsDAOException
   */
  public static List<Interests> getInterestsByUserID(Connection con, int userid) throws SQLException,
      InterestsDAOException {
    if (con == null) {
      throw new InterestsDAOException("InterestsDAO.getInterestsByUserId",
          "root.EX_CONNECTION_OPEN_FAILED");
    }

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(GET_IC_BY_USERID_QUERY);
      prepStmt.setInt(1, userid);

      List<Interests> result = new ArrayList<Interests>();
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        Interests ic = getInterestsfromResultSet(rs, con);
        result.add(ic);
      }
      return result;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }
  /**
   * getInterestsByPK sql query constant
   */
  public final static String GET_IC_BY_PK_QUERY =
      "SELECT id, name, criteria, workSpaceId, peasId, "
      + "authorId, afterDate, beforeDate, ownerId FROM SB_Interests WHERE id = ? ";

  /**
   * @param icID <code>Interests</code> id
   * @return Interests by its id
   */
  public static Interests getInterestsByPK(Connection con, int icID)
      throws SQLException, InterestsDAOException {
    if (con == null) {
      throw new InterestsDAOException("InterestsDAO.getInterestsByPK",
          "root.EX_CONNECTION_OPEN_FAILED");
    }
    PreparedStatement prepStmt = null;
    Interests result = null;
    ResultSet rs = null;

    try {
      prepStmt = con.prepareStatement(GET_IC_BY_PK_QUERY);
      prepStmt.setInt(1, icID);

      rs = prepStmt.executeQuery();

      if (rs.next()) {
        result = getInterestsfromResultSet(rs, con);
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
  private static Interests getInterestsfromResultSet(ResultSet rs,
      java.sql.Connection con) throws SQLException, InterestsDAOException {
    SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);

    Interests ic = new Interests();
    ic.setId(rs.getInt(1));
    ic.setName(rs.getString(2));
    ic.setQuery(rs.getString(3));
    ic.setWorkSpaceID(rs.getString(4));
    ic.setPeasID(rs.getString(5));
    ic.setAuthorID(rs.getString(6));

    try {
      String afterDate = rs.getString(7);
      String beforeDate = (rs.getString(8));

      if (afterDate != null && !afterDate.isEmpty()) {
        ic.setAfterDate(formatter.parse(afterDate));
      } else {
        ic.setAfterDate(null);
      }
      if (beforeDate == null || beforeDate.isEmpty()) {
        ic.setBeforeDate(null);
      } else {
        ic.setBeforeDate(formatter.parse(beforeDate));
      }
    } catch (ParseException e) {
      throw new InterestsDAOException("InterestsDAO.getInterestsfromResultSet",
          "root.EX_CANT_PARSE_DATE", e);
    }

    ic.setOwnerID(rs.getInt(9));

    List<Criteria> pdcContext = loadPdcContext(con, ic.getId());
    ic.setPdcContext(pdcContext);
    return ic;
  }
  /**
   * createInterests sql query constant
   */
  public final static String CREATE_IC_QUERY =
      "INSERT  INTO SB_Interests (id, name, criteria, workSpaceId, peasId, authorId, "
      + "afterDate, beforeDate, ownerId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ";

  /**
   * @return id of <code>Interests</code> created
   */
  public static int saveInterests(Connection con, Interests interests)
      throws SQLException, InterestsDAOException {
    if (con == null) {
      throw new InterestsDAOException("InterestsDAO.createInterests",
          "root.EX_CONNECTION_OPEN_FAILED");
    }
    if (interests == null) {
      throw new InterestsDAOException("InterestsDAO.createInterests",
          "Pdc.CANNOT_CREATE_INTEREST_CENTER");
    }
    PreparedStatement prepStmt = null;
    SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);

    int newId = -1;
    try {
      newId = DBUtil.getNextId("SB_Interests", "Id");
    } catch (Exception e) {
      throw new InterestsDAOException("InterestsDAO.createInterests",
          "root.EX_PK_GENERATION_FAILED", "SB_Interests", e);
    }

    try {
      prepStmt = con.prepareStatement(CREATE_IC_QUERY);
      prepStmt.setInt(1, newId);
      prepStmt.setString(2, interests.getName());
      prepStmt.setString(3, interests.getQuery());
      prepStmt.setString(4, interests.getWorkSpaceID());
      prepStmt.setString(5, interests.getPeasID());
      prepStmt.setString(6, interests.getAuthorID());
      if (interests.getAfterDate() != null) {
        prepStmt.setString(7, formatter.format(interests.getAfterDate()));
      } else {
        prepStmt.setString(7, "");
      }
      if (interests.getBeforeDate() != null) {
        prepStmt.setString(8, formatter.format(interests.getBeforeDate()));
      } else {
        prepStmt.setString(8, "");
      }

      prepStmt.setInt(9, interests.getOwnerID());

      int result = prepStmt.executeUpdate();
      if (result < 1) {
        throw new InterestsDAOException("InterestsDAO.createInterests",
            "Pdc.CANNOT_CREATE_INTEREST_CENTER", "ID: " + newId
            + ". DataObject: " + interests);
      }

      List<? extends Criteria> list = interests.getPdcContext();
      if (list != null && !list.isEmpty()) {
        appendPdcContext(con, list, newId);
      }

      interests.setId(newId);
    } finally {
      DBUtil.close(prepStmt);
    }

    return newId;
  }
  /**
   * updateInterests sql query constant
   */
  public final static String UPDATE_IC_QUERY =
      "UPDATE SB_Interests SET name = ?, criteria = ?, workSpaceId = ?, peasId = ?, "
      + "authorId = ?, afterDate = ?, beforeDate = ?, ownerId = ? WHERE id = ?";

  /**
   * perform updates of provided Interests
   */
  public static void updateInterests(Connection con, Interests interests)
      throws SQLException, InterestsDAOException {
    if (con == null) {
      throw new InterestsDAOException("InterestsDAO.updateInterests",
          "root.EX_CONNECTION_OPEN_FAILED");
    }
    PreparedStatement prepStmt = null;
    SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);

    try {
      prepStmt = con.prepareStatement(UPDATE_IC_QUERY);
      prepStmt.setInt(9, interests.getId());
      prepStmt.setString(1, interests.getName());
      prepStmt.setString(2, interests.getQuery());
      prepStmt.setString(3, interests.getWorkSpaceID());
      prepStmt.setString(4, interests.getPeasID());
      prepStmt.setString(5, interests.getAuthorID());
      if (interests.getAfterDate() != null) {
        prepStmt.setString(6, formatter.format(interests.getAfterDate()));
      } else {
        prepStmt.setString(6, "");
      }
      if (interests.getBeforeDate() != null) {
        prepStmt.setString(7, formatter.format(interests.getBeforeDate()));
      } else {
        prepStmt.setString(7, "");
      }
      prepStmt.setInt(8, interests.getOwnerID());

      int result = prepStmt.executeUpdate();
      if (result < 1) {
        throw new InterestsDAOException("InterestsDAO.updateInterests",
            "Pdc.CANNOT_UPDATE_INTEREST_CENTER", interests
            .toString());
      }

      List<? extends Criteria> list = interests.getPdcContext();
      if (list != null && !list.isEmpty()) {
        updatePdcContext(con, list, interests.getId());
      }

    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * @param removePKList id's of <code>Interests</code>s to be deleted
   */
  public static void removeInterestsByPK(Connection con, List<Integer> removePKList)
      throws SQLException, InterestsDAOException {
    for (Integer pk : removePKList) {
      removeInterestsByPK(con, pk);
    }
  }
  /**
   * removeInterestsById sql query constant
   */
  public final static String REMOVE_IC_BY_PKS_LIST_QUERY =
      "DELETE FROM SB_Interests WHERE id = ?";

  /**
   * @param removeID an id of <code>Interests</code> to be deleted
   */
  public static void removeInterestsByPK(Connection con, int removeID)
      throws SQLException, InterestsDAOException {
    if (con == null) {
      throw new InterestsDAOException("InterestsDAO.removeInterestsById",
          "root.EX_CONNECTION_OPEN_FAILED");
    }
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(REMOVE_IC_BY_PKS_LIST_QUERY);
      prepStmt.setInt(1, removeID);

      int result = prepStmt.executeUpdate();
      if (result < 1) {
        throw new InterestsDAOException("InterestsDAO.removeInterestsById",
            "Pdc.CANNOT_DELETE_INTEREST_CENTER", "ID=" + removeID);
      }

      removePdcContext(con, removeID);

    } finally {
      DBUtil.close(prepStmt);
    }
  }
  /**
   * loadPdcContext sql query constant
   */
  public final static String LOAD_PDC_PK_QUERY = "SELECT a.axisId, a.value "
      + "  FROM " + INTERESTS_AXES_TABLE_NAME + " a WHERE a.icId = ? ";

  /**
   * @return list of SearchCriteria
   */
  public static List<Criteria> loadPdcContext(Connection con, int icId)
      throws SQLException, InterestsDAOException {
    if (con == null) {
      throw new InterestsDAOException("InterestsDAO.loadPdcContext",
          "root.EX_CONNECTION_OPEN_FAILED");
    }
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<Criteria> result = null;

    try {
      prepStmt = con.prepareStatement(LOAD_PDC_PK_QUERY);
      prepStmt.setInt(1, icId);

      result = new ArrayList<Criteria>();
      rs = prepStmt.executeQuery();

      while (rs.next()) {
        int axisId = rs.getInt(1);
        String value = rs.getString(2);
        Criteria sc = new Criteria(axisId, value);

        result.add(sc);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return result;
  }
  /**
   * appendPdcContext sql query constant
   */
  public final static String CREATE_PDC_CONTEXT_QUERY = "INSERT  INTO "
      + INTERESTS_AXES_TABLE_NAME + " ( "
      + " id, icId, axisId, value) VALUES (?, ?, ?, ?) ";

  /**
   * Appends a list of SearchCriteria to the Interests by id
   *
   * @param icId the interests id
   */
  public static int[] appendPdcContext(Connection con, List<? extends Criteria> pdcContext,
      int icId) throws SQLException, InterestsDAOException {
    if (con == null) {
      throw new InterestsDAOException("InterestsDAO.appendPdcContext",
          "root.EX_CONNECTION_OPEN_FAILED");
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
        Criteria criteria = pdcContext.get(i);

        int newId = -1;
        try {
          newId = DBUtil.getNextId(INTERESTS_AXES_TABLE_NAME, "Id");
        } catch (Exception e) {
          throw new InterestsDAOException("InterestsDAO.appendPdcContext",
              "root.EX_PK_GENERATION_FAILED", INTERESTS_AXES_TABLE_NAME, e);
        }

        prepStmt.setInt(1, newId);
        prepStmt.setInt(2, icId);
        prepStmt.setInt(3, criteria.getAxisId());
        prepStmt.setString(4, criteria.getValue());

        int result = prepStmt.executeUpdate();
        if (result < 1) {
          throw new InterestsDAOException("InterestsDAO.appendPdcContext",
              "Pdc.CANNOT_CREATE_INTEREST_CENTER_PDC", "Criteria: "
              + criteria + ". ID = " + newId + ". For Interests ID = "
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
   * Updates SearchCriterias list for interests by id
   *
   * @param icId interests id
   */
  public static void updatePdcContext(Connection con, List<? extends Criteria> list, int icId)
      throws SQLException, InterestsDAOException {
    if (con == null) {
      throw new InterestsDAOException("InterestsDAO.updatePdcContext",
          "root.EX_CONNECTION_OPEN_FAILED");
    }
    if (icId < 0) {
      throw new InterestsDAOException("InterestsDAO.updatePdcContext",
          "root.EX_INVALID_ARG", String.valueOf(icId));
    }
    removePdcContext(con, icId);
    appendPdcContext(con, list, icId);
  }
  /**
   * removePdcContext sql query constant
   */
  public final static String REMOVE_IC_CONTEXT_QUERY = "delete from "
      + INTERESTS_AXES_TABLE_NAME + " where icId = ?";

  /**
   * Remove all SearchCriterias for provided interests id
   *
   * @param icId the interests id
   */
  public static void removePdcContext(Connection con, int icId)
      throws SQLException, InterestsDAOException {
    if (con == null) {
      throw new InterestsDAOException("InterestsDAO.removePdcContext",
          "root.EX_CONNECTION_OPEN_FAILED");
    }
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(REMOVE_IC_CONTEXT_QUERY);
      prepStmt.setInt(1, icId);

      int result = prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }
}
