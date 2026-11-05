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
package org.silverpeas.core.admin.component.dao;

import java.sql.Connection;
import java.sql.SQLException;
import org.silverpeas.core.annotation.Repository;

/** JDBC capacity checks using the caller's transaction and an existing space row as a mutex. */
@Repository
public class ComponentInstanceCapacityDAO {
  public int lockAndCount(Connection connection, int spaceId, String component, int excludedId)
      throws SQLException {
    // A no-op UPDATE is portable across H2, PostgreSQL, Oracle and SQL Server. Its row lock
    // is retained by the caller's JTA transaction through the subsequent insert/move/restore.
    try (var lock = connection.prepareStatement("UPDATE ST_Space SET orderNum = orderNum WHERE id = ?")) {
      lock.setInt(1, spaceId);
      if (lock.executeUpdate() != 1) { throw new SQLException("Unknown destination space"); }
    }
    try (var count = connection.prepareStatement("SELECT COUNT(*) FROM ST_ComponentInstance "
        + "WHERE spaceId = ? AND componentName = ? AND componentStatus IS NULL AND id <> ?")) {
      count.setInt(1, spaceId);
      count.setString(2, component);
      count.setInt(3, excludedId);
      try (var rows = count.executeQuery()) {
        rows.next();
        return rows.getInt(1);
      }
    }
  }
  public int lockAndCountPlatform(Connection connection, String component, int excludedId)
      throws SQLException {
    // Reuse a stable existing row as the platform mutex, without a new schema/table.
    try (var lock = connection.prepareStatement("UPDATE ST_Space SET orderNum = orderNum "
        + "WHERE id = (SELECT MIN(id) FROM ST_Space)")) {
      if (lock.executeUpdate() != 1) { throw new SQLException("No platform lock space"); }
    }
    try (var count = connection.prepareStatement("SELECT COUNT(*) FROM ST_ComponentInstance "
        + "WHERE componentName = ? AND componentStatus IS NULL AND id <> ?")) {
      count.setString(1, component);
      count.setInt(2, excludedId);
      try (var rows = count.executeQuery()) { rows.next(); return rows.getInt(1); }
    }
  }

  public java.util.List<String> platformInstanceIds(Connection connection, String component)
      throws SQLException {
    var ids = new java.util.ArrayList<String>();
    try (var query = connection.prepareStatement("SELECT id FROM ST_ComponentInstance "
        + "WHERE componentName = ? AND componentStatus IS NULL ORDER BY id")) {
      query.setString(1, component);
      try (var rows = query.executeQuery()) { while (rows.next()) { ids.add(component + rows.getInt(1)); } }
    }
    return ids;
  }

}
