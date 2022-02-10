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

package org.silverpeas.web.notificationuser.notification;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.notification.user.AbstractComponentInstanceManualUserNotification;
import org.silverpeas.core.notification.user.NotificationContext;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.web.notificationuser.Notification;

import javax.inject.Named;
import java.util.Optional;

import static org.silverpeas.core.ui.DisplayI18NHelper.getLanguages;
import static org.silverpeas.core.util.ResourceLocator.getLocalizationBundle;

/**
 * @author silveryocha
 */
@Named
public class UserNotificationInstanceManualUserNotification extends
    AbstractComponentInstanceManualUserNotification {

  @Override
  public UserNotification createUserNotification(final NotificationContext context) {
    final int priority;
    if (context.containsKey("priorityId")) {
      priority = Integer.parseInt(context.get("priorityId"));
    } else {
      priority = 0;
    }
    final Notification manualNotification = new Notification();
    manualNotification.setPriority(priority);
    final Optional<SilverpeasComponentInstance> component = Optional
        .ofNullable(context.getComponentId()).filter(StringUtil::isDefined)
        .flatMap(SilverpeasComponentInstance::getById);
    if (component.isPresent()) {
      final SilverpeasComponentInstance instance = component.get();
      final NotificationMetaData metaData = manualNotification.getNotificationMetaData();
      metaData.setComponentId(instance.getId());
      for (String language : getLanguages()) {
        final LocalizationBundle bundle = getLocalizationBundle(
            "org.silverpeas.alertUserPeas.multilang.alertUserPeasBundle", language);
        final String title = getBundle(language)
            .getStringWithParams("custom.notification.subject", instance.getLabel());
        metaData.addLanguage(language, title, bundle.getString("AuthorMessage"));
      }
    } else {
      final String userLanguage = context.getSender().getUserPreferences().getLanguage();
      manualNotification.setSource(getBundle(userLanguage).getString("manualNotification"));
    }
    manualNotification.setAddressId(context.get("notificationId"));
    return manualNotification;
  }

  private LocalizationBundle getBundle(final String language) {
    return getLocalizationBundle("org.silverpeas.notificationUser.multilang.notificationUserBundle",
        language);
  }
}
