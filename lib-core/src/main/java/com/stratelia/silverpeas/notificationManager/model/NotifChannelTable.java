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
package com.stratelia.silverpeas.notificationManager.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.stratelia.webactiv.util.AbstractTable;
import com.stratelia.webactiv.util.Schema;
import com.stratelia.webactiv.util.exception.UtilException;

public class NotifChannelTable extends AbstractTable {

  /**
   * Builds a new NotifChannelTable
   */
  public NotifChannelTable(Schema schema) {
    super(schema, "ST_NotifChannel");
  }

  /**
   * The column list used for every select query.
   */
  static final protected String NOTIFCHANNEL_COLUMNS =
      "id,name,description,couldBeAdded,fromAvailable,subjectAvailable";

  /**
   * Returns the unique NotifChannel row having a given id
   */
  public NotifChannelRow getNotifChannel(int id) throws UtilException {
    return (NotifChannelRow) getUniqueRow(SELECT_NOTIFCHANNEL_BY_ID, id);
  }

  static final private String SELECT_NOTIFCHANNEL_BY_ID = "select "
      + NOTIFCHANNEL_COLUMNS + " from ST_NotifChannel Where id = ?";

  /**
   * Returns all the rows.
   */
  public NotifChannelRow[] getAllRows() throws UtilException {
    return (NotifChannelRow[]) getRows(SELECT_ALL_NOTIFCHANNEL).toArray(
        new NotifChannelRow[0]);
  }

  static final private String SELECT_ALL_NOTIFCHANNEL = "select "
      + NOTIFCHANNEL_COLUMNS + " from ST_NotifChannel";

  /**
   * Returns the unique row given by a no parameters query.
   */
  public NotifChannelRow getNotifChannel(String query) throws UtilException {
    return (NotifChannelRow) getUniqueRow(query);
  }

  /**
   * Returns all the rows given by a no parameters query.
   */
  public NotifChannelRow[] getNotifChannels(String query) throws UtilException {
    return (NotifChannelRow[]) getRows(query).toArray(new NotifChannelRow[0]);
  }

  /**
   * Inserts in the database a new NotifChannel row.
   */
  public int create(NotifChannelRow notifChannel) throws UtilException {
    insertRow(INSERT_NOTIFCHANNEL, notifChannel);
    return notifChannel.getId();
  }

  static final private String INSERT_NOTIFCHANNEL = "insert into"
      + " ST_NotifChannel (id, name, description, couldBeAdded, fromAvailable, subjectAvailable)"
      + " values  (?, ?, ?, ?, ?, ?)";

  /**
   * Update the given NotifChannelRow
   */
  public void update(NotifChannelRow notifChannel) throws UtilException {
    updateRow(UPDATE_NOTIFCHANNEL, notifChannel);
  }

  static final private String UPDATE_NOTIFCHANNEL = "Update ST_NotifChannel set"
      + " name = ?,"
      + " description = ?,"
      + " couldBeAdded = ?,"
      + " fromAvailable = ?," + " subjectAvailable = ?" + " Where id = ?";

  /**
   * Updates theNotifChannel row. or inserts it if new.
   */
  public void save(NotifChannelRow notifChannel) throws UtilException {
    if (notifChannel.getId() == -1) {
      // No id : it's a creation
      create(notifChannel);
    } else {
      update(notifChannel);
    }
  }

  /**
   * Deletes theNotifChannelRow. after having removed all the reference to it.
   */
  public void delete(int id) throws UtilException {
    ((NotifSchema) schema).notifAddress.dereferenceNotifChannelId(id);
    updateRelation(DELETE_NOTIFCHANNEL, id);
  }

  static final private String DELETE_NOTIFCHANNEL = "delete from ST_NotifChannel where id=?";

  /**
   * Fetch the current NotifChannel row from a resultSet.
   */
  protected Object fetchRow(ResultSet rs) throws SQLException {
    return new NotifChannelRow(rs.getInt("id"), rs.getString("name"), rs
        .getString("description"), rs.getString("couldBeAdded"), rs
        .getString("fromAvailable"), rs.getString("subjectAvailable"));
  }

  /**
   * Prepares the statement to update the given row
   */
  protected void prepareUpdate(String updateQuery, PreparedStatement update,
      Object row) throws SQLException {
    NotifChannelRow r = (NotifChannelRow) row;
    update.setString(1, truncate(r.getName(), 20));
    update.setString(2, truncate(r.getDescription(), 200));
    update.setString(3, truncate(r.getCouldBeAdded(), 1));
    update.setString(4, truncate(r.getFromAvailable(), 1));
    update.setString(5, truncate(r.getSubjectAvailable(), 1));
    update.setInt(6, r.getId());
  }

  /**
   * Prepares the statement to insert the given row
   */
  protected void prepareInsert(String insertQuery, PreparedStatement insert,
      Object row) throws SQLException {
    NotifChannelRow r = (NotifChannelRow) row;
    if (r.getId() == -1) {
      r.setId(getNextId());
    }
    insert.setInt(1, r.getId());
    insert.setString(2, truncate(r.getName(), 20));
    insert.setString(3, truncate(r.getDescription(), 200));
    insert.setString(4, truncate(r.getCouldBeAdded(), 1));
    insert.setString(5, truncate(r.getFromAvailable(), 1));
    insert.setString(6, truncate(r.getSubjectAvailable(), 1));
  }

}
