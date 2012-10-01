/*
 *  Copyright (C) 2000 - 2011 Silverpeas
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
 *  "http://www.silverpeas.com/legal/licensing"
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
package com.silverpeas.sharing.control;

import javax.inject.Inject;
import javax.inject.Named;

import org.silverpeas.attachment.notification.AttachmentDeletionNotification;
import org.silverpeas.attachment.notification.AttachmentRef;
import org.silverpeas.versioning.notification.VersioningDeletionNotification;

import com.silverpeas.notification.DefaultNotificationSubscriber;
import com.silverpeas.notification.NotificationTopic;
import com.silverpeas.notification.SilverpeasNotification;
import com.silverpeas.sharing.model.Ticket;
import com.silverpeas.sharing.services.SharingTicketService;

import com.stratelia.silverpeas.versioning.model.Document;

import static com.silverpeas.notification.NotificationTopic.onTopic;
import static com.silverpeas.notification.RegisteredTopics.ATTACHMENT_TOPIC;

/**
 *
 * @author neysseri
 */
@Named
public class AttachmentSharingListener extends DefaultNotificationSubscriber {

  @Inject
  private SharingTicketService service;

  @Override
  public void subscribeOnTopics() {
    subscribeForNotifications(onTopic(ATTACHMENT_TOPIC.getTopicName()));
  }

  @Override
  public void unsubscribeOnTopics() {
    unsubscribeForNotifications(onTopic(ATTACHMENT_TOPIC.getTopicName()));
  }

  @Override
  public void onNotification(SilverpeasNotification notification, NotificationTopic onTopic) {
    if (ATTACHMENT_TOPIC.getTopicName().equals(onTopic.getName())) {
      if (notification instanceof AttachmentDeletionNotification) {
        AttachmentDeletionNotification deletion = (AttachmentDeletionNotification) notification;
        AttachmentRef attachment = deletion.getAttachment();
        if (attachment != null) {
          service.deleteTicketsForSharedObject(attachment.getOldSilverpeasId(), Ticket.FILE_TYPE);
        }
      } else if (notification instanceof VersioningDeletionNotification) {
        VersioningDeletionNotification deletion = (VersioningDeletionNotification) notification;
        Document document = deletion.getDocument();
        if (document != null) {
          service.deleteTicketsForSharedObject(Long.parseLong(document.getPk().getId()), Ticket.VERSION_TYPE);
        }
      }
    }
  }
}