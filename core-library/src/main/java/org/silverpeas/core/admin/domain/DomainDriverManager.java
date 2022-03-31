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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.domain;

import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.persistence.DomainRow;
import org.silverpeas.core.admin.persistence.KeyStoreRow;
import org.silverpeas.core.admin.persistence.OrganizationSchema;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.AdminNotFoundException;
import org.silverpeas.core.admin.user.UserIndexation;
import org.silverpeas.core.admin.user.dao.GroupDAO;
import org.silverpeas.core.admin.user.dao.UserDAO;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserDetailsSearchCriteria;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.Level;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.time.DurationFormatUtils.formatDurationHMS;
import static org.silverpeas.core.SilverpeasExceptionMessages.*;
import static org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery.unique;

/**
 * A manager of domain drivers. It exposes domain related operations and delegates the domain
 * specific tasks to the correct domain driver for a given domain identifier for which the
 * operation is performed.
 */
@Service
@Singleton
@Transactional(Transactional.TxType.MANDATORY)
public class DomainDriverManager extends AbstractDomainDriver {

  public static final String DOMAIN = "domain";
  public static final String GROUP = "group";
  private static final String USERS = "users";
  @Inject
  private UserDAO userDAO;
  @Inject
  private GroupDAO groupDAO;
  @Inject
  private OrganizationSchema organizationSchema;
  private Map<String, DomainDriver> domainDriverInstances = new ConcurrentHashMap<>();

  protected DomainDriverManager() {
  }

  /**
   * Create a new User.
   *
   * @param user
   * @return
   * @throws AdminException
   */
  @Override
  public String createUser(UserDetail user) throws AdminException {
    try {
      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(user.getDomainId());

      // Create User in specific domain
      return domainDriver.createUser(user);
    } catch (AdminException e) {
      throw new AdminException(failureOnAdding("user", user.getDisplayedName()), e);
    }
  }

  @Override
  public UserDetail importUser(String userLogin) throws AdminException {
    return null;
  }

  @Override
  public void removeUser(String userId) throws AdminException {
    // nothing to do
  }

  @Override
  public UserDetail synchroUser(String userId) throws AdminException {
    return null;
  }

  /**
   * Delete given user from Silverpeas
   *
   * @param userId user Id
   * @throws AdminException if the user deletion failed.
   */
  @Override
  public void deleteUser(String userId) throws AdminException {
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
    } catch (SQLException e) {
      throw new AdminException(failureOnDeleting("user", userId), e);
    }
  }

  /**
* @param user
* @throws AdminException
*/
  @Override
  public void updateUserDetail(UserDetail user) throws AdminException {
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
  public UserDetail getUser(String specificId) throws AdminException {
    return null;
  }

  @Override
  public List<UserDetail> listUsers(final Collection<String> specificIds) throws AdminException {
    return emptyList();
  }

  /**
* @param user
* @throws AdminException
*/
  @Override
  public void updateUserFull(UserFull user) throws AdminException {
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

  private String[] getUserIdsOfDomain(String domainId) throws AdminException {
    try(Connection connection = DBUtil.openConnection()) {
      List<String> userIds = userDAO.getUserIdsInDomain(connection, domainId);
      return userIds.toArray(new String[0]);
    } catch (SQLException e) {
      throw new AdminException(failureOnGetting("user in domain", domainId), e);
    }
  }

  @SuppressWarnings("unchecked")
  private <T extends UserDetail> List<T> loadUserEntities(Collection<String> userIds,
      Class<T> userModelClass) throws AdminException {
    long startTime = currentTimeMillis();
    long fetchingSpUsersTime = 0;
    long nbSpUsersFetched = 0;
    long fetchingDomainTime = 0;
    long nbDomainsFetched = 0;
    long fetchingDomainUsersTime = 0;
    long nbDomainUsersFetched = 0;
    long applyingSpDataToDomainUsersTime = 0;
    boolean isUserFull = userModelClass == UserFull.class;
    try(Connection connection = DBUtil.openConnection()) {
      // Get the silverpeas's user information
      long tmpStart = currentTimeMillis();
      final List<UserDetail> silverpeasUsers = userDAO.getUserByIds(connection, userIds);
      if (silverpeasUsers.size() != userIds.size()) {
        throw new AdminException(failureOnGetting(USERS, userIds));
      }
      fetchingSpUsersTime = currentTimeMillis() - tmpStart;
      nbSpUsersFetched = silverpeasUsers.size();
      // Get a DomainDriver instance
      tmpStart = currentTimeMillis();
      final Map<String, Pair<String, DomainDriver>> domainsById = new HashMap<>();
      for(final UserDetail spUser : silverpeasUsers) {
        final String domainId = spUser.getDomainId();
        if (!domainsById.containsKey(domainId)) {
          domainsById.put(domainId, Pair.of(domainId, this.getDomainDriver(domainId)));
        }
      }
      fetchingDomainTime = currentTimeMillis() - tmpStart;
      nbDomainsFetched = domainsById.size();
      tmpStart = currentTimeMillis();
      final Map<String, T> domainUsersBySpecificId = silverpeasUsers
          .stream()
          .collect(groupingBy(u -> domainsById.get(u.getDomainId()), mapping(u -> u, toList())))
          .entrySet()
          .stream()
          // Get User details from specific domain
          .flatMap(e -> toUsersFromDomain(e.getValue(), isUserFull, e.getKey()).stream())
          .collect(toMap(u -> format("%s@%s", u.getSpecificId(), u.getDomainId()), u -> (T) u));
      fetchingDomainUsersTime = currentTimeMillis() - tmpStart;
      nbDomainUsersFetched = domainUsersBySpecificId.size();
      tmpStart = currentTimeMillis();
      final List<T> users = silverpeasUsers.stream()
          .map(u -> {
            final T user = domainUsersBySpecificId.get(format("%s@%s", u.getSpecificId(), u.getDomainId()));
            if (user == null) {
              return null;
            }
            // Fill silverpeas info of user details
            user.setLogin(u.getLogin());
            user.setId(u.getId());
            user.setSpecificId(u.getSpecificId());
            user.setDomainId(u.getDomainId());
            user.setAccessLevel(u.getAccessLevel());
            user.setCreationDate(u.getCreationDate());
            user.setSaveDate(u.getSaveDate());
            user.setVersion(u.getVersion());
            user.setTosAcceptanceDate(u.getTosAcceptanceDate());
            user.setLastLoginDate(u.getLastLoginDate());
            user.setNbSuccessfulLoginAttempts(u.getNbSuccessfulLoginAttempts());
            user.setLastLoginCredentialUpdateDate(u.getLastLoginCredentialUpdateDate());
            user.setExpirationDate(u.getExpirationDate());
            user.setState(u.getState());
            user.setStateSaveDate(u.getStateSaveDate());
            user.setNotifManualReceiverLimit(u.getNotifManualReceiverLimit());
            if (isUserFull) {
              user.setLoginQuestion(u.getLoginQuestion());
              user.setLoginAnswer(u.getLoginAnswer());
            }
            return user;
          })
          .filter(Objects::nonNull)
          .collect(toList());
      applyingSpDataToDomainUsersTime = currentTimeMillis() - tmpStart;
      return users;
    } catch (SQLException e) {
      throw new AdminException(failureOnGetting(USERS, userIds), e);
    } finally {
      final SilverLogger logger = SilverLogger.getLogger(this);
      if (logger.isLoggable(Level.DEBUG)) {
        logger.debug(String.format(
            "Fetching %s %s instances in %s:" +
                "%n\t- fetching %s Silverpeas's users in %s" +
                "%n\t- fetching %s domains in %s" +
                "%n\t- fetching %s domain users in %s" +
                "%n\t- applying Silverpeas's user date to domain users in %s",
            userIds.size(), userModelClass.getSimpleName(), formatDurationHMS(currentTimeMillis() - startTime),
            nbSpUsersFetched, formatDurationHMS(fetchingSpUsersTime),
            nbDomainsFetched, formatDurationHMS(fetchingDomainTime),
            nbDomainUsersFetched, formatDurationHMS(fetchingDomainUsersTime),
            formatDurationHMS(applyingSpDataToDomainUsersTime)
        ));
      }
    }
  }

  @SuppressWarnings("unchecked")
  private <T extends UserDetail> List<T> toUsersFromDomain(final List<UserDetail> silverpeasUsers,
      final boolean isUserFull, final Pair<String, DomainDriver> domainDriver) {
    final Set<String> specificIds = silverpeasUsers.stream()
        .map(UserDetail::getSpecificId)
        .collect(toSet());
    final String identifier = domainDriver.getFirst();
    final DomainDriver driver = domainDriver.getSecond();
    final Mutable<Map<String, T>> domainUsersBySpecificId = Mutable.empty();
    try {
      domainUsersBySpecificId.set((isUserFull ?
          (List<T>) driver.listUserFulls(specificIds) :
          (List<T>) driver.listUsers(specificIds)).stream()
          .map(u -> {
            u.setDomainId(identifier);
            return u;
          })
          .collect(toMap(T::getSpecificId, u -> u)));
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
      domainUsersBySpecificId.set(emptyMap());
    }
    return silverpeasUsers.stream()
        .map(u -> {
          T user = domainUsersBySpecificId.get().get(u.getSpecificId());
          if (user == null) {
            SilverLogger.getLogger(this)
                .error("Cannot find user " + u.getSpecificId() + " in domain " + u.getDomainId());
            user = (T) (isUserFull ? new UserFull(driver) : new UserDetail());
            user.setFirstName(u.getFirstName());
            user.setLastName(u.getLastName());
            user.seteMail(u.geteMail());
          }
          return user;
        })
        .collect(Collectors.toList());
  }

  @Override
  public UserFull getUserFull(String specificId) throws AdminException {
    return unique(listUserFulls(singleton(specificId)));
  }

  @Override
  public List<UserFull> listUserFulls(final Collection<String> specificIds) throws AdminException {
    return loadUserEntities(specificIds, UserFull.class);
  }

  @Override
  public String[] getUserMemberGroupIds(String specificId) throws AdminException {
    return ArrayUtil.emptyStringArray();
  }

  @Override
  public UserDetail[] getAllUsers() throws AdminException {
    return new UserDetail[0];
  }

  @Override
  public UserDetail[] getUsersBySpecificProperty(String propertyName, String value) throws AdminException {
    return new UserDetail[0];
  }

  @Override
  public UserDetail[] getUsersByQuery(Map<String, String> query) throws AdminException {
    return new UserDetail[0];
  }

  @Override
  public GroupDetail importGroup(String groupName) throws AdminException {
    return null;
  }

  @Override
  public void removeGroup(String groupId) throws AdminException {
    // nothing to do
  }

  @Override
  public GroupDetail synchroGroup(String groupId) throws AdminException {
    return null;
  }

  /**
   * @param domainId
   * @return User[]
   * @throws AdminException
   */
  public UserDetail[] getAllUsers(String domainId) throws AdminException {
    UserDetail[] uds;
    try {
      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(domainId);

      // Get User detail from specific domain
      uds = domainDriver.getAllUsers();
    } catch (AdminException e) {
      throw new AdminException(failureOnGetting("users in domain", domainId), e);
    }
    return uds;
  }

  /**
   * Indexing all users information of given domain
   * @param domainId
   * @throws AdminException
   */
  public void indexAllUsers(String domainId) throws AdminException {
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
  public String createGroup(GroupDetail group) throws AdminException {
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
      throw new AdminException(failureOnAdding(GROUP, group.getName()), e);
    }
  }

  private void setGroupSpecificId(final GroupDetail group, final GroupDetail specificGroup)
      throws AdminException {
    if (StringUtil.isDefined(group.getSuperGroupId())) {
      // Get the user information
      try (Connection connection = DBUtil.openConnection()) {
        GroupDetail gr = groupDAO.getGroup(connection, group.getSuperGroupId());
        if (gr == null) {
          throw new AdminException(unknown("parent group", group.getSuperGroupId()));
        }
        specificGroup.setSuperGroupId(gr.getSpecificId());
      } catch (SQLException e) {
        throw new AdminException(e.getMessage(), e);
      }
    }
  }

  @Override
  public void deleteGroup(String groupId) throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      // Get the group information
      GroupDetail gr = groupDAO.getGroup(connection, groupId);
      if (gr == null) {
        throw new AdminException(unknown(GROUP, groupId));
      }

      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(gr.getDomainId());

      // Get GroupDetail detail from specific domain
      domainDriver.deleteGroup(gr.getSpecificId());

      // Delete index to given group
      unindexGroup(groupId);

    } catch (SQLException e) {
      throw new AdminException(failureOnDeleting(GROUP, groupId), e);
    }
  }

  /**
   * Update given group in specific domain
   * @param group
   */
  @Override
  public void updateGroup(GroupDetail group) throws AdminException {
    GroupDetail specificGroup = new GroupDetail(group);

    try (Connection connection = DBUtil.openConnection()) {
      // Set supergroup specific Id
      setGroupSpecificId(group, specificGroup);
      // Set subUsers specific Id
      specificGroup.setUserIds(translateUserIdsToSpecificIds(group.getDomainId(), group.
          getUserIds()));

      // Get the group information
      GroupDetail gr = groupDAO.getGroup(connection, group.getId());
      if (gr == null) {
        throw new AdminException(unknown(GROUP, group.getId()));
      }
      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(gr.getDomainId());
      specificGroup.setId(gr.getSpecificId());
      // Update GroupDetail in specific domain
      domainDriver.updateGroup(specificGroup);
    } catch (SQLException e) {
      throw new AdminException(failureOnUpdate(GROUP, group.getId()), e);
    }
  }

  /**
   * return group with given id (contains list of user ids for this group)
   * @param specificId
   * @return GroupDetail
   */
  @Override
  public GroupDetail getGroup(String specificId) throws AdminException {
    GroupDetail group;

    try (Connection connection = DBUtil.openConnection()) {
      // Get the user information
      GroupDetail gr = groupDAO.getGroup(connection, specificId);
      if (gr == null) {
        throw new AdminException(unknown(GROUP, specificId));
      }
      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(gr.getDomainId());
      // Get GroupDetail detail from specific domain
      group = domainDriver.getGroup(gr.getSpecificId());

      // Fill silverpeas info of group details
      group.setId(specificId);
      group.setSpecificId(gr.getSpecificId());
      group.setDomainId(gr.getDomainId());
    } catch (SQLException e) {
      throw new AdminException(failureOnGetting(GROUP, specificId), e);
    }
    return group;
  }

  @Override
  public GroupDetail getGroupByName(String groupName) throws AdminException {
    return null;
  }

  /**
   * return group with given group name in domain
   * @param groupName
   * @return GroupDetail
   */
  public GroupDetail getGroupByNameInDomain(String groupName, String domainId) throws AdminException {
    try {
      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(domainId);

      // Get the group information without id and userId[]
      return domainDriver.getGroupByName(groupName);

    } catch (AdminException e) {
      throw new AdminException(failureOnGetting(GROUP, groupName), e);
    }
  }

  @Override
  public GroupDetail[] getGroups(String groupId) throws AdminException {
    GroupDetail[] groups;

    try (Connection connection = DBUtil.openConnection()) {
      // Get the user information
      GroupDetail gr = groupDAO.getGroup(connection, groupId);
      if (gr == null) {
        throw new AdminException(unknown(GROUP, groupId));
      }

      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(gr.getDomainId());

      // Get Groups of GroupDetail from specific domain
      groups = domainDriver.getGroups(gr.getSpecificId());
    } catch (SQLException e) {
      throw new AdminException(failureOnGetting(GROUP, groupId), e);
    }
    return groups;
  }

  @Override
  public GroupDetail[] getAllGroups() throws AdminException {
    return new GroupDetail[0];
  }

  @Override
  public GroupDetail[] getAllRootGroups() throws AdminException {
    return new GroupDetail[0];
  }

  @Override
  public String[] getGroupMemberGroupIds(String groupId) throws AdminException {
    return ArrayUtil.emptyStringArray();
  }


  public String[] getGroupMemberGroupIds(String domainId, String groupId) throws AdminException {
    String[] groups;

    try {
      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(domainId);

      // Get GroupDetail from specific domain
      groups = domainDriver.getGroupMemberGroupIds(groupId);
    } catch (AdminException e) {
      throw new AdminException(failureOnGetting("subgroups of group", groupId), e);
    }
    return groups;
  }

  public GroupDetail[] getAllRootGroups(String domainId) throws AdminException {
    GroupDetail[] groups;
    try {
      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(domainId);
      // Get GroupDetail from specific domain
      groups = domainDriver.getAllRootGroups();
    } catch (AdminException e) {
      throw new AdminException(failureOnGetting("root groups in domain", domainId), e);
    }
    return groups;
  }

  public List<GroupDetail> getAllGroupOfDomain(String domainId) throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      return groupDAO.getAllGroupsByDomainId(connection, domainId, false);
    } catch (SQLException e) {
      throw new AdminException(failureOnGetting("all groups in domain", domainId), e);
    }
  }

  /**
   * Indexing all groups information of given domain
   * @param domainId
   * @throws AdminException
   */
  public void indexAllGroups(String domainId) throws AdminException {
    List<GroupDetail> groups = getAllGroupOfDomain(domainId);
    for (GroupDetail group : groups) {
      indexGroup(group);
    }
  }

  /**
   * Indexing a group
   * @param group
   */
  public void indexGroup(GroupDetail group) {
    FullIndexEntry indexEntry = new FullIndexEntry("groups", "GroupRow", group.getId());
    indexEntry.setLastModificationDate(new Date());
    indexEntry.setTitle(group.getName());
    indexEntry.setPreview(group.getDescription());

    // index some group informations
    indexEntry.addField("DomainId", group.getDomainId());
    indexEntry.addField("SpecificId", group.getSpecificId());
    indexEntry.addField("SuperGroupId", group.getSuperGroupId());
    indexEntry.addField("SynchroRule", group.getRule());

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


  public Map<String, String> authenticate(String sKey) throws AdminException {
    return authenticate(sKey, true);
  }

  /**
   * @param sKey anthentication key
   * @param removeKey remove after
   * @return
   * @throws AdminException
   */
  public Map<String, String> authenticate(String sKey, boolean removeKey) throws AdminException {
    Map<String, String> loginDomainId = new HashMap<>();
    try {
      // Get the domain information
      KeyStoreRow ksr = getOrganizationSchema().keyStore().getRecordByKey(idAsInt(sKey));
      if (ksr == null) {
        throw new AdminException(unknown("authentication key", sKey));
      }

      loginDomainId.put("login", ksr.login);
      loginDomainId.put("domainId", idAsString(ksr.domainId));

      // Remove key from keytore in database
      if (removeKey) {
        getOrganizationSchema().keyStore().removeKeyStoreRecord(idAsInt(sKey));
      }

      return loginDomainId;
    } catch (SQLException e) {
      throw new AdminException(failureOnValidating("authentication key", sKey), e);
    }
  }

  public Domain[] getAllDomains() throws AdminException {
    Domain[] valret;
    int i;

    try {
      // Get the domain information
      DomainRow[] drs = getOrganizationSchema().domain().getAllDomains();
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
      }
    } catch (SQLException e) {
      throw new AdminException(failureOnGetting("all domains", ""), e);
    }
    return valret;
  }

  public long getDomainActions(String domainId) throws AdminException {
    return getDomainDriver(domainId).getDriverActions();
  }

  public String getNextDomainId() {
    return idAsString(getOrganizationSchema().domain().getNextId());
  }

  public String createDomain(Domain theDomain) throws AdminException {
    try {
      final DomainRow dr = toDomainRow(theDomain);
      getOrganizationSchema().domain().createDomain(dr);
      return idAsString(dr.id);
    } catch (SQLException e) {
      throw new AdminException(failureOnAdding(DOMAIN, theDomain.getName()), e);
    }
  }

  public String updateDomain(Domain theDomain) throws AdminException {
    try {
      final DomainRow dr = toDomainRow(theDomain);
      getOrganizationSchema().domain().updateDomain(dr);
      if (domainDriverInstances.get(theDomain.getId()) != null) {
        domainDriverInstances.remove(theDomain.getId());
      }
      return theDomain.getId();
    } catch (SQLException e) {
      throw new AdminException(failureOnUpdate(DOMAIN, theDomain.getName()), e);
    }
  }

  private DomainRow toDomainRow(final Domain domain) {
    final DomainRow row = new DomainRow();
    row.id = idAsInt(domain.getId());
    row.name = domain.getName();
    row.description = domain.getDescription();
    row.className = domain.getDriverClassName();
    row.propFileName = domain.getPropFileName();
    row.authenticationServer = domain.getAuthenticationServer();
    row.theTimeStamp = "0";
    row.silverpeasServerURL = domain.getSilverpeasServerURL();
    return row;
  }

  public String removeDomain(String domainId) throws AdminException {
    try {
      // Remove the domain
      getOrganizationSchema().domain().removeDomain(idAsInt(domainId));
      if (domainDriverInstances.get(domainId) != null) {
        domainDriverInstances.remove(domainId);
      }

      return domainId;
    } catch (SQLException e) {
      throw new AdminException(failureOnDeleting(DOMAIN, domainId), e);
    }
  }

  public Domain getDomain(String domainId) throws AdminException {
    Domain valret;
    try {
      // Get the domain information
      DomainRow dr = getOrganizationSchema().domain().getDomain(idAsInt(domainId));
      if (dr == null) {
        throw new AdminNotFoundException(unknown(DOMAIN, domainId));
      }

      valret = new Domain();
      valret.setId(Integer.toString(dr.id));
      valret.setName(dr.name);
      valret.setDescription(dr.description);
      valret.setDriverClassName(dr.className);
      valret.setPropFileName(dr.propFileName);
      valret.setAuthenticationServer(dr.authenticationServer);
      valret.setSilverpeasServerURL(dr.silverpeasServerURL);
    } catch (SQLException e) {
      throw new AdminException(failureOnGetting(DOMAIN, domainId), e);
    }
    return valret;
  }

  @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = AdminException.class)
  public DomainDriver getDomainDriver(String domainId) throws AdminException {
    DomainDriver domainDriver;
    try {
      domainDriver = domainDriverInstances.get(domainId);
      if (domainDriver == null) {
        // Get the domain information
        DomainRow dr = getOrganizationSchema().domain().getDomain(idAsInt(domainId));
        if (dr == null) {
          throw new AdminException(unknown("driver for domain", domainId));
        }

        // Get the driver class name
        domainDriver = getDomainDriver(domainId, dr);

        // Save DomainDriver instance
        domainDriverInstances.put(domainId, domainDriver);
      }
    } catch (SQLException e) {
      throw new AdminException(failureOnGetting("driver of domain", domainId), e);
    }
    return domainDriver;
  }

  private DomainDriver getDomainDriver(final String domainId, final DomainRow dr) throws
      AdminException {
    final DomainDriver domainDriver;
    try {
      domainDriver = DomainDriverProvider.getDriver(dr.className);
      domainDriver.init(idAsInt(domainId), dr.propFileName, dr.authenticationServer);
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException |
        NoSuchMethodException | InvocationTargetException e) {
      throw new AdminException(failureOnGetting("driver of domain", domainId), e);
    }
    return domainDriver;
  }

  /**
   * Called when Admin starts the synchronization on a particular Domain
   */
  public void beginSynchronization(String sdomainId) throws AdminException {
    // Get a DomainDriver instance
    DomainDriver domainDriver = this.getDomainDriver(sdomainId);
    domainDriver.beginSynchronization();
  }

  /**
   * Called when Admin ends the synchronization
   * @param cancelSynchro true if the synchronization is cancelled, false if it ends normally
   */
  public String endSynchronization(String sdomainId, boolean cancelSynchro) throws AdminException {
    // Get a DomainDriver instance
    DomainDriver domainDriver = this.getDomainDriver(sdomainId);
    return domainDriver.endSynchronization(cancelSynchro);
  }

  private String[] translateUserIdsToSpecificIds(String domainId, String[] ids)
      throws AdminException {
    if (ids == null || ids.length == 0) {
      return ArrayUtil.emptyStringArray();
    }

    try(Connection connection = DBUtil.openConnection()) {
      List<UserDetail> users = userDAO.getUsersByCriteria(connection,
          new UserDetailsSearchCriteria().onDomainIds(domainId).onUserIds(ids));
      return users.stream().map(UserDetail::getSpecificId).toArray(String[]::new);
    } catch (SQLException e) {
      throw new AdminException(failureOnGetting(USERS, String.join(",", ids)), e);
    }
  }

  @Override
  public List<String> getUserAttributes() throws AdminException {
    return emptyList();
  }

  private OrganizationSchema getOrganizationSchema() {
    return organizationSchema;
  }

  private UserIndexation getUserIndexation() {
    return ServiceProvider.getService(UserIndexation.class);
  }

  @Override
  public void resetPassword(UserDetail user, String password) throws AdminException {
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
  public void resetEncryptedPassword(UserDetail user, String encryptedPassword) throws AdminException {
    try {
      // Get a DomainDriver instance
      DomainDriver domainDriver = this.getDomainDriver(user.getDomainId());
      // Update User detail in specific domain
      domainDriver.resetEncryptedPassword(user, encryptedPassword);
    } catch (AdminException e) {
      throw new AdminException("Fail to reset encrypted password for user " + user.getId(), e);
    }
  }
}
