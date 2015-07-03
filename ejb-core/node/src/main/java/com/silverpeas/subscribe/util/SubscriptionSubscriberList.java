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
package com.silverpeas.subscribe.util;

import com.silverpeas.subscribe.SubscriptionSubscriber;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.core.admin.OrganisationControllerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
   * com.silverpeas.subscribe.SubscriptionSubscriber}.
   * No filter is applied according to the {@link com.silverpeas.subscribe.constant.SubscriberType}.
   * @return a list of identifiers of any kind of
   * {@link com.silverpeas.subscribe.constant.SubscriberType}.
   */
  public List<String> getAllIds() {
    Set<String> allIds = new HashSet<String>();
    for (SubscriptionSubscriber subscriber : this) {
      allIds.add(subscriber.getId());
    }
    return new ArrayList<String>(allIds);
  }

  /**
   * Retrieves from the list content all unique identifiers of user identifiers (so the users of
   * groups are taken into account).
   * @return the complete list of user identifiers (those of groups too).
   */
  public List<String> getAllUserIds() {

    // Retrieving
    Set<String> allUserSubscriberIds = new HashSet<String>();
    Set<String> groupIds = new HashSet<String>();
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
      final OrganisationController organisationController =
          OrganisationControllerFactory.getOrganisationController();
      for (String groupId : groupIds) {
        for (UserDetail user : organisationController.getAllUsersOfGroup(groupId)) {
          allUserSubscriberIds.add(user.getId());
        }
      }
    }

    // Result
    return new ArrayList<String>(allUserSubscriberIds);
  }

  /**
   * Obtains subscription subscribers indexed by their type.
   * @return an instance of {@link SubscriptionSubscriberMapBySubscriberType}.
   */
  public SubscriptionSubscriberMapBySubscriberType indexBySubscriberType() {
    return new SubscriptionSubscriberMapBySubscriberType(this);
  }
}
