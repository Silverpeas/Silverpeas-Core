/*
 *  Copyright (C) 2000 - 2013 Silverpeas
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  As a special exception to the terms and conditions of version 3.0 of
 *  the GPL, you may redistribute this Program in connection with Free/Libre
 *  Open Source Software ("FLOSS") applications as described in Silverpeas's
 *  FLOSS exception.  You should have recieved a copy of the text describing
 *  the FLOSS exception, and it is also available here:
 *  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.silverpeas.web.sharing.control;

import org.silverpeas.core.sharing.model.Ticket;
import org.silverpeas.core.sharing.services.SharingTicketService;
import org.silverpeas.core.contribution.attachment.notification.AttachmentEvent;
import org.silverpeas.core.contribution.attachment.notification.AttachmentRef;
import org.silverpeas.core.notification.system.JMSResourceEventListener;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;

/**
 * @author neysseri
 */
@MessageDriven(name = "SharedAttachmentEventListener", activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup",
        propertyValue = "topic/attachments"),
    @ActivationConfigProperty(propertyName = "destinationType",
        propertyValue = "javax.jms.Topic"),
    @ActivationConfigProperty(propertyName = "acknowledgeMode",
        propertyValue = "Auto-acknowledge")})
public class AttachmentSharingListener extends JMSResourceEventListener<AttachmentEvent> {

  @Inject
  private SharingTicketService service;

  @Override
  protected Class<AttachmentEvent> getResourceEventClass() {
    return AttachmentEvent.class;
  }

  @Override
  public void onDeletion(final AttachmentEvent event) throws Exception {
    AttachmentRef attachment = event.getTransition().getBefore();
    if (attachment != null) {
      service.deleteTicketsForSharedObject(attachment.getOldSilverpeasId(), Ticket.FILE_TYPE);
    }
  }
}