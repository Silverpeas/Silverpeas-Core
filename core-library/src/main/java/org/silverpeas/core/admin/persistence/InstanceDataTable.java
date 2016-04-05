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

package org.silverpeas.core.admin.persistence;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.silverpeas.core.admin.component.model.Parameter;
import org.silverpeas.core.i18n.I18NHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A InstanceData object manages component parameters
 */
public class InstanceDataTable extends Table<InstanceDataRow> {

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
  public void createInstanceData(int componentId, Parameter parameter) throws
      AdminPersistenceException, SQLException {
    InstanceDataRow idr = new InstanceDataRow();

    idr.id = getNextId();
    idr.componentId = componentId;
    idr.name = parameter.getName();
    idr.label = parameter.getLabel().get(I18NHelper.defaultLanguage);
    idr.value = parameter.getValue();

    insertRow(INSERT_INSTANCEDATA, idr);
  }

  static final private String INSERT_INSTANCEDATA = "insert into ST_Instance_Data("
      + INSTANCEDATA_COLUMNS + ") values (?,?,?,?,?)";

  /**
   * Returns the instance whith the given id.
   */
  public InstanceDataRow getInstanceData(int id) throws AdminPersistenceException {
    return getUniqueRow(SELECT_INSTANCEDATA_BY_ID, id);
  }

  static final private String SELECT_INSTANCEDATA_BY_ID = "select "
      + INSTANCEDATA_COLUMNS + " from ST_Instance_Data where id = ?";

  @Override
  protected void prepareInsert(String insertQuery, PreparedStatement insert, InstanceDataRow row)
      throws SQLException {
    insert.setInt(1, row.id);
    insert.setInt(2, row.componentId);
    insert.setString(3, row.name);
    insert.setString(4, row.label);
    insert.setString(5, row.value);
  }

  /**
   * Returns all the parameters of the given component (List of SPParameter)
   */
  public List<Parameter> getAllParametersInComponent(int componentId) throws
      AdminPersistenceException {
    List<InstanceDataRow> rows = getRows(SELECT_ALL_COMPONENT_PARAMETERS, componentId);
    List<Parameter> params = new ArrayList<>();
    for (InstanceDataRow row : rows) {
      Parameter param = new Parameter();
      param.setName(row.name);
      param.setValue(row.value);
      HashMap<String, String> multilang = new HashMap<>();
      multilang.put(I18NHelper.defaultLanguage, row.label);
      param.setLabel(multilang);
      params.add(param);
    }
    return params;
  }

  static final private String SELECT_ALL_COMPONENT_PARAMETERS = "select "
      + INSTANCEDATA_COLUMNS
      + " from ST_Instance_Data where componentId = ? order by id";

  /**
   * Updates a instance data row.
   */
  public void updateInstanceData(int componentId, Parameter parameter) throws
      AdminPersistenceException, SQLException {
    InstanceDataRow idr = new InstanceDataRow();

    idr.componentId = componentId;
    idr.name = parameter.getName();
    idr.value = parameter.getValue();
    int nbRowUpdated = updateRow(UPDATE_INSTANCEDATA, idr);
    if (nbRowUpdated < 1) {
      // no lines has been updated we have to insert it
      createInstanceData(componentId, parameter);
    }
  }

  static final private String UPDATE_INSTANCEDATA = "UPDATE ST_Instance_Data"
      + " SET value = ?" + " where componentId = ? and name = ?";

  /**
   * Removes a instance data row.
   */
  public void removeInstanceData(int id) throws AdminPersistenceException {
    updateRelation(REMOVE_INSTANCEDATA, id);
  }

  static final private String REMOVE_INSTANCEDATA = "delete from ST_Instance_Data"
      + " where componentid = ?";

  /**
   * Fetch the current instanceData row from a resultSet.
   */
  @Override
  protected InstanceDataRow fetchRow(ResultSet rs) throws SQLException {
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

  @Override
  protected void prepareUpdate(String updateQuery, PreparedStatement update,
      InstanceDataRow row) throws SQLException {
    update.setString(1, row.value);
    update.setInt(2, row.componentId);
    update.setString(3, row.name);
  }
}