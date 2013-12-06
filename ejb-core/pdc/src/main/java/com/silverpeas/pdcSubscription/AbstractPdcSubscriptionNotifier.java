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

import java.util.Arrays;
import java.util.Collection;

import com.silverpeas.SilverpeasServiceProvider;
import com.silverpeas.notification.builder.AbstractUserNotificationBuilder;
import com.silverpeas.pdcSubscription.model.PDCSubscription;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;

public abstract class AbstractPdcSubscriptionNotifier extends AbstractUserNotificationBuilder {
  
  PDCSubscription subscription = null;
  
  public AbstractPdcSubscriptionNotifier(PDCSubscription subscription) {
    this.subscription = subscription;
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.REPORT;
  }
  
  protected String getMultilangPropertyFile() {
    return "org.silverpeas.pdcSubscription.multilang.pdcsubscription";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    String[] userIdsToNotify = {Integer.toString(subscription.getOwnerId())};
    return Arrays.asList(userIdsToNotify);
  }
  
  /**
   * @param userID
   * @return user preferred language by userid provided
   */
  protected String getUserLanguage(int userID) {
    return SilverpeasServiceProvider.getPersonalizationService().getUserSettings(
        String.valueOf(userID)).getLanguage();
  }

  @Override
  protected abstract String getComponentInstanceId();

  @Override
  protected abstract String getSender();

  @Override
  protected abstract void performBuild();

}
