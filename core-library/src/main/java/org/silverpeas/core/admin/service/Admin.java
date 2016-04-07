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
package org.silverpeas.core.admin.service;

import org.silverpeas.core.admin.component.ApplicationResourcePasting;
import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.admin.component.ComponentInstancePostConstruction;
import org.silverpeas.core.admin.component.ComponentInstancePreDestruction;
import org.silverpeas.core.admin.component.model.CompoSpace;
import org.silverpeas.core.admin.component.model.ComponentI18N;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.Parameter;
import org.silverpeas.core.admin.component.model.PasteDetail;
import org.silverpeas.core.admin.component.model.Profile;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.component.WAComponentRegistry;
import org.silverpeas.core.admin.domain.synchro.SynchroDomainReport;
import org.silverpeas.core.admin.domain.synchro.SynchroDomainScheduler;
import org.silverpeas.core.admin.domain.synchro.SynchroGroupScheduler;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.domain.DomainDriver;
import org.silverpeas.core.admin.domain.DomainDriverManager;
import org.silverpeas.core.admin.domain.DomainDriverManagerProvider;
import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.admin.domain.synchro.SynchroGroupReport;
import org.silverpeas.core.admin.service.cache.AdminCache;
import org.silverpeas.core.admin.service.cache.TreeCache;
import org.silverpeas.core.admin.space.SpaceAndChildren;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.space.SpaceInstanciator;
import org.silverpeas.core.admin.space.SpaceProfileInst;
import org.silverpeas.core.admin.space.SpaceProfileInstManager;
import org.silverpeas.core.admin.space.model.SpaceTemplate;
import org.silverpeas.core.contribution.contentcontainer.container.ContainerManager;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManager;
import org.silverpeas.core.admin.domain.driver.ldapdriver.LDAPSynchroUserItf;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.admin.user.GroupManager;
import org.silverpeas.core.admin.user.ProfileInstManager;
import org.silverpeas.core.admin.user.ProfiledObjectManager;
import org.silverpeas.core.admin.user.dao.SearchCriteriaDAOFactory;
import org.silverpeas.core.admin.user.UserManager;
import org.silverpeas.core.admin.user.model.*;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.domain.model.DomainCache;
import org.silverpeas.core.admin.user.model.GroupCache;
import org.silverpeas.core.admin.space.model.Space;
import org.silverpeas.core.admin.user.dao.GroupSearchCriteriaForDAO;
import org.silverpeas.core.admin.user.dao.UserSearchCriteriaForDAO;
import org.silverpeas.core.admin.persistence.AdminPersistenceException;
import org.silverpeas.core.admin.persistence.OrganizationSchemaPool;
import org.silverpeas.core.admin.persistence.ScheduledDBReset;
import org.silverpeas.core.admin.persistence.UserRow;
import org.silverpeas.core.admin.space.SpaceServiceProvider;
import org.silverpeas.core.admin.space.notification.SpaceEventNotifier;
import org.silverpeas.core.admin.space.quota.ComponentSpaceQuotaKey;
import org.silverpeas.core.admin.space.quota.DataStorageSpaceQuotaKey;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.admin.quota.model.Quota;
import org.silverpeas.core.index.indexing.IndexFileManager;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.ListSlice;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.util.logging.Level;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.silverpeas.core.silvertrace.SilverTrace.MODULE_ADMIN;

/**
 * The class Admin is the main class of the Administrator.
 * <br/> The role of the administrator is to create and maintain spaces.
 */
@Transactional(Transactional.TxType.SUPPORTS)
@Singleton
class Admin implements Administration {

  /**
   * Identifier of the administration component in Silverpeas. It identifies any administrative tool
   * or service belonging to the administration component. It can be then passed where an identifier
   * of a component or of an application instance (named also component instance) is expected.
   * </p>
   * Each administrative tool have the same identifier and this identifier refers the administration
   * component.
   */
  private static final String PARAM_MSG_KEY = "root.MSG_GEN_PARAM_VALUE";
  private static final String START_SERVER_MSG = "Admin.startServer";

  // Divers
  private static final Object semaphore = new Object();
  private static boolean delUsersOnDiffSynchro = true;
  private static boolean shouldFallbackGroupNames = true;
  private static boolean shouldFallbackUserLogins = false;
  private static String m_groupSynchroCron = "";
  private static String m_domainSynchroCron = "";
  // Helpers
  private static final GroupProfileInstManager groupProfileManager = new GroupProfileInstManager();
  private static SpaceInstanciator spaceInstanciator = null;
  // Entreprise client space Id
  private static int m_nEntrepriseClientSpaceId = 0;
  private static String administratorMail = null;
  private static String m_sDAPIGeneralAdminId = null;
  // Cache management
  private static final AdminCache cache = new AdminCache();
  private static SynchroGroupScheduler groupSynchroScheduler = null;
  private static SynchroDomainScheduler domainSynchroScheduler = null;
  private static SettingBundle roleMapping = null;
  private static boolean useProfileInheritance = false;
  private static transient boolean cacheLoaded = false;

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
  private ComponentInstManager componentManager;
  @Inject
  private SpaceProfileInstManager spaceProfileManager;
  @Inject
  private SpaceEventNotifier spaceEventNotifier;

  private void setup() {
    // Load silverpeas admin resources
    SettingBundle resources = ResourceLocator.getSettingBundle("org.silverpeas.admin.admin");
    roleMapping = ResourceLocator.getSettingBundle("org.silverpeas.admin.roleMapping");
    useProfileInheritance = resources.getBoolean("UseProfileInheritance", false);
    m_nEntrepriseClientSpaceId = resources.getInteger("EntrepriseClientSpaceId", 0);
    administratorMail = resources.getString("AdministratorEMail", null);
    m_sDAPIGeneralAdminId = resources.getString("DAPIGeneralAdminId");
    final ScheduledDBReset scheduledDBReset = new ScheduledDBReset();
    scheduledDBReset.initialize(resources.getString("DBConnectionResetScheduler", ""));

    shouldFallbackGroupNames = resources.getBoolean("FallbackGroupNames", true);
    shouldFallbackUserLogins = resources.getBoolean("FallbackUserLogins", false);
    m_domainSynchroCron = resources.getString("DomainSynchroCron", "* 4 * * *");
    m_groupSynchroCron = resources.getString("GroupSynchroCron", "* 5 * * *");
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
    // Init tree cache
    synchronized (Admin.class) {
      if (!cacheLoaded) {
        reloadCache();
      }
    }
  }

  @Override
  public void reloadCache() {
    cache.resetCache();
    TreeCache.clearCache();
    GroupCache.clearCache();
    try {

      List<SpaceInstLight> spaces =
          spaceManager.getAllSpaces(DomainDriverManagerProvider.getCurrentDomainDriverManager());
      for (SpaceInstLight space : spaces) {
        addSpaceInTreeCache(space, false);
      }

    } catch (Exception e) {
      SilverTrace.error("admin", "Constructor", "ERROR_WHEN_INITIALIZING_ADMIN", e);
    }
    cacheLoaded = true;
  }

  // -------------------------------------------------------------------------
  // Start Server actions
  // -------------------------------------------------------------------------
  @Override
  public void startServer() {
    // init synchronization of domains
    List<String> synchroDomainIds = new ArrayList<>();
    DomainDriverManager ddm = DomainDriverManagerProvider.getCurrentDomainDriverManager();
    Domain[] domains = null;
    try {
      domains = ddm.getAllDomains();
    } catch (AdminException e) {
      SilverTrace.error("admin", START_SERVER_MSG,
          "admin.CANT_LOAD_DOMAINS_DURING_INITIALIZATION", e);
    }
    if (domains != null) {
      for (Domain domain : domains) {
        DomainDriver synchroDomain;
        try {
          synchroDomain = ddm.getDomainDriver(Integer.parseInt(domain.getId()));
          if (synchroDomain != null && synchroDomain.isSynchroThreaded()) {
            synchroDomainIds.add(domain.getId());
          }
        } catch (Exception e) {
          SilverTrace.error("admin", START_SERVER_MSG,
              "admin.CANT_LOAD_DOMAIN_DURING_INITIALIZATION", "domainId = " + domain.getId(), e);
        }
      }
    }
    domainSynchroScheduler = new SynchroDomainScheduler();
    domainSynchroScheduler.initialize(m_domainSynchroCron, synchroDomainIds);

    // init synchronization of groups
    Group[] groups = null;
    try {
      groups = getSynchronizedGroups();
    } catch (AdminException e) {
      SilverTrace.error("admin", START_SERVER_MSG,
          "admin.CANT_LOAD_SYNCHRONIZED_GROUPS_DURING_INITIALIZATION", e);
    }
    List<String> synchronizedGroupIds = new ArrayList<>();
    if (groups != null) {
      for (Group group : groups) {
        if (group.isSynchronized()) {
          synchronizedGroupIds.add(group.getId());
        }
      }
    }
    groupSynchroScheduler = new SynchroGroupScheduler();
    groupSynchroScheduler.initialize(m_groupSynchroCron, synchronizedGroupIds);
  }

  private void addSpaceInTreeCache(SpaceInstLight space, boolean addSpaceToSuperSpace)
      throws NumberFormatException, AdminException {
    Space spaceInCache = new Space();
    spaceInCache.setSpace(space);
    List<ComponentInstLight> components = componentManager.getComponentsInSpace(space.getLocalId());
    spaceInCache.setComponents(components);

    List<SpaceInstLight> subSpaces = getSubSpaces(space.getId());

    spaceInCache.setSubspaces(subSpaces);
    TreeCache.addSpace(space.getLocalId(), spaceInCache);

    for (SpaceInstLight subSpace : subSpaces) {
      addSpaceInTreeCache(subSpace, false);
    }

    if (addSpaceToSuperSpace) {
      if (!space.isRoot()) {
        TreeCache.addSubSpace(Integer.parseInt(space.getFatherId()), space);
      }
    }
  }

  // -------------------------------------------------------------------------
  // SPACE RELATED FUNCTIONS
  // -------------------------------------------------------------------------
  @Override
  public String getGeneralSpaceId() {
    return SpaceInst.SPACE_KEY_PREFIX + m_nEntrepriseClientSpaceId;
  }

  @Override
  public void createSpaceIndex(int spaceId) {
    try {
      SpaceInstLight space = getSpaceInstLight(spaceId);
      createSpaceIndex(space);
    } catch (AdminException e) {
      SilverTrace.error(MODULE_ADMIN, "admin.createSpaceIndex", PARAM_MSG_KEY,
          "spaceId = " + spaceId);
    }
  }

  @Override
  public void createSpaceIndex(SpaceInstLight spaceInst) {
    // Index the space
    String spaceId = spaceInst.getId();
    FullIndexEntry indexEntry = new FullIndexEntry("Spaces", "Space", spaceId);
    indexEntry.setTitle(spaceInst.getName());
    indexEntry.setPreView(spaceInst.getDescription());
    indexEntry.setCreationUser(String.valueOf(spaceInst.getCreatedBy()));
    IndexEngineProxy.addIndexEntry(indexEntry);
  }

  @Override
  public void deleteSpaceIndex(SpaceInst spaceInst) {

    String spaceId = spaceInst.getId();
    FullIndexEntry indexEntry = new FullIndexEntry("Spaces", "Space", spaceId);
    IndexEngineProxy.removeIndexEntry(indexEntry.getPK());
  }

  @Override
  public String addSpaceInst(String userId, SpaceInst spaceInst) throws AdminException {
    Connection connectionProd = null;
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.
        getCurrentDomainDriverManager();
    domainDriverManager.startTransaction(false);
    try {

      connectionProd = openConnection(false);

      // Open the connections with auto-commit to false
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
      spaceManager.createSpaceInst(spaceInst, domainDriverManager);
      // put new space in cache
      cache.opAddSpace(getSpaceInstById(spaceInst.getLocalId()));

      // Instantiate the components
      ArrayList<ComponentInst> alCompoInst = spaceInst.getAllComponentsInst();
      for (ComponentInst componentInst : alCompoInst) {
        componentInst.setDomainFatherId(spaceInst.getId());
        addComponentInst(userId, componentInst, false);
      }

      // commit the transactions
      domainDriverManager.commit();
      connectionProd.commit();

      SpaceInstLight space = getSpaceInstLight(spaceInst.getLocalId());
      addSpaceInTreeCache(space, true);

      // indexation de l'espace

      createSpaceIndex(space);

      return spaceInst.getId();
    } catch (Exception e) {
      try {
        // Roll back the transactions
        domainDriverManager.rollback();
        connectionProd.rollback();
        cache.resetCache();
      } catch (Exception e1) {
        SilverTrace.error(MODULE_ADMIN, "Admin.addSpaceInst", "root.EX_ERR_ROLLBACK", e1);
      }
      throw new AdminException("Admin.addSpaceInst", SilverpeasException.ERROR,
          "admin.EX_ERR_ADD_SPACE", "space name : '" + spaceInst.getName() + "'", e);
    } finally {
      // close connection
      domainDriverManager.releaseOrganizationSchema();
      DBUtil.close(connectionProd);
    }
  }

  @Override
  public String deleteSpaceInstById(String userId, String spaceId, boolean definitive) throws
      AdminException {
    return deleteSpaceInstById(userId, spaceId, true, definitive);
  }

  @Override
  public String deleteSpaceInstById(String userId, String spaceId, boolean startNewTransaction,
      boolean definitive) throws AdminException {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.getDomainDriverManager();
    try {
      if (startNewTransaction) {
        domainDriverManager.startTransaction(false);
      }

      // Convert the client id in driver id
      int driverSpaceId = getDriverSpaceId(spaceId);

      // Get the space to delete
      SpaceInst spaceInst = getSpaceInstById(driverSpaceId);

      if (!definitive) {
        // Update the space in tables
        spaceManager.sendSpaceToBasket(domainDriverManager, spaceInst, userId);

        // delete all profiles (space, components and subspaces)
        deleteSpaceProfiles(spaceInst);

        // notify logical deletion
        notifyOnSpaceLogicalDeletion(spaceId);
      } else {
        // Get all the sub-spaces
        String[] subSpaceIds = getAllSubSpaceIds(spaceId);

        // Delete subspaces
        for (String subSpaceid : subSpaceIds) {
          deleteSpaceInstById(userId, subSpaceid, false, true);
        }

        // Delete subspaces already in bin
        List<SpaceInstLight> removedSpaces = getRemovedSpaces();
        for (SpaceInstLight removedSpace : removedSpaces) {
          if (String.valueOf(driverSpaceId).equals(removedSpace.getFatherId())) {
            deleteSpaceInstById(userId, removedSpace.getId(), false, true);
          }
        }

        // delete the space profiles instance
        for (int nI = 0; nI < spaceInst.getNumSpaceProfileInst(); nI++) {
          deleteSpaceProfileInst(spaceInst.getSpaceProfileInst(nI).getId(), false);
        }

        // Delete the components
        ArrayList<ComponentInst> alCompoInst = spaceInst.getAllComponentsInst();
        for (ComponentInst anAlCompoInst : alCompoInst) {
          deleteComponentInst(userId, getClientComponentId(anAlCompoInst), true, false);
        }

        // Delete the components already in bin
        List<ComponentInstLight> removedComponents = getRemovedComponents();
        for (ComponentInstLight removedComponent : removedComponents) {
          if (spaceId.equals(removedComponent.getDomainFatherId())) {
            deleteComponentInst(userId, removedComponent.getId(), true, false);
          }
        }
        // Delete the space in tables
        spaceManager.deleteSpaceInst(spaceInst, domainDriverManager);
      }

      if (startNewTransaction) {
        domainDriverManager.commit();
      }
      cache.opRemoveSpace(spaceInst);
      TreeCache.removeSpace(driverSpaceId);
      // desindexation de l'espace
      deleteSpaceIndex(spaceInst);
      return spaceId;
    } catch (Exception e) {
      // Roll back the transactions
      if (startNewTransaction) {
        rollback();
      }
      throw new AdminException("Admin.deleteSpaceInstById", SilverpeasException.ERROR,
          "admin.EX_ERR_DELETE_SPACE",
          "user Id : '" + userId + "', space Id : '" + spaceId + "'", e);
    } finally {
      if (startNewTransaction) {
        domainDriverManager.releaseOrganizationSchema();
      }
    }
  }

  private void notifyOnSpaceLogicalDeletion(String spaceId) throws AdminException {
    // notify of space logical deletion
    SpaceInst spaceInst = getSpaceInstById(spaceId);
    spaceEventNotifier.notifyEventOn(ResourceEvent.Type.REMOVING, spaceInst, spaceInst);

    // notify of direct sub spaces logical deletion too
    List<SpaceInstLight> spaces = TreeCache.getSubSpaces(getDriverSpaceId(spaceId));
    for (SpaceInstLight space : spaces) {
      notifyOnSpaceLogicalDeletion(space.getId());
    }
  }

  private void deleteSpaceProfiles(SpaceInst spaceInst) throws AdminException {
    // delete the space profiles
    for (int nI = 0; nI < spaceInst.getNumSpaceProfileInst(); nI++) {
      deleteSpaceProfileInst(spaceInst.getSpaceProfileInst(nI).getId(), false);
    }

    // delete the components profiles
    List<ComponentInst> components = spaceInst.getAllComponentsInst();
    for (ComponentInst component : components) {
      for (int p = 0; p < component.getNumProfileInst(); p++) {
        if (!component.getProfileInst(p).isInherited()) {
          deleteProfileInst(component.getProfileInst(p).getId(), false);
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
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.getDomainDriverManager();
    domainDriverManager.startTransaction(false);
    try {
      // Start transaction

      // Convert the client id in driver id
      int driverSpaceId = getDriverSpaceId(spaceId);
      // update data in database
      spaceManager.removeSpaceFromBasket(domainDriverManager, driverSpaceId);

      // force caches to be refreshed
      cache.removeSpaceInst(driverSpaceId);
      TreeCache.removeSpace(driverSpaceId);

      // Get the space and put it in the cache
      SpaceInst spaceInst = getSpaceInstById(driverSpaceId);
      // set superspace profiles to space
      if (useProfileInheritance && !spaceInst.isInheritanceBlocked() && !spaceInst.isRoot()) {
        updateSpaceInheritance(spaceInst, false);
      }
      domainDriverManager.commit();
      // indexation de l'espace

      createSpaceIndex(driverSpaceId);
      // reset space and eventually subspace
      cache.opAddSpace(spaceInst);
      addSpaceInTreeCache(getSpaceInstLight(driverSpaceId), true);
    } catch (Exception e) {
      rollback();
      throw new AdminException("Admin.restoreSpaceFromBasket", SilverpeasException.ERROR,
          "admin.EX_ERR_RESTORE_SPACE_FROM_BASKET", "spaceId = " + spaceId);
    } finally {
      domainDriverManager.releaseOrganizationSchema();
    }
  }

  @Override
  public SpaceInst getSpaceInstById(String spaceId) throws AdminException {
    try {
      return getSpaceInstById(getDriverSpaceId(spaceId));
    } catch (Exception e) {
      throw new AdminException("Admin.getSpaceInstById", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_SPACE", " space Id : '" + spaceId + "'", e);
    }
  }

  /**
   * Get the space instance with the given space id
   *
   * @param spaceId client space id
   * @return Space information as SpaceInst object
   */
  private SpaceInst getSpaceInstById(int spaceId)
      throws AdminException {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.getDomainDriverManager();
    try {
      SpaceInst spaceInst = cache.getSpaceInst(spaceId);
      if (spaceInst == null) {
        // Get space instance
        spaceInst = spaceManager.getSpaceInstById(domainDriverManager, spaceId);
        if (spaceInst != null) {
          // Store the spaceInst in cache
          cache.putSpaceInst(spaceInst);
        }
      }
      return spaceManager.copy(spaceInst);
    } catch (Exception e) {
      throw new AdminException("Admin.getSpaceInstById", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_SPACE", " space Id : '" + spaceId + "'", e);
    }
  }

  @Override
  public SpaceInst getPersonalSpace(String userId) throws AdminException {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.getDomainDriverManager();
    return spaceManager.getPersonalSpace(domainDriverManager, userId);
  }

  @Override
  public String[] getAllSubSpaceIds(String domainFatherId) throws AdminException {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.getDomainDriverManager();
    try {
      // get all sub space ids
      String[] asDriverSpaceIds = spaceManager.getAllSubSpaceIds(domainDriverManager,
          getDriverSpaceId(domainFatherId));
      // Convert all the driver space ids in client space ids
      asDriverSpaceIds = getClientSpaceIds(asDriverSpaceIds);

      return asDriverSpaceIds;
    } catch (Exception e) {
      throw new AdminException("Admin.getAllSubSpaceIds", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_ALL_SUBSPACE_IDS", " father space Id : '" + domainFatherId + "'", e);
    }
  }

  @Override
  public String updateSpaceInst(SpaceInst spaceInstNew) throws AdminException {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.getDomainDriverManager();

    domainDriverManager.startTransaction(false);
    try {
      SpaceInst oldSpace = getSpaceInstById(spaceInstNew.getId());
      // Open the connections with auto-commit to false
      // Update the space in tables
      spaceManager.updateSpaceInst(domainDriverManager, spaceInstNew);
      if (useProfileInheritance && (oldSpace.isInheritanceBlocked() != spaceInstNew.
          isInheritanceBlocked())) {
        updateSpaceInheritance(oldSpace, spaceInstNew.isInheritanceBlocked());
      }
      // commit the transactions
      domainDriverManager.commit();
      cache.opUpdateSpace(spaceInstNew);
      TreeCache.getSpaceInstLight(spaceInstNew.getLocalId()).setInheritanceBlocked(spaceInstNew.
          isInheritanceBlocked());
      // Update space in TreeCache
      SpaceInstLight spaceLight = spaceManager.getSpaceInstLightById(domainDriverManager,
          getDriverSpaceId(spaceInstNew.getId()));
      spaceLight.setInheritanceBlocked(spaceInstNew.isInheritanceBlocked());
      TreeCache.updateSpace(spaceLight);

      // indexation de l'espace

      createSpaceIndex(spaceLight);
      return spaceInstNew.getId();
    } catch (Exception e) {
      rollback();
      throw new AdminException("Admin.updateSpaceInst", SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_SPACE", "space Id : '" + spaceInstNew.getId() + "'", e);
    } finally {
      domainDriverManager.releaseOrganizationSchema();
    }
  }

  @Override
  public void updateSpaceOrderNum(String spaceId, int orderNum) throws AdminException {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.getDomainDriverManager();
    try {
      int driverSpaceId = getDriverSpaceId(spaceId);
      // Open the connections with auto-commit to false
      domainDriverManager.startTransaction(false);
      // Update the space in tables
      spaceManager.updateSpaceOrder(domainDriverManager, driverSpaceId, orderNum);
      // commit the transactions
      domainDriverManager.commit();
      cache.opUpdateSpace(spaceManager.getSpaceInstById(domainDriverManager, driverSpaceId));

      // Updating TreeCache
      SpaceInstLight space = TreeCache.getSpaceInstLight(driverSpaceId);
      // Update space order
      space.setOrderNum(orderNum);
      if (!space.isRoot()) {
        // Update brothers sort in TreeCache
        TreeCache.setSubspaces(getDriverSpaceId(space.getFatherId()),
            getSubSpaces(space.getFatherId()));
      }
    } catch (Exception e) {
      rollback();
      throw new AdminException("Admin.updateSpaceOrderNum", SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_SPACE", "space Id : '" + spaceId + "'", e);
    } finally {
      domainDriverManager.releaseOrganizationSchema();
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
          deleteSpaceProfileInst(profile.getId(), false);
        }
      } else {
        // Héritage des droits de l'espace
        // 1 - suppression des droits spécifiques du sous espace
        List<SpaceProfileInst> profiles = space.getProfiles();
        for (SpaceProfileInst profile : profiles) {
          if (profile != null && !profile.isManager()) {
            deleteSpaceProfileInst(profile.getId(), false);
          }
        }
        if (!space.isRoot()) {
          // 2 - affectation des droits de l'espace au sous espace
          setSpaceProfilesToSubSpace(space, null, true, false);
        }
      }
    } catch (AdminException e) {
      rollback();
      throw new AdminException("Admin.updateComponentInst", SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_SPACE", "spaceId = " + space.getId(), e);
    }
  }

  @Override
  public boolean isSpaceInstExist(String spaceId) throws AdminException {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.getDomainDriverManager();
    try {
      return spaceManager.isSpaceInstExist(domainDriverManager, getDriverSpaceId(spaceId));
    } catch (AdminException e) {
      throw new AdminException("Admin.isSpaceInstExist", SilverpeasException.ERROR,
          "admin.EX_ERR_IS_SPACE_EXIST", "space Id : '" + spaceId + "'", e);
    }
  }

  @Override
  public String[] getAllRootSpaceIds() throws AdminException {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.getDomainDriverManager();
    try {
      String[] driverSpaceIds = spaceManager.getAllRootSpaceIds(domainDriverManager);
      // Convert all the driver space ids in client space ids
      driverSpaceIds = getClientSpaceIds(driverSpaceIds);
      return driverSpaceIds;
    } catch (Exception e) {
      throw new AdminException("Admin.getAllSpaceIds", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_ALL_SPACE_IDS", e);
    }
  }

  @Override
  public List<SpaceInstLight> getPathToComponent(String componentId) throws AdminException {
    List<SpaceInstLight> path = new ArrayList<>(0);
    ComponentInstLight component = getComponentInstLight(componentId);
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
      while (!space.isRoot()) {
        String fatherId = space.getFatherId();
        space = getSpaceInstLight(getDriverSpaceId(fatherId));
        path.add(0, space);
      }
    }
    return path;
  }

  @Override
  public String[] getAllSpaceIds() throws AdminException {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.getDomainDriverManager();
    try {
      String[] driverSpaceIds = spaceManager.getAllSpaceIds(domainDriverManager);
      // Convert all the driver space ids in client space ids
      driverSpaceIds = getClientSpaceIds(driverSpaceIds);
      return driverSpaceIds;
    } catch (Exception e) {
      throw new AdminException("Admin.getAllSpaceIds", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_ALL_SPACE_IDS", e);
    }
  }

  @Override
  public List<SpaceInstLight> getRemovedSpaces() throws AdminException {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.getDomainDriverManager();
    try {
      return spaceManager.getRemovedSpaces(domainDriverManager);
    } catch (Exception e) {
      throw new AdminException("Admin.getRemovedSpaces", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_REMOVED_SPACES", e);
    }
  }

  @Override
  public List<ComponentInstLight> getRemovedComponents() throws AdminException {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.getDomainDriverManager();
    try {
      return componentManager.getRemovedComponents(domainDriverManager);
    } catch (Exception e) {
      throw new AdminException("Admin.getRemovedComponents", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_REMOVED_COMPONENTS", e);
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
      throw new AdminException("Admin.getSpaceNames", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_SPACE_NAMES", e);
    }
  }

  @Override
  public Map<String, SpaceTemplate> getAllSpaceTemplates() {
    return getSpaceInstanciator().getAllSpaceTemplates();
  }

  @Override
  public SpaceInst getSpaceInstFromTemplate(String templateName) {
    return getSpaceInstanciator().getSpaceToInstanciate(templateName);
  }

  // -------------------------------------------------------------------------
  // COMPONENT RELATED FUNCTIONS
  // -------------------------------------------------------------------------

  @Override
  public Map<String, WAComponent> getAllComponents() {
    return componentRegistry.getAllWAComponents();
  }

  @Override
  public ComponentInst getComponentInst(String sClientComponentId) throws AdminException {
    try {
      ComponentInst componentInst = getComponentInst(getDriverComponentId(sClientComponentId),
          null);
      componentInst.setDomainFatherId(getClientSpaceId(componentInst.getDomainFatherId()));
      return componentInst;
    } catch (Exception e) {
      throw new AdminException("Admin.getComponentInst", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_COMPONENT", "component Id: '" + sClientComponentId + "'", e);
    }
  }

  @Override
  public ComponentInstLight getComponentInstLight(String componentId) throws AdminException {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.getDomainDriverManager();
    try {
      int driverComponentId = getDriverComponentId(componentId);
      return componentManager.getComponentInstLight(domainDriverManager, driverComponentId);
    } catch (Exception e) {
      throw new AdminException("Admin.getComponentInstLight", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_COMPONENT", "component Id: '" + componentId + "'", e);
    }
  }

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
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.getDomainDriverManager();
    String driverComponentId;
    try {
      // Get the component instance
      ComponentInst componentInst = cache.getComponentInst(componentId);

      if (componentInst == null) {
        // Get component instance from database
        componentInst = componentManager.getComponentInst(domainDriverManager,
            componentId, fatherDriverSpaceId);
        // Store component instance in cache
        cache.putComponentInst(componentInst);
      }
      return componentManager.copy(componentInst);
    } catch (Exception e) {
      throw new AdminException("Admin.getComponentInst", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_COMPONENT", "component Id: '" + componentId + "'", e);
    }
  }

  @Override
  public List<Parameter> getComponentParameters(String componentId) {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.getDomainDriverManager();
    try {
      return componentManager.getParameters(domainDriverManager, getDriverComponentId(componentId));
    } catch (Exception e) {
      SilverTrace.error(MODULE_ADMIN, "Admin.getComponentParameters",
          "admin.EX_ERR_GET_COMPONENT_PARAMS", "sComponentId: '" + componentId + "'", e);
      return Collections.emptyList();
    }
  }

  @Override
  public String getComponentParameterValue(String componentId, String parameterName) {
    try {
      ComponentInst component = getComponentInst(componentId);
      if (component == null) {
        SilverTrace.error(MODULE_ADMIN, "Admin.getComponentParameterValue",
            "admin.EX_ERR_GET_COMPONENT_PARAMS", "Component not found - sComponentId: '"
            + componentId + "'");
        return StringUtil.EMPTY;
      }
      return component.getParameterValue(parameterName);
    } catch (Exception e) {
      SilverTrace.error(MODULE_ADMIN, "Admin.getComponentParameterValue",
          "admin.EX_ERR_GET_COMPONENT_PARAMS", "sComponentId: '" + componentId + "'", e);
      return "";
    }
  }

  @Override
  public void restoreComponentFromBasket(String componentId) throws AdminException {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.getDomainDriverManager();

    try {
      // Start transaction
      domainDriverManager.startTransaction(false);

      // update data in database
      componentManager.restoreComponentFromBasket(domainDriverManager, getDriverComponentId(
          componentId));

      // Get the component and put it in the cache
      ComponentInst componentInst = getComponentInst(componentId);

      if (useProfileInheritance && !componentInst.isInheritanceBlocked()) {
        // inherits profiles from space
        setSpaceProfilesToComponent(componentInst, null);
      }
      domainDriverManager.commit();
      cache.opUpdateComponent(componentInst);
      ComponentInstLight component = getComponentInstLight(componentId);
      TreeCache.addComponent(getDriverComponentId(componentId), component,
          getDriverSpaceId(component.getDomainFatherId()));
      createComponentIndex(component);
    } catch (Exception e) {
      rollback();
      throw new AdminException("Admin.restoreComponentFromBasket", SilverpeasException.ERROR,
          "admin.EX_ERR_RESTORE_COMPONENT_FROM_BASKET", "componentId = " + componentId);
    } finally {
      domainDriverManager.releaseOrganizationSchema();
    }
  }

  @Override
  public void createComponentIndex(String componentId) {
    try {
      ComponentInstLight component = getComponentInstLight(componentId);
      createComponentIndex(component);
    } catch (AdminException e) {
      SilverTrace.error(MODULE_ADMIN, "Admin.createComponentIndex",
          "admin.EX_ERR_GET_COMPONENT_PARAMS", "componentId: '" + componentId + "'", e);
    }
  }

  @Override
  public void createComponentIndex(ComponentInstLight componentInst) {
    if (componentInst != null) {
      // Index the component
      String componentId = componentInst.getId();
      FullIndexEntry indexEntry = new FullIndexEntry("Components", "Component", componentId);
      indexEntry.setTitle(componentInst.getLabel());
      indexEntry.setPreView(componentInst.getDescription());
      indexEntry.setCreationUser(Integer.toString(componentInst.getCreatedBy()));
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  /**
   * Delete the index for the specified component.
   *
   * @param componentId
   */
  private void deleteComponentIndex(String componentId) {
    FullIndexEntry indexEntry = new FullIndexEntry("Components", "Component", componentId);
    IndexEngineProxy.removeIndexEntry(indexEntry.getPK());
  }

  private void deleteComponentData(String componentId) {
    // deleting all files associated to this component
    FileRepositoryManager.deleteAbsolutePath(null, componentId, "");

    // deleting index files
    IndexFileManager.deleteComponentIndexFolder(componentId);
  }

  @Override
  public String addComponentInst(String sUserId, ComponentInst componentInst)
      throws AdminException, QuotaException {
    return addComponentInst(sUserId, componentInst, true);
  }

  @Override
  public String addComponentInst(String userId, ComponentInst componentInst,
      boolean startNewTransaction) throws AdminException, QuotaException {
    Connection connectionProd = null;
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.getDomainDriverManager();
    try {
      connectionProd = openConnection(false);

      if (startNewTransaction) {
        // Open the connections with auto-commit to false
        domainDriverManager.startTransaction(false);
      }

      // Get the father space inst
      SpaceInst spaceInstFather = getSpaceInstById(componentInst.getDomainFatherId());

      // Verify the component space quota
      SpaceServiceProvider.getComponentSpaceQuotaService().verify(
          ComponentSpaceQuotaKey.from(spaceInstFather));

      // Create the component instance
      componentManager.createComponentInst(componentInst,
          domainDriverManager, spaceInstFather.getLocalId());

      // Add the component to the space
      spaceInstFather.addComponentInst(componentInst);

      // Instantiate the component
      String componentName = componentInst.getName();
      String componentId = componentInst.getId();

      String[] asCompoNames = {componentName};
      String[] asCompoIds = {componentId};

      ComponentInstancePostConstruction.get(componentName)
          .ifPresent(c -> c.postConstruct(componentId));

      if (isContentManagedComponent(componentName)) {
        // Create the manager objects
        ContainerManager containerManager = new ContainerManager();
        ContentManager contentManager = new ContentManager();

        // Call the register functions
        containerManager.registerNewContainerInstance(connectionProd, componentId, "containerPDC",
            componentName);
        contentManager.registerNewContentInstance(connectionProd, componentId, "containerPDC",
            componentName);
      }

      if (useProfileInheritance && !componentInst.isInheritanceBlocked()) {
        // inherits profiles from space
        setSpaceProfilesToComponent(componentInst, spaceInstFather);
      }

      // commit the transactions
      if (startNewTransaction) {
        domainDriverManager.commit();
      }
      connectionProd.commit();
      cache.opAddComponent(componentInst);

      ComponentInstLight component = getComponentInstLight(componentId);
      TreeCache.addComponent(component.getLocalId(), component,
          getDriverSpaceId(spaceInstFather.getId()));

      // indexation du composant
      createComponentIndex(component);

      return componentId;
    } catch (Exception e) {
      try {
        if (startNewTransaction) {
          domainDriverManager.rollback();
        }
        connectionProd.rollback();
      } catch (Exception e1) {
        SilverTrace.error(MODULE_ADMIN, "Admin.addComponentInst", "root.EX_ERR_ROLLBACK", e1);
      }
      if (e instanceof QuotaException) {
        throw (QuotaException) e;
      }
      throw new AdminException("Admin.addComponentInst", SilverpeasException.ERROR,
          "admin.EX_ERR_ADD_COMPONENT", "component name: '" + componentInst.getName() + "'", e);
    } finally {
      if (startNewTransaction) {
        domainDriverManager.releaseOrganizationSchema();
      }
      DBUtil.close(connectionProd);
    }
  }

  boolean isContentManagedComponent(String componentName) {
    return "expertLocator".equals(componentName) || "questionReply".equals(componentName)
        || "whitePages".equals(componentName) || "kmelia".equals(componentName)
        || "kmax".equals(componentName) || "survey".equals(componentName)
        || "toolbox".equals(componentName) || "quickinfo".equals(componentName)
        || "almanach".equals(componentName) || "quizz".equals(componentName) || "forums".equals(
            componentName) || "pollingStation".equals(componentName) || "bookmark".equals(
            componentName) || "chat".equals(componentName) || "infoLetter".equals(componentName)
        || "webSites".equals(componentName) || "gallery".equals(componentName) || "blog".equals(
            componentName);
  }

  @Override
  public String deleteComponentInst(String userId, String componentId, boolean definitive) throws
      AdminException {
    return deleteComponentInst(userId, componentId, definitive, true);
  }

  /**
   * Deletes the given component instance in Silverpeas
   *
   * @param userId the unique identifier of the user requesting the deletion.
   * @param componentId the client identifier of the component instance (for a kmelia instance of id
   * 666, the client identifier of the instance is kmelia666)
   * @param definitive is the component instance deletion is definitive? If not, the component
   * instance is moved into the bin.
   * @param startNewTransaction is the deletion has to occur within a new transaction?
   * @return the client component instance identifier.
   * @throws AdminException if an error occurs while deleting the
   * component instance.
   */
  private String deleteComponentInst(String userId, String componentId, boolean definitive,
      boolean startNewTransaction) throws AdminException {
    Connection connectionProd = null;
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.getDomainDriverManager();
    try {
      if (startNewTransaction) {
        // Open the connections with auto-commit to false
        domainDriverManager.startTransaction(false);
      }

      // Convert the client id in driver id
      int sDriverComponentId = getDriverComponentId(componentId);

      // Get the component to delete
      ComponentInst componentInst = getComponentInst(sDriverComponentId, null);

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
          deleteProfileInst(componentInst.getProfileInst(nI).getId(), false);
        }
        componentManager.sendComponentToBasket(domainDriverManager, componentInst, userId);
      } else {
        connectionProd = openConnection(false);
        // Uninstantiate the components
        String componentName = componentInst.getName();

        ComponentInstancePreDestruction.get(componentName)
            .ifPresent(c -> c.preDestroy(componentId));

        ServiceProvider.getAllServices(ComponentInstanceDeletion.class).stream()
            .forEach(service -> service.delete(componentId));

        // delete the profiles instance
        for (int nI = 0; nI < componentInst.getNumProfileInst(); nI++) {
          deleteProfileInst(componentInst.getProfileInst(nI).getId(), false);
        }

        if (isContentManagedComponent(componentName)) {
          // Create the manager objects
          ContainerManager containerManager = new ContainerManager();
          ContentManager contentManager = new ContentManager();

          // Call the unregister functions
          containerManager.unregisterNewContainerInstance(connectionProd, componentId,
              "containerPDC", componentName);
          contentManager.unregisterNewContentInstance(connectionProd, componentId, "containerPDC",
              componentName);
        }

        // commit the deletion of all resources related to the component instance to delete
        connectionProd.commit();

        // Delete the component
        componentManager.deleteComponentInst(componentInst, domainDriverManager);
      }

      // commit the deletion of the component instance itself
      if (startNewTransaction) {
        domainDriverManager.commit();
      }
      cache.opRemoveComponent(componentInst);
      TreeCache.removeComponent(getDriverSpaceId(sFatherClientId), componentId);

      // unindex component
      deleteComponentIndex(componentId);

      if (definitive) {
        // delete definitively data stored on file server
        deleteComponentData(componentId);
      }

      return componentId;
    } catch (Exception e) {
      try {
        // Roll back the transactions
        if (startNewTransaction) {
          domainDriverManager.rollback();
        }
        if (connectionProd != null) {
          connectionProd.rollback();
        }
      } catch (Exception e1) {
        SilverTrace.error(MODULE_ADMIN, "Admin.deleteComponentInst",
            "root.EX_ERR_ROLLBACK", e1);
      }
      throw new AdminException("Admin.deleteComponentInst", SilverpeasException.ERROR,
          "admin.EX_ERR_DELETE_COMPONENT",
          "component Id: '" + componentId + "'", e);
    } finally {
      if (startNewTransaction) {
        domainDriverManager.releaseOrganizationSchema();
      }
      DBUtil.close(connectionProd);
    }
  }

  @Override
  public void updateComponentOrderNum(String componentId, int orderNum) throws AdminException {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.getDomainDriverManager();
    try {
      int driverComponentId = getDriverComponentId(componentId);
      // Open the connections with auto-commit to false
      domainDriverManager.startTransaction(false);

      // Update the Component in tables
      componentManager.updateComponentOrder(domainDriverManager, driverComponentId, orderNum);
      domainDriverManager.commit();
      cache.opUpdateComponent(componentManager.getComponentInst(domainDriverManager,
          driverComponentId, null));
    } catch (Exception e) {
      rollback();
      throw new AdminException("Admin.updateComponentOrderNum", SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_COMPONENT", "Component Id : '" + componentId + "'", e);
    } finally {
      domainDriverManager.releaseOrganizationSchema();
    }
  }

  @Override
  public String updateComponentInst(ComponentInst component) throws AdminException {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.getDomainDriverManager();
    try {
      ComponentInst oldComponent = getComponentInst(component.getId());
      String componentClientId = getClientComponentId(oldComponent);
      // Open the connections with auto-commit to false
      domainDriverManager.startTransaction(false);

      // Convert the client space Id in driver space Id
      int sDriverComponentId = getDriverComponentId(component.getId());
      // Update the components in tables
      componentManager.updateComponentInst(domainDriverManager, oldComponent, component);

      // Update the inherited rights
      if (useProfileInheritance && (oldComponent.isInheritanceBlocked() != component.
          isInheritanceBlocked())) {
        updateComponentInheritance(oldComponent, component.isInheritanceBlocked());
      }
      // commit the transactions
      domainDriverManager.commit();
      cache.opUpdateComponent(component);
      TreeCache.getComponent(componentClientId).setInheritanceBlocked(component.
          isInheritanceBlocked());
      // indexation du composant
      createComponentIndex(componentClientId);

      return componentClientId;
    } catch (Exception e) {
      rollback();
      throw new AdminException("Admin.updateComponentInst", SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_COMPONENT", "component Id: '" + component.getId() + "'", e);
    } finally {
      domainDriverManager.releaseOrganizationSchema();
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
          deleteProfileInst(profile.getId(), false);
        }
      } else {
        // suppression des droits du composant
        List<ProfileInst> profiles = component.getProfiles();
        for (ProfileInst profile : profiles) {
          deleteProfileInst(profile.getId(), false);
        }
        // affectation des droits de l'espace
        setSpaceProfilesToComponent(component, null);
      }
    } catch (AdminException e) {
      rollback();
      throw new AdminException("Admin.updateComponentInst", SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_COMPONENT", "component Id: '" + component.getId() + "'", e);
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
          updateSpaceProfileInst(profile, null, startNewTransaction);
        } else {
          addSpaceProfileInst(profile, null, startNewTransaction);
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
    }

    // Retrieve superSpace local profile
    SpaceProfileInst profile = space.getSpaceProfileInst(profileName);
    if (profile != null) {
      if (subSpaceProfile == null) {
        subSpaceProfile = new SpaceProfileInst();
        subSpaceProfile.setName(profileName);
        subSpaceProfile.setInherited(true);
      }
      subSpaceProfile.addGroups(profile.getAllGroups());
      subSpaceProfile.addUsers(profile.getAllUsers());
    }

    // Retrieve superSpace inherited profile
    SpaceProfileInst inheritedProfile = space.getInheritedSpaceProfileInst(profileName);
    if (inheritedProfile != null) {
      if (subSpaceProfile == null) {
        subSpaceProfile = new SpaceProfileInst();
        subSpaceProfile.setName(profileName);
        subSpaceProfile.setInherited(true);
      }
      subSpaceProfile.addGroups(inheritedProfile.getAllGroups());
      subSpaceProfile.addUsers(inheritedProfile.getAllUsers());
    }

    if (subSpaceProfile != null) {
      subSpace.addSpaceProfileInst(subSpaceProfile);
    }
  }

  @Override
  public void setSpaceProfilesToComponent(ComponentInst component, SpaceInst space) throws
      AdminException {
    setSpaceProfilesToComponent(component, space, false);
  }

  @Override
  public void setSpaceProfilesToComponent(ComponentInst component, SpaceInst space,
      boolean startNewTransaction) throws AdminException {
    WAComponent waComponent = componentRegistry.getWAComponent(component.getName()).get();
    List<Profile> componentRoles = waComponent.getProfiles();

    if (space == null) {
      space = getSpaceInstById(component.getDomainFatherId());
    }

    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      if (startNewTransaction) {
        domainDriverManager.startTransaction(false);
      }

      for (Profile componentRole : componentRoles) {
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

        List<String> spaceRoles = componentRole2SpaceRoles(componentRole.getName(),
            component.getName());
        for (String spaceRole : spaceRoles) {
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

        if (StringUtil.isDefined(inheritedProfile.getId())) {
          updateProfileInst(inheritedProfile, null, false, null);
        } else {
          if (!inheritedProfile.isEmpty()) {
            addProfileInst(inheritedProfile, null, false);
          }
        }
      }

      if (startNewTransaction) {
        domainDriverManager.commit();
      }
    } catch (Exception e) {
      if (startNewTransaction) {
        rollback();
      }
      throw new AdminException("Admin.setSpaceProfilesToComponent", SilverpeasException.ERROR,
          "admin.EX_ERR_SET_PROFILES", e);
    } finally {
      if (startNewTransaction) {
        domainDriverManager.releaseOrganizationSchema();
      }
    }
  }

  @Override
  public void moveSpace(String spaceId, String fatherId) throws AdminException {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.getDomainDriverManager();

    if (isParent(getDriverSpaceId(spaceId), getDriverSpaceId(fatherId))) {
      // space cannot be moved in one of its descendants
      return;
    }

    int shortSpaceId = getDriverSpaceId(spaceId);
    int shortFatherId = -1;
    if (StringUtil.isDefined(fatherId)) {
      shortFatherId = getDriverSpaceId(fatherId);
    }
    boolean moveOnTop = shortFatherId == -1;

    try {
      SpaceInst space = getSpaceInstById(shortSpaceId);
      int shortOldSpaceId = getDriverSpaceId(space.getDomainFatherId());

      // Open the connections with auto-commit to false
      domainDriverManager.startTransaction(false);
      // move space in database
      spaceManager.moveSpace(domainDriverManager, shortSpaceId, shortFatherId);

      // set space in last rank
      spaceManager.updateSpaceOrder(domainDriverManager, shortSpaceId,
          getAllSubSpaceIds(fatherId).length);

      if (useProfileInheritance) {
        space = spaceManager.getSpaceInstById(domainDriverManager, shortSpaceId);

        // inherited rights must be removed but local rights are preserved
        List<SpaceProfileInst> inheritedProfiles = space.getInheritedProfiles();
        for (SpaceProfileInst profile : inheritedProfiles) {
          deleteSpaceProfileInst(profile.getId(), false);
        }

        if (!moveOnTop) {
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
          spaceManager.updateSpaceInst(domainDriverManager, space);
        }
      }

      // commit transaction
      domainDriverManager.commit();

      // reset caches
      cache.resetSpaceInst();
      TreeCache.removeSpace(shortSpaceId);
      TreeCache.setSubspaces(shortOldSpaceId,
          spaceManager.getSubSpaces(domainDriverManager, shortOldSpaceId));
      addSpaceInTreeCache(spaceManager.getSpaceInstLightById(domainDriverManager, shortSpaceId),
          false);
      if (!moveOnTop) {
        TreeCache.setSubspaces(shortFatherId,
            spaceManager.getSubSpaces(domainDriverManager, shortFatherId));
      }

    } catch (Exception e) {
      rollback();
      throw new AdminException("Admin.moveSpace", SilverpeasException.ERROR,
          "admin.EX_ERR_MOVE_Space", "spaceId = " + spaceId + ",  fatherId =" + fatherId, e);
    } finally {
      domainDriverManager.releaseOrganizationSchema();
    }
  }

  @Override
  public void moveComponentInst(String spaceId, String componentId, String idComponentBefore,
      ComponentInst[] componentInsts) throws AdminException {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.getDomainDriverManager();
    try {

      int sDriverComponentId = getDriverComponentId(componentId);
      // Convert the client space Id in driver space Id
      int sDriverSpaceId = getDriverSpaceId(spaceId);

      ComponentInst componentInst = getComponentInst(componentId);
      String oldSpaceId = componentInst.getDomainFatherId();
      // Open the connections with auto-commit to false
      domainDriverManager.startTransaction(false);
      // Update the components in tables
      componentManager.moveComponentInst(domainDriverManager, sDriverSpaceId, sDriverComponentId);
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
      // commit the transactions
      domainDriverManager.commit();
      // Remove component from the Cache
      cache.resetSpaceInst();
      cache.resetComponentInst();
      TreeCache.setComponents(getDriverSpaceId(oldSpaceId),
          componentManager.getComponentsInSpace(getDriverSpaceId(oldSpaceId)));
      TreeCache.setComponents(getDriverSpaceId(spaceId),
          componentManager.getComponentsInSpace(getDriverSpaceId(spaceId)));
    } catch (Exception e) {
      rollback();
      throw new AdminException("Admin.moveComponentInst", SilverpeasException.ERROR,
          "admin.EX_ERR_MOVE_COMPONENT",
          "spaceId = " + spaceId + " component Id: '" + componentId + " ", e);
    } finally {
      domainDriverManager.releaseOrganizationSchema();
    }
  }

  @Override
  public void setComponentPlace(String componentId, String idComponentBefore,
      ComponentInst[] m_BrothersComponents) throws AdminException {
    int orderNum = 0;
    int i;
    ComponentInst theComponent = getComponentInst(componentId);

    for (i = 0; i < m_BrothersComponents.length; i++) {
      if (idComponentBefore.equals(m_BrothersComponents[i].getId())) {
        theComponent.setOrderNum(orderNum);
        updateComponentOrderNum(theComponent.getId(), orderNum);
        orderNum++;
      }
      if (m_BrothersComponents[i].getOrderNum() != orderNum) {
        m_BrothersComponents[i].setOrderNum(orderNum);
        updateComponentOrderNum(m_BrothersComponents[i].getId(), orderNum);
      }
      orderNum++;
    }
    if (orderNum == i) {
      theComponent.setOrderNum(orderNum);
      updateComponentOrderNum(theComponent.getId(), orderNum);
    }
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
    return asProfiles.toArray(new String[asProfiles.size()]);
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
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    ProfileInst profileInst = cache.getProfileInst(sProfileId);
    if (profileInst == null) {
      profileInst = profileManager.getProfileInst(domainDriverManager, sProfileId);
      cache.putProfileInst(profileInst);
    }
    return profileInst;
  }

  @Override
  public List<ProfileInst> getProfilesByObject(String objectId, String objectType,
      String componentId) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    return profiledObjectManager.getProfiles(domainDriverManager,
        Integer.parseInt(objectId), objectType,
        getDriverComponentId(componentId));
  }

  @Override
  public String[] getProfilesByObjectAndUserId(int objectId, String objectType, String componentId,
      String userId) throws AdminException {
    List<String> groups = getAllGroupsOfUser(userId);
    return profiledObjectManager.getUserProfileNames(objectId, objectType,
        getDriverComponentId(componentId), Integer.parseInt(userId), groups);
  }

  @Override
  public boolean isObjectAvailable(String componentId, int objectId, String objectType,
      String userId) throws AdminException {
    return userId == null
        || getProfilesByObjectAndUserId(objectId, objectType, componentId, userId).length > 0;
  }

  @Override
  public String addProfileInst(ProfileInst profileInst) throws AdminException {
    return addProfileInst(profileInst, null, true);
  }

  @Override
  public String addProfileInst(ProfileInst profileInst, String userId) throws AdminException {
    return addProfileInst(profileInst, userId, true);
  }

  /**
   * Get the given profile instance from Silverpeas
   */
  private String addProfileInst(ProfileInst profileInst, String userId, boolean startNewTransaction)
      throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      if (startNewTransaction) {
        domainDriverManager.startTransaction(false);
      }
      int driverFatherId = getDriverComponentId(profileInst.getComponentFatherId());
      String sProfileId = profileManager.createProfileInst(profileInst, domainDriverManager,
          driverFatherId);
      profileInst.setId(sProfileId);

      if (profileInst.getObjectId() == -1 || profileInst.getObjectId() == 0) {
        ComponentInst componentInstFather = getComponentInst(driverFatherId, null);
        componentInstFather.addProfileInst(profileInst);
        if (StringUtil.isDefined(userId)) {
          componentInstFather.setUpdaterUserId(userId);
          updateComponentInst(componentInstFather);
        }
      }
      if (startNewTransaction) {
        domainDriverManager.commit();
      }

      if (profileInst.getObjectId() == -1 || profileInst.getObjectId() == 0) {
        cache.opAddProfile(profileManager.getProfileInst(domainDriverManager, sProfileId));
      }
      return sProfileId;
    } catch (Exception e) {
      if (startNewTransaction) {
        rollback();
      }
      throw new AdminException("Admin.addProfileInst", SilverpeasException.ERROR,
          "admin.EX_ERR_ADD_PROFILE", "profile name: '" + profileInst.getName() + "'", e);
    } finally {
      if (startNewTransaction) {
        domainDriverManager.releaseOrganizationSchema();
      }
    }
  }

  @Override
  public String deleteProfileInst(String sProfileId, String userId) throws AdminException {
    return deleteProfileInst(sProfileId, userId, true);
  }

  private String deleteProfileInst(String sProfileId, boolean startNewTransaction) throws
      AdminException {
    return deleteProfileInst(sProfileId, null, startNewTransaction);
  }

  /**
   * Delete the given profile from Silverpeas
   *
   * @param profileId
   * @param userId
   * @param startNewTransaction
   * @return
   * @throws AdminException
   */
  private String deleteProfileInst(String profileId, String userId, boolean startNewTransaction)
      throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();

    ProfileInst profile = profileManager.getProfileInst(domainDriverManager, profileId);
    try {
      if (startNewTransaction) {
        domainDriverManager.startTransaction(false);
      }
      profileManager.deleteProfileInst(profile, domainDriverManager);
      if (StringUtil.isDefined(
          userId) && (profile.getObjectId() == -1 || profile.getObjectId() == 0)) {
        int driverFatherId = getDriverComponentId(profile.getComponentFatherId());
        ComponentInst component = getComponentInst(driverFatherId, null);

        component.setUpdaterUserId(userId);
        updateComponentInst(component);
      }

      if (startNewTransaction) {
        domainDriverManager.commit();
      }

      if (profile.getObjectId() == -1 || profile.getObjectId() == 0) {
        cache.opRemoveProfile(profile);
      }

      return profileId;
    } catch (Exception e) {
      if (startNewTransaction) {
        rollback();
      }
      throw new AdminException("Admin.deleteProfileInst", SilverpeasException.ERROR,
          "admin.EX_ERR_DELETE_PROFILE", "profile Id: '" + profileId + "'", e);
    } finally {
      if (startNewTransaction) {
        domainDriverManager.releaseOrganizationSchema();
      }
    }
  }

  @Override
  public String updateProfileInst(ProfileInst profileInstNew) throws AdminException {
    return updateProfileInst(profileInstNew, null, true, null);
  }

  @Override
  public String updateProfileInst(ProfileInst profileInstNew, String userId) throws AdminException {
    return updateProfileInst(profileInstNew, userId, true, null);
  }

  /**
   * Update the given profile in Silverpeas.
   *
   * @param newProfile
   * @param userId
   * @param startNewTransaction
   * @param rightAssignationMode the data is used from a copy/replace from operation. It is not a
   * nice way to handle this kind of information, but it is not possible to refactor the right
   * services.
   * @return
   * @throws AdminException
   */
  private String updateProfileInst(ProfileInst newProfile, String userId,
      boolean startNewTransaction, final RightAssignationContext.MODE rightAssignationMode)
      throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    if (StringUtil.isDefined(userId)) {
      SilverTrace.spy(MODULE_ADMIN, "Admin.updateProfileInst", "unknown", newProfile.
          getComponentFatherId(), newProfile.getName(), userId, SilverTrace.SPY_ACTION_UPDATE);
    }
    try {
      if (startNewTransaction) {
        domainDriverManager.startTransaction(false);
      }
      profileManager
          .updateProfileInst(groupManager, domainDriverManager, newProfile, rightAssignationMode);
      if (StringUtil.isDefined(
          userId) && (newProfile.getObjectId() == -1 || newProfile.getObjectId() == 0)) {
        int driverFatherId = getDriverComponentId(newProfile.getComponentFatherId());
        ComponentInst component = getComponentInst(driverFatherId, null);
        component.setUpdaterUserId(userId);
        updateComponentInst(component);
      }
      if (startNewTransaction) {
        domainDriverManager.commit();
      }
      if (newProfile.getObjectId() == -1 || newProfile.getObjectId() == 0) {
        cache.opUpdateProfile(newProfile);
      }

      return newProfile.getId();
    } catch (Exception e) {
      if (startNewTransaction) {
        rollback();
      }
      throw new AdminException("Admin.updateProfileInst", SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_PROFILE", "profile Id: '" + newProfile.getId() + "'", e);
    } finally {
      if (startNewTransaction) {
        domainDriverManager.releaseOrganizationSchema();
      }
    }
  }

  // --------------------------------------------------------------------------------------------------------
  // SPACE PROFILE RELATED FUNCTIONS
  // --------------------------------------------------------------------------------------------------------
  @Override
  public SpaceProfileInst getSpaceProfileInst(String spaceProfileId) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    return spaceProfileManager.getSpaceProfileInst(domainDriverManager, spaceProfileId, null);
  }

  @Override
  public String addSpaceProfileInst(SpaceProfileInst spaceProfile, String userId) throws
      AdminException {
    return addSpaceProfileInst(spaceProfile, userId, true);
  }

  /**
   * Add the space profile instance from Silverpeas.
   *
   * @param spaceProfile
   * @param userId
   * @param startNewTransaction
   * @return
   * @throws AdminException
   */
  private String addSpaceProfileInst(SpaceProfileInst spaceProfile, String userId,
      boolean startNewTransaction) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      if (startNewTransaction) {
        domainDriverManager.startTransaction(false);
      }
      Integer spaceId = getDriverComponentId(spaceProfile.getSpaceFatherId());
      String sSpaceProfileId = spaceProfileManager.createSpaceProfileInst(spaceProfile,
          domainDriverManager, spaceId);
      spaceProfile.setId(sSpaceProfileId);
      if (StringUtil.isDefined(userId)) {
        SpaceInst spaceInstFather = getSpaceInstById(spaceId);
        spaceInstFather.setUpdaterUserId(userId);
        updateSpaceInst(spaceInstFather);
      }
      // add new profile in spaces cache
      SpaceInst spaceInst = cache.getSpaceInst(spaceId);
      if (spaceInst != null) {
        spaceInst.addSpaceProfileInst(spaceProfile);
      }
      if (!spaceProfile.isInherited()) {
        SpaceProfileInst inheritedProfile = spaceProfileManager.getInheritedSpaceProfileInstByName(
            domainDriverManager, spaceId, spaceProfile.getName());
        if (inheritedProfile != null) {
          spaceProfile.addGroups(inheritedProfile.getAllGroups());
          spaceProfile.addUsers(inheritedProfile.getAllUsers());
        }
      }
      spreadSpaceProfile(spaceId, spaceProfile);
      if (startNewTransaction) {
        domainDriverManager.commit();
      }
      cache.opAddSpaceProfile(spaceProfile);
      return sSpaceProfileId;
    } catch (Exception e) {
      if (startNewTransaction) {
        rollback();
      }
      throw new AdminException("Admin.addSpaceProfileInst", SilverpeasException.ERROR,
          "admin.EX_ERR_ADD_SPACE_PROFILE", "space profile name: '" + spaceProfile.getName()
          + "'",
          e);
    } finally {
      if (startNewTransaction) {
        domainDriverManager.releaseOrganizationSchema();
      }
    }
  }

  @Override
  public String deleteSpaceProfileInst(String sSpaceProfileId, String userId)
      throws AdminException {
    return deleteSpaceProfileInst(sSpaceProfileId, userId, true);
  }

  private String deleteSpaceProfileInst(String sSpaceProfileId, boolean startNewTransaction) throws
      AdminException {
    return deleteSpaceProfileInst(sSpaceProfileId, null, startNewTransaction);
  }

  /**
   * Delete the given space profile from Silverpeas
   */
  private String deleteSpaceProfileInst(String sSpaceProfileId, String userId,
      boolean startNewTransaction) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    SpaceProfileInst spaceProfileInst = spaceProfileManager.getSpaceProfileInst(domainDriverManager,
        sSpaceProfileId, null);
    try {
      if (startNewTransaction) {
        domainDriverManager.startTransaction(false);
      }
      spaceProfileManager.deleteSpaceProfileInst(spaceProfileInst, domainDriverManager);
      cache.opRemoveSpaceProfile(spaceProfileInst);
      spaceProfileInst.removeAllGroups();
      spaceProfileInst.removeAllUsers();
      Integer spaceId = getDriverComponentId(spaceProfileInst.getSpaceFatherId());
      if (StringUtil.isDefined(userId)) {
        SpaceInst spaceInstFather = getSpaceInstById(spaceId);
        spaceInstFather.setUpdaterUserId(userId);
        updateSpaceInst(spaceInstFather);
      }
      if (!spaceProfileInst.isInherited()) {
        SpaceProfileInst inheritedProfile = spaceProfileManager.getInheritedSpaceProfileInstByName(
            domainDriverManager, spaceId,
            spaceProfileInst.getName());
        if (inheritedProfile != null) {
          spaceProfileInst.addGroups(inheritedProfile.getAllGroups());
          spaceProfileInst.addUsers(inheritedProfile.getAllUsers());
        }
      }
      spreadSpaceProfile(spaceId, spaceProfileInst);
      if (startNewTransaction) {
        domainDriverManager.commit();
      }

      return sSpaceProfileId;
    } catch (Exception e) {
      if (startNewTransaction) {
        rollback();
      }
      throw new AdminException("Admin.deleteSpaceProfileInst", SilverpeasException.ERROR,
          "admin.EX_ERR_DELETE_SPACEPROFILE", "space profile Id: '" + sSpaceProfileId + "'", e);
    } finally {
      if (startNewTransaction) {
        domainDriverManager.releaseOrganizationSchema();
      }
    }
  }

  /**
   * Update the given space profile in Silverpeas
   */
  private String updateSpaceProfileInst(SpaceProfileInst newSpaceProfile) throws AdminException {
    return updateSpaceProfileInst(newSpaceProfile, null);
  }

  @Override
  public String updateSpaceProfileInst(SpaceProfileInst newSpaceProfile, String userId) throws
      AdminException {
    return updateSpaceProfileInst(newSpaceProfile, userId, true);
  }

  @Override
  public String updateSpaceProfileInst(SpaceProfileInst newSpaceProfile, String userId,
      boolean startNewTransaction) throws AdminException {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.
        getCurrentDomainDriverManager();
    try {
      if (startNewTransaction) {
        domainDriverManager.startTransaction(false);
      }
      SpaceProfileInst oldSpaceProfile = spaceProfileManager.getSpaceProfileInst(
          domainDriverManager, newSpaceProfile.getId(), null);
      if (oldSpaceProfile == null) {
        return null;
      }
      String spaceProfileNewId = spaceProfileManager.updateSpaceProfileInst(oldSpaceProfile,
          domainDriverManager, newSpaceProfile);

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
          allProfileSources.add(spaceProfileManager
              .getSpaceProfileInstByName(domainDriverManager, spaceId, oldSpaceProfile.getName()));
        } else {
          allProfileSources.add(spaceProfileManager
              .getInheritedSpaceProfileInstByName(domainDriverManager, spaceId,
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
      if (startNewTransaction) {
        domainDriverManager.commit();
      }
      cache.opUpdateSpaceProfile(spaceProfileManager.getSpaceProfileInst(domainDriverManager,
          newSpaceProfile.getId(), null));

      return spaceProfileNewId;
    } catch (Exception e) {
      if (startNewTransaction) {
        rollback();
      }
      throw new AdminException("Admin.updateSpaceProfileInst", SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_SPACEPROFILE", "space profile Id: '" + newSpaceProfile.getId()
          + "'", e);
    } finally {
      if (startNewTransaction) {
        domainDriverManager.releaseOrganizationSchema();
      }
    }
  }

  private String spaceRole2ComponentRole(String spaceRole, String componentName) {
    return roleMapping.getString(componentName + "_" + spaceRole, spaceRole);
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

    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    // update profile in components
    List<ComponentInstLight> components = TreeCache.getComponents(spaceId);
    for (ComponentInstLight component : components) {
      if (component != null && !component.isInheritanceBlocked()) {
        String componentRole = spaceRole2ComponentRole(spaceProfile.getName(),
            component.getName());
        if (componentRole != null) {
          ProfileInst inheritedProfile = profileManager.getInheritedProfileInst(domainDriverManager,
              component.getLocalId(), componentRole);
          if (inheritedProfile != null) {
            inheritedProfile.removeAllGroups();
            inheritedProfile.removeAllUsers();

            inheritedProfile.addGroups(spaceProfile.getAllGroups());
            inheritedProfile.addUsers(spaceProfile.getAllUsers());

            List<String> profilesToCheck = componentRole2SpaceRoles(componentRole,
                component.getName());
            profilesToCheck.remove(spaceProfile.getName()); // exclude current space profile
            for (String profileToCheck : profilesToCheck) {
              SpaceProfileInst spi = spaceProfileManager.getSpaceProfileInstByName(
                  domainDriverManager, spaceId, profileToCheck);
              if (spi != null) {
                inheritedProfile.addGroups(spi.getAllGroups());
                inheritedProfile.addUsers(spi.getAllUsers());
              }
            }
            updateProfileInst(inheritedProfile);
          } else {
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
        }
      }
    }

    // update profile in subspaces
    List<SpaceInstLight> subSpaces = TreeCache.getSubSpaces(spaceId);
    for (SpaceInstLight subSpace : subSpaces) {
      if (!subSpace.isInheritanceBlocked()) {
        SpaceProfileInst subSpaceProfile = spaceProfileManager
            .getInheritedSpaceProfileInstByName(domainDriverManager, subSpace.getLocalId(),
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
          if (!subSpaceProfile.getAllGroups().isEmpty() || !subSpaceProfile.getAllUsers().isEmpty()) {
            addSpaceProfileInst(subSpaceProfile, null);
          }
        }
      }
    }
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
  public String[] getAllGroupIds() throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    return groupManager.getAllGroupIds(domainDriverManager);
  }

  @Override
  public boolean isGroupExist(String groupName) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    return groupManager.isGroupExist(domainDriverManager, groupName);
  }

  @Override
  public Group getGroup(String groupId) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    return groupManager.getGroup(domainDriverManager, groupId);
  }

  @Override
  public List<String> getPathToGroup(String groupId) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    return groupManager.getPathToGroup(domainDriverManager, groupId);
  }

  @Override
  public Group getGroupByNameInDomain(String groupName, String domainFatherId)
      throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    return groupManager.getGroupByNameInDomain(domainDriverManager, groupName, domainFatherId);
  }

  @Override
  public Group[] getGroups(String[] asGroupId) throws AdminException {
    if (asGroupId == null) {
      return new Group[0];
    }
    Group[] aGroup = new Group[asGroupId.length];
    for (int nI = 0; nI < asGroupId.length; nI++) {
      aGroup[nI] = getGroup(asGroupId[nI]);
    }
    return aGroup;
  }

  @Override
  public String addGroup(Group group) throws AdminException {
    try {
      return addGroup(group, false);
    } catch (Exception e) {
      throw new AdminException("Admin.addGroup", SilverpeasException.ERROR,
          "admin.EX_ERR_ADD_GROUP", "group name: '" + group.getName() + "'", e);
    }
  }

  @Override
  public String addGroup(Group group, boolean onlyInSilverpeas) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      domainDriverManager.startTransaction(false);
      if (group.getDomainId() != null && !onlyInSilverpeas) {
        domainDriverManager.startTransaction(group.getDomainId(), false);
      }
      String sGroupId = groupManager.addGroup(domainDriverManager, group, onlyInSilverpeas);
      group.setId(sGroupId);
      domainDriverManager.commit();
      if (group.getDomainId() != null && !onlyInSilverpeas) {
        domainDriverManager.commit(group.getDomainId());
      }
      if (group.isSynchronized()) {
        groupSynchroScheduler.addGroup(sGroupId);
      }
      cache.opAddGroup(group);
      return sGroupId;
    } catch (Exception e) {
      try {
        domainDriverManager.rollback();
        if (group.getDomainId() != null && !onlyInSilverpeas) {
          domainDriverManager.rollback(group.getDomainId());
        }
      } catch (Exception e1) {
        SilverTrace.error("admin", "Admin.addGroup", "root.EX_ERR_ROLLBACK", e1);
      }
      throw new AdminException("Admin.addGroup", SilverpeasException.ERROR,
          "admin.EX_ERR_ADD_GROUP", "group name: '" + group.getName() + "'", e);
    } finally {
      domainDriverManager.releaseOrganizationSchema();
      if (group.getDomainId() != null && !onlyInSilverpeas) {
        domainDriverManager.releaseOrganizationSchema();
      }
    }

  }

  @Override
  public String deleteGroupById(String sGroupId) throws AdminException {
    try {
      return deleteGroupById(sGroupId, false);
    } catch (Exception e) {
      throw new AdminException("Admin.deleteGroupById", SilverpeasException.ERROR,
          "admin.EX_ERR_DELETE_GROUP", "group Id: '" + sGroupId + "'", e);
    }
  }

  @Override
  public String deleteGroupById(String sGroupId, boolean onlyInSilverpeas) throws AdminException {
    Group group = null;
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      // Get group information
      group = getGroup(sGroupId);
      if (group == null) {
        throw new AdminException("Admin.deleteGroupById", SilverpeasException.ERROR,
            "admin.EX_ERR_GROUP_NOT_FOUND", "group Id: '" + sGroupId + "'");
      }
      domainDriverManager.startTransaction(false);
      if (group.getDomainId() != null && !onlyInSilverpeas) {
        domainDriverManager.startTransaction(group.getDomainId(), false);
      }

      // Delete group managers
      deleteGroupProfileInst(sGroupId, false);

      // Delete group itself
      String sReturnGroupId = groupManager.deleteGroupById(domainDriverManager, group,
          onlyInSilverpeas);
      domainDriverManager.commit();
      if (group.getDomainId() != null && !onlyInSilverpeas) {
        domainDriverManager.commit(group.getDomainId());
      }
      if (group.isSynchronized()) {
        groupSynchroScheduler.removeGroup(sGroupId);
      }
      cache.opRemoveGroup(group);
      return sReturnGroupId;
    } catch (Exception e) {
      try {
        domainDriverManager.rollback();
        if (group != null && group.getDomainId() != null && !onlyInSilverpeas) {
          domainDriverManager.rollback(group.getDomainId());
        }
      } catch (Exception e1) {
        SilverTrace.error("admin", "Admin.deleteGroupById", "root.EX_ERR_ROLLBACK", e1);
      }
      throw new AdminException("Admin.deleteGroupById", SilverpeasException.ERROR,
          "admin.EX_ERR_GROUP_NOT_FOUND", "group Id: '" + sGroupId + "'", e);
    } finally {
      domainDriverManager.releaseOrganizationSchema();
      if (group.getDomainId() != null && !onlyInSilverpeas) {
        domainDriverManager.releaseOrganizationSchema();
      }
    }
  }

  @Override
  public String updateGroup(Group group) throws AdminException {
    try {
      return updateGroup(group, false);
    } catch (Exception e) {
      throw new AdminException("Admin.updateGroup", SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_GROUP", "group name: '" + group.getName() + "'", e);
    }
  }

  @Override
  public String updateGroup(Group group, boolean onlyInSilverpeas) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      domainDriverManager.startTransaction(false);
      if (group.getDomainId() != null && !onlyInSilverpeas) {
        domainDriverManager.startTransaction(group.getDomainId(), false);
      }
      String sGroupId = groupManager.updateGroup(domainDriverManager, group, onlyInSilverpeas);
      domainDriverManager.commit();
      if (group.getDomainId() != null && !onlyInSilverpeas) {
        domainDriverManager.commit(group.getDomainId());
      }
      cache.opUpdateGroup(getGroup(sGroupId));
      return sGroupId;
    } catch (Exception e) {
      try {
        domainDriverManager.rollback();
        if (group.getDomainId() != null && !onlyInSilverpeas) {
          domainDriverManager.rollback(group.getDomainId());
        }
      } catch (Exception e1) {
        SilverTrace.error("admin", "Admin.updateGroup", "root.EX_ERR_ROLLBACK", e1);
      }
      throw new AdminException("Admin.updateGroup", SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_GROUP", "group name: '" + group.getName() + "'", e);
    } finally {
      domainDriverManager.releaseOrganizationSchema();
      if (group.getDomainId() != null && !onlyInSilverpeas) {
        domainDriverManager.releaseOrganizationSchema();
      }
    }
  }

  @Override
  public void removeUserFromGroup(String sUserId, String sGroupId) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      // Start transaction
      domainDriverManager.startTransaction(false);

      // Update group
      groupManager.removeUserFromGroup(domainDriverManager, sUserId, sGroupId);

      // Commit the transaction
      domainDriverManager.commit();

      cache.opUpdateGroup(getGroup(sGroupId));

    } catch (Exception e) {
      try {
        // Roll back the transactions
        domainDriverManager.rollback();
      } catch (Exception e1) {
        SilverTrace.error("admin", "Admin.removeUserFromGroup", "root.EX_ERR_ROLLBACK", e1);
      }
      throw new AdminException("Admin.removeUserFromGroup", SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_GROUP", "groupId = " + sGroupId + ", userId = " + sUserId, e);
    } finally {
      domainDriverManager.releaseOrganizationSchema();
    }
  }

  @Override
  public void addUserInGroup(String sUserId, String sGroupId) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      // Start transaction
      domainDriverManager.startTransaction(false);

      // Update group
      groupManager.addUserInGroup(domainDriverManager, sUserId, sGroupId);

      // Commit the transaction
      domainDriverManager.commit();

      cache.opUpdateGroup(getGroup(sGroupId));

    } catch (Exception e) {
      try {
        // Roll back the transactions
        domainDriverManager.rollback();
      } catch (Exception e1) {
        SilverTrace.error("admin", "Admin.addUserInGroup", "root.EX_ERR_ROLLBACK", e1);
      }
      throw new AdminException("Admin.addUserInGroup", SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_GROUP", "groupId = " + sGroupId + ", userId = " + sUserId, e);
    } finally {
      domainDriverManager.releaseOrganizationSchema();
    }
  }

  @Override
  public AdminGroupInst[] getAdminOrganization() throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    return groupManager.getAdminOrganization(domainDriverManager);
  }

  @Override
  public String[] getAllSubGroupIds(String groupId) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    return groupManager.getAllSubGroupIds(domainDriverManager, groupId);
  }

  @Override
  public String[] getAllSubGroupIdsRecursively(String groupId)
      throws AdminException {
    List<String> groupIds = groupManager.getAllSubGroupIdsRecursively(groupId);
    return groupIds.toArray(new String[groupIds.size()]);
  }

  @Override
  public String[] getAllRootGroupIds() throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    return groupManager.getAllRootGroupIds(domainDriverManager);
  }

  @Override
  public Group[] getAllRootGroups() throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    return groupManager.getAllRootGroups(domainDriverManager);
  }

  //
  // --------------------------------------------------------------------------------------------------------
  // GROUP PROFILE RELATED FUNCTIONS
  // --------------------------------------------------------------------------------------------------------
  @Override
  public GroupProfileInst getGroupProfileInst(String groupId) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    return groupProfileManager.getGroupProfileInst(domainDriverManager, null, groupId);
  }

  @Override
  public String addGroupProfileInst(GroupProfileInst spaceProfileInst)
      throws AdminException {
    return addGroupProfileInst(spaceProfileInst, true);
  }

  @Override
  public String addGroupProfileInst(GroupProfileInst groupProfileInst, boolean startNewTransaction)
      throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      if (startNewTransaction) {
        // Open the connections with auto-commit to false
        domainDriverManager.startTransaction(false);
      }

      // Create the space profile instance
      Group group = getGroup(groupProfileInst.getGroupId());
      String sProfileId = groupProfileManager.createGroupProfileInst(
          groupProfileInst, domainDriverManager, group.getId());
      groupProfileInst.setId(sProfileId);

      if (startNewTransaction) {
        // commit the transactions
        domainDriverManager.commit();
      }

      // m_Cache.opAddSpaceProfile(m_GroupProfileInstManager.getGroupProfileInst(m_DDManager,
      // sSpaceProfileId, null));
      return sProfileId;
    } catch (Exception e) {
      if (startNewTransaction) {
        rollback();
      }
      throw new AdminException("Admin.addGroupProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_ADD_SPACE_PROFILE",
          "group roleName = " + groupProfileInst.getName(), e);
    } finally {
      domainDriverManager.releaseOrganizationSchema();

    }
  }

  @Override
  public String deleteGroupProfileInst(String groupId) throws AdminException {
    return deleteGroupProfileInst(groupId, true);
  }

  @Override
  public String deleteGroupProfileInst(String groupId, boolean startNewTransaction)
      throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    // Get the SpaceProfile to delete
    GroupProfileInst groupProfileInst = groupProfileManager.getGroupProfileInst(domainDriverManager,
        null, groupId);

    try {
      if (startNewTransaction) {
        domainDriverManager.startTransaction(false);
      }

      // Delete the Profile in tables
      groupProfileManager.deleteGroupProfileInst(groupProfileInst, domainDriverManager);
      if (startNewTransaction) {
        // commit the transactions
        domainDriverManager.commit();
      }

      return groupId;
    } catch (Exception e) {
      if (startNewTransaction) {
        rollback();
      }
      throw new AdminException("Admin.deleteGroupProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_DELETE_GROUPPROFILE",
          "groupId = " + groupId, e);
    } finally {
      if (startNewTransaction) {
        domainDriverManager.releaseOrganizationSchema();
      }
    }
  }

  @Override
  public String updateGroupProfileInst(GroupProfileInst groupProfileInstNew)
      throws AdminException {
    String sSpaceProfileNewId = groupProfileInstNew.getId();
    if (!StringUtil.isDefined(sSpaceProfileNewId)) {
      sSpaceProfileNewId = addGroupProfileInst(groupProfileInstNew);
    } else {
      DomainDriverManager domainDriverManager =
          DomainDriverManagerProvider.getCurrentDomainDriverManager();
      try {
        domainDriverManager.startTransaction(false);
        GroupProfileInst oldSpaceProfile = groupProfileManager.getGroupProfileInst(
            domainDriverManager, null,
            groupProfileInstNew.getGroupId());
        // Update the group profile in tables
        groupProfileManager.updateGroupProfileInst(oldSpaceProfile,
            domainDriverManager, groupProfileInstNew);
        domainDriverManager.commit();
      } catch (Exception e) {
        rollback();
        throw new AdminException("Admin.updateGroupProfileInst",
            SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_SPACEPROFILE",
            "space profile Id: '" + groupProfileInstNew.getId() + "'", e);
      } finally {
        domainDriverManager.releaseOrganizationSchema();
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
        SilverTrace.error("admin", "Admin.indexAllGroups", "admin.CANT_INDEX_GROUPS",
            "domainId = " + domain.getId(), e);
      }
    }

    //Mixt Domain (id -1)
    try {
      indexGroups("-1");
    } catch (Exception e) {
      SilverTrace.error("admin", "Admin.indexAllGroups", "admin.CANT_INDEX_GROUPS",
          "domainId = -1", e);
    }
  }

  @Override
  public void indexGroups(String domainId) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      domainDriverManager.indexAllGroups(domainId);
    } catch (Exception e) {
      throw new AdminException("Admin.indexGroups",
          SilverpeasException.ERROR, "admin.CANT_INDEX_GROUPS", "domainId = " + domainId, e);
    }
  }

  // -------------------------------------------------------------------------
  // USER RELATED FUNCTIONS
  // -------------------------------------------------------------------------
  @Override
  public String[] getAllUsersIds() throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    return userManager.getAllUsersIds(domainDriverManager);
  }

  @Override
  public UserDetail getUserDetail(String sUserId) throws AdminException {
    if (!StringUtil.isDefined(sUserId) || "-1".equals(sUserId)) {
      return null;
    }
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    UserDetail ud = cache.getUserDetail(sUserId);
    if (ud == null) {
      ud = userManager.getUserDetail(domainDriverManager, sUserId);
      if (ud != null) {
        cache.putUserDetail(sUserId, ud);
      }
    }
    return ud;
  }

  @Override
  public UserDetail[] getUserDetails(String[] userIds) {
    if (userIds == null) {
      return ArrayUtil.EMPTY_USER_DETAIL_ARRAY;
    }

    List<UserDetail> users = new ArrayList<>(userIds.length);
    for (String userId : userIds) {
      try {
        users.add(getUserDetail(userId));
      } catch (AdminException e) {
        SilverTrace.error("admin", "Admin.getUserDetails", "admin.EX_ERR_GET_USER_DETAILS",
            "user id: '" + userId + "'", e);
      }
    }
    return users.toArray(new UserDetail[users.size()]);
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
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    return userManager.isEmailExisting(domainDriverManager, email);
  }

  @Override
  public String getUserIdByLoginAndDomain(String sLogin, String sDomainId) throws AdminException {
    Domain[] theDomains;
    String valret = null;
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    if (!StringUtil.isDefined(sDomainId)) {
      try {
        theDomains = domainDriverManager.getAllDomains();
      } catch (Exception e) {
        throw new AdminException("Admin.getUserIdByLoginAndDomain",
            SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_BY_LOGIN_DOMAIN",
            "login: '" + sLogin + "', domain id: '" + sDomainId + "'", e);
      }
      for (int i = 0; i < theDomains.length && valret == null; i++) {
        try {
          valret = userManager.getUserIdByLoginAndDomain(domainDriverManager, sLogin,
              theDomains[i].getId());
        } catch (Exception e) {
          throw new AdminException("Admin.getUserIdByLoginAndDomain", SilverpeasException.ERROR,
              "admin.EX_ERR_GET_USER_BY_LOGIN_DOMAIN", "login: '" + sLogin + "', domain id: '"
              + sDomainId + "'", e);
        }
      }
      if (valret == null) {
        throw new AdminException("Admin.getUserIdByLoginAndDomain", SilverpeasException.ERROR,
            "admin.EX_ERR_USER_NOT_FOUND", "login: '" + sLogin + "', in all domains");
      }
    } else {
      valret = userManager.getUserIdByLoginAndDomain(domainDriverManager, sLogin, sDomainId);
    }
    return valret;
  }

  @Override
  public String getUserIdByAuthenticationKey(String authenticationKey) throws Exception {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    Map<String, String> userParameters = domainDriverManager.authenticate(authenticationKey);
    String login = userParameters.get("login");
    String domainId = userParameters.get("domainId");
    return userManager.getUserIdByLoginAndDomain(domainDriverManager, login, domainId);
  }

  @Override
  public UserFull getUserFull(String sUserId) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    return userManager.getUserFull(domainDriverManager, sUserId);
  }

  @Override
  public UserFull getUserFull(String domainId, String specificId) throws Exception {

    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    DomainDriver synchroDomain = domainDriverManager.getDomainDriver(Integer.parseInt(domainId));
    return synchroDomain.getUserFull(specificId);
  }

  @Override
  public String addUser(UserDetail userDetail) throws AdminException {
    try {
      return addUser(userDetail, false);
    } catch (Exception e) {
      throw new AdminException("Admin.addUser", SilverpeasException.ERROR,
          "admin.EX_ERR_ADD_USER", userDetail.getFirstName() + " " + userDetail.getLastName(), e);
    }
  }

  @Override
  public String addUser(UserDetail userDetail, boolean addOnlyInSilverpeas) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      // Start transaction
      domainDriverManager.startTransaction(false);
      if (userDetail.getDomainId() != null && !addOnlyInSilverpeas) {
        domainDriverManager.startTransaction(userDetail.getDomainId(), false);
      }

      // add user
      String sUserId = userManager.addUser(domainDriverManager, userDetail, addOnlyInSilverpeas);

      // Commit the transaction
      domainDriverManager.commit();
      if (userDetail.getDomainId() != null && !addOnlyInSilverpeas) {
        domainDriverManager.commit(userDetail.getDomainId());
      }

      cache.opAddUser(userManager.getUserDetail(domainDriverManager, sUserId));
      // return group id
      return sUserId;
    } catch (Exception e) {
      try {
        // Roll back the transactions
        domainDriverManager.rollback();
        if (userDetail.getDomainId() != null && !addOnlyInSilverpeas) {
          domainDriverManager.rollback(userDetail.getDomainId());
        }
      } catch (Exception e1) {
        SilverTrace.error("admin", "Admin.addUser", "root.EX_ERR_ROLLBACK", e1);
      }
      throw new AdminException("Admin.addUser", SilverpeasException.ERROR,
          "admin.EX_ERR_ADD_USER", userDetail.getFirstName() + " " + userDetail.getLastName(), e);
    } finally {
      domainDriverManager.releaseOrganizationSchema();
      if (userDetail.getDomainId() != null && !addOnlyInSilverpeas) {
        domainDriverManager.releaseOrganizationSchema();
      }
    }
  }

  @Override
  public void migrateUser(UserDetail userDetail, String targetDomainId) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      // Start transaction
      domainDriverManager.startTransaction(false);
      domainDriverManager.startTransaction(targetDomainId, false);

      // migrate user
      userManager.migrateUser(domainDriverManager, userDetail, targetDomainId);

      // Commit the transaction
      domainDriverManager.commit();
      domainDriverManager.commit(targetDomainId);

      cache.opUpdateUser(userDetail);
    } catch (Exception e) {
      try {
        // Roll back the transactions
        domainDriverManager.rollback();
        domainDriverManager.rollback(targetDomainId);
      } catch (Exception e1) {
        SilverTrace.error("admin", "Admin.migrateUserFromSilverpeasToAnotherDomain",
            "root.EX_ERR_ROLLBACK", e1);
      }
      throw new AdminException("Admin.migrateUserFromSilverpeasToAnotherDomain",
          SilverpeasException.ERROR,
          "admin.EX_ERR_M_USER", userDetail.getFirstName() + " " + userDetail.getLastName(), e);
    } finally {
      domainDriverManager.releaseOrganizationSchema();
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
      throw new AdminException("Admin.updateUserState", SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_USER_STATE", "user id : '" + userId + "', state : '" + state.name()
          + "'", e);
    }
  }

  @Override
  public void userAcceptsTermsOfService(String userId) throws AdminException {
    try {
      UserDetail user = UserDetail.getById(userId);
      user.setTosAcceptanceDate(DateUtil.getNow());
      updateUser(user);
    } catch (Exception e) {
      throw new AdminException("Admin.updateTermsOfServiceAcceptanceDate",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_USER_TOS_ACCEPTANCE_DATE",
          "user id : '" + userId + "'", e);
    }
  }

  @Override
  public String deleteUser(String sUserId) throws AdminException {
    try {
      if (m_sDAPIGeneralAdminId.equals(sUserId)) {
        SilverTrace.warn("admin", "Admin.deleteUser",
            "admin.MSG_WARN_TRY_TO_DELETE_GENERALADMIN");
        return null;
      }

      return deleteUser(sUserId, false);

    } catch (Exception e) {
      throw new AdminException("Admin.deleteUser", SilverpeasException.ERROR,
          "admin.EX_ERR_DELETE_USER", "user id : '" + sUserId + "'", e);
    }
  }

  @Override
  public String deleteUser(String sUserId, boolean onlyInSilverpeas) throws AdminException {
    UserDetail user = null;
    boolean transactionStarted = false;
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      // removes all social network external account associated to this user account
      // TODO: use based JMS system notification instead of explicit call
      // SocialNetworkService.getInstance().removeAllExternalAccount(sUserId);

      user = getUserDetail(sUserId);
      if (user == null) {
        throw new AdminException("Admin.deleteUser", SilverpeasException.ERROR,
            "admin.EX_ERR_USER_NOT_FOUND", "user id : '" + sUserId + "'");
      }
      // Start transaction
      domainDriverManager.startTransaction(false);
      if (user.getDomainId() != null && !onlyInSilverpeas) {
        transactionStarted = true;
        domainDriverManager.startTransaction(user.getDomainId(), false);
      }

      // Delete the user
      String sReturnUserId = userManager.deleteUser(domainDriverManager, user, onlyInSilverpeas);

      // Commit the transaction
      domainDriverManager.commit();
      if (user.getDomainId() != null && !onlyInSilverpeas) {
        domainDriverManager.commit(user.getDomainId());
      }
      cache.opRemoveUser(user);
      return sReturnUserId;
    } catch (Exception e) {
      try {
        // Roll back the transactions
        domainDriverManager.rollback();
        if (transactionStarted) {
          domainDriverManager.rollback(user.getDomainId());
        }
      } catch (Exception e1) {
        SilverTrace.error("admin", "Admin.deleteUser", "root.EX_ERR_ROLLBACK", e1);
      }
      throw new AdminException("Admin.deleteUser", SilverpeasException.ERROR,
          "admin.EX_ERR_DELETE_USER", "user id : '" + sUserId + "'", e);
    } finally {
      domainDriverManager.releaseOrganizationSchema();
      if (transactionStarted) {
        domainDriverManager.releaseOrganizationSchema();
      }
    }
  }

  @Override
  public String updateUser(UserDetail user) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      // Start transaction
      domainDriverManager.startTransaction(false);

      // Update user
      String sUserId = userManager.updateUser(domainDriverManager, user);

      // Commit the transaction
      domainDriverManager.commit();

      cache.opUpdateUser(userManager.getUserDetail(domainDriverManager, sUserId));

      return sUserId;
    } catch (Exception e) {
      rollback();
      throw new AdminException("Admin.updateUser", SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_USER", "user id : '" + user.getId() + "'", e);
    } finally {
      domainDriverManager.releaseOrganizationSchema();
    }
  }

  @Override
  public String updateUserFull(UserFull user) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      // Start transaction
      domainDriverManager.startTransaction(false);
      if (user.getDomainId() != null) {
        domainDriverManager.startTransaction(user.getDomainId(), false);
      }

      // Update user
      String sUserId = userManager.updateUserFull(domainDriverManager, user);

      // Commit the transaction
      domainDriverManager.commit();
      if (user.getDomainId() != null) {
        domainDriverManager.commit(user.getDomainId());
      }
      cache.opUpdateUser(userManager.getUserDetail(domainDriverManager, sUserId));

      return sUserId;
    } catch (Exception e) {
      try {
        // Roll back the transactions
        domainDriverManager.rollback();
        if (user.getDomainId() != null) {
          domainDriverManager.rollback(user.getDomainId());
        }
      } catch (Exception e1) {
        SilverTrace.error("admin", "Admin.updateUserFull", "root.EX_ERR_ROLLBACK", e1);
      }
      throw new AdminException("Admin.updateUserFull", SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_USER", "user id : '" + user.getId() + "'", e);
    } finally {
      domainDriverManager.releaseOrganizationSchema();
      if (user.getDomainId() != null) {
        domainDriverManager.releaseOrganizationSchema();
      }
    }
  }

  // -------------------------------------------------------------------------
  // CONVERSION CLIENT <--> DRIVER SPACE ID
  // -------------------------------------------------------------------------
  /**
   * Converts client space id to driver space id
   */
  private int getDriverSpaceId(String sClientSpaceId) {
    if (sClientSpaceId != null) {
      if (sClientSpaceId.startsWith(SpaceInst.SPACE_KEY_PREFIX)) {
        return Integer.parseInt(sClientSpaceId.substring(SpaceInst.SPACE_KEY_PREFIX.length()));
      } else {
        return Integer.parseInt(sClientSpaceId);
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
  public String[] getClientSpaceIds(String[] asDriverSpaceIds) throws Exception {
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
    for (int nI = 0; nI < cBuf.length && sTableClientId.length() == 0; nI++) {
      if (cBuf[nI] == '0' || cBuf[nI] == '1' || cBuf[nI] == '2' || cBuf[nI] == '3' || cBuf[nI]
          == '4' || cBuf[nI] == '5' || cBuf[nI] == '6' || cBuf[nI] == '7' || cBuf[nI] == '8'
          || cBuf[nI] == '9') {
        sTableClientId = sClientComponentId.substring(nI);
      }
    }
    if (StringUtil.isDefined(sTableClientId)) {
      return Integer.parseInt(sTableClientId);
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
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      return domainDriverManager.getNextDomainId();
    } catch (Exception e) {
      throw new AdminException("Admin.getNextDomainId", SilverpeasException.ERROR,
          "admin.EX_ERR_ADD_DOMAIN", e);
    }
  }

  @Transactional
  @Override
  public String addDomain(Domain theDomain) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      String id = domainDriverManager.createDomain(theDomain);

      // Update the synchro scheduler
      DomainDriver domainDriver = domainDriverManager.getDomainDriver(Integer.parseInt(id));
      if (domainDriver.isSynchroThreaded()) {
        domainSynchroScheduler.addDomain(id);
      }

      return id;
    } catch (Exception e) {
      throw new AdminException("Admin.addDomain", SilverpeasException.ERROR,
          "admin.EX_ERR_ADD_DOMAIN", "domain name : '" + theDomain.getName() + "'", e);
    }
  }

  @Transactional
  @Override
  public String updateDomain(Domain domain) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      DomainCache.removeDomain(domain.getId());
      return domainDriverManager.updateDomain(domain);
    } catch (Exception e) {
      throw new AdminException("Admin.updateDomain", SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_DOMAIN", "domain name : '" + domain.getName() + "'", e);
    }
  }

  @Transactional
  @Override
  public String removeDomain(String domainId) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      // Remove all users
      UserDetail[] toRemoveUDs = userManager.getUsersOfDomain(domainDriverManager,
          domainId);
      if (toRemoveUDs != null) {
        for (UserDetail user : toRemoveUDs) {
          try {
            deleteUser(user.getId(), false);
          } catch (Exception e) {
            deleteUser(user.getId(), true);
          }
        }
      }
      // Remove all groups
      Group[] toRemoveGroups = groupManager.getGroupsOfDomain(domainDriverManager,
          domainId);
      if (toRemoveGroups != null) {
        for (Group group : toRemoveGroups) {
          try {
            deleteGroupById(group.getId(), false);
          } catch (Exception e) {
            deleteGroupById(group.getId(), true);
          }
        }
      }
      // Remove the domain
      domainDriverManager.removeDomain(domainId);
      // Update the synchro scheduler
      domainSynchroScheduler.removeDomain(domainId);
      DomainCache.removeDomain(domainId);

      return domainId;
    } catch (Exception e) {
      throw new AdminException("Admin.removeDomain", SilverpeasException.ERROR,
          "admin.MSG_ERR_DELETE_DOMAIN", "domain Id : '" + domainId + "'", e);
    }
  }

  @Override
  public Domain[] getAllDomains() throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      return domainDriverManager.getAllDomains();
    } catch (Exception e) {
      throw new AdminException("Admin.getAllDomains",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_ALL_DOMAINS", e);
    }
  }

  @Override
  public List<String> getAllDomainIdsForLogin(String login) throws AdminException {
    return userManager.getDomainsOfUser(login);
  }

  @Override
  public Domain getDomain(String domainId) throws AdminException {
    try {
      if (!StringUtil.isDefined(domainId) || !StringUtil.isInteger(domainId)) {
        domainId = "-1";
      }
      DomainDriverManager domainDriverManager =
          DomainDriverManagerProvider.getCurrentDomainDriverManager();

      Domain domain = DomainCache.getDomain(domainId);
      if (domain == null) {
        domain = domainDriverManager.getDomain(domainId);
        DomainCache.addDomain(domain);
      }
      return domain;
    } catch (Exception e) {
      throw new AdminException("Admin.getDomain", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_DOMAIN", "domain Id : '" + domainId + "'", e);
    }
  }

  @Override
  public long getDomainActions(String domainId) throws AdminException {
    try {
      if (domainId != null && domainId.equals("-1")) {
        return DomainDriver.ACTION_MASK_MIXED_GROUPS;
      }
      return DomainDriverManagerProvider.getCurrentDomainDriverManager().getDomainActions(domainId);
    } catch (Exception e) {
      throw new AdminException("Admin.getDomainActions",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_DOMAIN", "domain Id : '" + domainId + "'",
          e);
    }
  }

  @Override
  public Group[] getRootGroupsOfDomain(String domainId) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      return groupManager.getRootGroupsOfDomain(domainDriverManager, domainId);
    } catch (Exception e) {
      throw new AdminException("Admin.getGroupsOfDomain",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_DOMAIN", "domain Id : '" + domainId + "'",
          e);
    }
  }

  @Override
  public Group[] getSynchronizedGroups() throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      return groupManager.getSynchronizedGroups(domainDriverManager);
    } catch (Exception e) {
      throw new AdminException("Admin.getGroupsOfDomain",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_DOMAIN", e);
    }
  }

  @Override
  public String[] getRootGroupIdsOfDomain(String domainId) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      return groupManager.getRootGroupIdsOfDomain(domainDriverManager, domainId);
    } catch (Exception e) {
      throw new AdminException("Admin.getRootGroupIdsOfDomain",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_DOMAIN", "domain Id : '" + domainId + "'",
          e);
    }
  }

  @Override
  public UserDetail[] getAllUsersOfGroup(String groupId) throws AdminException {
    try {
      List<String> groupIds = new ArrayList<>();
      groupIds.add(groupId);
      groupIds.addAll(groupManager.getAllSubGroupIdsRecursively(groupId));

      return userManager.getAllUsersOfGroups(groupIds);
    } catch (Exception e) {
      throw new AdminException("Admin.getAllUsersOfGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_DOMAIN", "Group Id : '" + groupId + "'",
          e);
    }
  }

  @Override
  public UserDetail[] getUsersOfDomain(String domainId) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      if (domainId != null && "-1".equals(domainId)) {
        return ArrayUtil.EMPTY_USER_DETAIL_ARRAY;
      }
      return userManager.getUsersOfDomain(domainDriverManager, domainId);
    } catch (Exception e) {
      throw new AdminException("Admin.getUsersOfDomain",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_DOMAIN", "domain Id : '" + domainId + "'",
          e);
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
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      if (domainId != null && "-1".equals(domainId)) {
        return ArrayUtil.EMPTY_STRING_ARRAY;
      }
      return userManager.getUserIdsOfDomain(domainDriverManager, domainId);
    } catch (Exception e) {
      throw new AdminException("Admin.getUserIdsOfDomain",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_DOMAIN", "domain Id : '" + domainId + "'",
          e);
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
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      // Authenticate the given user
      Map<String, String> loginDomain = domainDriverManager.authenticate(sKey, removeKey);
      if ((!loginDomain.containsKey("login")) || (!loginDomain.containsKey("domainId"))) {
        throw new AdminException("Admin.authenticate", SilverpeasException.WARNING,
            "admin.MSG_ERR_AUTHENTICATE_USER", "key : '" + sKey + "'");
      }

      // Get the Silverpeas userId
      String sLogin = loginDomain.get("login");
      String sDomainId = loginDomain.get("domainId");

      DomainDriver synchroDomain = domainDriverManager.getDomainDriver(Integer.parseInt(
          sDomainId));
      // Get the user Id or import it if the domain accept it
      try {
        sUserId = userManager.getUserIdByLoginAndDomain(domainDriverManager, sLogin,
            sDomainId);
      } catch (Exception ex) {
        if (synchroDomain.isSynchroOnLoginEnabled() && !isAppInMaintenance) {//Try to import new user
          SilverTrace.warn("admin", "Admin.authenticate",
              "admin.EX_ERR_USER_NOT_FOUND", "Login: '" + sLogin + "', Domain: " + sDomainId, ex);
          sUserId = synchronizeImportUserByLogin(sDomainId, sLogin,
              synchroDomain.isSynchroOnLoginRecursToGroups());
        } else {
          throw ex;
        }
      }
      // Synchronize the user if the domain needs it
      if (synchroDomain.isSynchroOnLoginEnabled() && !isAppInMaintenance) {
        try {
          synchronizeUser(sUserId, synchroDomain.isSynchroOnLoginRecursToGroups());
        } catch (Exception ex) {
          SilverTrace.warn("admin", "Admin.authenticate",
              "admin.MSG_ERR_SYNCHRONIZE_USER", "UserId=" + sUserId + " Login: '" + sLogin
              + "', Domain: " + sDomainId, ex);
        }
      }

      return sUserId;
    } catch (Exception e) {
      throw new AdminException("Admin.authenticate",
          SilverpeasException.WARNING, "admin.MSG_ERR_AUTHENTICATE_USER",
          "key : '" + sKey + "'", e);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // QUERY FUNCTIONS
  // ---------------------------------------------------------------------------------------------
  @Override
  public String[] getDirectGroupsIdsOfUser(String userId) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      return groupManager.getDirectGroupsOfUser(domainDriverManager, userId);
    } catch (Exception e) {
      throw new AdminException("Admin.getDirectGroupsIdsOfUser",
          SilverpeasException.ERROR, "admin.EX_ERR_GROUP_NOT_FOUND",
          "user Id : '" + userId + "'", e);
    }
  }

  @Override
  public UserDetail[] searchUsers(UserDetail modelUser, boolean isAnd)
      throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      return userManager.searchUsers(domainDriverManager, modelUser, isAnd);
    } catch (Exception e) {
      throw new AdminException("Admin.searchUsers", SilverpeasException.ERROR,
          "admin.EX_ERR_USER_NOT_FOUND", e);
    }
  }

  @Override
  public Group[] searchGroups(Group modelGroup, boolean isAnd) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      return groupManager.searchGroups(domainDriverManager, modelGroup, isAnd);
    } catch (Exception e) {
      throw new AdminException("Admin.searchGroups", SilverpeasException.ERROR,
          "admin.EX_ERR_GROUP_NOT_FOUND", e);
    }
  }

  @Override
  public String[] getUserSpaceIds(String sUserId) throws AdminException {
    List<String> spaceIds = new ArrayList<>();

    // getting all components availables
    List<String> componentIds = getAllowedComponentIds(sUserId);
    for (String componentId : componentIds) {
      List<SpaceInstLight> spaces = TreeCache.getComponentPath(componentId);
      for (SpaceInstLight space : spaces) {
        if (!spaceIds.contains(space.getId())) {
          spaceIds.add(space.getId());
        }
      }
    }

    return spaceIds.toArray(new String[spaceIds.size()]);
  }

  private List<String> getAllGroupsOfUser(String userId) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    List<String> allGroupsOfUser = GroupCache.getAllGroupIdsOfUser(userId);
    if (allGroupsOfUser == null) {
      // group ids of user is not yet processed
      // process it and store it in cache
      allGroupsOfUser = groupManager.getAllGroupsOfUser(domainDriverManager, userId);
      // store groupIds of user in cache
      GroupCache.setAllGroupIdsOfUser(userId, allGroupsOfUser);
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
      List<String> result = new ArrayList<>();
      // getting all components availables
      List<String> componentIds = getAllowedComponentIds(sUserId);
      // getting all root spaces (sorted)
      String[] rootSpaceIds = getAllRootSpaceIds();
      // retain only allowed root spaces
      for (String rootSpaceId : rootSpaceIds) {
        if (isSpaceContainsOneComponent(componentIds, getDriverSpaceId(rootSpaceId), true)) {
          result.add(rootSpaceId);
        }
      }
      return result.toArray(new String[result.size()]);
    } catch (Exception e) {
      throw new AdminException("Admin.getUserRootSpaceIds",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_ALLOWED_ROOTSPACE_IDS", "user Id : '" + sUserId + "'", e);
    }
  }

  @Override
  public String[] getUserSubSpaceIds(String sUserId, String spaceId) throws AdminException {
    try {
      List<String> result = new ArrayList<>();
      // getting all components availables
      List<String> componentIds = getAllowedComponentIds(sUserId);
      // getting all subspaces
      List<SpaceInstLight> subspaces = TreeCache.getSubSpaces(getDriverSpaceId(spaceId));
      for (SpaceInstLight subspace : subspaces) {
        if (isSpaceContainsOneComponent(componentIds, subspace.getLocalId(), true)) {
          result.add(subspace.getId());
        }
      }
      return result.toArray(new String[result.size()]);

    } catch (Exception e) {
      throw new AdminException("Admin.getUserRootSpaceIds",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_ALLOWED_ROOTSPACE_IDS", "user Id : '" + sUserId + "'", e);
    }
  }

  @Override
  public boolean isSpaceAvailable(String userId, String spaceId) throws AdminException {
    List<String> componentIds = getAllowedComponentIds(userId);
    return isSpaceContainsOneComponent(componentIds, getDriverSpaceId(spaceId), true);
  }

  private boolean isSpaceContainsOneComponent(List<String> componentIds, int spaceId,
      boolean checkInSubspaces) {
    boolean find = false;

    List<ComponentInstLight> components = new ArrayList<>(TreeCache.getComponents(spaceId));

    // Is there at least one component available ?
    for (int c = 0; !find && c < components.size(); c++) {
      find = componentIds.contains(components.get(c).getId());
    }
    if (find) {
      return true;
    } else {
      if (checkInSubspaces) {
        // check in subspaces
        List<SpaceInstLight> subspaces = new ArrayList<>(TreeCache.getSubSpaces(spaceId));
        for (int s = 0; !find && s < subspaces.size(); s++) {
          find = isSpaceContainsOneComponent(componentIds, subspaces.get(s).getLocalId(),
              checkInSubspaces);
        }
      }
    }

    return find;
  }

  @Override
  public List<SpaceInstLight> getSubSpacesOfUser(String userId, String spaceId)
      throws AdminException {

    try {
      List<SpaceInstLight> result = new ArrayList<>();

      // getting all components availables
      List<String> componentIds = getAllowedComponentIds(userId);

      // getting all subspaces
      List<SpaceInstLight> subspaces = TreeCache.getSubSpaces(getDriverSpaceId(spaceId));
      for (SpaceInstLight subspace : subspaces) {
        if (isSpaceContainsOneComponent(componentIds, subspace.getLocalId(), true)) {
          result.add(subspace);
        }
      }

      return result;
    } catch (Exception e) {
      throw new AdminException("Admin.getSubSpacesOfUser",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_ALLOWED_SUBSPACES",
          "userId = " + userId + ", spaceId = " + spaceId, e);
    }
  }

  @Override
  public List<SpaceInstLight> getSubSpaces(String spaceId) throws AdminException {

    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    return spaceManager.getSubSpaces(domainDriverManager, getDriverSpaceId(spaceId));
  }

  @Override
  public List<ComponentInstLight> getAvailCompoInSpace(String userId, String spaceId)
      throws AdminException {

    try {
      List<String> allowedComponentIds = getAllowedComponentIds(userId);

      List<ComponentInstLight> allowedComponents = new ArrayList<>();

      List<ComponentInstLight> allComponents = TreeCache.getComponentsInSpaceAndSubspaces(
          getDriverSpaceId(spaceId));
      for (ComponentInstLight component : allComponents) {
        if (allowedComponentIds.contains(component.getId())) {
          allowedComponents.add(component);
        }
      }
      return allowedComponents;
    } catch (Exception e) {
      throw new AdminException("Admin.getAvailCompoInSpace",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_ALLOWED_COMPONENTS", "userId = " + userId + ", spaceId = "
          + spaceId, e);
    }
  }

  @Override
  public Map<String, SpaceAndChildren> getTreeView(String userId, String spaceId)
      throws AdminException {

    int driverSpaceId = getDriverSpaceId(spaceId);

    // Step 1 - get all availables spaces and components
    Collection<SpaceInstLight> spacesLight = getSubSpacesOfUser(userId, spaceId);
    Collection<ComponentInstLight> componentsLight = getAvailCompoInSpace(userId, spaceId);



    // Step 2 - build HashTable
    Map<String, SpaceAndChildren> spaceTrees = new HashMap<>();
    Iterator<SpaceInstLight> it = spacesLight.iterator();
    while (it.hasNext()) {
      SpaceInstLight space = it.next();
      spaceTrees.put(space.getId(), new SpaceAndChildren(space));
    }

    // Step 3 - add root space to hashtable
    SpaceInstLight rootSpace = getSpaceInstLight(driverSpaceId);
    spaceTrees.put(rootSpace.getId(), new SpaceAndChildren(rootSpace));

    // Step 4 - build dependances
    it = spacesLight.iterator();
    while (it.hasNext()) {
      SpaceInstLight child = it.next();
      String fatherId = getClientSpaceId(child.getFatherId());
      SpaceAndChildren father = spaceTrees.get(fatherId);
      if (father != null) {
        father.addSubSpace(child);
      }
    }

    for (ComponentInstLight child : componentsLight) {
      String fatherId = getClientSpaceId(child.getDomainFatherId());
      SpaceAndChildren father = spaceTrees.get(fatherId);
      if (father != null) {
        father.addComponent(child);
      }
    }

    return spaceTrees;
  }

  @Override
  public List<SpaceInstLight> getUserSpaceTreeview(String userId) throws Exception {

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
        treeview.add(TreeCache.getSpaceInstLight(currentSpaceId));
        addAuthorizedSpaceToTree(treeview, authorizedIds, currentSpaceId, 1);
      }
    }
    return treeview;
  }

  void addAuthorizedSpaceToTree(List<SpaceInstLight> treeview, Set<Integer> authorizedIds,
      int spaceId, int level) {
    List<SpaceInstLight> subSpaces = TreeCache.getSubSpaces(spaceId);
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
  void addAuthorizedSpace(Set<Integer> spaces, Set<String> componentsId, SpaceInstLight space) {
    if (space != null && !SpaceInst.STATUS_REMOVED.equals(space.getStatus()) &&
        !spaces.contains(space.getLocalId())) {
      int spaceId = space.getLocalId();
      spaces.add(spaceId);
      componentsId.removeAll(TreeCache.getComponentIds(spaceId));
      if (!space.isRoot()) {
        int fatherId = getDriverSpaceId(space.getFatherId());
        if (!spaces.contains(fatherId)) {
          SpaceInstLight parent = TreeCache.getSpaceInstLight(fatherId);
          addAuthorizedSpace(spaces, componentsId, parent);
        }
      }
    }
  }

  void filterSpaceFromComponents(Set<Integer> spaces, Set<String> componentsId, String componentId) {
    SpaceInstLight space = TreeCache.getSpaceContainingComponent(componentId);
    addAuthorizedSpace(spaces, componentsId, space);
    if (!componentsId.isEmpty()) {
      String newComponentId = componentsId.iterator().next();
      componentsId.remove(newComponentId);
      filterSpaceFromComponents(spaces, componentsId, newComponentId);
    }
  }

  @Override
  public String[] getAllowedSubSpaceIds(String userId, String spaceFatherId) throws AdminException {
    return getUserSubSpaceIds(userId, spaceFatherId);
  }

  private SpaceInstLight getSpaceInstLight(int spaceId)
      throws AdminException {

    return getSpaceInstLight(spaceId, -1);
  }

  private SpaceInstLight getSpaceInstLight(int spaceId, int level) throws AdminException {

    SpaceInstLight sil = TreeCache.getSpaceInstLight(spaceId);
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    if (sil == null) {
      sil = spaceManager.getSpaceInstLightById(domainDriverManager, spaceId);
    }
    if (sil != null) {
      if (level != -1) {
        sil.setLevel(level);
      }
      if (sil.getLevel() == -1) {
        sil.setLevel(TreeCache.getSpaceLevel(spaceId));
      }
    }
    return sil;
  }

  @Override
  public SpaceInstLight getSpaceInstLightById(String sClientSpaceId) throws AdminException {
    try {
      return getSpaceInstLight(getDriverSpaceId(sClientSpaceId));
    } catch (Exception e) {
      throw new AdminException("Admin.getSpaceInstLightById",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_SPACE", " space Id : '" + sClientSpaceId
          + "'", e);
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
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
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
        childSpaceIds = spaceManager.getAllSubSpaceIds(domainDriverManager, spaceId);
        // add them in result
        for (String childSpaceId : childSpaceIds) {
          if (!alManageableSpaceIds.contains(childSpaceId)) {
            alManageableSpaceIds.add(childSpaceId);
          }
        }
      }

      // Put user manageable space ids in cache
      asManageableSpaceIds = alManageableSpaceIds.toArray(new String[alManageableSpaceIds.size()]);

      return asManageableSpaceIds;
    } catch (Exception e) {
      throw new AdminException("Admin.getGroupManageableSpaceIds",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_MANAGEABLE_SPACE_IDS", "group Id : '" + sGroupId + "'", e);
    }
  }

  @Override
  public String[] getUserManageableSpaceIds(String sUserId) throws AdminException {
    Integer[] asManageableSpaceIds;
    ArrayList<String> alManageableSpaceIds = new ArrayList<>();
    ArrayList<Integer> alDriverManageableSpaceIds = new ArrayList<>();
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      // Get user manageable space ids from cache
      asManageableSpaceIds = cache.getManageableSpaceIds(sUserId);
      if (asManageableSpaceIds == null) {
        // Get user manageable space ids from database

        List<String> groupIds = getAllGroupsOfUser(sUserId);
        asManageableSpaceIds = userManager.getManageableSpaceIds(sUserId, groupIds);

        // Inherits manageability rights for space children
        String[] childSpaceIds;
        for (Integer asManageableSpaceId : asManageableSpaceIds) {
          // add manageable space id in result
          String asManageableSpaceIdAsString = String.valueOf(asManageableSpaceId);
          if (!alManageableSpaceIds.contains(asManageableSpaceIdAsString)) {
            alManageableSpaceIds.add(asManageableSpaceIdAsString);
            alDriverManageableSpaceIds.add(asManageableSpaceId);
          }

          // calculate manageable space's childs
          childSpaceIds = spaceManager.getAllSubSpaceIds(domainDriverManager,
              asManageableSpaceId);

          // add them in result
          for (String childSpaceId : childSpaceIds) {
            if (!alManageableSpaceIds.contains(childSpaceId)) {
              alManageableSpaceIds.add(childSpaceId);
              alDriverManageableSpaceIds.add(getDriverSpaceId(childSpaceId));
            }
          }
        }

        // Put user manageable space ids in cache
        asManageableSpaceIds = alDriverManageableSpaceIds.toArray(
            new Integer[alDriverManageableSpaceIds.size()]);
        cache.putManageableSpaceIds(sUserId, asManageableSpaceIds);
      }
      return alManageableSpaceIds.toArray(new String[alManageableSpaceIds.size()]);
    } catch (Exception e) {
      throw new AdminException("Admin.getUserManageableSpaceIds",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_MANAGEABLE_SPACE_IDS", "user Id : '" + sUserId + "'", e);
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
        SpaceInstLight space = TreeCache.getSpaceInstLight(asManageableSpaceId);
        if (space != null && space.isRoot()) {
          manageableRootSpaceIds.add(asManageableSpaceId.toString());
        }
      }
      return manageableRootSpaceIds.toArray(new String[manageableRootSpaceIds.size()]);

    } catch (Exception e) {
      throw new AdminException("Admin.getUserManageableSpaceRootIds",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_MANAGEABLE_SPACE_IDS", "user Id : '" + sUserId + "'", e);
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
        SpaceInstLight space = TreeCache.getSpaceInstLight(manageableSpaceId);
        while (space != null && !space.isRoot() && !find) {
          int driverFatherId = getDriverSpaceId(space.getFatherId());
          if (parentSpaceId == driverFatherId) {
            manageableRootSpaceIds.add(String.valueOf(manageableSpaceId));
            find = true;
          } else {
            space = TreeCache.getSpaceInstLight(driverFatherId);
          }
        }
      }
      return manageableRootSpaceIds.toArray(new String[manageableRootSpaceIds.size()]);
    } catch (Exception e) {
      throw new AdminException("Admin.getManageableSubSpaceIds",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_MANAGEABLE_SPACE_IDS", "user Id : '" + sUserId + "' Space = "
          + sParentSpaceId, e);
    }
  }

  @Override
  public List<String> getUserManageableGroupIds(String sUserId) throws AdminException {
    try {
      // get all groups of user
      List<String> groupIds = getAllGroupsOfUser(sUserId);

      return groupManager.getManageableGroupIds(sUserId, groupIds);
    } catch (Exception e) {
      throw new AdminException("Admin.getUserManageableGroupIds",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_MANAGEABLE_GROUP_IDS", "userId + " + sUserId,
          e);
    }
  }

  @Override
  public String[] getAvailCompoIds(String sClientSpaceId, String sUserId)
      throws AdminException {
    String[] asAvailCompoIds;

    try {
      // Converts client space id to driver space id
      int spaceId = getDriverSpaceId(sClientSpaceId);

      // Get available component ids from cache
      asAvailCompoIds = cache.getAvailCompoIds(spaceId, sUserId);

      if (asAvailCompoIds == null) {
        // Get available component ids from database
        List<ComponentInstLight> components = getAvailCompoInSpace(sUserId, sClientSpaceId);

        List<String> componentIds = new ArrayList<>();
        for (ComponentInstLight component : components) {
          componentIds.add(component.getId());
        }

        asAvailCompoIds = componentIds.toArray(new String[componentIds.size()]);

        // Store available component ids in cache
        cache.putAvailCompoIds(String.valueOf(spaceId), sUserId, asAvailCompoIds);
      }
      return asAvailCompoIds;

      // return getClientComponentIds(asAvailCompoIds);
    } catch (Exception e) {
      throw new AdminException("Admin.getAvailCompoIds",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", "user Id : '" + sUserId + "'", e);
    }
  }

  @Override
  public boolean isAnAdminTool(String toolId) {
    return ADMIN_COMPONENT_ID.equals(toolId);
  }

  @Override
  public boolean isComponentAvailable(String componentId, String userId)
      throws AdminException {
    try {
      return getAllowedComponentIds(userId).contains(componentId);
    } catch (Exception e) {
      throw new AdminException("Admin.isComponentAvailable",
          SilverpeasException.ERROR, "admin.EX_ERR_IS_COMPONENT_AVAILABLE",
          "user Id : '" + userId + "'" + " , component Id : '" + componentId + "'", e);
    }
  }

  @Override
  public boolean isComponentManageable(String componentId, String userId) throws AdminException {
    boolean manageable = getUserDetail(userId).isAccessAdmin();
    if (!manageable) {
      // check if user is manager of at least one space parent
      String[] spaceIds = getUserManageableSpaceIds(userId);
      ComponentInstLight component = getComponentInstLight(componentId);
      if (component != null) {
        List<String> toCheck = Arrays.asList(spaceIds);
        final int driverSpaceId = getDriverSpaceId(component.getDomainFatherId());
        manageable = toCheck.contains(String.valueOf(driverSpaceId));
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

      return asAvailCompoIds.toArray(new String[asAvailCompoIds.size()]);
    } catch (Exception e) {
      throw new AdminException("Admin.getAvailCompoIds",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", "user Id : '" + sUserId + "'", e);
    }
  }

  @Override
  public List<String> getAvailCompoIdsAtRoot(String sClientSpaceId, String sUserId,
      String componentNameRoot) throws AdminException {

    try {
      // Converts client space id to driver space id
      int spaceId = getDriverSpaceId(sClientSpaceId);

      // Get available component ids from database
      List<ComponentInstLight> components = TreeCache.getComponents(spaceId);

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
      throw new AdminException("Admin.getAvailCompoIdsAtRoot",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", "user Id : '" + sUserId + "'", e);
    }
  }

  @Override
  public String[] getAvailCompoIds(String userId) throws AdminException {
    try {
      List<String> componentIds = getAllowedComponentIds(userId);

      return componentIds.toArray(new String[componentIds.size()]);
    } catch (Exception e) {
      throw new AdminException("Admin.getAvailCompoIds",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", "user Id : '" + userId + "'", e);
    }
  }

  @Override
  public String[] getAvailDriverCompoIds(String sClientSpaceId, String sUserId)
      throws AdminException {
    try {
      // Get available component ids
      List<ComponentInstLight> components = getAvailCompoInSpace(sUserId, sClientSpaceId);

      List<String> componentIds = new ArrayList<>();
      for (ComponentInstLight component : components) {
        componentIds.add(component.getId());
      }

      return componentIds.toArray(new String[componentIds.size()]);
    } catch (Exception e) {
      throw new AdminException("Admin.getAvailDriverCompoIds",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", "user Id : '" + sUserId + "'", e);
    }
  }

  @Override
  public String[] getComponentIdsByNameAndUserId(String sUserId, String sComponentName)
      throws AdminException {

    List<String> allowedComponentIds = getAllowedComponentIds(sUserId, sComponentName);
    return allowedComponentIds.toArray(new String[allowedComponentIds.size()]);
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
      List<SpaceInstLight> path = TreeCache.getComponentPath(component.getId());
      if (path != null && !path.isEmpty()) {
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
      List<SpaceInstLight> path = TreeCache.getComponentPath(component.getId());
      for (SpaceInstLight space : path) {
        if (getDriverSpaceId(space.getFatherId()) == driverSpaceId) {
          if (!spaces.contains(space)) {
            spaces.add(space);
          }
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

      return alCompoSpace.toArray(new CompoSpace[alCompoSpace.size()]);
    } catch (Exception e) {
      throw new AdminException("Admin.getCompoForUser",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_AVAILABLE_INSTANCES_OF_COMPONENT",
          "user Id : '" + sUserId + "', component name: '" + sComponentName + "'", e);
    }
  }

  @Override
  public String[] getCompoId(String sComponentName) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    try {
      // Build the list of instanciated components with given componentName
      String[] matchingComponentIds = componentManager.getAllCompoIdsByComponentName(
          domainDriverManager, sComponentName);

      // check TreeCache to know if component is not removed neither into a removed space
      List<String> shortIds = new ArrayList<>();
      for (String componentId : matchingComponentIds) {
        ComponentInstLight component = TreeCache.getComponent(sComponentName + componentId);
        if (component != null) {
          shortIds.add(componentId);
        }
      }
      return shortIds.toArray(new String[shortIds.size()]);
    } catch (Exception e) {
      throw new AdminException("Admin.getCompoId", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_AVAILABLE_INSTANCES_OF_COMPONENT",
          "component name: '" + sComponentName + "'", e);
    }
  }

  @Override
  public String[] getProfileIds(String sUserId) throws AdminException {
    try {
      // Get the profile ids from cache
      String[] asProfilesIds = cache.getProfileIds(sUserId);

      if (asProfilesIds == null) {
        // retrieve value from database
        asProfilesIds = profileManager.getProfileIdsOfUser(sUserId, getAllGroupsOfUser(sUserId));

        // store values in cache
        if (asProfilesIds != null) {
          cache.putProfileIds(sUserId, asProfilesIds);
        }
      }

      return asProfilesIds;
    } catch (Exception e) {
      throw new AdminException("Admin.getProfileIds", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_PROFILES", "user Id : '" + sUserId + "'", e);
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
      SilverTrace.error("admin", "Admin.getCurrentProfiles", "admin.MSG_ERR_GET_CURRENT_PROFILE",
          e);
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

      for (ProfileInst profile : componentInst.getAllProfilesInst()) {
        if (profile != null) {
          if (profile.getName().equals(sProfile) || bAllProfiles) {
            // add direct users
            alUserIds.addAll(profile.getAllUsers());

            // add users of groups
            List<String> groupIds = profile.getAllGroups();
            for (String groupId : groupIds) {
              List<String> subGroupIds = groupManager.getAllSubGroupIdsRecursively(groupId);
              // add current group
              subGroupIds.add(groupId);
              if (subGroupIds != null && subGroupIds.size() > 0) {
                UserDetail[] users = userManager.getAllUsersOfGroups(subGroupIds);
                for (UserDetail user : users) {
                  alUserIds.add(user.getId());
                }
              }
            }
          }
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
      throw new AdminException("Admin.getUsers", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USERS_FOR_PROFILE_AND_COMPONENT", "profile : '" + sProfile
          + "', space Id: '" + sClientSpaceId + "' component Id: '" + sClientComponentId, e);
    }
  }

  @Override
  public Group[] getAllSubGroups(String parentGroupId) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    String[] theIds = groupManager.getAllSubGroupIds(domainDriverManager, parentGroupId);
    return getGroups(theIds);
  }

  @Override
  public UserDetail[] getFiltredDirectUsers(String sGroupId, String sUserLastNameFilter)
      throws AdminException {
    Group theGroup = getGroup(sGroupId);

    if (theGroup == null) {
      return ArrayUtil.EMPTY_USER_DETAIL_ARRAY;
    }
    String[] usersIds = theGroup.getUserIds();
    if (usersIds == null || usersIds.length <= 0) {
      return ArrayUtil.EMPTY_USER_DETAIL_ARRAY;
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
    return matchedUsers.toArray(new UserDetail[matchedUsers.size()]);
  }

  @Override
  public int getAllSubUsersNumber(String sGroupId) throws AdminException {
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();

    if (!StringUtil.isDefined(sGroupId)) {
      return userManager.getUserNumber(domainDriverManager);
    } else {

      return groupManager.getTotalUserCountInGroup("", sGroupId);
    }
  }

  @Override
  public int getUsersNumberOfDomain(String domainId) throws AdminException {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.
        getCurrentDomainDriverManager();
    try {
      if (!StringUtil.isDefined(domainId)) {
        return userManager.getUserNumber(domainDriverManager);
      }
      if ("-1".equals(domainId)) {
        return 0;
      }
      return userManager.getUsersNumberOfDomain(domainDriverManager, domainId);
    } catch (Exception e) {
      throw new AdminException("Admin.getUsersOfDomain",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_DOMAIN", "domain Id : '" + domainId + "'",
          e);
    }
  }

  @Override
  public String[] getAdministratorUserIds(String fromUserId) throws AdminException {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.
        getCurrentDomainDriverManager();
    return userManager.getAllAdminIds(domainDriverManager, getUserDetail(fromUserId));
  }

  @Override
  public String getAdministratorEmail() {
    return administratorMail;
  }

  @Override
  public String getDAPIGeneralAdminId() {
    return "0";
  }

  // -------------------------------------------------------------------------
  // CONNECTION TOOLS
  // -------------------------------------------------------------------------
  /**
   * Open a connection
   */
  private Connection openConnection(boolean bAutoCommit) throws AdminException {
    try {
      // Get the connection to the DB
      Connection connection = DBUtil.openConnection();
      connection.setAutoCommit(bAutoCommit);
      return connection;
    } catch (Exception e) {
      throw new AdminException("Admin.openConnection", SilverpeasException.FATAL,
          "root.EX_CONNECTION_OPEN_FAILED", e);
    }
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
  public String[] getAllSpaceIds(String sUserId) throws Exception {
    return getClientSpaceIds(getUserSpaceIds(sUserId));
  }

  @Override
  public String[] getAllRootSpaceIds(String sUserId) throws Exception {
    return getClientSpaceIds(getUserRootSpaceIds(sUserId));
  }

  @Override
  public String[] getAllSubSpaceIds(String sSpaceId, String sUserId)
      throws Exception {
    return getUserSubSpaceIds(sUserId, sSpaceId);
  }

  @Override
  public String[] getAllComponentIds(String sSpaceId) throws Exception {
    List<String> alCompoIds = new ArrayList<>();

    // Get the compo of this space
    SpaceInst spaceInst = getSpaceInstById(sSpaceId);
    List<ComponentInst> alCompoInst = spaceInst.getAllComponentsInst();

    if (alCompoInst != null) {
      for (ComponentInst anAlCompoInst : alCompoInst) {
        alCompoIds.add(anAlCompoInst.getId());
      }
    }

    return alCompoIds.toArray(new String[alCompoIds.size()]);
  }

  @Override
  public String[] getAllComponentIdsRecur(String sSpaceId) throws Exception {
    List<ComponentInstLight> components = TreeCache.getComponentsInSpaceAndSubspaces(
        getDriverSpaceId(sSpaceId));

    List<String> componentIds = new ArrayList<>();
    for (ComponentInstLight component : components) {
      componentIds.add(component.getId());
    }
    return componentIds.toArray(new String[componentIds.size()]);
  }

  @Override
  public String[] getAllComponentIdsRecur(String sSpaceId, String sUserId, String componentNameRoot,
      boolean inCurrentSpace, boolean inAllSpaces) throws Exception {
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

  /**
   * Return all the components Id recursively in (Space+subspaces, or only subspaces) available in
   * webactiv given a userId and a componentNameRoot
   *
   * @param sSpaceId
   * @param sUserId
   * @param componentNameRoot
   * @param inCurrentSpace
   * @return ArrayList of componentsIds
   * @author dlesimple
   */
  private ArrayList<String> getAllComponentIdsRecur(String sSpaceId, String sUserId,
      String componentNameRoot, boolean inCurrentSpace) throws Exception {
    ArrayList<String> alCompoIds = new ArrayList<>();
    getComponentIdsByNameAndUserId(sUserId, componentNameRoot);
    // Get components in the root of the space
    if (inCurrentSpace) {
      String[] componentIds = getAvailCompoIdsAtRoot(sSpaceId, sUserId);
      if (componentIds != null) {
        for (String componentId : componentIds) {
          ComponentInstLight compo = getComponentInstLight(componentId);
          if (compo.getName().equals(componentNameRoot)) {
            alCompoIds.add(compo.getId());
          }
        }
      }
    }

    // Get components in sub spaces
    String[] asSubSpaceIds = getAllSubSpaceIds(sSpaceId);
    for (int nI = 0; asSubSpaceIds != null && nI < asSubSpaceIds.length; nI++) {

      SpaceInst spaceInst = getSpaceInstById(asSubSpaceIds[nI]);
      String[] componentIds = getAvailCompoIds(spaceInst.getId(), sUserId);

      if (componentIds != null) {
        for (String componentId : componentIds) {
          ComponentInstLight compo = getComponentInstLight(componentId);
          if (compo.getName().equals(componentNameRoot)) {

            alCompoIds.add(compo.getId());
          }
        }
      }
    }
    return alCompoIds;
  }

  @Override
  public void synchronizeGroupByRule(String groupId, boolean scheduledMode) throws AdminException {

    Group group = getGroup(groupId);
    String rule = group.getRule();
    String domainId = group.getDomainId();
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.
        getCurrentDomainDriverManager();
    if (StringUtil.isDefined(rule)) {
      try {
        if (!scheduledMode) {
          SynchroGroupReport.setReportLevel(Level.DEBUG);
          SynchroGroupReport.startSynchro();
        }
        SynchroGroupReport.warn("admin.synchronizeGroup", "Synchronisation du groupe '" + group.
            getName() + "' - Regle de synchronisation = \"" + rule + "\"");
        List<String> actualUserIds = Arrays.asList(group.getUserIds());
        domainDriverManager.startTransaction(false);

        // Getting users according to rule
        List<String> userIds = new ArrayList<>();

        if (rule.toLowerCase().startsWith("ds_")) {
          if (rule.toLowerCase().startsWith("ds_accesslevel")) {
            userIds = synchronizeGroupByAccessRoleRule(rule, domainId);
          } else if (rule.toLowerCase().startsWith("ds_domain")) {
            userIds = synchronizeGroupByDomainRule(rule, domainId);
          }
        } else if (rule.toLowerCase().startsWith("dc_")) {
          // Extracting property name and searching property value
          String propertyName = rule.substring(rule.indexOf("_") + 1, rule.indexOf("=")).trim();
          String propertyValue = rule.substring(rule.indexOf("=") + 1).trim();

          if (domainId == null) {
            // All users by extra information
            Domain[] domains = getAllDomains();
            for (Domain domain : domains) {
              userIds.addAll(
                  getUserIdsBySpecificProperty(domain.getId(), propertyName, propertyValue));
            }
          } else {
            userIds = getUserIdsBySpecificProperty(domainId, propertyName, propertyValue);
          }
        } else {
          SilverTrace.error("admin", "Admin.synchronizeGroup", "admin.MSG_ERR_SYNCHRONIZE_GROUP",
              "rule '" + rule + "' for groupId '" + groupId + "' is not correct !");
        }

        // Add users
        List<String> newUsers = new ArrayList<>();
        if (userIds != null) {
          for (String userId : userIds) {
            if (!actualUserIds.contains(userId)) {
              newUsers.add(userId);
              SynchroGroupReport
                  .info("admin.synchronizeGroup", "Ajout de l'utilisateur " + userId);
            }
          }
        }
        SynchroGroupReport.warn("admin.synchronizeGroup",
            "Ajout de " + newUsers.size() + " utilisateur(s)");
        if (!newUsers.isEmpty()) {
          domainDriverManager.getOrganization().group.addUsersInGroup(newUsers.toArray(
              new String[newUsers.size()]), Integer.parseInt(groupId), false);
        }

        // Remove users
        List<String> removedUsers = new ArrayList<>();
        for (String actualUserId : actualUserIds) {
          if (userIds == null || !userIds.contains(actualUserId)) {
            removedUsers.add(actualUserId);
            SynchroGroupReport
                .info("admin.synchronizeGroup", "Suppression de l'utilisateur " + actualUserId);
          }
        }
        SynchroGroupReport.warn("admin.synchronizeGroup", "Suppression de " + removedUsers.size()
            + " utilisateur(s)");
        if (removedUsers.size() > 0) {
          domainDriverManager.getOrganization().group.removeUsersFromGroup(
              removedUsers.toArray(new String[removedUsers.size()]), Integer.parseInt(groupId),
              false);
        }
        domainDriverManager.commit();
      } catch (Exception e) {
        try {
          // Roll back the transactions
          domainDriverManager.rollback();
        } catch (Exception e1) {
          SilverTrace.error("admin", "Admin.synchronizeGroup", "root.EX_ERR_ROLLBACK", e1);
        }
        SynchroGroupReport.error("admin.synchronizeGroup",
            "Problème lors de la synchronisation : " + e.getMessage(), null);
        throw new AdminException("Admin.synchronizeGroup",
            SilverpeasException.ERROR, "admin.MSG_ERR_SYNCHRONIZE_GROUP",
            "groupId : '" + groupId + "'", e);
      } finally {
        if (!scheduledMode) {
          SynchroGroupReport.stopSynchro();
        }
        domainDriverManager.releaseOrganizationSchema();
      }
    }
  }

  private List<String> synchronizeGroupByDomainRule(String rule, String domainId)
      throws AdminPersistenceException {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.
        getCurrentDomainDriverManager();
    List<String> userIds = Collections.emptyList();
    // Extracting domain id
    String dId = rule.substring(rule.indexOf("=") + 1).trim();
    // Available only for "domaine mixte"
    if (domainId == null || "-1".equals(domainId)) {
      userIds = Arrays.asList(domainDriverManager.getOrganization().user.getUserIdsOfDomain(
          Integer.parseInt(dId)));
    }
    return userIds;
  }

  private List<String> synchronizeGroupByAccessRoleRule(String rule, String domainId)
      throws AdminException {
    List<String> userIds;// Extracting access level
    String accessLevel = rule.substring(rule.indexOf("=") + 1).trim();
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.
        getCurrentDomainDriverManager();
    if ("*".equalsIgnoreCase(accessLevel)) {
      // All users In case of "Domaine mixte", we retrieve all users of all domains
      // Else we get only users of group's domain
      if (domainId == null) {
        userIds = Arrays.asList(userManager.getAllUsersIds(domainDriverManager));
      } else {
        userIds = Arrays.asList(userManager.getUserIdsOfDomain(domainDriverManager, domainId));
      }
    } else {
      // All users by access level
      if (domainId == null) {
        userIds = Arrays.asList(domainDriverManager.getOrganization().user.getUserIdsByAccessLevel(
            UserAccessLevel.fromCode(accessLevel)));
      } else {
        userIds = Arrays.asList(userManager.getUserIdsOfDomainAndAccessLevel(domainDriverManager,
            domainId, UserAccessLevel.fromCode(accessLevel)));
      }
    }
    return userIds;
  }

  private List<String> getUserIdsBySpecificProperty(String domainId, String propertyName,
      String propertyValue) throws AdminException {
    int iDomainId = Integer.parseInt(domainId);
    UserDetail[] users = ArrayUtil.EMPTY_USER_DETAIL_ARRAY;
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.
        getCurrentDomainDriverManager();
    DomainDriver domainDriver = null;
    try {
      domainDriver = domainDriverManager.getDomainDriver(iDomainId);
    } catch (Exception e) {
      SynchroGroupReport.info("admin.getUserIdsBySpecificProperty",
          "Erreur ! Domaine " + iDomainId + " inaccessible !");
    }

    if (domainDriver != null) {
      try {
        users = domainDriver.getUsersBySpecificProperty(propertyName,
            propertyValue);
        if (users == null) {
          SynchroGroupReport.info("admin.getUserIdsBySpecificProperty",
              "La propriété '" + propertyName + "' n'est pas définie dans le domaine "
              + iDomainId);
        }
      } catch (Exception e) {
        SynchroGroupReport.info("admin.getUserIdsBySpecificProperty", "Domain " + domainId
            + " ne supporte pas les groupes synchronisés");
      }
    }

    List<String> specificIds = new ArrayList<>();
    if (users != null) {
      for (UserDetail user : users) {
        specificIds.add(user.getSpecificId());
      }
    }

    // We have to find users according to theirs specificIds
    UserRow[] usersInDomain = domainDriverManager.getOrganization().user.getUsersBySpecificIds(
        iDomainId, specificIds);
    List<String> userIds = new ArrayList<>();
    if (usersInDomain != null) {
      for (UserRow userInDomain : usersInDomain) {
        userIds.add(Integer.toString(userInDomain.id));
      }
    }

    return userIds;
  }

  // //////////////////////////////////////////////////////////
  // Synchronization tools
  // //////////////////////////////////////////////////////////
  private List<String> translateGroupIds(String sDomainId, String[] groupSpecificIds,
      boolean recursGroups) throws Exception {
    List<String> convertedGroupIds = new ArrayList<>();
    String groupId;
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.
        getCurrentDomainDriverManager();
    for (String groupSpecificId : groupSpecificIds) {
      try {
        groupId = groupManager.getGroupIdBySpecificIdAndDomainId(domainDriverManager,
            groupSpecificId, sDomainId);
      } catch (AdminException e) {
        // The group doesn't exist -> Synchronize him
        groupId = null;
        SilverTrace.warn("admin", "Admin.translateGroupIds",
            "admin.EX_ERR_GROUP_NOT_FOUND", "SpecId=" + groupSpecificId, e);
        if (recursGroups) {
          try {
            groupId = synchronizeImportGroup(sDomainId, groupSpecificId,
                null, true, true);
          } catch (AdminException ex) {
            // The group's synchro failed -> ignore him
            SilverTrace.warn("admin", "Admin.translateGroupIds",
                "admin.MSG_ERR_SYNCHRONIZE_GROUP", "SpecId=" + groupSpecificId, ex);
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

  private String[] translateUserIds(String sDomainId, String[] userSpecificIds)
      throws Exception {
    List<String> convertedUserIds = new ArrayList<>();
    String userId;
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.
        getCurrentDomainDriverManager();
    for (String userSpecificId : userSpecificIds) {
      try {
        userId = userManager.getUserIdBySpecificIdAndDomainId(domainDriverManager, userSpecificId,
            sDomainId);
      } catch (AdminException e) {
        // The user doesn't exist -> Synchronize him
        SilverTrace.warn("admin", "Admin.translateUserIds",
            "admin.EX_ERR_USER_NOT_FOUND", "SpecId=" + userSpecificId, e);
        try {
          userId = synchronizeImportUser(sDomainId, userSpecificId, false);
        } catch (AdminException ex) {
          // The user's synchro failed -> Ignore him
          SilverTrace.warn("admin", "Admin.translateUserIds",
              "admin.MSG_ERR_SYNCHRONIZE_USER", "SpecId=" + userSpecificId,
              ex);
          userId = null;
        }
      }
      if (userId != null) {
        convertedUserIds.add(userId);
      }
    }
    return convertedUserIds.toArray(new String[convertedUserIds.size()]);
  }

  @Override
  public String synchronizeGroup(String groupId, boolean recurs) throws Exception {

    Group theGroup = getGroup(groupId);
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.
        getCurrentDomainDriverManager();
    if (theGroup.isSynchronized()) {
      synchronizeGroupByRule(groupId, false);
    } else {
      DomainDriver synchroDomain = domainDriverManager.getDomainDriver(Integer.parseInt(theGroup
          .getDomainId()));
      Group gr = synchroDomain.synchroGroup(theGroup.getSpecificId());

      gr.setId(groupId);
      gr.setDomainId(theGroup.getDomainId());
      gr.setSuperGroupId(theGroup.getSuperGroupId());
      internalSynchronizeGroup(synchroDomain, gr, recurs);
    }
    return groupId;
  }

  @Override
  public String synchronizeImportGroup(String domainId, String groupKey, String askedParentId,
      boolean recurs, boolean isIdKey) throws Exception {

    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.
        getCurrentDomainDriverManager();
    DomainDriver synchroDomain = domainDriverManager.getDomainDriver(Integer.parseInt(
        domainId));
    Group gr;

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
        parentId = groupManager.getGroupIdBySpecificIdAndDomainId(
            domainDriverManager, parentSpecificIds[i], domainId);
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
      throw new AdminException("Admin.synchronizeImportGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_GROUP_PARENT_NOT_PRESENT",
          "group name : '" + groupKey + "'");
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
  public String synchronizeRemoveGroup(String groupId) throws Exception {

    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.
        getCurrentDomainDriverManager();
    Group theGroup = getGroup(groupId);
    DomainDriver synchroDomain = domainDriverManager.getDomainDriver(
        Integer.parseInt(theGroup.getDomainId()));
    synchroDomain.removeGroup(theGroup.getSpecificId());
    return deleteGroupById(groupId, true);
  }

  protected void internalSynchronizeGroup(DomainDriver synchroDomain,
      Group latestGroup, boolean recurs) throws Exception {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.
        getCurrentDomainDriverManager();
    latestGroup.setUserIds(translateUserIds(latestGroup.getDomainId(),
        latestGroup.getUserIds()));
    updateGroup(latestGroup, true);
    if (recurs) {
      Group[] childs = synchroDomain.getGroups(latestGroup.getSpecificId());

      for (final Group child : childs) {
        String existingGroupId = null;
        try {
          existingGroupId = groupManager
              .getGroupIdBySpecificIdAndDomainId(domainDriverManager, child.getSpecificId(),
                  latestGroup.getDomainId());
          Group existingGroup = getGroup(existingGroupId);
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
  public String synchronizeUser(String userId, boolean recurs) throws Exception {
    Collection<UserDetail> listUsersUpdate = new ArrayList<>();
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.
        getCurrentDomainDriverManager();

    try {
      // Start transaction
      domainDriverManager.startTransaction(false);

      UserDetail theUserDetail = getUserDetail(userId);
      DomainDriver synchroDomain = domainDriverManager.getDomainDriver(Integer.parseInt(
          theUserDetail.getDomainId()));
      // Synchronize the user's infos
      UserDetail ud = synchroDomain.synchroUser(theUserDetail.getSpecificId());
      ud.setId(userId);
      ud.setAccessLevel(theUserDetail.getAccessLevel());
      ud.setDomainId(theUserDetail.getDomainId());
      if (!ud.equals(theUserDetail) ||
          (ud.getState() != UserState.UNKNOWN && ud.getState() != theUserDetail.getState())) {
        copyDistantUserIntoSilverpeasUser(ud, theUserDetail);
        userManager.updateUser(domainDriverManager, theUserDetail);
        cache.opUpdateUser(userManager.getUserDetail(domainDriverManager, userId));
      }
      // Synchro manuelle : Ajoute ou Met à jour l'utilisateur
      listUsersUpdate.add(ud);

      // Synchronize the user's groups
      String[] incGroupsSpecificId = synchroDomain.getUserMemberGroupIds(
          theUserDetail.getSpecificId());
      List<String> incGroupsId = translateGroupIds(theUserDetail.getDomainId(),
          incGroupsSpecificId, recurs);
      String[] oldGroupsId = groupManager.getDirectGroupsOfUser(domainDriverManager, userId);
      for (String oldGroupId : oldGroupsId) {
        if (incGroupsId.contains(oldGroupId)) { // No changes have to be
          // performed to the group -> Remove it
          incGroupsId.remove(oldGroupId);
        } else {
          Group grpToRemove = groupManager.getGroup(domainDriverManager, oldGroupId);
          if (theUserDetail.getDomainId().equals(grpToRemove.getDomainId())) {
            // Remove the user from this group
            groupManager.removeUserFromGroup(domainDriverManager, userId, oldGroupId);
            cache.opRemoveUserFromGroup(userId, oldGroupId);
          }
        }
      }
      // Now the remaining groups of the vector are the groups where the user is
      // newly added
      for (String includedGroupId : incGroupsId) {
        groupManager.addUserInGroup(domainDriverManager, userId, includedGroupId);
        cache.opAddUserInGroup(userId, includedGroupId);
      }

      // traitement spécifique des users selon l'interface implémentée
      processSpecificSynchronization(theUserDetail.getDomainId(), null, listUsersUpdate, null);

      // Commit the transaction
      domainDriverManager.commit();

      // return user id
      return userId;
    } catch (Exception e) {
      rollback();
      throw new AdminException("Admin.synchronizeUser", SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_USER", "user id : '" + userId + "'", e);
    } finally {
      domainDriverManager.releaseOrganizationSchema();
    }
  }

  @Override
  public String synchronizeImportUserByLogin(String domainId, String userLogin, boolean recurs)
      throws Exception {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.
        getCurrentDomainDriverManager();

    DomainDriver synchroDomain = domainDriverManager.getDomainDriver(Integer.parseInt(
        domainId));
    UserDetail ud = synchroDomain.importUser(userLogin);
    ud.setDomainId(domainId);
    String userId = addUser(ud, true);
    // Synchronizes the user to add it to the groups and recursivaly add the groups
    synchronizeUser(userId, recurs);
    return userId;
  }

  @Override
  public String synchronizeImportUser(String domainId, String specificId, boolean recurs) throws
      Exception {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.
        getCurrentDomainDriverManager();

    DomainDriver synchroDomain = domainDriverManager.getDomainDriver(Integer.parseInt(
        domainId));
    UserDetail ud = synchroDomain.getUser(specificId);

    ud.setDomainId(domainId);
    String userId = addUser(ud, true);
    // Synchronizes the user to add it to the groups and recursivaly add the groups
    synchronizeUser(userId, recurs);
    return userId;
  }

  @Override
  public List<DomainProperty> getSpecificPropertiesToImportUsers(String domainId, String language)
      throws Exception {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.
        getCurrentDomainDriverManager();

    DomainDriver synchroDomain = domainDriverManager.getDomainDriver(Integer.parseInt(
        domainId));
    return synchroDomain.getPropertiesToImport(language);
  }

  @Override
  public UserDetail[] searchUsers(String domainId, Map<String, String> query)
      throws Exception {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.
        getCurrentDomainDriverManager();

    DomainDriver synchroDomain = domainDriverManager.getDomainDriver(Integer.parseInt(domainId));
    return synchroDomain.getUsersByQuery(query);
  }

  @Override
  public String synchronizeRemoveUser(String userId) throws Exception {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.
        getCurrentDomainDriverManager();

    UserDetail theUserDetail = getUserDetail(userId);
    DomainDriver synchroDomain = domainDriverManager.getDomainDriver(Integer.parseInt(theUserDetail.
        getDomainId()));
    synchroDomain.removeUser(theUserDetail.getSpecificId());
    deleteUser(userId, true);
    List<UserDetail> listUsersRemove = new ArrayList<>();
    listUsersRemove.add(theUserDetail);
    processSpecificSynchronization(theUserDetail.getDomainId(), null, null, listUsersRemove);
    return userId;
  }

  @Override
  public String synchronizeSilverpeasWithDomain(String sDomainId) throws Exception {
    return synchronizeSilverpeasWithDomain(sDomainId, false);
  }

  @Override
  public String synchronizeSilverpeasWithDomain(String sDomainId, boolean threaded)
      throws AdminException {
    String sReport = "Starting synchronization...\n\n";
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.
        getCurrentDomainDriverManager();
    synchronized (semaphore) {

      // Démarrage de la synchro avec la Popup d'affichage
      if (threaded) {
        SynchroDomainReport.setReportLevel(Level.WARNING);
      }
      SynchroDomainReport.startSynchro();
      try {
        SynchroDomainReport.warn("admin.synchronizeSilverpeasWithDomain",
            "Domain '" + domainDriverManager.getDomain(sDomainId).getName() + "', Id : "
            + sDomainId);
        // Start synchronization
        domainDriverManager.beginSynchronization(sDomainId);

        DomainDriver synchroDomain = domainDriverManager
            .getDomainDriver(Integer.parseInt(sDomainId));
        Domain theDomain = domainDriverManager.getDomain(sDomainId);
        String fromTimeStamp = theDomain.getTheTimeStamp();
        String toTimeStamp = synchroDomain.getTimeStamp(fromTimeStamp);
        // Start transaction
        domainDriverManager.startTransaction(false);
        domainDriverManager.startTransaction(sDomainId, false);

        // Synchronize users
        boolean importUsers = synchroDomain.mustImportUsers() || threaded;
        sReport += synchronizeUsers(sDomainId, fromTimeStamp, toTimeStamp, threaded, importUsers);

        // Synchronize groups
        // Get all users of the domain from Silverpeas
        UserDetail[] silverpeasUDs = userManager.getUsersOfDomain(domainDriverManager, sDomainId);
        HashMap<String, String> userIdsMapping = getUserIdsMapping(silverpeasUDs);
        sReport += "\n" + synchronizeGroups(sDomainId, userIdsMapping, fromTimeStamp, toTimeStamp);

        // All the synchro is finished -> set the new timestamp
        // ----------------------------------------------------
        theDomain.setTheTimeStamp(toTimeStamp);
        updateDomain(theDomain);

        // Commit the transaction
        domainDriverManager.commit();
        domainDriverManager.commit(sDomainId);

        // End synchronization
        String sDomainSpecificErrors = domainDriverManager.endSynchronization(sDomainId, false);
        SynchroDomainReport.warn("admin.synchronizeSilverpeasWithDomain", "----------------"
            + sDomainSpecificErrors);
        return sReport + "\n----------------\n" + sDomainSpecificErrors;
      } catch (Exception e) {
        try {
          // End synchronization
          domainDriverManager.endSynchronization(sDomainId, true);
          // Roll back the transactions
          domainDriverManager.rollback();
          domainDriverManager.rollback(sDomainId);
        } catch (Exception e1) {
          SilverTrace.error("admin", "Admin.synchronizeSilverpeasWithDomain",
              "root.EX_ERR_ROLLBACK", e1);
        }
        SynchroDomainReport.error("admin.synchronizeSilverpeasWithDomain",
            "Problème lors de la synchronisation : " + e.getMessage(), null);
        throw new AdminException("Admin.synchronizeSilverpeasWithDomain",
            SilverpeasException.ERROR, "admin.EX_ERR_SYNCHRONIZE_DOMAIN",
            "domain id : '" + sDomainId + "'\nReport:" + sReport, e);
      } finally {
        SynchroDomainReport.stopSynchro();// Fin de synchro avec la Popup d'affichage
        // Reset the cache
        cache.resetCache();
        domainDriverManager.releaseOrganizationSchema();
      }
    }
  }

  /**
   * Merge the data of a distant user into the data of a silverpeas user : - user identifier (the
   * distant one) - first name - last name - e-mail - login
   *
   * @param distantUser
   * @param silverpeasUser
   */
  private void copyDistantUserIntoSilverpeasUser(UserDetail distantUser, UserDetail silverpeasUser) {
    silverpeasUser.setSpecificId(distantUser.getSpecificId());
    silverpeasUser.setFirstName(distantUser.getFirstName());
    silverpeasUser.setLastName(distantUser.getLastName());
    silverpeasUser.seteMail(distantUser.geteMail());
    silverpeasUser.setLogin(distantUser.getLogin());
    if (distantUser.isDeactivatedState()) {
      // In this case, the user account is deactivated from the LDAP
      silverpeasUser.setState(distantUser.getState());
    } else if (distantUser.isValidState() && silverpeasUser.isDeactivatedState()) {
      // In this case, the user account is activated from the LDAP, so the Silverpeas user
      // account is again activated only if it was deactivated. Indeed, if it was blocked for
      // example, it is still blocked after a synchronization
      silverpeasUser.setState(distantUser.getState());
    }
  }

  /**
   * Synchronize users between cache and domain's datastore
   */
  private String synchronizeUsers(String domainId, String fromTimeStamp, String toTimeStamp,
      boolean threaded, boolean importUsers) throws AdminException {
    String specificId;
    String sReport = "User synchronization : \n";
    String message;
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    Collection<UserDetail> addedUsers = new ArrayList<>();
    Collection<UserDetail> updateUsers = new ArrayList<>();
    Collection<UserDetail> removedUsers = new ArrayList<>();

    SynchroDomainReport.warn("admin.synchronizeUsers", "Starting users synchronization...");
    try {
      // Get all users of the domain from distant datasource
      DomainDriver domainDriver = domainDriverManager.getDomainDriver(Integer.parseInt(domainId));
      UserDetail[] distantUDs = domainDriver.getAllChangedUsers(fromTimeStamp, toTimeStamp);

      message = distantUDs.length
          + " user(s) have been changed in LDAP since the last synchronization";
      sReport += message + "\n";
      SynchroDomainReport.info("admin.synchronizeUsers", message);

      // Get all users of the domain from Silverpeas
      UserDetail[] silverpeasUDs = userManager.getUsersOfDomain(domainDriverManager, domainId);
      SynchroDomainReport.info("admin.synchronizeUsers", "Adding or updating users in database...");

      // Add new users or update existing ones from distant datasource
      for (UserDetail distantUD : distantUDs) {
        UserDetail userToUpdateFromDistantUser = null;
        specificId = distantUD.getSpecificId();


        // search for user in Silverpeas database
        for (final UserDetail silverpeasUD : silverpeasUDs) {
          if (silverpeasUD.getSpecificId().equals(specificId) || (shouldFallbackUserLogins
              && silverpeasUD.getLogin().equals(distantUD.getLogin()))) {
            userToUpdateFromDistantUser = silverpeasUD;
            copyDistantUserIntoSilverpeasUser(distantUD, userToUpdateFromDistantUser);
            break;
          }
        }

        if (userToUpdateFromDistantUser != null) {
          // update user
          updateUserDuringSynchronization(domainDriverManager, userToUpdateFromDistantUser,
              updateUsers, sReport);
        } else if (importUsers) {
          // add user
          distantUD.setDomainId(domainId);
          addUserDuringSynchronization(domainDriverManager, distantUD, addedUsers, sReport);
        }
      }

      if (!threaded || (threaded && delUsersOnDiffSynchro)) {
        // Delete obsolete users from Silverpeas
        SynchroDomainReport.info("admin.synchronizeUsers", "Removing users from database...");
        distantUDs = domainDriverManager.getAllUsers(domainId);
        for (UserDetail silverpeasUD : silverpeasUDs) {
          boolean bFound = false;
          specificId = silverpeasUD.getSpecificId();

          // search for user in distant datasource
          for (final UserDetail distantUD : distantUDs) {
            if (distantUD.getSpecificId().equals(specificId) || (shouldFallbackUserLogins
                && silverpeasUD.getLogin().equals(distantUD.getLogin()))) {
              bFound = true;
              break;
            }
          }

          // if found, do nothing, else delete
          if (!bFound) {
            deleteUserDuringSynchronization(domainDriverManager, silverpeasUD, removedUsers,
                sReport);
          }
        }
      }

      // traitement spécifique des users selon l'interface implémentée
      processSpecificSynchronization(domainId, addedUsers, updateUsers, removedUsers);

      message = "Users synchronization terminated";
      sReport += message + "\n";
      SynchroDomainReport.warn("admin.synchronizeUsers", message);
      message = "# of updated users : " + updateUsers.size() + ", added : " + addedUsers.size()
          + ", removed : " + removedUsers.size();
      sReport += message + "\n";
      SynchroDomainReport.warn("admin.synchronizeUsers", message);
      return sReport;
    } catch (Exception e) {
      SynchroDomainReport.error("admin.synchronizeUsers", "Problem during synchronization of users : "
          + e.getMessage(), null);
      throw new AdminException("admin.synchronizeUsers",
          SilverpeasException.ERROR, "admin.EX_ERR_SYNCHRONIZE_DOMAIN_USERS",
          "domainId : '" + domainId + "'\nReport:" + sReport, e);
    }
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

  private void updateUserDuringSynchronization(DomainDriverManager domainDriverManager,
      UserDetail distantUD, Collection<UserDetail> updatedUsers, String sReport) {
    String specificId = distantUD.getSpecificId();
    try {
      String silverpeasId = userManager.updateUser(domainDriverManager, distantUD);
      updatedUsers.add(distantUD);
      String message = "user " + distantUD.getDisplayedName() + " updated (id:" + silverpeasId
          + " / specificId:" + specificId + ")";
      SynchroDomainReport.warn("admin.synchronizeUsers", message);
      sReport += message + "\n";
    } catch (AdminException aeMaj) {
      SilverLogger.getLogger(this).error("Full synchro: error while updating user " + specificId,
          aeMaj);
      String message = "problem updating user " + distantUD.getDisplayedName() + " (specificId:"
          + specificId + ") - " + aeMaj.getMessage();
      SynchroDomainReport.warn("admin.synchronizeUsers", message);
    }
  }

  private void addUserDuringSynchronization(DomainDriverManager domainDriverManager,
      UserDetail distantUD, Collection<UserDetail> addedUsers, String sReport) {
    String specificId = distantUD.getSpecificId();
    try {
      String silverpeasId = userManager.addUser(domainDriverManager, distantUD, true);
      if (silverpeasId.equals("")) {
        String message = "problem adding user " + distantUD.getDisplayedName() + "(specificId:"
            + specificId + ") - Login and LastName must be set !!!";
        sReport += message + "\n";
        SynchroDomainReport.warn("admin.synchronizeUsers", message);
        sReport += "user has not been added\n";
      } else {

        addedUsers.add(distantUD);
        String message = "user " + distantUD.getDisplayedName() + " added (id:" + silverpeasId
            + " / specificId:" + specificId + ")";
        sReport += message + "\n";
        SynchroDomainReport.warn("admin.synchronizeUsers", message);
      }
    } catch (AdminException ae) {
      SilverLogger.getLogger(this).error("Full synchro: error while adding user " + specificId, ae);
      String message = "problem adding user " + distantUD.getDisplayedName() + "(specificId:"
          + specificId + ") - " + ae.getMessage();
      SynchroDomainReport.warn("admin.synchronizeUsers", message);
      sReport += message + "\n";
      sReport += "user has not been added\n";
    }
  }

  private void deleteUserDuringSynchronization(DomainDriverManager domainDriverManager,
      UserDetail silverpeasUD, Collection<UserDetail> deletedUsers, String sReport) {
    String specificId = silverpeasUD.getSpecificId();
    try {
      userManager.deleteUser(domainDriverManager, silverpeasUD, true);
      deletedUsers.add(silverpeasUD);
      String message = "user " + silverpeasUD.getDisplayedName() + " deleted (id:" + specificId
          + ")";
      sReport += message + "\n";
      SynchroDomainReport.warn("admin.synchronizeUsers", message);
    } catch (AdminException aeDel) {
      SilverLogger.getLogger(this).error("Full synchro: error while deleting user " + specificId,
          aeDel);
      String message = "problem deleting user " + silverpeasUD.getDisplayedName() + " (specificId:"
          + specificId + ") - " + aeDel.getMessage();
      sReport += message + "\n";
      SynchroDomainReport.warn("admin.synchronizeUsers", message);
      sReport += "user has not been deleted\n";
    }
  }

  private void processSpecificSynchronization(String domainId, Collection<UserDetail> usersAdded,
      Collection<UserDetail> usersUpdated, Collection<UserDetail> usersRemoved) throws Exception {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.
        getCurrentDomainDriverManager();
    Domain theDomain = domainDriverManager.getDomain(domainId);
    String propDomainFileName = theDomain.getPropFileName();
    SettingBundle propDomainLdap = ResourceLocator.getSettingBundle(propDomainFileName);
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
        SilverTrace.warn("admin", "admin.synchronizeOnlyExistingUsers",
            PARAM_MSG_KEY, "Pb Loading class traitement Users ! ", e);
      }
    }
  }

  /**
   * Synchronize groups between cache and domain's datastore
   */
  private String synchronizeGroups(String domainId, Map<String, String> userIds,
      String fromTimeStamp, String toTimeStamp) throws Exception {
    boolean bFound;
    String specificId;
    String sReport = "Group synchronization : \n";
    Map<String, Group> allDistantGroups = new HashMap<>();
    int iNbGroupsAdded = 0;
    int iNbGroupsMaj = 0;
    int iNbGroupsDeleted = 0;
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.
        getCurrentDomainDriverManager();
    SynchroDomainReport.warn("admin.synchronizeGroups", "Starting groups synchronization...");
    try {
      // Get all root groups of the domain from distant datasource
      Group[] distantRootGroups = domainDriverManager.getAllRootGroups(domainId);
      // Get all groups of the domain from Silverpeas
      Group[] silverpeasGroups = groupManager.getGroupsOfDomain(domainDriverManager, domainId);

      SynchroDomainReport.info("admin.synchronizeGroups", "Adding or updating groups in database...");
      // Check for new groups resursively
      sReport += checkOutGroups(domainId, silverpeasGroups, distantRootGroups, allDistantGroups,
          userIds, null, iNbGroupsAdded, iNbGroupsMaj, iNbGroupsDeleted);

      // Delete obsolete groups
      SynchroDomainReport.info("admin.synchronizeGroups", "Removing groups from database...");
      Group[] distantGroups = allDistantGroups.values().toArray(
          new Group[allDistantGroups.size()]);
      for (Group silverpeasGroup : silverpeasGroups) {
        bFound = false;
        specificId = silverpeasGroup.getSpecificId();

        // search for group in distant datasource
        for (int nJ = 0; nJ < distantGroups.length && !bFound; nJ++) {
          if (distantGroups[nJ].getSpecificId().equals(specificId)) {
            bFound = true;
          } else if (shouldFallbackGroupNames && distantGroups[nJ].getName().equals(specificId)) {
            bFound = true;
          }
        }

        // if found, do nothing, else delete
        if (!bFound) {
          try {
            groupManager.deleteGroupById(domainDriverManager, silverpeasGroup, true);
            iNbGroupsDeleted++;
            sReport += "deleting group " + silverpeasGroup.getName() + "(id:" + specificId + ")\n";
            SynchroDomainReport.warn("admin.synchronizeGroups", "Group " + silverpeasGroup.getName()
                + " deleted (SpecificId:" + specificId + ")");
          } catch (AdminException aeDel) {
            SilverLogger.getLogger(this).error("Full synchro: error while deleting group " +
                specificId, aeDel);
            sReport += "problem deleting group " + silverpeasGroup.getName() + " (specificId:"
                + specificId + ") - " + aeDel.getMessage() + "\n";
            sReport += "group has not been deleted\n";
          }
        }
      }
      sReport += "Groups synchronization terminated\n";
      SynchroDomainReport.info("admin.synchronizeGroups",
          "# of groups updated : " + iNbGroupsMaj + ", added : " + iNbGroupsAdded
          + ", deleted : " + iNbGroupsDeleted);
      SynchroDomainReport.warn("admin.synchronizeGroups", "Groups synchronization terminated");
      return sReport;
    } catch (Exception e) {
      SynchroDomainReport.error("admin.synchronizeGroups",
          "Problème lors de la synchronisation des groupes : " + e.getMessage(), null);
      throw new AdminException("admin.synchronizeGroups", SilverpeasException.ERROR,
          "admin.EX_ERR_SYNCHRONIZE_DOMAIN_GROUPS",
          "domain id : '" + domainId + "'\nReport:" + sReport, e);
    }
  }

  /**
   * Checks for new groups resursively
   */
  // Au 1er appel : (domainId,silverpeasGroups,distantRootGroups,
  // allDistantGroups(vide), userIds, null)
  // No need to refresh cache : the cache is reseted at the end of the
  // synchronization
  private String checkOutGroups(String domainId, Group[] existingGroups, Group[] testedGroups,
      Map<String, Group> allIncluededGroups, Map<String, String> userIds, String superGroupId,
      int iNbGroupsAdded, int iNbGroupsMaj, int iNbGroupsDeleted) throws Exception {
    boolean bFound;
    String specificId;
    String silverpeasId = null;
    String report = "";
    String result;
    for (Group testedGroup : testedGroups) {
      allIncluededGroups.put(testedGroup.getSpecificId(), testedGroup);
    }
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.
        getCurrentDomainDriverManager();
    // Add new groups or update existing ones from distant datasource
    for (Group testedGroup : testedGroups) {
      bFound = false;
      specificId = testedGroup.getSpecificId();


      // search for group in Silverpeas database
      for (int nJ = 0;
           nJ < existingGroups.length && !bFound;
           nJ++) {
        if (existingGroups[nJ].getSpecificId().equals(specificId)) {
          bFound = true;
          testedGroup.setId(existingGroups[nJ].getId());
        } else if (shouldFallbackGroupNames && existingGroups[nJ].getSpecificId().equals(
            testedGroup.getName())) {
          bFound = true;
          testedGroup.setId(existingGroups[nJ].getId());
        }
      }
      // Prepare Group to be at Silverpeas format
      testedGroup.setDomainId(domainId);

      // Set the Parent Id
      if (bFound) {
        SynchroDomainReport.debug("admin.checkOutGroups", "avant maj du groupe " + specificId
            + ", recherche de ses groupes parents");
      } else {
        SynchroDomainReport.debug("admin.checkOutGroups", "avant ajout du groupe " + specificId
            + ", recherche de ses groupes parents");
      }
      String[] groupParentsIds = domainDriverManager.getGroupMemberGroupIds(domainId, testedGroup.
          getSpecificId());
      if ((groupParentsIds == null) || (groupParentsIds.length == 0)) {
        testedGroup.setSuperGroupId(null);
        SynchroDomainReport.debug("admin.checkOutGroups", "le groupe " + specificId + " n'a pas de père");
      } else {
        testedGroup.setSuperGroupId(superGroupId);
        if (superGroupId != null)// sécurité
        {
          SynchroDomainReport.debug("admin.checkOutGroups",
              "le groupe " + specificId + " a pour père le groupe " + domainDriverManager.getGroup(
                  superGroupId).getSpecificId() + " d'Id base " + superGroupId);
        }
      }
      String[] userSpecificIds = testedGroup.getUserIds();
      List<String> convertedUserIds = new ArrayList<>();
      for (String userSpecificId : userSpecificIds) {
        if (userIds.get(userSpecificId) != null) {
          convertedUserIds.add(userIds.get(userSpecificId));
        }
      }
      // Le groupe contiendra une liste d'IDs de users existant ds la base et
      // non + une liste de logins récupérés via LDAP
      testedGroup.setUserIds(convertedUserIds.toArray(new String[convertedUserIds.size()]));
      // if found, update, else create
      if (bFound)// MAJ
      {
        try {

          result = groupManager.updateGroup(domainDriverManager, testedGroup, true);
          if (StringUtil.isDefined(result)) {
            iNbGroupsMaj++;
            silverpeasId = testedGroup.getId();
            report += "updating group " + testedGroup.getName() + "(id:" + specificId + ")\n";
            SynchroDomainReport.warn("admin.checkOutGroups", "maj groupe " + testedGroup.getName()
                + " (id:" + silverpeasId + ") OK");
          } else// le name groupe non renseigné
          {
            SilverLogger.getLogger(this).info("Full Synchro: error while updating group {0}",
                specificId);
            report += "problem updating group id : " + specificId + "\n";
          }
        } catch (AdminException aeMaj) {
          SilverLogger.getLogger(this).info("Full Synchro: error while updating group {0}: ",
              specificId, aeMaj.getMessage());
          report += "problem updating group " + testedGroup.getName() + " (id:" + specificId + ") "
              + aeMaj.getMessage() + "\n";
          report += "group has not been updated\n";
        }
      } else { // AJOUT
        try {
          silverpeasId = groupManager.addGroup(domainDriverManager, testedGroup, true);
          if (StringUtil.isDefined(silverpeasId)) {
            iNbGroupsAdded++;

            report += "adding group " + testedGroup.getName() + "(id:" + specificId + ")\n";
            SynchroDomainReport.warn("admin.checkOutGroups", "ajout groupe " + testedGroup.getName()
                + " (id:" + silverpeasId + ") OK");
          } else { // le name groupe non renseigné

            report += "problem adding group id : " + specificId + "\n";
          }
        } catch (AdminException aeAdd) {

          report += "problem adding group " + testedGroup.getName() + " (id:" + specificId + ") "
              + aeAdd.getMessage() + "\n";
          report += "group has not been added\n";
        }
      }
      // Recurse with subgroups
      if (silverpeasId != null && silverpeasId.length() > 0) {
        Group[] subGroups = domainDriverManager.getGroups(silverpeasId);
        if (subGroups != null && subGroups.length > 0) {
          Group[] cleanSubGroups = removeCrossReferences(subGroups,
              allIncluededGroups, specificId);
          if (cleanSubGroups != null && cleanSubGroups.length > 0) {
            SynchroDomainReport.info("admin.checkOutGroups",
                "Ajout ou mise à jour de " + cleanSubGroups.length + " groupes fils du groupe "
                + specificId + "...");
            report += checkOutGroups(domainId, existingGroups, cleanSubGroups,
                allIncluededGroups,
                userIds, silverpeasId, iNbGroupsAdded, iNbGroupsMaj, iNbGroupsDeleted);
          }
        }
      }
    }
    return report;
  }

  /**
   * Remove cross reference risk between groups
   */
  private Group[] removeCrossReferences(Group[] subGroups, Map<String, Group> allIncluededGroups,
      String fatherId) throws Exception {
    ArrayList<Group> cleanSubGroups = new ArrayList<>();
    //noinspection UnusedAssignment,UnusedAssignment,UnusedAssignment
    for (Group subGroup : subGroups) {
      if (allIncluededGroups.get(subGroup.getSpecificId()) == null) {
        cleanSubGroups.add(subGroup);
      } else {
        SilverTrace.warn("admin", "Admin.removeCrossReferences", PARAM_MSG_KEY,
            "Cross removed for child : " + subGroup.getSpecificId() + " of father : " + fatherId);
      }
    }
    return cleanSubGroups.toArray(new Group[cleanSubGroups.size()]);
  }

  // -------------------------------------------------------------------------
  // For SelectionPeas
  // -------------------------------------------------------------------------
  @Override
  public String[] searchUsersIds(String sGroupId, String componentId, String[] profileIds,
      UserDetail modelUser) throws AdminException {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.
        getCurrentDomainDriverManager();
    try {
      List<String> userIds = new ArrayList<>();
      if (StringUtil.isDefined(sGroupId)) {
        // search users in group and subgroups
        UserDetail[] users = getAllUsersOfGroup(sGroupId);
        for (UserDetail user : users) {
          userIds.add(user.getId());
        }
        if (userIds.isEmpty()) {
          userIds = null;
        }
      } else if (profileIds != null && profileIds.length > 0) {
        // search users in profiles
        for (String profileId : profileIds) {
          ProfileInst profile = profileManager.getProfileInst(domainDriverManager, profileId);
          // add users directly attach to profile
          userIds.addAll(profile.getAllUsers());

          // add users indirectly attach to profile (groups attached to profile)
          List<String> groupIds = profile.getAllGroups();
          List<String> allGroupIds = new ArrayList<>();
          for (String groupId : groupIds) {
            allGroupIds.add(groupId);
            allGroupIds.addAll(groupManager.getAllSubGroupIdsRecursively(groupId));
          }
          userIds.addAll(userManager.getAllUserIdsOfGroups(allGroupIds));
        }
        if (userIds.isEmpty()) {
          userIds = null;
        }
      } else if (StringUtil.isDefined(componentId)) {
        // search users in component
        userIds.addAll(getUserIdsForComponent(componentId));
        if (userIds.isEmpty()) {
          userIds = null;
        }
      } else {
        // get all users
        userIds = new ArrayList<>();
      }

      if (userIds == null) {
        return ArrayUtil.EMPTY_STRING_ARRAY;
      }
      return userManager.searchUsersIds(domainDriverManager, userIds, modelUser);
    } catch (Exception e) {
      throw new AdminException("Admin.searchUsersIds", SilverpeasException.ERROR,
          "admin.EX_ERR_USER_NOT_FOUND", e);
    }
  }

  private List<String> getUserIdsForComponent(String componentId) throws AdminException {
    List<String> userIds = new ArrayList<>();

    ComponentInst component = getComponentInst(componentId);
    if (component != null) {
      if (component.isPublic()) {
        // component is public, all users are allowed to access it
        return Arrays.asList(getAllUsersIds());
      } else {
        List<ProfileInst> profiles = component.getAllProfilesInst();
        for (ProfileInst profile : profiles) {
          userIds.addAll(getUserIdsForComponentProfile(profile));
        }
      }
    }

    return userIds;
  }

  private List<String> getUserIdsForComponentProfile(ProfileInst profile) throws AdminException {
    List<String> userIds = new ArrayList<>();

    // add users directly attach to profile
    userIds.addAll(profile.getAllUsers());

    // add users indirectly attach to profile (groups attached to profile)
    List<String> groupIds = profile.getAllGroups();
    List<String> allGroupIds = new ArrayList<>();
    for (String groupId : groupIds) {
      allGroupIds.add(groupId);
      allGroupIds.addAll(groupManager.getAllSubGroupIdsRecursively(groupId));
    }
    userIds.addAll(userManager.getAllUserIdsOfGroups(allGroupIds));

    return userIds;
  }

  @Override
  public ListSlice<UserDetail> searchUsers(final UserDetailsSearchCriteria searchCriteria) throws
      AdminException {
    List<String> userIds = null;
    if (searchCriteria.isCriterionOnComponentInstanceIdSet()) {
      List<String> listOfRoleNames = null;
      if (searchCriteria.isCriterionOnRoleNamesSet()) {
        listOfRoleNames = Arrays.asList(searchCriteria.getCriterionOnRoleNames());
      }
      ComponentInst instance = getComponentInst(searchCriteria.getCriterionOnComponentInstanceId());
      if (((listOfRoleNames != null && !listOfRoleNames.isEmpty())) || !instance.isPublic()) {
        List<ProfileInst> profiles;
        if (searchCriteria.isCriterionOnResourceIdSet()) {
          profiles = getProfileInstsFor(searchCriteria.getCriterionOnResourceId(), instance.getId());
        } else {
          profiles = instance.getAllProfilesInst();
        }
        userIds = new ArrayList<>();
        for (ProfileInst aProfile : profiles) {
          if (listOfRoleNames == null || listOfRoleNames.isEmpty() || listOfRoleNames.
              contains(aProfile.getName())) {
            userIds.addAll(aProfile.getAllUsers());

            // users of the groups (and recursively of their subgroups) playing the role
            List<String> groupIds = aProfile.getAllGroups();
            List<String> allGroupIds = new ArrayList<>();
            for (String aGroupId : groupIds) {
              allGroupIds.add(aGroupId);
              allGroupIds.addAll(groupManager.getAllSubGroupIdsRecursively(aGroupId));
            }
            userIds.addAll(userManager.getAllUserIdsOfGroups(allGroupIds));
          }
        }
        if (userIds.isEmpty()) {
          userIds = null;
        }
      }
    }

    if (searchCriteria.isCriterionOnUserIdsSet()) {
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
    }

    SearchCriteriaDAOFactory factory = SearchCriteriaDAOFactory.getFactory();
    UserSearchCriteriaForDAO criteria = factory.getUserSearchCriteriaDAO();
    if (userIds != null) {
      criteria.onUserIds(userIds.toArray(new String[userIds.size()]));
    }
    if (searchCriteria.isCriterionOnGroupIdsSet()) {
      String[] theGroupIds = searchCriteria.getCriterionOnGroupIds();
      if (theGroupIds == UserDetailsSearchCriteria.ANY_GROUPS) {
        criteria.and().onGroupIds(SearchCriteria.ANY);
      } else {
        Set<String> groupIds = new HashSet<>();
        for (String aGroupId : theGroupIds) {
          groupIds.addAll(groupManager.getAllSubGroupIdsRecursively(aGroupId));
          groupIds.add(aGroupId);
        }
        criteria.and().onGroupIds(groupIds.toArray(new String[groupIds.size()]));
      }
    }
    if (searchCriteria.isCriterionOnDomainIdSet()) {
      criteria.and().onDomainId(searchCriteria.getCriterionOnDomainId());
    }
    if (searchCriteria.isCriterionOnAccessLevelsSet()) {
      criteria.and().onAccessLevels(searchCriteria.getCriterionOnAccessLevels());
    }
    if (searchCriteria.isCriterionOnUserStatesToExcludeSet()) {
      criteria.and().onUserStatesToExclude(searchCriteria.getCriterionOnUserStatesToExclude());
    }
    if (searchCriteria.isCriterionOnNameSet()) {
      criteria.and().onName(searchCriteria.getCriterionOnName());
    }
    if (searchCriteria.isCriterionOnPaginationSet()) {
      criteria.onPagination(searchCriteria.getCriterionOnPagination());
    }

    return userManager.getUsersMatchingCriteria(criteria);
  }

  @Override
  public ListSlice<Group> searchGroups(final GroupsSearchCriteria searchCriteria) throws
      AdminException {
    SearchCriteriaDAOFactory factory = SearchCriteriaDAOFactory.getFactory();
    GroupSearchCriteriaForDAO criteria = factory.getGroupSearchCriteriaDAO();
    if (searchCriteria.isCriterionOnComponentInstanceIdSet()) {
      List<String> listOfRoleNames = new ArrayList<>();
      if (searchCriteria.isCriterionOnRoleNamesSet()) {
        listOfRoleNames = Arrays.asList(searchCriteria.getCriterionOnRoleNames());
      }
      ComponentInst instance = getComponentInst(searchCriteria.getCriterionOnComponentInstanceId());
      if (!listOfRoleNames.isEmpty() || !instance.isPublic()) {
        List<ProfileInst> profiles;
        if (searchCriteria.isCriterionOnResourceIdSet()) {
          profiles = getProfileInstsFor(searchCriteria.getCriterionOnResourceId(), instance.getId());
        } else {
          profiles = instance.getAllProfilesInst();
        }
        List<String> roleIds = new ArrayList<>();
        for (ProfileInst aProfile : profiles) {
          if (listOfRoleNames.isEmpty() || listOfRoleNames.contains(aProfile.getName())) {
            roleIds.add(aProfile.getId());
          }
        }
        criteria.onRoleNames(roleIds.toArray(new String[roleIds.size()]));
      }
    }

    if (searchCriteria.mustBeRoot()) {
      criteria.onAsRootGroup();
    }

    if (searchCriteria.isCriterionOnDomainIdSet()) {
      String domainId = searchCriteria.getCriterionOnDomainId();
      if (searchCriteria.isCriterionOnMixedDomainIdSet()) {
        criteria.onMixedDomainOronDomainId(domainId);
      } else {
        criteria.onDomainId(domainId);
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

  @Override
  public String[] searchGroupsIds(boolean isRootGroup, String componentId, String[] profileId,
      Group modelGroup) throws AdminException {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.
        getCurrentDomainDriverManager();
    try {
      ComponentInst component = getComponentInst(componentId);
      if (component != null) {
        if (component.isPublic()) {
          // component is public, all groups are allowed to access it
          componentId = null;
        }
      }
      return groupManager.searchGroupsIds(domainDriverManager, isRootGroup,
          getDriverComponentId(componentId), profileId, modelGroup);
    } catch (Exception e) {
      throw new AdminException("Admin.searchGroupsIds",
          SilverpeasException.ERROR, "admin.EX_ERR_GROUP_NOT_FOUND", e);
    }
  }

  // -------------------------------------------------------------------------
  // For DB connection reset
  // -------------------------------------------------------------------------
  @Override
  public void resetAllDBConnections(boolean isScheduled) throws AdminException {
    try {
      OrganizationSchemaPool.releaseConnections();
    } catch (Exception e) {
      throw new AdminException("Admin.resetAllDBConnections",
          SilverpeasException.ERROR, "root.EX_CONNECTION_CLOSE_FAILED", e);
    }
  }

  private void rollback() {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.
        getCurrentDomainDriverManager();
    try {
      // Roll back the transactions
      domainDriverManager.rollback();
    } catch (Exception e1) {
      SilverTrace.error("admin", "Admin.rollback", "root.EX_ERR_ROLLBACK", e1);
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
        SilverTrace.error("admin", "Admin.indexAllUsers", "admin.CANT_INDEX_USERS",
            "domainId = " + domain.getId(), e);
      }
    }
  }

  @Override
  public void indexUsers(String domainId) throws AdminException {
    DomainDriverManager domainDriverManager = DomainDriverManagerProvider.
        getCurrentDomainDriverManager();
    try {
      domainDriverManager.indexAllUsers(domainId);
    } catch (Exception e) {
      throw new AdminException("Admin.indexUsers",
          SilverpeasException.ERROR, "admin.CANT_INDEX_USERS", "domainId = " + domainId, e);
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

    String lang = I18NHelper.defaultLanguage;
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
      String componentRootName = URLUtil.getComponentNameFromComponentId(pasteDetail.
          getFromComponentId());
      String className = componentRootName + ApplicationResourcePasting.NAME_SUFFIX;
      ApplicationResourcePasting componentPaste = ServiceProvider.getService(className);
      componentPaste.paste(pasteDetail);
    } catch (IllegalStateException e) {
    } catch (Exception e) {
      SilverTrace.warn("admin", "Admin.copyAndPasteComponent()", "root.GEN_EXIT_METHOD", e);
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
    List<SpaceInstLight> path = TreeCache.getSpacePath(anotherSpaceId);
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
      newSpace.setLanguage(I18NHelper.defaultLanguage);

      // Rename if spaceName already used in the destination space
      List<SpaceInstLight> subSpaces = new ArrayList<>();
      for (String subSpaceId : newBrotherIds) {
        subSpaces.add(getSpaceInstLight(getDriverSpaceId(subSpaceId)));
      }
      String name = renameSpace(newSpace.getName(I18NHelper.defaultLanguage), subSpaces);
      newSpace.setName(name);

      // Remove inherited profiles from cloned space
      newSpace.removeInheritedProfiles();

      // Remove components from cloned space
      List<ComponentInst> components = newSpace.getAllComponentsInst();
      newSpace.removeAllComponentsInst();

      // Add space
      newSpaceId = addSpaceInst(pasteDetail.getUserId(), newSpace);

      // Copy space quota
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

      // paste subspaces
      PasteDetail subSpacePasteDetail = new PasteDetail(pasteDetail.getUserId());
      subSpacePasteDetail.setOptions(pasteDetail.getOptions());
      subSpacePasteDetail.setToSpaceId(newSpaceId);
      List<SpaceInst> subSpaceInsts = newSpace.getSubSpaces();
      for(SpaceInst subSpaceInst: subSpaceInsts) {
        subSpacePasteDetail.setFromSpaceId(subSpaceInst.getId());
        copyAndPasteSpace(subSpacePasteDetail);
      }

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
      return getProfilesByObject(id, type, instanceId);
    }
    throw new AdminPersistenceException("Admin.getProfileInstFor", SilverTrace.TRACE_LEVEL_ERROR,
        "Bad resource identifier: " + resourceId);
  }

  /**
   * Get all the space profiles Id for the given user without group transitivity.
   */
  private String[] getDirectSpaceProfileIdsOfUser(String sUserId) throws AdminException {
    try {
      DomainDriverManager domainDriverManager =
          DomainDriverManagerProvider.getCurrentDomainDriverManager();
      return spaceProfileManager.getSpaceProfileIdsOfUserType(domainDriverManager, sUserId);
    } catch (Exception e) {
      throw new AdminException("Admin.getSpaceProfileIdsOfUserType", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_PROFILES", "user Id : '" + sUserId + "'", e);
    }
  }

  /**
   * Get all the space profiles Id for the given group
   */
  private String[] getDirectSpaceProfileIdsOfGroup(String groupId) throws AdminException {
    try {
      DomainDriverManager domainDriverManager =
          DomainDriverManagerProvider.getCurrentDomainDriverManager();
      return spaceProfileManager.getSpaceProfileIdsOfGroupType(domainDriverManager, groupId);
    } catch (Exception e) {
      throw new AdminException("Admin.getSpaceProfileIdsOfGroupType", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_PROFILES", "group Id : '" + groupId + "'", e);
    }
  }

  /**
   * Get all the component profiles Id for the given user without group transitivity.
   * (component object profiles are not retrieved)
   */
  @SuppressWarnings("unchecked")
  private String[] getDirectComponentProfileIdsOfUser(String sUserId) throws AdminException {
    try {
      return profileManager.getProfileIdsOfUser(sUserId, Collections.EMPTY_LIST);
    } catch (Exception e) {
      throw new AdminException("Admin.getComponentProfileIdsOfUserType", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_GROUP_COMPONENT_PROFILES", "user Id : '" + sUserId + "'", e);
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
      throw new AdminException("Admin.getComponentProfileIdsOfGroupType", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_GROUP_COMPONENT_PROFILES", "group Id : '" + groupId + "'", e);
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
      return profileManager.getAllComponentObjectProfileIdsOfUser(userId, Collections.EMPTY_LIST);
    } catch (Exception e) {
      throw new AdminException("Admin.getComponentObjectProfileIdsOfUserType",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_PROFILES", "user Id : '" + userId + "'",
          e);
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
      throw new AdminException("Admin.getComponentObjectProfileIdsOfGroupType",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_GROUP_PROFILES",
          "group Id : '" + sGroupId + "'", e);
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
      SpaceInstLight spaceInst =
          getSpaceInstLight(getDriverSpaceId(currentSourceSpaceProfile.getSpaceFatherId()));
      if (!spaceInst.isPersonalSpace()) {// do not treat the personal space
        switch (context.getTargetType()) {
          case USER:
            currentSourceSpaceProfile.addUser(context.getTargetId());
            break;
          case GROUP:
            currentSourceSpaceProfile.addGroup(context.getTargetId());
            break;
        }
        updateSpaceProfileInst(currentSourceSpaceProfile, context.getAuthor(), false);
      }
    }

    //Add component rights
    for (String profileId : sourceComponentProfileIds) {
      ProfileInst currentSourceProfile = getProfileInst(profileId);
      ComponentInst currentComponent =
          getComponentInst(currentSourceProfile.getComponentFatherId());
      String spaceId = currentComponent.getDomainFatherId();
      SpaceInstLight spaceInst = getSpaceInstLight(getDriverSpaceId(spaceId));

      if (currentComponent.getStatus() == null &&
          !spaceInst.isPersonalSpace()) {// do not treat the personal space
        switch (context.getTargetType()) {
          case USER:
            currentSourceProfile.addUser(context.getTargetId());
            break;
          case GROUP:
            currentSourceProfile.addGroup(context.getTargetId());
            break;
        }
        updateProfileInst(currentSourceProfile, context.getAuthor(), false, context.getMode());
      }
    }

    //Add nodes rights
    for (String profileId : sourceNodeProfileIds) {
      ProfileInst currentSourceProfile = getProfileInst(profileId);
      ComponentInst currentComponent =
          getComponentInst(currentSourceProfile.getComponentFatherId());
      String spaceId = currentComponent.getDomainFatherId();
      SpaceInstLight spaceInst = getSpaceInstLight(getDriverSpaceId(spaceId));

      if (currentComponent.getStatus() == null &&
          !spaceInst.isPersonalSpace()) {// do not treat the personal space
        switch (context.getTargetType()) {
          case USER:
            currentSourceProfile.addUser(context.getTargetId());
            break;
          case GROUP:
            currentSourceProfile.addGroup(context.getTargetId());
            break;
        }
        updateProfileInst(currentSourceProfile, context.getAuthor(), false, context.getMode());
      }
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
      if (!spaceInst.isPersonalSpace()) {// do not treat the personal space
        switch (context.getTargetType()) {
          case USER:
            currentTargetSpaceProfile.removeUser(context.getTargetId());
            break;
          case GROUP:
            currentTargetSpaceProfile.removeGroup(context.getTargetId());
            break;
        }
        updateSpaceProfileInst(currentTargetSpaceProfile, context.getAuthor(), false);
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

      if (currentComponent.getStatus() == null &&
          !spaceInst.isPersonalSpace()) {// do not treat the personal space
        switch (context.getTargetType()) {
          case USER:
            currentTargetProfile.removeUser(context.getTargetId());
            break;
          case GROUP:
            currentTargetProfile.removeGroup(context.getTargetId());
            break;
        }
        updateProfileInst(currentTargetProfile, context.getAuthor(), false, context.getMode());
      }
    }
  }

  /**
   * Centralized method to copy or replace rights.
   * @param context the context that defined what the treatment must perform.
   * @throws AdminException
   */
  private void assignRightsFromSourceToTarget(RightAssignationContext context)
      throws AdminException {

    DomainDriverManager ddManager = DomainDriverManagerProvider.getCurrentDomainDriverManager();

    try {
      ddManager.startTransaction(false);

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
      switch (context.getSourceType()) {
        case USER:
          spaceProfileIdsToCopy = getDirectSpaceProfileIdsOfUser(context.getSourceId());
          componentProfileIdsToCopy = getDirectComponentProfileIdsOfUser(context.getSourceId());
          if (context.isAssignObjectRights()) {
            componentObjectProfileIdsToCopy =
                getComponentObjectProfileIdsOfUserType(context.getSourceId());
          }
          break;
        case GROUP:
          spaceProfileIdsToCopy = getDirectSpaceProfileIdsOfGroup(context.getSourceId());
          componentProfileIdsToCopy = getDirectComponentProfileIdsOfGroup(context.getSourceId());
          if (context.isAssignObjectRights()) {
            componentObjectProfileIdsToCopy =
                getComponentObjectProfileIdsOfGroupType(context.getSourceId());
          }
          break;
      }
      // Loading existing target profile data identifiers
      if (RightAssignationContext.MODE.REPLACE.equals(context.getMode())) {
        switch (context.getTargetType()) {
          case USER:
            spaceProfileIdsToReplace = getDirectSpaceProfileIdsOfUser(context.getTargetId());
            componentProfileIdsToReplace =
                getDirectComponentProfileIdsOfUser(context.getTargetId());
            break;
          case GROUP:
            spaceProfileIdsToReplace = getDirectSpaceProfileIdsOfGroup(context.getTargetId());
            componentProfileIdsToReplace =
                getDirectComponentProfileIdsOfGroup(context.getTargetId());
            break;
        }
      }


      // Deleting the current rights of the targeted resource (into the transaction)
      deleteTargetProfiles(context, spaceProfileIdsToReplace, componentProfileIdsToReplace);

      // Adding the new rights for the targeted resource (into the transaction)
      addTargetProfiles(context, spaceProfileIdsToCopy, componentProfileIdsToCopy,
          componentObjectProfileIdsToCopy);

      // Committing all the modified profiles
      ddManager.commit();

    } catch (Exception e) {
      // Roll back the transactions
      try {
        ddManager.rollback();
        cache.resetCache();
      } catch (Exception e1) {
        SilverTrace.error(MODULE_ADMIN, "Admin.addSpaceInst", "root.EX_ERR_ROLLBACK", e1);
      }
      throw new AdminException("Admin.assignRightsFromSourceToTarget", SilverpeasException.ERROR,
          "admin.EX_ERR_ASSIGN_RIGHTS", e);
    } finally {
      ddManager.releaseOrganizationSchema();
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
    switch (operationMode) {
      case COPY:
      default:
        context = RightAssignationContext.copy();
        break;
      case REPLACE:
        context = RightAssignationContext.replace();
        break;
    }
    if (!nodeAssignRights) {
      context.withoutAssigningComponentObjectRights();
    }
    return context.setAuthor(authorId);
  }

  private SpaceInstanciator getSpaceInstanciator() {
    if (spaceInstanciator == null) {
      spaceInstanciator = new SpaceInstanciator(getAllComponents());
    }
    return spaceInstanciator;
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
      SilverTrace.error("admin", "Admin.isDomainManagerUser", "cannot load user " + userId, e);
    }
    return userDetail != null && userDetail.getDomainId().equals(domainId) &&
        UserAccessLevel.DOMAIN_ADMINISTRATOR.equals(userDetail.getAccessLevel());
  }
}
