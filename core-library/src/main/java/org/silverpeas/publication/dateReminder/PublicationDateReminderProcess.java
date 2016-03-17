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

import org.silverpeas.ui.DisplayI18NHelper;
import com.silverpeas.usernotification.builder.helper.UserNotificationHelper;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.webactiv.publication.model.PublicationDetail;
import org.silverpeas.EntityReference;
import org.silverpeas.dateReminder.persistent.PersistentResourceDateReminder;
import org.silverpeas.dateReminder.provider.DateReminderProcess;
import org.silverpeas.dateReminder.provider.DateReminderProcessRegistration;
import org.silverpeas.initialization.Initialization;
import org.silverpeas.publication.dateReminder.notification.PublicationDateReminderUserNotification;
import org.silverpeas.util.LocalizationBundle;
import org.silverpeas.util.ResourceLocator;

/**
 * An implementation of <code>DateReminderProcess</code>.
 * Process the date reminder that reference a publication in Silverpeas, such publication being
 * represented by a
 * <code>PublicationDetail</code> instance.
 * @author Cécile Bonin
 * @see org.silverpeas.dateReminder.provider.DateReminderProcessRegistration
 * @see org.silverpeas.publication.dateReminder.notification.PublicationDateReminderUserNotification
 */
public class PublicationDateReminderProcess implements DateReminderProcess, Initialization {

  @Override
  public void init() throws Exception {
    DateReminderProcessRegistration.register(PublicationDetail.class, this);
  }

  @Override
  public void release() throws Exception {
    DateReminderProcessRegistration.unregister(PublicationDetail.class, this);
  }

  @Override
  public EntityReference perform(final PersistentResourceDateReminder resourceDateReminder)
      throws NotificationManagerException {

    LocalizationBundle message = ResourceLocator
        .getLocalizationBundle("org.silverpeas.dateReminder.multilang.dateReminder",
            DisplayI18NHelper.getDefaultLanguage());

    //Perform date reminder about publication : send a notification
    PublicationDateReminderUserNotification publicationDateReminderUserNotification =
        new PublicationDateReminderUserNotification(resourceDateReminder, message);
    UserNotificationHelper.buildAndSend(publicationDateReminderUserNotification);

    //Return EntityReference
    PublicationNoteReference pubNoteReference =
        resourceDateReminder.getResource(PublicationNoteReference.class);
    return pubNoteReference;
  }
}