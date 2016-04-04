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

package org.silverpeas.core.admin.space.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.space.SpaceProfileInst;
import org.silverpeas.core.persistence.jdbc.DBUtil;

public class SpaceDAO {

  static final private String SPACE_COLUMNS =
      "id,domainFatherId,name,description,createdBy,firstPageType,firstPageExtraParam,orderNum,createTime,updateTime,removeTime,spaceStatus,updatedBy,removedBy,lang,isInheritanceBlocked,look,displaySpaceFirst,isPersonal";

  public SpaceDAO() {

  }

  public static List<Integer> getRootSpaceIds(Connection con)
      throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      List<Integer> spaceIds = new ArrayList<>();

      stmt = con.prepareStatement(querySortedRootSpaceIds);
      rs = stmt.executeQuery();

      while (rs.next()) {
        spaceIds.add(rs.getInt(1));
      }

      return spaceIds;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  private final static String querySortedRootSpaceIds = "SELECT id FROM st_space WHERE "
      + "domainFatherId IS NULL AND spacestatus IS NULL AND isPersonal IS NULL ORDER BY orderNum";

  public static List<SpaceInstLight> getSubSpaces(Connection con, int spaceId) throws SQLException {
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

  public static List<Integer> getManageableSpaceIds(Connection con, String userId,
      List<String> groupIds) throws SQLException {
    Set<Integer> manageableSpaceIds = new HashSet<>();
    if (StringUtil.isDefined(userId)) {
      manageableSpaceIds.addAll(getManageableSpaceIdsByUser(con, userId));
    }
    if (groupIds != null && groupIds.size() > 0) {
      manageableSpaceIds.addAll(getManageableSpaceIdsByGroups(con, groupIds));
    }
    return new ArrayList<>(manageableSpaceIds);
  }

  static final private String queryGetManageableSpaceIdsByUser = "SELECT st_spaceuserrole.spaceid "
      + "FROM st_spaceuserrole_user_rel, st_spaceuserrole WHERE "
      + "st_spaceuserrole_user_rel.spaceuserroleid = st_spaceuserrole.id AND st_spaceuserrole.rolename='"
      + SpaceProfileInst.SPACE_MANAGER + "' AND st_spaceuserrole_user_rel.userid=?";

  private static List<Integer> getManageableSpaceIdsByUser(Connection con, String userId)
      throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      List<Integer> spaceIds = new ArrayList<>();
      stmt = con.prepareStatement(queryGetManageableSpaceIdsByUser);
      stmt.setInt(1, Integer.parseInt(userId));

      rs = stmt.executeQuery();

      while (rs.next()) {
        spaceIds.add(rs.getInt(1));
      }
      return spaceIds;
    } finally {
      DBUtil.close(rs, stmt);
    }

  }

  private static List<Integer> getManageableSpaceIdsByGroups(Connection con, List<String> groupIds)
      throws SQLException {
    Statement stmt = null;
    ResultSet rs = null;

    try {
      String query = "SELECT st_spaceuserrole.spaceid FROM st_spaceuserrole_group_rel, "
          + "st_spaceuserrole WHERE st_spaceuserrole_group_rel.spaceuserroleid = st_spaceuserrole.id"
          + " AND st_spaceuserrole.rolename='"+ SpaceProfileInst.SPACE_MANAGER
          + "' AND st_spaceuserrole_group_rel.groupid IN (" + list2String(groupIds) + ")";
      List<Integer> manageableSpaceIds = new ArrayList<>();
      stmt = con.createStatement();
      rs = stmt.executeQuery(query);
      while (rs.next()) {
        manageableSpaceIds.add(rs.getInt(1));
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
        str.append(',');
      }
      str.append(ids.get(i));
    }
    return str.toString();
  }

  private static SpaceInstLight fetchSpace(ResultSet rs) throws SQLException {
    SpaceInstLight space = new SpaceInstLight();

    space.setLocalId(rs.getInt("id"));
    space.setFatherId(rs.getInt("domainFatherId"));
    space.setName(rs.getString("name"));
    space.setOrderNum(rs.getInt("orderNum"));
    space.setLook(rs.getString("look"));
    boolean isPersonalSpace = rs.getInt("isPersonal") == 1;
    space.setPersonalSpace(isPersonalSpace);
    boolean inheritanceBlocked = rs.getInt("isInheritanceBlocked") == 1;
    space.setInheritanceBlocked(inheritanceBlocked);

    return space;
  }

}
