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

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagementEngine;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.ClassifyValue;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.subscription.ContributionSubscribersProvider;
import org.silverpeas.core.subscription.SubscriptionService;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;
import org.silverpeas.kernel.logging.SilverLogger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The provider of the subscribers that are concerned by a contribution because of its
 * classification on the PdC: the users and the groups of users that are subscribed to position
 * criteria on the PdC satisfied by the classification of the contribution.
 * <p>
 * A contribution that isn't classified on the PdC, or that is handled by an application that
 * doesn't take in charge the classification on the PdC, has no such subscribers.
 * </p>
 * @author mmoquillon
 */
@Provider
public class PdcContributionSubscribersProvider implements ContributionSubscribersProvider {

  @Inject
  private ContentManagementEngine contentManagementEngine;
  // lazily got: the PdC manager is required only for the classified contributions
  @Inject
  private Instance<PdcManager> pdcManagerProvider;
  @Inject
  private PdcSubscriptionService pdcSubscriptionService;
  @Inject
  private SubscriptionService subscriptionService;

  @Override
  public SubscriptionSubscriberList getSubscribersOf(final ContributionIdentifier contribution) {
    final Set<SubscriptionSubscriber> subscribers = new HashSet<>();
    try {
      final List<List<ClassifyValue>> classification = getClassificationOf(contribution);
      if (!classification.isEmpty()) {
        pdcSubscriptionService.getPositionCriteriaMatching(classification)
            .forEach(c -> subscribers.addAll(subscriptionService.getSubscribers(c)));
      }
    } catch (ContentManagerException | PdcException e) {
      // a failure with the PdC mustn't prevent the other subscribers from being notified
      SilverLogger.getLogger(this)
          .error("Cannot get the subscribers on the PdC concerned by the contribution {0}",
              new String[]{contribution.asString()}, e);
    }
    return new SubscriptionSubscriberList(subscribers);
  }

  /**
   * Gets the classification on the PdC of the specified contribution. No content instance is
   * registered for the application of the contribution if this one isn't yet known of the content
   * management engine: the contribution is then just considered as not classified.
   * @param contribution the unique identifier of a contribution.
   * @return the positions of the contribution on the PdC, each of them expressed by its values on
   * the axis of the PdC. The list is empty if the contribution isn't classified.
   */
  private List<List<ClassifyValue>> getClassificationOf(final ContributionIdentifier contribution)
      throws ContentManagerException, PdcException {
    final String instanceId = contribution.getComponentInstanceId();
    final Set<Integer> silverContentIds = contentManagementEngine.getSilverContentId(
        List.of(contribution.getLocalId(), instanceId));
    if (silverContentIds.isEmpty()) {
      return List.of();
    }
    final List<ClassifyPosition> positions = pdcManagerProvider.get()
        .getPositions(silverContentIds.iterator().next(), instanceId);
    return positions.stream().map(ClassifyPosition::getValues).toList();
  }
}
