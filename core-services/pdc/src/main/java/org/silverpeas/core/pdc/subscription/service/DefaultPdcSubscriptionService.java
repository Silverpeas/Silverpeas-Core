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
/*
 * Aliaksei_Budnikau
 * Date: Oct 24, 2002
 */
package org.silverpeas.core.pdc.subscription.service;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.contentcontainer.content.*;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.pdc.classification.Criteria;
import org.silverpeas.core.pdc.classification.Value;
import org.silverpeas.core.pdc.pdc.model.AxisValueCriterion;
import org.silverpeas.core.pdc.subscription.model.PdcSubscriptionPositionCriteria;
import org.silverpeas.core.pdc.subscription.model.PdcSubscriptionRuntimeException;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.subscription.SubscriptionService;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.kernel.logging.SilverLogger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class DefaultPdcSubscriptionService implements PdcSubscriptionService {

  @Inject
  private OrganizationController organizationController;
  @Inject
  private ContentManagementEngine contentManagementEngine;
  @Inject
  private SubscriptionService subscriptionService;
  @Inject
  private UserProvider userProvider;

  @Override
  public Optional<PdcSubscriptionPositionCriteria> getPositionCriteria(final String id) {
    try (Connection conn = DBUtil.openConnection()) {
      return PdcSubscriptionDAO.getById(conn, id);
    } catch (SQLException re) {
      throw new PdcSubscriptionRuntimeException(re);
    }
  }

  @Override
  public List<PdcSubscriptionPositionCriteria> getAllPositionCriteria() {
    try (Connection conn = DBUtil.openConnection()) {
      return PdcSubscriptionDAO.getAll(conn);
    } catch (SQLException re) {
      throw new PdcSubscriptionRuntimeException(re);
    }
  }

  @Override
  public List<PdcSubscriptionPositionCriteria> getPositionCriteriaMatching(
      final Collection<? extends List<? extends Value>> classification) {
    return getAllPositionCriteria().stream()
        .filter(c -> classification.stream().anyMatch(p -> isCorrespondingSubscription(c, p)))
        .toList();
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public PdcSubscriptionPositionCriteria createPositionCriteria(final String name,
      final List<AxisValueCriterion> positions) {
    try (Connection conn = DBUtil.openConnection()) {
      final String id = PdcSubscriptionDAO.create(conn, name, positions);
      return new PdcSubscriptionPositionCriteria(id, name, positions);
    } catch (SQLException re) {
      throw new PdcSubscriptionRuntimeException(re);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void updatePositionCriteria(final PdcSubscriptionPositionCriteria resource) {
    try (Connection conn = DBUtil.openConnection()) {
      PdcSubscriptionDAO.update(conn, resource);
    } catch (SQLException re) {
      throw new PdcSubscriptionRuntimeException(re);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void deletePositionCriteria(final String id) {
    subscriptionService.unsubscribeByResource(PdcSubscriptionPositionCriteria.from(id));
    try (Connection conn = DBUtil.openConnection()) {
      PdcSubscriptionDAO.deleteById(conn, id);
    } catch (SQLException re) {
      throw new PdcSubscriptionRuntimeException(re);
    }
  }

  @Override
  public void checkAxisOnDelete(int axisId, String axisName) {
    final List<PdcSubscriptionPositionCriteria> resources = getResourcesUsing(axisId);
    for (final PdcSubscriptionPositionCriteria resource : resources) {
      notifyThenDelete(resource, axisName, false);
    }
  }

  @Override
  public void checkValueOnDelete(int axisId, String axisName, List<String> oldPath,
      List<String> newPath, List<org.silverpeas.core.pdc.pdc.model.Value> pathInfo) {
    final List<PdcSubscriptionPositionCriteria> resources = getResourcesUsing(axisId);
    for (final PdcSubscriptionPositionCriteria resource : resources) {
      // for each position criteria referring the axis affected by the value deletion, check if any
      // of its positions has been deleted
      if (checkSubscriptionRemove(resource, axisId, oldPath, newPath)) {
        notifyThenDelete(resource, axisName, true);
      }
    }
  }

  @Override
  public void notifyClassification(List<? extends Value> classifyValues, String componentId,
      int silverObjectId) {
    try {
      SilverContentVisibility visibility =
          contentManagementEngine.getSilverContentVisibility(silverObjectId);
      if (visibility.isVisible() != 1) {
        return;
      }
      final User author = userProvider.getCurrentRequester();
      // a notification is sent for each matching position criteria, but a subscriber is notified
      // only once whatever the number of his subscriptions matching the classification
      final Set<String> notifiedUserIds = new HashSet<>();
      for (final PdcSubscriptionPositionCriteria criteria : getPositionCriteriaMatching(
          Collections.singletonList(classifyValues))) {
        final List<String> subscriberIds = subscriptionService.getSubscribers(criteria)
            .getAllUserIds()
            .stream()
            .filter(u -> !notifiedUserIds.contains(u))
            .toList();
        final Map<String, ManagedContribution> contentsBySubscriber =
            getContentsBySubscriber(subscriberIds, componentId, silverObjectId);
        if (!contentsBySubscriber.isEmpty()) {
          UserNotificationHelper.buildAndSend(
              new PdcResourceClassificationUserNotification(criteria,
                  contentsBySubscriber.keySet(), contentsBySubscriber.values().iterator().next(),
                  author));
          notifiedUserIds.addAll(contentsBySubscriber.keySet());
        }
      }
    } catch (ContentManagerException e) {
      throw new PdcSubscriptionRuntimeException(e);
    }
  }

  /**
   * Gets the classified contribution as each of the specified subscribers is allowed to access
   * it. The subscribers that cannot access the contribution are left out.
   */
  private Map<String, ManagedContribution> getContentsBySubscriber(
      final Collection<String> subscriberIds, final String componentId,
      final int silverObjectId) {
    final Map<String, ManagedContribution> contentsBySubscriber = new LinkedHashMap<>();
    for (final String userId : subscriberIds) {
      if (organizationController.isComponentAvailableToUser(componentId, userId)) {
        // the user is able to see the component instance which contains the content
        final ManagedContribution silverContent =
            getSilverContent(componentId, silverObjectId, userId);
        if (silverContent != null) {
          contentsBySubscriber.put(userId, silverContent);
        } else {
          SilverLogger.getLogger(this).warn("User {0} not allowed to see the content {1}",
              userId, silverObjectId);
        }
      }
    }
    return contentsBySubscriber;
  }

  /**
   * Notifies all the subscribers of the given position criteria that it is about to be deleted,
   * then deletes both the set and the subscriptions on it.
   */
  private void notifyThenDelete(final PdcSubscriptionPositionCriteria resource, final String axisName,
      final boolean valueDeleted) {
    final List<String> subscribers =
        subscriptionService.getSubscribers(resource).getAllUserIds();
    if (!subscribers.isEmpty()) {
      UserNotificationHelper.buildAndSend(
          new PdcSubscriptionDeletionUserNotification(resource, subscribers, axisName,
              valueDeleted));
    }
    deletePositionCriteria(resource.getId());
  }

  private List<PdcSubscriptionPositionCriteria> getResourcesUsing(final int axisId) {
    try (Connection conn = DBUtil.openConnection()) {
      return PdcSubscriptionDAO.getByUsedAxis(conn, axisId);
    } catch (SQLException e) {
      throw new PdcSubscriptionRuntimeException(e);
    }
  }

  /**
   * get the silverContent object according to the given silverObjectid
   *
   * @param componentId - the component where is classified the silverContent
   * @param silverObjectId - the unique identifier of the silverContent
   * @return the {@link ManagedContribution} that has been classified onto the PdC
   */
  private ManagedContribution getSilverContent(String componentId, int silverObjectId,
      String userId) {

    List<ManagedContribution> silverContents;
    try {
      ContentPeas contentPeas = contentManagementEngine.getContentPeas(componentId);
      SilverpeasContentManager silverpeasContentManager = contentPeas.getContentManager();

      List<Integer> silverContentIds = Collections.singletonList(silverObjectId);
      List<ResourceReference> resourceReferences =
          contentManagementEngine.getResourceReferencesByContentIds(silverContentIds);

      silverContents = silverpeasContentManager.getSilverContentByReference(resourceReferences,
          userId);
    } catch (Exception e) {
      throw new PdcSubscriptionRuntimeException(e);
    }
    if (CollectionUtil.isNotEmpty(silverContents)) {
      return silverContents.getFirst();
    }

    return null;
  }

  /**
   * @param resource the position criteria on the PdC to check
   * @param axisId id of the axis value of which should be removed
   * @param oldPath list of original axis paths (before deletion)
   * @param newPath list new axis path to be places instead of old path
   * @return true if the position criteria should be removed
   */
  protected boolean checkSubscriptionRemove(PdcSubscriptionPositionCriteria resource,
      int axisId, List<String> oldPath, List<String> newPath) {
    for (Criteria criteria : resource.getCriteria()) {
      if (criteria.getAxisId() == axisId
          && checkValuesRemove(criteria.getValue(), oldPath, newPath)) {
        return true;
      }

    }
    return false;
  }

  protected boolean checkValuesRemove(String originalPath, List<String> oldPath,
      List<String> newPath) {
    if (!originalPath.endsWith("/")) {
      originalPath += "/";
    }
    // substring a value from original path. Ex: /2/3/9/ value will be /9/
    int idx = originalPath.lastIndexOf("/", originalPath.length() - 2);
    String value = originalPath.substring(idx);

    // check if extracted value presented in old path but not presented in
    // newPath
    return checkValueInPath(value, oldPath) && !checkValueInPath(value, newPath);
  }

  /**
   * Checks if values provided presented in provided path. <br> Ex1: path /2/3/4/5/ value: /5/
   * result:true <br> Ex2: path /2/3/4/5/ value: /3/ result true <br> Ex2: path /2/3/4/5/ value: /8/
   * result false
   */
  protected boolean checkValueInPath(String value, List<String> pathList) {
    for (String path : pathList) {
      String currentPath = path;
      if (currentPath == null) {
        // this means that first level has been removed, so use root path
        currentPath = "/0/";
      }
      if (currentPath.contains(value)) {
        return true;
      }
    }
    return false;
  }

  protected boolean isCorrespondingSubscription(PdcSubscriptionPositionCriteria resource,
      List<? extends Value> classifyValues) {
    List<? extends Criteria> searchCriterias = resource.getCriteria();
    if (classifyValues == null || searchCriterias.isEmpty()
        || classifyValues.isEmpty() || searchCriterias.size() > classifyValues.size()) {
      return false;
    }

    /*
     * The following algorithm implemented Loop every SearchCriteria and for axis of SearchCriteria
     * found ClassifyValue with such axis if true check getValue() of ClassifyValue and getValue()
     * of SearchCriteria. The start of the value String of ClassifyValue should match the whole
     * value String of SearchCriteria.
     */
    for (Criteria criteria : searchCriterias) {
      if (criteria == null) {
        continue;
      }
      boolean result = false;
      for (Value value : classifyValues) {
        if (checkValues(criteria, value)) {
          result = true;
          break;
        }
      }
      if (!result) {
        return false;
      }
    }
    return true;
  }

  protected boolean checkValues(Criteria criteria, Value searchValue) {
    return searchValue.getAxisId() == criteria.getAxisId() &&
           searchValue.getValue().startsWith(criteria.getValue());
  }
}
