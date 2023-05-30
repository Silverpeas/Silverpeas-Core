/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.admin.persistence;

import org.silverpeas.core.admin.component.model.LocalizedParameter;
import org.silverpeas.core.admin.component.model.Parameter;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.StringUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.silverpeas.core.util.CollectionUtil.isNotEmpty;

/**
 * A InstanceData object manages component parameters
 */
@Repository
public class InstanceDataTable extends Table<InstanceDataRow> {

  private static final String INSTANCE_DATA_TABLE = "ST_Instance_Data";
  private static final String INSTANCEDATA_COLUMNS = "id,componentId,name,label,value";
  private static final String INSERT_INSTANCEDATA = "insert into ST_Instance_Data("
      + INSTANCEDATA_COLUMNS + ") values (?,?,?,?,?)";
  private static final String SELECT_ALL_COMPONENTS_BY_PARAMETER_VALUE =
      "select " + INSTANCEDATA_COLUMNS +
          " from ST_Instance_Data where name = ? and value = ? order by id";
  private static final String SELECT_ALL_COMPONENT_PARAMETERS = "select " + INSTANCEDATA_COLUMNS
      + " from ST_Instance_Data where componentId = ? order by id";
  private static final String UPDATE_INSTANCEDATA = "UPDATE ST_Instance_Data"
      + " SET value = ?" + " where componentId = ? and name = ?";
  private static final String REMOVE_INSTANCEDATA = "delete from ST_Instance_Data"
      + " where componentid = ?";

  public InstanceDataTable() {
    super(INSTANCE_DATA_TABLE);
  }

  /**
   * Inserts in the database a new instanceData row.
   */
  public void createInstanceData(int componentId, LocalizedParameter parameter) throws SQLException {
    InstanceDataRow idr = new InstanceDataRow();
    idr.id = getNextId();
    idr.componentId = componentId;
    idr.name = parameter.getName();
    idr.label = parameter.getLabel();
    idr.value = parameter.getValue();

    insertRow(INSERT_INSTANCEDATA, idr);
  }

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
      SQLException {
    return getRows(SELECT_ALL_COMPONENT_PARAMETERS, componentId).stream()
        .map(this::asParameter)
        .collect(Collectors.toList());
  }

  private Parameter asParameter(final InstanceDataRow row) {
    Parameter param = new Parameter();
    param.setName(row.name);
    param.setValue(row.value);
    param.putLabel(I18NHelper.DEFAULT_LANGUAGE, row.label);
    return param;
  }

  /**
   * Get the value of given parameter and about given component.
   * @param componentId component identifier.
   * @param paramName parameter name.
   * @param ignoreCase true to ignore case on parameter name.
   * @return return the value as string, or {@link StringUtil#EMPTY} if parameter has not been
   * found.
   * @throws SQLException on database error.
   */
  public String getParameterValueByComponentAndParamName(final Integer componentId,
      final String paramName, final boolean ignoreCase) throws SQLException {
    final Mutable<String> result = Mutable.empty();
    final JdbcSqlQuery query = JdbcSqlQuery.createSelect("value")
        .from(INSTANCE_DATA_TABLE)
        .where("componentId = ?", componentId);
    if (ignoreCase) {
      query.and("lower(name) = ?", paramName.toLowerCase());
    } else {
      query.and("name = ?", paramName);
    }
    query.execute(r -> {
      result.set(r.getString(1));
      return null;
    });
    return result.orElse(StringUtil.EMPTY);
  }

  /**
   * Gets all parameters values by component and by parameter name.
   * @param componentIds list of component identifier.
   * @param paramNames optional list of parameter name. All parameters are retrieved if it is not
   * filled or null
   * @throws SQLException on database error.
   */
  public Map<Integer, Map<String, String>> getParameterValuesByComponentAndByParamName(
      final Collection<Integer> componentIds, final Collection<String> paramNames) throws SQLException {
    final Map<Integer, Map<String, String>> result = new HashMap<>(componentIds.size());
    JdbcSqlQuery.executeBySplittingOn(componentIds, (idBatch, ignore) -> {
      final JdbcSqlQuery query = JdbcSqlQuery
          .createSelect("componentId,name,value")
          .from(INSTANCE_DATA_TABLE)
          .where("componentId").in(idBatch);
      if (isNotEmpty(paramNames)) {
        query.and("name").in(paramNames);
      }
      query.execute(r -> {
        final int componentId = r.getInt(1);
        final String name = r.getString(2);
        final String value = r.getString(3);
        final Map<String, String> parameters = result
            .computeIfAbsent(componentId, i -> new HashMap<>());
        parameters.put(name, value);
        return null;
      });
    });
    return result;
  }

  /**
   * Returns all component ids according to given param and param value
   */
  public List<Integer> getComponentIdsWithParameterValue(Parameter param) throws SQLException {
    List<String> queryParams = new ArrayList<>();
    queryParams.add(param.getName());
    queryParams.add(param.getValue());
    List<InstanceDataRow> rows = getRows(SELECT_ALL_COMPONENTS_BY_PARAMETER_VALUE, queryParams);
    List<Integer> ids = new ArrayList<>();
    for (InstanceDataRow row : rows) {
      ids.add(row.componentId);
    }
    return ids;
  }

  /**
   * Updates a instance data row.
   */
  public void updateInstanceData(int componentId, LocalizedParameter parameter) throws SQLException {
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

  /**
   * Removes a instance data row.
   */
  public void removeInstanceData(int id) throws SQLException {
    updateRelation(REMOVE_INSTANCEDATA, id);
  }

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