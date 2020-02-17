/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.security.authorization;

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.MemoizedBooleanSupplier;
import org.silverpeas.core.util.MemoizedSupplier;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.*;
import static org.silverpeas.core.cache.service.VolatileCacheServiceProvider.getSessionVolatileResourceCacheService;
import static org.silverpeas.core.security.authorization.AccessControlOperation.*;

/**
 * Check the access to a publication for a user.
 * @author neysseric
 */
@Service
@Singleton
public class PublicationAccessController extends AbstractAccessController<PublicationPK>
    implements PublicationAccessControl {

  private static final String DATA_MANAGER_CONTEXT_KEY = "PublicationAccessControllerDataManager";

  private ComponentAccessControl componentAccessController;

  private NodeAccessControl nodeAccessController;

  @Inject
  PublicationAccessController(final ComponentAccessControl componentAccessController,
      final NodeAccessControl nodeAccessController) {
    // Instance by IoC only.
    this.componentAccessController = componentAccessController;
    this.nodeAccessController = nodeAccessController;
  }

  static DataManager getDataManager(final AccessControlContext context) {
    DataManager manager = context.get(DATA_MANAGER_CONTEXT_KEY, DataManager.class);
    if (manager == null) {
      manager = new DataManager(context);
      context.put(DATA_MANAGER_CONTEXT_KEY, manager);
    }
    return manager;
  }

  @Override
  public Stream<PublicationDetail> filterAuthorizedByUser(final String userId,
      final Collection<PublicationDetail> pubs, final AccessControlContext context) {
    final DataManager dataManager = getDataManager(context).loadCachesWithLoadedPublications(userId, pubs);
    return filterAuthorizedByUser(dataManager.getGivenPublicationPks(), userId, context)
        .map(dataManager::getPublicationData);
  }

  @Override
  public Stream<PublicationPK> filterAuthorizedByUser(final Collection<PublicationPK> pks,
      final String userId, final AccessControlContext context) {
    final DataManager dataManager = getDataManager(context).loadCaches(userId, pks);
    return pks.stream().map(dataManager::prepareCheckFor).filter(p -> isUserAuthorized(userId, p, context));
  }

  @Override
  public boolean isUserAuthorized(String userId, PublicationDetail pubDetail) {
    return isUserAuthorized(userId, pubDetail, AccessControlContext.init());
  }

  @Override
  public boolean isUserAuthorized(String userId, PublicationDetail pubDetail, final AccessControlContext context) {
    getDataManager(context).loadCachesWithLoadedPublication(pubDetail);
    return isUserAuthorized(userId, pubDetail.getPK(), context);
  }

  @Override
  public boolean isUserAuthorized(String userId, PublicationPK pubPk, final AccessControlContext context) {
    return isUserAuthorizedByContext(userId, pubPk, context, getUserRoles(userId, pubPk, context));
  }

  private boolean isUserAuthorizedByContext(String userId, PublicationPK pubPk,
      final AccessControlContext context, Set<SilverpeasRole> userRoles) {
    if (userRoles.isEmpty()) {
      // For now there is no point to perform the next verifications
      return false;
    }
    final Set<AccessControlOperation> operations = context.getOperations();
    if (isSearchActionFrom(operations)) {
      // For now, in case of search, visibility and status are checked before the use of
      // AccessController API. So it is a simple access case and there is no point to perform the
      // following processing.
      return true;
    }
    final SilverpeasRole safeHighestUserRole = getSafeSilverpeasRole(userRoles);
    final String instanceId = pubPk.getInstanceId();
    final boolean isCreationContext = !isNotCreationContext(pubPk.getId(), pubPk.getInstanceId());
    final DataManager dataManager = getDataManager(context);
    final PublicationDetail publicationDetail = isCreationContext
        ? null
        : dataManager.loadPublication(pubPk).getCurrentPublication();
    final Mutable<Boolean> authorized = Mutable.of(isCreationContext || publicationDetail != null);
    final BooleanSupplier canPublicationBePersistedOrDeleted = new MemoizedBooleanSupplier(
        () -> getComponentExtension(instanceId)
            .canPublicationBePersistedOrDeletedBy(publicationDetail, instanceId, userId,
                safeHighestUserRole, context));
    final Supplier<Optional<Location>> mainLocationSupplier = dataManager.getPublicationMainLocationSupplier(pubPk);
    // Checks
    if (isPublicationIntoTopicTrackerTrashFolder(context, instanceId, publicationDetail, mainLocationSupplier)) {
      // Trash case
      final ComponentAccessController.DataManager componentDataManager = ComponentAccessController.getDataManager(context);
      final boolean rightOnTopicsEnabled = componentDataManager.isRightOnTopicsEnabled(pubPk.getInstanceId());
      final boolean noSpecificRightsAndCanPutInTrash = !rightOnTopicsEnabled && canPublicationBePersistedOrDeleted.getAsBoolean();
      final boolean specificRightsAndAdminOrAuthorNotUser = rightOnTopicsEnabled
          && (safeHighestUserRole == SilverpeasRole.admin || publicationDetail.isPublicationEditor(userId));
      authorized.set(noSpecificRightsAndCanPutInTrash || specificRightsAndAdminOrAuthorNotUser);
    } else {
      // Verifying simple access
      authorized.filter(a -> a && publicationDetail != null && !canPublicationBePersistedOrDeleted.getAsBoolean())
                .ifPresent(a -> authorized.set(publicationDetail.isValid() && publicationDetail.isVisible()));
      // Verifying sharing
      authorized.filter(a -> a && isSharingActionFrom(operations))
                .ifPresent(a -> {
          final User user = User.getById(userId);
          final ComponentAccessController.DataManager componentDataManager = ComponentAccessController.getDataManager(context);
          authorized.set(!user.isAnonymous() && componentDataManager.isPublicationSharingEnabledForRole(instanceId, safeHighestUserRole));
        });
      // Verifying persist actions
      authorized.filter(a -> a && isPersistActionFrom(operations))
                .ifPresent(a -> authorized.set(canPublicationBePersistedOrDeleted.getAsBoolean()));
    }
    // Result
    return authorized.get();
  }

  private boolean isPublicationIntoTopicTrackerTrashFolder(final AccessControlContext context,
      final String instanceId, final PublicationDetail publicationDetail,
      final Supplier<Optional<Location>> mainLocationSupplier) {
    if (publicationDetail != null && isTopicTrackerSupported(instanceId, context)) {
      final Optional<Location> mainLocation = mainLocationSupplier.get();
      return mainLocation.isPresent() && NodePK.BIN_NODE_ID.equals(mainLocation.get().getId());
    }
    return false;
  }

  ComponentInstancePublicationAccessControlExtension getComponentExtension(final String instanceId) {
    return ComponentInstancePublicationAccessControlExtension.getByInstanceId(instanceId);
  }

  private SilverpeasRole getSafeSilverpeasRole(final Set<SilverpeasRole> userRoles) {
    SilverpeasRole safeHighestUserRole = SilverpeasRole.getHighestFrom(userRoles);
    if (safeHighestUserRole == null) {
      // Preventing from technical errors by using a role which is giving no authorization
      safeHighestUserRole = SilverpeasRole.reader;
    }
    return safeHighestUserRole;
  }

  @Override
  protected void fillUserRoles(Set<SilverpeasRole> userRoles, AccessControlContext context,
      String userId, PublicationPK publicationPK) {
    final String instanceId = publicationPK.getInstanceId();
    final String pubId = publicationPK.getId();
    final Set<SilverpeasRole> componentUserRoles = componentAccessController.getUserRoles(userId, instanceId, context);
    final boolean componentAccessAuthorized = componentAccessController.isUserAuthorized(componentUserRoles);
    if (isNotCreationContext(pubId, instanceId)) {
      final DataManager dataManager = getDataManager(context);
      final boolean needPubLoading = !isSearchActionFrom(dataManager.operations) && !dataManager.isLotOfDataMode();
      final PublicationDetail pubDetail = needPubLoading
          ? dataManager.loadPublication(publicationPK).getCurrentPublication()
          : null;
      if ((needPubLoading && pubDetail == null)
          || (isTopicTrackerSupported(instanceId, context)
              && fillTopicTrackerRoles(userRoles, context, userId, componentAccessAuthorized, publicationPK))) {
          return;
      }
    }
    if (componentAccessAuthorized) {
      userRoles.addAll(componentUserRoles);
    }
  }

  private boolean isTopicTrackerSupported(final String instanceId, final AccessControlContext context) {
    final ComponentAccessController.DataManager componentDataManager = ComponentAccessController.getDataManager(context);
    return componentDataManager.isTopicTrackerSupported(instanceId);
  }

  /**
   * Fills given userRoles with the given context.
   * @param userRoles the {@link Set} to fill.
   * @param context the context.
   * @param userId the identifier of the current user.
   * @param componentAccessAuthorized is component access authorized.
   * @param pubPK the publication primary key.
   * @return true if roles have been handled, false otherwise.
   */
  private boolean fillTopicTrackerRoles(final Set<SilverpeasRole> userRoles,
      final AccessControlContext context, final String userId,
      final boolean componentAccessAuthorized, final PublicationPK pubPK) {
    boolean rolesProcessed = false;
    if (!componentAccessAuthorized) {
      // Check if an alias of publication is authorized
      // (special treatment in case of the user has no access right on component instance)
      rolesProcessed = fillTopicTrackerAliasRoles(userRoles, context, userId, pubPK);
    } else {
      final ComponentAccessController.DataManager componentDataManager = ComponentAccessController.getDataManager(context);
      if (componentDataManager.isRightOnTopicsEnabled(pubPK.getInstanceId())) {
        // If rights are handled on folders, folder rights are checked !
        rolesProcessed = fillTopicTrackerNodeRoles(userRoles, context, userId, pubPK);
        if (rolesProcessed && CollectionUtil.isEmpty(userRoles)) {
          // if the publication is not on root node and if user has no rights on folder, check if
          // an alias of publication is authorized
          fillTopicTrackerAliasRoles(userRoles, context, userId, pubPK);
        }
      }
    }
    return rolesProcessed;
  }

  private boolean fillTopicTrackerNodeRoles(final Set<SilverpeasRole> userRoles,
      final AccessControlContext context, final String userId, final PublicationPK pubPK) {
    try {
      final Optional<Location> mainLocation = getDataManager(context).getPublicationMainLocationSupplier(pubPK).get();
      if (mainLocation.isPresent()) {
        if (pubPK.getInstanceId().equals(mainLocation.get().getInstanceId())) {
          // Checking on the instance hosting the publication
          final Set<SilverpeasRole> nodeUserRoles = nodeAccessController
              .getUserRoles(userId, mainLocation.get(), context);
          if (nodeAccessController.isUserAuthorized(nodeUserRoles)) {
            userRoles.addAll(nodeUserRoles);
          }
        } else {
          // Rights can not be verified from this main location as it is not hosted into the
          // component instance
          return true;
        }
      } else {
        // case of publications on root node (so component rights will be checked)
        return false;
      }
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex);
    }
    return true;
  }

  private boolean fillTopicTrackerAliasRoles(final Set<SilverpeasRole> userRoles,
      final AccessControlContext context, final String userId, final PublicationPK pubPk) {
    final Set<AccessControlOperation> operations = context.getOperations();
    if (!isPersistActionFrom(operations)) {
      try {
        final Collection<Location> locations = getDataManager(context).getAllPublicationAliases(pubPk);
        for (final Location location : locations) {
          final Set<SilverpeasRole> nodeUserRoles = nodeAccessController.getUserRoles(userId, location, context);
          if (nodeAccessController.isUserAuthorized(nodeUserRoles)) {
            // In case of alias, only user role is taken into account
            userRoles.add(SilverpeasRole.user);
            break;
          }
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
    return true;
  }

  private static boolean isNotCreationContext(final String pubId, final String instanceId) {
    return StringUtil.isInteger(pubId) && notAVolatileResource(pubId, instanceId);
  }

  private static boolean notAVolatileResource(final String pubId, final String instanceId) {
    try {
      return !getSessionVolatileResourceCacheService().contains(pubId, instanceId);
    } catch (Exception ignore) {
      // Case where rights are verified out of a user session (batch, wopi, etc.)
      return true;
    }
  }

  /**
   * Data manager.
   */
  static class DataManager {
    private final AccessControlContext context;
    private final Collection<AccessControlOperation> operations;
    private PublicationService publicationService;
    private MemoizedSupplier<List<Location>> allLocations = null;
    private MemoizedSupplier<Optional<Location>> lastMainLocation = null;
    private PublicationDetail lastPublicationDetail = null;
    boolean lotOfDataMode;
    List<PublicationPK> givenPublicationPks = null;
    Map<PublicationPK, PublicationDetail> publicationCache = null;
    Map<String, List<Location>> locationsByPublicationCache = null;

    DataManager(final AccessControlContext context) {
      this.context = context;
      this.operations = context.getOperations();
      this.lotOfDataMode = false;
      this.publicationService = PublicationService.get();
    }

    void loadCachesWithLoadedPublication(final PublicationDetail pub) {
      loadPublicationCacheByDetails(singletonList(pub));
    }

    DataManager loadCachesWithLoadedPublications(final String userId, final Collection<PublicationDetail> pubs) {
      loadPublicationCacheByDetails(pubs);
      loadCaches(userId, givenPublicationPks);
      return this;
    }

    DataManager loadCaches(final String userId, final Collection<PublicationPK> pks) {
      if (locationsByPublicationCache != null || pks.isEmpty()) {
        return this;
      }
      lotOfDataMode = true;
      loadPublicationCacheByPks(pks);
      final Set<String> instanceIds = givenPublicationPks.stream()
          .map(PublicationPK::getInstanceId)
          .collect(Collectors.toSet());
      final NodeAccessController.DataManager nodeDataManager = NodeAccessController.getDataManager(context);
      final Set<String> instanceIdsWithRightsOnTopic = nodeDataManager.loadCaches(userId, instanceIds);
      if (instanceIdsWithRightsOnTopic.isEmpty()) {
        locationsByPublicationCache = emptyMap();
      } else {
        final List<PublicationPK> pksForLocations = givenPublicationPks.stream()
            .filter(p -> instanceIdsWithRightsOnTopic.contains(p.getInstanceId()))
            .collect(Collectors.toList());
        locationsByPublicationCache = publicationService
            .getAllLocationsByPublicationIds(pksForLocations);
        final Set<String> additionalInstanceIdsToLoad = locationsByPublicationCache.entrySet().stream()
            .flatMap(e -> new ArrayList<>(e.getValue()).stream())
            .map(Location::getInstanceId)
            .filter(i -> ! instanceIds.contains(i))
            .collect(Collectors.toSet());
        if (!additionalInstanceIdsToLoad.isEmpty()) {
          nodeDataManager.completeCaches(userId, additionalInstanceIdsToLoad);
        }
      }
      return this;
    }

    private void loadPublicationCacheByPks(final Collection<PublicationPK> pks) {
      if (givenPublicationPks != null) {
        return;
      }
      givenPublicationPks = pks.stream().distinct().collect(Collectors.toList());
      if (isSearchActionFrom(operations)) {
        publicationCache = emptyMap();
      } else {
        final List<PublicationDetail> publications = publicationService.getMinimalDataByIds(givenPublicationPks);
        loadPublicationCloneCache(publications);
      }
    }

    private void loadPublicationCacheByDetails(final Collection<PublicationDetail> pubs) {
      if (givenPublicationPks != null) {
        return;
      }
      givenPublicationPks = pubs.stream().map(PublicationDetail::getPK).distinct().collect(Collectors.toList());
      if (isSearchActionFrom(operations)) {
        publicationCache = emptyMap();
      } else  {
        loadPublicationCloneCache(pubs);
      }
    }

    private void loadPublicationCloneCache(final Collection<PublicationDetail> publications) {
      final List<PublicationPK> masterPubPks = publications.stream()
          .filter(DataManager::isItAClone)
          .map(PublicationDetail::getClonePK)
          .collect(Collectors.toList());
      final List<PublicationDetail> masterPubs;
      if (masterPubPks.isEmpty()) {
        masterPubs = emptyList();
      } else {
        masterPubs = publicationService.getMinimalDataByIds(masterPubPks);
      }
      final int cacheSize = publications.size() + masterPubs.size();
      publicationCache = new HashMap<>(cacheSize);
      final Consumer<PublicationDetail> cacheSupplier = p -> publicationCache.put(p.getPK(), p);
      publications.forEach(cacheSupplier);
      masterPubs.forEach(cacheSupplier);
    }

    PublicationPK prepareCheckFor(final PublicationPK pubPK) {
      allLocations = null;
      lastMainLocation = null;
      lastPublicationDetail = null;
      return pubPK;
    }

    List<PublicationPK> getGivenPublicationPks() {
      return givenPublicationPks;
    }

    boolean isLotOfDataMode() {
      return lotOfDataMode;
    }

    Supplier<Optional<Location>> getPublicationMainLocationSupplier(final PublicationPK pk) {
      if (lastMainLocation == null) {
        lastMainLocation = new MemoizedSupplier<>(() -> getAllLocations(pk).get().stream()
            .filter(l -> !l.isAlias())
            .findFirst());
      }
      return lastMainLocation;
    }

    /**
     * Gets the aliases on the component instance referenced by the given {@link PublicationPK}.
     * @param pk the publication primary key.
     * @return a list of {@link Location} on the component instance.
     */
    List<Location> getAllPublicationAliases(final PublicationPK pk) {
      return getAllLocations(pk).get().stream()
          .filter(Location::isAlias)
          .collect(Collectors.toList());
    }

    private Supplier<List<Location>> getAllLocations(final PublicationPK pk) {
      if (allLocations == null) {
        allLocations = new MemoizedSupplier<>(() -> {
          if (locationsByPublicationCache != null) {
            return locationsByPublicationCache.getOrDefault(pk.getId(), emptyList());
          }
          return publicationService.getAllLocations(pk);
        });
      }
      return allLocations;
    }

    /**
     * Return the current loaded publication.
     * @return the {@link PublicationDetail} instance.
     */
    PublicationDetail getCurrentPublication() {
      return lastPublicationDetail;
    }

    /**
     * Return the publication. In case of a clone publication we need the cloned one (that is the
     * original publication).
     * <p>
     * Caching is handled.
     * </p>
     * @param pk the primary key of a publication.
     * @return the {@link PublicationDetail} instance.
     */
    DataManager loadPublication(final PublicationPK pk) {
      if(lastPublicationDetail == null || !lastPublicationDetail.getPK().equals(pk)) {
        PublicationDetail pubDetail = null;
        try {
          pubDetail = getPublicationData(pk);
          if (isItAClone(pubDetail)) {
            pubDetail = getPublicationData(pubDetail.getClonePK());
          }
        } catch (Exception e) {
          SilverLogger.getLogger(this).warn(e);
        }
        lastPublicationDetail = pubDetail;
      }
      return this;
    }

    private PublicationDetail getPublicationData(final PublicationPK pk) {
      if (publicationCache != null) {
        return publicationCache.get(pk);
      }
      return publicationService.getMinimalDataByIds(singleton(pk)).stream().findFirst().orElse(null);
    }

    private static boolean isItAClone(final PublicationDetail pubDetail) {
      return pubDetail != null && !pubDetail.isValid() && pubDetail.haveGotClone() && pubDetail.isClone();
    }
  }
}