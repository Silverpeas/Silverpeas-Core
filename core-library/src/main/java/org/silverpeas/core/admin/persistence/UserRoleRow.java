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

import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.util.StringUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.silverpeas.core.util.StringUtil.asInt;

public class UserRoleRow {

  private static final int NO_VALUE = -1;

  private int id = NO_VALUE;
  private int instanceId = NO_VALUE;
  private String name = null;
  private String roleName = null;
  private String description = null;
  private int isInherited = 0;
  private int objectId = NO_VALUE;
  private String objectType = null;

  private UserRoleRow() {
  }

  public static UserRoleRow fetch(final ResultSet rs) throws SQLException {
    UserRoleRow ur = new UserRoleRow();
    ur.id = rs.getInt(1);
    ur.instanceId = rs.getInt(2);
    ur.name = rs.getString(3);
    ur.roleName = rs.getString(4);
    ur.description = rs.getString(5);
    ur.isInherited = rs.getInt(6);
    ur.objectId = rs.getInt(7);
    ur.objectType = rs.getString(8);

    return ur;
  }

  public static UserRoleRow makeFrom(final ProfileInst profileInst) {
    UserRoleRow userRole = new UserRoleRow();

    userRole.id = asInt(profileInst.getId(), NO_VALUE);
    userRole.roleName = profileInst.getName();
    userRole.name = profileInst.getLabel();
    userRole.description = profileInst.getDescription();
    if (profileInst.isInherited()) {
      userRole.isInherited = 1;
    }
    userRole.objectId = asInt(profileInst.getObjectId().getId(), NO_VALUE);
    userRole.objectType = profileInst.getObjectId().getType().getCode();

    return userRole;
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

  public void unsetId() {
    this.id = NO_VALUE;
  }

  public boolean isIdDefined() {
    return id != NO_VALUE;
  }

  public int getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(final int instanceId) {
    this.instanceId = instanceId;
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

  public int getObjectId() {
    return objectId;
  }

  public boolean isObjectIdDefined() {
    return objectId != NO_VALUE;
  }

  public String getObjectType() {
    return objectType;
  }

  public boolean isObjectTypeDefined() {
    return StringUtil.isDefined(objectType);
  }

}
