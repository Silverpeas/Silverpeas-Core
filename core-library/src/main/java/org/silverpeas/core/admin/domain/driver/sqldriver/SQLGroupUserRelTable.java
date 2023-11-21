/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.admin.domain.driver.sqldriver;

import org.silverpeas.core.admin.service.AdminException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A GroupTable object manages the DomainSQL_Group table.
 */
@SuppressWarnings("SqlNoDataSourceInspection")
public class SQLGroupUserRelTable {
  private static final String SELECT = "select ";
  private static final String FROM = " from ";
  private static final String WHERE = " where ";
  private static final String DELETE_FROM = "delete from ";
  private final SQLSettings drvSettings;

  SQLGroupUserRelTable(SQLSettings ds) {
    drvSettings = ds;
  }

  /**
   * Returns all the User ids which compose a group.
   */
  List<String> getDirectUserIdsOfGroup(Connection c, int groupId) throws AdminException {
    String theQuery =
        SELECT + drvSettings.getRelUIDColumnName() + FROM + drvSettings.getRelTableName() + WHERE +
            drvSettings.getRelGIDColumnName() + " = ?";
    return executeSelectionQuery(c, theQuery, groupId);
  }

  private List<String> executeSelectionQuery(final Connection c, final String theQuery,
      final int id) throws AdminException {
    final List<String> theResult = new ArrayList<>();
    try(final PreparedStatement statement = c.prepareStatement(theQuery)) {
      statement.setInt(1, id);
      try(final ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          theResult.add(Integer.toString(rs.getInt(1)));
        }
      }
    } catch (SQLException e) {
      throw new AdminException(e.getMessage(), e);
    }
    return theResult;
  }

  /**
   * Insert a new group row.
   */
  void createGroupUserRel(Connection c, int groupId, int userId) throws AdminException {
    final String theQuery =
        "insert into " + drvSettings.getRelTableName() + "(" + drvSettings.getRelGIDColumnName() +
            "," + drvSettings.getRelUIDColumnName() + ") " + " values (?,?)";
    executeModificationQuery(c, theQuery, groupId, userId);
  }

  /**
   * Insert a new group row.
   */
  void removeGroupUserRel(Connection c, int groupId, int userId) throws AdminException {
    final String theQuery = DELETE_FROM + drvSettings.getRelTableName() + WHERE +
        drvSettings.getRelGIDColumnName() + " = ?" + " and " + drvSettings.getRelUIDColumnName() +
        " = ?";
    executeModificationQuery(c, theQuery, groupId, userId);
  }

  private void executeModificationQuery(final Connection c, final String theQuery,
      final int groupId,
      final int userId) throws AdminException {
    try (final PreparedStatement statement = c.prepareStatement(theQuery)) {
      statement.setInt(1, groupId);
      statement.setInt(2, userId);
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new AdminException(e.getMessage(), e);
    }
  }

  /**
   * Insert a new group row.
   */
  void removeAllUserRel(Connection c, int userId) throws AdminException {
    final String theQuery = DELETE_FROM + drvSettings.getRelTableName() + WHERE +
        drvSettings.getRelUIDColumnName() + " = ?";
    executeModificationQuery(c, theQuery, userId);
  }

  /**
   * Insert a new group row.
   */
  void removeAllGroupRel(Connection c, int groupId) throws AdminException {
    final String theQuery = DELETE_FROM + drvSettings.getRelTableName() + WHERE +
        drvSettings.getRelGIDColumnName() + " = ?";
    executeModificationQuery(c, theQuery, groupId);
  }

  private void executeModificationQuery(final Connection c, final String theQuery, final int id)
      throws AdminException {
    try (final PreparedStatement statement = c.prepareStatement(theQuery)) {
      statement.setInt(1, id);
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new AdminException(e.getMessage(), e);
    }
  }
}
