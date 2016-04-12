/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.notification.user.client;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.exception.SilverpeasException;

/**
 * @author Yohann Chastagnier
 */
public class CurrentUserNotificationContext {

  /**
   * Gets the {@link CurrentUserNotificationContext}
   * instance associated to the current request.
   * @return the {@link CurrentUserNotificationContext}
   * instance associated to the current request.
   */
  public static CurrentUserNotificationContext getCurrentUserNotificationContext() {
    CurrentUserNotificationContext current = CacheServiceProvider.getRequestCacheService()
        .get(CurrentUserNotificationContext.class.getName(), CurrentUserNotificationContext.class);
    if (current == null) {
      current = new CurrentUserNotificationContext();
      CacheServiceProvider.getRequestCacheService()
          .put(CurrentUserNotificationContext.class.getName(), current);
    }
    return current;
  }

  /**
   * Performs some control around a manual user notification by considering that the sender is
   * the current user.
   * @param notificationMetaData
   * @throws NotificationManagerException
   */
  public void checkManualUserNotification(NotificationMetaData notificationMetaData)
      throws NotificationManagerException {
    if (NotificationManagerSettings.isUserManualNotificationRecipientLimitEnabled() && notificationMetaData.isManualUserOne()) {
      int nbUserReceivers = notificationMetaData.getAllUserRecipients().size();

      // Checking the limit
      final boolean limitExceeded;
      UserDetail currentUser = UserDetail.getCurrentRequester();
      if (currentUser != null) {
        limitExceeded = currentUser.isUserManualNotificationUserReceiverLimit() &&
            currentUser.getUserManualNotificationUserReceiverLimitValue() < nbUserReceivers;
      } else {
        limitExceeded = NotificationManagerSettings.getUserManualNotificationRecipientLimit() < nbUserReceivers;
      }

      // Exception if limit exceeded
      if (limitExceeded) {
        throw new NotificationManagerException("CurrentUserNotificationContext",
            SilverpeasException.ERROR,
            "notificationManager.EX_USER_MANUAL_NOTIFICATION_LIMIT_EXCEEDED");
      }
    }
  }
}
