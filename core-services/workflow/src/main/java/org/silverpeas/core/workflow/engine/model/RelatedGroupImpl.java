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

import org.silverpeas.core.workflow.api.model.Item;
import org.silverpeas.core.workflow.api.model.RelatedGroup;
import org.silverpeas.core.workflow.engine.AbstractReferrableObject;

/**
 * Class implementing the representation of the &lt;relatedUser&gt; element of a Process Model.
 **/
public class RelatedGroupImpl extends AbstractReferrableObject implements RelatedGroup,
    Serializable {

  private static final long serialVersionUID = 383421738010797483L;
  private Item folderItem;
  private String role;

  /**
   * Constructor
   */
  public RelatedGroupImpl() {
    super();
  }

  /**
   * Get the referred item
   */
  public Item getFolderItem() {
    return folderItem;
  }

  /**
   * Set the referred item
   * @param folderItem item to refer
   */
  public void setFolderItem(Item folderItem) {
    this.folderItem = folderItem;
  }

  /**
   * Get the role to which the related user will be affected
   * @return the role name
   */
  public String getRole() {
    return this.role;
  }

  /**
   * Set the role to which the related user will be affected
   * @param role role as a String
   */
  public void setRole(String role) {
    this.role = role;
  }

  /*
   * @see AbstractReferrableObject#getKey()
   */
  public String getKey() {
    StringBuffer sb = new StringBuffer();

    if (folderItem instanceof AbstractReferrableObject)
      sb.append(((AbstractReferrableObject) folderItem).getKey());

    sb.append("|");

    if (role != null)
      sb.append(role);

    return sb.toString();
  }
}