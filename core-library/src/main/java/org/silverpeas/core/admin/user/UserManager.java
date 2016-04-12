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

import org.silverpeas.core.admin.domain.synchro.SynchroDomainReport;
import org.silverpeas.core.notification.user.delayed.delegate.DelayedNotificationDelegate;
import org.silverpeas.core.admin.space.dao.SpaceDAO;
import org.silverpeas.core.admin.user.dao.UserDAO;
import org.silverpeas.core.admin.user.dao.UserSearchCriteriaForDAO;
import org.silverpeas.core.admin.persistence.UserRow;
import org.silverpeas.core.admin.domain.AbstractDomainDriver;
import org.silverpeas.core.admin.domain.DomainDriverManager;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.admin.user.notification.UserEventNotifier;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.ListSlice;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.security.encryption.X509Factory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class UserManager {

  @Inject
  private UserDAO userDAO;

  @Inject
  private UserEventNotifier notifier;

  public UserManager() {
  }

  /**
   * Get domains of a login.
   *
   * @param login the login whose domains we want.
   * @return the list of domain ids where the specified login exists.
   * @throws AdminException
   */
  public List<String> getDomainsOfUser(String login) throws AdminException {
    Connection connection = null;
    try {
      connection = DBUtil.openConnection();
      return userDAO.getDomainsContainingLogin(connection, login);
    } catch (Exception e) {
      throw new AdminException("UserManager.getDomainsOfUser", SilverpeasException.ERROR,
          "Couldn't obtain the list of domains for login: " + login, e);
    } finally {
      DBUtil.close(connection);
    }
  }

  public int getUsersNumberOfDomain(DomainDriverManager ddManager, String domainId) throws
      AdminException {
    try {

      ddManager.getOrganizationSchema();
      return ddManager.getOrganization().user.getUserNumberOfDomain(idAsInt(domainId));
    } catch (Exception e) {
      throw new AdminException("UserManager.getUsersNumberOfDomain()",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USERSET_NUMBER", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  public int getUserNumber(DomainDriverManager ddManager) throws AdminException {
    try {

      ddManager.getOrganizationSchema();
      return ddManager.getOrganization().user.getUserNumber();
    } catch (Exception e) {
      throw new AdminException("UserManager.getUserNumber()",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USERSET_NUMBER", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the users that are in the group or one of his sub-groups
   *
   * @param groupIds
   * @return
   * @throws AdminException
   */
  public UserDetail[] getAllUsersOfGroups(List<String> groupIds) throws AdminException {
    if (groupIds == null || groupIds.isEmpty()) {
      return ArrayUtil.EMPTY_USER_DETAIL_ARRAY;
    }
    Connection con = null;
    try {
      con = DBUtil.openConnection();
      List<UserDetail> users = userDAO.getUsersOfGroups(con, groupIds);
      return users.toArray(new UserDetail[users.size()]);
    } catch (Exception e) {
      throw new AdminException("UserManager.getAllUsersOfGroups", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_GROUPS", e);
    } finally {
      DBUtil.close(con);
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
    Connection connection = null;
    try {
      connection = DBUtil.openConnection();
      return userDAO.getUsersByCriteria(connection, criteria);
    } catch (Exception e) {
      throw new AdminException("UserManager.getAllUsersMatching",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_GROUPS", e);
    } finally {
      DBUtil.close(connection);
    }
  }

  /**
   * Get the user ids that are in the group or one of his sub-groups
   *
   * @param groupIds
   * @return
   * @throws AdminException
   */
  public List<String> getAllUserIdsOfGroups(List<String> groupIds) throws AdminException {
    if (groupIds == null || groupIds.isEmpty()) {
      return new ArrayList<String>(0);
    }
    Connection con = null;
    try {
      con = DBUtil.openConnection();
      return userDAO.getUserIdsOfGroups(con, groupIds);
    } catch (Exception e) {
      throw new AdminException("UserManager.getAllUsersOfGroups",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_GROUPS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Get the users of domain
   *
   * @param ddManager
   * @param sDomainId
   * @return
   * @throws AdminException
   */
  public UserDetail[] getUsersOfDomain(DomainDriverManager ddManager, String sDomainId) throws
      AdminException {
    try {
      // Get users from Silverpeas
      ddManager.getOrganizationSchema();
      SynchroDomainReport.info("UserManager.getUsersOfDomain()",
          "Recherche des utilisateurs du domaine LDAP dans la base...");
      // Get users of domain from Silverpeas database
      UserRow[] urs = ddManager.getOrganization().user.getAllUserOfDomain(idAsInt(sDomainId));

      // Convert UserRow objects in UserDetail Object
      UserDetail[] aus = new UserDetail[urs.length];
      for (int nI = 0; nI < urs.length; nI++) {
        aus[nI] = userRow2UserDetail(urs[nI]);
        SynchroDomainReport.debug("UserManager.getUsersOfDomain()",
            "Utilisateur trouvé no : " + java.lang.Integer.toString(nI) + ", login : "
            + aus[nI].getLogin() + ", " + aus[nI].getFirstName() + ", "
            + aus[nI].getLastName() + ", " + aus[nI].geteMail());
      }
      SynchroDomainReport.info("UserManager.getUsersOfDomain()", "Récupération de "
          + urs.length + " utilisateurs du domaine LDAP dans la base");
      return aus;
    } catch (Exception e) {
      throw new AdminException("UserManager.getUsersOfDomain",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USERS_OF_DOMAIN",
          "domain Id: '" + sDomainId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the user ids of domain
   *
   * @param ddManager
   * @param sDomainId
   * @return
   * @throws AdminException
   */
  public String[] getUserIdsOfDomain(DomainDriverManager ddManager, String sDomainId) throws
      AdminException {
    try {
      // Get users from Silverpeas
      ddManager.getOrganizationSchema();
      // Get user ids of domain from Silverpeas database
      String[] uids = ddManager.getOrganization().user.getUserIdsOfDomain(idAsInt(sDomainId));
      if (uids != null) {
        return uids;
      }
      return ArrayUtil.EMPTY_STRING_ARRAY;
    } catch (Exception e) {
      throw new AdminException("UserManager.getUserIdsOfDomain",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USERS_OF_DOMAIN",
          "domain Id: '" + sDomainId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  public String[] getUserIdsOfDomainAndAccessLevel(DomainDriverManager ddManager, String sDomainId,
          UserAccessLevel accessLevel) throws AdminException {
    String[] uids;

    try {
      // Get users from Silverpeas
      ddManager.getOrganizationSchema();
      // Get user ids of domain from Silverpeas database
      uids = ddManager.getOrganization().user.getUserIdsOfDomainByAccessLevel(idAsInt(sDomainId),
          accessLevel);
      if (uids != null) {
        return uids;
      }
      return ArrayUtil.EMPTY_STRING_ARRAY;
    } catch (Exception e) {
      throw new AdminException("UserManager.getUserIdsOfDomain",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USERS_OF_DOMAIN_BY_ACCESSLEVEL", "domain Id: "
          + sDomainId + ", AccessLevel = " + accessLevel, e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get space ids manageable by given user
   *
   * @param sUserId
   * @param groupIds
   * @return
   * @throws AdminException
   */
  public Integer[] getManageableSpaceIds(String sUserId, List<String> groupIds)
      throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();
      List<Integer> spaceIds = SpaceDAO.getManageableSpaceIds(con, sUserId, groupIds);
      return spaceIds.toArray(new Integer[spaceIds.size()]);
    } catch (Exception e) {
      throw new AdminException("UserManager.getManageableSpaceIds", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_MANAGEABLE_SPACE_IDS", "user Id: '" + sUserId + "'", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Return all the user Ids available in Silverpeas
   *
   * @param ddManager
   * @return
   * @throws AdminException
   */
  public String[] getAllUsersIds(DomainDriverManager ddManager) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      return ddManager.getOrganization().user.getAllUserIds();
    } catch (Exception e) {
      throw new AdminException("UserManager.getAllUsersIds", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_ALL_USER_IDS", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get all the admin Ids available in Silverpeas
   *
   * @param ddManager
   * @param fromUser
   * @return
   * @throws AdminException
   */
  public String[] getAllAdminIds(DomainDriverManager ddManager, UserDetail fromUser) throws
      AdminException {
    try {
      ddManager.getOrganizationSchema();
      return ddManager.getOrganization().user.getAllAdminIds(fromUser);
    } catch (Exception e) {
      throw new AdminException("UserManager.getAllAdminIds",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_ALL_ADMIN_IDS", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the user corresponding to the given user Id (only infos in cache table)
   *
   * @param ddManager
   * @param sUserId
   * @return
   * @throws AdminException
   */
  public UserFull getUserFull(DomainDriverManager ddManager, String sUserId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      return ddManager.getUserFull(sUserId);
    } catch (Exception e) {
      throw new AdminException("UserManager.getUserFull", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_DETAIL", "user Id: '" + sUserId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the user corresponding to the given user Id (only infos in cache table)
   *
   * @param ddManager
   * @param sUserId
   * @return
   * @throws AdminException
   */
  public UserDetail getUserDetail(DomainDriverManager ddManager, String sUserId) throws
      AdminException {
    try {
      ddManager.getOrganizationSchema();
      UserRow ur = ddManager.getOrganization().user.getUser(idAsInt(sUserId));
      return userRow2UserDetail(ur);
    } catch (Exception e) {
      throw new AdminException("UserManager.getUser",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USER", "user Id: '"
          + sUserId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the Silverpeas user specific id of user qualified by given login and domain id
   *
   * @param ddManager
   * @param sSpecificId
   * @param sDomainId
   * @return
   * @throws AdminException
   */
  public String getUserIdBySpecificIdAndDomainId(DomainDriverManager ddManager, String sSpecificId,
      String sDomainId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      UserRow ur = ddManager.getOrganization().user.getUserBySpecificId(idAsInt(sDomainId),
          sSpecificId);
      return idAsString(ur.id);
    } catch (Exception e) {
      throw new AdminException("UserManager.getUserIdBySpecificIdAndDomainId",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_BY_LOGIN_DOMAIN",
          "user sSpecificId: '" + sSpecificId + "', domain Id: '" + sDomainId
          + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the Silverpeas user id of user qualified by given login and domain id
   *
   * @param ddManager
   * @param sLogin
   * @param sDomainId
   * @return
   * @throws AdminException
   */
  public String getUserIdByLoginAndDomain(DomainDriverManager ddManager, String sLogin,
      String sDomainId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      UserRow ur = ddManager.getOrganization().user.getUserByLogin(idAsInt(sDomainId), sLogin);
      return idAsString(ur.id);
    } catch (Exception e) {
      throw new AdminException("UserManager.getUserIdByLoginAndDomain",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_BY_LOGIN_DOMAIN",
          "user login: '" + sLogin + "', domain Id: '" + sDomainId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  public UserDetail[] searchUsers(DomainDriverManager ddManager,
      UserDetail modelUser, boolean isAnd) throws AdminException {
    UserRow[] urs;
    UserDetail[] aus;
    UserRow model;

    try {
      // Get users from Silverpeas
      ddManager.getOrganizationSchema();
      model = userDetail2UserRow(modelUser);
      if (!StringUtil.isDefined(modelUser.getId())) {
        model.id = -2;
      }
      if (!StringUtil.isDefined(modelUser.getDomainId())) {
        model.domainId = -2;
      }
      // Get users of domain from Silverpeas database
      urs = ddManager.getOrganization().user.searchUsers(model, isAnd);

      // Convert UserRow objects in UserDetail Object
      aus = new UserDetail[urs.length];
      for (int nI = 0; nI < urs.length; nI++) {
        aus[nI] = userRow2UserDetail(urs[nI]);
      }

      return aus;
    } catch (Exception e) {
      throw new AdminException("UserManager.searchUsers",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USERS_OF_DOMAIN", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  public String[] searchUsersIds(DomainDriverManager ddManager, List<String> userIds,
      UserDetail modelUser) throws AdminException {
    UserRow model;

    try {
      // Get users from Silverpeas
      ddManager.getOrganizationSchema();

      model = userDetail2UserRow(modelUser);
      if (!StringUtil.isDefined(modelUser.getId())) {
        model.id = -2;
      }
      if (!StringUtil.isDefined(modelUser.getDomainId())) {
        model.domainId = -2;
      }

      // Get users of domain from Silverpeas database
      return ddManager.getOrganization().user.searchUsersIds(userIds, model);
    } catch (Exception e) {
      throw new AdminException("UserManager.searchUsersIds",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USERS", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  public void migrateUser(DomainDriverManager ddManager, UserDetail userDetail,
      String targetDomainId) throws AdminException {
    if (userDetail == null || !StringUtil.isDefined(userDetail.getDomainId())) {
      throw new AdminException("UserManager.migrateUser",
          SilverpeasException.ERROR, "admin.EX_MIGRATE_USER", "User detail : " + userDetail);
    }

    try {
      ddManager.getOrganizationSchema();

      // create user in target Domain
      String oldDomainId = userDetail.getDomainId();
      UserFull userFull = getUserFull(ddManager, userDetail.getId());
      userFull.setDomainId(targetDomainId);
      String specificId = ddManager.createUser(userFull);
      userFull.setSpecificId(specificId);

      // User creation may reset password, force reset to old one
      userDetail.setDomainId(targetDomainId);
      userDetail.setSpecificId(specificId);
      ddManager.resetEncryptedPassword(userDetail, userFull.getPassword());

      // remove user from domainSilverpeas
      userFull.setDomainId(oldDomainId);
      ddManager.deleteUser(userFull.getId());

      // associates new user to silverpeas user
      userFull.setDomainId(targetDomainId);
      userFull.setSpecificId(specificId);

      // update user
      updateUser(ddManager, userFull);
    } catch (Exception e) {
      throw new AdminException("UserManager.migrateUser",
          SilverpeasException.ERROR, "admin.EX_ERR_MIGRATE_USER", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Add the given user in Silverpeas and specific domain
   *
   * @param ddManager
   * @param userDetail
   * @param addOnlyInSilverpeas
   * @return
   * @throws AdminException
   */
  public String addUser(DomainDriverManager ddManager, UserDetail userDetail,
      boolean addOnlyInSilverpeas) throws AdminException {
    String specificId;

    if (userDetail == null || !StringUtil.isDefined(userDetail.getLastName())
        || !StringUtil.isDefined(userDetail.getLogin())
        || !StringUtil.isDefined(userDetail.getDomainId())) {
      if (userDetail == null) {
        SynchroDomainReport.error("UserManager.addUser()",
            "Problème lors de l'ajout de l'utilisateur dans la base, cet utilisateur n'existe pas",
            null);
      } else if (!StringUtil.isDefined(userDetail.getLastName())) {
        SynchroDomainReport.error("UserManager.addUser()", "Problème lors de l'ajout de l'utilisateur "
            + userDetail.getSpecificId() + " dans la base, cet utilisateur n'a pas de nom", null);
      } else if (!StringUtil.isDefined(userDetail.getLogin())) {
        SynchroDomainReport.error("UserManager.addUser()", "Problème lors de l'ajout de l'utilisateur "
            + userDetail.getSpecificId() + " dans la base, login non spécifié", null);
      } else if (!StringUtil.isDefined(userDetail.getDomainId())) {
        SynchroDomainReport.error("UserManager.addUser()", "Problème lors de l'ajout de l'utilisateur "
            + userDetail.getSpecificId() + " dans la base, domaine non spécifié", null);
      }
      return "";
    }

    try {
      ddManager.getOrganizationSchema();
      SynchroDomainReport.info("UserManager.addUser()", "Ajout de l'utilisateur "
          + userDetail.getSpecificId() + " dans la base...");
      // Check that the given login is not already used
      UserRow ur = ddManager.getOrganization().user.getUserByLogin(
          idAsInt(userDetail.getDomainId()), userDetail.getLogin());
      if (ur != null) {
        SynchroDomainReport.error("UserManager.addUser()", "Utilisateur " + userDetail.getLogin()
            + " déjà présent dans la base avec ce login. Il n'a pas été rajouté", null);
        throw new AdminException("UserManager.addUser", SilverpeasException.ERROR,
            "admin.EX_ERR_LOGIN_ALREADY_USED", "user login: '" + userDetail.getLogin() + "'");
      }

      if (!addOnlyInSilverpeas) {
        // Create user in specific domain
        specificId = ddManager.createUser(userDetail);
        userDetail.setSpecificId(specificId);
      }

      // Create the user node in Silverpeas
      ur = this.userDetail2UserRow(userDetail);
      ddManager.getOrganization().user.createUser(ur);
      String sUserId = idAsString(ur.id);
      userDetail.setId(sUserId);
      notifier.notifyEventOn(ResourceEvent.Type.CREATION, userDetail);

      // index user information
      ddManager.indexUser(sUserId);

      // X509 ?
      long domainActions = ddManager.getDomainActions(userDetail.getDomainId());
      boolean isX509Enabled = (domainActions & AbstractDomainDriver.ACTION_X509_USER) != 0;
      if (isX509Enabled) {
        X509Factory.buildP12(sUserId, userDetail.getLogin(), userDetail.getLastName(),
            userDetail.getFirstName(), userDetail.getDomainId());
      }

      return sUserId;
    } catch (Exception e) {
      SynchroDomainReport.error("UserManager.addUser()",
          "problème à l'ajout de l'utilisateur " + userDetail.getFirstName()
          + " " + userDetail.getLastName() + "(specificId:"
          + userDetail.getSpecificId() + ") - " + e.getMessage(), null);
      throw new AdminException("UserManager.addUser",
          SilverpeasException.ERROR, "admin.EX_ERR_ADD_USER", "user login: '"
          + userDetail.getLogin() + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Delete the given user
   *
   * @param ddManager
   * @param user
   * @param onlyInSilverpeas
   * @return
   * @throws AdminException
   */
  public String deleteUser(DomainDriverManager ddManager, UserDetail user, boolean onlyInSilverpeas)
      throws AdminException {
    try {
      ddManager.getOrganizationSchema();

      // Send the delayed notifications of the user to delete
      try {
        DelayedNotificationDelegate.executeUserDeleting(Integer.valueOf(user.getId()));
      } catch (Exception e) {
        SynchroDomainReport.warn("UserManager.deleteUser()",
            "problème d'envoi des notifications journalisées "
            + user.getFirstName() + " " + user.getLastName() + "(specificId:"
            + user.getSpecificId() + ") - " + e.getMessage());
      }

      // Delete user from specific domain
      if (!onlyInSilverpeas) {
        ddManager.deleteUser(user.getId());
      }
      // Delete the user node from Silverpeas
      SynchroDomainReport.info("UserManager.deleteUser()", "Suppression de l'utilisateur " + user.
          getSpecificId() + " de la base...");
      ddManager.getOrganization().user.removeUser(idAsInt(user.getId()));
      notifier.notifyEventOn(ResourceEvent.Type.DELETION, user);

      // Delete index of user information
      ddManager.unindexUser(user.getId());

      // X509 ?
      long domainActions = ddManager.getDomainActions(user.getDomainId());
      boolean isX509Enabled = (domainActions & AbstractDomainDriver.ACTION_X509_USER) != 0;
      if (isX509Enabled) {
        X509Factory.revocateUserCertificate(user.getId());
      }

      return user.getId();
    } catch (Exception e) {
      SynchroDomainReport.error("UserManager.deleteUser()", "problème à la suppression de l'utilisateur "
          + user.getFirstName() + " " + user.getLastName() + "(specificId:"
          + user.getSpecificId() + ") - " + e.getMessage(), null);
      throw new AdminException("UserManager.deleteUser", SilverpeasException.ERROR,
          "admin.EX_ERR_DELETE_USER", "user id: '" + user.getId() + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Update the given user (only in silverpeas)
   *
   * @param ddManager
   * @param user
   * @return
   * @throws AdminException
   */
  public String updateUser(DomainDriverManager ddManager, UserDetail user) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      // make userRow instance
      UserRow ur = this.userDetail2UserRow(user);

      // update the user node in Silverpeas
      SynchroDomainReport.info("UserManager.updateUser()", "Maj de l'utilisateur "
          + user.getSpecificId() + " dans la base...");
      ddManager.getOrganization().user.updateUser(ur);

      // index user information
      ddManager.indexUser(user.getId());
      return user.getId();
    } catch (Exception e) {
      SynchroDomainReport.error("UserManager.updateUser()", "problème lors de la maj de l'utilisateur "
          + user.getFirstName() + " " + user.getLastName() + "(specificId:"
          + user.getSpecificId()
          + ") - " + e.getMessage(), null);
      throw new AdminException("UserManager.updateUser", SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_USER", "user id: '" + user.getId() + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Update the given user
   *
   * @param ddManager
   * @param userFull
   * @return
   * @throws AdminException
   */
  public String updateUserFull(DomainDriverManager ddManager, UserFull userFull) throws
      AdminException {
    try {
      ddManager.getOrganizationSchema();
      // update user in specific domain
      ddManager.updateUserFull(userFull);

      // make userRow instance
      UserRow ur = this.userDetail2UserRow(userFull);
      // update the user node in Silverpeas
      ddManager.getOrganization().user.updateUser(ur);

      return userFull.getId();
    } catch (Exception e) {
      throw new AdminException("UserManager.updateUserDetail", SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_USER_DETAIL", "user id: '" + userFull.getId() + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Checks if an existing user already have the given email
   *
   * @param email email to check
   *
   * @return true if at least one user with given email is found
   * @throws AdminException
   */
  public boolean isEmailExisting(DomainDriverManager ddManager, String email) throws AdminException {
    try {
      ddManager.getOrganizationSchema();

      UserRow[] users = ddManager.getOrganization().user.getUsersByEmail(email);

      return ((users != null) && (users.length > 0));
    } catch (Exception e) {
      throw new AdminException("UserManager.isEmailExisting", SilverpeasException.ERROR,
          "admin.CANT_CHECK_EMAIL", "email: '" + email + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get all users (except deleted ones) from all domains
   * @return a List of UserDetail sort by alphabetical order
   * @throws AdminException
   */
  public List<UserDetail> getAllUsers() throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();

      return userDAO.getAllUsers(con);
    } catch (Exception e) {
      throw new AdminException("UserManager.getAllUsers",
              SilverpeasException.ERROR, "admin.MSG_ERR_GET_ALL_USERS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Get all users (except deleted ones) from all domains
   * @return a List of UserDetail sort by reverse creation order
   * @throws AdminException
   */
  public List<UserDetail> getAllUsersFromNewestToOldest() throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();
      return userDAO.getAllUsersFromNewestToOldest(con);
    } catch (Exception e) {
      throw new AdminException("UserManager.getAllUsersFromNewestToOldest",
              SilverpeasException.ERROR, "admin.MSG_ERR_GET_ALL_USERS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Get all users (except deleted ones) from specified domains
   * @return a List of UserDetail sort by alphabetical order
   * @throws AdminException
   */
  public List<UserDetail> getUsersOfDomains(List<String> domainIds) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();

      return userDAO.getUsersOfDomains(con, domainIds);
    } catch (Exception e) {
      throw new AdminException("UserManager.getUsersOfDomains",
              SilverpeasException.ERROR, "admin.MSG_ERR_GET_ALL_USERS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Get all users (except deleted ones) from specified domains
   * @return a List of UserDetail sort by reverse creation order
   * @throws AdminException
   */
  public List<UserDetail> getUsersOfDomainsFromNewestToOldest(List<String> domainIds) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();

      return userDAO.getUsersOfDomainsFromNewestToOldest(con, domainIds);
    } catch (Exception e) {
      throw new AdminException("UserManager.getUsersOfDomains",
              SilverpeasException.ERROR, "admin.MSG_ERR_GET_ALL_USERS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Convert UserDetail to UserRow
   */
  private UserRow userDetail2UserRow(UserDetail user) {
    UserRow ur = new UserRow();
    ur.id = idAsInt(user.getId());
    ur.specificId = user.getSpecificId();
    ur.domainId = idAsInt(user.getDomainId());
    ur.login = user.getLogin();
    ur.firstName = user.getFirstName();
    ur.lastName = user.getLastName();
    ur.eMail = user.geteMail();
    ur.accessLevel = user.getAccessLevel().code();
    ur.loginQuestion = user.getLoginQuestion();
    ur.loginAnswer = user.getLoginAnswer();
    ur.creationDate = user.getCreationDate();
    ur.saveDate = user.getSaveDate();
    ur.version = user.getVersion();
    ur.tosAcceptanceDate = user.getTosAcceptanceDate();
    ur.lastLoginDate = user.getLastLoginDate();
    ur.nbSuccessfulLoginAttempts = user.getNbSuccessfulLoginAttempts();
    ur.lastLoginCredentialUpdateDate = user.getLastLoginCredentialUpdateDate();
    ur.expirationDate = user.getExpirationDate();
    ur.state = user.getState().name();
    ur.stateSaveDate = user.getStateSaveDate();
    ur.notifManualReceiverLimit = user.getNotifManualReceiverLimit();
    return ur;
  }

  /**
   * Convert UserRow to UserDetail
   */
  private UserDetail userRow2UserDetail(UserRow ur) {
    UserDetail user = new UserDetail();
    user.setId(idAsString(ur.id));
    user.setSpecificId(ur.specificId);
    user.setDomainId(idAsString(ur.domainId));
    user.setLogin(ur.login);
    user.setFirstName(ur.firstName);
    user.setLastName(ur.lastName);
    user.seteMail(ur.eMail);
    user.setAccessLevel(UserAccessLevel.fromCode(ur.accessLevel));
    user.setLoginQuestion(ur.loginQuestion);
    user.setLoginAnswer(ur.loginAnswer);
    user.setCreationDate(ur.creationDate);
    user.setSaveDate(ur.saveDate);
    user.setVersion(ur.version);
    user.setTosAcceptanceDate(ur.tosAcceptanceDate);
    user.setLastLoginDate(ur.lastLoginDate);
    user.setNbSuccessfulLoginAttempts(ur.nbSuccessfulLoginAttempts);
    user.setLastLoginCredentialUpdateDate(ur.lastLoginCredentialUpdateDate);
    user.setExpirationDate(ur.expirationDate);
    user.setState(UserState.from(ur.state));
    user.setStateSaveDate(ur.stateSaveDate);
    user.setNotifManualReceiverLimit(ur.notifManualReceiverLimit);
    return user;
  }

  /**
   * Convert String Id to int Id
   */
  private int idAsInt(String id) {
    if (id == null || id.length() == 0) {
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
   */
  private String idAsString(int id) {
    return java.lang.Integer.toString(id);
  }
}