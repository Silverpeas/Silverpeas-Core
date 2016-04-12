/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.persistence;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.exception.SilverpeasException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;

/**
 * A ComponentInstanceTable object manages the ST_ComponentInstance table.
 */
public class ComponentInstanceTable extends Table<ComponentInstanceRow> {

  public ComponentInstanceTable(OrganizationSchema organization) {
    super(organization, "ST_ComponentInstance");
    this.organization = organization;
  }

  static final private String INSTANCE_COLUMNS =
      "id,spaceId,name,componentName,description,createdBy,orderNum,createTime,updateTime," +
          "removeTime,componentStatus,updatedBy,removedBy,isPublic,isHidden,lang," +
          "isInheritanceBlocked";

  /**
   * Fetch the current instance row from a resultSet.
   * @param rs
   * @return the current instance row from a resultSet.
   * @throws SQLException
   */
  protected ComponentInstanceRow fetchComponentInstance(ResultSet rs) throws SQLException {
    ComponentInstanceRow i = new ComponentInstanceRow();
    i.id = rs.getInt(1);
    i.spaceId = rs.getInt(2);
    i.name = rs.getString(3);
    i.componentName = rs.getString(4);
    i.description = Table.getNotNullString(rs.getString(5));
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
   * Returns the instance whith the given id.
   * @param id
   * @return the instance whith the given id.
   * @throws AdminPersistenceException
   */
  public ComponentInstanceRow getComponentInstance(int id) throws AdminPersistenceException {
    return getUniqueRow(SELECT_INSTANCE_BY_ID, id);
  }

  static final private String SELECT_INSTANCE_BY_ID =
      "select " + INSTANCE_COLUMNS + " from ST_ComponentInstance where id = ?";

  /**
   * Returns the ComponentInstance of a given user role.
   * @param userRoleId
   * @return the ComponentInstance of a given user role.
   * @throws AdminPersistenceException
   */
  public ComponentInstanceRow getComponentInstanceOfUserRole(int userRoleId)
      throws AdminPersistenceException {
    return getUniqueRow(SELECT_USERROLE_INSTANCE, userRoleId);
  }

  static final private String SELECT_USERROLE_INSTANCE =
      "select " + Table.aliasColumns("i", INSTANCE_COLUMNS) +
          " from ST_ComponentInstance i, ST_UserRole us" +
          " where i.id = us.instanceId and us.id = ?";

  /**
   * Returns all the instances in a given space
   * @param spaceId
   * @return all the instances in a given space
   * @throws AdminPersistenceException
   */
  public ComponentInstanceRow[] getAllComponentInstancesInSpace(int spaceId)
      throws AdminPersistenceException {
    List<ComponentInstanceRow> rows = getRows(SELECT_ALL_SPACE_INSTANCES, spaceId);
    return rows.toArray(new ComponentInstanceRow[rows.size()]);
  }

  static final private String SELECT_ALL_SPACE_INSTANCES = "select " + INSTANCE_COLUMNS +
      " from ST_ComponentInstance where spaceId = ? and componentStatus is null" +
      " order by orderNum";

  /**
   * Returns all the instance ids in a given space
   * @param spaceId
   * @return all the instance ids in a given space
   * @throws AdminPersistenceException
   */
  public String[] getAllComponentInstanceIdsInSpace(int spaceId) throws AdminPersistenceException {
    List<String> ids = getIds(SELECT_ALL_SPACE_INSTANCE_IDS, spaceId);
    return ids.toArray(new String[ids.size()]);
  }

  static final private String SELECT_ALL_SPACE_INSTANCE_IDS =
      "select id from ST_ComponentInstance " + "where spaceId = ? and componentStatus is null" +
          " order by orderNum";

  /**
   * Returns all components which has been removed but not definitely deleted
   * @return all components which has been removed but not definitely deleted
   * @throws AdminPersistenceException
   */
  public ComponentInstanceRow[] getRemovedComponents() throws AdminPersistenceException {
    List<ComponentInstanceRow> rows = getRows(SELECT_REMOVED_COMPONENTS);
    return rows.toArray(new ComponentInstanceRow[rows.size()]);
  }

  static final private String SELECT_REMOVED_COMPONENTS =
      "select " + INSTANCE_COLUMNS + " from ST_ComponentInstance where componentStatus = '" +
          ComponentInst.STATUS_REMOVED + "' order by removeTime desc";

  /**
   * Returns the ComponentInstance whose fields match those of the given sample instance fields.
   * @param sampleInstance
   * @return the ComponentInstance whose fields match those of the given sample instance fields.
   * @throws AdminPersistenceException
   */
  public ComponentInstanceRow[] getAllMatchingComponentInstances(
      ComponentInstanceRow sampleInstance) throws AdminPersistenceException {
    String[] columns = new String[]{"componentName", "name", "description"};
    String[] values =
        new String[]{sampleInstance.componentName, sampleInstance.name, sampleInstance.description};
    List<ComponentInstanceRow> rows = getMatchingRows(INSTANCE_COLUMNS, columns, values);
    return rows.toArray(new ComponentInstanceRow[rows.size()]);
  }

  /**
   * Inserts in the database a new instance row.
   * @param instance
   * @throws AdminPersistenceException
   */
  public void createComponentInstance(ComponentInstanceRow instance)
      throws AdminPersistenceException {
    SpaceRow space = organization.space.getSpace(instance.spaceId);
    if (space == null) {
      throw new AdminPersistenceException("ComponentInstanceTable.createComponentInstance",
          SilverpeasException.ERROR, "admin.EX_ERR_SPACE_NOT_FOUND");
    }
    insertRow(INSERT_INSTANCE, instance);
  }

  static final private String INSERT_INSTANCE =
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
    insert.setString(3, Table.truncate(row.name, 100));
    insert.setString(4, Table.truncate(row.componentName, 100));
    insert.setString(5, Table.truncate(row.description, 500));

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

  public void updateComponentOrder(int componentId, int orderNum) throws AdminPersistenceException {
    int[] values = new int[]{orderNum, componentId};
    updateRelation(UPDATE_COMPONENT_ORDER, values);
  }

  static final private String UPDATE_COMPONENT_ORDER =
      "update ST_ComponentInstance set" + " orderNum = ?" + " where id = ?";

  public void updateComponentInheritance(int componentId, boolean inheritanceBlocked)
      throws AdminPersistenceException {
    int iInheritance = 0;
    if (inheritanceBlocked) {
      iInheritance = 1;
    }
    int[] values = new int[]{iInheritance, componentId};
    updateRelation(UPDATE_COMPONENT_INHERITANCE, values);
  }

  static final private String UPDATE_COMPONENT_INHERITANCE =
      "update ST_ComponentInstance set" + " isInheritanceBlocked = ?" + " where id = ?";

  /**
   * Updates in the database an instance row.
   * @param instance
   * @throws AdminPersistenceException
   */
  public void updateComponentInstance(ComponentInstanceRow instance)
      throws AdminPersistenceException {
    updateRow(UPDATE_INSTANCE, instance);
  }

  static final private String UPDATE_INSTANCE = "update ST_ComponentInstance set" +
      " name = ?, description = ?, createdBy = ?, orderNum = ?, updateTime = ?," +
      " updatedBy = ?, componentStatus = ?, isPublic = ?, isHidden = ?," + " lang = ?," +
      " isInheritanceBlocked = ?" + " where id = ?";

  /**
   * Check if a named component already exists in given space
   * @param spaceId
   * @param name
   * @throws AdminPersistenceException
   */
  public boolean isComponentIntoBasket(int spaceId, String name) throws AdminPersistenceException {
    PreparedStatement statement = null;
    ResultSet rs = null;
    try {
      statement = organization.getStatement(IS_COMPONENT_INTO_BASKET);
      statement.setString(1, name);
      statement.setInt(2, spaceId);
      statement.setString(3, ComponentInst.STATUS_REMOVED);
      rs = statement.executeQuery();
      return rs.next();
    } catch (SQLException e) {
      throw new AdminPersistenceException("ComponentInstanceTable.isComponentIntoBasket",
          SilverpeasException.ERROR, "admin.EX_ERR_SELECT", e);
    } finally {
      DBUtil.close(rs, statement);
    }
  }

  static final private String IS_COMPONENT_INTO_BASKET =
      "select * from ST_ComponentInstance where name = ? and spaceId = ? and componentStatus = ? ";

  /**
   * Delete the space and all his component instances.
   * @param id
   * @param tempLabel
   * @param userId
   * @throws AdminPersistenceException
   */
  public void sendComponentToBasket(int id, String tempLabel, String userId)
      throws AdminPersistenceException {
    PreparedStatement statement = null;
    try {
      statement = organization.getStatement(SEND_COMPONENT_IN_BASKET);
      statement.setString(1, tempLabel);
      statement.setInt(2, Integer.parseInt(userId));
      statement.setString(3, Long.toString(new Date().getTime()));
      statement.setString(4, ComponentInst.STATUS_REMOVED);
      statement.setInt(5, id);
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new AdminPersistenceException("ComponentInstanceTable.sendComponentToBasket",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE", e);
    } finally {
      DBUtil.close(statement);
    }
  }

  static final private String SEND_COMPONENT_IN_BASKET =
      "update ST_ComponentInstance set name = ?, " +
          "removedBy = ?, removeTime = ?, componentStatus = ? where id = ?";

  /**
   * Remove the space from the basket Space will be available again
   * @param id
   * @throws AdminPersistenceException
   */
  public void restoreComponentFromBasket(int id) throws AdminPersistenceException {
    PreparedStatement statement = null;
    try {
      statement = organization.getStatement(RESTORE_COMPONENT_FROM_BASKET);
      statement.setNull(1, Types.INTEGER);
      statement.setNull(2, Types.VARCHAR);
      statement.setNull(3, Types.VARCHAR);
      statement.setInt(4, id);
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new AdminPersistenceException("ComponentInstanceTable.restoreComponentFromBasket",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE", e);
    } finally {
      DBUtil.close(statement);
    }
  }

  static final private String RESTORE_COMPONENT_FROM_BASKET =
      "update ST_ComponentInstance set removedBy = ?, removeTime = ?, " +
          "componentStatus = ? where id = ?";

  public void moveComponentInstance(int spaceId, int componentId) throws AdminPersistenceException {
    int[] param = new int[2];
    param[0] = spaceId;
    param[1] = componentId;
    updateRelation(MOVE_COMPONENT_INSTANCE, param);
  }

  static final private String MOVE_COMPONENT_INSTANCE =
      "update ST_ComponentInstance set" + " spaceId = ? where id = ?";

  @Override
  protected void prepareUpdate(String updateQuery, PreparedStatement update,
      ComponentInstanceRow row) throws SQLException {
    update.setString(1, Table.truncate(row.name, 100));
    update.setString(2, Table.truncate(row.description, 500));
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
   * @param id
   * @throws AdminPersistenceException
   */
  public void removeComponentInstance(int id) throws AdminPersistenceException {
    ComponentInstanceRow instance = getComponentInstance(id);
    if (instance == null) {
      return;
    }
    // delete component roles
    UserRoleRow[] roles = organization.userRole.getAllUserRolesOfInstance(id);
    for (UserRoleRow role : roles) {
      organization.userRole.removeUserRole(role.id);
    }
    organization.instanceData.removeInstanceData(id);
    updateRelation(DELETE_INSTANCE, id);
  }

  static final private String DELETE_INSTANCE = "delete from ST_ComponentInstance where id = ?";

  /**
   * Fetch the current space row from a resultSet.
   */
  @Override
  protected ComponentInstanceRow fetchRow(ResultSet rs) throws SQLException {
    return fetchComponentInstance(rs);
  }

  private OrganizationSchema organization = null;
}
