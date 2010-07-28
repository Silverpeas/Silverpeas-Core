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
package com.silverpeas.socialNetwork.invitation;

import java.util.Date;

public class Invitation {

  private int id;
  private int senderId;
  private int receiverId;
  private String message;
  private Date invitationDate;

  public Invitation(int senderId, int receiverId, String message, Date invitationDate) {
    this.senderId = senderId;
    this.receiverId = receiverId;
    this.message = message;
    this.invitationDate = invitationDate;

  }

  public Invitation() {
  }

  public int getId() {
    return id;
  }

  public Date getInvitationDate() {
    return invitationDate;
  }

  public String getMessage() {
    return message;
  }

  public int getReceiverId() {
    return receiverId;
  }

  public int getSenderId() {
    return senderId;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setInvitationDate(Date invitationDate) {

    this.invitationDate = invitationDate;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setReceiverId(int receiverId) {
    this.receiverId = receiverId;
  }

  public void setSenderId(int senderId) {
    this.senderId = senderId;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Invitation other = (Invitation) obj;
    if (this.id != other.id) {
      return false;
    }
    if (this.senderId != other.senderId) {
      return false;
    }
    if (this.receiverId != other.receiverId) {
      return false;
    }
    if ((this.message == null) ? (other.message != null) : !this.message.equals(other.message)) {
      return false;
    }
    if (this.invitationDate != other.invitationDate && (this.invitationDate == null || !this.invitationDate.
        equals(other.invitationDate))) {
      return false;
    }
    return true;
  }
}
