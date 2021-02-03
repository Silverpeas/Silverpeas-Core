/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.pdcsubscription.control;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.pdc.classification.Criteria;
import org.silverpeas.core.pdc.pdc.model.AxisHeader;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.pdc.subscription.model.PdcSubscription;
import org.silverpeas.core.pdc.subscription.service.PdcSubscriptionService;
import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.subscription.SubscriptionResourceType;
import org.silverpeas.core.subscription.SubscriptionService;
import org.silverpeas.core.subscription.SubscriptionServiceProvider;
import org.silverpeas.core.subscription.service.ComponentSubscription;
import org.silverpeas.core.subscription.service.NodeSubscription;
import org.silverpeas.core.subscription.service.PKSubscription;
import org.silverpeas.core.subscription.service.PKSubscriptionResource;
import org.silverpeas.core.subscription.service.UserSubscriptionSubscriber;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.subscription.SubscriptionComparator;
import org.silverpeas.core.web.subscription.bean.AbstractSubscriptionBean;
import org.silverpeas.core.web.subscription.bean.SubscriptionBeanProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants.COMPONENT;
import static org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants.NODE;

public class PdcSubscriptionSessionController extends AbstractComponentSessionController {
  private static final long serialVersionUID = 3130701500269550099L;

  private PdcSubscription currentPdcSubscription = null;

  /**
   * Constructor Creates new PdcSubscription Session Controller
   */
  public PdcSubscriptionSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.pdcSubscriptionPeas.multilang.pdcSubscriptionBundle",
        "org.silverpeas.pdcSubscriptionPeas.settings.pdcSubscriptionPeasIcons");
  }

  private PdcSubscriptionService getPdcSubscriptionService() {
    return ServiceProvider.getService(PdcSubscriptionService.class);
  }

  private PdcManager getPdcBm() {
    return PdcManager.get();
  }

  private SubscriptionService getSubscribeService() {
    return SubscriptionServiceProvider.getSubscribeService();
  }

  public NodeService getNodeBm() {
    return NodeService.get();
  }

  public String getSubscriptionResourceTypeLabel(final SubscriptionResourceType type) {
    return SubscriptionBeanProvider.getSubscriptionTypeListLabel(type, getLanguage());
  }

  public List<AbstractSubscriptionBean> getUserSubscriptionsOfType(final String userId,
      final SubscriptionResourceType type) {
    String currentUserId = userId;
    if (!StringUtil.isDefined(currentUserId)) {
      currentUserId = getUserId();
    }
    final List<AbstractSubscriptionBean> subscribes = SubscriptionBeanProvider
        .getByUserSubscriberAndSubscriptionResourceType(type, currentUserId, getLanguage());
    subscribes.sort(new SubscriptionComparator());
    return subscribes;
  }

  public void deleteUserSubscriptionsOfType(final String[] selectedItems,
      final SubscriptionResourceType type) {
    final List<Subscription> subscriptionsToDeleted = Stream.of(selectedItems)
        .map(i -> {
          // exploding data
          final String[] subscriptionIdentifiers = i.split("-");
          final String resourceId = subscriptionIdentifiers[0];
          final String instanceId = subscriptionIdentifiers[1];
          final String creatorId = subscriptionIdentifiers[2];
          final Subscription subscription;
          if (type == COMPONENT) {
            subscription = new ComponentSubscription(UserSubscriptionSubscriber.from(getUserId()),
                instanceId, creatorId);
          } else if (type == NODE) {
            subscription = new NodeSubscription(UserSubscriptionSubscriber.from(getUserId()),
                new NodePK(resourceId, instanceId), creatorId);
          } else {
            subscription = new PKSubscription(UserSubscriptionSubscriber.from(getUserId()),
                PKSubscriptionResource.from(new ResourceReference(resourceId, instanceId), type),
                creatorId);
          }
          return subscription;
        })
        .collect(Collectors.toList());
      getSubscribeService().unsubscribe(subscriptionsToDeleted);
  }

  public List<PdcSubscription> getUserPDCSubscription() {
    return getPdcSubscriptionService().getPDCSubscriptionByUserId(Integer.parseInt(getUserId()));
  }

  public List<PdcSubscription> getUserPDCSubscription(int userId) {
    return getPdcSubscriptionService().getPDCSubscriptionByUserId(userId);
  }

  public PdcSubscription getPDCSubsriptionById(int id) {
    return getPdcSubscriptionService().getPDCSubsriptionById(id);
  }

  public void createPDCSubscription(PdcSubscription subscription) {
    subscription.setId(getPdcSubscriptionService().createPDCSubscription(subscription));
  }

  public void updatePDCSubscription(PdcSubscription subscription) {
    getPdcSubscriptionService().updatePDCSubscription(subscription);
  }

  public void removePDCSubscriptionById(int id) {
    getPdcSubscriptionService().removePDCSubscriptionById(id);
  }

  public void removeICByPK(int[] ids) {
    getPdcSubscriptionService().removePDCSubscriptionById(ids);
  }

  public AxisHeader getAxisHeader(String axisId) throws PdcException {
    return getPdcBm().getAxisHeader(axisId);
  }

  public List<Value> getFullPath(String valueId, String treeId) throws PdcException {
    return getPdcBm().getFullPath(valueId, treeId);
  }

  private String getLastValueOf(String path) {
    String newValueId = path;
    int len = path.length();
    path = path.substring(0, len - 1); // on retire le slash
    if (path.equals("/")) {
      newValueId = newValueId.substring(1); // on retire le slash
    } else {
      int lastIdx = path.lastIndexOf('/');
      newValueId = path.substring(lastIdx + 1);
    }
    return newValueId;
  }

  public List<List<Value>> getPathCriterias(List<? extends Criteria> searchCriterias) throws
      PdcException {
    List<List<Value>> pathCriteria = new ArrayList<>();

    if (!searchCriterias.isEmpty()) {
      for (Criteria sc : searchCriterias) {
        int searchAxisId = sc.getAxisId();
        String searchValue = getLastValueOf(sc.getValue());
        AxisHeader axis = getAxisHeader(Integer.toString(searchAxisId));

        String treeId = null;
        if (axis != null) {
          treeId = Integer.toString(axis.getRootId());
        }

        List<Value> fullPath = new ArrayList<>();
        if (searchValue != null && treeId != null) {
          fullPath = getFullPath(searchValue, treeId);
        }

        pathCriteria.add(fullPath);
      }
    }
    return pathCriteria;
  }

  public PdcSubscription getCurrentPdcSubscription() {
    return currentPdcSubscription;
  }

  public void setCurrentPdcSubscription(PdcSubscription currentPdcSubscription) {
    this.currentPdcSubscription = currentPdcSubscription;
  }

  public void createPDCSubscription(String name, final List<? extends Criteria> criteria) {
    PdcSubscription subscription =
        new PdcSubscription(-1, name, criteria, Integer.parseInt(getUserId()));
    createPDCSubscription(subscription);
  }

  public void updateCurrentSubscription(String name, final List<? extends Criteria> criteria) {
    PdcSubscription subscription = getCurrentPdcSubscription();
    if (StringUtil.isDefined(name)) {
      subscription.setName(name);
    }

    subscription.setPdcContext(criteria);
    updatePDCSubscription(subscription);
  }

  public PdcSubscription setAsCurrentPDCSubscription(String subscriptionId) {
    int id = Integer.parseInt(subscriptionId);
    PdcSubscription pdcSubscription = getPDCSubsriptionById(id);
    setCurrentPdcSubscription(pdcSubscription);
    return pdcSubscription;
  }
}
