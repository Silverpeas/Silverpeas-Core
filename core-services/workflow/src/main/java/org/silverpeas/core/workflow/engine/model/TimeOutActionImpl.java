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

import org.silverpeas.core.workflow.api.model.Action;
import org.silverpeas.core.workflow.api.model.Item;
import org.silverpeas.core.workflow.api.model.TimeOutAction;

/**
 * Representation of the &lt;timeoutAction&gt; element of a Process Model.
 * @author Ludovic Bertin
 */
public class TimeOutActionImpl implements TimeOutAction, Serializable {

  private static final long serialVersionUID = -7434214806057433378L;
  private Action action = null;
  private String delay = null;
  private int order = 0;
  private Item dateItem = null;

  /*
   * (non-Javadoc)
   * @see TimeOutAction#getAction()
   */
  @Override
  public Action getAction() {
    return action;
  }

  public void setAction(Action action) {
    this.action = action;
  }

  public void setDelay(String delay) {
    this.delay = delay;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  /**
   * @param dateItem the dateItem to set
   */
  public void setDateItem(Item dateItem) {
    this.dateItem = dateItem;
  }

  /*
   * (non-Javadoc)
   * @see TimeOutAction#getDelay()
   */
  @Override
  public String getDelay() {
    return delay;
  }

  /*
   * (non-Javadoc)
   * @see TimeOutAction#getOrder()
   */
  @Override
  public int getOrder() {
    return order;
  }

  /*
   * (non-Javadoc)
   * @see TimeOutAction#getDateItem()
   */
  @Override
  public Item getDateItem() {
    return dateItem;
  }

  /**
   * Get the timeout action order
   * @return order (as a String)
   */
  public String castor_getOrder() {
    if (order > 0)
      return String.valueOf(order);
    else
      return null;
  }

  /**
   * Set the timeout action order
   * @param order timeout order
   */
  public void castor_setOrder(String order) {
    try {
      this.order = (Integer.valueOf(order)).intValue();
    } catch (NumberFormatException e) {
      this.order = 0;
    }
  }

}
