package com.stratelia.webactiv.organization;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A AccessLevelTable object manages the ST_ACCESSLEVEL table.
 */

public class AccessLevelTable extends Table {
  public AccessLevelTable(OrganizationSchema organization) {
    super(organization, "ST_AccessLevel");
  }

  static final private String ACCESSLEVEL_COLUMNS = "id,name";

  /**
   * Fetch the current access level row from a resultSet.
   */
  protected AccessLevelRow fetchAccessLevel(ResultSet rs) throws SQLException {
    AccessLevelRow a = new AccessLevelRow();

    a.id = rs.getString(1);
    a.name = rs.getString(2);

    return a;
  }

  /**
   * Returns all the Access levels.
   */
  public AccessLevelRow[] getAllAccessLevels() throws AdminPersistenceException {
    return (AccessLevelRow[]) getRows(SELECT_ALL_ACCESSLEVELS).toArray(
        new AccessLevelRow[0]);
  }

  static final private String SELECT_ALL_ACCESSLEVELS = "select "
      + ACCESSLEVEL_COLUMNS + " from ST_AccessLevel";

  /**
   * Returns the Access level whith the given id.
   */
  public AccessLevelRow getAccessLevel(String id)
      throws AdminPersistenceException {
    return (AccessLevelRow) getUniqueRow(SELECT_ACCESSLEVEL_BY_ID, id);
  }

  static final private String SELECT_ACCESSLEVEL_BY_ID = "select "
      + ACCESSLEVEL_COLUMNS + " from ST_AccessLevel where id = ?";

  /**
   * Fetch the current accessLevel row from a resultSet.
   */
  protected Object fetchRow(ResultSet rs) throws SQLException {
    return fetchAccessLevel(rs);
  }

  /**
   * update a accessLevel
   */
  protected void prepareUpdate(String updateQuery, PreparedStatement update,
      Object row) {
    // not implemented
  }

  /**
   * insert a accessLevel
   */
  protected void prepareInsert(String insertQuery, PreparedStatement insert,
      Object row) {
    // not implemented
  }

}
