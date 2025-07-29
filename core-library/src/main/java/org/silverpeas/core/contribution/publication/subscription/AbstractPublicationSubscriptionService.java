/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.publication.subscription;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.subscription.SubscriberDirective;
import org.silverpeas.core.subscription.SubscriptionFactory;
import org.silverpeas.core.subscription.SubscriptionResourceType;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.service.AbstractResourceSubscriptionService;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;
import org.silverpeas.kernel.util.Mutable;
import org.silverpeas.kernel.util.Pair;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.silverpeas.core.contribution.publication.subscription.OnLocationDirective.onLocationId;
import static org.silverpeas.core.contribution.publication.subscription.PublicationSubscriptionConstants.PUBLICATION;
import static org.silverpeas.core.contribution.publication.subscription.PublicationSubscriptionConstants.PUBLICATION_ALIAS;
import static org.silverpeas.core.subscription.SubscriptionServiceProvider.getSubscribeService;
import static org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants.COMPONENT;
import static org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants.NODE;
import static org.silverpeas.kernel.util.Mutable.of;

/**
 * As the class is implementing {@link org.silverpeas.core.initialization.Initialization}, no
 * annotation appears in order to be taken into account by CDI.<br>
 * The service will be taken in charge by initialization treatments.
 * @author silveryocha
 */
public abstract class AbstractPublicationSubscriptionService extends AbstractResourceSubscriptionService {

  @Override
  public void init() {
    super.init();
    SubscriptionFactory.get().register(PUBLICATION,
        (r, s, i) -> new PublicationSubscriptionResource(new PublicationPK(r, i)),
        (s, r, c) -> new PublicationSubscription(s, (PublicationSubscriptionResource) r, c));
    SubscriptionFactory.get().register(PUBLICATION_ALIAS,
        (r, s, i) -> new PublicationAliasSubscriptionResource(new PublicationPK(r, i)),
        (s, r, c) -> new PublicationAliasSubscription(s, (PublicationAliasSubscriptionResource) r, c));
  }

  @Override
  public SubscriptionSubscriberList getSubscribersOfComponentAndTypedResource(
      final String componentInstanceId, final SubscriptionResourceType resourceType,
      final String resourceId, final SubscriberDirective... directives) {
    final Collection<SubscriptionSubscriber> subscribers = new HashSet<>();
    final Mutable<Pair<SubscriptionResourceType, String>> reference = of(Pair.of(resourceType, resourceId));
    if (reference.get().getFirst() == PUBLICATION_ALIAS) {
      // In that case, subscribers of publication alias must be verified.
      final PublicationPK publicationPK = new PublicationPK(resourceId, componentInstanceId);
      subscribers.addAll(getSubscribeService().getSubscribers(
          PublicationAliasSubscriptionResource.from(publicationPK)));
      // If a location is given, then verifying also the node corresponding to it
      stream(directives)
          .filter(OnLocationDirective.class::isInstance)
          .map(OnLocationDirective.class::cast)
          .findFirst()
          .ifPresent(d -> reference.set(Pair.of(NODE, d.getLocationId())));
    }
    if (reference.get().getFirst() == PUBLICATION) {
      // In that case, subscribers of publication must be verified.
      final PublicationPK publicationPK = new PublicationPK(resourceId, componentInstanceId);
      subscribers.addAll(getSubscribeService().getSubscribers(
          PublicationSubscriptionResource.from(publicationPK)));
      // Next parent type / reference to verify
      final List<Predicate<Location>> locationFilters = stream(directives)
          .filter(LocationFilterDirective.class::isInstance)
          .map(LocationFilterDirective.class::cast)
          .map(LocationFilterDirective::getFilter)
          .collect(toList());
      OrganizationController.get()
          .getComponentInstance(componentInstanceId)
          .filter(SilverpeasComponentInstance::isTopicTracker)
          .map(i -> PublicationService.get().getDetail(new PublicationPK(resourceId)))
          .ifPresentOrElse(p ->
                  // In that case, subscribers of all publication locations MUST be verified.
                  PublicationService.get()
                      .getAllLocations(p.getPK())
                      .stream()
                      .filter(locationFilters.stream().reduce(Predicate::and).orElse(x -> true))
                      .forEach(l -> {
                        if (l.isAlias()) {
                          subscribers.addAll(
                              getSubscribersOfComponentAndTypedResource(l.getComponentInstanceId(),
                                  PUBLICATION_ALIAS, resourceId, onLocationId(l.getLocalId())));
                        } else {
                          subscribers.addAll(getSubscribersOfComponentAndTypedResource(
                              l.getComponentInstanceId(), NODE, l.getLocalId()));
                        }
                      }),
              () ->
                  // In that case, subscribers of COMPONENT must be verified.
                  reference.set(Pair.of(COMPONENT, componentInstanceId)));
    }
    if (reference.get().getFirst() == NODE) {
      subscribers.addAll(super.getSubscribersOfComponentAndTypedResource(componentInstanceId, NODE,
          reference.get().getSecond()));
    }
    if (reference.get().getFirst() == COMPONENT) {
      subscribers.addAll(super.getSubscribersOfComponent(reference.get().getSecond()));
    }
    return new SubscriptionSubscriberList(subscribers);
  }
}
