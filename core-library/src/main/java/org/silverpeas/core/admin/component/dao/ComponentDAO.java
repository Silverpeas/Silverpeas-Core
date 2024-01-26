/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.admin.component.dao;

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.persistence.ComponentInstanceRow;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.kernel.util.StringUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ComponentDAO extends CommonDAO {

  private static final String INSTANCE_COLUMNS =
      "id,spaceId,name,componentName,description,createdBy,orderNum,createTime,updateTime,removeTime,componentStatus,updatedBy,removedBy,isPublic,isHidden,lang,isInheritanceBlocked";

  private static final String WHERE_INSTANCE_ID_MATCHES = " where c.id=r.instanceId";
  private static final String AND_COMPONENT_STATUS_IS_NULL = " and c.componentstatus is null";
  private static final String AND_OBJECT_ID_IS_NULL = " and r.objectId is null";

  private static final String QUERY_ALL_SPACE_INSTANCE_IDS = "select id, componentName"
      + " from ST_ComponentInstance where spaceId = ?"
      + " and componentStatus is null order by orderNum";

  private static final String QUERY_ALL_SPACE_INSTANCES =
      "SELECT " + INSTANCE_COLUMNS + " FROM ST_ComponentInstance WHERE spaceId = ?" +
          " AND componentStatus IS NULL ORDER BY orderNum";

  private static final String QUERY_ALL_AVAILABLE_COMPONENT_IDS =
      " SELECT DISTINCT(c.id), c.componentName" +
          " FROM st_componentinstance c, st_userrole r, st_userrole_user_rel ur" +
          WHERE_INSTANCE_ID_MATCHES + AND_COMPONENT_STATUS_IS_NULL + " AND r.id=ur.userroleid" +
          AND_OBJECT_ID_IS_NULL + " AND ur.userId = ? ";

  private static final String QUERY_ALL_PUBLIC_COMPONENT_IDS =
      " SELECT c.id, c.componentName" + " FROM st_componentinstance c" + " WHERE c.ispublic=1" +
          AND_COMPONENT_STATUS_IS_NULL;

  private static final  String QUERY_PUBLIC_COMPONENT_IDS_IN_SPACE =
      " select c.id, c.componentName, c.ordernum"
          + " from st_componentinstance c"
          + " where c.ispublic=1" + " and c.spaceId = ?" + AND_COMPONENT_STATUS_IS_NULL;

  private static final int COMPONENT_ID_COLUMN = 1;
  private static final int COMPONENT_NAME_COLUMN = 2;
  private static final int COMPONENT_ORDER_COLUMN = 3;

  private ComponentDAO() {
  }

  public static List<String> getComponentIdsInSpace(Connection con, int spaceId)
      throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      List<String> ids = new ArrayList<>();

      stmt = con.prepareStatement(QUERY_ALL_SPACE_INSTANCE_IDS);
      stmt.setInt(1, spaceId);

      rs = stmt.executeQuery();

      while (rs.next()) {
        ids.add(
            rs.getString(COMPONENT_NAME_COLUMN) + rs.getInt(COMPONENT_ID_COLUMN));
      }

      return ids;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  /**
   * Fetch the current instance row from a resultSet.
   */
  private static ComponentInstLight fetchComponentInstance(ResultSet rs) throws SQLException {

    ComponentInstanceRow row = new ComponentInstanceRow();

    row.id = rs.getInt("id");
    row.spaceId = rs.getInt("spaceId");
    row.name = rs.getString("name");
    row.componentName = rs.getString("componentName");
    row.description = rs.getString("description");
    row.inheritanceBlocked = rs.getInt("isInheritanceBlocked");
    row.hidden = rs.getInt("isHidden");
    row.createdBy = rs.getInt("createdBy");
    row.orderNum = rs.getInt("orderNum");
    row.createTime = rs.getString("createTime");
    row.updateTime = rs.getString("updateTime");
    row.removeTime = rs.getString("removeTime");
    row.status = rs.getString("componentStatus");
    row.updatedBy = rs.getInt("updatedBy");
    row.removedBy = rs.getInt("removedBy");
    row.publicAccess = rs.getInt("isPublic");
    row.lang = rs.getString("lang");

    return new ComponentInstLight(row);
  }

  public static List<String> getAllActiveComponentIds() throws SQLException {
    return JdbcSqlQuery
        .select("componentName, id")
        .from("ST_ComponentInstance")
        .where("componentStatus IS NULL")
        .and("componentName").in(WAComponent.getAll().stream().map(WAComponent::getName).collect(Collectors.toList()))
        .orderBy("orderNum")
        .execute(r -> r.getString(1) + r.getInt(2));
  }

  public static List<ComponentInstLight> getComponentsInSpace(Connection con, int spaceId)
      throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      List<ComponentInstLight> components = new ArrayList<>();

      stmt = con.prepareStatement(QUERY_ALL_SPACE_INSTANCES);
      stmt.setInt(1, spaceId);

      rs = stmt.executeQuery();

      while (rs.next()) {
        components.add(fetchComponentInstance(rs));
      }

      return components;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  public static List<String> getAllAvailableComponentIds(Connection con, List<String> groupIds,
      int userId, String componentName) throws SQLException {
    Set<String> componentIds = new HashSet<>(getAllPublicComponentIds(con));

    // Public component instance ids must be filtered in case when a component name filter is
    // defined.
    if (StringUtil.isDefined(componentName)) {
      componentIds.removeIf(s -> !s.startsWith(componentName));
    }

    if (groupIds != null && !groupIds.isEmpty()) {
      componentIds.addAll(getAllAvailableComponentIds(con, groupIds, componentName));
    }
    if (userId != -1) {
      componentIds.addAll(getAllAvailableComponentIds(con, userId, componentName));
    }
    return new ArrayList<>(componentIds);
  }

  private static List<String> getAllAvailableComponentIds(Connection con, List<String> groupIds,
      String componentName) throws SQLException {
    Statement stmt = null;
    ResultSet rs = null;
    try {
      List<String> ids = new ArrayList<>();

      String queryAllAvailableComponentIds = "select distinct(c.id), c.componentName" +
          " from st_componentinstance c, st_userrole r, st_userrole_group_rel gr" +
          WHERE_INSTANCE_ID_MATCHES + AND_COMPONENT_STATUS_IS_NULL + " and r.id=gr.userroleid" +
          AND_OBJECT_ID_IS_NULL
          + " and gr.groupId IN (" + list2String(groupIds) + ")";

      stmt = con.createStatement();
      rs = stmt.executeQuery(queryAllAvailableComponentIds);

      while (rs.next()) {
        String cName = rs.getString(COMPONENT_NAME_COLUMN);
        if (!StringUtil.isDefined(componentName) ||
            (StringUtil.isDefined(componentName) && componentName.equalsIgnoreCase(cName))) {
          ids.add(cName + rs.getInt(1));
        }
      }

      return ids;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  private static List<String> getAllAvailableComponentIds(Connection con, int userId,
      String componentName) throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      List<String> ids = new ArrayList<>();

      stmt = con.prepareStatement(QUERY_ALL_AVAILABLE_COMPONENT_IDS);
      stmt.setInt(1, userId);

      rs = stmt.executeQuery();

      while (rs.next()) {
        String cName = rs.getString(COMPONENT_NAME_COLUMN);
        if (!StringUtil.isDefined(componentName) ||
            (StringUtil.isDefined(componentName) && componentName.equalsIgnoreCase(cName))) {
          ids.add(cName + rs.getInt(1));
        }
      }

      return ids;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  private static List<String> getAllPublicComponentIds(Connection con)
      throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      List<String> ids = new ArrayList<>();

      stmt = con.prepareStatement(QUERY_ALL_PUBLIC_COMPONENT_IDS);

      rs = stmt.executeQuery();

      while (rs.next()) {
        ids.add(
            rs.getString(COMPONENT_NAME_COLUMN) + rs.getInt(COMPONENT_ID_COLUMN));
      }

      return ids;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  public static List<String> getAvailableComponentIdsInSpace(Connection con, List<String> groupIds,
      int userId, int spaceId, String componentName) throws SQLException {
    // get available components
    Set<ComponentInstLight> componentsSet = new HashSet<>(getPublicComponentsInSpace(con, spaceId));
    if (groupIds != null && !groupIds.isEmpty()) {
      componentsSet.addAll(getAvailableComponentsInSpace(con, groupIds, spaceId, componentName));
    }
    componentsSet.addAll(getAvailableComponentsInSpace(con, userId, spaceId, componentName));

    // sort components according to ordernum
    final List<ComponentInstLight> components = new ArrayList<>(componentsSet);
    components.sort(new ComponentInstLightSorter());
    List<String> componentIds = new ArrayList<>();
    for (ComponentInstLight component : components) {
      componentIds.add(component.getId());
    }
    return componentIds;
  }

  private static List<ComponentInstLight> getAvailableComponentsInSpace(Connection con,
      List<String> groupIds, int spaceId, String componentName)
      throws SQLException {
    Statement stmt = null;
    ResultSet rs = null;
    try {
      List<ComponentInstLight> components = new ArrayList<>();

      String queryAvailableComponentIdsInSpace =
          "select distinct(c.id), c.componentName, c.ordernum" +
              " from st_componentinstance c, st_userrole r, st_userrole_group_rel gr" +
              WHERE_INSTANCE_ID_MATCHES;
      if (StringUtil.isDefined(componentName)) {
        queryAvailableComponentIdsInSpace += " and c.componentName = '" + componentName + "'";
      }
      queryAvailableComponentIdsInSpace += AND_COMPONENT_STATUS_IS_NULL
          + " and c.spaceId = " + spaceId + " and r.id=gr.userroleid" + AND_OBJECT_ID_IS_NULL
          + " and gr.groupId IN (" + list2String(groupIds) + ")";

      stmt = con.createStatement();

      rs = stmt.executeQuery(queryAvailableComponentIdsInSpace);

      makeComponentInst(rs, components);

      return components;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  private static List<ComponentInstLight> getAvailableComponentsInSpace(Connection con, int userId,
      int spaceId, String componentName) throws SQLException {
    Statement stmt = null;
    ResultSet rs = null;
    try {
      List<ComponentInstLight> components = new ArrayList<>();

      String queryAvailableComponentIdsInSpace =
          " select distinct(c.id), c.componentName, c.ordernum"
          + " from st_componentinstance c, st_userrole r, st_userrole_user_rel ur"
          + " where c.spaceId = " + spaceId;
      if (StringUtil.isDefined(componentName)) {
        queryAvailableComponentIdsInSpace += " and c.componentName = '" + componentName + "'";
      }
      queryAvailableComponentIdsInSpace +=
          " and c.id=r.instanceId" + AND_COMPONENT_STATUS_IS_NULL + " and r.id=ur.userroleid" +
              AND_OBJECT_ID_IS_NULL
          + " and ur.userId = " + userId;

      stmt = con.createStatement();

      rs = stmt.executeQuery(queryAvailableComponentIdsInSpace);

      makeComponentInst(rs, components);

      return components;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  private static void makeComponentInst(final ResultSet rs,
      final List<ComponentInstLight> components) throws SQLException {
    while (rs.next()) {
      ComponentInstLight component = new ComponentInstLight();
      component.setLocalId(rs.getInt(COMPONENT_ID_COLUMN));
      component.setOrderNum(rs.getInt(COMPONENT_ORDER_COLUMN));
      component.setName(rs.getString(COMPONENT_NAME_COLUMN));
      components.add(component);
    }
  }

  private static List<ComponentInstLight> getPublicComponentsInSpace(Connection con, int spaceId)
      throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      List<ComponentInstLight> components = new ArrayList<>();

      stmt = con.prepareStatement(QUERY_PUBLIC_COMPONENT_IDS_IN_SPACE);
      stmt.setInt(1, spaceId);

      rs = stmt.executeQuery();

     makeComponentInst(rs, components);

      return components;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }
}