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
package org.silverpeas.core.admin.user;

import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.notification.NotificationException;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationParameters;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplates;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.StringUtil;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.silverpeas.core.SilverpeasExceptionMessages.failureOnAdding;
import static org.silverpeas.core.SilverpeasExceptionMessages.undefined;

@Service
@Singleton
public class UserRegistrationServiceLegacy implements UserRegistrationService {

  @Inject
  private Administration admin;
  private LocalizationBundle multilang = null;
  private final Random random = new Random();

  @PostConstruct
  void init() {
      multilang =
        ResourceLocator.getLocalizationBundle(
        "org.silverpeas.authentication.multilang.authentication", DisplayI18NHelper
        .getDefaultLanguage());
  }

  @Override
  public String registerUser(String firstName, String lastName,
      String email, String domainId) throws AdminException {
    return registerUser(firstName, lastName, email, domainId, UserAccessLevel.USER);
  }

  @Override
  public String registerUser(String firstName, String lastName,
      String email, String domainId, UserAccessLevel accessLevel) throws AdminException {

    // Generate user login
    String login = generateLogin(admin, domainId, email);
    if (login == null) {
      throw new AdminException(undefined("user login"));
    }

    // Generate password
    String password = generatePassword();

    // Add user
    UserDetail user = new UserDetail();
    user.setId("-1");
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setEmailAddress(email);
    user.setLogin(login);
    user.setDomainId(domainId);
    user.setAccessLevel(accessLevel);
    String userId = admin.addUser(user);

    if (!StringUtil.isDefined(userId)) {
      throw new AdminException(failureOnAdding("user", firstName + " " + lastName));
    }

    // Update UserFull information
    UserFull uf = admin.getUserFull(userId);
    if (uf != null) {
      uf.setPasswordValid(true);
      uf.setPassword(password);
      userId = admin.updateUserFull(uf);
      if (!StringUtil.isDefined(userId)) {
        throw new AdminException(failureOnAdding("user", firstName + " " + lastName));
      }
      // Send credentials to user
      sendCredentialsToUser(uf, password, URLUtil.getCurrentServerURL());
    }

    return userId;
  }

  @Override
  public UserDetail findUser(String userId) throws AdminException {
    return admin.getUserDetail(userId);
  }

  private String generatePassword() {
    byte[] password = new byte[8];
    for (int i = 0; i < 8; i++) {
      password[i] = (byte) (65 + random.nextInt(26));
    }
    return new String(password);
  }

  private String generateLogin(Administration admin, String domainId, String email) {
    try {
      String userId = admin.getUserIdByLoginAndDomain(email,
          domainId);
      if (userId == null) {
        return email;
      }
    } catch (AdminException e) {
      // An exception is thrown because user is not found
      // so this login is available
      return email;
    }

    return null;
  }

  private void sendCredentialsToUser(UserFull user, String password, String silverpeasServerURL) {
    try {
      Map<String, SilverpeasTemplate> templates = new HashMap<>();
      String subject = multilang.getString("registration.credentials.mail.subject");
      NotificationMetaData notifMetaData =
          new NotificationMetaData(NotificationParameters.PRIORITY_NORMAL, subject, templates,
          "credentialsMail");

      // Retrieve login page URL
      SettingBundle generalLook =
          ResourceLocator.getSettingBundle("org.silverpeas.lookAndFeel.generalLook");
      String loginPage = generalLook.getString("loginPage", "defaultLogin.jsp");
      StringBuilder url = new StringBuilder();
      if (!loginPage.startsWith("http")) {
        url.append(silverpeasServerURL);
        if (!URLUtil.getApplicationURL().startsWith("/")) {
          url.append("/");
        }
        url.append(URLUtil.getApplicationURL());
        if (!loginPage.startsWith("/")) {
          url.append("/");
        }
      }
      url.append(loginPage).append("?DomainId=").append(user.getDomainId());

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
      notifMetaData.setSender(UserDetail.SYSTEM_USER_ID);
      notifMetaData.addUserRecipients(new UserRecipient(user.getId()));

      notifyUser(notifMetaData);
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error("cannot send notification for userId={0}", new String[]{user.getId()}, e);
    }
  }

  private SilverpeasTemplate getNewTemplate() {
    return SilverpeasTemplates.createSilverpeasTemplateOnCore("socialNetwork");
  }

  private void notifyUser(NotificationMetaData notifMetaData)
      throws AdminException {
    try {
      NotificationSender notifSender = new NotificationSender(null);
      notifSender.notifyUser(notifMetaData);
    } catch (NotificationException e) {
      throw new AdminException("Fail to notify users", e);
    }
  }

  @Override
  public void migrateUserToDomain(UserDetail userDetail, String targetDomainId)
      throws AdminException {
    admin.migrateUser(userDetail, targetDomainId);
  }

  @Override
  public void updateUser(UserDetail userDetail) throws AdminException {
    admin.updateUser(userDetail);
  }

}
