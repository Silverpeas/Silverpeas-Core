/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.subscription;

import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.notification.ProfileInstEvent;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.notification.system.CDIResourceEventListener;
import org.silverpeas.core.subscription.service.GroupSubscriptionSubscriber;
import org.silverpeas.core.subscription.service.NodeSubscription;
import org.silverpeas.core.subscription.service.UserSubscriptionSubscriber;
import org.silverpeas.core.subscription.util.SubscriptionList;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Listener of a change in a right profile in order to update the subscriptions according to it.
 * For instance, if a right profile of a given resource (a component instance for example) is
 * updated  with a user or a group removed from that profile, then if this user or some users in
 * this group have a subscription to the resource, then it is removed.
 * @author mmoquillon
 */
public class SubscriptionProfileInstEventListener
    extends CDIResourceEventListener<ProfileInstEvent> {

  @Inject
  private SubscriptionService subscriptionService;

  @Inject
  private OrganizationController organizationController;

  @Override
  public void onUpdate(final ProfileInstEvent event) {
    final ProfileInst previous = event.getTransition().getBefore();
    final ProfileInst actual = event.getTransition().getAfter();
    final String instanceId = previous.getComponentFatherId();
    final NodePK nodePK = new NodePK(previous.getObjectId().getId(), instanceId);

    final List<String> previousUsers = previous.getAllUsers();
    final List<String> previousGroups = previous.getAllGroups();
    previousUsers.addAll(usersInGroups(previousGroups));
    final List<String> actualUsers = actual.getAllUsers();
    final List<String> actualGroups = actual.getAllGroups();
    actualUsers.addAll(usersInGroups(actualGroups));

    actualGroups.addAll(childrenOfGroups(actualGroups));
    previousGroups.addAll(childrenOfGroups(previousGroups));

    final Stream<String> streamOnUsers =
        previousUsers.stream().filter(u -> !actualUsers.contains(u));
    final Stream<String> streamOnGroups =
        previousGroups.stream().filter(g -> !actualGroups.contains(g));
    if (previous.isOnComponentInstance()) {
      removeComponentSubscriptions(streamOnUsers, streamOnGroups, instanceId);
    } else {
      removeNodeSubscriptions(streamOnUsers, streamOnGroups, nodePK);
    }
  }

  @Override
  public void onDeletion(final ProfileInstEvent event) throws Exception {
    final ProfileInst deleted = event.getTransition().getBefore();
    final String instanceId = deleted.getComponentFatherId();
    final NodePK nodePK = new NodePK(deleted.getObjectId().getId(), instanceId);

    final List<String> users = deleted.getAllUsers();
    final List<String> groups = deleted.getAllGroups();
    users.addAll(usersInGroups(groups));
    groups.addAll(childrenOfGroups(groups));
    if (deleted.isOnComponentInstance()) {
      removeComponentSubscriptions(users.stream(), groups.stream(), instanceId);
    } else {
      removeNodeSubscriptions(users.stream(), groups.stream(), nodePK);
    }
  }

  private List<String> usersInGroups(final List<String> group) {
    return group.stream()
        .map(g -> organizationController.getAllUsersOfGroup(g))
        .flatMap(Stream::of)
        .map(User::getId)
        .distinct()
        .collect(Collectors.toList());
  }

  private List<String> childrenOfGroups(final List<String> parent) {
    return parent.stream()
        .map(g -> organizationController.getRecursivelyAllSubgroups(g))
        .flatMap(Stream::of)
        .map(Group::getId)
        .distinct()
        .collect(Collectors.toList());
  }

  private void removeComponentSubscriptions(final Stream<String> streamOnUsers,
      final Stream<String> streamOnGroups,
      final String instanceId) {
    streamOnUsers.forEach(u -> {
      final SubscriptionList subscriptions =
          subscriptionService.getBySubscriberAndComponent(UserSubscriptionSubscriber.from(u),
              instanceId);
      subscriptionService.unsubscribe(subscriptions);
    });
    streamOnGroups.forEach(g -> {
      final SubscriptionList subscriptions =
          subscriptionService.getBySubscriberAndComponent(GroupSubscriptionSubscriber.from(g),
              instanceId);
      subscriptionService.unsubscribe(subscriptions);
    });
  }

  private void removeNodeSubscriptions(final Stream<String> streamOnUsers,
      final Stream<String> streamOnGroups, final NodePK nodePK) {
    streamOnUsers.forEach(u -> {
      NodeSubscription subscription =
          new NodeSubscription(UserSubscriptionSubscriber.from(u), nodePK);
      subscriptionService.unsubscribe(subscription);
    });
    streamOnGroups.forEach(g -> {
      NodeSubscription subscription =
          new NodeSubscription(GroupSubscriptionSubscriber.from(g), nodePK);
      subscriptionService.unsubscribe(subscription);
    });
  }
}
  