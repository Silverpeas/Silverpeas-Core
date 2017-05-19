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

import org.silverpeas.core.SilverpeasRuntimeException;
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
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
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
public class DomainDriverManager extends AbstractDomainDriver {

  @Inject
  private UserDAO userDAO;
  private ThreadLocal<Connection> currentConnection = new ThreadLocal<>();
  private ThreadLocal<OrganizationSchema> organization = new ThreadLocal<>();

  private Map<String, DomainDriver> domainDriverInstances = new ConcurrentHashMap<>();

  protected DomainDriverManager() {
  }

  // when we are in a transaction the connection must not be released.
  private boolean inTransaction = false;
  private int nbConnected = 0;

  /**
   * Performs the specified management process within a new transaction. This method is required for
   * each management of resources in Silverpeas implying the {@link DomainDriverManager}.
   * <p>
   * If there is already a started transaction, the specified process is then executed within this
   * transaction. Otherwise, a new one is started. The transaction is scoped to the current running
   * thread.
   * </p>
   */
  public <T> T doInTransaction(final Transaction.Process<T> managementProcess) {
    if (currentConnectionToDataSource() == null) {
      // start a new transaction
      try {
        return Transaction.performInNew(() -> {
          try {
            currentConnection.set(DBUtil.openConnection());
          } catch (SQLException e) {
            throw new SilverpeasRuntimeException(e.getMessage(), e);
          }

          return managementProcess.execute();
        });
      } finally {
        Connection connection = currentConnection.get();
        currentConnection.remove();
        DBUtil.close(connection);
      }
    } else {
      // perform the management process in the context of the existing transaction
      return Transaction.performInOne(managementProcess::execute);
    }
  }

  /**
   * Gets the current connection to the data source of Silverpeas.
   * @return the connection that was opened with the starting of a transaction either with
   * {@link DomainDriverManager#holdOrganizationSchema()} or with {@link
   * DomainDriverManager#doInTransaction(Transaction.Process)}.
   */
  public Connection getCurrentConnection() {
    Connection connection = currentConnectionToDataSource();
    if (connection == null) {
      throw new IllegalStateException(
          "Attempt to perform a persistence operation out of a transaction!");
    }
    return connection;
  }


  /**
   * Holds an organization schema from the pool to perform domain management.
   */
  public void holdOrganizationSchema() throws AdminException {
    synchronized (this) {
      if (getOrganization() == null) {
        try {
          organization.set(OrganizationSchemaPool.getOrganizationSchema());
          nbConnected = 0;
        } catch (AdminPersistenceException e) {
          throw new AdminException(failureOnGetting("organization", "schema"), e);
        }
      }
      nbConnected++;
    }
  }

  /**
   * Releases the organization schema that was previously holding.
   */
  public void releaseOrganizationSchema() throws AdminException {
    synchronized (this) {
      nbConnected--;
      if (getOrganization() != null && !inTransaction && nbConnected <= 0) {
        OrganizationSchemaPool.releaseOrganizationSchema(getOrganization());
        getOrganization().close();
        organization.set(null);
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

  /**
   * Delete given user from Silverpeas
   *
   * @param userId user Id
   * @throws Exception if the user deletion failed.
   */
  @Override
  public void deleteUser(String userId) throws Exception {
    try {
      // Get the user information
      UserDetail user = userDAO.getUserById(getCurrentConnection(), userId);
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

  public String[] getUserIdsOfDomain(String domainId) throws Exception {
    try {
      List<String> domainIds = userDAO.getUserIdsInDomain(getCurrentConnection(), domainId);
      return domainIds.toArray(new String[domainIds.size()]);
    } catch (SQLException e) {
      throw new AdminException(failureOnGetting("user in domain", domainId), e);
    }
  }

  private <T extends UserDetail> T loadUserEntity(String userId, Class<T> userModelClass)
      throws Exception {
    boolean isUserFull = userModelClass == UserFull.class;
    UserDetail user;
    try {
      // Get the user information
      final UserDetail silverpeasUser = userDAO.getUserById(getCurrentConnection(), userId);
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

  /**
   *
   * @param userId
   * @return
   * @throws Exception
   */
  public UserDetail getUserDetail(String userId) throws Exception {
    return loadUserEntity(userId, UserDetail.class);
  }

  @Override
  public String[] getUserMemberGroupIds(String userId) throws Exception {
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  /**
*
* @param userIds
* @return
* @throws Exception
*/
  public UserDetail[] getUsers(String[] userIds) throws Exception {
    UserDetail[] uds = new UserDetail[userIds.length];
    try {
      for (int nI = 0; nI < userIds.length; nI++) {
        uds[nI] = this.getUser(userIds[nI]);
      }
    } catch (AdminException e) {
      throw new AdminException(failureOnGetting("users", String.join(", ", userIds)), e);
    }
    return uds;
  }

  /**
* @return User[]
* @throws Exception
*/
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
    UserDetail[] uds = null;

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
*
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

  /**
*
* @param group
* @return
* @throws Exception
*/
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

  /**
*
* @param groupId
* @throws Exception
*/
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
*
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
*
* @param groupId
* @return GroupDetail
*/
  @Override
  public GroupDetail getGroup(String groupId) throws Exception {
    GroupDetail group = null;

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
*
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

  /**
* @param groupId
* @return GroupDetail[]
*/
  @Override
  public GroupDetail[] getGroups(String groupId) throws Exception {
    GroupDetail[] groups = null;

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

  /**
* @return GroupDetail[]
*/
  @Override
  public GroupDetail[] getAllGroups() throws Exception {
    return null;
  }

  /**
*
* @param domainId
* @return GroupDetail[]
* @throws Exception
*/
  public GroupDetail[] getAllGroups(String domainId) throws Exception {
    GroupDetail[] groups = null;
    try {
      // Set the OrganizationSchema (if not already done)
      holdOrganizationSchema();
      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(domainId);

      // Get GroupDetail from specific domain
      groups = domainDriver.getAllGroups();
    } catch (AdminException e) {
      throw new AdminException(failureOnGetting("all groups in domain", domainId), e);
    } finally {
      releaseOrganizationSchema();
    }
    return groups;
  }

  /**
*
* @return @throws Exception
*/
  @Override
  public GroupDetail[] getAllRootGroups() throws Exception {
    return null;
  }

  @Override
  public String[] getGroupMemberGroupIds(String groupId) throws Exception {
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  /**
*
* @param domainId
* @param groupId
* @return
* @throws Exception
*/
  public String[] getGroupMemberGroupIds(String domainId, String groupId) throws Exception {
    String[] groups = null;

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

  /**
*
* @param domainId
* @return
* @throws Exception
*/
  public GroupDetail[] getAllRootGroups(String domainId) throws Exception {
    GroupDetail[] groups = null;

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
*
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
*
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
*
* @param groupId
*/
  public void unindexGroup(String groupId) {
    FullIndexEntry indexEntry = new FullIndexEntry("groups", "GroupRow", groupId);
    IndexEngineProxy.removeIndexEntry(indexEntry.getPK());
  }

  /**
* @param sKey
* @return boolean
*/
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

  /**
* @return Domain[]
*/
  public Domain[] getAllDomains() throws AdminException {
    Domain[] valret = null;
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

  /**
*
* @param domainId
* @return
* @throws Exception
*/
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

  /**
* @param domainId
* @return String
*/
  public Domain getDomain(String domainId) throws Exception {
    Domain valret = null;

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

  /**
* @param domainId
* @return DomainDriver
   */
  public DomainDriver getDomainDriver(String domainId) throws Exception {
    DomainDriver domainDriver = null;
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
*
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
      inTransaction = !bAutoCommit;
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
      inTransaction = false;
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
      inTransaction = false;
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

    try {
      List<UserDetail> users = userDAO.getUsersByCriteria(getCurrentConnection(),
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

  public OrganizationSchema getOrganization() {
    return organization.get();
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
      if (organization.get() != null && organization.get().isOk()) {
        connection = organization.get().getConnection();
      }
    }
    return connection;
  }
}
