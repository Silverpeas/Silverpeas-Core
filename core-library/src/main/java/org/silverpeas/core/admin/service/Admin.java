/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.admin.service;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.silverpeas.core.admin.ProfiledObjectId;
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
import org.silverpeas.core.admin.user.UserManager;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.dao.GroupSearchCriteriaForDAO;
import org.silverpeas.core.admin.user.dao.SearchCriteriaDAOFactory;
import org.silverpeas.core.admin.user.dao.UserSearchCriteriaForDAO;
import org.silverpeas.core.admin.user.model.*;
import org.silverpeas.core.backgroundprocess.AbstractBackgroundProcessRequest;
import org.silverpeas.core.backgroundprocess.BackgroundProcessTask;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManager;
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
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;
import static java.util.Collections.singletonList;
import static org.silverpeas.core.SilverpeasExceptionMessages.*;
import static org.silverpeas.core.admin.domain.DomainDriver.ActionConstants.ACTION_MASK_MIXED_GROUPS;
import static org.silverpeas.core.util.StringUtil.isLong;

/**
 * The class Admin is the main class of the Administrator.
 * <p>
 * The role of the administrator is to create and maintain user domains, spaces,
 * component instances, and the user/group roles on that spaces and component instances.
 * </p>
 */
@Singleton
@Transactional(rollbackOn = AdminException.class)
class Admin implements Administration {

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
  private boolean delUsersOnDiffSynchro = true;
  private boolean shouldFallbackGroupNames = true;
  private boolean shouldFallbackUserLogins = false;
  private String groupSynchroCron = "";
  private String domainSynchroCron = "";
  private String senderEmail = null;
  private String senderName = null;
  private SynchroGroupScheduler groupSynchroScheduler = null;
  private SynchroDomainScheduler domainSynchroScheduler = null;
  private SettingBundle roleMapping = null;
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
  private ContentManager contentManager;
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
    roleMapping = ResourceLocator.getSettingBundle("org.silverpeas.admin.roleMapping");
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

  protected Admin() {
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
          .collect(Collectors.toList());
      groupSynchroScheduler = new SynchroGroupScheduler();
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
    indexEntry.setCreationDate(spaceInst.getCreateDate());
    indexEntry.setLastModificationUser(String.valueOf(spaceInst.getUpdatedBy()));
    indexEntry.setLastModificationDate(spaceInst.getUpdateDate());
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
          // inherits profiles from super space
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
      ArrayList<ComponentInst> alCompoInst = spaceInst.getAllComponentsInst();
      for (ComponentInst componentInst : alCompoInst) {
        componentInst.setDomainFatherId(spaceInst.getId());
        addComponentInst(userId, componentInst);
      }

      SpaceInstLight space = getSpaceInstLight(spaceInst.getLocalId());
      addSpaceInTreeCache(space, true);

      // indexation de l'espace

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
        deleteSpaceProfiles(spaceInst);

        // notify logical deletion
        notifyOnSpaceLogicalDeletion(spaceId);
      } else {
        deleteEffectivelySpaceInst(spaceInst, spaceId, driverSpaceId, userId);
      }

      cache.opRemoveSpace(spaceInst);
      treeCache.removeSpace(driverSpaceId);
      // desindexation de l'espace
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
    for (String subSpaceid : subSpaceIds) {
      deleteSpaceInstById(userId, subSpaceid, true);
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
    ArrayList<ComponentInst> alCompoInst = spaceInst.getAllComponentsInst();
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

  private void deleteSpaceProfiles(SpaceInst spaceInst) throws AdminException {
    // delete the space profiles
    for (int nI = 0; nI < spaceInst.getNumSpaceProfileInst(); nI++) {
      deleteSpaceProfileInst(spaceInst.getSpaceProfileInst(nI).getId());
    }

    // delete the components profiles
    List<ComponentInst> components = spaceInst.getAllComponentsInst();
    for (ComponentInst component : components) {
      for (int p = 0; p < component.getNumProfileInst(); p++) {
        if (!component.getProfileInst(p).isInherited()) {
          deleteProfileInst(component.getProfileInst(p).getId());
        }
      }
    }

    // delete the subspace profiles
    List<SpaceInst> subSpaces = spaceInst.getSubSpaces();
    for (SpaceInst subSpace: subSpaces) {
      deleteSpaceProfiles(subSpace);
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

      // Get the space and put it in the cache
      SpaceInst spaceInst = getSpaceInstById(driverSpaceId);
      // set superspace profiles to space
      if (useProfileInheritance && !spaceInst.isInheritanceBlocked() && !spaceInst.isRoot()) {
        updateSpaceInheritance(spaceInst, false);
      }
      // indexation de l'espace
      createSpaceIndex(driverSpaceId);
      // reset space and eventually subspace
      cache.opAddSpace(spaceInst);
      addSpaceInTreeCache(getSpaceInstLight(driverSpaceId), true);
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
   *
   * @param spaceId client space id
   * @return Space information as SpaceInst object
   */
  private SpaceInst getSpaceInstById(int spaceId) throws AdminException {
    try {
      final SpaceInst spaceInst;
      Optional<SpaceInst> optionalSpaceInst = cache.getSpaceInst(spaceId);
      if (!optionalSpaceInst.isPresent()) {
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
        return treeCache.getSubSpaces(getDriverSpaceId(domainFatherId)).stream()
            .map(SpaceInstLight::getId).toArray(String[]::new);
      } else {
        return getAllSubSpaceIdsWithoutCache(domainFatherId);
      }
    } catch (Exception e) {
      throw new AdminException(failureOnGetting(SUBSPACES_OF_SPACE, domainFatherId), e);
    }
  }

  private String[] getAllSubSpaceIdsWithoutCache(String domainFatherId) throws AdminException {
    try {
      // get all sub space ids
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

      // indexation de l'espace

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
   *
   * @param space
   * @param inheritanceBlocked
   * @throws AdminException
   */
  private void updateSpaceInheritance(SpaceInst space,
      boolean inheritanceBlocked) throws AdminException {
    try {
      if (inheritanceBlocked) {
        // suppression des droits hérités de l'espace
        List<SpaceProfileInst> inheritedProfiles = space.getInheritedProfiles();
        for (SpaceProfileInst profile : inheritedProfiles) {
          deleteSpaceProfileInst(profile.getId());
        }
      } else {
        // Héritage des droits de l'espace
        // 1 - suppression des droits spécifiques du sous espace
        List<SpaceProfileInst> profiles = space.getProfiles();
        for (SpaceProfileInst profile : profiles) {
          if (profile != null && !profile.isManager()) {
            deleteSpaceProfileInst(profile.getId());
          }
        }
        if (!space.isRoot()) {
          // 2 - affectation des droits de l'espace au sous espace
          setSpaceProfilesToSubSpace(space, null, true, false);
        }
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
  public List<SpaceInstLight> getPathToSpace(String spaceId, boolean includeTarget) throws
      AdminException {
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
      return ArrayUtil.EMPTY_STRING_ARRAY;
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
      Optional<PersonalComponentInstance> personalComponentInstance =
          PersonalComponentInstance.from(componentInstanceIdentifier);
      if (personalComponentInstance.isPresent()) {
        instance = personalComponentInstance.get();
      } else {
        final SilverpeasComponentInstance componentInstance = getComponentInst(
            componentInstanceIdentifier);
        instance = "-1".equals(componentInstance.getId()) ? null : componentInstance;
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
      ComponentInst componentInst = getComponentInst(getDriverComponentId(sClientComponentId),
          null);
      componentInst = checkComponentInstanceById(componentInst, sClientComponentId,
          nullComponentInstSupplier);
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

  /**
   * Return the component Inst corresponding to the given ID.
   *
   * @param componentId
   * @param fatherDriverSpaceId
   * @return the component Inst corresponding to the given ID.
   * @throws AdminException
   */
  private ComponentInst getComponentInst(int componentId,
      Integer fatherDriverSpaceId) throws AdminException {
    try {
      // Get the component instance
      Optional<ComponentInst> optionalInstance = cache.getComponentInst(componentId);
      final ComponentInst componentInst;
      if (!optionalInstance.isPresent()) {
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
    List<Parameter> parameters = getComponentParameters(componentId);
    for (Parameter parameter : parameters) {
      if (parameter.getName().equalsIgnoreCase(parameterName)) {
        return parameter.getValue();
      }
    }
    return StringUtil.EMPTY;
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
      FullIndexEntry indexEntry = new FullIndexEntry(INDEX_COMPONENT_SCOPE, "Component", componentId);

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
    indexEntry.setCreationDate(componentInst.getCreateDate());
    indexEntry.setLastModificationUser(componentInst.getUpdaterUserId());
    indexEntry.setLastModificationDate(componentInst.getUpdateDate());
  }

  private void setIndexEntry(ComponentInstLight componentInstLight, FullIndexEntry indexEntry) {
    setIndexEntryTranslations(componentInstLight.getTranslations(), indexEntry);
    indexEntry.setCreationUser(Integer.toString(componentInstLight.getCreatedBy()));
    indexEntry.setCreationDate(componentInstLight.getCreateDate());
    indexEntry.setLastModificationUser(String.valueOf(componentInstLight.getUpdatedBy()));
    indexEntry.setLastModificationDate(componentInstLight.getUpdateDate());
  }

  private void setIndexEntryTranslations(Map<String, ComponentI18N> translations,
      FullIndexEntry indexEntry) {
    for (Map.Entry<String, ComponentI18N> translation : translations.entrySet()) {
      indexEntry.setTitle(translation.getValue().getName(), translation.getKey());
      indexEntry.setPreview(translation.getValue().getDescription(), translation.getKey());
    }
  }

  /**
   * Delete the index for the specified component.
   *
   * @param componentId
   */
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
        contentManager.registerNewContentInstance(connection, componentId, "containerPDC",
            componentName);
      }

      if (useProfileInheritance && !componentInst.isInheritanceBlocked()) {
        // inherits profiles from space
        setSpaceProfilesToComponent(componentInst, spaceInstFather);
      }

      cache.opAddComponent(componentInst);

      ComponentInstLight component = getComponentInstLight(componentId);
      treeCache.addComponent(component, getDriverSpaceId(spaceInstFather.getId()));

      // indexation du composant
      createComponentIndex(component);

      return componentId;
    } catch (QuotaException e) {
      throw e;
    } catch (Exception e) {
      throw new AdminException(failureOnAdding(COMPONENT, componentInst.getName()), e);
    }
  }

  boolean isContentManagedComponent(String componentName) {
    return "questionReply".equals(componentName)
        || "whitePages".equals(componentName) || "kmelia".equals(componentName)
        || "kmax".equals(componentName) || "survey".equals(componentName)
        || "toolbox".equals(componentName) || "quickinfo".equals(componentName)
        || "almanach".equals(componentName) || "quizz".equals(componentName) || "forums".equals(
            componentName) || "pollingStation".equals(componentName) || "bookmark".equals(
            componentName) || "chat".equals(componentName) || "infoLetter".equals(componentName)
        || "webSites".equals(componentName) || "gallery".equals(componentName) || "blog".equals(
            componentName);
  }

  /**
   * Deletes the given component instance in Silverpeas
   *
   * @param userId the unique identifier of the user requesting the deletion.
   * @param componentId the client identifier of the component instance (for a kmelia instance of id
   * 666, the client identifier of the instance is kmelia666)
   * @param definitive is the component instance deletion is definitive? If not, the component
   * instance is moved into the bin.
   * @throws AdminException if an error occurs while deleting the
   * component instance.
   */
  @Override
  public String deleteComponentInst(String userId, String componentId, boolean definitive)
      throws AdminException {
    try {
      // Convert the client id in driver id
      int sDriverComponentId = getDriverComponentId(componentId);

      // Get the component to delete
      ComponentInst componentInst = getComponentInst(sDriverComponentId, null);
      componentInst = checkComponentInstanceById(componentInst, componentId,
          nullComponentInstSupplier);
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
          // Uninstantiate the components
          String componentName = componentInst.getName();

          ComponentInstancePreDestruction.get(componentName)
              .ifPresent(c -> c.preDestroy(componentId));

          ServiceProvider.getAllServices(ComponentInstanceDeletion.class)
              .stream()
              .forEach(service -> service.delete(componentId));

          // delete the profiles instance
          for (int nI = 0; nI < componentInst.getNumProfileInst(); nI++) {
            deleteProfileInst(componentInst.getProfileInst(nI).getId());
          }

          if (isContentManagedComponent(componentName)) {
            // Call the unregister functions
            contentManager.unregisterNewContentInstance(connection, componentId, "containerPDC",
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
    } catch (SQLException|ContentManagerException e) {
      throw new AdminException(failureOnDeleting(COMPONENT, componentId), e);
    }
  }

  private void updateComponentOrderNum(String componentId, int orderNum) throws AdminException {
    try {
      int driverComponentId = getDriverComponentId(componentId);
      // Update the Component in tables
      componentManager.updateComponentOrder(driverComponentId, orderNum);
      Optional<ComponentInst> optional = cache.getComponentInst(driverComponentId);
      if (optional.isPresent()) {
        optional.get().setOrderNum(orderNum);
      }
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

      // indexation du composant
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
   *
   * @param component
   * @param inheritanceBlocked
   * @throws AdminException
   */
  private void updateComponentInheritance(ComponentInst component, boolean inheritanceBlocked)
      throws AdminException {
    try {
      if (inheritanceBlocked) {
        // suppression des droits hérités de l'espace
        List<ProfileInst> inheritedProfiles = component.getInheritedProfiles();
        for (ProfileInst profile : inheritedProfiles) {
          deleteProfileInst(profile.getId());
        }
      } else {
        // suppression des droits du composant
        List<ProfileInst> profiles = component.getProfiles();
        for (ProfileInst profile : profiles) {
          deleteProfileInst(profile.getId());
        }
        // affectation des droits de l'espace
        setSpaceProfilesToComponent(component, null);
      }
    } catch (AdminException e) {
      throw new AdminException(failureOnUpdate(COMPONENT, component.getId()), e);
    }
  }

  @Override
  public void setSpaceProfilesToSubSpace(final SpaceInst subSpace, final SpaceInst space)
  throws AdminException {
    setSpaceProfilesToSubSpace(subSpace, space, false, false);
  }

  @Override
  public void setSpaceProfilesToSubSpace(final SpaceInst subSpace, final SpaceInst space,
      boolean persist, boolean startNewTransaction)
      throws AdminException {
    SpaceInst currentSpace = space;
    if (currentSpace == null) {
      currentSpace = getSpaceInstById(subSpace.getDomainFatherId());
    }

    setSpaceProfileToSubSpace(subSpace, currentSpace, SilverpeasRole.admin);
    setSpaceProfileToSubSpace(subSpace, currentSpace, SilverpeasRole.publisher);
    setSpaceProfileToSubSpace(subSpace, currentSpace, SilverpeasRole.writer);
    setSpaceProfileToSubSpace(subSpace, currentSpace, SilverpeasRole.reader);

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
   * Set space profile to a subspace. There is no persistance. The subspace object is enriched.
   *
   * @param subSpace the object to set profiles
   * @param space the object to get profiles
   * @param role the name of the profile
   * @throws AdminException
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
        List<String> spaceRoles = componentRole2SpaceRoles(componentRole.getName(),
            component.getName());
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

  @NotNull
  private ProfileInst removeInheritedComponentRole(final ComponentInst component,
      final Profile componentRole) {
    ProfileInst inheritedProfile = component.getInheritedProfileInst(componentRole.getName());
    if (inheritedProfile != null) {
      inheritedProfile.removeAllGroups();
      inheritedProfile.removeAllUsers();
    } else {
      inheritedProfile = new ProfileInst();
      inheritedProfile.setComponentFatherId(component.getId());
      inheritedProfile.setInherited(true);
      inheritedProfile.setName(componentRole.getName());
    }
    return inheritedProfile;
  }

  private void checkObjectsProfiles(String componentId) {
    List<ProfileInst> objectsProfiles = getProfileInsts(componentId);
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

  @NotNull
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

  @NotNull
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
    spaceManager.updateSpaceOrder(shortSpaceId, getAllSubSpaceIdsWithoutCache(fatherId).length);

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
        setSpaceProfilesToSubSpace(space, father, true, false);
      } else {
        // space uses only local rights
        // let it as it is
      }
    }

    // Merge inherited and specific for each type of profile
    Map<String, SpaceProfileInst> mergedProfiles = new HashMap<>();
    List<SpaceProfileInst> allProfiles = new ArrayList<>();
    allProfiles.addAll(space.getProfiles());
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
      // Convert the client space Id in driver space Id
      int sDriverSpaceId = getDriverSpaceId(spaceId);

      ComponentInst componentInst = getComponentInst(componentId);
      String oldSpaceId = componentInst.getDomainFatherId();
      // Update the components in tables
      componentManager.moveComponentInst(sDriverSpaceId, sDriverComponentId);
      componentInst.setDomainFatherId(String.valueOf(sDriverSpaceId));

      // set space profiles to component if it not use its own rights
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
  public String getProfileLabelfromName(String sComponentName, String sProfileName, String lang) {
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
    if (!optionalProfile.isPresent()) {
      profileInst = profileManager.getProfileInst(sProfileId);
      cache.putProfileInst(profileInst);
    } else {
      profileInst = optionalProfile.get();
    }
    return profileInst;
  }

  @Override
  public List<ProfileInst> getProfilesByObject(ProfiledObjectId objectRef, String componentId) throws AdminException {
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
  public String[] getProfilesByObjectAndGroupId(final ProfiledObjectId objectRef, final String componentId, final String groupId) throws AdminException {
    return profiledObjectManager.getUserProfileNames(objectRef, getDriverComponentId(componentId),
        -1, Collections.singletonList(groupId));
  }

  @Override
  public Map<Integer, List<String>> getProfilesByObjectTypeAndUserId(String objectType,
      String componentId, String userId) throws AdminException {
    List<String> groups = getAllGroupsOfUser(userId);
    return profiledObjectManager.getUserProfileNames(objectType, getDriverComponentId(componentId),
        Integer.parseInt(userId), groups);
  }

  @Override
  public boolean isObjectAvailableToUser(String componentId, ProfiledObjectId objectRef,
      String userId) throws AdminException {
    return userId == null ||
        getProfilesByObjectAndUserId(objectRef, componentId, userId).length > 0;
  }

  @Override
  public boolean isObjectAvailableToGroup(String componentId, ProfiledObjectId objectRef, String groupId) throws AdminException {
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
  public String addProfileInst(ProfileInst profileInst, String userId)
      throws AdminException {
    try {
      final String componentFatherId = profileInst.getComponentFatherId();
      int driverFatherId = getDriverComponentId(componentFatherId);
      String sProfileId = profileManager.createProfileInst(profileInst, driverFatherId);
      profileInst.setId(sProfileId);

      if (profileInst.getObjectId().isNotDefined() || profileInst.getObjectId().isRootNode()) {
        ComponentInst componentInstFather = getComponentInst(driverFatherId, null);
        componentInstFather = checkComponentInstanceById(componentInstFather, componentFatherId,
            nullComponentInstSupplier);
        Objects.requireNonNull(componentInstFather);
        componentInstFather.addProfileInst(profileInst);
        if (StringUtil.isDefined(userId)) {
          componentInstFather.setUpdaterUserId(userId);
          updateComponentInst(componentInstFather);
        }
        cache.opAddProfile(profileManager.getProfileInst(sProfileId));
      }
      return sProfileId;
    } catch (Exception e) {
      throw new AdminException(failureOnAdding(PROFILE, profileInst.getName()), e);
    }
  }

  private String deleteProfileInst(String sProfileId) throws
      AdminException {
    return deleteProfileInst(sProfileId, null);
  }

  /**
   * Delete the given profile from Silverpeas
   *
   * @param profileId
   * @param userId
   * @return
   * @throws AdminException
   */
  @Override
  public String deleteProfileInst(String profileId, String userId)
      throws AdminException {
    ProfileInst profile = profileManager.getProfileInst(profileId);
    try {
      profileManager.deleteProfileInst(profile);
      if (StringUtil.isDefined(userId)
          && (profile.getObjectId().isNotDefined() || profile.getObjectId().isRootNode())) {
        final String componentFatherId = profile.getComponentFatherId();
        int driverFatherId = getDriverComponentId(componentFatherId);
        ComponentInst component = getComponentInst(driverFatherId, null);
        component = checkComponentInstanceById(component, componentFatherId,
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

  private String doUpdateProfileInst(ProfileInst newProfile, String userId)
      throws AdminException {
    try {
      profileManager.updateProfileInst(newProfile);
      if (StringUtil.isDefined(
          userId) && (newProfile.getObjectId().isNotDefined() || newProfile.getObjectId().isRootNode())) {
        final String componentFatherId = newProfile.getComponentFatherId();
        int driverFatherId = getDriverComponentId(componentFatherId);
        ComponentInst component = getComponentInst(driverFatherId, null);
        component = checkComponentInstanceById(component, componentFatherId,
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
    return spaceProfileManager.getSpaceProfileInst(spaceProfileId);
  }

  /**
   * Add the space profile instance from Silverpeas.
   *
   * @param spaceProfile
   * @param userId
   * @return
   * @throws AdminException
   */
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

  private String deleteSpaceProfileInst(String sSpaceProfileId) throws
      AdminException {
    return deleteSpaceProfileInst(sSpaceProfileId, null);
  }

  /**
   * Delete the given space profile from Silverpeas
   */
  @Override
  public String deleteSpaceProfileInst(String sSpaceProfileId, String userId)
      throws AdminException {
    SpaceProfileInst spaceProfileInst = spaceProfileManager.getSpaceProfileInst(sSpaceProfileId);
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
  private String updateSpaceProfileInst(SpaceProfileInst newSpaceProfile) throws AdminException {
    return updateSpaceProfileInst(newSpaceProfile, null);
  }

  @Override
  public String updateSpaceProfileInst(SpaceProfileInst newSpaceProfile, String userId)
      throws AdminException {
    try {
      SpaceProfileInst oldSpaceProfile = spaceProfileManager.getSpaceProfileInst(
          newSpaceProfile.getId());
      if (oldSpaceProfile == null) {
        return null;
      }
      String spaceProfileNewId = spaceProfileManager.updateSpaceProfileInst(oldSpaceProfile,
          newSpaceProfile);

      // profile 'Manager' does not need to be spread
      if (!oldSpaceProfile.isManager()) {
        int spaceId = getDriverSpaceId(newSpaceProfile.getSpaceFatherId());
        if (StringUtil.isDefined(userId)) {
          SpaceInst spaceInstFather = getSpaceInstById(spaceId);
          spaceInstFather.setUpdaterUserId(userId);
          updateSpaceInst(spaceInstFather);
        }
        // Add inherited users and groups for this role
        List<SpaceProfileInst> allProfileSources = new ArrayList<>();
        allProfileSources.add(newSpaceProfile);
        if (newSpaceProfile.isInherited()) {
          allProfileSources.add(spaceProfileManager.getSpaceProfileInstByName(spaceId,
              oldSpaceProfile.getName()));
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
      cache.opUpdateSpaceProfile(spaceProfileManager.getSpaceProfileInst(newSpaceProfile.getId()));

      return spaceProfileNewId;
    } catch (Exception e) {
      throw new AdminException(failureOnUpdate(SPACE_PROFILE, newSpaceProfile.getId()), e);
    }
  }

  private String spaceRole2ComponentRole(String spaceRole, String componentName) {
    return roleMapping.getString(componentName + "_" + spaceRole, null);
  }

  private List<String> componentRole2SpaceRoles(String componentRole,
      String componentName) {
    List<String> roles = new ArrayList<>();

    String role = spaceRole2ComponentRole(SilverpeasRole.admin.toString(), componentName);
    if (role != null && role.equalsIgnoreCase(componentRole)) {
      roles.add(SilverpeasRole.admin.toString());
    }
    role = spaceRole2ComponentRole(SilverpeasRole.publisher.toString(), componentName);
    if (role != null && role.equalsIgnoreCase(componentRole)) {
      roles.add(SilverpeasRole.publisher.toString());
    }
    role = spaceRole2ComponentRole(SilverpeasRole.writer.toString(), componentName);
    if (role != null && role.equalsIgnoreCase(componentRole)) {
      roles.add(SilverpeasRole.writer.toString());
    }
    role = spaceRole2ComponentRole(SilverpeasRole.reader.toString(), componentName);
    if (role != null && role.equalsIgnoreCase(componentRole)) {
      roles.add(SilverpeasRole.reader.toString());
    }
    return roles;
  }

  private void spreadSpaceProfile(int spaceId, SpaceProfileInst spaceProfile) throws AdminException {
    // update profile in components
    List<ComponentInstLight> components = treeCache.getComponents(spaceId);
    updateProfilesInComponents(spaceId, spaceProfile, components);

    // update profile in subspaces
    List<SpaceInstLight> subSpaces = treeCache.getSubSpaces(spaceId);
    updateProfilesInSubspaces(spaceProfile, subSpaces);
  }

  private void updateProfilesInSubspaces(final SpaceProfileInst spaceProfile,
      final List<SpaceInstLight> subSpaces) throws AdminException {
    for (SpaceInstLight subSpace : subSpaces) {
      if (!subSpace.isInheritanceBlocked()) {
        SpaceProfileInst subSpaceProfile = spaceProfileManager
            .getInheritedSpaceProfileInstByName(subSpace.getLocalId(), spaceProfile.getName());
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
          if (!subSpaceProfile.getAllGroups().isEmpty() || !subSpaceProfile.getAllUsers().isEmpty()) {
            addSpaceProfileInst(subSpaceProfile, null);
          }
        }
      }
    }
  }

  private void updateProfilesInComponents(final int spaceId, final SpaceProfileInst spaceProfile,
      final List<ComponentInstLight> components) throws AdminException {
    for (ComponentInstLight component : components) {
      if (component != null && !component.isInheritanceBlocked()) {
        String componentRole = spaceRole2ComponentRole(spaceProfile.getName(),
            component.getName());
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
    inheritedProfile.setComponentFatherId(component.getId());
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

    List<String> profilesToCheck = componentRole2SpaceRoles(componentRole,
        component.getName());
    profilesToCheck.remove(spaceProfile.getName()); // exclude current space profile
    for (String profileToCheck : profilesToCheck) {
      SpaceProfileInst spi =
          spaceProfileManager.getSpaceProfileInstByName(spaceId, profileToCheck);
      if (spi != null) {
        inheritedProfile.addGroups(spi.getAllGroups());
        inheritedProfile.addUsers(spi.getAllUsers());
      }
    }
    updateProfileInst(inheritedProfile);
  }

  // -------------------------------------------------------------------------
  // GROUP RELATED FUNCTIONS
  // -------------------------------------------------------------------------
  @Override
  public String[] getGroupNames(String[] groupIds) throws AdminException {
    if (groupIds == null) {
      return ArrayUtil.EMPTY_STRING_ARRAY;
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
    try {
      return addGroup(group, false);
    } catch (Exception e) {
      throw new AdminException(failureOnAdding(GROUP, group.getName()), e);
    }
  }

  @Override
  public String addGroup(GroupDetail group, boolean onlyInSilverpeas) throws AdminException {
    try {
      String sGroupId = groupManager.addGroup(group, onlyInSilverpeas);
      group.setId(sGroupId);
      if (group.isSynchronized()) {
        groupSynchroScheduler.addGroup(sGroupId);
      }
      cache.opAddGroup(group);
      return sGroupId;
    } catch (Exception e) {
      throw new AdminException(failureOnAdding(GROUP, group.getName()), e);
    }
  }

  @Override
  public String deleteGroupById(String sGroupId) throws AdminException {
    try {
      return deleteGroupById(sGroupId, false);
    } catch (Exception e) {
      throw new AdminException(failureOnDeleting(GROUP, sGroupId), e);
    }
  }

  @Override
  public String deleteGroupById(String sGroupId, boolean onlyInSilverpeas) throws AdminException {
    // Get group information
    GroupDetail  group = getGroup(sGroupId);
    if (group == null) {
      throw new AdminException(unknown(GROUP, sGroupId));
    }
    try {

      // Delete group profiles
      deleteGroupProfileInst(sGroupId);

      // Listing the group and its sub groups before the recursive deletion
      final List<GroupDetail> groupAndSubGroups = new ArrayList<>();
      groupAndSubGroups.add(group);
      Collections.addAll(groupAndSubGroups, getRecursivelyAllSubGroups(sGroupId));

      // Delete group itself
      String sReturnGroupId = groupManager.deleteGroup(group, onlyInSilverpeas);

      // Removing the deleted groups from caches
      groupAndSubGroups.forEach(g -> {
        if (g.isSynchronized()) {
          groupSynchroScheduler.removeGroup(g.getId());
        }
        cache.opRemoveGroup(g);
      });
      return sReturnGroupId;
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
      String sGroupId = groupManager.updateGroup(group, onlyInSilverpeas);
      cache.resetOnUpdateGroup();
      return sGroupId;
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
    return groupProfileManager.getGroupProfileInst(null, groupId);
  }

  @Override
  public String addGroupProfileInst(GroupProfileInst groupProfileInst)
      throws AdminException {
    try {
      // Create the space profile instance
      GroupDetail group = getGroup(groupProfileInst.getGroupId());
      String sProfileId = groupProfileManager.createGroupProfileInst(
          groupProfileInst, group.getId());
      groupProfileInst.setId(sProfileId);
      return sProfileId;
    } catch (Exception e) {
      throw new AdminException(failureOnAdding(GROUP_PROFILE, groupProfileInst.getName()), e);
    }
  }

  @Override
  public String deleteGroupProfileInst(String groupId) throws AdminException {
    // Get the SpaceProfile to delete
    GroupProfileInst groupProfileInst = groupProfileManager.getGroupProfileInst(null, groupId);
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
  public String updateGroupProfileInst(GroupProfileInst groupProfileInstNew)
      throws AdminException {
    String sSpaceProfileNewId = groupProfileInstNew.getId();
    if (!StringUtil.isDefined(sSpaceProfileNewId)) {
      sSpaceProfileNewId = addGroupProfileInst(groupProfileInstNew);
    } else {
      try {
        GroupProfileInst oldSpaceProfile = groupProfileManager.getGroupProfileInst(null,
            groupProfileInstNew.getGroupId());
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
    if (!StringUtil.isDefined(sUserId) || "-1".equals(sUserId)) {
      return null;
    }

    final UserDetail ud;
    Optional<UserDetail> optionalUser = cache.getUserDetail(sUserId);
    if (!optionalUser.isPresent()) {
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

  /**
   * Deactivates the user represented by the given identifier.
   *
   * @param userId
   * @throws AdminException
   */
  @Override
  public void deactivateUser(String userId) throws AdminException {
    updateUserState(userId, UserState.DEACTIVATED);
  }

  /**
   * Activate the user represented by the given identifier.
   *
   * @param userId
   * @throws AdminException
   */
  @Override
  public void activateUser(String userId) throws AdminException {
    updateUserState(userId, UserState.VALID);
  }

  /**
   * Updates the user state from a user id.
   *
   * @param userId
   * @param state
   * @throws AdminException
   */
  private void updateUserState(String userId, UserState state) throws AdminException {
    try {
      UserDetail user = UserDetail.getById(userId);
      user.setState(state);
      user.setStateSaveDate(new Date());
      updateUser(user);
    } catch (Exception e) {
      throw new AdminException(
          failureOnUpdate("state of user " + userId, "to " + state.getName()), e);
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
      SilverLogger.getLogger(this).warn(
          "Attempt to remove the main administrator account by user " +
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
      if (!optionalDomain.isPresent()) {
        domain = domainDriverManager.getDomain(domainId);
        domainCache.addDomain(domain);
      } else {
        domain = optionalDomain.get();
      }
      return domain;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting(DOMAIN, domainId), e);
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
      throw new AdminException(failureOnGetting("root groups of domain", domainId),e);
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
        return ArrayUtil.EMPTY_STRING_ARRAY;
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
  public String identify(String sKey, String sSessionId, boolean isAppInMaintenance, boolean removeKey) throws AdminException {
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
      // Get the user Id or import it if the domain accept it
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

    // getting all components availables
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
    if (!optionalGroups.isPresent()) {
      // group ids of user is not yet processed
      // process it and store it in cache
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

    return componentManager.getAllowedComponentIds(Integer.parseInt(userId), allGroupsOfUser,
        null, componentName);
  }

  @Override
  public String[] getUserRootSpaceIds(String sUserId) throws AdminException {
    try {
      // getting all components availables
      final List<String> componentIds = getAllowedComponentIds(sUserId);
      return getRootSpaceIds(componentIds);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("root spaces accessible by user", sUserId), e);
    }
  }

  private String[] getRootSpaceIds(List<String> componentIds) throws AdminException {
    final List<String> result = new ArrayList<>();
    // getting all root spaces (sorted)
    final String[] rootSpaceIds = getAllRootSpaceIds();
    // retain only allowed root spaces
    for (String rootSpaceId : rootSpaceIds) {
      if (isSpaceContainsOneComponent(componentIds, getDriverSpaceId(rootSpaceId), true)) {
        result.add(rootSpaceId);
      }
    }
    return result.toArray(new String[0]);
  }

  @Override
  public String[] getUserSubSpaceIds(String sUserId, String spaceId) throws AdminException {
    try {
      // getting all components availables
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
      subspaces = subspaces.filter(s -> isSpaceContainsOneComponent(componentIds, s.getLocalId(), true));
    }
    return subspaces.map(SpaceInstLight::getId).toArray(String[]::new);
  }

  @Override
  public boolean isSpaceAvailable(String userId, String spaceId) throws AdminException {
    List<String> componentIds = getAllowedComponentIds(userId);
    return isSpaceAvailable(componentIds, spaceId);
  }

  private boolean isSpaceAvailable(List<String> componentIds, String spaceId) {
    return isSpaceContainsOneComponent(componentIds, getDriverSpaceId(spaceId), true);
  }

  private boolean isSpaceContainsOneComponent(List<String> componentIds, int spaceId,
      boolean checkInSubspaces) {
    boolean find = false;

    List<ComponentInstLight> components = new ArrayList<>(treeCache.getComponents(spaceId));

    // Is there at least one component available ?
    for (int c = 0; !find && c < components.size(); c++) {
      find = componentIds.contains(components.get(c).getId());
    }
    if (find) {
      return true;
    } else {
      if (checkInSubspaces) {
        // check in subspaces
        List<SpaceInstLight> subspaces = new ArrayList<>(treeCache.getSubSpaces(spaceId));
        for (int s = 0; !find && s < subspaces.size(); s++) {
          find = isSpaceContainsOneComponent(componentIds, subspaces.get(s).getLocalId(),
              checkInSubspaces);
        }
      }
    }

    return find;
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

      List<ComponentInstLight> allComponents = treeCache.getComponentsInSpaceAndSubspaces(
          getDriverSpaceId(spaceId));
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

    Set<String> componentsId = new HashSet<>(Arrays.asList(getAvailCompoIds(userId)));
    Set<Integer> authorizedIds = new HashSet<>(100);
    if (!componentsId.isEmpty()) {
      String componentId = componentsId.iterator().next();
      componentsId.remove(componentId);
      filterSpaceFromComponents(authorizedIds, componentsId, componentId);
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
  private void addAuthorizedSpace(Set<Integer> spaces, Set<String> componentsId, SpaceInstLight space) {
    if (!SpaceInst.STATUS_REMOVED.equals(space.getStatus()) &&
        !spaces.contains(space.getLocalId())) {
      int spaceId = space.getLocalId();
      spaces.add(spaceId);
      componentsId.removeAll(treeCache.getComponentIds(spaceId));
      if (!space.isRoot()) {
        int fatherId = getDriverSpaceId(space.getFatherId());
        if (!spaces.contains(fatherId)) {
          Optional<SpaceInstLight> parent = treeCache.getSpaceInstLight(fatherId);
          parent.ifPresent(p -> addAuthorizedSpace(spaces, componentsId, p));
        }
      }
    }
  }

  private void filterSpaceFromComponents(Set<Integer> spaces, Set<String> componentsId, String componentId) {
    Optional<SpaceInstLight> space = treeCache.getSpaceContainingComponent(componentId);
    space.ifPresent(s -> addAuthorizedSpace(spaces, componentsId, s));
    if (!componentsId.isEmpty()) {
      String newComponentId = componentsId.iterator().next();
      componentsId.remove(newComponentId);
      filterSpaceFromComponents(spaces, componentsId, newComponentId);
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

  private SpaceWithSubSpacesAndComponents getFullTreeview(final List<String> componentIds,
      final boolean ignoreEmptySpaces)
      throws AdminException {
    final String[] spaceIds = getRootSpaceIds(componentIds);
    final SpaceWithSubSpacesAndComponents root = new SpaceWithSubSpacesAndComponents(new SpaceInstLight());
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
  public List<UserDetail> getRemovedUsers(final String... domainIds) throws AdminException {
    return userManager.getRemovedUsersOfDomains(domainIds);
  }

  @Override
  public List<UserDetail> getNonBlankedDeletedUsers(final String... domainIds) throws AdminException {
    return userManager.getNonBlankedDeletedUsersOfDomains(domainIds);
  }

  @Override
  public void blankDeletedUsers(final String targetDomainId, final List<String> userIds)
      throws AdminException {
    final UserDetail[] users = getUserDetails(userIds.toArray(new String[0]));
    for (final UserDetail user : users) {
      if (user.getDomainId().equals(targetDomainId)) {
        userManager.blankUser(user);
        cache.opUpdateUser(userManager.getUserDetail(user.getId()));
      }
    }
  }

  private SpaceWithSubSpacesAndComponents geTreeview(List<String> componentIds, String spaceId,
      final boolean ignoreEmptySpaces)
      throws AdminException {
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
    final List<ComponentInstLight> spaceComponents = treeCache.getComponents(getDriverSpaceId(spaceId))
        .stream()
        .filter(c -> componentIds.contains(c.getId()))
        .collect(Collectors.toList());
    space.setComponents(spaceComponents);
    return space;
  }

  @Override
  public String[] getAllowedSubSpaceIds(String userId, String spaceFatherId) throws AdminException {
    return getUserSubSpaceIds(userId, spaceFatherId);
  }

  private SpaceInstLight getSpaceInstLight(int spaceId) throws AdminException {
    return getSpaceInstLight(spaceId, -1);
  }

  private SpaceInstLight getSpaceInstLight(int spaceId, int level) throws AdminException {
    Optional<SpaceInstLight> optionalSpace = treeCache.getSpaceInstLight(spaceId);
    final SpaceInstLight sil = optionalSpace.isPresent() ? optionalSpace.get() :
        spaceManager.getSpaceInstLightById(spaceId);
    if (sil != null) {
      if (level != -1) {
        sil.setLevel(level);
      }
      if (sil.getLevel() == -1) {
        sil.setLevel(treeCache.getSpaceLevel(spaceId));
      }
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
  public String[] getGroupManageableSpaceIds(String sGroupId)
      throws AdminException {
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

        // calculate manageable space's childs
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
    final Integer[] result;
    ArrayList<String> alManageableSpaceIds = new ArrayList<>();
    ArrayList<Integer> alDriverManageableSpaceIds = new ArrayList<>();
    try {
      // Get user manageable space ids from cache
      Optional<Integer[]> optionalSpaceIds = cache.getManageableSpaceIds(sUserId);
      if (!optionalSpaceIds.isPresent()) {
        // Get user manageable space ids from database

        List<String> groupIds = getAllGroupsOfUser(sUserId);
        final Integer[] manageableSpaceIds = userManager.getManageableSpaceIds(sUserId, groupIds);

        // Inherits manageability rights for space children
        String[] childSpaceIds;
        for (Integer asManageableSpaceId : manageableSpaceIds) {
          // add manageable space id in result
          String asManageableSpaceIdAsString = String.valueOf(asManageableSpaceId);
          if (!alManageableSpaceIds.contains(asManageableSpaceIdAsString)) {
            alManageableSpaceIds.add(asManageableSpaceIdAsString);
            alDriverManageableSpaceIds.add(asManageableSpaceId);
          }

          // calculate manageable space's childs
          childSpaceIds = spaceManager.getAllSubSpaceIds(asManageableSpaceId);

          // add them in result
          for (String childSpaceId : childSpaceIds) {
            if (!alManageableSpaceIds.contains(childSpaceId)) {
              alManageableSpaceIds.add(childSpaceId);
              alDriverManageableSpaceIds.add(getDriverSpaceId(childSpaceId));
            }
          }
        }

        // Put user manageable space ids in cache
        result = alDriverManageableSpaceIds.toArray(new Integer[0]);
        cache.putManageableSpaceIds(sUserId, result);
      } else {
        result = optionalSpaceIds.get();
      }
      return Arrays.stream(result).map(String::valueOf).toArray(String[]::new);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("spaces manageable by user", sUserId), e);
    }
  }

  @Override
  public String[] getUserManageableSpaceRootIds(String sUserId)
      throws AdminException {
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
      throw new AdminException(
          failureOnGetting(SUBSPACES_OF_SPACE + sParentSpaceId,
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

    if (role == SilverpeasRole.Manager) {
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
  public String[] getAvailCompoIds(String sClientSpaceId, String sUserId)
      throws AdminException {
    final String[] asAvailCompoIds;

    try {
      // Converts client space id to driver space id
      int spaceId = getDriverSpaceId(sClientSpaceId);

      // Get available component ids from cache
      Optional<String[]> optionalInstanceIds = cache.getAvailCompoIds(spaceId, sUserId);

      if (!optionalInstanceIds.isPresent()) {
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
          failureOnGetting(COMPONENTS_IN_SPACE + sClientSpaceId,
              AVAILABLE_TO_USER + sUserId), e);
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
      List<String> asAvailCompoIds = componentManager.getAllowedComponentIds(Integer.parseInt(
          sUserId), groupIds, spaceId);

      return asAvailCompoIds.toArray(new String[0]);
    } catch (Exception e) {
      throw new AdminException(
          failureOnGetting("root components in space " + sClientSpaceId,
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
        if (allowedComponentIds.contains(component.getId()) && component.getName().startsWith(
            componentNameRoot)) {
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
      String componentName)
      throws AdminException {
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
        // Set the component Id
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
          failureOnGetting("instances of component " + sComponentName,
              AVAILABLE_TO_USER + sUserId), e);
    }
  }

  @Override
  public String[] getCompoId(String sComponentName) throws AdminException {
    try {
      // Build the list of instanciated components with given componentName
      String[] matchingComponentIds =
          componentManager.getAllCompoIdsByComponentName(sComponentName);

      // check TreeCache to know if component is not removed neither into a removed space
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
        // check TreeCache to know if component is not removed neither into a removed space
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
      if (!optionalProfileIds.isPresent()) {
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
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  @Override
  public String[] getCurrentProfiles(String sUserId, String componentId)
      throws AdminException {
    return profileManager.getProfileNamesOfUser(sUserId, getAllGroupsOfUser(sUserId),
        getDriverComponentId(componentId));
  }

  @Override
  public UserDetail[] getUsers(boolean bAllProfiles, String sProfile, String sClientSpaceId,
      String sClientComponentId) throws AdminException {
    ArrayList<String> alUserIds = new ArrayList<>();

    try {
      ComponentInst componentInst = getComponentInst(
          getDriverComponentId(sClientComponentId), getDriverSpaceId(sClientSpaceId));
      componentInst = checkComponentInstanceById(componentInst, sClientComponentId,
          nullComponentInstSupplier);
      Objects.requireNonNull(componentInst);

      for (ProfileInst profile : componentInst.getAllProfilesInst()) {
        if (profile != null && (profile.getName().equals(sProfile) || bAllProfiles)) {
          // add direct users
          alUserIds.addAll(profile.getAllUsers());
          // add users of groups
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
      throw new AdminException(
          failureOnGetting("users with profile " + sProfile,
              "of the component " + sClientComponentId), e);
    }
  }

  private void addUsersOfAllGroups(final ArrayList<String> alUserIds, final ProfileInst profile)
      throws AdminException {
    List<String> groupIds = profile.getAllGroups();
    for (String groupId : groupIds) {
      List<String> subGroupIds = groupManager.getAllSubGroupIdsRecursively(groupId);
      // add current group
      subGroupIds.add(groupId);
      if (!subGroupIds.isEmpty()) {
        UserDetail[] users = userManager.getAllUsersInGroups(subGroupIds);
        for (UserDetail user : users) {
          alUserIds.add(user.getId());
        }
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
  public UserDetail[] getFiltredDirectUsers(String sGroupId, String sUserLastNameFilter)
      throws AdminException {
    GroupDetail theGroup = getGroup(sGroupId);

    if (theGroup == null) {
      return new UserDetail[0];
    }
    String[] usersIds = theGroup.getUserIds();
    if (usersIds == null || usersIds.length <= 0) {
      return new UserDetail[0];
    }
    if (sUserLastNameFilter == null || sUserLastNameFilter.length() <= 0) {
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
      return ArrayUtil.EMPTY_STRING_ARRAY;
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
  public String[] getAllSubSpaceIds(String sSpaceId, String sUserId)
      throws AdminException {
    return getUserSubSpaceIds(sUserId, sSpaceId);
  }

  @Override
  public String[] getAllComponentIds(String sSpaceId) throws AdminException {
    List<String> alCompoIds = new ArrayList<>();

    // Get the compo of this space
    SpaceInst spaceInst = getSpaceInstById(sSpaceId);
    List<SilverpeasComponentInstance> alCompoInst = spaceInst.getAllComponentInstances();

    if (alCompoInst != null) {
      for (SilverpeasComponentInstance anAlCompoInst : alCompoInst) {
        alCompoIds.add(anAlCompoInst.getId());
      }
    }

    return alCompoIds.toArray(new String[0]);
  }

  @Override
  public String[] getAllComponentIdsRecur(String sSpaceId) throws AdminException {
    List<ComponentInstLight> components = treeCache.getComponentsInSpaceAndSubspaces(
        getDriverSpaceId(sSpaceId));

    List<String> componentIds = new ArrayList<>();
    for (ComponentInstLight component : components) {
      componentIds.add(component.getId());
    }

    final SpaceInst space = getSpaceInstById(sSpaceId);
    if (space.isPersonalSpace()) {
      PersonalComponent.getAll().forEach(
          p -> componentIds.add(PersonalComponentInstance.from(space.getCreator(), p).getId()));
    }

    return componentIds.toArray(new String[0]);
  }

  @Override
  public String[] getAllComponentIdsRecur(String sSpaceId, String sUserId, String componentNameRoot,
      boolean inCurrentSpace, boolean inAllSpaces) throws AdminException {
    ArrayList<String> alCompoIds = new ArrayList<>();
    // In All silverpeas
    if (inAllSpaces) {
      CompoSpace[] cs = getCompoForUser(sUserId, componentNameRoot);
      for (CompoSpace c : cs) {
        alCompoIds.add(c.getComponentId());
      }
    } else {
      alCompoIds = getAllComponentIdsRecur(sSpaceId, sUserId,
          componentNameRoot, inCurrentSpace);
    }
    return arrayListToString(alCompoIds);
  }

  private ArrayList<String> getAllComponentIdsRecur(String sSpaceId, String sUserId,
      String componentNameRoot, boolean inCurrentSpace) throws AdminException {
    ArrayList<String> alCompoIds = new ArrayList<>();
    getComponentIdsByNameAndUserId(sUserId, componentNameRoot);
    // Get components in the root of the space
    if (inCurrentSpace) {
      String[] componentIds = getAvailCompoIdsAtRoot(sSpaceId, sUserId);
      addComponentIdsMatchingName(componentNameRoot, componentIds, alCompoIds);
    }

    // Get components in sub spaces
    String[] asSubSpaceIds = getAllSubSpaceIds(sSpaceId);
    for (int nI = 0; asSubSpaceIds != null && nI < asSubSpaceIds.length; nI++) {

      SpaceInst spaceInst = getSpaceInstById(asSubSpaceIds[nI]);
      String[] componentIds = getAvailCompoIds(spaceInst.getId(), sUserId);

      addComponentIdsMatchingName(componentNameRoot, componentIds, alCompoIds);
    }
    return alCompoIds;
  }

  private void addComponentIdsMatchingName(final String componentNameRoot, final String[] componentIds,
      final ArrayList<String> alCompoIds) throws AdminException {
    if (componentIds != null) {
      for (String componentId : componentIds) {
        ComponentInstLight compo = getComponentInstLight(componentId);
        if (compo.getName().equals(componentNameRoot)) {
          alCompoIds.add(compo.getId());
        }
      }
    }
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
              () -> "Ajout de l'utilisateur d'ID " +
                  newUsers.stream().collect(Collectors.joining(", ")) + " dans le groupe d'ID " +
                  groupId);
          groupManager.addUsersInGroup(newUsers, groupId);
        }

        // Remove users
        List<String> removedUsers = new ArrayList<>();
        synchroAddUsersToRemove(actualUserIds, userIds, removedUsers);
        SynchroGroupReport.warn(ADMIN_SYNCHRONIZE_GROUP, REMOVE_OF + removedUsers.size()
            + " utilisateur(s)");
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
        SynchroGroupReport
            .info(ADMIN_SYNCHRONIZE_GROUP, "Suppression de l'utilisateur " + actualUserId);
      }
    }
  }

  private void synchroAddUsersToAdd(final List<String> actualUserIds, final List<String> userIds,
      final List<String> newUsers) {
    for (String userId : userIds) {
      if (!actualUserIds.contains(userId)) {
        newUsers.add(userId);
        SynchroGroupReport
            .info(ADMIN_SYNCHRONIZE_GROUP, "Ajout de l'utilisateur " + userId);
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
        SilverLogger.getLogger(this).warn("Group {0} not found. Synchronize it",
            groupSpecificId);
        if (recursGroups) {
          try {
            groupId = synchronizeImportGroup(sDomainId, groupSpecificId,
                null, true, true);
          } catch (AdminException ex) {
            // The group's synchro failed -> ignore him
            SilverLogger.getLogger(this).error(ex);
            groupId = null;
          }
        }
      }

      if (groupId != null) {
        convertedGroupIds.add(groupId);
      }
    }
    return convertedGroupIds;
  }

  private String[] translateUserIds(String sDomainId, String[] userSpecificIds) {
    List<String> convertedUserIds = new ArrayList<>();
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
        convertedUserIds.add(userId);
      }
    }
    return convertedUserIds.toArray(new String[0]);
  }

  @Override
  public String synchronizeGroup(String groupId, boolean recurs) throws AdminException {

    GroupDetail theGroup = getGroup(groupId);
    if (theGroup.isSynchronized()) {
      synchronizeGroupByRule(groupId, false);
    } else {
      DomainDriver synchroDomain = domainDriverManager.getDomainDriver(theGroup.getDomainId());
      GroupDetail gr = synchroDomain.synchroGroup(theGroup.getSpecificId());

      gr.setId(groupId);
      gr.setDomainId(theGroup.getDomainId());
      gr.setSuperGroupId(theGroup.getSuperGroupId());
      internalSynchronizeGroup(synchroDomain, gr, recurs);
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
        if (askedParentId != null && !askedParentId.isEmpty() && !askedParentId.equals(
            parentId)) {
          // It is not the matching parent
          parentId = null;
        }
      } catch (AdminException e) {
        // The user doesn't exist -> Synchronize him
        parentId = null;
      }
    }
    if (parentId == null && (parentSpecificIds.length > 0 || (askedParentId != null
        && askedParentId.length() > 0))) {// We
      // can't
      // add
      // the
      // group
      // (just
      // the
      // same
      // restriction as for the directories...)
      throw new AdminException("Fail to synchronize imported group " + groupKey
          + " in domain " + domainId);
    }
    // The group is a root group or have a known parent
    gr.setSuperGroupId(parentId);

    // We must first add the group with no child. Then, the childs will be added
    // during the internal synchronization function call
    String[] specificIds = gr.getUserIds();
    gr.setUserIds(ArrayUtil.EMPTY_STRING_ARRAY);
    String groupId = addGroup(gr, true);
    gr.setId(groupId);
    gr.setUserIds(specificIds);
    internalSynchronizeGroup(synchroDomain, gr, recurs);
    return groupId;
  }

  @Override
  public String synchronizeRemoveGroup(String groupId) throws AdminException {
    GroupDetail theGroup = getGroup(groupId);
    DomainDriver synchroDomain = domainDriverManager.getDomainDriver(theGroup.getDomainId());
    synchroDomain.removeGroup(theGroup.getSpecificId());
    return deleteGroupById(groupId, true);
  }

  protected void internalSynchronizeGroup(DomainDriver synchroDomain,
      GroupDetail latestGroup, boolean recurs) throws AdminException {
    latestGroup.setUserIds(translateUserIds(latestGroup.getDomainId(),
        latestGroup.getUserIds()));
    updateGroup(latestGroup, true);
    if (recurs) {
      GroupDetail[] childs = synchroDomain.getGroups(latestGroup.getSpecificId());

      for (final GroupDetail child : childs) {
        String existingGroupId = null;
        try {
          existingGroupId = groupManager
              .getGroupIdBySpecificIdAndDomainId(child.getSpecificId(), latestGroup.getDomainId());
          GroupDetail existingGroup = getGroup(existingGroupId);
          if (existingGroup.getSuperGroupId().equals(latestGroup.getId())) {
            // Only synchronize the group if latestGroup is his true parent
            synchronizeGroup(existingGroupId, recurs);
          }
        } catch (AdminException e) {
          // The group doesn't exist -> Import him
          if (existingGroupId == null) { // Import the new group
            synchronizeImportGroup(latestGroup.getDomainId(), child.getSpecificId(),
                latestGroup.getId(), recurs, true);
          }
        }
      }
    }
  }

  @Override
  public String synchronizeUser(String userId, boolean recurs) throws AdminException {
    Collection<UserDetail> listUsersUpdate = new ArrayList<>();
    try {
      UserDetail theUserDetail = getUserDetail(userId);
      DomainDriver synchroDomain = domainDriverManager.getDomainDriver(theUserDetail.getDomainId());
      // Synchronize the user's infos
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
      String[] incGroupsSpecificId = synchroDomain.getUserMemberGroupIds(
          theUserDetail.getSpecificId());
      List<String> incGroupsId = translateGroupIds(theUserDetail.getDomainId(),
          incGroupsSpecificId, recurs);
      List<GroupDetail> oldGroups = groupManager.getDirectGroupsOfUser(userId);
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
    // Synchronizes the user to add it to the groups and recursivaly add the groups
    synchronizeUser(userId, recurs);
    return userId;
  }

  @Override
  public String synchronizeImportUser(String domainId, String specificId, boolean recurs) throws
      AdminException {
    DomainDriver synchroDomain = domainDriverManager.getDomainDriver(domainId);
    UserDetail ud = synchroDomain.getUser(specificId);

    ud.setDomainId(domainId);
    String userId = addUser(ud, true);
    // Synchronizes the user to add it to the groups and recursivaly add the groups
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
    processSpecificSynchronization(theUserDetail.getDomainId(), null, null, singletonList(theUserDetail));
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
      final Pair<String, List<AbstractBackgroundProcessRequest>> result = Transaction
          .performInNew(new Process<Pair<String, List<AbstractBackgroundProcessRequest>>>() {
        @Override
        public Pair<String, List<AbstractBackgroundProcessRequest>> execute() throws Exception {
          String sReport = "Starting synchronization...\n\n";
          synchronized (semaphore) {
            // Starting synchronization with a status popup
            SynchroDomainReport.startSynchro();
            try {
              SynchroDomainReport.info(ADMIN_SYNCHRONIZE_DOMAIN,
                  "Domain '" + domainDriverManager.getDomain(sDomainId).getName() + "', Id : " +
                      sDomainId);
              // Start synchronization
              domainDriverManager.beginSynchronization(sDomainId);
              final DomainDriver synchroDomain = domainDriverManager.getDomainDriver(sDomainId);
              // Synchronize users
              final boolean addUserIntoSilverpeas = synchroDomain.mustImportUsers() || threaded;
              final SyncOfUsersContext context = new SyncOfUsersContext(sDomainId, threaded,
                  addUserIntoSilverpeas, delUsersOnDiffSynchro);
              final SyncOfUsersContext syncOfUsersContext = synchronizeUsers(context);
              sReport += syncOfUsersContext.getReport();
              // Synchronize groups
              // Get all users of the domain from Silverpeas
              final UserDetail[] silverpeasUDs = userManager.getAllUsersInDomain(sDomainId, true);
              final Map<String, String> userIdsMapping = getUserIdsMapping(silverpeasUDs);
              sReport += "\n" + synchronizeGroups(sDomainId, userIdsMapping);
              // End synchronization
              final String sDomainSpecificErrors = domainDriverManager.endSynchronization(sDomainId, false);
              if (StringUtil.isDefined(sDomainSpecificErrors)) {
                SynchroDomainReport
                    .info(ADMIN_SYNCHRONIZE_DOMAIN, "----------------" + sDomainSpecificErrors);
              }
              return Pair.of(
                  sReport + "\n----------------\n" + sDomainSpecificErrors,
                  singletonList(syncOfUsersContext.getIndexationBackgroundProcess()));
            } catch (Exception e) {
              try {
                // End synchronization
                domainDriverManager.endSynchronization(sDomainId, true);
              } catch (Exception e1) {
                SilverLogger.getLogger(this).error(e1);
              }
              SynchroDomainReport.error(ADMIN_SYNCHRONIZE_DOMAIN,
                  "Problème lors de la synchronisation : " + e.getMessage(), null);
              throw new AdminException(
                  "Fail to synchronize domain " + sDomainId + ". Report: " + sReport, e);
            } finally {
              SynchroDomainReport.stopSynchro();// Fin de synchro avec la Popup d'affichage
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
    boolean dataUpdated =  !Objects.equals(silverpeasUser.getSpecificId(), distantUser.getSpecificId());
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
      message = "# of updated users: " + context.getUpdatedUsers().size() +
          ", added: " + context.getAddedUsers().size() +
          ", removed: " + context.getRemovedUsers().size() +
          ", restored: " + context.getRestoredUsers().size() +
          ", deleted: " + context.getDeletedUsers().size();
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
      final UserDetail userToUpdateFromDistantUser = getUserBySpecificIdOrFallbackLoginFrom(distantUD, indexedSpUsers);
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
   * Background process request which ensure the reminder scheduler to not be disturbed by user
   * notification send processing.
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
  private Map<String, UserDetail> indexUsersBySpecificIdAndLogin(
      final UserDetail[] silverpeasUDs) {
    final Map<String, UserDetail> indexedSilverpeasUsers = new HashMap<>(silverpeasUDs.length * 2);
    Arrays.stream(silverpeasUDs).forEach(u -> {
      indexedSilverpeasUsers.put(u.getSpecificId(), u);
      indexedSilverpeasUsers.put(u.getLogin(), u);
    });
    return indexedSilverpeasUsers;
  }

  @Nonnull
  private Set<String> extractUserSpecificIdAndFallbackLogin(final UserDetail[] users) {
    final Set<String> indexedUsers = new HashSet<>(
        shouldFallbackUserLogins ? (users.length * 2) : users.length);
    Arrays.stream(users).forEach(u -> {
      indexedUsers.add(u.getSpecificId());
      if (shouldFallbackUserLogins) {
        indexedUsers.add(u.getLogin());
      }
    });
    return indexedUsers;
  }

  /**
   * @param silverpeasUDs existing users after synchronization
   * @return a Map <specificId, userId>
   */
  private HashMap<String, String> getUserIdsMapping(UserDetail[] silverpeasUDs) {
    HashMap<String, String> ids = new HashMap<>();
    for (UserDetail user : silverpeasUDs) {
      ids.put(user.getSpecificId(), user.getId());
    }
    return ids;
  }

  private void updateUserDuringSynchronization(final SyncOfUsersContext context,
      final UserDetail distantUD) {
    final String specificId = distantUD.getSpecificId();
    try {
      final String silverpeasId = userManager.updateUser(distantUD, false);
      context.getUpdatedUsers().put(silverpeasId, distantUD);
      final String message = format("{0} {1} updated (id:{2} / specificId:{3})", USER,
          distantUD.getDisplayedName(), silverpeasId, specificId);
      SynchroDomainReport.info(ADMIN_SYNCHRONIZE_USERS, message);
      context.appendToReport(message).appendToReport("\n");
    } catch (AdminException aeMaj) {
      SilverLogger.getLogger(this)
          .error("Full synchro: error while updating user " + specificId, aeMaj);
      final String errorMessage = format("problem updating user {0} (specificId:{1}) - {2}",
          distantUD.getDisplayedName(), specificId, aeMaj.getMessage());
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
        final String message = format(
            "problem adding user {0} (specificId:{1}) - Login and LastName must be set !!!",
            distantUD.getDisplayedName(), specificId);
        context.appendToReport(message).appendToReport("\n");
        SynchroDomainReport.warn(ADMIN_SYNCHRONIZE_USERS, message);
        context.appendToReport("user has not been added\n");
      } else {
        context.getAddedUsers().put(silverpeasId, distantUD);
        final String message = format("{0} {1} added (id:{2} / specificId:{3})", USER,
            distantUD.getDisplayedName(), silverpeasId, specificId);
        context.appendToReport(message).appendToReport("\n");
        SynchroDomainReport.info(ADMIN_SYNCHRONIZE_USERS, message);
      }
    } catch (AdminException ae) {
      SilverLogger.getLogger(this).error("Full synchro: error while adding user " + specificId, ae);
      final String message = format("problem adding user {0}(specificId:{1}) - {2}",
          distantUD.getDisplayedName(), specificId, ae.getMessage());
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
      final String message = format("{0} {1} removed (id:{2} / specificId:{3})", USER,
          silverpeasUD.getDisplayedName(), silverpeasUD.getId(), specificId);
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
      silverpeasUD.setState(UserState.VALID);
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
            final String message = format("{0} {1} deleted (id:{2} / login:{3})", USER,
                distantUD.getDisplayedName(), distantUD.getId(), login);
            context.appendToReport(message).appendToReport("\n");
            SynchroDomainReport.info(ADMIN_SYNCHRONIZE_USERS, message);
          } else {
            final String message = format(
                "{0} {1} must have 'REMOVED' state for deletion (id:{2} / login:{3})", USER,
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
        LDAPSynchroUserItf synchroUser = (LDAPSynchroUserItf) Class.forName(nomClasseSynchro).
            newInstance();
        if (synchroUser != null) {
          synchroUser.processUsers(added, updated, removed);
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
  }

  /**
   * Synchronize groups between cache and domain's datastore
   */
  private String synchronizeGroups(String domainId, Map<String, String> userIds)
      throws AdminException {
    boolean bFound;
    String specificId;
    StringBuilder sReport = new StringBuilder("GroupDetail synchronization : \n");
    Map<String, GroupDetail> allDistantGroups = new HashMap<>();
    int iNbGroupsAdded = 0;
    int iNbGroupsMaj = 0;
    int iNbGroupsDeleted = 0;
    SynchroDomainReport.info(ADMIN_SYNCHRONIZE_GROUPS, "Starting groups synchronization...");
    try {
      // Get all root groups of the domain from distant datasource
      GroupDetail[] distantRootGroups = domainDriverManager.getAllRootGroups(domainId);
      // Get all groups of the domain from Silverpeas
      GroupDetail[] silverpeasGroups = groupManager.getGroupsOfDomain(domainId);

      SynchroDomainReport.debug(ADMIN_SYNCHRONIZE_GROUPS, "Adding or updating groups in database...");
      // Check for new groups resursively
      final CheckoutGroupDescriptor descriptor = new CheckoutGroupDescriptor().setDomainId(domainId)
          .setExistingGroups(silverpeasGroups)
          .setTestedGroups(distantRootGroups)
          .setAllIncludedGroups(allDistantGroups)
          .setUserIds(userIds)
          .setNbGroupsAdded(iNbGroupsAdded)
          .setNbGroupsUpdated(iNbGroupsMaj);
      sReport.append(checkOutGroups(descriptor));

      // Delete obsolete groups
      SynchroDomainReport.debug(ADMIN_SYNCHRONIZE_GROUPS, "Removing groups from database...");
      GroupDetail[] distantGroups = allDistantGroups.values().toArray(new GroupDetail[0]);
      for (GroupDetail silverpeasGroup : silverpeasGroups) {
        bFound = false;
        specificId = silverpeasGroup.getSpecificId();

        // search for group in distant datasource
        for (int nJ = 0; nJ < distantGroups.length && !bFound; nJ++) {
          if (distantGroups[nJ].getSpecificId().equals(specificId) ||
              (shouldFallbackGroupNames && distantGroups[nJ].getName().equals(specificId))) {
            bFound = true;
          }
        }

        // if found, do nothing, else delete
        if (!bFound) {
          iNbGroupsDeleted =
              synchroDeleteGroup(specificId, silverpeasGroup, sReport, iNbGroupsDeleted);
        }
      }
      sReport.append("Groups synchronization terminated\n");
      SynchroDomainReport.info(ADMIN_SYNCHRONIZE_GROUPS,
          "# of groups updated : " + iNbGroupsMaj + ", added : " + iNbGroupsAdded
          + ", deleted : " + iNbGroupsDeleted);
      SynchroDomainReport.info(ADMIN_SYNCHRONIZE_GROUPS, "Groups synchronization terminated");
      return sReport.toString();
    } catch (Exception e) {
      SynchroDomainReport.error(ADMIN_SYNCHRONIZE_GROUPS,
          "Problème lors de la synchronisation des groupes : " + e.getMessage(), null);
      throw new AdminException("Fails to synchronize groups in domain " + domainId
          + ".Report: " + sReport, e);
    }
  }

  private int synchroDeleteGroup(final String specificId, final GroupDetail silverpeasGroup,
      final StringBuilder sReport, int iNbGroupsDeleted) {
    try {
      groupManager.deleteGroup(silverpeasGroup, true);
      iNbGroupsDeleted++;
      sReport.append("deleting group " + silverpeasGroup.getName() + "(id:" + specificId + ")\n");
      SynchroDomainReport.info(ADMIN_SYNCHRONIZE_GROUPS,
          "GroupDetail " + silverpeasGroup.getName() + " deleted (SpecificId:" + specificId + ")");
    } catch (AdminException aeDel) {
      SilverLogger.getLogger(this)
          .error("Full synchro: error while deleting group " + specificId, aeDel);
      sReport.append(
          "problem deleting group " + silverpeasGroup.getName() + " (specificId:" + specificId +
              ") - " + aeDel.getMessage() + "\n");
      sReport.append("group has not been deleted\n");
    }
    return iNbGroupsDeleted;
  }

  /**
   * Checks for new groups resursively
   */
  // Au 1er appel : (domainId,silverpeasGroups,distantRootGroups,
  // allDistantGroups(vide), userIds, null)
  // No need to refresh cache : the cache is reseted at the end of the
  // synchronization
  private String checkOutGroups(final CheckoutGroupDescriptor descriptor) throws AdminException {
    StringBuilder report = new StringBuilder();
    // Add new groups or update existing ones from distant data source
    descriptor.addTestedGroupsInAllIncludedGroups();
    for (GroupDetail testedGroup : descriptor.getTestedGroups()) {
      // Prepare GroupDetail to be at Silverpeas format
      testedGroup.setDomainId(descriptor.getDomainId());
      final String specificId = testedGroup.getSpecificId();

      // search for group in Silverpeas database
      Optional<GroupDetail> foundGroup = Arrays.stream(descriptor.getExistingGroups())
          .filter(g -> g.getSpecificId().equals(specificId) ||
              (shouldFallbackGroupNames && g.getSpecificId().equals(testedGroup.getName())))
          .findFirst();
      if (foundGroup.isPresent()) {
        testedGroup.setId(foundGroup.get().getId());
        SynchroDomainReport.debug(
            ADMIN_SYNCHRONIZE_CHECK_OUT_GROUPS, "avant maj du groupe " + specificId
                + ", recherche de ses groupes parents");
      } else {
        SynchroDomainReport.debug(
            ADMIN_SYNCHRONIZE_CHECK_OUT_GROUPS, "avant ajout du groupe " + specificId
                + ", recherche de ses groupes parents");
      }

      setParentGroup(descriptor, specificId, testedGroup);
      setUserIds(descriptor, testedGroup);
      // if found, update, else create
      final String silverpeasId;
      if (foundGroup.isPresent()) {
        silverpeasId = updateGroup(descriptor, specificId, testedGroup, report);
      } else { // AJOUT
        silverpeasId = addGroup(descriptor, specificId, testedGroup, report);
      }
      // Recurse with subgroups
      recursWithSubGroups(descriptor, specificId, silverpeasId, report);
    }
    return report.toString();
  }

  private void recursWithSubGroups(final CheckoutGroupDescriptor descriptor,
      final String specificId, final String silverpeasId, final StringBuilder report)
      throws AdminException {
    if (silverpeasId != null && silverpeasId.length() > 0) {
      GroupDetail[] subGroups = domainDriverManager.getGroups(silverpeasId);
      if (subGroups != null && subGroups.length > 0) {
        GroupDetail[] cleanSubGroups = removeCrossReferences(subGroups,
            descriptor.getAllIncludedGroups(), specificId);
        if (cleanSubGroups != null && cleanSubGroups.length > 0) {
          SynchroDomainReport.debug(ADMIN_SYNCHRONIZE_CHECK_OUT_GROUPS,
              "Ajout ou mise à jour de " + cleanSubGroups.length + " groupes fils du groupe "
              + specificId + "...");
          descriptor.setTestedGroups(cleanSubGroups).setSuperGroupId(silverpeasId);
          report.append(checkOutGroups(descriptor));
        }
      }
    }
  }

  @Nullable
  private String addGroup(final CheckoutGroupDescriptor descriptor, final String specificId,
      final GroupDetail testedGroup, final StringBuilder report) {
    String silverpeasId = null;
    try {
      silverpeasId = groupManager.addGroup(testedGroup, true);
      if (StringUtil.isDefined(silverpeasId)) {
        descriptor.setNbGroupsAdded(descriptor.getNbGroupsAdded() + 1);

        report.append("adding group " + testedGroup.getName() + "(id:" + specificId + ")\n");
        SynchroDomainReport.debug(
            ADMIN_SYNCHRONIZE_CHECK_OUT_GROUPS, "ajout groupe " + testedGroup.getName() +
                ID_IS + silverpeasId + ") OK");
      } else { // le name groupe non renseigné

        report.append("problem adding group id : " + specificId + "\n");
      }
    } catch (AdminException aeAdd) {

      report.append(
          "problem adding group " + testedGroup.getName() + ID_IS + specificId + ") " +
              aeAdd.getMessage() + "\n");
      report.append("group has not been added\n");
    }
    return silverpeasId;
  }

  private String updateGroup(final CheckoutGroupDescriptor descriptor, final String specificId,
      final GroupDetail testedGroup, final StringBuilder report) {
    final String result;
    String silverpeasId = null;
    try {
      result = groupManager.updateGroup(testedGroup, true);
      if (StringUtil.isDefined(result)) {
        descriptor.setNbGroupsUpdated(descriptor.getNbGroupsUpdated() + 1);
        silverpeasId = testedGroup.getId();
        report.append("updating group " + testedGroup.getName() + "(id:" + specificId + ")\n");
        SynchroDomainReport.debug(ADMIN_SYNCHRONIZE_CHECK_OUT_GROUPS, "maj groupe " + testedGroup.getName() +
            ID_IS + silverpeasId + ") OK");
      } else {
        // le name groupe non renseigné
        SilverLogger.getLogger(this).error("Full Synchro: error while updating group {0}",
            specificId);
        report.append("problem updating group id : " + specificId + "\n");
      }
    } catch (AdminException aeMaj) {
      SilverLogger.getLogger(this).error("Full Synchro: error while updating group {0}: ",
          specificId, aeMaj.getMessage());
      report.append(
          "problem updating group " + testedGroup.getName() + ID_IS + specificId + ") " +
              aeMaj.getMessage() + "\n");
      report.append("group has not been updated\n");
    }
    return silverpeasId;
  }

  private void setUserIds(final CheckoutGroupDescriptor descriptor, final GroupDetail testedGroup) {
    String[] userSpecificIds = testedGroup.getUserIds();
    List<String> convertedUserIds = new ArrayList<>();
    for (String userSpecificId : userSpecificIds) {
      if (descriptor.getUserIds().get(userSpecificId) != null) {
        convertedUserIds.add(descriptor.getUserIds().get(userSpecificId));
      }
    }
    // Le groupe contiendra une liste d'IDs de users existant ds la base et
    // non + une liste de logins récupérés via LDAP
    testedGroup.setUserIds(convertedUserIds.toArray(new String[0]));
  }

  private void setParentGroup(final CheckoutGroupDescriptor descriptor, final String specificId,
      final GroupDetail testedGroup) throws AdminException {
    String[] groupParentsIds =
        domainDriverManager.getGroupMemberGroupIds(descriptor.getDomainId(), testedGroup.
        getSpecificId());
    if ((groupParentsIds == null) || (groupParentsIds.length == 0)) {
      testedGroup.setSuperGroupId(null);
      SynchroDomainReport.debug(
          ADMIN_SYNCHRONIZE_CHECK_OUT_GROUPS, "le groupe " + specificId + " n'a pas de père");
    } else {
      testedGroup.setSuperGroupId(descriptor.getSuperGroupId());
      if (descriptor.getSuperGroupId() != null)// sécurité
      {
        SynchroDomainReport.debug(ADMIN_SYNCHRONIZE_CHECK_OUT_GROUPS,
            "le groupe " + specificId + " a pour père le groupe " + domainDriverManager.getGroup(
                descriptor.getSuperGroupId()).getSpecificId() + " d'Id base " +
                descriptor.getSuperGroupId());
      }
    }
  }

  /**
   * Remove cross reference risk between groups
   */
  private GroupDetail[] removeCrossReferences(GroupDetail[] subGroups, Map<String, GroupDetail> allIncluededGroups,
      String fatherId) {
    ArrayList<GroupDetail> cleanSubGroups = new ArrayList<>();
    //noinspection UnusedAssignment,UnusedAssignment,UnusedAssignment
    for (GroupDetail subGroup : subGroups) {
      if (allIncluededGroups.get(subGroup.getSpecificId()) == null) {
        cleanSubGroups.add(subGroup);
      } else {
        SilverLogger.getLogger(this).warn("Cross deletion for child {0} of the father {1}",
            subGroup.getSpecificId(), fatherId);
      }
    }
    return cleanSubGroups.toArray(new GroupDetail[0]);
  }

  @Override
  public List<String> searchUserIdsByProfile(final List<String> profileIds) throws AdminException {
    Set<String> userIds = new HashSet<>();
    // search users in profiles
    try {
      for (String profileId : profileIds) {
        ProfileInst profile = profileManager.getProfileInst(profileId);
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
  public ListSlice<UserDetail> searchUsers(final UserDetailsSearchCriteria searchCriteria) throws
      AdminException {
    List<String> userIds = null;
    if (searchCriteria.isCriterionOnComponentInstanceIdSet()) {
      userIds = searchUsersInComponentInstance(searchCriteria, userIds);
    }

    if (searchCriteria.isCriterionOnUserIdsSet()) {
      userIds = searchUserByTheirIds(searchCriteria, userIds);
    }

    SearchCriteriaDAOFactory factory = SearchCriteriaDAOFactory.getFactory();
    UserSearchCriteriaForDAO criteria = factory.getUserSearchCriteriaDAO();
    if (userIds != null) {
      criteria.onUserIds(userIds.toArray(new String[0]));
    }
    if (searchCriteria.isCriterionOnGroupIdsSet()) {
      setCriteriaWithGroupIds(searchCriteria, criteria);
    }
    if (searchCriteria.isCriterionOnDomainIdSet()) {
      criteria.and().onDomainIds(searchCriteria.getCriterionOnDomainIds());
    }
    if (searchCriteria.isCriterionOnUserSpecificIdsSet()) {
      criteria.and().onUserSpecificIds(searchCriteria.getCriterionOnUserSpecificIds());
    }
    if (searchCriteria.isCriterionOnAccessLevelsSet()) {
      criteria.and().onAccessLevels(searchCriteria.getCriterionOnAccessLevels());
    }
    if (searchCriteria.isCriterionOnUserStatesToExcludeSet()) {
      criteria.and().onUserStatesToExclude(searchCriteria.getCriterionOnUserStatesToExclude());
    }
    if (searchCriteria.isCriterionOnNameSet()) {
      criteria.and().onName(searchCriteria.getCriterionOnName());
    } else {
      if (searchCriteria.isCriterionOnFirstNameSet()) {
        criteria.and().onFirstName(searchCriteria.getCriterionOnFirstName());
      }
      if (searchCriteria.isCriterionOnLastNameSet()) {
        criteria.and().onLastName(searchCriteria.getCriterionOnLastName());
      }
    }
    if (searchCriteria.isCriterionOnPaginationSet()) {
      criteria.onPagination(searchCriteria.getCriterionOnPagination());
    }

    return userManager.getUsersMatchingCriteria(criteria);
  }

  private void setCriteriaWithGroupIds(final UserDetailsSearchCriteria searchCriteria,
      final UserSearchCriteriaForDAO criteria) throws AdminException {
    String[] theGroupIds = searchCriteria.getCriterionOnGroupIds();
    if (theGroupIds == UserDetailsSearchCriteria.ANY_GROUPS) {
      criteria.and().onGroupIds(SearchCriteria.ANY);
    } else {
      Set<String> groupIds = new HashSet<>();
      for (String aGroupId : theGroupIds) {
        groupIds.addAll(groupManager.getAllSubGroupIdsRecursively(aGroupId));
        groupIds.add(aGroupId);
      }
      criteria.and().onGroupIds(groupIds.toArray(new String[0]));
    }
  }

  @NotNull
  private List<String> searchUserByTheirIds(final UserDetailsSearchCriteria searchCriteria,
      List<String> userIds) {
    if (userIds == null) {
      userIds = Arrays.asList(searchCriteria.getCriterionOnUserIds());
    } else {
      List<String> userIdsInCriterion = Arrays.asList(searchCriteria.getCriterionOnUserIds());
      List<String> userIdsToTake = new ArrayList<>();
      for (String userId : userIds) {
        if (userIdsInCriterion.contains(userId)) {
          userIdsToTake.add(userId);
        }
      }
      userIds = userIdsToTake;
    }
    return userIds;
  }

  @Nullable
  private List<String> searchUsersInComponentInstance(
      final UserDetailsSearchCriteria searchCriteria, final List<String> userIds) throws AdminException {
    List<String> listOfRoleNames = Collections.emptyList();
    List<String> result = userIds == null ? new ArrayList<>() : userIds;
    if (searchCriteria.isCriterionOnRoleNamesSet()) {
      listOfRoleNames = Arrays.asList(searchCriteria.getCriterionOnRoleNames());
    }
    SilverpeasComponentInstance instance =
        getComponentInstance(searchCriteria.getCriterionOnComponentInstanceId());
    if (!listOfRoleNames.isEmpty() || !instance.isPublic()) {
      result = new ArrayList<>();
      if (!instance.isPersonal()) {
        addUserIdsByCriteria(instance, searchCriteria, listOfRoleNames, result);
      } else {
        final User user = ((SilverpeasPersonalComponentInstance) instance).getUser();
        final Collection<String> userRoles =
            instance.getSilverpeasRolesFor(user).stream().map(Enum::name)
                .collect(Collectors.toList());
        if (!CollectionUtil.intersection(userRoles, listOfRoleNames).isEmpty()) {
          result.add(user.getId());
        }
      }
      if (result.isEmpty()) {
        result = null;
      }
    }
    return result;
  }

  private void addUserIdsByCriteria(final SilverpeasComponentInstance instance,
      final UserDetailsSearchCriteria searchCriteria, final List<String> listOfRoleNames,
      final List<String> result) throws AdminException {
    List<ProfileInst> profiles;
    if (searchCriteria.isCriterionOnResourceIdSet()) {
      profiles =
          getProfileInstsFor(searchCriteria.getCriterionOnResourceId(), instance.getId());
    } else {
      profiles = getComponentInst(instance.getId()).getAllProfilesInst();
    }
    for (ProfileInst aProfile : profiles) {
      if (listOfRoleNames.isEmpty() || listOfRoleNames.
          contains(aProfile.getName())) {
        addAllUsersInProfile(aProfile, result);
      }
    }
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
  public SilverpeasList<GroupDetail> searchGroups(final GroupsSearchCriteria searchCriteria) throws
      AdminException {
    SearchCriteriaDAOFactory factory = SearchCriteriaDAOFactory.getFactory();
    GroupSearchCriteriaForDAO criteria = factory.getGroupSearchCriteriaDAO();
    if (searchCriteria.isCriterionOnComponentInstanceIdSet()) {
      makeCriteriaOnComponentInstanceId(searchCriteria, criteria);
    }

    if (searchCriteria.childrenRequired()) {
      criteria.withChildren();
    }

    if (searchCriteria.mustBeRoot()) {
      criteria.onAsRootGroup();
    }

    if (searchCriteria.isCriterionOnDomainIdSet()) {
      String domainId = searchCriteria.getCriterionOnDomainId();
      if (searchCriteria.isCriterionOnMixedDomainIdSet()) {
        criteria.onMixedDomainOrOnDomainId(domainId);
      } else {
        criteria.onDomainIds(domainId);
      }
    }

    if (searchCriteria.isCriterionOnGroupIdsSet()) {
      criteria.and().onGroupIds(searchCriteria.getCriterionOnGroupIds());
    }

    if (searchCriteria.isCriterionOnNameSet()) {
      criteria.and().onName(searchCriteria.getCriterionOnName());
    }

    if (searchCriteria.isCriterionOnAccessLevelsSet()) {
      criteria.and().onAccessLevels(searchCriteria.getCriterionOnAccessLevels());
    }

    if (searchCriteria.isCriterionOnUserStatesToExcludeSet()) {
      criteria.and().onUserStatesToExclude(searchCriteria.getCriterionOnUserStatesToExclude());
    }

    if (searchCriteria.isCriterionOnSuperGroupIdSet()) {
      criteria.and().onSuperGroupId(searchCriteria.getCriterionOnSuperGroupId());
    }

    if (searchCriteria.isCriterionOnPaginationSet()) {
      criteria.onPagination(searchCriteria.getCriterionOnPagination());
    }

    return groupManager.getGroupsMatchingCriteria(criteria);
  }

  private void makeCriteriaOnComponentInstanceId(final GroupsSearchCriteria searchCriteria,
      final GroupSearchCriteriaForDAO criteria) throws AdminException {
    final List<String> listOfRoleNames = new ArrayList<>();
    if (searchCriteria.isCriterionOnRoleNamesSet()) {
      listOfRoleNames.addAll(Arrays.asList(searchCriteria.getCriterionOnRoleNames()));
    }
    SilverpeasComponentInstance instance =
        getComponentInstance(searchCriteria.getCriterionOnComponentInstanceId());
    if (!listOfRoleNames.isEmpty() || !instance.isPublic()) {
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
            .filter(p -> listOfRoleNames.isEmpty() || listOfRoleNames.contains(p.getName()))
            .forEach(p -> roleIds.add(p.getId()));
      }
      criteria.onRoleNames(roleIds.toArray(new String[0]));
    }
  }

  // -------------------------------------------------------------------------
  // Node profile management
  // -------------------------------------------------------------------------
  @Override
  public void indexAllUsers() throws AdminException {
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
  public String copyAndPasteComponent(PasteDetail pasteDetail) throws AdminException,
      QuotaException {
    if (!StringUtil.isDefined(pasteDetail.getToSpaceId())) {
      // cannot paste component on root
      return null;
    }
    ComponentInst newCompo = (ComponentInst) getComponentInst(pasteDetail.getFromComponentId()).
        clone();
    SpaceInst destinationSpace = getSpaceInstById(pasteDetail.getToSpaceId());

    String lang = newCompo.getLanguage();
    if (StringUtil.isNotDefined(lang)) {
      lang = I18NHelper.defaultLanguage;
    }
    // Creation
    newCompo.setLocalId(-1);
    newCompo.setDomainFatherId(destinationSpace.getId());
    newCompo.setOrderNum(destinationSpace.getNumComponentInst());
    newCompo.setCreateDate(new Date());
    newCompo.setCreatorUserId(pasteDetail.getUserId());
    newCompo.setLanguage(lang);

    // Rename if componentName already exists in the destination space
    String label = renameComponentName(newCompo.getLabel(lang), destinationSpace.
        getAllComponentsInst());
    newCompo.setLabel(label);
    ComponentI18N translation = newCompo.getTranslation(lang);
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
      ApplicationResourcePasting componentPaste = ServiceProvider
          .getServiceByComponentInstanceAndNameSuffix(pasteDetail.getFromComponentId(),
              ApplicationResourcePasting.NAME_SUFFIX);
      componentPaste.paste(pasteDetail);
    } catch (IllegalStateException e) {
      SilverLogger.getLogger(this).silent(e);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
    return sComponentId;
  }

  /**
   * Rename component Label if necessary
   *
   * @param label
   * @param listComponents
   * @return
   */
  private String renameComponentName(String label, ArrayList<ComponentInst> listComponents) {
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
    for(SpaceInst subSpaceInst: subSpaceInsts) {
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

  @NotNull
  private SpaceInst createPasteSpace(final PasteDetail pasteDetail, final SpaceInst oldSpace,
      final String toSpaceId) throws AdminException {
    SpaceInst newSpace = oldSpace.clone();
    newSpace.setLocalId(-1);
    List<String> newBrotherIds;
    if (StringUtil.isDefined(toSpaceId)) {
      SpaceInst destinationSpace = getSpaceInstById(toSpaceId);
      newSpace.setDomainFatherId(destinationSpace.getId());
      List<SpaceInst> brothers = destinationSpace.getSubSpaces();
      newBrotherIds = new ArrayList<>(brothers.size());
      for(SpaceInst brother: brothers) {
        newBrotherIds.add(brother.getId());
      }
    } else {
      newSpace.setDomainFatherId("-1");
      newBrotherIds = Arrays.asList(getAllRootSpaceIds());
    }
    newSpace.setOrderNum(newBrotherIds.size());
    newSpace.setCreateDate(new Date());
    newSpace.setCreatorUserId(pasteDetail.getUserId());
    String lang = oldSpace.getLanguage();
    if (StringUtil.isNotDefined(lang)) {
      lang = I18NHelper.defaultLanguage;
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
   *
   * @param resourceId the unique identifier of a resource managed in a component instance.
   * @param instanceId the unique identifier of the component instance.
   * @return a list of profile instances.
   */
  private List<ProfileInst> getProfileInstsFor(String resourceId, String instanceId) throws
      AdminException {
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
   * Get all the space profiles Id for the given user without group transitivity.
   */
  private String[] getDirectSpaceProfileIdsOfUser(String sUserId) throws AdminException {
    try {
      return spaceProfileManager.getSpaceProfileIdsOfUserType(sUserId);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("space profiles of user", sUserId), e);
    }
  }

  /**
   * Get all the space profiles Id for the given group
   */
  private String[] getDirectSpaceProfileIdsOfGroup(String groupId) throws AdminException {
    try {
      return spaceProfileManager.getSpaceProfileIdsOfGroupType(groupId);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("space profiles of group", groupId), e);
    }
  }

  /**
   * Get all the component profiles Id for the given user without group transitivity.
   * (component object profiles are not retrieved)
   */
  @SuppressWarnings("unchecked")
  private String[] getDirectComponentProfileIdsOfUser(String sUserId) throws AdminException {
    try {
      return profileManager.getProfileIdsOfUser(sUserId, Collections.emptyList());
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("component profiles of user", sUserId), e);
    }
  }

  /**
   * Get all the component profiles Id for the given group.
   * (component object profiles are not retrieved)
   */
  @SuppressWarnings("unchecked")
  private String[] getDirectComponentProfileIdsOfGroup(String groupId) throws AdminException {
    try {
      return profileManager.getProfileIdsOfGroup(groupId);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("component profiles of group", groupId), e);
    }
  }

  /**
   * Get all the component object profiles Id for the given user.
   * (direct component profiles are not retrieved)
   */
  @SuppressWarnings("unchecked")
  private String[] getComponentObjectProfileIdsOfUserType(String userId) throws AdminException {
    try {
      // retrieve value from database
      return profileManager.getAllComponentObjectProfileIdsOfUser(userId, Collections.emptyList());
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("component profiles of user", userId), e);
    }
  }

  /**
   * Get all the component object profiles Id for the given group.
   * (direct component profiles are not retrieved)
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

    //Add space rights (and sub-space and components, by inheritance)
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
          getComponentInst(currentSourceProfile.getComponentFatherId());
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

    // Delete space rights (and sub-space and components, by inheritance) for target
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
          getComponentInst(currentTargetProfile.getComponentFatherId());
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
   * @throws AdminException
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

  private static class CheckoutGroupDescriptor {
    private String domainId;
    private GroupDetail[] existingGroups;
    private GroupDetail[] testedGroups;
    private Map<String, GroupDetail> allIncludedGroups;
    private Map<String, String> userIds;
    private String superGroupId;
    private int nbGroupsAdded;
    private int nbGroupsUpdated;

    void addTestedGroupsInAllIncludedGroups() {
      for (GroupDetail testedGroup : testedGroups) {
        allIncludedGroups.put(testedGroup.getSpecificId(), testedGroup);
      }
    }

    public String getDomainId() {
      return domainId;
    }

    public CheckoutGroupDescriptor setDomainId(final String domainId) {
      this.domainId = domainId;
      return this;
    }

    public GroupDetail[] getExistingGroups() {
      return existingGroups;
    }

    public CheckoutGroupDescriptor setExistingGroups(final GroupDetail[] existingGroups) {
      this.existingGroups = existingGroups;
      return this;
    }

    public GroupDetail[] getTestedGroups() {
      return testedGroups;
    }

    public CheckoutGroupDescriptor setTestedGroups(final GroupDetail[] testedGroups) {
      this.testedGroups = testedGroups;
      return this;
    }

    public Map<String, GroupDetail> getAllIncludedGroups() {
      return allIncludedGroups;
    }

    public CheckoutGroupDescriptor setAllIncludedGroups(
        final Map<String, GroupDetail> allIncludedGroups) {
      this.allIncludedGroups = allIncludedGroups;
      return this;
    }

    public Map<String, String> getUserIds() {
      return userIds;
    }

    public CheckoutGroupDescriptor setUserIds(final Map<String, String> userIds) {
      this.userIds = userIds;
      return this;
    }

    public String getSuperGroupId() {
      return superGroupId;
    }

    public CheckoutGroupDescriptor setSuperGroupId(final String superGroupId) {
      this.superGroupId = superGroupId;
      return this;
    }

    public int getNbGroupsAdded() {
      return nbGroupsAdded;
    }

    public CheckoutGroupDescriptor setNbGroupsAdded(final int nbGroupsAdded) {
      this.nbGroupsAdded = nbGroupsAdded;
      return this;
    }

    public int getNbGroupsUpdated() {
      return nbGroupsUpdated;
    }

    public CheckoutGroupDescriptor setNbGroupsUpdated(final int nbGroupsUpdated) {
      this.nbGroupsUpdated = nbGroupsUpdated;
      return this;
    }
  }
}
