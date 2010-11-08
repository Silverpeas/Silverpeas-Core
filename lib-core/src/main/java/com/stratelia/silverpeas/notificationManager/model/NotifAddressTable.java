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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.stratelia.silverpeas.notificationManager.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.stratelia.webactiv.util.AbstractTable;
import com.stratelia.webactiv.util.Schema;
import com.stratelia.webactiv.util.exception.UtilException;

public class NotifAddressTable extends AbstractTable {

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
    return (NotifAddressRow) getUniqueRow(SELECT_NOTIFADDRESS_BY_ID, id);
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
    return (NotifAddressRow[]) getRows(req).toArray(new NotifAddressRow[0]);
  }

  /**
   * Returns all the NotifAddressRow having a given notifChannelId
   */
  public NotifAddressRow[] getAllByNotifChannelId(int notifChannelId)
      throws UtilException {
    return (NotifAddressRow[]) getRows(
        SELECT_ALL_NOTIFADDRESS_WITH_GIVEN_NOTIFCHANNELID, notifChannelId)
        .toArray(new NotifAddressRow[0]);
  }

  static final private String SELECT_ALL_NOTIFADDRESS_WITH_GIVEN_NOTIFCHANNELID = "select "
      + NOTIFADDRESS_COLUMNS + " from ST_NotifAddress where notifChannelId=?";

  /**
   * Returns all the NotifAddressRow having a given userId
   */
  public NotifAddressRow[] getAllByUserId(int userId) throws UtilException {
    return (NotifAddressRow[]) getRows(
        SELECT_ALL_NOTIFADDRESS_WITH_GIVEN_USERID, userId).toArray(
        new NotifAddressRow[0]);
  }

  static final private String SELECT_ALL_NOTIFADDRESS_WITH_GIVEN_USERID = "select "
      + NOTIFADDRESS_COLUMNS + " from ST_NotifAddress where userId=?";

  /**
   * Returns all the rows.
   */
  public NotifAddressRow[] getAllRows() throws UtilException {
    return (NotifAddressRow[]) getRows(SELECT_ALL_NOTIFADDRESS).toArray(
        new NotifAddressRow[0]);
  }

  static final private String SELECT_ALL_NOTIFADDRESS = "select "
      + NOTIFADDRESS_COLUMNS + " from ST_NotifAddress";

  /**
   * Returns the unique row given by a no parameters query.
   */
  public NotifAddressRow getNotifAddress(String query) throws UtilException {
    return (NotifAddressRow) getUniqueRow(query);
  }

  /**
   * Returns all the rows given by a no parameters query.
   */
  public NotifAddressRow[] getNotifAddresss(String query) throws UtilException {
    return (NotifAddressRow[]) getRows(query).toArray(new NotifAddressRow[0]);
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
  public void dereferenceNotifChannelId(int notifChannelId)
      throws UtilException {
    NotifAddressRow[] notifAddressToBeDeleted = getAllByNotifChannelId(notifChannelId);
    for (int i = 0; i < notifAddressToBeDeleted.length; i++) {
      delete(notifAddressToBeDeleted[i].getId());
    }
  }

  /**
   * Removes a reference to UserId
   */
  public void dereferenceUserId(int userId) throws UtilException {
    NotifAddressRow[] notifAddressToBeDeleted = getAllByUserId(userId);
    for (int i = 0; i < notifAddressToBeDeleted.length; i++) {
      delete(notifAddressToBeDeleted[i].getId());
    }
  }

  /**
   * Fetch the current NotifAddress row from a resultSet.
   */
  protected Object fetchRow(ResultSet rs) throws SQLException {
    return new NotifAddressRow(rs.getInt("id"), rs.getInt("userId"), rs
        .getString("notifName"), rs.getInt("notifChannelId"), rs
        .getString("address"), rs.getString("usage"), rs.getInt("priority"));
  }

  /**
   * Prepares the statement to update the given row
   */
  protected void prepareUpdate(String updateQuery, PreparedStatement update,
      Object row) throws SQLException {
    NotifAddressRow r = (NotifAddressRow) row;
    update.setInt(1, r.getUserId());
    update.setString(2, truncate(r.getNotifName(), 20));
    update.setInt(3, r.getNotifChannelId());
    update.setString(4, truncate(r.getAddress(), 250));
    update.setString(5, truncate(r.getUsage(), 20));
    update.setInt(6, r.getPriority());
    update.setInt(7, r.getId());
  }

  /**
   * Prepares the statement to insert the given row
   */
  protected void prepareInsert(String insertQuery, PreparedStatement insert,
      Object row) throws SQLException {
    NotifAddressRow r = (NotifAddressRow) row;
    if (r.getId() == -1) {
      r.setId(getNextId());
    }
    insert.setInt(1, r.getId());
    insert.setInt(2, r.getUserId());
    insert.setString(3, truncate(r.getNotifName(), 20));
    insert.setInt(4, r.getNotifChannelId());
    insert.setString(5, truncate(r.getAddress(), 250));
    insert.setString(6, truncate(r.getUsage(), 20));
    insert.setInt(7, r.getPriority());
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
