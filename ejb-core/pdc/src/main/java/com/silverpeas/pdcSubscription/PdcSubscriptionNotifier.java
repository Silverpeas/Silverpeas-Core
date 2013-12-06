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
package com.silverpeas.pdcSubscription;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.silverpeas.pdcSubscription.model.PDCSubscription;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.webactiv.util.ResourceLocator;

public class PdcSubscriptionNotifier extends AbstractPdcSubscriptionNotifier {
  
  SilverContentInterface silverContent = null;
  
  public PdcSubscriptionNotifier(PDCSubscription subscription, SilverContentInterface content) {
    super(subscription);
    this.silverContent = content;
  }

  @Override
  protected String getComponentInstanceId() {
    return silverContent.getInstanceId();
  }

  @Override
  protected String getSender() {
    return silverContent.getCreatorId();
  }

  @Override
  protected void performBuild() {
    String lang = getUserLanguage(subscription.getOwnerId());
    ResourceLocator resources = getBundle(lang);
    
    final StringBuilder message = new StringBuilder(150);

    message.append(resources.getString("Subscription"));
    message.append(subscription.getName());
    message.append("\n");

    message.append(resources.getString("DocumentName"));
    message.append(silverContent.getName(lang));
    message.append("\n");
    
    String documentURL = "";
    String contentUrl = silverContent.getURL();
    if (contentUrl != null) {
      StringBuilder documentUrlBuffer = new StringBuilder().append(
          "/RpdcSearch/jsp/GlobalContentForward?contentURL=");
      try {
        documentUrlBuffer.append(URLEncoder.encode(contentUrl, "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        documentUrlBuffer.append(contentUrl);
      }
      documentUrlBuffer.append("&componentId=").append(getComponentInstanceId());
      documentURL = documentUrlBuffer.toString();
    }
    
    
    getNotificationMetaData().setTitle(resources.getString("standartMessage"));
    getNotificationMetaData().setContent(message.toString());
    getNotificationMetaData().setLink(documentURL);
  }

}