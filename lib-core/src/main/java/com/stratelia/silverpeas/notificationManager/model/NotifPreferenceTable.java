package com.stratelia.silverpeas.notificationManager.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.stratelia.webactiv.util.AbstractTable;
import com.stratelia.webactiv.util.Schema;
import com.stratelia.webactiv.util.exception.UtilException;

public class NotifPreferenceTable extends AbstractTable {

  /**
   * Builds a new NotifPreferenceTable
   */
  public NotifPreferenceTable(Schema schema) {
    super(schema, "ST_NotifPreference");
  }

  /**
   * The column list used for every select query.
   */
  static final protected String NOTIFPREFERENCE_COLUMNS = "id,notifAddressId,componentInstanceId,userId,messageType";

  /**
   * Returns the unique NotifPreference row having a given id
   */
  public NotifPreferenceRow getNotifPreference(int id) throws UtilException {
    return (NotifPreferenceRow) getUniqueRow(SELECT_NOTIFPREFERENCE_BY_ID, id);
  }

  static final private String SELECT_NOTIFPREFERENCE_BY_ID = "select "
      + NOTIFPREFERENCE_COLUMNS + " from ST_NotifPreference where id = ?";

  /**
   * Returns the unique NotifPreference row having the given
   * userId,componentInstanceId,messageType
   */
  public NotifPreferenceRow getByUserIdAndComponentInstanceIdAndMessageType(
      int userId, int componentInstanceId, int messageType)
      throws UtilException {
    int[] intArgs = { userId, componentInstanceId, messageType };
    return (NotifPreferenceRow) getUniqueRow(
        SELECT_NOTIFPREFERENCE_BY_USERID_AND_COMPONENTINSTANCEID_AND_MESSAGETYPE,
        intArgs);
  }

  static final private String SELECT_NOTIFPREFERENCE_BY_USERID_AND_COMPONENTINSTANCEID_AND_MESSAGETYPE = "select "
      + NOTIFPREFERENCE_COLUMNS
      + " from ST_NotifPreference where "
      + "userId=? and componentInstanceId=? and messageType=?";

  /**
   * Returns all the NotifPreferenceRow having a given componentInstanceId
   */
  public NotifPreferenceRow[] getAllByComponentInstanceId(
      int componentInstanceId) throws UtilException {
    return (NotifPreferenceRow[]) getRows(
        SELECT_ALL_NOTIFPREFERENCE_WITH_GIVEN_COMPONENTINSTANCEID,
        componentInstanceId).toArray(new NotifPreferenceRow[0]);
  }

  static final private String SELECT_ALL_NOTIFPREFERENCE_WITH_GIVEN_COMPONENTINSTANCEID = "select "
      + NOTIFPREFERENCE_COLUMNS
      + " from ST_NotifPreference where componentInstanceId=?";

  /**
   * Returns all the NotifPreferenceRow having a given userId
   */
  public NotifPreferenceRow[] getAllByUserId(int userId) throws UtilException {
    return (NotifPreferenceRow[]) getRows(
        SELECT_ALL_NOTIFPREFERENCE_WITH_GIVEN_USERID, userId).toArray(
        new NotifPreferenceRow[0]);
  }

  static final private String SELECT_ALL_NOTIFPREFERENCE_WITH_GIVEN_USERID = "select "
      + NOTIFPREFERENCE_COLUMNS + " from ST_NotifPreference where userId=?";

  /**
   * Returns all the rows.
   */
  public NotifPreferenceRow[] getAllRows() throws UtilException {
    return (NotifPreferenceRow[]) getRows(SELECT_ALL_NOTIFPREFERENCE).toArray(
        new NotifPreferenceRow[0]);
  }

  static final private String SELECT_ALL_NOTIFPREFERENCE = "select "
      + NOTIFPREFERENCE_COLUMNS + " from ST_NotifPreference";

  /**
   * Returns the unique row given by a no parameters query.
   */
  public NotifPreferenceRow getNotifPreference(String query)
      throws UtilException {
    return (NotifPreferenceRow) getUniqueRow(query);
  }

  /**
   * Returns all the rows given by a no parameters query.
   */
  public NotifPreferenceRow[] getNotifPreferences(String query)
      throws UtilException {
    return (NotifPreferenceRow[]) getRows(query).toArray(
        new NotifPreferenceRow[0]);
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
   * Deletes theNotifPreferenceRow. after having removed all the reference to
   * it.
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
    NotifPreferenceRow[] notifPreferenceToBeDeleted = getAllByComponentInstanceId(componentInstanceId);
    for (int i = 0; i < notifPreferenceToBeDeleted.length; i++) {
      delete(notifPreferenceToBeDeleted[i].getId());
    }
  }

  /**
   * Removes a reference to UserId
   */
  public void dereferenceUserId(int userId) throws UtilException {
    NotifPreferenceRow[] notifPreferenceToBeDeleted = getAllByUserId(userId);
    for (int i = 0; i < notifPreferenceToBeDeleted.length; i++) {
      delete(notifPreferenceToBeDeleted[i].getId());
    }
  }

  /**
   * Fetch the current NotifPreference row from a resultSet.
   */
  protected Object fetchRow(ResultSet rs) throws SQLException {
    return new NotifPreferenceRow(rs.getInt("id"), rs.getInt("notifAddressId"),
        rs.getInt("componentInstanceId"), rs.getInt("userId"), rs
            .getInt("messageType"));
  }

  /**
   * Prepares the statement to update the given row
   */
  protected void prepareUpdate(String updateQuery, PreparedStatement update,
      Object row) throws SQLException {
    NotifPreferenceRow r = (NotifPreferenceRow) row;
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
      Object row) throws SQLException {
    NotifPreferenceRow r = (NotifPreferenceRow) row;
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
