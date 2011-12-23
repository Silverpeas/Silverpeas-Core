package com.silverpeas.admin.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import com.silverpeas.ui.DisplayI18NHelper;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
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
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.AdminReference;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

@Named("silverpeasUserService")
public class UserServiceLegacy implements UserService {

	ResourceLocator settings = null;
  ResourceLocator multilang = null;
	String templatePath = null;
  String customerTemplatePath = null;

	@PostConstruct
	void init() {
		settings = new ResourceLocator(
				"com.silverpeas.socialnetwork.settings.socialNetworkSettings", "");

    multilang = new ResourceLocator(
        "com.silverpeas.socialnetwork.multilang.registration", DisplayI18NHelper.getDefaultLanguage());

		templatePath = settings.getString("templatePath");
    customerTemplatePath = settings.getString("customerTemplatePath");
	}

	@Override
	public String registerUser(String firstName, String lastName,
			String email) throws AdminException {

		Admin admin = AdminReference.getAdminService();

		String domainId = settings.getString("authentication.justRegisteredDomainId", "0");
		String login = generateLogin(admin, domainId, email);
		if (login == null) {
			throw new AdminException(
					"SilverpeasAdminServiceLegacy.createGuestUser",
					SilverpeasException.ERROR, "admin.EX_NO_LOGIN_AVAILABLE");
		}
		String password = generatePassword();

		UserDetail user = new UserDetail();
		user.setId("-1");
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.seteMail(email);
		user.setLogin(login);
		user.setDomainId(domainId);
		user.setAccessLevel("U");

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

		return userId;
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

	private void sendCredentialsToUser(UserFull user, String password) {
    try {
      Map<String, SilverpeasTemplate> templates = new HashMap<String, SilverpeasTemplate>();
      String subject = multilang.getString("credentialsMail.subject");
      NotificationMetaData notifMetaData = new NotificationMetaData( NotificationParameters.NORMAL, subject, templates, "credentialsMail" );

      SilverpeasTemplate template = getNewTemplate();
      template.setAttribute("fullName", user.getDisplayedName());
      template.setAttribute("login", user.getLogin());
      template.setAttribute("password", password);
      templates.put(DisplayI18NHelper.getDefaultLanguage(), template);
      notifMetaData.addLanguage(DisplayI18NHelper.getDefaultLanguage(), subject, "");
      notifMetaData.setSender("0");
      notifMetaData.addUserRecipients(new UserRecipient[] { new UserRecipient(user.getId()) });

      notifMetaData.setLink(URLManager.getApplicationURL());
      notifyUser(notifMetaData, null);
    } catch (Exception e) {
      SilverTrace.error("socialNetwork", "UserServiceLegacy.sendCredentialsToUser", "EX_SEND_NOTIFICATION_FAILED", "userId=" + user.getId(), e);
    }
  }

  private SilverpeasTemplate getNewTemplate() {
    Properties templateConfiguration = new Properties();
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR, templatePath);
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR, customerTemplatePath);
    return SilverpeasTemplateFactory.createSilverpeasTemplate(templateConfiguration);
  }

  private void notifyUser(NotificationMetaData notifMetaData, String componentId)
  throws AdminException {
    try {
      NotificationSender notifSender = new NotificationSender(componentId);
      notifSender.notifyUser(notifMetaData);
    } catch (NotificationManagerException e) {
      throw new AdminException("SilverpeasAdminServiceLegacy.notifyUser", SilverpeasException.ERROR, "EX_SEND_NOTIFICATION_FAILED", e);
    }
  }


}
