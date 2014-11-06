/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.pdcSubscriptionPeas.control;

import com.silverpeas.pdcSubscription.PdcSubscriptionRuntimeException;
import com.silverpeas.pdcSubscription.control.PdcSubscriptionService;
import com.silverpeas.pdcSubscription.model.PDCSubscription;
import com.silverpeas.subscribe.Subscription;
import com.silverpeas.subscribe.SubscriptionService;
import com.silverpeas.subscribe.SubscriptionServiceProvider;
import com.silverpeas.subscribe.constant.SubscriptionResourceType;
import com.silverpeas.subscribe.service.ComponentSubscription;
import com.silverpeas.subscribe.service.NodeSubscription;
import com.silverpeas.subscribe.service.UserSubscriptionSubscriber;
import org.silverpeas.util.ServiceProvider;
import org.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.classifyEngine.Criteria;
import com.stratelia.silverpeas.pdc.control.PdcManager;
import com.stratelia.silverpeas.pdc.model.AxisHeader;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.Value;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import org.silverpeas.util.EJBUtilitaire;
import org.silverpeas.util.JNDINames;
import org.silverpeas.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.node.control.NodeService;
import com.stratelia.webactiv.node.model.NodeDetail;
import com.stratelia.webactiv.node.model.NodePK;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.silverpeas.subscription.SubscriptionComparator;
import org.silverpeas.subscription.bean.ComponentSubscriptionBean;
import org.silverpeas.subscription.bean.NodeSubscriptionBean;

public class PdcSubscriptionSessionController extends AbstractComponentSessionController {

  private PDCSubscription currentPDCSubscription = null;

  /**
   * Constructor Creates new PdcSubscription Session Controller
   *
   * @param mainSessionCtrl
   * @param componentContext
   */
  public PdcSubscriptionSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.silverpeas.pdcSubscriptionPeas.multilang.pdcSubscriptionBundle",
        "com.silverpeas.pdcSubscriptionPeas.settings.pdcSubscriptionPeasIcons");
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
    try {
      return EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeService.class);
    } catch (Exception e) {
      throw new PdcSubscriptionRuntimeException("PdcSubscriptionSessionController.getNodeService()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
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

  public List<PDCSubscription> getUserPDCSubscription() throws RemoteException {
    return getPdcSubscriptionService().getPDCSubscriptionByUserId(Integer.parseInt(getUserId()));
  }

  public List<PDCSubscription> getUserPDCSubscription(int userId) throws RemoteException {
    return getPdcSubscriptionService().getPDCSubscriptionByUserId(userId);
  }

  public PDCSubscription getPDCSubsriptionById(int id) throws RemoteException {
    return getPdcSubscriptionService().getPDCSubsriptionById(id);
  }

  public void createPDCSubscription(PDCSubscription subscription)
      throws RemoteException {
    subscription.setId(getPdcSubscriptionService().createPDCSubscription(subscription));
  }

  public void updatePDCSubscription(PDCSubscription subscription) throws RemoteException {
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

  public PDCSubscription getCurrentPDCSubscription() {
    return currentPDCSubscription;
  }

  public void setCurrentPDCSubscription(PDCSubscription currentPDCSubscription) {
    this.currentPDCSubscription = currentPDCSubscription;
  }

  public void createPDCSubscription(String name, final List<? extends Criteria> criteria) throws
      RemoteException {
    PDCSubscription subscription =
        new PDCSubscription(-1, name, criteria, Integer.parseInt(getUserId()));
    createPDCSubscription(subscription);
  }

  public void updateCurrentSubscription(String name, final List<? extends Criteria> criteria) throws
      RemoteException {
    PDCSubscription subscription = getCurrentPDCSubscription();
    if (StringUtil.isDefined(name)) {
      subscription.setName(name);
    }

    subscription.setPdcContext(criteria);
    updatePDCSubscription(subscription);
  }

  public PDCSubscription setAsCurrentPDCSubscription(String subscriptionId) throws RemoteException {
    int id = Integer.valueOf(subscriptionId);
    PDCSubscription pdcSubscription = getPDCSubsriptionById(id);
    setCurrentPDCSubscription(pdcSubscription);
    return pdcSubscription;
  }
}
