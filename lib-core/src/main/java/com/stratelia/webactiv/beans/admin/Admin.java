/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.beans.admin;

import static com.stratelia.silverpeas.silvertrace.SilverTrace.MODULE_ADMIN;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.containerManager.ContainerManager;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.domains.ldapdriver.LDAPSynchroUserItf;
import com.stratelia.silverpeas.domains.silverpeasdriver.DomainSPSchemaPool;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.SilverpeasSettings;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.cache.AdminCache;
import com.stratelia.webactiv.beans.admin.cache.GroupCache;
import com.stratelia.webactiv.beans.admin.cache.Space;
import com.stratelia.webactiv.beans.admin.cache.TreeCache;
import com.stratelia.webactiv.beans.admin.instance.control.Instanciateur;
import com.stratelia.webactiv.beans.admin.instance.control.SPParameter;
import com.stratelia.webactiv.beans.admin.instance.control.WAComponent;
import com.stratelia.webactiv.beans.admin.spaceTemplates.SpaceInstanciateur;
import com.stratelia.webactiv.beans.admin.spaceTemplates.SpaceTemplate;
import com.stratelia.webactiv.beans.admin.spaceTemplates.SpaceTemplateProfile;
import com.stratelia.webactiv.organization.OrganizationSchemaPool;
import com.stratelia.webactiv.organization.ScheduledDBReset;
import com.stratelia.webactiv.organization.UserRow;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.pool.ConnectionPool;

/**
 * @author neysseri
 *
 */
/**
 * The class Admin is the main class of the Administrator.<BR>
 * The role of the administrator is to create and maintain spaces.
 */
public class Admin extends Object {

  public static final String SPACE_KEY_PREFIX = "WA";
  // Divers
  static private Object m_diffSynchroExmut = new Object();
  static private long m_threadDelay = 900;
  static private boolean m_delUsersOnDiffSynchro = true;
  static private boolean m_bFallbackGroupNames = true;
  static private boolean m_bFallbackUserLogins = false;
  static private String m_groupSynchroCron = "";
  // Helpers
  static private SpaceInstManager m_SpaceInstManager = new SpaceInstManager();
  static private ComponentInstManager m_ComponentInstManager = new ComponentInstManager();
  static private ProfileInstManager m_ProfileInstManager = new ProfileInstManager();
  static private SpaceProfileInstManager m_SpaceProfileInstManager = new SpaceProfileInstManager();
  static private GroupManager m_GroupManager = new GroupManager();
  static private UserManager m_UserManager = new UserManager();
  static private DomainDriverManager m_DDManager = new DomainDriverManager();
  static private ProfiledObjectManager m_ProfiledObjectManager = new ProfiledObjectManager();
  static private GroupProfileInstManager m_GroupProfileInstManager = new GroupProfileInstManager();
  // Component instanciator
  static private Instanciateur m_compoInstanciator = null;
  static private SpaceInstanciateur m_spaceInstanciator = null;
  // Entreprise client space Id
  static private String m_sAdminDBDriver = null;
  static private String m_sWaProductionDb;
  static private String m_sWaProductionUser;
  static private String m_sWaProductionPswd;
  static private int m_nEntrepriseClientSpaceId = 0;
  static private String m_sAdministratorEMail = null;
  static private String m_sDAPIGeneralAdminId = null;
  // User Logs
  static private Hashtable<String, UserLog> m_hUserLog = new Hashtable<String, UserLog>(0);
  private static SimpleDateFormat formatter = new SimpleDateFormat(
      "dd/MM/yyyy HH:mm:ss:S");
  // Cache management
  static private AdminCache m_Cache = new AdminCache();
  // static private TreeCache treeCache = new TreeCache();
  // DB Connections Scheduled Resets
  static private ScheduledDBReset m_DBResetScheduler = null;
  public static final String basketSuffix = " (Restauré)";
  static private SynchroGroupScheduler groupSynchroScheduler = null;
  static private ResourceLocator roleMapping = null;
  static private boolean useProfileInheritance = false;

  /**
   * Admin Constructor
   */
  public Admin() {
    if (m_sAdminDBDriver == null) {
      // Load silverpeas admin resources
      ResourceLocator resources = new ResourceLocator(
          "com.stratelia.webactiv.beans.admin.admin", "");
      roleMapping = new ResourceLocator("com.silverpeas.admin.roleMapping", "");
      useProfileInheritance = resources.getBoolean("UseProfileInheritance",
          false);

      m_sAdminDBDriver = resources.getString("AdminDBDriver");
      m_sWaProductionDb = resources.getString("WaProductionDb");
      m_sWaProductionUser = resources.getString("WaProductionUser");
      m_sWaProductionPswd = resources.getString("WaProductionPswd");

      m_nEntrepriseClientSpaceId = Integer.parseInt(resources.getString("EntrepriseClientSpaceId"));
      m_sAdministratorEMail = resources.getString("AdministratorEMail");
      m_sDAPIGeneralAdminId = resources.getString("DAPIGeneralAdminId");

      if (m_DBResetScheduler == null) {
        m_DBResetScheduler = new ScheduledDBReset();
        m_DBResetScheduler.initialize(resources.getString(
            "DBConnectionResetScheduler", ""));
      }

      m_bFallbackGroupNames = SilverpeasSettings.readBoolean(resources,
          "FallbackGroupNames", true);
      m_bFallbackUserLogins = SilverpeasSettings.readBoolean(resources,
          "FallbackUserLogins", false);
      m_threadDelay = SilverpeasSettings.readLong(resources,
          "AdminThreadedSynchroDelay", 900);
      m_groupSynchroCron = SilverpeasSettings.readString(resources,
          "GroupSynchroCron", "* 5 * * *");
      m_delUsersOnDiffSynchro = SilverpeasSettings.readBoolean(resources,
          "DelUsersOnThreadedSynchro", true);

      // Cache management
      m_Cache.setCacheAvailable(resources.getString("UseCache", "1").equals("1"));

      // Initialize component instanciator
      if (m_compoInstanciator == null) {
        m_compoInstanciator = new Instanciateur();
      }

      if (m_spaceInstanciator == null) {
        m_spaceInstanciator = new SpaceInstanciateur(getAllComponents());
      }
      // Init tree cache
      reloadCache();
    }
  }

  public final void reloadCache() {
    TreeCache.clearCache();
    // Init tree cache
    try {
      SilverTrace.info(MODULE_ADMIN, "admin.startServer",
          "root.MSG_GEN_PARAM_VALUE", "Start filling tree cache...");
      List<SpaceInstLight> spaces = m_SpaceInstManager.getAllSpaces(m_DDManager);
      for (SpaceInstLight space : spaces) {
        addSpaceInTreeCache(space);
      }
      SilverTrace.info(MODULE_ADMIN, "admin.startServer",
          "root.MSG_GEN_PARAM_VALUE", "Tree cache filled !");
    } catch (Exception e) {
      SilverTrace.error("admin", "Constructor", "ERROR_WHEN_INITIALIZING_ADMIN", e);
    }
  }

  // -------------------------------------------------------------------------
  // Start Server actions
  // -------------------------------------------------------------------------
  public void startServer() throws Exception {
    try {
      m_DDManager.startServer(this, m_threadDelay);
    } catch (Exception e) {
      SilverTrace.error(MODULE_ADMIN, "Admin.startServer", "ERROR_WHEN_STARTING_DOMAINS", e);
    }

    // Group[] groups = getRootGroupsOfDomain("-1");
    Group[] groups = getSynchronizedGroups();
    List<String> synchronizedGroupIds = new ArrayList<String>();
    Group group = null;
    for (int g = 0; g < groups.length; g++) {
      group = groups[g];
      if (group.isSynchronized()) {
        synchronizedGroupIds.add(group.getId());
      }
    }

    groupSynchroScheduler = new SynchroGroupScheduler();
    groupSynchroScheduler.initialize(m_groupSynchroCron, this,
        synchronizedGroupIds);
  }

  private void addSpaceInTreeCache(SpaceInstLight space) throws NumberFormatException,
      AdminException {
    Space spaceInCache = new Space();
    spaceInCache.setSpace(space);

    // add sorted components to space's cache
    List<ComponentInstLight> components =
        m_ComponentInstManager.getComponentsInSpace(Integer.parseInt(space.getShortId()));
    spaceInCache.setComponents(components);

    spaceInCache.setSubspaces(getSubSpaces(space.getShortId()));
    TreeCache.addSpace(space.getShortId(), spaceInCache);
  }

  // -------------------------------------------------------------------------
  // SPACE RELATED FUNCTIONS
  // -------------------------------------------------------------------------
  /**
   * Get Enterprise space id
   * @return The general space id
   */
  public String getGeneralSpaceId() {
    return SPACE_KEY_PREFIX + m_nEntrepriseClientSpaceId;
  }

  public void createSpaceIndex(SpaceInst spaceInst) {
    FullIndexEntry indexEntry = null;

    SilverTrace.info(MODULE_ADMIN, "admin.createSpaceIndex",
        "root.MSG_GEN_PARAM_VALUE", "Space Name : " + spaceInst.getName()
        + " Space Id : " + spaceInst.getId());

    if (spaceInst != null) {
      // Index the space
      String spaceId = getSpaceId(spaceInst);
      indexEntry = new FullIndexEntry("Spaces", "Space", spaceId);
      indexEntry.setTitle((String) spaceInst.getName());
      indexEntry.setPreView(spaceInst.getDescription());
      indexEntry.setCreationUser(spaceInst.getCreatorUserId());

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
   * @param userId Id of user who add the space
   * @param spaceInst SpaceInst object containing information about the space to be created
   * @return the created space id
   */
  public String addSpaceInst(String userId, SpaceInst spaceInst)
      throws AdminException {
    Connection connectionProd = null;

    try {
      SilverTrace.info(MODULE_ADMIN, "admin.addSpaceInst",
          "root.MSG_GEN_PARAM_VALUE", "Space Name : " + spaceInst.getName()
          + " NbCompo: " + spaceInst.getNumComponentInst());
      connectionProd = openConnection(m_sWaProductionDb, m_sWaProductionUser,
          m_sWaProductionPswd, false);

      // Open the connections with auto-commit to false
      m_DDManager.startTransaction(false);
      if (!spaceInst.isRoot()) {
        // It's a subspace
        // Convert the client id in driver id
        spaceInst.setDomainFatherId(getDriverSpaceId(spaceInst.getDomainFatherId()));

        if (useProfileInheritance) {
          // inherits profiles from super space
          // set super space profiles to new space
          setSpaceProfilesToSubSpace(spaceInst, null);
        }
      }

      // Create the space instance
      spaceInst.setCreatorUserId(userId);

      String sSpaceInstId = m_SpaceInstManager.createSpaceInst(spaceInst,
          m_DDManager);
      spaceInst.setId(getClientSpaceId(sSpaceInstId));

      // put new space in cache
      m_Cache.opAddSpace(getSpaceInstById(sSpaceInstId, true));

      // Instantiate the components
      ArrayList<ComponentInst> alCompoInst = spaceInst.getAllComponentsInst();
      for (int nI = 0; nI < alCompoInst.size(); nI++) {
        ComponentInst componentInst = alCompoInst.get(nI);
        componentInst.setDomainFatherId(spaceInst.getId());
        addComponentInst(userId, componentInst, false);
      }

      // commit the transactions
      m_DDManager.commit();
      connectionProd.commit();

      addSpaceInTreeCache(getSpaceInstLight(sSpaceInstId));

      // indexation de l'espace
      SilverTrace.info(MODULE_ADMIN, "admin.addSpaceInst",
          "root.MSG_GEN_PARAM_VALUE", "Indexation : spaceInst = "
          + spaceInst.getName());
      createSpaceIndex(spaceInst);

      return spaceInst.getId();
    } catch (Exception e) {
      try {
        // Roll back the transactions
        m_DDManager.rollback();
        connectionProd.rollback();
        m_Cache.resetCache();
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
        m_DDManager.startTransaction(false);
      }

      // Convert the client id in driver id
      String sDriverSpaceId = getDriverSpaceId(sClientSpaceId);

      // Get the space to delete
      SpaceInst spaceInst = getSpaceInstById(sDriverSpaceId, true);

      if (!definitive) {
        // Update the space in tables
        m_SpaceInstManager.sendSpaceToBasket(m_DDManager, sDriverSpaceId,
            spaceInst.getName() + Admin.basketSuffix, sUserId);

        // delete all profiles (space, components and subspaces)
        deleteSpaceProfiles(spaceInst);
      } else {
        // Get all the sub-spaces
        String[] sSubSpaceIds = getAllSubSpaceIds(sClientSpaceId);

        // Delete subspaces
        for (int i = 0; sSubSpaceIds != null && i < sSubSpaceIds.length; i++) {
          deleteSpaceInstById(sUserId, sSubSpaceIds[i], false, true);
        }

        // Delete subspaces already in bin
        List<SpaceInstLight> removedSpaces = getRemovedSpaces();
        for (SpaceInstLight removedSpace : removedSpaces) {
          if (sDriverSpaceId.equals(removedSpace.getFatherId())) {
            deleteSpaceInstById(sUserId, removedSpace.getFullId(), false, true);
          }
        }

        // delete the space profiles instance
        for (int nI = 0; nI < spaceInst.getNumSpaceProfileInst(); nI++) {
          deleteSpaceProfileInst(spaceInst.getSpaceProfileInst(nI).getId(),
              false);
        }

        // Delete the components
        ArrayList<ComponentInst> alCompoInst = spaceInst.getAllComponentsInst();
        for (int nI = 0; nI < alCompoInst.size(); nI++) {
          deleteComponentInst(sUserId, alCompoInst.get(nI).getId(), true, false);
        }

        // Delete the components already in bin
        List<ComponentInstLight> removedComponents = getRemovedComponents();
        for (ComponentInstLight removedComponent : removedComponents) {
          if (sClientSpaceId.equals(removedComponent.getDomainFatherId())) {
            deleteComponentInst(sUserId, removedComponent.getId(), true, false);
          }
        }

        // Delete the space in tables
        m_SpaceInstManager.deleteSpaceInst(spaceInst, m_DDManager);
      }

      if (startNewTransaction) {
        m_DDManager.commit();
      }
      m_Cache.opRemoveSpace(spaceInst);
      TreeCache.removeSpace(sDriverSpaceId);

      // desindexation de l'espace
      deleteSpaceIndex(spaceInst);

      return sClientSpaceId;
    } catch (Exception e) {
      // Roll back the transactions
      if (startNewTransaction) {
        rollback();
      }
      throw new AdminException("Admin.deleteSpaceInstById",
          SilverpeasException.ERROR, "admin.EX_ERR_DELETE_SPACE", "user Id : '"
          + sUserId + "', space Id : '" + sClientSpaceId + "'", e);
    }
  }

  private void deleteSpaceProfiles(SpaceInst spaceInst) throws AdminException {
    // delete the space profiles
    for (int nI = 0; nI < spaceInst.getNumSpaceProfileInst(); nI++) {
      deleteSpaceProfileInst(spaceInst.getSpaceProfileInst(nI).getId(), false);
    }

    // delete the components profiles
    List<ComponentInst> alCompoInst = spaceInst.getAllComponentsInst();
    ComponentInst component;
    for (int nI = 0; nI < alCompoInst.size(); nI++) {
      component = alCompoInst.get(nI);
      for (int p = 0; p < component.getNumProfileInst(); p++) {
        if (!component.getProfileInst(p).isInherited()) {
          deleteProfileInst(component.getProfileInst(p).getId(), false);
        }
      }
    }

    // delete the subspace profiles
    String[] sSubSpaceIds = spaceInst.getSubSpaceIds();
    SpaceInst subSpace;
    for (int i = 0; sSubSpaceIds != null && i < sSubSpaceIds.length; i++) {
      subSpace = getSpaceInstById(sSubSpaceIds[i]);
      deleteSpaceProfiles(subSpace);
    }
  }

  public void restoreSpaceFromBasket(String sClientSpaceId)
      throws AdminException {
    try {
      // Start transaction
      m_DDManager.startTransaction(false);

      // Convert the client id in driver id
      String sDriverSpaceId = getDriverSpaceId(sClientSpaceId);

      // update data in database
      m_SpaceInstManager.removeSpaceFromBasket(m_DDManager, sDriverSpaceId);

      // Get the space and put it in the cache
      SpaceInst spaceInst = getSpaceInstById(sDriverSpaceId, true);

      // set superspace profiles to space
      if (useProfileInheritance && !spaceInst.isInheritanceBlocked()
          && !spaceInst.isRoot()) {
        updateSpaceInheritance(spaceInst, false);
      }

      m_DDManager.commit();

      // indexation de l'espace
      SilverTrace.info(MODULE_ADMIN, "admin.restoreSpaceFromBasket",
          "root.MSG_GEN_PARAM_VALUE", "Indexation : spaceInst = "
          + spaceInst.getName());
      createSpaceIndex(spaceInst);
      // reset space and eventually subspace
      m_Cache.opAddSpace(spaceInst);
    } catch (Exception e) {
      rollback();
      throw new AdminException("Admin.restoreSpaceFromBasket",
          SilverpeasException.ERROR, "admin.EX_ERR_RESTORE_SPACE_FROM_BASKET",
          "spaceId = " + sClientSpaceId);
    }
  }

  /**
   * Get the space instance with the given space id
   * @param sClientSpaceId client space id
   * @return Space information as SpaceInst object
   */
  public SpaceInst getSpaceInstById(String sClientSpaceId)
      throws AdminException {
    try {
      SpaceInst spaceInst = getSpaceInstById(sClientSpaceId, false);

      // Put the client space Id back
      spaceInst.setId(sClientSpaceId);

      // Put the client component Id back
      ArrayList<ComponentInst> alCompoInst = spaceInst.getAllComponentsInst();
      for (int nI = 0; nI < alCompoInst.size(); nI++) {
        String sClientComponentId =
            getClientComponentId((alCompoInst.get(nI)).getId());
        ((ComponentInst) alCompoInst.get(nI)).setId(sClientComponentId);
      }

      // Put the client sub spaces Id back
      String[] asSubSpaceIds = spaceInst.getSubSpaceIds();
      for (int nI = 0; asSubSpaceIds != null && nI < asSubSpaceIds.length; nI++) {
        asSubSpaceIds[nI] = getClientSpaceId(asSubSpaceIds[nI]);
      }
      spaceInst.setSubSpaceIds(asSubSpaceIds);

      return spaceInst;
    } catch (Exception e) {
      throw new AdminException("Admin.getSpaceInstById",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_SPACE", " space Id : '"
          + sClientSpaceId + "'", e);
    }
  }

  /**
   * Get the space instance with the given space id
   * @param sSpaceId client space id
   * @param bDriverSpaceId true is space id is in 'driver' format, false for 'client' format
   * @return Space information as SpaceInst object
   */
  private SpaceInst getSpaceInstById(String sSpaceId, boolean bDriverSpaceId)
      throws AdminException {
    String sDriverSpaceId = "";
    try {
      // Convert the client id in driver id
      if (bDriverSpaceId) {
        sDriverSpaceId = sSpaceId;
      } else {
        sDriverSpaceId = getDriverSpaceId(sSpaceId);
      }

      SpaceInst spaceInst = m_Cache.getSpaceInst(sDriverSpaceId);
      if (spaceInst == null) {
        // Get space instance
        spaceInst = m_SpaceInstManager.getSpaceInstById(m_DDManager,
            sDriverSpaceId);
        // Store the spaceInst in cache
        m_Cache.putSpaceInst(spaceInst);
      }
      return m_SpaceInstManager.copy(spaceInst);
    } catch (Exception e) {
      throw new AdminException("Admin.getSpaceInstById",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_SPACE", " space Id : '"
          + sSpaceId + "'", e);
    }
  }

  public SpaceInst getPersonalSpace(String userId) throws AdminException {
    return m_SpaceInstManager.getPersonalSpace(m_DDManager, userId);
  }

  /**
   * Get all the subspaces Ids available in Silverpeas given a domainFatherId (client id format)
   * @param sDomainFatherId Id of the father space
   * @return an array of String containing the ids of spaces that are child of given space
   */
  public String[] getAllSubSpaceIds(String sDomainFatherId)
      throws AdminException {
    String[] asDriverSpaceIds = null;
    SilverTrace.debug(MODULE_ADMIN, "Admin.getAllSubSpaceIds",
        "root.MSG_GEN_ENTER_METHOD", "father space id: '" + sDomainFatherId
        + "'");

    try {
      // get all sub space ids
      asDriverSpaceIds = m_SpaceInstManager.getAllSubSpaceIds(m_DDManager,
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
   * @param spaceInstNew SpaceInst object containing new information for space to be updated
   * @return the updated space id
   */
  public String updateSpaceInst(SpaceInst spaceInstNew) throws AdminException {
    try {
      SpaceInst oldSpace = getSpaceInstById(spaceInstNew.getId());

      // Open the connections with auto-commit to false
      m_DDManager.startTransaction(false);

      SilverTrace.debug(MODULE_ADMIN, "Admin.updateSpaceInst",
          "root.MSG_GEN_ENTER_METHOD", "Before id: '" + spaceInstNew.getId()
          + "' after Id: " + getDriverSpaceId(spaceInstNew.getId()));
      // Convert the client id in driver id
      spaceInstNew.setId(getDriverSpaceId(spaceInstNew.getId()));

      // Update the space in tables
      m_SpaceInstManager.updateSpaceInst(m_DDManager, spaceInstNew);

      if (useProfileInheritance
          && (oldSpace.isInheritanceBlocked() != spaceInstNew.isInheritanceBlocked())) {
        updateSpaceInheritance(oldSpace, spaceInstNew.isInheritanceBlocked());
      }

      // commit the transactions
      m_DDManager.commit();

      m_Cache.opUpdateSpace(spaceInstNew);

      // indexation de l'espace
      SilverTrace.info(MODULE_ADMIN, "admin.updateSpaceInst",
          "root.MSG_GEN_PARAM_VALUE", "Indexation : spaceInst = "
          + spaceInstNew.getName());
      createSpaceIndex(spaceInstNew);

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
      m_DDManager.startTransaction(false);

      // Update the space in tables
      m_SpaceInstManager.updateSpaceOrder(m_DDManager, sDriverSpaceId, orderNum);

      // commit the transactions
      m_DDManager.commit();

      m_Cache.opUpdateSpace(m_SpaceInstManager.getSpaceInstById(m_DDManager,
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
   * @param spaceId
   * @param inheritanceBlocked
   * @throws AdminException
   */
  private void updateSpaceInheritance(SpaceInst space,
      boolean inheritanceBlocked) throws AdminException {
    try {
      if (inheritanceBlocked) {
        // suppression des droits hérités de l'espace
        List<SpaceProfileInst> inheritedProfiles = space.getInheritedProfiles();

        SpaceProfileInst profile = null;
        for (int i = 0; i < inheritedProfiles.size(); i++) {
          profile = inheritedProfiles.get(i);
          deleteSpaceProfileInst(profile.getId(), false);
        }
      } else {
        // Héritage des droits de l'espace

        // 1 - suppression des droits spécifiques du sous espace
        List<SpaceProfileInst> profiles = space.getProfiles();

        SpaceProfileInst profile = null;
        for (int i = 0; i < profiles.size(); i++) {
          profile = profiles.get(i);
          if (profile != null && !"Manager".equalsIgnoreCase(profile.getName())) {
            deleteSpaceProfileInst(profile.getId(), false);
          }
        }

        if (!space.isRoot()) {
          // 2 - affectation des droits de l'espace au sous espace
          setSpaceProfilesToSubSpace(space, null);

          // profiles = space.getAllSpaceProfilesInst();
          profiles = space.getInheritedProfiles();
          for (int i = 0; i < profiles.size(); i++) {
            profile = profiles.get(i);
            addSpaceProfileInst(profile, null, false);
          }
        }
      }
    } catch (Exception e) {
      rollback();
      throw new AdminException("Admin.updateComponentInst",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_SPACE", "spaceId = "
          + space.getId(), e);
    }
  }

  /*
   * Tests if a space with given space id exists
   * @param sClientSpaceId if of space to be tested
   * @return true if the given space instance name is an existing space
   */
  public boolean isSpaceInstExist(String sClientSpaceId) throws AdminException {
    try {
      return m_SpaceInstManager.isSpaceInstExist(m_DDManager,
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
      String[] asDriverSpaceIds = m_SpaceInstManager.getAllRootSpaceIds(m_DDManager);

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
    String[] asDriverSpaceIds = null;
    SilverTrace.debug(MODULE_ADMIN, "Admin.getAllSpaceIds",
        "root.MSG_GEN_ENTER_METHOD");

    try {
      asDriverSpaceIds = m_SpaceInstManager.getAllSpaceIds(m_DDManager);

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
   * @return a List of SpaceInstLight
   */
  public List<SpaceInstLight> getRemovedSpaces() throws AdminException {
    SilverTrace.debug(MODULE_ADMIN, "Admin.getRemovedSpaces",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return m_SpaceInstManager.getRemovedSpaces(m_DDManager);
    } catch (Exception e) {
      throw new AdminException("Admin.getRemovedSpaces",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_REMOVED_SPACES", e);
    }
  }

  /**
   * Returns all components which has been removed but not definitely deleted
   * @return a List of ComponentInstLight
   */
  public List<ComponentInstLight> getRemovedComponents() throws AdminException {
    SilverTrace.debug(MODULE_ADMIN, "Admin.getRemovedComponents",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return m_ComponentInstManager.getRemovedComponents(m_DDManager);
    } catch (Exception e) {
      throw new AdminException("Admin.getRemovedComponents",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_REMOVED_COMPONENTS", e);
    }
  }

  /**
   * Return the the spaces name corresponding to the given space ids
   */
  public String[] getSpaceNames(String[] asClientSpaceIds)
      throws AdminException {
    if (asClientSpaceIds == null) {
      return new String[0];
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

  public Hashtable<String, SpaceTemplate> getAllSpaceTemplates() {
    return m_spaceInstanciator.getAllSpaceTemplates();
  }

  public SpaceTemplateProfile[] getTemplateProfiles(String templateName) {
    return m_spaceInstanciator.getTemplateProfiles(templateName);
  }

  public SpaceInst getSpaceInstFromTemplate(String templateName) {
    return m_spaceInstanciator.getSpaceToInstanciate(templateName);
  }

  // -------------------------------------------------------------------------
  // COMPONENT RELATED FUNCTIONS
  // -------------------------------------------------------------------------
  /**
   * Return all the components name available in Silverpeas
   */
  public Hashtable<String, String> getAllComponentsNames() throws AdminException {
    SilverTrace.debug(MODULE_ADMIN, "Admin.getAllComponentsNames",
        "root.MSG_GEN_ENTER_METHOD");

    Hashtable<String, String> hComponents = Instanciateur.getAllComponentsNames();

    for (Enumeration<String> e = hComponents.keys(); e.hasMoreElements();) {
      String sKey = e.nextElement();
      String sLabel = hComponents.get(sKey);
      SilverTrace.debug(MODULE_ADMIN, "Admin.getAllComponentsNames",
          "admin.MSG_INFO_COMPONENT_FOUND", sKey + ": " + sLabel);
    }

    return hComponents;
  }

  /**
   * Return all the components of silverpeas read in the xmlComponent directory
   */
  public Hashtable<String, WAComponent> getAllComponents() {
    return Instanciateur.getWAComponents();
  }

  /**
   * return the component Inst corresponding to the given ID
   */
  public ComponentInst getComponentInst(String sClientComponentId)
      throws AdminException {
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
  public ComponentInstLight getComponentInstLight(String sClientComponentId)
      throws AdminException {
    try {
      String sDriverComponentId = getDriverComponentId(sClientComponentId);
      ComponentInstLight componentInst =
          m_ComponentInstManager.getComponentInstLight(m_DDManager, sDriverComponentId);

      return componentInst;
    } catch (Exception e) {
      throw new AdminException("Admin.getComponentInstLight",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_COMPONENT",
          "component Id: '" + sClientComponentId + "'", e);
    }
  }

  /**
   * return the component Inst corresponding to the given ID
   */
  private ComponentInst getComponentInst(String sComponentId,
      boolean bDriverComponentId, String sFatherDriverSpaceId)
      throws AdminException {
    String sDriverComponentId = null;

    try {
      // Converts space id if necessary
      if (bDriverComponentId) {
        sDriverComponentId = sComponentId;
      } else {
        sDriverComponentId = getDriverComponentId(sComponentId);
      }

      // Get the component instance
      ComponentInst componentInst = m_Cache.getComponentInst(sDriverComponentId);
      SilverTrace.info(MODULE_ADMIN, "Admin.getComponentInst",
          "root.MSG_GEN_ENTER_METHOD", "componentInst=" + componentInst
          + " id=" + sDriverComponentId);
      if (componentInst == null) {
        // Get component instance from database
        componentInst = m_ComponentInstManager.getComponentInst(m_DDManager,
            sDriverComponentId, sFatherDriverSpaceId);
        SilverTrace.info(MODULE_ADMIN, "Admin.getComponentInst",
            "root.MSG_GEN_ENTER_METHOD", "componentInst FatherId="
            + componentInst.getDomainFatherId());
        // Store component instance in cache
        m_Cache.putComponentInst(componentInst);
      }
      return m_ComponentInstManager.copy(componentInst);
    } catch (Exception e) {
      throw new AdminException("Admin.getComponentInst",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_COMPONENT",
          "component Id: '" + sComponentId + "'", e);
    }
  }

  /**
   * return the component Inst name (ex.: kmelia) corresponding to the given ID
   */
  public String getComponentInstName(String bDriverComponentId)
      throws AdminException {
    return m_ComponentInstManager.getComponentInstName(m_DDManager,
        bDriverComponentId);
  }

  /**
   * Get the parameters for the given component
   */
  public List<SPParameter> getComponentParameters(String sComponentId) {
    try {
      // Get the component inst corresponding to the given Id
      ComponentInst compoInst = getComponentInst(sComponentId);

      return compoInst.getParameters();
    } catch (Exception e) {
      SilverTrace.error(MODULE_ADMIN, "Admin.getComponentParameters",
          "admin.EX_ERR_GET_COMPONENT_PARAMS", "sComponentId: '" + sComponentId
          + "'", e);
      return new ArrayList<SPParameter>();
    }
  }

  /**
   * Return the value of the parameter for the given component and the given name of parameter
   */
  public String getComponentParameterValue(String sComponentId,
      String parameterName) {
    try {
      // Get the component inst corresponding to the given Id
      ComponentInst compoInst = getComponentInst(sComponentId);
      return compoInst.getParameterValue(parameterName);
    } catch (Exception e) {
      SilverTrace.error(MODULE_ADMIN, "Admin.getComponentParameterValue",
          "admin.EX_ERR_GET_COMPONENT_PARAMS", "sComponentId: '" + sComponentId
          + "'", e);
      return "";
    }
  }

  public void restoreComponentFromBasket(String sComponentId)
      throws AdminException {
    try {
      // Start transaction
      m_DDManager.startTransaction(false);

      // update data in database
      m_ComponentInstManager.restoreComponentFromBasket(m_DDManager,
          getDriverComponentId(sComponentId));

      // Get the component and put it in the cache
      ComponentInst componentInst = getComponentInst(sComponentId);

      if (useProfileInheritance && !componentInst.isInheritanceBlocked()) {
        // inherits profiles from space
        setSpaceProfilesToComponent(componentInst, null);
      }

      createComponentIndex(componentInst);

      m_DDManager.commit();

      m_Cache.opUpdateComponent(componentInst);
      ComponentInstLight component = getComponentInstLight(sComponentId);
      TreeCache.addComponent(getDriverComponentId(sComponentId), component,
          getDriverSpaceId(component.getDomainFatherId()));
    } catch (Exception e) {
      rollback();
      throw new AdminException("Admin.restoreComponentFromBasket",
          SilverpeasException.ERROR,
          "admin.EX_ERR_RESTORE_COMPONENT_FROM_BASKET", "componentId = "
          + sComponentId);
    }
  }

  public void createComponentIndex(ComponentInst componentInst) {
    FullIndexEntry indexEntry = null;

    if (componentInst != null) {
      // Index the component
      SilverTrace.debug(MODULE_ADMIN, "Admin.createComponentIndex",
          "root.MSG_GEN_ENTER_METHOD", "componentInst.getName() = "
          + componentInst.getName() + "' componentInst.getId() = "
          + componentInst.getId() + " componentInst.getLabel() = "
          + componentInst.getLabel());

      String componentId = "";
      if (componentInst.getId().startsWith(componentInst.getName())) {
        componentId = componentInst.getId();
      } else {
        componentId = componentInst.getName().concat(componentInst.getId());
      }
      indexEntry = new FullIndexEntry("Components", "Component", componentId);
      indexEntry.setTitle((String) componentInst.getLabel());
      indexEntry.setPreView(componentInst.getDescription());
      // indexEntry.setCreationDate(componentInst.getCreateDate());
      // indexEntry.setCreationUser();
      indexEntry.setCreationUser(componentInst.getCreatorUserId());
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
    String sDriverComponentId = "";

    try {
      connectionProd = openConnection(m_sWaProductionDb, m_sWaProductionUser,
          m_sWaProductionPswd, false);

      if (startNewTransaction) {
        // Open the connections with auto-commit to false
        m_DDManager.startTransaction(false);
      }

      // Get the father space inst
      SpaceInst spaceInstFather = getSpaceInstById(componentInst.getDomainFatherId());

      // Create the component instance
      sDriverComponentId = m_ComponentInstManager.createComponentInst(componentInst, m_DDManager,
          getDriverSpaceId(spaceInstFather.getId()));

      // Add the component to the space
      spaceInstFather.addComponentInst(componentInst);

      // Put the new Id for client
      componentInst.setId(sDriverComponentId);

      // Instantiate the component
      String componentName = componentInst.getName();
      String componentId = componentName + componentInst.getId();

      String[] asCompoNames = { componentName };
      String[] asCompoIds = { componentId };
      instantiateComponents(sUserId, asCompoIds, asCompoNames, spaceInstFather.getId(),
          connectionProd);

      // !!! Hard coded workaround !!!!!!!
      if (componentName.equals("sources")
          || componentName.equals("documentation")
          || componentName.equals("infoTracker")) {
        // Create the manager objects
        ContainerManager containerManager = new ContainerManager();
        ContentManager contentManager = new ContentManager();

        // Call the register functions
        containerManager.registerNewContainerInstance(connectionProd,
            componentId, "containerPDC", "fileBoxPlus");
        contentManager.registerNewContentInstance(connectionProd, componentId,
            "containerPDC", "fileBoxPlus");
      } else if (componentName.equals("expertLocator")
          || componentName.equals("questionReply")
          || componentName.equals("whitePages")
          || componentName.equals("kmelia") || componentName.equals("survey")
          || componentName.equals("toolbox")
          || componentName.equals("quickinfo")
          || componentName.equals("almanach") || componentName.equals("quizz")
          || componentName.equals("forums")
          || componentName.equals("pollingStation")
          || componentName.equals("bookmark") || componentName.equals("chat")
          || componentName.equals("infoLetter")
          || componentName.equals("webSites")
          || componentName.equals("gallery") || componentName.equals("blog")) {
        // Create the manager objects
        ContainerManager containerManager = new ContainerManager();
        ContentManager contentManager = new ContentManager();

        // Call the register functions
        containerManager.registerNewContainerInstance(connectionProd,
            componentId, "containerPDC", componentName);
        contentManager.registerNewContentInstance(connectionProd, componentId,
            "containerPDC", componentName);
      }

      if (useProfileInheritance) {
        // inherits profiles from space
        setSpaceProfilesToComponent(componentInst, spaceInstFather);
      }

      // commit the transactions
      if (startNewTransaction) {
        m_DDManager.commit();
      }
      connectionProd.commit();
      m_Cache.opAddComponent(componentInst);
      TreeCache.addComponent(sDriverComponentId, getComponentInstLight(componentId),
          getDriverSpaceId(spaceInstFather.getId()));

      // indexation du composant
      createComponentIndex(componentInst);

      return componentId;
    } catch (Exception e) {
      try {
        if (startNewTransaction) {
          m_DDManager.rollback();
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

  public String deleteComponentInst(String sUserId, String sClientComponentId,
      boolean definitive) throws AdminException {
    return deleteComponentInst(sUserId, sClientComponentId, definitive, true);
  }

  /**
   * Delete the given component from Silverpeas
   */
  public String deleteComponentInst(String sUserId, String sClientComponentId,
      boolean definitive, boolean startNewTransaction) throws AdminException {
    Connection connectionProd = null;
    ComponentInst componentInst = null;

    SilverTrace.spy(MODULE_ADMIN, "Admin.deleteComponentInst()", "ACP",
        sClientComponentId, "", sUserId, SilverTrace.SPY_ACTION_DELETE);

    try {
      if (startNewTransaction) {
        // Open the connections with auto-commit to false
        m_DDManager.startTransaction(false);
      }

      // Convert the client id in driver id
      String sDriverComponentId = getDriverComponentId(sClientComponentId);

      // Get the component to delete
      componentInst = getComponentInst(sDriverComponentId, true, null);

      // Get the father id
      String sFatherClientId = componentInst.getDomainFatherId();

      if (!definitive) {
        // delete the profiles instance
        for (int nI = 0; nI < componentInst.getNumProfileInst(); nI++) {
          deleteProfileInst(componentInst.getProfileInst(nI).getId(), false);
        }

        m_ComponentInstManager.sendComponentToBasket(m_DDManager,
            sDriverComponentId, componentInst.getLabel() + Admin.basketSuffix,
            sUserId);
      } else {
        connectionProd = openConnection(m_sWaProductionDb, m_sWaProductionUser,
            m_sWaProductionPswd, false);

        // Uninstantiate the components
        String componentName = componentInst.getName();
        String[] asCompoName = { componentName };
        String[] asCompoId = { sClientComponentId };
        unInstantiateComponents(sUserId, asCompoId, asCompoName,
            getClientSpaceId(sFatherClientId), connectionProd);

        // delete the profiles instance
        for (int nI = 0; nI < componentInst.getNumProfileInst(); nI++) {
          deleteProfileInst(componentInst.getProfileInst(nI).getId(), false);
        }

        // Delete the component
        m_ComponentInstManager.deleteComponentInst(componentInst, m_DDManager);

        // !!! Hard coded workaround !!!!!!!
        if (componentName.equals("sources")
            || componentName.equals("documentation")
            || componentName.equals("infoTracker")) {
          // Create the manager objects
          ContainerManager containerManager = new ContainerManager();
          ContentManager contentManager = new ContentManager();

          // Call the unregister functions
          containerManager.unregisterNewContainerInstance(connectionProd,
              sClientComponentId, "containerPDC", "fileBoxPlus");
          contentManager.unregisterNewContentInstance(connectionProd,
              sClientComponentId, "containerPDC", "fileBoxPlus");
        } else if (componentName.equals("whitePages")
            || componentName.equals("questionReply")
            || componentName.equals("expertLocator")
            || componentName.equals("kmelia") || componentName.equals("survey")
            || componentName.equals("toolbox")
            || componentName.equals("quickinfo")
            || componentName.equals("almanach")
            || componentName.equals("quizz") || componentName.equals("forums")
            || componentName.equals("pollingStation")
            || componentName.equals("bookmark") || componentName.equals("chat")
            || componentName.equals("infoLetter")
            || componentName.equals("webSites")
            || componentName.equals("gallery") || componentName.equals("blog")) {
          // Create the manager objects
          ContainerManager containerManager = new ContainerManager();
          ContentManager contentManager = new ContentManager();

          // Call the unregister functions
          containerManager.unregisterNewContainerInstance(connectionProd,
              sClientComponentId, "containerPDC", componentName);
          contentManager.unregisterNewContentInstance(connectionProd,
              sClientComponentId, "containerPDC", componentName);
        }

        // commit the transactions
        connectionProd.commit();
      }
      if (startNewTransaction) {
        m_DDManager.commit();
      }
      m_Cache.opRemoveComponent(componentInst);
      TreeCache.removeComponent(getDriverSpaceId(sFatherClientId), sDriverComponentId);

      // desindexation du composant
      deleteComponentIndex(componentInst);

      return sClientComponentId;
    } catch (Exception e) {
      try {
        // Roll back the transactions
        if (startNewTransaction) {
          m_DDManager.rollback();
        }
        connectionProd.rollback();
      } catch (Exception e1) {
        SilverTrace.error(MODULE_ADMIN, "Admin.deleteComponentInst",
            "root.EX_ERR_ROLLBACK", e1);
      }
      throw new AdminException("Admin.deleteComponentInst",
          SilverpeasException.ERROR, "admin.EX_ERR_DELETE_COMPONENT",
          "component Id: '" + sClientComponentId + "'", e);
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
      m_DDManager.startTransaction(false);

      // Update the Component in tables
      m_ComponentInstManager.updateComponentOrder(m_DDManager,
          sDriverComponentId, orderNum);

      m_DDManager.commit();

      m_Cache.opUpdateComponent(m_ComponentInstManager.getComponentInst(
          m_DDManager, sDriverComponentId, null));
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
  public String updateComponentInst(ComponentInst componentInstNew)
      throws AdminException {
    try {
      ComponentInst oldComponentInst = getComponentInst(componentInstNew.getId());

      String componentClientId = componentInstNew.getId();

      // Open the connections with auto-commit to false
      m_DDManager.startTransaction(false);

      // Convert the client space Id in driver space Id
      String sDriverComponentId = getDriverComponentId(componentInstNew.getId());
      componentInstNew.setId(sDriverComponentId);

      // Update the components in tables
      m_ComponentInstManager.updateComponentInst(m_DDManager, componentInstNew);

      // Update the inherited rights
      if (useProfileInheritance
          && (oldComponentInst.isInheritanceBlocked() != componentInstNew.isInheritanceBlocked())) {
        updateComponentInheritance(oldComponentInst, componentInstNew.isInheritanceBlocked());
      }

      // commit the transactions
      m_DDManager.commit();

      m_Cache.opUpdateComponent(componentInstNew);

      // put clientId as Id
      componentInstNew.setId(componentClientId);

      // indexation du composant
      createComponentIndex(componentInstNew);

      return componentClientId;
    } catch (Exception e) {
      rollback();
      throw new AdminException("Admin.updateComponentInst",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_COMPONENT",
          "component Id: '" + componentInstNew.getId() + "'", e);
    }
  }

  /**
   * Update the inheritance mode between a component and its space. If inheritanceBlocked is true
   * then all inherited space profiles are removed. If inheritanceBlocked is false then all
   * component profiles are removed and space profiles are inherited.
   * @param componentId
   * @param inheritanceBlocked
   * @throws AdminException
   */
  private void updateComponentInheritance(ComponentInst component,
      boolean inheritanceBlocked) throws AdminException {
    try {
      if (inheritanceBlocked) {
        // suppression des droits hérités de l'espace
        List<ProfileInst> inheritedProfiles = component.getInheritedProfiles();

        ProfileInst profile = null;
        for (int i = 0; i < inheritedProfiles.size(); i++) {
          profile = inheritedProfiles.get(i);
          deleteProfileInst(profile.getId(), false);
        }
      } else {
        // suppression des droits du composant
        List<ProfileInst> profiles = component.getProfiles();

        ProfileInst profile = null;
        for (int i = 0; i < profiles.size(); i++) {
          profile = profiles.get(i);
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
   * @param subSpace the object to set profiles
   * @param space the object to get profiles
   * @throws AdminException
   */
  private void setSpaceProfilesToSubSpace(SpaceInst subSpace, SpaceInst space)
      throws AdminException {
    if (space == null) {
      space = getSpaceInstById(subSpace.getDomainFatherId(), true);
    }

    setSpaceProfileToSubSpace(subSpace, space, SilverpeasRole.admin.toString());
    setSpaceProfileToSubSpace(subSpace, space, SilverpeasRole.publisher.toString());
    setSpaceProfileToSubSpace(subSpace, space, SilverpeasRole.writer.toString());
    setSpaceProfileToSubSpace(subSpace, space, SilverpeasRole.reader.toString());
  }

  /**
   * Set space profile to a subspace. There is no persistance. The subspace object is enriched.
   * @param subSpace the object to set profiles
   * @param space the object to get profiles
   * @param profileName the name of the profile
   * @throws AdminException
   */
  private void setSpaceProfileToSubSpace(SpaceInst subSpace, SpaceInst space,
      String profileName) {
    SpaceProfileInst subSpaceProfile = null;

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
   * @param component the object to set profiles
   * @param space the object to get profiles
   * @throws AdminException
   */
  public void setSpaceProfilesToComponent(ComponentInst component,
      SpaceInst space) throws AdminException {
    WAComponent waComponent = Instanciateur.getWAComponent(component.getName());
    String[] componentRoles = waComponent.getProfilList();

    if (space == null) {
      space = getSpaceInstById(component.getDomainFatherId(), false);
    }

    for (int cr = 0; cr < componentRoles.length; cr++) {
      ProfileInst inheritedProfile = component.getInheritedProfileInst(componentRoles[cr]);

      if (inheritedProfile != null) {
        inheritedProfile.removeAllGroups();
        inheritedProfile.removeAllUsers();
      } else {
        inheritedProfile = new ProfileInst();
        inheritedProfile.setComponentFatherId(component.getId());
        inheritedProfile.setInherited(true);
        inheritedProfile.setName(componentRoles[cr]);
      }

      List<String> spaceRoles = componentRole2SpaceRoles(componentRoles[cr], component.getName());
      String spaceRole = null;
      for (int sr = 0; sr < spaceRoles.size(); sr++) {
        spaceRole = spaceRoles.get(sr);

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

  // NEWD DLE
  /**
   * Move the given component in Silverpeas
   */
  public void moveComponentInst(String spaceId, String componentId,
      String idComponentBefore, ComponentInst[] componentInsts)
      throws AdminException {
    try {
      SilverTrace.info(MODULE_ADMIN, "admin.moveComponentInst",
          "root.MSG_GEN_PARAM_VALUE", "spaceId= " + spaceId + "  componentId="
          + componentId);
      // Convert the client component Id in driver component Id
      String sDriverComponentId = getDriverComponentId(componentId);

      // Convert the client space Id in driver space Id
      String sDriverSpaceId = getDriverSpaceId(spaceId);
      SilverTrace.info(MODULE_ADMIN, "admin.moveComponentInst",
          "root.MSG_GEN_PARAM_VALUE", "sDriverSpaceId= " + sDriverSpaceId
          + "  sDriverComponentId=" + sDriverComponentId);

      ComponentInst componentInst = getComponentInst(componentId);
      String oldSpaceId = componentInst.getDomainFatherId();

      // Open the connections with auto-commit to false
      m_DDManager.startTransaction(false);

      // Update the components in tables
      m_ComponentInstManager.moveComponentInst(m_DDManager, sDriverSpaceId,
          sDriverComponentId);

      // Remove links for the old spaceId
      // String currentSpaceId = getDriverSpaceId(oldSpaceId);
      /*
       * SilverTrace.info(MODULE_ADMIN, "admin.moveComponentInst", "root.MSG_GEN_PARAM_VALUE",
       * "Remove Link space Father= " + currentSpaceId);
       * m_DDManager.organization.userSet.removeUserSetFromUserSet("I", Integer
       * .parseInt(sDriverComponentId), "S", Integer.parseInt(getDriverSpaceId(currentSpaceId)));
       */

      // Add links for the new spaceId
      /*
       * currentSpaceId = getDriverSpaceId(spaceId); SilverTrace.info(MODULE_ADMIN,
       * "admin.moveComponentInst", "root.MSG_GEN_PARAM_VALUE", "Add Link space Father= " +
       * currentSpaceId); m_DDManager.organization.userSet.addUserSetInUserSet("I", Integer
       * .parseInt(sDriverComponentId), "S", Integer.parseInt(getDriverSpaceId(currentSpaceId)));
       */

      // Set new space
      componentInst.setDomainFatherId(getDriverSpaceId(spaceId));

      // Set component in order
      SilverTrace.info(MODULE_ADMIN, "admin.moveComponentInst",
          "root.MSG_GEN_PARAM_VALUE", "Avant setComponentPlace: componentId="
          + componentId + " idComponentBefore=" + idComponentBefore);
      setComponentPlace(componentId, idComponentBefore, componentInsts);

      // Update extraParamPage from Space if necessary
      SilverTrace.info(MODULE_ADMIN, "admin.moveComponentInst",
          "root.MSG_GEN_PARAM_VALUE", "FirstPageExtraParam="
          + getSpaceInstById(oldSpaceId).getFirstPageExtraParam()
          + " oldSpaceId=" + oldSpaceId);
      SpaceInst oldSpaceInst = getSpaceInstById(getDriverSpaceId(oldSpaceId));
      SilverTrace.info(MODULE_ADMIN, "admin.moveComponentInst",
          "root.MSG_GEN_PARAM_VALUE", "oldSpaceInst=" + oldSpaceInst
          + " componentId=" + componentId);
      if (oldSpaceInst.getFirstPageExtraParam() != null) {
        if (oldSpaceInst.getFirstPageExtraParam().equals(componentId)) {
          oldSpaceInst.setFirstPageExtraParam("");
          oldSpaceInst.setFirstPageType(0);
          updateSpaceInst(oldSpaceInst);
        }
      }

      // commit the transactions
      m_DDManager.commit();

      // Remove component from the Cache
      m_Cache.resetSpaceInst();
      m_Cache.resetComponentInst();

      // reset treecache list in old and new spaces
      TreeCache.setComponents(getDriverSpaceId(oldSpaceId), m_ComponentInstManager
          .getComponentsInSpace(Integer.parseInt(getDriverSpaceId(oldSpaceId))));
      TreeCache.setComponents(getDriverSpaceId(spaceId), m_ComponentInstManager
          .getComponentsInSpace(Integer.parseInt(getDriverSpaceId(spaceId))));
    } catch (Exception e) {
      rollback();
      throw new AdminException("Admin.moveComponentInst",
          SilverpeasException.ERROR, "admin.EX_ERR_MOVE_COMPONENT",
          "spaceId = " + spaceId + " component Id: '" + componentId + " ", e);
    }
  }

  public void setComponentPlace(String componentId, String idComponentBefore,
      ComponentInst[] m_BrothersComponents) throws AdminException {
    int orderNum = 0;
    int i = 0;
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
      orderNum++;
    }
  }

  // NEWF DLE
  public String getRequestRouter(String sComponentName) {
    WAComponent wac = Instanciateur.getWAComponent(sComponentName);

    if (wac == null) {
      return "R" + sComponentName;
    } else {
      return wac.getRequestRouter();
    }
  }

  // --------------------------------------------------------------------------------------------------------
  // PROFILE RELATED FUNCTIONS
  // --------------------------------------------------------------------------------------------------------
  /**
   * Get all the profiles name available for the given component
   */
  public String[] getAllProfilesNames(String sComponentName)
      throws AdminException {
    String[] asProfiles = null;

    WAComponent wac = Instanciateur.getWAComponent(sComponentName);
    if (wac != null) {
      asProfiles = wac.getProfilList();
    }

    if (asProfiles != null) {
      return asProfiles;
    } else {
      return new String[0];
    }
  }

  /**
   * Get the profile label from its name
   */
  public String getProfileLabelfromName(String sComponentName,
      String sProfileName) throws AdminException {
    String[] asProfiles = null;
    String[] asProfilesLabel = null;

    WAComponent wac = Instanciateur.getWAComponent(sComponentName);
    if (wac != null) {
      asProfiles = wac.getProfilList();
      asProfilesLabel = wac.getProfilLabelList();
    }

    if (asProfiles != null && asProfilesLabel != null
        && asProfiles.length == asProfilesLabel.length) {
      String sProfileLabel = sProfileName;
      boolean bFound = false;

      for (int nI = 0; nI < asProfiles.length && !bFound; nI++) {
        if (asProfiles[nI].equals(sProfileName)) {
          bFound = true;
          sProfileLabel = asProfilesLabel[nI];
        }
      }
      return sProfileLabel;
    } else {
      return sProfileName;
    }
  }

  /**
   * Get the profile instance corresponding to the given id
   */
  public ProfileInst getProfileInst(String sProfileId) throws AdminException {
    ProfileInst profileInst = m_Cache.getProfileInst(sProfileId);
    if (profileInst == null) {
      // get profile instance from database
      profileInst = m_ProfileInstManager.getProfileInst(m_DDManager,
          sProfileId, null);
      // store profile instance in cache
      m_Cache.putProfileInst(profileInst);
    }
    return profileInst;
  }

  public List<ProfileInst> getProfilesByObject(String objectId, String objectType,
      String componentId) throws AdminException {
    List<ProfileInst> profiles =
        m_ProfiledObjectManager.getProfiles(m_DDManager, Integer.parseInt(objectId), objectType,
        Integer.parseInt(getDriverComponentId(componentId)));

    return profiles;
  }

  public String[] getProfilesByObjectAndUserId(int objectId, String objectType,
      String componentId, String userId) throws AdminException {

    List<String> groups = getAllGroupsOfUser(userId);

    return m_ProfiledObjectManager.getUserProfileNames(objectId, objectType, Integer
        .parseInt(getDriverComponentId(componentId)),
        Integer.parseInt(userId), groups);
  }

  public boolean isObjectAvailable(String componentId, int objectId,
      String objectType, String userId) throws AdminException {
    if (userId == null) {
      return true;
    }
    return getProfilesByObjectAndUserId(objectId, objectType, componentId, userId).length > 0;
  }

  public String addProfileInst(ProfileInst profileInst) throws AdminException {
    return addProfileInst(profileInst, null, true);
  }

  public String addProfileInst(ProfileInst profileInst, String userId)
      throws AdminException {
    return addProfileInst(profileInst, userId, true);
  }

  /**
   * Get the given profile instance from Silverpeas
   */
  private String addProfileInst(ProfileInst profileInst, String userId,
      boolean startNewTransaction) throws AdminException {
    try {
      if (startNewTransaction) {
        // Open the connections with auto-commit to false
        m_DDManager.startTransaction(false);
      }

      // Convert the parent component id
      String sDriverFatherId = getDriverComponentId(profileInst.getComponentFatherId());

      String sProfileId = m_ProfileInstManager.createProfileInst(profileInst,
          m_DDManager, sDriverFatherId);
      profileInst.setId(sProfileId);

      if (profileInst.getObjectId() == -1 || profileInst.getObjectId() == 0) {
        // Get according component
        ComponentInst componentInstFather = getComponentInst(sDriverFatherId,
            true, null);

        // Add the profile to the component
        componentInstFather.addProfileInst(profileInst);

        // Update component to change last modification date
        if (StringUtil.isDefined(userId)) {
          componentInstFather.setUpdaterUserId(userId);
          updateComponentInst(componentInstFather);
        }
      }

      if (startNewTransaction) {
        // commit the transactions
        m_DDManager.commit();
      }

      if (profileInst.getObjectId() == -1 || profileInst.getObjectId() == 0) {
        m_Cache.opAddProfile(m_ProfileInstManager.getProfileInst(m_DDManager,
            sProfileId, null));
      }

      return sProfileId;
    } catch (Exception e) {
      if (startNewTransaction) {
        rollback();
      }

      throw new AdminException("Admin.addProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_ADD_PROFILE",
          "profile name: '" + profileInst.getName() + "'", e);
    }
  }

  public String deleteProfileInst(String sProfileId) throws AdminException {
    return deleteProfileInst(sProfileId, null);
  }

  public String deleteProfileInst(String sProfileId, String userId)
      throws AdminException {
    return deleteProfileInst(sProfileId, userId, true);
  }

  private String deleteProfileInst(String sProfileId,
      boolean startNewTransaction) throws AdminException {
    return deleteProfileInst(sProfileId, null, startNewTransaction);
  }

  /**
   * Delete the given profile from Silverpeas
   */
  private String deleteProfileInst(String sProfileId, String userId,
      boolean startNewTransaction) throws AdminException {
    // Get the Profile to delete
    ProfileInst profileInst = m_ProfileInstManager.getProfileInst(m_DDManager,
        sProfileId, null);

    try {
      if (startNewTransaction) {
        // Open the connections with auto-commit to false
        m_DDManager.startTransaction(false);
      }

      // Delete the Profile in tables
      m_ProfileInstManager.deleteProfileInst(profileInst, m_DDManager);

      // Update component to change last modification date
      if (StringUtil.isDefined(userId)
          && (profileInst.getObjectId() == -1 || profileInst.getObjectId() == 0)) {
        // Get according component
        ComponentInst component = getComponentInst(profileInst.getComponentFatherId(), true, null);

        component.setUpdaterUserId(userId);
        updateComponentInst(component);
      }

      if (startNewTransaction) {
        // commit the transactions
        m_DDManager.commit();
      }

      if (profileInst.getObjectId() == -1 || profileInst.getObjectId() == 0) {
        m_Cache.opRemoveProfile(profileInst);
      }

      return sProfileId;
    } catch (Exception e) {
      if (startNewTransaction) {
        rollback();
      }
      throw new AdminException("Admin.deleteProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_DELETE_PROFILE",
          "profile Id: '" + sProfileId + "'", e);
    }
  }

  public String updateProfileInst(ProfileInst profileInstNew)
      throws AdminException {
    return updateProfileInst(profileInstNew, null, true);
  }

  public String updateProfileInst(ProfileInst profileInstNew, String userId)
      throws AdminException {
    return updateProfileInst(profileInstNew, userId, true);
  }

  /**
   * Update the given profile in Silverpeas
   */
  private String updateProfileInst(ProfileInst profileInstNew, String userId,
      boolean startNewTransaction) throws AdminException {
    if (StringUtil.isDefined(userId)) {
      SilverTrace.spy(MODULE_ADMIN, "Admin.updateProfileInst", "unknown",
          profileInstNew.getComponentFatherId(), profileInstNew.getName(),
          userId, SilverTrace.SPY_ACTION_UPDATE);
    }
    try {
      if (startNewTransaction) {
        // Open the connections with auto-commit to false
        m_DDManager.startTransaction(false);
      }

      // Update the profile in tables
      String sProfileNewId =
          m_ProfileInstManager.updateProfileInst(
          m_ProfileInstManager.getProfileInst(m_DDManager, profileInstNew.getId(), null),
          m_DDManager, profileInstNew);

      // Update component to change last modification date
      if (StringUtil.isDefined(userId)
          && (profileInstNew.getObjectId() == -1 || profileInstNew.getObjectId() == 0)) {
        // Get according component
        ComponentInst component =
            getComponentInst(profileInstNew.getComponentFatherId(), true, null);

        component.setUpdaterUserId(userId);
        updateComponentInst(component);
      }

      if (startNewTransaction) {
        // commit the transactions
        m_DDManager.commit();
      }

      if (profileInstNew.getObjectId() == -1
          || profileInstNew.getObjectId() == 0) {
        m_Cache.opUpdateProfile(m_ProfileInstManager.getProfileInst(
            m_DDManager, sProfileNewId, null));
      }

      return sProfileNewId;
    } catch (Exception e) {
      if (startNewTransaction) {
        rollback();
      }
      throw new AdminException("Admin.updateProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_PROFILE",
          "profile Id: '" + profileInstNew.getId() + "'", e);
    }
  }

  // --------------------------------------------------------------------------------------------------------
  // SPACE PROFILE RELATED FUNCTIONS
  // --------------------------------------------------------------------------------------------------------
  /**
   * Get the space profile instance corresponding to the given ID
   */
  public SpaceProfileInst getSpaceProfileInst(String sSpaceProfileId)
      throws AdminException {
    return m_SpaceProfileInstManager.getSpaceProfileInst(m_DDManager,
        sSpaceProfileId, null);
  }

  public String addSpaceProfileInst(SpaceProfileInst spaceProfileInst,
      String userId) throws AdminException {
    return addSpaceProfileInst(spaceProfileInst, userId, true);
  }

  /**
   * Add the space profile instance from Silverpeas
   */
  private String addSpaceProfileInst(SpaceProfileInst spaceProfileInst,
      String userId, boolean startNewTransaction) throws AdminException {
    try {
      if (startNewTransaction) {
        // Open the connections with auto-commit to false
        m_DDManager.startTransaction(false);
      }

      // Create the space profile instance
      SpaceInst spaceInstFather = getSpaceInstById(spaceProfileInst.getSpaceFatherId(), false);
      String sSpaceProfileId =
          m_SpaceProfileInstManager.createSpaceProfileInst(spaceProfileInst, m_DDManager,
          spaceInstFather.getId());
      spaceProfileInst.setId(sSpaceProfileId);

      if (StringUtil.isDefined(userId)) {
        // Update the last modification date
        spaceInstFather.setUpdaterUserId(userId);
        updateSpaceInst(spaceInstFather);
      }

      // Add the space profile to the space
      spaceInstFather.addSpaceProfileInst(spaceProfileInst);

      if (!spaceProfileInst.isInherited()) {
        // Add inherited users and groups for this role
        SpaceProfileInst inheritedProfile =
            spaceInstFather.getInheritedSpaceProfileInst(spaceProfileInst.getName());
        if (inheritedProfile != null) {
          spaceProfileInst.addGroups(inheritedProfile.getAllGroups());
          spaceProfileInst.addUsers(inheritedProfile.getAllUsers());
        }
      }

      spreadSpaceProfile(spaceInstFather, spaceProfileInst);

      if (startNewTransaction) {
        // commit the transactions
        m_DDManager.commit();
      }

      m_Cache.opAddSpaceProfile(m_SpaceProfileInstManager.getSpaceProfileInst(
          m_DDManager, sSpaceProfileId, null));

      return sSpaceProfileId;
    } catch (Exception e) {
      if (startNewTransaction) {
        rollback();
      }
      throw new AdminException("Admin.addSpaceProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_ADD_SPACE_PROFILE",
          "space profile name: '" + spaceProfileInst.getName() + "'", e);
    }
  }

  public String deleteSpaceProfileInst(String sSpaceProfileId, String userId)
      throws AdminException {
    return deleteSpaceProfileInst(sSpaceProfileId, userId, true);
  }

  private String deleteSpaceProfileInst(String sSpaceProfileId,
      boolean startNewTransaction) throws AdminException {
    return deleteSpaceProfileInst(sSpaceProfileId, null, startNewTransaction);
  }

  /**
   * Delete the given space profile from Silverpeas
   */
  private String deleteSpaceProfileInst(String sSpaceProfileId, String userId,
      boolean startNewTransaction) throws AdminException {
    // Get the SpaceProfile to delete
    SpaceProfileInst spaceProfileInst =
        m_SpaceProfileInstManager.getSpaceProfileInst(m_DDManager, sSpaceProfileId, null);

    try {
      if (startNewTransaction) {
        // Open the connections with auto-commit to false
        m_DDManager.startTransaction(false);
      }

      // Delete the Profile in tables
      m_SpaceProfileInstManager.deleteSpaceProfileInst(spaceProfileInst,
          m_DDManager);

      m_Cache.opRemoveSpaceProfile(spaceProfileInst);

      spaceProfileInst.removeAllGroups();
      spaceProfileInst.removeAllUsers();
      SpaceInst spaceInstFather = getSpaceInstById(spaceProfileInst.getSpaceFatherId(), false);

      if (StringUtil.isDefined(userId)) {
        // Update the last modification date
        spaceInstFather.setUpdaterUserId(userId);
        updateSpaceInst(spaceInstFather);
      }

      if (!spaceProfileInst.isInherited()) {
        // Add inherited users and groups for this role
        SpaceProfileInst inheritedProfile =
            spaceInstFather.getInheritedSpaceProfileInst(spaceProfileInst.getName());
        if (inheritedProfile != null) {
          spaceProfileInst.addGroups(inheritedProfile.getAllGroups());
          spaceProfileInst.addUsers(inheritedProfile.getAllUsers());
        }
      }

      spreadSpaceProfile(spaceInstFather, spaceProfileInst);

      if (startNewTransaction) {
        // commit the transactions
        m_DDManager.commit();
      }

      return sSpaceProfileId;
    } catch (Exception e) {
      if (startNewTransaction) {
        rollback();
      }
      throw new AdminException("Admin.deleteSpaceProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_DELETE_SPACEPROFILE",
          "space profile Id: '" + sSpaceProfileId + "'", e);
    }
  }

  /**
   * Update the given space profile in Silverpeas
   */
  private String updateSpaceProfileInst(SpaceProfileInst spaceProfileInstNew)
      throws AdminException {
    return updateSpaceProfileInst(spaceProfileInstNew, null);
  }

  public String updateSpaceProfileInst(SpaceProfileInst spaceProfileInstNew,
      String userId) throws AdminException {
    try {
      // Open the connections with auto-commit to false
      m_DDManager.startTransaction(false);

      SpaceProfileInst oldSpaceProfile =
          m_SpaceProfileInstManager.getSpaceProfileInst(m_DDManager, spaceProfileInstNew.getId(),
          null);

      // Update the space profile in tables
      String sSpaceProfileNewId =
          m_SpaceProfileInstManager.updateSpaceProfileInst(oldSpaceProfile, m_DDManager,
          spaceProfileInstNew);

      if (!"Manager".equalsIgnoreCase(oldSpaceProfile.getName())) {
        // spread profile into subspaces and components
        SpaceInst spaceInstFather = getSpaceInstById(spaceProfileInstNew.getSpaceFatherId(), false);

        if (StringUtil.isDefined(userId)) {
          // Update the last modification date
          spaceInstFather.setUpdaterUserId(userId);
          updateSpaceInst(spaceInstFather);
        }

        // if (!oldSpaceProfile.isInherited())
        // {
        // Add inherited users and groups for this role
        SpaceProfileInst inheritedProfile =
            spaceInstFather.getInheritedSpaceProfileInst(oldSpaceProfile.getName());
        if (inheritedProfile != null) {
          spaceProfileInstNew.addGroups(inheritedProfile.getAllGroups());
          spaceProfileInstNew.addUsers(inheritedProfile.getAllUsers());
        }
        // }

        spreadSpaceProfile(spaceInstFather, spaceProfileInstNew);
      }

      // commit the transactions
      m_DDManager.commit();

      m_Cache.opUpdateSpaceProfile(m_SpaceProfileInstManager.getSpaceProfileInst(m_DDManager,
          spaceProfileInstNew.getId(), null));

      return sSpaceProfileNewId;
    } catch (Exception e) {
      rollback();
      throw new AdminException("Admin.updateSpaceProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_SPACEPROFILE",
          "space profile Id: '" + spaceProfileInstNew.getId() + "'", e);
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

  private void spreadSpaceProfile(SpaceInst space, SpaceProfileInst spaceProfile)
      throws AdminException {
    SilverTrace.info("admin", "Admin.spreadSpaceProfile",
        "root.MSG_GEN_ENTER_METHOD", "spaceId = " + space.getId()
        + ", profile = " + spaceProfile.getName());

    // update profile in components
    Iterator<ComponentInst> components = space.getAllComponentsInst().iterator();
    while (components.hasNext()) {
      ComponentInst component = components.next();

      if (component != null && !component.isInheritanceBlocked()) {
        String componentRole = spaceRole2ComponentRole(spaceProfile.getName(),
            component.getName());
        if (componentRole != null) {
          ProfileInst inheritedProfile = component.getInheritedProfileInst(componentRole);
          if (inheritedProfile != null) {
            inheritedProfile.removeAllGroups();
            inheritedProfile.removeAllUsers();

            inheritedProfile.addGroups(spaceProfile.getAllGroups());
            inheritedProfile.addUsers(spaceProfile.getAllUsers());

            updateProfileInst(inheritedProfile);

            List<String> profilesToCheck = componentRole2SpaceRoles(componentRole,
                component.getName());
            profilesToCheck.remove(spaceProfile.getName()); // exclude current
            // space profile

            String profileToCheck = null;
            for (int p = 0; p < profilesToCheck.size(); p++) {
              profileToCheck = profilesToCheck.get(p);
              SpaceProfileInst spi = space.getSpaceProfileInst(profileToCheck);
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

            if (inheritedProfile.getNumGroup() > 0
                || inheritedProfile.getNumUser() > 0) {
              addProfileInst(inheritedProfile);
            }
          }
        }
      }
    }

    // update profile in subspaces
    String[] subSpaceIds = space.getSubSpaceIds();
    for (int s = 0; subSpaceIds != null && s < subSpaceIds.length; s++) {
      SpaceInst subSpace = getSpaceInstById(subSpaceIds[s], false);

      if (!subSpace.isInheritanceBlocked()) {
        SpaceProfileInst subSpaceProfile =
            subSpace.getInheritedSpaceProfileInst(spaceProfile.getName());
        if (subSpaceProfile != null) {
          subSpaceProfile.removeAllGroups();
          subSpaceProfile.removeAllUsers();

          subSpaceProfile.addGroups(spaceProfile.getAllGroups());
          subSpaceProfile.addUsers(spaceProfile.getAllUsers());

          updateSpaceProfileInst(subSpaceProfile);
        } else {
          subSpaceProfile = new SpaceProfileInst();

          subSpaceProfile.setName(spaceProfile.getName());
          subSpaceProfile.setInherited(true);
          subSpaceProfile.setSpaceFatherId(subSpace.getId());
          subSpaceProfile.addGroups(spaceProfile.getAllGroups());
          subSpaceProfile.addUsers(spaceProfile.getAllUsers());

          if (subSpaceProfile.getNumGroup() > 0
              || subSpaceProfile.getNumUser() > 0) {
            addSpaceProfileInst(subSpaceProfile, null);
          }
        }

        // spreadSpaceProfile(subSpace, spaceProfile);
      }
    }
  }

  // -------------------------------------------------------------------------
  // GROUP RELATED FUNCTIONS
  // -------------------------------------------------------------------------
  /**
   * Get the group names corresponding to the given group ids
   */
  public String[] getGroupNames(String[] asGroupIds) throws AdminException {
    if (asGroupIds == null) {
      return new String[0];
    }

    String[] asGroupNames = new String[asGroupIds.length];
    for (int nI = 0; nI < asGroupIds.length; nI++) {
      asGroupNames[nI] = getGroupName(asGroupIds[nI]);
    }

    return asGroupNames;
  }

  /**
   * Get the group name corresponding to the given group id
   */
  public String getGroupName(String sGroupId) throws AdminException {
    Group group = getGroup(sGroupId);
    return group.getName();
  }

  /**
   * Get the all the groups ids available in Silverpeas
   */
  public String[] getAllGroupIds() throws AdminException {
    String[] asAllGroupIds = m_GroupManager.getAllGroupIds(m_DDManager);

    return asAllGroupIds;
  }

  /**
   * Tests if group exists in Silverpeas
   * @return true if a group with the given name
   */
  public boolean isGroupExist(String sName) throws AdminException {
    return m_GroupManager.isGroupExist(m_DDManager, sName);
  }

  /**
   * Get group information with the given id
   */
  public Group getGroup(String groupId) throws AdminException {
    return m_GroupManager.getGroup(m_DDManager, groupId);
  }

  public List<String> getPathToGroup(String groupId) throws AdminException {
    return m_GroupManager.getPathToGroup(m_DDManager, groupId);
  }

  /**
   * Get group information with the given group name
   */
  public Group getGroupByNameInDomain(String sGroupName, String sDomainFatherId)
      throws AdminException {
    return m_GroupManager.getGroupByNameInDomain(m_DDManager, sGroupName,
        sDomainFatherId);
  }

  /**
   * Get groups information with the given ids
   */
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

  /**
   * Add the given group in Silverpeas
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
   * Add the given group in Silverpeas
   */
  public String addGroup(Group group, boolean onlyInSilverpeas)
      throws AdminException {
    try {
      // Start transaction
      m_DDManager.startTransaction(false);
      if (group.getDomainId() != null && !onlyInSilverpeas) {
        m_DDManager.startTransaction(group.getDomainId(), false);
      }

      // add group
      String sGroupId = m_GroupManager.addGroup(m_DDManager, group,
          onlyInSilverpeas);
      group.setId(sGroupId);
      // Commit the transaction
      m_DDManager.commit();
      if (group.getDomainId() != null && !onlyInSilverpeas) {
        m_DDManager.commit(group.getDomainId());
      }

      if (group.isSynchronized()) {
        // groupSynchroThread.addGroup(sGroupId);
        groupSynchroScheduler.addGroup(sGroupId);
      }

      m_Cache.opAddGroup(group);

      // return group id
      return sGroupId;
    } catch (Exception e) {
      try {
        // Roll back the transactions
        m_DDManager.rollback();
        if (group.getDomainId() != null && !onlyInSilverpeas) {
          m_DDManager.rollback(group.getDomainId());
        }
      } catch (Exception e1) {
        SilverTrace.error("admin", "Admin.addGroup", "root.EX_ERR_ROLLBACK", e1);
      }
      throw new AdminException("Admin.addGroup", SilverpeasException.ERROR,
          "admin.EX_ERR_ADD_GROUP", "group name: '" + group.getName() + "'", e);
    }
  }

  /**
   * Delete the group with the given Id The delete is apply recursively to the sub-groups
   */
  public String deleteGroupById(String sGroupId) throws AdminException {
    try {
      return deleteGroupById(sGroupId, false);
    } catch (Exception e) {
      throw new AdminException("Admin.deleteGroupById",
          SilverpeasException.ERROR, "admin.EX_ERR_DELETE_GROUP", "group Id: '"
          + sGroupId + "'", e);
    }
  }

  /**
   * Delete the group with the given Id The delete is apply recursively to the sub-groups
   */
  public String deleteGroupById(String sGroupId, boolean onlyInSilverpeas)
      throws AdminException {
    Group group = null;
    try {
      // Get group information
      group = getGroup(sGroupId);
      if (group == null) {
        throw new AdminException("Admin.deleteGroupById",
            SilverpeasException.ERROR, "admin.EX_ERR_GROUP_NOT_FOUND",
            "group Id: '" + sGroupId + "'");
      }

      // Start transaction
      m_DDManager.startTransaction(false);
      if (group.getDomainId() != null && !onlyInSilverpeas) {
        m_DDManager.startTransaction(group.getDomainId(), false);
      }

      // Delete the group
      String sReturnGroupId = m_GroupManager.deleteGroupById(m_DDManager,
          group, onlyInSilverpeas);

      // Commit the transaction
      m_DDManager.commit();
      if (group.getDomainId() != null && !onlyInSilverpeas) {
        m_DDManager.commit(group.getDomainId());
      }

      if (group.isSynchronized()) {
        // groupSynchroThread.removeGroup(sGroupId);
        groupSynchroScheduler.removeGroup(sGroupId);
      }

      m_Cache.opRemoveGroup(group);

      return sReturnGroupId;
    } catch (Exception e) {
      try {
        // Roll back the transactions
        m_DDManager.rollback();
        if (group.getDomainId() != null && !onlyInSilverpeas) {
          m_DDManager.rollback(group.getDomainId());
        }
      } catch (Exception e1) {
        SilverTrace.error("admin", "Admin.deleteGroupById",
            "root.EX_ERR_ROLLBACK", e1);
      }
      throw new AdminException("Admin.deleteGroupById",
          SilverpeasException.ERROR, "admin.EX_ERR_GROUP_NOT_FOUND",
          "group Id: '" + sGroupId + "'", e);
    }
  }

  /**
   * Update the given group in Silverpeas and specific
   */
  public String updateGroup(Group group) throws AdminException {
    try {
      return updateGroup(group, false);
    } catch (Exception e) {
      throw new AdminException("Admin.updateGroup", SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_GROUP", "group name: '" + group.getName() + "'",
          e);
    }
  }

  /**
   * Update the given group in Silverpeas and specific
   */
  public String updateGroup(Group group, boolean onlyInSilverpeas)
      throws AdminException {
    try {
      // Start transaction
      m_DDManager.startTransaction(false);
      if (group.getDomainId() != null && !onlyInSilverpeas) {
        m_DDManager.startTransaction(group.getDomainId(), false);
      }

      // Update group
      String sGroupId = m_GroupManager.updateGroup(m_DDManager, group,
          onlyInSilverpeas);

      // Commit the transaction
      m_DDManager.commit();
      if (group.getDomainId() != null && !onlyInSilverpeas) {
        m_DDManager.commit(group.getDomainId());
      }

      m_Cache.opUpdateGroup(getGroup(sGroupId));

      return sGroupId;
    } catch (Exception e) {
      try {
        // Roll back the transactions
        m_DDManager.rollback();
        if (group.getDomainId() != null && !onlyInSilverpeas) {
          m_DDManager.rollback(group.getDomainId());
        }
      } catch (Exception e1) {
        SilverTrace.error("admin", "Admin.updateGroup", "root.EX_ERR_ROLLBACK",
            e1);
      }
      throw new AdminException("Admin.updateGroup", SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_GROUP", "group name: '" + group.getName() + "'",
          e);
    }
  }

  public void removeUserFromGroup(String sUserId, String sGroupId) throws AdminException {
    try {
      // Start transaction
      m_DDManager.startTransaction(false);

      // Update group
      m_GroupManager.removeUserFromGroup(m_DDManager, sUserId, sGroupId);

      // Commit the transaction
      m_DDManager.commit();

      m_Cache.opUpdateGroup(getGroup(sGroupId));

    } catch (Exception e) {
      try {
        // Roll back the transactions
        m_DDManager.rollback();
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
      m_DDManager.startTransaction(false);

      // Update group
      m_GroupManager.addUserInGroup(m_DDManager, sUserId, sGroupId);

      // Commit the transaction
      m_DDManager.commit();

      m_Cache.opUpdateGroup(getGroup(sGroupId));

    } catch (Exception e) {
      try {
        // Roll back the transactions
        m_DDManager.rollback();
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
    return m_GroupManager.getAdminOrganization(m_DDManager);
  }

  // JCC 25/03/2002 BEGIN
  /**
   * Gets the set of Ids denoting the direct subgroups of a given group
   * @param groupId The ID of the parent group
   * @return the Ids as an array of <code>String</code>.
   */
  public String[] getAllSubGroupIds(String groupId) throws AdminException {
    return m_GroupManager.getAllSubGroupIds(m_DDManager, groupId);
  }

  public String[] getAllSubGroupIdsRecursively(String groupId)
      throws AdminException {
    List<String> groupIds = m_GroupManager.getAllSubGroupIdsRecursively(groupId);
    return groupIds.toArray(new String[groupIds.size()]);
  }

  /**
   * Gets the set of Ids denoting the direct subgroups of a given group
   * @param groupId The ID of the parent group
   * @return the Ids as an array of <code>String</code>.
   */
  public String[] getAllRootGroupIds() throws AdminException {
    return m_GroupManager.getAllRootGroupIds(m_DDManager);
  }

  //
  // --------------------------------------------------------------------------------------------------------
  // GROUP PROFILE RELATED FUNCTIONS
  // --------------------------------------------------------------------------------------------------------
  /**
   * Get the group profile instance corresponding to the given ID
   */
  public GroupProfileInst getGroupProfileInst(String groupId)
      throws AdminException {
    return m_GroupProfileInstManager.getGroupProfileInst(m_DDManager, null,
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
        m_DDManager.startTransaction(false);
      }

      // Create the space profile instance
      Group group = getGroup(groupProfileInst.getGroupId());
      String sProfileId = m_GroupProfileInstManager.createGroupProfileInst(
          groupProfileInst, m_DDManager, group.getId());
      groupProfileInst.setId(sProfileId);

      if (startNewTransaction) {
        // commit the transactions
        m_DDManager.commit();
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
        m_GroupProfileInstManager.getGroupProfileInst(m_DDManager, null, groupId);

    try {
      if (startNewTransaction) {
        // Open the connections with auto-commit to false
        m_DDManager.startTransaction(false);
      }

      // Delete the Profile in tables
      m_GroupProfileInstManager.deleteGroupProfileInst(groupProfileInst,
          m_DDManager);

      // m_Cache.opRemoveSpaceProfile(groupProfileInst);

      if (startNewTransaction) {
        // commit the transactions
        m_DDManager.commit();
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
        m_DDManager.startTransaction(false);

        GroupProfileInst oldSpaceProfile =
            m_GroupProfileInstManager.getGroupProfileInst(m_DDManager, null, groupProfileInstNew
                .getGroupId());

        // Update the group profile in tables
        m_GroupProfileInstManager.updateGroupProfileInst(oldSpaceProfile,
            m_DDManager, groupProfileInstNew);

        m_DDManager.commit();

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

  // -------------------------------------------------------------------------
  // USER RELATED FUNCTIONS
  // -------------------------------------------------------------------------
  /**
   * Get all the users Ids available in Silverpeas
   */
  public String[] getAllUsersIds() throws AdminException {
    return m_UserManager.getAllUsersIds(m_DDManager);
  }

  /**
   * Get the user detail corresponding to the given user Id
   */
  public UserDetail getUserDetail(String sUserId) throws AdminException {
    UserDetail ud = m_Cache.getUserDetail(sUserId);
    if (ud == null) {
      ud = m_UserManager.getUserDetail(m_DDManager, sUserId);
      if (ud != null) {
        m_Cache.putUserDetail(sUserId, ud);
      }
    }
    return ud;
  }

  /**
   * Get the user details corresponding to the given user Ids
   */
  public UserDetail[] getUserDetails(String[] asUserId) throws AdminException {
    if (asUserId == null) {
      return new UserDetail[0];
    }

    UserDetail[] aUserDetail = new UserDetail[asUserId.length];
    for (int nI = 0; nI < asUserId.length; nI++) {
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

  public Hashtable<String, String> getUsersLanguage(List<String> userIds) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.ADMIN_DATASOURCE);
    } catch (UtilException e) {
      throw new AdminException("Admin.getUsersLanguage",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
    Hashtable<String, String> usersLanguage = null;
    try {
      usersLanguage = PersonalizationDAO.getUsersLanguage(con, userIds);
    } catch (SQLException se) {
      throw new AdminException("Admin.getUsersLanguage",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", se);
    } finally {
      closeConnection(con);
    }
    return usersLanguage;
  }

  /**
   * Get the user Id corresponding to Domain/Login
   */
  public String getUserIdByLoginAndDomain(String sLogin, String sDomainId)
      throws AdminException {
    Domain[] theDomains = null;
    String valret = null;

    if (sDomainId == null || sDomainId.length() == 0) {
      try {
        theDomains = m_DDManager.getAllDomains();
      } catch (Exception e) {
        throw new AdminException("Admin.getUserIdByLoginAndDomain",
            SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_BY_LOGIN_DOMAIN",
            "login: '" + sLogin + "', domain id: '" + sDomainId + "'", e);
      }

      for (int i = 0; i < theDomains.length && valret == null; i++) {
        try {
          valret = m_UserManager.getUserIdByLoginAndDomain(m_DDManager, sLogin,
              theDomains[i].getId());
        } catch (Exception e) {
          throw new AdminException("Admin.getUserIdByLoginAndDomain",
              SilverpeasException.ERROR,
              "admin.EX_ERR_GET_USER_BY_LOGIN_DOMAIN", "login: '" + sLogin
              + "', domain id: '" + sDomainId + "'", e);
        }
      }
      if (valret == null) {
        throw new AdminException("Admin.getUserIdByLoginAndDomain",
            SilverpeasException.ERROR, "admin.EX_ERR_USER_NOT_FOUND",
            "login: '" + sLogin + "', in all domains");
      }
    } else {
      valret = m_UserManager.getUserIdByLoginAndDomain(m_DDManager, sLogin,
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
    Hashtable<String, String> userParameters = m_DDManager.authenticate(authenticationKey);
    String login = userParameters.get("login");
    String domainId = userParameters.get("domainId");
    return m_UserManager.getUserIdByLoginAndDomain(m_DDManager, login, domainId);
  }

  /**
   * Get the user corresponding to the given user Id (only infos in cache table)
   */
  public UserFull getUserFull(String sUserId) throws AdminException {
    return m_UserManager.getUserFull(m_DDManager, sUserId);
  }

  public UserFull getUserFull(String domainId, String specificId)
      throws Exception {
    SilverTrace.info("admin", "admin.getUserFull", "root.MSG_GEN_ENTER_METHOD",
        "domainId=" + domainId);
    AbstractDomainDriver synchroDomain = m_DDManager.getDomainDriver(Integer.parseInt(domainId));

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
   * @param userDetail user to add
   * @param addOnlyInSilverpeas true if user must not be added in distant datasource (used by
   * synchronization tools)
   * @return id of created user
   */
  public String addUser(UserDetail userDetail, boolean addOnlyInSilverpeas)
      throws AdminException {
    try {
      // Start transaction
      m_DDManager.startTransaction(false);
      if (userDetail.getDomainId() != null && !addOnlyInSilverpeas) {
        m_DDManager.startTransaction(userDetail.getDomainId(), false);
      }

      // add user
      String sUserId = m_UserManager.addUser(m_DDManager, userDetail,
          addOnlyInSilverpeas);

      // Commit the transaction
      m_DDManager.commit();
      if (userDetail.getDomainId() != null && !addOnlyInSilverpeas) {
        m_DDManager.commit(userDetail.getDomainId());
      }

      m_Cache.opAddUser(m_UserManager.getUserDetail(m_DDManager, sUserId));
      // return group id
      return sUserId;
    } catch (Exception e) {
      try {
        // Roll back the transactions
        m_DDManager.rollback();
        if (userDetail.getDomainId() != null && !addOnlyInSilverpeas) {
          m_DDManager.rollback(userDetail.getDomainId());
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
      if (sUserId.equals(m_sDAPIGeneralAdminId)) {
        SilverTrace.warn("admin", "Admin.deleteUser",
            "admin.MSG_WARN_TRY_TO_DELETE_GENERALADMIN");
        return null;
      } else {
        return deleteUser(sUserId, false);
      }

    } catch (Exception e) {
      throw new AdminException("Admin.deleteUser", SilverpeasException.ERROR,
          "admin.EX_ERR_DELETE_USER", "user id : '" + sUserId + "'", e);
    }
  }

  /**
   * Delete the given user from silverpeas and specific domain
   */
  public String deleteUser(String sUserId, boolean onlyInSilverpeas)
      throws AdminException {
    UserDetail user = null;

    try {
      // Get user information from Silverpeas database only
      user = getUserDetail(sUserId);
      if (user == null) {
        throw new AdminException("Admin.deleteUser", SilverpeasException.ERROR,
            "admin.EX_ERR_USER_NOT_FOUND", "user id : '" + sUserId + "'");
      }

      // Start transaction
      m_DDManager.startTransaction(false);
      if (user.getDomainId() != null && !onlyInSilverpeas) {
        m_DDManager.startTransaction(user.getDomainId(), false);
      }

      // Delete the user
      String sReturnUserId = m_UserManager.deleteUser(m_DDManager, user,
          onlyInSilverpeas);

      // Commit the transaction
      m_DDManager.commit();
      if (user.getDomainId() != null && !onlyInSilverpeas) {
        m_DDManager.commit(user.getDomainId());
      }

      m_Cache.opRemoveUser(user);

      return sReturnUserId;
    } catch (Exception e) {
      try {
        // Roll back the transactions
        m_DDManager.rollback();
        if (user.getDomainId() != null && !onlyInSilverpeas) {
          m_DDManager.rollback(user.getDomainId());
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
      m_DDManager.startTransaction(false);

      // Update user
      String sUserId = m_UserManager.updateUser(m_DDManager, user);

      // Commit the transaction
      m_DDManager.commit();

      m_Cache.opUpdateUser(m_UserManager.getUserDetail(m_DDManager, sUserId));

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
      m_DDManager.startTransaction(false);
      if (user.getDomainId() != null) {
        m_DDManager.startTransaction(user.getDomainId(), false);
      }

      // Update user
      String sUserId = m_UserManager.updateUserFull(m_DDManager, user);

      // Commit the transaction
      m_DDManager.commit();
      if (user.getDomainId() != null) {
        m_DDManager.commit(user.getDomainId());
      }
      m_Cache.opUpdateUser(m_UserManager.getUserDetail(m_DDManager, sUserId));

      return sUserId;
    } catch (Exception e) {
      try {
        // Roll back the transactions
        m_DDManager.rollback();
        if (user.getDomainId() != null) {
          m_DDManager.rollback(user.getDomainId());
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
      for (int nI = 0; nI < asComponentIds.length; nI++) {
        SilverTrace.debug("admin", "Admin.instantiateComponents",
            "root.MSG_GEN_ENTER_METHOD", "spaceid: " + sSpaceId
            + " and component " + asComponentIds[nI]);

        m_compoInstanciator.setConnection(connectionProd);
        m_compoInstanciator.setSpaceId(sSpaceId);
        m_compoInstanciator.setComponentId(asComponentIds[nI]);
        m_compoInstanciator.setUserId(userId);
        m_compoInstanciator.instantiateComponentName(asComponentNames[nI]);
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

    for (int nI = 0; nI < asComponentIds.length; nI++) {
      try {
        SilverTrace.debug("admin", "Admin.instantiateComponents",
            "root.MSG_GEN_ENTER_METHOD", "spaceid: " + sSpaceId
            + " and component " + asComponentIds[nI]);

        m_compoInstanciator.setConnection(connectionProd);
        m_compoInstanciator.setSpaceId(sSpaceId);
        m_compoInstanciator.setComponentId(asComponentIds[nI]);
        m_compoInstanciator.setUserId(userId);
        m_compoInstanciator.unInstantiateComponentName(asComponentNames[nI]);
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
    for (int nI = 0; nI < asDriverSpaceIds.length; nI++) {
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
    for (int nI = 0; nI < cBuf.length && sTableClientId.length() == 0; nI++) {
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
      return m_DDManager.createDomain(theDomain);
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
      return m_DDManager.updateDomain(theDomain);
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
      UserDetail[] toRemoveUDs = m_UserManager.getUsersOfDomain(m_DDManager,
          domainId);
      if (toRemoveUDs != null) {
        for (i = 0; i < toRemoveUDs.length; i++) {
          try {
            deleteUser(toRemoveUDs[i].getId(), false);
          } catch (Exception e) {
            deleteUser(toRemoveUDs[i].getId(), true);
          }
        }
      }
      // Remove all groups
      Group[] toRemoveGroups = m_GroupManager.getGroupsOfDomain(m_DDManager,
          domainId);
      if (toRemoveGroups != null) {
        for (i = 0; i < toRemoveGroups.length; i++) {
          try {
            deleteGroupById(toRemoveGroups[i].getId(), false);
          } catch (Exception e) {
            deleteGroupById(toRemoveGroups[i].getId(), true);
          }
        }
      }
      // Remove the domain
      return m_DDManager.removeDomain(domainId);
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
      return m_DDManager.getAllDomains();
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
      return m_DDManager.getDomain(domainId);
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
        return AbstractDomainDriver.ACTION_MASK_MIXED_GROUPS;
      }
      return m_DDManager.getDomainActions(domainId);
    } catch (Exception e) {
      throw new AdminException("Admin.getDomainActions",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_DOMAIN", "domain Id : '"
          + domainId + "'", e);
    }
  }

  public Group[] getRootGroupsOfDomain(String domainId) throws AdminException {
    try {
      return m_GroupManager.getRootGroupsOfDomain(m_DDManager, domainId);
    } catch (Exception e) {
      throw new AdminException("Admin.getGroupsOfDomain",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_DOMAIN", "domain Id : '"
          + domainId + "'", e);
    }
  }

  public Group[] getSynchronizedGroups() throws AdminException {
    try {
      return m_GroupManager.getSynchronizedGroups(m_DDManager);
    } catch (Exception e) {
      throw new AdminException("Admin.getGroupsOfDomain",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_DOMAIN", e);
    }
  }

  public String[] getRootGroupIdsOfDomain(String domainId)
      throws AdminException {
    try {
      return m_GroupManager.getRootGroupIdsOfDomain(m_DDManager, domainId);
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
      groupIds.addAll(m_GroupManager.getAllSubGroupIdsRecursively(groupId));

      return m_UserManager.getAllUsersOfGroups(groupIds);
    } catch (Exception e) {
      throw new AdminException("Admin.getAllUsersOfGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_DOMAIN", "Group Id : '"
          + groupId + "'", e);
    }
  }

  public UserDetail[] getUsersOfDomain(String domainId) throws AdminException {
    try {
      if (domainId != null && domainId.equals("-1")) {
        return new UserDetail[0];
      }
      return m_UserManager.getUsersOfDomain(m_DDManager, domainId);
    } catch (Exception e) {
      throw new AdminException("Admin.getUsersOfDomain",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_DOMAIN", "domain Id : '"
          + domainId + "'", e);
    }
  }

  public String[] getUserIdsOfDomain(String domainId) throws AdminException {
    try {
      if (domainId != null && domainId.equals("-1")) {
        return new String[0];
      }
      return m_UserManager.getUserIdsOfDomain(m_DDManager, domainId);
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
    String sUserId = null;

    try {
      // Authenticate the given user
      Hashtable<String, String> loginDomain = m_DDManager.authenticate(sKey, removeKey);
      if ((!loginDomain.containsKey("login"))
          || (!loginDomain.containsKey("domainId"))) {
        throw new AdminException("Admin.authenticate",
            SilverpeasException.WARNING, "admin.MSG_ERR_AUTHENTICATE_USER",
            "key : '" + sKey + "'");
      }

      // Get the Silverpeas userId
      String sLogin = loginDomain.get("login");
      String sDomainId = loginDomain.get("domainId");

      AbstractDomainDriver synchroDomain = m_DDManager.getDomainDriver(Integer.parseInt(sDomainId));
      // Get the user Id or import it if the domain accept it
      try {
        sUserId = m_UserManager.getUserIdByLoginAndDomain(m_DDManager, sLogin,
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
      UserLog userLog = m_hUserLog.get(sUserId);
      if (userLog != null) {
        // The user is already logged, remove it
        m_hUserLog.remove(sUserId);
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
      m_hUserLog.put(sUserId, userLog);

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
      return m_GroupManager.getDirectGroupsOfUser(m_DDManager, userId);
    } catch (Exception e) {
      throw new AdminException("Admin.getDirectGroupsIdsOfUser",
          SilverpeasException.ERROR, "admin.EX_ERR_GROUP_NOT_FOUND",
          "user Id : '" + userId + "'", e);
    }
  }

  public UserDetail[] searchUsers(UserDetail modelUser, boolean isAnd)
      throws AdminException {
    try {
      return m_UserManager.searchUsers(m_DDManager, modelUser, isAnd);
    } catch (Exception e) {
      throw new AdminException("Admin.searchUsers", SilverpeasException.ERROR,
          "admin.EX_ERR_USER_NOT_FOUND", e);
    }
  }

  public Group[] searchGroups(Group modelGroup, boolean isAnd)
      throws AdminException {
    try {
      return m_GroupManager.searchGroups(m_DDManager, modelGroup, isAnd);
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
    for (String componentId : componentIds) {
      List<SpaceInstLight> spaces = TreeCache.getComponentPath(componentId);
      for (SpaceInstLight space : spaces) {
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
      String[] directGroupIds = m_GroupManager.getDirectGroupsOfUser(m_DDManager, userId);
      for (int g = 0; g < directGroupIds.length; g++) {
        Group group = m_GroupManager.getGroup(directGroupIds[g], false);
        if (group != null) {
          allGroupsOfUser.add(group.getId());
          while (StringUtil.isDefined(group.getSuperGroupId())) {
            group = m_GroupManager.getGroup(group.getSuperGroupId(), false);
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
    // getting all groups of users
    List<String> allGroupsOfUser = getAllGroupsOfUser(userId);

    return m_ComponentInstManager.getAllowedComponentIds(Integer.parseInt(userId), allGroupsOfUser);
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
      for (int s = 0; s < rootSpaceIds.length; s++) {
        String rootSpaceId = rootSpaceIds[s];
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
      for (SpaceInstLight subspace : subspaces) {
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
    for (int c = 0; !find && c < components.size(); c++) {
      find = componentIds.contains(components.get(c).getId());
    }
    if (find) {
      return true;
    } else {
      if (checkInSubspaces) {
        // check in subspaces
        List<SpaceInstLight> subspaces =
            new ArrayList<SpaceInstLight>(TreeCache.getSubSpaces(spaceId));
        for (int s = 0; !find && s < subspaces.size(); s++) {
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
   * @param userId
   * @param spaceId
   * @return a list of SpaceInstLight
   * @author neysseri
   * @throws AdminException
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
      for (SpaceInstLight subspace : subspaces) {
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
      return m_SpaceInstManager.getSubSpaces(getDriverSpaceId(spaceId));
    } catch (Exception e) {
      throw new AdminException("Admin.getSubSpaces",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_SUBSPACES",
          "spaceId = " + spaceId, e);
    }
  }

  /**
   * Get components of a given space (and subspaces) available to a user.
   * @param userId
   * @param spaceId
   * @return a list of ComponentInstLight
   * @author neysseri
   * @throws AdminException
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
      for (ComponentInstLight component : allComponents) {
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

  public Hashtable<String, SpaceAndChildren> getTreeView(String userId, String spaceId)
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
    Hashtable<String, SpaceAndChildren> spaceTrees = new Hashtable<String, SpaceAndChildren>();
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
      SpaceInstLight child = (SpaceInstLight) it.next();
      String fatherId = SPACE_KEY_PREFIX + child.getFatherId();
      SpaceAndChildren father = spaceTrees.get(fatherId);
      if (father != null) {
        father.addSubSpace(child);
      }
    }

    Iterator<ComponentInstLight> it2 = componentsLight.iterator();
    while (it2.hasNext()) {
      ComponentInstLight child = it2.next();
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
   * @param userId
   * @return an ordered list of SpaceInstLight. Built according a depth-first algorithm.
   * @author neysseri
   * @throws AdminException
   */
  public List<SpaceInstLight> getUserSpaceTreeview(String userId) throws Exception {
    SilverTrace.info("admin", "Admin.getUserSpaceTreeview",
        "root.MSG_GEN_ENTER_METHOD", "user id = " + userId);
    String[] rootSpaceIds = getAllRootSpaceIds(userId);
    String rootSpaceId = null;
    List<SpaceInstLight> treeview = new ArrayList<SpaceInstLight>();
    for (int s = 0; s < rootSpaceIds.length; s++) {
      rootSpaceId = rootSpaceIds[s];
      treeview.add(getSpaceInstLight(getDriverSpaceId(rootSpaceId), 0));

      treeview = getUserSpaceSubTreeview(treeview, userId, rootSpaceId, 1);
    }
    return treeview;
  }

  private List<SpaceInstLight> getUserSpaceSubTreeview(List<SpaceInstLight> treeview,
      String userId,
      String spaceFatherId, int level) throws AdminException {
    SilverTrace.info("admin", "Admin.getUserSpaceSubTreeview",
        "root.MSG_GEN_ENTER_METHOD", "user id = " + userId
        + ", spaceFatherId = " + spaceFatherId + ", level = " + level);

    String[] subSpaceIds = getAllowedSubSpaceIds(userId, spaceFatherId);
    String subSpaceId = null;
    for (int s = 0; s < subSpaceIds.length; s++) {
      subSpaceId = subSpaceIds[s];
      treeview.add(getSpaceInstLight(subSpaceId, level));

      treeview = getUserSpaceSubTreeview(treeview, userId, subSpaceId,
          level + 1);
    }
    return treeview;
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

  private SpaceInstLight getSpaceInstLight(String spaceId, int level)
      throws AdminException {
    SilverTrace.info("admin", "Admin.getSpaceInstLight",
        "root.MSG_GEN_ENTER_METHOD", "spaceId = " + spaceId + ", level = "
        + level);
    SpaceInstLight sil = m_Cache.getSpaceInstLight(spaceId);
    if (sil == null) {
      SpaceInst si = m_Cache.getSpaceInst(spaceId);
      if (si != null) {
        sil = new SpaceInstLight(si);
      } else {
        sil = m_SpaceInstManager.getSpaceInstLightById(m_DDManager, spaceId);
      }
      m_Cache.putSpaceInstLight(sil);
    }
    if (level != -1) {
      sil.setLevel(level);
    }
    return sil;
  }

  /**
   * Get the space instance light (only spaceid, fatherId and name) with the given space id
   * @param sClientSpaceId client space id (as WAxx)
   * @return Space information as SpaceInstLight object
   */
  public SpaceInstLight getSpaceInstLightById(String sClientSpaceId)
      throws AdminException {
    try {
      SpaceInstLight spaceInst = getSpaceInstLight(getDriverSpaceId(sClientSpaceId));
      return spaceInst;
    } catch (Exception e) {
      throw new AdminException("Admin.getSpaceInstLightById",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_SPACE", " space Id : '"
          + sClientSpaceId + "'", e);
    }
  }

  /**
   * Return the higher space according to a subspace (N level compliant)
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
    String[] asManageableSpaceIds = null;
    ArrayList<String> alManageableSpaceIds = new ArrayList<String>();

    try {
      // Get user manageable space ids from database
      List<String> groupIds = new ArrayList<String>();
      groupIds.add(sGroupId);
      List<String> manageableSpaceIds = m_SpaceInstManager.getManageableSpaceIds(null, groupIds);
      asManageableSpaceIds = manageableSpaceIds.toArray(new String[manageableSpaceIds.size()]);

      // Inherits manageability rights for space children
      String[] childSpaceIds = null;
      for (int nI = 0; nI < asManageableSpaceIds.length; nI++) {
        // add manageable space id in result
        if (!alManageableSpaceIds.contains(asManageableSpaceIds[nI])) {
          alManageableSpaceIds.add(asManageableSpaceIds[nI]);
        }

        // calculate manageable space's childs
        childSpaceIds = m_SpaceInstManager.getAllSubSpaceIds(m_DDManager,
            asManageableSpaceIds[nI]);

        // add them in result
        for (int nJ = 0; nJ < childSpaceIds.length; nJ++) {
          if (!alManageableSpaceIds.contains(childSpaceIds[nJ])) {
            alManageableSpaceIds.add(childSpaceIds[nJ]);
          }
        }
      }

      // Put user manageable space ids in cache
      asManageableSpaceIds = (String[]) alManageableSpaceIds.toArray(new String[0]);

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
  public String[] getUserManageableSpaceIds(String sUserId)
      throws AdminException {
    String[] asManageableSpaceIds = null;
    ArrayList<String> alManageableSpaceIds = new ArrayList<String>();

    try {
      // Get user manageable space ids from cache
      asManageableSpaceIds = m_Cache.getManageableSpaceIds(sUserId);
      if (asManageableSpaceIds == null) {
        // Get user manageable space ids from database

        List<String> groupIds = getAllGroupsOfUser(sUserId);
        asManageableSpaceIds = m_UserManager.getManageableSpaceIds(sUserId, groupIds);

        // Inherits manageability rights for space children
        String[] childSpaceIds = null;
        for (int nI = 0; nI < asManageableSpaceIds.length; nI++) {
          // add manageable space id in result
          if (!alManageableSpaceIds.contains(asManageableSpaceIds[nI])) {
            alManageableSpaceIds.add(asManageableSpaceIds[nI]);
          }

          // calculate manageable space's childs
          childSpaceIds = m_SpaceInstManager.getAllSubSpaceIds(m_DDManager,
              asManageableSpaceIds[nI]);

          // add them in result
          for (int nJ = 0; nJ < childSpaceIds.length; nJ++) {
            if (!alManageableSpaceIds.contains(childSpaceIds[nJ])) {
              alManageableSpaceIds.add(childSpaceIds[nJ]);
            }
          }
        }

        // Put user manageable space ids in cache
        asManageableSpaceIds = (String[]) alManageableSpaceIds.toArray(new String[0]);
        m_Cache.putManageableSpaceIds(sUserId, asManageableSpaceIds);
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
      String[] asManageableSpaceIds = m_UserManager.getManageableSpaceIds(sUserId, groupIds);

      // retain only root spaces
      List<String> manageableRootSpaceIds = new ArrayList<String>();
      for (int s = 0; s < asManageableSpaceIds.length; s++) {
        SpaceInstLight space = TreeCache.getSpaceInstLight(asManageableSpaceIds[s]);
        if (space.isRoot()) {
          manageableRootSpaceIds.add(asManageableSpaceIds[s]);
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
   * Get the spaces roots ids manageable by given user Id
   */
  public String[] getUserManageableSubSpaceIds(String sUserId,
      String sParentSpaceId) throws AdminException {
    try {
      // Get user manageable space ids from database
      List<String> groupIds = getAllGroupsOfUser(sUserId);
      String[] asManageableSpaceIds = m_UserManager.getManageableSpaceIds(sUserId, groupIds);

      String parentSpaceId = getDriverSpaceId(sParentSpaceId);

      // retain only sub spaces
      List<String> manageableRootSpaceIds = new ArrayList<String>();
      for (int s = 0; s < asManageableSpaceIds.length; s++) {
        SpaceInstLight space = TreeCache.getSpaceInstLight(asManageableSpaceIds[s]);
        if (parentSpaceId.equals(space.getFatherId())) {
          manageableRootSpaceIds.add(asManageableSpaceIds[s]);
        }
      }
      return manageableRootSpaceIds.toArray(new String[manageableRootSpaceIds.size()]);
    } catch (Exception e) {
      throw new AdminException("Admin.getManageableSubSpaceIds",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_MANAGEABLE_SPACE_IDS", "user Id : '" + sUserId
          + "' Spacez = " + sParentSpaceId, e);
    }
  }

  public List<String> getUserManageableGroupIds(String sUserId) throws AdminException {
    try {
      // get all groups of user
      List<String> groupIds = getAllGroupsOfUser(sUserId);

      return m_GroupManager.getManageableGroupIds(sUserId, groupIds);
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
    String[] asAvailCompoIds = null;

    try {
      // Converts client space id to driver space id
      String spaceId = getDriverSpaceId(sClientSpaceId);

      // Get available component ids from cache
      asAvailCompoIds = m_Cache.getAvailCompoIds(spaceId, sUserId);

      if (asAvailCompoIds == null) {
        // Get available component ids from database
        List<ComponentInstLight> components = getAvailCompoInSpace(sUserId, sClientSpaceId);

        List<String> componentIds = new ArrayList<String>();
        for (ComponentInstLight component : components) {
          componentIds.add(component.getId());
        }

        asAvailCompoIds = componentIds.toArray(new String[componentIds.size()]);

        // Store available component ids in cache
        m_Cache.putAvailCompoIds(spaceId, sUserId, asAvailCompoIds);
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

  /**
   * Get ids of components allowed to user in given space (not in subspaces)
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
      // m_ComponentInstManager.getAvailCompoIdsInSpaceAtRoot(m_DDManager, spaceId, sUserId);
      List<String> groupIds = getAllGroupsOfUser(sUserId);
      List<String> asAvailCompoIds =
          m_ComponentInstManager.getAllowedComponentIds(Integer.parseInt(sUserId), groupIds,
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
   * Get the componentIds allowed for the given user Id in the given space and the componentNameRoot
   * @author dlesimple
   * @param sClientSpaceId
   * @param sUserId
   * @param componentRootName
   * @return ArrayList of componentIds
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
   * Get the component ids allowed for the given user Id in the given space
   */
  public String[] getAvailCompoIds(String sUserId) throws AdminException {
    try {
      List<String> componentIds = getAllowedComponentIds(sUserId);

      return componentIds.toArray(new String[componentIds.size()]);
    } catch (Exception e) {
      throw new AdminException("Admin.getAvailCompoIds",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", "user Id : '"
          + sUserId + "'", e);
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
      for (ComponentInstLight component : components) {
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

    List<String> result = new ArrayList<String>();

    List<String> allowedComponentIds = getAllowedComponentIds(sUserId);
    for (String allowedComponentId : allowedComponentIds) {
      ComponentInstLight componentInst = getComponentInstLight(allowedComponentId);

      if (componentInst.getName().equals(sComponentName)) {
        result.add(componentInst.getId());
      }
    }
    return result.toArray(new String[result.size()]);
  }

  /**
   * gets the available component for a given user
   * @param userId user identifier used to get component
   * @param componentName type of component to retrieve ( for example : kmelia, forums, blog)
   * @return a list of ComponentInstLight object
   * @throws AdminException
   */
  public List<ComponentInstLight> getAvailComponentInstLights(
      String userId, String componentName) throws AdminException {

    List<ComponentInstLight> components = new ArrayList<ComponentInstLight>();
    List<String> allowedComponentIds = getAllowedComponentIds(userId);

    for (String allowedComponentId : allowedComponentIds) {
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
   * @param userId
   * @param componentName the component type (kmelia, gallery...)
   * @return a list of root spaces
   * @throws AdminException
   */
  public List<SpaceInstLight> getRootSpacesContainingComponent(String userId, String componentName)
      throws AdminException {
    List<SpaceInstLight> spaces = new ArrayList<SpaceInstLight>();
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

  /**
   * This method returns all sub spaces which contains at least one allowed component of type
   * componentName in this space or subspaces.
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

    for (ComponentInstLight component : components) {
      List<SpaceInstLight> path = TreeCache.getComponentPath(component.getId());
      for (SpaceInstLight space : path) {
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

      for (ComponentInstLight componentInst : components) {

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

      return (CompoSpace[]) alCompoSpace.toArray(new CompoSpace[0]);
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
          m_ComponentInstManager.getAllCompoIdsByComponentName(m_DDManager, sComponentName);
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
      String[] asProfilesIds = m_Cache.getProfileIds(sUserId);

      if (asProfilesIds == null) {
        // retrieve value from database
        asProfilesIds =
            m_ProfileInstManager.getProfileIdsOfUser(sUserId, getAllGroupsOfUser(sUserId));

        // store values in cache
        if (asProfilesIds != null) {
          m_Cache.putProfileIds(sUserId, asProfilesIds);
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
      return m_ProfileInstManager.getProfileIdsOfGroup(m_DDManager, sGroupId);
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

      for (int nI = 0; nI < asProfileIds.length; nI++) {
        for (int nJ = 0; nJ < componentInst.getNumProfileInst(); nJ++) {
          if (componentInst.getProfileInst(nJ).getId().equals(asProfileIds[nI])) {
            alProfiles.add(componentInst.getProfileInst(nJ).getName());
          }
        }
      }

      return arrayListToString(removeTuples(alProfiles));
    } catch (Exception e) {
      SilverTrace.error("admin", "Admin.getCurrentProfiles",
          "admin.MSG_ERR_GET_CURRENT_PROFILE", e);
      return new String[0];
    }
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

      for (int nI = 0; nI < componentInst.getNumProfileInst(); nI++) {
        ProfileInst profile = componentInst.getProfileInst(nI);
        if (profile != null) {
          if (profile.getName().equals(sProfile) || bAllProfiles) {
            // add direct users
            alUserIds.addAll(profile.getAllUsers());

            // add users of groups
            List<String> groupIds = profile.getAllGroups();
            for (String groupId : groupIds) {
              List<String> subGroupIds = m_GroupManager.getAllSubGroupIdsRecursively(groupId);
              // add current group
              subGroupIds.add(groupId);
              if (subGroupIds != null && subGroupIds.size() > 0) {
                UserDetail[] users = m_UserManager.getAllUsersOfGroups(subGroupIds);
                for (int u = 0; u < users.length; u++) {
                  alUserIds.add(users[u].getId());
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
          "admin.EX_ERR_GET_USERS_FOR_PROFILE_AND_COMPONENT", "profile : '"
          + sProfile + "', space Id: '" + sClientSpaceId
          + "' component Id: '" + sClientComponentId, e);
    }
  }

  /**
   * For use in userPanel : return the direct sub-groups
   */
  public Group[] getAllSubGroups(String parentGroupId) throws AdminException {
    String[] theIds = m_GroupManager.getAllSubGroupIds(m_DDManager,
        parentGroupId);
    return getGroups(theIds);
  }

  /**
   * For use in userPanel : return the users that are direct child of a given group
   */
  public UserDetail[] getFiltredDirectUsers(String sGroupId,
      String sUserLastNameFilter) throws AdminException {
    Group theGroup = getGroup(sGroupId);
    UserDetail currentUser = null;
    ArrayList<UserDetail> matchedUsers;
    int i;
    String upperFilter;
    String[] usersIds = null;

    if (theGroup == null) {
      return new UserDetail[0];
    }
    usersIds = theGroup.getUserIds();
    if ((usersIds == null) || (usersIds.length <= 0)) {
      return new UserDetail[0];
    }
    if ((sUserLastNameFilter == null) || (sUserLastNameFilter.length() <= 0)) {
      return getUserDetails(usersIds);
    }
    upperFilter = sUserLastNameFilter.toUpperCase();
    matchedUsers = new ArrayList<UserDetail>();
    for (i = 0; i < usersIds.length; i++) {
      currentUser = getUserDetail(usersIds[i]);
      if ((currentUser != null)
          && (currentUser.getLastName().toUpperCase().startsWith(upperFilter))) {
        matchedUsers.add(currentUser);
      }
    }
    return (UserDetail[]) matchedUsers.toArray(new UserDetail[0]);
  }

  /**
   * For use in userPanel : return the total number of users recursivly contained in a group
   */
  public int getAllSubUsersNumber(String sGroupId) throws AdminException {
    if (!StringUtil.isDefined(sGroupId)) {
      return m_UserManager.getUserNumber(m_DDManager);
    } else {

      // add users directly in this group
      int nb = m_GroupManager.getNBUsersDirectlyInGroup(sGroupId);

      // add users in sub groups
      List<String> groupIds = m_GroupManager.getAllSubGroupIdsRecursively(sGroupId);
      for (String groupId : groupIds) {
        nb += m_GroupManager.getNBUsersDirectlyInGroup(groupId);
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
        return m_UserManager.getUserNumber(m_DDManager);
      }
      if (domainId.equals("-1")) {
        return 0;
      } else {
        return m_UserManager.getUsersNumberOfDomain(m_DDManager, domainId);
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
    return m_UserManager.getAllAdminIds(m_DDManager, getUserDetail(fromUserId));
  }

  /**
   * Get administrator Email
   * @return String
   */
  public String getAdministratorEmail() {
    return m_sAdministratorEMail;
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
    UserLog[] userLogs = new UserLog[m_hUserLog.size()];

    int nI = 0;
    for (Enumeration<String> e = m_hUserLog.keys(); e.hasMoreElements();) {
      userLogs[nI++] = m_hUserLog.get(e.nextElement());
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
      Class.forName(m_sAdminDBDriver);

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
      connection = null;
      SilverTrace.error("admin", "Admin.closeConnection",
          "root.EX_CONNECTION_CLOSE_FAILED", e);
    }
  }

  // -------------------------------------------------------------------------
  // UTILS
  // -------------------------------------------------------------------------
  private String[] arrayListToString(ArrayList<String> al) {
    if (al == null) {
      return new String[0];
    }

    String[] as = new String[al.size()];
    for (int nI = 0; nI < al.size(); nI++) {
      as[nI] = al.get(nI);
    }

    return as;
  }

  private ArrayList<String> removeTuples(ArrayList<String> al) {
    if (al == null) {
      return new ArrayList<String>();
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
  public String[] getAllSpaceIds(String sUserId) throws Exception {
    return getClientSpaceIds(getUserSpaceIds(sUserId));
  }

  /** Return all the root spaces Id available in webactiv */
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
      for (int nI = 0; nI < alCompoInst.size(); nI++) {
        alCompoIds.add(alCompoInst.get(nI).getId());
      }
    }

    return arrayListToString(alCompoIds);
  }

  /**
   * Return all the componentIds recursively in the subspaces available in webactiv given a space id
   */
  public String[] getAllComponentIdsRecur(String sSpaceId) throws Exception {
    List<ComponentInstLight> components =
        TreeCache.getComponentsInSpaceAndSubspaces(getDriverSpaceId(sSpaceId));

    List<String> componentIds = new ArrayList<String>();
    for (ComponentInstLight component : components) {
      componentIds.add(component.getId());
    }
    return componentIds.toArray(new String[componentIds.size()]);
  }

  /**
   * Return all the components Id recursively in (Space+subspaces, or only subspaces or in
   * Silverpeas) available in silverpeas given a userId and a componentNameRoot
   * @author dlesimple
   * @param sSpaceId
   * @param sUserId
   * @param componentNameRoot
   * @param inCurrentSpace
   * @param inAllSpaces
   * @return Array of componentsIds
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
      for (int i = 0; i < cs.length; i++) {
        alCompoIds.add(cs[i].getComponentId());
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
   * @author dlesimple
   * @param sSpaceId
   * @param sUserId
   * @param componentNameRoot
   * @param inCurrentSpace
   * @return ArrayList of componentsIds
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
        for (int nJ = 0; nJ < componentIds.length; nJ++) {
          ComponentInstLight compo = (ComponentInstLight) getComponentInstLight(componentIds[nJ]);
          if (compo.getName().equals(componentNameRoot)) {
            alCompoIds.add(compo.getId());
          }
        }
      }
    }

    // Get components in sub spaces
    String[] asSubSpaceIds = getAllSubSpaceIds(sSpaceId);
    for (int nI = 0; asSubSpaceIds != null && nI < asSubSpaceIds.length; nI++) {
      SilverTrace.info("admin", "Admin.getAllComponentIdsRecur",
          "root.MSG_GEN_PARAM.VALUE", "Sub spaceId=" + asSubSpaceIds[nI]);
      spaceInst = getSpaceInstById(asSubSpaceIds[nI]);
      String[] componentIds = getAvailCompoIds(spaceInst.getId(), sUserId);

      if (componentIds != null) {
        for (int nJ = 0; nJ < componentIds.length; nJ++) {
          ComponentInstLight compo = (ComponentInstLight) getComponentInstLight(componentIds[nJ]);
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

        m_DDManager.startTransaction(false);

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
                userIds = Arrays.asList(m_UserManager.getAllUsersIds(m_DDManager));
              } else {
                userIds = Arrays.asList(m_UserManager.getUserIdsOfDomain(
                    m_DDManager, domainId));
              }
            } else {
              // All users by access level
              if (domainId == null) {
                userIds =
                    Arrays.asList(m_DDManager.organization.user
                        .getUserIdsByAccessLevel(accessLevel));
              } else {
                userIds =
                    Arrays.asList(m_UserManager.getUserIdsOfDomainAndAccessLevel(m_DDManager,
                    domainId,
                    accessLevel));
              }
            }
          } else if (rule.toLowerCase().startsWith("ds_domain")) {
            // Extracting domain id
            String dId = rule.substring(rule.indexOf("=") + 1).trim();

            // Available only for "domaine mixte"
            if ("-1".equals(domainId)) {
              userIds =
                  Arrays.asList(m_DDManager.organization.user.getUserIdsOfDomain(Integer
                      .parseInt(dId)));
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
            for (int d = 0; d < domains.length; d++) {
              userIds.addAll(getUserIdsBySpecificProperty(domains[d].getId(),
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
        for (int i = 0; userIds != null && i < userIds.size(); i++) {
          String userId = userIds.get(i);
          boolean bFound = false;
          for (int j = 0; j < actualUserIds.length && !bFound; j++) {
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
          m_DDManager.organization.group.addUsersInGroup(
              (String[]) newUsers.toArray(new String[0]), Integer.parseInt(groupId), false);
        }

        // Remove users
        List<String> removedUsers = new ArrayList<String>();
        for (int i = 0; i < actualUserIds.length; i++) {
          String actualUserId = actualUserIds[i];
          boolean bFound = false;
          String userId = null;
          for (int j = 0; userIds != null && j < userIds.size() && !bFound; j++) {
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
          m_DDManager.organization.group.removeUsersFromGroup(
              (String[]) removedUsers.toArray(new String[0]), Integer.parseInt(groupId), false);
        }

        m_DDManager.commit();
      } catch (Exception e) {
        try {
          // Roll back the transactions
          m_DDManager.rollback();
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
    UserDetail[] users = new UserDetail[0];
    AbstractDomainDriver domainDriver = null;
    try {
      domainDriver = m_DDManager.getDomainDriver(iDomainId);
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
    for (int u = 0; users != null && u < users.length; u++) {
      specificIds.add(users[u].getSpecificId());
    }

    // We have to find users according to theirs specificIds
    UserRow[] usersInDomain =
        m_DDManager.organization.user.getUsersBySpecificIds(iDomainId, specificIds);
    List<String> userIds = new ArrayList<String>();
    for (int i = 0; usersInDomain != null && i < usersInDomain.length; i++) {
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
    AbstractDomainDriver synchroDomain;
    String specificId = null;
    String silverpeasId = null;
    UserDetail[] distantUDs;
    UserDetail spUserDetail;
    Group[] distantGroups;
    Group spGroup;
    int nI;
    String fromTimeStamp, toTimeStamp;
    Domain theDomain;
    String timeStampField = null;

    Collection<UserDetail> listUsersUpdate = new ArrayList<UserDetail>();
    Collection<UserDetail> listUsersRemove = new ArrayList<UserDetail>();

    try {
      synchroDomain = m_DDManager.getDomainDriver(Integer.parseInt(domainId));
      if (!synchroDomain.isSynchroInProcess()) {
        synchronized (m_diffSynchroExmut) {
          theDomain = m_DDManager.getDomain(domainId);
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
            for (nI = 0; nI < distantUDs.length; nI++) {
              specificId = distantUDs[nI].getSpecificId();
              silverpeasId = "";
              try {
                silverpeasId = m_UserManager.getUserIdBySpecificIdAndDomainId(
                    m_DDManager, specificId, domainId);
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
                  if (distantUDs[nI].equals(spUserDetail) == false) {
                    m_UserManager.updateUser(m_DDManager, distantUDs[nI]);
                    m_Cache.opUpdateUser(m_UserManager.getUserDetail(m_DDManager, silverpeasId));
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
            for (nI = 0; nI < distantGroups.length; nI++) {
              specificId = distantGroups[nI].getSpecificId();
              silverpeasId = "";
              try {
                silverpeasId =
                    m_GroupManager.getGroupIdBySpecificIdAndDomainId(m_DDManager, specificId,
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

            if (m_delUsersOnDiffSynchro) {
              // Remove deleted users from distant datasource
              // Get all users of the domain from distant datasource
              distantUDs = m_DDManager.getAllUsers(domainId);

              // Get all users of the domain from Silverpeas
              UserDetail[] silverpeasUDs = m_UserManager.getUsersOfDomain(
                  m_DDManager, domainId);

              boolean bFound = false;
              int nbDeletedUsers = 0;
              for (nI = 0; nI < silverpeasUDs.length; nI++) {
                bFound = false;
                specificId = silverpeasUDs[nI].getSpecificId();

                // search for user in distant datasource
                for (int nJ = 0; nJ < distantUDs.length && !bFound; nJ++) {
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
                    m_UserManager.deleteUser(m_DDManager, silverpeasUDs[nI], true);
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
            m_DDManager.updateDomain(theDomain);

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

  private Vector<String> translateGroupIds(String sDomainId, String[] groupSpecificIds,
      boolean recursGroups) throws Exception {
    Vector<String> convertedGroupIds = new Vector<String>();
    String groupId = null;

    for (int i = 0; i < groupSpecificIds.length; i++) {
      try {
        groupId = m_GroupManager.getGroupIdBySpecificIdAndDomainId(m_DDManager,
            groupSpecificIds[i], sDomainId);
      } catch (AdminException e) // The group doesn't exist -> Synchronize him
      {
        groupId = null;
        SilverTrace.warn("admin", "Admin.translateGroupIds",
            "admin.EX_ERR_GROUP_NOT_FOUND", "SpecId=" + groupSpecificIds[i], e);
        if (recursGroups) {
          try {
            groupId = synchronizeImportGroup(sDomainId, groupSpecificIds[i],
                null, true, true);
          } catch (AdminException ex) // The group's synchro failed -> Ignore
          // him
          {
            SilverTrace.warn("admin", "Admin.translateGroupIds",
                "admin.MSG_ERR_SYNCHRONIZE_GROUP", "SpecId="
                + groupSpecificIds[i], ex);
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
    Vector<String> convertedUserIds = new Vector<String>();
    String userId;

    for (int i = 0; i < userSpecificIds.length; i++) {
      try {
        userId = m_UserManager.getUserIdBySpecificIdAndDomainId(m_DDManager,
            userSpecificIds[i], sDomainId);
      } catch (AdminException e) // The user doesn't exist -> Synchronize him
      {
        SilverTrace.warn("admin", "Admin.translateUserIds",
            "admin.EX_ERR_USER_NOT_FOUND", "SpecId=" + userSpecificIds[i], e);
        try {
          userId = synchronizeImportUser(sDomainId, userSpecificIds[i], false);
        } catch (AdminException ex) // The user's synchro failed -> Ignore him
        {
          SilverTrace.warn("admin", "Admin.translateUserIds",
              "admin.MSG_ERR_SYNCHRONIZE_USER", "SpecId=" + userSpecificIds[i],
              ex);
          userId = null;
        }
      }
      if (userId != null) {
        convertedUserIds.add(userId);
      }
    }
    return (String[]) convertedUserIds.toArray(new String[0]);
  }

  /**
   *
   */
  public String synchronizeGroup(String groupId, boolean recurs)
      throws Exception {
    SilverTrace.info("admin", "admin.synchronizeGroup",
        "root.MSG_GEN_ENTER_METHOD", "GroupId=" + groupId);
    Group theGroup = getGroup(groupId);

    if (theGroup.isSynchronized()) {
      synchronizeGroupByRule(groupId, false);
    } else {
      AbstractDomainDriver synchroDomain =
          m_DDManager.getDomainDriver(Integer.parseInt(theGroup.getDomainId()));
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
    AbstractDomainDriver synchroDomain = m_DDManager.getDomainDriver(Integer.parseInt(domainId));
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
    for (i = 0; (i < parentSpecificIds.length) && (parentId == null); i++) {
      try {
        parentId = m_GroupManager.getGroupIdBySpecificIdAndDomainId(
            m_DDManager, parentSpecificIds[i], domainId);
        if ((askedParentId != null) && (askedParentId.length() > 0)
            && (askedParentId.equals(parentId) == false)) { // It is not the
          // matching parent
          parentId = null;
        }
      } catch (AdminException e) // The user doesn't exist -> Synchronize him
      {
        parentId = null;
      }
    }
    if ((parentId == null)
        &&
        ((parentSpecificIds.length > 0) || ((askedParentId != null) && (askedParentId.length() > 0)))) {// We
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
    gr.setUserIds(new String[0]);
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
    AbstractDomainDriver synchroDomain =
        m_DDManager.getDomainDriver(Integer.parseInt(theGroup.getDomainId()));
    synchroDomain.removeGroup(theGroup.getSpecificId());
    return deleteGroupById(groupId, true);
  }

  protected void internalSynchronizeGroup(AbstractDomainDriver synchroDomain,
      Group latestGroup, boolean recurs) throws Exception {
    latestGroup.setUserIds(translateUserIds(latestGroup.getDomainId(),
        latestGroup.getUserIds()));
    updateGroup(latestGroup, true);
    if (recurs) {
      Group[] childs = synchroDomain.getGroups(latestGroup.getSpecificId());
      int i;
      String existingGroupId;
      Group existingGroup;

      for (i = 0; i < childs.length; i++) {
        existingGroupId = null;
        try {
          existingGroupId =
              m_GroupManager.getGroupIdBySpecificIdAndDomainId(m_DDManager, childs[i]
                  .getSpecificId(), latestGroup.getDomainId());
          existingGroup = getGroup(existingGroupId);
          if (existingGroup.getSuperGroupId().equals(latestGroup.getId())) { // Only
            // synchronize
            // the
            // group
            // if
            // latestGroup
            // is
            // his
            // true
            // parent
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
    UserDetail theUserDetail;
    AbstractDomainDriver synchroDomain;
    UserDetail ud;
    String[] incGroupsSpecificId;
    Vector<String> incGroupsId;
    String[] oldGroupsId;
    int i;

    Collection<UserDetail> listUsersUpdate = new ArrayList<UserDetail>();

    SilverTrace.info("admin", "admin.synchronizeUser", "root.MSG_GEN_ENTER_METHOD", "userId="
        + userId);
    try {
      // Start transaction
      m_DDManager.startTransaction(false);

      theUserDetail = getUserDetail(userId);
      synchroDomain = m_DDManager.getDomainDriver(Integer.parseInt(theUserDetail.getDomainId()));
      // Synchronize the user's infos
      ud = synchroDomain.synchroUser(theUserDetail.getSpecificId());
      ud.setId(userId);
      ud.setAccessLevel(theUserDetail.getAccessLevel());
      ud.setDomainId(theUserDetail.getDomainId());
      if (ud.equals(theUserDetail) == false) {
        m_UserManager.updateUser(m_DDManager, ud);
        m_Cache.opUpdateUser(m_UserManager.getUserDetail(m_DDManager, userId));
      }
      // Synchro manuelle : Ajoute ou Met à jour l'utilisateur
      listUsersUpdate.add(ud);

      // Synchronize the user's groups
      incGroupsSpecificId = synchroDomain.getUserMemberGroupIds(theUserDetail.getSpecificId());
      incGroupsId = translateGroupIds(theUserDetail.getDomainId(),
          incGroupsSpecificId, recurs);
      oldGroupsId = m_GroupManager.getDirectGroupsOfUser(m_DDManager, userId);
      for (i = 0; i < oldGroupsId.length; i++) {
        if (incGroupsId.contains(oldGroupsId[i])) { // No changes have to be
          // performed to the group ->
          // Remove it
          incGroupsId.removeElement(oldGroupsId[i]);
        } else {
          Group grpToRemove = m_GroupManager.getGroup(m_DDManager,
              oldGroupsId[i]);
          if (theUserDetail.getDomainId().equals(grpToRemove.getDomainId())) {
            // Remove the user from this group
            m_GroupManager.removeUserFromGroup(m_DDManager, userId,
                oldGroupsId[i]);
            m_Cache.opRemoveUserFromGroup(userId, oldGroupsId[i]);
          }
        }
      }
      // Now the remaining groups of the vector are the groups where the user is
      // newly added
      for (i = 0; i < incGroupsId.size(); i++) {
        m_GroupManager.addUserInGroup(m_DDManager, userId, incGroupsId.elementAt(i));
        m_Cache.opAddUserInGroup(userId, incGroupsId.elementAt(i));
      }

      // traitement spécifique des users selon l'interface implémentée
      processSpecificSynchronization(theUserDetail.getDomainId(), null, listUsersUpdate, null);

      // Commit the transaction
      m_DDManager.commit();

      // return user id
      return userId;
    } catch (Exception e) {
      rollback();
      throw new AdminException("Admin.synchronizeUser",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_USER", "user id : '"
          + userId + "'", e);
    }
  }

  /**
   * Synchronize Users and groups between cache and domain's datastore
   */
  public String synchronizeImportUserByLogin(String domainId, String userLogin,
      boolean recurs) throws Exception {
    SilverTrace.info("admin", "admin.synchronizeImportUserByLogin",
        "root.MSG_GEN_ENTER_METHOD", "userLogin=" + userLogin);
    AbstractDomainDriver synchroDomain = m_DDManager.getDomainDriver(Integer.parseInt(domainId));
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
  public String synchronizeImportUser(String domainId, String specificId,
      boolean recurs) throws Exception {
    SilverTrace.info("admin", "admin.synchronizeImportUser",
        "root.MSG_GEN_ENTER_METHOD", "specificId=" + specificId);
    AbstractDomainDriver synchroDomain = m_DDManager.getDomainDriver(Integer.parseInt(domainId));
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
    AbstractDomainDriver synchroDomain = m_DDManager.getDomainDriver(Integer.parseInt(domainId));

    return synchroDomain.getPropertiesToImport(language);
  }

  public UserDetail[] searchUsers(String domainId, Hashtable<String, String> query)
      throws Exception {
    SilverTrace.info("admin", "admin.searchUsers", "root.MSG_GEN_ENTER_METHOD",
        "domainId=" + domainId);
    AbstractDomainDriver synchroDomain = m_DDManager.getDomainDriver(Integer.parseInt(domainId));

    return synchroDomain.getUsersByQuery(query);
  }

  /**
   * Synchronize Users and groups between cache and domain's datastore
   */
  public String synchronizeRemoveUser(String userId) throws Exception {
    SilverTrace.info("admin", "admin.synchronizeRemoveUser",
        "root.MSG_GEN_ENTER_METHOD", "userId=" + userId);
    UserDetail theUserDetail = getUserDetail(userId);
    AbstractDomainDriver synchroDomain =
        m_DDManager.getDomainDriver(Integer.parseInt(theUserDetail.getDomainId()));
    synchroDomain.removeUser(theUserDetail.getSpecificId());
    deleteUser(userId, true);
    List<UserDetail> listUsersRemove = new ArrayList<UserDetail>();
    listUsersRemove.add(theUserDetail);

    // traitement spécifique des users selon l'interface implémentée
    processSpecificSynchronization(theUserDetail.getDomainId(), null, null, listUsersRemove);

    return userId;
  }

  /**
   * Synchronize Users and groups between cache and domain's datastore
   */
  public String synchronizeSilverpeasWithDomain(String sDomainId)
      throws Exception {
    String sReport = "Starting synchronization...\n\n";
    Hashtable<String, String> userIds = new Hashtable<String, String>();
    String sDomainSpecificErrors = null;
    String fromTimeStamp, toTimeStamp;
    Domain theDomain;
    AbstractDomainDriver synchroDomain;

    synchronized (m_diffSynchroExmut) {
      SilverTrace.info("admin", "admin.synchronizeSilverpeasWithDomain",
          "root.MSG_GEN_ENTER_METHOD", "domainID=" + sDomainId);
      // Démarrage de la synchro avec la Popup d'affichage
      SynchroReport.startSynchro();
      // SynchroReport.warn("admin.synchronizeSilverpeasWithDomain",
      // "Début de synchronisation...",null);
      try {
        SynchroReport.warn("admin.synchronizeSilverpeasWithDomain",
            "Domaine : " + m_DDManager.getDomain(sDomainId).getName()
            + ", ID : " + sDomainId, null);
        // Start synchronization
        m_DDManager.beginSynchronization(sDomainId);

        synchroDomain = m_DDManager.getDomainDriver(Integer.parseInt(sDomainId));
        theDomain = m_DDManager.getDomain(sDomainId);
        fromTimeStamp = theDomain.getTheTimeStamp();
        toTimeStamp = synchroDomain.getTimeStamp(fromTimeStamp);
        SilverTrace.info("admin", "admin.synchronizeSilverpeasWithDomain",
            "root.MSG_GEN_ENTER_METHOD", "TimeStamps from " + fromTimeStamp
            + " to " + toTimeStamp);

        // Start transaction
        m_DDManager.startTransaction(false);
        m_DDManager.startTransaction(sDomainId, false);

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
        m_DDManager.updateDomain(theDomain);

        // Commit the transaction
        m_DDManager.commit();
        m_DDManager.commit(sDomainId);

        // End synchronization
        sDomainSpecificErrors = m_DDManager.endSynchronization(sDomainId, false);
        SynchroReport.warn("admin.synchronizeSilverpeasWithDomain",
            "----------------" + sDomainSpecificErrors, null);
        // return group id
        return sReport + "\n----------------\n" + sDomainSpecificErrors;
      } catch (Exception e) {
        try {
          // End synchronization
          m_DDManager.endSynchronization(sDomainId, true);
          // Roll back the transactions
          m_DDManager.rollback();
          m_DDManager.rollback(sDomainId);
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
        m_Cache.resetCache();
      }
    }
  }

  /**
   * Synchronize users between cache and domain's datastore
   */
  private String synchronizeUsers(String domainId, Hashtable<String, String> userIds)
      throws Exception {
    boolean bFound = false;
    String specificId = null;
    String silverpeasId = null;
    String sReport = "User synchronization : \n";
    int iNbUsersAdded, iNbUsersMaj, iNbUsersDeleted;
    iNbUsersAdded = iNbUsersMaj = iNbUsersDeleted = 0;

    Collection<UserDetail> listUsersCreate = new ArrayList<UserDetail>();
    Collection<UserDetail> listUsersUpdate = new ArrayList<UserDetail>();
    Collection<UserDetail> listUsersRemove = new ArrayList<UserDetail>();

    SynchroReport.warn("admin.synchronizeUsers", "SYNCHRONISATION UTILISATEURS :", null);
    try {
      // Clear conversion table
      userIds.clear();

      // Get all users of the domain from distant datasource
      UserDetail[] distantUDs = m_DDManager.getAllUsers(domainId);

      // Get all users of the domain from Silverpeas
      UserDetail[] silverpeasUDs = m_UserManager.getUsersOfDomain(m_DDManager,
          domainId);

      SynchroReport.info("admin.synchronizeUsers",
          "AJOUT ou MISE A JOUR des utilisateurs dans la base...", null);
      SynchroReport.info("admin.synchronizeUsers",
          "Valeur du paramètre 'm_bFallbackUserLogins' = "
          + m_bFallbackUserLogins, null);

      // Add new users or update existing ones from distant datasource
      for (int nI = 0; nI < distantUDs.length; nI++) {
        bFound = false;
        specificId = distantUDs[nI].getSpecificId();
        SilverTrace.info("admin", "admin.synchronizeUsers",
            "root.MSG_GEN_PARAM_VALUE", "%%%%FULLSYNCHRO%%%%>Deal with user : "
            + specificId);

        // search for user in Silverpeas database
        for (int nJ = 0; nJ < silverpeasUDs.length && !bFound; nJ++) {
          if (silverpeasUDs[nJ].getSpecificId().equals(specificId)
              || (m_bFallbackUserLogins && silverpeasUDs[nJ].getLogin().equals(
              distantUDs[nI].getLogin()))) {
            bFound = true;
            distantUDs[nI].setId(silverpeasUDs[nJ].getId());
            distantUDs[nI].setAccessLevel(silverpeasUDs[nJ].getAccessLevel());
            userIds.put(specificId, silverpeasUDs[nJ].getId());
          }
        }

        distantUDs[nI].setDomainId(domainId);
        // if found, update, else create
        if (bFound)// MAJ
        {
          try {
            SilverTrace.info("admin", "admin.synchronizeUsers",
                "root.MSG_GEN_PARAM_VALUE",
                "%%%%FULLSYNCHRO%%%%>Update User : " + distantUDs[nI].getId());
            silverpeasId = m_UserManager.updateUser(m_DDManager, distantUDs[nI]);
            listUsersUpdate.add(distantUDs[nI]);
            iNbUsersMaj++;
            SynchroReport.warn("admin.synchronizeUsers", "maj utilisateur "
                + distantUDs[nI].getFirstName() + " "
                + distantUDs[nI].getLastName() + " (id:" + silverpeasId
                + " / specificId:" + specificId + ") OK", null);
            sReport += "updating user " + distantUDs[nI].getFirstName() + " "
                + distantUDs[nI].getLastName() + "(id:" + specificId + ")\n";
          } catch (AdminException aeMaj) {
            SilverTrace.info("admin", "admin.synchronizeUsers",
                "root.MSG_GEN_PARAM_VALUE",
                "%%%%FULLSYNCHRO%%%%>PB Updating User ! " + specificId, aeMaj);
            sReport += "problem updating user " + distantUDs[nI].getFirstName()
                + " " + distantUDs[nI].getLastName() + " (specificId:"
                + specificId + ") - " + aeMaj.getMessage() + "\n";
            sReport += "user has not been updated\n";
          }
        } else// AJOUT
        {
          try {
            silverpeasId = m_UserManager.addUser(m_DDManager, distantUDs[nI],
                true);
            if (silverpeasId.equals("")) {
              SilverTrace.info("admin", "admin.synchronizeUsers",
                  "root.MSG_GEN_PARAM_VALUE",
                  "%%%%FULLSYNCHRO%%%%>PB Adding User ! " + specificId);
              sReport += "problem adding user " + distantUDs[nI].getFirstName()
                  + " " + distantUDs[nI].getLastName() + "(specificId:"
                  + specificId + ") - Login and LastName must be set !!!\n";
              sReport += "user has not been added\n";
            } else {
              iNbUsersAdded++;
              SilverTrace.info("admin", "admin.synchronizeUsers",
                  "root.MSG_GEN_PARAM_VALUE", "%%%%FULLSYNCHRO%%%%>Add User : "
                  + silverpeasId);
              listUsersCreate.add(distantUDs[nI]);
              sReport += "adding user " + distantUDs[nI].getFirstName() + " "
                  + distantUDs[nI].getLastName() + "(id:" + silverpeasId
                  + " / specificId:" + specificId + ")\n";
              SynchroReport.warn("admin.synchronizeUsers", "ajout utilisateur "
                  + distantUDs[nI].getFirstName() + " "
                  + distantUDs[nI].getLastName() + " (id:" + silverpeasId
                  + " / specificId:" + specificId + ") OK", null);
              userIds.put(specificId, silverpeasId);
            }
          } catch (AdminException ae) {
            SilverTrace.info("admin", "admin.synchronizeUsers",
                "root.MSG_GEN_PARAM_VALUE",
                "%%%%FULLSYNCHRO%%%%>PB Adding User ! " + specificId, ae);
            sReport += "problem adding user " + distantUDs[nI].getFirstName()
                + " " + distantUDs[nI].getLastName() + "(specificId:"
                + specificId + ") - " + ae.getMessage() + "\n";
            sReport += "user has not been added\n";
          }
        }
      }

      // Delete obsolete users from Silverpeas
      SynchroReport.info("admin.synchronizeUsers",
          "SUPPRESSION des éventuels utilisateurs obsolètes de la base...",
          null);
      for (int nI = 0; nI < silverpeasUDs.length; nI++) {
        bFound = false;
        specificId = silverpeasUDs[nI].getSpecificId();

        // search for user in distant datasource
        for (int nJ = 0; nJ < distantUDs.length && !bFound; nJ++) {
          if (distantUDs[nJ].getSpecificId().equals(specificId)
              || (m_bFallbackUserLogins && silverpeasUDs[nI].getLogin().equals(
              distantUDs[nJ].getLogin()))) {
            bFound = true;
          }
        }

        // if found, do nothing, else delete
        if (!bFound) {
          try {
            SilverTrace.info("admin", "admin.synchronizeUsers",
                "root.MSG_GEN_PARAM_VALUE",
                "%%%%FULLSYNCHRO%%%%>Delete User : " + silverpeasUDs[nI]);
            m_UserManager.deleteUser(m_DDManager, silverpeasUDs[nI], true);
            listUsersRemove.add(silverpeasUDs[nI]);
            iNbUsersDeleted++;
            sReport += "deleting user " + silverpeasUDs[nI].getFirstName()
                + " " + silverpeasUDs[nI].getLastName() + "(id:" + specificId
                + ")\n";
            SynchroReport.warn("admin.synchronizeUsers",
                "suppression utilisateur " + silverpeasUDs[nI].getFirstName()
                + " " + silverpeasUDs[nI].getLastName() + " (specificId:"
                + specificId + ") OK", null);
          } catch (AdminException aeDel) {
            SilverTrace.info("admin", "admin.synchronizeUsers",
                "root.MSG_GEN_PARAM_VALUE",
                "%%%%FULLSYNCHRO%%%%>PB deleting User ! " + specificId, aeDel);
            sReport += "problem deleting user "
                + silverpeasUDs[nI].getFirstName() + " "
                + silverpeasUDs[nI].getLastName() + " (specificId:"
                + specificId + ") - " + aeDel.getMessage() + "\n";
            sReport += "user has not been deleted\n";
          }
        }
      }

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
  private String synchronizeOnlyExistingUsers(String domainId, Hashtable<String, String> userIds)
      throws Exception {
    boolean bFound = false;
    String specificId = null;
    String silverpeasId = null;
    String sReport = "User synchronization : \n";
    int iNbUsersAdded, iNbUsersMaj, iNbUsersDeleted;
    iNbUsersAdded = iNbUsersMaj = iNbUsersDeleted = 0;

    Collection<UserDetail> listUsersUpdate = new ArrayList<UserDetail>();
    Collection<UserDetail> listUsersRemove = new ArrayList<UserDetail>();

    SynchroReport.warn("admin.synchronizeOnlyExistingUsers",
        "SYNCHRONISATION UTILISATEURS :", null);
    try {
      // Clear conversion table
      userIds.clear();

      // Get all users of the domain from distant datasource
      UserDetail[] distantUDs = m_DDManager.getAllUsers(domainId);

      // Get all users of the domain from Silverpeas
      UserDetail[] silverpeasUDs = m_UserManager.getUsersOfDomain(m_DDManager,
          domainId);

      SynchroReport.info("admin.synchronizeOnlyExistingUsers",
          "MISE A JOUR ou SUPPRESSION des utilisateurs dans la base...", null);
      SynchroReport.info("admin.synchronizeOnlyExistingUsers",
          "Valeur du paramètre 'm_bFallbackUserLogins' = "
          + m_bFallbackUserLogins, null);

      UserDetail userSP = null;
      UserDetail userLDAP = null;

      // Update existing users from distant datasource
      for (int nI = 0; nI < silverpeasUDs.length; nI++) {
        bFound = false;
        userSP = silverpeasUDs[nI];
        specificId = userSP.getSpecificId();

        // search for user in distant datasource
        for (int nJ = 0; nJ < distantUDs.length && !bFound; nJ++) {
          userLDAP = distantUDs[nJ];
          if (userLDAP.getSpecificId().equals(specificId)
              || (m_bFallbackUserLogins && userLDAP.getLogin().equals(
              userSP.getLogin()))) {
            bFound = true;
            userLDAP.setId(userSP.getId());
            userLDAP.setAccessLevel(userSP.getAccessLevel());
            userIds.put(specificId, userSP.getId());
          }
        }

        userLDAP.setDomainId(domainId);
        // if found, update else delete
        if (bFound)// MAJ
        {
          try {
            SilverTrace.info("admin", "admin.synchronizeOnlyExistingUsers",
                "root.MSG_GEN_PARAM_VALUE",
                "%%%%FULLSYNCHRO%%%%>Update User : " + userLDAP.getId());
            silverpeasId = m_UserManager.updateUser(m_DDManager, userLDAP);
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
                "root.MSG_GEN_PARAM_VALUE",
                "%%%%FULLSYNCHRO%%%%>Delete User : " + silverpeasUDs[nI]);
            m_UserManager.deleteUser(m_DDManager, userSP, true);
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
                + " (specificId:" + specificId + ") - " + aeDel.getMessage()
                + "\n";
            sReport += "user has not been deleted\n";
          }
        }
      }

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
    Domain theDomain = m_DDManager.getDomain(domainId);
    String propDomainFileName = theDomain.getPropFileName();
    ResourceLocator propDomainLdap = new ResourceLocator(propDomainFileName, "");
    String nomClasseSynchro = propDomainLdap.getString("synchro.Class");
    if (StringUtil.isDefined(nomClasseSynchro)) {
      if (usersAdded == null) {
        usersAdded = new ArrayList<UserDetail>();
      }
      if (usersUpdated == null) {
        usersUpdated = new ArrayList<UserDetail>();
      }
      if (usersRemoved == null) {
        usersRemoved = new ArrayList<UserDetail>();
      }
      LDAPSynchroUserItf synchroUser = null;
      try {
        synchroUser = (LDAPSynchroUserItf) Class.forName(nomClasseSynchro).newInstance();
        if (synchroUser != null) {
          synchroUser.processUsers(usersAdded, usersUpdated, usersRemoved);
        }
      } catch (Exception e) {
        SilverTrace.warn("admin", "admin.synchronizeOnlyExistingUsers",
            "root.MSG_GEN_PARAM_VALUE", "Pb Loading class traitement Users ! ");
        synchroUser = null;
      }
    }
  }

  /**
   * Synchronize groups between cache and domain's datastore
   */
  private String synchronizeGroups(String domainId, Hashtable<String, String> userIds)
      throws Exception {
    boolean bFound = false;
    String specificId = null;
    String sReport = "Group synchronization : \n";
    Hashtable<String, Group> allDistantGroups = new Hashtable<String, Group>();
    int iNbGroupsAdded, iNbGroupsMaj, iNbGroupsDeleted;

    iNbGroupsAdded = iNbGroupsMaj = iNbGroupsDeleted = 0;

    SynchroReport.warn("admin.synchronizeGroups", "SYNCHRONISATION GROUPES :",
        null);
    try {
      // Get all root groups of the domain from distant datasource
      Group[] distantRootGroups = m_DDManager.getAllRootGroups(domainId);

      // Get all groups of the domain from Silverpeas
      Group[] silverpeasGroups = m_GroupManager.getGroupsOfDomain(m_DDManager,
          domainId);

      SynchroReport.info("admin.synchronizeGroups",
          "AJOUT ou MISE A JOUR des groupes dans la base...", null);
      // Check for new groups resursively
      sReport += checkOutGroups(domainId, silverpeasGroups, distantRootGroups,
          allDistantGroups, userIds, null, iNbGroupsAdded, iNbGroupsMaj,
          iNbGroupsDeleted);

      // Delete obsolete groups
      SynchroReport.info("admin.synchronizeGroups",
          "SUPPRESSION des éventuels groupes obsolètes de la base...", null);
      Group[] distantGroups = (Group[]) allDistantGroups.values().toArray(
          new Group[0]);
      for (int nI = 0; nI < silverpeasGroups.length; nI++) {
        bFound = false;
        specificId = silverpeasGroups[nI].getSpecificId();

        // search for group in distant datasource
        for (int nJ = 0; nJ < distantGroups.length && !bFound; nJ++) {
          if (distantGroups[nJ].getSpecificId().equals(specificId)) {
            bFound = true;
          } else if (m_bFallbackGroupNames
              && distantGroups[nJ].getName().equals(specificId)) {
            bFound = true;
          }
        }

        // if found, do nothing, else delete
        if (!bFound) {
          try {
            SilverTrace.info("admin", "admin.synchronizeGroups",
                "root.MSG_GEN_PARAM_VALUE",
                "%%%%FULLSYNCHRO%%%%>Delete group : "
                + silverpeasGroups[nI].getId() + " - " + specificId);
            m_GroupManager.deleteGroupById(m_DDManager, silverpeasGroups[nI],
                true);
            iNbGroupsDeleted++;
            sReport += "deleting group " + silverpeasGroups[nI].getName()
                + "(id:" + specificId + ")\n";
            SynchroReport.warn("admin.synchronizeGroups", "suppression groupe "
                + silverpeasGroups[nI].getName() + "(SpecificId:" + specificId
                + ") OK", null);
          } catch (AdminException aeDel) {
            SilverTrace.info("admin", "admin.synchronizeGroups",
                "root.MSG_GEN_PARAM_VALUE",
                "%%%%FULLSYNCHRO%%%%>PB deleting group ! " + specificId, aeDel);
            sReport += "problem deleting group "
                + silverpeasGroups[nI].getName() + " (specificId:" + specificId
                + ") - " + aeDel.getMessage() + "\n";
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
          "Problème lors de la synchronisation des groupes : "
          + e.getMessage(), null);
      throw new AdminException("admin.synchronizeGroups",
          SilverpeasException.ERROR, "admin.EX_ERR_SYNCHRONIZE_DOMAIN_GROUPS",
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
  private String checkOutGroups(String domainId, Group[] existingGroups,
      Group[] testedGroups, Hashtable<String, Group> allIncluededGroups,
      Hashtable<String, String> userIds,
      String superGroupId, int iNbGroupsAdded, int iNbGroupsMaj,
      int iNbGroupsDeleted) throws Exception {
    boolean bFound = false;
    String specificId = null;
    String silverpeasId = null;
    String sReport = "";
    String sResult = "";
    int nI;

    for (nI = 0; nI < testedGroups.length; nI++) {
      allIncluededGroups.put(testedGroups[nI].getSpecificId(), testedGroups[nI]);
    }
    // Add new groups or update existing ones from distant datasource
    for (nI = 0; nI < testedGroups.length; nI++) {
      bFound = false;
      specificId = testedGroups[nI].getSpecificId();

      SilverTrace.info("admin", "admin.checkOutGroups",
          "root.MSG_GEN_PARAM_VALUE", "%%%%FULLSYNCHRO%%%%>Deal with group : "
          + specificId);
      // search for group in Silverpeas database
      for (int nJ = 0; nJ < existingGroups.length && !bFound; nJ++) {
        if (existingGroups[nJ].getSpecificId().equals(specificId)) {
          bFound = true;
          testedGroups[nI].setId(existingGroups[nJ].getId());
        } else if (m_bFallbackGroupNames
            && existingGroups[nJ].getSpecificId().equals(
            testedGroups[nI].getName())) {
          bFound = true;
          testedGroups[nI].setId(existingGroups[nJ].getId());
        }
      }

      // Prepare Group to be at Silverpeas format
      testedGroups[nI].setDomainId(domainId);

      // Set the Parent Id
      if (bFound) {
        SynchroReport.debug("admin.checkOutGroups", "avant maj du groupe "
            + specificId + ", recherche de ses groupes parents", null);
      } else {
        SynchroReport.debug("admin.checkOutGroups", "avant ajout du groupe "
            + specificId + ", recherche de ses groupes parents", null);
      }
      String[] groupParentsIds = m_DDManager.getGroupMemberGroupIds(domainId,
          testedGroups[nI].getSpecificId());
      if ((groupParentsIds == null) || (groupParentsIds.length == 0)) {
        testedGroups[nI].setSuperGroupId(null);
        SynchroReport.debug("admin.checkOutGroups", "le groupe " + specificId
            + " n'a pas de père", null);
      } else {
        testedGroups[nI].setSuperGroupId(superGroupId);
        if (superGroupId != null)// sécurité
        {
          SynchroReport.debug("admin.checkOutGroups", "le groupe " + specificId
              + " a pour père le groupe "
              + m_DDManager.getGroup(superGroupId).getSpecificId()
              + " d'Id base " + superGroupId, null);
        }
      }

      String[] groupUserIds = testedGroups[nI].getUserIds();
      Vector<String> convertedUserIds = new Vector<String>();
      for (int nK = 0; nK < groupUserIds.length; nK++) {
        if (userIds.get(groupUserIds[nK]) != null) {
          convertedUserIds.add(userIds.get(groupUserIds[nK]));
        }
      }
      // Le groupe contiendra une liste d'IDs de users existant ds la base et
      // non + une liste de logins récupérés via LDAP
      testedGroups[nI].setUserIds((String[]) convertedUserIds.toArray(new String[0]));

      // if found, update, else create
      if (bFound)// MAJ
      {
        try {
          SilverTrace.info("admin", "admin.checkOutGroups",
              "root.MSG_GEN_PARAM_VALUE", "%%%%FULLSYNCHRO%%%%>Update group : "
              + testedGroups[nI].getId());
          sResult = m_GroupManager.updateGroup(m_DDManager, testedGroups[nI],
              true);
          if (!sResult.equals("")) {
            iNbGroupsMaj++;
            silverpeasId = testedGroups[nI].getId();
            sReport += "updating group " + testedGroups[nI].getName() + "(id:"
                + specificId + ")\n";
            SynchroReport.warn("admin.checkOutGroups", "maj groupe "
                + testedGroups[nI].getName() + " (id:" + silverpeasId + ") OK",
                null);
          } else// le name groupe non renseigné
          {
            SilverTrace.info("admin", "admin.checkOutGroups",
                "root.MSG_GEN_PARAM_VALUE",
                "%%%%FULLSYNCHRO%%%%>PB Updating Group ! " + specificId);
            sReport += "problem updating group id : " + specificId + "\n";
          }
        } catch (AdminException aeMaj) {
          SilverTrace.info("admin", "admin.checkOutGroups",
              "root.MSG_GEN_PARAM_VALUE",
              "%%%%FULLSYNCHRO%%%%>PB Updating Group ! " + specificId, aeMaj);
          sReport += "problem updating group " + testedGroups[nI].getName()
              + " (id:" + specificId + ") " + aeMaj.getMessage() + "\n";
          sReport += "group has not been updated\n";
        }
      } else// AJOUT
      {
        try {
          silverpeasId = m_GroupManager.addGroup(m_DDManager, testedGroups[nI],
              true);
          if (!silverpeasId.equals("")) {
            iNbGroupsAdded++;
            SilverTrace.info("admin", "admin.checkOutGroups",
                "root.MSG_GEN_PARAM_VALUE", "%%%%FULLSYNCHRO%%%%>Add group : "
                + silverpeasId);
            sReport += "adding group " + testedGroups[nI].getName() + "(id:"
                + specificId + ")\n";
            SynchroReport.warn("admin.checkOutGroups", "ajout groupe "
                + testedGroups[nI].getName() + " (id:" + silverpeasId + ") OK",
                null);
          } else// le name groupe non renseigné
          {
            SilverTrace.info("admin", "admin.checkOutGroups",
                "root.MSG_GEN_PARAM_VALUE",
                "%%%%FULLSYNCHRO%%%%>PB Adding Group ! " + specificId);
            sReport += "problem adding group id : " + specificId + "\n";
          }
        } catch (AdminException aeAdd) {
          SilverTrace.info("admin", "admin.checkOutGroups",
              "root.MSG_GEN_PARAM_VALUE",
              "%%%%FULLSYNCHRO%%%%>PB Adding Group ! " + specificId, aeAdd);
          sReport += "problem adding group " + testedGroups[nI].getName()
              + " (id:" + specificId + ") " + aeAdd.getMessage() + "\n";
          sReport += "group has not been added\n";
        }
      }
      // Recurse with subgroups
      if (silverpeasId != null && silverpeasId.length() > 0) {
        Group[] subGroups = m_DDManager.getGroups(silverpeasId);
        if (subGroups != null && subGroups.length > 0) {
          Group[] cleanSubGroups = removeCrossReferences(subGroups,
              allIncluededGroups, specificId);
          if (cleanSubGroups != null && cleanSubGroups.length > 0) {
            SynchroReport.info("admin.checkOutGroups",
                "Ajout ou mise à jour de " + cleanSubGroups.length
                + " groupes fils du groupe " + specificId + "...", null);
            sReport += checkOutGroups(domainId, existingGroups, cleanSubGroups,
                allIncluededGroups, userIds, silverpeasId, iNbGroupsAdded,
                iNbGroupsMaj, iNbGroupsDeleted);
          }
        }
      }
    }
    return sReport;
  }

  /**
   * Remove cross reference risk between groups
   */
  private Group[] removeCrossReferences(Group[] subGroups,
      Hashtable<String, Group> allIncluededGroups, String fatherId) throws Exception {
    ArrayList<Group> cleanSubGroups = new ArrayList<Group>();
    int nI;

    for (nI = 0; nI < subGroups.length; nI++) {
      if (allIncluededGroups.get(subGroups[nI].getSpecificId()) == null) {
        cleanSubGroups.add(subGroups[nI]);
      } else {
        SilverTrace.warn("admin", "Admin.removeCrossReferences",
            "root.MSG_GEN_PARAM_VALUE", "Cross removed for child : "
            + subGroups[nI].getSpecificId() + " of father : " + fatherId);
      }
    }
    return (Group[]) cleanSubGroups.toArray(new Group[0]);
  }

  // -------------------------------------------------------------------------
  // For SelectionPeas
  // -------------------------------------------------------------------------
  public String[] searchUsersIds(String sGroupId, String componentId,
      String[] profileIds, UserDetail modelUser) throws AdminException {
    try {
      List<String> userIds = new ArrayList<String>();
      if (StringUtil.isDefined(sGroupId)) {
        // search users in group and subgroups
        UserDetail[] users = getAllUsersOfGroup(sGroupId);
        for (UserDetail user : users) {
          userIds.add(user.getId());
        }
        if (userIds.size() == 0) {
          userIds = null;
        }
      } else if (profileIds != null && profileIds.length > 0) {
        // search users in profiles
        for (int p = 0; p < profileIds.length; p++) {
          ProfileInst profile =
              m_ProfileInstManager.getProfileInst(m_DDManager, profileIds[p], null);

          // add users directly attach to profile
          userIds.addAll(profile.getAllUsers());

          // add users indirectly attach to profile (groups attached to profile)
          List<String> groupIds = profile.getAllGroups();
          List<String> allGroupIds = new ArrayList<String>();
          for (String groupId : groupIds) {
            allGroupIds.add(groupId);
            allGroupIds.addAll(m_GroupManager.getAllSubGroupIdsRecursively(groupId));
          }
          userIds.addAll(m_UserManager.getAllUserIdsOfGroups(allGroupIds));
        }
        if (userIds.size() == 0) {
          userIds = null;
        }
      } else if (StringUtil.isDefined(componentId)) {
        // search users in component
        userIds.addAll(getUserIdsForComponent(componentId));
        if (userIds.size() == 0) {
          userIds = null;
        }
      } else {
        // get all users
        userIds = new ArrayList<String>();
      }

      if (userIds == null) {
        return new String[0];
      } else {
        return m_UserManager.searchUsersIds(m_DDManager, userIds, modelUser);
      }
    } catch (Exception e) {
      throw new AdminException("Admin.searchUsersIds",
          SilverpeasException.ERROR, "admin.EX_ERR_USER_NOT_FOUND", e);
    }
  }

  private List<String> getUserIdsForComponent(String componentId) throws AdminException {
    List<String> userIds = new ArrayList<String>();

    ComponentInst component = getComponentInst(componentId);
    List<ProfileInst> profiles = component.getAllProfilesInst();
    for (ProfileInst profile : profiles) {
      userIds.addAll(getUserIdsForComponentProfile(profile));
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
    for (String groupId : groupIds) {
      allGroupIds.add(groupId);
      allGroupIds.addAll(m_GroupManager.getAllSubGroupIdsRecursively(groupId));
    }
    userIds.addAll(m_UserManager.getAllUserIdsOfGroups(allGroupIds));

    return userIds;
  }

  public String[] searchGroupsIds(boolean isRootGroup, String componentId,
      String[] profileId, Group modelGroup) throws AdminException {
    try {
      return m_GroupManager.searchGroupsIds(m_DDManager, isRootGroup,
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
      DomainSPSchemaPool.releaseConnections();
      ConnectionPool.releaseConnections();
    } catch (Exception e) {
      throw new AdminException("Admin.resetAllDBConnections",
          SilverpeasException.ERROR, "root.EX_CONNECTION_CLOSE_FAILED", e);
    }
  }

  private void rollback() {
    try {
      // Roll back the transactions
      m_DDManager.rollback();
    } catch (Exception e1) {
      SilverTrace.error("admin", "Admin.rollback", "root.EX_ERR_ROLLBACK", e1);
    }
  }

  // -------------------------------------------------------------------------
  // Node profile management
  // -------------------------------------------------------------------------
  /**
   * Use when a node or subnode is created
   * @param object
   */
  public void registerProfiledObject(ProfiledObject object) {
  }

  /**
   * Use when a node is deleted
   * @param object
   */
  public void unregisterProfiledObject(ProfiledObject object) {
  }

  private String getSpaceId(SpaceInst spaceInst) {
    if (spaceInst.getId().startsWith(SPACE_KEY_PREFIX)) {
      return spaceInst.getId();
    }
    return SPACE_KEY_PREFIX + spaceInst.getId();
  }
}
