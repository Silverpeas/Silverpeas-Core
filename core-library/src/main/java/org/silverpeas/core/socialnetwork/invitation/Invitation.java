/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.socialnetwork.invitation;

import java.util.Date;
import java.util.Objects;

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
    return Objects.equals(this.id, other.id)
        && Objects.equals(this.senderId, other.senderId)
        && Objects.equals(this.receiverId, other.receiverId)
        && Objects.equals(this.message, other.message)
        && Objects.equals(this.invitationDate, other.invitationDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, senderId, receiverId, message, invitationDate);
  }
}
