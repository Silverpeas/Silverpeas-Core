/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/*
 * Aliaksei_Budnikau
 * Date: Oct 24, 2002
 */
package org.silverpeas.core.pdc.subscription.service;

import org.silverpeas.core.pdc.classification.Criteria;
import org.silverpeas.core.pdc.subscription.model.PdcSubscription;
import org.silverpeas.core.pdc.subscription.model.PdcSubscriptionRuntimeException;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.kernel.annotation.NonNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PdcSubscriptionDAO {

  private PdcSubscriptionDAO() {
    /* This utility class should not be instantiated */
  }

  private static final String PD_SUBSCRIPTION_TABLE_NAME = "SB_PDC_Subscription";
  private static final String PD_SUBSCRIPTION_AXIS_TABLE_NAME = "SB_PDC_Subscription_Axis";
  private static final String GET_SUBSCRIPTION_BY_USERID_QUERY =
      "SELECT id, name, ownerId " + " FROM " + PD_SUBSCRIPTION_TABLE_NAME + " WHERE ownerId = ? ";

  public static List<PdcSubscription> getPDCSubscriptionByUserId(@NonNull Connection conn,
      int userId)
      throws PdcSubscriptionRuntimeException, SQLException {
    Objects.requireNonNull(conn);
    if (userId < 0) {
      throw new PdcSubscriptionRuntimeException("No valid user specified for the PdC " +
                                                "subscription: " + userId);
    }
    List<PdcSubscription> result = new ArrayList<>();
    try (PreparedStatement prepStmt = conn.prepareStatement(GET_SUBSCRIPTION_BY_USERID_QUERY)) {
      prepStmt.setInt(1, userId);
      try (ResultSet rs = prepStmt.executeQuery()) {
        while (rs.next()) {
          PdcSubscription sc = getSubScFromRS(rs);
          sc.setPdcContext(getCriteriaBySubscriptionID(conn, sc.getId()));
          result.add(sc);
        }
      }
    }
    return result;
  }

  public static final String GET_ALL_SUBSCRIPTIONS_QUERY =
      "SELECT id, name, ownerId FROM " + PD_SUBSCRIPTION_TABLE_NAME;

  public static List<PdcSubscription> getAllPDCSubscriptions(@NonNull Connection conn)
      throws PdcSubscriptionRuntimeException, SQLException {
    Objects.requireNonNull(conn);
    List<PdcSubscription> result = new ArrayList<>();
    try (PreparedStatement prepStmt = conn.prepareStatement(GET_ALL_SUBSCRIPTIONS_QUERY);
         ResultSet rs = prepStmt.executeQuery()) {
      while (rs.next()) {
        PdcSubscription sc = getSubScFromRS(rs);
        sc.setPdcContext(getCriteriaBySubscriptionID(conn, sc.getId()));
        result.add(sc);
      }
    }
    return result;
  }

  private static PdcSubscription getSubScFromRS(ResultSet rs)
      throws SQLException, PdcSubscriptionRuntimeException {
    return new PdcSubscription(rs.getInt("id"), rs
        .getString("name"), null, rs.getInt("ownerId"));
  }

  private static final String GET_CRITERIA_BY_SC_ID_QUERY =
      "SELECT id, pdcSubscriptionId, axisId, val FROM "
      + PD_SUBSCRIPTION_AXIS_TABLE_NAME + " WHERE pdcSubscriptionId = ? ";

  private static List<Criteria> getCriteriaBySubscriptionID(@NonNull Connection conn,
      int scId) throws PdcSubscriptionRuntimeException, SQLException {
    List<Criteria> result = new ArrayList<>();
    if (scId < 0) {
      return result;
    }

    try (PreparedStatement prepStmt = conn.prepareStatement(GET_CRITERIA_BY_SC_ID_QUERY)) {
      prepStmt.setInt(1, scId);
      try (ResultSet rs = prepStmt.executeQuery()) {
        while (rs.next()) {
          Criteria sc = getSCFromRS(rs);
          result.add(sc);
        }
      }
    }
    return result;
  }

  private static Criteria getSCFromRS(ResultSet rs) throws SQLException,
      PdcSubscriptionRuntimeException {
    return new Criteria(rs.getInt("axisId"), rs.getString("val"));
  }

  public static final String GET_SUBSCRIPTION_BY_ID_QUERY =
      "SELECT id, name, ownerId FROM " + PD_SUBSCRIPTION_TABLE_NAME + " WHERE id = ? ";

  public static PdcSubscription getPdcSubscriptionById(@NonNull Connection conn, int id)
      throws PdcSubscriptionRuntimeException, SQLException {
    Objects.requireNonNull(conn);
    if (id < 0) {
      throw new PdcSubscriptionRuntimeException("The subscription identifier is invalid: " + id);
    }
    PdcSubscription result = null;
    try (PreparedStatement prepStmt = conn.prepareStatement(GET_SUBSCRIPTION_BY_ID_QUERY)) {
      prepStmt.setInt(1, id);
      try (ResultSet rs = prepStmt.executeQuery()) {
        if (rs.next()) {
          result = getSubScFromRS(rs);
          result.setPdcContext(getCriteriaBySubscriptionID(conn, result.getId()));
        }
      }
    }
    return result;
  }

  private static final String CREATE_PDC_SUBSCRIPTION_QUERY =
      "INSERT INTO " + PD_SUBSCRIPTION_TABLE_NAME + " (id, name, ownerId ) VALUES (?, ?, ?)";

  public static int createPDCSubscription(@NonNull Connection conn,
      @NonNull PdcSubscription subscription) throws PdcSubscriptionRuntimeException,
      SQLException {
    Objects.requireNonNull(conn);
    Objects.requireNonNull(subscription);

    int newId;
    try {
      newId = DBUtil.getNextId(PD_SUBSCRIPTION_TABLE_NAME, "id");
    } catch (Exception e) {
      throw new PdcSubscriptionRuntimeException(e);
    }

    try (PreparedStatement prepStmt = conn.prepareStatement(CREATE_PDC_SUBSCRIPTION_QUERY)) {
      prepStmt.setInt(1, newId);
      prepStmt.setString(2, subscription.getName());
      prepStmt.setInt(3, subscription.getOwnerId());

      int count = prepStmt.executeUpdate();
      if (count < 1) {
        throw new PdcSubscriptionRuntimeException("Fail to save subscription " + subscription);
      }

      List<? extends Criteria> ctx = subscription.getPdcContext();
      if (ctx != null && !ctx.isEmpty()) {
        createSearchCriteria(conn, ctx, newId);
      }
    }

    return newId;
  }

  public static final String CREATE_PDC_SEARCH_CRITERIA_QUERY =
      "INSERT INTO " + PD_SUBSCRIPTION_AXIS_TABLE_NAME +
      " (id, pdcSubscriptionId, axisId, val) VALUES (?, ?, ?, ?)";

  private static void createSearchCriteria(@NonNull Connection conn,
      @NonNull List<? extends Criteria> searchCriteria, int subscriptionId)
      throws PdcSubscriptionRuntimeException, SQLException {
    if (searchCriteria.isEmpty()) {
      return;
    }

    int newId;
    try (PreparedStatement prepStmt = conn.prepareStatement(CREATE_PDC_SEARCH_CRITERIA_QUERY)) {
      prepStmt.setInt(2, subscriptionId);
      for (Criteria sc : searchCriteria) {
        try {
          newId = DBUtil.getNextId(PD_SUBSCRIPTION_AXIS_TABLE_NAME, "id");
        } catch (Exception e) {
          throw new PdcSubscriptionRuntimeException(e);
        }
        prepStmt.setInt(1, newId);
        prepStmt.setInt(3, sc.getAxisId());
        prepStmt.setString(4, sc.getValue());

        int count = prepStmt.executeUpdate();
        if (count < 1) {
          throw new PdcSubscriptionRuntimeException("Fail to save criteria " + sc);
        }
      }
    }
  }

  public static final String UPDATE_PDC_SUBSCRIPTION_QUERY =
      "UPDATE " + PD_SUBSCRIPTION_TABLE_NAME + " SET name = ? , ownerId = ? WHERE id = ?";

  public static void updatePDCSubscription(@NonNull Connection conn,
      @NonNull PdcSubscription subscription) throws PdcSubscriptionRuntimeException,
      SQLException {
    Objects.requireNonNull(conn);
    Objects.requireNonNull(subscription);

    try (PreparedStatement prepStmt = conn.prepareStatement(UPDATE_PDC_SUBSCRIPTION_QUERY)) {
      prepStmt.setString(1, subscription.getName());
      prepStmt.setInt(2, subscription.getOwnerId());
      prepStmt.setInt(3, subscription.getId());

      int count = prepStmt.executeUpdate();
      if (count < 1) {
        throw new PdcSubscriptionRuntimeException("Fail to save subscription " + subscription);
      }

      List<? extends Criteria> ctx = subscription.getPdcContext();
      removeSearchCriteria(conn, subscription.getId());
      if (ctx != null && !ctx.isEmpty()) {
        createSearchCriteria(conn, ctx, subscription.getId());
      }
    }
  }

  public static final String REMOVE_SUBSCRIPTION_BY_ID_QUERY =
      "delete from " + PD_SUBSCRIPTION_TABLE_NAME + " where id = ? ";

  public static void removePDCSubscriptionById(@NonNull Connection conn, int id)
      throws PdcSubscriptionRuntimeException, SQLException {
    Objects.requireNonNull(conn);
    if (id < 0) {
      throw new PdcSubscriptionRuntimeException("Invalid subscription identifier: " + id);
    }

    try (PreparedStatement prepStmt = conn.prepareStatement(REMOVE_SUBSCRIPTION_BY_ID_QUERY)) {
      prepStmt.setInt(1, id);

      int count = prepStmt.executeUpdate();
      removeSearchCriteria(conn, id);

      if (count < 1) {
        throw new PdcSubscriptionRuntimeException("Fail to remove subscription " + id);
      }

    }
  }

  public static final String REMOVE_SCS_QUERY =
      "delete from " + PD_SUBSCRIPTION_AXIS_TABLE_NAME + " where pdcSubscriptionId = ? ";

  private static void removeSearchCriteria(Connection conn, int subscriptionId)
      throws PdcSubscriptionRuntimeException, SQLException {
    try (PreparedStatement prepStmt = conn.prepareStatement(REMOVE_SCS_QUERY)) {
      prepStmt.setInt(1, subscriptionId);
      prepStmt.executeUpdate();
    }
  }

  public static void removePDCSubscriptionById(@NonNull Connection conn,
      @NonNull int[] ids)
      throws PdcSubscriptionRuntimeException, SQLException {
    Objects.requireNonNull(conn);
    Objects.requireNonNull(ids);
    for (int id : ids) {
      removePDCSubscriptionById(conn, id);
    }
  }

  public static final String FIND_SUBSCRIPTION_BY_AXIS_QUERY =
      "SELECT pdcSubscriptionId FROM " + PD_SUBSCRIPTION_AXIS_TABLE_NAME + " WHERE axisId = ? ";

  public static List<PdcSubscription> getPDCSubscriptionByUsedAxis(
      @NonNull Connection conn, int axisId) throws PdcSubscriptionRuntimeException, SQLException {
    Objects.requireNonNull(conn);
    if (axisId < 0) {
      throw new PdcSubscriptionRuntimeException("Invalid PdC axis identifier: " + axisId);
    }
    List<PdcSubscription> result = new ArrayList<>();
    List<Integer> ids = new ArrayList<>();

    try (PreparedStatement prepStmt = conn.prepareStatement(FIND_SUBSCRIPTION_BY_AXIS_QUERY)) {
      prepStmt.setInt(1, axisId);
      try (ResultSet rs = prepStmt.executeQuery()) {
        while (rs.next()) {
          Integer subscriptionId = rs.getInt(1);
          if (!ids.contains(subscriptionId)) {
            ids.add(subscriptionId);
          }
        }
        for (Integer subscriptionId : ids) {
          PdcSubscription subscription = getPdcSubscriptionById(conn, subscriptionId);
          result.add(subscription);
        }
      }
    }

    return result;
  }
}
