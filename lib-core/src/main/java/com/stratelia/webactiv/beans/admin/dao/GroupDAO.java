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
import java.util.List;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.util.DBUtil;

public class GroupDAO {

  static final private String GROUP_COLUMNS =
      "id,specificId,domainId,superGroupId,name,description,synchroRule";

  public GroupDAO() {

  }

  static final private String queryGetGroup = "select " + GROUP_COLUMNS
      + " from ST_Group where id = ?";

  public static Group getGroup(Connection con, String groupId)
      throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(queryGetGroup);
      stmt.setInt(1, Integer.parseInt(groupId));

      rs = stmt.executeQuery();

      Group group = null;
      if (rs.next()) {
        group = fetchGroup(rs);
      }
      return group;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  static final private String queryGetSubGroups = "select " + GROUP_COLUMNS
      + " from ST_Group where superGroupId = ?";

  public static List<Group> getSubGroups(Connection con, String groupId) throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      List<Group> groups = new ArrayList<Group>();
      stmt = con.prepareStatement(queryGetSubGroups);
      stmt.setInt(1, Integer.parseInt(groupId));

      rs = stmt.executeQuery();

      while (rs.next()) {
        groups.add(fetchGroup(rs));
      }
      return groups;
    } finally {
      DBUtil.close(rs, stmt);
    }

  }

  static final private String queryGetNBUsersDirectlyInGroup = "select count(userid)"
      + " from st_group_user_rel where groupid = ?";

  public static int getNBUsersDirectlyInGroup(Connection con, String groupId) throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(queryGetNBUsersDirectlyInGroup);
      stmt.setInt(1, Integer.parseInt(groupId));

      rs = stmt.executeQuery();

      if (rs.next()) {
        return rs.getInt(1);
      }
      return 0;
    } finally {
      DBUtil.close(rs, stmt);
    }

  }

  public static List<String> getManageableGroupIds(Connection con, String userId,
      List<String> groupIds) throws SQLException {
    List<String> manageableGroupIds = new ArrayList<String>();
    if (StringUtil.isDefined(userId)) {
      manageableGroupIds.addAll(getManageableGroupIdsByUser(con, userId));
    }
    if (groupIds != null && groupIds.size() > 0) {
      manageableGroupIds.addAll(getManageableGroupIdsByGroups(con, groupIds));
    }
    return manageableGroupIds;
  }

  static final private String queryGetManageableGroupIdsByUser = "select st_groupuserrole.groupid"
      + " from st_groupuserrole_user_rel, st_groupuserrole "
      + " where st_groupuserrole_user_rel.groupuserroleid=st_groupuserrole.id"
      + " and st_groupuserrole_user_rel.userid=?";

  private static List<String> getManageableGroupIdsByUser(Connection con, String userId)
      throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      List<String> groupIds = new ArrayList<String>();
      stmt = con.prepareStatement(queryGetManageableGroupIdsByUser);
      stmt.setInt(1, Integer.parseInt(userId));

      rs = stmt.executeQuery();

      while (rs.next()) {
        groupIds.add(Integer.toString(rs.getInt(1)));
      }
      return groupIds;
    } finally {
      DBUtil.close(rs, stmt);
    }

  }

  private static List<String> getManageableGroupIdsByGroups(Connection con, List<String> groupIds)
      throws SQLException {
    Statement stmt = null;
    ResultSet rs = null;

    try {
      String queryGetManageableGroupIdsByUser = "select st_groupuserrole.groupid"
          + " from st_groupuserrole_group_rel, st_groupuserrole "
          + " where st_groupuserrole_group_rel.groupuserroleid=st_groupuserrole.id"
          + " and st_groupuserrole_group_rel.groupid IN (" + list2String(groupIds) + ")";

      List<String> manageableGroupIds = new ArrayList<String>();
      stmt = con.createStatement();

      rs = stmt.executeQuery(queryGetManageableGroupIdsByUser);

      while (rs.next()) {
        manageableGroupIds.add(Integer.toString(rs.getInt(1)));
      }
      return manageableGroupIds;
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

  /**
   * Fetch the current group row from a resultSet.
   */
  private static Group fetchGroup(ResultSet rs) throws SQLException {

    Group group = new Group();
    group.setId(Integer.toString(rs.getInt(1)));
    group.setSpecificId(rs.getString(2));
    if (group.getSpecificId().equals("-1")) {
      group.setSpecificId(null);
    }
    group.setDomainId(Integer.toString(rs.getInt(3)));
    group.setSuperGroupId(Integer.toString(rs.getInt(4)));
    if (rs.wasNull()) {
      group.setSuperGroupId(null);
    }
    group.setName(rs.getString(5));
    group.setDescription(rs.getString(6));
    group.setRule(rs.getString(7));
    return group;
  }
}
