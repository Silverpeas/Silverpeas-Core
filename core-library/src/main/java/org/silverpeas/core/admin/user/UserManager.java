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
package org.silverpeas.core.admin.user;

import org.silverpeas.core.admin.domain.AbstractDomainDriver;
import org.silverpeas.core.admin.domain.DomainDriverManager;
import org.silverpeas.core.admin.domain.synchro.SynchroDomainReport;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.space.dao.SpaceDAO;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.dao.UserDAO;
import org.silverpeas.core.admin.user.dao.UserSearchCriteriaForDAO;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.admin.user.notification.UserEventNotifier;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.notification.user.delayed.delegate.DelayedNotificationDelegate;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.security.encryption.X509Factory;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.ListSlice;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.silverpeas.core.SilverpeasExceptionMessages.*;

@Singleton
@Transactional(Transactional.TxType.MANDATORY)
public class UserManager {

  public static final String USERMANAGER_SYNCHRO_REPORT = "UserManager";
  public static final String IN_DOMAIN = "in domain ";
  public static final String ALL_USERS = "all users";
  public static final String SPECIFIC_ID = "(specificId:";
  @Inject
  private UserDAO userDAO;

  @Inject
  private UserEventNotifier notifier;

  @Inject
  private DomainDriverManager domainDriverManager;

  protected UserManager() {
  }

  /**
   * Gets an instance of the {@link UserManager} from the underlying IoD container.
   * @return a {@link UserManager} instance.
   */
  public static UserManager get() {
    return ServiceProvider.getService(UserManager.class);
  }

  /**
   * Gets all the domains in which there is a user with the specified login.
   *
   * @param login the login of a user in a domain.
   * @return the list of domain identifiers in which there is a user with the given login.
   * @throws AdminException if the getting of the domains fails.
   */
  public List<String> getDomainsByUserLogin(String login) throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      return userDAO.getDomainsContainingLogin(connection, login);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("domains having user login", login), e);
    }
  }

  /**
   * Is the user with the specified identifier exists in Silverpeas?
   * @param id the unique identifier of the user to check its existance.
   * @return true if such a user exist or false otherwise.
   */
  public boolean isUserExisting(final String id) throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      return userDAO.isUserByIdExists(connection, id);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("the existence of user", id), e);
    }
  }

  /**
   * Gets the number of users in the specified domain.
   * @param domainId the unique identifier of the domain.
   * @return the user count in the given domain.
   * @throws AdminException if an error occurs while counting the user in the domain.
   */
  public int getNumberOfUsersInDomain(String domainId) throws
      AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      return userDAO.getUserCountByCriteria(connection, UserSearchCriteriaForDAO.newCriteria()
          .onDomainId(domainId)
          .onUserStatesToExclude(UserState.DELETED));
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("user count in domain", domainId), e);
    }
  }

  /**
   * Gets the total number of users in Silverpeas, whatever the domain to which they belong.
   * @return the total number of users in Silverpeas.
   * @throws AdminException if the user counting fails.
   */
  public int getUserCount() throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      return userDAO.getUserCountByCriteria(connection,
          UserSearchCriteriaForDAO.newCriteria().onUserStatesToExclude(UserState.DELETED));
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("total user count", ""), e);
    }
  }

  /**
   * Gets the users that are at least in one of the specified groups or one of their subgroups.
   *
   * @param groupIds the unique identifiers of the groups.
   * @return all the users that are in the specified groups and subgroups.
   * @throws AdminException if the getting of users fails.
   */
  public UserDetail[] getAllUsersInGroups(List<String> groupIds) throws AdminException {
    if (groupIds == null || groupIds.isEmpty()) {
      return new UserDetail[0];
    }
    try (Connection connection = DBUtil.openConnection()) {
      List<UserDetail> users = userDAO.getUsersInGroups(connection, groupIds);
      return users.toArray(new UserDetail[users.size()]);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("users in groups", String.join(", ", groupIds)), e);
    }
  }

  /**
   * Gets the users that match the specified criteria.
   *
   * @param criteria the criteria in searching of user details.
   * @return a slice of the list of user details matching the criteria or an empty list of no ones
   * are found.
   * @throws AdminException if an error occurs while getting the user details.
   */
  public ListSlice<UserDetail> getUsersMatchingCriteria(final UserSearchCriteriaForDAO criteria)
      throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      return userDAO.getUsersByCriteria(connection, criteria);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("users matching some criteria", ""), e);
    }
  }

  /**
   * Gets the identifier of the users that are directly in the specified group. The users that are
   * in the sub groups of the given group aren't taken into account; to take also such users, then
   * use the method {@link UserManager#getAllUserIdsInGroups(List)}.
   * @param groupId the unique identifier of the group.
   * @return a list of user identifiers.
   * @throws AdminException if the getting of user identifiers fails.
   */
  public List<String> getDirectUserIdsInGroup(final String groupId) throws AdminException {
    try(Connection connection = DBUtil.openConnection()) {
      return userDAO.getDirectUserIdsInGroup(connection, groupId);
    } catch(Exception e) {
      throw new AdminException(failureOnGetting("direct users in group", groupId), e);
    }
  }

  /**
   * Gets the identifier of the users that are at least in one of the specified group or one of
   * their sub-groups
   *
   * @param groupIds the idenfifiers of the groups.
   * @return a list of user identifiers.
   * @throws AdminException if the getting fails.
   */
  public List<String> getAllUserIdsInGroups(List<String> groupIds) throws AdminException {
    if (groupIds == null || groupIds.isEmpty()) {
      return new ArrayList<>(0);
    }
    try (Connection connection = DBUtil.openConnection()) {
      return userDAO.getUserIdsInGroups(connection, groupIds);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("users in groups", String.join(", ", groupIds)), e);
    }
  }

  public List<String> getDirectUserIdsInRole(final String roleId) throws AdminException {
    try(Connection connection = DBUtil.openConnection()) {
      return userDAO.getUserIdsByUserRole(connection, roleId);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("users in role", roleId), e);
    }
  }

  public List<String> getDirectUserIdsInSpaceRole(final String spaceRoleId) throws AdminException {
    try(Connection connection = DBUtil.openConnection()) {
      return userDAO.getUserIdsBySpaceUserRole(connection, spaceRoleId);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("users in space role", spaceRoleId), e);
    }
  }

  public List<String> getDirectUserIdsInGroupRole(final String groupRoleId) throws AdminException {
    try(Connection connection = DBUtil.openConnection()) {
      return userDAO.getDirectUserIdsByGroupUserRole(connection, groupRoleId);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("users in group role", groupRoleId), e);
    }
  }

  /**
   * Gets all the users that belong to the specified domain in order to perform a synchronization
   * with the service backing the domain.
   * @param sDomainId the unique identifier of a domain in Silverpeas.
   * @return an array with all the users in that domain.
   * @throws AdminException if the getting fails.
   */
  public UserDetail[] getAllUsersInDomain(String sDomainId) throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      final String usersOfDomain = ".getAllUsersInDomain()";
      SynchroDomainReport.info(USERMANAGER_SYNCHRO_REPORT + usersOfDomain,
          "Recherche des utilisateurs du domaine LDAP (domaine " + sDomainId + ") dans la base...");
      // Get users of domain from Silverpeas database
      ListSlice<UserDetail> users = userDAO.getUsersByCriteria(connection,
          UserSearchCriteriaForDAO.newCriteria()
              .onDomainId(sDomainId)
              .onUserStatesToExclude(UserState.DELETED));

      UserDetail[] usersInDomain = new UserDetail[users.size()];
      int i = 0;
      for (UserDetail u : users) {
        usersInDomain[i++] = u;
        SynchroDomainReport.debug(USERMANAGER_SYNCHRO_REPORT + usersOfDomain,
            "Utilisateur trouvé no : " + i + ", login : " + u.getLogin() + ", " + u.getFirstName() +
                ", " + u.getLastName() + ", " + u.geteMail());
      }
      SynchroDomainReport.info(USERMANAGER_SYNCHRO_REPORT + usersOfDomain,
          "Récupération de " + users.size() + " utilisateurs du domaine LDAP dans la base");
      return usersInDomain;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("users in domain", sDomainId), e);
    }
  }

  /**
   * Gets the identifier of all the users that belong to the specified domain.
   * @param sDomainId the unique identifier of the domain in Silverpeas.
   * @return an array with all the user identifiers.
   * @throws AdminException if the getting of the user identifiers fails.
   */
  public List<String> getAllUserIdsInDomain(String sDomainId) throws
      AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      // Get user ids of domain from Silverpeas database
      return userDAO.getUserIdsInDomain(connection, sDomainId);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("users in domain", sDomainId), e);
    }
  }

  public String[] getUserIdsByAccessLevel(UserAccessLevel accessLevel) throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      List<String> userIds = userDAO.getUserIdsByAccessLevel(connection, accessLevel);
      if (userIds.isEmpty()) {
        return ArrayUtil.EMPTY_STRING_ARRAY;
      }
      return userIds.toArray(new String[userIds.size()]);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("users with access level ", accessLevel.getName()),
          e);
    }
  }

  /**
   * Gets the identifier of all the users that belong to the specified domain and that have the
   * given access level.
   * @param sDomainId the unique identifier of a domain in Silverpeas.
   * @param accessLevel the access level of the users to get.
   * @return an array with all the user identifiers.
   * @throws AdminException if the getting of the user identifiers fails.
   */
  public String[] getUserIdsByDomainAndByAccessLevel(String sDomainId,
          UserAccessLevel accessLevel) throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      List<String> userIds =
          userDAO.getUserIdsByAccessLevelInDomain(connection, accessLevel, sDomainId);
      if (userIds.isEmpty()) {
        return ArrayUtil.EMPTY_STRING_ARRAY;
      }
      return userIds.toArray(new String[userIds.size()]);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("users with access level " + accessLevel.getName(),
          IN_DOMAIN + sDomainId), e);
    }
  }

  /**
   * Gets the identifiers of all the spaces that are manageable by given user and by the specified
   * groups of users.
   * @param sUserId the unique identifier of the space managers.
   * @param groupIds the unique identifiers of the groups in which users are space managers.
   * @return an array with the identifiers of the spaces.
   * @throws AdminException if an error occurs while getting the space identifiers.
   */
  public Integer[] getManageableSpaceIds(String sUserId, List<String> groupIds)
      throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      List<Integer> spaceIds = SpaceDAO.getManageableSpaceIds(connection, sUserId, groupIds);
      return spaceIds.toArray(new Integer[spaceIds.size()]);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("spaces manageable by user", sUserId), e);
    }
  }

  /**
   * Gets the identifier of all the users in Silverpeas.
   * @return an array with the identifier of all the users in Silverpeas.
   * @throws AdminException if an error occurs while getting all the user identifiers.
   */
  public List<String> getAllUsersIds() throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      return userDAO.getAllUserIds(connection);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting(ALL_USERS, ""), e);
    }
  }

  /**
   * Gets the identifier of all the administrators in the Silverpeas domain of the specified user.
   * These administrators are the users that have the administrative access right or that are
   * the domain manager.
   * @param fromUser the user from which the query for administrators are performed. The
   * administrators must be in the same domain than this user.
   * @return an array with the identifier of all the administrators in the domain of the given user.
   * @throws AdminException if an error occurs while querying the administrators.
   */
  public String[] getAllAdminIds(UserDetail fromUser) throws
      AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      List<String> adminIds = userDAO.getAllAdminIds(connection, fromUser);
      return adminIds.toArray(new String[adminIds.size()]);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("admin users", ""), e);
    }
  }

  /**
   * Get full information about the user with the given unique identifier (only info in cache table)
   * from its domain.
   * @param sUserId the unique identifier of the user to get.
   * @return a {@link UserFull} instance.
   * @throws AdminException if an error occurs while getting the user.
   */
  public UserFull getUserFull(String sUserId) throws AdminException {
    try {
      return domainDriverManager.getUserFull(sUserId);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("user", sUserId), e);
    }
  }

  /**
   * Get the user corresponding to the given user Id (only infos in cache table).
   * <p>If the user does not exists, null is returned.</p>
   * @param sUserId the identifier of searched identifier.
   * @return the corresponding {@link UserDetail} instance if any, null otherwise.
   * @throws AdminException on technical error.
   */
  public UserDetail getUserDetail(String sUserId) throws
      AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      return userDAO.getUserById(connection, sUserId);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("user", sUserId), e);
    }
  }

  /**
   * Gets the unique identifier of the user in Silverpeas having the specified specific identifier
   * in the given domain.
   * <p>If the user does not exists, null is returned.</p>
   * @param sSpecificId the specific identifier of the searched user in the given domain.
   * @param sDomainId the identifier of the domain the user belongs to.
   * @return the corresponding {@link UserDetail} instance if any, null otherwise.
   * @throws AdminException on technical error.
   */
  public String getUserIdBySpecificIdAndDomainId(String sSpecificId, String sDomainId)
      throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      UserDetail user = userDAO.getUserBySpecificId(connection, sDomainId, sSpecificId);
      return user != null ? user.getId() : null;
    } catch (Exception e) {
      throw new AdminException(
          failureOnGetting("user with specific id " + sSpecificId, IN_DOMAIN + sDomainId), e);
    }
  }

  /**
   * Gets all the users having the specified unique identifier for the given domain.
   * @param specificIds an array of domain specific identifiers.
   * @param domainId the unique identifier of the domain.
   * @return a list of users matching the identifiers specific to the given domain.
   */
  public List<UserDetail> getUsersBySpecificIdsAndDomainId(final List<String> specificIds,
      final String domainId) throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      return userDAO.getUsersBySpecificIds(connection, domainId, specificIds);
    } catch (Exception e) {
      String sSpecificIds = specificIds.stream().collect(Collectors.joining(", "));
      throw new AdminException(
          failureOnGetting("users with specific ids " + sSpecificIds, IN_DOMAIN + domainId), e);
    }
  }

  /**
   * Gets the unique identifier of the user in Silverpeas that is qualified by the given login and
   * that belongs to the specified domain.
   * <p>If the user does not exists, null is returned.</p>
   * @param sLogin the login of the searched user in the specified domain.
   * @param sDomainId the identifier of the domain the user belongs to.
   * @return the corresponding {@link UserDetail} instance if any, null otherwise.
   * @throws AdminException on technical error.
   */
  public String getUserIdByLoginAndDomain(String sLogin, String sDomainId) throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      UserDetail user = userDAO.getUserByLogin(connection, sDomainId, sLogin);
      return user != null ? user.getId() : null;
    } catch (Exception e) {
      throw new AdminException(
          failureOnGetting("users with login " + sLogin, IN_DOMAIN + sDomainId), e);
    }
  }

  public void migrateUser(UserDetail userDetail, String targetDomainId) throws AdminException {
    if (userDetail == null || !StringUtil.isDefined(userDetail.getDomainId())) {
      throw new AdminException(undefined("user"));
    }

    try {
      // create user in target Domain
      String oldDomainId = userDetail.getDomainId();
      UserFull userFull = getUserFull(userDetail.getId());
      userFull.setDomainId(targetDomainId);
      String specificId = domainDriverManager.createUser(userFull);
      userFull.setSpecificId(specificId);

      // User creation may reset password, force reset to old one
      userDetail.setDomainId(targetDomainId);
      userDetail.setSpecificId(specificId);
      domainDriverManager.resetEncryptedPassword(userDetail, userFull.getPassword());

      // remove user from domainSilverpeas
      userFull.setDomainId(oldDomainId);
      domainDriverManager.deleteUser(userFull.getId());

      // associates new user to silverpeas user
      userFull.setDomainId(targetDomainId);
      userFull.setSpecificId(specificId);

      // update user
      updateUser(userFull);
    } catch (Exception e) {
      throw new AdminException(failureOnUpdate("user", userDetail.getId()), e);
    }
  }

  /**
   * Adds the given user in Silverpeas and optionally in the specified domain in Silverpeas (this
   * only works with domains that can be directly managed by Silverpeas).
   * @param userDetail the detail about the user to add.
   * @param addOnlyInSilverpeas does the user be registered into only Silverpeas? If false, the user
   * will be also registered into the domain the user has to belong to.
   * @return the unique identifier of the added user. This identifier is set by the registering
   * process.
   * @throws AdminException if the user registering fails.
   */
  public String addUser(UserDetail userDetail, boolean addOnlyInSilverpeas) throws AdminException {
    final String addUser = ".addUser()";
    final String pbAddUser = "Problème lors de l'ajout de l'utilisateur ";
    if (userDetail == null || !StringUtil.isDefined(userDetail.getLastName())
        || !StringUtil.isDefined(userDetail.getLogin())
        || !StringUtil.isDefined(userDetail.getDomainId())) {
      if (userDetail == null) {
        SynchroDomainReport.error(USERMANAGER_SYNCHRO_REPORT + addUser,
            pbAddUser + "dans la base, cet utilisateur n'existe pas",
            null);
      } else if (!StringUtil.isDefined(userDetail.getLastName())) {
        SynchroDomainReport.error(USERMANAGER_SYNCHRO_REPORT + addUser, pbAddUser
            + userDetail.getSpecificId() + " dans la base, cet utilisateur n'a pas de nom", null);
      } else if (!StringUtil.isDefined(userDetail.getLogin())) {
        SynchroDomainReport.error(USERMANAGER_SYNCHRO_REPORT + addUser, pbAddUser
            + userDetail.getSpecificId() + " dans la base, login non spécifié", null);
      } else if (!StringUtil.isDefined(userDetail.getDomainId())) {
        SynchroDomainReport.error(USERMANAGER_SYNCHRO_REPORT + addUser, pbAddUser
            + userDetail.getSpecificId() + " dans la base, domaine non spécifié", null);
      }
      return "";
    }

    try(Connection connection = DBUtil.openConnection()) {
      SynchroDomainReport.info(USERMANAGER_SYNCHRO_REPORT + addUser,
          "Ajout de l'utilisateur " + userDetail.getSpecificId() + " dans la base...");
      UserDetail user = userDAO.getUserByLogin(connection, userDetail.getDomainId(),
          userDetail.getLogin());
      if (user != null) {
        SynchroDomainReport.error(USERMANAGER_SYNCHRO_REPORT + addUser,
            "Utilisateur " + userDetail.getLogin() +
                " déjà présent dans la base avec ce login. Il n'a pas été rajouté", null);
        throw new AdminException(failureOnAdding("user", userDetail.getLogin()));
      }

      if (!addOnlyInSilverpeas) {
        // Create user in specific domain
        String specificId = domainDriverManager.createUser(userDetail);
        userDetail.setSpecificId(specificId);
      }

      String userId = userDAO.saveUser(connection, userDetail);
      userDetail.setId(userId);

      notifier.notifyEventOn(ResourceEvent.Type.CREATION, userDetail);

      domainDriverManager.indexUser(userDetail.getId());

      // X509?
      long domainActions = domainDriverManager.getDomainActions(userDetail.getDomainId());
      boolean isX509Enabled = (domainActions & AbstractDomainDriver.ACTION_X509_USER) != 0;
      if (isX509Enabled) {
        X509Factory.buildP12(userDetail.getId(), userDetail.getLogin(), userDetail.getLastName(),
            userDetail.getFirstName(), userDetail.getDomainId());
      }

      return userDetail.getId();
    } catch (Exception e) {
      SynchroDomainReport.error(USERMANAGER_SYNCHRO_REPORT + addUser,
          pbAddUser + userDetail.getFirstName() + " " + userDetail.getLastName() + SPECIFIC_ID +
              userDetail.getSpecificId() + ") - " + e.getMessage(), null);
      throw new AdminException(failureOnAdding("user", userDetail.getLogin()), e);
    }
  }

  /**
   * Deletes the given user in Silverpeas and optionally in the domain he belongs to (this
   * only works with domains that can be directly managed by Silverpeas).
   * @param user the user to delete.
   * @param onlyInSilverpeas the user has to be deleted only in Silverpeas or also in the domain
   * he belongs to?
   * @return the unique identifier of the deleted user.
   * @throws AdminException if the deletion fails.
   */
  public String deleteUser(UserDetail user, boolean onlyInSilverpeas)
      throws AdminException {
    final String deleteUser = ".deleteUser()";
    try(Connection connection = DBUtil.openConnection()) {
      // Send the delayed notifications of the user to delete
      delayedNotificationOfUserDeletion(user);

      // Delete user from specific domain
      if (!onlyInSilverpeas) {
        domainDriverManager.deleteUser(user.getId());
      }
      // Delete the user node from Silverpeas
      SynchroDomainReport.info(USERMANAGER_SYNCHRO_REPORT + deleteUser,
          "Suppression de l'utilisateur " + user.
              getSpecificId() + " de la base...");
      userDAO.deleteUser(connection, user);
      notifier.notifyEventOn(ResourceEvent.Type.DELETION, user);

      // Delete index of user information
      domainDriverManager.unindexUser(user.getId());

      // X509?
      long domainActions = domainDriverManager.getDomainActions(user.getDomainId());
      boolean isX509Enabled = (domainActions & AbstractDomainDriver.ACTION_X509_USER) != 0;
      if (isX509Enabled) {
        X509Factory.revocateUserCertificate(user.getId());
      }

      return user.getId();
    } catch (Exception e) {
      SynchroDomainReport.error(USERMANAGER_SYNCHRO_REPORT + deleteUser,
          "problème à la suppression de l'utilisateur " + user.getFirstName() + " " +
              user.getLastName() + SPECIFIC_ID + user.getSpecificId() + ") - " + e.getMessage(),
          null);
      throw new AdminException(failureOnDeleting("user", user.getId()), e);
    }
  }

  private void delayedNotificationOfUserDeletion(final UserDetail user) {
    try {
      DelayedNotificationDelegate.executeUserDeleting(Integer.valueOf(user.getId()));
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
      SynchroDomainReport.warn(USERMANAGER_SYNCHRO_REPORT + ".delayedNotificationOfUserDeletion()",
          "problème d'envoi des notifications journalisées " + user.getFirstName() + " " +
              user.getLastName() + SPECIFIC_ID + user.getSpecificId() + ") - " + e.getMessage());
    }
  }

  /**
   * Updates the given user (only in silverpeas, the user isn't updated in the domain he belongs
   * to).
   * @param user the user to update.
   * @return the unique identifier of the user that was updated.
   * @throws AdminException if an error occurs while updating the user.
   */
  public String updateUser(UserDetail user) throws AdminException {
    try(Connection connection = DBUtil.openConnection()) {
      // update the user node in Silverpeas
      SynchroDomainReport.info("UserManager.updateUser()",
          "Maj de l'utilisateur " + user.getSpecificId() + " dans la base...");
      userDAO.updateUser(connection, user);

      // index user information
      domainDriverManager.indexUser(user.getId());
      return user.getId();
    } catch (Exception e) {
      SynchroDomainReport.error("UserManager.updateUser()",
          "problème lors de la maj de l'utilisateur " + user.getFirstName() + " " +
              user.getLastName() + SPECIFIC_ID + user.getSpecificId() + ") - " + e.getMessage(),
          null);
      throw new AdminException(failureOnUpdate("user", user.getId()), e);
    }
  }

  /**
   * Updates the given user both in Silverpeas and in the domain to which the user belongs.
   *
   * @param userFull the user with full information about him.
   * @return the unique identifier of the updated user.
   * @throws AdminException if the update fails.
   */
  public String updateUserFull(UserFull userFull) throws AdminException {
    try(Connection connection = DBUtil.openConnection()) {
      domainDriverManager.updateUserFull(userFull);
      userDAO.updateUser(connection, userFull);
      return userFull.getId();
    } catch (Exception e) {
      throw new AdminException(failureOnUpdate("user", userFull.getId()), e);
    }
  }

  /**
   * Checks if an existing user already have the given email.
   * @param email email to check
   *
   * @return true if at least one user with given email is found. False otherwise.
   * @throws AdminException if the checking fails.
   */
  public boolean isEmailExisting(final String email) throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      return userDAO.isUserEmailExisting(connection, email);
    } catch (Exception e) {
      throw new AdminException(unknown("user with email", email), e);
    }
  }

  /**
   * Gets all users (except deleted ones) from all domains.
   * @return a List of UserDetail sort by alphabetical order
   * @throws AdminException if getting all the users fails.
   */
  public List<UserDetail> getAllUsers() throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      return userDAO.getAllUsers(connection);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting(ALL_USERS, ""), e);
    }
  }

  /**
   * Get all users (except deleted ones) from all domains
   * @return a List of UserDetail sort by reverse creation order
   * @throws AdminException if the getting fails.
   */
  public List<UserDetail> getAllUsersFromNewestToOldest() throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      return userDAO.getAllUsersFromNewestToOldest(connection);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting(ALL_USERS, ""), e);
    }
  }

  /**
   * Get all users (except deleted ones) from specified domains
   * @return a List of UserDetail sort by alphabetical order
   * @throws AdminException if getting all the users in the specified domains fails.
   */
  public List<UserDetail> getUsersOfDomains(List<String> domainIds) throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      return userDAO.getUsersOfDomains(connection, domainIds);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("users in domains", String.join(", ", domainIds)),
          e);
    }
  }

  /**
   * Get all users (except deleted ones) from specified domains
   * @return a List of UserDetail sort by reverse creation order
   * @throws AdminException if the getting of all the users in the specified domains fails.
   */
  public List<UserDetail> getUsersOfDomainsFromNewestToOldest(List<String> domainIds) throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      return userDAO.getUsersOfDomainsFromNewestToOldest(connection, domainIds);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting(ALL_USERS, ""), e);
    }
  }
}