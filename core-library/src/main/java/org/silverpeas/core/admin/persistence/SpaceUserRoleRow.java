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
package org.silverpeas.core.admin.persistence;

import org.silverpeas.core.admin.space.SpaceProfileInst;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.silverpeas.core.util.StringUtil.asInt;

public class SpaceUserRoleRow {

  private static final int NO_VALUE = -1;

  private int id = NO_VALUE;
  private int spaceId = NO_VALUE;
  private String name = null;
  private String roleName = null;
  private String description = null;
  private int isInherited = 0;

  private SpaceUserRoleRow() {
  }

  public static SpaceUserRoleRow from(final SpaceProfileInst spaceProfileInst) {
    SpaceUserRoleRow spaceUserRole = new SpaceUserRoleRow();
    spaceUserRole.id = asInt(spaceProfileInst.getId(), -1);
    spaceUserRole.roleName = spaceProfileInst.getName();
    spaceUserRole.name = spaceProfileInst.getLabel();
    spaceUserRole.description = spaceProfileInst.getDescription();
    if (spaceProfileInst.isInherited()) {
      spaceUserRole.isInherited = 1;
    }
    return spaceUserRole;
  }

  public static SpaceUserRoleRow fetch(final ResultSet rs) throws SQLException {
    SpaceUserRoleRow sur = new SpaceUserRoleRow();
    sur.id = rs.getInt(1);
    sur.spaceId = rs.getInt(2);
    sur.name = rs.getString(3);
    sur.roleName = rs.getString(4);
    sur.description = rs.getString(5);
    sur.isInherited = rs.getInt(6);
    return sur;
  }

  public int getId() {
    return id;
  }

  public void setId(final int id) {
    if (id < 0) {
      throw new IllegalArgumentException("The identifier must not be negative");
    }
    this.id = id;
  }

  public boolean isIdDefined() {
    return id != NO_VALUE;
  }

  public int getSpaceId() {
    return spaceId;
  }

  public void setSpaceId(final int spaceId) {
    this.spaceId = spaceId;
  }

  public String getName() {
    return name;
  }

  public String getRoleName() {
    return roleName;
  }

  public String getDescription() {
    return description;
  }

  public int getInheritance() {
    return isInherited;
  }
}
