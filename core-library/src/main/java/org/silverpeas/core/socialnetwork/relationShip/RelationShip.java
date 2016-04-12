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

package org.silverpeas.core.socialnetwork.relationShip;

import java.util.Date;

public class RelationShip {

  private int idRelationShip;
  private int user1Id;
  private int user2Id;
  private int typeRelationShipId;
  private Date acceptanceDate;
  private int inviterId;

  public RelationShip(int user1Id, int user2Id, int typeRelationShipId, Date acceptanceDate,
      int inviterId) {
    this.user1Id = user1Id;
    this.user2Id = user2Id;
    this.typeRelationShipId = typeRelationShipId;
    this.acceptanceDate = acceptanceDate;
  }

  public RelationShip() {
  }

  /**
   * get id of relationShip
   * @return int
   */
  public int getId() {
    return idRelationShip;
  }

  /**
   * get date of relationShip
   * @return date
   */
  public Date getAcceptanceDate() {
    return acceptanceDate;
  }

  /**
   * get the id of this ralationShip type
   * @return int
   */
  public int getTypeRelationShipId() {
    return typeRelationShipId;
  }

  /**
   * get the first user of this Invitation (the inviter)
   * @return int
   */
  public int getUser1Id() {
    return user1Id;
  }

  /**
   * get the second user of this Invitation
   * @return int
   */
  public int getUser2Id() {
    return user2Id;
  }

  /**
   * set the id of relationShip
   * @param id
   */
  public void setId(int id) {
    this.idRelationShip = id;
  }

  /**
   * set the date of relationShip
   * @param acceptanceDate Date
   */
  public void setAcceptanceDate(Date acceptanceDate) {
    this.acceptanceDate = acceptanceDate;
  }

  /**
   * set the date of relationShip
   * @param typeRelationShipId int
   */
  public void setTypeRelationShipId(int typeRelationShipId) {
    this.typeRelationShipId = typeRelationShipId;
  }

  /**
   * set the first user of relationShip
   * @param user1Id int
   */
  public void setUser1Id(int user1Id) {
    this.user1Id = user1Id;
  }

  /**
   * set the second user of relationShip
   * @param user2Id int
   */
  public void setUser2Id(int user2Id) {
    this.user2Id = user2Id;
  }

  /**
   * set the inviter user
   * @param inviterId int
   */
  public void setInviterId(int inviterId) {
    this.inviterId = inviterId;
  }

  /**
   * get the inviter
   * @return int
   */
  public int getInviterId() {
    return inviterId;
  }

  /**
   * Indicates whether some other object is "equal to" this one.
   *@param obj the reference object with which to compare.
   *@return <code>true</code> if this object is the same as the obj argument; <code>false</code>
   * otherwise.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final RelationShip other = (RelationShip) obj;
    if (this.idRelationShip != other.idRelationShip) {
      return false;
    }
    if (this.user1Id != other.user1Id) {
      return false;
    }
    if (this.user2Id != other.user2Id) {
      return false;
    }
    if (this.typeRelationShipId != other.typeRelationShipId) {
      return false;
    }
    if (this.acceptanceDate != other.acceptanceDate &&
        (this.acceptanceDate == null || !this.acceptanceDate.
        equals(other.acceptanceDate))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 43 * hash + this.idRelationShip;
    hash = 43 * hash + this.user1Id;
    hash = 43 * hash + this.user2Id;
    hash = 43 * hash + this.typeRelationShipId;
    hash = 43 * hash + (this.acceptanceDate != null ? this.acceptanceDate.hashCode() : 0);
    return hash;
  }
}
