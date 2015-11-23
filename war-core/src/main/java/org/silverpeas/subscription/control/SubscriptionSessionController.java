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

import com.silverpeas.accesscontrol.AccessController;
import com.silverpeas.accesscontrol.AccessControllerProvider;
import com.silverpeas.accesscontrol.ComponentAccessController;
import com.silverpeas.subscribe.Subscription;
import com.silverpeas.subscribe.SubscriptionResource;
import com.silverpeas.subscribe.SubscriptionService;
import com.silverpeas.subscribe.SubscriptionServiceFactory;
import com.silverpeas.subscribe.constant.SubscriberType;
import com.silverpeas.subscribe.constant.SubscriptionMethod;
import com.silverpeas.subscribe.constant.SubscriptionResourceType;
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
import com.stratelia.webactiv.beans.admin.ObjectType;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.node.model.NodeRuntimeException;
import org.silverpeas.subscription.SubscriptionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.silverpeas.subscribe.util.SubscriptionUtil.filterSubscriptionsOnDomainVisibility;
import static com.silverpeas.subscribe.util.SubscriptionUtil.isSameVisibilityAsTheCurrentRequester;

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
    SubscriptionResource resource = getContext().getResource();
    Map<SubscriberType, Collection<String>> subscriberIdsByTypes = SubscriptionUtil
        .indexSubscriberIdsByType(
            getSubscriptionService().getSubscribers(resource, SubscriptionMethod.FORCED));
    // Users
    List<String> userIds = new ArrayList<String>(subscriberIdsByTypes.get(SubscriberType.USER));
    if (getUserDetail().isDomainRestricted()) {
      Iterator<String> itOfUserIds = userIds.iterator();
      while (itOfUserIds.hasNext()) {
        if (!isSameVisibilityAsTheCurrentRequester(UserDetail.getById(itOfUserIds.next()),
            getUserDetail())) {
          itOfUserIds.remove();
        }
      }
    }
    sel.setSelectedElements(userIds);
    // Groups
    List<String> groupIds = new ArrayList<String>(subscriberIdsByTypes.get(SubscriberType.GROUP));
    if (getUserDetail().isDomainRestricted()) {
      Iterator<String> itOfGroupIds = groupIds.iterator();
      while (itOfGroupIds.hasNext()) {
        if (!isSameVisibilityAsTheCurrentRequester(Group.getById(itOfGroupIds.next()),
            getUserDetail())) {
          itOfGroupIds.remove();
        }
      }
    }
    sel.setSelectedSets(groupIds);

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
          getNodeService().getHeader(new NodePK(resource.getId(), resource.getInstanceId()));
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
    Collection<Subscription> subscriptionsToDelete = getSubscriptionService()
        .getByResource(resource, SubscriptionMethod.FORCED);
    subscriptionsToDelete.removeAll(subscriptions);
    filterSubscriptionsOnDomainVisibility(subscriptionsToDelete, getUserDetail());

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
      subscriptionService = SubscriptionServiceFactory.getFactory().getSubscribeService();
    }
    return subscriptionService;
  }

  /**
   * Gets the node service.
   */
  private NodeBm getNodeService() {
    try {
      return EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBm.class);
    } catch (Exception e) {
      throw new NodeRuntimeException("SubscriptionSessionController.getNodeService()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /**
   * Gets the component access controller.
   */
  private ComponentAccessController getComponentAccessController() {
    AccessController<String> accessController =
        AccessControllerProvider.getAccessController("componentAccessController");
    return (ComponentAccessController) accessController;
  }
}
