/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.workflow.engine.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.model.Role;
import org.silverpeas.core.workflow.api.model.Roles;

/**
 * Class implementing the representation of the &lt;roles&gt; element of a Process Model.
 */
public class RolesImpl implements Serializable, Roles {

  private static final long serialVersionUID = 4241149699620983852L;
  private List<Role> roleList;

  /**
   * Constructor
   */
  public RolesImpl() {
    roleList = new ArrayList<>();
  }

  /*
   * (non-Javadoc)
   * @see Roles#addRole(com.silverpeas.workflow .api.model.Role)
   */
  @Override
  public void addRole(Role role) {
    roleList.add(role);
  }

  /*
   * (non-Javadoc)
   * @see Roles#createRole()
   */
  @Override
  public Role createRole() {
    return new RoleImpl();
  }

  /*
   * (non-Javadoc)
   * @see Roles#getRole(java.lang.String)
   */
  @Override
  public Role getRole(String name) {
    if (roleList == null) {
      return null;
    }
    for (Role role : roleList) {
      if (name.equals(role.getName())) {
        return role;
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * @see Roles#getRoles()
   */
  @Override
  public Role[] getRoles() {
    if (roleList == null) {
      return null;
    }
    return roleList.toArray(new Role[roleList.size()]);
  }

  /*
   * (non-Javadoc)
   * @see Roles#iterateRole()
   */
  @Override
  public Iterator<Role> iterateRole() {
    return roleList.iterator();
  }

  /*
   * (non-Javadoc)
   * @see Roles#removeRole(java.lang.String)
   */
  @Override
  public void removeRole(String strRoleName) throws WorkflowException {
    Role role = createRole();
    role.setName(strRoleName);
    if (roleList == null) {
      return;
    }

    if (!roleList.remove(role)) {
      throw new WorkflowException("RolesImpl.removeRole()", "workflowEngine.EX_ROLE_NOT_FOUND",
          strRoleName == null ? "<null>" : strRoleName);
    }
  }
}