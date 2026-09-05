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

import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.pdc.subscription.model.PdcSubscriptionPositionCriteria;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.kernel.bundle.LocalizationBundle;

import java.util.Collection;

public class PdcSubscriptionDeletionUserNotification
    extends AbstractPdcSubscriptionUserNotification<PdcSubscriptionPositionCriteria> {

  private static final String MESSAGE_DELETE_TITLE = "notification.delete.title";
  private static final String SOURCE_CLASSIFICATION = "pdcClassification";

  private final boolean valueDeleted;
  private final String axisName;

  public PdcSubscriptionDeletionUserNotification(PdcSubscriptionPositionCriteria pdcResource,
      Collection<String> recipientIds, String axisName, boolean valueDeleted) {
    super(pdcResource, recipientIds, pdcResource);
    this.valueDeleted = valueDeleted;
    this.axisName = axisName;
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.DELETE;
  }

  @Override
  protected boolean isSendImmediately() {
    return true;
  }

  @Override
  protected String getComponentInstanceId() {
    // This notification doesn't concern a component.
    return null;
  }

  @Override
  protected String getSender() {
    // Empty is here returned, because the notification is from the platform and not from another user.
    return "";
  }

  @Override
  protected void performBuild(final PdcSubscriptionPositionCriteria resource) {
    DisplayI18NHelper.getLanguages().forEach(lang -> {
      final LocalizationBundle resources = getBundle(lang);
      final StringBuilder message = new StringBuilder(150);

      if (valueDeleted) {
        message.append(resources.getString("deleteOnValueMessage"));
      } else {
        message.append(resources.getString("deleteOnAxisMessage"));
      }
      message.append("\n");

      message.append(resources.getString("Subscription"));
      message.append(resource.getName());
      message.append("\n");

      message.append(resources.getString("Axis"));
      message.append(axisName);
      message.append("\n");

      getNotificationMetaData()
          .addLanguage(lang, resources.getString(MESSAGE_DELETE_TITLE), message.toString());
    });
    getNotificationMetaData().setSource(
        getBundle(DisplayI18NHelper.getDefaultLanguage()).getString(SOURCE_CLASSIFICATION));
  }

  @Override
  protected void performNotificationResource(final PdcSubscriptionPositionCriteria resource,
      final NotificationResourceData notificationResourceData) {
    // Nothing is done here because of delayed notification that is not handled for this kind of
    // PDC user notification.
  }
}
