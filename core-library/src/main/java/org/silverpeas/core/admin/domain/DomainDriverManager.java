/**
* Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.admin.domain;

import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.persistence.AdminPersistenceException;
import org.silverpeas.core.admin.persistence.DomainRow;
import org.silverpeas.core.admin.persistence.GroupRow;
import org.silverpeas.core.admin.persistence.KeyStoreRow;
import org.silverpeas.core.admin.persistence.OrganizationSchema;
import org.silverpeas.core.admin.persistence.OrganizationSchemaPool;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.UserIndexation;
import org.silverpeas.core.admin.user.dao.UserDAO;
import org.silverpeas.core.admin.user.dao.UserSearchCriteriaForDAO;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.exception.UtilException;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.silverpeas.core.SilverpeasExceptionMessages.*;

/**
 * A manager of domain drivers. It exposes domain related operations and delegates the domain
 * specific tasks to the correct domain driver for a given domain identifier for which the
 * operation is performed.
 */
@Singleton
@Transactional(Transactional.TxType.MANDATORY)
public class DomainDriverManager extends AbstractDomainDriver {

  @Inject
  private UserDAO userDAO;
  private ThreadLocal<Connection> currentConnection = new ThreadLocal<>();
  private ThreadLocal<OrganizationSchemaHolder> organizationHolder = new ThreadLocal<>();

  private Map<String, DomainDriver> domainDriverInstances = new ConcurrentHashMap<>();

  protected DomainDriverManager() {
  }

  /**
   * Holds an organization schema from the pool to perform domain management.
   */
  public synchronized void holdOrganizationSchema() throws AdminException {
    if (organizationHolder.get() == null) {
      try {
        organizationHolder.set(new OrganizationSchemaHolder());
      } catch (AdminPersistenceException e) {
        throw new AdminException(failureOnGetting("organization", "schema"), e);
      }
    }
    organizationHolder.get().hold();
  }

  /**
   * Releases the organization schema that was previously holding.
   */
  public synchronized void releaseOrganizationSchema() throws AdminException {
    OrganizationSchemaHolder organization = organizationHolder.get();
    if (organization != null) {
      organization.release();
      if (organization.isFree()) {
        organizationHolder.remove();
      }
    }
  }

  @Override
  public UserDetail[] getAllChangedUsers(String fromTimeStamp, String toTimeStamp) throws Exception {
    return new UserDetail[0];
  }

  @Override
  public GroupDetail[] getAllChangedGroups(String fromTimeStamp, String toTimeStamp) throws Exception {
    return new GroupDetail[0];
  }

  /**
   * Create a new User.
   *
   * @param user
   * @return
   * @throws Exception
   */
  @Override
  public String createUser(UserDetail user) throws Exception {
    try {
      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(user.getDomainId());

      // Create User in specific domain
      String sUserId = domainDriver.createUser(user);
      return sUserId;
    } catch (AdminException e) {
      throw new AdminException(failureOnAdding("user", user.getDisplayedName()), e);
    }
  }

  @Override
  public UserDetail importUser(String userLogin) throws Exception {
    return null;
  }

  @Override
  public void removeUser(String userId) throws Exception {
  }

  @Override
  public UserDetail synchroUser(String userId) throws Exception {
    return null;
  }

  /**
   * Delete given user from Silverpeas
   *
   * @param userId user Id
   * @throws Exception if the user deletion failed.
   */
  @Override
  public void deleteUser(String userId) throws Exception {
    try(Connection connection = DBUtil.openConnection()) {
      // Get the user information
      UserDetail user = userDAO.getUserById(connection, userId);
      if (user == null) {
        throw new AdminException(failureOnDeleting("user", userId));
      }
      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(user.getDomainId());
      // Get User detail from specific domain
      domainDriver.deleteUser(user.getSpecificId());
      // Delete index to given user
      unindexUser(userId);
    } catch (AdminException e) {
      throw new AdminException(failureOnDeleting("user", userId), e);
    }
  }

  /**
* @param user
* @throws Exception
*/
  @Override
  public void updateUserDetail(UserDetail user) throws Exception {
    try {
      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(user.getDomainId());
      // Update User detail in specific domain
      domainDriver.updateUserDetail(user);
    } catch (AdminException e) {
      throw new AdminException(failureOnUpdate("user", user.getId()), e);
    }
  }

  @Override
  public UserDetail getUser(String userId) throws Exception {
    return null;
  }

  /**
* @param user
* @throws Exception
*/
  @Override
  public void updateUserFull(UserFull user) throws Exception {
    try {
      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(user.getDomainId());
      // Update User detail in specific domain
      domainDriver.updateUserFull(user);

      // index informations relative to given user
      indexUser(user.getId());
    } catch (AdminException e) {
      throw new AdminException(failureOnUpdate("user", user.getId()), e);
    }
  }

  private String[] getUserIdsOfDomain(String domainId) throws Exception {
    try(Connection connection = DBUtil.openConnection()) {
      List<String> domainIds = userDAO.getUserIdsInDomain(connection, domainId);
      return domainIds.toArray(new String[domainIds.size()]);
    } catch (SQLException e) {
      throw new AdminException(failureOnGetting("user in domain", domainId), e);
    }
  }

  private <T extends UserDetail> T loadUserEntity(String userId, Class<T> userModelClass)
      throws Exception {
    boolean isUserFull = userModelClass == UserFull.class;
    UserDetail user;
    try(Connection connection = DBUtil.openConnection()) {
      // Get the user information
      final UserDetail silverpeasUser = userDAO.getUserById(connection, userId);
      if (silverpeasUser == null) {
        throw new AdminException(failureOnGetting("user", userId));
      }

      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(silverpeasUser.getDomainId());

      // Get User detail from specific domain
      try {
        user = isUserFull ? domainDriver.getUserFull(silverpeasUser.getSpecificId()) :
            domainDriver.getUser(silverpeasUser.getSpecificId());
      } catch (AdminException e) {
        SilverLogger.getLogger(this)
            .error("Cannot find user " + userId + " in domain " + silverpeasUser.getDomainId(), e);
        user = isUserFull ? new UserFull(domainDriver) : new UserDetail();
        user.setFirstName(silverpeasUser.getFirstName());
        user.setLastName(silverpeasUser.getLastName());
        user.seteMail(silverpeasUser.geteMail());
      }

      // Fill silverpeas info of user details
      user.setLogin(silverpeasUser.getLogin());
      user.setId(userId);
      user.setSpecificId(silverpeasUser.getSpecificId());
      user.setDomainId(silverpeasUser.getDomainId());
      user.setAccessLevel(silverpeasUser.getAccessLevel());
      user.setCreationDate(silverpeasUser.getCreationDate());
      user.setSaveDate(silverpeasUser.getSaveDate());
      user.setVersion(silverpeasUser.getVersion());
      user.setTosAcceptanceDate(silverpeasUser.getTosAcceptanceDate());
      user.setLastLoginDate(silverpeasUser.getLastLoginDate());
      user.setNbSuccessfulLoginAttempts(silverpeasUser.getNbSuccessfulLoginAttempts());
      user.setLastLoginCredentialUpdateDate(silverpeasUser.getLastLoginCredentialUpdateDate());
      user.setExpirationDate(silverpeasUser.getExpirationDate());
      user.setState(silverpeasUser.getState());
      user.setStateSaveDate(silverpeasUser.getStateSaveDate());
      user.setNotifManualReceiverLimit(silverpeasUser.getNotifManualReceiverLimit());

      if (isUserFull) {
        user.setLoginQuestion(silverpeasUser.getLoginQuestion());
        user.setLoginAnswer(silverpeasUser.getLoginAnswer());
      }

    } catch (AdminException e) {
      throw new AdminException(failureOnGetting("user", userId), e);
    }
    return (T) user;
  }

  @Override
  public UserFull getUserFull(String userId) throws Exception {
    return loadUserEntity(userId, UserFull.class);
  }

  @Override
  public String[] getUserMemberGroupIds(String userId) throws Exception {
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  @Override
  public UserDetail[] getAllUsers() throws Exception {
    return null;
  }

  @Override
  public UserDetail[] getUsersBySpecificProperty(String propertyName, String value) throws Exception {
    return new UserDetail[0];
  }

  @Override
  public UserDetail[] getUsersByQuery(Map<String, String> query) throws Exception {
    return new UserDetail[0];
  }

  @Override
  public GroupDetail importGroup(String groupName) throws Exception {
    return null;
  }

  @Override
  public void removeGroup(String groupId) throws Exception {
  }

  @Override
  public GroupDetail synchroGroup(String groupId) throws Exception {
    return null;
  }

  /**
   * @param domainId
   * @return User[]
   * @throws Exception
   */
  public UserDetail[] getAllUsers(String domainId) throws Exception {
    UserDetail[] uds;

    try {
      // Set the OrganizationSchema (if not already done)
      this.holdOrganizationSchema();
      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(domainId);

      // Get User detail from specific domain
      uds = domainDriver.getAllUsers();
    } catch (AdminException e) {
      throw new AdminException(failureOnGetting("users in domain", domainId), e);
    } finally {
      releaseOrganizationSchema();
    }
    return uds;
  }

  /**
   * Indexing all users information of given domain
   * @param domainId
   * @throws Exception
   */
  public void indexAllUsers(String domainId) throws Exception {
    String[] userIds = getUserIdsOfDomain(domainId);
    for (String userId : userIds) {
      indexUser(userId);
    }
  }

  public void indexUser(String userId) {
    getUserIndexation().indexUser(userId);
  }

  public void unindexUser(String userId) {
    getUserIndexation().unindexUser(userId);
  }


  @Override
  public String createGroup(GroupDetail group) throws Exception {
    GroupDetail specificGroup = new GroupDetail(group);
    try {
      // Set supergroup specific Id
      setGroupSpecificId(group, specificGroup);
      // Set subUsers specific Id
      specificGroup.setUserIds(translateUserIdsToSpecificIds(group.getDomainId(), group.
          getUserIds()));
      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(group.getDomainId());

      // Update GroupDetail in specific domain
      return domainDriver.createGroup(specificGroup);
    } catch (AdminException e) {
      throw new AdminException(failureOnAdding("group", group.getName()), e);
    }
  }

  private void setGroupSpecificId(final GroupDetail group, final GroupDetail specificGroup)
      throws AdminException {
    if (StringUtil.isDefined(group.getSuperGroupId())) {
      // Get the user information
      GroupRow gr = getOrganization().group.getGroup(idAsInt(group.getSuperGroupId()));
      if (gr == null) {
        throw new AdminException(unknown("parent group", group.getSuperGroupId()));
      }
      specificGroup.setSuperGroupId(gr.specificId);
    }
  }

  @Override
  public void deleteGroup(String groupId) throws Exception {
    try {
      // Set the OrganizationSchema (if not already done)
      holdOrganizationSchema();

      // Get the group information
      GroupRow gr = getOrganization().group.getGroup(idAsInt(groupId));
      if (gr == null) {
        throw new AdminException(unknown("group", groupId));
      }

      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(idAsString(gr.domainId));

      // Get GroupDetail detail from specific domain
      domainDriver.deleteGroup(gr.specificId);

      // Delete index to given group
      unindexGroup(groupId);

    } catch (AdminException e) {
      throw new AdminException(failureOnDeleting("group", groupId), e);
    } finally {
      releaseOrganizationSchema();
    }
  }

  /**
   * Update given group in specific domain
   * @param group
   */
  @Override
  public void updateGroup(GroupDetail group) throws Exception {
    GroupDetail specificGroup = new GroupDetail(group);

    try {
      // Set supergroup specific Id
      setGroupSpecificId(group, specificGroup);
      // Set subUsers specific Id
      specificGroup.setUserIds(translateUserIdsToSpecificIds(group.getDomainId(), group.
          getUserIds()));

      // Get the group information
      GroupRow gr = getOrganization().group.getGroup(idAsInt(group.getId()));
      if (gr == null) {
        throw new AdminException(unknown("group", group.getId()));
      }
      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(idAsString(gr.domainId));
      specificGroup.setId(gr.specificId);
      // Update GroupDetail in specific domain
      domainDriver.updateGroup(specificGroup);
    } catch (AdminException e) {
      throw new AdminException(failureOnUpdate("group", group.getId()), e);
    }
  }

  /**
   * return group with given id (contains list of user ids for this group)
   * @param groupId
   * @return GroupDetail
   */
  @Override
  public GroupDetail getGroup(String groupId) throws Exception {
    GroupDetail group;

    try {
      // Set the OrganizationSchema (if not already done)
      holdOrganizationSchema();
      // Get the user information
      GroupRow gr = getOrganization().group.getGroup(idAsInt(groupId));
      if (gr == null) {
        throw new AdminException(unknown("group", groupId));
      }
      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(idAsString(gr.domainId));
      // Get GroupDetail detail from specific domain
      group = domainDriver.getGroup(gr.specificId);

      // Fill silverpeas info of group details
      group.setId(groupId);
      group.setSpecificId(gr.specificId);
      group.setDomainId(idAsString(gr.domainId));
    } catch (AdminException e) {
      throw new AdminException(failureOnGetting("group", groupId), e);
    } finally {
      releaseOrganizationSchema();
    }
    return group;
  }

  @Override
  public GroupDetail getGroupByName(String groupName) throws Exception {
    return null;
  }

  /**
   * return group with given group name in domain
   * @param groupName
   * @return GroupDetail
   */
  public GroupDetail getGroupByNameInDomain(String groupName, String domainId) throws Exception {
    try {
      // Set the OrganizationSchema (if not already done)
      holdOrganizationSchema();

      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(domainId);

      // Get the group information without id and userId[]
      return domainDriver.getGroupByName(groupName);

    } catch (AdminException e) {
      throw new AdminException(failureOnGetting("group", groupName), e);
    } finally {
      releaseOrganizationSchema();
    }
  }

  @Override
  public GroupDetail[] getGroups(String groupId) throws Exception {
    GroupDetail[] groups;

    try {
      // Set the OrganizationSchema (if not already done)
      holdOrganizationSchema();

      // Get the user information
      GroupRow gr = getOrganization().group.getGroup(idAsInt(groupId));
      if (gr == null) {
        throw new AdminException(unknown("group", groupId));
      }

      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(idAsString(gr.domainId));

      // Get Groups of GroupDetail from specific domain
      groups = domainDriver.getGroups(gr.specificId);
    } catch (AdminException e) {
      throw new AdminException(failureOnGetting("group", groupId), e);
    } finally {
      releaseOrganizationSchema();
    }
    return groups;
  }

  @Override
  public GroupDetail[] getAllGroups() throws Exception {
    return null;
  }

  @Override
  public GroupDetail[] getAllRootGroups() throws Exception {
    return null;
  }

  @Override
  public String[] getGroupMemberGroupIds(String groupId) throws Exception {
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }


  public String[] getGroupMemberGroupIds(String domainId, String groupId) throws Exception {
    String[] groups;

    try {
      // Set the OrganizationSchema (if not already done)
      holdOrganizationSchema();

      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(domainId);

      // Get GroupDetail from specific domain
      groups = domainDriver.getGroupMemberGroupIds(groupId);
    } catch (AdminException e) {
      throw new AdminException(failureOnGetting("subgroups of group", groupId), e);
    } finally {
      releaseOrganizationSchema();
    }
    return groups;
  }

  public GroupDetail[] getAllRootGroups(String domainId) throws Exception {
    GroupDetail[] groups;

    try {
      // Set the OrganizationSchema (if not already done)
      holdOrganizationSchema();
      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(domainId);
      // Get GroupDetail from specific domain
      groups = domainDriver.getAllRootGroups();
    } catch (AdminException e) {
      throw new AdminException(failureOnGetting("root groups in domain", domainId), e);
    } finally {
      releaseOrganizationSchema();
    }
    return groups;
  }

  public GroupRow[] getAllGroupOfDomain(String domainId) throws Exception {
    try {
      holdOrganizationSchema();
      return getOrganization().group.getAllGroupsOfDomain(Integer.parseInt(domainId));
    } catch (AdminException e) {
      throw new AdminException(failureOnGetting("all groups in domain", domainId), e);
    } finally {
      releaseOrganizationSchema();
    }
  }

  /**
   * Indexing all groups information of given domain
   * @param domainId
   * @throws Exception
   */
  public void indexAllGroups(String domainId) throws Exception {
    GroupRow[] tabGroup = getAllGroupOfDomain(domainId);
    for (GroupRow group : tabGroup) {
      indexGroup(group);
    }
  }

  /**
   * Indexing a group
   * @param group
   */
  public void indexGroup(GroupRow group) {

    FullIndexEntry indexEntry = new FullIndexEntry("groups", "GroupRow", Integer.toString(group.id));
    indexEntry.setLastModificationDate(new Date());
    indexEntry.setTitle(group.name);
    indexEntry.setPreView(group.description);

    // index some group informations
    indexEntry.addField("DomainId", Integer.toString(group.domainId));
    indexEntry.addField("SpecificId", group.specificId);
    indexEntry.addField("SuperGroupId", Integer.toString(group.superGroupId));
    indexEntry.addField("SynchroRule", group.rule);

    IndexEngineProxy.addIndexEntry(indexEntry);
  }

  /**
   * Unindexing a group
   * @param groupId
   */
  public void unindexGroup(String groupId) {
    FullIndexEntry indexEntry = new FullIndexEntry("groups", "GroupRow", groupId);
    IndexEngineProxy.removeIndexEntry(indexEntry.getPK());
  }


  public Map<String, String> authenticate(String sKey) throws Exception {
    return authenticate(sKey, true);
  }

  /**
   * @param sKey anthentication key
   * @param removeKey remove after
   * @return
   * @throws Exception
   */
  public Map<String, String> authenticate(String sKey, boolean removeKey) throws Exception {
    Map<String, String> loginDomainId = new HashMap<String, String>();
    try {
      startTransaction(false);

      // Get the domain information
      KeyStoreRow ksr = getOrganization().keyStore.getRecordByKey(idAsInt(sKey));
      if (ksr == null) {
        throw new AdminException(unknown("authentication key", sKey));
      }

      loginDomainId.put("login", ksr.login);
      loginDomainId.put("domainId", idAsString(ksr.domainId));

      // Remove key from keytore in database
      if (removeKey) {
        getOrganization().keyStore.removeKeyStoreRecord(idAsInt(sKey));
      }

      // Commit transaction
      commit();
      return loginDomainId;
    } catch (AdminPersistenceException e) {
      try {
        this.rollback();
      } catch (Exception e1) {
        SilverTrace.error("admin", "DomainDriverManager.authenticate", "root.EX_ERR_ROLLBACK", e1);
      }
      throw new AdminException(failureOnValidating("authentication key", sKey), e);
    } finally {
      releaseOrganizationSchema();
    }
  }

  public Domain[] getAllDomains() throws AdminException {
    Domain[] valret;
    int i;

    try {
      // Set the OrganizationSchema (if not already done)
      holdOrganizationSchema();

      // Get the domain information
      DomainRow[] drs = getOrganization().domain.getAllDomains();
      if ((drs == null) || (drs.length <= 0)) {
        throw new AdminException("No domains found");
      }

      valret = new Domain[drs.length];
      for (i = 0; i < drs.length; i++) {
        valret[i] = new Domain();
        valret[i].setId(java.lang.Integer.toString(drs[i].id));
        valret[i].setName(drs[i].name);
        valret[i].setDescription(drs[i].description);
        valret[i].setDriverClassName(drs[i].className);
        valret[i].setPropFileName(drs[i].propFileName);
        valret[i].setAuthenticationServer(drs[i].authenticationServer);
        valret[i].setTheTimeStamp(drs[i].theTimeStamp);
      }
    } catch (AdminPersistenceException e) {
      throw new AdminException(failureOnGetting("all domains", ""), e);
    } finally {
      releaseOrganizationSchema();
    }
    return valret;
  }

  public long getDomainActions(String domainId) throws Exception {
    return getDomainDriver(domainId).getDriverActions();
  }

  public String getNextDomainId() throws Exception {
    try {
      startTransaction(false);
      int domainId = getOrganization().domain.getNextId();
      this.commit();
      return idAsString(domainId);
    } catch (AdminException e) {
      try {
        rollback();
      } catch (Exception e1) {
        SilverTrace.error("admin", "DomainDriverManager.getNextDomainId", "root.EX_ERR_ROLLBACK",
            e1);
      }
      throw new AdminException(e);
    } finally {
      releaseOrganizationSchema();
    }
  }

  public String createDomain(Domain theDomain) throws Exception {
    try {
      startTransaction(false);

      DomainRow dr = new DomainRow();
      dr.id = (StringUtil.isInteger(theDomain.getId())) ? Integer.valueOf(theDomain.getId()) : -1;
      dr.name = theDomain.getName();
      dr.description = theDomain.getDescription();
      dr.className = theDomain.getDriverClassName();
      dr.propFileName = theDomain.getPropFileName();
      dr.authenticationServer = theDomain.getAuthenticationServer();
      dr.theTimeStamp = theDomain.getTheTimeStamp();
      dr.silverpeasServerURL = theDomain.getSilverpeasServerURL();

      // Create domain
      getOrganization().domain.createDomain(dr);

      return idAsString(dr.id);
    } catch (AdminException e) {
      throw new AdminException(failureOnAdding("domain", theDomain.getName()), e);
    } finally {
      releaseOrganizationSchema();
    }
  }

  public String updateDomain(Domain theDomain) throws Exception {
    try {
      startTransaction(false);

      DomainRow dr = new DomainRow();
      dr.id = idAsInt(theDomain.getId());
      dr.name = theDomain.getName();
      dr.description = theDomain.getDescription();
      dr.className = theDomain.getDriverClassName();
      dr.propFileName = theDomain.getPropFileName();
      dr.authenticationServer = theDomain.getAuthenticationServer();
      dr.theTimeStamp = theDomain.getTheTimeStamp();
      dr.silverpeasServerURL = theDomain.getSilverpeasServerURL();

      // Create domain
      getOrganization().domain.updateDomain(dr);
      if (domainDriverInstances.get(theDomain.getId()) != null) {
        domainDriverInstances.remove(theDomain.getId());
      }

      return theDomain.getId();
    } catch (AdminException e) {
      throw new AdminException(failureOnUpdate("domain", theDomain.getName()), e);
    } finally {
      releaseOrganizationSchema();
    }
  }

  public String removeDomain(String domainId) throws Exception {
    try {
      startTransaction(false);

      // Remove the domain
      getOrganization().domain.removeDomain(idAsInt(domainId));
      if (domainDriverInstances.get(domainId) != null) {
        domainDriverInstances.remove(domainId);
      }

      return domainId;
    } catch (AdminException e) {
      throw new AdminException(failureOnDeleting("domain", domainId), e);
    } finally {
      releaseOrganizationSchema();
    }
  }

  public Domain getDomain(String domainId) throws Exception {
    Domain valret;

    try {
      // Set the OrganizationSchema (if not already done)
      holdOrganizationSchema();

      // Get the domain information
      DomainRow dr = getOrganization().domain.getDomain(idAsInt(domainId));
      if (dr == null) {
        throw new AdminException(unknown("domain", domainId));
      }

      valret = new Domain();
      valret.setId(Integer.toString(dr.id));
      valret.setName(dr.name);
      valret.setDescription(dr.description);
      valret.setDriverClassName(dr.className);
      valret.setPropFileName(dr.propFileName);
      valret.setAuthenticationServer(dr.authenticationServer);
      valret.setTheTimeStamp(dr.theTimeStamp);
      valret.setSilverpeasServerURL(dr.silverpeasServerURL);
    } catch (AdminPersistenceException e) {
      throw new AdminException(failureOnGetting("domain", domainId), e);
    } finally {
      releaseOrganizationSchema();
    }
    return valret;
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public DomainDriver getDomainDriver(String domainId) throws Exception {
    DomainDriver domainDriver;
    boolean osAllocated = false;
    try {
      domainDriver = domainDriverInstances.get(domainId);
      if (domainDriver == null) {
        // Set the OrganizationSchema (if not already done)
        holdOrganizationSchema();
        osAllocated = true;

        // Get the domain information
        DomainRow dr = getOrganization().domain.getDomain(idAsInt(domainId));
        if (dr == null) {
          throw new AdminException(unknown("driver for domain", domainId));
        }

        // Get the driver class name
        try {
          domainDriver = DomainDriverProvider.getDriver(dr.className);
          domainDriver.init(idAsInt(domainId), dr.propFileName, dr.authenticationServer);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
          throw new AdminException(failureOnGetting("driver of domain", domainId), e);
        }

        // Save DomainDriver instance
        domainDriverInstances.put(domainId, domainDriver);
      }
    } catch (AdminPersistenceException e) {
      throw new AdminException(failureOnGetting("driver of domain", domainId), e);
    } finally {
      if (osAllocated) {
        releaseOrganizationSchema();
      }
    }
    return domainDriver;
  }

  /**
   * Called when Admin starts the synchronization on a particular Domain
   */
  public void beginSynchronization(String sdomainId) throws Exception {
    // Get a DomainDriver instance
    DomainDriver domainDriver = this.getDomainDriver(sdomainId);
    domainDriver.beginSynchronization();
  }

  /**
   * Called when Admin ends the synchronization
   * @param cancelSynchro true if the synchronization is cancelled, false if it ends normally
   */
  public String endSynchronization(String sdomainId, boolean cancelSynchro) throws Exception {
    // Get a DomainDriver instance
    DomainDriver domainDriver = this.getDomainDriver(sdomainId);
    return domainDriver.endSynchronization(cancelSynchro);
  }

  /**
   * Start a new transaction
   */
  @Override
  public void startTransaction(boolean bAutoCommit) {
    try {
      holdOrganizationSchema();
    } catch (AdminException ex) {
      throw new UtilException("DomainDriverManager", "startTransaction", ex);
    }
  }

  /**
   * Commit transaction
   */
  @Override
  public void commit() throws Exception {
    try {
      getOrganization().commit();
    } catch (Exception e) {
      throw new AdminException("Transaction commit failure", e);
    }
  }

  /**
   * Rollback transaction
   */
  @Override
  public void rollback() throws Exception {
    try {
      getOrganization().rollback();
    } catch (Exception e) {
      throw new AdminException("Transaction rollback failure", e);
    }
  }

  /**
   * Start a new transaction in specific domain driver
   */
  public void startTransaction(String domainId, boolean bAutoCommit) throws Exception {
    try {
      // Get a AbstractDomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(domainId);
      // Start transaction
      domainDriver.startTransaction(bAutoCommit);
    } catch (Exception e) {
      throw new AdminException("Fail to start transaction for domain " + domainId, e);
    }
  }

  /**
   * Commit transaction in specific domain driver
   */
  public void commit(String domainId) throws Exception {
    try {
      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(domainId);
      // Commit transaction
      domainDriver.commit();
    } catch (Exception e) {
      throw new AdminException("Fail to commit transaction for domain " + domainId, e);
    }
  }

  /**
   * Rollback transaction in specific domain driver
   */
  public void rollback(String domainId) throws Exception {
    try {
      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(domainId);
      // Commit transaction
      domainDriver.rollback();
    } catch (Exception e) {
      throw new AdminException("Fail to rollback transaction for domain " + domainId, e);
    }
  }

  private String[] translateUserIdsToSpecificIds(String domainId, String[] ids)
      throws AdminException {
    if (ids == null) {
      return null;
    }
    if (ids.length == 0) {
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }

    try(Connection connection = DBUtil.openConnection()) {
      List<UserDetail> users = userDAO.getUsersByCriteria(connection,
          UserSearchCriteriaForDAO.newCriteria().onDomainId(domainId).onUserIds(ids));
      List<String> specificIds =
          users.stream().map(UserDetail::getSpecificId).collect(Collectors.toList());
      return specificIds.toArray(new String[specificIds.size()]);
    } catch (SQLException e) {
      SilverLogger.getLogger(this).error(e);
      throw new AdminException(
          failureOnGetting("users", Arrays.stream(ids).collect(Collectors.joining(","))));
    }
  }

  @Override
  public List<String> getUserAttributes() throws Exception {
    return null;
  }

  public synchronized OrganizationSchema getOrganization() {
    return organizationHolder.get().getOrganizationSchema();
  }

  private UserIndexation getUserIndexation() {
    return ServiceProvider.getService(UserIndexation.class);
  }

  @Override
  public void resetPassword(UserDetail user, String password) throws Exception {
    try {
      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(user.getDomainId());
      // Update User detail in specific domain
      domainDriver.resetPassword(user, password);
    } catch (AdminException e) {
      throw new AdminException("Fail to reset password for user " + user.getId(), e);
    }
  }

  @Override
  public void resetEncryptedPassword(UserDetail user, String encryptedPassword) throws Exception {
    try {
      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(user.getDomainId());
      // Update User detail in specific domain
      domainDriver.resetEncryptedPassword(user, encryptedPassword);
    } catch (AdminException e) {
      throw new AdminException("Fail to reset encrypted password for user " + user.getId(), e);
    }
  }

  private Connection currentConnectionToDataSource() {
    Connection connection = currentConnection.get();
    if (connection == null) {
      // we're not in a domain driver transaction scope but in the scope of the old transactional
      // mechanism
      if (organizationHolder.get() != null &&
          organizationHolder.get().getOrganizationSchema().isOk()) {
        connection = organizationHolder.get().getOrganizationSchema().getConnection();
      }
    }
    return connection;
  }

  private static class OrganizationSchemaHolder {
    private OrganizationSchema organizationSchema;
    private int holdCounter = 0;

    public OrganizationSchemaHolder() throws AdminPersistenceException {
      organizationSchema = OrganizationSchemaPool.getOrganizationSchema();
    }

    public OrganizationSchema getOrganizationSchema() {
      return organizationSchema;
    }

    public boolean isFree() {
      return holdCounter <= 0;
    }

    public void hold() {
      holdCounter++;
    }

    public void release() {
      holdCounter--;
      if (holdCounter <= 0) {
        OrganizationSchemaPool.releaseOrganizationSchema(organizationSchema);
        organizationSchema.close();
      }
    }
  }
}
