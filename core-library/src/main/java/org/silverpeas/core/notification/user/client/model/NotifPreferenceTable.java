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

package org.silverpeas.core.notification.user.client.model;

import org.silverpeas.core.persistence.jdbc.AbstractTable;
import org.silverpeas.core.persistence.jdbc.Schema;
import org.silverpeas.core.exception.UtilException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class NotifPreferenceTable extends AbstractTable<NotifPreferenceRow> {

  /**
   * Builds a new NotifPreferenceTable
   */
  public NotifPreferenceTable(Schema schema) {
    super(schema, "ST_NotifPreference");
  }

  /**
   * The column list used for every select query.
   */
  static final protected String NOTIFPREFERENCE_COLUMNS =
      "id,notifAddressId,componentInstanceId,userId,messageType";

  /**
   * Returns the unique NotifPreference row having a given id
   */
  public NotifPreferenceRow getNotifPreference(int id) throws UtilException {
    return getUniqueRow(SELECT_NOTIFPREFERENCE_BY_ID, id);
  }

  static final private String SELECT_NOTIFPREFERENCE_BY_ID = "select "
      + NOTIFPREFERENCE_COLUMNS + " from ST_NotifPreference where id = ?";

  /**
   * Returns the unique NotifPreference row having the given userId,componentInstanceId,messageType
   */
  public NotifPreferenceRow getByUserIdAndComponentInstanceIdAndMessageType(
      int userId, int componentInstanceId, int messageType)
      throws UtilException {
    int[] intArgs = { userId, componentInstanceId, messageType };
    return getUniqueRow(SELECT_NOTIFPREFERENCE_BY_USERID_AND_COMPONENTINSTANCEID_AND_MESSAGETYPE,
        intArgs);
  }

  static final private String SELECT_NOTIFPREFERENCE_BY_USERID_AND_COMPONENTINSTANCEID_AND_MESSAGETYPE =
      "select "
      + NOTIFPREFERENCE_COLUMNS
      + " from ST_NotifPreference where "
      + "userId=? and componentInstanceId=? and messageType=?";

  /**
   * Returns all the NotifPreferenceRow having a given componentInstanceId
   */
  public NotifPreferenceRow[] getAllByComponentInstanceId(
      int componentInstanceId) throws UtilException {
    List<NotifPreferenceRow> rows = getRows(
        SELECT_ALL_NOTIFPREFERENCE_WITH_GIVEN_COMPONENTINSTANCEID, componentInstanceId);
    return rows.toArray(new NotifPreferenceRow[rows.size()]);
  }

  static final private String SELECT_ALL_NOTIFPREFERENCE_WITH_GIVEN_COMPONENTINSTANCEID = "select "
      + NOTIFPREFERENCE_COLUMNS
      + " from ST_NotifPreference where componentInstanceId=?";

  /**
   * Returns all the NotifPreferenceRow having a given userId
   */
  public NotifPreferenceRow[] getAllByUserId(int userId) throws UtilException {
    List<NotifPreferenceRow> rows = getRows(SELECT_ALL_NOTIFPREFERENCE_WITH_GIVEN_USERID, userId);
    return rows.toArray(new NotifPreferenceRow[rows.size()]);
  }

  static final private String SELECT_ALL_NOTIFPREFERENCE_WITH_GIVEN_USERID = "select "
      + NOTIFPREFERENCE_COLUMNS + " from ST_NotifPreference where userId=?";

  /**
   * Returns all the rows.
   */
  public NotifPreferenceRow[] getAllRows() throws UtilException {
    List<NotifPreferenceRow> rows = getRows(SELECT_ALL_NOTIFPREFERENCE);
    return rows.toArray(new NotifPreferenceRow[rows.size()]);
  }

  static final private String SELECT_ALL_NOTIFPREFERENCE = "select "
      + NOTIFPREFERENCE_COLUMNS + " from ST_NotifPreference";

  /**
   * Returns the unique row given by a no parameters query.
   */
  public NotifPreferenceRow getNotifPreference(String query) throws UtilException {
    return getUniqueRow(query);
  }

  /**
   * Returns all the rows given by a no parameters query.
   */
  public NotifPreferenceRow[] getNotifPreferences(String query) throws UtilException {
    List<NotifPreferenceRow> rows = getRows(query);
    return rows.toArray(new NotifPreferenceRow[rows.size()]);
  }

  /**
   * Inserts in the database a new NotifPreference row.
   */
  public int create(NotifPreferenceRow notifPreference) throws UtilException {
    insertRow(INSERT_NOTIFPREFERENCE, notifPreference);
    return notifPreference.getId();
  }

  static final private String INSERT_NOTIFPREFERENCE = "insert into"
      + " ST_NotifPreference (id, notifAddressId, componentInstanceId, userId, messageType)"
      + " values  (?, ?, ?, ?, ?)";

  /**
   * Update the given NotifPreferenceRow
   */
  public void update(NotifPreferenceRow notifPreference) throws UtilException {
    updateRow(UPDATE_NOTIFPREFERENCE, notifPreference);
  }

  static final private String UPDATE_NOTIFPREFERENCE = "update ST_NotifPreference set"
      + " notifAddressId = ?,"
      + " componentInstanceId = ?,"
      + " userId = ?,"
      + " messageType = ?" + " Where id = ?";

  /**
   * Updates theNotifPreference row. or inserts it if new.
   */
  public void save(NotifPreferenceRow notifPreference) throws UtilException {
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
  public void delete(int id) throws UtilException {
    updateRelation(DELETE_NOTIFPREFERENCE, id);
  }

  static final private String DELETE_NOTIFPREFERENCE = "delete from ST_NotifPreference where id=?";

  /**
   * Removes a reference to ComponentInstanceId
   */
  public void dereferenceComponentInstanceId(int componentInstanceId)
      throws UtilException {
    NotifPreferenceRow[] notifPreferenceToBeDeleted =
        getAllByComponentInstanceId(componentInstanceId);
    for (NotifPreferenceRow aNotifPreferenceToBeDeleted : notifPreferenceToBeDeleted) {
      delete(aNotifPreferenceToBeDeleted.getId());
    }
  }

  /**
   * Removes a reference to UserId
   */
  public void dereferenceUserId(int userId) throws UtilException {
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
