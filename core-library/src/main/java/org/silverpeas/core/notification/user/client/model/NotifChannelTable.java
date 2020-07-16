/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.notification.user.client.model;

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.jdbc.AbstractTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class NotifChannelTable extends AbstractTable<NotifChannelRow> {

  /**
   * Builds a new NotifChannelTable
   */
  NotifChannelTable() {
    super("ST_NotifChannel");
  }

  /**
   * The column list used for every select query.
   */
  protected static final String NOTIFCHANNEL_COLUMNS =
      "id,name,description,couldBeAdded,fromAvailable,subjectAvailable";

  /**
   * Returns the unique NotifChannel row having a given id
   */
  public NotifChannelRow getNotifChannel(int id) throws SQLException {
    return getUniqueRow(SELECT_NOTIFCHANNEL_BY_ID, id);
  }

  private static final String SELECT_NOTIFCHANNEL_BY_ID = "select "
      + NOTIFCHANNEL_COLUMNS + " from ST_NotifChannel Where id = ?";

  /**
   * Returns all the rows.
   */
  public NotifChannelRow[] getAllRows() throws SQLException {
    List<NotifChannelRow> rows = getRows(SELECT_ALL_NOTIFCHANNEL);
    return rows.toArray(new NotifChannelRow[rows.size()]);
  }

  private static final String SELECT_ALL_NOTIFCHANNEL = "select "
      + NOTIFCHANNEL_COLUMNS + " from ST_NotifChannel";

  /**
   * Returns the unique row given by a no parameters query.
   */
  public NotifChannelRow getNotifChannel(String query) throws SQLException {
    return getUniqueRow(query);
  }

  /**
   * Returns all the rows given by a no parameters query.
   */
  public NotifChannelRow[] getNotifChannels(String query) throws SQLException {
    List<NotifChannelRow> rows = getRows(query);
    return rows.toArray(new NotifChannelRow[rows.size()]);
  }

  /**
   * Inserts in the database a new NotifChannel row.
   */
  public int create(NotifChannelRow notifChannel) throws SQLException {
    insertRow(INSERT_NOTIFCHANNEL, notifChannel);
    return notifChannel.getId();
  }

  private static final String INSERT_NOTIFCHANNEL = "insert into"
      + " ST_NotifChannel (id, name, description, couldBeAdded, fromAvailable, subjectAvailable)"
      + " values  (?, ?, ?, ?, ?, ?)";

  /**
   * Update the given NotifChannelRow
   */
  public void update(NotifChannelRow notifChannel) throws SQLException {
    updateRow(UPDATE_NOTIFCHANNEL, notifChannel);
  }

  private static final String UPDATE_NOTIFCHANNEL = "Update ST_NotifChannel set"
      + " name = ?,"
      + " description = ?,"
      + " couldBeAdded = ?,"
      + " fromAvailable = ?," + " subjectAvailable = ?" + " Where id = ?";

  /**
   * Updates theNotifChannel row. or inserts it if new.
   */
  public void save(NotifChannelRow notifChannel) throws SQLException {
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
  public void delete(int id) throws SQLException {
    NotificationSchema.get().notifAddress().dereferenceNotifChannelId(id);
    updateRelation(DELETE_NOTIFCHANNEL, id);
  }

  private static final String DELETE_NOTIFCHANNEL = "delete from ST_NotifChannel where id=?";

  /**
   * Fetch the current NotifChannel row from a resultSet.
   */
  protected NotifChannelRow fetchRow(ResultSet rs) throws SQLException {
    return new NotifChannelRow(rs.getInt("id"), rs.getString("name"), rs
        .getString("description"), rs.getString("couldBeAdded"), rs
        .getString("fromAvailable"), rs.getString("subjectAvailable"));
  }

  /**
   * Prepares the statement to update the given row
   */
  protected void prepareUpdate(String updateQuery, PreparedStatement update,
      NotifChannelRow r) throws SQLException {
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
      NotifChannelRow r) throws SQLException {
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
