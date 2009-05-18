package com.stratelia.silverpeas.notificationManager.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.stratelia.webactiv.util.AbstractTable;
import com.stratelia.webactiv.util.Schema;
import com.stratelia.webactiv.util.exception.UtilException;

public class NotifDefaultAddressTable extends AbstractTable {

  /**
   * Builds a new NotifDefaultAddressTable
   */
  public NotifDefaultAddressTable(Schema schema)
  {
    super(schema, "ST_NotifDefaultAddress");
  }

  /**
   * The column list used for every select query.
   */
  static final protected String NOTIFDEFAULTADDRESS_COLUMNS
    = "id,userId,notifAddressId";

  /**
   * Returns the unique NotifDefaultAddress row having a given id
   */
  public NotifDefaultAddressRow getNotifDefaultAddress(int id)
    throws UtilException
  {
    return (NotifDefaultAddressRow) getUniqueRow(SELECT_NOTIFDEFAULTADDRESS_BY_ID,id);
  }

  static final private String SELECT_NOTIFDEFAULTADDRESS_BY_ID
    = "select " + NOTIFDEFAULTADDRESS_COLUMNS + " from ST_NotifDefaultAddress Where id = ?";


  /**
   * Returns all the NotifDefaultAddressRow having a given userId
   */
  public NotifDefaultAddressRow[] getAllByUserId(int userId)
    throws UtilException
  {
    return (NotifDefaultAddressRow[]) getRows(SELECT_ALL_NOTIFDEFAULTADDRESS_WITH_GIVEN_USERID,userId)
           .toArray(new NotifDefaultAddressRow[0]);
  }

  static final private String SELECT_ALL_NOTIFDEFAULTADDRESS_WITH_GIVEN_USERID = "select " + NOTIFDEFAULTADDRESS_COLUMNS + " from ST_NotifDefaultAddress where userId=?";

  /**
   * Returns all the rows.
   */
  public NotifDefaultAddressRow[] getAllRows() throws UtilException
  {
    return (NotifDefaultAddressRow[]) getRows(SELECT_ALL_NOTIFDEFAULTADDRESS).toArray(new NotifDefaultAddressRow[0]);
  }

  static final private String SELECT_ALL_NOTIFDEFAULTADDRESS
    = "select " + NOTIFDEFAULTADDRESS_COLUMNS + " from ST_NotifDefaultAddress";

  /**
   * Returns the unique row
   * given by a no parameters query.
   */
  public NotifDefaultAddressRow getNotifDefaultAddress(String query) throws UtilException
  {
    return (NotifDefaultAddressRow) getUniqueRow(query);
  }

  /**
   * Returns all the rows
   * given by a no parameters query.
   */
  public NotifDefaultAddressRow[] getNotifDefaultAddresss(String query) throws UtilException
  {
    return (NotifDefaultAddressRow[]) getRows(query)
           .toArray(new NotifDefaultAddressRow[0]);
  }

  /**
   * Inserts in the database a new NotifDefaultAddress row.
   */
  public int create(NotifDefaultAddressRow notifDefaultAddress)
    throws UtilException
  {
    insertRow(INSERT_NOTIFDEFAULTADDRESS, notifDefaultAddress);
    return notifDefaultAddress.getId();
  }
  static final private String INSERT_NOTIFDEFAULTADDRESS
    = "insert into"
    + " ST_NotifDefaultAddress (id, userId, notifAddressId)"
    + " values  (?, ?, ?)";

  /**
   * Update the given NotifDefaultAddressRow
   */
  public void update(NotifDefaultAddressRow notifDefaultAddress) throws UtilException
  {
    updateRow(UPDATE_NOTIFDEFAULTADDRESS, notifDefaultAddress);
  }
  static final private String UPDATE_NOTIFDEFAULTADDRESS
    = "update ST_NotifDefaultAddress set"
    + " userId = ?,"
    + " notifAddressId = ?"
    + " Where id = ?";

  /**
   * Updates theNotifDefaultAddress row.
   * or inserts it if new.
   */
  public void save(NotifDefaultAddressRow notifDefaultAddress)
    throws UtilException
  {
    if (notifDefaultAddress.getId() == -1) {
      // No id : it's a creation
      create(notifDefaultAddress) ;
    } else {
      update(notifDefaultAddress) ;
    }
  }

  /**
   * Deletes theNotifDefaultAddressRow.
   * after having removed all the reference to it.
   */
  public void delete(int id)
     throws UtilException
  {
    updateRelation(DELETE_NOTIFDEFAULTADDRESS, id);
  }

    static final private String DELETE_NOTIFDEFAULTADDRESS
       = "delete from ST_NotifDefaultAddress where id=?";

  /**
   * Removes a reference to UserId
   */
  public void dereferenceUserId(int userId)
    throws UtilException
  {
    NotifDefaultAddressRow[] notifDefaultAddressToBeDeleted
       = getAllByUserId(userId);
    for (int i=0; i<notifDefaultAddressToBeDeleted.length; i++)
    {
       delete(notifDefaultAddressToBeDeleted[i].getId());
    }
  }


  /**
   * Fetch the current NotifDefaultAddress row from a resultSet.
   */
  protected Object fetchRow(ResultSet rs)
     throws SQLException
  {
    return new NotifDefaultAddressRow(
      rs.getInt("id"),
      rs.getInt("userId"),
      rs.getInt("notifAddressId")) ;
  }

  /**
   * Prepares the statement to update the given row
   */
  protected void prepareUpdate(String updateQuery,
                               PreparedStatement update,
                               Object row)
    throws SQLException
  {
    NotifDefaultAddressRow r = (NotifDefaultAddressRow) row;
    update.setInt(1, r.getUserId());
    update.setInt(2, r.getNotifAddressId());
    update.setInt(3, r.getId());
  }

  /**
   * Prepares the statement to insert the given row
   */
  protected void prepareInsert(String insertQuery,
                               PreparedStatement insert,
                               Object row)
    throws SQLException
  {
    NotifDefaultAddressRow r = (NotifDefaultAddressRow) row;
    if (r.getId() == -1)
    {
      r.setId(getNextId());
    }
    insert.setInt(1, r.getId());
    insert.setInt(2, r.getUserId());
    insert.setInt(3, r.getNotifAddressId());
  }

}
