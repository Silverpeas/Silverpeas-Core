/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.socialnetwork.myprofil.control;

import org.silverpeas.core.admin.domain.DomainDriver;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserDetailsSearchCriteria;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.notification.message.MessageNotifier;
import org.silverpeas.core.notification.user.SimpleUserNotification;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.personalization.service.PersonalizationServiceProvider;
import org.silverpeas.core.security.authentication.AuthenticationCredential;
import org.silverpeas.core.security.authentication.AuthenticationService;
import org.silverpeas.core.security.authentication.AuthenticationServiceProvider;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.socialnetwork.invitation.Invitation;
import org.silverpeas.core.socialnetwork.invitation.InvitationService;
import org.silverpeas.core.socialnetwork.model.ExternalAccount;
import org.silverpeas.core.socialnetwork.model.SocialNetworkID;
import org.silverpeas.core.socialnetwork.relationship.RelationShipService;
import org.silverpeas.core.socialnetwork.service.SocialNetworkService;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.web.socialnetwork.invitation.model.InvitationUser;

import java.util.*;

import static org.silverpeas.core.admin.user.constant.UserState.*;

/**
 * @author Bensalem Nabil
 */
public class MyProfilSessionController extends AbstractComponentSessionController {

  private final AdminController adminCtrl = ServiceProvider.getService(AdminController.class);
  private final RelationShipService relationShipService = RelationShipService.get();
  private final InvitationService invitationService = InvitationService.get();
  private long domainActions = -1;

  public MyProfilSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, "org.silverpeas.social.multilang.socialNetworkBundle",
        "org.silverpeas.social.settings.socialNetworkIcons",
        "org.silverpeas.social.settings.socialNetworkSettings");
  }

  /**
   * get all RelationShips ids for current user.
   * @return :List<String>
   */
  public List<String> getContactsIdsForUser() {
    return relationShipService.getMyContactsIds(Integer.parseInt(getUserId()));
  }

  /**
   * get this user with full information
   * @param userId the user identifier
   * @return UserFull
   */
  public UserFull getUserFull(String userId) {
    return this.getOrganisationController().getUserFull(userId);
  }

  public boolean isUserDomainRW() {
    return (getDomainActions() & DomainDriver.ActionConstants.ACTION_UPDATE_USER) != 0;
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
      String oldPassword, String newPassword, String userLoginQuestion,
      String userLoginAnswer, HttpRequest request)
      throws AuthenticationException, AdminException {
    UserDetail user = UserDetail.getById(idUser);

    AuthenticationCredential credential =
        AuthenticationCredential.newWithAsLogin(user.getLogin()).withAsPassword(oldPassword)
            .withAsDomainId(user.getDomainId());
    if (isUserDomainRW()) {
      // the user didn't set a new password, so no need to change it
      if (newPassword != null && newPassword.length() != 0) {
        // In this case, this method checks if oldPassword and actual password match !
        changePassword(credential, newPassword);
      }

      // It is important to load user data the most later possible because of potential updates
      // of user data by password management services
      UserFull theModifiedUser = adminCtrl.getUserFull(idUser);
      theModifiedUser.setLastName(userLastName);
      theModifiedUser.setFirstName(userFirstName);
      theModifiedUser.setEmailAddress(userEMail);
      theModifiedUser.setLoginQuestion(userLoginQuestion);
      theModifiedUser.setLoginAnswer(userLoginAnswer);
      // the user didn't set a new password, so no need to change it
      if (newPassword != null && newPassword.length() != 0) {
        theModifiedUser.setPassword(newPassword);
      }

      // process extra properties
      Enumeration<String> parameters = request.getParameterNames();
      String parameterName;
      String property;
      String propertiesPrefix = "prop_";
      while (parameters.hasMoreElements()) {
        parameterName = parameters.nextElement();
        if (parameterName.startsWith(propertiesPrefix)) {
          // remove prefix
          property = parameterName.substring(propertiesPrefix.length());
          if (theModifiedUser.isPropertyUpdatableByUser(property) ||
              (isAdmin() && theModifiedUser.isPropertyUpdatableByAdmin(property))) {
            theModifiedUser.setValue(property, request.getParameter(parameterName));
          }
        }
      }

      // process data from extra template
      processDataOfExtraTemplate(request);

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

  public boolean updatablePropertyExists() {
    UserFull userFull = getUserFull(getUserId());
    return (userFull.isAtLeastOnePropertyUpdatableByUser() ||
        (isAdmin() && userFull.isAtLeastOnePropertyUpdatableByAdmin()) ||
        getExtraTemplate() != null);
  }

  /**
   * Get all social networks linked to current user account
   * @return a dictionary in which each social network is mapped to an account of this user in this social network.
   */
  public Map<SocialNetworkID, ExternalAccount> getAllMyNetworks() {
    Map<SocialNetworkID, ExternalAccount> networks = new EnumMap<>(SocialNetworkID.class);

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

  public boolean deleteMyAccount(HttpRequest request) {
    String message = request.getParameter("message");
    boolean forgetMe = request.getParameterAsBoolean("forgetMe");

    User currentUser = User.getCurrentRequester();

    Administration admin = Administration.get();
    try {
      admin.deleteUser(getUserId());
    } catch (AdminException e) {
      MessageNotifier.addError(getMultilang().getString("myProfile.actions.deleteAccount.error"));
      return false;
    }

    // notify all admins and domain admins
    if (StringUtil.isDefined(message)) {
      message = "\"".concat(message).concat("\"");
    }
    notifyAdminsToDeletion(message);

    if (forgetMe) {
      try {
        admin.blankDeletedUsers(currentUser.getDomainId(), Collections.singletonList(getUserId()));
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
    return true;
  }

  private void notifyAdminsToDeletion(final String message) {
    final OrganizationController organizationController = OrganizationController.get();
    User currentUser = User.getCurrentRequester();
    final Domain domain = organizationController.getDomain(currentUser.getDomainId());
    UserDetailsSearchCriteria criteria = new UserDetailsSearchCriteria()
        .onUserStatesToExclude(BLOCKED, DEACTIVATED, REMOVED)
        .onAccessLevels(UserAccessLevel.ADMINISTRATOR);
    final List<User> admins = organizationController.searchUsers(criteria);
    criteria = new UserDetailsSearchCriteria()
        .onDomainIds(currentUser.getDomainId())
        .onUserStatesToExclude(BLOCKED, DEACTIVATED, REMOVED)
        .onAccessLevels(UserAccessLevel.DOMAIN_ADMINISTRATOR);
    admins.addAll(organizationController.searchUsers(criteria));

    SimpleUserNotification.fromSystem()
        .toUsers(admins)
        .withTitle(l -> bundle(l).getStringWithParams("myProfile.deleteAccount.notif.subject", currentUser.getDisplayedName()))
        .andMessage(l -> bundle(l).getStringWithParams("myProfile.deleteAccount.notif.body", currentUser.getDisplayedName(), domain.getName(), message))
        .send();
  }

  private LocalizationBundle bundle(final String locale) {
    return ResourceLocator.getLocalizationBundle(
        "org.silverpeas.social.multilang.socialNetworkBundle", locale);
  }

  private void processDataOfExtraTemplate(HttpRequest request) {
    PublicationTemplate template = getExtraTemplate();
    if (template != null) {
      try {
        PublicationTemplateManager.getInstance()
            .saveData(template.getFileName(), getTemplateContext(), request.getFileItems());
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
        MessageNotifier.addError("Les données du formulaire n'ont pas été enregistrées !");
      }
    }
  }

  private PagesContext getTemplateContext() {
    return PagesContext.getDirectoryContext(getUserId(), getUserId(), getLanguage());
  }

  private PublicationTemplate getExtraTemplate() {
    PublicationTemplateManager templateManager = PublicationTemplateManager.getInstance();
    PagesContext context = getTemplateContext();
    return templateManager.getDirectoryTemplate(context);
  }
}
