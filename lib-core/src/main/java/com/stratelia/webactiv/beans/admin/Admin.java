/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.beans.admin;

import com.google.common.collect.Sets;
import com.silverpeas.admin.components.ComponentPasteInterface;
import com.silverpeas.admin.components.Instanciateur;
import com.silverpeas.admin.components.Parameter;
import com.silverpeas.admin.components.PasteDetail;
import com.silverpeas.admin.components.Profile;
import com.silverpeas.admin.components.WAComponent;
import com.silverpeas.admin.spaces.SpaceInstanciator;
import com.silverpeas.admin.spaces.SpaceTemplate;
import com.silverpeas.util.ArrayUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.containerManager.ContainerManager;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.domains.ldapdriver.LDAPSynchroUserItf;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.cache.AdminCache;
import com.stratelia.webactiv.beans.admin.cache.GroupCache;
import com.stratelia.webactiv.beans.admin.cache.Space;
import com.stratelia.webactiv.beans.admin.cache.TreeCache;
import com.stratelia.webactiv.organization.OrganizationSchemaPool;
import com.stratelia.webactiv.organization.ScheduledDBReset;
import com.stratelia.webactiv.organization.UserRow;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.pool.ConnectionPool;
import org.apache.commons.lang3.time.FastDateFormat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.stratelia.silverpeas.silvertrace.SilverTrace.MODULE_ADMIN;

/**
 * @author neysseri
 *
 */
/**
 * The class Admin is the main class of the Administrator.<BR> The role of the administrator is to
 * create and maintain spaces.
 */
public final class Admin {

  public static final String SPACE_KEY_PREFIX = "WA";
  // Divers
  static private final Object semaphore = new Object();
  static private long threadDelay = 900;
  static private boolean delUsersOnDiffSynchro = true;
  static private boolean shouldFallbackGroupNames = true;
  static private boolean shouldFallbackUserLogins = false;
  static private String m_groupSynchroCron = "";
  // Helpers
  static private final SpaceInstManager spaceManager = new SpaceInstManager();
  static private final ComponentInstManager componentManager = new ComponentInstManager();
  static private final ProfileInstManager profileManager = new ProfileInstManager();
  static private final SpaceProfileInstManager spaceProfileManager = new SpaceProfileInstManager();
  static private final GroupManager groupManager = new GroupManager();
  static private final UserManager userManager = new UserManager();
  static private final DomainDriverManager domainDriverManager = new DomainDriverManager();
  static private final ProfiledObjectManager profiledObjectManager = new ProfiledObjectManager();
  static private final GroupProfileInstManager groupProfileManager = new GroupProfileInstManager();
  // Component instanciator
  static private Instanciateur componentInstanciator = null;
  static private SpaceInstanciator spaceInstanciator = null;
  // Entreprise client space Id
  static private String adminDBDriver = null;
  static private String productionDbUrl;
  static private String productionDbLogin;
  static private String productionDbPassword;
  static private int m_nEntrepriseClientSpaceId = 0;
  static private String administratorMail = null;
  static private String m_sDAPIGeneralAdminId = null;
  // User Logs
  static private Map<String, UserLog> loggedUsers = Collections.synchronizedMap(
      new HashMap<String, UserLog>(
      0));
  private static FastDateFormat formatter = FastDateFormat.getInstance("dd/MM/yyyy HH:mm:ss:S");
  // Cache management
  static private AdminCache cache = new AdminCache();
  // DB Connections Scheduled Resets
  static private ScheduledDBReset m_DBResetScheduler = null;
  public static final String basketSuffix = " (Restauré)";
  static private SynchroGroupScheduler groupSynchroScheduler = null;
  static private ResourceLocator roleMapping = null;
  static private boolean useProfileInheritance = false;
  private static transient boolean cacheLoaded = false;

  /**
   * Admin Constructor
   */
  public Admin() {
    if (adminDBDriver == null) {
      // Load silverpeas admin resources
      ResourceLocator resources = new ResourceLocator("com.stratelia.webactiv.beans.admin.admin",
          "");
      roleMapping = new ResourceLocator("com.silverpeas.admin.roleMapping", "");
      useProfileInheritance = resources.getBoolean("UseProfileInheritance", false);

      adminDBDriver = resources.getString("AdminDBDriver");
      productionDbUrl = resources.getString("WaProductionDb");
      productionDbLogin = resources.getString("WaProductionUser");
      productionDbPassword = resources.getString("WaProductionPswd");

      m_nEntrepriseClientSpaceId = Integer.parseInt(resources.getString("EntrepriseClientSpaceId"));
      administratorMail = resources.getString("AdministratorEMail");
      m_sDAPIGeneralAdminId = resources.getString("DAPIGeneralAdminId");

      if (m_DBResetScheduler == null) {
        m_DBResetScheduler = new ScheduledDBReset();
        m_DBResetScheduler.initialize(resources.getString("DBConnectionResetScheduler", ""));
      }

      shouldFallbackGroupNames = resources.getBoolean("FallbackGroupNames", true);
      shouldFallbackUserLogins = resources.getBoolean("FallbackUserLogins", false);
      threadDelay = resources.getLong("AdminThreadedSynchroDelay", 900);
      m_groupSynchroCron = resources.getString("GroupSynchroCron", "* 5 * * *");
      delUsersOnDiffSynchro = resources.getBoolean("DelUsersOnThreadedSynchro", true);

      // Cache management
      cache.setCacheAvailable(StringUtil.getBooleanValue(resources.getString("UseCache", "1")));

      // Initialize component instanciator
      if (componentInstanciator == null) {
        componentInstanciator = new Instanciateur();
      }

      if (spaceInstanciator == null) {
        spaceInstanciator = new SpaceInstanciator(getAllComponents());
      }
      // Init tree cache
      synchronized (Admin.class) {
        if (!cacheLoaded) {
          reloadCache();
        }
      }
    }
  }

  public void reloadCache() {
    cache.resetCache();
    TreeCache.clearCache();
    GroupCache.clearCache();
    try {
      SilverTrace.info(MODULE_ADMIN, "admin.startServer", "root.MSG_GEN_PARAM_VALUE",
          "Start filling tree cache...");
      List<SpaceInstLight> spaces = spaceManager.getAllSpaces(domainDriverManager);
      for (SpaceInstLight space : spaces) {
        addSpaceInTreeCache(space, false);
      }
      SilverTrace.info(MODULE_ADMIN, "admin.startServer", "root.MSG_GEN_PARAM_VALUE",
          "Tree cache filled !");
    } catch (Exception e) {
      SilverTrace.error("admin", "Constructor", "ERROR_WHEN_INITIALIZING_ADMIN", e);
    }
    cacheLoaded = true;
  }

  // -------------------------------------------------------------------------
  // Start Server actions
  // -------------------------------------------------------------------------
  public void startServer() throws Exception {
    try {
      domainDriverManager.startServer(this, threadDelay);
    } catch (Exception e) {
      SilverTrace.error(MODULE_ADMIN, "Admin.startServer", "ERROR_WHEN_STARTING_DOMAINS", e);
    }
    Group[] groups = getSynchronizedGroups();
    List<String> synchronizedGroupIds = new ArrayList<String>(groups.length);
    for (Group group : groups) {
      if (group.isSynchronized()) {
        synchronizedGroupIds.add(group.getId());
      }
    }
    groupSynchroScheduler = new SynchroGroupScheduler();
    groupSynchroScheduler.initialize(m_groupSynchroCron, this, synchronizedGroupIds);
  }

  private void addSpaceInTreeCache(SpaceInstLight space, boolean addSpaceToSuperSpace)
      throws NumberFormatException, AdminException {
    Space spaceInCache = new Space();
    spaceInCache.setSpace(space);
    List<ComponentInstLight> components = componentManager.getComponentsInSpace(
        Integer.parseInt(space.getShortId()));
    spaceInCache.setComponents(components);

    spaceInCache.setSubspaces(getSubSpaces(space.getShortId()));
    TreeCache.addSpace(space.getShortId(), spaceInCache);

    if (addSpaceToSuperSpace) {
      if (!space.isRoot()) {
        TreeCache.addSubSpace(space.getFatherId(), space);
      }
    }
  }

  // -------------------------------------------------------------------------
  // SPACE RELATED FUNCTIONS
  // -------------------------------------------------------------------------
  /**
   * Get Enterprise space id
   *
   * @return The general space id
   */
  public String getGeneralSpaceId() {
    return SPACE_KEY_PREFIX + m_nEntrepriseClientSpaceId;
  }
  
  public void createSpaceIndex(int spaceId) {
    try {
      SpaceInstLight space = getSpaceInstLight(String.valueOf(spaceId));
      createSpaceIndex(space);
    } catch (AdminException e) {
      SilverTrace.error(MODULE_ADMIN, "admin.createSpaceIndex",
          "root.MSG_GEN_PARAM_VALUE", "spaceId = " + spaceId);
    }
  }

  public void createSpaceIndex(SpaceInstLight spaceInst) {
    FullIndexEntry indexEntry;

    SilverTrace.info(MODULE_ADMIN, "admin.createSpaceIndex",
        "root.MSG_GEN_PARAM_VALUE", "Space Name : " + spaceInst.getName()
        + " Space Id : " + spaceInst.getShortId());

    if (spaceInst != null) {
      // Index the space
      String spaceId = spaceInst.getFullId();
      indexEntry = new FullIndexEntry("Spaces", "Space", spaceId);
      indexEntry.setTitle(spaceInst.getName());
      indexEntry.setPreView(spaceInst.getDescription());
      indexEntry.setCreationUser(String.valueOf(spaceInst.getCreatedBy()));

      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  public void deleteSpaceIndex(SpaceInst spaceInst) {
    SilverTrace.info("admin", "admin.deleteSpaceIndex",
        "root.MSG_GEN_PARAM_VALUE", "Space Name : " + spaceInst.getName()
        + " Space Id : " + spaceInst.getId());
    // Index the space
    String spaceId = getSpaceId(spaceInst);
    FullIndexEntry indexEntry = new FullIndexEntry("Spaces", "Space", spaceId);
    IndexEngineProxy.removeIndexEntry(indexEntry.getPK());
  }

  /**
   * add a space instance in database
   *
   * @param userId Id of user who add the space
   * @param spaceInst SpaceInst object containing information about the space to be created
   * @return the created space id
   */
  public String addSpaceInst(String userId, SpaceInst spaceInst) throws AdminException {
    Connection connectionProd = null;

    try {
      SilverTrace.info(MODULE_ADMIN, "admin.addSpaceInst",
          "root.MSG_GEN_PARAM_VALUE", "Space Name : " + spaceInst.getName()
          + " NbCompo: " + spaceInst.getNumComponentInst());
      connectionProd = openConnection(productionDbUrl, productionDbLogin,
          productionDbPassword, false);

      // Open the connections with auto-commit to false
      domainDriverManager.startTransaction(false);
      if (!spaceInst.isRoot()) {
        // It's a subspace
        // Convert the client id in driver id
        spaceInst.setDomainFatherId(getDriverSpaceId(spaceInst.getDomainFatherId()));

        if (useProfileInheritance && !spaceInst.isInheritanceBlocked()) {
          // inherits profiles from super space
          // set super space profiles to new space
          setSpaceProfilesToSubSpace(spaceInst, null);
        }
      }

      // Create the space instance
      spaceInst.setCreatorUserId(userId);

      String sSpaceInstId = spaceManager.createSpaceInst(spaceInst,
          domainDriverManager);
      spaceInst.setId(getClientSpaceId(sSpaceInstId));

      // put new space in cache
      cache.opAddSpace(getSpaceInstById(sSpaceInstId, true));

      // Instantiate the components
      ArrayList<ComponentInst> alCompoInst = spaceInst.getAllComponentsInst();
      for (ComponentInst componentInst : alCompoInst) {
        componentInst.setDomainFatherId(spaceInst.getId());
        addComponentInst(userId, componentInst, false);
      }

      // commit the transactions
      domainDriverManager.commit();
      connectionProd.commit();

      SpaceInstLight space = getSpaceInstLight(sSpaceInstId);
      addSpaceInTreeCache(space, true);

      // indexation de l'espace
      SilverTrace.info(MODULE_ADMIN, "admin.addSpaceInst",
          "root.MSG_GEN_PARAM_VALUE", "Indexation : spaceInst = "
          + spaceInst.getName());
      createSpaceIndex(space);

      return spaceInst.getId();
    } catch (Exception e) {
      try {
        // Roll back the transactions
        domainDriverManager.rollback();
        connectionProd.rollback();
        cache.resetCache();
      } catch (Exception e1) {
        SilverTrace.error(MODULE_ADMIN, "Admin.addSpaceInst",
            "root.EX_ERR_ROLLBACK", e1);
      }
      throw new AdminException("Admin.addSpaceInst", SilverpeasException.ERROR,
          "admin.EX_ERR_ADD_SPACE", "space name : '" + spaceInst.getName()
          + "'", e);
    } finally {
      // close connection
      closeConnection(connectionProd);
    }
  }

  /**
   * Delete the given space The delete is apply recursively to the sub-spaces
   *
   * @param sUserId Id of user who deletes the space
   * @param sClientSpaceId Id of the space to be deleted
   * @return the deleted space id
   */
  public String deleteSpaceInstById(String sUserId, String sClientSpaceId,
      boolean definitive) throws AdminException {
    return deleteSpaceInstById(sUserId, sClientSpaceId, true, definitive);
  }

  /**
   * Delete the given space if it's not the general space The delete is apply recursively to the
   * sub-spaces
   *
   * @param sUserId Id of user who deletes the space
   * @param sClientSpaceId Id of the space to be deleted
   * @param startNewTransaction Flag : must be true at first call to initialize transaction, then
   * false for recurrents calls
   * @return the deleted space id
   */
  public String deleteSpaceInstById(String sUserId, String sClientSpaceId,
      boolean startNewTransaction, boolean definitive) throws AdminException {
    SilverTrace.spy(MODULE_ADMIN, "Admin.deleteSpaceInstById()", sClientSpaceId,
        "ASP", "", sUserId, SilverTrace.SPY_ACTION_DELETE);

    try {
      if (startNewTransaction) {
        // Start transaction
        domainDriverManager.startTransaction(false);
      }

      // Convert the client id in driver id
      String sDriverSpaceId = getDriverSpaceId(sClientSpaceId);

      // Get the space to delete
      SpaceInst spaceInst = getSpaceInstById(sDriverSpaceId, true);

      if (!definitive) {
        // Update the space in tables
        spaceManager.sendSpaceToBasket(domainDriverManager, sDriverSpaceId,
            spaceInst.getName() + Admin.basketSuffix, sUserId);

        // delete all profiles (space, components and subspaces)
        deleteSpaceProfiles(spaceInst);
      } else {
        // Get all the sub-spaces
        String[] sSubSpaceIds = getAllSubSpaceIds(sClientSpaceId);

        // Delete subspaces
        for (int i = 0;
            sSubSpaceIds != null && i < sSubSpaceIds.length;
            i++) {
          deleteSpaceInstById(sUserId, sSubSpaceIds[i], false, true);
        }

        // Delete subspaces already in bin
        List<SpaceInstLight> removedSpaces = getRemovedSpaces();
        for (SpaceInstLight removedSpace :
            removedSpaces) {
          if (sDriverSpaceId.equals(removedSpace.getFatherId())) {
            deleteSpaceInstById(sUserId, removedSpace.getFullId(), false, true);
          }
        }

        // delete the space profiles instance
        for (int nI = 0;
            nI < spaceInst.getNumSpaceProfileInst();
            nI++) {
          deleteSpaceProfileInst(spaceInst.getSpaceProfileInst(nI).getId(),
              false);
        }

        // Delete the components
        ArrayList<ComponentInst> alCompoInst = spaceInst.getAllComponentsInst();
        for (ComponentInst anAlCompoInst : alCompoInst) {
          deleteComponentInst(sUserId, anAlCompoInst.getId(), true, false);
        }

        // Delete the components already in bin
        List<ComponentInstLight> removedComponents = getRemovedComponents();
        for (ComponentInstLight removedComponent :
            removedComponents) {
          if (sClientSpaceId.equals(removedComponent.getDomainFatherId())) {
            deleteComponentInst(sUserId, removedComponent.getId(), true, false);
          }
        }

        // Delete the space in tables
        spaceManager.deleteSpaceInst(spaceInst, domainDriverManager);
      }

      if (startNewTransaction) {
        domainDriverManager.commit();
      }
      cache.opRemoveSpace(spaceInst);
      TreeCache.removeSpace(sDriverSpaceId);

      // desindexation de l'espace
      deleteSpaceIndex(spaceInst);

      return sClientSpaceId;
    } catch (Exception e) {
      // Roll back the transactions
      if (startNewTransaction) {
        rollback();
      }
      throw new AdminException("Admin.deleteSpaceInstById", SilverpeasException.ERROR,
          "admin.EX_ERR_DELETE_SPACE", "user Id : '"
          + sUserId + "', space Id : '" + sClientSpaceId + "'", e);
    }
  }

  private void deleteSpaceProfiles(SpaceInst spaceInst) throws AdminException {
    // delete the space profiles
    for (int nI = 0;
        nI < spaceInst.getNumSpaceProfileInst();
        nI++) {
      deleteSpaceProfileInst(spaceInst.getSpaceProfileInst(nI).getId(), false);
    }

    // delete the components profiles
    List<ComponentInst> alCompoInst = spaceInst.getAllComponentsInst();
    ComponentInst component;
    for (ComponentInst anAlCompoInst : alCompoInst) {
      component = anAlCompoInst;
      for (int p = 0;
          p < component.getNumProfileInst();
          p++) {
        if (!component.getProfileInst(p).isInherited()) {
          deleteProfileInst(component.getProfileInst(p).getId(), false);
        }
      }
    }

    // delete the subspace profiles
    String[] sSubSpaceIds = spaceInst.getSubSpaceIds();
    SpaceInst subSpace;
    for (int i = 0;
        sSubSpaceIds != null && i < sSubSpaceIds.length;
        i++) {
      subSpace = getSpaceInstById(sSubSpaceIds[i]);
      deleteSpaceProfiles(subSpace);
    }
  }

  public void restoreSpaceFromBasket(String sClientSpaceId) throws AdminException {
    try {
      // Start transaction
      domainDriverManager.startTransaction(false);
      // Convert the client id in driver id
      String sDriverSpaceId = getDriverSpaceId(sClientSpaceId);
      // update data in database
      spaceManager.removeSpaceFromBasket(domainDriverManager, sDriverSpaceId);
      // Get the space and put it in the cache
      SpaceInst spaceInst = getSpaceInstById(sDriverSpaceId, true);
      // set superspace profiles to space
      if (useProfileInheritance && !spaceInst.isInheritanceBlocked() && !spaceInst.isRoot()) {
        updateSpaceInheritance(spaceInst, false);
      }
      domainDriverManager.commit();
      // indexation de l'espace
      SilverTrace.info(MODULE_ADMIN, "admin.restoreSpaceFromBasket",
          "root.MSG_GEN_PARAM_VALUE", "Indexation : spaceInst = "
          + spaceInst.getName());
      createSpaceIndex(Integer.parseInt(sDriverSpaceId));
      // reset space and eventually subspace
      cache.opAddSpace(spaceInst);
    } catch (Exception e) {
      rollback();
      throw new AdminException("Admin.restoreSpaceFromBasket",
          SilverpeasException.ERROR, "admin.EX_ERR_RESTORE_SPACE_FROM_BASKET",
          "spaceId = " + sClientSpaceId);
    }
  }

  /**
   * Get the space instance with the given space id
   *
   * @param sClientSpaceId client space id
   * @return Space information as SpaceInst object
   */
  public SpaceInst getSpaceInstById(String sClientSpaceId) throws AdminException {
    try {
      SpaceInst spaceInst = getSpaceInstById(sClientSpaceId, false);

      // Put the client space Id back
      spaceInst.setId(sClientSpaceId);

      // Put the client component Id back
      ArrayList<ComponentInst> alCompoInst = spaceInst.getAllComponentsInst();
      for (ComponentInst anAlCompoInst : alCompoInst) {
        String sClientComponentId = getClientComponentId(anAlCompoInst.getId());
        anAlCompoInst.setId(sClientComponentId);
      }

      // Put the client sub spaces Id back
      String[] asSubSpaceIds = spaceInst.getSubSpaceIds();
      for (int nI = 0;
          asSubSpaceIds != null && nI < asSubSpaceIds.length;
          nI++) {
        asSubSpaceIds[nI] = getClientSpaceId(asSubSpaceIds[nI]);
      }
      spaceInst.setSubSpaceIds(asSubSpaceIds);
      return spaceInst;
    } catch (Exception e) {
      throw new AdminException("Admin.getSpaceInstById", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_SPACE", " space Id : '" + sClientSpaceId + "'", e);
    }
  }

  /**
   * Get the space instance with the given space id
   *
   * @param sSpaceId client space id
   * @param bDriverSpaceId true is space id is in 'driver' format, false for 'client' format
   * @return Space information as SpaceInst object
   */
  private SpaceInst getSpaceInstById(String sSpaceId, boolean bDriverSpaceId)
      throws AdminException {
    try {

      String sDriverSpaceId;
      // Convert the client id in driver id
      if (bDriverSpaceId) {
        sDriverSpaceId = sSpaceId;
      } else {
        sDriverSpaceId = getDriverSpaceId(sSpaceId);
      }

      SpaceInst spaceInst = cache.getSpaceInst(sDriverSpaceId);
      if (spaceInst == null) {
        // Get space instance
        spaceInst = spaceManager.getSpaceInstById(domainDriverManager, sDriverSpaceId);
        // Store the spaceInst in cache
        cache.putSpaceInst(spaceInst);
      }
      return spaceManager.copy(spaceInst);
    } catch (Exception e) {
      throw new AdminException("Admin.getSpaceInstById", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_SPACE", " space Id : '" + sSpaceId + "'", e);
    }
  }

  public SpaceInst getPersonalSpace(String userId) throws AdminException {
    return spaceManager.getPersonalSpace(domainDriverManager, userId);
  }

  /**
   * Get all the subspaces Ids available in Silverpeas given a domainFatherId (client id format)
   *
   * @param sDomainFatherId Id of the father space
   * @return an array of String containing the ids of spaces that are child of given space
   */
  public String[] getAllSubSpaceIds(String sDomainFatherId)
      throws AdminException {
    String[] asDriverSpaceIds;
    SilverTrace.debug(MODULE_ADMIN, "Admin.getAllSubSpaceIds",
        "root.MSG_GEN_ENTER_METHOD", "father space id: '" + sDomainFatherId + "'");

    try {
      // get all sub space ids
      asDriverSpaceIds = spaceManager.getAllSubSpaceIds(domainDriverManager,
          getDriverSpaceId(sDomainFatherId));

      // Convert all the driver space ids in client space ids
      asDriverSpaceIds = getClientSpaceIds(asDriverSpaceIds);

      return asDriverSpaceIds;
    } catch (Exception e) {
      throw new AdminException("Admin.getAllSubSpaceIds",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_ALL_SUBSPACE_IDS",
          " father space Id : '" + sDomainFatherId + "'", e);
    }
  }

  /**
   * Updates the space (with the given name) with the given space Updates only the node
   *
   * @param spaceInstNew SpaceInst object containing new information for space to be updated
   * @return the updated space id
   */
  public String updateSpaceInst(SpaceInst spaceInstNew) throws AdminException {
    try {
      SpaceInst oldSpace = getSpaceInstById(spaceInstNew.getId());

      // Open the connections with auto-commit to false
      domainDriverManager.startTransaction(false);

      SilverTrace.debug(MODULE_ADMIN, "Admin.updateSpaceInst",
          "root.MSG_GEN_ENTER_METHOD", "Before id: '" + spaceInstNew.getId()
          + "' after Id: " + getDriverSpaceId(spaceInstNew.getId()));
      // Convert the client id in driver id
      spaceInstNew.setId(getDriverSpaceId(spaceInstNew.getId()));

      // Update the space in tables
      spaceManager.updateSpaceInst(domainDriverManager, spaceInstNew);

      if (useProfileInheritance
          && (oldSpace.isInheritanceBlocked() != spaceInstNew.isInheritanceBlocked())) {
        updateSpaceInheritance(oldSpace, spaceInstNew.isInheritanceBlocked());
      }

      // commit the transactions
      domainDriverManager.commit();

      cache.opUpdateSpace(spaceInstNew);

      // Update space in TreeCache
      SpaceInstLight spaceLight =
          spaceManager.getSpaceInstLightById(domainDriverManager, getDriverSpaceId(
          spaceInstNew.getId()));
      TreeCache.updateSpace(spaceLight);

      // indexation de l'espace
      SilverTrace.info(MODULE_ADMIN, "admin.updateSpaceInst",
          "root.MSG_GEN_PARAM_VALUE", "Indexation : spaceInst = "
          + spaceInstNew.getName());
      createSpaceIndex(spaceLight);

      return spaceInstNew.getId();
    } catch (Exception e) {
      rollback();
      throw new AdminException("Admin.updateSpaceInst",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_SPACE",
          "space Id : '" + spaceInstNew.getId() + "'", e);
    }
  }

  public void updateSpaceOrderNum(String sSpaceId, int orderNum)
      throws AdminException {
    try {
      SilverTrace.debug(MODULE_ADMIN, "Admin.updateSpaceOrderNum",
          "root.MSG_GEN_ENTER_METHOD", "Space id: '" + sSpaceId
          + "' New Order num: " + Integer.toString(orderNum));
      String sDriverSpaceId = getDriverSpaceId(sSpaceId);

      // Open the connections with auto-commit to false
      domainDriverManager.startTransaction(false);

      // Update the space in tables
      spaceManager.updateSpaceOrder(domainDriverManager, sDriverSpaceId, orderNum);

      // commit the transactions
      domainDriverManager.commit();

      cache.opUpdateSpace(spaceManager.getSpaceInstById(domainDriverManager,
          sDriverSpaceId));

      // Update subspaces sort in TreeCache
      SpaceInstLight space = TreeCache.getSpaceInstLight(sDriverSpaceId);
      if (!space.isRoot()) {
        TreeCache.setSubspaces(space.getFatherId(), getSubSpaces(space.getFatherId()));
      }
    } catch (Exception e) {
      rollback();
      throw new AdminException("Admin.updateSpaceOrderNum",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_SPACE",
          "space Id : '" + sSpaceId + "'", e);
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
          if (profile != null && !"Manager".equalsIgnoreCase(profile.getName())) {
            deleteSpaceProfileInst(profile.getId(), false);
          }
        }

        if (!space.isRoot()) {
          // 2 - affectation des droits de l'espace au sous espace
          setSpaceProfilesToSubSpace(space, null);
          profiles = space.getInheritedProfiles();
          for (SpaceProfileInst profile : profiles) {
            addSpaceProfileInst(profile, null, false);
          }
        }
      }
    } catch (Exception e) {
      rollback();
      throw new AdminException("Admin.updateComponentInst", SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_SPACE", "spaceId = " + space.getId(), e);
    }
  }

  /*
   * Tests if a space with given space id exists
   * @param sClientSpaceId if of space to be tested
   * @return true if the given space instance name is an existing space
   */
  public boolean isSpaceInstExist(String sClientSpaceId) throws AdminException {
    try {
      return spaceManager.isSpaceInstExist(domainDriverManager,
          getDriverSpaceId(sClientSpaceId));
    } catch (Exception e) {
      throw new AdminException("Admin.isSpaceInstExist",
          SilverpeasException.ERROR, "admin.EX_ERR_IS_SPACE_EXIST",
          "space Id : '" + sClientSpaceId + "'", e);
    }
  }

  /**
   * Return the all the root spaces Ids available in Silverpeas
   */
  public String[] getAllRootSpaceIds() throws AdminException {
    SilverTrace.debug(MODULE_ADMIN, "Admin.getAllSpaceIds",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      String[] asDriverSpaceIds = spaceManager.getAllRootSpaceIds(domainDriverManager);

      // Convert all the driver space ids in client space ids
      asDriverSpaceIds = getClientSpaceIds(asDriverSpaceIds);

      return asDriverSpaceIds;
    } catch (Exception e) {
      throw new AdminException("Admin.getAllSpaceIds",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_ALL_SPACE_IDS", e);
    }
  }

  /**
   * Retrieve spaces from root to component
   *
   * @param componentId the target component
   * @return a List of SpaceInstLight
   * @throws AdminException
   */
  public List<SpaceInstLight> getPathToComponent(String componentId) throws AdminException {
    List<SpaceInstLight> path = new ArrayList<SpaceInstLight>();
    ComponentInstLight component = getComponentInstLight(componentId);
    if (component != null) {
      String spaceId = component.getDomainFatherId();
      return getPathToSpace(spaceId, true);
    }
    return path;
  }

  /**
   * Retrieve spaces from root to space identified by spaceId
   *
   * @param spaceId the target space
   * @return a List of SpaceInstLight
   * @throws AdminException
   */
  public List<SpaceInstLight> getPathToSpace(String spaceId, boolean includeTarget)
      throws AdminException {
    List<SpaceInstLight> path = new ArrayList<SpaceInstLight>();
    SpaceInstLight space = getSpaceInstLight(getDriverSpaceId(spaceId));
    if (space != null) {
      if (includeTarget) {
        path.add(0, space);
      }
      while (!space.isRoot()) {
        String fatherId = space.getFatherId();
        space = getSpaceInstLight(fatherId);
        path.add(0, space);
      }
    }
    return path;
  }

  /**
   * Return the all the spaces Ids available in Silverpeas
   */
  public String[] getAllSpaceIds() throws AdminException {
    String[] asDriverSpaceIds;
    SilverTrace.debug(MODULE_ADMIN, "Admin.getAllSpaceIds",
        "root.MSG_GEN_ENTER_METHOD");

    try {
      asDriverSpaceIds = spaceManager.getAllSpaceIds(domainDriverManager);

      // Convert all the driver space ids in client space ids
      asDriverSpaceIds = getClientSpaceIds(asDriverSpaceIds);

      return asDriverSpaceIds;
    } catch (Exception e) {
      throw new AdminException("Admin.getAllSpaceIds",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_ALL_SPACE_IDS", e);
    }
  }

  /**
   * Returns all spaces which has been removed but not definitely deleted
   *
   * @return a List of SpaceInstLight
   */
  public List<SpaceInstLight> getRemovedSpaces() throws AdminException {
    SilverTrace.debug(MODULE_ADMIN, "Admin.getRemovedSpaces",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return spaceManager.getRemovedSpaces(domainDriverManager);
    } catch (Exception e) {
      throw new AdminException("Admin.getRemovedSpaces",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_REMOVED_SPACES", e);
    }
  }

  /**
   * Returns all components which has been removed but not definitely deleted
   *
   * @return a List of ComponentInstLight
   */
  public List<ComponentInstLight> getRemovedComponents() throws AdminException {
    SilverTrace.debug(MODULE_ADMIN, "Admin.getRemovedComponents",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return componentManager.getRemovedComponents(domainDriverManager);
    } catch (Exception e) {
      throw new AdminException("Admin.getRemovedComponents",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_REMOVED_COMPONENTS", e);
    }
  }

  /**
   * Return the the spaces name corresponding to the given space ids
   *
   * @param asClientSpaceIds
   * @return
   * @throws AdminException
   */
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
      throw new AdminException("Admin.getSpaceNames",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_SPACE_NAMES", e);
    }
  }

  public Map<String, SpaceTemplate> getAllSpaceTemplates() {
    return spaceInstanciator.getAllSpaceTemplates();
  }

  public SpaceInst getSpaceInstFromTemplate(String templateName) {
    return spaceInstanciator.getSpaceToInstanciate(templateName);
  }

  // -------------------------------------------------------------------------
  // COMPONENT RELATED FUNCTIONS
  // -------------------------------------------------------------------------
  /**
   * Return all the components name available in Silverpeas
   *
   * @return all the components name available in Silverpeas
   * @throws AdminException
   */
  public Map<String, String> getAllComponentsNames() throws AdminException {
    SilverTrace.debug(MODULE_ADMIN, "Admin.getAllComponentsNames",
        "root.MSG_GEN_ENTER_METHOD");

    Map<String, String> hComponents = Instanciateur.getAllComponentsNames();

    for (Map.Entry<String, String> entry :
        hComponents.entrySet()) {
      SilverTrace.debug(MODULE_ADMIN, "Admin.getAllComponentsNames",
          "admin.MSG_INFO_COMPONENT_FOUND", entry.getKey() + ": " + entry.getValue());
    }

    return hComponents;
  }

  /**
   * Return all the components of silverpeas read in the xmlComponent directory
   */
  public Map<String, WAComponent> getAllComponents() {
    return Instanciateur.getWAComponents();
  }

  /**
   * return the component Inst corresponding to the given ID
   */
  public ComponentInst getComponentInst(String sClientComponentId) throws AdminException {
    try {
      ComponentInst componentInst = getComponentInst(sClientComponentId, false,
          null);
      componentInst.setId(sClientComponentId);
      componentInst.setDomainFatherId(getClientSpaceId(componentInst.getDomainFatherId()));

      return componentInst;
    } catch (Exception e) {
      throw new AdminException("Admin.getComponentInst",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_COMPONENT",
          "component Id: '" + sClientComponentId + "'", e);
    }
  }

  /**
   * return the component Inst Light corresponding to the given ID
   */
  public ComponentInstLight getComponentInstLight(String sClientComponentId) throws AdminException {
    try {
      String sDriverComponentId = getDriverComponentId(sClientComponentId);
      ComponentInstLight componentInst =
          componentManager.getComponentInstLight(domainDriverManager, sDriverComponentId);

      return componentInst;
    } catch (Exception e) {
      throw new AdminException("Admin.getComponentInstLight", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_COMPONENT", "component Id: '" + sClientComponentId + "'", e);
    }
  }

  /**
   * return the component Inst corresponding to the given ID
   */
  private ComponentInst getComponentInst(String sComponentId,
      boolean bDriverComponentId, String sFatherDriverSpaceId)
      throws AdminException {
    String sDriverComponentId;

    try {
      // Converts space id if necessary
      if (bDriverComponentId) {
        sDriverComponentId = sComponentId;
      } else {
        sDriverComponentId = getDriverComponentId(sComponentId);
      }

      // Get the component instance
      ComponentInst componentInst = cache.getComponentInst(sDriverComponentId);
      SilverTrace.info(MODULE_ADMIN, "Admin.getComponentInst",
          "root.MSG_GEN_ENTER_METHOD", "componentInst=" + componentInst
          + " id=" + sDriverComponentId);
      if (componentInst == null) {
        // Get component instance from database
        componentInst = componentManager.getComponentInst(domainDriverManager,
            sDriverComponentId, sFatherDriverSpaceId);
        SilverTrace.info(MODULE_ADMIN, "Admin.getComponentInst",
            "root.MSG_GEN_ENTER_METHOD", "componentInst FatherId="
            + componentInst.getDomainFatherId());
        // Store component instance in cache
        cache.putComponentInst(componentInst);
      }
      return componentManager.copy(componentInst);
    } catch (Exception e) {
      throw new AdminException("Admin.getComponentInst", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_COMPONENT", "component Id: '" + sComponentId + "'", e);
    }
  }

  /**
   * return the component Inst name (ex.: kmelia) corresponding to the given ID
   */
  public String getComponentInstName(String bDriverComponentId) throws AdminException {
    return componentManager.getComponentInstName(domainDriverManager, bDriverComponentId);
  }

  /**
   * Get the parameters for the given component
   */
  public List<Parameter> getComponentParameters(String sComponentId) {
    try {
      return componentManager.getParameters(domainDriverManager, getDriverComponentId(sComponentId));
    } catch (Exception e) {
      SilverTrace.error(MODULE_ADMIN, "Admin.getComponentParameters",
          "admin.EX_ERR_GET_COMPONENT_PARAMS", "sComponentId: '" + sComponentId + "'", e);
      return new ArrayList<Parameter>();
    }
  }

  /**
   * Return the value of the parameter for the given component and the given name of parameter
   */
  public String getComponentParameterValue(String sComponentId, String parameterName) {
    try {
      Parameter parameter =
          componentManager.getParameter(domainDriverManager, getDriverComponentId(sComponentId),
          parameterName);
      if (parameter != null) {
        return parameter.getValue();
      }
    } catch (Exception e) {
      SilverTrace.error(MODULE_ADMIN, "Admin.getComponentParameterValue",
          "admin.EX_ERR_GET_COMPONENT_PARAMS", "sComponentId: '" + sComponentId + "'", e);
      return "";
    }
    return "";
  }

  public void restoreComponentFromBasket(String sComponentId)
      throws AdminException {
    try {
      // Start transaction
      domainDriverManager.startTransaction(false);

      // update data in database
      componentManager.restoreComponentFromBasket(domainDriverManager, getDriverComponentId(
          sComponentId));

      // Get the component and put it in the cache
      ComponentInst componentInst = getComponentInst(sComponentId);

      if (useProfileInheritance && !componentInst.isInheritanceBlocked()) {
        // inherits profiles from space
        setSpaceProfilesToComponent(componentInst, null);
      }
      domainDriverManager.commit();
      cache.opUpdateComponent(componentInst);
      ComponentInstLight component = getComponentInstLight(sComponentId);
      TreeCache.addComponent(getDriverComponentId(sComponentId), component,
          getDriverSpaceId(component.getDomainFatherId()));
      createComponentIndex(component);
    } catch (Exception e) {
      rollback();
      throw new AdminException("Admin.restoreComponentFromBasket",
          SilverpeasException.ERROR, "admin.EX_ERR_RESTORE_COMPONENT_FROM_BASKET", "componentId = "
          + sComponentId);
    }
  }
  
  public void createComponentIndex(String componentId) {
    try {
      ComponentInstLight component = getComponentInstLight(componentId);
      createComponentIndex(component);
    } catch (AdminException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void createComponentIndex(ComponentInstLight componentInst) {
    FullIndexEntry indexEntry;

    if (componentInst != null) {
      // Index the component
      SilverTrace.debug(MODULE_ADMIN, "Admin.createComponentIndex",
          "root.MSG_GEN_ENTER_METHOD", "componentInst.getName() = "
          + componentInst.getName() + "' componentInst.getId() = "
          + componentInst.getId() + " componentInst.getLabel() = "
          + componentInst.getLabel());

      String componentId;
      if (componentInst.getId().startsWith(componentInst.getName())) {
        componentId = componentInst.getId();
      } else {
        componentId = componentInst.getName().concat(componentInst.getId());
      }
      indexEntry = new FullIndexEntry("Components", "Component", componentId);
      indexEntry.setTitle(componentInst.getLabel());
      indexEntry.setPreView(componentInst.getDescription());
      indexEntry.setCreationUser(Integer.toString(componentInst.getCreatedBy()));
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  public void deleteComponentIndex(ComponentInst componentInst) {
    String componentId = componentInst.getName().concat(componentInst.getId());
    FullIndexEntry indexEntry = new FullIndexEntry("Components", "Component",
        componentId);

    IndexEngineProxy.removeIndexEntry(indexEntry.getPK());
  }

  public String addComponentInst(String sUserId, ComponentInst componentInst)
      throws AdminException {
    return addComponentInst(sUserId, componentInst, true);
  }

  /**
   * Add the given component instance in Silverpeas
   */
  public String addComponentInst(String sUserId, ComponentInst componentInst,
      boolean startNewTransaction) throws AdminException {
    Connection connectionProd = null;
    String sDriverComponentId;

    try {
      connectionProd = openConnection(productionDbUrl, productionDbLogin, productionDbPassword,
          false);

      if (startNewTransaction) {
        // Open the connections with auto-commit to false
        domainDriverManager.startTransaction(false);
      }

      // Get the father space inst
      SpaceInst spaceInstFather = getSpaceInstById(componentInst.getDomainFatherId());

      // Create the component instance
      sDriverComponentId = componentManager.createComponentInst(componentInst, domainDriverManager,
          getDriverSpaceId(spaceInstFather.getId()));

      // Add the component to the space
      spaceInstFather.addComponentInst(componentInst);

      // Put the new Id for client
      componentInst.setId(sDriverComponentId);

      // Instantiate the component
      String componentName = componentInst.getName();
      String componentId = componentName + componentInst.getId();

      String[] asCompoNames = {componentName};
      String[] asCompoIds = {componentId};
      instantiateComponents(sUserId, asCompoIds, asCompoNames, spaceInstFather.getId(),
          connectionProd);

      // !!! Hard coded workaround !!!!!!!
      if (componentName.equals("sources") || componentName.equals("documentation")
          || componentName.equals("infoTracker")) {
        // Create the manager objects
        ContainerManager containerManager = new ContainerManager();
        ContentManager contentManager = new ContentManager();

        // Call the register functions
        containerManager.registerNewContainerInstance(connectionProd,
            componentId, "containerPDC", "fileBoxPlus");
        contentManager.registerNewContentInstance(connectionProd, componentId,
            "containerPDC", "fileBoxPlus");
      } else if (isContentManagedComponent(componentName)) {
        // Create the manager objects
        ContainerManager containerManager = new ContainerManager();
        ContentManager contentManager = new ContentManager();

        // Call the register functions
        containerManager.registerNewContainerInstance(connectionProd,
            componentId, "containerPDC", componentName);
        contentManager.registerNewContentInstance(connectionProd, componentId,
            "containerPDC", componentName);
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
      TreeCache.addComponent(sDriverComponentId, component,
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
        SilverTrace.error(MODULE_ADMIN, "Admin.addComponentInst",
            "root.EX_ERR_ROLLBACK", e1);
      }
      throw new AdminException("Admin.addComponentInst",
          SilverpeasException.ERROR, "admin.EX_ERR_ADD_COMPONENT",
          "component name: '" + componentInst.getName() + "'", e);
    } finally {
      // close connection
      closeConnection(connectionProd);
    }
  }

  boolean isContentManagedComponent(String componentName) {
    return "expertLocator".equals(componentName) || "questionReply".equals(componentName)
        || "whitePages".equals(componentName) || "kmelia".equals(componentName) || "survey".equals(
        componentName) || "toolbox".equals(componentName) || "quickinfo".equals(componentName)
        || "almanach".equals(componentName) || "quizz".equals(componentName)
        || "forums".equals(componentName) || "pollingStation".equals(componentName)
        || "bookmark".equals(componentName) || "chat".equals(componentName)
        || "infoLetter".equals(componentName) || "webSites".equals(componentName)
        || "gallery".equals(componentName) || "blog".equals(componentName);
  }

  public String deleteComponentInst(String userId, String componentId, boolean definitive) throws
      AdminException {
    return deleteComponentInst(userId, componentId, definitive, true);
  }

  /**
   * Delete the given component from Silverpeas
   * @param userId
   * @param componentId
   * @param definitive
   * @param startNewTransaction
   * @return
   * @throws AdminException 
   */
  public String deleteComponentInst(String userId, String componentId, boolean definitive,
      boolean startNewTransaction) throws AdminException {
    Connection connectionProd = null;
    ComponentInst componentInst;

    SilverTrace.spy(MODULE_ADMIN, "Admin.deleteComponentInst()", "ACP",
        componentId, "", userId, SilverTrace.SPY_ACTION_DELETE);

    try {
      if (startNewTransaction) {
        // Open the connections with auto-commit to false
        domainDriverManager.startTransaction(false);
      }

      // Convert the client id in driver id
      String sDriverComponentId = getDriverComponentId(componentId);

      // Get the component to delete
      componentInst = getComponentInst(sDriverComponentId, true, null);

      // Get the father id
      String sFatherClientId = componentInst.getDomainFatherId();

      if (!definitive) {
        // delete the profiles instance
        for (int nI = 0; nI < componentInst.getNumProfileInst(); nI++) {
          deleteProfileInst(componentInst.getProfileInst(nI).getId(), false);
        }

        componentManager.sendComponentToBasket(domainDriverManager,
            sDriverComponentId, componentInst.getLabel() + Admin.basketSuffix, userId);
      } else {
        connectionProd = openConnection(productionDbUrl, productionDbLogin,
            productionDbPassword, false);

        // Uninstantiate the components
        String componentName = componentInst.getName();
        String[] asCompoName = {componentName};
        String[] asCompoId = {componentId};
        unInstantiateComponents(userId, asCompoId, asCompoName,
            getClientSpaceId(sFatherClientId), connectionProd);

        // delete the profiles instance
        for (int nI = 0;
            nI < componentInst.getNumProfileInst();
            nI++) {
          deleteProfileInst(componentInst.getProfileInst(nI).getId(), false);
        }

        // Delete the component
        componentManager.deleteComponentInst(componentInst, domainDriverManager);

        // !!! Hard coded workaround !!!!!!!
        if (componentName.equals("sources")
            || componentName.equals("documentation")
            || componentName.equals("infoTracker")) {
          // Create the manager objects
          ContainerManager containerManager = new ContainerManager();
          ContentManager contentManager = new ContentManager();

          // Call the unregister functions
          containerManager.unregisterNewContainerInstance(connectionProd,
              componentId, "containerPDC", "fileBoxPlus");
          contentManager.unregisterNewContentInstance(connectionProd,
              componentId, "containerPDC", "fileBoxPlus");
        } else if (isContentManagedComponent(componentName)) {
          // Create the manager objects
          ContainerManager containerManager = new ContainerManager();
          ContentManager contentManager = new ContentManager();

          // Call the unregister functions
          containerManager.unregisterNewContainerInstance(connectionProd,
              componentId, "containerPDC", componentName);
          contentManager.unregisterNewContentInstance(connectionProd,
              componentId, "containerPDC", componentName);
        }

        // commit the transactions
        connectionProd.commit();
      }
      if (startNewTransaction) {
        domainDriverManager.commit();
      }
      cache.opRemoveComponent(componentInst);
      TreeCache.removeComponent(getDriverSpaceId(sFatherClientId), sDriverComponentId);

      // desindexation du composant
      deleteComponentIndex(componentInst);

      return componentId;
    } catch (Exception e) {
      try {
        // Roll back the transactions
        if (startNewTransaction) {
          domainDriverManager.rollback();
        }
        connectionProd.rollback();
      } catch (Exception e1) {
        SilverTrace.error(MODULE_ADMIN, "Admin.deleteComponentInst",
            "root.EX_ERR_ROLLBACK", e1);
      }
      throw new AdminException("Admin.deleteComponentInst",
          SilverpeasException.ERROR, "admin.EX_ERR_DELETE_COMPONENT",
          "component Id: '" + componentId + "'", e);
    } finally {
      // close connection
      closeConnection(connectionProd);
    }
  }

  public void updateComponentOrderNum(String sComponentId, int orderNum)
      throws AdminException {
    try {
      SilverTrace.debug(MODULE_ADMIN, "Admin.updateComponentOrderNum",
          "root.MSG_GEN_ENTER_METHOD", "Component id: '" + sComponentId
          + "' New Order num: " + Integer.toString(orderNum));
      String sDriverComponentId = getDriverComponentId(sComponentId);

      // Open the connections with auto-commit to false
      domainDriverManager.startTransaction(false);

      // Update the Component in tables
      componentManager.updateComponentOrder(domainDriverManager,
          sDriverComponentId, orderNum);

      domainDriverManager.commit();

      cache.opUpdateComponent(componentManager.getComponentInst(
          domainDriverManager, sDriverComponentId, null));
    } catch (Exception e) {
      rollback();
      throw new AdminException("Admin.updateComponentOrderNum",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_COMPONENT",
          "Component Id : '" + sComponentId + "'", e);
    }
  }

  /**
   * Update the given component in Silverpeas
   */
  public String updateComponentInst(ComponentInst componentInstNew) throws AdminException {
    try {
      ComponentInst oldComponentInst = getComponentInst(componentInstNew.getId());

      String componentClientId = componentInstNew.getId();

      // Open the connections with auto-commit to false
      domainDriverManager.startTransaction(false);

      // Convert the client space Id in driver space Id
      String sDriverComponentId = getDriverComponentId(componentInstNew.getId());
      componentInstNew.setId(sDriverComponentId);

      // Update the components in tables
      componentManager.updateComponentInst(domainDriverManager, componentInstNew);

      // Update the inherited rights
      if (useProfileInheritance
          && (oldComponentInst.isInheritanceBlocked() != componentInstNew.isInheritanceBlocked())) {
        updateComponentInheritance(oldComponentInst, componentInstNew.isInheritanceBlocked());
      }

      // commit the transactions
      domainDriverManager.commit();

      cache.opUpdateComponent(componentInstNew);

      // put clientId as Id
      componentInstNew.setId(componentClientId);

      // indexation du composant
      createComponentIndex(componentClientId);

      return componentClientId;
    } catch (Exception e) {
      rollback();
      throw new AdminException("Admin.updateComponentInst", SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_COMPONENT", "component Id: '" + componentInstNew.getId() + "'", e);
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
  private void updateComponentInheritance(ComponentInst component,
      boolean inheritanceBlocked) throws AdminException {
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
    } catch (Exception e) {
      rollback();
      throw new AdminException("Admin.updateComponentInst",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_COMPONENT",
          "component Id: '" + component.getId() + "'", e);
    }
  }

  /**
   * Set space profiles to a subspace. There is no persistance. The subspace object is enriched.
   *
   * @param subSpace     the object to set profiles
   * @param space the object to get profiles
   * @throws AdminException
   */
  private void setSpaceProfilesToSubSpace(final SpaceInst subSpace, final SpaceInst space)
      throws AdminException {
    SpaceInst currentSpace = space;
    if (currentSpace == null) {
      currentSpace = getSpaceInstById(subSpace.getDomainFatherId(), true);
    }

    setSpaceProfileToSubSpace(subSpace, currentSpace, SilverpeasRole.admin);
    setSpaceProfileToSubSpace(subSpace, currentSpace, SilverpeasRole.publisher);
    setSpaceProfileToSubSpace(subSpace, currentSpace, SilverpeasRole.writer);
    setSpaceProfileToSubSpace(subSpace, currentSpace, SilverpeasRole.reader);
  }

  /**
   * Set space profile to a subspace. There is no persistance. The subspace object is enriched.
   *
   * @param subSpace    the object to set profiles
   * @param space       the object to get profiles
   * @param role the name of the profile
   * @throws AdminException
   */
  private void setSpaceProfileToSubSpace(SpaceInst subSpace, SpaceInst space, SilverpeasRole role) {
    SpaceProfileInst subSpaceProfile = null;
    String profileName = role.toString();

    // Retrieve superSpace local profile
    SpaceProfileInst profile = space.getSpaceProfileInst(profileName);
    if (profile != null) {
      subSpaceProfile = new SpaceProfileInst();
      subSpaceProfile.setName(profileName);
      subSpaceProfile.setInherited(true);
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

  /**
   * Set space profile to a component. There is persistance.
   *
   * @param component the object to set profiles
   * @param space the object to get profiles
   * @throws AdminException
   */
  public void setSpaceProfilesToComponent(ComponentInst component, SpaceInst space) throws
      AdminException {
    WAComponent waComponent = Instanciateur.getWAComponent(component.getName());
    List<Profile> componentRoles = waComponent.getProfiles();

    if (space == null) {
      space = getSpaceInstById(component.getDomainFatherId(), false);
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
      String spaceRole;
      for (String spaceRole1 : spaceRoles) {
        spaceRole = spaceRole1;
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

      if (!inheritedProfile.getAllGroups().isEmpty()
          || !inheritedProfile.getAllUsers().isEmpty()) {
        if (StringUtil.isDefined(inheritedProfile.getId())) {
          updateProfileInst(inheritedProfile, null, false);
        } else {
          addProfileInst(inheritedProfile, null, false);
        }
      }
    }
  }

  /**
   * Move the given component in Silverpeas.
   *
   * @param spaceId
   * @param componentId
   * @param idComponentBefore
   * @param componentInsts
   * @throws AdminException
   */
  public void moveComponentInst(String spaceId, String componentId, String idComponentBefore,
      ComponentInst[] componentInsts) throws AdminException {
    try {
      SilverTrace.info(MODULE_ADMIN, "admin.moveComponentInst", "root.MSG_GEN_PARAM_VALUE",
          "spaceId= " + spaceId + " componentId=" + componentId);
      String sDriverComponentId = getDriverComponentId(componentId);
      // Convert the client space Id in driver space Id
      String sDriverSpaceId = getDriverSpaceId(spaceId);
      SilverTrace.info(MODULE_ADMIN, "admin.moveComponentInst", "root.MSG_GEN_PARAM_VALUE",
          "sDriverSpaceId= " + sDriverSpaceId + " sDriverComponentId=" + sDriverComponentId);
      ComponentInst componentInst = getComponentInst(componentId);
      String oldSpaceId = componentInst.getDomainFatherId();
      // Open the connections with auto-commit to false
      domainDriverManager.startTransaction(false);
      // Update the components in tables
      componentManager.moveComponentInst(domainDriverManager, sDriverSpaceId, sDriverComponentId);
      componentInst.setDomainFatherId(getDriverSpaceId(spaceId));
      // Set component in order
      SilverTrace.info(MODULE_ADMIN, "admin.moveComponentInst", "root.MSG_GEN_PARAM_VALUE",
          "Avant setComponentPlace: componentId=" + componentId + " idComponentBefore="
          + idComponentBefore);
      setComponentPlace(componentId, idComponentBefore, componentInsts);

      // Update extraParamPage from Space if necessary
      SilverTrace.info(MODULE_ADMIN, "admin.moveComponentInst", "root.MSG_GEN_PARAM_VALUE",
          "FirstPageExtraParam=" + getSpaceInstById(oldSpaceId).getFirstPageExtraParam()
          + " oldSpaceId=" + oldSpaceId);
      SpaceInst oldSpaceInst = getSpaceInstById(getDriverSpaceId(oldSpaceId));
      SilverTrace.info(MODULE_ADMIN, "admin.moveComponentInst", "root.MSG_GEN_PARAM_VALUE",
          "oldSpaceInst=" + oldSpaceInst + " componentId=" + componentId);
      if (oldSpaceInst.getFirstPageExtraParam() != null) {
        if (oldSpaceInst.getFirstPageExtraParam().equals(componentId)) {
          oldSpaceInst.setFirstPageExtraParam("");
          oldSpaceInst.setFirstPageType(0);
          updateSpaceInst(oldSpaceInst);
        }
      }
      // commit the transactions
      domainDriverManager.commit();
      // Remove component from the Cache
      cache.resetSpaceInst();
      cache.resetComponentInst();
      TreeCache.setComponents(getDriverSpaceId(oldSpaceId),
          componentManager.getComponentsInSpace(Integer.parseInt(getDriverSpaceId(oldSpaceId))));
      TreeCache.setComponents(getDriverSpaceId(spaceId),
          componentManager.getComponentsInSpace(Integer.parseInt(getDriverSpaceId(spaceId))));
    } catch (Exception e) {
      rollback();
      throw new AdminException("Admin.moveComponentInst", SilverpeasException.ERROR,
          "admin.EX_ERR_MOVE_COMPONENT",
          "spaceId = " + spaceId + " component Id: '" + componentId + " ", e);
    }
  }

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

  public String getRequestRouter(String sComponentName) {
    WAComponent wac = Instanciateur.getWAComponent(sComponentName);
    if (wac == null || !StringUtil.isDefined(wac.getRouter())) {
      return "R" + sComponentName;
    }
    return wac.getRouter();
  }

  // --------------------------------------------------------------------------------------------------------
  // PROFILE RELATED FUNCTIONS
  // --------------------------------------------------------------------------------------------------------
  /**
   * Get all the profiles name available for the given component.
   *
   * @param sComponentName
   * @return
   * @throws AdminException
   */
  public String[] getAllProfilesNames(String sComponentName) throws AdminException {
    String[] asProfiles = null;
    WAComponent wac = Instanciateur.getWAComponent(sComponentName);
    if (wac != null) {
      List<Profile> profiles = wac.getProfiles();
      List<String> profileNames = new ArrayList<String>(profiles.size());
      for (Profile profile :
          profiles) {
        profileNames.add(profile.getName());
      }
      asProfiles = profileNames.toArray(new String[profileNames.size()]);
    }

    if (asProfiles != null) {
      return asProfiles;
    }
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  /**
   * Get the profile label from its name.
   *
   * @param sComponentName
   * @param sProfileName
   * @return
   * @throws AdminException
   */
  public String getProfileLabelfromName(String sComponentName, String sProfileName, String lang)
      throws AdminException {
    WAComponent wac = Instanciateur.getWAComponent(sComponentName);
    if (wac != null) {
      List<Profile> profiles = wac.getProfiles();
      String sProfileLabel = sProfileName;
      for (Profile profile : profiles) {
        if (profile.getName().equals(sProfileName)) {
          return profile.getLabel().get(lang);
        }
      }
      return sProfileLabel;
    }
    return sProfileName;
  }

  /**
   * Get the profile instance corresponding to the given id
   *
   * @param sProfileId
   * @return
   * @throws AdminException
   */
  public ProfileInst getProfileInst(String sProfileId) throws AdminException {
    ProfileInst profileInst = cache.getProfileInst(sProfileId);
    if (profileInst == null) {
      profileInst = profileManager.getProfileInst(domainDriverManager, sProfileId, null);
      cache.putProfileInst(profileInst);
    }
    return profileInst;
  }

  public List<ProfileInst> getProfilesByObject(String objectId, String objectType,
      String componentId) throws AdminException {
    List<ProfileInst> profiles = profiledObjectManager.getProfiles(domainDriverManager,
        Integer.parseInt(objectId), objectType,
        Integer.parseInt(getDriverComponentId(componentId)));
    return profiles;
  }

  public String[] getProfilesByObjectAndUserId(int objectId, String objectType, String componentId,
      String userId) throws AdminException {
    List<String> groups = getAllGroupsOfUser(userId);
    return profiledObjectManager.getUserProfileNames(objectId, objectType,
        Integer.parseInt(getDriverComponentId(componentId)), Integer.parseInt(userId), groups);
  }

  public boolean isObjectAvailable(String componentId, int objectId, String objectType,
      String userId) throws AdminException {
    if (userId == null) {
      return true;
    }
    return getProfilesByObjectAndUserId(objectId, objectType, componentId, userId).length > 0;
  }

  public String addProfileInst(ProfileInst profileInst) throws AdminException {
    return addProfileInst(profileInst, null, true);
  }

  public String addProfileInst(ProfileInst profileInst, String userId) throws AdminException {
    return addProfileInst(profileInst, userId, true);
  }

  /**
   * Get the given profile instance from Silverpeas
   */
  private String addProfileInst(ProfileInst profileInst, String userId, boolean startNewTransaction)
      throws AdminException {
    try {
      if (startNewTransaction) {
        domainDriverManager.startTransaction(false);
      }
      String sDriverFatherId = getDriverComponentId(profileInst.getComponentFatherId());
      String sProfileId = profileManager.createProfileInst(profileInst, domainDriverManager,
          sDriverFatherId);
      profileInst.setId(sProfileId);

      if (profileInst.getObjectId() == -1 || profileInst.getObjectId() == 0) {
        ComponentInst componentInstFather = getComponentInst(sDriverFatherId, true, null);
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
        cache.opAddProfile(profileManager.getProfileInst(domainDriverManager, sProfileId, null));
      }
      return sProfileId;
    } catch (Exception e) {
      if (startNewTransaction) {
        rollback();
      }
      throw new AdminException("Admin.addProfileInst", SilverpeasException.ERROR,
          "admin.EX_ERR_ADD_PROFILE", "profile name: '" + profileInst.getName() + "'", e);
    }
  }

  public String deleteProfileInst(String sProfileId) throws AdminException {
    return deleteProfileInst(sProfileId, null);
  }

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
    ProfileInst profile = profileManager.getProfileInst(domainDriverManager, profileId, null);
    try {
      if (startNewTransaction) {
        domainDriverManager.startTransaction(false);
      }
      profileManager.deleteProfileInst(profile, domainDriverManager);
      if (StringUtil.isDefined(
          userId) && (profile.getObjectId() == -1 || profile.getObjectId() == 0)) {
        ComponentInst component = getComponentInst(profile.getComponentFatherId(), true, null);

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
    }
  }

  public String updateProfileInst(ProfileInst profileInstNew) throws AdminException {
    return updateProfileInst(profileInstNew, null, true);
  }

  public String updateProfileInst(ProfileInst profileInstNew, String userId) throws AdminException {
    return updateProfileInst(profileInstNew, userId, true);
  }

  /**
   * Update the given profile in Silverpeas.
   *
   * @param newProfile
   * @param userId
   * @param startNewTransaction
   * @return
   * @throws AdminException
   */
  private String updateProfileInst(ProfileInst newProfile, String userId,
      boolean startNewTransaction) throws AdminException {
    if (StringUtil.isDefined(userId)) {
      SilverTrace.spy(MODULE_ADMIN, "Admin.updateProfileInst", "unknown", newProfile.
          getComponentFatherId(), newProfile.getName(), userId, SilverTrace.SPY_ACTION_UPDATE);
    }
    try {
      if (startNewTransaction) {
        domainDriverManager.startTransaction(false);
      }
      profileManager.updateProfileInst(domainDriverManager, newProfile);
      if (StringUtil.isDefined(
          userId) && (newProfile.getObjectId() == -1 || newProfile.getObjectId() == 0)) {
        ComponentInst component = getComponentInst(newProfile.getComponentFatherId(), true, null);
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
    }
  }

  // --------------------------------------------------------------------------------------------------------
  // SPACE PROFILE RELATED FUNCTIONS
  // --------------------------------------------------------------------------------------------------------
  /**
   * Get the space profile instance corresponding to the given ID
   *
   * @param speceProfileId
   * @return
   * @throws AdminException
   */
  public SpaceProfileInst getSpaceProfileInst(String speceProfileId) throws AdminException {
    return spaceProfileManager.getSpaceProfileInst(domainDriverManager, speceProfileId, null);
  }

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
    try {
      if (startNewTransaction) {
        domainDriverManager.startTransaction(false);
      }
      String spaceId = getDriverComponentId(spaceProfile.getSpaceFatherId());

      String sSpaceProfileId = spaceProfileManager.createSpaceProfileInst(spaceProfile,
          domainDriverManager, spaceId);
      spaceProfile.setId(sSpaceProfileId);
      if (StringUtil.isDefined(userId)) {
        SpaceInst spaceInstFather = getSpaceInstById(spaceId, false);
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
          "admin.EX_ERR_ADD_SPACE_PROFILE", "space profile name: '" + spaceProfile.getName() + "'",
          e);
    }
  }

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
      String spaceId = getDriverComponentId(spaceProfileInst.getSpaceFatherId());
      if (StringUtil.isDefined(userId)) {
        SpaceInst spaceInstFather = getSpaceInstById(spaceId, false);
        spaceInstFather.setUpdaterUserId(userId);
        updateSpaceInst(spaceInstFather);
      }
      if (!spaceProfileInst.isInherited()) {
        SpaceProfileInst inheritedProfile =
            spaceProfileManager.getInheritedSpaceProfileInstByName(domainDriverManager, spaceId,
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
    }
  }

  /**
   * Update the given space profile in Silverpeas
   */
  private String updateSpaceProfileInst(SpaceProfileInst newSpaceProfile) throws AdminException {
    return updateSpaceProfileInst(newSpaceProfile, null);
  }

  public String updateSpaceProfileInst(SpaceProfileInst newSpaceProfile, String userId) throws
      AdminException {
    try {
      domainDriverManager.startTransaction(false);
      SpaceProfileInst oldSpaceProfile = spaceProfileManager.getSpaceProfileInst(
          domainDriverManager,
          newSpaceProfile.getId(), null);
      String sSpaceProfileNewId = spaceProfileManager.updateSpaceProfileInst(oldSpaceProfile,
          domainDriverManager, newSpaceProfile);

      if (!"Manager".equalsIgnoreCase(oldSpaceProfile.getName())) {
        String spaceId = getDriverSpaceId(newSpaceProfile.getSpaceFatherId());
        if (StringUtil.isDefined(userId)) {
          SpaceInst spaceInstFather = getSpaceInstById(spaceId, false);
          spaceInstFather.setUpdaterUserId(userId);
          updateSpaceInst(spaceInstFather);
        }
        // Add inherited users and groups for this role
        SpaceProfileInst inheritedProfile =
            spaceProfileManager.getInheritedSpaceProfileInstByName(domainDriverManager, spaceId,
            oldSpaceProfile.getName());
        if (inheritedProfile != null) {
          newSpaceProfile.addGroups(inheritedProfile.getAllGroups());
          newSpaceProfile.addUsers(inheritedProfile.getAllUsers());
        }
        spreadSpaceProfile(spaceId, newSpaceProfile);
      }
      domainDriverManager.commit();
      cache.opUpdateSpaceProfile(spaceProfileManager.getSpaceProfileInst(domainDriverManager,
          newSpaceProfile.getId(), null));

      return sSpaceProfileNewId;
    } catch (Exception e) {
      rollback();
      throw new AdminException("Admin.updateSpaceProfileInst", SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_SPACEPROFILE", "space profile Id: '" + newSpaceProfile.getId() + "'",
          e);
    }
  }

  private String spaceRole2ComponentRole(String spaceRole, String componentName) {
    return roleMapping.getString(componentName + "_" + spaceRole);
  }

  private List<String> componentRole2SpaceRoles(String componentRole,
      String componentName) {
    List<String> roles = new ArrayList<String>();

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

  private void spreadSpaceProfile(String spaceId, SpaceProfileInst spaceProfile)
      throws AdminException {
    SilverTrace.info("admin", "Admin.spreadSpaceProfile", "root.MSG_GEN_ENTER_METHOD",
        "spaceId = " + spaceId + ", profile = " + spaceProfile.getName());

    // update profile in components
    List<ComponentInstLight> components = TreeCache.getComponents(spaceId);
    for (ComponentInstLight component : components) {
      if (component != null && !component.isInheritanceBlocked()) {
        String componentRole = spaceRole2ComponentRole(spaceProfile.getName(),
            component.getName());
        if (componentRole != null) {
          ProfileInst inheritedProfile =
              profileManager.getInheritedProfileInst(domainDriverManager,
              getDriverComponentId(component.getId()),
              componentRole);
          if (inheritedProfile != null) {
            inheritedProfile.removeAllGroups();
            inheritedProfile.removeAllUsers();

            inheritedProfile.addGroups(spaceProfile.getAllGroups());
            inheritedProfile.addUsers(spaceProfile.getAllUsers());
            //updateProfileInst(inheritedProfile);

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
        SpaceProfileInst subSpaceProfile =
            spaceProfileManager.getInheritedSpaceProfileInstByName(domainDriverManager,
            subSpace.getShortId(), spaceProfile.getName());
        if (subSpaceProfile != null) {
          subSpaceProfile.setGroups(spaceProfile.getAllGroups());
          subSpaceProfile.setUsers(spaceProfile.getAllUsers());
          updateSpaceProfileInst(subSpaceProfile);
        } else {
          subSpaceProfile = new SpaceProfileInst();
          subSpaceProfile.setName(spaceProfile.getName());
          subSpaceProfile.setInherited(true);
          subSpaceProfile.setSpaceFatherId(subSpace.getShortId());
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
  /**
   * Get the group names corresponding to the given group ids.
   *
   * @param groupIds
   * @return
   * @throws AdminException
   */
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

  /**
   * Get the group name corresponding to the given group id.
   *
   * @param sGroupId
   * @return
   * @throws AdminException
   */
  public String getGroupName(String sGroupId) throws AdminException {
    return getGroup(sGroupId).getName();
  }

  /**
   * Get the all the groups ids available in Silverpeas.
   *
   * @return
   * @throws AdminException
   */
  public String[] getAllGroupIds() throws AdminException {
    return groupManager.getAllGroupIds(domainDriverManager);
  }

  /**
   * Tests if group exists in Silverpeas.
   *
   * @param groupName
   * @return true if a group with the given name
   * @throws AdminException
   */
  public boolean isGroupExist(String groupName) throws AdminException {
    return groupManager.isGroupExist(domainDriverManager, groupName);
  }

  /**
   * Get group information with the given id
   *
   * @param groupId
   * @return
   * @throws AdminException
   */
  public Group getGroup(String groupId) throws AdminException {
    return groupManager.getGroup(domainDriverManager, groupId);
  }

  public List<String> getPathToGroup(String groupId) throws AdminException {
    return groupManager.getPathToGroup(domainDriverManager, groupId);
  }

  /**
   * Get group information with the given group name.
   *
   * @param groupName
   * @param domainFatherId
   * @return
   * @throws AdminException
   */
  public Group getGroupByNameInDomain(String groupName, String domainFatherId)
      throws AdminException {
    return groupManager.getGroupByNameInDomain(domainDriverManager, groupName, domainFatherId);
  }

  /**
   * Get groups information with the given ids.
   *
   * @param asGroupId
   * @return
   * @throws AdminException
   */
  public Group[] getGroups(String[] asGroupId) throws AdminException {
    if (asGroupId == null) {
      return ArrayUtil.EMPTY_GROUP_ARRAY;
    }
    Group[] aGroup = new Group[asGroupId.length];
    for (int nI = 0;
        nI < asGroupId.length;
        nI++) {
      aGroup[nI] = getGroup(asGroupId[nI]);
    }
    return aGroup;
  }

  /**
   * Add the given group in Silverpeas.
   *
   * @param group
   * @return
   * @throws AdminException
   */
  public String addGroup(Group group) throws AdminException {
    try {
      return addGroup(group, false);
    } catch (Exception e) {
      throw new AdminException("Admin.addGroup", SilverpeasException.ERROR,
          "admin.EX_ERR_ADD_GROUP", "group name: '" + group.getName() + "'", e);
    }
  }

  /**
   * Add the given group in Silverpeas.
   *
   * @param group
   * @param onlyInSilverpeas
   * @return
   * @throws AdminException
   */
  public String addGroup(Group group, boolean onlyInSilverpeas) throws AdminException {
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
    }
  }

  /**
   * Delete the group with the given Id The delete is apply recursively to the sub-groups.
   *
   * @param sGroupId
   * @return
   * @throws AdminException
   */
  public String deleteGroupById(String sGroupId) throws AdminException {
    try {
      return deleteGroupById(sGroupId, false);
    } catch (Exception e) {
      throw new AdminException("Admin.deleteGroupById", SilverpeasException.ERROR,
          "admin.EX_ERR_DELETE_GROUP", "group Id: '" + sGroupId + "'", e);
    }
  }

  /**
   * Delete the group with the given Id The delete is apply recursively to the sub-groups.
   *
   * @param sGroupId
   * @param onlyInSilverpeas
   * @return
   * @throws AdminException
   */
  public String deleteGroupById(String sGroupId, boolean onlyInSilverpeas) throws AdminException {
    Group group = null;
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
    }
  }

  /**
   * Update the given group in Silverpeas and specific.
   *
   * @param group
   * @return
   * @throws AdminException
   */
  public String updateGroup(Group group) throws AdminException {
    try {
      return updateGroup(group, false);
    } catch (Exception e) {
      throw new AdminException("Admin.updateGroup", SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_GROUP", "group name: '" + group.getName() + "'", e);
    }
  }

  /**
   * Update the given group in Silverpeas and specific
   *
   * @param group
   * @param onlyInSilverpeas
   * @return
   * @throws AdminException
   */
  public String updateGroup(Group group, boolean onlyInSilverpeas) throws AdminException {
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
    }
  }

  public void removeUserFromGroup(String sUserId, String sGroupId) throws AdminException {
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
        SilverTrace.error("admin", "Admin.removeUserFromGroup", "root.EX_ERR_ROLLBACK",
            e1);
      }
      throw new AdminException("Admin.removeUserFromGroup", SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_GROUP", "groupId = " + sGroupId + ", userId = " + sUserId,
          e);
    }
  }

  public void addUserInGroup(String sUserId, String sGroupId) throws AdminException {
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
        SilverTrace.error("admin", "Admin.addUserInGroup", "root.EX_ERR_ROLLBACK",
            e1);
      }
      throw new AdminException("Admin.addUserInGroup", SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_GROUP", "groupId = " + sGroupId + ", userId = " + sUserId,
          e);
    }
  }

  /**
   * Get Silverpeas organization
   */
  public AdminGroupInst[] getAdminOrganization() throws AdminException {
    return groupManager.getAdminOrganization(domainDriverManager);
  }

  // JCC 25/03/2002 BEGIN
  /**
   * Gets the set of Ids denoting the direct subgroups of a given group
   *
   * @param groupId The ID of the parent group
   * @return the Ids as an array of <code>String</code>.
   */
  public String[] getAllSubGroupIds(String groupId) throws AdminException {
    return groupManager.getAllSubGroupIds(domainDriverManager, groupId);
  }

  public String[] getAllSubGroupIdsRecursively(String groupId)
      throws AdminException {
    List<String> groupIds = groupManager.getAllSubGroupIdsRecursively(groupId);
    return groupIds.toArray(new String[groupIds.size()]);
  }

  /**
   * Gets the set of Ids denoting the groupswithout any parent.
   * @return the Ids as an array of <code>String</code>.
   */
  public String[] getAllRootGroupIds() throws AdminException {
    return groupManager.getAllRootGroupIds(domainDriverManager);
  }

  //
  // --------------------------------------------------------------------------------------------------------
  // GROUP PROFILE RELATED FUNCTIONS
  // --------------------------------------------------------------------------------------------------------
  /**
   * Get the group profile instance corresponding to the given ID
   */
  public GroupProfileInst getGroupProfileInst(String groupId) throws AdminException {
    return groupProfileManager.getGroupProfileInst(domainDriverManager, null,
        groupId);
  }

  public String addGroupProfileInst(GroupProfileInst spaceProfileInst)
      throws AdminException {
    return addGroupProfileInst(spaceProfileInst, true);
  }

  /**
   * Add the space profile instance from Silverpeas
   */
  public String addGroupProfileInst(GroupProfileInst groupProfileInst,
      boolean startNewTransaction) throws AdminException {
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
    }
  }

  public String deleteGroupProfileInst(String groupId) throws AdminException {
    return deleteGroupProfileInst(groupId, true);
  }

  /**
   * Delete the given space profile from Silverpeas
   */
  public String deleteGroupProfileInst(String groupId,
      boolean startNewTransaction) throws AdminException {
    // Get the SpaceProfile to delete
    GroupProfileInst groupProfileInst =
        groupProfileManager.getGroupProfileInst(domainDriverManager, null, groupId);

    try {
      if (startNewTransaction) {
        // Open the connections with auto-commit to false
        domainDriverManager.startTransaction(false);
      }

      // Delete the Profile in tables
      groupProfileManager.deleteGroupProfileInst(groupProfileInst,
          domainDriverManager);

      // m_Cache.opRemoveSpaceProfile(groupProfileInst);

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
    }
  }

  /**
   * Update the given space profile in Silverpeas
   */
  public String updateGroupProfileInst(GroupProfileInst groupProfileInstNew)
      throws AdminException {
    try {
      String sSpaceProfileNewId = groupProfileInstNew.getId();
      if (!StringUtil.isDefined(sSpaceProfileNewId)) {
        // the profile doesn't exist, we have to create it
        sSpaceProfileNewId = addGroupProfileInst(groupProfileInstNew);
      } else {
        domainDriverManager.startTransaction(false);

        GroupProfileInst oldSpaceProfile =
            groupProfileManager.getGroupProfileInst(domainDriverManager, null, groupProfileInstNew.
            getGroupId());

        // Update the group profile in tables
        groupProfileManager.updateGroupProfileInst(oldSpaceProfile,
            domainDriverManager, groupProfileInstNew);

        domainDriverManager.commit();

        // m_Cache.opUpdateSpaceProfile(m_GroupProfileInstManager.getGroupProfileInst(m_DDManager,
        // groupProfileInstNew.getId(), null));
      }

      return sSpaceProfileNewId;
    } catch (Exception e) {
      rollback();
      throw new AdminException("Admin.updateGroupProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_SPACEPROFILE",
          "space profile Id: '" + groupProfileInstNew.getId() + "'", e);
    }
  }
  
  /**
   * @throws AdminException
   */
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

  /**
   * @param domainId
   * @throws AdminException
   */
  public void indexGroups(String domainId) throws AdminException {
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
  /**
   * Get all the users Ids available in Silverpeas
   */
  public String[] getAllUsersIds() throws AdminException {
    return userManager.getAllUsersIds(domainDriverManager);
  }

  /**
   * Get the user detail corresponding to the given user Id
   *
   * @param sUserId the user id.
   * @return the user detail corresponding to the given user Id
   * @throws AdminException
   */
  public UserDetail getUserDetail(String sUserId) throws AdminException {
    if (!StringUtil.isDefined(sUserId) || "-1".equals(sUserId)) {
      return null;
    }
    UserDetail ud = cache.getUserDetail(sUserId);
    if (ud == null) {
      ud = userManager.getUserDetail(domainDriverManager, sUserId);
      if (ud != null) {
        cache.putUserDetail(sUserId, ud);
      }
    }
    return ud;
  }

  /**
   * Get the user details corresponding to the given user Ids
   */
  public UserDetail[] getUserDetails(String[] asUserId) throws AdminException {
    if (asUserId == null) {
      return ArrayUtil.EMPTY_USER_DETAIL_ARRAY;
    }

    UserDetail[] aUserDetail = new UserDetail[asUserId.length];
    for (int nI = 0;
        nI < asUserId.length;
        nI++) {
      try {
        aUserDetail[nI] = getUserDetail(asUserId[nI]);
      } catch (AdminException e) {
        SilverTrace.error("admin", "Admin.getUserDetails",
            "admin.EX_ERR_GET_USER_DETAILS", "user id: '" + asUserId[nI] + "'",
            e);
        aUserDetail[nI] = null;
      }
    }

    return aUserDetail;
  }

  /**
   * Get the user Id corresponding to Domain/Login
   *
   * @param sLogin
   * @param sDomainId
   * @return
   * @throws AdminException
   */
  public String getUserIdByLoginAndDomain(String sLogin, String sDomainId) throws AdminException {
    Domain[] theDomains;
    String valret = null;

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
              "admin.EX_ERR_GET_USER_BY_LOGIN_DOMAIN", "login: '" + sLogin
              + "', domain id: '" + sDomainId + "'", e);
        }
      }
      if (valret == null) {
        throw new AdminException("Admin.getUserIdByLoginAndDomain", SilverpeasException.ERROR,
            "admin.EX_ERR_USER_NOT_FOUND", "login: '" + sLogin + "', in all domains");
      }
    } else {

      valret = userManager.getUserIdByLoginAndDomain(domainDriverManager, sLogin,
          sDomainId);
    }
    return valret;
  }

  /**
   * @param authenticationKey The authentication key.
   * @return The user id corresponding to the authentication key.
   * @throws Exception
   */
  public String getUserIdByAuthenticationKey(String authenticationKey) throws Exception {

    Map<String, String> userParameters = domainDriverManager.authenticate(authenticationKey);
    String login = userParameters.get("login");
    String domainId = userParameters.get("domainId");
    return userManager.getUserIdByLoginAndDomain(domainDriverManager, login, domainId);
  }

  /**
   * Get the user corresponding to the given user Id (only infos in cache table)
   *
   * @param sUserId
   * @return
   * @throws AdminException
   */
  public UserFull getUserFull(String sUserId) throws AdminException {
    return userManager.getUserFull(domainDriverManager, sUserId);
  }

  public UserFull getUserFull(String domainId, String specificId)
      throws Exception {
    SilverTrace.info("admin", "admin.getUserFull", "root.MSG_GEN_ENTER_METHOD",
        "domainId=" + domainId);
    DomainDriver synchroDomain = domainDriverManager.getDomainDriver(Integer.parseInt(
        domainId));

    return synchroDomain.getUserFull(specificId);
  }

  /**
   * Add the given user in Silverpeas and specific domain
   */
  public String addUser(UserDetail userDetail) throws AdminException {
    try {
      return addUser(userDetail, false);
    } catch (Exception e) {
      throw new AdminException("Admin.addUser", SilverpeasException.ERROR,
          "admin.EX_ERR_ADD_USER", userDetail.getFirstName() + " "
          + userDetail.getLastName(), e);
    }
  }

  /**
   * Add the given user in Silverpeas and specific domain
   *
   * @param userDetail user to add
   * @param addOnlyInSilverpeas true if user must not be added in distant datasource (used by
   * synchronization tools)
   * @return id of created user
   */
  public String addUser(UserDetail userDetail, boolean addOnlyInSilverpeas)
      throws AdminException {
    try {
      // Start transaction
      domainDriverManager.startTransaction(false);
      if (userDetail.getDomainId() != null && !addOnlyInSilverpeas) {
        domainDriverManager.startTransaction(userDetail.getDomainId(), false);
      }

      // add user
      String sUserId = userManager.addUser(domainDriverManager, userDetail,
          addOnlyInSilverpeas);

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
          "admin.EX_ERR_ADD_USER", userDetail.getFirstName() + " "
          + userDetail.getLastName(), e);
    }
  }

  /**
   * Delete the given user from silverpeas and specific domain
   */
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

  /**
   * Delete the given user from silverpeas and specific domain
   */
  public String deleteUser(String sUserId, boolean onlyInSilverpeas) throws AdminException {
    UserDetail user = null;

    try {
      // Get user information from Silverpeas database only
      user = getUserDetail(sUserId);
      if (user == null) {
        throw new AdminException("Admin.deleteUser", SilverpeasException.ERROR,
            "admin.EX_ERR_USER_NOT_FOUND", "user id : '" + sUserId + "'");
      }

      // Start transaction
      domainDriverManager.startTransaction(false);
      if (user.getDomainId() != null && !onlyInSilverpeas) {
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
        if (user.getDomainId() != null && !onlyInSilverpeas) {
          domainDriverManager.rollback(user.getDomainId());
        }
      } catch (Exception e1) {
        SilverTrace.error("admin", "Admin.deleteUser", "root.EX_ERR_ROLLBACK",
            e1);
      }
      throw new AdminException("Admin.deleteUser", SilverpeasException.ERROR,
          "admin.EX_ERR_DELETE_USER", "user id : '" + sUserId + "'", e);
    }
  }

  /**
   * Update the given user (ONLY IN SILVERPEAS)
   */
  public String updateUser(UserDetail user) throws AdminException {
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
    }
  }

  /**
   * Update the given user in Silverpeas and specific domain
   */
  public String updateUserFull(UserFull user) throws AdminException {
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
        SilverTrace.error("admin", "Admin.updateUserFull",
            "root.EX_ERR_ROLLBACK", e1);
      }
      throw new AdminException("Admin.updateUserFull",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_USER", "user id : '"
          + user.getId() + "'", e);
    }
  }

  // -------------------------------------------------------------------------
  // COMPONENT RELATED FUNCTIONS
  // -------------------------------------------------------------------------
  /**
   * Instantiate the space Components
   */
  private void instantiateComponents(String userId, String[] asComponentIds,
      String[] asComponentNames, String sSpaceId, Connection connectionProd)
      throws AdminException {
    try {
      for (int nI = 0;
          nI < asComponentIds.length;
          nI++) {
        SilverTrace.debug("admin", "Admin.instantiateComponents",
            "root.MSG_GEN_ENTER_METHOD", "spaceid: " + sSpaceId
            + " and component " + asComponentIds[nI]);

        componentInstanciator.setConnection(connectionProd);
        componentInstanciator.setSpaceId(sSpaceId);
        componentInstanciator.setComponentId(asComponentIds[nI]);
        componentInstanciator.setUserId(userId);
        componentInstanciator.instantiateComponentName(asComponentNames[nI]);
      }
    } catch (Exception e) {
      throw new AdminException("Admin.instantiateComponents",
          SilverpeasException.ERROR, "admin.EX_ERR_INSTANTIATE_COMPONENTS", e);
    }
  }

  /**
   * Uninstantiate the space Components
   */
  private void unInstantiateComponents(String userId, String[] asComponentIds,
      String[] asComponentNames, String sSpaceId, Connection connectionProd) {

    for (int nI = 0;
        nI < asComponentIds.length;
        nI++) {
      try {
        SilverTrace.debug("admin", "Admin.instantiateComponents",
            "root.MSG_GEN_ENTER_METHOD", "spaceid: " + sSpaceId
            + " and component " + asComponentIds[nI]);

        componentInstanciator.setConnection(connectionProd);
        componentInstanciator.setSpaceId(sSpaceId);
        componentInstanciator.setComponentId(asComponentIds[nI]);
        componentInstanciator.setUserId(userId);
        componentInstanciator.unInstantiateComponentName(asComponentNames[nI]);
      } catch (Exception e) {
        SilverTrace.warn("admin", "Admin.unInstantiateComponents",
            "admin.EX_ERR_UNINSTANTIATE_COMPONENTS", "Deleting data from component '"
            + asComponentNames[nI] + "' failed", e);
      }
    }

  }

  // -------------------------------------------------------------------------
  // CONVERSION CLIENT <--> DRIVER SPACE ID
  // -------------------------------------------------------------------------
  /**
   * Converts client space id to driver space id
   */
  private String getDriverSpaceId(String sClientSpaceId) {
    if ((sClientSpaceId != null)
        && (sClientSpaceId.startsWith(SPACE_KEY_PREFIX))) {
      return sClientSpaceId.substring(SPACE_KEY_PREFIX.length());
    }
    return sClientSpaceId;

  }

  /**
   * Converts driver space id to client space id
   */
  public String getClientSpaceId(String sDriverSpaceId) {
    if ((sDriverSpaceId != null)
        && (!sDriverSpaceId.startsWith(SPACE_KEY_PREFIX))) {
      return SPACE_KEY_PREFIX + sDriverSpaceId;
    }
    return sDriverSpaceId;
  }

  /**
   * Converts driver space ids to client space ids
   */
  public String[] getClientSpaceIds(String[] asDriverSpaceIds) throws Exception {
    String[] asClientSpaceIds = new String[asDriverSpaceIds.length];
    for (int nI = 0;
        nI < asDriverSpaceIds.length;
        nI++) {
      asClientSpaceIds[nI] = getClientSpaceId(asDriverSpaceIds[nI]);
    }
    return asClientSpaceIds;
  }

  private String getDriverComponentId(String sClientComponentId) {
    SilverTrace.debug("admin", "Admin.getDriverComponentId",
        "root.MSG_GEN_ENTER_METHOD", "component id: " + sClientComponentId);
    if (sClientComponentId == null) {
      return "";
    }

    return getTableClientComponentIdFromClientComponentId(sClientComponentId);
  }

  /**
   * Return 23 for parameter kmelia23
   */
  private String getTableClientComponentIdFromClientComponentId(
      String sClientComponentId) {
    String sTableClientId = "";

    // Remove the component name to get the table client id
    char[] cBuf = sClientComponentId.toCharArray();
    for (int nI = 0;
        nI < cBuf.length && sTableClientId.length() == 0;
        nI++) {
      if (cBuf[nI] == '0' || cBuf[nI] == '1' || cBuf[nI] == '2'
          || cBuf[nI] == '3' || cBuf[nI] == '4' || cBuf[nI] == '5'
          || cBuf[nI] == '6' || cBuf[nI] == '7' || cBuf[nI] == '8'
          || cBuf[nI] == '9') {
        sTableClientId = sClientComponentId.substring(nI);
      }
    }

    return sTableClientId;
  }

  /**
   * Return kmelia23 for parameter 23
   */
  private String getClientComponentId(String sDriverComponentId)
      throws Exception {
    SilverTrace.debug("admin", "Admin.getClientComponentId",
        "root.MSG_GEN_ENTER_METHOD", "component id: " + sDriverComponentId);

    return getComponentInstName(sDriverComponentId) + sDriverComponentId;
  }

  // -------------------------------------------------------------------------
  // DOMAIN QUERY
  // -------------------------------------------------------------------------
  /**
   * Create a new domain
   */
  public String addDomain(Domain theDomain) throws AdminException {
    try {
      return domainDriverManager.createDomain(theDomain);
    } catch (Exception e) {
      throw new AdminException("Admin.addDomain", SilverpeasException.ERROR,
          "admin.EX_ERR_ADD_DOMAIN", "domain name : '" + theDomain.getName()
          + "'", e);
    }
  }

  /**
   * Update a domain
   */
  public String updateDomain(Domain theDomain) throws AdminException {
    try {
      return domainDriverManager.updateDomain(theDomain);
    } catch (Exception e) {
      throw new AdminException("Admin.updateDomain", SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_DOMAIN", "domain name : '" + theDomain.getName()
          + "'", e);
    }
  }

  /**
   * Remove a domain
   */
  public String removeDomain(String domainId) throws AdminException {
    int i;
    try {
      // Remove all users
      UserDetail[] toRemoveUDs = userManager.getUsersOfDomain(domainDriverManager,
          domainId);
      if (toRemoveUDs != null) {
        for (i = 0;
            i < toRemoveUDs.length;
            i++) {
          try {
            deleteUser(toRemoveUDs[i].getId(), false);
          } catch (Exception e) {
            deleteUser(toRemoveUDs[i].getId(), true);
          }
        }
      }
      // Remove all groups
      Group[] toRemoveGroups = groupManager.getGroupsOfDomain(domainDriverManager,
          domainId);
      if (toRemoveGroups != null) {
        for (i = 0;
            i < toRemoveGroups.length;
            i++) {
          try {
            deleteGroupById(toRemoveGroups[i].getId(), false);
          } catch (Exception e) {
            deleteGroupById(toRemoveGroups[i].getId(), true);
          }
        }
      }
      // Remove the domain
      return domainDriverManager.removeDomain(domainId);
    } catch (Exception e) {
      throw new AdminException("Admin.removeDomain", SilverpeasException.ERROR,
          "admin.MSG_ERR_DELETE_DOMAIN", "domain Id : '" + domainId + "'", e);
    }
  }

  /**
   * Get all domains
   */
  public Domain[] getAllDomains() throws AdminException {
    try {
      return domainDriverManager.getAllDomains();
    } catch (Exception e) {
      throw new AdminException("Admin.getAllDomains",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_ALL_DOMAINS", e);
    }
  }

  /**
   * Get a domain with given id
   */
  public Domain getDomain(String domainId) throws AdminException {
    try {
      return domainDriverManager.getDomain(domainId);
    } catch (Exception e) {
      throw new AdminException("Admin.getDomain", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_DOMAIN", "domain Id : '" + domainId + "'", e);
    }
  }

  /**
   * Get a domain with given id
   */
  public long getDomainActions(String domainId) throws AdminException {
    try {
      if (domainId != null && domainId.equals("-1")) {
        return DomainDriver.ACTION_MASK_MIXED_GROUPS;
      }
      return domainDriverManager.getDomainActions(domainId);
    } catch (Exception e) {
      throw new AdminException("Admin.getDomainActions",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_DOMAIN", "domain Id : '"
          + domainId + "'", e);
    }
  }

  public Group[] getRootGroupsOfDomain(String domainId) throws AdminException {
    try {
      return groupManager.getRootGroupsOfDomain(domainDriverManager, domainId);
    } catch (Exception e) {
      throw new AdminException("Admin.getGroupsOfDomain",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_DOMAIN", "domain Id : '"
          + domainId + "'", e);
    }
  }

  public Group[] getSynchronizedGroups() throws AdminException {
    try {
      return groupManager.getSynchronizedGroups(domainDriverManager);
    } catch (Exception e) {
      throw new AdminException("Admin.getGroupsOfDomain",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_DOMAIN", e);
    }
  }

  public String[] getRootGroupIdsOfDomain(String domainId)
      throws AdminException {
    try {
      return groupManager.getRootGroupIdsOfDomain(domainDriverManager, domainId);
    } catch (Exception e) {
      throw new AdminException("Admin.getRootGroupIdsOfDomain",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_DOMAIN", "domain Id : '"
          + domainId + "'", e);
    }
  }

  public UserDetail[] getAllUsersOfGroup(String groupId) throws AdminException {
    try {
      List<String> groupIds = new ArrayList<String>();
      groupIds.add(groupId);
      groupIds.addAll(groupManager.getAllSubGroupIdsRecursively(groupId));

      return userManager.getAllUsersOfGroups(groupIds);
    } catch (Exception e) {
      throw new AdminException("Admin.getAllUsersOfGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_DOMAIN", "Group Id : '"
          + groupId + "'", e);
    }
  }

  public UserDetail[] getUsersOfDomain(String domainId) throws AdminException {
    try {
      if ("-1".equals(domainId) && domainId != null) {
        return ArrayUtil.EMPTY_USER_DETAIL_ARRAY;
      }
      return userManager.getUsersOfDomain(domainDriverManager, domainId);
    } catch (Exception e) {
      throw new AdminException("Admin.getUsersOfDomain",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_DOMAIN", "domain Id : '"
          + domainId + "'", e);
    }
  }

  public String[] getUserIdsOfDomain(String domainId) throws AdminException {
    try {
      if ("-1".equals(domainId) && domainId != null) {
        return ArrayUtil.EMPTY_STRING_ARRAY;
      }
      return userManager.getUserIdsOfDomain(domainDriverManager, domainId);
    } catch (Exception e) {
      throw new AdminException("Admin.getUserIdsOfDomain",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_DOMAIN", "domain Id : '"
          + domainId + "'", e);
    }
  }

  // -------------------------------------------------------------------------
  // USERS QUERY
  // -------------------------------------------------------------------------
  /**
   * Get the user id for the given login password
   */
  public String authenticate(String sKey, String sSessionId,
      boolean isAppInMaintenance) throws AdminException {
    return authenticate(sKey, sSessionId, isAppInMaintenance, true);
  }

  /**
   * Get the user id for the given login password
   */
  public String authenticate(String sKey, String sSessionId, boolean isAppInMaintenance,
      boolean removeKey) throws AdminException {
    String sUserId;

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
        if (synchroDomain.isSynchroOnLoginEnabled() && !isAppInMaintenance) { // Try
          // to
          // import
          // new
          // user
          SilverTrace.warn("admin", "Admin.authenticate",
              "admin.EX_ERR_USER_NOT_FOUND", "Login: '" + sLogin
              + "', Domain: " + sDomainId, ex);
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
              "admin.MSG_ERR_SYNCHRONIZE_USER", "UserId=" + sUserId
              + " Login: '" + sLogin + "', Domain: " + sDomainId, ex);
        }
      }

      // Check that the user is not already in the pool
      UserLog userLog = loggedUsers.get(sUserId);
      if (userLog != null) {
        // The user is already logged, remove it
        loggedUsers.remove(sUserId);
        SilverTrace.info("admin", "Admin.authenticate",
            "admin.MSG_USER_ALREADY_LOGGED", "user id: '" + sUserId
            + "', log time: " + formatter.format(userLog.getLogDate()));
      }

      // Add the user in the pool of UserLog
      userLog = new UserLog();
      userLog.setSessionId(sSessionId);
      userLog.setUserId(sUserId);
      userLog.setUserLogin(sLogin);
      userLog.setLogDate(new Date());
      loggedUsers.put(sUserId, userLog);

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
  public String[] getDirectGroupsIdsOfUser(String userId) throws AdminException {
    try {
      return groupManager.getDirectGroupsOfUser(domainDriverManager, userId);
    } catch (Exception e) {
      throw new AdminException("Admin.getDirectGroupsIdsOfUser",
          SilverpeasException.ERROR, "admin.EX_ERR_GROUP_NOT_FOUND",
          "user Id : '" + userId + "'", e);
    }
  }

  public UserDetail[] searchUsers(UserDetail modelUser, boolean isAnd)
      throws AdminException {
    try {
      return userManager.searchUsers(domainDriverManager, modelUser, isAnd);
    } catch (Exception e) {
      throw new AdminException("Admin.searchUsers", SilverpeasException.ERROR,
          "admin.EX_ERR_USER_NOT_FOUND", e);
    }
  }

  public Group[] searchGroups(Group modelGroup, boolean isAnd)
      throws AdminException {
    try {
      return groupManager.searchGroups(domainDriverManager, modelGroup, isAnd);
    } catch (Exception e) {
      throw new AdminException("Admin.searchGroups", SilverpeasException.ERROR,
          "admin.EX_ERR_GROUP_NOT_FOUND", e);
    }
  }

  /**
   * Get the spaces ids allowed for the given user Id
   */
  public String[] getUserSpaceIds(String sUserId) throws AdminException {
    List<String> spaceIds = new ArrayList<String>();

    // getting all components availables
    List<String> componentIds = getAllowedComponentIds(sUserId);
    for (String componentId :
        componentIds) {
      List<SpaceInstLight> spaces = TreeCache.getComponentPath(componentId);
      for (SpaceInstLight space :
          spaces) {
        if (!spaceIds.contains(space.getFullId())) {
          spaceIds.add(space.getFullId());
        }
      }
    }

    return spaceIds.toArray(new String[spaceIds.size()]);
  }

  private List<String> getAllGroupsOfUser(String userId) throws AdminException {
    List<String> allGroupsOfUser = GroupCache.getAllGroupIdsOfUser(userId);
    if (allGroupsOfUser == null) {
      // group ids of user is not yet processed
      // process it and store it in cache
      allGroupsOfUser = new ArrayList<String>();

      String[] directGroupIds = groupManager.getDirectGroupsOfUser(domainDriverManager, userId);
      for (String directGroupId : directGroupIds) {
        Group group = groupManager.getGroup(directGroupId);
        if (group != null) {
          allGroupsOfUser.add(group.getId());
          while (StringUtil.isDefined(group.getSuperGroupId())) {
            group = groupManager.getGroup(group.getSuperGroupId());
            if (group != null) {
              allGroupsOfUser.add(group.getId());
            }
          }
        }
      }
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

  /**
   * Get the root spaces ids allowed for the given user Id
   */
  public String[] getUserRootSpaceIds(String sUserId) throws AdminException {
    try {
      List<String> result = new ArrayList<String>();
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
          "admin.EX_ERR_GET_USER_ALLOWED_ROOTSPACE_IDS", "user Id : '"
          + sUserId + "'", e);
    }
  }

  public String[] getUserSubSpaceIds(String sUserId, String spaceId) throws AdminException {
    try {
      List<String> result = new ArrayList<String>();
      // getting all components availables
      List<String> componentIds = getAllowedComponentIds(sUserId);
      // getting all subspaces
      List<SpaceInstLight> subspaces = TreeCache.getSubSpaces(getDriverSpaceId(spaceId));
      for (SpaceInstLight subspace :
          subspaces) {
        if (isSpaceContainsOneComponent(componentIds, subspace.getShortId(), true)) {
          result.add(subspace.getShortId());
        }
      }
      return result.toArray(new String[result.size()]);

    } catch (Exception e) {
      throw new AdminException("Admin.getUserRootSpaceIds",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_ALLOWED_ROOTSPACE_IDS", "user Id : '"
          + sUserId + "'", e);
    }
  }

  /**
   * This method permit to know if given space is allowed to given user.
   *
   * @param userId
   * @param spaceId
   * @return true if user is allowed to access to one component (at least) in given space, false
   * otherwise.
   * @throws AdminException
   */
  public boolean isSpaceAvailable(String userId, String spaceId) throws AdminException {
    List<String> componentIds = getAllowedComponentIds(userId);
    return isSpaceContainsOneComponent(componentIds, getDriverSpaceId(spaceId), true);
  }

  private boolean isSpaceContainsOneComponent(List<String> componentIds, String spaceId,
      boolean checkInSubspaces) {
    boolean find = false;

    List<ComponentInstLight> components =
        new ArrayList<ComponentInstLight>(TreeCache.getComponents(spaceId));

    // Is there at least one component available ?
    for (int c = 0;
        !find && c < components.size();
        c++) {
      find = componentIds.contains(components.get(c).getId());
    }
    if (find) {
      return true;
    } else {
      if (checkInSubspaces) {
        // check in subspaces
        List<SpaceInstLight> subspaces =
            new ArrayList<SpaceInstLight>(TreeCache.getSubSpaces(spaceId));
        for (int s = 0;
            !find && s < subspaces.size();
            s++) {
          find =
              isSpaceContainsOneComponent(componentIds, subspaces.get(s).getShortId(),
              checkInSubspaces);
        }
      }
    }

    return find;
  }

  /**
   * Get subspaces of a given space available to a user.
   *
   * @param userId
   * @param spaceId
   * @return a list of SpaceInstLight
   * @throws AdminException
   * @author neysseri
   */
  public List<SpaceInstLight> getSubSpacesOfUser(String userId, String spaceId)
      throws AdminException {
    SilverTrace.info("admin", "Admin.getSubSpacesOfUser",
        "root.MSG_GEN_ENTER_METHOD", "userId = " + userId + ", spaceId = "
        + spaceId);
    try {
      List<SpaceInstLight> result = new ArrayList<SpaceInstLight>();

      // getting all components availables
      List<String> componentIds = getAllowedComponentIds(userId);

      // getting all subspaces
      List<SpaceInstLight> subspaces = TreeCache.getSubSpaces(getDriverSpaceId(spaceId));
      for (SpaceInstLight subspace :
          subspaces) {
        if (isSpaceContainsOneComponent(componentIds, subspace.getShortId(), true)) {
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

  public List<SpaceInstLight> getSubSpaces(String spaceId)
      throws AdminException {
    SilverTrace.info("admin", "Admin.getSubSpaces", "root.MSG_GEN_ENTER_METHOD", "spaceId = "
        + spaceId);
    try {
      return spaceManager.getSubSpaces(getDriverSpaceId(spaceId));
    } catch (Exception e) {
      throw new AdminException("Admin.getSubSpaces",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_SUBSPACES",
          "spaceId = " + spaceId, e);
    }
  }

  /**
   * Get components of a given space (and subspaces) available to a user.
   *
   * @param userId
   * @param spaceId
   * @return a list of ComponentInstLight
   * @throws AdminException
   * @author neysseri
   */
  public List<ComponentInstLight> getAvailCompoInSpace(String userId, String spaceId)
      throws AdminException {
    SilverTrace.info("admin", "Admin.getAvailCompoInSpace",
        "root.MSG_GEN_ENTER_METHOD", "userId = " + userId + ", spaceId = "
        + spaceId);
    try {
      List<String> allowedComponentIds = getAllowedComponentIds(userId);

      List<ComponentInstLight> allowedComponents = new ArrayList<ComponentInstLight>();

      List<ComponentInstLight> allComponents =
          TreeCache.getComponentsInSpaceAndSubspaces(getDriverSpaceId(spaceId));
      for (ComponentInstLight component :
          allComponents) {
        if (allowedComponentIds.contains(component.getId())) {
          allowedComponents.add(component);
        }
      }
      return allowedComponents;
    } catch (Exception e) {
      throw new AdminException("Admin.getAvailCompoInSpace",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_ALLOWED_COMPONENTS", "userId = " + userId
          + ", spaceId = " + spaceId, e);
    }
  }

  public Map<String, SpaceAndChildren> getTreeView(String userId, String spaceId)
      throws AdminException {
    SilverTrace.info("admin", "Admin.getTreeView", "root.MSG_GEN_ENTER_METHOD",
        "userId = " + userId + ", spaceId = " + spaceId);
    spaceId = getDriverSpaceId(spaceId);

    // Step 1 - get all availables spaces and components
    Collection<SpaceInstLight> spacesLight = getSubSpacesOfUser(userId, spaceId);
    Collection<ComponentInstLight> componentsLight = getAvailCompoInSpace(userId, spaceId);

    SilverTrace.info("admin", "Admin.getTreeView", "root.MSG_GEN_PARAM_VALUE",
        "SQL Queries done !");

    // Step 2 - build HashTable
    Map<String, SpaceAndChildren> spaceTrees = new HashMap<String, SpaceAndChildren>();
    Iterator<SpaceInstLight> it = spacesLight.iterator();
    while (it.hasNext()) {
      SpaceInstLight space = it.next();
      spaceTrees.put(space.getFullId(), new SpaceAndChildren(space));
    }

    // Step 3 - add root space to hashtable
    SpaceInstLight rootSpace = getSpaceInstLight(spaceId);
    spaceTrees.put(rootSpace.getFullId(), new SpaceAndChildren(rootSpace));

    // Step 4 - build dependances
    it = spacesLight.iterator();
    while (it.hasNext()) {
      SpaceInstLight child = it.next();
      String fatherId = SPACE_KEY_PREFIX + child.getFatherId();
      SpaceAndChildren father = spaceTrees.get(fatherId);
      if (father != null) {
        father.addSubSpace(child);
      }
    }

    for (ComponentInstLight child : componentsLight) {
      String fatherId = SPACE_KEY_PREFIX + child.getDomainFatherId();
      SpaceAndChildren father = spaceTrees.get(fatherId);
      if (father != null) {
        father.addComponent(child);
      }
    }
    SilverTrace.info("admin", "Admin.getTreeView", "root.MSG_GEN_EXIT_METHOD",
        "userId = " + userId + ", spaceId = " + spaceId);
    return spaceTrees;
  }

  /**
   * Get all spaces available to a user. N levels compliant. Infos of each space are in
   * SpaceInstLight object.
   *
   * @param userId
   * @return an ordered list of SpaceInstLight. Built according a depth-first algorithm.
   * @throws Exception
   * @author neysseri
   */
  public List<SpaceInstLight> getUserSpaceTreeview(String userId) throws Exception {
    SilverTrace.info("admin", "Admin.getUserSpaceTreeview",
        "root.MSG_GEN_ENTER_METHOD", "user id = " + userId);
    Set<String> componentsId = Sets.newHashSet(getAvailCompoIds(userId));
    Set<String> authorizedIds = new HashSet<String>(100);
    if (!componentsId.isEmpty()) {
      String componentId = componentsId.iterator().next();
      componentsId.remove(componentId);
      filterSpaceFromComponents(authorizedIds, componentsId, componentId);
    }
    String[] rootSpaceIds = getAllRootSpaceIds(userId);
    List<SpaceInstLight> treeview = new ArrayList<SpaceInstLight>(authorizedIds.size());
    for (String spaceId : rootSpaceIds) {
      String currentSpaceId = getDriverSpaceId(spaceId);
      if (authorizedIds.contains(currentSpaceId)) {
        treeview.add(TreeCache.getSpaceInstLight(currentSpaceId));
        addAuthorizedSpaceToTree(treeview, authorizedIds, currentSpaceId, 1);
      }
    }
    return treeview;
  }

  void addAuthorizedSpaceToTree(List<SpaceInstLight> treeview, Set<String> authorizedIds,
      String spaceId, int level) {
    SilverTrace.debug("admin", "Admin.addAuthorizedSpaceToTree", "root.MSG_GEN_ENTER_METHOD",
        "size of treeview = " + treeview.size());
    List<SpaceInstLight> subSpaces = TreeCache.getSubSpaces(spaceId);
    for (SpaceInstLight space : subSpaces) {
      String subSpaceId = getDriverSpaceId(space.getFullId());
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
  void addAuthorizedSpace(Set<String> spaces, Set<String> componentsId, SpaceInstLight space) {
    SilverTrace.debug("admin", "Admin.addAuthorizedSpace", "root.MSG_GEN_ENTER_METHOD",
        "#componentIds = " + componentsId.size());
    if (space != null && !SpaceInst.STATUS_REMOVED.equals(space.getStatus())
        && !spaces.contains(space.getShortId())) {
      SilverTrace.debug("admin", "Admin.addAuthorizedSpace", "root.MSG_GEN_PARAM_VALUE",
          "space = " + space.getFullId());
      String spaceId = getDriverSpaceId(space.getFullId());
      spaces.add(spaceId);
      componentsId.removeAll(TreeCache.getComponentIds(spaceId));
      if (!space.isRoot()) {
        String fatherId = getDriverSpaceId(space.getFatherId());
        if (!spaces.contains(fatherId)) {
          SpaceInstLight parent = TreeCache.getSpaceInstLight(fatherId);
          addAuthorizedSpace(spaces, componentsId, parent);
        }
      }
    }
  }

  void filterSpaceFromComponents(Set<String> spaces, Set<String> componentsId, String componentId) {
    SilverTrace.debug("admin", "Admin.filterSpaceFromComponents", "root.MSG_GEN_ENTER_METHOD",
        "#componentIds = " + componentsId.size() + ", componentId = " + componentId);
    SpaceInstLight space = TreeCache.getSpaceContainingComponent(componentId);
    addAuthorizedSpace(spaces, componentsId, space);
    if (!componentsId.isEmpty()) {
      String newComponentId = componentsId.iterator().next();
      componentsId.remove(newComponentId);
      filterSpaceFromComponents(spaces, componentsId, newComponentId);
    }
  }

  public String[] getAllowedSubSpaceIds(String userId, String spaceFatherId) throws AdminException {
    return getUserSubSpaceIds(userId, spaceFatherId);
  }

  private SpaceInstLight getSpaceInstLight(String spaceId)
      throws AdminException {
    SilverTrace.info("admin", "Admin.getSpaceInstLight",
        "root.MSG_GEN_ENTER_METHOD", "spaceId = " + spaceId);
    return getSpaceInstLight(spaceId, -1);
  }

  private SpaceInstLight getSpaceInstLight(String spaceId, int level) throws AdminException {
    SilverTrace.info("admin", "Admin.getSpaceInstLight", "root.MSG_GEN_ENTER_METHOD",
        "spaceId = " + spaceId + ", level = " + level);
    SpaceInstLight sil = TreeCache.getSpaceInstLight(spaceId);
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

  /**
   * Get the space instance light (only spaceid, fatherId and name) with the given space id
   *
   * @param sClientSpaceId client space id (as WAxx)
   * @return Space information as SpaceInstLight object
   */
  public SpaceInstLight getSpaceInstLightById(String sClientSpaceId) throws AdminException {
    try {
      return getSpaceInstLight(getDriverSpaceId(sClientSpaceId));
    } catch (Exception e) {
      throw new AdminException("Admin.getSpaceInstLightById",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_SPACE", " space Id : '"
          + sClientSpaceId + "'", e);
    }
  }

  /**
   * Return the higher space according to a subspace (N level compliant)
   *
   * @param spaceId the subspace id
   * @return a SpaceInstLight object
   * @throws AdminException
   */
  public SpaceInstLight getRootSpace(String spaceId) throws AdminException {
    SpaceInstLight sil = getSpaceInstLight(getDriverSpaceId(spaceId));
    while (sil != null && !sil.isRoot()) {
      sil = getSpaceInstLight(sil.getFatherId());
    }
    return sil;
  }

  /**
   * Get the spaces ids manageable by given group Id
   */
  public String[] getGroupManageableSpaceIds(String sGroupId)
      throws AdminException {
    String[] asManageableSpaceIds;
    ArrayList<String> alManageableSpaceIds = new ArrayList<String>();

    try {
      // Get user manageable space ids from database
      List<String> groupIds = new ArrayList<String>();
      groupIds.add(sGroupId);
      List<String> manageableSpaceIds = spaceManager.getManageableSpaceIds(null, groupIds);
      asManageableSpaceIds = manageableSpaceIds.toArray(new String[manageableSpaceIds.size()]);

      // Inherits manageability rights for space children
      String[] childSpaceIds;
      for (String asManageableSpaceId : asManageableSpaceIds) {
        // add manageable space id in result
        if (!alManageableSpaceIds.contains(asManageableSpaceId)) {
          alManageableSpaceIds.add(asManageableSpaceId);
        }

        // calculate manageable space's childs
        childSpaceIds = spaceManager.getAllSubSpaceIds(domainDriverManager,
            asManageableSpaceId);

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
          "admin.EX_ERR_GET_USER_MANAGEABLE_SPACE_IDS", "group Id : '"
          + sGroupId + "'", e);
    }
  }

  /**
   * Get the spaces ids manageable by given user Id
   */
  public String[] getUserManageableSpaceIds(String sUserId) throws AdminException {
    String[] asManageableSpaceIds;
    ArrayList<String> alManageableSpaceIds = new ArrayList<String>();

    try {
      // Get user manageable space ids from cache
      asManageableSpaceIds = cache.getManageableSpaceIds(sUserId);
      if (asManageableSpaceIds == null) {
        // Get user manageable space ids from database

        List<String> groupIds = getAllGroupsOfUser(sUserId);
        asManageableSpaceIds = userManager.getManageableSpaceIds(sUserId, groupIds);

        // Inherits manageability rights for space children
        String[] childSpaceIds;
        for (String asManageableSpaceId : asManageableSpaceIds) {
          // add manageable space id in result
          if (!alManageableSpaceIds.contains(asManageableSpaceId)) {
            alManageableSpaceIds.add(asManageableSpaceId);
          }

          // calculate manageable space's childs
          childSpaceIds = spaceManager.getAllSubSpaceIds(domainDriverManager,
              asManageableSpaceId);

          // add them in result
          for (String childSpaceId : childSpaceIds) {
            if (!alManageableSpaceIds.contains(childSpaceId)) {
              alManageableSpaceIds.add(childSpaceId);
            }
          }
        }

        // Put user manageable space ids in cache
        asManageableSpaceIds = alManageableSpaceIds.toArray(
            new String[alManageableSpaceIds.size()]);
        cache.putManageableSpaceIds(sUserId, asManageableSpaceIds);
      }
      return asManageableSpaceIds;
    } catch (Exception e) {
      throw new AdminException("Admin.getUserManageableSpaceIds",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_MANAGEABLE_SPACE_IDS", "user Id : '" + sUserId
          + "'", e);
    }
  }

  /**
   * Get the spaces roots ids manageable by given user Id
   */
  public String[] getUserManageableSpaceRootIds(String sUserId)
      throws AdminException {
    try {
      // Get user manageable space ids from database
      List<String> groupIds = getAllGroupsOfUser(sUserId);
      String[] asManageableSpaceIds = userManager.getManageableSpaceIds(sUserId, groupIds);

      // retain only root spaces
      List<String> manageableRootSpaceIds = new ArrayList<String>();
      for (String asManageableSpaceId : asManageableSpaceIds) {
        SpaceInstLight space = TreeCache.getSpaceInstLight(asManageableSpaceId);
        if (space.isRoot()) {
          manageableRootSpaceIds.add(asManageableSpaceId);
        }
      }
      return manageableRootSpaceIds.toArray(new String[manageableRootSpaceIds.size()]);

    } catch (Exception e) {
      throw new AdminException("Admin.getUserManageableSpaceRootIds",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_MANAGEABLE_SPACE_IDS", "user Id : '" + sUserId
          + "'", e);
    }
  }

  /**
   * Get the sub space ids manageable by given user Id in given space
   */
  public String[] getUserManageableSubSpaceIds(String sUserId,
      String sParentSpaceId) throws AdminException {
    try {
      // Get user manageable space ids from database
      List<String> groupIds = getAllGroupsOfUser(sUserId);
      String[] asManageableSpaceIds = userManager.getManageableSpaceIds(sUserId, groupIds);

      String parentSpaceId = getDriverSpaceId(sParentSpaceId);

      // retain only sub spaces
      boolean find;
      List<String> manageableRootSpaceIds = new ArrayList<String>();
      for (String manageableSpaceId :
          asManageableSpaceIds) {
        find = false;
        SpaceInstLight space = TreeCache.getSpaceInstLight(manageableSpaceId);
        while (!space.isRoot() && !find) {
          if (parentSpaceId.equals(space.getFatherId())) {
            manageableRootSpaceIds.add(manageableSpaceId);
            find = true;
          } else {
            space = TreeCache.getSpaceInstLight(space.getFatherId());
          }
        }
      }
      return manageableRootSpaceIds.toArray(new String[manageableRootSpaceIds.size()]);
    } catch (Exception e) {
      throw new AdminException("Admin.getManageableSubSpaceIds",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_MANAGEABLE_SPACE_IDS", "user Id : '" + sUserId
          + "' Space = " + sParentSpaceId, e);
    }
  }

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

  /**
   * Get the component ids allowed for the given user Id in the given space
   */
  public String[] getAvailCompoIds(String sClientSpaceId, String sUserId)
      throws AdminException {
    String[] asAvailCompoIds;

    try {
      // Converts client space id to driver space id
      String spaceId = getDriverSpaceId(sClientSpaceId);

      // Get available component ids from cache
      asAvailCompoIds = cache.getAvailCompoIds(spaceId, sUserId);

      if (asAvailCompoIds == null) {
        // Get available component ids from database
        List<ComponentInstLight> components = getAvailCompoInSpace(sUserId, sClientSpaceId);

        List<String> componentIds = new ArrayList<String>();
        for (ComponentInstLight component :
            components) {
          componentIds.add(component.getId());
        }

        asAvailCompoIds = componentIds.toArray(new String[componentIds.size()]);

        // Store available component ids in cache
        cache.putAvailCompoIds(spaceId, sUserId, asAvailCompoIds);
      }
      return asAvailCompoIds;

      // return getClientComponentIds(asAvailCompoIds);
    } catch (Exception e) {
      throw new AdminException("Admin.getAvailCompoIds",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", "user Id : '"
          + sUserId + "'", e);
    }
  }

  public boolean isComponentAvailable(String componentId, String userId)
      throws AdminException {
    try {
      return getAllowedComponentIds(userId).contains(componentId);
    } catch (Exception e) {
      throw new AdminException("Admin.isComponentAvailable",
          SilverpeasException.ERROR, "admin.EX_ERR_IS_COMPONENT_AVAILABLE",
          "user Id : '" + userId + "'" + " , component Id : '" + componentId
          + "'", e);
    }
  }
  
  public boolean isComponentManageable(String componentId, String userId) throws AdminException {
    boolean manageable = getUserDetail(userId).isAccessAdmin();
    if (!manageable) {
      // check if user is manager of at least one space parent
      String[] spaceIds = getUserManageableSpaceIds(userId);
      ComponentInstLight component = getComponentInstLight(componentId);
      if (component != null) {
        List<String> toCheck = Arrays.asList(spaceIds);
        manageable = toCheck.contains(getDriverSpaceId(component.getDomainFatherId()));
      }
    }
    return manageable;
  }

  /**
   * Get ids of components allowed to user in given space (not in subspaces)
   *
   * @return an array of componentId (kmelia12, hyperlink145...)
   * @throws AdminException
   */
  public String[] getAvailCompoIdsAtRoot(String sClientSpaceId, String sUserId)
      throws AdminException {
    try {
      // Converts client space id to driver space id
      String spaceId = getDriverSpaceId(sClientSpaceId);

      // Get available component ids from database
      // String[] asAvailCompoIds =
      // m_ComponentInstManager.getAvailCompoIdsInSpaceAtRoot(m_DDManager, spaceId, userId);
      List<String> groupIds = getAllGroupsOfUser(sUserId);
      List<String> asAvailCompoIds =
          componentManager.getAllowedComponentIds(Integer.parseInt(sUserId), groupIds,
          spaceId);

      return asAvailCompoIds.toArray(new String[asAvailCompoIds.size()]);

      // return getClientComponentIds(asAvailCompoIds);
    } catch (Exception e) {
      throw new AdminException("Admin.getAvailCompoIds",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", "user Id : '"
          + sUserId + "'", e);
    }
  }

  /**
   * Get the componentIds allowed for the given user Id in the given space and the
   * componentNameRoot
   *
   * @param sClientSpaceId
   * @param sUserId
   * @param componentRootName
   * @return ArrayList of componentIds
   * @author dlesimple
   */
  public List<String> getAvailCompoIdsAtRoot(String sClientSpaceId,
      String sUserId, String componentNameRoot) throws AdminException {

    try {
      // Converts client space id to driver space id
      String spaceId = getDriverSpaceId(sClientSpaceId);

      // Get available component ids from database
      List<ComponentInstLight> components = TreeCache.getComponents(spaceId);

      List<String> allowedComponentIds = getAllowedComponentIds(sUserId);
      List<String> result = new ArrayList<String>();
      for (ComponentInstLight component : components) {
        if (allowedComponentIds.contains(component.getId())
            && component.getName().startsWith(componentNameRoot)) {
          result.add(component.getId());
        }
      }

      return result;
    } catch (Exception e) {
      throw new AdminException("Admin.getAvailCompoIdsAtRoot",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", "user Id : '"
          + sUserId + "'", e);
    }
  }

  /**
   * Get the component ids allowed for the given user Id.
   * @param userId
   */
  public String[] getAvailCompoIds(String userId) throws AdminException {
    try {
      List<String> componentIds = getAllowedComponentIds(userId);

      return componentIds.toArray(new String[componentIds.size()]);
    } catch (Exception e) {
      throw new AdminException("Admin.getAvailCompoIds",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", "user Id : '"
          + userId + "'", e);
    }
  }

  /**
   * Get the driver component ids allowed for the given user Id in the given space
   */
  public String[] getAvailDriverCompoIds(String sClientSpaceId, String sUserId)
      throws AdminException {
    try {
      // Get available component ids
      List<ComponentInstLight> components = getAvailCompoInSpace(sUserId, sClientSpaceId);

      List<String> componentIds = new ArrayList<String>();
      for (ComponentInstLight component :
          components) {
        componentIds.add(component.getId());
      }

      return componentIds.toArray(new String[componentIds.size()]);
    } catch (Exception e) {
      throw new AdminException("Admin.getAvailDriverCompoIds",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", "user Id : '"
          + sUserId + "'", e);
    }
  }

  public String[] getComponentIdsByNameAndUserId(String sUserId,
      String sComponentName) throws AdminException {

    List<String> allowedComponentIds = getAllowedComponentIds(sUserId, sComponentName);
    return allowedComponentIds.toArray(new String[allowedComponentIds.size()]);
  }

  /**
   * gets the available component for a given user
   *
   * @param userId user identifier used to get component
   * @param componentName type of component to retrieve ( for example : kmelia, forums, blog)
   * @return a list of ComponentInstLight object
   * @throws AdminException
   */
  public List<ComponentInstLight> getAvailComponentInstLights(
      String userId, String componentName) throws AdminException {

    List<ComponentInstLight> components = new ArrayList<ComponentInstLight>();
    List<String> allowedComponentIds = getAllowedComponentIds(userId, componentName);

    for (String allowedComponentId :
        allowedComponentIds) {
      ComponentInstLight componentInst = getComponentInstLight(allowedComponentId);

      if (componentInst.getName().equalsIgnoreCase(componentName)) {
        components.add(componentInst);
      }
    }
    return components;
  }

  /**
   * This method returns all root spaces which contains at least one allowed component of type
   * componentName in this space or subspaces.
   *
   * @param userId
   * @param componentName the component type (kmelia, gallery...)
   * @return a list of root spaces
   * @throws AdminException
   */
  public List<SpaceInstLight> getRootSpacesContainingComponent(String userId, String componentName)
      throws AdminException {
    List<SpaceInstLight> spaces = new ArrayList<SpaceInstLight>();
    List<ComponentInstLight> components = getAvailComponentInstLights(userId, componentName);
    for (ComponentInstLight component :
        components) {
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

  /**
   * This method returns all sub spaces which contains at least one allowed component of type
   * componentName in this space or subspaces.
   *
   * @param userId
   * @param componentName the component type (kmelia, gallery...)
   * @return a list of root spaces
   * @throws AdminException
   */
  public List<SpaceInstLight> getSubSpacesContainingComponent(String spaceId, String userId,
      String componentName)
      throws AdminException {
    List<SpaceInstLight> spaces = new ArrayList<SpaceInstLight>();
    spaceId = getDriverSpaceId(spaceId);
    List<ComponentInstLight> components = getAvailComponentInstLights(userId, componentName);

    for (ComponentInstLight component :
        components) {
      List<SpaceInstLight> path = TreeCache.getComponentPath(component.getId());
      for (SpaceInstLight space :
          path) {
        if (space.getFatherId().equals(spaceId)) {
          if (!spaces.contains(space)) {
            spaces.add(space);
          }
        }
      }
    }
    return spaces;
  }

  /**
   * Get the tuples (space id, compo id) allowed for the given user and given component name
   */
  public CompoSpace[] getCompoForUser(String sUserId, String sComponentName)
      throws AdminException {
    ArrayList<CompoSpace> alCompoSpace = new ArrayList<CompoSpace>();

    try {
      List<ComponentInstLight> components = getAvailComponentInstLights(sUserId, sComponentName);

      for (ComponentInstLight componentInst :
          components) {

        // Create new instance of CompoSpace
        CompoSpace compoSpace = new CompoSpace();

        // Set the component Id
        compoSpace.setComponentId(componentInst.getId());

        // Set the component label
        if (componentInst.getLabel().length() > 0) {
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
          "user Id : '" + sUserId + "', component name: '" + sComponentName
          + "'", e);
    }
  }

  /**
   * Return the compo id for the given component name
   */
  public String[] getCompoId(String sComponentName) throws AdminException {
    try {
      // Build the list of instanciated components with given componentName
      String[] asMatchingComponentIds =
          componentManager.getAllCompoIdsByComponentName(domainDriverManager, sComponentName);
      return asMatchingComponentIds;
    } catch (Exception e) {
      throw new AdminException("Admin.getCompoId", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_AVAILABLE_INSTANCES_OF_COMPONENT",
          "component name: '" + sComponentName + "'", e);
    }
  }

  /**
   * Get all the profiles Id for the given user
   */
  public String[] getProfileIds(String sUserId) throws AdminException {
    try {
      // Get the component instance from cache
      String[] asProfilesIds = cache.getProfileIds(sUserId);

      if (asProfilesIds == null) {
        // retrieve value from database
        asProfilesIds =
            profileManager.getProfileIdsOfUser(sUserId, getAllGroupsOfUser(sUserId));

        // store values in cache
        if (asProfilesIds != null) {
          cache.putProfileIds(sUserId, asProfilesIds);
        }
      }

      return asProfilesIds;
    } catch (Exception e) {
      throw new AdminException("Admin.getProfiles", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_PROFILES", "user Id : '" + sUserId + "'", e);
    }
  }

  /**
   * Get all the profiles Id for the given group
   */
  public String[] getProfileIdsOfGroup(String sGroupId) throws AdminException {
    try {
      // retrieve value from database
      return profileManager.getProfileIdsOfGroup(domainDriverManager, sGroupId);
    } catch (Exception e) {
      throw new AdminException("Admin.getProfileIdsOfGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_GROUP_PROFILES",
          "group Id : '" + sGroupId + "'", e);
    }
  }

  /**
   * Get the profile names of the given user for the given component
   */
  public String[] getCurrentProfiles(String sUserId, ComponentInst componentInst)
      throws AdminException {
    ArrayList<String> alProfiles = new ArrayList<String>();

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
      SilverTrace.error("admin", "Admin.getCurrentProfiles",
          "admin.MSG_ERR_GET_CURRENT_PROFILE", e);
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  /**
   * Get the profile names of the given user for the given component
   */
  public String[] getCurrentProfiles(String sUserId, String componentId)
      throws AdminException {
    return profileManager.getProfileNamesOfUser(sUserId, getAllGroupsOfUser(sUserId), Integer.
        parseInt(getDriverComponentId(componentId)));
  }

  /**
   * if bAllProfiles = true, return all the user details for the given space and given component if
   * bAllProfiles = false, return the user details only for the given profile for the given space
   * and given component
   */
  public UserDetail[] getUsers(boolean bAllProfiles, String sProfile,
      String sClientSpaceId, String sClientComponentId) throws AdminException {
    ArrayList<String> alUserIds = new ArrayList<String>();

    try {
      ComponentInst componentInst = getComponentInst(
          getDriverComponentId(sClientComponentId), true,
          getDriverSpaceId(sClientSpaceId));

      for (int nI = 0;
          nI < componentInst.getNumProfileInst();
          nI++) {
        ProfileInst profile = componentInst.getProfileInst(nI);
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
      for (int nI = 0;
          nI < userDetails.length;
          nI++) {
        userDetails[nI] = getUserDetail(alUserIds.get(nI));
      }

      return userDetails;
    } catch (Exception e) {
      throw new AdminException("Admin.getUsers", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USERS_FOR_PROFILE_AND_COMPONENT", "profile : '"
          + sProfile + "', space Id: '" + sClientSpaceId
          + "' component Id: '" + sClientComponentId, e);
    }
  }

  /**
   * For use in userPanel : return the direct sub-groups
   */
  public Group[] getAllSubGroups(String parentGroupId) throws AdminException {
    String[] theIds = groupManager.getAllSubGroupIds(domainDriverManager,
        parentGroupId);
    return getGroups(theIds);
  }

  /**
   * For use in userPanel : return the users that are direct child of a given group
   */
  public UserDetail[] getFiltredDirectUsers(String sGroupId,
      String sUserLastNameFilter) throws AdminException {
    Group theGroup = getGroup(sGroupId);
    UserDetail currentUser;
    ArrayList<UserDetail> matchedUsers;
    int i;
    String upperFilter;
    String[] usersIds;

    if (theGroup == null) {
      return ArrayUtil.EMPTY_USER_DETAIL_ARRAY;
    }
    usersIds = theGroup.getUserIds();
    if (usersIds == null || usersIds.length <= 0) {
      return ArrayUtil.EMPTY_USER_DETAIL_ARRAY;
    }
    if ((sUserLastNameFilter == null) || (sUserLastNameFilter.length() <= 0)) {
      return getUserDetails(usersIds);
    }
    upperFilter = sUserLastNameFilter.toUpperCase();
    matchedUsers = new ArrayList<UserDetail>();
    for (i = 0;
        i < usersIds.length;
        i++) {
      currentUser = getUserDetail(usersIds[i]);
      if ((currentUser != null)
          && (currentUser.getLastName().toUpperCase().startsWith(upperFilter))) {
        matchedUsers.add(currentUser);
      }
    }
    return matchedUsers.toArray(new UserDetail[matchedUsers.size()]);
  }

  /**
   * For use in userPanel : return the total number of users recursivly contained in a group
   */
  public int getAllSubUsersNumber(String sGroupId) throws AdminException {
    if (!StringUtil.isDefined(sGroupId)) {
      return userManager.getUserNumber(domainDriverManager);
    } else {

      // add users directly in this group
      int nb = groupManager.getNBUsersDirectlyInGroup(sGroupId);

      // add users in sub groups
      List<String> groupIds = groupManager.getAllSubGroupIdsRecursively(sGroupId);
      for (String groupId : groupIds) {
        nb += groupManager.getNBUsersDirectlyInGroup(groupId);
      }
      return nb;
    }
  }

  /**
   * this method gets number user in domain. If domain id is null, it returns number user of all
   * domain
   */
  public int getUsersNumberOfDomain(String domainId) throws AdminException {
    try {
      if (domainId == null || domainId.length() == 0) {
        return userManager.getUserNumber(domainDriverManager);
      }
      if (domainId.equals("-1")) {
        return 0;
      } else {
        return userManager.getUsersNumberOfDomain(domainDriverManager, domainId);
      }
    } catch (Exception e) {
      throw new AdminException("Admin.getUsersOfDomain",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_DOMAIN", "domain Id : '"
          + domainId + "'", e);
    }
  }

  // -------------------------------------------------------------------------
  // MISCELLANEOUS
  // -------------------------------------------------------------------------
  /**
   * Get the Ids of the administrators
   */
  public String[] getAdministratorUserIds(String fromUserId)
      throws AdminException {
    return userManager.getAllAdminIds(domainDriverManager, getUserDetail(fromUserId));
  }

  /**
   * Get administrator Email
   *
   * @return String
   */
  public String getAdministratorEmail() {
    return administratorMail;
  }

  /**
   * Get the administrator email
   */
  public String getDAPIGeneralAdminId() {
    return "0";
  }

  /**
   * Get the list of connected users
   */
  public UserLog[] getUserConnected() {
    UserLog[] userLogs = new UserLog[loggedUsers.size()];
    int nI = 0;
    for (String user : loggedUsers.keySet()) {
      userLogs[nI++] = loggedUsers.get(user);
    }
    return userLogs;
  }

  // -------------------------------------------------------------------------
  // CONNECTION TOOLS
  // -------------------------------------------------------------------------
  /**
   * Open a connection
   */
  private Connection openConnection(String sDbUrl, String sUser, String sPswd,
      boolean bAutoCommit) throws AdminException {
    try {
      // Load the driver (registers itself)
      Class.forName(adminDBDriver);

      // Get the connection to the DB
      Connection connection = DriverManager.getConnection(sDbUrl, sUser, sPswd);
      connection.setAutoCommit(bAutoCommit);
      return connection;
    } catch (Exception e) {
      throw new AdminException("Admin.openConnection",
          SilverpeasException.FATAL, "root.EX_CONNECTION_OPEN_FAILED",
          "Db url: '" + sDbUrl + "', user: '" + sUser + "'", e);
    }
  }

  /**
   * Close connection
   */
  private void closeConnection(Connection connection) {
    try {
      if (connection != null && !connection.isClosed()) {
        connection.close();
      }
    } catch (SQLException e) {
      SilverTrace.error("admin", "Admin.closeConnection", "root.EX_CONNECTION_CLOSE_FAILED", e);
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
    for (int nI = 0;
        nI < al.size();
        nI++) {
      as[nI] = al.get(nI);
    }

    return as;
  }

  private ArrayList<String> removeTuples(ArrayList<String> al) {
    if (al == null) {
      return new ArrayList<String>();
    }

    for (int nI = 0;
        nI < al.size();
        nI++) {
      while (al.lastIndexOf(al.get(nI)) != al.indexOf(al.get(nI))) {
        al.remove(al.lastIndexOf(al.get(nI)));
      }
    }

    return al;
  }

  // -------------------------------------------------------------------
  // RE-INDEXATION
  // -------------------------------------------------------------------
  public String[] getAllSpaceIds(String sUserId) throws Exception {
    return getClientSpaceIds(getUserSpaceIds(sUserId));
  }

  /**
   * Return all the root spaces Id available in webactiv
   */
  public String[] getAllRootSpaceIds(String sUserId) throws Exception {
    return getClientSpaceIds(getUserRootSpaceIds(sUserId));
  }

  /**
   * Return all the subSpaces Id available in webactiv given a space id (driver format)
   */
  public String[] getAllSubSpaceIds(String sSpaceId, String sUserId)
      throws Exception {
    return getClientSpaceIds(getUserSubSpaceIds(sUserId, sSpaceId));
  }

  /**
   * Return all the components Id in the subspaces available in webactiv given a space id
   */
  public String[] getAllComponentIds(String sSpaceId) throws Exception {
    ArrayList<String> alCompoIds = new ArrayList<String>();

    // Get the compo of this space
    SpaceInst spaceInst = getSpaceInstById(sSpaceId);
    ArrayList<ComponentInst> alCompoInst = spaceInst.getAllComponentsInst();

    if (alCompoInst != null) {
      for (ComponentInst anAlCompoInst : alCompoInst) {
        alCompoIds.add(anAlCompoInst.getId());
      }
    }

    return arrayListToString(alCompoIds);
  }

  /**
   * Return all the componentIds recursively in the subspaces available in webactiv given a space
   * id
   */
  public String[] getAllComponentIdsRecur(String sSpaceId) throws Exception {
    List<ComponentInstLight> components =
        TreeCache.getComponentsInSpaceAndSubspaces(getDriverSpaceId(sSpaceId));

    List<String> componentIds = new ArrayList<String>();
    for (ComponentInstLight component :
        components) {
      componentIds.add(component.getId());
    }
    return componentIds.toArray(new String[componentIds.size()]);
  }

  /**
   * Return all the components Id recursively in (Space+subspaces, or only subspaces or in
   * Silverpeas) available in silverpeas given a userId and a componentNameRoot
   *
   * @param sSpaceId
   * @param sUserId
   * @param componentNameRoot
   * @param inCurrentSpace
   * @param inAllSpaces
   * @return Array of componentsIds
   * @author dlesimple
   */
  public String[] getAllComponentIdsRecur(String sSpaceId, String sUserId,
      String componentNameRoot, boolean inCurrentSpace, boolean inAllSpaces)
      throws Exception {
    SilverTrace.info("admin", "Admin.getAllComponentIdsRecur",
        "root.MSG_GEN_PARAM_VALUE", "inCurrentSpace=" + inCurrentSpace
        + " inAllSpaces=" + inAllSpaces);
    ArrayList<String> alCompoIds = new ArrayList<String>();
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
    ArrayList<String> alCompoIds = new ArrayList<String>();
    SpaceInst spaceInst = getSpaceInstById(sSpaceId);

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
    for (int nI = 0;
        asSubSpaceIds != null && nI < asSubSpaceIds.length;
        nI++) {
      SilverTrace.info("admin", "Admin.getAllComponentIdsRecur",
          "root.MSG_GEN_PARAM.VALUE", "Sub spaceId=" + asSubSpaceIds[nI]);
      spaceInst = getSpaceInstById(asSubSpaceIds[nI]);
      String[] componentIds = getAvailCompoIds(spaceInst.getId(), sUserId);

      if (componentIds != null) {
        for (String componentId : componentIds) {
          ComponentInstLight compo = getComponentInstLight(componentId);
          if (compo.getName().equals(componentNameRoot)) {
            SilverTrace.info("admin", "Admin.getAllComponentIdsRecur",
                "root.MSG_GEN_PARAM.VALUE", "componentId in subspace="
                + compo.getId());
            alCompoIds.add(compo.getId());
          }
        }
      }
    }
    return alCompoIds;
  }

  public void synchronizeGroupByRule(String groupId, boolean scheduledMode)
      throws AdminException {
    SilverTrace.info("admin", "Admin.synchronizeGroup",
        "root.MSG_GEN_ENTER_METHOD", "groupId = " + groupId);

    Group group = getGroup(groupId);
    String rule = group.getRule();
    String domainId = group.getDomainId();

    if (rule != null && rule.length() > 0) {
      try {
        if (!scheduledMode) {
          SynchroGroupReport.setTraceLevel(SynchroGroupReport.TRACE_LEVEL_DEBUG);
          SynchroGroupReport.startSynchro();
        }

        SynchroGroupReport.warn("admin.synchronizeGroup",
            "Synchronisation du groupe '" + group.getName()
            + "' - Regle de synchronisation = \"" + rule + "\"", null);
        String[] actualUserIds = group.getUserIds();

        domainDriverManager.startTransaction(false);

        // Getting users according to rule
        List<String> userIds = null;

        if (rule.toLowerCase().startsWith("ds_")) {
          if (rule.toLowerCase().startsWith("ds_accesslevel")) {
            // Extracting access level
            String accessLevel = rule.substring(rule.indexOf("=") + 1).trim();

            if (accessLevel.equalsIgnoreCase("*")) {
              // All users
              // In case of "Domaine mixte", we retrieve all users of all
              // domains
              // Else we get only users of group's domain
              if (domainId == null) {
                userIds = Arrays.asList(userManager.getAllUsersIds(domainDriverManager));
              } else {
                userIds = Arrays.asList(userManager.getUserIdsOfDomain(
                    domainDriverManager, domainId));
              }
            } else {
              // All users by access level
              if (domainId == null) {
                userIds =
                    Arrays.asList(domainDriverManager.organization.user.getUserIdsByAccessLevel(
                    accessLevel));
              } else {
                userIds =
                    Arrays.asList(userManager.getUserIdsOfDomainAndAccessLevel(domainDriverManager,
                    domainId,
                    accessLevel));
              }
            }
          } else if (rule.toLowerCase().startsWith("ds_domain")) {
            // Extracting domain id
            String dId = rule.substring(rule.indexOf("=") + 1).trim();

            // Available only for "domaine mixte"
            if (domainId == null || "-1".equals(domainId)) {
              userIds =
                  Arrays.asList(
                  domainDriverManager.organization.user.getUserIdsOfDomain(
                  Integer.parseInt(dId)));
            }
          }
        } else if (rule.toLowerCase().startsWith("dc_")) {
          // Extracting property name and searching property value
          String propertyName = rule.substring(rule.indexOf("_") + 1,
              rule.indexOf("=")).trim();
          String propertyValue = rule.substring(rule.indexOf("=") + 1).trim();

          userIds = new ArrayList<String>();
          if (domainId == null) {
            // All users by extra information
            Domain[] domains = getAllDomains();
            for (Domain domain : domains) {
              userIds.addAll(getUserIdsBySpecificProperty(domain.getId(),
                  propertyName, propertyValue));
            }
          } else {
            userIds.addAll(getUserIdsBySpecificProperty(domainId, propertyName,
                propertyValue));
          }
        } else {
          SilverTrace.error("admin", "Admin.synchronizeGroup",
              "admin.MSG_ERR_SYNCHRONIZE_GROUP", "rule '" + rule
              + "' for groupId '" + groupId + "' is not correct !");
        }

        // Add users
        List<String> newUsers = new ArrayList<String>();
        for (int i = 0;
            userIds != null && i < userIds.size();
            i++) {
          String userId = userIds.get(i);
          boolean bFound = false;
          for (int j = 0;
              j < actualUserIds.length && !bFound;
              j++) {
            if (actualUserIds[j].equals(userId)) {
              bFound = true;
            }
          }
          if (!bFound) {
            newUsers.add(userId);
            SynchroGroupReport.info("admin.synchronizeGroup",
                "Ajout de l'utilisateur " + userId, null);
          }
        }

        SynchroGroupReport.warn("admin.synchronizeGroup", "Ajout de "
            + newUsers.size() + " utilisateur(s)", null);
        if (newUsers.size() > 0) {
          domainDriverManager.organization.group.addUsersInGroup(
              newUsers.toArray(new String[newUsers.size()]), Integer.parseInt(groupId), false);
        }

        // Remove users
        List<String> removedUsers = new ArrayList<String>();
        for (String actualUserId : actualUserIds) {
          boolean bFound = false;
          String userId;
          for (int j = 0;
              userIds != null && j < userIds.size() && !bFound;
              j++) {
            userId = userIds.get(j);
            if (userId.equals(actualUserId)) {
              bFound = true;
            }
          }
          if (!bFound) {
            removedUsers.add(actualUserId);
            SynchroGroupReport.info("admin.synchronizeGroup",
                "Suppression de l'utilisateur " + actualUserId, null);
          }
        }

        SynchroGroupReport.warn("admin.synchronizeGroup", "Suppression de "
            + removedUsers.size() + " utilisateur(s)", null);
        if (removedUsers.size() > 0) {
          domainDriverManager.organization.group.removeUsersFromGroup(
              removedUsers.toArray(new String[removedUsers.size()]), Integer.parseInt(groupId),
              false);
        }

        domainDriverManager.commit();
      } catch (Exception e) {
        try {
          // Roll back the transactions
          domainDriverManager.rollback();
        } catch (Exception e1) {
          SilverTrace.error("admin", "Admin.synchronizeGroup",
              "root.EX_ERR_ROLLBACK", e1);
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
      }
    }
  }

  private List<String> getUserIdsBySpecificProperty(String domainId,
      String propertyName, String propertyValue) throws AdminException {
    int iDomainId = Integer.parseInt(domainId);
    UserDetail[] users = ArrayUtil.EMPTY_USER_DETAIL_ARRAY;
    DomainDriver domainDriver = null;
    try {
      domainDriver = domainDriverManager.getDomainDriver(iDomainId);
    } catch (Exception e) {
      SynchroGroupReport.info("admin.getUserIdsBySpecificProperty",
          "Erreur ! Domaine " + iDomainId + " inaccessible !", null);
    }

    if (domainDriver != null) {
      try {
        users = domainDriver.getUsersBySpecificProperty(propertyName,
            propertyValue);
        if (users == null) {
          SynchroGroupReport.info("admin.getUserIdsBySpecificProperty",
              "La propriété '" + propertyName
              + "' n'est pas définie dans le domaine " + iDomainId, null);
        }
      } catch (Exception e) {
        SynchroGroupReport.info("admin.getUserIdsBySpecificProperty", "Domain "
            + domainId + " ne supporte pas les groupes synchronisés", null);
      }
    }

    List<String> specificIds = new ArrayList<String>();
    for (int u = 0;
        users != null && u < users.length;
        u++) {
      specificIds.add(users[u].getSpecificId());
    }

    // We have to find users according to theirs specificIds
    UserRow[] usersInDomain =
        domainDriverManager.organization.user.getUsersBySpecificIds(iDomainId, specificIds);
    List<String> userIds = new ArrayList<String>();
    for (int i = 0;
        usersInDomain != null && i < usersInDomain.length;
        i++) {
      userIds.add(Integer.toString(usersInDomain[i].id));
    }

    return userIds;
  }

  // //////////////////////////////////////////////////////////
  // Synchronization tools
  // //////////////////////////////////////////////////////////
  // Performs a differencial synchro
  public void difSynchro(String domainId) throws Exception {
    String sReport = "Dif User synchronization : \n";
    DomainDriver synchroDomain;
    String specificId;
    String silverpeasId;
    UserDetail[] distantUDs;
    UserDetail spUserDetail;
    Group[] distantGroups;
    Group spGroup;
    int nI;
    String fromTimeStamp, toTimeStamp;
    Domain theDomain;
    String timeStampField;

    Collection<UserDetail> listUsersUpdate = new ArrayList<UserDetail>();
    Collection<UserDetail> listUsersRemove = new ArrayList<UserDetail>();

    try {
      synchroDomain = domainDriverManager.getDomainDriver(Integer.parseInt(domainId));
      if (!synchroDomain.isSynchroInProcess()) {
        synchronized (semaphore) {
          theDomain = domainDriverManager.getDomain(domainId);
          fromTimeStamp = theDomain.getTheTimeStamp();
          toTimeStamp = synchroDomain.getTimeStamp(fromTimeStamp);
          timeStampField = synchroDomain.getTimeStampField();

          if (timeStampField == null
              || (timeStampField != null && !fromTimeStamp.equals(toTimeStamp))) {
            // FIRST Synchro the users
            // -----------------------
            // Get all users of the domain from distant datasource that have
            // changed since last full or update synchro
            distantUDs = synchroDomain.getAllChangedUsers(fromTimeStamp,
                toTimeStamp);
            for (nI = 0;
                nI < distantUDs.length;
                nI++) {
              specificId = distantUDs[nI].getSpecificId();
              silverpeasId = "";
              try {
                silverpeasId = userManager.getUserIdBySpecificIdAndDomainId(
                    domainDriverManager, specificId, domainId);
              } catch (AdminException e) // The user doesn't exist ->
              // Synchronize him
              {
                SilverTrace.warn("admin", "Admin.difSynchro",
                    "admin.EX_ERR_USER_NOT_FOUND", "SpecId=" + specificId, e);
                // The user doesnt exist -> import him
                try {
                  synchronizeImportUser(domainId, specificId, false);
                } catch (AdminException ex) // The user's synchro failed ->
                // Ignore him
                {
                  SilverTrace.warn("admin", "Admin.difSynchro",
                      "admin.MSG_ERR_SYNCHRONIZE_USER", "SpecId=" + specificId,
                      ex);
                }
              }
              if (silverpeasId.length() > 0) {
                try {
                  // The user exist -> update it
                  spUserDetail = getUserDetail(silverpeasId);
                  distantUDs[nI].setId(silverpeasId);
                  distantUDs[nI].setAccessLevel(spUserDetail.getAccessLevel());
                  distantUDs[nI].setDomainId(spUserDetail.getDomainId());
                  if (!distantUDs[nI].equals(spUserDetail)) {
                    userManager.updateUser(domainDriverManager, distantUDs[nI]);
                    cache.opUpdateUser(
                        userManager.getUserDetail(domainDriverManager, silverpeasId));
                    listUsersUpdate.add(distantUDs[nI]);
                  }
                } catch (AdminException ex) // The user's synchro failed ->
                // Ignore him
                {
                  SilverTrace.warn("admin", "Admin.difSynchro",
                      "admin.MSG_ERR_SYNCHRONIZE_USER", "UserId="
                      + silverpeasId + " SpecId=" + specificId, ex);
                }
              }
            }

            // SECOND Synchro the groups
            // -------------------------

            distantGroups = synchroDomain.getAllChangedGroups(fromTimeStamp,
                toTimeStamp);
            // Add new users or update existing ones from distant datasource
            for (nI = 0;
                nI < distantGroups.length;
                nI++) {
              specificId = distantGroups[nI].getSpecificId();
              silverpeasId = "";
              try {
                silverpeasId =
                    groupManager.getGroupIdBySpecificIdAndDomainId(domainDriverManager, specificId,
                    domainId);
              } catch (AdminException e) {
                SilverTrace.warn("admin", "Admin.difSynchro",
                    "admin.EX_ERR_GROUP_NOT_FOUND", "SpecId=" + specificId, e);
                // The group doesnt exist -> import him
                try {
                  synchronizeImportGroup(domainId, specificId, null, false,
                      true);
                } catch (AdminException ex) // The group's synchro failed ->
                // Ignore him
                {
                  SilverTrace.warn("admin", "Admin.difSynchro",
                      "admin.MSG_ERR_SYNCHRONIZE_GROUP",
                      "SpecId=" + specificId, ex);
                }
              }
              if (silverpeasId.length() > 0) {
                try {
                  // The group exist -> update it
                  spGroup = getGroup(silverpeasId);
                  distantGroups[nI].setId(silverpeasId);
                  distantGroups[nI].setDomainId(spGroup.getDomainId());
                  // For the moment, Group's move is not supported
                  distantGroups[nI].setSuperGroupId(spGroup.getSuperGroupId());
                  distantGroups[nI].setUserIds(translateUserIds(domainId,
                      distantGroups[nI].getUserIds()));
                  updateGroup(distantGroups[nI], true);
                } catch (AdminException ex) // The group's synchro failed ->
                // Ignore him
                {
                  SilverTrace.warn("admin", "Admin.difSynchro",
                      "admin.MSG_ERR_SYNCHRONIZE_GROUP", "GroupId="
                      + silverpeasId + " SpecId=" + specificId, ex);
                }
              }
            }

            if (delUsersOnDiffSynchro) {
              // Remove deleted users from distant datasource
              // Get all users of the domain from distant datasource
              distantUDs = domainDriverManager.getAllUsers(domainId);

              // Get all users of the domain from Silverpeas
              UserDetail[] silverpeasUDs = userManager.getUsersOfDomain(
                  domainDriverManager, domainId);

              boolean bFound;
              int nbDeletedUsers = 0;
              for (nI = 0;
                  nI < silverpeasUDs.length;
                  nI++) {
                bFound = false;
                specificId = silverpeasUDs[nI].getSpecificId();

                // search for user in distant datasource
                for (int nJ = 0;
                    nJ < distantUDs.length && !bFound;
                    nJ++) {
                  if (distantUDs[nJ].getSpecificId().equals(specificId)) {
                    bFound = true;
                  }
                }

                // if found, do nothing, else delete
                if (!bFound) {
                  try {
                    SilverTrace.info("admin", "admin.difSynchro",
                        "root.MSG_GEN_PARAM_VALUE",
                        "%%%%DIFFSYNCHRO%%%%>Delete User : "
                        + silverpeasUDs[nI]);
                    userManager.deleteUser(domainDriverManager, silverpeasUDs[nI], true);
                    listUsersRemove.add(distantUDs[nI]);
                  } catch (AdminException aeDel) {
                    SilverTrace.error("admin", "admin.difSynchro",
                        "root.MSG_GEN_PARAM_VALUE",
                        "%%%%DIFFSYNCHRO%%%%>PB deleting User ! " + specificId,
                        aeDel);
                  }
                }
              }
              SilverTrace.warn("admin", "admin.difSynchro",
                  "root.MSG_GEN_PARAM_VALUE", nbDeletedUsers
                  + " users have been deleted !");
            }

            // All the synchro is finished -> set the new timestamp
            // ----------------------------------------------------
            theDomain.setTheTimeStamp(toTimeStamp);
            domainDriverManager.updateDomain(theDomain);

            // traitement spécifique des users selon l'interface implémentée
            processSpecificSynchronization(domainId, null, listUsersUpdate, listUsersRemove);
          }
        }
      } else {
        SilverTrace.warn("admin", "admin.difSynchro",
            "root.MSG_GEN_EXIT_METHOD",
            "Full synchro currently running, skipping diff synchro....");
      }
    } catch (Exception e) {
      SynchroReport.error("admin.difSynchro",
          "Problème lors de la synchronisation des utilisateurs : "
          + e.getMessage(), null);
      throw new AdminException("admin.difSynchro", SilverpeasException.ERROR,
          "admin.EX_ERR_SYNCHRONIZE_DOMAIN_USERS", "domain id : '" + domainId
          + "'\nReport:" + sReport, e);
    }
  }

  private List<String> translateGroupIds(String sDomainId, String[] groupSpecificIds,
      boolean recursGroups) throws Exception {
    List<String> convertedGroupIds = new ArrayList<String>();
    String groupId;

    for (String groupSpecificId : groupSpecificIds) {
      try {
        groupId = groupManager.getGroupIdBySpecificIdAndDomainId(domainDriverManager,
            groupSpecificId, sDomainId);
      } catch (AdminException e) // The group doesn't exist -> Synchronize him
      {
        groupId = null;
        SilverTrace.warn("admin", "Admin.translateGroupIds",
            "admin.EX_ERR_GROUP_NOT_FOUND", "SpecId=" + groupSpecificId, e);
        if (recursGroups) {
          try {
            groupId = synchronizeImportGroup(sDomainId, groupSpecificId,
                null, true, true);
          } catch (AdminException ex) // The group's synchro failed -> Ignore
          // him
          {
            SilverTrace.warn("admin", "Admin.translateGroupIds",
                "admin.MSG_ERR_SYNCHRONIZE_GROUP", "SpecId="
                + groupSpecificId, ex);
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
    List<String> convertedUserIds = new ArrayList<String>();
    String userId;

    for (String userSpecificId : userSpecificIds) {
      try {
        userId = userManager.getUserIdBySpecificIdAndDomainId(domainDriverManager,
            userSpecificId, sDomainId);
      } catch (AdminException e) // The user doesn't exist -> Synchronize him
      {
        SilverTrace.warn("admin", "Admin.translateUserIds",
            "admin.EX_ERR_USER_NOT_FOUND", "SpecId=" + userSpecificId, e);
        try {
          userId = synchronizeImportUser(sDomainId, userSpecificId, false);
        } catch (AdminException ex) // The user's synchro failed -> Ignore him
        {
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

  /**
   *
   */
  public String synchronizeGroup(String groupId, boolean recurs) throws Exception {
    SilverTrace.info("admin", "admin.synchronizeGroup",
        "root.MSG_GEN_ENTER_METHOD", "GroupId=" + groupId);
    Group theGroup = getGroup(groupId);

    if (theGroup.isSynchronized()) {
      synchronizeGroupByRule(groupId, false);
    } else {
      DomainDriver synchroDomain =
          domainDriverManager.getDomainDriver(Integer.parseInt(theGroup.getDomainId()));
      Group gr = synchroDomain.synchroGroup(theGroup.getSpecificId());

      gr.setId(groupId);
      gr.setDomainId(theGroup.getDomainId());
      gr.setSuperGroupId(theGroup.getSuperGroupId());
      internalSynchronizeGroup(synchroDomain, gr, recurs);
    }
    return groupId;
  }

  /**
   *
   */
  public String synchronizeImportGroup(String domainId, String groupKey,
      String askedParentId, boolean recurs, boolean isIdKey) throws Exception {
    SilverTrace.info("admin", "admin.synchronizeImportGroup",
        "root.MSG_GEN_ENTER_METHOD", "groupKey=" + groupKey);
    DomainDriver synchroDomain = domainDriverManager.getDomainDriver(Integer.parseInt(
        domainId));
    Group gr;
    String groupId;
    String[] specificIds;
    int i;
    String[] parentSpecificIds;
    String parentId;

    if (isIdKey) {
      gr = synchroDomain.synchroGroup(groupKey);
    } else {
      gr = synchroDomain.importGroup(groupKey);
    }
    gr.setDomainId(domainId);

    // We now search for the parent of this group
    // ------------------------------------------
    // First, we get the parents of the group
    parentSpecificIds = synchroDomain.getGroupMemberGroupIds(gr.getSpecificId());
    parentId = null;
    for (i = 0;
        (i < parentSpecificIds.length) && (parentId == null);
        i++) {
      try {
        parentId = groupManager.getGroupIdBySpecificIdAndDomainId(
            domainDriverManager, parentSpecificIds[i], domainId);
        if (askedParentId != null && !askedParentId.isEmpty() && !askedParentId.equals(
            parentId)) { // It is not the
          // matching parent
          parentId = null;
        }
      } catch (AdminException e) // The user doesn't exist -> Synchronize him
      {
        parentId = null;
      }
    }
    if ((parentId == null)
        && ((parentSpecificIds.length > 0) || ((askedParentId != null) && (askedParentId.length() > 0)))) {// We
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
    specificIds = gr.getUserIds();
    gr.setUserIds(ArrayUtil.EMPTY_STRING_ARRAY);
    groupId = addGroup(gr, true);
    gr.setId(groupId);
    gr.setUserIds(specificIds);
    internalSynchronizeGroup(synchroDomain, gr, recurs);
    return groupId;
  }

  /**
   *
   */
  public String synchronizeRemoveGroup(String groupId) throws Exception {
    SilverTrace.info("admin", "admin.synchronizeRemoveGroup",
        "root.MSG_GEN_ENTER_METHOD", "GroupId=" + groupId);
    Group theGroup = getGroup(groupId);
    DomainDriver synchroDomain =
        domainDriverManager.getDomainDriver(Integer.parseInt(theGroup.getDomainId()));
    synchroDomain.removeGroup(theGroup.getSpecificId());
    return deleteGroupById(groupId, true);
  }

  protected void internalSynchronizeGroup(DomainDriver synchroDomain,
      Group latestGroup, boolean recurs) throws Exception {
    latestGroup.setUserIds(translateUserIds(latestGroup.getDomainId(),
        latestGroup.getUserIds()));
    updateGroup(latestGroup, true);
    if (recurs) {
      Group[] childs = synchroDomain.getGroups(latestGroup.getSpecificId());
      int i;
      String existingGroupId;
      Group existingGroup;

      for (i = 0;
          i < childs.length;
          i++) {
        existingGroupId = null;
        try {
          existingGroupId =
              groupManager.getGroupIdBySpecificIdAndDomainId(domainDriverManager,
              childs[i].getSpecificId(), latestGroup.getDomainId());
          existingGroup = getGroup(existingGroupId);
          if (existingGroup.getSuperGroupId().equals(latestGroup.getId())) {
            // Only synchronize the group if latestGroup is his true parent
            synchronizeGroup(existingGroupId, recurs);
          }
        } catch (AdminException e) // The group doesn't exist -> Import him
        {
          if (existingGroupId == null) { // Import the new group
            synchronizeImportGroup(latestGroup.getDomainId(), childs[i].getSpecificId(),
                latestGroup.getId(), recurs, true);
          }
        }
      }
    }
  }

  /**
   * Synchronize Users and groups between cache and domain's datastore
   */
  public String synchronizeUser(String userId, boolean recurs) throws Exception {
    int i;

    Collection<UserDetail> listUsersUpdate = new ArrayList<UserDetail>();

    SilverTrace.info("admin", "admin.synchronizeUser", "root.MSG_GEN_ENTER_METHOD", "userId="
        + userId);
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
      if (!ud.equals(theUserDetail)) {
        userManager.updateUser(domainDriverManager, ud);
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
      for (i = 0; i < oldGroupsId.length; i++) {
        if (incGroupsId.contains(oldGroupsId[i])) { // No changes have to be
          // performed to the group ->
          // Remove it
          incGroupsId.remove(oldGroupsId[i]);
        } else {
          Group grpToRemove = groupManager.getGroup(domainDriverManager,
              oldGroupsId[i]);
          if (theUserDetail.getDomainId().equals(grpToRemove.getDomainId())) {
            // Remove the user from this group
            groupManager.removeUserFromGroup(domainDriverManager, userId,
                oldGroupsId[i]);
            cache.opRemoveUserFromGroup(userId, oldGroupsId[i]);
          }
        }
      }
      // Now the remaining groups of the vector are the groups where the user is
      // newly added
      for (i = 0; i < incGroupsId.size(); i++) {
        groupManager.addUserInGroup(domainDriverManager, userId, incGroupsId.get(i));
        cache.opAddUserInGroup(userId, incGroupsId.get(i));
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
    }
  }

  /**
   * Synchronize Users and groups between cache and domain's datastore
   */
  public String synchronizeImportUserByLogin(String domainId, String userLogin, boolean recurs)
      throws Exception {
    SilverTrace.info("admin", "admin.synchronizeImportUserByLogin",
        "root.MSG_GEN_ENTER_METHOD", "userLogin=" + userLogin);
    DomainDriver synchroDomain = domainDriverManager.getDomainDriver(Integer.parseInt(
        domainId));
    UserDetail ud = synchroDomain.importUser(userLogin);
    String userId;
    ud.setDomainId(domainId);
    userId = addUser(ud, true);
    // Synchronizes the user to add it to the groups and recursivaly add the groups
    synchronizeUser(userId, recurs);
    return userId;
  }

  /**
   * Synchronize Users and groups between cache and domain's datastore
   */
  public String synchronizeImportUser(String domainId, String specificId, boolean recurs) throws
      Exception {
    SilverTrace.info("admin", "admin.synchronizeImportUser",
        "root.MSG_GEN_ENTER_METHOD", "specificId=" + specificId);
    DomainDriver synchroDomain = domainDriverManager.getDomainDriver(Integer.parseInt(
        domainId));
    UserDetail ud = synchroDomain.getUser(specificId);
    String userId;

    ud.setDomainId(domainId);
    userId = addUser(ud, true);
    // Synchronizes the user to add it to the groups and recursivaly add the groups
    synchronizeUser(userId, recurs);
    return userId;
  }

  public List<DomainProperty> getSpecificPropertiesToImportUsers(String domainId,
      String language) throws Exception {
    SilverTrace.info("admin", "admin.getSpecificPropertiesToImportUsers",
        "root.MSG_GEN_ENTER_METHOD", "domainId=" + domainId);
    DomainDriver synchroDomain = domainDriverManager.getDomainDriver(Integer.parseInt(
        domainId));
    return synchroDomain.getPropertiesToImport(language);
  }

  public UserDetail[] searchUsers(String domainId, Map<String, String> query)
      throws Exception {
    SilverTrace.info("admin", "admin.searchUsers", "root.MSG_GEN_ENTER_METHOD",
        "domainId=" + domainId);
    DomainDriver synchroDomain = domainDriverManager.getDomainDriver(Integer.parseInt(domainId));
    return synchroDomain.getUsersByQuery(query);
  }

  /**
   * Synchronize Users and groups between cache and domain's datastore.
   *
   * @param userId
   * @return
   * @throws Exception
   */
  public String synchronizeRemoveUser(String userId) throws Exception {
    SilverTrace.info("admin", "admin.synchronizeRemoveUser", "root.MSG_GEN_ENTER_METHOD",
        "userId=" + userId);
    UserDetail theUserDetail = getUserDetail(userId);
    DomainDriver synchroDomain = domainDriverManager.getDomainDriver(Integer.parseInt(theUserDetail.
        getDomainId()));
    synchroDomain.removeUser(theUserDetail.getSpecificId());
    deleteUser(userId, true);
    List<UserDetail> listUsersRemove = new ArrayList<UserDetail>();
    listUsersRemove.add(theUserDetail);
    processSpecificSynchronization(theUserDetail.getDomainId(), null, null, listUsersRemove);
    return userId;
  }

  /**
   * Synchronize Users and groups between cache and domain's datastore
   */
  public String synchronizeSilverpeasWithDomain(String sDomainId) throws Exception {
    String sReport = "Starting synchronization...\n\n";
    Map<String, String> userIds = new HashMap<String, String>();
    String sDomainSpecificErrors;
    String fromTimeStamp, toTimeStamp;
    Domain theDomain;
    DomainDriver synchroDomain;

    synchronized (semaphore) {
      SilverTrace.info("admin", "admin.synchronizeSilverpeasWithDomain",
          "root.MSG_GEN_ENTER_METHOD", "domainID=" + sDomainId);
      // Démarrage de la synchro avec la Popup d'affichage
      SynchroReport.startSynchro();
      // SynchroReport.warn("admin.synchronizeSilverpeasWithDomain",
      // "Début de synchronisation...",null);
      try {
        SynchroReport.warn("admin.synchronizeSilverpeasWithDomain",
            "Domaine : " + domainDriverManager.getDomain(sDomainId).getName()
            + ", ID : " + sDomainId, null);
        // Start synchronization
        domainDriverManager.beginSynchronization(sDomainId);

        synchroDomain = domainDriverManager.getDomainDriver(Integer.parseInt(sDomainId));
        theDomain = domainDriverManager.getDomain(sDomainId);
        fromTimeStamp = theDomain.getTheTimeStamp();
        toTimeStamp = synchroDomain.getTimeStamp(fromTimeStamp);
        SilverTrace.info("admin", "admin.synchronizeSilverpeasWithDomain",
            "root.MSG_GEN_ENTER_METHOD", "TimeStamps from " + fromTimeStamp
            + " to " + toTimeStamp);

        // Start transaction
        domainDriverManager.startTransaction(false);
        domainDriverManager.startTransaction(sDomainId, false);

        // Synchronize users
        if (synchroDomain.mustImportUsers()) {
          sReport += synchronizeUsers(sDomainId, userIds);
        } else {
          sReport += synchronizeOnlyExistingUsers(sDomainId, userIds);
        }

        // Synchronize groups
        sReport += "\n" + synchronizeGroups(sDomainId, userIds);

        // All the synchro is finished -> set the new timestamp
        // ----------------------------------------------------
        theDomain.setTheTimeStamp(toTimeStamp);
        domainDriverManager.updateDomain(theDomain);

        // Commit the transaction
        domainDriverManager.commit();
        domainDriverManager.commit(sDomainId);

        // End synchronization
        sDomainSpecificErrors = domainDriverManager.endSynchronization(sDomainId, false);
        SynchroReport.warn("admin.synchronizeSilverpeasWithDomain",
            "----------------" + sDomainSpecificErrors, null);
        // return group id
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
        SynchroReport.error("admin.synchronizeSilverpeasWithDomain",
            "Problème lors de la synchronisation : " + e.getMessage(), null);
        throw new AdminException("Admin.synchronizeSilverpeasWithDomain",
            SilverpeasException.ERROR, "admin.EX_ERR_SYNCHRONIZE_DOMAIN",
            "domain id : '" + sDomainId + "'\nReport:" + sReport, e);
      } finally {
        SynchroReport.stopSynchro();// Fin de synchro avec la Popup d'affichage
        // Reset the cache
        cache.resetCache();
      }
    }
  }

  /**
   * Synchronize users between cache and domain's datastore
   */
  private String synchronizeUsers(String domainId, Map<String, String> userIds) throws Exception {
    boolean bFound;
    String specificId;
    String silverpeasId;
    String sReport = "User synchronization : \n";
    int iNbUsersAdded = 0;
    int iNbUsersMaj = 0;
    int iNbUsersDeleted = 0;

    Collection<UserDetail> listUsersCreate = new ArrayList<UserDetail>();
    Collection<UserDetail> listUsersUpdate = new ArrayList<UserDetail>();
    Collection<UserDetail> listUsersRemove = new ArrayList<UserDetail>();

    SynchroReport.warn("admin.synchronizeUsers", "SYNCHRONISATION UTILISATEURS :", null);
    try {
      // Clear conversion table
      userIds.clear();
      // Get all users of the domain from distant datasource
      UserDetail[] distantUDs = domainDriverManager.getAllUsers(domainId);
      // Get all users of the domain from Silverpeas
      UserDetail[] silverpeasUDs = userManager.getUsersOfDomain(domainDriverManager, domainId);
      SynchroReport.info("admin.synchronizeUsers",
          "AJOUT ou MISE A JOUR des utilisateurs dans la base...", null);
      SynchroReport.info("admin.synchronizeUsers", "Valeur du paramètre 'm_bFallbackUserLogins' = "
          + shouldFallbackUserLogins, null);

      // Add new users or update existing ones from distant datasource
      for (UserDetail distantUD : distantUDs) {
        bFound = false;
        specificId = distantUD.getSpecificId();
        SilverTrace.info("admin", "admin.synchronizeUsers", "root.MSG_GEN_PARAM_VALUE",
            "%%%%FULLSYNCHRO%%%%>Deal with user : " + specificId);

        // search for user in Silverpeas database
        for (int nJ = 0;
            nJ < silverpeasUDs.length && !bFound;
            nJ++) {
          if (silverpeasUDs[nJ].getSpecificId().equals(specificId)
              || (shouldFallbackUserLogins && silverpeasUDs[nJ].getLogin().equals(
              distantUD.getLogin()))) {
            bFound = true;
            distantUD.setId(silverpeasUDs[nJ].getId());
            distantUD.setAccessLevel(silverpeasUDs[nJ].getAccessLevel());
            userIds.put(specificId, silverpeasUDs[nJ].getId());
          }
        }

        distantUD.setDomainId(domainId);
        // if found, update, else create
        if (bFound) { // MAJ
          try {
            SilverTrace.info("admin", "admin.synchronizeUsers",
                "root.MSG_GEN_PARAM_VALUE",
                "%%%%FULLSYNCHRO%%%%>Update User : " + distantUD.getId());
            silverpeasId = userManager.updateUser(domainDriverManager, distantUD);
            listUsersUpdate.add(distantUD);
            iNbUsersMaj++;
            SynchroReport.warn("admin.synchronizeUsers", "maj utilisateur "
                + distantUD.getFirstName() + " "
                + distantUD.getLastName() + " (id:" + silverpeasId
                + " / specificId:" + specificId + ") OK", null);
            sReport += "updating user " + distantUD.getFirstName() + " "
                + distantUD.getLastName() + "(id:" + specificId + ")\n";
          } catch (AdminException aeMaj) {
            SilverTrace.info("admin", "admin.synchronizeUsers",
                "root.MSG_GEN_PARAM_VALUE",
                "%%%%FULLSYNCHRO%%%%>PB Updating User ! " + specificId, aeMaj);
            sReport += "problem updating user " + distantUD.getFirstName()
                + " " + distantUD.getLastName() + " (specificId:"
                + specificId + ") - " + aeMaj.getMessage() + "\n";
            sReport += "user has not been updated\n";
          }
        } else// AJOUT
        {
          try {
            silverpeasId = userManager.addUser(domainDriverManager, distantUD,
                true);
            if (silverpeasId.equals("")) {
              SilverTrace.info("admin", "admin.synchronizeUsers",
                  "root.MSG_GEN_PARAM_VALUE",
                  "%%%%FULLSYNCHRO%%%%>PB Adding User ! " + specificId);
              sReport += "problem adding user " + distantUD.getFirstName()
                  + " " + distantUD.getLastName() + "(specificId:"
                  + specificId + ") - Login and LastName must be set !!!\n";
              sReport += "user has not been added\n";
            } else {
              iNbUsersAdded++;
              SilverTrace.info("admin", "admin.synchronizeUsers",
                  "root.MSG_GEN_PARAM_VALUE", "%%%%FULLSYNCHRO%%%%>Add User : "
                  + silverpeasId);
              listUsersCreate.add(distantUD);
              sReport += "adding user " + distantUD.getFirstName() + " "
                  + distantUD.getLastName() + "(id:" + silverpeasId
                  + " / specificId:" + specificId + ")\n";
              SynchroReport.warn("admin.synchronizeUsers", "ajout utilisateur "
                  + distantUD.getFirstName() + " "
                  + distantUD.getLastName() + " (id:" + silverpeasId
                  + " / specificId:" + specificId + ") OK", null);
              userIds.put(specificId, silverpeasId);
            }
          } catch (AdminException ae) {
            SilverTrace.info("admin", "admin.synchronizeUsers",
                "root.MSG_GEN_PARAM_VALUE",
                "%%%%FULLSYNCHRO%%%%>PB Adding User ! " + specificId, ae);
            sReport += "problem adding user " + distantUD.getFirstName()
                + " " + distantUD.getLastName() + "(specificId:"
                + specificId + ") - " + ae.getMessage() + "\n";
            sReport += "user has not been added\n";
          }
        }
      }

      // Delete obsolete users from Silverpeas
      SynchroReport.info("admin.synchronizeUsers",
          "SUPPRESSION des éventuels utilisateurs obsolètes de la base...",
          null);
      for (UserDetail silverpeasUD : silverpeasUDs) {
        bFound = false;
        specificId = silverpeasUD.getSpecificId();

        // search for user in distant datasource
        for (int nJ = 0;
            nJ < distantUDs.length && !bFound;
            nJ++) {
          if (distantUDs[nJ].getSpecificId().equals(specificId)
              || (shouldFallbackUserLogins && silverpeasUD.getLogin().equals(
              distantUDs[nJ].getLogin()))) {
            bFound = true;
          }
        }

        // if found, do nothing, else delete
        if (!bFound) {
          try {
            SilverTrace.info("admin", "admin.synchronizeUsers",
                "root.MSG_GEN_PARAM_VALUE",
                "%%%%FULLSYNCHRO%%%%>Delete User : " + silverpeasUD);
            userManager.deleteUser(domainDriverManager, silverpeasUD, true);
            listUsersRemove.add(silverpeasUD);
            iNbUsersDeleted++;
            sReport += "deleting user " + silverpeasUD.getFirstName()
                + " " + silverpeasUD.getLastName() + "(id:" + specificId
                + ")\n";
            SynchroReport.warn("admin.synchronizeUsers",
                "suppression utilisateur " + silverpeasUD.getFirstName()
                + " " + silverpeasUD.getLastName() + " (specificId:"
                + specificId + ") OK", null);
          } catch (AdminException aeDel) {
            SilverTrace.info("admin", "admin.synchronizeUsers",
                "root.MSG_GEN_PARAM_VALUE",
                "%%%%FULLSYNCHRO%%%%>PB deleting User ! " + specificId, aeDel);
            sReport += "problem deleting user "
                + silverpeasUD.getFirstName() + " "
                + silverpeasUD.getLastName() + " (specificId:"
                + specificId + ") - " + aeDel.getMessage() + "\n";
            sReport += "user has not been deleted\n";
          }
        }
      }

      //noinspection UnusedAssignment
      distantUDs = null;
      silverpeasUDs = null;

      // traitement spécifique des users selon l'interface implémentée
      processSpecificSynchronization(domainId, listUsersCreate, listUsersUpdate, listUsersRemove);

      sReport += "User synchronization terminated\n";
      SynchroReport.info("admin.synchronizeUsers",
          "Nombre d'utilisateurs mis à jour : " + iNbUsersMaj + ", ajoutés : "
          + iNbUsersAdded + ", supprimés : " + iNbUsersDeleted, null);
      SynchroReport.warn("admin.synchronizeUsers",
          "Synchronisation utilisateurs terminée", null);
      return sReport;
    } catch (Exception e) {
      SynchroReport.error("admin.synchronizeUsers",
          "Problème lors de la synchronisation des utilisateurs : "
          + e.getMessage(), null);
      throw new AdminException("admin.synchronizeUsers",
          SilverpeasException.ERROR, "admin.EX_ERR_SYNCHRONIZE_DOMAIN_USERS",
          "domain id : '" + domainId + "'\nReport:" + sReport, e);
    }
  }

  /**
   * Synchronize users between cache and domain's datastore
   */
  private String synchronizeOnlyExistingUsers(String domainId, Map<String, String> userIds)
      throws Exception {
    boolean bFound;
    String specificId;
    String silverpeasId;
    String sReport = "User synchronization : \n";
    int iNbUsersAdded = 0;
    int iNbUsersMaj = 0;
    int iNbUsersDeleted = 0;
    Collection<UserDetail> listUsersUpdate = new ArrayList<UserDetail>();
    Collection<UserDetail> listUsersRemove = new ArrayList<UserDetail>();

    SynchroReport.warn("admin.synchronizeOnlyExistingUsers",
        "SYNCHRONISATION UTILISATEURS :", null);
    try {
      // Clear conversion table
      userIds.clear();

      // Get all users of the domain from distant datasource
      UserDetail[] distantUDs = domainDriverManager.getAllUsers(domainId);

      // Get all users of the domain from Silverpeas
      UserDetail[] silverpeasUDs = userManager.getUsersOfDomain(domainDriverManager, domainId);

      SynchroReport.info("admin.synchronizeOnlyExistingUsers",
          "MISE A JOUR ou SUPPRESSION des utilisateurs dans la base...", null);
      SynchroReport.info("admin.synchronizeOnlyExistingUsers",
          "Valeur du paramètre 'm_bFallbackUserLogins' = " + shouldFallbackUserLogins, null);
      UserDetail userLDAP = null;
      // Update existing users from distant datasource
      for (UserDetail userSP : silverpeasUDs) {
        bFound = false;
        specificId = userSP.getSpecificId();
        // search for user in distant datasource
        for (int nJ = 0; nJ < distantUDs.length && !bFound; nJ++) {
          userLDAP = distantUDs[nJ];
          if (userLDAP.getSpecificId().equals(specificId) || (shouldFallbackUserLogins && userLDAP.
              getLogin().equals(userSP.getLogin()))) {
            bFound = true;
            userLDAP.setId(userSP.getId());
            userLDAP.setAccessLevel(userSP.getAccessLevel());
            userIds.put(specificId, userSP.getId());
          }
        }
        if (userLDAP != null) {
          userLDAP.setDomainId(domainId);
        }
        // if found, update else delete
        if (bFound) { // MAJ
          try {
            SilverTrace.info("admin", "admin.synchronizeOnlyExistingUsers",
                "root.MSG_GEN_PARAM_VALUE",
                "%%%%FULLSYNCHRO%%%%>Update User : " + userLDAP.getId());
            silverpeasId = userManager.updateUser(domainDriverManager, userLDAP);
            listUsersUpdate.add(userLDAP);
            iNbUsersMaj++;
            SynchroReport.warn("admin.synchronizeOnlyExistingUsers",
                "maj utilisateur " + userLDAP.getDisplayedName() + " (id:"
                + silverpeasId + " / specificId:" + specificId + ") OK",
                null);
            sReport += "updating user " + userLDAP.getDisplayedName() + "(id:"
                + specificId + ")\n";
          } catch (AdminException aeMaj) {
            SilverTrace.info("admin", "admin.synchronizeOnlyExistingUsers",
                "root.MSG_GEN_PARAM_VALUE",
                "%%%%FULLSYNCHRO%%%%>PB Updating User ! " + specificId, aeMaj);
            sReport += "problem updating user " + userLDAP.getDisplayedName()
                + " (specificId:" + specificId + ") - " + aeMaj.getMessage()
                + "\n";
            sReport += "user has not been updated\n";
          }
        } else {
          try {
            SilverTrace.info("admin", "admin.synchronizeOnlyExistingUsers",
                "root.MSG_GEN_PARAM_VALUE", "%%%%FULLSYNCHRO%%%%>Delete User : " + userSP);
            userManager.deleteUser(domainDriverManager, userSP, true);
            listUsersRemove.add(userSP);
            iNbUsersDeleted++;
            sReport += "deleting user " + userSP.getDisplayedName() + "(id:"
                + specificId + ")\n";
            SynchroReport.warn("admin.synchronizeOnlyExistingUsers",
                "suppression utilisateur " + userSP.getDisplayedName()
                + " (specificId:" + specificId + ") OK", null);
          } catch (AdminException aeDel) {
            SilverTrace.info("admin", "admin.synchronizeOnlyExistingUsers",
                "root.MSG_GEN_PARAM_VALUE",
                "%%%%FULLSYNCHRO%%%%>PB deleting User ! " + specificId, aeDel);
            sReport += "problem deleting user " + userSP.getDisplayedName()
                + " (specificId:" + specificId + ") - " + aeDel.getMessage() + "\n";
            sReport += "user has not been deleted\n";
          }
        }
      }

      //noinspection UnusedAssignment,UnusedAssignment,UnusedAssignment,UnusedAssignment
      distantUDs = null;
      silverpeasUDs = null;

      // traitement spécifique des users selon l'interface implémentée
      processSpecificSynchronization(domainId, null, listUsersUpdate, listUsersRemove);

      sReport += "User synchronization terminated\n";
      SynchroReport.info("admin.synchronizeOnlyExistingUsers",
          "Nombre d'utilisateurs mis à jour : " + iNbUsersMaj + ", ajoutés : "
          + iNbUsersAdded + ", supprimés : " + iNbUsersDeleted, null);
      SynchroReport.warn("admin.synchronizeOnlyExistingUsers",
          "Synchronisation utilisateurs terminée", null);
      return sReport;
    } catch (Exception e) {
      SynchroReport.error("admin.synchronizeOnlyExistingUsers",
          "Problème lors de la synchronisation des utilisateurs : "
          + e.getMessage(), null);
      throw new AdminException("admin.synchronizeOnlyExistingUsers",
          SilverpeasException.ERROR, "admin.EX_ERR_SYNCHRONIZE_DOMAIN_USERS",
          "domain id : '" + domainId + "'\nReport:" + sReport, e);
    }
  }

  private void processSpecificSynchronization(String domainId, Collection<UserDetail> usersAdded,
      Collection<UserDetail> usersUpdated, Collection<UserDetail> usersRemoved) throws Exception {
    Domain theDomain = domainDriverManager.getDomain(domainId);
    String propDomainFileName = theDomain.getPropFileName();
    ResourceLocator propDomainLdap = new ResourceLocator(propDomainFileName, "");
    String nomClasseSynchro = propDomainLdap.getString("synchro.Class");
    if (StringUtil.isDefined(nomClasseSynchro)) {

      Collection<UserDetail> added = usersAdded;
      Collection<UserDetail> updated = usersUpdated;
      Collection<UserDetail> removed = usersRemoved;
      if (added == null) {
        added = new ArrayList<UserDetail>();
      }
      if (updated == null) {
        updated = new ArrayList<UserDetail>();
      }
      if (removed == null) {
        removed = new ArrayList<UserDetail>();
      }
      try {
        LDAPSynchroUserItf synchroUser = (LDAPSynchroUserItf) Class.forName(nomClasseSynchro).
            newInstance();
        if (synchroUser != null) {
          synchroUser.processUsers(added, updated, removed);
        }
      } catch (Exception e) {
        SilverTrace.warn("admin", "admin.synchronizeOnlyExistingUsers",
            "root.MSG_GEN_PARAM_VALUE", "Pb Loading class traitement Users ! ", e);
      }
    }
  }

  /**
   * Synchronize groups between cache and domain's datastore
   */
  private String synchronizeGroups(String domainId, Map<String, String> userIds) throws Exception {
    boolean bFound;
    String specificId;
    String sReport = "Group synchronization : \n";
    Map<String, Group> allDistantGroups = new HashMap<String, Group>();
    int iNbGroupsAdded = 0;
    int iNbGroupsMaj = 0;
    int iNbGroupsDeleted = 0;
    SynchroReport.warn("admin.synchronizeGroups", "SYNCHRONISATION GROUPES :", null);
    try {
      // Get all root groups of the domain from distant datasource
      Group[] distantRootGroups = domainDriverManager.getAllRootGroups(domainId);
      // Get all groups of the domain from Silverpeas
      Group[] silverpeasGroups = groupManager.getGroupsOfDomain(domainDriverManager, domainId);

      SynchroReport.info("admin.synchronizeGroups",
          "AJOUT ou MISE A JOUR des groupes dans la base...", null);
      // Check for new groups resursively
      sReport += checkOutGroups(domainId, silverpeasGroups, distantRootGroups, allDistantGroups,
          userIds, null, iNbGroupsAdded, iNbGroupsMaj, iNbGroupsDeleted);

      // Delete obsolete groups
      SynchroReport.info("admin.synchronizeGroups",
          "SUPPRESSION des éventuels groupes obsolètes de la base...", null);
      Group[] distantGroups = allDistantGroups.values().toArray(
          new Group[allDistantGroups.size()]);
      for (Group silverpeasGroup : silverpeasGroups) {
        bFound = false;
        specificId = silverpeasGroup.getSpecificId();

        // search for group in distant datasource
        for (int nJ = 0;
            nJ < distantGroups.length && !bFound;
            nJ++) {
          if (distantGroups[nJ].getSpecificId().equals(specificId)) {
            bFound = true;
          } else if (shouldFallbackGroupNames && distantGroups[nJ].getName().equals(specificId)) {
            bFound = true;
          }
        }

        // if found, do nothing, else delete
        if (!bFound) {
          try {
            SilverTrace.info("admin", "admin.synchronizeGroups", "root.MSG_GEN_PARAM_VALUE",
                "%%%%FULLSYNCHRO%%%%>Delete group : " + silverpeasGroup.getId() + " - " + specificId);
            groupManager.deleteGroupById(domainDriverManager, silverpeasGroup, true);
            iNbGroupsDeleted++;
            sReport += "deleting group " + silverpeasGroup.getName() + "(id:" + specificId + ")\n";
            SynchroReport.warn("admin.synchronizeGroups", "suppression groupe "
                + silverpeasGroup.getName() + "(SpecificId:" + specificId + ") OK", null);
          } catch (AdminException aeDel) {
            SilverTrace.info("admin", "admin.synchronizeGroups", "root.MSG_GEN_PARAM_VALUE",
                "%%%%FULLSYNCHRO%%%%>PB deleting group ! " + specificId, aeDel);
            sReport += "problem deleting group " + silverpeasGroup.getName() + " (specificId:"
                + specificId + ") - " + aeDel.getMessage() + "\n";
            sReport += "group has not been deleted\n";
          }
        }
      }
      sReport += "Group synchronization terminated\n";
      SynchroReport.info("admin.synchronizeGroups",
          "Nombre de groupes mis à jour : " + iNbGroupsMaj + ", ajoutés : "
          + iNbGroupsAdded + ", supprimés : " + iNbGroupsDeleted, null);
      SynchroReport.warn("admin.synchronizeGroups",
          "Synchronisation groupes terminée", null);
      return sReport;
    } catch (Exception e) {
      SynchroReport.error("admin.synchronizeGroups",
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
    // Add new groups or update existing ones from distant datasource
    for (Group testedGroup : testedGroups) {
      bFound = false;
      specificId = testedGroup.getSpecificId();

      SilverTrace.info("admin", "admin.checkOutGroups",
          "root.MSG_GEN_PARAM_VALUE", "%%%%FULLSYNCHRO%%%%>Deal with group : "
          + specificId);
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
        SynchroReport.debug("admin.checkOutGroups", "avant maj du groupe "
            + specificId + ", recherche de ses groupes parents", null);
      } else {
        SynchroReport.debug("admin.checkOutGroups", "avant ajout du groupe "
            + specificId + ", recherche de ses groupes parents", null);
      }
      String[] groupParentsIds = domainDriverManager.getGroupMemberGroupIds(domainId, testedGroup.
          getSpecificId());
      if ((groupParentsIds == null) || (groupParentsIds.length == 0)) {
        testedGroup.setSuperGroupId(null);
        SynchroReport.debug("admin.checkOutGroups", "le groupe " + specificId + " n'a pas de père",
            null);
      } else {
        testedGroup.setSuperGroupId(superGroupId);
        if (superGroupId != null)// sécurité
        {
          SynchroReport.debug("admin.checkOutGroups",
              "le groupe " + specificId + " a pour père le groupe " + domainDriverManager.getGroup(
              superGroupId).getSpecificId() + " d'Id base " + superGroupId, null);
        }
      }
      String[] groupUserIds = testedGroup.getUserIds();
      List<String> convertedUserIds = new ArrayList<String>();
      for (String groupUserId : groupUserIds) {
        if (userIds.get(groupUserId) != null) {
          convertedUserIds.add(userIds.get(groupUserId));
        }
      }
      // Le groupe contiendra une liste d'IDs de users existant ds la base et
      // non + une liste de logins récupérés via LDAP
      testedGroup.setUserIds(convertedUserIds.toArray(new String[convertedUserIds.size()]));
      // if found, update, else create
      if (bFound)// MAJ
      {
        try {
          SilverTrace.info("admin", "admin.checkOutGroups", "root.MSG_GEN_PARAM_VALUE",
              "%%%%FULLSYNCHRO%%%%>Update group : " + testedGroup.getId());
          result = groupManager.updateGroup(domainDriverManager, testedGroup, true);
          if (StringUtil.isDefined(result)) {
            iNbGroupsMaj++;
            silverpeasId = testedGroup.getId();
            report += "updating group " + testedGroup.getName() + "(id:"
                + specificId + ")\n";
            SynchroReport.warn("admin.checkOutGroups", "maj groupe "
                + testedGroup.getName() + " (id:" + silverpeasId + ") OK",
                null);
          } else// le name groupe non renseigné
          {
            SilverTrace.info("admin", "admin.checkOutGroups",
                "root.MSG_GEN_PARAM_VALUE",
                "%%%%FULLSYNCHRO%%%%>PB Updating Group ! " + specificId);
            report += "problem updating group id : " + specificId + "\n";
          }
        } catch (AdminException aeMaj) {
          SilverTrace.info("admin", "admin.checkOutGroups",
              "root.MSG_GEN_PARAM_VALUE",
              "%%%%FULLSYNCHRO%%%%>PB Updating Group ! " + specificId, aeMaj);
          report += "problem updating group " + testedGroup.getName()
              + " (id:" + specificId + ") " + aeMaj.getMessage() + "\n";
          report += "group has not been updated\n";
        }
      } else { // AJOUT
        try {
          silverpeasId = groupManager.addGroup(domainDriverManager, testedGroup, true);
          if (StringUtil.isDefined(silverpeasId)) {
            iNbGroupsAdded++;
            SilverTrace.info("admin", "admin.checkOutGroups", "root.MSG_GEN_PARAM_VALUE",
                "%%%%FULLSYNCHRO%%%%>Add group : " + silverpeasId);
            report += "adding group " + testedGroup.getName() + "(id:" + specificId + ")\n";
            SynchroReport.warn("admin.checkOutGroups", "ajout groupe "
                + testedGroup.getName() + " (id:" + silverpeasId + ") OK",
                null);
          } else { // le name groupe non renseigné
            SilverTrace.info("admin", "admin.checkOutGroups", "root.MSG_GEN_PARAM_VALUE",
                "%%%%FULLSYNCHRO%%%%>PB Adding Group ! " + specificId);
            report += "problem adding group id : " + specificId + "\n";
          }
        } catch (AdminException aeAdd) {
          SilverTrace.info("admin", "admin.checkOutGroups", "root.MSG_GEN_PARAM_VALUE",
              "%%%%FULLSYNCHRO%%%%>PB Adding Group ! " + specificId, aeAdd);
          report += "problem adding group " + testedGroup.getName()
              + " (id:" + specificId + ") " + aeAdd.getMessage() + "\n";
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
            SynchroReport.info("admin.checkOutGroups",
                "Ajout ou mise à jour de " + cleanSubGroups.length + " groupes fils du groupe "
                + specificId + "...", null);
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
    ArrayList<Group> cleanSubGroups = new ArrayList<Group>();
    //noinspection UnusedAssignment,UnusedAssignment,UnusedAssignment
    for (Group subGroup : subGroups) {
      if (allIncluededGroups.get(subGroup.getSpecificId()) == null) {
        cleanSubGroups.add(subGroup);
      } else {
        SilverTrace.warn("admin", "Admin.removeCrossReferences", "root.MSG_GEN_PARAM_VALUE",
            "Cross removed for child : " + subGroup.getSpecificId() + " of father : " + fatherId);
      }
    }
    return cleanSubGroups.toArray(new Group[cleanSubGroups.size()]);
  }

  // -------------------------------------------------------------------------
  // For SelectionPeas
  // -------------------------------------------------------------------------
  public String[] searchUsersIds(String sGroupId, String componentId, String[] profileIds,
      UserDetail modelUser) throws AdminException {
    try {
      List<String> userIds = new ArrayList<String>();
      if (StringUtil.isDefined(sGroupId)) {
        // search users in group and subgroups
        UserDetail[] users = getAllUsersOfGroup(sGroupId);
        for (UserDetail user :
            users) {
          userIds.add(user.getId());
        }
        if (userIds.isEmpty()) {
          userIds = null;
        }
      } else if (profileIds != null && profileIds.length > 0) {
        // search users in profiles
        for (String profileId : profileIds) {
          ProfileInst profile = profileManager.getProfileInst(domainDriverManager, profileId,
              null);
          // add users directly attach to profile
          userIds.addAll(profile.getAllUsers());

          // add users indirectly attach to profile (groups attached to profile)
          List<String> groupIds = profile.getAllGroups();
          List<String> allGroupIds = new ArrayList<String>();
          for (String groupId :
              groupIds) {
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
        userIds = new ArrayList<String>();
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
    List<String> userIds = new ArrayList<String>();

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
    List<String> userIds = new ArrayList<String>();

    // add users directly attach to profile
    userIds.addAll(profile.getAllUsers());

    // add users indirectly attach to profile (groups attached to profile)
    List<String> groupIds = profile.getAllGroups();
    List<String> allGroupIds = new ArrayList<String>();
    for (String groupId :
        groupIds) {
      allGroupIds.add(groupId);
      allGroupIds.addAll(groupManager.getAllSubGroupIdsRecursively(groupId));
    }
    userIds.addAll(userManager.getAllUserIdsOfGroups(allGroupIds));

    return userIds;
  }

  public String[] searchGroupsIds(boolean isRootGroup, String componentId,
      String[] profileId, Group modelGroup) throws AdminException {
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
  public void resetAllDBConnections(boolean isScheduled) throws AdminException {
    try {
      SilverTrace.info("admin", "Admin.resetAllDBConnections",
          "root.MSG_GEN_ENTER_METHOD",
          "RESET ALL DB CONNECTIONS ! (Scheduled : " + isScheduled + ")");
      OrganizationSchemaPool.releaseConnections();
      ConnectionPool.releaseConnections();
    } catch (Exception e) {
      throw new AdminException("Admin.resetAllDBConnections",
          SilverpeasException.ERROR, "root.EX_CONNECTION_CLOSE_FAILED", e);
    }
  }

  private void rollback() {
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
  private String getSpaceId(SpaceInst spaceInst) {
    if (spaceInst.getId().startsWith(SPACE_KEY_PREFIX)) {
      return spaceInst.getId();
    }
    return SPACE_KEY_PREFIX + spaceInst.getId();
  }

  public void indexAllUsers() throws AdminException {
    Domain[] domains = getAllDomains();
    for (Domain domain :
        domains) {
      try {
        indexUsers(domain.getId());
      } catch (Exception e) {
        SilverTrace.error("admin", "Admin.indexAllUsers", "admin.CANT_INDEX_USERS",
            "domainId = " + domain.getId(), e);
      }
    }
  }

  public void indexUsers(String domainId) throws AdminException {
    try {
      domainDriverManager.indexAllUsers(domainId);
    } catch (Exception e) {
      throw new AdminException("Admin.indexUsers",
          SilverpeasException.ERROR, "admin.CANT_INDEX_USERS", "domainId = " + domainId, e);
    }
  }

  public String copyAndPasteComponent(String componentId, String spaceId, String userId) throws
      AdminException {
    if (!StringUtil.isDefined(spaceId)) {
      // cannot paste component on root
      return null;
    }
    ComponentInst newCompo = (ComponentInst) getComponentInst(componentId).clone();
    SpaceInst destinationSpace = getSpaceInstById(spaceId);

    // Creation
    newCompo.setId("-1");
    newCompo.setDomainFatherId(destinationSpace.getId());
    newCompo.setOrderNum(destinationSpace.getNumComponentInst());
    newCompo.setCreateDate(new Date());
    newCompo.setCreatorUserId(userId);
    newCompo.setLanguage(I18NHelper.defaultLanguage);

    // Rename if componentName already exists in the destination space
    String label =
        renameComponentName(newCompo.getLabel(I18NHelper.defaultLanguage), destinationSpace.
        getAllComponentsInst());
    newCompo.setLabel(label);

    // Delete inherited profiles only
    // It will be processed by admin
    newCompo.removeInheritedProfiles();

    // Add the component
    String sComponentId = addComponentInst(userId, newCompo);

    // Execute specific paste by the component
    try {
      PasteDetail pasteDetail = new PasteDetail(componentId, sComponentId, userId);
      String componentRootName = URLManager.getComponentNameFromComponentId(componentId);
      String className =
          "com.silverpeas.component." + componentRootName + "." + componentRootName.substring(0, 1).
          toUpperCase() + componentRootName.substring(1) + "Paste";
      if (Class.forName(className).getClass() != null) {
        ComponentPasteInterface componentPaste = (ComponentPasteInterface) Class.forName(className).
            newInstance();
        componentPaste.paste(pasteDetail);
      }
    } catch (Exception e) {
      SilverTrace.warn("admin",
          "Admin.copyAndPasteComponent()",
          "root.GEN_EXIT_METHOD", e);
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
    for (int i = 0;
        i < listComponents.size();
        i++) {
      ComponentInst componentInst = listComponents.get(i);
      if (componentInst.getLabel().equals(newComponentLabel)) {
        newComponentLabel = "Copie de " + label;
        return renameComponentName(newComponentLabel, listComponents);
      }
    }
    return newComponentLabel;
  }

  public String copyAndPasteSpace(String spaceId, String toSpaceId, String userId)
      throws AdminException {
    String newSpaceId = null;
    boolean pasteAllowed = StringUtil.isDefined(spaceId);
    if (StringUtil.isDefined(toSpaceId)) {
      // First, check if target space is not a sub space of paste space
      List<SpaceInstLight> path = TreeCache.getSpacePath(toSpaceId);
      for (int i = 0;
          i < path.size() && pasteAllowed;
          i++) {
        pasteAllowed = !spaceId.equalsIgnoreCase(path.get(i).getFullId());
      }
    }
    if (pasteAllowed) {
      // paste space itself
      SpaceInst newSpace = getSpaceInstById(spaceId).clone();
      newSpace.setId("-1");
      List<String> newBrotherIds;
      if (StringUtil.isDefined(toSpaceId)) {
        SpaceInst destinationSpace = getSpaceInstById(toSpaceId);
        newSpace.setDomainFatherId(destinationSpace.getId());
        newBrotherIds = Arrays.asList(destinationSpace.getSubSpaceIds());
      } else {
        newSpace.setDomainFatherId("-1");
        newBrotherIds = Arrays.asList(getAllRootSpaceIds());
      }
      newSpace.setOrderNum(newBrotherIds.size());
      newSpace.setCreateDate(new Date());
      newSpace.setCreatorUserId(userId);
      newSpace.setLanguage(I18NHelper.defaultLanguage);

      // Rename if spaceName already used in the destination space
      List<SpaceInstLight> subSpaces = new ArrayList<SpaceInstLight>();
      for (String subSpaceId :
          newBrotherIds) {
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
      newSpaceId = addSpaceInst(userId, newSpace);

      // paste components of space
      for (ComponentInst component :
          components) {
        copyAndPasteComponent(component.getId(), newSpaceId, userId);
      }

      // paste subspaces
      String[] subSpaceIds = newSpace.getSubSpaceIds();
      for (String subSpaceId :
          subSpaceIds) {
        copyAndPasteSpace(subSpaceId, newSpaceId, userId);
      }
    }
    return newSpaceId;
  }

  private String renameSpace(String label, List<SpaceInstLight> listSpaces) {
    String newSpaceLabel = label;
    for (SpaceInstLight space :
        listSpaces) {
      if (space.getName().equals(newSpaceLabel)) {
        newSpaceLabel = "Copie de " + label;
        return renameSpace(newSpaceLabel, listSpaces);
      }
    }
    return newSpaceLabel;
  }
}