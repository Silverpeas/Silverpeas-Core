/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.admin.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import com.silverpeas.ui.DisplayI18NHelper;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.AdminReference;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

@Named("silverpeasUserService")
public class UserServiceLegacy implements UserService {

  ResourceLocator multilang = null;

  @PostConstruct
  void init() {
      multilang =
        new ResourceLocator(
        "com.silverpeas.social.multilang.registration", DisplayI18NHelper
        .getDefaultLanguage());
  }

  @Override
  public String registerUser(String firstName, String lastName,
      String email, String domainId) throws AdminException {
    return registerUser(firstName, lastName, email, domainId, UserDetail.USER_ACCESS);
  }

  @Override
  public String registerUser(String firstName, String lastName,
      String email, String domainId, String accessLevel) throws AdminException {

    Admin admin = AdminReference.getAdminService();

    // Generate user login
    String login = generateLogin(admin, domainId, email);
    if (login == null) {
      throw new AdminException(
          "SilverpeasAdminServiceLegacy.createGuestUser",
          SilverpeasException.ERROR, "admin.EX_NO_LOGIN_AVAILABLE");
    }

    // Generate password
    String password = generatePassword();

    // Add user
    UserDetail user = new UserDetail();
    user.setId("-1");
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.seteMail(email);
    user.setLogin(login);
    user.setDomainId(domainId);
    user.setAccessLevel(accessLevel);
    String userId = admin.addUser(user);

    if (!StringUtil.isDefined(userId)) {
      throw new AdminException(
          "SilverpeasAdminServiceLegacy.createGuestUser",
          SilverpeasException.ERROR, "admin.EX_ADD_USER_FAILED");
    }

    // Update UserFull informations
    UserFull uf = admin.getUserFull(userId);
    if (uf != null) {
      uf.setPasswordValid(true);
      uf.setPassword(password);
      userId = admin.updateUserFull(uf);
      if (!StringUtil.isDefined(userId)) {
        throw new AdminException(
            "SilverpeasAdminServiceLegacy.createGuestUser",
            SilverpeasException.ERROR, "admin.EX_ADD_USER_FAILED");
      }
    }

    // Send credentials to user
    Domain domain = admin.getDomain(domainId);
    sendCredentialsToUser(uf, password, domain.getSilverpeasServerURL());

    return userId;
  }

  @Override
  public UserDetail findUser(String userId) throws AdminException {
    Admin admin = AdminReference.getAdminService();
    return admin.getUserDetail(userId);
  }

  private String generatePassword() {
    Random random = new Random();
    byte[] password = new byte[8];
    for (int i = 0; i < 8; i++) {
      password[i] = (byte) (65 + random.nextInt(26));
    }
    return new String(password);
  }

  private String generateLogin(Admin admin, String domainId,
      String email) {
    try {
      String userId = admin.getUserIdByLoginAndDomain(email,
          domainId);
      if (userId == null) {
        return email;
      }
    } catch (AdminException e) {
      // An exception is thrown because user is not found
      // so this login is available
      SilverTrace.debug("admin",
          "SilverpeasAdminServiceLegacy.generateLogin",
          "firstTryFailed", "firstName :" + email, e);
      return email;
    }

    return null;
  }

  private void sendCredentialsToUser(UserFull user, String password, String silverpeasServerURL) {
    try {
      Map<String, SilverpeasTemplate> templates = new HashMap<String, SilverpeasTemplate>();
      String subject = multilang.getString("credentialsMail.subject");
      NotificationMetaData notifMetaData =
          new NotificationMetaData(NotificationParameters.NORMAL, subject, templates,
          "credentialsMail");

      // Retrieve login page URL
      ResourceLocator generalLook =
          new ResourceLocator("com.stratelia.silverpeas.lookAndFeel.generalLook", "");
      String loginPage = generalLook.getString("loginPage", "defaultLogin.jsp");
      StringBuffer url = new StringBuffer();
      if (!loginPage.startsWith("http")) {
        url.append(silverpeasServerURL);
        if (!URLManager.getApplicationURL().startsWith("/")) {
          url.append("/");
        }
        url.append(URLManager.getApplicationURL());
        if (!loginPage.startsWith("/")) {
          url.append("/");
        }
      }
      url.append(loginPage).append("?DomainId=").append(user.getDomainId());

      Admin admin = AdminReference.getAdminService();
      Domain svpDomain = admin.getDomain(user.getDomainId());

      SilverpeasTemplate template = getNewTemplate();
      template.setAttribute("fullName", user.getDisplayedName());
      template.setAttribute("login", user.getLogin());
      template.setAttribute("password", password);
      if (admin.getAllDomains().length > 1) {
        // do not display domain info if it is unique
        template.setAttribute("domain", svpDomain);
      }
      template.setAttribute("url", url.toString());
      templates.put(DisplayI18NHelper.getDefaultLanguage(), template);
      notifMetaData.addLanguage(DisplayI18NHelper.getDefaultLanguage(), subject, "");
      notifMetaData.setSender("0");
      notifMetaData.addUserRecipients(new UserRecipient[] { new UserRecipient(user.getId()) });

      notifyUser(notifMetaData, null);
    } catch (Exception e) {
      SilverTrace.error("socialNetwork", "UserServiceLegacy.sendCredentialsToUser",
          "EX_SEND_NOTIFICATION_FAILED", "userId=" + user.getId(), e);
    }
  }

  private SilverpeasTemplate getNewTemplate() {
    return SilverpeasTemplateFactory.createSilverpeasTemplateOnCore("socialNetwork");
  }

  private void notifyUser(NotificationMetaData notifMetaData, String componentId)
      throws AdminException {
    try {
      NotificationSender notifSender = new NotificationSender(componentId);
      notifSender.notifyUser(notifMetaData);
    } catch (NotificationManagerException e) {
      throw new AdminException("SilverpeasAdminServiceLegacy.notifyUser",
          SilverpeasException.ERROR, "EX_SEND_NOTIFICATION_FAILED", e);
    }
  }

  @Override
  public void migrateUserToDomain(UserDetail userDetail, String targetDomainId)
      throws AdminException {
    Admin admin = AdminReference.getAdminService();
    admin.migrateUser(userDetail, targetDomainId);
  }

  @Override
  public void updateUser(UserDetail userDetail) throws AdminException {
    Admin admin = AdminReference.getAdminService();
    admin.updateUser(userDetail);
  }



}
