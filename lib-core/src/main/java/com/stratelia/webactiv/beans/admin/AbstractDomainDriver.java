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

import com.silverpeas.util.StringUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

abstract public class AbstractDomainDriver extends Object {

  protected int m_DomainId = -1; // The domainId of this instance of domain
  // driver
  protected List<DomainProperty> m_Properties = new ArrayList<DomainProperty>(); // liste ordonnée
  // des
  // properties du bundle
  // domainSP
  protected String[] m_aKeys = null;
  protected String m_PropertiesMultilang = "";
  protected Hashtable<String, HashMap<String, String>> m_PropertiesLabels =
      new Hashtable<String, HashMap<String, String>>();
  protected Hashtable<String, HashMap<String, String>> m_PropertiesDescriptions =
      new Hashtable<String, HashMap<String, String>>();
  protected String[] m_mapParameters = null;
  protected boolean synchroInProcess = false;
  protected boolean x509Enabled = false;
  /**
   * No possible actions Mask
   * @see #getDriverActions
   */
  final static public long ACTION_NONE = 0x00000000;
  /**
   * Read Users' infos action Mask
   * @see #getDriverActions
   */
  final static public long ACTION_READ_USER = 0x00000001;
  /**
   * Read Groups' infos action Mask
   * @see #getDriverActions
   */
  final static public long ACTION_READ_GROUP = 0x00000002;
  /**
   * Update Users' infos action Mask
   * @see #getDriverActions
   */
  final static public long ACTION_UPDATE_USER = 0x00000004;
  /**
   * Update Groups' infos action Mask
   * @see #getDriverActions
   */
  final static public long ACTION_UPDATE_GROUP = 0x00000008;
  /**
   * Create User action Mask
   * @see #getDriverActions
   */
  final static public long ACTION_CREATE_USER = 0x00000010;
  /**
   * Create Group action Mask
   * @see #getDriverActions
   */
  final static public long ACTION_CREATE_GROUP = 0x00000020;
  /**
   * Delete User action Mask
   * @see #getDriverActions
   */
  final static public long ACTION_DELETE_USER = 0x00000040;
  /**
   * Delete Group action Mask
   * @see #getDriverActions
   */
  final static public long ACTION_DELETE_GROUP = 0x00000080;
  /**
   * Add/Remove User from group action Mask
   * @see #getDriverActions
   */
  final static public long ACTION_EDIT_USER_IN_GROUP = 0x00000100;
  /**
   * Add a user in Silverpeas DB by synchronization with a reference LDAP DB
   * @see #getDriverActions
   */
  final static public long ACTION_IMPORT_USER = 0x00000200;
  /**
   * Updates user Silverpeas infos from LDAP DB
   * @see #getDriverActions
   */
  final static public long ACTION_SYNCHRO_USER = 0x00000400;
  /**
   * Remove user entry from Silverpeas
   * @see #getDriverActions
   */
  final static public long ACTION_REMOVE_USER = 0x00000800;
  /**
   * Add a group in Silverpeas DB by synchronization with a reference LDAP DB
   * @see #getDriverActions
   */
  final static public long ACTION_IMPORT_GROUP = 0x00001000;
  /**
   * Updates group Silverpeas infos from LDAP DB
   * @see #getDriverActions
   */
  final static public long ACTION_SYNCHRO_GROUP = 0x00002000;
  /**
   * Remove group entry from Silverpeas
   * @see #getDriverActions
   */
  final static public long ACTION_REMOVE_GROUP = 0x00004000;
  /**
   * Create a x509 certificate and store it in server's truststore
   * @see #getDriverActions
   */
  final static public long ACTION_X509_USER = 0x00008000;
  /**
   * All available actions Mask
   * @see #getDriverActions
   */
  final static public long ACTION_MASK_ALL = 0xFFFFFFFF;
  final static public long ACTION_MASK_RW = ACTION_READ_USER
      | ACTION_READ_GROUP | ACTION_UPDATE_USER | ACTION_UPDATE_GROUP
      | ACTION_CREATE_USER | ACTION_CREATE_GROUP | ACTION_DELETE_USER
      | ACTION_DELETE_GROUP | ACTION_EDIT_USER_IN_GROUP;
  final static public long ACTION_MASK_RO = ACTION_READ_USER
      | ACTION_READ_GROUP | ACTION_IMPORT_USER | ACTION_SYNCHRO_USER
      | ACTION_REMOVE_USER | ACTION_IMPORT_GROUP | ACTION_SYNCHRO_GROUP
      | ACTION_REMOVE_GROUP;
  final static public long ACTION_MASK_MIXED_GROUPS = ACTION_READ_GROUP
      | ACTION_UPDATE_GROUP | ACTION_CREATE_GROUP | ACTION_DELETE_GROUP
      | ACTION_EDIT_USER_IN_GROUP;

  /**
   * Initialize the domain driver with the initialization parameter stocked in table This parameter
   * could be a table name or a ressource file name or whatever specified by the domain driver
   * Default : ressource file name
   * @param domainId id of domain
   * @param initParam name of resource file
   * @param authenticationServer name of the authentication server (no more used yet)
   * @throws Exception
   */
  public void init(int domainId, String initParam, String authenticationServer)
      throws Exception {
    ResourceLocator rs = new ResourceLocator(initParam, "");
    int nbProps = 0;
    int i;
    String s;
    DomainProperty newElmt;

    m_DomainId = domainId;

    // Init the domain's specific users properties
    m_Properties.clear();
    m_PropertiesMultilang = rs.getString("property.ResourceFile");
    s = rs.getString("property.Number");
    if ((s != null) && (s.length() > 0)) {
      nbProps = Integer.parseInt(s);
    }
    m_aKeys = new String[nbProps];
    m_mapParameters = new String[nbProps];
    for (i = 1; i <= nbProps; i++) {
      s = rs.getString("property_" + Integer.toString(i) + ".Name");
      if ((s != null) && (s.length() > 0)) {
        newElmt = new DomainProperty(rs, String.valueOf(i)); // Retreives all
        // property's
        // infos
        m_Properties.add(newElmt);
        m_aKeys[i - 1] = newElmt.getName();
        m_mapParameters[i - 1] = newElmt.getMapParameter();
      }
    }

    // X509 Certificates management is enable ?
    x509Enabled = rs.getBoolean("security.x509.enabled", false);

    // Init the domain's properties
    initFromProperties(rs);
  }

  public String[] getPropertiesNames() {
    return m_aKeys;
  }

  public DomainProperty getProperty(String propName) {
    Iterator<DomainProperty> it = m_Properties.iterator();
    DomainProperty domainProp;
    while (it.hasNext()) {
      domainProp = it.next();
      if (domainProp.getName().equals(propName)) {
        return domainProp;
      }
    }
    return null;
  }

  public String[] getMapParameters() {
    return m_mapParameters;
  }

  public List<DomainProperty> getPropertiesToImport(String language) {
    List<DomainProperty> props = new ArrayList<DomainProperty>();

    HashMap<String, String> theLabels = getPropertiesLabels(language);
    HashMap<String, String> theDescriptions = getPropertiesDescriptions(language);

    addPropertiesToImport(props, theDescriptions);

    Iterator<DomainProperty> it = m_Properties.iterator();
    DomainProperty domainProp;
    while (it.hasNext()) {
      domainProp = it.next();
      if (domainProp.isUsedToImport()) {
        String propLabel = theLabels.get(domainProp.getName());
        String propDescription = theDescriptions.get(domainProp.getName());
        domainProp.setLabel(propLabel);
        domainProp.setDescription(propDescription);
        props.add(domainProp);
      }
    }
    return props;
  }

  public void addPropertiesToImport(List<DomainProperty> props) {
  }

  /**
   * 
   * @param props
   * @param theDescriptions 
   */
  public void addPropertiesToImport(List<DomainProperty> props, HashMap<String, String> theDescriptions) {
  }

  public HashMap<String, String> getPropertiesLabels(String language) {
    HashMap<String, String> valret = m_PropertiesLabels.get(language);
    if (valret == null) {
      HashMap<String, String> newLabels = new HashMap<String, String>();
      ResourceLocator rs = new ResourceLocator(m_PropertiesMultilang, language);
      for (int i = 0; i < m_aKeys.length; i++) {
        newLabels.put(m_aKeys[i], rs.getString(m_aKeys[i]));
      }
      m_PropertiesLabels.put(language, newLabels);
      valret = newLabels;
    }
    return valret;
  }

  public HashMap<String, String> getPropertiesDescriptions(String language) {
    HashMap<String, String> valret = m_PropertiesDescriptions.get(language);

    if (valret == null) {
      HashMap<String, String> newDescriptions = new HashMap<String, String>();
      ResourceLocator rs = new ResourceLocator(m_PropertiesMultilang, language);
      for (int i = 0; i < m_aKeys.length; i++) {
        newDescriptions.put(m_aKeys[i], rs.getString(m_aKeys[i] + ".description"));
      }
      m_PropertiesDescriptions.put(language, newDescriptions);
      valret = newDescriptions;
    }
    return valret;
  }

  /**
   * Virtual method that performs extra initialization from a properties file. To overload by the
   * class who need it.
   * @param rs name of resource file
   */
  public void initFromProperties(ResourceLocator rs) throws Exception {
  }

  /**
   * Called when Admin starts the synchronization
   */
  public long getDriverActions() {
    if (x509Enabled) {
      return ACTION_MASK_RW | AbstractDomainDriver.ACTION_X509_USER;
    }
    return ACTION_MASK_RW;
  }

  public boolean isSynchroOnLoginEnabled() {
    return false;
  }

  public boolean isSynchroThreaded() {
    return false;
  }

  public boolean isSynchroOnLoginRecursToGroups() {
    return true;
  }

  public boolean isGroupsInheritProfiles() {
    return false;
  }

  public boolean mustImportUsers() {
    return true;
  }

  public String getTimeStamp(String minTimeStamp) throws Exception {
    return "";
  }

  public String getTimeStampField() throws Exception {
    return null;
  }

  public boolean isX509CertificateEnabled() {
    return false;
  }

  public UserDetail[] getAllChangedUsers(String fromTimeStamp,
      String toTimeStamp) throws Exception {
    throw new AdminException("AbstractDomainDriver.getAllChangedUsers",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  public Group[] getAllChangedGroups(String fromTimeStamp, String toTimeStamp)
      throws Exception {
    throw new AdminException("AbstractDomainDriver.getAllChangedGroups",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  /**
   * Called when Admin starts the synchronization
   */
  public void beginSynchronization() throws Exception {
    synchroInProcess = true;
  }

  public boolean isSynchroInProcess() throws Exception {
    return synchroInProcess;
  }

  /**
   * Called when Admin ends the synchronization
   * @param cancelSynchro true if the synchronization is cancelled, false if it ends normally
   */
  public String endSynchronization(boolean cancelSynchro) throws Exception {
    synchroInProcess = false;
    return "";
  }

  /**
   * Import a given user in Database from the reference
   * @param userLogin The User Login to import
   * @return The User object that contain new user information
   */
  public UserDetail importUser(String userLogin) throws Exception {
    throw new AdminException("AbstractDomainDriver.importUser",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  /**
   * Remove a given user from database
   * @param userId The user id To remove synchro
   */
  public void removeUser(String userId) throws Exception {
    throw new AdminException("AbstractDomainDriver.removeUser",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  /**
   * Update user information in database
   * @param userId The User Id to synchronize
   * @return The User object that contain new user information
   */
  public UserDetail synchroUser(String userId) throws Exception {
    throw new AdminException("AbstractDomainDriver.synchroUser",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  /**
   * Create a given user in Database
   * @param m_User The User object that contain new user information@return String
   * @return The user id as stored in the database
   */
  public String createUser(UserDetail user) throws Exception {
    throw new AdminException("AbstractDomainDriver.createUser",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  /**
   * Delete a given user from database
   * @param userId The user id as stored in the database
   */
  public void deleteUser(String userId) throws Exception {
    throw new AdminException("AbstractDomainDriver.deleteUser",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  /**
   * Update user information in database
   * @param m_User The User object that contain user information
   */
  public void updateUserFull(UserFull user) throws Exception {
    throw new AdminException("AbstractDomainDriver.updateUser",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  /**
   * Update user information in database
   * @param m_User The User object that contain user information
   */
  public void updateUserDetail(UserDetail user) throws Exception {
    throw new AdminException("AbstractDomainDriver.updateUser",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  /**
   * Retrieve user information from database
   * @param userId The user id as stored in the database
   * @return The User object that contain new user information
   */
  public UserDetail getUser(String userId) throws Exception {
    throw new AdminException("AbstractDomainDriver.getUser",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  /**
   * Retrieve user information from database
   * @param userId The user id as stored in the database
   * @return The full User object that contain ALL user informations
   */
  abstract public UserFull getUserFull(String userId) throws Exception;

  /**
   * Retrieve user's groups
   * @param userId The user id as stored in the database
   * @return The User's groups specific Ids
   */
  public String[] getUserMemberGroupIds(String userId) throws Exception {
    throw new AdminException("AbstractDomainDriver.getUserMemberGroupIds",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  /**
   * Retrieve all users from the database
   * @return User[] An array of User Objects that contain users information
   */
  public UserDetail[] getAllUsers() throws Exception {
    throw new AdminException("AbstractDomainDriver.getAllUsers",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  public UserDetail[] getUsersBySpecificProperty(String propertyName,
      String value) throws Exception {
    throw new AdminException("AbstractDomainDriver.getUsersBySpecificProperty",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  public UserDetail[] getUsersByQuery(Hashtable<String, String> query) throws Exception {
    throw new AdminException("AbstractDomainDriver.getUsersByQuery",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  /**
   * Import a given group in Database from the reference
   * @param groupName The group name to import
   * @return The group object that contain new group information
   */
  public Group importGroup(String groupName) throws Exception {
    throw new AdminException("AbstractDomainDriver.importGroup",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  /**
   * Remove a given group from database
   * @param groupId The group id To remove synchro
   */
  public void removeGroup(String groupId) throws Exception {
    throw new AdminException("AbstractDomainDriver.removeGroup",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  /**
   * Update group information in database
   * @param groupId The group Id to synchronize
   * @return The group object that contain new group information
   */
  public Group synchroGroup(String groupId) throws Exception {
    throw new AdminException("AbstractDomainDriver.synchroGroup",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  /**
   * Create a given group in database
   * @param m_Group New group information
   * @return The group id as stored in the database
   */
  public String createGroup(Group m_Group) throws Exception {
    throw new AdminException("AbstractDomainDriver.createGroup",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  /**
   * Delete a given group from database
   * @param groupId The group id as stored in the database
   */
  public void deleteGroup(String groupId) throws Exception {
    throw new AdminException("AbstractDomainDriver.deleteGroup",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  /**
   * Update group information in database
   * @param m_Group The Group object that contains user information
   * @throws Exception  
   */
  public void updateGroup(Group m_Group) throws Exception {
    throw new AdminException("AbstractDomainDriver.updateGroup",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  /**
   * Retrieve group information from database
   * @param groupId The group id as stored in the database
   * @return The Group object that contains group information
   * @throws Exception  
   */
  public Group getGroup(String groupId) throws Exception {
    throw new AdminException("AbstractDomainDriver.getGroup",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  /**
   * Retrieve group information from database
   * @param groupName The group name as stored in the database
   * @return The Group object that contains group information
   * @throws Exception  
   */
  public Group getGroupByName(String groupName) throws Exception {
    throw new AdminException("AbstractDomainDriver.getGroupByName",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  /**
   * Retrieve all groups contained in the given group
   * @param groupId The group id as stored in the database
   * @return Group[] An array of Group Objects that contain groups information
   * @throws Exception  
   */
  public Group[] getGroups(String groupId) throws Exception {
    throw new AdminException("AbstractDomainDriver.getGroups",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  /**
   * Retrieve all groups from the database
   * @return Group[] An array of Group Objects that contain groups information
   * @throws Exception  
   */
  public Group[] getAllGroups() throws Exception {
    throw new AdminException("AbstractDomainDriver.getAllGroups",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  /**
   * Retrieve all root groups from the database
   * @return Group[] An array of Group Objects that contain groups information
   * @throws Exception  
   */
  public Group[] getAllRootGroups() throws Exception {
    throw new AdminException("AbstractDomainDriver.getAllRootGroups",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  /**
   * Retrieve group's parents
   * @param groupId The group id as stored in the database
   * @return The Group's parents specific Ids
   * @throws Exception  
   */
  public String[] getGroupMemberGroupIds(String groupId) throws Exception {
    throw new AdminException("AbstractDomainDriver.getGroupMemberGroupIds",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  /**
   * Start a new transaction
   * @param bAutoCommit Specifies is transaction is automatically committed (without explicit
   * 'commit' statement)
   * @throws Exception  
   */
  public void startTransaction(boolean bAutoCommit) throws Exception {
    throw new AdminException("AbstractDomainDriver.startTransaction",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  /**
   * Commit transaction
   * @throws Exception  
   */
  public void commit() throws Exception {
    throw new AdminException("AbstractDomainDriver.commit",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  /**
   * Rollback transaction
   * @throws Exception  
   */
  public void rollback() throws Exception {
    throw new AdminException("AbstractDomainDriver.rollback",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }

  static protected int idAsInt(String id) {
    if (!StringUtil.isDefined(id)) {
      return -1; // the null id.
    }
    try {
      return Integer.parseInt(id);
    } catch (NumberFormatException e) {
      return -1; // the null id.
    }
  }

  /**
   * Convert int Id to String Id
   * @param id id to convert
   */
  static protected String idAsString(int id) {
    return String.valueOf(id);
  }

  /**
   * get user specifics attributes for the driver
   * @param userId
   * @return List of attributes name
   * @throws Exception
   */
  public List<String> getUserAttributes() throws Exception {
    throw new AdminException("AbstractDomainDriver.getUserAttributes",
        SilverpeasException.ERROR, "admin.EX_ERR_DOMAIN_DOES_NOT_SUPPORT",
        "DomainId=" + Integer.toString(m_DomainId));
  }
}