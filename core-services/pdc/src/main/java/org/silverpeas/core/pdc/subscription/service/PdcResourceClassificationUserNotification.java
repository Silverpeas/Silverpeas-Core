/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.pdc.subscription.service;

import org.silverpeas.core.contribution.contentcontainer.content.ManagedContribution;
import org.silverpeas.core.notification.user.UserSubscriptionNotificationBehavior;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.pdc.subscription.model.PdcSubscriptionPositionCriteria;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.kernel.bundle.LocalizationBundle;

import java.util.Collection;

import static org.silverpeas.core.util.URLUtil.getSearchResultURL;
import static org.silverpeas.kernel.util.StringUtil.defaultStringIfNotDefined;

public class PdcResourceClassificationUserNotification
    extends AbstractPdcSubscriptionUserNotification<ManagedContribution>
    implements UserSubscriptionNotificationBehavior {

  public PdcResourceClassificationUserNotification(PdcSubscriptionPositionCriteria pdcResource,
      Collection<String> recipientIds, ManagedContribution silverContent) {
    super(pdcResource, recipientIds, silverContent);
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.CLASSIFIED;
  }

  @Override
  protected boolean isSendImmediately() {
    /*
     * For now, pdc notifications can not be handled by delayed notification mechanism. When
     * it will be the case, don't forget to remove this overridden method
     */
    return true;
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getComponentInstanceId();
  }

  @Override
  protected String getSender() {
    return getResource().getCreator().getId();
  }

  @Override
  protected void performBuild(final ManagedContribution silverContent) {
    DisplayI18NHelper.getLanguages().forEach(lang -> {
      final LocalizationBundle resources = getBundle(lang);
      final String message = resources.getString("Subscription") +
          getPdcSubscriptionPositionCriteria().getName() + "\n" + resources.getString("DocumentName") +
          silverContent.getName(lang) + "\n";
      getNotificationMetaData()
          .addLanguage(lang, resources.getString("standartMessage"), message);
    });
  }

  /**
   * As the notification is sent at once to several subscribers that don't necessarily share the
   * same language, a {@link NotificationResourceData} is computed for each of the languages
   * supported by the platform instead of for the single default one.
   */
  @Override
  protected void performNotificationResource(final ManagedContribution silverContent) {
    DisplayI18NHelper.getLanguages().forEach(lang -> {
      final NotificationResourceData data = initializeNotificationResourceData();
      performNotificationResource(silverContent, data, lang);
      getNotificationMetaData().setNotificationResourceData(lang, data);
    });
  }

  @Override
  protected void performNotificationResource(final ManagedContribution silverContent,
      final NotificationResourceData notificationResourceData) {
    performNotificationResource(silverContent, notificationResourceData,
        DisplayI18NHelper.getDefaultLanguage());
  }

  private void performNotificationResource(final ManagedContribution silverContent,
      final NotificationResourceData notificationResourceData, final String language) {

    // If the resource is not a SilvepeasContent implementation, id and type are filled here.
    if (notificationResourceData.getResourceId() == null) {
      notificationResourceData.setResourceId(silverContent.getId());
      notificationResourceData.setResourceType("PDCSubscriptionUnknownResourceType");
    }

    // Resource name and description are filled in relation with the language of the recipients.
    notificationResourceData.setResourceName(silverContent.getName(language));
    notificationResourceData.setResourceDescription(silverContent.getDescription(language));
  }

  @Override
  protected String getResourceURL(final ManagedContribution silverContent) {
    return defaultStringIfNotDefined(getSearchResultURL(silverContent), null);
  }
}
