/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.socialnetwork.myprofil.control;

import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.personalization.service.PersonalizationServiceProvider;
import org.silverpeas.core.socialnetwork.invitation.Invitation;
import org.silverpeas.core.socialnetwork.invitation.InvitationService;
import org.silverpeas.core.socialnetwork.model.ExternalAccount;
import org.silverpeas.core.socialnetwork.model.SocialNetworkID;
import org.silverpeas.core.socialnetwork.relationShip.RelationShip;
import org.silverpeas.core.socialnetwork.relationShip.RelationShipService;
import org.silverpeas.core.socialnetwork.service.SocialNetworkService;
import org.silverpeas.core.notification.user.client.NotificationManagerException;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationParameters;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.admin.domain.DomainDriver;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.security.authentication.AuthenticationCredential;
import org.silverpeas.core.security.authentication.AuthenticationService;
import org.silverpeas.core.security.authentication.AuthenticationServiceProvider;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.util.Link;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;
import org.silverpeas.web.socialnetwork.invitation.model.InvitationUser;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

/**
 * @author Bensalem Nabil
 */
public class MyProfilSessionController extends AbstractComponentSessionController {

  private AdminController adminCtrl = ServiceProvider.getService(AdminController.class);
  private RelationShipService relationShipService = RelationShipService.get();
  private InvitationService invitationService = InvitationService.get();
  private long domainActions = -1;

  public MyProfilSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, "org.silverpeas.social.multilang.socialNetworkBundle",
        "org.silverpeas.social.settings.socialNetworkIcons",
        "org.silverpeas.social.settings.socialNetworkSettings");
  }

  /**
   * get all RelationShips ids for this user.
   * @param userId the user identifier
   * @return :List<String>
   */
  public List<String> getContactsIdsForUser(String userId) {
    try {
      return relationShipService.getMyContactsIds(Integer.parseInt(userId));
    } catch (SQLException ex) {
      SilverTrace
          .error("MyProfilSessionController", "MyProfilSessionController.getContactsIdsForUser", "",
              ex);
    }
    return new ArrayList<>();
  }

  /**
   * get this user with full information
   * @param userId the user identifier
   * @return UserFull
   */
  public UserFull getUserFul(String userId) {
    return this.getOrganisationController().getUserFull(userId);
  }

  public boolean isUserDomainRW() {
    return (getDomainActions() & DomainDriver.ACTION_UPDATE_USER) != 0;
  }

  public boolean isAdmin() {
    return (getUserDetail().isAccessAdmin());
  }

  public long getDomainActions() {
    if (domainActions == -1) {
      domainActions = adminCtrl.getDomainActions(getUserDetail().getDomainId());
    }
    return domainActions;
  }

  public void modifyUser(String idUser, String userLastName, String userFirstName, String userEMail,
      String userAccessLevel, String oldPassword, String newPassword, String userLoginQuestion,
      String userLoginAnswer, Map<String, String> properties)
      throws AuthenticationException, AdminException {
    UserDetail user = UserDetail.getById(idUser);

    AuthenticationCredential credential =
        AuthenticationCredential.newWithAsLogin(user.getLogin()).withAsPassword(oldPassword)
            .withAsDomainId(user.getDomainId());
    if (isUserDomainRW()) {
      // Si l'utilisateur n'a pas entré de nouveau mdp, on ne le change pas
      if (newPassword != null && newPassword.length() != 0) {
        // In this case, this method checks if oldPassword and actual password match !
        changePassword(credential, newPassword);
      }

      // It is important to load user data the most later possible because of potential updates
      // of user data by password management services
      UserFull theModifiedUser = adminCtrl.getUserFull(idUser);
      theModifiedUser.setLastName(userLastName);
      theModifiedUser.setFirstName(userFirstName);
      theModifiedUser.seteMail(userEMail);
      theModifiedUser.setLoginQuestion(userLoginQuestion);
      theModifiedUser.setLoginAnswer(userLoginAnswer);
      // Si l'utilisateur n'a pas entré de nouveau mdp, on ne le change pas
      if (newPassword != null && newPassword.length() != 0) {
        theModifiedUser.setPassword(newPassword);
      }

      // process extra properties
      for (Map.Entry<String, String> property : properties.entrySet()) {
        if (theModifiedUser.isPropertyUpdatableByUser(property.getKey()) ||
            (isAdmin() && theModifiedUser.isPropertyUpdatableByAdmin(property.getKey()))) {
          theModifiedUser.setValue(property.getKey(), property.getValue());
        }
      }
      adminCtrl.updateUserFull(theModifiedUser);

    } else {
      if (StringUtil.isDefined(newPassword)) {
        changePassword(credential, newPassword);
      }
    }
  }

  private void changePassword(AuthenticationCredential credential, String newPassword)
      throws AuthenticationException {
    AuthenticationService authenticator = AuthenticationServiceProvider.getService();
    authenticator.changePassword(credential, newPassword);
  }

  public UserPreferences getPreferences() {
    return getPersonalization();
  }

  public void savePreferences(UserPreferences preferences) {
    PersonalizationServiceProvider.getPersonalizationService().saveUserSettings(preferences);
  }

  public List<SpaceInstLight> getSpaceTreeview() {
    return getOrganisationController().getSpaceTreeview(getUserId());
  }

  /**
   * return my invitation list sent
   * @return List<InvitationUser>
   */
  public List<InvitationUser> getAllMyInvitationsSent() {
    List<InvitationUser> invitationUsers = new ArrayList<>();
    List<Invitation> invitations =
        invitationService.getAllMyInvitationsSent(Integer.parseInt(getUserId()));
    for (Invitation varI : invitations) {
      invitationUsers
          .add(new InvitationUser(varI, getUserDetail(Integer.toString(varI.getReceiverId()))));
    }
    return invitationUsers;
  }

  /**
   * return my invitation list Received
   * @return List<InvitationUser>
   */
  public List<InvitationUser> getAllMyInvitationsReceived() {
    List<InvitationUser> invitationUsers = new ArrayList<>();
    List<Invitation> invitations =
        invitationService.getAllMyInvitationsReceive(Integer.parseInt(getUserId()));
    for (Invitation varI : invitations) {
      invitationUsers
          .add(new InvitationUser(varI, getUserDetail(Integer.toString(varI.getSenderId()))));
    }
    return invitationUsers;
  }

  public void sendInvitation(String receiverId, String message) {
    Invitation invitation =
        new Invitation(Integer.parseInt(getUserId()), Integer.parseInt(receiverId), message,
            new Date());
    if (invitationService.invite(invitation) >= 0) {
      notifyUser(receiverId, message);
    }
  }

  /**
   * @param receiverId the receiver user identifier
   * @param message the message
   */
  private void notifyUser(String receiverId, String message) {
    try {
      NotificationSender notificationSender = new NotificationSender(null);

      // Send a notification to alert people about a new relationship ask.
      Map<String, SilverpeasTemplate> templates = new HashMap<>();
      String subject = getString("myProfile.invitations.notification.send.subject");
      NotificationMetaData notifMetaData =
          new NotificationMetaData(NotificationParameters.NORMAL, subject, templates,
              "sendInvitation");

      UserDetail senderUser = getUserDetail();
      notifMetaData.setSource(senderUser.getDisplayedName());

      List<String> languages = DisplayI18NHelper.getLanguages();
      for (String language : languages) {
        // Create a new silverpeas template
        SilverpeasTemplate template = getNewTemplate();
        template.setAttribute("senderUser", senderUser);
        template.setAttribute("userName", senderUser.getDisplayedName());
        template.setAttribute("senderMessage", message);
        templates.put(language, template);
        notifMetaData.addLanguage(language, subject, "");
        LocalizationBundle localizedMessage = ResourceLocator.getLocalizationBundle(
            "org.silverpeas.social.multilang.socialNetworkBundle", language);
        String translation;
        try {
          translation =
              localizedMessage.getString("myProfile.invitations.notification.send.subject");
        } catch (MissingResourceException ex) {
          translation = subject;
        }
        notifMetaData.addLanguage(language, translation, "");

        String url = URLUtil.getURL(URLUtil.CMP_MYPROFILE, null, null)
            + "MyInvitations";
        Link link = new Link(url, localizedMessage.getString("myProfile.invitations.notification.notifLinkLabel"));
        notifMetaData.setLink(link, language);
        if (StringUtil.isDefined(message)) {
          setNotificationContent(notifMetaData, message, language);
        }
      }
      notifMetaData.setSender(getUserId());
      notifMetaData.addUserRecipient(new UserRecipient(receiverId));
      notificationSender.notifyUser(notifMetaData);
    } catch (NotificationManagerException e) {
      SilverTrace.error("MyProfilSessionController", "MyProfilSessionController.sendInvitation",
          "root.EX_CANT_SEND_MESSAGE", e);
    }
  }

  private void setNotificationContent(NotificationMetaData notif, String message, String language) {
    notif.addExtraMessage(message, language);
  }

  /**
   * @param id the invitation identifier
   * @see InvitationService#ignoreInvitation(int)
   */
  public void ignoreInvitation(String id) {
    invitationService.ignoreInvitation(Integer.parseInt(id));
  }

  /**
   * @param invitationId the invitation identifier
   */
  public void acceptInvitation(String invitationId) {
    int relationShipId = invitationService.accepteInvitation(Integer.parseInt(invitationId));
    if (relationShipId >= 0) {
      acceptInvitationNotif(relationShipId);
    }
  }

  /**
   * @param relationShipId
   */
  private void acceptInvitationNotif(int relationShipId) {
    RelationShip curRelation = invitationService.getRelationShip(relationShipId);
    try {
      // Retrieve sender information
      UserDetail senderUser = getUserDetail();
      String displayedName = senderUser.getDisplayedName();

      NotificationSender notificationSender = new NotificationSender(null);
      // Send a notification to alert people about new relationship.
      Map<String, SilverpeasTemplate> templates = new HashMap<>();
      String subject =
          displayedName + " " + getString("myProfile.invitations.notification.accept.subject");

      NotificationMetaData notifMetaData =
          new NotificationMetaData(NotificationParameters.NORMAL, subject, templates,
              "acceptInvitation");

      notifMetaData.setSource(displayedName);

      List<String> languages = DisplayI18NHelper.getLanguages();
      for (String language : languages) {
        // Create a new silverpeas template
        SilverpeasTemplate template = getNewTemplate();
        template.setAttribute("senderUser", senderUser);
        template.setAttribute("userName", senderUser.getDisplayedName());
        templates.put(language, template);
        notifMetaData.addLanguage(language, subject, "");
        LocalizationBundle localizedMessage = ResourceLocator.getLocalizationBundle(
            "org.silverpeas.social.multilang.socialNetworkBundle", language);
        String translation;
        try {
          translation =
              localizedMessage.getString("myProfile.invitations.notification.accept.subject");
        } catch (MissingResourceException ex) {
          translation = subject;
        }
        notifMetaData.addLanguage(language, translation, "");
      }
      notifMetaData.setSender(getUserId());
      notifMetaData.addUserRecipient(new UserRecipient(String.valueOf(curRelation.getInviterId())));
      notificationSender.notifyUser(notifMetaData);
    } catch (NotificationManagerException e) {
      SilverTrace.error("MyProfilSessionController", "MyProfilSessionController.sendInvitation",
          "root.EX_CANT_SEND_MESSAGE", e);
    }
  }

  /**
   * @return a SilverpeasTemplate
   */
  private SilverpeasTemplate getNewTemplate() {
    return SilverpeasTemplateFactory.createSilverpeasTemplateOnCore("socialNetwork");
  }

  public boolean updatablePropertyExists() {
    UserFull userFull = getUserFul(getUserId());
    return ((userFull.isAtLeastOnePropertyUpdatableByUser()) ||
        (isAdmin() && userFull.isAtLeastOnePropertyUpdatableByAdmin()));
  }

  /**
   * Get all social networks linked to current user account
   * @return
   */
  public Map<SocialNetworkID, ExternalAccount> getAllMyNetworks() {
    Map<SocialNetworkID, ExternalAccount> networks = new HashMap<>();

    List<ExternalAccount> externalAccounts = SocialNetworkService.getInstance().
        getUserExternalAccounts(getUserId());
    for (ExternalAccount account : externalAccounts) {
      networks.put(account.getNetworkId(), account);
    }

    return networks;
  }

  public void unlinkSocialNetworkFromSilverpeas(SocialNetworkID networkId) {
    SocialNetworkService.getInstance().removeExternalAccount(getUserId(), networkId);
  }
}
