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
package com.silverpeas.sharing.control;

import com.silverpeas.notification.DefaultNotificationSubscriber;
import com.silverpeas.notification.NotificationTopic;
import com.silverpeas.notification.SilverpeasNotification;
import com.silverpeas.sharing.model.Ticket;
import com.silverpeas.sharing.services.SharingTicketService;
import com.stratelia.webactiv.publication.model.PublicationPK;

import javax.inject.Inject;
import javax.inject.Named;

import org.silverpeas.publication.notification.PublicationDeletionNotification;

import static com.silverpeas.notification.NotificationTopic.onTopic;
import static com.silverpeas.notification.RegisteredTopics.PUBLICATION_TOPIC;

/**
 *
 * @author neysseric
 */
@Named
public class PublicationSharingListener extends DefaultNotificationSubscriber {

  @Inject
  private SharingTicketService service;

  @Override
  public void subscribeOnTopics() {
    subscribeForNotifications(onTopic(PUBLICATION_TOPIC.getTopicName()));
  }

  @Override
  public void unsubscribeOnTopics() {
    unsubscribeForNotifications(onTopic(PUBLICATION_TOPIC.getTopicName()));
  }

  @Override
  public void onNotification(SilverpeasNotification notification, NotificationTopic onTopic) {
    if (PUBLICATION_TOPIC.getTopicName().equals(onTopic.getName())) {
      PublicationDeletionNotification deletion = (PublicationDeletionNotification) notification;
      PublicationPK pk = deletion.getPublicationPK();
      service.deleteTicketsForSharedObject(Long.parseLong(pk.getId()), Ticket.PUBLICATION_TYPE);
    }
  }
}
