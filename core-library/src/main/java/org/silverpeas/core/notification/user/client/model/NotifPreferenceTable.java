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
package org.silverpeas.core.notification.user.client.model;

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.jdbc.AbstractTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class NotifPreferenceTable extends AbstractTable<NotifPreferenceRow> {

  /**
   * Builds a new NotifPreferenceTable
   */
  NotifPreferenceTable() {
    super("ST_NotifPreference");
  }

  /**
   * The column list used for every select query.
   */
  protected static final String NOTIFPREFERENCE_COLUMNS =
      "id,notifAddressId,componentInstanceId,userId,messageType";

  /**
   * Returns the unique NotifPreference row having a given id
   */
  public NotifPreferenceRow getNotifPreference(int id) throws SQLException {
    return getUniqueRow(SELECT_NOTIFPREFERENCE_BY_ID, id);
  }

  private static final String SELECT = "select ";
  private static final String SELECT_NOTIFPREFERENCE_BY_ID = SELECT
      + NOTIFPREFERENCE_COLUMNS + " from ST_NotifPreference where id = ?";

  /**
   * Returns the unique NotifPreference row having the given userId,componentInstanceId,messageType
   */
  public NotifPreferenceRow getByUserIdAndComponentInstanceIdAndMessageType(
      int userId, int componentInstanceId, int messageType)
      throws SQLException {
    int[] intArgs = { userId, componentInstanceId, messageType };
    return getUniqueRow(SELECT_NOTIFPREFERENCE_BY_USERID_AND_COMPONENTINSTANCEID_AND_MESSAGETYPE,
        intArgs);
  }

  private static final String SELECT_NOTIFPREFERENCE_BY_USERID_AND_COMPONENTINSTANCEID_AND_MESSAGETYPE =
      SELECT
      + NOTIFPREFERENCE_COLUMNS
      + " from ST_NotifPreference where "
      + "userId=? and componentInstanceId=? and messageType=?";

  /**
   * Returns all the NotifPreferenceRow having a given componentInstanceId
   */
  public NotifPreferenceRow[] getAllByComponentInstanceId(
      int componentInstanceId) throws SQLException {
    List<NotifPreferenceRow> rows = getRows(
        SELECT_ALL_NOTIFPREFERENCE_WITH_GIVEN_COMPONENTINSTANCEID, componentInstanceId);
    return rows.toArray(new NotifPreferenceRow[rows.size()]);
  }

  private static final String SELECT_ALL_NOTIFPREFERENCE_WITH_GIVEN_COMPONENTINSTANCEID = SELECT
      + NOTIFPREFERENCE_COLUMNS
      + " from ST_NotifPreference where componentInstanceId=?";

  /**
   * Returns all the NotifPreferenceRow having a given userId
   */
  public NotifPreferenceRow[] getAllByUserId(int userId) throws SQLException {
    List<NotifPreferenceRow> rows = getRows(SELECT_ALL_NOTIFPREFERENCE_WITH_GIVEN_USERID, userId);
    return rows.toArray(new NotifPreferenceRow[rows.size()]);
  }

  private static final String SELECT_ALL_NOTIFPREFERENCE_WITH_GIVEN_USERID = SELECT
      + NOTIFPREFERENCE_COLUMNS + " from ST_NotifPreference where userId=?";

  /**
   * Returns all the rows.
   */
  public NotifPreferenceRow[] getAllRows() throws SQLException {
    List<NotifPreferenceRow> rows = getRows(SELECT_ALL_NOTIFPREFERENCE);
    return rows.toArray(new NotifPreferenceRow[rows.size()]);
  }

  private static final String SELECT_ALL_NOTIFPREFERENCE = SELECT
      + NOTIFPREFERENCE_COLUMNS + " from ST_NotifPreference";

  /**
   * Returns the unique row given by a no parameters query.
   */
  public NotifPreferenceRow getNotifPreference(String query) throws SQLException {
    return getUniqueRow(query);
  }

  /**
   * Returns all the rows given by a no parameters query.
   */
  public NotifPreferenceRow[] getNotifPreferences(String query) throws SQLException {
    List<NotifPreferenceRow> rows = getRows(query);
    return rows.toArray(new NotifPreferenceRow[rows.size()]);
  }

  /**
   * Inserts in the database a new NotifPreference row.
   */
  public int create(NotifPreferenceRow notifPreference) throws SQLException {
    insertRow(INSERT_NOTIFPREFERENCE, notifPreference);
    return notifPreference.getId();
  }

  private static final String INSERT_NOTIFPREFERENCE = "insert into"
      + " ST_NotifPreference (id, notifAddressId, componentInstanceId, userId, messageType)"
      + " values  (?, ?, ?, ?, ?)";

  /**
   * Update the given NotifPreferenceRow
   */
  public void update(NotifPreferenceRow notifPreference) throws SQLException {
    updateRow(UPDATE_NOTIFPREFERENCE, notifPreference);
  }

  private static final String UPDATE_NOTIFPREFERENCE = "update ST_NotifPreference set"
      + " notifAddressId = ?,"
      + " componentInstanceId = ?,"
      + " userId = ?,"
      + " messageType = ?" + " Where id = ?";

  /**
   * Updates theNotifPreference row. or inserts it if new.
   */
  public void save(NotifPreferenceRow notifPreference) throws SQLException {
    if (notifPreference.getId() == -1) {
      // No id : it's a creation
      create(notifPreference);
    } else {
      update(notifPreference);
    }
  }

  /**
   * Deletes theNotifPreferenceRow. after having removed all the reference to it.
   */
  public void delete(int id) throws SQLException {
    updateRelation(DELETE_NOTIFPREFERENCE, id);
  }

  private static final String DELETE_NOTIFPREFERENCE = "delete from ST_NotifPreference where id=?";

  /**
   * Removes a reference to ComponentInstanceId
   */
  public void dereferenceComponentInstanceId(int componentInstanceId)
      throws SQLException {
    NotifPreferenceRow[] notifPreferenceToBeDeleted =
        getAllByComponentInstanceId(componentInstanceId);
    for (NotifPreferenceRow aNotifPreferenceToBeDeleted : notifPreferenceToBeDeleted) {
      delete(aNotifPreferenceToBeDeleted.getId());
    }
  }

  /**
   * Removes a reference to UserId
   */
  public void dereferenceUserId(int userId) throws SQLException {
    NotifPreferenceRow[] notifPreferenceToBeDeleted = getAllByUserId(userId);
    for (NotifPreferenceRow aNotifPreferenceToBeDeleted : notifPreferenceToBeDeleted) {
      delete(aNotifPreferenceToBeDeleted.getId());
    }
  }

  /**
   * Fetch the current NotifPreference row from a resultSet.
   */
  protected NotifPreferenceRow fetchRow(ResultSet rs) throws SQLException {
    return new NotifPreferenceRow(rs.getInt("id"), rs.getInt("notifAddressId"),
        rs.getInt("componentInstanceId"), rs.getInt("userId"), rs
        .getInt("messageType"));
  }

  /**
   * Prepares the statement to update the given row
   */
  protected void prepareUpdate(String updateQuery, PreparedStatement update,
      NotifPreferenceRow r) throws SQLException {
    update.setInt(1, r.getNotifAddressId());
    update.setInt(2, r.getComponentInstanceId());
    update.setInt(3, r.getUserId());
    update.setInt(4, r.getMessageType());
    update.setInt(5, r.getId());
  }

  /**
   * Prepares the statement to insert the given row
   */
  protected void prepareInsert(String insertQuery, PreparedStatement insert,
      NotifPreferenceRow r) throws SQLException {
    if (r.getId() == -1) {
      r.setId(getNextId());
    }
    insert.setInt(1, r.getId());
    insert.setInt(2, r.getNotifAddressId());
    insert.setInt(3, r.getComponentInstanceId());
    insert.setInt(4, r.getUserId());
    insert.setInt(5, r.getMessageType());
  }

}
