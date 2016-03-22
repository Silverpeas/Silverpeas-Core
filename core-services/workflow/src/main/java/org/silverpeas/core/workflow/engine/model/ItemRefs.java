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

import org.silverpeas.core.workflow.engine.AbstractReferrableObject;

/**
 * TODO remove, not used
 **/
public class ItemRefs extends AbstractReferrableObject implements Serializable {
  private static final long serialVersionUID = 430140382083226328L;
  private String roleName = "default";

  /**
   * Constructor
   */
  public ItemRefs() {
    super();
  }

  /**
   * TODO remove Get the itemRefs
   * @return the itemRefs as a Vector / public Vector getItemRefList() { return itemRefList; } /**
   * Get the role for which the list of items must be returned
   */
  public String getRoleName() {
    return roleName;
  }

  /**
   * Set the role for which the list of items must be returned
   * @param roleName role name
   */
  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  /**
   * Get the unique key, used by equals method
   * @return unique key
   */
  @Override
  public String getKey() {
    return (this.roleName);
  }
}
