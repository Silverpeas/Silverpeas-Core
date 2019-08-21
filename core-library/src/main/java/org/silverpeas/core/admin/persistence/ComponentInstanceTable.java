/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.admin.persistence;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.persistence.jdbc.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import static org.silverpeas.core.SilverpeasExceptionMessages.unknown;

/**
 * A ComponentInstanceTable object manages the ST_ComponentInstance table.
 */
public class ComponentInstanceTable extends Table<ComponentInstanceRow> {

  private static final String INSTANCE_COLUMNS =
      "id,spaceId,name,componentName,description,createdBy,orderNum,createTime,updateTime," +
          "removeTime,componentStatus,updatedBy,removedBy,isPublic,isHidden,lang," +
          "isInheritanceBlocked";
  private static final String SELECT = "select ";
  private static final String SELECT_INSTANCE_BY_ID =
      SELECT + INSTANCE_COLUMNS + " from ST_ComponentInstance where id = ?";
  private static final String DELETE_INSTANCE = "delete from ST_ComponentInstance where id = ?";

  public ComponentInstanceTable() {
    super("ST_ComponentInstance");
  }

  /**
   * Fetch the current instance row from a resultSet.
   * @param rs result set
   * @return the current instance row from a resultSet.
   * @throws SQLException on error
   */
  protected ComponentInstanceRow fetchComponentInstance(ResultSet rs) throws SQLException {
    ComponentInstanceRow i = new ComponentInstanceRow();
    i.id = rs.getInt(1);
    i.spaceId = rs.getInt(2);
    i.name = rs.getString(3);
    i.componentName = rs.getString(4);
    i.description = getNotNullString(rs.getString(5));
    i.createdBy = rs.getInt(6);
    if (rs.wasNull()) {
      i.createdBy = -1;
    }
    i.orderNum = rs.getInt(7);
    if (rs.wasNull()) {
      i.orderNum = -1;
    }

    i.createTime = rs.getString(8);
    i.updateTime = rs.getString(9);
    i.removeTime = rs.getString(10);
    i.status = rs.getString(11);

    i.updatedBy = rs.getInt(12);
    if (rs.wasNull()) {
      i.updatedBy = -1;
    }

    i.removedBy = rs.getInt(13);
    if (rs.wasNull()) {
      i.removedBy = -1;
    }

    i.publicAccess = rs.getInt(14);
    i.hidden = rs.getInt(15);

    i.lang = rs.getString(16);

    i.inheritanceBlocked = rs.getInt(17);

    return i;
  }

  /**
   * Returns the instance with the given id.
   * @param id the unique identifier of the component instance
   * @return the instance with the given id.
   * @throws SQLException on error
   */
  public ComponentInstanceRow getComponentInstance(int id) throws SQLException {
    return getUniqueRow(SELECT_INSTANCE_BY_ID, id);
  }

  /**
   * Returns the ComponentInstance of a given user role.
   * @param userRoleId id of user role
   * @return the ComponentInstance of a given user role.
   * @throws SQLException on error
   */
  public ComponentInstanceRow getComponentInstanceOfUserRole(int userRoleId)
      throws SQLException {
    return getUniqueRow(SELECT_USERROLE_INSTANCE, userRoleId);
  }

  private static final String SELECT_USERROLE_INSTANCE =
      SELECT + Table.aliasColumns("i", INSTANCE_COLUMNS) +
          " from ST_ComponentInstance i, ST_UserRole us" +
          " where i.id = us.instanceId and us.id = ?";

  /**
   * Returns all the instances in a given space
   * @param spaceId the space id
   * @return all the instances in a given space
   * @throws SQLException on error
   */
  public ComponentInstanceRow[] getAllComponentInstancesInSpace(int spaceId)
      throws SQLException {
    List<ComponentInstanceRow> rows = getRows(SELECT_ALL_SPACE_INSTANCES, spaceId);
    return rows.toArray(new ComponentInstanceRow[rows.size()]);
  }

  private static final String SELECT_ALL_SPACE_INSTANCES = SELECT + INSTANCE_COLUMNS +
      " from ST_ComponentInstance where spaceId = ? and componentStatus is null" +
      " order by orderNum";

  /**
   * Returns all the instance ids in a given space
   * @param spaceId the space id
   * @return all the instance ids in a given space
   * @throws SQLException on error
   */
  public String[] getAllComponentInstanceIdsInSpace(int spaceId) throws SQLException {
    List<String> ids = getIds(SELECT_ALL_SPACE_INSTANCE_IDS, spaceId);
    return ids.toArray(new String[ids.size()]);
  }

  private static final String SELECT_ALL_SPACE_INSTANCE_IDS =
      "select id from ST_ComponentInstance " + "where spaceId = ? and componentStatus is null" +
          " order by orderNum";

  /**
   * Returns all components which has been removed but not definitely deleted
   * @return all components which has been removed but not definitely deleted
   * @throws SQLException on error
   */
  public ComponentInstanceRow[] getRemovedComponents() throws SQLException {
    List<ComponentInstanceRow> rows = getRows(SELECT_REMOVED_COMPONENTS);
    return rows.toArray(new ComponentInstanceRow[rows.size()]);
  }

  private static final String SELECT_REMOVED_COMPONENTS =
      SELECT + INSTANCE_COLUMNS + " from ST_ComponentInstance where componentStatus = '" +
          ComponentInst.STATUS_REMOVED + "' order by removeTime desc";

  /**
   * Returns the ComponentInstance whose fields match those of the given sample instance fields.
   * @param sampleInstance a row with the fields of the component instance
   * @return the ComponentInstance whose fields match those of the given sample instance fields.
   * @throws SQLException on error
   */
  public ComponentInstanceRow[] getAllMatchingComponentInstances(
      ComponentInstanceRow sampleInstance) throws SQLException {
    String[] columns = new String[]{"componentName", "name", "description"};
    String[] values =
        new String[]{sampleInstance.componentName, sampleInstance.name, sampleInstance.description};
    List<ComponentInstanceRow> rows = getMatchingRows(INSTANCE_COLUMNS, columns, values);
    return rows.toArray(new ComponentInstanceRow[rows.size()]);
  }

  /**
   * Inserts in the database a new instance row.
   * @param instance a row with the fields of a component instance
   * @throws SQLException on error
   */
  public void createComponentInstance(ComponentInstanceRow instance)
      throws SQLException {
    SpaceRow space = OrganizationSchema.get().space().getSpace(instance.spaceId);
    if (space == null) {
      throw new SQLException(unknown("space", String.valueOf(instance.spaceId)));
    }
    insertRow(INSERT_INSTANCE, instance);
  }

  private static final String INSERT_INSTANCE =
      "insert into ST_ComponentInstance(" + INSTANCE_COLUMNS +
          ") values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

  @Override
  protected void prepareInsert(String insertQuery, PreparedStatement insert,
      ComponentInstanceRow row) throws SQLException {
    if (row.id == -1) {
      row.id = getNextId();
    }
    insert.setInt(1, row.id);
    insert.setInt(2, row.spaceId);
    insert.setString(3, truncate(row.name, 100));
    insert.setString(4, truncate(row.componentName, 100));
    insert.setString(5, truncate(row.description, 500));

    if (row.createdBy == -1) {
      insert.setNull(6, Types.INTEGER);
    } else {
      insert.setInt(6, row.createdBy);
    }
    insert.setInt(7, row.orderNum);

    insert.setString(8, String.valueOf(new Date().getTime()));
    insert.setString(9, null);
    insert.setString(10, null);
    insert.setString(11, row.status);

    insert.setNull(12, Types.INTEGER);
    insert.setNull(13, Types.INTEGER);

    insert.setInt(14, row.publicAccess);
    insert.setInt(15, row.hidden);

    insert.setString(16, row.lang);

    insert.setInt(17, row.inheritanceBlocked);
  }

  public void updateComponentOrder(int componentId, int orderNum) throws SQLException {
    int[] values = new int[]{orderNum, componentId};
    updateRelation(UPDATE_COMPONENT_ORDER, values);
  }

  public static final String UPDATE_ST_COMPONENT_INSTANCE_SET = "update ST_ComponentInstance set";
  public static final String WHERE_ID = " where id = ?";
  private static final String UPDATE_COMPONENT_ORDER =
      UPDATE_ST_COMPONENT_INSTANCE_SET + " orderNum = ?" + WHERE_ID;

  public void updateComponentInheritance(int componentId, boolean inheritanceBlocked)
      throws SQLException {
    int iInheritance = 0;
    if (inheritanceBlocked) {
      iInheritance = 1;
    }
    int[] values = new int[]{iInheritance, componentId};
    updateRelation(UPDATE_COMPONENT_INHERITANCE, values);
  }

  private static final String UPDATE_COMPONENT_INHERITANCE =
      UPDATE_ST_COMPONENT_INSTANCE_SET + " isInheritanceBlocked = ?" + WHERE_ID;

  /**
   * Updates in the database an instance row.
   * @param instance the row with the fields of the component instance
   * @throws SQLException
   */
  public void updateComponentInstance(ComponentInstanceRow instance)
      throws SQLException {
    updateRow(UPDATE_INSTANCE, instance);
  }

  private static final String UPDATE_INSTANCE = UPDATE_ST_COMPONENT_INSTANCE_SET +
      " name = ?, description = ?, createdBy = ?, orderNum = ?, updateTime = ?," +
      " updatedBy = ?, componentStatus = ?, isPublic = ?, isHidden = ?," + " lang = ?," +
      " isInheritanceBlocked = ?" + WHERE_ID;

  /**
   * Check if a named component already exists in given space
   * @param spaceId the space id
   * @param name the name of a component
   * @throws SQLException on error
   */
  public boolean isComponentIntoBasket(int spaceId, String name) throws SQLException {
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement statement = connection.prepareStatement(IS_COMPONENT_INTO_BASKET)) {
      statement.setString(1, name);
      statement.setInt(2, spaceId);
      statement.setString(3, ComponentInst.STATUS_REMOVED);
      try (ResultSet rs = statement.executeQuery()) {
        return rs.next();
      }
    }
  }

  private static final String IS_COMPONENT_INTO_BASKET =
      "select * from ST_ComponentInstance where name = ? and spaceId = ? and componentStatus = ? ";

  /**
   * Delete the space and all his component instances.
   * @param id the component id
   * @param tempLabel the temporary label
   * @param userId the user id that deletes the space
   * @throws AdminPersistenceException on error
   */
  public void sendComponentToBasket(int id, String tempLabel, String userId)
      throws SQLException {
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement statement = connection.prepareStatement(SEND_COMPONENT_IN_BASKET)) {
      statement.setString(1, tempLabel);
      statement.setInt(2, Integer.parseInt(userId));
      statement.setString(3, Long.toString(new Date().getTime()));
      statement.setString(4, ComponentInst.STATUS_REMOVED);
      statement.setInt(5, id);
      statement.executeUpdate();
    }
  }

  private static final String SEND_COMPONENT_IN_BASKET =
      "update ST_ComponentInstance set name = ?, " +
          "removedBy = ?, removeTime = ?, componentStatus = ? where id = ?";

  /**
   * Remove the space from the basket Space will be available again
   * @param id the component id
   * @throws SQLException on error
   */
  public void restoreComponentFromBasket(int id) throws SQLException {
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement statement = connection.prepareStatement(RESTORE_COMPONENT_FROM_BASKET)) {
      statement.setNull(1, Types.INTEGER);
      statement.setNull(2, Types.VARCHAR);
      statement.setNull(3, Types.VARCHAR);
      statement.setInt(4, id);
      statement.executeUpdate();
    }
  }

  private static final String RESTORE_COMPONENT_FROM_BASKET =
      "update ST_ComponentInstance set removedBy = ?, removeTime = ?, " +
          "componentStatus = ? where id = ?";

  public void moveComponentInstance(int spaceId, int componentId) throws SQLException {
    int[] param = new int[2];
    param[0] = spaceId;
    param[1] = componentId;
    updateRelation(MOVE_COMPONENT_INSTANCE, param);
  }

  private static final String MOVE_COMPONENT_INSTANCE =
      UPDATE_ST_COMPONENT_INSTANCE_SET + " spaceId = ? where id = ?";

  @Override
  protected void prepareUpdate(String updateQuery, PreparedStatement update,
      ComponentInstanceRow row) throws SQLException {
    update.setString(1, truncate(row.name, 100));
    update.setString(2, truncate(row.description, 500));
    if (row.createdBy == -1) {
      update.setNull(3, Types.INTEGER);
    } else {
      update.setInt(3, row.createdBy);
    }
    update.setInt(4, row.orderNum);

    update.setString(5, String.valueOf(new Date().getTime()));
    if (row.updatedBy == -1) {
      update.setNull(6, Types.INTEGER);
    } else {
      update.setInt(6, row.updatedBy);
    }
    update.setString(7, row.status);

    update.setInt(8, row.publicAccess);
    update.setInt(9, row.hidden);

    update.setString(10, row.lang);

    update.setInt(11, row.inheritanceBlocked);

    update.setInt(12, row.id);
  }

  /**
   * Delete a component instance and all his user role sets.
   * @param id the component instance identifier
   * @throws SQLException on error
   */
  public void removeComponentInstance(int id) throws SQLException {
    ComponentInstanceRow instance = getComponentInstance(id);
    if (instance == null) {
      return;
    }
    // delete component roles
    OrganizationSchema schema = OrganizationSchema.get();
    UserRoleTable userRoleTable = schema.userRole();
    UserRoleRow[] roles = userRoleTable.getAllUserRolesOfInstance(id);
    for (UserRoleRow role : roles) {
      userRoleTable.removeUserRole(role.getId());
    }
    schema.instanceData().removeInstanceData(id);
    updateRelation(DELETE_INSTANCE, id);
  }

  @Override
  protected ComponentInstanceRow fetchRow(ResultSet rs) throws SQLException {
    return fetchComponentInstance(rs);
  }

}
