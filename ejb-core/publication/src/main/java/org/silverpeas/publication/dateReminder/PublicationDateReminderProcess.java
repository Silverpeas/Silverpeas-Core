/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.publication.dateReminder;

import com.silverpeas.annotation.Service;
import com.silverpeas.notification.builder.helper.UserNotificationHelper;
import com.silverpeas.ui.DisplayI18NHelper;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import org.silverpeas.EntityReference;
import org.silverpeas.dateReminder.persistent.PersistentResourceDateReminder;
import org.silverpeas.dateReminder.provider.DateReminderProcess;
import org.silverpeas.dateReminder.provider.DateReminderProcessRegistration;
import org.silverpeas.publication.dateReminder.notification.PublicationDateReminderUserNotification;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;


/**
 * An implementation of <code>DateReminderProcess</code>.
 * Process the date reminder that reference a publication in Silverpeas, such publication being
 * represented by a
 * <code>PublicationDetail</code> instance.
 * @see org.silverpeas.dateReminder.provider.DateReminderProcessRegistration
 * @see org.silverpeas.publication.dateReminder.notification.PublicationDateReminderUserNotification
 *
 * @author Cécile Bonin
 */
@Service
public class PublicationDateReminderProcess implements DateReminderProcess {

  @PostConstruct
  public void register() {
    DateReminderProcessRegistration.register(PublicationDetail.class, this);
  }

  @PreDestroy
  public void unregister() {
    DateReminderProcessRegistration.unregister(PublicationDetail.class, this);
  }

  /**
   * Constructs a NotificationSender from an instanceId.
   * @param componentInstanceId the instanceId of the component.
   * @return NotificationSender
   */
  protected NotificationSender getNotificationSender(String componentInstanceId) {
    return new NotificationSender(componentInstanceId);
  }

  /**
   * Notifies the specified users, identified by their identifier, with the specified notification
   * information.
   *
   * @param notification the notification information.
   * @throws NotificationManagerException if the notification of the recipients fail.
   */
  protected void notifyUsers(final NotificationMetaData notification)
      throws NotificationManagerException {
    NotificationSender notificationSender = getNotificationSender(notification.getComponentId());
    notificationSender.notifyUser(notification);
  }

  @Override
  public EntityReference perform(final PersistentResourceDateReminder resourceDateReminder)
      throws NotificationManagerException {

    ResourceLocator message =
        new ResourceLocator("org.silverpeas.dateReminder.multilang.dateReminder",
            DisplayI18NHelper.getDefaultLanguage());

    //Perform date reminder about publication : send a notification
    PublicationDateReminderUserNotification publicationDateReminderUserNotification =
        new PublicationDateReminderUserNotification(resourceDateReminder, message);
    final NotificationMetaData notification = UserNotificationHelper
          .build(publicationDateReminderUserNotification);
    notifyUsers(notification);

    //Return EntityReference
    PublicationNoteReference pubNoteReference =
        resourceDateReminder.getResource(PublicationNoteReference.class);
    return pubNoteReference;
  }
}
