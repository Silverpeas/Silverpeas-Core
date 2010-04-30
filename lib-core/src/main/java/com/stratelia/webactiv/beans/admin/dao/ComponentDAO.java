/**
 * Copyright (C) 2000 - 2010 Silverpeas
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
package com.stratelia.webactiv.beans.admin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.util.DBUtil;

public class ComponentDAO {

  static final private String INSTANCE_COLUMNS =
      "id,spaceId,name,componentName,description,createdBy,orderNum,createTime,updateTime,removeTime,componentStatus,updatedBy,removedBy,isPublic,isHidden,lang,isInheritanceBlocked";

  public ComponentDAO() {

  }

  static final private String queryAllSpaceInstanceIds = "select id, componentName"
      + " from ST_ComponentInstance where spaceId = ?"
      + " and componentStatus is null order by orderNum";

  public static List<String> getComponentIdsInSpace(Connection con, int spaceId)
      throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      List<String> ids = new ArrayList<String>();

      stmt = con.prepareStatement(queryAllSpaceInstanceIds);
      stmt.setInt(1, spaceId);

      rs = stmt.executeQuery();

      while (rs.next()) {
        ids.add(rs.getString(2) + Integer.toString(rs.getInt(1)));
      }

      return ids;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  /**
   * Fetch the current instance row from a resultSet.
   */
  private static ComponentInstLight fetchComponentInstance(ResultSet rs)
      throws SQLException {

    ComponentInstLight i = new ComponentInstLight();

    i.setId(Integer.toString(rs.getInt(1)));
    i.setDomainFatherId(Integer.toString(rs.getInt(2)));
    i.setLabel(rs.getString(3));
    i.setName(rs.getString(4));

    return i;
  }

  static final private String queryAllSpaceInstances = "select " + INSTANCE_COLUMNS
      + " from ST_ComponentInstance where spaceId = ?"
      + " and componentStatus is null order by orderNum";

  public static List<ComponentInstLight> getComponentsInSpace(Connection con, int spaceId)
      throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      List<ComponentInstLight> components = new ArrayList<ComponentInstLight>();

      stmt = con.prepareStatement(queryAllSpaceInstances);
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

  private static String list2String(List<String> ids) {
    StringBuilder str = new StringBuilder();
    for (int i = 0; i < ids.size(); i++) {
      if (i != 0) {
        str.append(",");
      }
      str.append(ids.get(i));
    }
    return str.toString();
  }

  public static List<String> getAllAvailableComponentIds(Connection con, List<String> groupIds,
      int userId) throws SQLException {
    Set<String> componentIds = new HashSet<String>();
    componentIds.addAll(getAllPublicComponentIds(con));
    if (groupIds != null && groupIds.size() > 0) {
      componentIds.addAll(getAllAvailableComponentIds(con, groupIds));
    }
    componentIds.addAll(getAllAvailableComponentIds(con, userId));
    return new ArrayList<String>(componentIds);
  }

  private static List<String> getAllAvailableComponentIds(Connection con, List<String> groupIds)
      throws SQLException {
    Statement stmt = null;
    ResultSet rs = null;
    try {
      List<String> ids = new ArrayList<String>();

      String queryAllAvailableComponentIds = "select distinct(c.id), c.componentName"
          + " from st_componentinstance c, st_userrole r, st_userrole_group_rel gr"
          + " where c.id=r.instanceId"
          + " and c.componentstatus is null"
          + " and r.id=gr.userroleid"
          + " and r.objectId is null"
          + " and gr.groupId IN (" + list2String(groupIds) + ")";

      stmt = con.createStatement();
      rs = stmt.executeQuery(queryAllAvailableComponentIds);

      while (rs.next()) {
        ids.add(rs.getString(2) + Integer.toString(rs.getInt(1)));
      }

      return ids;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  private final static String queryAllAvailableComponentIds =
      " select distinct(c.id), c.componentName"
      + " from st_componentinstance c, st_userrole r, st_userrole_user_rel ur"
      + " where c.id=r.instanceId"
      + " and c.componentstatus is null"
      + " and r.id=ur.userroleid"
      + " and r.objectId is null"
      + " and ur.userId = ? ";

  private static List<String> getAllAvailableComponentIds(Connection con, int userId)
      throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      List<String> ids = new ArrayList<String>();

      stmt = con.prepareStatement(queryAllAvailableComponentIds);
      stmt.setInt(1, userId);

      rs = stmt.executeQuery();

      while (rs.next()) {
        ids.add(rs.getString(2) + Integer.toString(rs.getInt(1)));
      }

      return ids;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  private final static String queryAllPublicComponentIds = " select c.id, c.componentName"
      + " from st_componentinstance c"
      + " where c.ispublic=1"
      + " and c.componentstatus is null";

  private static List<String> getAllPublicComponentIds(Connection con)
      throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      List<String> ids = new ArrayList<String>();

      stmt = con.prepareStatement(queryAllPublicComponentIds);

      rs = stmt.executeQuery();

      while (rs.next()) {
        ids.add(rs.getString(2) + Integer.toString(rs.getInt(1)));
      }

      return ids;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  public static List<String> getAvailableComponentIdsInSpace(Connection con, List<String> groupIds,
      int userId, String spaceId) throws SQLException {
    Set<String> componentIds = new HashSet<String>();
    componentIds.addAll(getPublicComponentIdsInSpace(con, spaceId));
    if (groupIds != null && groupIds.size() > 0) {
      componentIds.addAll(getAvailableComponentIdsInSpace(con, groupIds, spaceId));
    }
    componentIds.addAll(getAvailableComponentIdsInSpace(con, userId, spaceId));
    return new ArrayList<String>(componentIds);
  }

  private static List<String> getAvailableComponentIdsInSpace(Connection con,
      List<String> groupIds, String spaceId)
      throws SQLException {
    Statement stmt = null;
    ResultSet rs = null;
    try {
      List<String> ids = new ArrayList<String>();

      String queryAvailableComponentIdsInSpace = "select distinct(c.id), c.componentName"
          + " from st_componentinstance c, st_userrole r, st_userrole_group_rel gr"
          + " where c.id=r.instanceId"
          + " and c.componentstatus is null"
          + " and c.spaceId = " + spaceId
          + " and r.id=gr.userroleid"
          + " and r.objectId is null"
          + " and gr.groupId IN (" + list2String(groupIds) + ")";

      stmt = con.createStatement();

      rs = stmt.executeQuery(queryAvailableComponentIdsInSpace);

      while (rs.next()) {
        ids.add(rs.getString(2) + Integer.toString(rs.getInt(1)));
      }

      return ids;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  private final static String queryAvailableComponentIdsInSpace =
      " select distinct(c.id), c.componentName"
      + " from st_componentinstance c, st_userrole r, st_userrole_user_rel ur"
      + " where c.spaceId = ? "
      + " and c.id=r.instanceId"
      + " and c.componentstatus is null"
      + " and r.id=ur.userroleid"
      + " and r.objectId is null"
      + " and ur.userId = ? ";

  private static List<String> getAvailableComponentIdsInSpace(Connection con, int userId,
      String spaceId)
      throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      List<String> ids = new ArrayList<String>();

      stmt = con.prepareStatement(queryAvailableComponentIdsInSpace);
      stmt.setInt(1, Integer.parseInt(spaceId));
      stmt.setInt(2, userId);

      rs = stmt.executeQuery();

      while (rs.next()) {
        ids.add(rs.getString(2) + Integer.toString(rs.getInt(1)));
      }

      return ids;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  private final static String queryPublicComponentIdsInSpace = " select c.id, c.componentName"
      + " from st_componentinstance c"
      + " where c.ispublic=1"
      + " and c.spaceId = ?"
      + " and c.componentstatus is null";

  private static List<String> getPublicComponentIdsInSpace(Connection con, String spaceId)
      throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      List<String> ids = new ArrayList<String>();

      stmt = con.prepareStatement(queryPublicComponentIdsInSpace);
      stmt.setInt(1, Integer.parseInt(spaceId));

      rs = stmt.executeQuery();

      while (rs.next()) {
        ids.add(rs.getString(2) + Integer.toString(rs.getInt(1)));
      }

      return ids;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }
}
