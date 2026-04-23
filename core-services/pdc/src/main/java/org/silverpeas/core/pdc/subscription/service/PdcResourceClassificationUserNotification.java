/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
import org.silverpeas.core.pdc.subscription.model.PdcSubscription;
import org.silverpeas.kernel.bundle.LocalizationBundle;

import static org.silverpeas.core.util.URLUtil.getSearchResultURL;
import static org.silverpeas.kernel.util.StringUtil.defaultStringIfNotDefined;

public class PdcResourceClassificationUserNotification
    extends AbstractPdcSubscriptionUserNotification<ManagedContribution>
    implements UserSubscriptionNotificationBehavior {

  public PdcResourceClassificationUserNotification(PdcSubscription pdcSubscription,
      ManagedContribution silverContent) {
    super(pdcSubscription, silverContent);
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
    String lang = getUserLanguage(getPdcSubscription().getOwnerId());
    LocalizationBundle resources = getBundle(lang);

    String message = resources.getString("Subscription") + getPdcSubscription().getName() +
      "\n" + resources.getString("DocumentName") + silverContent.getName(lang) + "\n";

    getNotificationMetaData().setTitle(resources.getString("standartMessage"));
    getNotificationMetaData().setContent(message);
  }

  @Override
  protected void performNotificationResource(final ManagedContribution silverContent,
      final NotificationResourceData notificationResourceData) {

    // If the resource is not a SilvepeasContent implementation, id and type are filled here.
    if (notificationResourceData.getResourceId() == null) {
      notificationResourceData.setResourceId(silverContent.getId());
      notificationResourceData.setResourceType("PDCSubscriptionUnknownResourceType");
    }

    // Resource name and description are filled in relation with the user language.
    String lang = getUserLanguage(getPdcSubscription().getOwnerId());
    notificationResourceData.setResourceName(silverContent.getName(lang));
    notificationResourceData.setResourceDescription(silverContent.getDescription(lang));
  }

  @Override
  protected String getResourceURL(final ManagedContribution silverContent) {
    return defaultStringIfNotDefined(getSearchResultURL(silverContent), null);
  }
}
