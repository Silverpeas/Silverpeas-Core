/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.pdcsubscription.control;

import org.owasp.encoder.Encode;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.pdc.classification.Criteria;
import org.silverpeas.core.pdc.pdc.model.AxisHeader;
import org.silverpeas.core.pdc.pdc.model.AxisValueCriterion;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.pdc.subscription.model.PdcSubscription;
import org.silverpeas.core.pdc.subscription.model.PdcSubscriptionPositionCriteria;
import org.silverpeas.core.pdc.subscription.service.PdcSubscriptionService;
import org.silverpeas.core.subscription.*;
import org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants;
import org.silverpeas.core.subscription.constant.SubscriberType;
import org.silverpeas.core.subscription.constant.SubscriptionMethod;
import org.silverpeas.core.subscription.service.GroupSubscriptionSubscriber;
import org.silverpeas.core.subscription.service.UserSubscriptionSubscriber;
import org.silverpeas.core.subscription.util.SubscriptionList;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberMapBySubscriberType;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.selection.SelectionUsersGroups;
import org.silverpeas.core.web.subscription.SubscriptionComparator;
import org.silverpeas.core.web.subscription.bean.AbstractSubscriptionBean;
import org.silverpeas.core.web.subscription.bean.SubscriptionBeanProvider;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.util.StringUtil;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.silverpeas.core.pdc.subscription.model.PdcSubscriptionConstants.PDC;
import static org.silverpeas.core.subscription.SubscriptionResourceType.from;
import static org.silverpeas.core.subscription.util.SubscriptionUtil.isSameVisibilityAsTheCurrentRequester;

public class PdcSubscriptionSessionController extends AbstractComponentSessionController {
  @Serial
  private static final long serialVersionUID = 3130701500269550099L;

  /**
   * The subscription on the PdC that is currently edited by the user.
   */
  private final PdcSubscriptionEdition edition = new PdcSubscriptionEdition();

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

  private PdcManager getPdcManager() {
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
    return category.getHandledTypes()
        .stream()
        .flatMap(t ->
            SubscriptionBeanProvider.getByUserSubscriberAndSubscriptionResourceType(t,
                currentUserId, getLanguage()).stream())
        .sorted(new SubscriptionComparator())
        .collect(Collectors.toList());
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

  /**
   * Gets all the position criteria on the PdC the specified user is subscribed to, either by
   * himself or because the subscription has been forced for him or for one of his groups.
   * @param userId the unique identifier of a user.
   * @return a list of position criteria on the PdC, sorted by their name.
   */
  public List<PdcSubscriptionData> getUserPdcSubscriptions(final String userId)
      throws PdcException {
    final List<Subscription> subscriptions = getSubscribeService().getByUserSubscriber(userId)
        .stream()
        .filter(s -> PDC.equals(s.getResource().getType()))
        .sorted(Comparator.comparing(s -> positionCriteriaOf(s).getName()))
        .toList();
    final List<PdcSubscriptionData> data = new ArrayList<>(subscriptions.size());
    for (final Subscription subscription : subscriptions) {
      final PdcSubscriptionPositionCriteria resource = positionCriteriaOf(subscription);
      final PdcSubscriptionData.Builder builder = PdcSubscriptionData
          .about(resource.getId(), resource.getName())
          .withPositions(getPathCriteria(resource.getCriteria()), getLanguage())
          .withNature(natureOf(subscription));
      if (SubscriptionMethod.FORCED == subscription.getSubscriptionMethod()) {
        builder.asForced();
      }
      data.add(builder.build());
    }
    return data;
  }

  private static PdcSubscriptionPositionCriteria positionCriteriaOf(final Subscription subscription) {
    return (PdcSubscriptionPositionCriteria) subscription.getResource();
  }

  /**
   * Builds the localized nature of the given subscription: is it a personal subscription or one
   * that has been forced, and, when it comes from a group of users, the name of that group.
   */
  private String natureOf(final Subscription subscription) {
    final StringBuilder nature =
        new StringBuilder(getString("SubscriptionMethod." + subscription.getSubscriptionMethod()));
    final SubscriptionSubscriber subscriber = subscription.getSubscriber();
    if (SubscriberType.GROUP == subscriber.getType()) {
      nature.append(" ")
          .append(getString("SubscriptionType.GROUP"))
          .append(" <strong>")
          .append(Encode.forHtml(Group.getById(subscriber.getId()).getName()))
          .append("</strong>");
    }
    return nature.toString();
  }

  /**
   * Is the current user allowed to manage the forced subscriptions on the PdC?
   * @return true if he is either an administrator or a manager of the PdC. Manager
   * of at least one of the axis of the PdC aren't allowed to manage forced subscriptions on the
   * PdC.
   */
  public boolean isPdcManager() {
    return getUserDetail().isAccessAdmin() || getUserDetail().isAccessPdcManager();
  }

  /**
   * Gets all the position criteria on the PdC on which a subscription has been forced by a manager
   * of the PdC for at least one user or one group of users.
   * @return a list of position criteria on the PdC, sorted by their name.
   */
  public List<PdcSubscriptionData> getForcedPdcSubscriptions() throws PdcException {
    final List<PdcSubscriptionPositionCriteria> resources = getPdcSubscriptionService().getAllPositionCriteria()
        .stream()
        .filter(r -> !getSubscribeService().getByResource(r, SubscriptionMethod.FORCED).isEmpty())
        .sorted(Comparator.comparing(PdcSubscriptionPositionCriteria::getName))
        .toList();
    final List<PdcSubscriptionData> data = new ArrayList<>(resources.size());
    for (final PdcSubscriptionPositionCriteria resource : resources) {
      // only the forced subscriptions are listed here, hence rendering the nature of each of them
      // would be pointless
      data.add(PdcSubscriptionData.about(resource.getId(), resource.getName())
          .withPositions(getPathCriteria(resource.getCriteria()), getLanguage())
          .asForced()
          .build());
    }
    return data;
  }

  /**
   * Creates a new position criteria on the PdC and subscribes the current user to it.
   * @param name the name of the position criteria.
   * @param positions the positions on the axis of the PdC.
   */
  public void createPdcSubscription(final String name, final List<AxisValueCriterion> positions) {
    final PdcSubscriptionPositionCriteria resource =
        getPdcSubscriptionService().createPositionCriteria(name, positions);
    final Subscription subscription = SubscriptionFactory.get()
        .createSubscriptionInstance(UserSubscriptionSubscriber.from(getUserId()), resource,
            getUserId());
    getSubscribeService().subscribe(subscription);
  }

  /**
   * Updates the position criteria on the PdC that is currently edited. The subscriptions on it are
   * left untouched.
   * @param name the new name of the position criteria or nothing to keep the current one.
   * @param positions the new positions on the axis of the PdC.
   */
  public void updateCurrentPdcSubscription(final String name,
      final List<AxisValueCriterion> positions) {
    final PdcSubscriptionPositionCriteria current = getCurrentPdcSubscription();
    final String newName = StringUtil.isDefined(name) ? name : current.getName();
    getPdcSubscriptionService()
        .updatePositionCriteria(new PdcSubscriptionPositionCriteria(current.getId(), newName, positions));
  }

  /**
   * Unsubscribes the current user from the specified position criteria on the PdC. Once a set has
   * no more subscribers, it is deleted as it isn't shared between the subscribers: each of them is
   * defined for a given subscription.
   * <p>
   * Only the subscriptions the user has created himself are removed: a forced subscription is
   * managed from the window of the PdC by its managers and hence is silently skipped here.
   * </p>
   * @param resourceIds the identifiers of the position criteria on the PdC.
   */
  public void deletePdcSubscriptions(final String[] resourceIds) {
    final PdcSubscriptionService service = getPdcSubscriptionService();
    for (final String resourceId : resourceIds) {
      final PdcSubscriptionPositionCriteria resource = PdcSubscriptionPositionCriteria.from(resourceId);
      final List<Subscription> selfCreated = getSubscribeService()
          .getBySubscriberAndResource(UserSubscriptionSubscriber.from(getUserId()), resource)
          .stream()
          .filter(s -> SubscriptionMethod.SELF_CREATION == s.getSubscriptionMethod())
          .collect(Collectors.toList());
      if (selfCreated.isEmpty()) {
        continue;
      }
      getSubscribeService().unsubscribe(selfCreated);
      if (getSubscribeService().getByResource(resource).isEmpty()) {
        service.deletePositionCriteria(resourceId);
      }
    }
  }

  public AxisHeader getAxisHeader(String axisId) throws PdcException {
    return getPdcManager().getAxisHeader(axisId);
  }

  public List<Value> getFullPath(String valueId, String treeId) throws PdcException {
    return getPdcManager().getFullPath(valueId, treeId);
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

  public List<List<Value>> getPathCriteria(List<? extends Criteria> searchCriteria) throws
      PdcException {
    List<List<Value>> pathCriteria = new ArrayList<>();

    if (!searchCriteria.isEmpty()) {
      for (Criteria sc : searchCriteria) {
        int searchAxisId = sc.getAxisId();
        String searchValue = getLastValueOf(sc.getValue());
        AxisHeader axis = getAxisHeader(Integer.toString(searchAxisId));

        String treeId = null;
        if (axis != null) {
          treeId = Integer.toString(axis.getRootId());
        }

        List<Value> fullPath = new ArrayList<>();
        if (treeId != null) {
          fullPath = getFullPath(searchValue, treeId);
        }

        pathCriteria.add(fullPath);
      }
    }
    return pathCriteria;
  }

  /**
   * Gets the position criteria on the PdC that is currently edited.
   * @return a {@link PdcSubscriptionPositionCriteria} instance or null if no set is currently edited.
   */
  public PdcSubscriptionPositionCriteria getCurrentPdcSubscription() {
    return edition.getPositionCriteria();
  }

  /**
   * Sets as currently edited the position criteria on the PdC with the specified identifier.
   * @param resourceId the identifier of position criteria on the PdC.
   * @return the corresponding {@link PdcSubscriptionPositionCriteria} instance.
   */
  public PdcSubscriptionPositionCriteria setAsCurrentPdcSubscription(final String resourceId) {
    edition.setResourceId(resourceId);
    return getCurrentPdcSubscription();
  }

  /*
   * Management of the forced subscriptions from the window of the PdC. Unlike a personal
   * subscription, a forced one is defined with its subscribers within the same screen, and hence
   * the edition has to be kept in the session while the manager of the PdC goes to the user panel
   * to designate them.
   */

  /**
   * Starts the edition of a forced subscription on the PdC. The edition is initialized either from
   * an existing position criteria and its current forced subscribers or, if no identifier is given,
   * from scratch for a subscription to be created.
   * @param resourceId the identifier of position criteria on the PdC or null for a creation.
   * @param scope the name of the window of the PdC from which the edition is started. It is kept
   * along the edition as it has to survive the round trip to the user panel.
   */
  public void startForcedPdcSubscriptionEdition(final String resourceId, final String scope) {
    edition.clear();
    edition.setScope(scope);
    if (StringUtil.isDefined(resourceId)) {
      final PdcSubscriptionPositionCriteria resource = setAsCurrentPdcSubscription(resourceId);
      edition.setDefinition(resource.getName(), resource.getCriteria());
      final SubscriptionSubscriberMapBySubscriberType subscribers = getSubscribeService()
          .getSubscribers(resource, SubscriptionMethod.FORCED)
          .indexBySubscriberType()
          .filterOnDomainVisibilityFrom(getUserDetail());
      edition.setSubscribers(subscribers.get(SubscriberType.USER).getAllIds(),
          subscribers.get(SubscriberType.GROUP).getAllIds());
    }
  }

  /**
   * Keeps the edition in progress of a forced subscription on the PdC before leaving the screen of
   * edition, typically to designate the subscribers in the user panel.
   * @param name the name of the position criteria as filled in the screen of edition.
   * @param positions the positions on the axis of the PdC as selected in the screen of edition.
   */
  public void keepForcedPdcSubscriptionEdition(final String name,
      final List<AxisValueCriterion> positions) {
    edition.setDefinition(name, positions);
  }

  public String getEditedPdcSubscriptionName() {
    return edition.getName();
  }

  public String getEditedPdcSubscriptionScope() {
    return edition.getScope();
  }

  /**
   * Is the forced subscription being edited a new one, not yet saved?
   * @return true if the edition is about a creation, false if it is about an update.
   */
  public boolean isEditedPdcSubscriptionNew() {
    return edition.isNew();
  }

  public List<? extends Criteria> getEditedPdcSubscriptionPositions() {
    return edition.getPositions();
  }

  /**
   * Gets the users that are currently designated as the subscribers of the forced subscription
   * being edited.
   * @return a list of users, sorted by their name.
   */
  public List<UserDetail> getEditedPdcSubscriptionUsers() {
    return Stream.of(SelectionUsersGroups.getUserDetails(edition.getUserIds()))
        .sorted(Comparator.comparing(UserDetail::getDisplayedName))
        .collect(Collectors.toList());
  }

  /**
   * Gets the groups of users that are currently designated as the subscribers of the forced
   * subscription being edited.
   * @return a list of groups of users, sorted by their name.
   */
  public List<Group> getEditedPdcSubscriptionGroups() {
    return Stream.of(SelectionUsersGroups.getGroups(edition.getGroupIds()))
        .sorted(Comparator.comparing(Group::getName))
        .collect(Collectors.toList());
  }

  /**
   * Initializes the user panel with the subscribers currently designated for the forced
   * subscription being edited.
   * @return the URL of the user panel.
   */
  public String toUserPanel() {
    final String context = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
    final Selection sel = getSelection();
    sel.resetAll();
    sel.setHostPath(null);
    sel.setGoBackURL(context + "/RpdcSubscriptionPeas/jsp/FromUserPanel");
    sel.setCancelURL(context + "/RpdcSubscriptionPeas/jsp/BackToPdcSubscription");
    sel.setMultiSelect(true);
    sel.setPopupMode(false);
    sel.setSelectedElements(edition.getUserIds());
    sel.setSelectedSets(edition.getGroupIds());
    // no extra parameter is set here: unlike the resources of the other types of subscription, the
    // PdC isn't handled by a component instance and hence the selection isn't restricted to the
    // users and groups that are allowed to access a given application.
    return Selection.getSelectionURL();
  }

  /**
   * Gets back from the user panel the subscribers designated for the forced subscription being
   * edited. Only the users and the groups having the same domain visibility as the current
   * requester are kept.
   */
  public void fromUserPanel() {
    final Selection sel = getSelection();
    edition.setSubscribers(
        Stream.of(SelectionUsersGroups.getUserDetails(sel.getSelectedElements()))
            .filter(u -> isSameVisibilityAsTheCurrentRequester(u, getUserDetail()))
            .map(UserDetail::getId)
            .collect(Collectors.toList()),
        Stream.of(SelectionUsersGroups.getGroups(sel.getSelectedSets()))
            .filter(g -> isSameVisibilityAsTheCurrentRequester(g, getUserDetail()))
            .map(Group::getId)
            .collect(Collectors.toList()));
  }

  /**
   * Saves the forced subscription on the PdC being edited: the position criteria is created or
   * updated, then the subscriptions of the designated subscribers are registered while those of
   * the subscribers that have been withdrawn are removed.
   * @param name the name of the position criteria.
   * @param positions the positions on the axis of the PdC.
   */
  public void saveForcedPdcSubscription(final String name,
      final List<AxisValueCriterion> positions) {
    keepForcedPdcSubscriptionEdition(name, positions);
    final PdcSubscriptionPositionCriteria resource;
    if (edition.isNew()) {
      resource =
          getPdcSubscriptionService().createPositionCriteria(edition.getName(), edition.getPositions());
      edition.setResourceId(resource.getId());
    } else {
      resource = new PdcSubscriptionPositionCriteria(edition.getResourceId(), edition.getName(),
          edition.getPositions());
      getPdcSubscriptionService().updatePositionCriteria(resource);
    }
    final List<Subscription> subscriptions = Stream.concat(
            Stream.of(edition.getUserIds()).map(UserSubscriptionSubscriber::from),
            Stream.of(edition.getGroupIds()).map(GroupSubscriptionSubscriber::from))
        .map(s -> (Subscription) new PdcSubscription(s, resource, SubscriptionMethod.FORCED,
            getUserId()))
        .collect(Collectors.toList());
    // getting all the existing forced subscriptions and selecting those that have to be deleted
    final SubscriptionList toDelete =
        getSubscribeService().getByResource(resource, SubscriptionMethod.FORCED);
    toDelete.removeAll(subscriptions);
    toDelete.filterOnDomainVisibilityFrom(getUserDetail());
    getSubscribeService().unsubscribe(toDelete);
    // nothing is registered for the subscriptions that already exist
    getSubscribeService().subscribe(subscriptions);
  }

  /**
   * Deletes the specified position criteria on the PdC as well as all the forced subscriptions on
   * them. Unlike a personal subscription, a forced one is entirely managed by the managers of the
   * PdC and hence the position criteria is deleted whatever its subscribers.
   * @param resourceIds the identifiers of the position criteria on the PdC.
   */
  public void deleteForcedPdcSubscriptions(final String[] resourceIds) {
    final PdcSubscriptionService service = getPdcSubscriptionService();
    for (final String resourceId : resourceIds) {
      service.deletePositionCriteria(resourceId);
    }
  }

  /**
   * A subscription on the PdC being edited by the user.
   * <p>
   * Both the definition of the position criteria and, for a forced subscription, the subscribers
   * that are designated for it are edited within the same screen. As the designation of the
   * subscribers is done in the user panel, the edition in progress is kept here in the session in
   * order to survive the round trip to that panel.
   * </p>
   */
  private static class PdcSubscriptionEdition implements Serializable {
    @Serial
    private static final long serialVersionUID = -4062576374581172745L;

    private String resourceId = null;
    private String name = "";
    private String scope = "";
    private List<AxisValueCriterion> positions = List.of();
    private List<String> userIds = List.of();
    private List<String> groupIds = List.of();

    void clear() {
      resourceId = null;
      name = "";
      scope = "";
      positions = List.of();
      userIds = List.of();
      groupIds = List.of();
    }

    String getScope() {
      return scope;
    }

    void setScope(final String scope) {
      this.scope = scope == null ? "" : scope;
    }

    boolean isNew() {
      return resourceId == null;
    }

    String getResourceId() {
      return resourceId;
    }

    void setResourceId(final String resourceId) {
      this.resourceId = resourceId;
    }

    PdcSubscriptionPositionCriteria getPositionCriteria() {
      return resourceId == null ? null : PdcSubscriptionPositionCriteria.from(resourceId);
    }

    String getName() {
      return name;
    }

    List<AxisValueCriterion> getPositions() {
      return positions;
    }

    void setDefinition(final String name, final List<AxisValueCriterion> positions) {
      this.name = name == null ? "" : name;
      this.positions = positions == null ? List.of() : List.copyOf(positions);
    }

    String[] getUserIds() {
      return userIds.toArray(new String[0]);
    }

    String[] getGroupIds() {
      return groupIds.toArray(new String[0]);
    }

    void setSubscribers(final Collection<String> userIds, final Collection<String> groupIds) {
      this.userIds = List.copyOf(userIds);
      this.groupIds = List.copyOf(groupIds);
    }
  }
}
