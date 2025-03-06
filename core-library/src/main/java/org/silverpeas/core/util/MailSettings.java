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
package org.silverpeas.core.util;

import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.util.StringUtil;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ehugonnet
 */
public class MailSettings {

  private static final String SMTP_SERVER = "SMTPServer";
  private static final String SMTP_AUTH = "SMTPAuthentication";
  private static final String SMTP_PORT = "SMTPPort";
  private static final String SMTP_LOGIN = "SMTPUser";
  private static final String SMTP_PASSWORD = "SMTPPwd";
  private static final String SMTP_DEBUG = "SMTPDebug";
  private static final String SMTP_SECURE = "SMTPSecure";

  private static Set<String> domains;
  public static final SettingBundle configuration = ResourceLocator.getSettingBundle(
      "org.silverpeas.notificationserver.channel.smtp.smtpSettings");

  static {
    reloadConfiguration(configuration.getString("AuthorizedDomains", ""));
  }

  public static boolean isForceReplyToSenderField() {
    return configuration.getBoolean("ForceReplyToSenderField", false);
  }

  /**
   * Should be used only in tests.
   *
   * @param domainsList the list of coma separated authorized domains for email sender addresses.
   */
  static void reloadConfiguration(String domainsList) {
    if (StringUtil.isDefined(domainsList)) {
      String[] authorizedDomains = StringUtil.split(domainsList, ',');
      domains = new HashSet<>(authorizedDomains.length);
      for (String domain : authorizedDomains) {
        if (StringUtil.isDefined(domain)) {
          domains.add(domain.trim());
        }

      }
    } else {
      domains = Set.of();
    }
  }

  public static synchronized boolean isDomainAuthorized(String email) {
    if (StringUtil.isDefined(email)) {
      String emailAddress = email.toLowerCase(I18NHelper.defaultLocale);
      return domains.isEmpty() || domains.stream()
          .map(d -> d.toLowerCase(I18NHelper.defaultLocale))
          .anyMatch(emailAddress::endsWith);
    }
    return false;
  }

  public static synchronized InternetAddress getAuthorizedEmailAddress(String pFrom,
      String personalName) throws AddressException, UnsupportedEncodingException {
    String senderAddress = getAuthorizedEmail(pFrom);
    InternetAddress address = new InternetAddress(senderAddress, true);
    // - If email is authorized (senderAddress.equals(pFrom)), use it as it (personalName)
    // - If email is not authorized (!senderAddress.equals(pFrom)), use default one and default
    //   personal name too (notificationPersonalName)
    String personal = senderAddress.equals(pFrom) ? personalName :
        configuration.getString("NotificationPersonalName");
    if (StringUtil.isDefined(personal)) {
      address.setPersonal(personal, Charsets.UTF_8.name());
    }
    return address;
  }

  public static synchronized String getAuthorizedEmail(String email) {
    if (isDomainAuthorized(email)) {
      return email;
    }
    return configuration.getString("NotificationAddress");
  }

  public static String getMailServer() {
    return configuration.getString(SMTP_SERVER);
  }

  public static boolean isAuthenticated() {
    return configuration.getBoolean(SMTP_AUTH, false);
  }

  public static boolean isDebug() {
    return configuration.getBoolean(SMTP_DEBUG, false);
  }

  public static String getLogin() {
    return configuration.getString(SMTP_LOGIN);
  }

  public static String getPassword() {
    return configuration.getString(SMTP_PASSWORD);
  }

  public static int getPort() {
    return configuration.getInteger(SMTP_PORT, 25);
  }

  public static boolean isSecure() {
    return configuration.getBoolean(SMTP_SECURE, false);
  }

  private MailSettings() {
  }
}
