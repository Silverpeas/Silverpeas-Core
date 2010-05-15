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

package com.silverpeas.workflow.engine.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.model.Role;
import com.silverpeas.workflow.api.model.Roles;

/**
 * Class implementing the representation of the &lt;roles&gt; element of a Process Model.
 */
public class RolesImpl implements Serializable, Roles {
  private List roleList;

  /**
   * Constructor
   */
  public RolesImpl() {
    roleList = new ArrayList();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Roles#addRole(com.silverpeas.workflow .api.model.Role)
   */
  public void addRole(Role role) {
    roleList.add(role);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Roles#createRole()
   */
  public Role createRole() {
    return new RoleImpl();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Roles#getRole(java.lang.String)
   */
  public Role getRole(String name) {
    Role role = null;

    if (roleList == null)
      return null;

    for (int r = 0; r < roleList.size(); r++) {
      role = (Role) roleList.get(r);
      if (name.equals(role.getName()))
        return role;
    }

    return null;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Roles#getRoles()
   */
  public Role[] getRoles() {
    if (roleList == null)
      return null;

    return (Role[]) roleList.toArray(new RoleImpl[0]);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Roles#iterateRole()
   */
  public Iterator iterateRole() {
    return roleList.iterator();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Roles#removeRole(java.lang.String)
   */
  public void removeRole(String strRoleName) throws WorkflowException {
    Role role = createRole();

    role.setName(strRoleName);

    if (roleList == null)
      return;

    if (!roleList.remove(role))
      throw new WorkflowException("RolesImpl.removeRole()", //$NON-NLS-1$
          "workflowEngine.EX_ROLE_NOT_FOUND", // $NON-NLS-1$
          strRoleName == null ? "<null>" //$NON-NLS-1$
              : strRoleName);
  }
}