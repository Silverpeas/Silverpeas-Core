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
 * Date: Oct 24, 2002
 */
package org.silverpeas.core.pdc.subscription.service;

import org.silverpeas.core.pdc.subscription.model.PdcSubscription;
import org.silverpeas.core.pdc.subscription.model.PdcSubscriptionRuntimeException;
import org.silverpeas.core.pdc.classification.Criteria;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.exception.SilverpeasException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PdcSubscriptionDAO {

  public final static String PDC_SUBSRIPTION_TABLE_NAME = "SB_PDC_Subscription";
  public final static String PDC_SUBSRIPTION_AXIS_TABLE_NAME = "SB_PDC_Subscription_Axis";
  public static final String GET_SUBSCRIPTION_BY_USERID_QUERY = "SELECT id, name, ownerId "
      + " FROM " + PDC_SUBSRIPTION_TABLE_NAME + " WHERE ownerId = ? ";

  public static List<PdcSubscription> getPDCSubscriptionByUserId(Connection conn, int userId)
      throws PdcSubscriptionRuntimeException, SQLException {
    if (conn == null) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionDAO.getPDCSubscriptionByUserId",
          SilverpeasException.ERROR, "root.EX_NO_CONNECTION");
    }
    if (userId < 0) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionDAO.getPDCSubscriptionByUserId",
          SilverpeasException.ERROR, "root.EX_NULL_VALUE_OBJECT_OR_PK");
    }
    List<PdcSubscription> result = new ArrayList<PdcSubscription>();
    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      prepStmt = conn.prepareStatement(GET_SUBSCRIPTION_BY_USERID_QUERY);
      prepStmt.setInt(1, userId);

      rs = prepStmt.executeQuery();

      while (rs.next()) {
        PdcSubscription sc = getSubScFromRS(rs);
        sc.setPdcContext(getCriteriasBySubscriptionID(conn, sc.getId()));
        result.add(sc);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return result;
  }
  public static final String GET_ALL_SUBSCRIPTIONS_QUERY = "SELECT id, name, ownerId FROM "
      + PDC_SUBSRIPTION_TABLE_NAME;

  public static List<PdcSubscription> getAllPDCSubscriptions(Connection conn)
      throws PdcSubscriptionRuntimeException, SQLException {
    if (conn == null) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionDAO.getAllPDCSubscriptions",
          SilverpeasException.ERROR, "root.EX_NO_CONNECTION");
    }
    List<PdcSubscription> result = new ArrayList<PdcSubscription>();
    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      prepStmt = conn.prepareStatement(GET_ALL_SUBSCRIPTIONS_QUERY);

      rs = prepStmt.executeQuery();

      while (rs.next()) {
        PdcSubscription sc = getSubScFromRS(rs);
        sc.setPdcContext(getCriteriasBySubscriptionID(conn, sc.getId()));
        result.add(sc);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return result;
  }

  private static PdcSubscription getSubScFromRS(ResultSet rs)
      throws SQLException, PdcSubscriptionRuntimeException {
    PdcSubscription result = new PdcSubscription(rs.getInt("id"), rs
        .getString("name"), null, rs.getInt("ownerId"));
    return result;
  }
  public final static String GET_CRITERIAS_BY_SC_ID_QUERY =
      "SELECT id, pdcSubscriptionId, axisId, value FROM "
      + PDC_SUBSRIPTION_AXIS_TABLE_NAME + " WHERE pdcSubscriptionId = ? ";

  private static List<Criteria> getCriteriasBySubscriptionID(Connection conn,
      int scId) throws PdcSubscriptionRuntimeException, SQLException {
    if (conn == null) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionDAO.getCriteriasBySubscriptionID",
          SilverpeasException.ERROR, "root.EX_NO_CONNECTION");
    }
    List<Criteria> result = new ArrayList<Criteria>();
    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    if (scId < 0) {
      return result;
    }

    try {
      prepStmt = conn.prepareStatement(GET_CRITERIAS_BY_SC_ID_QUERY);
      prepStmt.setInt(1, scId);

      rs = prepStmt.executeQuery();

      while (rs.next()) {
        Criteria sc = getSCFromRS(rs);
        result.add(sc);
      }

    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return result;
  }

  private static Criteria getSCFromRS(ResultSet rs) throws SQLException,
      PdcSubscriptionRuntimeException {
    Criteria result = new Criteria(rs.getInt("axisId"), rs.getString("value"));
    return result;
  }
  public static final String GET_SUBSCRIPTION_BY_ID_QUERY = "SELECT id, name, ownerId FROM "
      + PDC_SUBSRIPTION_TABLE_NAME + " WHERE id = ? ";

  public static PdcSubscription getPDCSubsriptionById(Connection conn, int id)
      throws PdcSubscriptionRuntimeException, SQLException {
    if (conn == null) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionDAO.PDCSubsriptionById",
          SilverpeasException.ERROR, "root.EX_NO_CONNECTION");
    }
    if (id < 0) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionDAO.PDCSubsriptionById",
          SilverpeasException.ERROR, "root.EX_WRONG_PK");
    }
    PdcSubscription result = null;
    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      prepStmt = conn.prepareStatement(GET_SUBSCRIPTION_BY_ID_QUERY);
      prepStmt.setInt(1, id);

      rs = prepStmt.executeQuery();

      if (rs.next()) {
        result = getSubScFromRS(rs);
        result
            .setPdcContext(getCriteriasBySubscriptionID(conn, result.getId()));
      }

    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return result;
  }
  public final static String CREATE_PDCSUBSCR_QUERY = "INSERT INTO "
      + PDC_SUBSRIPTION_TABLE_NAME + " (id, name, ownerId ) VALUES (?, ?, ?)";

  public static int createPDCSubscription(Connection conn,
      PdcSubscription subscription) throws PdcSubscriptionRuntimeException,
      SQLException {
    if (conn == null) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionDAO.createPDCSubscription",
          SilverpeasException.ERROR, "root.EX_NO_CONNECTION");
    }
    if (subscription == null) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionDAO.createPDCSubscription",
          SilverpeasException.ERROR, "root.EX_NULL_VALUE_OBJECT_OR_PK");
    }
    PreparedStatement prepStmt = null;
    int newId = -1;

    try {
      newId = DBUtil.getNextId(PDC_SUBSRIPTION_TABLE_NAME, "id");
    } catch (Exception e) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionDAO.createPDCSubscription",
          SilverpeasException.ERROR, "root.EX_GET_NEXTID_FAILED",
          PDC_SUBSRIPTION_TABLE_NAME, e);
    }

    try {
      prepStmt = conn.prepareStatement(CREATE_PDCSUBSCR_QUERY);

      prepStmt.setInt(1, newId);
      prepStmt.setString(2, subscription.getName());
      prepStmt.setInt(3, subscription.getOwnerId());

      int rownum = prepStmt.executeUpdate();
      if (rownum < 1) {
        throw new PdcSubscriptionRuntimeException(
            "PdcSubscriptionDAO.createPDCSubscription",
            SilverpeasException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
            subscription);
      }

      List<? extends Criteria> ctx = subscription.getPdcContext();
      if (ctx != null && !ctx.isEmpty()) {
        createSearchCriterias(conn, ctx, newId);
      }

    } finally {
      DBUtil.close(prepStmt);
    }

    return newId;
  }
  public final static String CREATE_PDC_SEARCHCRITERIA_QUERY = "INSERT INTO "
      + PDC_SUBSRIPTION_AXIS_TABLE_NAME
      + " (id, pdcSubscriptionId, axisId, value) VALUES (?, ?, ?, ?)";

  private static void createSearchCriterias(Connection conn,
      List<? extends Criteria> searchCriterias, int subscriptionId)
      throws PdcSubscriptionRuntimeException, SQLException {
    if (conn == null) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionDAO.createSearchCriterias",
          SilverpeasException.ERROR, "root.EX_NO_CONNECTION");
    }
    if (searchCriterias == null) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionDAO.createSearchCriterias",
          SilverpeasException.ERROR, "root.EX_NULL_VALUE_OBJECT_OR_PK");
    }
    if (searchCriterias.isEmpty()) {
      return;
    }

    PreparedStatement prepStmt = null;
    int newId = -1;

    try {
      prepStmt = conn.prepareStatement(CREATE_PDC_SEARCHCRITERIA_QUERY);

      for (Criteria sc : searchCriterias) {
        try {
          newId = DBUtil.getNextId(PDC_SUBSRIPTION_AXIS_TABLE_NAME, "id");
        } catch (Exception e) {
          throw new PdcSubscriptionRuntimeException(
              "PdcSubscriptionDAO.createSearchCriterias",
              SilverpeasException.ERROR, "root.EX_GET_NEXTID_FAILED",
              PDC_SUBSRIPTION_AXIS_TABLE_NAME, e);
        }

        prepStmt.setInt(1, newId);
        prepStmt.setInt(2, subscriptionId);
        prepStmt.setInt(3, sc.getAxisId());
        prepStmt.setString(4, sc.getValue());

        int rownum = prepStmt.executeUpdate();
        if (rownum < 1) {
          throw new PdcSubscriptionRuntimeException(
              "PdcSubscriptionDAO.createSearchCriterias",
              SilverpeasException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
              sc);
        }
      }
    } finally {
      DBUtil.close(prepStmt);
    }
  }
  public final static String UPDATE_PDC_SUBSCR_QUERY = "UPDATE "
      + PDC_SUBSRIPTION_TABLE_NAME
      + " SET name = ? , ownerId = ? WHERE id = ? ";

  public static void updatePDCSubscription(Connection conn,
      PdcSubscription subscription) throws PdcSubscriptionRuntimeException,
      SQLException {
    if (conn == null) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionDAO.updatePDCSubscription",
          SilverpeasException.ERROR, "root.EX_NO_CONNECTION");
    }
    if (subscription == null) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionDAO.updatePDCSubscription",
          SilverpeasException.ERROR, "root.EX_NULL_VALUE_OBJECT_OR_PK");
    }
    PreparedStatement prepStmt = null;

    try {
      prepStmt = conn.prepareStatement(UPDATE_PDC_SUBSCR_QUERY);
      prepStmt.setString(1, subscription.getName());
      prepStmt.setInt(2, subscription.getOwnerId());
      prepStmt.setInt(3, subscription.getId());

      int rownum = prepStmt.executeUpdate();
      if (rownum < 1) {
        throw new PdcSubscriptionRuntimeException(
            "PdcSubscriptionDAO.updatePDCSubscription",
            SilverpeasException.ERROR, "root.EX_RECORD_UPDATE_FAILED",
            subscription);
      }

      List<? extends Criteria> ctx = subscription.getPdcContext();
      removeSearchCriterias(conn, subscription.getId());
      if (ctx != null && !ctx.isEmpty()) {
        createSearchCriterias(conn, ctx, subscription.getId());
      }

    } finally {
      DBUtil.close(prepStmt);
    }
  }
  public final static String REMOVE_SUBSCR_BYID_QUERY = "delete from "
      + PDC_SUBSRIPTION_TABLE_NAME + " where id = ? ";

  public static void removePDCSubscriptionById(Connection conn, int id)
      throws PdcSubscriptionRuntimeException, SQLException {
    if (conn == null) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionDAO.removePDCSubscriptionById",
          SilverpeasException.ERROR, "root.EX_NO_CONNECTION");
    }
    if (id < 0) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionDAO.removePDCSubscriptionById",
          SilverpeasException.ERROR, "root.EX_WRONG_PK");
    }
    PreparedStatement prepStmt = null;

    try {
      prepStmt = conn.prepareStatement(REMOVE_SUBSCR_BYID_QUERY);

      prepStmt.setInt(1, id);

      int rownum = prepStmt.executeUpdate();
      removeSearchCriterias(conn, id);

      if (rownum < 1) {
        throw new PdcSubscriptionRuntimeException(
            "PdcSubscriptionDAO.removePDCSubscriptionById",
            SilverpeasException.ERROR, "root.EX_RECORD_NOTFOUND", String
            .valueOf(id));
      }

    } finally {
      DBUtil.close(prepStmt);
    }
  }
  public final static String REMOVE_SCS_QUERY = "delete from "
      + PDC_SUBSRIPTION_AXIS_TABLE_NAME + " where pdcSubscriptionId = ? ";

  private static void removeSearchCriterias(Connection conn, int subscrID)
      throws PdcSubscriptionRuntimeException, SQLException {
    if (conn == null) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionDAO.removeSearchCriterias",
          SilverpeasException.ERROR, "root.EX_NO_CONNECTION");
    }
    if (subscrID < 0) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionDAO.removeSearchCriterias",
          SilverpeasException.ERROR, "root.EX_WRONG_PK");
    }
    PreparedStatement prepStmt = null;

    try {
      prepStmt = conn.prepareStatement(REMOVE_SCS_QUERY);
      prepStmt.setInt(1, subscrID);

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void removePDCSubscriptionById(Connection conn, int[] ids)
      throws PdcSubscriptionRuntimeException, SQLException {
    if (conn == null) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionDAO.removePDCSubscriptionById",
          SilverpeasException.ERROR, "root.EX_NO_CONNECTION");
    }
    if (ids == null) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionDAO.removePDCSubscriptionById",
          SilverpeasException.ERROR, "root.EX_NULL_VALUE_OBJECT_OR_PK");
    }

    for (int id : ids) {
      removePDCSubscriptionById(conn, id);
    }
  }
  public final static String FIND_SUBSCRIPTION_BY_AXIS_QUERY = "SELECT pdcSubscriptionId FROM "
      + PDC_SUBSRIPTION_AXIS_TABLE_NAME + " WHERE axisId = ? ";

  public static List<PdcSubscription> getPDCSubscriptionByUsedAxis(Connection conn,
      int axisId) throws PdcSubscriptionRuntimeException, SQLException {
    if (conn == null) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionDAO.getPDCSubscriptionByUsedAxis",
          SilverpeasException.ERROR, "root.EX_NO_CONNECTION");
    }
    if (axisId < 0) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionDAO.getPDCSubscriptionByUsedAxis",
          SilverpeasException.ERROR, "root.EX_NULL_VALUE_OBJECT_OR_PK");
    }
    List<PdcSubscription> result = new ArrayList<PdcSubscription>();

    List<Integer> ids = new ArrayList<Integer>();

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      prepStmt = conn.prepareStatement(FIND_SUBSCRIPTION_BY_AXIS_QUERY);
      prepStmt.setInt(1, axisId);
      rs = prepStmt.executeQuery();

      while (rs.next()) {
        Integer subscrId = rs.getInt(1);
        if (!ids.contains(subscrId)) {
          ids.add(subscrId);
        }
      }

      for (Integer subscrId : ids) {
        PdcSubscription subscription = getPDCSubsriptionById(conn, subscrId);
        result.add(subscription);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return result;
  }
}
