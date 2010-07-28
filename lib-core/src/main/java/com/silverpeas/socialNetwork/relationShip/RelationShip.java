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
package com.silverpeas.socialNetwork.relationShip;

import java.util.Date;

public class RelationShip {

  private int idRelationShip;
  private int user1Id;
  private int user2Id;
  private int typeRelationShipId;
  private Date acceptanceDate;

  public RelationShip(int user1Id, int user2Id, int typeRelationShipId, Date acceptanceDate) {
    this.user1Id = user1Id;
    this.user2Id = user2Id;
    this.typeRelationShipId = typeRelationShipId;
    this.acceptanceDate = acceptanceDate;
  }

  public RelationShip() {
    // TODO Auto-generated constructor stub
  }

  public int getId() {
    return idRelationShip;
  }

  public Date getAcceptanceDate() {
    return acceptanceDate;
  }

  public int getTypeRelationShipId() {
    return typeRelationShipId;
  }

  public int getUser1Id() {
    return user1Id;
  }

  public int getUser2Id() {
    return user2Id;
  }

  public void setId(int id) {
    this.idRelationShip = id;
  }

  public void setAcceptanceDate(Date acceptanceDate) {
    this.acceptanceDate = acceptanceDate;
  }

  public void setTypeRelationShipId(int typeRelationShipId) {
    this.typeRelationShipId = typeRelationShipId;
  }

  public void setUser1Id(int user1Id) {
    this.user1Id = user1Id;
  }

  public void setUser2Id(int user2Id) {
    this.user2Id = user2Id;
  }

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
    if (this.acceptanceDate != other.acceptanceDate && (this.acceptanceDate == null || !this.acceptanceDate.
        equals(other.acceptanceDate))) {
      return false;
    }
    return true;
  }
}
