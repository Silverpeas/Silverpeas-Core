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
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentInterface;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.util.LocalizationBundle;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class PdcResourceClassificationUserNotification
    extends AbstractPdcSubscriptionUserNotification<SilverContentInterface> {

  public PdcResourceClassificationUserNotification(PdcSubscription pdcSubscription,
      SilverContentInterface silverContent) {
    super(pdcSubscription, silverContent);
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.CLASSIFIED;
  }

  @Override
  protected boolean isSendImmediatly() {
    /**
     * TODO for now, pdc notifications can not be handled by delayed notification mechanism. When
     * it will be the case, don't forget to remove this overridden method
     */
    return true;
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getInstanceId();
  }

  @Override
  protected String getSender() {
    return getResource().getCreatorId();
  }

  @Override
  protected void performBuild(final SilverContentInterface silverContent) {
    String lang = getUserLanguage(getPdcSubscription().getOwnerId());
    LocalizationBundle resources = getBundle(lang);

    final StringBuilder message = new StringBuilder(150);

    message.append(resources.getString("Subscription"));
    message.append(getPdcSubscription().getName());
    message.append("\n");

    message.append(resources.getString("DocumentName"));
    message.append(silverContent.getName(lang));
    message.append("\n");

    getNotificationMetaData().setTitle(resources.getString("standartMessage"));
    getNotificationMetaData().setContent(message.toString());
  }

  @Override
  protected void performNotificationResource(final SilverContentInterface silverContent,
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
  protected String getResourceURL(final SilverContentInterface silverContent) {
    String contentUrl = silverContent.getURL();
    if (contentUrl != null) {
      StringBuilder documentUrlBuffer =
          new StringBuilder().append("/RpdcSearch/jsp/GlobalContentForward?contentURL=");
      try {
        documentUrlBuffer.append(URLEncoder.encode(contentUrl, "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        documentUrlBuffer.append(contentUrl);
      }
      documentUrlBuffer.append("&componentId=").append(getComponentInstanceId());
      return documentUrlBuffer.toString();
    }

    // In other cases, no resource URL can be build.
    return null;
  }
}