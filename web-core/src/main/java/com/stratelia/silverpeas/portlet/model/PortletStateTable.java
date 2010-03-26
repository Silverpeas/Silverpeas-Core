/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.stratelia.webactiv.util.AbstractTable;
import com.stratelia.webactiv.util.Schema;
import com.stratelia.webactiv.util.exception.UtilException;

public class PortletStateTable extends AbstractTable {

  /**
   * Builds a new PortletStateTable
   */
  public PortletStateTable(Schema schema) {
    super(schema, "ST_PortletState");
  }

  /**
   * The column list used for every select query.
   */
  static final protected String PORTLETSTATE_COLUMNS = "id,state,userId,portletRowId";

  /**
   * Returns the unique PortletState row having a given id
   */
  public PortletStateRow getPortletState(int id) throws UtilException {
    return (PortletStateRow) getUniqueRow(SELECT_PORTLETSTATE_BY_ID, id);
  }

  static final private String SELECT_PORTLETSTATE_BY_ID = "Select "
      + PORTLETSTATE_COLUMNS + " from ST_PortletState Where id = ?";

  /**
   * Returns all the PortletState rows having a given portletRowId
   */
  public PortletStateRow[] getAllByPortletRowId(int aPortletRowId,
      String orderField) throws UtilException {
    String req = "select " + PORTLETSTATE_COLUMNS + " from ST_PortletState"
        + " Where PortletRowId = " + aPortletRowId;
    if (orderField != null) {
      req = req + " order by " + orderField;
    }
    return (PortletStateRow[]) getRows(req).toArray(new PortletStateRow[0]);
  }

  /**
   * Returns all the PortletStateRow having a given portletRowId
   */
  public PortletStateRow[] getAllByPortletRowId(int portletRowId)
      throws UtilException {
    return (PortletStateRow[]) getRows(
        SELECT_ALL_PORTLETSTATE_WITH_GIVEN_PORTLETROWID, portletRowId).toArray(
        new PortletStateRow[0]);
  }

  static final private String SELECT_ALL_PORTLETSTATE_WITH_GIVEN_PORTLETROWID = "select "
      + PORTLETSTATE_COLUMNS + " from ST_PortletState where portletRowId=?";

  /**
   * Returns all the PortletStateRow having a given userId
   */
  public PortletStateRow[] getAllByUserId(int userId) throws UtilException {
    return (PortletStateRow[]) getRows(
        SELECT_ALL_PORTLETSTATE_WITH_GIVEN_USERID, userId).toArray(
        new PortletStateRow[0]);
  }

  static final private String SELECT_ALL_PORTLETSTATE_WITH_GIVEN_USERID = "select "
      + PORTLETSTATE_COLUMNS + " from ST_PortletState where userId=?";

  /**
   * Returns all the rows.
   */
  public PortletStateRow[] getAllRows() throws UtilException {
    return (PortletStateRow[]) getRows(SELECT_ALL_PORTLETSTATE).toArray(
        new PortletStateRow[0]);
  }

  static final private String SELECT_ALL_PORTLETSTATE = "select "
      + PORTLETSTATE_COLUMNS + " from ST_PortletState";

  /**
   * Returns the unique row given by a no parameters query.
   */
  public PortletStateRow getPortletState(String query) throws UtilException {
    return (PortletStateRow) getUniqueRow(query);
  }

  /**
   * Returns all the rows given by a no parameters query.
   */
  public PortletStateRow[] getPortletStates(String query) throws UtilException {
    return (PortletStateRow[]) getRows(query).toArray(new PortletStateRow[0]);
  }

  /**
   * Inserts in the database a new PortletState row.
   */
  public int create(PortletStateRow portletState) throws UtilException {
    insertRow(INSERT_PORTLETSTATE, portletState);
    return portletState.getId();
  }

  static final private String INSERT_PORTLETSTATE = "insert into"
      + " ST_PortletState (id, state, userId, portletRowId)"
      + " values  (?, ?, ?, ?)";

  /**
   * Update the given PortletStateRow
   */
  public void update(PortletStateRow portletState) throws UtilException {
    updateRow(UPDATE_PORTLETSTATE, portletState);
  }

  static final private String UPDATE_PORTLETSTATE = "Update ST_PortletState set"
      + " state = ?," + " userId = ?," + " portletRowId = ?" + " Where id = ?";

  /**
   * Updates thePortletState row. or inserts it if new.
   */
  public void save(PortletStateRow portletState) throws UtilException {
    if (portletState.getId() == -1) {
      // No id : it's a creation
      create(portletState);
    } else {
      update(portletState);
    }
  }

  /**
   * Deletes thePortletStateRow. after having removed all the reference to it.
   */
  public void delete(int id) throws UtilException {
    updateRelation(DELETE_PORTLETSTATE, id);
  }

  static final private String DELETE_PORTLETSTATE = "delete from ST_PortletState where id=?";

  /**
   * Removes a reference to PortletRowId
   */
  public void dereferencePortletRowId(int portletRowId) throws UtilException {
    PortletStateRow[] portletStateToBeDeleted = getAllByPortletRowId(portletRowId);
    for (int i = 0; i < portletStateToBeDeleted.length; i++) {
      delete(portletStateToBeDeleted[i].getId());
    }
  }

  /**
   * Removes a reference to UserId
   */
  public void dereferenceUserId(int userId) throws UtilException {
    PortletStateRow[] portletStateToBeDeleted = getAllByUserId(userId);
    for (int i = 0; i < portletStateToBeDeleted.length; i++) {
      delete(portletStateToBeDeleted[i].getId());
    }
  }

  /**
   * Fetch the current PortletState row from a resultSet.
   */
  protected Object fetchRow(ResultSet rs) throws SQLException {
    return new PortletStateRow(rs.getInt("id"), rs.getInt("state"), rs
        .getInt("userId"), rs.getInt("portletRowId"));
  }

  /**
   * Prepares the statement to update the given row
   */
  protected void prepareUpdate(String updateQuery, PreparedStatement update,
      Object row) throws SQLException {
    PortletStateRow r = (PortletStateRow) row;
    update.setInt(1, r.getState());
    update.setInt(2, r.getUserId());
    update.setInt(3, r.getPortletRowId());
    update.setInt(4, r.getId());
  }

  /**
   * Prepares the statement to insert the given row
   */
  protected void prepareInsert(String insertQuery, PreparedStatement insert,
      Object row) throws SQLException {
    PortletStateRow r = (PortletStateRow) row;
    if (r.getId() == -1) {
      r.setId(getNextId());
    }
    insert.setInt(1, r.getId());
    insert.setInt(2, r.getState());
    insert.setInt(3, r.getUserId());
    insert.setInt(4, r.getPortletRowId());
  }
}
