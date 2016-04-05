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

package org.silverpeas.core.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.i18n.I18NHelper;

import org.apache.commons.lang3.CharEncoding;

/**
 * @author ehugonnet
 */
public class MailUtil {

  public static final String SMTP_SERVER = "SMTPServer";
  public static final String SMTP_AUTH = "SMTPAuthentication";
  public static final String SMTP_PORT = "SMTPPort";
  public static final String SMTP_LOGIN = "SMTPUser";
  public static final String SMTP_PASSWORD = "SMTPPwd";
  public static final String SMTP_DEBUG = "SMTPDebug";
  public static final String SMTP_SECURE = "SMTPSecure";
  private static final String mailhost;
  private static final boolean authenticated;
  private static final boolean secure;
  private static final boolean debug;
  private static final int port;
  private static final String login;
  private static final String password;
  private static final String notificationAddress;
  private static final String notificationPersonalName;
  private static final boolean forceReplyToSenderField;
  private static Iterable<String> domains;
  public static final SettingBundle configuration = ResourceLocator.getSettingBundle(
      "org.silverpeas.notificationserver.channel.smtp.smtpSettings");

  static {
    mailhost = configuration.getString(SMTP_SERVER);
    authenticated = configuration.getBoolean(SMTP_AUTH, false);
    port = configuration.getInteger(SMTP_PORT, 25);
    login = configuration.getString(SMTP_LOGIN);
    password = configuration.getString(SMTP_PASSWORD);
    debug = configuration.getBoolean(SMTP_DEBUG, false);
    secure = configuration.getBoolean(SMTP_SECURE, false);
    notificationAddress = configuration.getString("NotificationAddress");
    notificationPersonalName = configuration.getString("NotificationPersonalName");
    forceReplyToSenderField = configuration.getBoolean("ForceReplyToSenderField", false);
    reloadConfiguration(configuration.getString("AuthorizedDomains", ""));
  }

  public static boolean isForceReplyToSenderField() {
    return forceReplyToSenderField;
  }

  /**
   * Should be used only in tests.
   *
   * @param domainsList the list of coma separated authorized domains for email sender addresses.
   */
  static void reloadConfiguration(String domainsList) {
    if (StringUtil.isDefined(domainsList)) {
      String[] authorizedDomains = StringUtil.split(domainsList, ',');
      domains = new ArrayList<String>(authorizedDomains.length);
      for (String domain : authorizedDomains) {
        if (StringUtil.isDefined(domain)) {
          ((List<String>) domains).add(domain.trim());
        }

      }
    } else {
      domains = Collections.singletonList("");
    }
  }

  public synchronized static boolean isDomainAuthorized(String email) {
    if (StringUtil.isDefined(email)) {
      String emailAddress = email.toLowerCase(I18NHelper.defaultLocale);
      for (String domain : domains) {
        if (emailAddress.endsWith(domain.toLowerCase(I18NHelper.defaultLocale))) {
          return true;
        }
      }
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
    String personal = senderAddress.equals(pFrom) ? personalName : notificationPersonalName;
    if (StringUtil.isDefined(personal)) {
      address.setPersonal(personal, CharEncoding.UTF_8);
    }
    return address;
  }

  public synchronized static String getAuthorizedEmail(String email) {
    if (isDomainAuthorized(email)) {
      return email;
    }
    return notificationAddress;
  }

  public static String getMailServer() {
    return mailhost;
  }

  public static boolean isAuthenticated() {
    return authenticated;
  }

  public static boolean isDebug() {
    return debug;
  }

  public static String getLogin() {
    return login;
  }

  public static String getPassword() {
    return password;
  }

  public static int getPort() {
    return port;
  }

  public static boolean isSecure() {
    return secure;
  }

  private MailUtil() {
  }
}
