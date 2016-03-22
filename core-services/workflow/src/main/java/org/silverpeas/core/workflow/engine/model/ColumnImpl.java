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

import org.silverpeas.core.workflow.api.model.AbstractDescriptor;
import org.silverpeas.core.workflow.api.model.Column;
import org.silverpeas.core.workflow.api.model.Item;
import org.silverpeas.core.workflow.engine.AbstractReferrableObject;

/**
 * Class implementing the representation of the &lt;column&gt; element of a Process Model.
 **/
public class ColumnImpl extends AbstractReferrableObject implements Column, AbstractDescriptor,
    Serializable {
  private static final long serialVersionUID = 3766121048611753846L;

  // ~ Instance fields ////////////////////////////////////////////////////////
  private AbstractDescriptor parent;
  private boolean hasId = false;
  private int id;
  private Item item;

  /************* Implemented methods *****************************************/

  // ~ Methods ////////////////////////////////////////////////////////////////

  /*
   * @see Column#getItem()
   */
  public Item getItem() {
    return item;
  }

  /*
   * @see Column#setItem(Item)
   */
  public void setItem(Item item) {
    this.item = item;
  }

  /*
   * (non-Javadoc)
   * @see AbstractDescriptor#setId(int)
   */
  public void setId(int id) {
    this.id = id;
    hasId = true;
  }

  /*
   * (non-Javadoc)
   * @see AbstractDescriptor#getId()
   */
  public int getId() {
    return id;
  }

  /*
   * (non-Javadoc)
   * @see AbstractDescriptor#setParent(com.silverpeas
   * .workflow.api.model.AbstractDescriptor)
   */
  public void setParent(AbstractDescriptor parent) {
    this.parent = parent;
  }

  /*
   * (non-Javadoc)
   * @see AbstractDescriptor#getParent()
   */
  public AbstractDescriptor getParent() {
    return parent;
  }

  /*
   * (non-Javadoc)
   * @see AbstractDescriptor#hasId()
   */
  public boolean hasId() {
    return hasId;
  }

  /*
   * (non-Javadoc)
   * @see AbstractReferrableObject#getKey()
   */
  public String getKey() {
    if (item == null)
      return "";
    else
      return item.getName();
  }
}