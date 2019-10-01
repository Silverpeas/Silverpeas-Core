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
package org.silverpeas.core.admin.space;

import org.silverpeas.core.admin.BaseRightProfile;

/**
 * A right profile on a collaboration space in Silverpeas. Such a profile defines the users and the
 * user groups that can access a given space in Silverpeas with some well defined privileges. By
 * default all the right profiles of a space are inherited to the component instances that are
 * included in those spaces.
 */
public class SpaceProfileInst extends BaseRightProfile {

  private static final long serialVersionUID = -1776888916109816898L;

  public static final String SPACE_MANAGER = "Manager";

  private String spaceFatherId = "";

  public void setSpaceFatherId(String sSpaceFatherId) {
    spaceFatherId = sSpaceFatherId;
  }

  public String getSpaceFatherId() {
    return spaceFatherId;
  }

  public String getGroup(int nIndex) {
    return getAllGroups().get(nIndex);
  }

  public String getUser(int nIndex) {
    return getAllUsers().get(nIndex);
  }

  public boolean isManager() {
    return SPACE_MANAGER.equalsIgnoreCase(getName());
  }

  @SuppressWarnings("unchecked")
  protected SpaceProfileInst copy() {
    SpaceProfileInst copy = new SpaceProfileInst();
    copy.setDescription(getDescription());
    copy.setInherited(isInherited());
    copy.setLabel(getLabel());
    copy.setName(getName());
    copy.setSpaceFatherId(spaceFatherId);
    copy.setGroups(getAllGroups());
    copy.setUsers(getAllUsers());
    return copy;
  }
}