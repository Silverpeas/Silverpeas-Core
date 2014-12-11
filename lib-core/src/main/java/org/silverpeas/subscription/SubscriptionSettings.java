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
package org.silverpeas.subscription;

import com.stratelia.webactiv.util.ResourceLocator;

/**
 * All settings around the subscription services.
 * @author Yohann Chastagnier
 */
public class SubscriptionSettings {

  /**
   * The relative path of the properties file containing the settings of subscription services.
   */
  public static final String SETTINGS_PATH =
      "org.silverpeas.subscription.settings.subscriptionSettings";


  /**
   * Gets all the settings of subscription services.
   * @return the resource with the different subscription settings.
   */
  private static ResourceLocator getSettings() {
    return new ResourceLocator(SETTINGS_PATH, "");
  }

  /**
   * Indicates if the the confirmation of sending of subscription notification is enabled.
   * @return true if enabled (default value), false otherwise.
   */
  public static boolean isSubscriptionNotificationSendingConfirmationEnabled() {
    return getSettings().getBoolean("subscription.send.confirmation.enabled", true);
  }
}
