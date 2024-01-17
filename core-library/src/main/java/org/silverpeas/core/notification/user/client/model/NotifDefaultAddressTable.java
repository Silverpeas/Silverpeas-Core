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
public class NotifDefaultAddressTable extends AbstractTable<NotifDefaultAddressRow> {

  /**
   * Builds a new NotifDefaultAddressTable
   */
  NotifDefaultAddressTable() {
    super("ST_NotifDefaultAddress");
  }

  /**
   * The column list used for every select query.
   */
  protected static final String NOTIFDEFAULTADDRESS_COLUMNS = "id,userId,notifAddressId";

  /**
   * Returns the unique NotifDefaultAddress row having a given id
   */
  public NotifDefaultAddressRow getNotifDefaultAddress(int id)
      throws SQLException {
    return getUniqueRow(SELECT_NOTIFDEFAULTADDRESS_BY_ID, id);
  }

  private static final String SELECT = "select ";
  private static final String SELECT_NOTIFDEFAULTADDRESS_BY_ID = SELECT
      + NOTIFDEFAULTADDRESS_COLUMNS
      + " from ST_NotifDefaultAddress Where id = ?";

  /**
   * Returns all the NotifDefaultAddressRow having a given userId
   */
  public NotifDefaultAddressRow[] getAllByUserId(int userId)
      throws SQLException {
    List<NotifDefaultAddressRow> rows = getRows(
        SELECT_ALL_NOTIFDEFAULTADDRESS_WITH_GIVEN_USERID, userId);
    return rows.toArray(new NotifDefaultAddressRow[rows.size()]);
  }

  private static final String SELECT_ALL_NOTIFDEFAULTADDRESS_WITH_GIVEN_USERID = SELECT
      + NOTIFDEFAULTADDRESS_COLUMNS
      + " from ST_NotifDefaultAddress where userId=?";

  /**
   * Returns all the rows.
   */
  public NotifDefaultAddressRow[] getAllRows() throws SQLException {
    List<NotifDefaultAddressRow> rows = getRows(SELECT_ALL_NOTIFDEFAULTADDRESS);
    return rows.toArray(new NotifDefaultAddressRow[rows.size()]);
  }

  private static final String SELECT_ALL_NOTIFDEFAULTADDRESS = SELECT
      + NOTIFDEFAULTADDRESS_COLUMNS + " from ST_NotifDefaultAddress";

  /**
   * Returns the unique row given by a no parameters query.
   */
  public NotifDefaultAddressRow getNotifDefaultAddress(String query)
      throws SQLException {
    return getUniqueRow(query);
  }

  /**
   * Returns all the rows given by a no parameters query.
   */
  public NotifDefaultAddressRow[] getNotifDefaultAddresss(String query)
      throws SQLException {
    List<NotifDefaultAddressRow> rows = getRows(query);
    return rows.toArray(new NotifDefaultAddressRow[rows.size()]);
  }

  /**
   * Inserts in the database a new NotifDefaultAddress row.
   */
  public int create(NotifDefaultAddressRow notifDefaultAddress)
      throws SQLException {
    insertRow(INSERT_NOTIFDEFAULTADDRESS, notifDefaultAddress);
    return notifDefaultAddress.getId();
  }

  private static final String INSERT_NOTIFDEFAULTADDRESS = "insert into"
      + " ST_NotifDefaultAddress (id, userId, notifAddressId)"
      + " values  (?, ?, ?)";

  /**
   * Update the given NotifDefaultAddressRow
   */
  public void update(NotifDefaultAddressRow notifDefaultAddress) throws SQLException {
    updateRow(UPDATE_NOTIFDEFAULTADDRESS, notifDefaultAddress);
  }

  private static final String UPDATE_NOTIFDEFAULTADDRESS = "update ST_NotifDefaultAddress set"
      + " userId = ?," + " notifAddressId = ?" + " Where id = ?";

  /**
   * Updates theNotifDefaultAddress row. or inserts it if new.
   */
  public void save(NotifDefaultAddressRow notifDefaultAddress)
      throws SQLException {
    if (notifDefaultAddress.getId() == -1) {
      // No id : it's a creation
      create(notifDefaultAddress);
    } else {
      update(notifDefaultAddress);
    }
  }

  /**
   * Deletes theNotifDefaultAddressRow. after having removed all the reference to it.
   */
  public void delete(int id) throws SQLException {
    updateRelation(DELETE_NOTIFDEFAULTADDRESS, id);
  }

  private static final String DELETE_NOTIFDEFAULTADDRESS =
      "delete from ST_NotifDefaultAddress where id=?";

  /**
   * Removes a reference to UserId
   */
  public void dereferenceUserId(int userId) throws SQLException {
    NotifDefaultAddressRow[] notifDefaultAddressToBeDeleted = getAllByUserId(userId);
    for (NotifDefaultAddressRow aNotifDefaultAddressToBeDeleted : notifDefaultAddressToBeDeleted) {
      delete(aNotifDefaultAddressToBeDeleted.getId());
    }
  }

  /**
   * Fetch the current NotifDefaultAddress row from a resultSet.
   */
  protected NotifDefaultAddressRow fetchRow(ResultSet rs) throws SQLException {
    return new NotifDefaultAddressRow(rs.getInt("id"), rs.getInt("userId"), rs
        .getInt("notifAddressId"));
  }

  /**
   * Prepares the statement to update the given row
   */
  protected void prepareUpdate(String updateQuery, PreparedStatement update,
      NotifDefaultAddressRow r) throws SQLException {
    update.setInt(1, r.getUserId());
    update.setInt(2, r.getNotifAddressId());
    update.setInt(3, r.getId());
  }

  /**
   * Prepares the statement to insert the given row
   */
  protected void prepareInsert(String insertQuery, PreparedStatement insert,
      NotifDefaultAddressRow r) throws SQLException {
    if (r.getId() == -1) {
      r.setId(getNextId());
    }
    insert.setInt(1, r.getId());
    insert.setInt(2, r.getUserId());
    insert.setInt(3, r.getNotifAddressId());
  }

}
