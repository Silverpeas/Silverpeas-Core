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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.silverpeas.core.notification.user.builder.AbstractResourceUserNotificationBuilder;
import org.silverpeas.core.pdc.subscription.model.PdcSubscriptionPositionCriteria;

import java.util.Collection;
import java.util.List;

/**
 * The base of the notifications that are sent to the subscribers of position criteria on the PdC.
 * <p>
 * As position criteria can be subscribed by several users, either by themselves or by a manager
 * of the PdC that has forced the subscription for them or for their group, the notification is
 * built for all the recipients at once. The implementations have therefore to build their message
 * for each of the languages supported by the platform instead of for the language of a single
 * recipient.
 * </p>
 */
public abstract class AbstractPdcSubscriptionUserNotification<T>
    extends AbstractResourceUserNotificationBuilder<T> {

  private final PdcSubscriptionPositionCriteria pdcResource;
  private final Collection<String> recipientIds;

  protected AbstractPdcSubscriptionUserNotification(PdcSubscriptionPositionCriteria pdcResource,
      Collection<String> recipientIds, T resource) {
    super(resource);
    this.pdcResource = pdcResource;
    this.recipientIds = List.copyOf(recipientIds);
  }

  @Override
  protected @NonNull String getLocalizationBundlePath() {
    return "org.silverpeas.pdcSubscription.multilang.pdcsubscription";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return recipientIds;
  }

  /**
   * Gets the position criteria on the PdC the notification is about.
   * @return a {@link PdcSubscriptionPositionCriteria} instance.
   */
  public PdcSubscriptionPositionCriteria getPdcSubscriptionPositionCriteria() {
    return pdcResource;
  }
}
