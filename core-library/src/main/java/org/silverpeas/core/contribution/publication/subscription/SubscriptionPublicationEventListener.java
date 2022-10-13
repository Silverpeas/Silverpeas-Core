/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.contribution.publication.subscription;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.notification.PublicationEvent;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.subscription.AbstractProfiledResourceSubscriptionListener;
import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.subscription.SubscriptionResource;

import javax.inject.Singleton;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

/**
 * Listener of events on the deletion of a node in a component instance to delete all subscriptions
 * on that node.
 * @author silveryocha
 */
@Bean
@Singleton
public class SubscriptionPublicationEventListener
    extends AbstractProfiledResourceSubscriptionListener<PublicationDetail, PublicationEvent> {

  @Override
  public void onDeletion(final PublicationEvent event) throws Exception {
    final PublicationDetail publication = event.getTransition().getBefore();
    final List<SubscriptionResource> toDelete = getSubscriptionService()
        .getByResource(PublicationAliasSubscriptionResource.from(new PublicationPK(publication.getId())))
        .stream()
        .map(Subscription::getResource)
        .distinct()
        .collect(Collectors.toList());
    getSubscriptionService().unsubscribeByResources(toDelete);
    super.onDeletion(event);
  }

  @Override
  public void onUpdate(final PublicationEvent event) throws Exception {
    final PublicationDetail publication = event.getTransition().getBefore();
    OrganizationController.get().getComponentInstance(publication.getInstanceId())
        .filter(SilverpeasComponentInstance::isTopicTracker)
        .ifPresent(t -> {
          // moving subscription on main location
          final List<Subscription> subscriptionsToDelete = getSubscriptionService()
              .getByResource(PublicationSubscriptionResource.from(new PublicationPK(publication.getId())))
              .stream()
              .filter(not(s -> s.getResource().getInstanceId().equals(publication.getInstanceId())))
              .collect(Collectors.toList());
          final PublicationSubscriptionResource resource = PublicationSubscriptionResource.from(publication);
          final List<Subscription> subscriptionsToCreate = subscriptionsToDelete.stream()
              .map(s -> new PublicationSubscription(s.getSubscriber(), resource, s.getCreatorId()))
              .collect(Collectors.toList());
          getSubscriptionService().unsubscribeByResources(subscriptionsToDelete.stream()
              .map(Subscription::getResource)
              .distinct()
              .collect(Collectors.toList()));
          getSubscriptionService().subscribe(subscriptionsToCreate);
          // removing aliases subscription if any
          final Set<SubscriptionResource> potentials = PublicationService.get()
              .getAllAliases(publication.getPK())
              .stream()
              .map(Location::getComponentInstanceId)
              .distinct()
              .map(i -> PublicationAliasSubscriptionResource.from(new PublicationPK(publication.getId(), i)))
              .collect(Collectors.toSet());
          final List<SubscriptionResource> toDelete = getSubscriptionService()
              .getByResource(PublicationAliasSubscriptionResource.from(new PublicationPK(publication.getId())))
              .stream()
              .map(Subscription::getResource)
              .distinct()
              .filter(not(potentials::contains))
              .collect(Collectors.toList());
          getSubscriptionService().unsubscribeByResources(toDelete);
        });
    super.onUpdate(event);
  }

  @Override
  protected SubscriptionResource getSubscriptionResource(final PublicationDetail resource) {
    return PublicationSubscriptionResource.from(resource);
  }

  @Override
  protected boolean isSubscriptionEnabled(final PublicationDetail resource) {
    return true;
  }
}
  