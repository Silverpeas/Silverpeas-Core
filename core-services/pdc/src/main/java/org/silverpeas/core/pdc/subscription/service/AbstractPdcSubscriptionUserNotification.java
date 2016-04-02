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

import org.silverpeas.core.personalization.service.PersonalizationServiceProvider;
import org.silverpeas.core.notification.user.builder.AbstractResourceUserNotificationBuilder;
import org.silverpeas.core.pdc.subscription.model.PdcSubscription;
import org.silverpeas.core.util.StringUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractPdcSubscriptionUserNotification<T>
    extends AbstractResourceUserNotificationBuilder<T> {

  private final PdcSubscription pdcSubscription;
  private final Map<Integer, String> userLanguages = new HashMap<Integer, String>();

  public AbstractPdcSubscriptionUserNotification(PdcSubscription pdcSubscription, T resource) {
    super(resource);
    this.pdcSubscription = pdcSubscription;
  }

  @Override
  protected String getMultilangPropertyFile() {
    return "org.silverpeas.pdcSubscription.multilang.pdcsubscription";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return Collections.singletonList(String.valueOf(pdcSubscription.getOwnerId()));
  }

  /**
   * @param userID
   * @return user preferred language by userid provided
   */
  protected String getUserLanguage(int userID) {
    String userLanguage = userLanguages.get(userID);
    if (StringUtil.isNotDefined(userLanguage)) {
      userLanguage = PersonalizationServiceProvider.getPersonalizationService()
          .getUserSettings(String.valueOf(userID)).getLanguage();
      userLanguages.put(userID, userLanguage);
    }
    return userLanguage;
  }

  public PdcSubscription getPdcSubscription() {
    return pdcSubscription;
  }
}
