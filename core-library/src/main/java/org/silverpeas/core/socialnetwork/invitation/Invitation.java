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

package org.silverpeas.core.socialnetwork.invitation;

import java.util.Date;

public class Invitation {

  private int id;
  private int senderId;
  private int receiverId;
  private String message;
  private Date invitationDate;

  /**
   * Constructor using fields
   * @param senderId
   * @param receiverId
   * @param message
   * @param invitationDate
   */
  public Invitation(int senderId, int receiverId, String message, Date invitationDate) {
    this.senderId = senderId;
    this.receiverId = receiverId;
    this.message = message;
    this.invitationDate = invitationDate;
  }

  /**
   * Default Constructor
   */
  public Invitation() {
  }

  /**
   * get Id of invitation
   * @return id the invitation identifier
   */
  public int getId() {
    return id;
  }

  /**
   * get Date of invitation
   * @return Date
   */
  public Date getInvitationDate() {
    return invitationDate;
  }

  /**
   * get Message of invitation
   * @return String
   */
  public String getMessage() {
    return message;
  }

  /**
   * get the receiver of invitation
   * @return int
   */
  public int getReceiverId() {
    return receiverId;
  }

  /**
   * get the sender of invitation
   * @return int
   */
  public int getSenderId() {
    return senderId;
  }

  /**
   * set the Id of invitation
   * @param id
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * set the Date of invitation
   * @param invitationDate
   */
  public void setInvitationDate(Date invitationDate) {

    this.invitationDate = invitationDate;
  }

  /**
   * set the Message of invitation
   * @param message
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * set the Receiver of invitation
   * @param receiverId
   */
  public void setReceiverId(int receiverId) {
    this.receiverId = receiverId;
  }

  /**
   * set the sender of invitation
   * @param senderId
   */
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
    if (this.invitationDate != other.invitationDate &&
        (this.invitationDate == null || !this.invitationDate.
        equals(other.invitationDate))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 67 * hash + this.id;
    hash = 67 * hash + this.senderId;
    hash = 67 * hash + this.receiverId;
    hash = 67 * hash + (this.message != null ? this.message.hashCode() : 0);
    hash = 67 * hash + (this.invitationDate != null ? this.invitationDate.hashCode() : 0);
    return hash;
  }
}
