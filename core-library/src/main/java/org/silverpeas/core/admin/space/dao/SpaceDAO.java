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
package org.silverpeas.core.admin.space.dao;

import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.space.SpaceProfileInst;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.util.StringUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public class SpaceDAO {

  private static final String SPACE_COLUMNS =
      "id,domainFatherId,name,description,spaceStatus,createdBy,firstPageType,firstPageExtraParam,orderNum,createTime,updateTime,removeTime,updatedBy,removedBy,lang,isInheritanceBlocked,look,displaySpaceFirst,isPersonal";


  public List<Integer> getRootSpaceIds(Connection con)
      throws SQLException {
    return getSpaceIdsByQuery(con, QUERY_SORTED_ROOT_SPACE_IDS);
  }

  private static final String QUERY_SORTED_ROOT_SPACE_IDS = "SELECT id FROM st_space WHERE "
      + "domainFatherId IS NULL AND spacestatus IS NULL AND isPersonal IS NULL ORDER BY orderNum";

  public List<SpaceInstLight> getSubSpaces(Connection con, int spaceId) throws SQLException {
    final List<SpaceInstLight> spaces = new ArrayList<>();
    try (final PreparedStatement stmt = con.prepareStatement(QUERY_SORTED_SUB_SPACES)) {
      stmt.setInt(1, spaceId);
      try (final ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          spaces.add(fetchSpace(rs));
        }
      }
    }
    return spaces;
  }

  private static final String QUERY_SORTED_SUB_SPACES = "select " + SPACE_COLUMNS
      + " from st_space"
      + " where domainFatherId = ? "
      + " and spacestatus is null"
      + " order by orderNum";

  public List<Integer> getManageableSpaceIds(Connection con, String userId,
      List<String> groupIds) throws SQLException {
    Set<Integer> manageableSpaceIds = new HashSet<>();
    if (StringUtil.isDefined(userId)) {
      manageableSpaceIds.addAll(getManageableSpaceIdsByUser(con, userId));
    }
    if (groupIds != null && !groupIds.isEmpty()) {
      manageableSpaceIds.addAll(getManageableSpaceIdsByGroups(con, groupIds));
    }
    return new ArrayList<>(manageableSpaceIds);
  }

  private static final String QUERY_GET_MANAGEABLE_SPACE_IDS_BY_USER =
      "SELECT st_spaceuserrole.spaceid "
      + "FROM st_spaceuserrole_user_rel, st_spaceuserrole WHERE "
      + "st_spaceuserrole_user_rel.spaceuserroleid = st_spaceuserrole.id AND st_spaceuserrole.rolename='"
      + SpaceProfileInst.SPACE_MANAGER + "' AND st_spaceuserrole_user_rel.userid=?";

  private List<Integer> getManageableSpaceIdsByUser(Connection con, String userId)
      throws SQLException {
    final List<Integer> spaceIds = new ArrayList<>();
    try (final PreparedStatement stmt = con.prepareStatement(
        QUERY_GET_MANAGEABLE_SPACE_IDS_BY_USER)) {
      stmt.setInt(1, Integer.parseInt(userId));
      try (final ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          spaceIds.add(rs.getInt(1));
        }
      }
    }
    return spaceIds;
  }

  private List<Integer> getManageableSpaceIdsByGroups(Connection con, List<String> groupIds)
      throws SQLException {
    final String query = "SELECT st_spaceuserrole.spaceid FROM st_spaceuserrole_group_rel, " +
        "st_spaceuserrole WHERE st_spaceuserrole_group_rel.spaceuserroleid = st_spaceuserrole.id" +
        " AND st_spaceuserrole.rolename='" + SpaceProfileInst.SPACE_MANAGER +
        "' AND st_spaceuserrole_group_rel.groupid IN (" + list2String(groupIds) + ")";
    return getSpaceIdsByQuery(con, query);
  }

  private List<Integer> getSpaceIdsByQuery(final Connection con, final String query)
      throws SQLException {
    final List<Integer> spaceIds = new ArrayList<>();
    try (final PreparedStatement stmt = con.prepareStatement(query);
         final ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) {
        spaceIds.add(rs.getInt(1));
      }
    }
    return spaceIds;
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

  private SpaceInstLight fetchSpace(ResultSet rs) throws SQLException {
    final SpaceInstLight space = new SpaceInstLight();
    space.setLocalId(rs.getInt("id"));
    space.setFatherId(rs.getInt("domainFatherId"));
    space.setName(rs.getString("name"));
    space.setOrderNum(rs.getInt("orderNum"));
    space.setLook(rs.getString("look"));
    space.setStatus(rs.getString("spaceStatus"));
    boolean isPersonalSpace = rs.getInt("isPersonal") == 1;
    space.setPersonalSpace(isPersonalSpace);
    boolean inheritanceBlocked = rs.getInt("isInheritanceBlocked") == 1;
    space.setInheritanceBlocked(inheritanceBlocked);
    return space;
  }

}
