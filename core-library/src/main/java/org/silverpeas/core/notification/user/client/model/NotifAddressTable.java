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

public class NotifAddressTable extends AbstractTable<NotifAddressRow> {

  /**
   * Builds a new NotifAddressTable
   */
  public NotifAddressTable(Schema schema) {
    super(schema, "ST_NotifAddress");
  }

  /**
   * The column list used for every select query.
   */
  static final protected String NOTIFADDRESS_COLUMNS =
      "id,userId,notifName,notifChannelId,address,usage,priority";

  /**
   * Returns the unique NotifAddress row having a given id
   */
  public NotifAddressRow getNotifAddress(int id) throws UtilException {
    return getUniqueRow(SELECT_NOTIFADDRESS_BY_ID, id);
  }

  static final private String SELECT_NOTIFADDRESS_BY_ID = "Select "
      + NOTIFADDRESS_COLUMNS + " from ST_NotifAddress Where id = ?";

  /**
   * Returns all the NotifAddress rows having a given userId
   */
  public NotifAddressRow[] getAllByUserId(int aUserId, String orderField)
      throws UtilException {
    String req = "select " + NOTIFADDRESS_COLUMNS + " from ST_NotifAddress"
        + " Where UserId = " + aUserId;
    if (orderField != null) {
      req = req + " order by " + orderField;
    }
    List<NotifAddressRow> rows = getRows(req);
    return rows.toArray(new NotifAddressRow[rows.size()]);
  }

  /**
   * Returns all the NotifAddressRow having a given notifChannelId
   */
  public NotifAddressRow[] getAllByNotifChannelId(int notifChannelId) throws UtilException {
    List<NotifAddressRow> rows =
        getRows(SELECT_ALL_NOTIFADDRESS_WITH_GIVEN_NOTIFCHANNELID, notifChannelId);
    return rows.toArray(new NotifAddressRow[rows.size()]);
  }

  static final private String SELECT_ALL_NOTIFADDRESS_WITH_GIVEN_NOTIFCHANNELID = "select "
      + NOTIFADDRESS_COLUMNS + " from ST_NotifAddress where notifChannelId=?";

  /**
   * Returns all the NotifAddressRow having a given userId
   */
  public NotifAddressRow[] getAllByUserId(int userId) throws UtilException {
    List<NotifAddressRow> rows = getRows(SELECT_ALL_NOTIFADDRESS_WITH_GIVEN_USERID, userId);
    return rows.toArray(new NotifAddressRow[rows.size()]);
  }

  static final private String SELECT_ALL_NOTIFADDRESS_WITH_GIVEN_USERID = "select "
      + NOTIFADDRESS_COLUMNS + " from ST_NotifAddress where userId=?";

  /**
   * Returns all the rows.
   */
  public NotifAddressRow[] getAllRows() throws UtilException {
    List<NotifAddressRow> rows = getRows(SELECT_ALL_NOTIFADDRESS);
    return rows.toArray(new NotifAddressRow[rows.size()]);
  }

  static final private String SELECT_ALL_NOTIFADDRESS = "select "
      + NOTIFADDRESS_COLUMNS + " from ST_NotifAddress";

  /**
   * Returns the unique row given by a no parameters query.
   */
  public NotifAddressRow getNotifAddress(String query) throws UtilException {
    return getUniqueRow(query);
  }

  /**
   * Returns all the rows given by a no parameters query.
   */
  public NotifAddressRow[] getNotifAddresss(String query) throws UtilException {
    List<NotifAddressRow> rows = getRows(query);
    return rows.toArray(new NotifAddressRow[rows.size()]);
  }

  /**
   * Inserts in the database a new NotifAddress row.
   */
  public int create(NotifAddressRow notifAddress) throws UtilException {
    insertRow(INSERT_NOTIFADDRESS, notifAddress);
    return notifAddress.getId();
  }

  static final private String INSERT_NOTIFADDRESS = "insert into"
      + " ST_NotifAddress (id, userId, notifName, notifChannelId, address, usage, priority)"
      + " values  (?, ?, ?, ?, ?, ?, ?)";

  /**
   * Update the given NotifAddressRow
   */
  public void update(NotifAddressRow notifAddress) throws UtilException {
    updateRow(UPDATE_NOTIFADDRESS, notifAddress);
  }

  static final private String UPDATE_NOTIFADDRESS = "update ST_NotifAddress set"
      + " userId = ?,"
      + " notifName = ?,"
      + " notifChannelId = ?,"
      + " address = ?," + " usage = ?," + " priority = ?" + " Where id = ?";

  /**
   * Updates theNotifAddress row. or inserts it if new.
   */
  public void save(NotifAddressRow notifAddress) throws UtilException {
    if (notifAddress.getId() == -1) {
      // No id : it's a creation
      create(notifAddress);
    } else {
      update(notifAddress);
    }
  }

  /**
   * Deletes theNotifAddressRow. after having removed all the reference to it.
   */
  public void delete(int id) throws UtilException {
    updateRelation(DELETE_NOTIFADDRESS, id);
  }

  static final private String DELETE_NOTIFADDRESS = "delete from ST_NotifAddress where id=?";

  /**
   * Removes a reference to NotifChannelId
   */
  public void dereferenceNotifChannelId(int notifChannelId) throws UtilException {
    NotifAddressRow[] notifAddressToBeDeleted = getAllByNotifChannelId(notifChannelId);
    for (NotifAddressRow aNotifAddressToBeDeleted : notifAddressToBeDeleted) {
      delete(aNotifAddressToBeDeleted.getId());
    }
  }

  /**
   * Removes a reference to UserId
   */
  public void dereferenceUserId(int userId) throws UtilException {
    NotifAddressRow[] notifAddressToBeDeleted = getAllByUserId(userId);
    for (NotifAddressRow aNotifAddressToBeDeleted : notifAddressToBeDeleted) {
      delete(aNotifAddressToBeDeleted.getId());
    }
  }

  /**
   * Fetch the current NotifAddress row from a resultSet.
   */
  protected NotifAddressRow fetchRow(ResultSet rs) throws SQLException {
    return new NotifAddressRow(rs.getInt("id"), rs.getInt("userId"), rs
        .getString("notifName"), rs.getInt("notifChannelId"), rs
        .getString("address"), rs.getString("usage"), rs.getInt("priority"));
  }

  /**
   * Prepares the statement to update the given row
   */
  protected void prepareUpdate(String updateQuery, PreparedStatement update,
      NotifAddressRow row) throws SQLException {
    update.setInt(1, row.getUserId());
    update.setString(2, truncate(row.getNotifName(), 20));
    update.setInt(3, row.getNotifChannelId());
    update.setString(4, truncate(row.getAddress(), 250));
    update.setString(5, truncate(row.getUsage(), 20));
    update.setInt(6, row.getPriority());
    update.setInt(7, row.getId());
  }

  /**
   * Prepares the statement to insert the given row
   */
  protected void prepareInsert(String insertQuery, PreparedStatement insert,
      NotifAddressRow row) throws SQLException {
    if (row.getId() == -1) {
      row.setId(getNextId());
    }
    insert.setInt(1, row.getId());
    insert.setInt(2, row.getUserId());
    insert.setString(3, truncate(row.getNotifName(), 20));
    insert.setInt(4, row.getNotifChannelId());
    insert.setString(5, truncate(row.getAddress(), 250));
    insert.setString(6, truncate(row.getUsage(), 20));
    insert.setInt(7, row.getPriority());
  }

  public void deleteAndPropagate(int notifAddressId, int defaultAddress)
      throws UtilException {
    NotifPreferenceRow[] nprs = null;
    NotifPreferenceTable npt = ((NotifSchema) schema).notifPreference;
    NotifDefaultAddressRow[] ndars = null;
    NotifDefaultAddressTable ndat = ((NotifSchema) schema).notifDefaultAddress;
    int i;

    // Remove the preferences that are linked to this Address
    nprs = npt
        .getNotifPreferences("select * from ST_NotifPreference where notifAddressId = "
        + Integer.toString(notifAddressId));
    for (i = 0; i < nprs.length; i++) {
      npt.delete(nprs[i].getId());
    }

    // Update the Default media that is linked to this Address
    ndars = ndat
        .getNotifDefaultAddresss("select * from ST_NotifDefaultAddress where notifAddressId = "
        + Integer.toString(notifAddressId));
    for (i = 0; i < ndars.length; i++) {
      ndars[i].setNotifAddressId(defaultAddress);
      ndat.update(ndars[i]);
    }

    delete(notifAddressId);
  }

}
