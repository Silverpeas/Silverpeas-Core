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
package com.silverpeas.socialNetwork.myProfil.control;

import com.silverpeas.SilverpeasServiceProvider;
import com.silverpeas.jobDomainPeas.JobDomainSettings;
import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.socialNetwork.SocialNetworkException;
import com.silverpeas.socialNetwork.invitation.Invitation;
import com.silverpeas.socialNetwork.invitation.InvitationService;
import com.silverpeas.socialNetwork.invitation.model.InvitationUser;
import com.silverpeas.socialNetwork.relationShip.RelationShipService;
import com.silverpeas.ui.DisplayI18NHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.authentication.AuthenticationException;
import com.stratelia.silverpeas.authentication.LoginPasswordAuthentication;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AbstractDomainDriver;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.util.exception.SilverpeasException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Bensalem Nabil
 */
public class MyProfilSessionController extends AbstractComponentSessionController {

  private AdminController m_AdminCtrl = null;
  private RelationShipService relationShipService = new RelationShipService();
  private InvitationService invitationService = null;
  private long domainActions = -1;

  public MyProfilSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl,
        componentContext,
        "com.silverpeas.socialNetwork.multilang.socialNetworkBundle",
        "com.silverpeas.socialNetwork.settings.socialNetworkIcons",
        "com.silverpeas.socialNetwork.settings.socialNetworkSettings");
    m_AdminCtrl = new AdminController(getUserId());
    invitationService = new InvitationService();
  }

  /**
   * get all RelationShips ids for this user
   *
   * @return:List<String>
   * @param: int myId
   */
  public List<String> getContactsIdsForUser(String userId) {
    try {
      return relationShipService.getMyContactsIds(Integer.parseInt(userId));
    } catch (SQLException ex) {
      SilverTrace.error("MyContactProfilSessionController",
          "MyContactProfilSessionController.getContactsForUser", "", ex);
    }
    return new ArrayList<String>();
  }

  public boolean isAContact(String userId) {
    if (StringUtil.isDefined(userId)) {
      try {
        return relationShipService.isInRelationShip(Integer.parseInt(getUserId()), Integer.parseInt(
            userId));
      } catch (SQLException e) {
        SilverTrace.error("MyContactProfilSessionController",
            "MyContactProfilSessionController.getContactsForUser", "", e);
      }
    }
    return false;
  }

  /**
   * get this user with full information
   *
   * @param userId
   * @return UserFull
   */
  public UserFull getUserFul(String userId) {
    return this.getOrganizationController().getUserFull(userId);
  }

  /**
   * update the properties of user
   *
   * @param idUser
   * @param properties
   * @throws SocialNetworkException
   */
  public void modifyUser(String idUser, Map<String, String> properties) throws
      SocialNetworkException {
    UserFull theModifiedUser = null;
    String idRet = null;

    SilverTrace.info("personalizationPeas",
        "PersonalizationPeasSessionController.modifyUser()",
        "root.MSG_GEN_ENTER_METHOD", "UserId=" + idUser);

    theModifiedUser = m_AdminCtrl.getUserFull(idUser);
    if (theModifiedUser == null) {
      throw new SocialNetworkException(
          "MyProfilSessionController.modifyUser()",
          SilverpeasException.ERROR, "admin.EX_ERR_UNKNOWN_USER");
    }
    // process extra properties
    Set<String> keys = properties.keySet();
    Iterator<String> iKeys = keys.iterator();
    String key = null;
    String value = null;
    while (iKeys.hasNext()) {
      key = iKeys.next();
      value = properties.get(key);

      theModifiedUser.setValue(key, value);
    }

    idRet = m_AdminCtrl.updateUserFull(theModifiedUser);
    if (idRet == null || idRet.length() <= 0) {
      throw new SocialNetworkException(
          "MyProfilSessionController.modifyUser()",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_USER", "UserId="
              + idUser);
    }

  }

  public boolean isUserDomainRW() {
    return (getDomainActions() & AbstractDomainDriver.ACTION_CREATE_USER) != 0;
  }

  public long getDomainActions() {
    if (domainActions == -1) {
      domainActions = m_AdminCtrl.getDomainActions(getUserDetail().getDomainId());
    }
    return domainActions;
  }

  public int getMinLengthPwd() {
    return JobDomainSettings.m_MinLengthPwd;
  }

  public boolean isBlanksAllowedInPwd() {
    return JobDomainSettings.m_BlanksAllowedInPwd;
  }

  public void modifyUser(String idUser, String userLastName, String userFirstName, String userEMail,
      String userAccessLevel, String oldPassword, String newPassword, String userLoginQuestion,
      String userLoginAnswer, Map<String, String> properties) throws AuthenticationException {
    SilverTrace.info("personalizationPeas", "PersonalizationPeasSessionController.modifyUser()",
        "root.MSG_GEN_ENTER_METHOD", "UserId=" + idUser + " userLastName=" + userLastName
            + " userFirstName=" + userFirstName + " userEMail=" + userEMail + " userAccessLevel="
            + userAccessLevel);

    UserFull theModifiedUser = m_AdminCtrl.getUserFull(idUser);

    if (isUserDomainRW()) {
      theModifiedUser.setLastName(userLastName);
      theModifiedUser.setFirstName(userFirstName);
      theModifiedUser.seteMail(userEMail);
      theModifiedUser.setLoginQuestion(userLoginQuestion);
      theModifiedUser.setLoginAnswer(userLoginAnswer);
      // Si l'utilisateur n'a pas entré de nouveau mdp, on ne le change pas
      if (newPassword != null && newPassword.length() != 0) {
        // In this case, this method checks if oldPassword and actual password match !
        changePassword(theModifiedUser.getLogin(), oldPassword, newPassword, theModifiedUser.
            getDomainId());

        theModifiedUser.setPassword(newPassword);
      }

      // process extra properties
      Set<String> keys = properties.keySet();
      Iterator<String> iKeys = keys.iterator();
      String key = null;
      String value = null;
      while (iKeys.hasNext()) {
        key = iKeys.next();
        value = properties.get(key);
        theModifiedUser.setValue(key, value);
      }
      m_AdminCtrl.updateUserFull(theModifiedUser);

    } else {
      if (StringUtil.isDefined(newPassword)) {
        changePassword(theModifiedUser.getLogin(), oldPassword, newPassword,
            theModifiedUser.getDomainId());
      }
    }
  }

  private void changePassword(String login, String oldPassword, String newPassword, String domainId)
      throws AuthenticationException {
    LoginPasswordAuthentication auth = new LoginPasswordAuthentication();
    auth.changePassword(login, oldPassword, newPassword, domainId);
  }

  public UserPreferences getPreferences() {
    return getPersonalization();
  }

  public void savePreferences(UserPreferences preferences) {
    SilverpeasServiceProvider.getPersonalizationService().saveUserSettings(preferences);
  }

  public List<String> getAllLanguages() {
    return DisplayI18NHelper.getLanguages();
  }

  public List<SpaceInstLight> getSpaceTreeview() {
    return getOrganizationController().getSpaceTreeview(getUserId());
  }

  /**
   * return my invitation list sent
   *
   * @return List<InvitationUser>
   */
  public List<InvitationUser> getAllMyInvitationsSent() {
    List<InvitationUser> invitationUsers = new ArrayList<InvitationUser>();
    List<Invitation> invitations =
        invitationService.getAllMyInvitationsSent(Integer.parseInt(getUserId()));
    for (Invitation varI :
        invitations) {
      invitationUsers.add(new InvitationUser(varI,
          getUserDetail(Integer.toString(varI.getReceiverId()))));
    }
    return invitationUsers;
  }

  /**
   * return my invitation list Received
   *
   * @return List<InvitationUser>
   */
  public List<InvitationUser> getAllMyInvitationsReceived() {
    List<InvitationUser> invitationUsers = new ArrayList<InvitationUser>();
    List<Invitation> invitations =
        invitationService.getAllMyInvitationsReceive(Integer.parseInt(getUserId()));
    for (Invitation varI :
        invitations) {
      invitationUsers.add(new InvitationUser(varI, getUserDetail(
          Integer.toString(varI.getSenderId()))));
    }
    return invitationUsers;
  }

  public void sendInvitation(String receiverId, String message) {
    Invitation invitation =
        new Invitation(Integer.parseInt(getUserId()), Integer.parseInt(receiverId), message,
            new Date());
    invitationService.invite(invitation);
  }

  public void ignoreInvitation(String id) {
    invitationService.ignoreInvitation(Integer.parseInt(id));
  }

  public void acceptInvitation(String id) {
    invitationService.accepteInvitation(Integer.parseInt(id));
  }
}