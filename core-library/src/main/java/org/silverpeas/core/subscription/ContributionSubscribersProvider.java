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
package org.silverpeas.core.subscription;

import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;

/**
 * A provider of the subscribers that are concerned by a contribution in another way than by a
 * subscription to the contribution itself or to one of the resources that contain it (a folder,
 * the component instance, ...).
 * <p>
 * The subscribers to a resource in a component instance are provided by the
 * {@link ResourceSubscriptionService} of the component. But a user can also be interested in a
 * contribution because of a characteristic of it that is transverse to the component instances; for
 * example, the classification of the contribution on the PdC. Each service in Silverpeas that
 * manages such a kind of subscriptions has to implement this interface in order for its subscribers
 * to be notified about the contribution as any other subscriber. The implementation has to be a
 * bean managed by the underlying IoC container.
 * </p>
 * <p>
 * The access rights of the subscribers on the contribution aren't expected to be checked by the
 * implementations: this is under the responsibility of the code that notifies the subscribers.
 * </p>
 * @author mmoquillon
 * @see org.silverpeas.core.subscription.service.ResourceSubscriptionProvider#getSubscribersConcernedBy(ContributionIdentifier)
 */
public interface ContributionSubscribersProvider {

  /**
   * Gets all the subscribers that are concerned by the specified contribution according to the
   * subscriptions managed by this provider.
   * @param contribution the unique identifier of a contribution.
   * @return the subscribers concerned by the contribution. The list is empty if there is no such
   * subscribers or if the contribution isn't taken in charge by this provider.
   */
  SubscriptionSubscriberList getSubscribersOf(ContributionIdentifier contribution);
}
