/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.contribution.publication.model;

import java.util.Date;

import org.silverpeas.core.node.model.NodePK;

public class Alias extends NodePK {

  private static final long serialVersionUID = 6640210341762444143L;

  private String userId = null;
  private Date date = null;
  private int pubOrder = 0;

  private String userName = null; // Not persistent

  public Alias(String nodeId, String instanceId) {
    super(nodeId, instanceId);
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  /**
   * Return the value of the pubOrder property.
   * @return the value of pubOrder.
   */
  public int getPubOrder() {
    return pubOrder;
  }

  /**
   * Set the value of the pubOrder property.
   * @param pubOrder the new value of pubOrder.
   */
  public void setPubOrder(int pubOrder) {
    this.pubOrder = pubOrder;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Alias)) {
      return false;
    }
    return super.equals(other);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
