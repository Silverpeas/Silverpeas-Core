/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.portlet.model;

import com.stratelia.webactiv.util.AbstractTable;
import com.stratelia.webactiv.util.Schema;
import com.stratelia.webactiv.util.exception.UtilException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PortletColumnTable extends AbstractTable<PortletColumnRow> {

  /**
   * Builds a new PortletColumnTable
   */
  public PortletColumnTable(Schema schema) {
    super(schema, "ST_PortletColumn");
  }

  /**
   * The column list used for every select query.
   */
  static final protected String PORTLETCOLUMN_COLUMNS = "id,spaceId,columnWidth,nbCol";

  /**
   * Returns the unique PortletColumn row having a given id
   */
  public PortletColumnRow getPortletColumn(int id) throws UtilException {
    return getUniqueRow(SELECT_PORTLETCOLUMN_BY_ID, id);
  }

  static final private String SELECT_PORTLETCOLUMN_BY_ID = "Select "
      + PORTLETCOLUMN_COLUMNS + " from ST_PortletColumn Where id = ?";

  /**
   * Returns all the PortletColumn rows having a given spaceId
   */
  public PortletColumnRow[] getAllBySpaceId(int aSpaceId, String orderField)
      throws UtilException {
    String req = "select " + PORTLETCOLUMN_COLUMNS + " from ST_PortletColumn"
        + " Where SpaceId = " + aSpaceId;
    if (orderField != null) {
      req = req + " order by " + orderField;
    }
    List<PortletColumnRow> rows = getRows(req);
    return rows.toArray(new PortletColumnRow[rows.size()]);
  }

  /**
   * Returns all the PortletColumnRow having a given spaceId
   */
  public PortletColumnRow[] getAllBySpaceId(int spaceId) throws UtilException {
    List<PortletColumnRow> rows = getRows(SELECT_ALL_PORTLETCOLUMN_WITH_GIVEN_SPACEID, spaceId);
    return rows.toArray(new PortletColumnRow[rows.size()]);
  }

  static final private String SELECT_ALL_PORTLETCOLUMN_WITH_GIVEN_SPACEID = "select "
      + PORTLETCOLUMN_COLUMNS + " from ST_PortletColumn where spaceId=?";

  /**
   * Returns all the rows.
   */
  public PortletColumnRow[] getAllRows() throws UtilException {
    List<PortletColumnRow> rows = getRows(SELECT_ALL_PORTLETCOLUMN);
    return rows.toArray(new PortletColumnRow[rows.size()]);
  }

  static final private String SELECT_ALL_PORTLETCOLUMN = "select "
      + PORTLETCOLUMN_COLUMNS + " from ST_PortletColumn";

  /**
   * Returns the unique row given by a no parameters query.
   */
  public PortletColumnRow getPortletColumn(String query) throws UtilException {
    return getUniqueRow(query);
  }

  /**
   * Returns all the rows given by a no parameters query.
   */
  public PortletColumnRow[] getPortletColumns(String query) throws UtilException {
    List<PortletColumnRow> rows = getRows(query);
    return rows.toArray(new PortletColumnRow[rows.size()]);
  }

  /**
   * Inserts in the database a new PortletColumn row.
   */
  public int create(PortletColumnRow portletColumn) throws UtilException {
    insertRow(INSERT_PORTLETCOLUMN, portletColumn);
    return portletColumn.getId();
  }

  static final private String INSERT_PORTLETCOLUMN = "insert into"
      + " ST_PortletColumn (id, spaceId, columnWidth, nbCol)"
      + " values  (?, ?, ?, ?)";

  /**
   * Update the given PortletColumnRow
   */
  public void update(PortletColumnRow portletColumn) throws UtilException {
    updateRow(UPDATE_PORTLETCOLUMN, portletColumn);
  }

  static final private String UPDATE_PORTLETCOLUMN = "Update ST_PortletColumn set"
      + " spaceId = ?," + " columnWidth = ?," + " nbCol = ?" + " Where id = ?";

  /**
   * Updates thePortletColumn row. or inserts it if new.
   */
  public void save(PortletColumnRow portletColumn) throws UtilException {
    if (portletColumn.getId() == -1) {
      // No id : it's a creation
      create(portletColumn);
    } else {
      update(portletColumn);
    }
  }

  /**
   * Deletes thePortletColumnRow. after having removed all the reference to it.
   */
  public void delete(int id) throws UtilException {
    ((PortletSchema) schema).portletRow.dereferencePortletColumnId(id);
    updateRelation(DELETE_PORTLETCOLUMN, id);
  }

  static final private String DELETE_PORTLETCOLUMN = "delete from ST_PortletColumn where id=?";

  /**
   * Removes a reference to SpaceId
   */
  public PortletColumnRow[] dereferenceSpaceId(int spaceId) throws UtilException {
    PortletColumnRow[] portletColumnToBeDeleted = getAllBySpaceId(spaceId);
    for (PortletColumnRow aPortletColumnToBeDeleted : portletColumnToBeDeleted) {
      delete(aPortletColumnToBeDeleted.getId());
    }
    return portletColumnToBeDeleted;
  }

  /**
   * Fetch the current PortletColumn row from a resultSet.
   */
  protected PortletColumnRow fetchRow(ResultSet rs) throws SQLException {
    return new PortletColumnRow(rs.getInt("id"), rs.getInt("spaceId"), rs
        .getString("columnWidth"), rs.getInt("nbCol"));
  }

  /**
   * Prepares the statement to update the given row
   */
  protected void prepareUpdate(String updateQuery, PreparedStatement update,
      PortletColumnRow row) throws SQLException {
    update.setInt(1, row.getSpaceId());
    update.setString(2, truncate(row.getColumnWidth(), 10));
    update.setInt(3, row.getNbCol());
    update.setInt(4, row.getId());
  }

  /**
   * Prepares the statement to insert the given row
   */
  protected void prepareInsert(String insertQuery, PreparedStatement insert,
      PortletColumnRow row) throws SQLException {
    if (row.getId() == -1) {
      row.setId(getNextId());
    }
    insert.setInt(1, row.getId());
    insert.setInt(2, row.getSpaceId());
    insert.setString(3, truncate(row.getColumnWidth(), 10));
    insert.setInt(4, row.getNbCol());
  }
}
