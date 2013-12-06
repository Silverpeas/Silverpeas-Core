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

import com.silverpeas.pdcSubscription.model.PDCSubscription;
import com.stratelia.webactiv.util.ResourceLocator;

public class PdcSubscriptionDeletionNotifier extends AbstractPdcSubscriptionNotifier {
  
  private final static String MESSAGE_DELETE_TITLE = "notification.delete.title";
  private final static String SOURCE_CLASSIFICATION = "pdcClassification";
  
  boolean valueDeleted = false;
  String axisName = null;
  
  public PdcSubscriptionDeletionNotifier(PDCSubscription subscription, String axisName, boolean valueDeleted) {
    super(subscription);
    this.valueDeleted = valueDeleted;
    this.axisName = axisName;
  }
  
  @Override
  protected boolean isSendImmediatly() {
    return true;
  }

  @Override
  protected String getComponentInstanceId() {
    return null;
  }

  @Override
  protected String getSender() {
    return "";
  }

  @Override
  protected void performBuild() {
    String lang = getUserLanguage(subscription.getOwnerId());
    ResourceLocator resources = getBundle(lang);
    
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

}