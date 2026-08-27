/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.pdc.subscription.service;

import org.silverpeas.core.pdc.classification.Criteria;
import org.silverpeas.core.pdc.pdc.model.AxisValueCriterion;
import org.silverpeas.core.pdc.subscription.model.PdcSubscriptionPositionCriteria;
import org.silverpeas.core.pdc.subscription.model.PdcSubscriptionRuntimeException;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.kernel.annotation.NonNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The persistence of the position criteria on the PdC that can be aimed by a subscription. The
 * subscriptions themselves aren't persisted here but by the subscription API of Silverpeas.
 */
public class PdcSubscriptionDAO {

  private PdcSubscriptionDAO() {
    /* This utility class should not be instantiated */
  }

  private static final String RESOURCE_TABLE = "SB_PDC_Subscription";
  private static final String POSITION_TABLE = "SB_PDC_Subscription_Axis";
  private static final String RESOURCE_ID_CLAUSE = "id = ?";
  private static final String POSITION_OWNER_CLAUSE = "pdcSubscriptionId = ?";

  /**
   * Gets the position criteria on the PdC with the specified identifier.
   * @param con a connection to the data source.
   * @param id the unique identifier of position criteria on the PdC.
   * @return the fully valued position criteria or nothing if there is no such a set.
   * @throws SQLException if an error occurs while requesting the data source.
   */
  public static Optional<PdcSubscriptionPositionCriteria> getById(@NonNull Connection con,
      @NonNull String id) throws SQLException {
    Objects.requireNonNull(con);
    final int resourceId = asResourceId(id);
    final String name = JdbcSqlQuery.select("name")
        .from(RESOURCE_TABLE)
        .where(RESOURCE_ID_CLAUSE, resourceId)
        .executeUniqueWith(con, r -> r.getString(1));
    return name == null ? Optional.empty()
        : Optional.of(new PdcSubscriptionPositionCriteria(id, name, getPositionsOf(con, resourceId)));
  }

  /**
   * Gets all the position criteria on the PdC. The positions of all the sets are fetched in a
   * single request as this method is invoked each time a contribution is classified on the PdC.
   * @param con a connection to the data source.
   * @return a list of fully valued position criteria on the PdC.
   * @throws SQLException if an error occurs while requesting the data source.
   */
  public static List<PdcSubscriptionPositionCriteria> getAll(@NonNull Connection con) throws SQLException {
    Objects.requireNonNull(con);
    final Map<String, List<AxisValueCriterion>> positionsByResource = getAllPositions(con);
    return JdbcSqlQuery.select("id, name")
        .from(RESOURCE_TABLE)
        .executeWith(con, r -> {
          final String id = String.valueOf(r.getInt("id"));
          return new PdcSubscriptionPositionCriteria(id, r.getString("name"),
              positionsByResource.getOrDefault(id, List.of()));
        });
  }

  /**
   * Gets all the position criteria on the PdC having at least one position on the specified axis.
   * @param con a connection to the data source.
   * @param axisId the unique identifier of an axis of the PdC.
   * @return a list of fully valued position criteria on the PdC.
   * @throws SQLException if an error occurs while requesting the data source.
   */
  public static List<PdcSubscriptionPositionCriteria> getByUsedAxis(@NonNull Connection con, int axisId)
      throws SQLException {
    Objects.requireNonNull(con);
    if (axisId < 0) {
      throw new PdcSubscriptionRuntimeException("Invalid PdC axis identifier: " + axisId);
    }
    final List<String> ids = JdbcSqlQuery.select("DISTINCT pdcSubscriptionId")
        .from(POSITION_TABLE)
        .where("axisId = ?", axisId)
        .executeWith(con, r -> String.valueOf(r.getInt(1)));
    final List<PdcSubscriptionPositionCriteria> resources = new ArrayList<>(ids.size());
    for (final String id : ids) {
      getById(con, id).ifPresent(resources::add);
    }
    return resources;
  }

  /**
   * Saves the specified position criteria on the PdC.
   * @param con a connection to the data source.
   * @param name the name of the position criteria.
   * @param positions the positions on the axis of the PdC.
   * @return the unique identifier of the newly created position criteria.
   * @throws SQLException if an error occurs while requesting the data source.
   */
  public static String create(@NonNull Connection con, @NonNull String name,
      final List<? extends Criteria> positions) throws SQLException {
    Objects.requireNonNull(con);
    Objects.requireNonNull(name);
    final int newId = DBUtil.getNextId(RESOURCE_TABLE, "id");
    JdbcSqlQuery.insertInto(RESOURCE_TABLE)
        .withInsertParam("id", newId)
        .withInsertParam("name", name)
        .executeWith(con);
    createPositions(con, newId, positions);
    return String.valueOf(newId);
  }

  /**
   * Updates both the name and the positions of the specified position criteria on the PdC.
   * @param con a connection to the data source.
   * @param resource the position criteria to update.
   * @throws SQLException if an error occurs while requesting the data source.
   */
  public static void update(@NonNull Connection con, @NonNull PdcSubscriptionPositionCriteria resource)
      throws SQLException {
    Objects.requireNonNull(con);
    Objects.requireNonNull(resource);
    final int resourceId = asResourceId(resource.getId());
    final long count = JdbcSqlQuery.update(RESOURCE_TABLE)
        .withUpdateParam("name", resource.getName())
        .where(RESOURCE_ID_CLAUSE, resourceId)
        .executeWith(con);
    if (count < 1) {
      throw new PdcSubscriptionRuntimeException("Fail to save the position criteria " + resource);
    }
    deletePositions(con, resourceId);
    createPositions(con, resourceId, resource.getCriteria());
  }

  /**
   * Deletes the position criteria on the PdC with the specified identifier as well as its
   * positions.
   * @param con a connection to the data source.
   * @param id the unique identifier of position criteria on the PdC.
   * @throws SQLException if an error occurs while requesting the data source.
   */
  public static void deleteById(@NonNull Connection con, @NonNull String id) throws SQLException {
    Objects.requireNonNull(con);
    final int resourceId = asResourceId(id);
    deletePositions(con, resourceId);
    JdbcSqlQuery.deleteFrom(RESOURCE_TABLE)
        .where(RESOURCE_ID_CLAUSE, resourceId)
        .executeWith(con);
  }

  private static Map<String, List<AxisValueCriterion>> getAllPositions(final Connection con)
      throws SQLException {
    final Map<String, List<AxisValueCriterion>> positionsByResource = new HashMap<>();
    JdbcSqlQuery.select("pdcSubscriptionId, axisId, val")
        .from(POSITION_TABLE)
        .executeWith(con, r -> positionsByResource
            .computeIfAbsent(String.valueOf(r.getInt(1)), k -> new ArrayList<>())
            .add(new AxisValueCriterion(r.getInt(2), r.getString(3))));
    return positionsByResource;
  }

  private static List<AxisValueCriterion> getPositionsOf(final Connection con, final int resourceId)
      throws SQLException {
    return JdbcSqlQuery.select("axisId, val")
        .from(POSITION_TABLE)
        .where(POSITION_OWNER_CLAUSE, resourceId)
        .executeWith(con, r -> new AxisValueCriterion(r.getInt(1), r.getString(2)));
  }

  private static void createPositions(final Connection con, final int resourceId,
      final List<? extends Criteria> positions) throws SQLException {
    if (positions == null || positions.isEmpty()) {
      return;
    }
    for (final Criteria position : positions) {
      JdbcSqlQuery.insertInto(POSITION_TABLE)
          .withInsertParam("id", DBUtil.getNextId(POSITION_TABLE, "id"))
          .withInsertParam("pdcSubscriptionId", resourceId)
          .withInsertParam("axisId", position.getAxisId())
          .withInsertParam("val", position.getValue())
          .executeWith(con);
    }
  }

  private static void deletePositions(final Connection con, final int resourceId)
      throws SQLException {
    JdbcSqlQuery.deleteFrom(POSITION_TABLE)
        .where(POSITION_OWNER_CLAUSE, resourceId)
        .executeWith(con);
  }

  private static int asResourceId(final String id) {
    try {
      final int resourceId = Integer.parseInt(id);
      if (resourceId < 0) {
        throw new PdcSubscriptionRuntimeException("The identifier of the position criteria on the " +
            "PdC is invalid: " + id);
      }
      return resourceId;
    } catch (NumberFormatException e) {
      throw new PdcSubscriptionRuntimeException("The identifier of the position criteria on the " +
          "PdC isn't a number: " + id);
    }
  }
}
