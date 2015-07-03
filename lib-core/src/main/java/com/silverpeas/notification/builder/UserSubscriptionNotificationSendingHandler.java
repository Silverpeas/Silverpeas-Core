/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
package com.silverpeas.notification.builder;

import com.silverpeas.notification.SilverpeasNotification;
import com.silverpeas.util.StringUtil;
import org.silverpeas.subscription.SubscriptionSettings;

import javax.servlet.http.HttpServletRequest;

import static org.silverpeas.cache.service.CacheServiceFactory.getRequestCacheService;
import static org.silverpeas.cache.service.CacheServiceFactory.getThreadCacheService;

/**
 * This class handles the feature that permits to skip the user subscription notification sending.
 * @author Yohann Chastagnier
 */
public class UserSubscriptionNotificationSendingHandler {

  private static final String SENDING_NOT_ENABLED_KEY =
      UserSubscriptionNotificationSendingHandler.class.getName() + "#SENDING_ENABLED";

  private static final String SENDING_NOT_ENABLED_JMS_WAY_KEY =
      UserSubscriptionNotificationSendingHandler.class.getName() + "#SENDING_ENABLED_JMS_WAY";

  /**
   * As treatments of asynchronous JMS notifications are executed in an other context of the user
   * request, the indicator of the confirmation of subscription notification sending must be
   * managed here (as this indicator is attached to the user request).
   * @param silverpeasNotification the notification that will be send on JMS.
   */
  public static void setupSilverpeasNotification(SilverpeasNotification silverpeasNotification) {
    if (!isEnabledForCurrentRequest()) {
      silverpeasNotification.addParameter(
          UserSubscriptionNotificationBehavior.SKIP_SUBSCRIPTION_NOTIFICATION_SENDING_HTTP_PARAM,
          "true");
    }
  }

  /**
   * Verifies from a silverpeas notification if it is indicated that subscription notification
   * sending must be skipped.
   * @param silverpeasNotification the notification sent on JMS.
   */
  public static void verifySilverpeasNotification(SilverpeasNotification silverpeasNotification) {
    if (SubscriptionSettings.isSubscriptionNotificationSendingConfirmationEnabled() && StringUtil
        .getBooleanValue(silverpeasNotification.getParameterValue(
            UserSubscriptionNotificationBehavior
                .SKIP_SUBSCRIPTION_NOTIFICATION_SENDING_HTTP_PARAM))) {
      getThreadCacheService().put(SENDING_NOT_ENABLED_JMS_WAY_KEY, true);
    } else {
      getThreadCacheService().remove(SENDING_NOT_ENABLED_JMS_WAY_KEY);
    }
  }

  /**
   * Verifies from a request if it is indicated that subscription notification sending must be
   * skipped for the current request.
   * @param request the current HTTP request.
   */
  public static void verifyRequestParameters(HttpServletRequest request) {
    if (SubscriptionSettings.isSubscriptionNotificationSendingConfirmationEnabled() && StringUtil
        .getBooleanValue(request.getParameter(
            UserSubscriptionNotificationBehavior
                .SKIP_SUBSCRIPTION_NOTIFICATION_SENDING_HTTP_PARAM))) {
      getRequestCacheService().put(SENDING_NOT_ENABLED_KEY, true);
    }
  }

  /**
   * Indicates if the user subscription notification sending is enabled for the current request.
   * @return true if enabled, false otherwise.
   */
  public static boolean isEnabledForCurrentRequest() {
    Boolean notEnabled = getRequestCacheService().get(SENDING_NOT_ENABLED_KEY, Boolean.class);
    if (notEnabled == null) {
      notEnabled = getThreadCacheService().get(SENDING_NOT_ENABLED_JMS_WAY_KEY, Boolean.class);
    }
    return notEnabled == null || !notEnabled;
  }
}
