/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.admin.persistence;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccessLevelTable extends Table<AccessLevelRow> {

  public AccessLevelTable(OrganizationSchema organization) {
    super(organization, "ST_AccessLevel");
  }

  /**
   * Fetch the current access level row from a resultSet.
   * @param rs
   * @return the current access level row from a resultSet.
   * @throws SQLException
   */
  protected AccessLevelRow fetchAccessLevel(ResultSet rs) throws SQLException {
    AccessLevelRow a = new AccessLevelRow();
    a.id = rs.getString("id");
    a.name = rs.getString("name");
    return a;
  }

  /**
   * Returns all the Access levels.
   * @return all the Access levels.
   * @throws AdminPersistenceException
   */
  public AccessLevelRow[] getAllAccessLevels() throws AdminPersistenceException {
    return getRows(SELECT_ALL_ACCESSLEVELS).toArray(
        new AccessLevelRow[getRows(SELECT_ALL_ACCESSLEVELS).size()]);
  }

  static final private String SELECT_ALL_ACCESSLEVELS = "SELECT id, name FROM ST_AccessLevel";

  /**
   * Returns the Access level whith the given id.
   * @param id
   * @return the Access level whith the given id.
   * @throws AdminPersistenceException
   */
  public AccessLevelRow getAccessLevel(String id) throws AdminPersistenceException {
    return getUniqueRow(SELECT_ACCESSLEVEL_BY_ID, id);
  }

  static final private String SELECT_ACCESSLEVEL_BY_ID =
      "SELECT id, name FROM  ST_AccessLevel WHERE id = ?";

  /**
   * Fetch the current accessLevel row from a resultSet.
   * @param rs
   * @return the current accessLevel row from a resultSet.
   * @throws SQLException
   */
  @Override
  protected AccessLevelRow fetchRow(ResultSet rs) throws SQLException {
    return fetchAccessLevel(rs);
  }

  /**
   * update a accessLevel
   */
  @Override
  protected void prepareUpdate(String updateQuery, PreparedStatement update, AccessLevelRow row) {
    // not implemented
  }

  /**
   * insert a accessLevel
   */
  @Override
  protected void prepareInsert(String insertQuery, PreparedStatement insert, AccessLevelRow row) {
    // not implemented
  }
}