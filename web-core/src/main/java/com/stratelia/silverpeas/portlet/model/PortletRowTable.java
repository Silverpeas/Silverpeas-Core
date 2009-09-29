package com.stratelia.silverpeas.portlet.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.stratelia.webactiv.util.AbstractTable;
import com.stratelia.webactiv.util.Schema;
import com.stratelia.webactiv.util.exception.UtilException;

public class PortletRowTable extends AbstractTable {

  /**
   * Builds a new PortletRowTable
   */
  public PortletRowTable(Schema schema) {
    super(schema, "ST_PortletRow");
  }

  /**
   * The column list used for every select query.
   */
  static final protected String PORTLETROW_COLUMNS = "id,InstanceId,portletColumnId,rowHeight,nbRow";

  /**
   * Returns the unique PortletRow row having a given id
   */
  public PortletRowRow getPortletRow(int id) throws UtilException {
    return (PortletRowRow) getUniqueRow(SELECT_PORTLETROW_BY_ID, id);
  }

  static final private String SELECT_PORTLETROW_BY_ID = "Select "
      + PORTLETROW_COLUMNS + " from ST_PortletRow Where id = ?";

  /**
   * Returns all the PortletRow rows having a given portletColumnId
   */
  public PortletRowRow[] getAllByPortletColumnId(int aPortletColumnId,
      String orderField) throws UtilException {
    String req = "select " + PORTLETROW_COLUMNS + " from ST_PortletRow"
        + " Where PortletColumnId = " + aPortletColumnId;
    if (orderField != null) {
      req = req + " order by " + orderField;
    }
    return (PortletRowRow[]) getRows(req).toArray(new PortletRowRow[0]);
  }

  /**
   * Returns all the PortletRowRow having a given instanceId
   */
  public PortletRowRow[] getAllByInstanceId(int InstanceId)
      throws UtilException {
    return (PortletRowRow[]) getRows(
        SELECT_ALL_PORTLETROW_WITH_GIVEN_INSTANCEID, InstanceId).toArray(
        new PortletRowRow[0]);
  }

  static final private String SELECT_ALL_PORTLETROW_WITH_GIVEN_INSTANCEID = "select "
      + PORTLETROW_COLUMNS + " from ST_PortletRow where InstanceId=?";

  /**
   * Returns all the PortletRowRow having a given portletColumnId
   */
  public PortletRowRow[] getAllByPortletColumnId(int portletColumnId)
      throws UtilException {
    return (PortletRowRow[]) getRows(
        SELECT_ALL_PORTLETROW_WITH_GIVEN_PORTLETCOLUMNID, portletColumnId)
        .toArray(new PortletRowRow[0]);
  }

  static final private String SELECT_ALL_PORTLETROW_WITH_GIVEN_PORTLETCOLUMNID = "select "
      + PORTLETROW_COLUMNS + " from ST_PortletRow where portletColumnId=?";

  /**
   * Returns all the rows.
   */
  public PortletRowRow[] getAllRows() throws UtilException {
    return (PortletRowRow[]) getRows(SELECT_ALL_PORTLETROW).toArray(
        new PortletRowRow[0]);
  }

  static final private String SELECT_ALL_PORTLETROW = "select "
      + PORTLETROW_COLUMNS + " from ST_PortletRow";

  /**
   * Returns the unique row given by a no parameters query.
   */
  public PortletRowRow getPortletRow(String query) throws UtilException {
    return (PortletRowRow) getUniqueRow(query);
  }

  /**
   * Returns all the rows given by a no parameters query.
   */
  public PortletRowRow[] getPortletRows(String query) throws UtilException {
    return (PortletRowRow[]) getRows(query).toArray(new PortletRowRow[0]);
  }

  /**
   * Inserts in the database a new PortletRow row.
   */
  public int create(PortletRowRow portletRow) throws UtilException {
    insertRow(INSERT_PORTLETROW, portletRow);
    return portletRow.getId();
  }

  static final private String INSERT_PORTLETROW = "insert into"
      + " ST_PortletRow (id, InstanceId, portletColumnId, rowHeight, nbRow)"
      + " values  (?, ?, ?, ?, ?)";

  /**
   * Update the given PortletRowRow
   */
  public void update(PortletRowRow portletRow) throws UtilException {
    updateRow(UPDATE_PORTLETROW, portletRow);
  }

  static final private String UPDATE_PORTLETROW = "Update ST_PortletRow set"
      + " InstanceId = ?," + " portletColumnId = ?," + " rowHeight = ?,"
      + " nbRow = ?" + " Where id = ?";

  /**
   * Updates thePortletRow row. or inserts it if new.
   */
  public void save(PortletRowRow portletRow) throws UtilException {
    if (portletRow.getId() == -1) {
      // No id : it's a creation
      create(portletRow);
    } else {
      update(portletRow);
    }
  }

  /**
   * Deletes thePortletRowRow. after having removed all the reference to it.
   */
  public void delete(int id) throws UtilException {
    ((PortletSchema) schema).portletState.dereferencePortletRowId(id);
    updateRelation(DELETE_PORTLETROW, id);
  }

  static final private String DELETE_PORTLETROW = "delete from ST_PortletRow where id=?";

  /**
   * Removes a reference to InstanceId
   */
  public PortletRowRow[] dereferenceInstanceId(int InstanceId)
      throws UtilException {
    PortletRowRow[] portletRowToBeDeleted = getAllByInstanceId(InstanceId);
    for (int i = 0; i < portletRowToBeDeleted.length; i++) {
      delete(portletRowToBeDeleted[i].getId());
    }

    return portletRowToBeDeleted;
  }

  /**
   * Removes a reference to PortletColumnId
   */
  public PortletRowRow[] dereferencePortletColumnId(int portletColumnId)
      throws UtilException {
    PortletRowRow[] portletRowToBeDeleted = getAllByPortletColumnId(portletColumnId);
    for (int i = 0; i < portletRowToBeDeleted.length; i++) {
      delete(portletRowToBeDeleted[i].getId());
    }
    return portletRowToBeDeleted;
  }

  /**
   * Fetch the current PortletRow row from a resultSet.
   */
  protected Object fetchRow(ResultSet rs) throws SQLException {
    return new PortletRowRow(rs.getInt("id"), rs.getInt("InstanceId"), rs
        .getInt("portletColumnId"), rs.getInt("rowHeight"), rs.getInt("nbRow"));
  }

  /**
   * Prepares the statement to update the given row
   */
  protected void prepareUpdate(String updateQuery, PreparedStatement update,
      Object row) throws SQLException {
    PortletRowRow r = (PortletRowRow) row;
    update.setInt(1, r.getInstanceId());
    update.setInt(2, r.getPortletColumnId());
    update.setInt(3, r.getRowHeight());
    update.setInt(4, r.getNbRow());
    update.setInt(5, r.getId());
  }

  /**
   * Prepares the statement to insert the given row
   */
  protected void prepareInsert(String insertQuery, PreparedStatement insert,
      Object row) throws SQLException {
    PortletRowRow r = (PortletRowRow) row;
    if (r.getId() == -1) {
      r.setId(getNextId());
    }
    insert.setInt(1, r.getId());
    insert.setInt(2, r.getInstanceId());
    insert.setInt(3, r.getPortletColumnId());
    insert.setInt(4, r.getRowHeight());
    insert.setInt(5, r.getNbRow());
  }
}
