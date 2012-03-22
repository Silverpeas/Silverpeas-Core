/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
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

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.util.DBUtil;

public class SpaceDAO {

  static final private String SPACE_COLUMNS =
      "id,domainFatherId,name,description,createdBy,firstPageType,firstPageExtraParam,orderNum,createTime,updateTime,removeTime,spaceStatus,updatedBy,removedBy,lang,isInheritanceBlocked,look,displaySpaceFirst,isPersonal";

  public SpaceDAO() {

  }

  public static List<String> getRootSpaceIds(Connection con)
      throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      List<String> spaceIds = new ArrayList<String>();

      stmt = con.prepareStatement(querySortedRootSpaceIds);
      rs = stmt.executeQuery();

      while (rs.next()) {
        spaceIds.add(Integer.toString(rs.getInt(1)));
      }

      return spaceIds;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  private final static String querySortedRootSpaceIds = "select id"
      + " from st_space"
      + " where domainFatherId is null "
      + " and spacestatus is null"
      + " and isPersonal is null"
      + " order by orderNum";

  public static List<SpaceInstLight> getSubSpaces(Connection con, int spaceId)
      throws SQLException {

    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      List<SpaceInstLight> spaces = new ArrayList<SpaceInstLight>();

      stmt = con.prepareStatement(querySortedSubSpaces);
      stmt.setInt(1, spaceId);

      rs = stmt.executeQuery();

      while (rs.next()) {
        spaces.add(fetchSpace(rs));
      }

      return spaces;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  private final static String querySortedSubSpaces = "select " + SPACE_COLUMNS
      + " from st_space"
      + " where domainFatherId = ? "
      + " and spacestatus is null"
      + " order by orderNum";

  public static List<String> getManageableSpaceIds(Connection con, String userId,
      List<String> groupIds) throws SQLException {
    Set<String> manageableSpaceIds = new HashSet<String>();
    if (StringUtil.isDefined(userId)) {
      manageableSpaceIds.addAll(getManageableSpaceIdsByUser(con, userId));
    }
    if (groupIds != null && groupIds.size() > 0) {
      manageableSpaceIds.addAll(getManageableSpaceIdsByGroups(con, groupIds));
    }
    return new ArrayList<String>(manageableSpaceIds);
  }

  static final private String queryGetManageableSpaceIdsByUser = "select st_spaceuserrole.spaceid"
      + " from st_spaceuserrole_user_rel, st_spaceuserrole "
      + " where st_spaceuserrole_user_rel.spaceuserroleid=st_spaceuserrole.id"
      + " and st_spaceuserrole.rolename='Manager'"
      + " and st_spaceuserrole_user_rel.userid=?";

  private static List<String> getManageableSpaceIdsByUser(Connection con, String userId)
      throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      List<String> groupIds = new ArrayList<String>();
      stmt = con.prepareStatement(queryGetManageableSpaceIdsByUser);
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

  private static List<String> getManageableSpaceIdsByGroups(Connection con, List<String> groupIds)
      throws SQLException {
    Statement stmt = null;
    ResultSet rs = null;

    try {
      String query = "select st_spaceuserrole.spaceid"
          + " from st_spaceuserrole_group_rel, st_spaceuserrole "
          + " where st_spaceuserrole_group_rel.spaceuserroleid=st_spaceuserrole.id"
          + " and st_spaceuserrole.rolename='Manager'"
          + " and st_spaceuserrole_group_rel.groupid IN (" + list2String(groupIds) + ")";

      List<String> manageableSpaceIds = new ArrayList<String>();
      stmt = con.createStatement();

      rs = stmt.executeQuery(query);

      while (rs.next()) {
        manageableSpaceIds.add(Integer.toString(rs.getInt(1)));
      }
      return manageableSpaceIds;
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

  private static SpaceInstLight fetchSpace(ResultSet rs) throws SQLException {
    SpaceInstLight space = new SpaceInstLight();

    space.setId(rs.getInt(1));
    space.setFatherId(rs.getInt(2));
    space.setName(rs.getString(3));

    return space;
  }

}
