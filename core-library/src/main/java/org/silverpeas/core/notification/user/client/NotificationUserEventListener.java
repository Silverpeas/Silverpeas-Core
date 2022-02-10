/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.notification.user.client;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.notification.UserEvent;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.notification.system.CDIResourceEventListener;
import org.silverpeas.core.notification.user.client.constant.BuiltInNotifAddress;
import org.silverpeas.core.notification.user.client.constant.NotifChannel;
import org.silverpeas.core.notification.user.client.model.NotifDefaultAddressRow;
import org.silverpeas.core.notification.user.client.model.NotifDefaultAddressTable;
import org.silverpeas.core.notification.user.client.model.NotificationSchema;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.SQLException;
import java.util.Optional;

/**
 * A listener of events about a given user account in Silverpeas.
 * @author mmoquillon
 */
@Bean
@Singleton
public class NotificationUserEventListener extends CDIResourceEventListener<UserEvent> {

  @Inject
  private NotificationSchema notificationSchema;

  @Override
  @Transactional
  public void onDeletion(final UserEvent event) throws Exception {
    dereferenceUserFromUserNotification(event);
  }

  @Transactional
  public void dereferenceUserFromUserNotification(UserEvent event) throws SQLException {
    UserDetail user = event.getTransition().getBefore();
    int userId = Integer.parseInt(user.getId());
    notificationSchema.notifDefaultAddress().dereferenceUserId(userId);
    notificationSchema.notifPreference().dereferenceUserId(userId);
    notificationSchema.notifAddress().dereferenceUserId(userId);
  }

  @Override
  @Transactional
  public void onCreation(final UserEvent event) throws Exception {
    UserDetail user = event.getTransition().getAfter();
    checkNotificationChannel(user);
  }

  @Transactional
  public void checkNotificationChannel(UserDetail user) throws SQLException {
    // if user have no email defined, using silvermail by default
    if (StringUtil.isNotDefined(user.geteMail())) {
      int silverMailChannelId = BuiltInNotifAddress.BASIC_SILVERMAIL.getId();
      var userId = Integer.parseInt(user.getId());
      // check if silvermail channel is used by default in the platform settings
      Optional<NotifChannel> silverMailChannelAsDefault =
          NotificationManagerSettings.getDefaultChannels().stream()
              .filter(c -> c.getMediaType().getId() == silverMailChannelId)
              .findFirst();
      if (silverMailChannelAsDefault.isEmpty()) {
        NotifDefaultAddressTable ndat = notificationSchema.notifDefaultAddress();
        var newRow = new NotifDefaultAddressRow(-1, userId, silverMailChannelId);
        ndat.create(newRow);
      }
    }
  }
}
