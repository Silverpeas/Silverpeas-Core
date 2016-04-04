/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.pdcsubscription.control;

import org.silverpeas.core.pdc.subscription.model.PdcSubscription;
import org.silverpeas.core.pdc.subscription.service.PdcSubscriptionService;
import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.subscription.SubscriptionService;
import org.silverpeas.core.subscription.SubscriptionServiceProvider;
import org.silverpeas.core.subscription.constant.SubscriptionResourceType;
import org.silverpeas.core.subscription.service.ComponentSubscription;
import org.silverpeas.core.subscription.service.NodeSubscription;
import org.silverpeas.core.subscription.service.UserSubscriptionSubscriber;
import org.silverpeas.core.pdc.classification.Criteria;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.pdc.pdc.model.AxisHeader;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.web.subscription.SubscriptionComparator;
import org.silverpeas.core.web.subscription.bean.ComponentSubscriptionBean;
import org.silverpeas.core.web.subscription.bean.NodeSubscriptionBean;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PdcSubscriptionSessionController extends AbstractComponentSessionController {

  private PdcSubscription currentPdcSubscription = null;

  /**
   * Constructor Creates new PdcSubscription Session Controller
   *
   * @param mainSessionCtrl
   * @param componentContext
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
    return ServiceProvider.getService(PdcManager.class);
  }

  private SubscriptionService getSubscribeService() {
    return SubscriptionServiceProvider.getSubscribeService();
  }

  public NodeService getNodeBm() {
    return ServiceProvider.getService(NodeService.class);
  }

  public Collection<NodeSubscriptionBean> getNodeUserSubscriptions(String userId) {
    String currentUserId = userId;
    List<NodeSubscriptionBean> subscribes = new ArrayList<NodeSubscriptionBean>();
    if (!StringUtil.isDefined(currentUserId)) {
      currentUserId = getUserId();
    }
    Collection<Subscription> list = getSubscribeService().getByUserSubscriber(currentUserId);
    for (Subscription subscription : list) {
      try {
        // Subscriptions managed at this level are only those of node subscription.
        if (SubscriptionResourceType.NODE.equals(subscription.getResource().getType())) {
          ComponentInstLight componentInstLight = getOrganisationController()
              .getComponentInstLight(subscription.getResource().getInstanceId());
          if (componentInstLight != null) {
            Collection<NodeDetail> path =
                getNodeBm().getPath((NodePK) subscription.getResource().getPK());
            subscribes.add(
                new NodeSubscriptionBean(subscription, path, componentInstLight, getLanguage()));
          }
        }
      } catch (Exception e) {
        // User subscribed to a non existing component or topic .Do nothing. Process next
        // subscription.
      }
    }
    Collections.sort(subscribes, new SubscriptionComparator());
    return subscribes;
  }

  public Collection<ComponentSubscriptionBean> getComponentUserSubscriptions(String userId) {
    String currentUserId = userId;
    List<ComponentSubscriptionBean> subscribes = new ArrayList<ComponentSubscriptionBean>();
    if (!StringUtil.isDefined(currentUserId)) {
      currentUserId = getUserId();
    }
    Collection<Subscription> list = getSubscribeService().getByUserSubscriber(currentUserId);
    for (Subscription subscription : list) {
      // Subscriptions managed at this level are only those of node subscription.
      if (SubscriptionResourceType.COMPONENT.equals(subscription.getResource().getType())) {
        ComponentInstLight componentInstLight = getOrganisationController()
            .getComponentInstLight(subscription.getResource().getInstanceId());
        if (componentInstLight != null) {
          subscribes
              .add(new ComponentSubscriptionBean(subscription, componentInstLight, getLanguage()));
        }
      }
    }
    Collections.sort(subscribes, new SubscriptionComparator());
    return subscribes;
  }

  public void deleteThemes(String[] themes) {
    for (final String theme : themes) {
      // convertir la chaine en NodePK
      String[] subscribtionIdentifiers = theme.split("-");
      String nodeId = subscribtionIdentifiers[0];
      String instanceId = subscribtionIdentifiers[1];
      String creatorId = subscribtionIdentifiers[2];
      NodeSubscription subscription =
          new NodeSubscription(UserSubscriptionSubscriber.from(getUserId()),
          new NodePK(nodeId, instanceId), creatorId);
      getSubscribeService().unsubscribe(subscription);
    }
  }

  public void deleteComponentSubscription(String[] subscriptions) {
    for (final String subscribtion : subscriptions) {
      // convertir la chaine en NodePK
      String[] subscribtionIdentifiers = subscribtion.split("-");
      String instanceId = subscribtionIdentifiers[0];
      String creatorId = subscribtionIdentifiers[1];
      ComponentSubscription subscription =
          new ComponentSubscription(UserSubscriptionSubscriber.from(getUserId()), instanceId,
          creatorId);
      getSubscribeService().unsubscribe(subscription);
    }
  }

  public List<PdcSubscription> getUserPDCSubscription() throws RemoteException {
    return getPdcSubscriptionService().getPDCSubscriptionByUserId(Integer.parseInt(getUserId()));
  }

  public List<PdcSubscription> getUserPDCSubscription(int userId) throws RemoteException {
    return getPdcSubscriptionService().getPDCSubscriptionByUserId(userId);
  }

  public PdcSubscription getPDCSubsriptionById(int id) throws RemoteException {
    return getPdcSubscriptionService().getPDCSubsriptionById(id);
  }

  public void createPDCSubscription(PdcSubscription subscription)
      throws RemoteException {
    subscription.setId(getPdcSubscriptionService().createPDCSubscription(subscription));
  }

  public void updatePDCSubscription(PdcSubscription subscription) throws RemoteException {
    getPdcSubscriptionService().updatePDCSubscription(subscription);
  }

  public void removePDCSubscriptionById(int id) throws RemoteException {
    getPdcSubscriptionService().removePDCSubscriptionById(id);
  }

  public void removeICByPK(int[] ids) throws RemoteException {
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
      int lastIdx = path.lastIndexOf("/");
      newValueId = path.substring(lastIdx + 1);
    }
    return newValueId;
  }

  public List<List<Value>> getPathCriterias(List<? extends Criteria> searchCriterias) throws
      Exception {
    List<List<Value>> pathCriteria = new ArrayList<List<Value>>();

    if (searchCriterias.size() > 0) {
      for (Criteria sc : searchCriterias) {
        int searchAxisId = sc.getAxisId();
        String searchValue = getLastValueOf(sc.getValue());
        AxisHeader axis = getAxisHeader(Integer.toString(searchAxisId));

        String treeId = null;
        if (axis != null) {
          treeId = Integer.toString(axis.getRootId());
        }

        List<Value> fullPath = new ArrayList<Value>();
        if (searchValue != null && treeId != null) {
          fullPath = getFullPath(searchValue, treeId);
        }

        pathCriteria.add(fullPath);
      }
    }
    return pathCriteria;
  }

  @Override
  public void close() {
  }

  public PdcSubscription getCurrentPdcSubscription() {
    return currentPdcSubscription;
  }

  public void setCurrentPdcSubscription(PdcSubscription currentPdcSubscription) {
    this.currentPdcSubscription = currentPdcSubscription;
  }

  public void createPDCSubscription(String name, final List<? extends Criteria> criteria) throws
      RemoteException {
    PdcSubscription subscription =
        new PdcSubscription(-1, name, criteria, Integer.parseInt(getUserId()));
    createPDCSubscription(subscription);
  }

  public void updateCurrentSubscription(String name, final List<? extends Criteria> criteria) throws
      RemoteException {
    PdcSubscription subscription = getCurrentPdcSubscription();
    if (StringUtil.isDefined(name)) {
      subscription.setName(name);
    }

    subscription.setPdcContext(criteria);
    updatePDCSubscription(subscription);
  }

  public PdcSubscription setAsCurrentPDCSubscription(String subscriptionId) throws RemoteException {
    int id = Integer.valueOf(subscriptionId);
    PdcSubscription pdcSubscription = getPDCSubsriptionById(id);
    setCurrentPdcSubscription(pdcSubscription);
    return pdcSubscription;
  }
}
