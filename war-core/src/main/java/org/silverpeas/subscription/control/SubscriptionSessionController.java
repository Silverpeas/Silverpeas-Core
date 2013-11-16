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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.subscription.control;

import com.silverpeas.subscribe.Subscription;
import com.silverpeas.subscribe.SubscriptionService;
import com.silverpeas.subscribe.SubscriptionServiceFactory;
import com.silverpeas.subscribe.constant.SubscriberType;
import com.silverpeas.subscribe.constant.SubscriptionMethod;
import com.silverpeas.subscribe.service.ComponentSubscription;
import com.silverpeas.subscribe.service.GroupSubscriptionSubscriber;
import com.silverpeas.subscribe.service.NodeSubscription;
import com.silverpeas.subscribe.service.UserSubscriptionSubscriber;
import com.silverpeas.subscribe.util.SubscriptionUtil;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.node.model.NodePK;
import org.silverpeas.subscription.SubscriptionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * User: Yohann Chastagnier
 * Date: 04/03/13
 */
public class SubscriptionSessionController extends AbstractComponentSessionController {

  private SubscriptionService subscriptionService;

  /**
   * Default constructor.
   * @param controller
   * @param context
   */
  public SubscriptionSessionController(final MainSessionController controller,
      final ComponentContext context) {
    super(controller, context, "org.silverpeas.subscription.multilang.subscriptionBundle");
  }

  /**
   * Gets the context.
   * @return
   */
  public SubscriptionContext getContext() {
    return getSubscriptionContext();
  }

  /*
   * Initialize UserPanel with the list of Silverpeas subscribers
   */
  public String toUserPanel() {
    String context = GeneralPropertiesManager.getString("ApplicationURL");
    String validateUrl = context + "/RSubscription/jsp/FromUserPanel";
    String cancelUrl = context + "/RSubscription/jsp/Main";
    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostPath(null);

    sel.setGoBackURL(validateUrl);
    sel.setCancelURL(cancelUrl);

    // Contraintes
    sel.setMultiSelect(true);
    sel.setPopupMode(false);

    // Subscribers
    Map<SubscriberType, Collection<String>> subscriberIdsByTypes = SubscriptionUtil
        .indexSubscriberIdsByType(getSubscriptionService()
            .getSubscribers(getContext().getResource(), SubscriptionMethod.FORCED));
    // Users
    sel.setSelectedElements(subscriberIdsByTypes.get(SubscriberType.USER));
    // Groups
    sel.setSelectedSets(subscriberIdsByTypes.get(SubscriberType.GROUP));

    if (sel.getSelectedElements().length == 0 && sel.getSelectedSets().length == 0) {
      sel.setFirstPage(Selection.FIRST_PAGE_BROWSE);
    } else {
      sel.setFirstPage(Selection.FIRST_PAGE_CART);
    }

    // Returning the destination
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  /*
   * Retour du UserPanel
   */
  public void fromUserPanel() {

    // Getting selection informations
    Selection sel = getSelection();
    UserDetail[] users = SelectionUsersGroups.getUserDetails(sel.getSelectedElements());
    Group[] groups = SelectionUsersGroups.getGroups(sel.getSelectedSets());

    // Initializing necessary subscriptions
    Collection<Subscription> subscriptions =
        new ArrayList<Subscription>(users.length + groups.length);
    for (UserDetail user : users) {
      switch (getContext().getResource().getType()) {
        case NODE:
          subscriptions.add(new NodeSubscription(UserSubscriptionSubscriber.from(user.getId()),
              (NodePK) getContext().getResource().getPK(), getUserId()));
          break;
        case COMPONENT:
          subscriptions.add(new ComponentSubscription(UserSubscriptionSubscriber.from(user.getId()),
              getContext().getResource().getInstanceId(), getUserId()));
          break;
      }
    }
    for (Group group : groups) {
      switch (getContext().getResource().getType()) {
        case NODE:
          subscriptions.add(new NodeSubscription(GroupSubscriptionSubscriber.from(group.getId()),
              (NodePK) getContext().getResource().getPK(), getUserId()));
          break;
        case COMPONENT:
          subscriptions.add(
              new ComponentSubscription(GroupSubscriptionSubscriber.from(group.getId()),
                  getContext().getResource().getInstanceId(), getUserId()));
          break;
      }
    }

    // Getting all existing subscriptions and selecting those that have to be deleted
    Collection<Subscription> subscriptionsToDelete = getSubscriptionService()
        .getByResource(getContext().getResource(), SubscriptionMethod.FORCED);
    subscriptionsToDelete.removeAll(subscriptions);

    // Deleting
    getSubscriptionService().unsubscribe(subscriptionsToDelete);

    // Creating subscriptions (nothing is registered for subscriptions that already exist)
    getSubscriptionService().subscribe(subscriptions);
  }

  /**
   * Gets the subscription service.
   * @return
   */
  private SubscriptionService getSubscriptionService() {
    if (subscriptionService == null) {
      subscriptionService = SubscriptionServiceFactory.getFactory().getSubscribeService();
    }
    return subscriptionService;
  }
}
