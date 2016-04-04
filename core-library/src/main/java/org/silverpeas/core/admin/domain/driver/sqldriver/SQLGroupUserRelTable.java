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

package org.silverpeas.core.admin.domain.driver.sqldriver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.exception.SilverpeasException;

/**
 * A GroupTable object manages the DomainSQL_Group table.
 */
public class SQLGroupUserRelTable {
  SQLSettings drvSettings = new SQLSettings();

  public SQLGroupUserRelTable(SQLSettings ds) {
    drvSettings = ds;
  }

  /**
   * Returns all the User ids which compose a group.
   */
  public List<String> getDirectUserIdsOfGroup(Connection c, int groupId) throws AdminException {
    ResultSet rs = null;
    PreparedStatement statement = null;
    List<String> theResult = new ArrayList<>();
    String theQuery =
        "select " + drvSettings.getRelUIDColumnName() + " from " + drvSettings.getRelTableName() +
            " where " + drvSettings.getRelGIDColumnName() + " = ?";

    try {
      statement = c.prepareStatement(theQuery);
      statement.setInt(1, groupId);
      rs = statement.executeQuery();
      while (rs.next()) {
        theResult.add(Integer.toString(rs.getInt(1)));
      }
    } catch (SQLException e) {
      throw new AdminException("SQLGroupUserRelTable.getDirectUserIdsOfGroup",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", "Query = " + theQuery, e);
    } finally {
      DBUtil.close(rs, statement);
    }
    return theResult;
  }

  /**
   * Returns all the groups in a given userRole (not recursive).
   */
  public List<String> getDirectGroupIdsOfUser(Connection c, int userId) throws AdminException {
    ResultSet rs = null;
    PreparedStatement statement = null;
    List<String> theResult = new ArrayList<>();
    String theQuery =
        "select " + drvSettings.getRelGIDColumnName() + " from " + drvSettings.getRelTableName() +
            " where " + drvSettings.getRelUIDColumnName() + " = ?";

    try {
      statement = c.prepareStatement(theQuery);
      statement.setInt(1, userId);
      rs = statement.executeQuery();
      while (rs.next()) {
        theResult.add(Integer.toString(rs.getInt(1)));
      }
    } catch (SQLException e) {
      throw new AdminException("SQLGroupUserRelTable.getDirectGroupIdsOfUser",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", "Query = " + theQuery, e);
    } finally {
      DBUtil.close(rs, statement);
    }
    return theResult;
  }

  /**
   * Insert a new group row.
   */
  public int createGroupUserRel(Connection c, int groupId, int userId) throws AdminException {
    PreparedStatement statement = null;
    String theQuery =
        "insert into " + drvSettings.getRelTableName() + "(" + drvSettings.getRelGIDColumnName() +
            "," + drvSettings.getRelUIDColumnName() + ") " + " values (?,?)";

    try {
      statement = c.prepareStatement(theQuery);
      statement.setInt(1, groupId);
      statement.setInt(2, userId);
      return statement.executeUpdate();
    } catch (SQLException e) {
      throw new AdminException("SQLGroupUserRelTable.createGroupUserRel", SilverpeasException.ERROR,
          "root.EX_SQL_QUERY_FAILED", "Query = " + theQuery, e);
    } finally {
      DBUtil.close(statement);
    }
  }

  /**
   * Insert a new group row.
   */
  public int removeGroupUserRel(Connection c, int groupId, int userId) throws AdminException {
    PreparedStatement statement = null;
    String theQuery = "delete from " + drvSettings.getRelTableName() + " where " +
        drvSettings.getRelGIDColumnName() + " = ?" + " and " + drvSettings.getRelUIDColumnName() +
        " = ?";

    try {
      statement = c.prepareStatement(theQuery);
      statement.setInt(1, groupId);
      statement.setInt(2, userId);
      return statement.executeUpdate();
    } catch (SQLException e) {
      throw new AdminException("SQLGroupUserRelTable.removeGroupUserRel", SilverpeasException.ERROR,
          "root.EX_SQL_QUERY_FAILED", "Query = " + theQuery, e);
    } finally {
      DBUtil.close(statement);
    }
  }

  /**
   * Insert a new group row.
   */
  public int removeAllUserRel(Connection c, int userId) throws AdminException {
    PreparedStatement statement = null;
    String theQuery = "delete from " + drvSettings.getRelTableName() + " where " +
        drvSettings.getRelUIDColumnName() + " = ?";

    try {
      statement = c.prepareStatement(theQuery);
      statement.setInt(1, userId);
      return statement.executeUpdate();
    } catch (SQLException e) {
      throw new AdminException("SQLGroupUserRelTable.removeAllUserRel", SilverpeasException.ERROR,
          "root.EX_SQL_QUERY_FAILED", "Query = " + theQuery, e);
    } finally {
      DBUtil.close(statement);
    }
  }

  /**
   * Insert a new group row.
   */
  public int removeAllGroupRel(Connection c, int groupId) throws AdminException {
    PreparedStatement statement = null;
    String theQuery = "delete from " + drvSettings.getRelTableName() + " where " +
        drvSettings.getRelGIDColumnName() + " = ?";

    try {
      statement = c.prepareStatement(theQuery);
      statement.setInt(1, groupId);
      return statement.executeUpdate();
    } catch (SQLException e) {
      throw new AdminException("SQLGroupUserRelTable.removeAllGroupRel", SilverpeasException.ERROR,
          "root.EX_SQL_QUERY_FAILED", "Query = " + theQuery, e);
    } finally {
      DBUtil.close(statement);
    }
  }

  /**
   * Tests if a user is in given group (not recursive).
   */
  public boolean isUserDirectlyInGroup(Connection c, int userId, int groupId)
      throws AdminException {
    ResultSet rs = null;
    PreparedStatement statement = null;
    String theQuery =
        "select " + drvSettings.getRelUIDColumnName() + " from " + drvSettings.getRelTableName() +
            " where " + drvSettings.getRelGIDColumnName() + " = ? AND " +
            drvSettings.getRelUIDColumnName() + " = ?";

    try {
      statement = c.prepareStatement(theQuery);
      statement.setInt(1, groupId);
      statement.setInt(2, userId);
      rs = statement.executeQuery();
      return rs.next();
    } catch (SQLException e) {
      throw new AdminException("SQLGroupUserRelTable.isUserDirectlyInGroup",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", "Query = " + theQuery, e);
    } finally {
      DBUtil.close(rs, statement);
    }
  }
}
