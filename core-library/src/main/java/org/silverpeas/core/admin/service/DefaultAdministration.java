/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.admin.service;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.silverpeas.core.admin.ProfiledObjectId;
import org.silverpeas.core.admin.ProfiledObjectIds;
import org.silverpeas.core.admin.ProfiledObjectType;
import org.silverpeas.core.admin.RightProfile;
import org.silverpeas.core.admin.component.ApplicationResourcePasting;
import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.admin.component.ComponentInstancePostConstruction;
import org.silverpeas.core.admin.component.ComponentInstancePreDestruction;
import org.silverpeas.core.admin.component.WAComponentRegistry;
import org.silverpeas.core.admin.component.model.*;
import org.silverpeas.core.admin.domain.DomainDriver;
import org.silverpeas.core.admin.domain.DomainDriverManager;
import org.silverpeas.core.admin.domain.driver.ldapdriver.LDAPSynchroUserItf;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.domain.model.DomainCache;
import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.admin.domain.synchro.SynchroDomainReport;
import org.silverpeas.core.admin.domain.synchro.SynchroDomainScheduler;
import org.silverpeas.core.admin.domain.synchro.SynchroGroupReport;
import org.silverpeas.core.admin.domain.synchro.SynchroGroupScheduler;
import org.silverpeas.core.admin.persistence.AdminPersistenceException;
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.admin.quota.model.Quota;
import org.silverpeas.core.admin.service.cache.AdminCache;
import org.silverpeas.core.admin.service.cache.TreeCache;
import org.silverpeas.core.admin.space.SpaceI18N;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.space.SpaceProfileInst;
import org.silverpeas.core.admin.space.SpaceProfileInstManager;
import org.silverpeas.core.admin.space.SpaceServiceProvider;
import org.silverpeas.core.admin.space.model.Space;
import org.silverpeas.core.admin.space.notification.SpaceEventNotifier;
import org.silverpeas.core.admin.space.quota.ComponentSpaceQuotaKey;
import org.silverpeas.core.admin.space.quota.DataStorageSpaceQuotaKey;
import org.silverpeas.core.admin.user.GroupManager;
import org.silverpeas.core.admin.user.ProfileInstManager;
import org.silverpeas.core.admin.user.ProfiledObjectManager;
import org.silverpeas.core.admin.user.UserIndexation;
import org.silverpeas.core.admin.user.UserManager;
import org.silverpeas.core.admin.user.constant.GroupState;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.*;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.backgroundprocess.AbstractBackgroundProcessRequest;
import org.silverpeas.core.backgroundprocess.BackgroundProcessTask;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagementEngine;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.Process;
import org.silverpeas.core.util.*;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.logging.Level;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.silverpeas.core.SilverpeasExceptionMessages.*;
import static org.silverpeas.core.admin.domain.DomainDriver.ActionConstants.ACTION_MASK_MIXED_GROUPS;
import static org.silverpeas.core.admin.service.DefaultAdministration.CheckoutGroupDescriptor.synchronizingDomainFrom;
import static org.silverpeas.core.admin.service.DefaultAdministration.CheckoutGroupDescriptor.synchronizingOneGroupWithSuperGroupId;
import static org.silverpeas.core.util.ArrayUtil.contains;
import static org.silverpeas.core.util.CollectionUtil.intersection;
import static org.silverpeas.core.util.StringUtil.*;

/**
 * The class Admin is the main class of the Administrator.
 * <p>
 * The role of the administrator is to create and maintain user domains, spaces, component
 * instances, and the user/group roles on that spaces and component instances.
 * </p>
 */
@Service
@Singleton
@Transactional(rollbackOn = AdminException.class)
class DefaultAdministration implements Administration {

  /**
   * The unique identifier of the main administrator (root) in Silverpeas. It is hard configured in
   * Silverpeas.
   */
  private static final String ADMIN_ID = "0";

  /**
   * Text to pass in the exception's messages
   */
  private static final String REMOVE_OF = "Suppression de ";
  private static final String INDEX_SPACE_SCOPE = "Spaces";
  private static final String INDEX_COMPONENT_SCOPE = "Components";
  private static final String SPACE = "space";
  private static final String SUBSPACES_OF_SPACE = "subspaces of space ";
  private static final String COMPONENT = "component";
  private static final String PROFILE = "profile";
  private static final String SPACE_PROFILE = "space profile";
  private static final String GROUP = "group";
  private static final String ACCESSIBLE_BY_USER = "accessible by user ";
  private static final String USER = "user ";
  private static final String IN_GROUP = "in group ";
  private static final String GROUP_PROFILE = "group profile";
  private static final String DOMAIN_ID_PARAM = "domainId";
  private static final String LOGIN_PARAM = "login";
  private static final String DOMAIN = "domain";
  private static final String COMPONENTS_IN_SPACE = "components in space ";
  private static final String AVAILABLE_TO_USER = "available to user ";
  private static final String ADMIN_SYNCHRONIZE_GROUP = "admin.synchronizeGroup";
  private static final String ADMIN_SYNCHRONIZE_DOMAIN = "admin.synchronizeSilverpeasWithDomain";
  private static final String ADMIN_SYNCHRONIZE_USERS = "admin.synchronizeUsers";
  private static final String ADMIN_SYNCHRONIZE_GROUPS = "admin.synchronizeGroups";
  private static final String ADMIN_SYNCHRONIZE_CHECK_OUT_GROUPS = "admin.checkOutGroups";
  private static final String ID_IS = " (id:";

  // Divers
  private final Object semaphore = new Object();
  private final SynchroGroupScheduler groupSynchroScheduler = new SynchroGroupScheduler();
  private boolean delUsersOnDiffSynchro = true;
  private boolean shouldFallbackGroupNames = true;
  private boolean shouldFallbackUserLogins = false;
  private String groupSynchroCron = "";
  private String domainSynchroCron = "";
  private String senderEmail = null;
  private String senderName = null;
  private SynchroDomainScheduler domainSynchroScheduler = null;
  private AppRoleMappingManager roleMapping = null;
  private boolean useProfileInheritance = false;

  @Inject
  private AdminCache cache;
  @Inject
  private WAComponentRegistry componentRegistry;
  @Inject
  private UserManager userManager;
  @Inject
  private GroupManager groupManager;
  @Inject
  private SpaceInstManager spaceManager;
  @Inject
  private ProfiledObjectManager profiledObjectManager;
  @Inject
  private ProfileInstManager profileManager;
  @Inject
  private GroupProfileInstManager groupProfileManager;
  @Inject
  private ComponentInstManager componentManager;
  @Inject
  private SpaceProfileInstManager spaceProfileManager;
  @Inject
  private SpaceEventNotifier spaceEventNotifier;
  @Inject
  private ContentManagementEngine contentMgtEngine;
  @Inject
  private DomainDriverManager domainDriverManager;
  @Inject
  private DomainCache domainCache;
  @Inject
  private TreeCache treeCache;
  @Inject
  private GroupCache groupCache;

  private void setup() {
    // Load silverpeas admin resources
    SettingBundle resources = ResourceLocator.getSettingBundle("org.silverpeas.admin.admin");
    roleMapping = new AppRoleMappingManager(componentRegistry);
    useProfileInheritance = resources.getBoolean("UseProfileInheritance", false);
    senderEmail = resources.getString("SenderEmail");
    senderName = resources.getString("SenderName");
    shouldFallbackGroupNames = resources.getBoolean("FallbackGroupNames", true);
    shouldFallbackUserLogins = resources.getBoolean("FallbackUserLogins", false);
    domainSynchroCron = resources.getString("DomainSynchroCron", "* 4 * * *");
    groupSynchroCron = resources.getString("GroupSynchroCron", "* 5 * * *");
    delUsersOnDiffSynchro = resources.getBoolean("DelUsersOnThreadedSynchro", true);
    // Cache management
    cache.setCacheAvailable(StringUtil.getBooleanValue(resources.getString("UseCache", "1")));
  }

  protected DefaultAdministration() {
    // Hidden constructor
  }

  @PostConstruct
  private void initialize() {
    setup();
    Transaction.performInOne(() -> {
      this.reloadCache();
      return null;
    });
  }

  @Override
  public void reloadCache() {
    cache.resetCache();
    treeCache.clearCache();
    groupCache.clearCache();
    try {

      List<SpaceInstLight> spaces = spaceManager.getAllSpaces();
      for (SpaceInstLight space : spaces) {
        addSpaceInTreeCache(space, false);
      }

    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  @Override
  public void initSynchronization() {
    // init synchronization of domains
    List<String> synchroDomainIds = new ArrayList<>();
    Domain[] domains = null;
    try {
      domains = domainDriverManager.getAllDomains();
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e);
    }
    if (domains != null) {
      for (Domain domain : domains) {
        DomainDriver synchroDomain;
        try {
          synchroDomain = domainDriverManager.getDomainDriver(domain.getId());
          if (synchroDomain != null && synchroDomain.isSynchroThreaded()) {
            synchroDomainIds.add(domain.getId());
          }
        } catch (Exception e) {
          SilverLogger.getLogger(this).error(e);
        }
      }
    }
    domainSynchroScheduler = new SynchroDomainScheduler();
    domainSynchroScheduler.initialize(domainSynchroCron, synchroDomainIds);

    // init synchronization of groups
    List<GroupDetail> groups = null;
    try {
      groups = getSynchronizedGroups();
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e);
    }
    if (groups != null) {
      List<String> synchronizedGroupIds = groups.stream()
          .filter(GroupDetail::isSynchronized)
          .map(GroupDetail::getId)
          .collect(toList());
      groupSynchroScheduler.initialize(groupSynchroCron, synchronizedGroupIds);
    }
  }

  private void addSpaceInTreeCache(SpaceInstLight space, boolean addSpaceToSuperSpace)
      throws AdminException {
    Space spaceInCache = new Space();
    spaceInCache.setSpaceInstLight(space);
    List<ComponentInstLight> components = componentManager.getComponentsInSpace(space.getLocalId());
    spaceInCache.setComponents(components);

    List<SpaceInstLight> subSpaces = getSubSpaces(space.getId());

    spaceInCache.setSubspaces(subSpaces);
    treeCache.addSpace(space.getLocalId(), spaceInCache);

    for (SpaceInstLight subSpace : subSpaces) {
      addSpaceInTreeCache(subSpace, false);
    }

    if (addSpaceToSuperSpace && !space.isRoot()) {
      treeCache.addSubSpace(Integer.parseInt(space.getFatherId()), space);
    }
  }

  // -------------------------------------------------------------------------
  // SPACE RELATED FUNCTIONS
  // -------------------------------------------------------------------------

  @Override
  public void createSpaceIndex(int spaceId) {
    try {
      SpaceInstLight space = getSpaceInstLight(spaceId);
      createSpaceIndex(space);
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  @Override
  public void createSpaceIndex(SpaceInstLight spaceInst) {
    // Index the space
    String spaceId = spaceInst.getId();
    FullIndexEntry indexEntry = new FullIndexEntry(INDEX_SPACE_SCOPE, "Space", spaceId);
    // index all translations
    Map<String, SpaceI18N> translations = spaceInst.getTranslations();
    for (Map.Entry<String, SpaceI18N> translation : translations.entrySet()) {
      indexEntry.setTitle(translation.getValue().getName(), translation.getKey());
      indexEntry.setPreview(translation.getValue().getDescription(), translation.getKey());
    }
    indexEntry.setCreationUser(String.valueOf(spaceInst.getCreatedBy()));
    indexEntry.setCreationDate(spaceInst.getCreationDate());
    indexEntry.setLastModificationUser(String.valueOf(spaceInst.getUpdatedBy()));
    indexEntry.setLastModificationDate(spaceInst.getLastUpdateDate());
    IndexEngineProxy.addIndexEntry(indexEntry);
  }

  @Override
  public void deleteSpaceIndex(SpaceInst spaceInst) {
    String spaceId = spaceInst.getId();
    FullIndexEntry indexEntry = new FullIndexEntry(INDEX_SPACE_SCOPE, "Space", spaceId);
    IndexEngineProxy.removeIndexEntry(indexEntry.getPK());
  }

  @Override
  public void deleteAllSpaceIndexes() {
    IndexEngineProxy.removeScopedIndexEntries(INDEX_SPACE_SCOPE);
  }

  @Override
  public String addSpaceInst(String userId, SpaceInst spaceInst) throws AdminException {
    try {
      if (!spaceInst.isRoot()) {
        // It's a subspace
        // Convert the client id in driver id
        int localId = getDriverSpaceId(spaceInst.getDomainFatherId());
        spaceInst.setDomainFatherId(String.valueOf(localId));
        if (useProfileInheritance && !spaceInst.isInheritanceBlocked()) {
          // inherit profiles from super space
          // set super space profiles to new space
          setSpaceProfilesToSubSpace(spaceInst, null);
        }
      }
      // Create the space instance
      spaceInst.setCreatorUserId(userId);
      spaceManager.createSpaceInst(spaceInst);
      // put new space in cache
      cache.opAddSpace(getSpaceInstById(spaceInst.getLocalId()));

      // Instantiate the components
      List<ComponentInst> alCompoInst = spaceInst.getAllComponentsInst();
      for (ComponentInst componentInst : alCompoInst) {
        componentInst.setDomainFatherId(spaceInst.getId());
        addComponentInst(userId, componentInst);
      }

      SpaceInstLight space = getSpaceInstLight(spaceInst.getLocalId());
      addSpaceInTreeCache(space, true);

      // indexation of the space

      createSpaceIndex(space);

      return spaceInst.getId();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
      try {
        cache.resetCache();
      } catch (Exception e1) {
        SilverLogger.getLogger(this).error(e1);
      }
      throw new AdminException(failureOnAdding(SPACE, spaceInst.getName()), e);
    }
  }

  @Override
  public String deleteSpaceInstById(String userId, String spaceId, boolean definitive)
      throws AdminException {
    try {
      // Convert the client id in driver id
      int driverSpaceId = getDriverSpaceId(spaceId);

      // Get the space to delete
      SpaceInst spaceInst = getSpaceInstById(driverSpaceId);

      if (!definitive) {
        // Update the space in tables
        spaceManager.sendSpaceToBasket(spaceInst, userId);

        // delete all profiles (space, components and subspaces)
        deleteSpaceProfilesOnSpaceLogicalDeletion(spaceInst);

        // notify logical deletion
        notifyOnSpaceLogicalDeletion(spaceId);
      } else {
        deleteEffectivelySpaceInst(spaceInst, spaceId, driverSpaceId, userId);
      }

      cache.opRemoveSpace(spaceInst);
      treeCache.removeSpace(driverSpaceId);
      // unindex the space
      deleteSpaceIndex(spaceInst);
      return spaceId;
    } catch (Exception e) {
      throw new AdminException(failureOnDeleting(SPACE, spaceId), e);
    }
  }

  private void deleteEffectivelySpaceInst(final SpaceInst spaceInst, final String spaceId,
      final int driverSpaceId, final String userId) throws AdminException {
    // Get all the sub-spaces
    String[] subSpaceIds = getAllSubSpaceIdsWithoutCache(spaceId);

    // Delete subspaces
    for (String subSpaceId : subSpaceIds) {
      deleteSpaceInstById(userId, subSpaceId, true);
    }

    // Delete subspaces already in bin
    List<SpaceInstLight> removedSpaces = getRemovedSpaces();
    for (SpaceInstLight removedSpace : removedSpaces) {
      if (String.valueOf(driverSpaceId).equals(removedSpace.getFatherId())) {
        deleteSpaceInstById(userId, removedSpace.getId(), true);
      }
    }

    // delete the space profiles instance
    for (int nI = 0; nI < spaceInst.getNumSpaceProfileInst(); nI++) {
      deleteSpaceProfileInst(spaceInst.getSpaceProfileInst(nI).getId());
    }

    // Delete the components
    List<ComponentInst> alCompoInst = spaceInst.getAllComponentsInst();
    for (ComponentInst anAlCompoInst : alCompoInst) {
      deleteComponentInst(userId, getClientComponentId(anAlCompoInst), true);
    }

    // Delete the components already in bin
    List<ComponentInstLight> removedComponents = getRemovedComponents();
    for (ComponentInstLight removedComponent : removedComponents) {
      if (spaceId.equals(removedComponent.getDomainFatherId())) {
        deleteComponentInst(userId, removedComponent.getId(), true);
      }
    }
    // Delete the space in tables
    spaceManager.deleteSpaceInst(spaceInst);
  }

  private void notifyOnSpaceLogicalDeletion(String spaceId) throws AdminException {
    // notify of space logical deletion
    SpaceInst spaceInst = getSpaceInstById(spaceId);
    spaceEventNotifier.notifyEventOn(ResourceEvent.Type.REMOVING, spaceInst, spaceInst);

    // notify of direct sub spaces logical deletion too
    List<SpaceInstLight> spaces = treeCache.getSubSpaces(getDriverSpaceId(spaceId));
    for (SpaceInstLight space : spaces) {
      notifyOnSpaceLogicalDeletion(space.getId());
    }
  }

  private void deleteSpaceProfilesOnSpaceLogicalDeletion(SpaceInst spaceInst)
      throws AdminException {
    // delete the space profiles
    for (int nI = 0; nI < spaceInst.getNumSpaceProfileInst(); nI++) {
      deleteSpaceProfileInst(spaceInst.getSpaceProfileInst(nI).getId());
    }

    // delete the components profiles
    List<ComponentInst> components = spaceInst.getAllComponentsInst();
    for (ComponentInst component : components) {
      for (int p = 0; p < component.getNumProfileInst(); p++) {
        deleteProfileInst(component.getProfileInst(p).getId());
      }
    }

    // delete the subspace profiles
    List<SpaceInst> subSpaces = spaceInst.getSubSpaces();
    for (SpaceInst subSpace : subSpaces) {
      deleteSpaceProfilesOnSpaceLogicalDeletion(subSpace);
    }
  }

  @Override
  public void restoreSpaceFromBasket(String spaceId) throws AdminException {
    try {
      // Convert the client id in driver id
      int driverSpaceId = getDriverSpaceId(spaceId);
      // update data in database
      spaceManager.removeSpaceFromBasket(driverSpaceId);

      // force caches to be refreshed
      cache.removeSpaceInst(driverSpaceId);
      treeCache.removeSpace(driverSpaceId);
      // restore space in global tree
      addSpaceInTreeCache(getSpaceInstLight(driverSpaceId), true);

      // Get the space and put it in the cache
      SpaceInst spaceInst = getSpaceInstById(driverSpaceId);
      // set super space profiles to space
      if (useProfileInheritance && !spaceInst.isInheritanceBlocked() && !spaceInst.isRoot()) {
        updateSpaceInheritance(spaceInst, false);
      }
      // index the space
      createSpaceIndex(driverSpaceId);
      // reset space and eventually subspace
      cache.opAddSpace(spaceInst);
    } catch (Exception e) {
      throw new AdminException(failureOnRestoring(SPACE, spaceId), e);
    }
  }

  @Override
  public SpaceInst getSpaceInstById(String spaceId) throws AdminException {
    try {
      return getSpaceInstById(getDriverSpaceId(spaceId));
    } catch (Exception e) {
      throw new AdminException(failureOnGetting(SPACE, spaceId), e);
    }
  }

  /**
   * Get the space instance with the given space id
   * @param spaceId client space id
   * @return Space information as SpaceInst object
   */
  private SpaceInst getSpaceInstById(int spaceId) throws AdminException {
    try {
      final SpaceInst spaceInst;
      Optional<SpaceInst> optionalSpaceInst = cache.getSpaceInst(spaceId);
      if (optionalSpaceInst.isEmpty()) {
        // Get space instance
        spaceInst = spaceManager.getSpaceInstById(spaceId);
        if (spaceInst != null) {
          // Store the spaceInst in cache
          cache.putSpaceInst(spaceInst);
        }
      } else {
        spaceInst = optionalSpaceInst.get();
      }
      return spaceManager.copy(spaceInst);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting(SPACE, String.valueOf(spaceId)), e);
    }
  }

  @Override
  public SpaceInst getPersonalSpace(String userId) throws AdminException {
    return spaceManager.getPersonalSpace(userId);
  }

  @Override
  public String[] getAllSubSpaceIds(String domainFatherId) throws AdminException {
    try {
      int spaceId = getDriverSpaceId(domainFatherId);
      if (treeCache.isSpacePresent(spaceId)) {
        return treeCache.getSubSpaces(getDriverSpaceId(domainFatherId))
            .stream()
            .map(SpaceInstLight::getId)
            .toArray(String[]::new);
      } else {
        return getAllSubSpaceIdsWithoutCache(domainFatherId);
      }
    } catch (Exception e) {
      throw new AdminException(failureOnGetting(SUBSPACES_OF_SPACE, domainFatherId), e);
    }
  }

  private String[] getAllSubSpaceIdsWithoutCache(String domainFatherId) throws AdminException {
    try {
      // get all space children ids
      String[] asDriverSpaceIds = spaceManager.getAllSubSpaceIds(getDriverSpaceId(domainFatherId));
      // Convert all the driver space ids in client space ids
      return getClientSpaceIds(asDriverSpaceIds);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting(SUBSPACES_OF_SPACE, domainFatherId), e);
    }
  }

  @Override
  public String updateSpaceInst(SpaceInst spaceInstNew) throws AdminException {
    try {
      SpaceInst oldSpace = getSpaceInstById(spaceInstNew.getId());
      // Update the space in tables
      spaceManager.updateSpaceInst(spaceInstNew);
      if (useProfileInheritance && (oldSpace.isInheritanceBlocked() != spaceInstNew.
          isInheritanceBlocked())) {
        updateSpaceInheritance(oldSpace, spaceInstNew.isInheritanceBlocked());
      }
      cache.opUpdateSpace(spaceInstNew);
      Optional<SpaceInstLight> spaceInCache =
          treeCache.getSpaceInstLight(spaceInstNew.getLocalId());
      spaceInCache.ifPresent(s -> s.setInheritanceBlocked(spaceInstNew.isInheritanceBlocked()));
      // Update space in TreeCache
      SpaceInstLight spaceLight =
          spaceManager.getSpaceInstLightById(getDriverSpaceId(spaceInstNew.getId()));
      spaceLight.setInheritanceBlocked(spaceInstNew.isInheritanceBlocked());
      treeCache.updateSpace(spaceLight);

      // indexation of the space

      createSpaceIndex(spaceLight);
      return spaceInstNew.getId();
    } catch (Exception e) {
      throw new AdminException(failureOnUpdate(SPACE, spaceInstNew.getId()), e);
    }
  }

  @Override
  public void updateSpaceOrderNum(String spaceId, int orderNum) throws AdminException {
    try {
      int driverSpaceId = getDriverSpaceId(spaceId);
      // Update the space in tables
      spaceManager.updateSpaceOrder(driverSpaceId, orderNum);
      cache.opUpdateSpace(spaceManager.getSpaceInstById(driverSpaceId));

      // Update space order
      Optional<SpaceInstLight> optionalSpace = treeCache.getSpaceInstLight(driverSpaceId);
      // the space is null if it was just deleted while the update of the ranking concerns one of
      // its sibling
      if (optionalSpace.isPresent()) {
        final SpaceInstLight space = optionalSpace.get();
        space.setOrderNum(orderNum);
        if (!space.isRoot()) {
          // Update brothers sort in TreeCache
          treeCache.setSubspaces(getDriverSpaceId(space.getFatherId()),
              getSubSpaces(space.getFatherId()));
        }
      }
    } catch (Exception e) {
      throw new AdminException(failureOnUpdate(SPACE, spaceId), e);
    }
  }

  /**
   * Update the inheritance mode between a subSpace and its space. If inheritanceBlocked is true
   * then all inherited space profiles are removed. If inheritanceBlocked is false then all subSpace
   * profiles are removed and space profiles are inherited.
   * @param space a space instance
   * @param inheritanceBlocked is inheritance is blocked?
   * @throws AdminException if an error occurs
   */
  private void updateSpaceInheritance(SpaceInst space, boolean inheritanceBlocked)
      throws AdminException {
    try {
      final SpaceInst parentSpace;
      if (inheritanceBlocked) {
        // 1 - delete the rights inherited from the space
        List<SpaceProfileInst> inheritedProfiles = space.getInheritedProfiles();
        for (SpaceProfileInst profile : inheritedProfiles) {
          deleteSpaceProfileInst(profile.getId());
        }
        // Parent space is itself
        parentSpace = getSpaceInstById(space.getId());
      } else {
        // Inheritance of the space rights
        // 1 - delete the rights specific to the child space
        List<SpaceProfileInst> profiles = space.getProfiles();
        for (SpaceProfileInst profile : profiles) {
          if (profile != null && !profile.isManager()) {
            deleteSpaceProfileInst(profile.getId());
          }
        }
        // Parent space will be retrieved automatically
        parentSpace = null;
      }
      if (!space.isRoot()) {
        // 2 - setting of the rights from the parent space to the child space
        setSpaceProfilesToSubSpace(space, parentSpace, true);
      }
    } catch (AdminException e) {
      throw new AdminException(failureOnUpdate(SPACE, space.getId()), e);
    }
  }

  @Override
  public boolean isSpaceInstExist(String spaceId) throws AdminException {
    try {
      return spaceManager.isSpaceInstExist(getDriverSpaceId(spaceId));
    } catch (AdminException e) {
      throw new AdminException(e.getMessage(), e);
    }
  }

  @Override
  public String[] getAllRootSpaceIds() throws AdminException {
    try {
      String[] driverSpaceIds = spaceManager.getAllRootSpaceIds();
      // Convert all the driver space ids in client space ids
      driverSpaceIds = getClientSpaceIds(driverSpaceIds);
      return driverSpaceIds;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("root spaces", ""), e);
    }
  }

  @Override
  public List<SpaceInstLight> getPathToComponent(String componentId) throws AdminException {
    List<SpaceInstLight> path = new ArrayList<>();
    ComponentInst component = getComponentInst(componentId);
    if (component != null) {
      String spaceId = component.getDomainFatherId();
      return getPathToSpace(spaceId, true);
    }
    return path;
  }

  @Override
  public List<SpaceInstLight> getPathToSpace(String spaceId, boolean includeTarget)
      throws AdminException {
    List<SpaceInstLight> path = new ArrayList<>(10);
    SpaceInstLight space = getSpaceInstLight(getDriverSpaceId(spaceId));
    if (space != null) {
      if (includeTarget) {
        path.add(0, space);
      }
      while (space != null && !space.isRoot()) {
        String fatherId = space.getFatherId();
        space = getSpaceInstLight(getDriverSpaceId(fatherId));
        path.add(0, space);
      }
    }
    return path;
  }

  @Override
  public String[] getAllSpaceIds() throws AdminException {
    try {
      String[] driverSpaceIds = spaceManager.getAllSpaceIds();
      // Convert all the driver space ids in client space ids
      driverSpaceIds = getClientSpaceIds(driverSpaceIds);
      return driverSpaceIds;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("all spaces", ""), e);
    }
  }

  @Override
  public List<SpaceInstLight> getRemovedSpaces() throws AdminException {
    try {
      return spaceManager.getRemovedSpaces();
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("all removed spaces", ""), e);
    }
  }

  @Override
  public List<ComponentInstLight> getRemovedComponents() throws AdminException {
    try {
      return componentManager.getRemovedComponents();
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("all removed components", ""), e);
    }
  }

  @Override
  public String[] getSpaceNames(String[] asClientSpaceIds) throws AdminException {
    if (asClientSpaceIds == null) {
      return ArrayUtil.emptyStringArray();
    }
    try {
      String[] asSpaceNames = new String[asClientSpaceIds.length];
      for (int nI = 0; nI < asClientSpaceIds.length; nI++) {
        SpaceInstLight spaceInst = getSpaceInstLightById(asClientSpaceIds[nI]);
        asSpaceNames[nI] = spaceInst.getName();
      }
      return asSpaceNames;
    } catch (Exception e) {
      throw new AdminException(
          failureOnGetting("space names of", String.join(", ", asClientSpaceIds)), e);
    }
  }

  // -------------------------------------------------------------------------
  // COMPONENT RELATED FUNCTIONS
  // -------------------------------------------------------------------------

  @Override
  public Map<String, WAComponent> getAllWAComponents() {
    return componentRegistry.getAllWAComponents();
  }

  @Override
  public SilverpeasComponentInstance getComponentInstance(final String componentInstanceIdentifier)
      throws AdminException {
    final SilverpeasComponentInstance instance;
    try {
      final Optional<PersonalComponentInstance> personalComponentInstance =
          PersonalComponentInstance.from(componentInstanceIdentifier);
      if (personalComponentInstance.isPresent()) {
        instance = personalComponentInstance.get();
      } else {
        final Optional<ToolInstance> toolInstance = ToolInstance.from(componentInstanceIdentifier);
        if (toolInstance.isPresent()) {
          instance = toolInstance.get();
        } else {
          final SilverpeasComponentInstance componentInstance =
              getComponentInst(componentInstanceIdentifier);
          instance = "-1".equals(componentInstance.getId()) ? null : componentInstance;
        }
      }
      return instance;
    } catch (Exception e) {
      throw new AdminException(
          failureOnGetting("Silverpeas component", componentInstanceIdentifier), e);
    }
  }

  @Override
  public ComponentInst getComponentInst(String sClientComponentId) throws AdminException {
    try {
      ComponentInst componentInst =
          getComponentInst(getDriverComponentId(sClientComponentId), null);
      componentInst =
          checkComponentInstanceById(componentInst, sClientComponentId, nullComponentInstSupplier);
      Objects.requireNonNull(componentInst);
      componentInst.setDomainFatherId(getClientSpaceId(componentInst.getDomainFatherId()));
      return componentInst;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting(COMPONENT, sClientComponentId), e);
    }
  }

  @Override
  public ComponentInstLight getComponentInstLight(String componentId) throws AdminException {
    try {
      final int driverComponentId = getDriverComponentId(componentId);
      final ComponentInstLight instance = componentManager.getComponentInstLight(driverComponentId);
      return checkComponentInstanceById(instance, componentId, null);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting(COMPONENT, componentId), e);
    }
  }

  private <T extends SilverpeasComponentInstance> T checkComponentInstanceById(
      final T componentInstance, final String componentId,
      final Supplier<T> nullComponentInstance) {
    if (componentInstance != null) {
      if (componentInstance.getId().equals(componentId)
          || "-1".equals(componentInstance.getId())
          || isLong(componentId)) {
        return componentInstance;
      }
      SilverLogger.getLogger(this).error("{0}. Wrong component {1} has been found!!",
          failureOnGetting(COMPONENT, componentId), componentInstance.getId());
      return nullComponentInstance != null ? nullComponentInstance.get() : null;
    }
    return null;
  }

  private final Supplier<ComponentInst> nullComponentInstSupplier = () -> {
    try {
      return getComponentInst(-1, -1);
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e);
      return null;
    }
  };

  private ComponentInst getComponentInst(int componentId, Integer fatherDriverSpaceId)
      throws AdminException {
    try {
      // Get the component instance
      Optional<ComponentInst> optionalInstance = cache.getComponentInst(componentId);
      final ComponentInst componentInst;
      if (optionalInstance.isEmpty()) {
        // Get component instance from database
        componentInst = componentManager.getComponentInst(componentId, fatherDriverSpaceId);
        // Store component instance in cache
        cache.putComponentInst(componentInst);
      } else {
        componentInst = optionalInstance.get();
      }
      return componentManager.copy(componentInst);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting(COMPONENT, String.valueOf(componentId)), e);
    }
  }

  @Override
  public List<Parameter> getComponentParameters(String componentId) {
    try {
      return componentManager.getParameters(getDriverComponentId(componentId));
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
      return Collections.emptyList();
    }
  }

  @Override
  public String getComponentParameterValue(String componentId, String parameterName) {
    try {
      return componentManager.getParameterValueByComponentAndParamName(
          getDriverComponentId(componentId), parameterName.toLowerCase(), true);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
    return StringUtil.emptyString();
  }

  @Override
  public Map<String, Map<String, String>> getParameterValuesByComponentIdThenByParamName(
      final Collection<String> componentIds, final Collection<String> paramNames) {
    final Map<Integer, String> localComponentIds = new HashMap<>(componentIds.size());
    componentIds.forEach(i -> localComponentIds.put(getDriverComponentId(i), i));
    final Map<String, Map<String, String>> result = new HashMap<>(componentIds.size());
    try {
      componentManager
          .getParameterValuesByComponentIdThenByParamName(localComponentIds.keySet(), paramNames)
          .forEach((k, v) -> result.put(localComponentIds.get(k), v));
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
    return result;
  }

  @Override
  public void restoreComponentFromBasket(String componentId) throws AdminException {
    try {
      // update data in database
      componentManager.restoreComponentFromBasket(getDriverComponentId(componentId));

      // Get the component and put it in the cache
      ComponentInst componentInst = getComponentInst(componentId);

      if (useProfileInheritance && !componentInst.isInheritanceBlocked()) {
        // inherits profiles from space
        setSpaceProfilesToComponent(componentInst, null);
      }
      cache.opUpdateComponent(componentInst);
      ComponentInstLight component = getComponentInstLight(componentId);
      treeCache.addComponent(component, getDriverSpaceId(component.getDomainFatherId()));
      createComponentIndex(component);
    } catch (Exception e) {
      throw new AdminException(failureOnRestoring(COMPONENT, componentId));
    }
  }

  @Override
  public void createComponentIndex(String componentId) {
    try {
      final SilverpeasComponentInstance componentInstance = getComponentInstance(componentId);
      if (!componentInstance.isPersonal()) {
        createComponentIndex(componentInstance);
      }
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  @Override
  public void createComponentIndex(SilverpeasComponentInstance instance) {
    if (instance != null) {
      // Index the component
      String componentId = instance.getId();
      FullIndexEntry indexEntry =
          new FullIndexEntry(INDEX_COMPONENT_SCOPE, "Component", componentId);

      if (instance instanceof ComponentInst) {
        setIndexEntry((ComponentInst) instance, indexEntry);
      } else if (instance instanceof ComponentInstLight) {
        setIndexEntry((ComponentInstLight) instance, indexEntry);
      }

      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  private void setIndexEntry(ComponentInst componentInst, FullIndexEntry indexEntry) {
    setIndexEntryTranslations(componentInst.getTranslations(), indexEntry);
    indexEntry.setCreationUser(componentInst.getCreatorUserId());
    indexEntry.setCreationDate(componentInst.getCreationDate());
    indexEntry.setLastModificationUser(componentInst.getUpdaterUserId());
    indexEntry.setLastModificationDate(componentInst.getLastUpdateDate());
  }

  private void setIndexEntry(ComponentInstLight componentInstLight, FullIndexEntry indexEntry) {
    setIndexEntryTranslations(componentInstLight.getTranslations(), indexEntry);
    indexEntry.setCreationUser(Integer.toString(componentInstLight.getCreatedBy()));
    indexEntry.setCreationDate(componentInstLight.getCreationDate());
    indexEntry.setLastModificationUser(String.valueOf(componentInstLight.getUpdatedBy()));
    indexEntry.setLastModificationDate(componentInstLight.getLastUpdateDate());
  }

  private void setIndexEntryTranslations(Map<String, ComponentI18N> translations,
      FullIndexEntry indexEntry) {
    for (Map.Entry<String, ComponentI18N> translation : translations.entrySet()) {
      indexEntry.setTitle(translation.getValue().getName(), translation.getKey());
      indexEntry.setPreview(translation.getValue().getDescription(), translation.getKey());
    }
  }

  private void deleteComponentIndex(String componentId) {
    FullIndexEntry indexEntry = new FullIndexEntry(INDEX_COMPONENT_SCOPE, "Component", componentId);
    IndexEngineProxy.removeIndexEntry(indexEntry.getPK());
  }

  private void deleteComponentData(String componentId) {
    // deleting all files associated to this component
    FileRepositoryManager.deleteAbsolutePath(componentId, "");

    // deleting index files
    IndexEngineProxy.removeScopedIndexEntries(componentId);
  }

  @Override
  public void deleteAllComponentIndexes() {
    IndexEngineProxy.removeScopedIndexEntries(INDEX_COMPONENT_SCOPE);
  }

  @Override
  public String addComponentInst(String userId, ComponentInst componentInst)
      throws AdminException, QuotaException {
    try (Connection connection = DBUtil.openConnection()) {
      // Get the father space inst
      SpaceInst spaceInstFather = getSpaceInstById(componentInst.getDomainFatherId());

      // Verify the component space quota
      SpaceServiceProvider.getComponentSpaceQuotaService()
          .verify(ComponentSpaceQuotaKey.from(spaceInstFather));

      // Create the component instance
      componentManager.createComponentInst(componentInst, spaceInstFather.getLocalId());

      // Add the component to the space
      spaceInstFather.addComponentInst(componentInst);

      // Instantiate the component
      String componentName = componentInst.getName();
      String componentId = componentInst.getId();

      ComponentInstancePostConstruction.get(componentName)
          .ifPresent(c -> c.postConstruct(componentId));

      if (isContentManagedComponent(componentName)) {
        // Call the register functions
        contentMgtEngine.registerNewContentInstance(connection, componentId, "containerPDC",
            componentName);
      }

      if (useProfileInheritance && !componentInst.isInheritanceBlocked()) {
        // inherits profiles from space
        setSpaceProfilesToComponent(componentInst, spaceInstFather);
      }

      cache.opAddComponent(componentInst);

      ComponentInstLight component = getComponentInstLight(componentId);
      treeCache.addComponent(component, getDriverSpaceId(spaceInstFather.getId()));

      // indexation of the component
      createComponentIndex(component);

      return componentId;
    } catch (QuotaException e) {
      throw e;
    } catch (Exception e) {
      throw new AdminException(failureOnAdding(COMPONENT, componentInst.getName()), e);
    }
  }

  boolean isContentManagedComponent(String componentName) {
    return "questionReply".equals(componentName) || "whitePages".equals(componentName) ||
        "kmelia".equals(componentName) || "kmax".equals(componentName) ||
        "survey".equals(componentName) || "toolbox".equals(componentName) ||
        "quickinfo".equals(componentName) || "almanach".equals(componentName) ||
        "quizz".equals(componentName) || "forums".equals(componentName) ||
        "pollingStation".equals(componentName) || "bookmark".equals(componentName) ||
        "chat".equals(componentName) || "infoLetter".equals(componentName) ||
        "webSites".equals(componentName) || "gallery".equals(componentName) ||
        "blog".equals(componentName);
  }

  /**
   * Deletes the given component instance in Silverpeas
   * @param userId the unique identifier of the user requesting the deletion.
   * @param componentId the client identifier of the component instance (for a kmelia instance of id
   * 666, the client identifier of the instance is kmelia666)
   * @param definitive is the component instance deletion is definitive? If not, the component
   * instance is moved into the bin.
   * @throws AdminException if an error occurs while deleting the component instance.
   */
  @Override
  public String deleteComponentInst(String userId, String componentId, boolean definitive)
      throws AdminException {
    try {
      // Convert the client id in driver id
      int sDriverComponentId = getDriverComponentId(componentId);

      // Get the component to delete
      ComponentInst componentInst = getComponentInst(sDriverComponentId, null);
      componentInst =
          checkComponentInstanceById(componentInst, componentId, nullComponentInstSupplier);
      Objects.requireNonNull(componentInst);

      // Get the father id
      String sFatherClientId = componentInst.getDomainFatherId();

      // Check if component is used as space homepage
      SpaceInst space = getSpaceInstById(sFatherClientId);
      if (space.getFirstPageType() == SpaceInst.FP_TYPE_COMPONENT_INST &&
          space.getFirstPageExtraParam().equals(componentId)) {
        space.setFirstPageType(SpaceInst.FP_TYPE_STANDARD);
        space.setFirstPageExtraParam(null);
        updateSpaceInst(space);
      }

      if (!definitive) {
        // delete the profiles instance
        for (int nI = 0; nI < componentInst.getNumProfileInst(); nI++) {
          deleteProfileInst(componentInst.getProfileInst(nI).getId());
        }
        componentManager.sendComponentToBasket(componentInst, userId);
      } else {
        try (Connection connection = DBUtil.openConnection()) {
          // dispose the components
          String componentName = componentInst.getName();

          ComponentInstancePreDestruction.get(componentName)
              .ifPresent(c -> c.preDestroy(componentId));

          ServiceProvider.getAllServices(ComponentInstanceDeletion.class)
              .forEach(service -> service.delete(componentId));

          // delete the profiles instance
          for (int nI = 0; nI < componentInst.getNumProfileInst(); nI++) {
            deleteProfileInst(componentInst.getProfileInst(nI).getId());
          }

          if (isContentManagedComponent(componentName)) {
            // Call the unregister functions
            contentMgtEngine.unregisterNewContentInstance(connection, componentId, "containerPDC",
                componentName);
          }
        }
        // Delete the component
        componentManager.deleteComponentInst(componentInst);
      }

      cache.opRemoveComponent(componentInst);
      treeCache.removeComponent(getDriverSpaceId(sFatherClientId), componentId);

      // unindex component
      deleteComponentIndex(componentId);

      if (definitive) {
        // delete definitively data stored on file server
        deleteComponentData(componentId);
      }

      return componentId;
    } catch (SQLException | ContentManagerException e) {
      throw new AdminException(failureOnDeleting(COMPONENT, componentId), e);
    }
  }

  private void updateComponentOrderNum(String componentId, int orderNum) throws AdminException {
    try {
      int driverComponentId = getDriverComponentId(componentId);
      // Update the Component in tables
      componentManager.updateComponentOrder(driverComponentId, orderNum);
      Optional<ComponentInst> optional = cache.getComponentInst(driverComponentId);
      optional.ifPresent(componentInst -> componentInst.setOrderNum(orderNum));
    } catch (Exception e) {
      throw new AdminException(failureOnUpdate(COMPONENT, componentId), e);
    }
  }

  @Override
  public String updateComponentInst(ComponentInst component) throws AdminException {
    try {
      ComponentInst oldComponent = getComponentInst(component.getId());
      String componentClientId = getClientComponentId(oldComponent);

      // Update the components in tables
      componentManager.updateComponentInst(oldComponent, component);

      // Update the inherited rights
      if (useProfileInheritance && (oldComponent.isInheritanceBlocked() != component.
          isInheritanceBlocked())) {
        updateComponentInheritance(oldComponent, component.isInheritanceBlocked());
      }

      cache.opUpdateComponent(component);
      treeCache.updateComponent(getComponentInstLight(component.getId()));

      // indexation of the component
      createComponentIndex(componentClientId);

      return componentClientId;
    } catch (Exception e) {
      throw new AdminException(failureOnUpdate(COMPONENT, component.getId()), e);
    }
  }

  /**
   * Update the inheritance mode between a component and its space. If inheritanceBlocked is true
   * then all inherited space profiles are removed. If inheritanceBlocked is false then all
   * component profiles are removed and space profiles are inherited.
   * @param component a component instance
   * @param inheritanceBlocked is the inheritance blocked?
   * @throws AdminException if an error occurs.
   */
  private void updateComponentInheritance(ComponentInst component, boolean inheritanceBlocked)
      throws AdminException {
    try {
      if (inheritanceBlocked) {
        // deletion of the rights inherited from the space
        List<ProfileInst> inheritedProfiles = component.getInheritedProfiles();
        for (ProfileInst profile : inheritedProfiles) {
          deleteProfileInst(profile.getId());
        }
      } else {
        // deletion of the component rights
        List<ProfileInst> profiles = component.getProfiles();
        for (ProfileInst profile : profiles) {
          deleteProfileInst(profile.getId());
        }
        // setting of the rights from the space
        setSpaceProfilesToComponent(component, null);
      }
    } catch (AdminException e) {
      throw new AdminException(failureOnUpdate(COMPONENT, component.getId()), e);
    }
  }

  @Override
  public void setSpaceProfilesToSubSpace(final SpaceInst subSpace, final SpaceInst space)
      throws AdminException {
    setSpaceProfilesToSubSpace(subSpace, space, false);
  }

  @Override
  public void setSpaceProfilesToSubSpace(final SpaceInst subSpace, final SpaceInst space,
      boolean persist) throws AdminException {
    SpaceInst currentSpace = space;
    if (currentSpace == null) {
      currentSpace = getSpaceInstById(subSpace.getDomainFatherId());
    }

    setSpaceProfileToSubSpace(subSpace, currentSpace, SilverpeasRole.ADMIN);
    setSpaceProfileToSubSpace(subSpace, currentSpace, SilverpeasRole.PUBLISHER);
    setSpaceProfileToSubSpace(subSpace, currentSpace, SilverpeasRole.WRITER);
    setSpaceProfileToSubSpace(subSpace, currentSpace, SilverpeasRole.READER);

    if (persist) {
      for (SpaceProfileInst profile : subSpace.getInheritedProfiles()) {
        if (StringUtil.isDefined(profile.getId())) {
          if (profile.isEmpty()) {
            // we delete a space profile in this context only if it is empty
            deleteSpaceProfileInst(profile.getId(), null);
          } else {
            updateSpaceProfileInst(profile, null);
          }
        } else {
          addSpaceProfileInst(profile, null);
        }
      }
    }
  }

  /**
   * Set space profile to a subspace. There is no persistence. The subspace object is enriched.
   * @param subSpace the object to set profiles
   * @param space the object to get profiles
   * @param role the name of the profile
   */
  private void setSpaceProfileToSubSpace(SpaceInst subSpace, SpaceInst space, SilverpeasRole role) {
    String profileName = role.toString();
    SpaceProfileInst subSpaceProfile = subSpace.getInheritedSpaceProfileInst(profileName);
    if (subSpaceProfile != null) {
      subSpaceProfile.removeAllGroups();
      subSpaceProfile.removeAllUsers();
    } else {
      subSpaceProfile = new SpaceProfileInst();
      subSpaceProfile.setName(profileName);
      subSpaceProfile.setInherited(true);
    }

    // Retrieve superSpace local profile
    SpaceProfileInst profile = space.getSpaceProfileInst(profileName);
    if (profile != null) {
      subSpaceProfile.addGroups(profile.getAllGroups());
      subSpaceProfile.addUsers(profile.getAllUsers());
    }

    // Retrieve superSpace inherited profile
    SpaceProfileInst inheritedProfile = space.getInheritedSpaceProfileInst(profileName);
    if (inheritedProfile != null) {
      subSpaceProfile.addGroups(inheritedProfile.getAllGroups());
      subSpaceProfile.addUsers(inheritedProfile.getAllUsers());
    }

    if (!subSpaceProfile.isEmpty()) {
      subSpace.addSpaceProfileInst(subSpaceProfile);
    }
  }

  @Override
  public void setSpaceProfilesToComponent(final ComponentInst component, final SpaceInst spaceInst)
      throws AdminException {
    WAComponent waComponent = componentRegistry.getWAComponent(component.getName())
        .orElseThrow(
            () -> new AdminException("No such component with name " + component.getName()));
    List<Profile> componentRoles = waComponent.getProfiles();
    final SpaceInst space =
        spaceInst == null ? getSpaceInstById(component.getDomainFatherId()) : spaceInst;
    try {
      for (Profile componentRole : componentRoles) {
        ProfileInst inheritedProfile = removeInheritedComponentRole(component, componentRole);
        List<String> spaceRoles =
            componentRole2SpaceRoles(componentRole.getName(), component.getName());
        removeInheritedSpaceRole(space, spaceRoles, inheritedProfile);
        if (StringUtil.isDefined(inheritedProfile.getId())) {
          updateProfileInst(inheritedProfile);
        } else {
          if (!inheritedProfile.isEmpty()) {
            addProfileInst(inheritedProfile, null);
          }
        }
      }
    } catch (Exception e) {
      throw new AdminException(
          "Fail to set profiles of space " + space.getId() + " to component" + component.getId(),
          e);
    }

    // Now that rights are applied on component, check rights on component objects to delete
    // groups or users who have no more rights on component
    checkObjectsProfiles(component.getId());
  }

  private void removeInheritedSpaceRole(final SpaceInst space, final List<String> spaceRoles,
      final ProfileInst inheritedProfile) {
    for (final String spaceRole : spaceRoles) {
      SpaceProfileInst spaceProfile = space.getSpaceProfileInst(spaceRole);
      if (spaceProfile != null) {
        inheritedProfile.addGroups(spaceProfile.getAllGroups());
        inheritedProfile.addUsers(spaceProfile.getAllUsers());
      }

      spaceProfile = space.getInheritedSpaceProfileInst(spaceRole);
      if (spaceProfile != null) {
        inheritedProfile.addGroups(spaceProfile.getAllGroups());
        inheritedProfile.addUsers(spaceProfile.getAllUsers());
      }
    }
  }

  @Nonnull
  private ProfileInst removeInheritedComponentRole(final ComponentInst component,
      final Profile componentRole) {
    ProfileInst inheritedProfile = component.getInheritedProfileInst(componentRole.getName());
    if (inheritedProfile != null) {
      inheritedProfile.removeAllGroups();
      inheritedProfile.removeAllUsers();
    } else {
      inheritedProfile = new ProfileInst();
      inheritedProfile.setComponentFatherId(component.getLocalId());
      inheritedProfile.setInherited(true);
      inheritedProfile.setName(componentRole.getName());
    }
    return inheritedProfile;
  }

  private void checkObjectsProfiles(String componentId) {
    List<ProfileInst> objectsProfiles = getProfileInsts(componentId);
    if (objectsProfiles == null) {
      return;
    }
    for (ProfileInst objectProfile : objectsProfiles) {
      try {
        List<String> groupIdsToRemove = getGroupIdsToRemove(componentId, objectProfile);
        List<String> userIdsToRemove = getUserIdsToRemove(componentId, objectProfile);
        if (!groupIdsToRemove.isEmpty() || !userIdsToRemove.isEmpty()) {
          for (String groupId : groupIdsToRemove) {
            objectProfile.removeGroup(groupId);
          }
          for (String userId : userIdsToRemove) {
            objectProfile.removeUser(userId);
          }
          profileManager.updateProfileInst(objectProfile);
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this)
            .warn("Error when checking object profile " + objectProfile.getId(), e);
      }
    }
  }

  @Nonnull
  private List<String> getUserIdsToRemove(final String componentId, final ProfileInst objectProfile)
      throws AdminException {
    List<String> userIdsToRemove = new ArrayList<>();
    List<String> userIds = objectProfile.getAllUsers();
    for (String userId : userIds) {
      if (!isComponentAvailableToUser(componentId, userId)) {
        userIdsToRemove.add(userId);
      }
    }
    return userIdsToRemove;
  }

  @Nonnull
  private List<String> getGroupIdsToRemove(final String componentId,
      final ProfileInst objectProfile) throws AdminException {
    List<String> groupIdsToRemove = new ArrayList<>();
    List<String> groupIds = objectProfile.getAllGroups();
    for (String groupId : groupIds) {
      if (!isComponentAvailableToGroup(componentId, groupId)) {
        groupIdsToRemove.add(groupId);
      }
    }
    return groupIdsToRemove;
  }

  @Nullable
  private List<ProfileInst> getProfileInsts(final String componentId) {
    List<ProfileInst> objectsProfiles = null;
    try {
      int shortComponentId = getDriverComponentId(componentId);
      objectsProfiles = profiledObjectManager.getProfiles(shortComponentId);
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .warn("Error when getting all component objects profiles " + componentId, e);
    }
    return objectsProfiles;
  }

  @Override
  public void moveSpace(String spaceId, String fatherId) throws AdminException {
    if (isParent(getDriverSpaceId(spaceId), getDriverSpaceId(fatherId))) {
      // space cannot be moved in one of its descendants
      return;
    }

    int shortSpaceId = getDriverSpaceId(spaceId);
    int shortFatherId = StringUtil.isDefined(fatherId) ? getDriverSpaceId(fatherId) : -1;
    boolean moveOnTop = shortFatherId == -1;

    SpaceInst space = getSpaceInstById(shortSpaceId);
    int shortOldSpaceId = getDriverSpaceId(space.getDomainFatherId());

    // move space in database
    spaceManager.moveSpace(shortSpaceId, shortFatherId);

    // set space in last rank
    int position =
        moveOnTop ? getAllRootSpaceIds().length : getAllSubSpaceIdsWithoutCache(fatherId).length;
    spaceManager.updateSpaceOrder(shortSpaceId, position);

    if (useProfileInheritance) {
      processProfileInstsOnSpaceMove(shortSpaceId, shortFatherId, moveOnTop);
    }

    // reset caches
    cache.resetSpaceInst();
    treeCache.removeSpace(shortSpaceId);
    treeCache.setSubspaces(shortOldSpaceId, spaceManager.getSubSpaces(shortOldSpaceId));
    addSpaceInTreeCache(spaceManager.getSpaceInstLightById(shortSpaceId), false);
    if (!moveOnTop) {
      treeCache.setSubspaces(shortFatherId, spaceManager.getSubSpaces(shortFatherId));
    }

    String[] allComponentIds = getAllComponentIdsRecur(spaceId);
    for (String componentId : allComponentIds) {
      checkObjectsProfiles(componentId);
    }
  }

  private void processProfileInstsOnSpaceMove(final int shortSpaceId, final int shortFatherId,
      final boolean moveOnTop) throws AdminException {
    final SpaceInst space;
    space = spaceManager.getSpaceInstById(shortSpaceId);

    if (moveOnTop) {
      // inherited rights must be removed but local rights are preserved
      List<SpaceProfileInst> inheritedProfiles = space.getInheritedProfiles();
      for (SpaceProfileInst profile : inheritedProfiles) {
        deleteSpaceProfileInst(profile.getId());
      }
    } else {
      if (!space.isInheritanceBlocked()) {
        // space inherits rights from parent
        SpaceInst father = getSpaceInstById(shortFatherId);
        setSpaceProfilesToSubSpace(space, father, true);
      }
    }

    // Merge inherited and specific for each type of profile
    Map<String, SpaceProfileInst> mergedProfiles = new HashMap<>();
    List<SpaceProfileInst> allProfiles = new ArrayList<>(space.getProfiles());
    if (!moveOnTop) {
      allProfiles.addAll(space.getInheritedProfiles());
    }
    for (SpaceProfileInst profile : allProfiles) {
      SpaceProfileInst mergedProfile = mergedProfiles.get(profile.getName());
      if (mergedProfile == null) {
        mergedProfile = new SpaceProfileInst();
        mergedProfile.setName(profile.getName());
        mergedProfile.setInherited(true);
        mergedProfiles.put(profile.getName(), mergedProfile);
      }
      mergedProfile.addGroups(profile.getAllGroups());
      mergedProfile.addUsers(profile.getAllUsers());
    }

    // Spread profiles
    for (SpaceProfileInst profile : mergedProfiles.values()) {
      spreadSpaceProfile(shortSpaceId, profile);
    }

    if (moveOnTop) {
      // on top level, space inheritance is not applicable
      space.setInheritanceBlocked(false);
      spaceManager.updateSpaceInst(space);
    }
  }

  @Override
  public void moveComponentInst(String spaceId, String componentId, String idComponentBefore,
      ComponentInst[] componentInsts) throws AdminException {
    try {

      int sDriverComponentId = getDriverComponentId(componentId);
      // Convert the client space id to driver space id
      int sDriverSpaceId = getDriverSpaceId(spaceId);

      ComponentInst componentInst = getComponentInst(componentId);
      String oldSpaceId = componentInst.getDomainFatherId();
      // Update the components in tables
      componentManager.moveComponentInst(sDriverSpaceId, sDriverComponentId);
      componentInst.setDomainFatherId(String.valueOf(sDriverSpaceId));

      // set space profiles to component if it doesn't use its own rights
      if (!componentInst.isInheritanceBlocked()) {
        setSpaceProfilesToComponent(componentInst, null);
      }

      if (StringUtil.isDefined(idComponentBefore) && componentInsts != null) {
        // Set component in order
        setComponentPlace(componentId, idComponentBefore, componentInsts);
      } else {
        // set component in last rank
        updateComponentOrderNum(componentId, getAllComponentIds(spaceId).length);
      }

      // Update extraParamPage from Space if necessary
      SpaceInst fromSpace = getSpaceInstById(getDriverSpaceId(oldSpaceId));
      String spaceHomePage = fromSpace.getFirstPageExtraParam();

      if (StringUtil.isDefined(spaceHomePage) && spaceHomePage.equals(componentId)) {
        fromSpace.setFirstPageExtraParam("");
        fromSpace.setFirstPageType(0);
        updateSpaceInst(fromSpace);
      }
      // Remove component from the Cache
      cache.resetSpaceInst();
      cache.resetComponentInst();
      treeCache.setComponents(getDriverSpaceId(oldSpaceId),
          componentManager.getComponentsInSpace(getDriverSpaceId(oldSpaceId)));
      treeCache.setComponents(getDriverSpaceId(spaceId),
          componentManager.getComponentsInSpace(getDriverSpaceId(spaceId)));
    } catch (Exception e) {
      throw new AdminException("Fail to move component " + componentId + " into space " + spaceId,
          e);
    }
  }

  @Override
  public void setComponentPlace(String componentId, String idComponentBefore,
      ComponentInst[] brothersComponents) throws AdminException {
    int orderNum = 0;
    int i;
    ComponentInst theComponent = getComponentInst(componentId);

    for (i = 0; i < brothersComponents.length; i++) {
      if (idComponentBefore.equals(brothersComponents[i].getId())) {
        theComponent.setOrderNum(orderNum);
        updateComponentOrderNum(theComponent.getId(), orderNum);
        orderNum++;
      }
      if (brothersComponents[i].getOrderNum() != orderNum) {
        brothersComponents[i].setOrderNum(orderNum);
        updateComponentOrderNum(brothersComponents[i].getId(), orderNum);
      }
      orderNum++;
    }
    if (orderNum == i) {
      theComponent.setOrderNum(orderNum);
      updateComponentOrderNum(theComponent.getId(), orderNum);
    }

    // update caches
    int spaceId = getDriverSpaceId(theComponent.getSpaceId());
    treeCache.setComponents(spaceId, componentManager.getComponentsInSpace(spaceId));
    cache.removeSpaceInst(spaceId);
  }

  @Override
  public String getRequestRouter(String sComponentName) {
    return componentRegistry.getWAComponent(sComponentName)
        .filter(wac -> StringUtil.isDefined(wac.getRouter()))
        .map(WAComponent::getRouter)
        .orElse("R" + sComponentName);
  }

  // --------------------------------------------------------------------------------------------------------
  // PROFILE RELATED FUNCTIONS
  // --------------------------------------------------------------------------------------------------------
  @Override
  public String[] getAllProfilesNames(String sComponentName) {
    List<String> asProfiles = new ArrayList<>();
    componentRegistry.getWAComponent(sComponentName).ifPresent(wac -> {
      List<Profile> profiles = wac.getProfiles();
      List<String> profileNames = new ArrayList<>(profiles.size());
      for (Profile profile : profiles) {
        profileNames.add(profile.getName());
      }
      asProfiles.addAll(profileNames);
    });
    return asProfiles.toArray(new String[0]);
  }

  @Override
  public String getProfileLabelFromName(String sComponentName, String sProfileName, String lang) {
    return componentRegistry.getWAComponent(sComponentName).map(wac -> {
      List<Profile> profiles = wac.getProfiles();
      for (Profile profile : profiles) {
        if (profile.getName().equals(sProfileName)) {
          return profile.getLabel().get(lang);
        }
      }
      return sProfileName;
    }).orElse(sProfileName);
  }

  @Override
  public ProfileInst getProfileInst(String sProfileId) throws AdminException {
    final ProfileInst profileInst;
    Optional<ProfileInst> optionalProfile = cache.getProfileInst(sProfileId);
    if (optionalProfile.isEmpty()) {
      profileInst = profileManager.getProfileInst(sProfileId, false);
      cache.putProfileInst(profileInst);
    } else {
      profileInst = optionalProfile.get();
    }
    return profileInst;
  }

  @Override
  public List<ProfileInst> getProfilesByObject(ProfiledObjectId objectRef, String componentId)
      throws AdminException {
    return profiledObjectManager.getProfiles(objectRef, getDriverComponentId(componentId));
  }

  @Override
  public String[] getProfilesByObjectAndUserId(ProfiledObjectId objectRef, String componentId,
      String userId) throws AdminException {
    List<String> groups = getAllGroupsOfUser(userId);
    return profiledObjectManager.getUserProfileNames(objectRef, getDriverComponentId(componentId),
        Integer.parseInt(userId), groups);
  }

  @Override
  public String[] getProfilesByObjectAndGroupId(final ProfiledObjectId objectRef,
      final String componentId, final String groupId) throws AdminException {
    List<String> groupIds = groupManager.getPathToGroup(groupId);
    groupIds.add(groupId);
    return profiledObjectManager.getUserProfileNames(objectRef, getDriverComponentId(componentId),
        -1, groupIds);
  }

  @Override
  public Map<Pair<String, String>, Set<String>> getUserProfilesByComponentIdAndObjectId(
      final ProfiledObjectIds profiledObjectIds, final Collection<String> componentIds,
      final String userId) throws AdminException {
    final Map<Integer, String> localComponentIds =
        componentIds.stream().collect(toMap(this::getDriverComponentId, i -> i));
    final List<String> groups = getAllGroupsOfUser(userId);
    return profiledObjectManager.getUserProfileNames(profiledObjectIds, localComponentIds.keySet(),
            Integer.parseInt(userId), groups)
        .entrySet()
        .stream()
        .collect(toMap(
            e -> Pair.of(localComponentIds.get(e.getKey().getFirst()),
                String.valueOf(e.getKey().getSecond())),
            Map.Entry::getValue));
  }

  @Override
  public Map<String, List<String>> getProfilesByObjectTypeAndUserId(
      final ProfiledObjectType profiledObjectType, final String componentId, final String userId)
      throws AdminException {
    final List<String> groups = getAllGroupsOfUser(userId);
    return profiledObjectManager.getUserProfileNames(profiledObjectType,
        getDriverComponentId(componentId), Integer.parseInt(userId), groups);
  }

  @Override
  public boolean isObjectAvailableToUser(String componentId, ProfiledObjectId objectRef,
      String userId) throws AdminException {
    return userId == null ||
        getProfilesByObjectAndUserId(objectRef, componentId, userId).length > 0;
  }

  @Override
  public boolean isObjectAvailableToGroup(String componentId, ProfiledObjectId objectRef,
      String groupId) throws AdminException {
    return groupId == null ||
        getProfilesByObjectAndGroupId(objectRef, componentId, groupId).length > 0;
  }

  @Override
  public String addProfileInst(ProfileInst profileInst) throws AdminException {
    return addProfileInst(profileInst, null);
  }

  /**
   * Get the given profile instance from Silverpeas
   */
  @Override
  public String addProfileInst(ProfileInst profileInst, String userId) throws AdminException {
    try {
      int driverFatherId = profileInst.getComponentFatherId();
      String sProfileId = profileManager.createProfileInst(profileInst, driverFatherId);
      profileInst.setId(sProfileId);

      if (profileInst.getObjectId().isNotDefined() || profileInst.getObjectId().isRootNode()) {
        ComponentInst componentInstFather = getComponentInst(driverFatherId, null);
        componentInstFather =
            checkComponentInstanceById(componentInstFather, String.valueOf(driverFatherId),
                nullComponentInstSupplier);
        Objects.requireNonNull(componentInstFather);
        componentInstFather.addProfileInst(profileInst);
        if (StringUtil.isDefined(userId)) {
          componentInstFather.setUpdaterUserId(userId);
          updateComponentInst(componentInstFather);
        }
        cache.opAddProfile(profileManager.getProfileInst(sProfileId, false));
      }
      return sProfileId;
    } catch (Exception e) {
      throw new AdminException(failureOnAdding(PROFILE, profileInst.getName()), e);
    }
  }

  private void deleteProfileInst(String sProfileId) throws AdminException {
    deleteProfileInst(sProfileId, null);
  }

  @Override
  public String deleteProfileInst(String profileId, String userId) throws AdminException {
    ProfileInst profile = profileManager.getProfileInst(profileId, true);
    try {
      profileManager.deleteProfileInst(profile);
      if (StringUtil.isDefined(userId) &&
          (profile.getObjectId().isNotDefined() || profile.getObjectId().isRootNode())) {
        final int driverFatherId = profile.getComponentFatherId();
        ComponentInst component = getComponentInst(driverFatherId, null);
        component = checkComponentInstanceById(component, String.valueOf(driverFatherId),
            nullComponentInstSupplier);
        Objects.requireNonNull(component);

        component.setUpdaterUserId(userId);
        updateComponentInst(component);
      }

      if (profile.getObjectId().isNotDefined() || profile.getObjectId().isRootNode()) {
        cache.opRemoveProfile(profile);
      }

      return profileId;
    } catch (Exception e) {
      throw new AdminException(failureOnDeleting(PROFILE, profileId), e);
    }
  }

  @Override
  public String updateProfileInst(ProfileInst profileInstNew) throws AdminException {
    return doUpdateProfileInst(profileInstNew, null);
  }

  @Override
  public String updateProfileInst(ProfileInst profileInstNew, String userId) throws AdminException {
    return doUpdateProfileInst(profileInstNew, userId);
  }

  private String doUpdateProfileInst(ProfileInst newProfile, String userId) throws AdminException {
    try {
      profileManager.updateProfileInst(newProfile);
      if (StringUtil.isDefined(userId) &&
          (newProfile.getObjectId().isNotDefined() || newProfile.getObjectId().isRootNode())) {
        final int driverFatherId = newProfile.getComponentFatherId();
        ComponentInst component = getComponentInst(driverFatherId, null);
        component = checkComponentInstanceById(component, String.valueOf(driverFatherId),
            nullComponentInstSupplier);
        Objects.requireNonNull(component);
        component.setUpdaterUserId(userId);
        updateComponentInst(component);
      }
      if (newProfile.getObjectId().isNotDefined() || newProfile.getObjectId().isRootNode()) {
        cache.opUpdateProfile(newProfile);
      }

      return newProfile.getId();
    } catch (Exception e) {
      throw new AdminException(failureOnUpdate(PROFILE, newProfile.getId()), e);
    }
  }

  // --------------------------------------------------------------------------------------------------------
  // SPACE PROFILE RELATED FUNCTIONS
  // --------------------------------------------------------------------------------------------------------
  @Override
  public SpaceProfileInst getSpaceProfileInst(String spaceProfileId) throws AdminException {
    return spaceProfileManager.getSpaceProfileInst(spaceProfileId, false);
  }

  @Override
  public String addSpaceProfileInst(SpaceProfileInst spaceProfile, String userId)
      throws AdminException {
    try {
      Integer spaceId = getDriverComponentId(spaceProfile.getSpaceFatherId());
      String sSpaceProfileId = spaceProfileManager.createSpaceProfileInst(spaceProfile, spaceId);
      spaceProfile.setId(sSpaceProfileId);
      if (StringUtil.isDefined(userId)) {
        SpaceInst spaceInstFather = getSpaceInstById(spaceId);
        spaceInstFather.setUpdaterUserId(userId);
        updateSpaceInst(spaceInstFather);
      }
      // add new profile in spaces cache
      Optional<SpaceInst> spaceInst = cache.getSpaceInst(spaceId);
      spaceInst.ifPresent(s -> s.addSpaceProfileInst(spaceProfile));

      // profile 'Manager' does not need to be spread
      if (!spaceProfile.isManager()) {
        spreadInheritedSpaceProfile(spaceProfile, spaceId);
      }

      cache.opAddSpaceProfile(spaceProfile);
      return sSpaceProfileId;
    } catch (Exception e) {
      throw new AdminException(failureOnAdding(SPACE_PROFILE, spaceProfile.getName()), e);
    }
  }

  private void spreadInheritedSpaceProfile(final SpaceProfileInst spaceProfile,
      final Integer spaceId) throws AdminException {
    if (!spaceProfile.isInherited()) {
      SpaceProfileInst inheritedProfile =
          spaceProfileManager.getInheritedSpaceProfileInstByName(spaceId, spaceProfile.getName());
      if (inheritedProfile != null) {
        spaceProfile.addGroups(inheritedProfile.getAllGroups());
        spaceProfile.addUsers(inheritedProfile.getAllUsers());
      }
    }
    spreadSpaceProfile(spaceId, spaceProfile);
  }

  private void deleteSpaceProfileInst(String sSpaceProfileId) throws AdminException {
    deleteSpaceProfileInst(sSpaceProfileId, null);
  }

  /**
   * Delete the given space profile from Silverpeas
   */
  @Override
  public String deleteSpaceProfileInst(String sSpaceProfileId, String userId)
      throws AdminException {
    SpaceProfileInst spaceProfileInst =
        spaceProfileManager.getSpaceProfileInst(sSpaceProfileId, true);
    if (spaceProfileInst == null) {
      return sSpaceProfileId;
    }
    try {
      spaceProfileManager.deleteSpaceProfileInst(spaceProfileInst);
      cache.opRemoveSpaceProfile(spaceProfileInst);
      spaceProfileInst.removeAllGroups();
      spaceProfileInst.removeAllUsers();
      Integer spaceId = getDriverComponentId(spaceProfileInst.getSpaceFatherId());
      if (StringUtil.isDefined(userId)) {
        SpaceInst spaceInstFather = getSpaceInstById(spaceId);
        spaceInstFather.setUpdaterUserId(userId);
        updateSpaceInst(spaceInstFather);
      }
      spreadInheritedSpaceProfile(spaceProfileInst, spaceId);

      return sSpaceProfileId;
    } catch (Exception e) {
      throw new AdminException(failureOnDeleting(SPACE_PROFILE, sSpaceProfileId), e);
    }
  }

  /**
   * Update the given space profile in Silverpeas
   */
  private void updateSpaceProfileInst(SpaceProfileInst newSpaceProfile) throws AdminException {
    updateSpaceProfileInst(newSpaceProfile, null);
  }

  @Override
  public String updateSpaceProfileInst(SpaceProfileInst newSpaceProfile, String userId)
      throws AdminException {
    try {
      final int spaceId = getDriverSpaceId(newSpaceProfile.getSpaceFatherId());
      SpaceProfileInst oldSpaceProfile =
          spaceProfileManager.getSpaceProfileInst(newSpaceProfile.getId(), false);
      if (oldSpaceProfile == null) {
        // Potential case of space that is switching inheritance blocking status
        // Avoiding the cache repository in order to get the new data of the space
        final SpaceInst spaceInstFather = spaceManager.getSpaceInstById(spaceId);
        if (spaceInstFather.isInheritanceBlocked()) {
          // Spreading the specific rights
          final SpaceProfileInst spaceSpecificProfile =
              spaceProfileManager.getSpaceProfileInstByName(
                  spaceId, newSpaceProfile.getName());
          if (spaceSpecificProfile != null) {
            final SpaceProfileInst profileToSpread = new SpaceProfileInst();
            profileToSpread.setName(newSpaceProfile.getName());
            profileToSpread.setInherited(true);
            profileToSpread.addGroups(spaceSpecificProfile.getAllGroups());
            profileToSpread.addUsers(spaceSpecificProfile.getAllUsers());
            spreadSpaceProfile(spaceId, profileToSpread);
          }
        }
        return null;
      }
      String spaceProfileNewId =
          spaceProfileManager.updateSpaceProfileInst(oldSpaceProfile, newSpaceProfile);

      // profile 'Manager' does not need to be spread
      if (!oldSpaceProfile.isManager()) {
        if (StringUtil.isDefined(userId)) {
          final SpaceInst spaceInstFather = getSpaceInstById(spaceId);
          spaceInstFather.setUpdaterUserId(userId);
          updateSpaceInst(spaceInstFather);
        }
        // Add inherited users and groups for this role
        List<SpaceProfileInst> allProfileSources = new ArrayList<>();
        allProfileSources.add(newSpaceProfile);
        if (newSpaceProfile.isInherited()) {
          allProfileSources.add(
              spaceProfileManager.getSpaceProfileInstByName(spaceId, oldSpaceProfile.getName()));
        } else {
          allProfileSources.add(spaceProfileManager.getInheritedSpaceProfileInstByName(spaceId,
              oldSpaceProfile.getName()));
        }
        SpaceProfileInst profileToSpread = new SpaceProfileInst();
        profileToSpread.setName(oldSpaceProfile.getName());
        profileToSpread.setInherited(true);
        allProfileSources.remove(null);
        for (SpaceProfileInst spaceProfile : allProfileSources) {
          profileToSpread.addGroups(spaceProfile.getAllGroups());
          profileToSpread.addUsers(spaceProfile.getAllUsers());
        }
        spreadSpaceProfile(spaceId, profileToSpread);
      }
      cache.opUpdateSpaceProfile(
          spaceProfileManager.getSpaceProfileInst(newSpaceProfile.getId(), false));

      return spaceProfileNewId;
    } catch (Exception e) {
      throw new AdminException(failureOnUpdate(SPACE_PROFILE, newSpaceProfile.getId()), e);
    }
  }

  private String spaceRole2ComponentRole(String spaceRole, String componentName) {
    return roleMapping.spaceRoleToComponentOne(spaceRole, componentName);
  }

  private List<String> componentRole2SpaceRoles(String componentRole, String componentName) {
    List<String> roles = new ArrayList<>();
    for (var role : InheritableSpaceRoles.getAllRoles()) {
      String mappedRole = spaceRole2ComponentRole(role.getName(), componentName);
      if (mappedRole != null && mappedRole.equalsIgnoreCase(componentRole)) {
        roles.add(role.getName());
      }
    }
    return roles;
  }

  private void spreadSpaceProfile(int spaceId, SpaceProfileInst spaceProfile)
      throws AdminException {
    // update profile in components
    List<ComponentInstLight> components = treeCache.getComponents(spaceId);
    spreadSpaceProfilesInComponents(spaceId, spaceProfile, components);

    // update profile in subspaces
    List<SpaceInstLight> subSpaces = treeCache.getSubSpaces(spaceId);
    spreadSpaceProfilesInSubspaces(spaceProfile, subSpaces);
  }

  private void spreadSpaceProfilesInSubspaces(final SpaceProfileInst spaceProfile,
      final List<SpaceInstLight> subSpaces) throws AdminException {
    for (SpaceInstLight subSpace : subSpaces) {
      if (!subSpace.isInheritanceBlocked()) {
        SpaceProfileInst subSpaceProfile =
            spaceProfileManager.getInheritedSpaceProfileInstByName(subSpace.getLocalId(),
                spaceProfile.getName());
        if (subSpaceProfile != null) {
          subSpaceProfile.setGroups(spaceProfile.getAllGroups());
          subSpaceProfile.setUsers(spaceProfile.getAllUsers());
          updateSpaceProfileInst(subSpaceProfile);
        } else {
          subSpaceProfile = new SpaceProfileInst();
          subSpaceProfile.setName(spaceProfile.getName());
          subSpaceProfile.setInherited(true);
          subSpaceProfile.setSpaceFatherId(String.valueOf(subSpace.getLocalId()));
          subSpaceProfile.addGroups(spaceProfile.getAllGroups());
          subSpaceProfile.addUsers(spaceProfile.getAllUsers());
          if (!subSpaceProfile.getAllGroups().isEmpty() ||
              !subSpaceProfile.getAllUsers().isEmpty()) {
            addSpaceProfileInst(subSpaceProfile, null);
          }
        }
      }
    }
  }

  private void spreadSpaceProfilesInComponents(final int spaceId,
      final SpaceProfileInst spaceProfile, final List<ComponentInstLight> components)
      throws AdminException {
    for (ComponentInstLight component : components) {
      if (component != null && !component.isInheritanceBlocked()) {
        String componentRole = spaceRole2ComponentRole(spaceProfile.getName(), component.getName());
        if (componentRole != null) {
          ProfileInst inheritedProfile =
              profileManager.getInheritedProfileInst(component.getLocalId(), componentRole);
          if (inheritedProfile != null) {
            updateInheritedProfileInsts(spaceId, spaceProfile, inheritedProfile, componentRole,
                component);
          } else {
            addNewProfileInsts(spaceProfile, componentRole, component);
          }
        }
      }
    }
  }

  private void addNewProfileInsts(final SpaceProfileInst spaceProfile, final String componentRole,
      final ComponentInstLight component) throws AdminException {
    final ProfileInst inheritedProfile;
    inheritedProfile = new ProfileInst();
    inheritedProfile.setComponentFatherId(component.getLocalId());
    inheritedProfile.setName(componentRole);
    inheritedProfile.setInherited(true);
    inheritedProfile.addGroups(spaceProfile.getAllGroups());
    inheritedProfile.addUsers(spaceProfile.getAllUsers());
    if (inheritedProfile.getNumGroup() > 0 || inheritedProfile.getNumUser() > 0) {
      addProfileInst(inheritedProfile);
    }
  }

  private void updateInheritedProfileInsts(final int spaceId, final SpaceProfileInst spaceProfile,
      final ProfileInst inheritedProfile, final String componentRole,
      final ComponentInstLight component) throws AdminException {
    inheritedProfile.removeAllGroups();
    inheritedProfile.removeAllUsers();

    inheritedProfile.addGroups(spaceProfile.getAllGroups());
    inheritedProfile.addUsers(spaceProfile.getAllUsers());

    List<String> profilesToCheck = componentRole2SpaceRoles(componentRole, component.getName());
    profilesToCheck.remove(spaceProfile.getName()); // exclude current space profile
    for (String profileToCheck : profilesToCheck) {
      Stream.of(spaceProfileManager.getInheritedSpaceProfileInstByName(spaceId, profileToCheck),
              spaceProfileManager.getSpaceProfileInstByName(spaceId, profileToCheck))
          .filter(Objects::nonNull)
          .forEach(p -> {
            inheritedProfile.addGroups(p.getAllGroups());
            inheritedProfile.addUsers(p.getAllUsers());
          });
    }
    updateProfileInst(inheritedProfile);
  }

  // -------------------------------------------------------------------------
  // GROUP RELATED FUNCTIONS
  // -------------------------------------------------------------------------
  @Override
  public String[] getGroupNames(String[] groupIds) throws AdminException {
    if (groupIds == null) {
      return ArrayUtil.emptyStringArray();
    }
    String[] asGroupNames = new String[groupIds.length];
    for (int nI = 0; nI < groupIds.length; nI++) {
      asGroupNames[nI] = getGroupName(groupIds[nI]);
    }
    return asGroupNames;
  }

  @Override
  public String getGroupName(String sGroupId) throws AdminException {
    return getGroup(sGroupId).getName();
  }

  @Override
  public List<GroupDetail> getAllGroups() throws AdminException {
    return groupManager.getAllGroups();
  }

  @Override
  public boolean isGroupExist(String groupName) throws AdminException {
    return groupManager.isGroupExist(groupName);
  }

  @Override
  public GroupDetail getGroup(String groupId) throws AdminException {
    return groupManager.getGroup(groupId);
  }

  @Override
  public List<String> getPathToGroup(String groupId) throws AdminException {
    return groupManager.getPathToGroup(groupId);
  }

  @Override
  public GroupDetail getGroupByNameInDomain(String groupName, String domainFatherId)
      throws AdminException {
    return groupManager.getGroupByNameInDomain(groupName, domainFatherId);
  }

  @Override
  public GroupDetail[] getGroups(String[] asGroupId) throws AdminException {
    if (asGroupId == null) {
      return new GroupDetail[0];
    }
    GroupDetail[] aGroup = new GroupDetail[asGroupId.length];
    for (int nI = 0; nI < asGroupId.length; nI++) {
      aGroup[nI] = getGroup(asGroupId[nI]);
    }
    return aGroup;
  }

  @Override
  public String addGroup(GroupDetail group) throws AdminException {
    return addGroup(group, false);
  }

  @Override
  public String addGroup(GroupDetail group, boolean onlyInSilverpeas) throws AdminException {
    try {
      final String groupId = groupManager.addGroup(group, onlyInSilverpeas, true);
      group.setId(groupId);
      groupSynchroScheduler.updateContextWith(group);
      cache.opAddGroup(group);
      return groupId;
    } catch (AdminException e) {
      throw e;
    } catch (Exception e) {
      throw new AdminException(failureOnAdding(GROUP, group.getName()), e);
    }
  }

  @Override
  public List<GroupDetail> restoreGroup(final String groupId) throws AdminException {
    final GroupDetail group = getGroup(groupId);
    if (group == null) {
      throw new AdminException(unknown(GROUP, groupId));
    }
    try {
      final List<GroupDetail> restoredGroups = groupManager.restoreGroup(group, true);
      restoredGroups.forEach(groupSynchroScheduler::updateContextWith);
      if (!restoredGroups.isEmpty()) {
        cache.resetCache();
      }
      return restoredGroups;
    } catch (Exception e) {
      throw new AdminException(failureOnRemoving(GROUP, groupId), e);
    }
  }

  @Override
  public List<GroupDetail> removeGroup(final String groupId) throws AdminException {
    final GroupDetail group = getGroup(groupId);
    if (group == null) {
      throw new AdminException(unknown(GROUP, groupId));
    }
    try {
      final List<GroupDetail> removedGroups = groupManager.removeGroup(group, true);
      // Removing the removed groups from caches
      removedGroups.forEach(g -> {
        groupSynchroScheduler.removeFromContext(g);
        cache.opRemoveGroup(g);
      });
      return removedGroups;
    } catch (Exception e) {
      throw new AdminException(failureOnRemoving(GROUP, groupId), e);
    }
  }

  @Override
  public List<GroupDetail> deleteGroupById(String sGroupId) throws AdminException {
    try {
      return deleteGroupById(sGroupId, false);
    } catch (Exception e) {
      throw new AdminException(failureOnDeleting(GROUP, sGroupId), e);
    }
  }

  @Override
  public List<GroupDetail> deleteGroupById(String sGroupId, boolean onlyInSilverpeas)
      throws AdminException {
    // Get group information
    GroupDetail group = getGroup(sGroupId);
    if (group == null) {
      throw new AdminException(unknown(GROUP, sGroupId));
    }
    try {
      // Delete group profiles
      deleteGroupProfileInst(sGroupId);
      // Delete group itself
      final List<GroupDetail> deletedGroups = groupManager.deleteGroup(group, onlyInSilverpeas);
      // Removing the deleted groups from caches
      deletedGroups.forEach(g -> {
        groupSynchroScheduler.removeFromContext(g);
        cache.opRemoveGroup(g);
      });
      return deletedGroups;
    } catch (Exception e) {
      throw new AdminException(failureOnDeleting(GROUP, group.getId()), e);
    }
  }

  @Override
  public String updateGroup(GroupDetail group) throws AdminException {
    try {
      return updateGroup(group, false);
    } catch (Exception e) {
      throw new AdminException(failureOnUpdate(GROUP, group.getId()), e);
    }
  }

  @Override
  public String updateGroup(GroupDetail group, boolean onlyInSilverpeas) throws AdminException {
    try {
      String groupId = groupManager.updateGroup(group, onlyInSilverpeas);
      groupSynchroScheduler.updateContextWith(group);
      cache.resetOnUpdateGroup();
      return groupId;
    } catch (Exception e) {
      throw new AdminException(failureOnUpdate(GROUP, group.getId()), e);
    }
  }

  @Override
  public void removeUserFromGroup(String sUserId, String sGroupId) throws AdminException {
    try {
      // Update group
      groupManager.removeUserFromGroup(sUserId, sGroupId);

      cache.resetOnUpdateGroup();

    } catch (Exception e) {
      throw new AdminException(failureOnDeleting(USER + sUserId, IN_GROUP + sGroupId), e);
    }
  }

  @Override
  public void addUserInGroup(String sUserId, String sGroupId) throws AdminException {
    try {
      // Update group
      groupManager.addUserInGroup(sUserId, sGroupId);
      cache.resetOnUpdateGroup();
    } catch (Exception e) {
      throw new AdminException(failureOnAdding(USER + sUserId, IN_GROUP + sGroupId), e);
    }
  }

  @Override
  public List<GroupDetail> getAllRootGroups() throws AdminException {
    return groupManager.getAllRootGroups();
  }

  //
  // --------------------------------------------------------------------------------------------------------
  // GROUP PROFILE RELATED FUNCTIONS
  // --------------------------------------------------------------------------------------------------------
  @Override
  public GroupProfileInst getGroupProfileInst(String groupId) throws AdminException {
    return groupProfileManager.getGroupProfileInst(groupId, false);
  }

  @Override
  public String addGroupProfileInst(GroupProfileInst groupProfileInst) throws AdminException {
    try {
      // Create the space profile instance
      GroupDetail group = getGroup(groupProfileInst.getGroupId());
      String sProfileId =
          groupProfileManager.createGroupProfileInst(groupProfileInst, group.getId());
      groupProfileInst.setId(sProfileId);
      return sProfileId;
    } catch (Exception e) {
      throw new AdminException(failureOnAdding(GROUP_PROFILE, groupProfileInst.getName()), e);
    }
  }

  @Override
  public String deleteGroupProfileInst(String groupId) throws AdminException {
    // Get the SpaceProfile to delete
    GroupProfileInst groupProfileInst = groupProfileManager.getGroupProfileInst(groupId, true);
    if (groupProfileInst == null) {
      return groupId;
    }
    try {
      // Delete the Profile in tables
      groupProfileManager.deleteGroupProfileInst(groupProfileInst);
      return groupId;
    } catch (Exception e) {
      throw new AdminException(failureOnDeleting(GROUP_PROFILE, groupId), e);
    }
  }

  @Override
  public String updateGroupProfileInst(GroupProfileInst groupProfileInstNew) throws AdminException {
    String sSpaceProfileNewId = groupProfileInstNew.getId();
    if (!StringUtil.isDefined(sSpaceProfileNewId)) {
      sSpaceProfileNewId = addGroupProfileInst(groupProfileInstNew);
    } else {
      try {
        GroupProfileInst oldSpaceProfile =
            groupProfileManager.getGroupProfileInst(groupProfileInstNew.getGroupId(), false);
        // Update the group profile in tables
        groupProfileManager.updateGroupProfileInst(oldSpaceProfile, groupProfileInstNew);
      } catch (Exception e) {
        throw new AdminException(failureOnUpdate(GROUP_PROFILE, groupProfileInstNew.getId()), e);
      }
    }
    return sSpaceProfileNewId;

  }

  @Override
  public void indexAllGroups() throws AdminException {

    // starting by remove all indexed data relative to groups
    IndexEngineProxy.removeScopedIndexEntries("groups");

    Domain[] domains = getAllDomains(); //All domains except Mixt Domain (id -1)
    for (Domain domain : domains) {
      try {
        indexGroups(domain.getId());
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }

    //Mixt Domain (id -1)
    try {
      indexGroups("-1");
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  @Override
  public void indexGroups(String domainId) throws AdminException {
    try {
      domainDriverManager.indexAllGroups(domainId);
    } catch (Exception e) {
      throw new AdminException(failureOnIndexing("groups in domain", domainId), e);
    }
  }

  // -------------------------------------------------------------------------
  // USER RELATED FUNCTIONS
  // -------------------------------------------------------------------------
  @Override
  public String[] getAllUsersIds() throws AdminException {
    List<String> userIds = userManager.getAllUsersIds();
    return userIds.toArray(new String[0]);
  }

  @Override
  public UserDetail getUserDetail(String sUserId) throws AdminException {
    if (!StringUtil.isDefined(sUserId)) {
      return null;
    } else if (sUserId.equals(UserDetail.SYSTEM_USER_ID)) {
      return UserDetail.getSystemUser();
    }

    final UserDetail ud;
    Optional<UserDetail> optionalUser = cache.getUserDetail(sUserId);
    if (optionalUser.isEmpty()) {
      ud = userManager.getUserDetail(sUserId);
      if (ud != null) {
        cache.putUserDetail(sUserId, ud);
      }
    } else {
      ud = optionalUser.get();
    }
    return ud;
  }

  @Override
  public UserDetail[] getUserDetails(String[] userIds) {
    if (userIds == null) {
      return new UserDetail[0];
    }

    List<UserDetail> users = new ArrayList<>(userIds.length);
    for (String userId : userIds) {
      try {
        users.add(getUserDetail(userId));
      } catch (AdminException e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
    return users.toArray(new UserDetail[0]);
  }

  @Override
  public List<UserDetail> getAllUsers() throws AdminException {
    return userManager.getAllUsers();
  }

  @Override
  public List<UserDetail> getAllUsersFromNewestToOldest() throws AdminException {
    return userManager.getAllUsersFromNewestToOldest();
  }

  @Override
  public boolean isEmailExisting(String email) throws AdminException {
    return userManager.isEmailExisting(email);
  }

  @Override
  public String getUserIdByLoginAndDomain(String sLogin, String sDomainId) throws AdminException {
    Domain[] theDomains;
    String valret = null;
    if (!StringUtil.isDefined(sDomainId)) {
      try {
        theDomains = domainDriverManager.getAllDomains();
      } catch (Exception e) {
        throw new AdminException(
            failureOnGetting("user by login and domain:", sLogin + "/" + sDomainId), e);
      }
      for (int i = 0; i < theDomains.length && valret == null; i++) {
        try {
          valret = userManager.getUserIdByLoginAndDomain(sLogin, theDomains[i].getId());
        } catch (Exception e) {
          throw new AdminException(
              failureOnGetting("user by login and domain:", sLogin + "/" + sDomainId), e);
        }
      }
      if (valret == null) {
        throw new AdminException(unknown("in all domains user with login", sLogin));
      }
    } else {
      valret = userManager.getUserIdByLoginAndDomain(sLogin, sDomainId);
    }
    return valret;
  }

  @Override
  public String getUserIdByAuthenticationKey(String authenticationKey) throws AdminException {
    Map<String, String> userParameters = domainDriverManager.authenticate(authenticationKey);
    String login = userParameters.get(LOGIN_PARAM);
    String domainId = userParameters.get(DOMAIN_ID_PARAM);
    return userManager.getUserIdByLoginAndDomain(login, domainId);
  }

  @Override
  public UserFull getUserFull(String sUserId) throws AdminException {
    return userManager.getUserFull(sUserId);
  }

  @Override
  public List<UserFull> getUserFulls(final Collection<String> userIds) throws AdminException {
    return userManager.listUserFulls(userIds);
  }

  @Override
  public UserFull getUserFull(String domainId, String specificId) throws AdminException {
    DomainDriver synchroDomain = domainDriverManager.getDomainDriver(domainId);
    return synchroDomain.getUserFull(specificId);
  }

  @Override
  public String addUser(UserDetail userDetail) throws AdminException {
    try {
      return addUser(userDetail, false);
    } catch (Exception e) {
      throw new AdminException(failureOnAdding("user", userDetail.getDisplayedName()), e);
    }
  }

  @Override
  public String addUser(UserDetail userDetail, boolean addOnlyInSilverpeas) throws AdminException {
    try {
      // add user
      return userManager.addUser(userDetail, addOnlyInSilverpeas, true);
    } catch (Exception e) {
      throw new AdminException(failureOnAdding("user", userDetail.getDisplayedName()), e);
    }
  }

  @Override
  public void migrateUser(UserDetail userDetail, String targetDomainId) throws AdminException {
    try {
      userManager.migrateUser(userDetail, targetDomainId);
      cache.opUpdateUser(userDetail);
    } catch (Exception e) {
      throw new AdminException(
          failureOnAdding(USER + userDetail.getId(), "in domain " + targetDomainId), e);
    }
  }

  @Override
  public void blockUser(String userId) throws AdminException {
    updateUserState(userId, UserState.BLOCKED);
  }

  @Override
  public void unblockUser(String userId) throws AdminException {
    updateUserState(userId, UserState.VALID);
  }

  @Override
  public void deactivateUser(String userId) throws AdminException {
    updateUserState(userId, UserState.DEACTIVATED);
  }

  @Override
  public void activateUser(String userId) throws AdminException {
    updateUserState(userId, UserState.VALID);
  }

  private void updateUserState(String userId, UserState state) throws AdminException {
    try {
      UserDetail user = UserDetail.getById(userId);
      user.setState(state);
      user.setStateSaveDate(new Date());
      updateUser(user);
    } catch (Exception e) {
      throw new AdminException(failureOnUpdate("state of user " + userId, "to " + state.getName()),
          e);
    }
  }

  @Override
  public void userAcceptsTermsOfService(String userId) throws AdminException {
    try {
      UserDetail user = UserDetail.getById(userId);
      user.setTosAcceptanceDate(DateUtil.getNow());
      updateUser(user);
    } catch (Exception e) {
      throw new AdminException(failureOnUpdate("terms of service acceptance for user", userId), e);
    }
  }

  @Override
  public String restoreUser(final String sUserId) throws AdminException {
    final UserDetail user = getUserDetail(sUserId);
    if (user == null) {
      throw new AdminException(unknown("user", sUserId));
    }
    try {
      final String removedUserId = userManager.restoreUser(user, true);
      cache.resetCache();
      return removedUserId;
    } catch (Exception e) {
      throw new AdminException(failureOnRemoving("user", sUserId), e);
    }
  }

  @Override
  public String removeUser(final String sUserId) throws AdminException {
    if (ADMIN_ID.equals(sUserId)) {
      SilverLogger.getLogger(this)
          .warn("Attempt to remove the main administrator account by user " +
              User.getCurrentRequester().getId());
      return null;
    }
    final UserDetail user = getUserDetail(sUserId);
    if (user == null) {
      throw new AdminException(unknown("user", sUserId));
    }
    try {
      final String removedUserId = userManager.removeUser(user, true);
      cache.opRemoveUser(user);
      return removedUserId;
    } catch (Exception e) {
      throw new AdminException(failureOnRemoving("user", sUserId), e);
    }
  }

  @Override
  public String deleteUser(String sUserId) throws AdminException {
    try {
      if (ADMIN_ID.equals(sUserId)) {
        SilverLogger.getLogger(this)
            .warn("Attempt to delete the main administrator account by user " +
                User.getCurrentRequester().getId());
        return null;
      }

      return deleteUser(sUserId, false);

    } catch (Exception e) {
      throw new AdminException(failureOnDeleting("user", sUserId), e);
    }
  }

  @Override
  public String deleteUser(String sUserId, boolean onlyInSilverpeas) throws AdminException {
    UserDetail user;
    try {
      user = getUserDetail(sUserId);
      if (user == null) {
        throw new AdminException(unknown("user", sUserId));
      }

      // Delete the user
      String sReturnUserId = userManager.deleteUser(user, onlyInSilverpeas);

      cache.opRemoveUser(user);
      return sReturnUserId;
    } catch (Exception e) {
      throw new AdminException(failureOnDeleting("user", sUserId), e);
    }
  }

  @Override
  public String updateUser(UserDetail user) throws AdminException {
    try {
      // Update user
      String sUserId = userManager.updateUser(user, true);

      cache.opUpdateUser(userManager.getUserDetail(sUserId));

      return sUserId;
    } catch (Exception e) {
      throw new AdminException(failureOnUpdate("user", user.getId()), e);
    }
  }

  @Override
  public String updateUserFull(UserFull user) throws AdminException {
    try {
      // Update user
      String sUserId = userManager.updateUserFull(user);

      cache.opUpdateUser(userManager.getUserDetail(sUserId));

      return sUserId;
    } catch (Exception e) {
      throw new AdminException(failureOnUpdate("user", user.getId()), e);
    }
  }

  // -------------------------------------------------------------------------
  // CONVERSION CLIENT <--> DRIVER SPACE ID
  // -------------------------------------------------------------------------

  /**
   * Converts client space id to driver space id
   */
  private int getDriverSpaceId(String sClientSpaceId) {
    String clientSpaceId = sClientSpaceId;
    if (clientSpaceId != null) {
      if (clientSpaceId.startsWith(SpaceInst.SPACE_KEY_PREFIX)) {
        clientSpaceId = clientSpaceId.substring(SpaceInst.SPACE_KEY_PREFIX.length());
      }
      try {
        return Integer.parseInt(clientSpaceId);
      } catch (NumberFormatException e) {
        SilverLogger.getLogger(this)
            .warn("can not get driver space id from {0} : {1}", sClientSpaceId, e.getMessage());
      }
    }
    return -1;
  }

  @Override
  public String getClientSpaceId(String sDriverSpaceId) {
    if (sDriverSpaceId != null && !sDriverSpaceId.startsWith(SpaceInst.SPACE_KEY_PREFIX)) {
      return SpaceInst.SPACE_KEY_PREFIX + sDriverSpaceId;
    }
    return sDriverSpaceId;
  }

  @Override
  public String[] getClientSpaceIds(String[] asDriverSpaceIds) {
    String[] asClientSpaceIds = new String[asDriverSpaceIds.length];
    for (int nI = 0; nI < asDriverSpaceIds.length; nI++) {
      asClientSpaceIds[nI] = getClientSpaceId(asDriverSpaceIds[nI]);
    }
    return asClientSpaceIds;
  }

  private Integer getDriverComponentId(String sClientComponentId) {
    if (sClientComponentId == null) {
      return null;
    }

    return getTableClientComponentIdFromClientComponentId(sClientComponentId);
  }

  /**
   * @return 23 for parameter kmelia23
   */
  private Integer getTableClientComponentIdFromClientComponentId(String sClientComponentId) {
    String sTableClientId = "";

    // Remove the component name to get the table client id
    char[] cBuf = sClientComponentId.toCharArray();
    if (Character.isDigit(cBuf[cBuf.length - 1])) {
      for (int nI = 0; nI < cBuf.length && sTableClientId.length() == 0; nI++) {
        if (Character.isDigit(cBuf[nI])) {
          sTableClientId = sClientComponentId.substring(nI);
        }
      }
      if (StringUtil.isDefined(sTableClientId)) {
        return Integer.parseInt(sTableClientId);
      }
    }
    return -1;
  }

  /**
   * Return kmelia23 for parameter 23
   */
  private String getClientComponentId(ComponentInst component) {
    return getClientComponentId(component.getName(), component.getId());
  }

  private String getClientComponentId(String componentName, String sDriverComponentId) {
    if (StringUtil.isInteger(sDriverComponentId)) {
      return componentName + sDriverComponentId;
    }
    // id is already in client format
    return sDriverComponentId;
  }

  // -------------------------------------------------------------------------
  // DOMAIN QUERY
  // -------------------------------------------------------------------------
  @Override
  public String getNextDomainId() throws AdminException {
    try {
      return domainDriverManager.getNextDomainId();
    } catch (Exception e) {
      throw new AdminException(e.getMessage(), e);
    }
  }

  @Override
  public String addDomain(Domain theDomain) throws AdminException {
    try {
      String id = domainDriverManager.createDomain(theDomain);

      // Update the synchro scheduler
      DomainDriver domainDriver = domainDriverManager.getDomainDriver(id);
      if (domainDriver.isSynchroThreaded()) {
        domainSynchroScheduler.addDomain(id);
      }

      return id;
    } catch (Exception e) {
      throw new AdminException(failureOnAdding(DOMAIN, theDomain.getName()), e);
    }
  }

  @Override
  public String updateDomain(Domain domain) throws AdminException {
    try {
      domainCache.removeDomain(domain.getId());
      return domainDriverManager.updateDomain(domain);
    } catch (Exception e) {
      throw new AdminException(failureOnUpdate(DOMAIN, domain.getId()), e);
    }
  }

  @Override
  public String removeDomain(String domainId) throws AdminException {
    try {
      // Remove all users
      UserDetail[] toRemoveUDs = userManager.getAllUsersInDomain(domainId, true);
      if (toRemoveUDs != null) {
        SilverLogger.getLogger(this).debug("[Domain deletion] Remove all the users...");
        for (final UserDetail user : toRemoveUDs) {
          deleteUser(user.getId(), false);
        }
      }

      // Remove all groups
      GroupDetail[] toRemoveGroups = groupManager.getRootGroupsOfDomain(domainId);
      if (toRemoveGroups != null) {
        SilverLogger.getLogger(this).debug("[Domain deletion] Remove all the groups...");
        for (final GroupDetail group : toRemoveGroups) {
          deleteGroupById(group.getId(), false);
        }
      }

      SilverLogger.getLogger(this).debug("[Domain deletion] Then remove the domain...");
      // Remove the domain
      domainDriverManager.removeDomain(domainId);
      // Update the synchro scheduler
      domainSynchroScheduler.removeDomain(domainId);
      domainCache.removeDomain(domainId);

      return domainId;
    } catch (Exception e) {
      throw new AdminException(failureOnDeleting(DOMAIN, domainId), e);
    }
  }

  @Override
  public Domain[] getAllDomains() throws AdminException {
    try {
      return domainDriverManager.getAllDomains();
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("all domains", "in Silverpeas"), e);
    }
  }

  @Override
  public List<String> getAllDomainIdsForLogin(String login) throws AdminException {
    return userManager.getDomainsByUserLogin(login);
  }

  @Override
  public Domain getDomain(String domainId) throws AdminException {
    try {
      if (!StringUtil.isDefined(domainId) || !StringUtil.isInteger(domainId)) {
        domainId = "-1";
      }
      final Domain domain;
      Optional<Domain> optionalDomain = domainCache.getDomain(domainId);
      if (optionalDomain.isEmpty()) {
        domain = domainDriverManager.getDomain(domainId);
        domainCache.addDomain(domain);
      } else {
        domain = optionalDomain.get();
      }
      return domain;
    } catch (AdminNotFoundException e) {
      SilverLogger.getLogger(this).warn(e.getMessage());
      return null;
    }
  }

  @Override
  public long getDomainActions(String domainId) throws AdminException {
    try {
      if (domainId != null && domainId.equals("-1")) {
        return ACTION_MASK_MIXED_GROUPS;
      }
      return domainDriverManager.getDomainActions(domainId);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("actions of domain", domainId), e);
    }
  }

  @Override
  public GroupDetail[] getRootGroupsOfDomain(String domainId) throws AdminException {
    try {
      return groupManager.getRootGroupsOfDomain(domainId);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("root groups of domain", domainId), e);
    }
  }

  @Override
  public List<GroupDetail> getSynchronizedGroups() throws AdminException {
    try {
      return groupManager.getSynchronizedGroups();
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("synchronized groups", ""), e);
    }
  }

  @Override
  public UserDetail[] getAllUsersOfGroup(String groupId) throws AdminException {
    try {
      List<String> groupIds = new ArrayList<>();
      groupIds.add(groupId);
      groupIds.addAll(groupManager.getAllSubGroupIdsRecursively(groupId));
      return userManager.getAllUsersInGroups(groupIds);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("all users in group", groupId), e);
    }
  }

  @Override
  public UserDetail[] getUsersOfDomain(String domainId) throws AdminException {
    try {
      if ("-1".equals(domainId)) {
        return new UserDetail[0];
      }
      return userManager.getAllUsersInDomain(domainId, false);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("all users in domain", domainId), e);
    }
  }

  @Override
  public List<UserDetail> getUsersOfDomains(List<String> domainIds) throws AdminException {
    return userManager.getUsersOfDomains(domainIds);
  }

  @Override
  public List<UserDetail> getUsersOfDomainsFromNewestToOldest(List<String> domainIds)
      throws AdminException {
    return userManager.getUsersOfDomainsFromNewestToOldest(domainIds);
  }

  @Override
  public String[] getUserIdsOfDomain(String domainId) throws AdminException {
    try {
      if ("-1".equals(domainId)) {
        return ArrayUtil.emptyStringArray();
      }
      List<String> userIds = userManager.getAllUserIdsInDomain(domainId);
      return userIds.toArray(new String[0]);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("all users in domain", domainId), e);
    }
  }

  // -------------------------------------------------------------------------
  // USERS QUERY
  // -------------------------------------------------------------------------
  @Override
  public String identify(String sKey, String sSessionId, boolean isAppInMaintenance)
      throws AdminException {
    return identify(sKey, sSessionId, isAppInMaintenance, true);
  }

  @Override
  public String identify(String sKey, String sSessionId, boolean isAppInMaintenance,
      boolean removeKey) throws AdminException {
    String sUserId;
    try {
      // Authenticate the given user
      Map<String, String> loginDomain = domainDriverManager.authenticate(sKey, removeKey);
      if ((!loginDomain.containsKey(LOGIN_PARAM)) || (!loginDomain.containsKey(DOMAIN_ID_PARAM))) {
        throw new AdminException(undefined("domain for authentication key " + sKey));
      }

      // Get the Silverpeas userId
      String sLogin = loginDomain.get(LOGIN_PARAM);
      String sDomainId = loginDomain.get(DOMAIN_ID_PARAM);

      DomainDriver synchroDomain = domainDriverManager.getDomainDriver(sDomainId);
      // Get the user id or import it if the domain accept it
      sUserId = synchroGetUserId(isAppInMaintenance, sLogin, sDomainId, synchroDomain);
      // Synchronize the user if the domain needs it
      doSynchronizeUser(sUserId, synchroDomain, isAppInMaintenance);

      return sUserId;
    } catch (Exception e) {
      throw new AdminException("Fail to identify authentication key " + sKey, e);
    }
  }

  private void doSynchronizeUser(final String sUserId, final DomainDriver synchroDomain,
      final boolean isAppInMaintenance) {
    if (synchroDomain.isSynchroOnLoginEnabled() && !isAppInMaintenance) {
      try {
        synchronizeUser(sUserId, synchroDomain.isSynchroOnLoginRecursToGroups());
      } catch (Exception ex) {
        SilverLogger.getLogger(this).error(ex);
      }
    }
  }

  private String synchroGetUserId(final boolean isAppInMaintenance, final String sLogin,
      final String sDomainId, final DomainDriver synchroDomain) throws AdminException {
    String sUserId;
    try {
      sUserId = userManager.getUserIdByLoginAndDomain(sLogin, sDomainId);
    } catch (Exception ex) {
      if (synchroDomain.isSynchroOnLoginEnabled() && !isAppInMaintenance) {//Try to import new user
        SilverLogger.getLogger(this)
            .warn("User with login {0} in domain {1} not found", sLogin, sDomainId);
        sUserId = synchronizeImportUserByLogin(sDomainId, sLogin,
            synchroDomain.isSynchroOnLoginRecursToGroups());
      } else {
        throw ex;
      }
    }
    return sUserId;
  }

  // ---------------------------------------------------------------------------------------------
  // QUERY FUNCTIONS
  // ---------------------------------------------------------------------------------------------
  @Override
  public List<GroupDetail> getDirectGroupsOfUser(String userId) throws AdminException {
    try {
      return groupManager.getDirectGroupsOfUser(userId);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("direct groups of user", userId), e);
    }
  }

  @Override
  public String[] getUserSpaceIds(String sUserId) throws AdminException {
    List<String> spaceIds = new ArrayList<>();

    // getting all available components
    List<String> componentIds = getAllowedComponentIds(sUserId);
    for (String componentId : componentIds) {
      List<SpaceInstLight> spaces = treeCache.getComponentPath(componentId);
      for (SpaceInstLight space : spaces) {
        if (!spaceIds.contains(space.getId())) {
          spaceIds.add(space.getId());
        }
      }
    }

    return spaceIds.toArray(new String[0]);
  }

  private List<String> getAllGroupsOfUser(String userId) throws AdminException {
    final List<String> allGroupsOfUser;
    Optional<List<String>> optionalGroups = groupCache.getAllGroupIdsOfUser(userId);
    if (optionalGroups.isEmpty()) {
      // group ids of user are not yet processed and store it in cache
      allGroupsOfUser = groupManager.getAllGroupsOfUser(userId);
      // store groupIds of user in cache
      groupCache.setAllGroupIdsOfUser(userId, allGroupsOfUser);
    } else {
      allGroupsOfUser = optionalGroups.get();
    }
    return allGroupsOfUser;
  }

  private List<String> getAllowedComponentIds(String userId) throws AdminException {
    return getAllowedComponentIds(userId, null);
  }

  private List<String> getAllowedComponentIds(String userId, String componentName)
      throws AdminException {
    // getting all groups of users
    List<String> allGroupsOfUser = getAllGroupsOfUser(userId);

    return componentManager.getAllowedComponentIds(Integer.parseInt(userId), allGroupsOfUser, null,
        componentName);
  }

  @Override
  public String[] getUserRootSpaceIds(String sUserId) throws AdminException {
    try {
      // getting all available components
      final List<String> componentIds = getAllowedComponentIds(sUserId);
      return getRootSpaceIds(componentIds);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("root spaces accessible by user", sUserId), e);
    }
  }

  private String[] getRootSpaceIds(List<String> componentIds) throws AdminException {
    final Set<String> indexedComponentIds = new HashSet<>(componentIds);
    // getting all root spaces (sorted)
    final String[] rootSpaceIds = getAllRootSpaceIds();
    // retain only allowed root spaces
    return Stream.of(rootSpaceIds)
        .filter(i -> isSpaceContainsOneComponent(indexedComponentIds, getDriverSpaceId(i)))
        .toArray(String[]::new);
  }

  @Override
  public String[] getUserSubSpaceIds(String sUserId, String spaceId) throws AdminException {
    try {
      // getting all available components
      final List<String> componentIds = getAllowedComponentIds(sUserId);
      return getSubSpaceIds(componentIds, spaceId, true);
    } catch (Exception e) {
      throw new AdminException(
          failureOnGetting(SUBSPACES_OF_SPACE + spaceId, ACCESSIBLE_BY_USER + sUserId), e);
    }
  }

  private String[] getSubSpaceIds(final List<String> componentIds, final String spaceId,
      final boolean ignoreEmptySpaces) {
    Stream<SpaceInstLight> subspaces = treeCache.getSubSpaces(getDriverSpaceId(spaceId)).stream();
    if (ignoreEmptySpaces) {
      final HashSet<String> indexedComponentIds = new HashSet<>(componentIds);
      subspaces =
          subspaces.filter(s -> isSpaceContainsOneComponent(indexedComponentIds, s.getLocalId()));
    }
    return subspaces.map(SpaceInstLight::getId).toArray(String[]::new);
  }

  @Override
  public boolean isSpaceAvailable(String userId, String spaceId) throws AdminException {
    return getUserSpaceAvailabilityChecker(userId).isAvailable(spaceId);
  }

  @Override
  public UserSpaceAvailabilityChecker getUserSpaceAvailabilityChecker(final String userId)
      throws AdminException {
    return new UserSpaceAvailabilityChecker(userId, getAllowedComponentIds(userId),
        this::getDriverSpaceId, this::isSpaceContainsOneComponent);
  }

  private boolean isSpaceContainsOneComponent(Set<String> componentIds, int spaceId) {
    final List<ComponentInstLight> components = treeCache.getComponents(spaceId);
    if (components.stream().noneMatch(c -> componentIds.contains(c.getId()))) {
      // check in subspaces
      final List<SpaceInstLight> subspaces = treeCache.getSubSpaces(spaceId);
      return subspaces.stream()
          .anyMatch(s -> isSpaceContainsOneComponent(componentIds, s.getLocalId()));
    }
    // There is at least one component available
    return true;
  }

  @Override
  public List<SpaceInstLight> getSubSpaces(String spaceId) throws AdminException {
    return spaceManager.getSubSpaces(getDriverSpaceId(spaceId));
  }

  @Override
  public List<ComponentInstLight> getAvailCompoInSpace(String userId, String spaceId)
      throws AdminException {

    try {
      List<String> allowedComponentIds = getAllowedComponentIds(userId);

      List<ComponentInstLight> allowedComponents = new ArrayList<>();

      List<ComponentInstLight> allComponents =
          treeCache.getComponentsInSpaceAndSubspaces(getDriverSpaceId(spaceId));
      for (ComponentInstLight component : allComponents) {
        if (allowedComponentIds.contains(component.getId())) {
          allowedComponents.add(component);
        }
      }
      return allowedComponents;
    } catch (Exception e) {
      throw new AdminException(
          failureOnGetting(COMPONENTS_IN_SPACE + spaceId, ACCESSIBLE_BY_USER + userId), e);
    }
  }

  @Override
  public List<SpaceInstLight> getUserSpaceTreeview(String userId) throws AdminException {
    final Set<String> componentIds = new HashSet<>(Arrays.asList(getAvailCompoIds(userId)));
    final Set<Integer> authorizedIds = new HashSet<>(100);
    while (!componentIds.isEmpty()) {
      String componentId = componentIds.iterator().next();
      componentIds.remove(componentId);
      Optional<SpaceInstLight> space = treeCache.getSpaceContainingComponent(componentId);
      space.ifPresent(s -> addAuthorizedSpace(authorizedIds, componentIds, s));
    }
    String[] rootSpaceIds = getAllRootSpaceIds(userId);
    List<SpaceInstLight> treeview = new ArrayList<>(authorizedIds.size());
    for (String spaceId : rootSpaceIds) {
      int currentSpaceId = getDriverSpaceId(spaceId);
      if (authorizedIds.contains(currentSpaceId)) {
        Optional<SpaceInstLight> optionalSpace = treeCache.getSpaceInstLight(currentSpaceId);
        optionalSpace.ifPresent(s -> {
          treeview.add(s);
          addAuthorizedSpaceToTree(treeview, authorizedIds, currentSpaceId, 1);
        });
      }
    }
    return treeview;
  }

  void addAuthorizedSpaceToTree(List<SpaceInstLight> treeview, Set<Integer> authorizedIds,
      int spaceId, int level) {
    List<SpaceInstLight> subSpaces = treeCache.getSubSpaces(spaceId);
    for (SpaceInstLight space : subSpaces) {
      int subSpaceId = space.getLocalId();
      if (authorizedIds.contains(subSpaceId)) {
        space.setLevel(level);
        treeview.add(space);
        addAuthorizedSpaceToTree(treeview, authorizedIds, subSpaceId, level + 1);
      }
    }
  }

  /**
   * @param spaces list of authorized spaces built by this method
   * @param componentsId list of components' id (base to get authorized spaces)
   * @param space a space candidate to be in authorized spaces list
   */
  private void addAuthorizedSpace(Set<Integer> spaces, Set<String> componentsId,
      SpaceInstLight space) {
    if (!SpaceInst.STATUS_REMOVED.equals(space.getStatus()) &&
        !spaces.contains(space.getLocalId())) {
      int spaceId = space.getLocalId();
      spaces.add(spaceId);
      treeCache.getComponentIds(spaceId)
          .forEach(componentsId::remove);
      if (!space.isRoot()) {
        int fatherId = getDriverSpaceId(space.getFatherId());
        if (!spaces.contains(fatherId)) {
          Optional<SpaceInstLight> parent = treeCache.getSpaceInstLight(fatherId);
          parent.ifPresent(p -> addAuthorizedSpace(spaces, componentsId, p));
        }
      }
    }
  }

  @Override
  public SpaceWithSubSpacesAndComponents getFullTreeview() throws AdminException {
    return getFullTreeview(componentManager.getAllActiveComponentIds(), false);
  }

  @Override
  public SpaceWithSubSpacesAndComponents getAllowedFullTreeview(String userId)
      throws AdminException {
    return getFullTreeview(getAllowedComponentIds(userId), true);
  }

  @Override
  public SpaceWithSubSpacesAndComponents getAllowedFullTreeviewOnComponentName(final String userId,
      final String componentName) throws AdminException {
    return getFullTreeview(getAllowedComponentIds(userId, componentName), true);
  }

  private SpaceWithSubSpacesAndComponents getFullTreeview(final List<String> componentIds,
      final boolean ignoreEmptySpaces) throws AdminException {
    final String[] spaceIds = getRootSpaceIds(componentIds);
    final SpaceWithSubSpacesAndComponents root =
        new SpaceWithSubSpacesAndComponents(new SpaceInstLight());
    final List<SpaceWithSubSpacesAndComponents> spaces = new ArrayList<>();
    for (final String spaceId : spaceIds) {
      spaces.add(geTreeview(componentIds, spaceId, ignoreEmptySpaces));
    }
    root.setSubSpaces(spaces);
    return root;
  }

  @Override
  public SpaceWithSubSpacesAndComponents getAllowedFullTreeview(String userId, String spaceId)
      throws AdminException {
    final List<String> componentIds = getAllowedComponentIds(userId);
    return geTreeview(componentIds, spaceId, true);
  }

  @Override
  public List<GroupDetail> getRemovedGroups(final String... domainIds) throws AdminException {
    return groupManager.getRemovedGroupsOfDomains(domainIds);
  }

  @Override
  public List<UserDetail> getRemovedUsers(final String... domainIds) throws AdminException {
    return userManager.getRemovedUsersOfDomains(domainIds);
  }

  @Override
  public List<UserDetail> getNonBlankedDeletedUsers(final String... domainIds)
      throws AdminException {
    return userManager.getNonBlankedDeletedUsersOfDomains(domainIds);
  }

  @Override
  public void blankDeletedUsers(final String targetDomainId, final List<String> userIds)
      throws AdminException {
    final UserDetail[] users = getUserDetails(userIds.toArray(new String[0]));
    for (final UserDetail user : users) {
      if (user.getDomainId().equals(targetDomainId)) {
        String userName = user.getDisplayedName();
        userManager.blankUser(user);
        cache.opUpdateUser(userManager.getUserDetail(user.getId()));
        SilverLogger.getLogger(this).info("User " + userName + " blanked");
      }
    }
  }

  private SpaceWithSubSpacesAndComponents geTreeview(List<String> componentIds, String spaceId,
      final boolean ignoreEmptySpaces) throws AdminException {
    final SpaceInstLight spaceInst = getSpaceInstLight(getDriverSpaceId(spaceId));
    final SpaceWithSubSpacesAndComponents space = new SpaceWithSubSpacesAndComponents(spaceInst);
    // process subspaces
    final String[] subSpaceIds = getSubSpaceIds(componentIds, spaceId, ignoreEmptySpaces);
    final List<SpaceWithSubSpacesAndComponents> subSpaces = new ArrayList<>(subSpaceIds.length);
    for (final String subSpaceId : subSpaceIds) {
      subSpaces.add(geTreeview(componentIds, subSpaceId, ignoreEmptySpaces));
    }
    space.setSubSpaces(subSpaces);
    // process components
    final List<SilverpeasComponentInstance> spaceComponents =
        treeCache.getComponents(getDriverSpaceId(spaceId))
            .stream()
            .filter(c -> componentIds.contains(c.getId()))
            .collect(toList());
    space.setComponents(spaceComponents);
    return space;
  }

  @Override
  public String[] getAllowedSubSpaceIds(String userId, String spaceFatherId) throws AdminException {
    return getUserSubSpaceIds(userId, spaceFatherId);
  }

  private SpaceInstLight getSpaceInstLight(int spaceId) throws AdminException {
    Optional<SpaceInstLight> optionalSpace = treeCache.getSpaceInstLight(spaceId);
    final SpaceInstLight sil = optionalSpace.isPresent() ? optionalSpace.get() :
        spaceManager.getSpaceInstLightById(spaceId);
    if (sil != null && sil.getLevel() == -1) {
      sil.setLevel(treeCache.getSpaceLevel(spaceId));
    }
    return sil;
  }

  @Override
  public SpaceInstLight getSpaceInstLightById(String sClientSpaceId) throws AdminException {
    try {
      return getSpaceInstLight(getDriverSpaceId(sClientSpaceId));
    } catch (Exception e) {
      throw new AdminException(failureOnGetting(SPACE, sClientSpaceId), e);
    }
  }

  @Override
  public SpaceInstLight getRootSpace(String spaceId) throws AdminException {
    SpaceInstLight sil = getSpaceInstLight(getDriverSpaceId(spaceId));
    while (sil != null && !sil.isRoot()) {
      sil = getSpaceInstLight(getDriverSpaceId(sil.getFatherId()));
    }
    return sil;
  }

  @Override
  public String[] getGroupManageableSpaceIds(String sGroupId) throws AdminException {
    String[] asManageableSpaceIds;
    ArrayList<String> alManageableSpaceIds = new ArrayList<>();
    try {
      // Get user manageable space ids from database
      List<String> groupIds = new ArrayList<>();
      groupIds.add(sGroupId);
      List<Integer> manageableSpaceIds = spaceManager.getManageableSpaceIds(null, groupIds);

      // Inherits manageability rights for space children
      String[] childSpaceIds;
      for (Integer spaceId : manageableSpaceIds) {
        String asManageableSpaceId = String.valueOf(spaceId);
        // add manageable space id in result
        if (!alManageableSpaceIds.contains(asManageableSpaceId)) {
          alManageableSpaceIds.add(asManageableSpaceId);
        }

        // calculate manageable space's children
        childSpaceIds = spaceManager.getAllSubSpaceIds(spaceId);
        // add them in result
        for (String childSpaceId : childSpaceIds) {
          if (!alManageableSpaceIds.contains(childSpaceId)) {
            alManageableSpaceIds.add(childSpaceId);
          }
        }
      }

      // Put user manageable space ids in cache
      asManageableSpaceIds = alManageableSpaceIds.toArray(new String[0]);

      return asManageableSpaceIds;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("spaces manageable by group", sGroupId), e);
    }
  }

  @Override
  public String[] getUserManageableSpaceIds(String sUserId) throws AdminException {
    final String[] result;
    try {
      // Get user manageable space ids from cache
      final Optional<String[]> cachedSpaceIds = cache.getManageableSpaceIds(sUserId);
      if (cachedSpaceIds.isEmpty()) {
        final List<String> groupIds = getAllGroupsOfUser(sUserId);
        // Get space ids on which the user is specifically indicated as manager
        final Integer[] directSpacedIds = userManager.getManageableSpaceIds(sUserId, groupIds);
        final List<SpaceInst> consumer = new ArrayList<>();
        for(Integer directSpaceId : directSpacedIds) {
          consumer.add(getSpaceInstById(String.valueOf(directSpaceId)));
        }
        // Identifying all managed space ids
        final Set<String> allSpaces = new HashSet<>();
        while (!consumer.isEmpty()) {
          final SpaceInst current = consumer.remove(0);
          allSpaces.add(String.valueOf(current.getLocalId()));
          consumer.addAll(current.getSubSpaces());
        }
        result = allSpaces.toArray(new String[0]);
        cache.putManageableSpaceIds(sUserId, result);
      } else {
        result = cachedSpaceIds.get();
      }
      return result;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("spaces manageable by user", sUserId), e);
    }
  }

  @Override
  public String[] getUserManageableSpaceRootIds(String sUserId) throws AdminException {
    try {
      // Get user manageable space ids from database
      List<String> groupIds = getAllGroupsOfUser(sUserId);
      Integer[] asManageableSpaceIds = userManager.getManageableSpaceIds(sUserId, groupIds);

      // retain only root spaces
      List<String> manageableRootSpaceIds = new ArrayList<>();
      for (Integer asManageableSpaceId : asManageableSpaceIds) {
        Optional<SpaceInstLight> space = treeCache.getSpaceInstLight(asManageableSpaceId);
        space.filter(SpaceInstLight::isRoot)
            .ifPresent(s -> manageableRootSpaceIds.add(asManageableSpaceId.toString()));
      }
      return manageableRootSpaceIds.toArray(new String[0]);

    } catch (Exception e) {
      throw new AdminException(failureOnGetting("root spaces manageable by user", sUserId), e);
    }
  }

  @Override
  public String[] getUserManageableSubSpaceIds(String sUserId, String sParentSpaceId)
      throws AdminException {
    try {
      // Get user manageable space ids from database
      List<String> groupIds = getAllGroupsOfUser(sUserId);
      Integer[] asManageableSpaceIds = userManager.getManageableSpaceIds(sUserId, groupIds);

      int parentSpaceId = getDriverSpaceId(sParentSpaceId);

      // retain only sub spaces
      boolean find;
      List<String> manageableRootSpaceIds = new ArrayList<>();
      for (Integer manageableSpaceId : asManageableSpaceIds) {
        find = false;
        Optional<SpaceInstLight> space = treeCache.getSpaceInstLight(manageableSpaceId);
        while (space.isPresent() && !space.get().isRoot() && !find) {
          int driverFatherId = getDriverSpaceId(space.get().getFatherId());
          if (parentSpaceId == driverFatherId) {
            manageableRootSpaceIds.add(String.valueOf(manageableSpaceId));
            find = true;
          } else {
            space = treeCache.getSpaceInstLight(driverFatherId);
          }
        }
      }
      return manageableRootSpaceIds.toArray(new String[0]);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting(SUBSPACES_OF_SPACE + sParentSpaceId,
          "that are manageable by user" + sUserId), e);
    }
  }

  @Override
  public SpaceProfile getSpaceProfile(String spaceId, SilverpeasRole role) throws AdminException {
    SpaceProfile spaceProfile = new SpaceProfile();
    SpaceInst space = getSpaceInstById(spaceId);

    // get profile explicitly defined
    SpaceProfileInst profile = space.getSpaceProfileInst(role.getName());
    if (profile != null) {
      spaceProfile.setProfile(profile);
    }

    if (role == SilverpeasRole.MANAGER) {
      // get groups and users implicitly inherited from space parents
      boolean root = space.isRoot();
      String parentId = space.getDomainFatherId();
      while (!root) {
        SpaceInst parent = getSpaceInstById(parentId);
        SpaceProfileInst parentProfile = parent.getSpaceProfileInst(role.getName());
        spaceProfile.addInheritedProfile(parentProfile);
        root = parent.isRoot();
        parentId = parent.getDomainFatherId();
      }
    } else {
      // get groups and users from inherited profile
      spaceProfile.addInheritedProfile(space.getInheritedSpaceProfileInst(role.getName()));
    }

    return spaceProfile;
  }

  @Override
  public List<String> getUserManageableGroupIds(String sUserId) throws AdminException {
    try {
      // get all groups of user
      List<String> groupIds = getAllGroupsOfUser(sUserId);

      return groupManager.getManageableGroupIds(sUserId, groupIds);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("groups manageable by user", sUserId), e);
    }
  }

  @Override
  public String[] getAvailCompoIds(String sClientSpaceId, String sUserId) throws AdminException {
    final String[] asAvailCompoIds;

    try {
      // Converts client space id to driver space id
      int spaceId = getDriverSpaceId(sClientSpaceId);

      // Get available component ids from cache
      Optional<String[]> optionalInstanceIds = cache.getAvailCompoIds(spaceId, sUserId);

      if (optionalInstanceIds.isEmpty()) {
        // Get available component ids from database
        asAvailCompoIds = getAvailableInstanceIds(sClientSpaceId, sUserId);
        // Store available component ids in cache
        cache.putAvailCompoIds(String.valueOf(spaceId), sUserId, asAvailCompoIds);
      } else {
        asAvailCompoIds = optionalInstanceIds.get();
      }
      return asAvailCompoIds;

    } catch (Exception e) {
      throw new AdminException(
          failureOnGetting(COMPONENTS_IN_SPACE + sClientSpaceId, AVAILABLE_TO_USER + sUserId), e);
    }
  }

  private String[] getAvailableInstanceIds(final String clientSpaceId, final String userId)
      throws AdminException {
    final List<String> componentIds = new ArrayList<>();
    final List<ComponentInstLight> components = getAvailCompoInSpace(userId, clientSpaceId);
    for (ComponentInstLight component : components) {
      componentIds.add(component.getId());
    }
    return componentIds.toArray(new String[0]);
  }

  @Override
  public boolean isAnAdminTool(String toolId) {
    return Constants.ADMIN_COMPONENT_ID.equals(toolId);
  }

  @Override
  public List<String> getAvailableComponentsByUser(final String userId) throws AdminException {
    try {
      return getAllowedComponentIds(userId);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("components available by user", userId), e);
    }
  }

  @Override
  public boolean isComponentAvailableToUser(String componentId, String userId)
      throws AdminException {
    try {
      return getAllowedComponentIds(userId).contains(componentId);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("components available by user", userId), e);
    }
  }

  @Override
  public boolean isComponentAvailableToGroup(final String componentId, final String groupId)
      throws AdminException {
    List<String> groupIds = groupManager.getPathToGroup(groupId);
    groupIds.add(groupId);
    return componentManager.getAllowedComponentIds(-1, groupIds).contains(componentId);
  }

  @Override
  public boolean isComponentManageable(String componentId, String userId) throws AdminException {
    boolean manageable = getUserDetail(userId).isAccessAdmin();
    if (!manageable) {
      // check if user is manager of at least one space parent
      List<String> toCheck = Arrays.asList(getUserManageableSpaceIds(userId));
      List<SpaceInstLight> path = getPathToComponent(componentId);
      for (SpaceInstLight space : path) {
        if (toCheck.contains(String.valueOf(space.getLocalId()))) {
          manageable = true;
          break;
        }
      }
    }
    return manageable;
  }

  @Override
  public String[] getAvailCompoIdsAtRoot(String sClientSpaceId, String sUserId)
      throws AdminException {
    try {
      // Converts client space id to driver space id
      int spaceId = getDriverSpaceId(sClientSpaceId);
      List<String> groupIds = getAllGroupsOfUser(sUserId);
      List<String> asAvailCompoIds =
          componentManager.getAllowedComponentIds(Integer.parseInt(sUserId), groupIds, spaceId);

      return asAvailCompoIds.toArray(new String[0]);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("root components in space " + sClientSpaceId,
          AVAILABLE_TO_USER + sUserId), e);
    }
  }

  @Override
  public List<String> getAvailCompoIdsAtRoot(String sClientSpaceId, String sUserId,
      String componentNameRoot) throws AdminException {

    try {
      // Converts client space id to driver space id
      int spaceId = getDriverSpaceId(sClientSpaceId);

      // Get available component ids from database
      List<ComponentInstLight> components = treeCache.getComponents(spaceId);

      List<String> allowedComponentIds = getAllowedComponentIds(sUserId);
      List<String> result = new ArrayList<>();
      for (ComponentInstLight component : components) {
        if (allowedComponentIds.contains(component.getId()) &&
            component.getName().startsWith(componentNameRoot)) {
          result.add(component.getId());
        }
      }

      return result;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("root components in space " + sClientSpaceId,
          AVAILABLE_TO_USER + sUserId), e);
    }
  }

  @Override
  public String[] getAvailCompoIds(String userId) throws AdminException {
    try {
      List<String> componentIds = getAllowedComponentIds(userId);

      return componentIds.toArray(new String[0]);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("components available to user", userId), e);
    }
  }

  @Override
  public String[] getComponentIdsByNameAndUserId(String sUserId, String sComponentName)
      throws AdminException {

    List<String> allowedComponentIds = getAllowedComponentIds(sUserId, sComponentName);
    return allowedComponentIds.toArray(new String[0]);
  }

  @Override
  public List<ComponentInstLight> getAvailComponentInstLights(String userId, String componentName)
      throws AdminException {

    List<ComponentInstLight> components = new ArrayList<>();
    List<String> allowedComponentIds = getAllowedComponentIds(userId, componentName);

    for (String allowedComponentId : allowedComponentIds) {
      ComponentInstLight componentInst = getComponentInstLight(allowedComponentId);

      if (componentInst.getName().equalsIgnoreCase(componentName)) {
        components.add(componentInst);
      }
    }
    return components;
  }

  @Override
  public List<SpaceInstLight> getRootSpacesContainingComponent(String userId, String componentName)
      throws AdminException {
    List<SpaceInstLight> spaces = new ArrayList<>();
    List<ComponentInstLight> components = getAvailComponentInstLights(userId, componentName);
    for (ComponentInstLight component : components) {
      List<SpaceInstLight> path = treeCache.getComponentPath(component.getId());
      if (!path.isEmpty()) {
        SpaceInstLight root = path.get(0);
        if (!spaces.contains(root)) {
          spaces.add(root);
        }
      }
    }
    return spaces;
  }

  @Override
  public List<SpaceInstLight> getSubSpacesContainingComponent(String spaceId, String userId,
      String componentName) throws AdminException {
    List<SpaceInstLight> spaces = new ArrayList<>();
    int driverSpaceId = getDriverSpaceId(spaceId);
    List<ComponentInstLight> components = getAvailComponentInstLights(userId, componentName);

    for (ComponentInstLight component : components) {
      List<SpaceInstLight> path = treeCache.getComponentPath(component.getId());
      for (SpaceInstLight space : path) {
        if (getDriverSpaceId(space.getFatherId()) == driverSpaceId && !spaces.contains(space)) {
          spaces.add(space);
        }
      }
    }
    return spaces;
  }

  @Override
  public CompoSpace[] getCompoForUser(String sUserId, String sComponentName) throws AdminException {
    ArrayList<CompoSpace> alCompoSpace = new ArrayList<>();

    try {
      List<ComponentInstLight> components = getAvailComponentInstLights(sUserId, sComponentName);
      for (ComponentInstLight componentInst : components) {
        // Create new instance of CompoSpace
        CompoSpace compoSpace = new CompoSpace();
        // Set the component id
        compoSpace.setComponentId(componentInst.getId());
        // Set the component label
        if (StringUtil.isDefined(componentInst.getLabel())) {
          compoSpace.setComponentLabel(componentInst.getLabel());
        } else {
          compoSpace.setComponentLabel(componentInst.getName());
        }

        // Set the space label
        compoSpace.setSpaceId(getClientSpaceId(componentInst.getDomainFatherId()));

        SpaceInstLight spaceInst = getSpaceInstLightById(componentInst.getDomainFatherId());
        compoSpace.setSpaceLabel(spaceInst.getName());

        alCompoSpace.add(compoSpace);
      }

      return alCompoSpace.toArray(new CompoSpace[0]);
    } catch (Exception e) {
      throw new AdminException(
          failureOnGetting("instances of component " + sComponentName, AVAILABLE_TO_USER + sUserId),
          e);
    }
  }

  @Override
  public String[] getCompoId(String sComponentName) throws AdminException {
    try {
      // Build the list of instantiated components with given componentName
      String[] matchingComponentIds =
          componentManager.getAllCompoIdsByComponentName(sComponentName);

      // check TreeCache to know if component is not removed nor is into a removed space
      List<String> shortIds = new ArrayList<>();
      for (String componentId : matchingComponentIds) {
        Optional<ComponentInstLight> component =
            treeCache.getComponent(sComponentName + componentId);
        component.ifPresent(c -> shortIds.add(componentId));
      }
      return shortIds.toArray(new String[0]);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("instances of component", sComponentName), e);
    }
  }

  @Override
  public List<ComponentInstLight> getComponentsWithParameter(String paramName, String paramValue) {
    try {
      final Parameter param = new Parameter();
      param.setName(paramName);
      param.setValue(paramValue);
      final List<Integer> componentIds = componentManager.getComponentIds(param);
      final List<ComponentInstLight> components = new ArrayList<>();
      for (Integer id : componentIds) {
        ComponentInst component = getComponentInst(id, null);
        // check TreeCache to know if component is not removed nor is into a removed space
        Optional<ComponentInstLight> componentLight = treeCache.getComponent(component.getId());
        componentLight.filter(c -> !c.isRemoved()).ifPresent(components::add);
      }
      return components;
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
      return Collections.emptyList();
    }
  }

  @Override
  public String[] getProfileIds(String sUserId) throws AdminException {
    try {
      final String[] asProfilesIds;
      // Get the profile ids from cache
      Optional<String[]> optionalProfileIds = cache.getProfileIds(sUserId);
      if (optionalProfileIds.isEmpty()) {
        // retrieve value from database
        asProfilesIds = profileManager.getProfileIdsOfUser(sUserId, getAllGroupsOfUser(sUserId));
        // store values in cache
        if (asProfilesIds != null) {
          cache.putProfileIds(sUserId, asProfilesIds);
        }
      } else {
        asProfilesIds = optionalProfileIds.get();
      }

      return asProfilesIds;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("profiles of user", sUserId), e);
    }
  }

  @Override
  public String[] getProfileIdsOfGroup(String sGroupId) throws AdminException {
    return getDirectComponentProfileIdsOfGroup(sGroupId);
  }

  @Override
  public String[] getCurrentProfiles(String sUserId, ComponentInst componentInst) {
    ArrayList<String> alProfiles = new ArrayList<>();

    try {
      // Build the list of profiles containing the given user
      String[] asProfileIds = getProfileIds(sUserId);

      for (String asProfileId : asProfileIds) {
        for (int nJ = 0; nJ < componentInst.getNumProfileInst(); nJ++) {
          if (componentInst.getProfileInst(nJ).getId().equals(asProfileId)) {
            alProfiles.add(componentInst.getProfileInst(nJ).getName());
          }
        }
      }

      return arrayListToString(removeTuples(alProfiles));
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
      return ArrayUtil.emptyStringArray();
    }
  }

  @Override
  public String[] getCurrentProfiles(String sUserId, String componentId) throws AdminException {
    return profileManager.getProfileNamesOfUser(sUserId, getAllGroupsOfUser(sUserId),
        getDriverComponentId(componentId));
  }

  @Override
  public Map<String, Set<String>> getUserProfilesByComponentId(final String userId,
      final Collection<String> componentIds) throws AdminException {
    final Map<Integer, String> localComponentIds = componentIds.stream()
        .collect(toMap(this::getDriverComponentId, i -> i));
    return profileManager
        .getProfileNamesOfUser(userId, getAllGroupsOfUser(userId), localComponentIds.keySet())
        .entrySet().stream()
        .collect(toMap(e -> localComponentIds.get(e.getKey()), Map.Entry::getValue));
  }

  @Override
  public List<String> getSpaceUserProfilesBySpaceId(final String userId, final String spaceId)
      throws AdminException {
    return spaceProfileManager.getSpaceProfileNamesOfUser(userId, getAllGroupsOfUser(userId),
        getDriverSpaceId(spaceId));
  }

  @Override
  public Map<String, Set<String>> getSpaceUserProfilesBySpaceIds(final String userId,
      final Collection<String> spaceIds) throws AdminException {
    final Map<Integer, String> localSpaceIds = spaceIds.stream()
        .collect(toMap(this::getDriverSpaceId, i -> i));
    return spaceProfileManager
        .getSpaceProfileNamesOfUser(userId, getAllGroupsOfUser(userId), localSpaceIds.keySet())
        .entrySet().stream()
        .collect(toMap(e -> localSpaceIds.get(e.getKey()), Map.Entry::getValue));
  }

  @Override
  public UserDetail[] getUsers(boolean bAllProfiles, String sProfile, String sClientSpaceId,
      String sClientComponentId) throws AdminException {
    ArrayList<String> alUserIds = new ArrayList<>();

    try {
      ComponentInst componentInst = getComponentInst(getDriverComponentId(sClientComponentId),
          getDriverSpaceId(sClientSpaceId));
      componentInst =
          checkComponentInstanceById(componentInst, sClientComponentId, nullComponentInstSupplier);
      Objects.requireNonNull(componentInst);

      for (ProfileInst profile : componentInst.getAllProfilesInst()) {
        if (profile != null && (profile.getName().equals(sProfile) || bAllProfiles)) {
          // add direct users
          alUserIds.addAll(profile.getAllUsers());
          // add users of valid groups
          addUsersOfAllGroups(alUserIds, profile);
        }
      }

      removeTuples(alUserIds);

      // Get the users details
      UserDetail[] userDetails = new UserDetail[alUserIds.size()];
      for (int nI = 0; nI < userDetails.length; nI++) {
        userDetails[nI] = getUserDetail(alUserIds.get(nI));
      }

      return userDetails;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("users with profile " + sProfile,
          "of the component " + sClientComponentId), e);
    }
  }

  private void addUsersOfAllGroups(final Collection<String> alUserIds, final ProfileInst profile)
      throws AdminException {
    final List<String> groupIds = profile.getAllGroups();
    for (String groupId : groupIds) {
      final List<String> subGroupIds = groupManager.getAllSubGroupIdsRecursively(groupId);
      // add current group
      subGroupIds.add(groupId);
      final UserDetail[] users = userManager.getAllUsersInGroups(subGroupIds);
      for (final UserDetail user : users) {
        alUserIds.add(user.getId());
      }
    }
  }

  @Override
  public GroupDetail[] getAllSubGroups(String parentGroupId) throws AdminException {
    List<GroupDetail> subgroups = groupManager.getSubGroups(parentGroupId);
    return subgroups.toArray(new GroupDetail[0]);
  }

  @Override
  public GroupDetail[] getRecursivelyAllSubGroups(String parentGroupId) throws AdminException {
    List<GroupDetail> subgroups = groupManager.getRecursivelySubGroups(parentGroupId);
    return subgroups.toArray(new GroupDetail[0]);
  }

  @Override
  public UserDetail[] getFilteredDirectUsers(String sGroupId, String sUserLastNameFilter)
      throws AdminException {
    GroupDetail theGroup = getGroup(sGroupId);

    if (theGroup == null) {
      return new UserDetail[0];
    }
    String[] usersIds = theGroup.getUserIds();
    if (usersIds == null || usersIds.length == 0) {
      return new UserDetail[0];
    }
    if (sUserLastNameFilter == null || sUserLastNameFilter.length() == 0) {
      return getUserDetails(usersIds);
    }
    String upperFilter = sUserLastNameFilter.toUpperCase();
    ArrayList<UserDetail> matchedUsers = new ArrayList<>();
    for (final String usersId : usersIds) {
      UserDetail currentUser = getUserDetail(usersId);
      if (currentUser != null && currentUser.getLastName().toUpperCase().startsWith(upperFilter)) {
        matchedUsers.add(currentUser);
      }
    }
    return matchedUsers.toArray(new UserDetail[0]);
  }

  @Override
  public int getAllSubUsersNumber(String sGroupId) throws AdminException {
    if (!StringUtil.isDefined(sGroupId)) {
      return userManager.getUserCount();
    } else {

      return groupManager.getTotalUserCountInGroup("", sGroupId);
    }
  }

  @Override
  public int getUsersNumberOfDomain(String domainId) throws AdminException {
    try {
      if (!StringUtil.isDefined(domainId)) {
        return userManager.getUserCount();
      }
      if ("-1".equals(domainId)) {
        return 0;
      }
      return userManager.getNumberOfUsersInDomain(domainId);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("user count in domain", domainId), e);
    }
  }

  @Override
  public String[] getAdministratorUserIds(String fromUserId) throws AdminException {
    return userManager.getAllAdminIds(getUserDetail(fromUserId));
  }

  @Override
  public String getSilverpeasEmail() {
    return senderEmail;
  }

  @Override
  public String getSilverpeasName() {
    return senderName;
  }

  @Override
  public String getDAPIGeneralAdminId() {
    return "0";
  }


  // -------------------------------------------------------------------------
  // UTILS
  // -------------------------------------------------------------------------
  private String[] arrayListToString(ArrayList<String> al) {
    if (al == null) {
      return ArrayUtil.emptyStringArray();
    }

    String[] as = new String[al.size()];
    for (int nI = 0; nI < al.size(); nI++) {
      as[nI] = al.get(nI);
    }

    return as;
  }

  private ArrayList<String> removeTuples(ArrayList<String> al) {
    if (al == null) {
      return new ArrayList<>();
    }

    for (int nI = 0; nI < al.size(); nI++) {
      while (al.lastIndexOf(al.get(nI)) != al.indexOf(al.get(nI))) {
        al.remove(al.lastIndexOf(al.get(nI)));
      }
    }

    return al;
  }

  // -------------------------------------------------------------------
  // RE-INDEXATION
  // -------------------------------------------------------------------
  @Override
  public String[] getAllSpaceIds(String sUserId) throws AdminException {
    return getClientSpaceIds(getUserSpaceIds(sUserId));
  }

  @Override
  public String[] getAllRootSpaceIds(String sUserId) throws AdminException {
    return getClientSpaceIds(getUserRootSpaceIds(sUserId));
  }

  @Override
  public String[] getAllSubSpaceIds(String sSpaceId, String sUserId) throws AdminException {
    return getUserSubSpaceIds(sUserId, sSpaceId);
  }

  @Override
  public String[] getAllComponentIds(String sSpaceId) throws AdminException {
    return Optional.of(getSpaceInstById(sSpaceId))
        .stream()
        .flatMap(s -> s.getAllComponentInstances().stream())
        .map(SilverpeasComponentInstance::getId)
        .toArray(String[]::new);
  }

  @Override
  public String[] getAllComponentIdsRecur(String sSpaceId) throws AdminException {
    List<ComponentInstLight> components =
        treeCache.getComponentsInSpaceAndSubspaces(getDriverSpaceId(sSpaceId));

    List<String> componentIds = new ArrayList<>();
    for (ComponentInstLight component : components) {
      componentIds.add(component.getId());
    }

    final SpaceInst space = getSpaceInstById(sSpaceId);
    if (space.isPersonalSpace()) {
      PersonalComponent.getAll()
          .forEach(
              p -> componentIds.add(PersonalComponentInstance.from(space.getCreator(), p).getId()));
    }

    return componentIds.toArray(new String[0]);
  }

  @Override
  public void synchronizeGroupByRule(String groupId, boolean scheduledMode) throws AdminException {
    GroupDetail group = getGroup(groupId);
    String rule = group.getRule();
    if (StringUtil.isDefined(rule)) {
      try {
        if (!scheduledMode) {
          SynchroGroupReport.setReportLevel(Level.DEBUG);
          SynchroGroupReport.startSynchro();
        }
        SynchroGroupReport.warn(ADMIN_SYNCHRONIZE_GROUP, "Synchronisation du groupe '" + group.
            getName() + "' - Regle de synchronisation = \"" + rule + "\"");
        List<String> actualUserIds = Arrays.asList(group.getUserIds());
        // Getting users according to rule
        List<String> userIds = GroupSynchronizationRule.from(group).getUserIds();

        // Add users
        List<String> newUsers = new ArrayList<>();
        if (userIds != null) {
          synchroAddUsersToAdd(actualUserIds, userIds, newUsers);
        }
        SynchroGroupReport.warn(ADMIN_SYNCHRONIZE_GROUP,
            "Ajout de " + newUsers.size() + " utilisateur(s)");
        if (!newUsers.isEmpty()) {
          SynchroGroupReport.debug("admin.synchronizeGroup()",
              () -> "Ajout de l'utilisateur d'ID " + String.join(", ", newUsers) +
                  " dans le groupe d'ID " +
                  groupId);
          groupManager.addUsersInGroup(newUsers, groupId);
        }

        // Remove users
        List<String> removedUsers = new ArrayList<>();
        synchroAddUsersToRemove(actualUserIds, userIds, removedUsers);
        SynchroGroupReport.warn(ADMIN_SYNCHRONIZE_GROUP,
            REMOVE_OF + removedUsers.size() + " utilisateur(s)");
        if (!removedUsers.isEmpty()) {
          groupManager.removeUsersFromGroup(removedUsers, groupId);
        }
      } catch (Exception e) {
        SynchroGroupReport.error(ADMIN_SYNCHRONIZE_GROUP,
            "Error during the processing of synchronization rule of group '" + groupId + "': " +
                e.getMessage(), null);
        throw new AdminException("Fail to synchronize group " + groupId, e);
      } finally {
        if (!scheduledMode) {
          SynchroGroupReport.stopSynchro();
        }
      }
    }
  }

  private void synchroAddUsersToRemove(final List<String> actualUserIds, final List<String> userIds,
      final List<String> removedUsers) {
    for (String actualUserId : actualUserIds) {
      if (userIds == null || !userIds.contains(actualUserId)) {
        removedUsers.add(actualUserId);
        SynchroGroupReport.info(ADMIN_SYNCHRONIZE_GROUP,
            "Suppression de l'utilisateur " + actualUserId);
      }
    }
  }

  private void synchroAddUsersToAdd(final List<String> actualUserIds, final List<String> userIds,
      final List<String> newUsers) {
    for (String userId : userIds) {
      if (!actualUserIds.contains(userId)) {
        newUsers.add(userId);
        SynchroGroupReport.info(ADMIN_SYNCHRONIZE_GROUP, "Ajout de l'utilisateur " + userId);
      }
    }
  }

  // //////////////////////////////////////////////////////////
  // Synchronization tools
  // //////////////////////////////////////////////////////////
  private List<String> translateGroupIds(String sDomainId, String[] groupSpecificIds,
      boolean recursGroups) {
    List<String> convertedGroupIds = new ArrayList<>();
    String groupId;
    for (String groupSpecificId : groupSpecificIds) {
      try {
        groupId = groupManager.getGroupIdBySpecificIdAndDomainId(groupSpecificId, sDomainId);
      } catch (AdminException e) {
        // The group doesn't exist -> Synchronize him
        groupId = null;
        SilverLogger.getLogger(this).warn("Group {0} not found. Synchronize it", groupSpecificId);
        if (recursGroups) {
          try {
            groupId = synchronizeImportGroup(sDomainId, groupSpecificId, null, true, true);
          } catch (AdminException ex) {
            // The group's synchro failed -> ignore him
            SilverLogger.getLogger(this).error(ex);
          }
        }
      }

      if (groupId != null) {
        convertedGroupIds.add(groupId);
      }
    }
    return convertedGroupIds;
  }

  private Map<String, String> translateUserIds(String sDomainId, String[] userSpecificIds) {
    final Map<String, String> userIdMapping = new HashMap<>(userSpecificIds.length);
    String userId = null;
    for (String userSpecificId : userSpecificIds) {
      try {
        userId = userManager.getUserIdBySpecificIdAndDomainId(userSpecificId, sDomainId);
        if (userId == null) {
          // The user doesn't exist -> Synchronize him
          SilverLogger.getLogger(this)
              .warn("The user {0} doesn't exist. Synchronize it", userSpecificId);
          userId = synchronizeImportUser(sDomainId, userSpecificId, false);
        }
      } catch (AdminException e) {
        // The user's synchro failed -> Ignore him
        SilverLogger.getLogger(this).error(e);
      }
      if (userId != null) {
        userIdMapping.put(userSpecificId, userId);
      }
    }
    return userIdMapping;
  }

  @Override
  public String synchronizeGroup(String groupId, boolean recurs) throws AdminException {
    final GroupDetail silverpeasGroup = getGroup(groupId);
    if (silverpeasGroup.isSynchronized()) {
      synchronizeGroupByRule(groupId, false);
    } else {
      final String domainId = silverpeasGroup.getDomainId();
      final String specificId = silverpeasGroup.getSpecificId();
      final Mutable<GroupDetail> distantGroup = Mutable.empty();
      final DomainDriver synchroDomain = domainDriverManager.getDomainDriver(domainId);
      try {
        distantGroup.set(synchroDomain.synchroGroup(specificId));
      } catch (AdminException e) {
        SilverLogger.getLogger(this)
            .warn("Group {0} not found into identity manager, removing it", groupId);
      }
      if (!distantGroup.isPresent()) {
        return synchronizeRemoveGroup(groupId);
      }
      final GroupDetail existingDistantGroup = distantGroup.get();
      existingDistantGroup.setId(groupId);
      existingDistantGroup.setDomainId(domainId);
      existingDistantGroup.setSuperGroupId(silverpeasGroup.getSuperGroupId());
      internalSynchronizeGroup(synchroDomain, existingDistantGroup, silverpeasGroup, recurs);
    }
    return groupId;
  }

  @Override
  public String synchronizeImportGroup(String domainId, String groupKey, String askedParentId,
      boolean recurs, boolean isIdKey) throws AdminException {
    DomainDriver synchroDomain = domainDriverManager.getDomainDriver(domainId);
    GroupDetail gr;

    if (isIdKey) {
      gr = synchroDomain.synchroGroup(groupKey);
    } else {
      gr = synchroDomain.importGroup(groupKey);
    }
    gr.setDomainId(domainId);

    // We now search for the parent of this group
    // ------------------------------------------
    // First, we get the parents of the group
    String[] parentSpecificIds = synchroDomain.getGroupMemberGroupIds(gr.getSpecificId());
    String parentId = null;
    for (int i = 0; i < parentSpecificIds.length && parentId == null; i++) {
      try {
        parentId = groupManager.getGroupIdBySpecificIdAndDomainId(parentSpecificIds[i], domainId);
        if (askedParentId != null && !askedParentId.isEmpty() && !askedParentId.equals(parentId)) {
          // It is not the matching parent
          parentId = null;
        }
      } catch (AdminException e) {
        // The user doesn't exist -> Synchronize him
      }
    }
    if (parentId == null && (parentSpecificIds.length > 0 ||
        (askedParentId != null && askedParentId.length() > 0))) {// We
      // can't add the group (just the same restriction as for the directories...)
      throw new AdminException(
          "Fail to synchronize imported group " + groupKey + " in domain " + domainId);
    }
    // The group is a root group or have a known parent
    gr.setSuperGroupId(parentId);

    // We must first add the group with no child. Then, the childs will be added
    // during the internal synchronization function call
    String[] specificIds = gr.getUserIds();
    gr.setUserIds(ArrayUtil.emptyStringArray());
    String groupId = addGroup(gr, true);
    gr.setId(groupId);
    gr.setUserIds(specificIds);
    final GroupDetail spGroup = getGroup(groupId);
    internalSynchronizeGroup(synchroDomain, gr, spGroup, recurs);
    return groupId;
  }

  @Override
  public String synchronizeRemoveGroup(String groupId) throws AdminException {
    GroupDetail theGroup = getGroup(groupId);
    DomainDriver synchroDomain = domainDriverManager.getDomainDriver(theGroup.getDomainId());
    synchroDomain.removeGroup(theGroup.getSpecificId());
    final List<GroupDetail> removedGroups = removeGroup(groupId);
    return removedGroups.stream().findFirst().map(GroupDetail::getId).orElse(EMPTY);
  }

  protected void internalSynchronizeGroup(DomainDriver synchroDomain, GroupDetail distantGroup,
      GroupDetail silverpeasGroup, boolean recurs) throws AdminException {
    final Map<String, String> userIdMapping = translateUserIds(distantGroup.getDomainId(),
        distantGroup.getUserIds());
    final SyncOfGroupsContext context = new SyncOfGroupsContext(distantGroup.getDomainId(),
        userIdMapping);
    if (mergeDistantGroupIntoSilverpeasGroup(context,
        synchronizingOneGroupWithSuperGroupId(distantGroup.getSuperGroupId()),
        distantGroup, silverpeasGroup)) {
      updateGroup(silverpeasGroup, true);
    }
    if (recurs) {
      GroupDetail[] children = synchroDomain.getGroups(distantGroup.getSpecificId());
      for (final GroupDetail child : children) {
        String existingGroupId = null;
        try {
          existingGroupId = groupManager.getGroupIdBySpecificIdAndDomainId(child.getSpecificId(),
              distantGroup.getDomainId());
          GroupDetail existingGroup = getGroup(existingGroupId);
          if (existingGroup.getSuperGroupId().equals(distantGroup.getId())) {
            // Only synchronize the group if distantGroup is his true parent
            //noinspection ConstantConditions
            synchronizeGroup(existingGroupId, recurs);
          }
        } catch (AdminException e) {
          // The group doesn't exist -> Import him
          if (existingGroupId == null) {
            // Import the new group
            //noinspection ConstantConditions
            synchronizeImportGroup(distantGroup.getDomainId(), child.getSpecificId(),
                distantGroup.getId(), recurs, true);
          }
        }
      }
    }
  }

  @Override
  public String synchronizeUser(String userId, boolean recurs) throws AdminException {
    Collection<UserDetail> listUsersUpdate = new ArrayList<>();
    UserDetail theUserDetail = getUserDetail(userId);
    DomainDriver synchroDomain = domainDriverManager.getDomainDriver(theUserDetail.getDomainId());
    // Synchronize the user's infos
    final Mutable<UserDetail> silverpeasUser = Mutable.empty();
    try {
      silverpeasUser.set(synchroDomain.synchroUser(theUserDetail.getSpecificId()));
    } catch (AdminException e) {
      SilverLogger.getLogger(this)
          .warn("User {0} not found into identity manager, removing it", userId);
    }
    try {
      if (!silverpeasUser.isPresent()) {
        return synchronizeRemoveUser(userId);
      }
      UserDetail ud = synchroDomain.synchroUser(theUserDetail.getSpecificId());
      ud.setId(userId);
      ud.setAccessLevel(theUserDetail.getAccessLevel());
      ud.setDomainId(theUserDetail.getDomainId());
      if (!ud.equals(theUserDetail) ||
          (ud.getState() != UserState.UNKNOWN && ud.getState() != theUserDetail.getState())) {
        mergeDistantUserIntoSilverpeasUser(ud, theUserDetail);
        userManager.updateUser(theUserDetail, true);
        cache.opUpdateUser(userManager.getUserDetail(userId));
      }
      // Synchro manuelle : Ajoute ou Met à jour l'utilisateur
      listUsersUpdate.add(ud);

      // Synchronize the user's groups
      String[] incGroupsSpecificId =
          synchroDomain.getUserMemberGroupIds(theUserDetail.getSpecificId());
      List<String> incGroupsId =
          translateGroupIds(theUserDetail.getDomainId(), incGroupsSpecificId, recurs);
      final List<GroupDetail> oldGroups = groupManager.getAllDirectGroupsOfUser(userId);
      for (GroupDetail oldGroup : oldGroups) {
        if (incGroupsId.contains(oldGroup.getId())) { // No changes have to be
          // performed to the group -> Remove it
          incGroupsId.remove(oldGroup.getId());
        } else {
          if (theUserDetail.getDomainId().equals(oldGroup.getDomainId())) {
            // Remove the user from this group
            groupManager.removeUserFromGroup(userId, oldGroup.getId());
            cache.opRemoveUserFromGroup(userId);
          }
        }
      }
      // Now the remaining groups of the vector are the groups where the user is
      // newly added
      for (String includedGroupId : incGroupsId) {
        groupManager.addUserInGroup(userId, includedGroupId);
        cache.opAddUserInGroup(userId);
      }

      // traitement spécifique des users selon l'interface implémentée
      processSpecificSynchronization(theUserDetail.getDomainId(), null, listUsersUpdate, null);

      // return user id
      return userId;
    } catch (Exception e) {
      throw new AdminException("Fail to synchronize user " + userId, e);
    }
  }

  @Override
  public String synchronizeImportUserByLogin(String domainId, String userLogin, boolean recurs)
      throws AdminException {
    DomainDriver synchroDomain = domainDriverManager.getDomainDriver(domainId);
    UserDetail ud = synchroDomain.importUser(userLogin);
    ud.setDomainId(domainId);
    String userId = addUser(ud, true);
    // Synchronizes the user to add it to the groups and recursively add the groups
    synchronizeUser(userId, recurs);
    return userId;
  }

  @Override
  public String synchronizeImportUser(String domainId, String specificId, boolean recurs)
      throws AdminException {
    DomainDriver synchroDomain = domainDriverManager.getDomainDriver(domainId);
    UserDetail ud = synchroDomain.getUser(specificId);
    ud.setDomainId(domainId);
    String userId = addUser(ud, true);
    // Synchronizes the user to add it to the groups and recursively add the groups
    synchronizeUser(userId, recurs);
    return userId;
  }

  @Override
  public List<DomainProperty> getSpecificPropertiesToImportUsers(String domainId, String language)
      throws AdminException {
    DomainDriver synchroDomain = domainDriverManager.getDomainDriver(domainId);
    return synchroDomain.getPropertiesToImport(language);
  }

  @Override
  public UserDetail[] searchUsers(String domainId, Map<String, String> query)
      throws AdminException {
    DomainDriver synchroDomain = domainDriverManager.getDomainDriver(domainId);
    return synchroDomain.getUsersByQuery(query);
  }

  @Override
  public String synchronizeRemoveUser(String userId) throws AdminException {
    UserDetail theUserDetail = getUserDetail(userId);
    DomainDriver synchroDomain = domainDriverManager.getDomainDriver(theUserDetail.getDomainId());
    synchroDomain.removeUser(theUserDetail.getSpecificId());
    removeUser(userId);
    processSpecificSynchronization(theUserDetail.getDomainId(), null, null,
        singletonList(theUserDetail));
    return userId;
  }

  @Override
  public String synchronizeSilverpeasWithDomain(String sDomainId) throws AdminException {
    return synchronizeSilverpeasWithDomain(sDomainId, false);
  }

  @Override
  public String synchronizeSilverpeasWithDomain(String sDomainId, boolean threaded)
      throws AdminException {
    try {
      final Pair<String, List<AbstractBackgroundProcessRequest>> result = Transaction.performInNew(
          new Process<>() {
            @Override
            public Pair<String, List<AbstractBackgroundProcessRequest>> execute() throws Exception {
              String sReport = "Starting synchronization...\n\n";
              synchronized (semaphore) {
                // Starting synchronization with a status popup
                SynchroDomainReport.startSynchro();
                try {
                  SynchroDomainReport.info(ADMIN_SYNCHRONIZE_DOMAIN, "Domain '" +
                      domainDriverManager.getDomain(sDomainId)
                          .getName() + "', Id : " + sDomainId);
                  // Start synchronization
                  domainDriverManager.beginSynchronization(sDomainId);
                  final DomainDriver synchroDomain = domainDriverManager.getDomainDriver(sDomainId);
                  // Synchronize users
                  final boolean addUserIntoSilverpeas = synchroDomain.mustImportUsers() || threaded;
                  final SyncOfUsersContext userContext = new SyncOfUsersContext(sDomainId, threaded,
                      addUserIntoSilverpeas, delUsersOnDiffSynchro);
                  final SyncOfUsersContext syncOfUsersContext = synchronizeUsers(userContext);
                  sReport += syncOfUsersContext.getReport();
                  // Synchronize groups
                  // Get all users of the domain from Silverpeas
                  final Map<String, String> userIdsMapping =
                      stream(userManager.getAllUsersInDomain(sDomainId, true))
                          .collect(toMap(UserDetail::getSpecificId, UserDetail::getId));
                  final SyncOfGroupsContext groupContext = new SyncOfGroupsContext(sDomainId,
                      userIdsMapping);
                  final SyncOfGroupsContext syncOfGroupsContext = synchronizeGroups(groupContext);
                  sReport += "\n" + syncOfGroupsContext.getReport();
                  // End synchronization
                  final String sDomainSpecificErrors =
                      domainDriverManager.endSynchronization(sDomainId, false);
                  if (StringUtil.isDefined(sDomainSpecificErrors)) {
                    SynchroDomainReport.info(ADMIN_SYNCHRONIZE_DOMAIN,
                        "----------------" + sDomainSpecificErrors);
                  }
                  return Pair.of(sReport + "\n----------------\n" + sDomainSpecificErrors,
                      List.of(syncOfUsersContext.getIndexationBackgroundProcess(),
                          syncOfGroupsContext.getIndexationBackgroundProcess()));
                } catch (Exception e) {
                  try {
                    // End synchronization
                    domainDriverManager.endSynchronization(sDomainId, true);
                  } catch (Exception e1) {
                    SilverLogger.getLogger(this)
                        .error(e1);
                  }
                  SynchroDomainReport.error(ADMIN_SYNCHRONIZE_DOMAIN,
                      "Problème lors de la synchronisation : " + e.getMessage(), null);
                  throw new AdminException(
                      "Fail to synchronize domain " + sDomainId + ". Report: " + sReport, e);
                } finally {
                  SynchroDomainReport.stopSynchro();// Synchro ending with a popup
                  // Reset the cache
                  cache.resetCache();
                }
              }
            }
          });
      result.getSecond().forEach(BackgroundProcessTask::push);
      return result.getFirst();
    } catch (Exception e) {
      if (e.getCause() instanceof AdminException) {
        throw e;
      }
      throw new AdminException(e);
    }
  }

  /**
   * Merge the data of a distant user into the data of a silverpeas user : - user identifier (the
   * distant one) - first name - last name - e-mail - login
   * @param distantUser {@link UserDetail} representing data on externam repository.
   * @param silverpeasUser {@link UserDetail} representing data on silverpeas.
   * @return true if a data has changed, false otherwise.
   */
  static boolean mergeDistantUserIntoSilverpeasUser(final UserDetail distantUser,
      final UserDetail silverpeasUser) {
    boolean dataUpdated =
        !Objects.equals(silverpeasUser.getSpecificId(), distantUser.getSpecificId());
    silverpeasUser.setSpecificId(distantUser.getSpecificId());
    dataUpdated |= !Objects.equals(silverpeasUser.getFirstName(), distantUser.getFirstName());
    silverpeasUser.setFirstName(distantUser.getFirstName());
    dataUpdated |= !Objects.equals(silverpeasUser.getLastName(), distantUser.getLastName());
    silverpeasUser.setLastName(distantUser.getLastName());
    dataUpdated |= !Objects.equals(silverpeasUser.geteMail(), distantUser.geteMail());
    silverpeasUser.seteMail(distantUser.geteMail());
    dataUpdated |= !Objects.equals(silverpeasUser.getLogin(), distantUser.getLogin());
    silverpeasUser.setLogin(distantUser.getLogin());
    if (silverpeasUser.isRemovedState()) {
      return dataUpdated;
    }
    if (distantUser.isDeactivatedState() ||
        (distantUser.isValidState() && silverpeasUser.isDeactivatedState())) {
      // The user account is deactivated from the LDAP
      // or
      // The user account is activated from the LDAP, so the Silverpeas user
      // account is again activated only if it was deactivated. Indeed, if it was blocked
      // for example, it is still blocked after a synchronization
      dataUpdated |= !Objects.equals(silverpeasUser.getState(), distantUser.getState());
      silverpeasUser.setState(distantUser.getState());
    }
    return dataUpdated;
  }

  /**
   * Synchronize users between cache and domain's datasource
   */
  private SyncOfUsersContext synchronizeUsers(final SyncOfUsersContext context)
      throws AdminException {
    final String domainId = context.getDomainId();
    context.appendToReport("User synchronization : \n");
    String message;
    SynchroDomainReport.info(ADMIN_SYNCHRONIZE_USERS, "Starting synchronization of users...");
    final UserDetail[] distantUDs = domainDriverManager.getAllUsers(context.getDomainId());
    SynchroDomainReport.debug(ADMIN_SYNCHRONIZE_USERS,
        format("Existing currently {0} users in external repository before synchronization",
            distantUDs.length));
    final UserDetail[] silverpeasUDs = userManager.getAllUsersInDomain(domainId, true);
    SynchroDomainReport.debug(ADMIN_SYNCHRONIZE_USERS,
        format("Existing currently {0} users in Silverpeas before synchronization",
            silverpeasUDs.length));
    try {
      performRemoveOfUsersDuringSynchronization(context, distantUDs, silverpeasUDs);
      performSaveOfUsersDuringSynchronization(context, distantUDs, silverpeasUDs);
      processSpecificSynchronization(domainId, context.getAddedUsers().values(),
          context.getUpdatedUsers().values(), context.getRemovedUsers().values());
      message = "Synchronization of users terminated";
      context.appendToReport(message).appendToReport("\n");
      SynchroDomainReport.info(ADMIN_SYNCHRONIZE_USERS, message);
      message = format(
          "# of updated users: {0}, added: {1}, removed: {2}, restored: {3}, deleted: {4}",
          context.getUpdatedUsers().size(), context.getAddedUsers().size(),
          context.getRemovedUsers().size(), context.getRestoredUsers().size(),
          context.getDeletedUsers().size());
      context.appendToReport(message).appendToReport("\n");
      SynchroDomainReport.info(ADMIN_SYNCHRONIZE_USERS, message);
      context.setIndexationBackgroundProcess(
          new BackgroundUserIndexationProcess(domainDriverManager, context));
      return context;
    } catch (Exception e) {
      SynchroDomainReport.error(ADMIN_SYNCHRONIZE_USERS,
          "Problem during synchronization of users : " + e.getMessage(), null);
      throw new AdminException(
          "Fail to synchronize domain " + domainId + ". Report: " + context.getReport(), e);
    }
  }

  private void performRemoveOfUsersDuringSynchronization(final SyncOfUsersContext context,
      final UserDetail[] distantUDs, final UserDetail[] silverpeasUDs) {
    if (context.isRemoveOperationToPerform()) {
      SynchroDomainReport.debug(ADMIN_SYNCHRONIZE_USERS, "Removing users from database...");
      final Set<String> indexedDistantUsers = extractUserSpecificIdAndFallbackLogin(distantUDs);
      for (UserDetail silverpeasUD : silverpeasUDs) {
        // search for user in distant datasource
        if (!silverpeasUD.isRemovedState() &&
            !existsUserBySpecificIdOrFallbackLoginIn(silverpeasUD, indexedDistantUsers)) {
          removeUserDuringSynchronization(context, silverpeasUD);
        }
      }
    }
  }

  private void performSaveOfUsersDuringSynchronization(final SyncOfUsersContext context,
      final UserDetail[] distantUDs, final UserDetail[] silverpeasUDs) {
    SynchroDomainReport.debug(ADMIN_SYNCHRONIZE_USERS, "Saving users in database...");
    final Map<String, UserDetail> indexedSpUsers = indexUsersBySpecificIdAndLogin(silverpeasUDs);
    for (final UserDetail distantUD : distantUDs) {
      final UserDetail userToUpdateFromDistantUser =
          getUserBySpecificIdOrFallbackLoginFrom(distantUD, indexedSpUsers);
      if (userToUpdateFromDistantUser != null) {
        if (userToUpdateFromDistantUser.isRemovedState()) {
          restoreUserDuringSynchronization(context, distantUD, userToUpdateFromDistantUser);
        } else if (mergeDistantUserIntoSilverpeasUser(distantUD, userToUpdateFromDistantUser)) {
          updateUserDuringSynchronization(context, userToUpdateFromDistantUser);
        }
      } else if (context.isAddOperationToPerform()) {
        deleteRemovedUserDuringSynchronization(context, distantUD, indexedSpUsers);
        distantUD.setDomainId(context.getDomainId());
        addUserDuringSynchronization(context, distantUD);
      }
    }
  }

  /**
   * Background process request in charge of user indexation.
   */
  private static class BackgroundUserIndexationProcess extends AbstractBackgroundProcessRequest {

    private final DomainDriverManager domainDriverManager;
    private final SyncOfUsersContext context;

    private BackgroundUserIndexationProcess(final DomainDriverManager domainDriverManager,
        final SyncOfUsersContext context) {
      super();
      this.domainDriverManager = domainDriverManager;
      this.context = context;
    }

    @Override
    protected void process() {
      final SilverLogger logger = SilverLogger.getLogger(this);
      final long start = System.currentTimeMillis();
      final String totalOfUsers = String.valueOf(
          context.getAddedUsers().size() + context.getUpdatedUsers().size() +
              context.getRemovedUsers().size() + context.getRestoredUsers().size());
      logger.debug(format("Starting indexation of {0} users on domain id {1}...", totalOfUsers,
          context.getDomainId()));
      Transaction.performInOne(() -> {
        logger.debug(format("unindexation of {0} removed users on domain id {1}...",
            String.valueOf(context.getRemovedUsers().size()), context.getDomainId()));
        context.getRemovedUsers().keySet().forEach(domainDriverManager::unindexUser);
        logger.debug(format("indexation of {0} restored users on domain id {1}...",
            String.valueOf(context.getRestoredUsers().size()), context.getDomainId()));
        context.getRestoredUsers().keySet().forEach(domainDriverManager::indexUser);
        logger.debug(format("indexation of {0} added users on domain id {1}...",
            String.valueOf(context.getAddedUsers().size()), context.getDomainId()));
        context.getAddedUsers().keySet().forEach(domainDriverManager::indexUser);
        logger.debug(format("indexation of {0} updated users on domain id {1}...",
            String.valueOf(context.getUpdatedUsers().size()), context.getDomainId()));
        context.getUpdatedUsers().keySet().forEach(domainDriverManager::indexUser);
        return null;
      });
      final long end = System.currentTimeMillis();
      logger.debug(
          () -> format("Ending indexation of {0} users on domain id {1} in {2}", totalOfUsers,
              context.getDomainId(), DurationFormatUtils.formatDurationHMS(end - start)));
    }
  }

  /**
   * Background process request in charge of group indexation.
   */
  private static class BackgroundGroupIndexationProcess extends AbstractBackgroundProcessRequest {

    private final DomainDriverManager domainDriverManager;
    private final SyncOfGroupsContext context;

    private BackgroundGroupIndexationProcess(final DomainDriverManager domainDriverManager,
        final SyncOfGroupsContext context) {
      super();
      this.domainDriverManager = domainDriverManager;
      this.context = context;
    }

    @Override
    protected void process() {
      final SilverLogger logger = SilverLogger.getLogger(this);
      final long start = System.currentTimeMillis();
      final String totalOfGroups = String.valueOf(
          context.getAddedGroups().size() + context.getUpdatedGroups().size() +
              context.getRemovedGroups().size() + context.getRestoredGroups().size());
      logger.debug(format("Starting indexation of {0} groups on domain id {1}...", totalOfGroups,
          context.getDomainId()));
      Transaction.performInOne(() -> {
        logger.debug(format("unindexation of {0} removed groups on domain id {1}...",
            String.valueOf(context.getRemovedGroups().size()), context.getDomainId()));
        context.getRemovedGroups().keySet().forEach(domainDriverManager::unindexGroup);
        logger.debug(format("indexation of {0} restored groups on domain id {1}...",
            String.valueOf(context.getRestoredGroups().size()), context.getDomainId()));
        context.getRestoredGroups().values().forEach(domainDriverManager::indexGroup);
        logger.debug(format("indexation of {0} added groups on domain id {1}...",
            String.valueOf(context.getAddedGroups().size()), context.getDomainId()));
        context.getAddedGroups().values().forEach(domainDriverManager::indexGroup);
        logger.debug(format("indexation of {0} updated groups on domain id {1}...",
            String.valueOf(context.getUpdatedGroups().size()), context.getDomainId()));
        context.getUpdatedGroups().values().forEach(domainDriverManager::indexGroup);
        return null;
      });
      final long end = System.currentTimeMillis();
      logger.debug(
          () -> format("Ending indexation of {0} groups on domain id {1} in {2}", totalOfGroups,
              context.getDomainId(), DurationFormatUtils.formatDurationHMS(end - start)));
    }
  }

  @Nullable
  private UserDetail getUserBySpecificIdOrFallbackLoginFrom(@Nonnull final UserDetail user,
      final Map<String, UserDetail> indexedUsers) {
    UserDetail indexedUser = indexedUsers.get(user.getSpecificId());
    if (indexedUser == null && shouldFallbackUserLogins) {
      indexedUser = indexedUsers.get(user.getLogin());
    }
    return indexedUser;
  }

  private boolean existsUserBySpecificIdOrFallbackLoginIn(final UserDetail user,
      final Set<String> indexedUsers) {
    return indexedUsers.contains(user.getSpecificId()) ||
        (shouldFallbackUserLogins && indexedUsers.contains(user.getLogin()));
  }

  @Nonnull
  private Map<String, UserDetail> indexUsersBySpecificIdAndLogin(final UserDetail[] silverpeasUDs) {
    final Map<String, UserDetail> indexedSilverpeasUsers = new HashMap<>(silverpeasUDs.length * 2);
    stream(silverpeasUDs).forEach(u -> {
      indexedSilverpeasUsers.put(u.getSpecificId(), u);
      indexedSilverpeasUsers.put(u.getLogin(), u);
    });
    return indexedSilverpeasUsers;
  }

  @Nonnull
  private Set<String> extractUserSpecificIdAndFallbackLogin(final UserDetail[] users) {
    final Set<String> indexedUsers =
        new HashSet<>(shouldFallbackUserLogins ? (users.length * 2) : users.length);
    stream(users).forEach(u -> {
      indexedUsers.add(u.getSpecificId());
      if (shouldFallbackUserLogins) {
        indexedUsers.add(u.getLogin());
      }
    });
    return indexedUsers;
  }

  private void updateUserDuringSynchronization(final SyncOfUsersContext context,
      final UserDetail distantUD) {
    final String specificId = distantUD.getSpecificId();
    try {
      final String silverpeasId = userManager.updateUser(distantUD, false);
      context.getUpdatedUsers().put(silverpeasId, distantUD);
      final String message =
          format("{0} {1} updated (id:{2} / specificId:{3})", USER, distantUD.getDisplayedName(),
              silverpeasId, specificId);
      SynchroDomainReport.info(ADMIN_SYNCHRONIZE_USERS, message);
      context.appendToReport(message).appendToReport("\n");
    } catch (AdminException aeMaj) {
      SilverLogger.getLogger(this)
          .error("Full synchro: error while updating user " + specificId, aeMaj);
      final String errorMessage =
          format("problem updating user {0} (specificId:{1}) - {2}", distantUD.getDisplayedName(),
              specificId, aeMaj.getMessage());
      context.appendToReport(errorMessage).appendToReport("\n");
      SynchroDomainReport.warn(ADMIN_SYNCHRONIZE_USERS, errorMessage);
      context.appendToReport("user hasn't been updated\n");
    }
  }

  private void addUserDuringSynchronization(final SyncOfUsersContext context,
      final UserDetail distantUD) {
    final String specificId = distantUD.getSpecificId();
    try {
      final String silverpeasId = userManager.addUser(distantUD, true, false);
      if (silverpeasId.equals("")) {
        final String message =
            format("problem adding user {0} (specificId:{1}) - Login and LastName must be set !!!",
                distantUD.getDisplayedName(), specificId);
        context.appendToReport(message).appendToReport("\n");
        SynchroDomainReport.warn(ADMIN_SYNCHRONIZE_USERS, message);
        context.appendToReport("user has not been added\n");
      } else {
        context.getAddedUsers().put(silverpeasId, distantUD);
        final String message =
            format("{0} {1} added (id:{2} / specificId:{3})", USER, distantUD.getDisplayedName(),
                silverpeasId, specificId);
        context.appendToReport(message).appendToReport("\n");
        SynchroDomainReport.info(ADMIN_SYNCHRONIZE_USERS, message);
      }
    } catch (AdminException ae) {
      SilverLogger.getLogger(this).error("Full synchro: error while adding user " + specificId, ae);
      final String message =
          format("problem adding user {0}(specificId:{1}) - {2}", distantUD.getDisplayedName(),
              specificId, ae.getMessage());
      SynchroDomainReport.warn(ADMIN_SYNCHRONIZE_USERS, message);
      context.appendToReport(message).appendToReport("\n");
      context.appendToReport("user has not been added\n");
    }
  }

  private void removeUserDuringSynchronization(final SyncOfUsersContext context,
      final UserDetail silverpeasUD) {
    final String specificId = silverpeasUD.getSpecificId();
    try {
      userManager.removeUser(silverpeasUD, false);
      silverpeasUD.setState(UserState.REMOVED);
      context.getRemovedUsers().put(silverpeasUD.getId(), silverpeasUD);
      final String message =
          format("{0} {1} removed (id:{2} / specificId:{3})", USER, silverpeasUD.getDisplayedName(),
              silverpeasUD.getId(), specificId);
      context.appendToReport(message).appendToReport("\n");
      SynchroDomainReport.info(ADMIN_SYNCHRONIZE_USERS, message);
    } catch (AdminException aeDel) {
      SilverLogger.getLogger(this)
          .error("Full synchro: error while removing user " + specificId, aeDel);
      final String message = format("problem removing user {0} (specificId:{1}) - {2}",
          silverpeasUD.getDisplayedName(), specificId, aeDel.getMessage());
      context.appendToReport(message).appendToReport("\n");
      SynchroDomainReport.warn(ADMIN_SYNCHRONIZE_USERS, message);
      context.appendToReport("user has not been removed\n");
    }
  }

  private void restoreUserDuringSynchronization(final SyncOfUsersContext context,
      final UserDetail distantUD, final UserDetail silverpeasUD) {
    final String specificId = silverpeasUD.getSpecificId();
    try {
      userManager.restoreUser(silverpeasUD, false);
      final UserDetail updatedRestoreData = getUserDetail(silverpeasUD.getId());
      silverpeasUD.setState(updatedRestoreData.getState());
      silverpeasUD.setStateSaveDate(updatedRestoreData.getStateSaveDate());
      if (mergeDistantUserIntoSilverpeasUser(distantUD, silverpeasUD)) {
        userManager.updateUser(silverpeasUD, false);
      }
      context.getRestoredUsers().put(silverpeasUD.getId(), silverpeasUD);
      final String message = format("{0} {1} restored (id:{2} / specificId:{3})", USER,
          silverpeasUD.getDisplayedName(), silverpeasUD.getId(), specificId);
      context.appendToReport(message).appendToReport("\n");
      SynchroDomainReport.debug(ADMIN_SYNCHRONIZE_USERS, message);
    } catch (AdminException aeDel) {
      SilverLogger.getLogger(this)
          .error("Full synchro: error while restoring user " + specificId, aeDel);
      final String message = format("problem restoring user {0} (specificId:{1}) - {2}",
          silverpeasUD.getDisplayedName(), specificId, aeDel.getMessage());
      context.appendToReport(message).appendToReport("\n");
      SynchroDomainReport.warn(ADMIN_SYNCHRONIZE_USERS, message);
      context.appendToReport("user has not been restored\n");
    }
  }

  private void deleteRemovedUserDuringSynchronization(final SyncOfUsersContext context,
      final UserDetail distantUD, final Map<String, UserDetail> indexedSpUsers) {
    final String login = distantUD.getLogin();
    try {
      if (!shouldFallbackUserLogins) {
        final UserDetail silverpeasUser = indexedSpUsers.get(login);
        if (silverpeasUser != null) {
          if (!distantUD.getSpecificId().equals(silverpeasUser.getSpecificId()) &&
              silverpeasUser.isRemovedState()) {
            userManager.deleteUser(silverpeasUser, true);
            context.getDeletedUsers().put(silverpeasUser.getId(), silverpeasUser);
            final String message =
                format("{0} {1} deleted (id:{2} / login:{3})", USER, distantUD.getDisplayedName(),
                    distantUD.getId(), login);
            context.appendToReport(message).appendToReport("\n");
            SynchroDomainReport.info(ADMIN_SYNCHRONIZE_USERS, message);
          } else {
            final String message =
                format("{0} {1} must have 'REMOVED' state for deletion (id:{2} / login:{3})", USER,
                    distantUD.getDisplayedName(), distantUD.getId(), login);
            throw new AdminException(message);
          }
        }
      }
    } catch (AdminException aeDel) {
      SilverLogger.getLogger(this)
          .error("Full synchro: error while deleting user with login " + login, aeDel);
      final String message = format("problem deleting user {0} (domainId:{1}, login:{2}) - {3}",
          distantUD.getDisplayedName(), context.getDomainId(), login, aeDel.getMessage());
      context.appendToReport(message).appendToReport("\n");
      SynchroDomainReport.warn(ADMIN_SYNCHRONIZE_USERS, message);
      context.appendToReport("user has not been deleted\n");
    }
  }

  @SuppressWarnings("unchecked")
  private void processSpecificSynchronization(String domainId, Collection<UserDetail> usersAdded,
      Collection<UserDetail> usersUpdated, Collection<UserDetail> usersRemoved)
      throws AdminException {
    Domain theDomain = domainDriverManager.getDomain(domainId);
    SettingBundle propDomainLdap = theDomain.getSettings();
    String nomClasseSynchro = propDomainLdap.getString("synchro.Class", null);
    if (StringUtil.isDefined(nomClasseSynchro)) {
      Collection<UserDetail> added = usersAdded;
      Collection<UserDetail> updated = usersUpdated;
      Collection<UserDetail> removed = usersRemoved;
      if (added == null) {
        added = new ArrayList<>();
      }
      if (updated == null) {
        updated = new ArrayList<>();
      }
      if (removed == null) {
        removed = new ArrayList<>();
      }
      try {
        Constructor<LDAPSynchroUserItf> constructor =
            (Constructor<LDAPSynchroUserItf>) Class.forName(nomClasseSynchro).getConstructor();
        LDAPSynchroUserItf synchroUser = constructor.newInstance();
        synchroUser.processUsers(added, updated, removed);
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
  }

  /**
   * Synchronize groups between cache and domain's datastore
   */
  private SyncOfGroupsContext synchronizeGroups(final SyncOfGroupsContext context)
      throws AdminException {
    final String domainId = context.getDomainId();
    context.appendToReport("GroupDetail synchronization : \n");
    String message;
    SynchroDomainReport.info(ADMIN_SYNCHRONIZE_GROUPS, "Starting groups synchronization...");
    final GroupDetail[] distantRootGroups = domainDriverManager.getAllRootGroups(domainId);
    SynchroDomainReport.debug(ADMIN_SYNCHRONIZE_GROUPS,
        format("Existing currently {0} ROOT groups in external repository before synchronization",
            distantRootGroups.length));
    final List<GroupDetail> allSilverpeasGroups = groupManager.getGroupsOfDomain(domainId);
    SynchroDomainReport.debug(ADMIN_SYNCHRONIZE_GROUPS,
        format("Existing currently {0} groups in Silverpeas before synchronization",
            allSilverpeasGroups.size()));
    try {
      SynchroDomainReport.debug(ADMIN_SYNCHRONIZE_GROUPS,
          "Adding or updating groups in database...");
      // Check for checking out groups recursively
      final CheckoutGroupDescriptor descriptor =
          synchronizingDomainFrom(allSilverpeasGroups, distantRootGroups);
      checkOutGroups(context, descriptor);
      // Delete obsolete groups
      SynchroDomainReport.debug(ADMIN_SYNCHRONIZE_GROUPS, "Removing groups from database...");
      final Collection<GroupDetail> distantGroups = descriptor.getAllSavedDistantGroups().values();
      allSilverpeasGroups.stream()
          // search for group in distant datasource
          .filter(s -> distantGroups.stream().noneMatch(d ->
              d.getSpecificId().equals(s.getSpecificId()) ||
                  (shouldFallbackGroupNames && d.getName().equals(s.getSpecificId()))))
          // not already removed
          .filter(not(GroupDetail::isRemovedState))
          // if found, do nothing, else delete
          .forEach(s -> synchroRemoveGroup(context, s.getSpecificId(), s));
      message = "Groups synchronization terminated\n";
      context.appendToReport(message).appendToReport("\n");
      SynchroDomainReport.info(ADMIN_SYNCHRONIZE_USERS, message);
      message = format(
          "# of updated groups: {0}, added: {1}, removed: {2}, restored: {3}",
          context.getUpdatedGroups().size(), context.getAddedGroups().size(),
          context.getRemovedGroups().size(), context.getRestoredGroups().size());
      context.appendToReport(message).appendToReport("\n");
      SynchroDomainReport.info(ADMIN_SYNCHRONIZE_GROUPS, message);
      context.setIndexationBackgroundProcess(
          new BackgroundGroupIndexationProcess(domainDriverManager, context));
      return context;
    } catch (Exception e) {
      SynchroDomainReport.error(ADMIN_SYNCHRONIZE_GROUPS,
          "Problème lors de la synchronisation des groupes : " + e.getMessage(), null);
      throw new AdminException(
          "Fails to synchronize groups in domain " + domainId, e);
    }
  }

  private void synchroRemoveGroup(final SyncOfGroupsContext context, final String specificId,
      final GroupDetail silverpeasGroup) {
    try {
      final List<GroupDetail> removedGroups = groupManager.removeGroup(silverpeasGroup, false);
      removedGroups.forEach(g -> {
        groupSynchroScheduler.removeFromContext(g);
        cache.opRemoveGroup(g);
        context.getRemovedGroups().put(g.getId(), g);
        context.appendToReport(
            format("removing group {0} (id:{1})\n", g.getName(), g.getSpecificId()));
        SynchroDomainReport.info(ADMIN_SYNCHRONIZE_GROUPS,
            "GroupDetail " + g.getName() + " removed (SpecificId:" + g.getSpecificId() + ")");
      });
    } catch (AdminException aeDel) {
      SilverLogger.getLogger(this)
          .error("Full synchro: error while removing group " + specificId, aeDel);
      context.appendToReport(format(
          "problem removing group {0} (specificId:{1}) - {2}\ngroup has not been removed\n",
          silverpeasGroup.getName(), specificId, aeDel.getMessage()));
    }
  }

  /**
   * Checks for new groups recursively
   */
  // In first call: (domainId,silverpeasGroups,distantRootGroups,
  // allDistantGroups(vide), userIds, null)
  // No need to refresh cache: the cache is reset at the end of the synchronization
  private void checkOutGroups(final SyncOfGroupsContext context,
      final CheckoutGroupDescriptor descriptor) throws AdminException {
    // Add new groups or existing ones to update from distant data source
    descriptor.addDistantTestedGroupsInAllSavedDistantGroups();
    for (final GroupDetail distantGroup : descriptor.getDistantTestedGroups()) {
      // Prepare GroupDetail to be at Silverpeas format
      distantGroup.setDomainId(context.getDomainId());
      final String specificId = distantGroup.getSpecificId();
      // search for group into Silverpeas database
      final Optional<GroupDetail> foundGroup = descriptor.getAllSilverpeasGroups().stream()
          .filter(g -> g.getSpecificId().equals(specificId) ||
              (shouldFallbackGroupNames && g.getSpecificId().equals(distantGroup.getName())))
          .findFirst();
      // if found, update, else create
      final String silverpeasId;
      if (foundGroup.isPresent()) {
        SynchroDomainReport.debug(ADMIN_SYNCHRONIZE_CHECK_OUT_GROUPS,
            "avant maj du groupe " + specificId + ", recherche de ses groupes parents");
        final GroupDetail silverpeasGroup = foundGroup.get();
        if (silverpeasGroup.isRemovedState()) {
          silverpeasId =
              restoreGroupDuringSynchronization(context, descriptor, distantGroup, silverpeasGroup);
        } else if (mergeDistantGroupIntoSilverpeasGroup(context, descriptor, distantGroup,
            silverpeasGroup)) {
          silverpeasId = updateGroupDuringSynchronization(context, specificId, silverpeasGroup);
        } else {
          silverpeasId = silverpeasGroup.getId();
        }
      } else {
        SynchroDomainReport.debug(ADMIN_SYNCHRONIZE_CHECK_OUT_GROUPS,
            "avant ajout du groupe " + specificId + ", recherche de ses groupes parents");
        mergeDistantGroupIntoSilverpeasGroup(context, descriptor, distantGroup, distantGroup);
        silverpeasId = addGroupDuringSynchronization(context, specificId, distantGroup);
      }
      // Recurse with subgroups
      recursWithSubGroups(context, descriptor, specificId, silverpeasId);
    }
  }

  private String restoreGroupDuringSynchronization(final SyncOfGroupsContext context,
      final CheckoutGroupDescriptor descriptor, final GroupDetail distantGroup,
      final GroupDetail silverpeasGroup) {
    final String specificId = silverpeasGroup.getSpecificId();
    try {
      groupManager.restoreGroup(silverpeasGroup, false);
      final GroupDetail updatedRestoreData = getGroup(silverpeasGroup.getId());
      silverpeasGroup.setState(updatedRestoreData.getState());
      silverpeasGroup.setStateSaveDate(updatedRestoreData.getStateSaveDate());
      if (mergeDistantGroupIntoSilverpeasGroup(context, descriptor, distantGroup,
          silverpeasGroup)) {
        groupManager.updateGroup(silverpeasGroup, false);
      }
      final String groupId = silverpeasGroup.getId();
      context.getRestoredGroups().put(groupId, silverpeasGroup);
      final String message = format("{0} {1} restored (id:{2} / specificId:{3})", GROUP,
          silverpeasGroup.getName(), groupId, specificId);
      context.appendToReport(message).appendToReport("\n");
      SynchroDomainReport.debug(ADMIN_SYNCHRONIZE_GROUPS, message);
      return groupId;
    } catch (AdminException aeDel) {
      SilverLogger.getLogger(this)
          .error("Full synchro: error while restoring group " + specificId, aeDel);
      final String message = format("problem restoring group {0} (specificId:{1}) - {2}",
          silverpeasGroup.getName(), specificId, aeDel.getMessage());
      context.appendToReport(message).appendToReport("\n");
      SynchroDomainReport.warn(ADMIN_SYNCHRONIZE_GROUPS, message);
      context.appendToReport("group has not been restored\n");
    }
    return EMPTY;
  }

  /**
   * Merge the data of a distant group into the data of a silverpeas group : - group identifier (the
   * distant one) - name - description - parent group - users of group
   * @param context the synchronization process context.
   * @param descriptor the descriptor of current group synchronization.
   * @param distantGroup {@link GroupDetail} representing data on external repository.
   * @param silverpeasGroup {@link GroupDetail} representing data on silverpeas.
   * @return true if a data has changed, false otherwise.
   */
  boolean mergeDistantGroupIntoSilverpeasGroup(final SyncOfGroupsContext context,
      final CheckoutGroupDescriptor descriptor,
      final GroupDetail distantGroup, final GroupDetail silverpeasGroup) throws AdminException {
    boolean dataUpdated =
        !Objects.equals(silverpeasGroup.getSpecificId(), distantGroup.getSpecificId());
    silverpeasGroup.setSpecificId(distantGroup.getSpecificId());
    dataUpdated |= !Objects.equals(silverpeasGroup.getName(), distantGroup.getName());
    silverpeasGroup.setName(distantGroup.getName());
    dataUpdated |= !Objects.equals(silverpeasGroup.getDescription(), distantGroup.getDescription());
    silverpeasGroup.setDescription(distantGroup.getDescription());
    dataUpdated |= setParentGroup(context, descriptor, silverpeasGroup);
    distantGroup.setSuperGroupId(silverpeasGroup.getSuperGroupId());
    dataUpdated |= setUserIds(context, distantGroup, silverpeasGroup);
    distantGroup.setUserIds(silverpeasGroup.getUserIds());
    if (silverpeasGroup.isRemovedState()) {
      return dataUpdated;
    }
    final GroupState distantState = Optional.of(distantGroup.getState())
        .filter(not(GroupState.UNKNOWN::equals))
        .orElse(GroupState.VALID);
    dataUpdated |= !Objects.equals(silverpeasGroup.getState(), distantState);
    silverpeasGroup.setState(distantState);
    return dataUpdated;
  }

  private void recursWithSubGroups(final SyncOfGroupsContext context,
      final CheckoutGroupDescriptor descriptor, final String specificId, final String silverpeasId)
      throws AdminException {
    if (isDefined(silverpeasId)) {
      final GroupDetail[] subGroups = domainDriverManager.getGroups(silverpeasId);
      if (subGroups != null && subGroups.length > 0) {
        final GroupDetail[] cleanSubGroups =
            removeCrossReferences(subGroups, descriptor.getAllSavedDistantGroups(), specificId);
        if (cleanSubGroups.length > 0) {
          SynchroDomainReport.debug(ADMIN_SYNCHRONIZE_CHECK_OUT_GROUPS,
              "Ajout ou mise à jour de " + cleanSubGroups.length + " groupes fils du groupe " +
                  specificId + "...");
          descriptor.setDistantTestedGroups(cleanSubGroups).setSuperGroupId(silverpeasId);
          checkOutGroups(context, descriptor);
        }
      }
    }
  }

  @Nullable
  private String addGroupDuringSynchronization(final SyncOfGroupsContext context,
      final String specificId,
      final GroupDetail group) {
    String silverpeasId = null;
    try {
      silverpeasId = groupManager.addGroup(group, true, false);
      if (StringUtil.isDefined(silverpeasId)) {
        context.getAddedGroups().put(silverpeasId, group);
        context.appendToReport(format("adding group {0} (id:{1})\n", group.getName(), specificId));
        SynchroDomainReport.debug(ADMIN_SYNCHRONIZE_CHECK_OUT_GROUPS,
            "ajout groupe " + group.getName() + ID_IS + silverpeasId + ") OK");
      } else {
        // the group name not defined
        context.appendToReport(format("problem adding group id: {0}", specificId));
      }
    } catch (AdminException aeAdd) {
      context.appendToReport(format(
          "problem adding group {0} (id:{1}) {2}\ngroup has not been added\n",
          group.getName(), specificId, aeAdd.getMessage()));
    }
    return silverpeasId;
  }

  private String updateGroupDuringSynchronization(final SyncOfGroupsContext context,
      final String specificId, final GroupDetail group) {
    String silverpeasId = null;
    try {
      final String result = groupManager.updateGroup(group, true);
      if (StringUtil.isDefined(result)) {
        context.getUpdatedGroups().put(group.getId(), group);
        silverpeasId = group.getId();
        context.appendToReport(
            format("updating group {0} (id:{1})\n", group.getName(), specificId));
        SynchroDomainReport.debug(ADMIN_SYNCHRONIZE_CHECK_OUT_GROUPS,
            "maj groupe " + group.getName() + ID_IS + silverpeasId + ") OK");
      } else {
        // the group name not defined
        SilverLogger.getLogger(this)
            .error("Full Synchro: error while updating group {0}", specificId);
        context.appendToReport(format("problem updating group id : {0}\n", specificId));
      }
    } catch (AdminException aeMaj) {
      SilverLogger.getLogger(this)
          .error("Full Synchro: error while updating group {0}: ", specificId, aeMaj.getMessage());
      context.appendToReport(format(
          "problem updating group {0} (id:{1}) {2}\ngroup has not been updated\n",
          group.getName(), specificId, aeMaj.getMessage()));
    }
    return silverpeasId;
  }

  private boolean setUserIds(final SyncOfGroupsContext context, final GroupDetail distantGroup,
      final GroupDetail silverpeasGroup) {
    final List<String> previousUserIds = stream(silverpeasGroup.getUserIds())
        .distinct()
        .sorted()
        .collect(toList());
    final List<String> newUserIds = stream(distantGroup.getUserIds())
        .map(context.getUserIdsMapping()::get)
        .filter(Objects::nonNull)
        .distinct()
        .sorted()
        .collect(toList());
    silverpeasGroup.setUserIds(newUserIds.toArray(String[]::new));
    return !String.join(",", previousUserIds).equals(String.join(",", newUserIds));
  }

  private boolean setParentGroup(final SyncOfGroupsContext context,
      final CheckoutGroupDescriptor descriptor, final GroupDetail group) throws AdminException {
    final String specificId = group.getSpecificId();
    final String currentSuperGroupId = group.getSuperGroupId();
    final String[] groupParentsIds = domainDriverManager.getGroupMemberGroupIds(
        context.getDomainId(), group.getSpecificId());
    if (groupParentsIds == null || groupParentsIds.length == 0) {
      group.setSuperGroupId(null);
      SynchroDomainReport.debug(ADMIN_SYNCHRONIZE_CHECK_OUT_GROUPS,
          "le groupe " + specificId + " n'a pas de père");
    } else {
      // security
      final String superGroupId = descriptor.getSuperGroupId();
      group.setSuperGroupId(superGroupId);
      if (superGroupId != null) {
        SynchroDomainReport.debug(ADMIN_SYNCHRONIZE_CHECK_OUT_GROUPS,
            "le groupe " + specificId + " a pour père le groupe " +
                domainDriverManager.getGroup(superGroupId).getSpecificId() +
                " d'Id base " + superGroupId);
      }
    }
    return !Objects.equals(currentSuperGroupId, group.getSuperGroupId());
  }

  private GroupDetail[] removeCrossReferences(GroupDetail[] subGroups,
      Map<String, GroupDetail> allIncludedGroups, String fatherId) {
    final List<GroupDetail> cleanSubGroups = new ArrayList<>(subGroups.length);
    for (final GroupDetail subGroup : subGroups) {
      if (allIncludedGroups.get(subGroup.getSpecificId()) == null) {
        cleanSubGroups.add(subGroup);
      } else {
        SilverLogger.getLogger(this)
            .warn("Cross deletion for child {0} of the father {1}", subGroup.getSpecificId(),
                fatherId);
      }
    }
    return cleanSubGroups.toArray(new GroupDetail[0]);
  }

  @Override
  public List<String> searchUserIdsByProfile(final List<String> profileIds,
      final boolean includeRemovedUsersAndGroups) throws AdminException {
    Set<String> userIds = new HashSet<>();
    // search users in profiles
    try {
      for (String profileId : profileIds) {
        ProfileInst profile = profileManager.getProfileInst(profileId, includeRemovedUsersAndGroups);
        // add users directly attach to profile
        addAllUsersInProfile(profile, userIds);
      }
    } catch (Exception e) {
      throw new AdminException("Fail to search user ids by some profiles", e);
    }

    return new ArrayList<>(userIds);
  }

  // -------------------------------------------------------------------------
  // For SelectionPeas
  // -------------------------------------------------------------------------

  @Override
  public SilverpeasList<UserDetail> searchUsers(final UserDetailsSearchCriteria searchCriteria)
      throws AdminException {
    setAllValidGroupChildren(searchCriteria);
    final List<String> roleNames = Optional.ofNullable(searchCriteria.getCriterionOnRoleNames())
        .filter(ArrayUtil::isNotEmpty)
        .stream()
        .flatMap(Stream::of)
        .collect(toList());
    if (searchCriteria.isCriterionOnComponentInstanceIdSet()) {
      final String instanceId = searchCriteria.getCriterionOnComponentInstanceId();
      final SilverpeasComponentInstance instance = getComponentInstance(instanceId);
      if (instance == null) {
        // instance does not exist, stopping the search
        SilverLogger.getLogger(this).warn(failureOnGetting("component instance", instanceId));
        return ListSlice.emptyList();
      } else if (!roleNames.isEmpty() || !instance.isPublic()) {
        if (!instance.isPersonal()) {
          setGroupsPlayingRole(roleNames, instance, searchCriteria);
        } else {
          final String currentUserId =
              getCurrentUserPlayingRole(roleNames, instance, searchCriteria);
          if (StringUtil.isDefined(currentUserId)) {
            searchCriteria.onUserIds(currentUserId);
          } else {
            return ListSlice.emptyList();
          }
        }
      }
    } else if (!roleNames.isEmpty()) {
      SilverLogger.getLogger(this)
          .warn("searching users on role name(s) {0} without specifying a component instance " +
              "identifier. No role name filtering is performed.");
    }
    return userManager.getUsersMatchingCriteria(searchCriteria);
  }

  private String getCurrentUserPlayingRole(final List<String> listOfRoleNames,
      final SilverpeasComponentInstance instance, final UserDetailsSearchCriteria searchCriteria) {
    final User user = ((SilverpeasPersonalComponentInstance) instance).getUser();
    return Optional.of(instance.getSilverpeasRolesFor(user)
            .stream()
            .map(SilverpeasRole::getName)
            .collect(toList()))
        .filter(r -> listOfRoleNames.isEmpty() || !intersection(r, listOfRoleNames).isEmpty())
        .filter(r -> !searchCriteria.isCriterionOnUserIdsSet() ||
            contains(searchCriteria.getCriterionOnUserIds(), user.getId()))
        .map(r -> user.getId())
        .orElse(EMPTY);
  }

  private void setGroupsPlayingRole(final List<String> roleNames,
      final SilverpeasComponentInstance instance, final UserDetailsSearchCriteria searchCriteria)
      throws AdminException {
    final Set<String> groupIds = getRecursivelyValidGroupsIdPlaying(roleNames, instance,
        searchCriteria.getCriterionOnResourceId());
    final String[] groupIdArray = groupIds.toArray(new String[0]);
    searchCriteria.onGroupsInRoles(groupIdArray);
    if (searchCriteria.isCriterionOnAnyGroupSet()) {
      searchCriteria.onGroupIds(groupIdArray);
    }
  }

  private void setAllValidGroupChildren(final UserDetailsSearchCriteria searchCriteria)
      throws AdminException {
    if (searchCriteria.isCriterionOnGroupIdsSet()) {
      final Set<String> allGroupsId = new HashSet<>();
      for (String aGroupId : searchCriteria.getCriterionOnGroupIds()) {
        allGroupsId.addAll(groupManager.getAllSubGroupIdsRecursively(aGroupId));
        allGroupsId.add(aGroupId);
      }
      searchCriteria.onGroupIds(allGroupsId.toArray(new String[0]));
    }
  }

  private Set<String> getRecursivelyValidGroupsIdPlaying(final List<String> roleNames,
      final SilverpeasComponentInstance instance, final String resourceId) throws AdminException {
    final Set<String> allGroupIds = new HashSet<>();
    final List<ProfileInst> profiles;
    if (StringUtil.isDefined(resourceId)) {
      profiles = getProfileInstsFor(resourceId, instance.getId());
    } else {
      profiles = getComponentInst(instance.getId()).getAllProfilesInst();
    }
    for (ProfileInst aProfile : profiles) {
      if (roleNames.isEmpty() || roleNames.contains(aProfile.getName())) {
        // groups (and recursively their subgroups) playing the role
        List<String> groupIds = aProfile.getAllGroups();
        for (String aGroupId : groupIds) {
          allGroupIds.add(aGroupId);
          allGroupIds.addAll(groupManager.getAllSubGroupIdsRecursively(aGroupId));
        }
      }
    }
    return allGroupIds;
  }

  private void addAllUsersInProfile(final ProfileInst aProfile, final Collection<String> userIds)
      throws AdminException {
    userIds.addAll(aProfile.getAllUsers());

    // users of the groups (and recursively of their subgroups) playing the role
    List<String> groupIds = aProfile.getAllGroups();
    List<String> allGroupIds = new ArrayList<>();
    for (String aGroupId : groupIds) {
      allGroupIds.add(aGroupId);
      allGroupIds.addAll(groupManager.getAllSubGroupIdsRecursively(aGroupId));
    }
    userIds.addAll(userManager.getAllUserIdsInGroups(allGroupIds));
  }

  @Override
  public SilverpeasList<GroupDetail> searchGroups(final GroupsSearchCriteria searchCriteria)
      throws AdminException {
    final List<String> roleNames =
        Optional.ofNullable(searchCriteria.getAndClearCriterionOnRoleNames())
            .filter(ArrayUtil::isNotEmpty)
            .stream()
            .flatMap(Stream::of)
            .collect(toList());
    if (searchCriteria.isCriterionOnComponentInstanceIdSet()) {
      // ok, replace role names and component instance by role ids.
      SilverpeasComponentInstance instance =
          getComponentInstance(searchCriteria.getCriterionOnComponentInstanceId());
      if (!roleNames.isEmpty() || !instance.isPublic()) {
        List<String> roleIds = new ArrayList<>();
        if (!instance.isPersonal()) {
          List<ProfileInst> profiles;
          if (searchCriteria.isCriterionOnResourceIdSet()) {
            profiles =
                getProfileInstsFor(searchCriteria.getCriterionOnResourceId(), instance.getId());
          } else {
            profiles = getComponentInst(instance.getId()).getAllProfilesInst();
          }
          profiles.stream()
              .filter(p -> roleNames.isEmpty() || roleNames.contains(p.getName()))
              .forEach(p -> roleIds.add(p.getId()));
          // if empty, given criteria are not consistent. A dummy role id is set in order to get an
          // empty result
          Optional.of(roleIds).filter(List::isEmpty).ifPresent(r -> r.add("-1000"));
        }
        searchCriteria.onRoleNames(roleIds.toArray(new String[0]));
      }
    } else if (!roleNames.isEmpty()) {
      SilverLogger.getLogger(this)
          .warn("searching groups on role name(s) {0} without specifying a component instance " +
              "identifier. No role name filtering is performed.");
    }
    return groupManager.getGroupsMatchingCriteria(searchCriteria);
  }

  // -------------------------------------------------------------------------
  // Node profile management
  // -------------------------------------------------------------------------
  @Override
  public void indexAllUsers() throws AdminException {

    // starting by remove all indexed data relative to users
    IndexEngineProxy.removeScopedIndexEntries(UserIndexation.COMPONENT_ID);

    Domain[] domains = getAllDomains();
    for (Domain domain : domains) {
      try {
        indexUsers(domain.getId());
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
  }

  @Override
  public void indexUsers(String domainId) throws AdminException {
    try {
      domainDriverManager.indexAllUsers(domainId);
    } catch (Exception e) {
      throw new AdminException(failureOnIndexing("users in domain", domainId), e);
    }
  }

  @Override
  public String copyAndPasteComponent(PasteDetail pasteDetail)
      throws AdminException, QuotaException {
    if (!StringUtil.isDefined(pasteDetail.getToSpaceId())) {
      // cannot paste component on root
      return null;
    }
    ComponentInst newCompo = new ComponentInst(getComponentInst(pasteDetail.getFromComponentId()));
    SpaceInst destinationSpace = getSpaceInstById(pasteDetail.getToSpaceId());

    String lang = newCompo.getLanguage();
    if (StringUtil.isNotDefined(lang)) {
      lang = I18NHelper.DEFAULT_LANGUAGE;
    }
    // Creation
    newCompo.setLocalId(-1);
    newCompo.setDomainFatherId(destinationSpace.getId());
    newCompo.setOrderNum(destinationSpace.getNumComponentInst());
    newCompo.setCreationDate(new Date());
    newCompo.setCreatorUserId(pasteDetail.getUserId());
    newCompo.setLanguage(lang);

    // Rename if componentName already exists in the destination space
    String label = renameComponentName(newCompo.getLabel(lang), destinationSpace.
        getAllComponentsInst());
    newCompo.setLabel(label);
    ComponentI18N translation = newCompo.getTranslations().get(lang);
    if (translation != null) {
      translation.setName(label);
    }

    // Delete inherited profiles only
    // It will be processed by admin
    newCompo.removeInheritedProfiles();

    // Add the component
    String sComponentId = addComponentInst(pasteDetail.getUserId(), newCompo);

    // Execute specific paste by the component
    try {
      pasteDetail.setToComponentId(sComponentId);
      ApplicationResourcePasting componentPaste =
          ServiceProvider.getServiceByComponentInstanceAndNameSuffix(
              pasteDetail.getFromComponentId(), ApplicationResourcePasting.NAME_SUFFIX);
      componentPaste.paste(pasteDetail);
    } catch (IllegalStateException e) {
      SilverLogger.getLogger(this).silent(e);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
    return sComponentId;
  }

  private String renameComponentName(String label, List<ComponentInst> listComponents) {
    String newComponentLabel = label;
    for (ComponentInst componentInst : listComponents) {
      if (componentInst.getLabel().equals(newComponentLabel)) {
        newComponentLabel = "Copie de " + label;
        return renameComponentName(newComponentLabel, listComponents);
      }
    }
    return newComponentLabel;
  }

  //check if spaceId is not parent of anotherSpace
  private boolean isParent(int spaceId, Integer anotherSpaceId) throws AdminException {
    if (anotherSpaceId == null || anotherSpaceId < 0) {
      return false;
    }
    List<SpaceInstLight> path = treeCache.getSpacePath(anotherSpaceId);
    if (path.isEmpty()) {
      path = getPathToSpace(String.valueOf(anotherSpaceId), true);
    }
    for (SpaceInstLight space : path) {
      if (spaceId == space.getLocalId()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String copyAndPasteSpace(PasteDetail pasteDetail) throws AdminException, QuotaException {
    String newSpaceId = null;
    String spaceId = pasteDetail.getFromSpaceId();
    String toSpaceId = pasteDetail.getToSpaceId();
    boolean pasteAllowed = !isParent(getDriverSpaceId(spaceId), getDriverSpaceId(toSpaceId));
    if (pasteAllowed) {
      // paste space itself
      SpaceInst oldSpace = getSpaceInstById(spaceId);
      SpaceInst newSpace = createPasteSpace(pasteDetail, oldSpace, toSpaceId);

      // Remove inherited profiles from cloned space
      newSpace.removeInheritedProfiles();

      // Remove components from cloned space
      List<ComponentInst> components = newSpace.getAllComponentsInst();
      newSpace.removeAllComponentsInst();

      // Add space
      newSpaceId = addSpaceInst(pasteDetail.getUserId(), newSpace);

      // Copy space quota
      copySpaceQuota(oldSpace, newSpace);

      // paste components
      String componentIdAsHomePage =
          pasteComponentsOfSpace(pasteDetail, newSpaceId, newSpace, components);

      // paste subspaces
      pasteSubspacesOfSpace(pasteDetail, newSpaceId, newSpace);

      // update parameter of space home page if needed
      String newFirstPageExtraParam = null;
      if (StringUtil.isDefined(componentIdAsHomePage)) {
        newFirstPageExtraParam = componentIdAsHomePage;
      } else if (newSpace.getFirstPageType() == SpaceInst.FP_TYPE_HTML_PAGE) {
        String oldURL = newSpace.getFirstPageExtraParam();
        newFirstPageExtraParam = oldURL.replaceAll(spaceId, newSpaceId);
      }

      if (StringUtil.isDefined(newFirstPageExtraParam)) {
        SpaceInst space = getSpaceInstById(newSpaceId);
        space.setFirstPageExtraParam(newFirstPageExtraParam);
        updateSpaceInst(space);
      }
    }
    return newSpaceId;
  }

  private void pasteSubspacesOfSpace(final PasteDetail pasteDetail, final String newSpaceId,
      final SpaceInst newSpace) throws AdminException, QuotaException {
    PasteDetail subSpacePasteDetail = new PasteDetail(pasteDetail.getUserId());
    subSpacePasteDetail.setOptions(pasteDetail.getOptions());
    subSpacePasteDetail.setToSpaceId(newSpaceId);
    List<SpaceInst> subSpaceInsts = newSpace.getSubSpaces();
    for (SpaceInst subSpaceInst : subSpaceInsts) {
      subSpacePasteDetail.setFromSpaceId(subSpaceInst.getId());
      copyAndPasteSpace(subSpacePasteDetail);
    }
  }

  @Nullable
  private String pasteComponentsOfSpace(final PasteDetail pasteDetail, final String newSpaceId,
      final SpaceInst newSpace, final List<ComponentInst> components)
      throws AdminException, QuotaException {
    // verify space homepage
    String componentIdAsHomePage = null;
    if (newSpace.getFirstPageType() == SpaceInst.FP_TYPE_COMPONENT_INST) {
      componentIdAsHomePage = newSpace.getFirstPageExtraParam();
    }

    // paste components of space
    PasteDetail componentPasteDetail = new PasteDetail(pasteDetail.getUserId());
    componentPasteDetail.setOptions(pasteDetail.getOptions());
    componentPasteDetail.setToSpaceId(newSpaceId);
    for (ComponentInst component : components) {
      componentPasteDetail.setFromComponentId(component.getId());
      String componentId = copyAndPasteComponent(componentPasteDetail);
      // check if new component must be used as home page of new space
      if (componentIdAsHomePage != null && componentIdAsHomePage.equals(component.getId())) {
        componentIdAsHomePage = componentId;
      }
    }
    return componentIdAsHomePage;
  }

  private void copySpaceQuota(final SpaceInst oldSpace, final SpaceInst newSpace)
      throws QuotaException {
    Quota dataStorageQuota = SpaceServiceProvider.getDataStorageSpaceQuotaService()
        .get(DataStorageSpaceQuotaKey.from(oldSpace));
    if (dataStorageQuota.exists()) {
      SpaceServiceProvider.getDataStorageSpaceQuotaService()
          .initialize(DataStorageSpaceQuotaKey.from(newSpace), dataStorageQuota);
    }
    Quota componentQuota = SpaceServiceProvider.getComponentSpaceQuotaService()
        .get(ComponentSpaceQuotaKey.from(oldSpace));
    if (componentQuota.exists()) {
      SpaceServiceProvider.getComponentSpaceQuotaService()
          .initialize(ComponentSpaceQuotaKey.from(newSpace), componentQuota);
    }
  }

  @Nonnull
  private SpaceInst createPasteSpace(final PasteDetail pasteDetail, final SpaceInst oldSpace,
      final String toSpaceId) throws AdminException {
    SpaceInst newSpace = new SpaceInst(oldSpace);
    newSpace.setLocalId(-1);
    List<String> newBrotherIds;
    if (StringUtil.isDefined(toSpaceId)) {
      SpaceInst destinationSpace = getSpaceInstById(toSpaceId);
      newSpace.setDomainFatherId(destinationSpace.getId());
      List<SpaceInst> brothers = destinationSpace.getSubSpaces();
      newBrotherIds = new ArrayList<>(brothers.size());
      for (SpaceInst brother : brothers) {
        newBrotherIds.add(brother.getId());
      }
    } else {
      newSpace.setDomainFatherId("-1");
      newBrotherIds = Arrays.asList(getAllRootSpaceIds());
    }
    newSpace.setOrderNum(newBrotherIds.size());
    newSpace.setCreationDate(new Date());
    newSpace.setCreatorUserId(pasteDetail.getUserId());
    String lang = oldSpace.getLanguage();
    if (StringUtil.isNotDefined(lang)) {
      lang = I18NHelper.DEFAULT_LANGUAGE;
    }
    newSpace.setLanguage(lang);

    // Rename if spaceName already used in the destination space
    List<SpaceInstLight> subSpaces = new ArrayList<>();
    for (String subSpaceId : newBrotherIds) {
      subSpaces.add(getSpaceInstLight(getDriverSpaceId(subSpaceId)));
    }
    String name = renameSpace(newSpace.getName(newSpace.getLanguage()), subSpaces);
    newSpace.setName(name);

    return newSpace;
  }

  private String renameSpace(String label, List<SpaceInstLight> listSpaces) {
    String newSpaceLabel = label;
    for (SpaceInstLight space : listSpaces) {
      if (space.getName().equals(newSpaceLabel)) {
        newSpaceLabel = "Copie de " + label;
        return renameSpace(newSpaceLabel, listSpaces);
      }
    }
    return newSpaceLabel;
  }

  /**
   * Gets all the profile instances defined for the specified resource in the specified component
   * instance.
   * @param resourceId the unique identifier of a resource managed in a component instance.
   * @param instanceId the unique identifier of the component instance.
   * @return a list of profile instances.
   */
  private List<ProfileInst> getProfileInstsFor(String resourceId, String instanceId)
      throws AdminException {
    Pattern objectIdPattern = Pattern.compile("([a-zA-Z]+)(\\d+)");
    Matcher matcher = objectIdPattern.matcher(resourceId);
    if (matcher.matches() && matcher.groupCount() == 2) {
      String type = matcher.group(1);
      String id = matcher.group(2);
      ProfiledObjectId objectId = new ProfiledObjectId(ProfiledObjectType.fromCode(type), id);
      return getProfilesByObject(objectId, instanceId);
    }
    throw new AdminPersistenceException(
        failureOnGetting("profiles on resource " + resourceId, "of component " + instanceId));
  }

  /**
   * Get all the space profiles identifier for the given user without group transitivity.
   */
  private String[] getDirectSpaceProfileIdsOfUser(String sUserId) throws AdminException {
    try {
      return spaceProfileManager.getSpaceProfileIdsOfUserType(sUserId);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("space profiles of user", sUserId), e);
    }
  }

  /**
   * Get all the space profiles identifier for the given group
   */
  private String[] getDirectSpaceProfileIdsOfGroup(String groupId) throws AdminException {
    try {
      return spaceProfileManager.getSpaceProfileIdsOfGroupType(groupId);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("space profiles of group", groupId), e);
    }
  }

  /**
   * Get all the component profile ids for the given user without group transitivity. (component
   * object profiles are not retrieved)
   */
  private String[] getDirectComponentProfileIdsOfUser(String sUserId) throws AdminException {
    try {
      return profileManager.getProfileIdsOfUser(sUserId, Collections.emptyList());
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("component profiles of user", sUserId), e);
    }
  }

  /**
   * Get all the component profile ids for the given group. (component object profiles are not
   * retrieved)
   */
  private String[] getDirectComponentProfileIdsOfGroup(String groupId) throws AdminException {
    try {
      return profileManager.getProfileIdsOfGroup(groupId);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("component profiles of group", groupId), e);
    }
  }

  /**
   * Get all the component object profile ids for the given user. (direct component profiles are not
   * retrieved)
   */
  private String[] getComponentObjectProfileIdsOfUserType(String userId) throws AdminException {
    try {
      // retrieve value from database
      return profileManager.getAllComponentObjectProfileIdsOfUser(userId, Collections.emptyList());
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("component profiles of user", userId), e);
    }
  }

  /**
   * Get all the component object profile ids for the given group. (direct component profiles are
   * not retrieved)
   */
  private String[] getComponentObjectProfileIdsOfGroupType(String sGroupId) throws AdminException {
    try {
      // retrieve value from database
      return profileManager.getAllComponentObjectProfileIdsOfGroup(sGroupId);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("component profiles of group", sGroupId), e);
    }
  }

  /*
   * Add target profiles (space, components, nodes)
   */
  private void addTargetProfiles(RightAssignationContext context, String[] sourceSpaceProfileIds,
      String[] sourceComponentProfileIds, String[] sourceNodeProfileIds) throws AdminException {

    //Add space rights (and children spaces and components, by inheritance)
    for (String spaceProfileId : sourceSpaceProfileIds) {
      SpaceProfileInst currentSourceSpaceProfile = getSpaceProfileInst(spaceProfileId);
      if (currentSourceSpaceProfile != null) {
        SpaceInstLight spaceInst =
            getSpaceInstLight(getDriverSpaceId(currentSourceSpaceProfile.getSpaceFatherId()));
        if (spaceInst != null && !spaceInst.isPersonalSpace()) {
          // do not treat the personal space
          addInRightProfile(context, currentSourceSpaceProfile);
          updateSpaceProfileInst(currentSourceSpaceProfile, context.getAuthor());
        }
      }
    }

    //Add component rights
    addComponentRights(context, sourceComponentProfileIds);

    //Add nodes rights
    addComponentRights(context, sourceNodeProfileIds);
  }

  private void addComponentRights(final RightAssignationContext context,
      final String[] sourceComponentProfileIds) throws AdminException {
    for (String profileId : sourceComponentProfileIds) {
      ProfileInst currentSourceProfile = getProfileInst(profileId);
      ComponentInst currentComponent =
          getComponentInst(currentSourceProfile.getComponentFatherId(), null);
      String spaceId = currentComponent.getDomainFatherId();
      SpaceInstLight spaceInst = getSpaceInstLight(getDriverSpaceId(spaceId));
      if (currentComponent.getStatus() == null && spaceInst != null &&
          !spaceInst.isPersonalSpace()) {
        // do not treat the personal space
        addInRightProfile(context, currentSourceProfile);
        doUpdateProfileInst(currentSourceProfile, context.getAuthor());
      }
    }
  }

  private void addInRightProfile(final RightAssignationContext context,
      final RightProfile rightProfile) {
    if (context.getTargetType() == RightAssignationContext.RESOURCE_TYPE.USER) {
      rightProfile.addUser(context.getTargetId());
    } else if (context.getTargetType() == RightAssignationContext.RESOURCE_TYPE.GROUP) {
      rightProfile.addGroup(context.getTargetId());
    }
  }

  /*
   * Delete target profiles about spaces, components and component objects (node for example).
   */
  private void deleteTargetProfiles(RightAssignationContext context,
      final String[] spaceProfileIdsToDeleteForTarget, final String[] componentProfileIdsForTarget)
      throws AdminException {

    // Delete space rights (and children spaces and components, by inheritance) for target
    for (String spaceProfileId : spaceProfileIdsToDeleteForTarget) {
      SpaceProfileInst currentTargetSpaceProfile = getSpaceProfileInst(spaceProfileId);
      SpaceInstLight spaceInst =
          getSpaceInstLight(getDriverSpaceId(currentTargetSpaceProfile.getSpaceFatherId()));
      if (spaceInst != null && !spaceInst.isPersonalSpace()) {// do not treat the personal space
        removeFromRightProfile(context, currentTargetSpaceProfile);
        updateSpaceProfileInst(currentTargetSpaceProfile, context.getAuthor());
      }
    }

    // Delete component and node rights for target (it is the same Profile manager that handles
    // component and node rights)
    for (String profileId : componentProfileIdsForTarget) {
      ProfileInst currentTargetProfile = getProfileInst(profileId);
      ComponentInst currentComponent =
          getComponentInst(currentTargetProfile.getComponentFatherId(), null);
      String spaceId = currentComponent.getDomainFatherId();
      SpaceInstLight spaceInst = getSpaceInstLight(getDriverSpaceId(spaceId));

      if (currentComponent.getStatus() == null && spaceInst != null &&
          !spaceInst.isPersonalSpace()) {// do not treat the personal space
        removeFromRightProfile(context, currentTargetProfile);
        doUpdateProfileInst(currentTargetProfile, context.getAuthor());
      }
    }
  }

  private void removeFromRightProfile(final RightAssignationContext context,
      final RightProfile rightProfile) {
    if (context.getTargetType() == RightAssignationContext.RESOURCE_TYPE.USER) {
      rightProfile.removeUser(context.getTargetId());
    } else if (context.getTargetType() == RightAssignationContext.RESOURCE_TYPE.GROUP) {
      rightProfile.removeGroup(context.getTargetId());
    }
  }

  /**
   * Centralized method to copy or replace rights.
   * @param context the context that defined what the treatment must perform.
   * @throws AdminException if an error occurs
   */
  private void assignRightsFromSourceToTarget(RightAssignationContext context)
      throws AdminException {
    try {
      if (context.areSourceAndTargetEqual()) {
        //target = source, so nothing is done
        return;
      }

      String[] spaceProfileIdsToCopy = new String[0];
      String[] componentProfileIdsToCopy = new String[0];
      String[] componentObjectProfileIdsToCopy = new String[0];
      String[] spaceProfileIdsToReplace = new String[0];
      String[] componentProfileIdsToReplace = new String[0];

      // Loading existing source profile data identifiers
      if (context.getSourceType() == RightAssignationContext.RESOURCE_TYPE.USER) {
        spaceProfileIdsToCopy = getDirectSpaceProfileIdsOfUser(context.getSourceId());
        componentProfileIdsToCopy = getDirectComponentProfileIdsOfUser(context.getSourceId());
        if (context.isAssignObjectRights()) {
          componentObjectProfileIdsToCopy =
              getComponentObjectProfileIdsOfUserType(context.getSourceId());
        }

      } else if (context.getSourceType() == RightAssignationContext.RESOURCE_TYPE.GROUP) {
        spaceProfileIdsToCopy = getDirectSpaceProfileIdsOfGroup(context.getSourceId());
        componentProfileIdsToCopy = getDirectComponentProfileIdsOfGroup(context.getSourceId());
        if (context.isAssignObjectRights()) {
          componentObjectProfileIdsToCopy =
              getComponentObjectProfileIdsOfGroupType(context.getSourceId());
        }

      }
      // Loading existing target profile data identifiers
      if (RightAssignationContext.MODE.REPLACE.equals(context.getMode())) {
        if (context.getTargetType() == RightAssignationContext.RESOURCE_TYPE.USER) {
          spaceProfileIdsToReplace = getDirectSpaceProfileIdsOfUser(context.getTargetId());
          componentProfileIdsToReplace = getDirectComponentProfileIdsOfUser(context.getTargetId());

        } else if (context.getTargetType() == RightAssignationContext.RESOURCE_TYPE.GROUP) {
          spaceProfileIdsToReplace = getDirectSpaceProfileIdsOfGroup(context.getTargetId());
          componentProfileIdsToReplace = getDirectComponentProfileIdsOfGroup(context.getTargetId());

        }
      }


      // Deleting the current rights of the targeted resource (into the transaction)
      deleteTargetProfiles(context, spaceProfileIdsToReplace, componentProfileIdsToReplace);

      // Adding the new rights for the targeted resource (into the transaction)
      addTargetProfiles(context, spaceProfileIdsToCopy, componentProfileIdsToCopy,
          componentObjectProfileIdsToCopy);

    } catch (Exception e) {
      cache.resetCache();
      throw new AdminException("Fail to assign rights", e);
    }
  }

  @Override
  public void assignRightsFromUserToUser(RightAssignationContext.MODE operationMode,
      String sourceUserId, String targetUserId, boolean nodeAssignRights, String authorId)
      throws AdminException {
    RightAssignationContext context =
        initializeRightAssignationContext(operationMode, nodeAssignRights, authorId)
            .fromUserId(sourceUserId).toUserId(targetUserId);
    assignRightsFromSourceToTarget(context);
  }

  @Override
  public void assignRightsFromUserToGroup(RightAssignationContext.MODE operationMode,
      String sourceUserId, String targetGroupId, boolean nodeAssignRights, String authorId)
      throws AdminException {
    RightAssignationContext context =
        initializeRightAssignationContext(operationMode, nodeAssignRights, authorId)
            .fromUserId(sourceUserId).toGroupId(targetGroupId);
    assignRightsFromSourceToTarget(context);
  }

  @Override
  public void assignRightsFromGroupToUser(RightAssignationContext.MODE operationMode,
      String sourceGroupId, String targetUserId, boolean nodeAssignRights, String authorId)
      throws AdminException {
    RightAssignationContext context =
        initializeRightAssignationContext(operationMode, nodeAssignRights, authorId)
            .fromGroupId(sourceGroupId).toUserId(targetUserId);
    assignRightsFromSourceToTarget(context);
  }

  @Override
  public void assignRightsFromGroupToGroup(RightAssignationContext.MODE operationMode,
      String sourceGroupId, String targetGroupId, boolean nodeAssignRights, String authorId)
      throws AdminException {
    RightAssignationContext context =
        initializeRightAssignationContext(operationMode, nodeAssignRights, authorId)
            .fromGroupId(sourceGroupId).toGroupId(targetGroupId);
    assignRightsFromSourceToTarget(context);
  }

  /**
   * Initializing a right assignation context.
   * @param operationMode : value of {@link RightAssignationContext.MODE}
   * @param nodeAssignRights : true if you want also to add rights to nodes
   * @param authorId : the userId of the author of this action
   * @return the initialized {@link RightAssignationContext}
   */
  private RightAssignationContext initializeRightAssignationContext(
      RightAssignationContext.MODE operationMode, boolean nodeAssignRights, String authorId) {
    final RightAssignationContext context;
    if (operationMode == RightAssignationContext.MODE.REPLACE) {
      context = RightAssignationContext.replace();
    } else {
      context = RightAssignationContext.copy();
    }
    if (!nodeAssignRights) {
      context.withoutAssigningComponentObjectRights();
    }
    return context.setAuthor(authorId);
  }

  /**
   * @param userId the user identifier
   * @param domainId the domain identifier
   * @return true if user identified by given userId is the manager of given domain identifier
   */
  @Override
  public boolean isDomainManagerUser(String userId, String domainId) {
    UserDetail userDetail = null;
    try {
      userDetail = getUserDetail(userId);
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e);
    }
    return userDetail != null && userDetail.getDomainId().equals(domainId) &&
        UserAccessLevel.DOMAIN_ADMINISTRATOR.equals(userDetail.getAccessLevel());
  }

  static class CheckoutGroupDescriptor {
    private final Map<String, GroupDetail> allSavedDistantGroups = new HashMap<>();
    private final List<GroupDetail> allSilverpeasGroups;
    private GroupDetail[] distantTestedGroups;
    private String superGroupId;

    static CheckoutGroupDescriptor synchronizingDomainFrom(
        final List<GroupDetail> allSilverpeasGroups, final GroupDetail[] distantRootGroups) {
      return new CheckoutGroupDescriptor(allSilverpeasGroups, distantRootGroups);
    }

    static CheckoutGroupDescriptor synchronizingOneGroupWithSuperGroupId(
        final String superGroupId) {
      return new CheckoutGroupDescriptor(null, null).setSuperGroupId(superGroupId);
    }

    private CheckoutGroupDescriptor(final List<GroupDetail> allSilverpeasGroups,
        final GroupDetail[] distantRootGroups) {
      this.allSilverpeasGroups = allSilverpeasGroups;
      setDistantTestedGroups(distantRootGroups);
    }

    void addDistantTestedGroupsInAllSavedDistantGroups() {
      for (GroupDetail testedGroup : distantTestedGroups) {
        allSavedDistantGroups.put(testedGroup.getSpecificId(), testedGroup);
      }
    }

    public List<GroupDetail> getAllSilverpeasGroups() {
      return allSilverpeasGroups;
    }

    public GroupDetail[] getDistantTestedGroups() {
      return distantTestedGroups;
    }

    public CheckoutGroupDescriptor setDistantTestedGroups(final GroupDetail[] distantTestedGroups) {
      this.distantTestedGroups = distantTestedGroups;
      return this;
    }

    public Map<String, GroupDetail> getAllSavedDistantGroups() {
      return allSavedDistantGroups;
    }

    public String getSuperGroupId() {
      return superGroupId;
    }

    public CheckoutGroupDescriptor setSuperGroupId(final String superGroupId) {
      this.superGroupId = superGroupId;
      return this;
    }
  }

  /**
   * This manager is in charge of providing the role mapping between a space and a component. For
   * example, a 'manager' at space level can be mapped to 'user' at component child.
   */
  private static class AppRoleMappingManager {
    private final WAComponentRegistry componentRegistry;

    private AppRoleMappingManager(final WAComponentRegistry componentRegistry) {
      this.componentRegistry = componentRegistry;
    }

    /**
     * Gets the role in the component that is mapped with the specified space role. By convention,
     * whether there is no explicit mapping defined in the component descriptor, the role in the
     * component is mapped to the role in the space on their name (they have the same
     * name).
     * @param spaceRole the role at space level.
     * @param componentName the name of the aimed component.
     * @return the name of the role in the component that is mapped with the specified space role
     * name. Or null if no such mapping exists, even by taking into account the default mapping
     * rule.
     */
    @Nullable
    private String spaceRoleToComponentOne(final String spaceRole, final String componentName) {
      return componentRegistry.getWAComponent(componentName)
          .stream()
          .map(WAComponent::getProfiles)
          .flatMap(l -> l.stream()
              .filter(p -> p.getSpaceProfileMapping()
                  .getProfiles()
                  .stream()
                  .map(org.silverpeas.core.admin.component.model.SpaceProfile::getValue)
                  .anyMatch(s -> s.equals(spaceRole)))
              .map(Profile::getName))
          .findFirst()
          .orElse(null);
    }
  }
}
