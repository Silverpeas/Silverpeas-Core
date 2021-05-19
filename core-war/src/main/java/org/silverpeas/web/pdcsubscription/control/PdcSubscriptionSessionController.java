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

import org.silverpeas.core.pdc.classification.Criteria;
import org.silverpeas.core.pdc.pdc.model.AxisHeader;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.pdc.subscription.model.PdcSubscription;
import org.silverpeas.core.pdc.subscription.service.PdcSubscriptionService;
import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.subscription.SubscriptionFactory;
import org.silverpeas.core.subscription.SubscriptionResource;
import org.silverpeas.core.subscription.SubscriptionResourceType;
import org.silverpeas.core.subscription.SubscriptionService;
import org.silverpeas.core.subscription.SubscriptionServiceProvider;
import org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants;
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

import static org.silverpeas.core.subscription.SubscriptionResourceType.from;

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

  public String getSubscriptionResourceTypeLabel(final SubscriptionResourceType type) {
    return SubscriptionBeanProvider.getSubscriptionTypeListLabel(type, getLanguage());
  }

  public List<SubscriptionCategory> getSubscriptionCategories() {
    return SubscriptionCategoryWebManager.get().getCategories(this);
  }

  /**
   * Gets the Subscription category from its identifier.
   * @param categoryId the identifier of a subscription category.
   * @return a {@link SubscriptionCategory} instance.
   */
  public SubscriptionCategory getSubscriptionCategory(final String categoryId) {
    return getSubscriptionCategories().stream()
        .filter(c -> c.getId().equals(categoryId))
        .findFirst()
        .orElseGet(() -> getSubscriptionCategories().stream()
            .filter(c -> c.getId().equals(CommonSubscriptionResourceConstants.COMPONENT.getName()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("COMPONENT category MUST exists")));
  }

  public List<AbstractSubscriptionBean> getUserSubscriptionsOfCategory(final String userId,
      final SubscriptionCategory category) {
    final String currentUserId = StringUtil.isDefined(userId) ? userId : getUserId();
    final List<AbstractSubscriptionBean> subscribes = category.getHandledTypes()
        .stream()
        .flatMap(t -> SubscriptionBeanProvider.getByUserSubscriberAndSubscriptionResourceType(t,
            currentUserId, getLanguage()).stream())
        .collect(Collectors.toList());
    subscribes.sort(new SubscriptionComparator());
    return subscribes;
  }

  public void deleteUserSubscriptions(final String[] selectedItems) {
    final SubscriptionFactory factory = SubscriptionFactory.get();
    final List<Subscription> subscriptionsToDeleted = Stream.of(selectedItems).map(i -> {
      // exploding data
      final String[] subscriptionIdentifiers = i.split("@");
      final SubscriptionResourceType type = from(subscriptionIdentifiers[0]);
      final String resourceId = subscriptionIdentifiers[1];
      final String instanceId = subscriptionIdentifiers[2];
      final String creatorId = subscriptionIdentifiers[3];
      final SubscriptionResource subscriptionResource = factory.createSubscriptionResourceInstance(
          type, resourceId, null, instanceId);
      return factory.createSubscriptionInstance(
          UserSubscriptionSubscriber.from(getUserId()), subscriptionResource, creatorId);
    }).collect(Collectors.toList());
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
