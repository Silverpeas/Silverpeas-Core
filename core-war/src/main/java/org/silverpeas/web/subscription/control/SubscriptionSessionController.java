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
package org.silverpeas.web.subscription.control;

import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.subscription.SubscriptionResource;
import org.silverpeas.core.subscription.SubscriptionService;
import org.silverpeas.core.subscription.SubscriptionServiceProvider;
import org.silverpeas.core.subscription.constant.SubscriberType;
import org.silverpeas.core.subscription.constant.SubscriptionMethod;
import org.silverpeas.core.subscription.constant.SubscriptionResourceType;
import org.silverpeas.core.subscription.service.ComponentSubscription;
import org.silverpeas.core.subscription.service.GroupSubscriptionSubscriber;
import org.silverpeas.core.subscription.service.NodeSubscription;
import org.silverpeas.core.subscription.service.UserSubscriptionSubscriber;
import org.silverpeas.core.subscription.util.SubscriptionList;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberMapBySubscriberType;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.selection.SelectionUsersGroups;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.ObjectType;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.web.subscription.SubscriptionContext;
import org.silverpeas.core.util.ResourceLocator;

import java.util.ArrayList;
import java.util.Collection;

import static org.silverpeas.core.subscription.util.SubscriptionUtil.isSameVisibilityAsTheCurrentRequester;

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
    String context = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
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
    SubscriptionResource resource = getContext().getResource();
    SubscriptionSubscriberMapBySubscriberType subscriberIdsByTypes = getSubscriptionService()
        .getSubscribers(resource, SubscriptionMethod.FORCED)
        .indexBySubscriberType().filterOnDomainVisibilityFrom(getUserDetail());

    // Users
    sel.setSelectedElements(subscriberIdsByTypes.get(SubscriberType.USER).getAllIds());
    // Groups
    sel.setSelectedSets(subscriberIdsByTypes.get(SubscriberType.GROUP).getAllIds());

    if (sel.getSelectedElements().length == 0 && sel.getSelectedSets().length == 0) {
      sel.setFirstPage(Selection.FIRST_PAGE_BROWSE);
    } else {
      sel.setFirstPage(Selection.FIRST_PAGE_CART);
    }

    // Add extra params
    SelectionUsersGroups sug = new SelectionUsersGroups();
    sug.setComponentId(resource.getInstanceId());
    if (resource.getType() == SubscriptionResourceType.NODE &&
        getComponentAccessController().isRightOnTopicsEnabled(sug.getComponentId())) {
      NodeDetail node =
          NodeService.get().getHeader(new NodePK(resource.getId(), resource.getInstanceId()));
      if (node.haveRights()) {
        sug.setObjectId(ObjectType.NODE.getCode() + node.getRightsDependsOn());
      }
    }
    sel.setExtraParams(sug);

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
    SubscriptionResource resource = getContext().getResource();
    Collection<Subscription> subscriptions =
        new ArrayList<Subscription>(users.length + groups.length);
    for (UserDetail user : users) {
      if (!isSameVisibilityAsTheCurrentRequester(user, getUserDetail())) {
        continue;
      }
      switch (resource.getType()) {
        case NODE:
          subscriptions.add(new NodeSubscription(UserSubscriptionSubscriber.from(user.getId()),
              (NodePK) resource.getPK(), getUserId()));
          break;
        case COMPONENT:
          subscriptions.add(new ComponentSubscription(UserSubscriptionSubscriber.from(user.getId()),
              resource.getInstanceId(), getUserId()));
          break;
      }
    }
    for (Group group : groups) {
      if (!isSameVisibilityAsTheCurrentRequester(group, getUserDetail())) {
        continue;
      }
      switch (resource.getType()) {
        case NODE:
          subscriptions.add(new NodeSubscription(GroupSubscriptionSubscriber.from(group.getId()),
              (NodePK) resource.getPK(), getUserId()));
          break;
        case COMPONENT:
          subscriptions.add(
              new ComponentSubscription(GroupSubscriptionSubscriber.from(group.getId()),
                  resource.getInstanceId(), getUserId()));
          break;
      }
    }

    // Getting all existing subscriptions and selecting those that have to be deleted
    SubscriptionList subscriptionsToDelete = getSubscriptionService()
        .getByResource(resource, SubscriptionMethod.FORCED);
    subscriptionsToDelete.removeAll(subscriptions);
    subscriptionsToDelete.filterOnDomainVisibilityFrom(getUserDetail());

    // Deleting
    getSubscriptionService().unsubscribe(subscriptionsToDelete);

    // Creating subscriptions (nothing is registered for subscriptions that already exist)
    getSubscriptionService().subscribe(subscriptions);
  }

  /**
   * Gets the subscription service.
   */
  private SubscriptionService getSubscriptionService() {
    if (subscriptionService == null) {
      subscriptionService = SubscriptionServiceProvider.getSubscribeService();
    }
    return subscriptionService;
  }
}
