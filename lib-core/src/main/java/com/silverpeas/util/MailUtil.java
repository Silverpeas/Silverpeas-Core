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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.util;

import com.google.common.base.Splitter;
import com.stratelia.webactiv.util.ResourceLocator;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Collections;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

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
  static final Splitter DOMAIN_SPLITTER = Splitter.on(',').trimResults();
  private static final String mailhost;
  private static final boolean authenticated;
  private static final boolean secure;
  private static final boolean debug;
  private static final int port;
  private static final String login;
  private static final String password;
  private static final String notificationAddress;
  private static final String notificationPersonalName;
  private static Iterable<String> domains;
  public static final ResourceLocator configuration = new ResourceLocator(
      "com.stratelia.silverpeas.notificationserver.channel.smtp.smtpSettings", "");
  private static final MessageFormat emailFormatter = new MessageFormat("\"{0}\"<{1}>");

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
    reloadConfiguration(configuration.getString("AuthorizedDomains", ""));
  }

  /**
   * Should be used only in tests.
   * @param domainsList the list of coma separated authorized domains for email sender addresses.
   */
  static void reloadConfiguration(String domainsList) {
    if (StringUtil.isDefined(domainsList)) {
      domains = DOMAIN_SPLITTER.split(domainsList);
    } else {
      domains = Collections.singletonList("");
    }
  }

  public synchronized static boolean isDomainAuthorized(String email) {
    if (StringUtil.isDefined(email)) {
      String emailAddress = email.toLowerCase();
      for (String domain : domains) {
        if (emailAddress.endsWith(domain.toLowerCase())) {
          return true;
        }
      }
    }
    return false;
  }

  public static synchronized InternetAddress getAuthorizedEmailAddress(String pFrom) throws
      AddressException, UnsupportedEncodingException {
    String senderAddress = getAuthorizedEmail(pFrom);
    return new InternetAddress(emailFormatter.format(new String[] {
        pFrom.substring(0, pFrom.indexOf(
        '@')), senderAddress }), true);
  }
  
  public static synchronized InternetAddress getAuthorizedEmailAddress(String pFrom,
      String personalName) throws AddressException, UnsupportedEncodingException {
    String senderAddress = getAuthorizedEmail(pFrom);
    if (senderAddress.equals(pFrom)) {
      // email is authorized, use it as it
      InternetAddress address = new InternetAddress(pFrom, true);
      address.setPersonal(personalName, "UTF-8");
      return address;
    }
    // email is not authorized, use default one and default personal name too
    InternetAddress address = new InternetAddress(senderAddress, true);
    address.setPersonal(notificationPersonalName, "UTF-8");
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
