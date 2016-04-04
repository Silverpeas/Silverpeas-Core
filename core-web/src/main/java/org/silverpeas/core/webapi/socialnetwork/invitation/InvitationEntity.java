/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.webapi.socialnetwork.invitation;

import org.silverpeas.core.webapi.profile.ProfileResourceBaseURIs;
import org.silverpeas.core.socialnetwork.invitation.Invitation;
import org.silverpeas.core.webapi.base.WebEntity;

import java.net.URI;
import java.util.Date;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The entity emboddied into an HTTP request or response and that represents the state of an
 * invitation from a given user to another one.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class InvitationEntity extends Invitation implements WebEntity {
  private static final long serialVersionUID = -3801150192463666441L;

  public static InvitationEntity fromInvitation(final Invitation invitation) {
    return new InvitationEntity(invitation);
  }

  @XmlElement
  private URI uri;
  @XmlElement @NotNull
  private URI senderUri;
  @XmlElement @NotNull
  private URI receiverUri;
  private final Invitation invitation;

  private InvitationEntity(Invitation invitation) {
    this.invitation = invitation;
    this.senderUri = ProfileResourceBaseURIs.uriOfUser(String.valueOf(invitation.getSenderId()));
    this.receiverUri = ProfileResourceBaseURIs.uriOfUser(String.valueOf(invitation.getReceiverId()));
  }

  @Override
  public URI getURI() {
    return uri;
  }

  public URI getReceiverUri() {
    return receiverUri;
  }

  public URI getSenderUri() {
    return senderUri;
  }

  @Override
  public void setSenderId(int senderId) {
    invitation.setSenderId(senderId);
  }

  @Override
  public void setReceiverId(int receiverId) {
    invitation.setReceiverId(receiverId);
  }

  @Override
  public void setMessage(String message) {
    invitation.setMessage(message);
  }

  @Override
  public void setInvitationDate(Date invitationDate) {
    invitation.setInvitationDate(invitationDate);
  }

  @Override
  public void setId(int id) {
    invitation.setId(id);
  }

  @XmlElement @NotNull
  @Override
  public int getSenderId() {
    return invitation.getSenderId();
  }

  @XmlElement @NotNull
  @Override
  public int getReceiverId() {
    return invitation.getReceiverId();
  }

  @XmlElement
  @Override
  public String getMessage() {
    return invitation.getMessage();
  }

  @XmlElement @NotNull
  @Override
  public Date getInvitationDate() {
    return invitation.getInvitationDate();
  }

  @XmlElement
  @Override
  public int getId() {
    return invitation.getId();
  }

  public InvitationEntity withAsUri(URI invitationUri) {
    this.uri = invitationUri;
    return this;
  }

  protected InvitationEntity() {
    this.invitation = new Invitation();
  }
}
