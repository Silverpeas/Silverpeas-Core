/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.workflow.engine.model;

import java.io.Serializable;

import org.silverpeas.core.workflow.api.model.UserInRole;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class implementing the representation of the &lt;userInRole&gt; element of a Process Model.
 **/
@XmlRootElement(name = "userInRole")
@XmlAccessorType(XmlAccessType.NONE)
public class UserInRoleImpl implements UserInRole, Serializable {
  private static final long serialVersionUID = -6419166381111612814L;
  @XmlAttribute(name = "name")
  private String roleName;

  /**
   * Constructor
   */
  public UserInRoleImpl() {
    super();
  }

  /**
   * Get name of the role
   */
  public String getRoleName() {
    return this.roleName;
  }

  /**
   * Set name of the role
   * @param roleName
   */
  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final UserInRoleImpl that = (UserInRoleImpl) o;

    return roleName != null ? roleName.equals(that.roleName) : that.roleName == null;
  }

  @Override
  public int hashCode() {
    return roleName != null ? roleName.hashCode() : 0;
  }
}