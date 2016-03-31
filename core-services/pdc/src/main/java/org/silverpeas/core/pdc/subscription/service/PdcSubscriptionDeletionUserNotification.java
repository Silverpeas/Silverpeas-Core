/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.pdc.subscription.service;

import org.silverpeas.core.pdc.subscription.model.PdcSubscription;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.util.LocalizationBundle;

public class PdcSubscriptionDeletionUserNotification
    extends AbstractPdcSubscriptionUserNotification<PdcSubscription> {

  private final static String MESSAGE_DELETE_TITLE = "notification.delete.title";
  private final static String SOURCE_CLASSIFICATION = "pdcClassification";

  boolean valueDeleted = false;
  String axisName = null;

  public PdcSubscriptionDeletionUserNotification(PdcSubscription pdcSubscription, String axisName,
      boolean valueDeleted) {
    super(pdcSubscription, pdcSubscription);
    this.valueDeleted = valueDeleted;
    this.axisName = axisName;
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.DELETE;
  }

  @Override
  protected boolean isSendImmediatly() {
    return true;
  }

  @Override
  protected String getComponentInstanceId() {
    // This notification doesn't concerned a component.
    return null;
  }

  @Override
  protected String getSender() {
    // Empty is here returned, because the notification is from the platform and not from an
    // other user.
    return "";
  }

  @Override
  protected void performBuild(final PdcSubscription subscription) {
    String lang = getUserLanguage(subscription.getOwnerId());
    LocalizationBundle resources = getBundle(lang);

    final StringBuilder message = new StringBuilder(150);

    if (valueDeleted) {
      message.append(resources.getString("deleteOnValueMessage"));
    } else {
      message.append(resources.getString("deleteOnAxisMessage"));
    }
    message.append("\n");

    message.append(resources.getString("Subscription"));
    message.append(subscription.getName());
    message.append("\n");

    message.append(resources.getString("Axis"));
    message.append(axisName);
    message.append("\n");

    getNotificationMetaData().setTitle(resources.getString(MESSAGE_DELETE_TITLE));
    getNotificationMetaData().setContent(message.toString());
    getNotificationMetaData().setSource(resources.getString(SOURCE_CLASSIFICATION));
  }

  @Override
  protected void performNotificationResource(final PdcSubscription resource,
      final NotificationResourceData notificationResourceData) {
    // Nothing is done here because of delayed notification that is not handled for this kind of
    // PDC user notification.
  }
}