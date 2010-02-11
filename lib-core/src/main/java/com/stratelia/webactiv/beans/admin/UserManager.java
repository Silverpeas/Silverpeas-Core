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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
/*
 * @author Norbert CHAIX
 * @version 1.0
 * date 13/08/2001
 */

package com.stratelia.webactiv.beans.admin;

import java.util.List;

import com.silverpeas.util.security.X509Factory;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.organization.UserRow;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class UserManager extends Object {
  /**
   * Constructor
   */
  public UserManager() {
  }

  public int getUsersNumberOfDomain(DomainDriverManager ddManager,
      String domainId) throws AdminException {
    try {
      SilverTrace.info("admin", "UserManager.getUsersNumberOfDomain()",
          "root.MSG_GEN_ENTER_METHOD");
      ddManager.getOrganizationSchema();
      return ddManager.organization.user
          .getUserNumberOfDomain(idAsInt(domainId));
    } catch (Exception e) {
      throw new AdminException("UserManager.getUsersNumberOfDomain()",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USERSET_NUMBER", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  public int getUserNumber(DomainDriverManager ddManager) throws AdminException {
    try {
      SilverTrace.info("admin", "UserManager.getUserNumber()",
          "root.MSG_GEN_ENTER_METHOD");
      ddManager.getOrganizationSchema();
      return ddManager.organization.user.getUserNumber();
    } catch (Exception e) {
      throw new AdminException("UserManager.getUserNumber()",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USERSET_NUMBER", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the users of given profile
   */
  public String[] getUsersOfProfile(DomainDriverManager ddManager,
      String sProfileId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      return ddManager.organization.user
          .getAllUserIdsOfUserRole(idAsInt(sProfileId));
    } catch (Exception e) {
      throw new AdminException("UserManager.getUsersOfProfile",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USERS_OF_PROFILE",
          "profile Id: '" + sProfileId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the users that are in the group or one of his sub-groups
   */
  public UserDetail[] getAllUsersOfGroup(DomainDriverManager ddManager,
      String sGroupId) throws AdminException {
    UserRow[] urs = null;
    UserDetail[] aus = null;

    try {
      ddManager.getOrganizationSchema();
      urs = ddManager.organization.user.getAllUsersOfGroup(idAsInt(sGroupId));
      // Convert UserRow objects in UserDetail Object
      aus = new UserDetail[urs.length];
      for (int nI = 0; nI < urs.length; nI++) {
        aus[nI] = userRow2UserDetail(urs[nI]);
      }

      return aus;
    } catch (Exception e) {
      throw new AdminException("UserManager.getAllUsersOfGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_GROUPS",
          "GroupId = '" + sGroupId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the users of domain
   */
  public UserDetail[] getUsersOfDomain(DomainDriverManager ddManager,
      String sDomainId) throws AdminException {
    UserRow[] urs = null;
    UserDetail[] aus = null;

    try {
      // Get users from Silverpeas
      ddManager.getOrganizationSchema();

      SynchroReport.info("UserManager.getUsersOfDomain()",
          "Recherche des utilisateurs du domaine LDAP dans la base...", null);
      // Get users of domain from Silverpeas database
      urs = ddManager.organization.user.getAllUserOfDomain(idAsInt(sDomainId));

      // Convert UserRow objects in UserDetail Object
      aus = new UserDetail[urs.length];
      for (int nI = 0; nI < urs.length; nI++) {
        aus[nI] = userRow2UserDetail(urs[nI]);
        SynchroReport.debug("UserManager.getUsersOfDomain()",
            "Utilisateur trouvé no : " + Integer.toString(nI) + ", login : "
            + aus[nI].getLogin() + ", " + aus[nI].getFirstName() + ", "
            + aus[nI].getLastName() + ", " + aus[nI].geteMail(), null);
      }
      SynchroReport.info("UserManager.getUsersOfDomain()", "Récupération de "
          + urs.length + " utilisateurs du domaine LDAP dans la base", null);
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
   */
  public String[] getUserIdsOfDomain(DomainDriverManager ddManager,
      String sDomainId) throws AdminException {
    String[] uids = null;

    try {
      // Get users from Silverpeas
      ddManager.getOrganizationSchema();

      // Get user ids of domain from Silverpeas database
      uids = ddManager.organization.user.getUserIdsOfDomain(idAsInt(sDomainId));

      if (uids != null)
        return uids;
      else
        return new String[0];
    } catch (Exception e) {
      throw new AdminException("UserManager.getUserIdsOfDomain",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USERS_OF_DOMAIN",
          "domain Id: '" + sDomainId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  public String[] getUserIdsOfDomainAndAccessLevel(
      DomainDriverManager ddManager, String sDomainId, String accessLevel)
      throws AdminException {
    String[] uids = null;

    try {
      // Get users from Silverpeas
      ddManager.getOrganizationSchema();

      // Get user ids of domain from Silverpeas database
      uids = ddManager.organization.user.getUserIdsOfDomainByAccessLevel(
          idAsInt(sDomainId), accessLevel);

      if (uids != null)
        return uids;
      else
        return new String[0];
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
   * Get the available space ids for given user
   */
  public String[] getAllowedSpaceIds(DomainDriverManager ddManager,
      String sUserId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      return ddManager.organization.user.getAllSpaceIds(idAsInt(sUserId));
    } catch (Exception e) {
      throw new AdminException("UserManager.getAllowedSpaceIds",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_ALLOWED_SPACE_IDS",
          "user Id: '" + sUserId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the available space ids for given user according space father id
   */
  public String[] getAllowedSpaceIdsByFatherId(DomainDriverManager ddManager,
      String sUserId, String fatherId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      return ddManager.organization.user.getSpaceIdsByFatherId(
          idAsInt(sUserId), idAsInt(fatherId));
    } catch (Exception e) {
      throw new AdminException("UserManager.getAllowedSpaceIdsByFatherId",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_ALLOWED_SPACE_IDS",
          "user Id: '" + sUserId + "', fatherId = " + fatherId, e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the available root space ids for given user
   */
  public String[] getAllowedRootSpaceIds(DomainDriverManager ddManager,
      String sUserId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      return ddManager.organization.user.getRootSpaceIds(idAsInt(sUserId));
    } catch (Exception e) {
      throw new AdminException("UserManager.getAllowedRootSpaceIds",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_ALLOWED_SPACE_IDS",
          "user Id = " + sUserId, e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get space ids manageable by given user
   */
  public String[] getManageableSpaceIds(DomainDriverManager ddManager,
      String sUserId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      return ddManager.organization.user
          .getManageableSpaceIds(idAsInt(sUserId));
    } catch (Exception e) {
      throw new AdminException("UserManager.getManageableSpaceIds",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_MANAGEABLE_SPACE_IDS", "user Id: '" + sUserId
          + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get space ids manageable by given user
   */
  public String[] getManageableSubSpaceIds(DomainDriverManager ddManager,
      String sUserId, String sSpaceId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      return ddManager.organization.user.getManageableSubSpaceIds(
          idAsInt(sUserId), idAsInt(sSpaceId));
    } catch (Exception e) {
      throw new AdminException("UserManager.getManageableSubSpaceIds",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_MANAGEABLE_SPACE_IDS", "user Id: '" + sUserId
          + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  public List<String> getManageableGroupIds(DomainDriverManager ddManager,
      String sUserId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      return ddManager.organization.user
          .getManageableGroupIds(idAsInt(sUserId));
    } catch (Exception e) {
      throw new AdminException("UserManager.getManageableSpaceIds",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_MANAGEABLE_GROUP_IDS", "userId = " + sUserId,
          e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Return all the user Ids available in Silverpeas
   */
  public String[] getAllUsersIds(DomainDriverManager ddManager)
      throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      return ddManager.organization.user.getAllUserIds();
    } catch (Exception e) {
      throw new AdminException("UserManager.getAllUsersIds",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_ALL_USER_IDS", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get all the admin Ids available in Silverpeas
   */
  public String[] getAllAdminIds(DomainDriverManager ddManager,
      UserDetail fromUser) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      String[] asAdminIds = ddManager.organization.user
          .getAllAdminIds(fromUser);
      return asAdminIds;
    } catch (Exception e) {
      throw new AdminException("UserManager.getAllAdminIds",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_ALL_ADMIN_IDS", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the user corresponding to the given user Id (only infos in cache table)
   */
  public UserFull getUserFull(DomainDriverManager ddManager, String sUserId)
      throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      return ddManager.getUserFull(sUserId);
    } catch (Exception e) {
      throw new AdminException("UserManager.getUserFull",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_DETAIL",
          "user Id: '" + sUserId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the user corresponding to the given user Id (only infos in cache table)
   */
  public UserDetail getUserDetail(DomainDriverManager ddManager, String sUserId)
      throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      UserRow ur = ddManager.organization.user.getUser(idAsInt(sUserId));

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
   */
  public String getUserIdBySpecificIdAndDomainId(DomainDriverManager ddManager,
      String sSpecificId, String sDomainId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      UserRow ur = ddManager.organization.user.getUserBySpecificId(
          idAsInt(sDomainId), sSpecificId);
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
   */
  public String getUserIdByLoginAndDomain(DomainDriverManager ddManager,
      String sLogin, String sDomainId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      UserRow ur = ddManager.organization.user.getUserByLogin(
          idAsInt(sDomainId), sLogin);
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
    UserRow[] urs = null;
    UserDetail[] aus = null;
    UserRow model;

    try {
      // Get users from Silverpeas
      ddManager.getOrganizationSchema();

      model = userDetail2UserRow(modelUser);
      if ((modelUser.getId() == null) || (modelUser.getId().length() <= 0)) {
        model.id = -2;
      }
      if ((modelUser.getDomainId() == null)
          || (modelUser.getDomainId().length() <= 0)) {
        model.domainId = -2;
      }
      // Get users of domain from Silverpeas database
      urs = ddManager.organization.user.searchUsers(model, isAnd);

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

  public String[] searchUsersIds(DomainDriverManager ddManager, String groupId,
      String componentId, String[] aProfileId, UserDetail modelUser)
      throws AdminException {
    String[] uids = null;
    UserRow model;
    int[] aRoleId = null;

    try {
      // Get users from Silverpeas
      ddManager.getOrganizationSchema();

      model = userDetail2UserRow(modelUser);
      if ((modelUser.getId() == null) || (modelUser.getId().length() <= 0)) {
        model.id = -2;
      }
      if ((modelUser.getDomainId() == null)
          || (modelUser.getDomainId().length() <= 0)) {
        model.domainId = -2;
      }
      if (aProfileId != null) {
        aRoleId = new int[aProfileId.length];
        for (int i = 0; i < aProfileId.length; i++) {
          aRoleId[i] = idAsInt(aProfileId[i]);
        }
      }
      // Get users of domain from Silverpeas database
      uids = ddManager.organization.user.searchUsersIds(idAsInt(groupId),
          idAsInt(componentId), aRoleId, model);

      return uids;
    } catch (Exception e) {
      throw new AdminException("UserManager.searchUsersIdsInGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USERS_OF_DOMAIN", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Add the given user in Silverpeas and specific domain
   */
  public String addUser(DomainDriverManager ddManager, UserDetail userDetail,
      boolean addOnlyInSilverpeas) throws AdminException {
    String specificId = null;

    if (userDetail == null || userDetail.getLastName().length() == 0
        || userDetail.getLogin().length() == 0
        || userDetail.getDomainId().length() == 0) {
      SilverTrace.error("admin", "UserManager.addUser",
          "admin.MSG_ERR_ADD_USER", userDetail.getFirstName() + " "
          + userDetail.getLastName()
          + "domainID, login or lastName is not set");
      if (userDetail.getLastName().length() == 0)
        SynchroReport.error("UserManager.addUser()",
            "Problème lors de l'ajout de l'utilisateur "
            + userDetail.getSpecificId()
            + " dans la base, cet utilisateur n'a pas de nom", null);
      else if (userDetail.getLogin().length() == 0)
        SynchroReport.error("UserManager.addUser()",
            "Problème lors de l'ajout de l'utilisateur "
            + userDetail.getSpecificId()
            + " dans la base, login non spécifié", null);
      else if (userDetail.getDomainId().length() == 0)
        SynchroReport.error("UserManager.addUser()",
            "Problème lors de l'ajout de l'utilisateur "
            + userDetail.getSpecificId()
            + " dans la base, domaine non spécifié", null);

      // SynchroReport.error("UserManager.addUser()", "Utilisateur " +
      // userDetail.getSpecificId() + " non rajouté", null);

      return "";
    }

    try {
      ddManager.getOrganizationSchema();
      SynchroReport.info("UserManager.addUser()", "Ajout de l'utilisateur "
          + userDetail.getSpecificId() + " dans la base...", null);
      // Check that the given login is not already used
      UserRow ur = ddManager.organization.user.getUserByLogin(
          idAsInt(userDetail.getDomainId()), userDetail.getLogin());
      if (ur != null) {
        SynchroReport
            .error(
            "UserManager.addUser()",
            "Utilisateur "
            + userDetail.getLogin()
            + " déjà présent dans la base avec ce login. Il n'a pas été rajouté",
            null);
        throw new AdminException("UserManager.addUser",
            SilverpeasException.ERROR, "admin.EX_ERR_LOGIN_ALREADY_USED",
            "user login: '" + userDetail.getLogin() + "'");
      }

      if (!addOnlyInSilverpeas) {
        // Create user in specific domain
        specificId = ddManager.createUser(userDetail);
        userDetail.setSpecificId(specificId);
      }

      // Create the user node in Silverpeas
      ur = this.userDetail2UserRow(userDetail);
      ddManager.organization.user.createUser(ur);
      String sUserId = idAsString(ur.id);

      // X509 ?
      long domainActions = ddManager.getDomainActions(userDetail.getDomainId());
      boolean isX509Enabled = (domainActions & AbstractDomainDriver.ACTION_X509_USER) != 0;
      if (isX509Enabled) {
        X509Factory
            .buildP12(sUserId, userDetail.getLogin(), userDetail.getLastName(),
            userDetail.getFirstName(), userDetail.getDomainId());
      }

      return sUserId;
    } catch (Exception e) {
      SynchroReport.error("UserManager.addUser()",
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
   */
  public String deleteUser(DomainDriverManager ddManager, UserDetail user,
      boolean onlyInSilverpeas) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      // Delete user from specific domain
      if (!onlyInSilverpeas)
        ddManager.deleteUser(user.getId());

      // Delete the user node from Silverpeas
      SynchroReport.info("UserManager.deleteUser()",
          "Suppression de l'utilisateur " + user.getSpecificId()
          + " de la base...", null);
      ddManager.organization.user.removeUser(idAsInt(user.getId()));

      // X509 ?
      long domainActions = ddManager.getDomainActions(user.getDomainId());
      boolean isX509Enabled = (domainActions & AbstractDomainDriver.ACTION_X509_USER) != 0;
      if (isX509Enabled) {
        X509Factory.revocateUserCertificate(user.getId());
      }

      return user.getId();
    } catch (Exception e) {
      SynchroReport.error("UserManager.deleteUser()",
          "problème à la suppression de l'utilisateur " + user.getFirstName()
          + " " + user.getLastName() + "(specificId:"
          + user.getSpecificId() + ") - " + e.getMessage(), null);
      throw new AdminException("UserManager.deleteUser",
          SilverpeasException.ERROR, "admin.EX_ERR_DELETE_USER", "user id: '"
          + user.getId() + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Update the given user (only in silverpeas)
   */
  public String updateUser(DomainDriverManager ddManager, UserDetail user)
      throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      // make userRow instance
      UserRow ur = this.userDetail2UserRow(user);

      // update the user node in Silverpeas
      SynchroReport.info("UserManager.updateUser()", "Maj de l'utilisateur "
          + user.getSpecificId() + " dans la base...", null);
      ddManager.organization.user.updateUser(ur);

      return user.getId();
    } catch (Exception e) {
      SynchroReport.error("UserManager.updateUser()",
          "problème lors de la maj de l'utilisateur " + user.getFirstName()
          + " " + user.getLastName() + "(specificId:"
          + user.getSpecificId() + ") - " + e.getMessage(), null);
      throw new AdminException("UserManager.updateUser",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_USER", "user id: '"
          + user.getId() + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Update the given user
   */
  public String updateUserFull(DomainDriverManager ddManager, UserFull userFull)
      throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      // update user in specific domain
      ddManager.updateUserFull(userFull);

      // make userRow instance
      UserRow ur = this.userDetail2UserRow((UserDetail) userFull);

      // update the user node in Silverpeas
      ddManager.organization.user.updateUser(ur);

      return userFull.getId();
    } catch (Exception e) {
      throw new AdminException("UserManager.updateUserDetail",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_USER_DETAIL",
          "user id: '" + userFull.getId() + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
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
    ur.accessLevel = user.getAccessLevel();
    ur.loginQuestion = user.getLoginQuestion();
    ur.loginAnswer = user.getLoginAnswer();

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
    user.setAccessLevel(ur.accessLevel);
    user.setLoginQuestion(ur.loginQuestion);
    user.setLoginAnswer(ur.loginAnswer);

    return user;
  }

  /**
   * Convert String Id to int Id
   */
  private int idAsInt(String id) {
    if (id == null || id.length() == 0)
      return -1; // the null id.

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
    return Integer.toString(id);
  }
}