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
package com.stratelia.webactiv.organization;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.instance.control.SPParameter;
import com.stratelia.webactiv.beans.admin.instance.control.SPParameters;

/**
 * A InstanceData object manages component parameters
 */

public class InstanceDataTable extends Table {
  public InstanceDataTable(OrganizationSchema organization) {
    super(organization, "ST_Instance_Data");
  }

  static final private String INSTANCEDATA_COLUMNS = "id,componentId,name,label,value";

  /**
   * Fetch the current instanceData row from a resultSet.
   */
  protected InstanceDataRow fetchUserSet(ResultSet rs) throws SQLException {
    InstanceDataRow idr = new InstanceDataRow();

    idr.id = rs.getInt(1);
    idr.componentId = rs.getInt(2);
    idr.name = rs.getString(3);
    idr.label = rs.getString(4);
    idr.value = rs.getString(5);

    return idr;
  }

  /**
   * Inserts in the database a new instanceData row.
   */
  public void createInstanceData(int componentId, SPParameter parameter)
      throws AdminPersistenceException, SQLException {
    SilverTrace.info("admin", "InstanceDataTable.createInstanceData",
        "root.MSG_GEN_PARAM_VALUE", "componentId = " + componentId
        + ", parameter = " + parameter.toString());
    InstanceDataRow idr = new InstanceDataRow();

    idr.id = getNextId();
    idr.componentId = componentId;
    idr.name = parameter.getName();
    idr.label = parameter.getLabel();
    idr.value = parameter.getValue();

    insertRow(INSERT_INSTANCEDATA, idr);
  }

  static final private String INSERT_INSTANCEDATA = "insert into ST_Instance_Data("
      + INSTANCEDATA_COLUMNS + ") values (?,?,?,?,?)";

  /**
   * Returns the instance whith the given id.
   */
  public InstanceDataRow getInstanceData(int id)
      throws AdminPersistenceException {
    return (InstanceDataRow) getUniqueRow(SELECT_INSTANCEDATA_BY_ID, id);
  }

  static final private String SELECT_INSTANCEDATA_BY_ID = "select "
      + INSTANCEDATA_COLUMNS + " from ST_Instance_Data where id = ?";

  protected void prepareInsert(String insertQuery, PreparedStatement insert,
      Object row) throws SQLException {
    InstanceDataRow idr = (InstanceDataRow) row;

    insert.setInt(1, idr.id);
    insert.setInt(2, idr.componentId);
    insert.setString(3, idr.name);
    insert.setString(4, idr.label);
    insert.setString(5, idr.value);
  }

  /**
   * Returns all the parameters of the given component (List of SPParameter)
   */
  @SuppressWarnings("unchecked")
  public SPParameters getAllParametersInComponent(int componentId)
      throws AdminPersistenceException {
    List<InstanceDataRow> rows = (List<InstanceDataRow>) getRows(SELECT_ALL_COMPONENT_PARAMETERS, componentId);
    SPParameters params = new SPParameters();
    for(InstanceDataRow row : rows) {
      SPParameter param = new SPParameter(row.name, row.value, row.label);
      params.addParameter(param);
    }

    return params;
  }

  static final private String SELECT_ALL_COMPONENT_PARAMETERS = "select "
      + INSTANCEDATA_COLUMNS
      + " from ST_Instance_Data where componentId = ? order by id";

  /**
   * Updates a instance data row.
   */
  public void updateInstanceData(int componentId, SPParameter parameter)
      throws AdminPersistenceException, SQLException {
    InstanceDataRow idr = new InstanceDataRow();

    idr.componentId = componentId;
    idr.name = parameter.getName();
    idr.value = parameter.getValue();
    int nbRowUpdated = updateRow(UPDATE_INSTANCEDATA, idr);
    if (nbRowUpdated < 1) {
      // no lines has been updated
      // we have to insert it
      createInstanceData(componentId, parameter);
    }
  }

  static final private String UPDATE_INSTANCEDATA = "UPDATE ST_Instance_Data"
      + " SET value = ?" + " where componentId = ? and name = ?";

  /**
   * Removes a instance data row.
   */
  public void removeInstanceData(int id) throws AdminPersistenceException {
    // InstanceDataRow idr = getInstanceData(id);
    // if (idr == null) return;

    updateRelation(REMOVE_INSTANCEDATA, id);
  }

  static final private String REMOVE_INSTANCEDATA = "delete from ST_Instance_Data"
      + " where componentid = ?";

  /**
   * Fetch the current instanceData row from a resultSet.
   */
  protected Object fetchRow(ResultSet rs) throws SQLException {
    return fetchInstanceData(rs);
  }

  /**
   * Fetch the current instanceData row from a resultSet.
   */
  protected InstanceDataRow fetchInstanceData(ResultSet rs) throws SQLException {
    InstanceDataRow idr = new InstanceDataRow();

    idr.id = rs.getInt(1);
    idr.componentId = rs.getInt(2);
    idr.name = rs.getString(3);
    idr.label = rs.getString(4);
    idr.value = rs.getString(5);

    return idr;
  }

  protected void prepareUpdate(String updateQuery, PreparedStatement update,
      Object row) throws SQLException {
    InstanceDataRow idr = (InstanceDataRow) row;

    update.setString(1, idr.value);
    update.setInt(2, idr.componentId);
    update.setString(3, idr.name);
  }
}