/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.subscription.util;

import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.subscription.constant.SubscriberType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.silverpeas.core.subscription.util.SubscriptionUtil.isSameVisibilityAsTheCurrentRequester;

/**
 * @author Yohann Chastagnier
 */
public class SubscriptionSubscriberList extends ArrayList<SubscriptionSubscriber> {
  private static final long serialVersionUID = -4159319020430997899L;

  public SubscriptionSubscriberList() {
    super();
  }

  public SubscriptionSubscriberList(final Collection<? extends SubscriptionSubscriber> c) {
    super(c);
  }

  /**
   * Retrieves from the list content all identifiers of {@link
   * SubscriptionSubscriber}.
   * No filter is applied according to the {@link SubscriberType}.
   * @return a list of identifiers of any kind of
   * {@link SubscriberType}.
   */
  public List<String> getAllIds() {
    Set<String> allIds = new HashSet<>();
    for (SubscriptionSubscriber subscriber : this) {
      allIds.add(subscriber.getId());
    }
    return new ArrayList<>(allIds);
  }

  /**
   * Retrieves from the list content all unique identifiers of user identifiers (so the users of
   * groups are taken into account).
   * @return the complete list of user identifiers (those of groups too).
   */
  public List<String> getAllUserIds() {

    // Retrieving
    Set<String> allUserSubscriberIds = new HashSet<>();
    Set<String> groupIds = new HashSet<>();
    for (SubscriptionSubscriber subscriber : this) {
      switch (subscriber.getType()) {
        case USER:
          allUserSubscriberIds.add(subscriber.getId());
          break;
        case GROUP:
          groupIds.add(subscriber.getId());
          break;
      }
    }

    // Retrieving users from groups if any
    if (!groupIds.isEmpty()) {
      final OrganizationController organisationController =
          OrganizationControllerProvider.getOrganisationController();
      for (String groupId : groupIds) {
        for (UserDetail user : organisationController.getAllUsersOfGroup(groupId)) {
          allUserSubscriberIds.add(user.getId());
        }
      }
    }

    // Result
    return new ArrayList<>(allUserSubscriberIds);
  }

  /**
   * Obtains subscription subscribers indexed by their type.
   * @return an instance of {@link SubscriptionSubscriberMapBySubscriberType}.
   */
  public SubscriptionSubscriberMapBySubscriberType indexBySubscriberType() {
    return new SubscriptionSubscriberMapBySubscriberType(this);
  }

  /**
   * Removes from this list the subscribers that have not the same domain visibility as the one
   * of the given user.
   * @param user the user that represents the visibility to verify.
   * @return itself.
   */
  public SubscriptionSubscriberList filterOnDomainVisibilityFrom(final UserDetail user) {
    if (user.isDomainRestricted()) {
      Iterator<SubscriptionSubscriber> itOfSubscribers = this.iterator();
      while (itOfSubscribers.hasNext()) {
        SubscriptionSubscriber subscriber = itOfSubscribers.next();
        if (!SubscriptionUtil.isSameVisibilityAsTheCurrentRequester(subscriber, user)) {
          itOfSubscribers.remove();
        }
      }
    }
    return this;
  }
}
