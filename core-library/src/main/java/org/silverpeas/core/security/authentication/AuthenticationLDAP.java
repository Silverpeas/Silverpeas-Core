/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.authentication;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.ldap.LDAPModification;
import com.novell.ldap.LDAPSearchResults;
import org.silverpeas.core.admin.domain.driver.ldapdriver.LdapConfiguration;
import org.silverpeas.core.security.authentication.exception.AuthenticationBadCredentialException;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.security.authentication.exception.AuthenticationHostException;
import org.silverpeas.core.security.authentication.exception.AuthenticationPasswordAboutToExpireException;
import org.silverpeas.core.security.authentication.exception.AuthenticationPasswordExpired;
import org.silverpeas.core.security.authentication.exception.AuthenticationPasswordMustBeChangedAtNextLogon;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static java.time.temporal.ChronoField.*;
import static org.silverpeas.core.util.Charsets.UTF_8;

/**
 * This class performs the LDAP authentication
 *
 * @author tleroi, mmoquillon
 */
public class AuthenticationLDAP extends Authentication {

  private static final int INTERVALS_PER_MILLISECOND = 1000000 / 100;
  private static final long MILLISECONDS_BETWEEN_1601_AND_1970 = Long.parseLong("11644473600000");
  private static final String BASEDN_SEPARATOR = ";;";
  private static final int FORMAT_NANOSECOND = 0;
  private static final int FORMAT_TIMESTAMP = 1;

  /**
   * Formatter used to format the 'TimeStamp' representation of a LDAP attribute.
   * Its pattern is derived from the GeneralizedTime type as defined by the LDAP standard.
   * See https://ldapwiki.com/wiki/GeneralizedTime for more information.
   * Here, it is a simple attempt to implement the standard with some restrictions in order to not
   * complexify the code for date time format not used by the commonly used LDAP servers: we don't
   * support the optional leap-second and the comma separator in the optional fraction parts.
   */
  private static final DateTimeFormatter GENERALIZED_TIME =
      new DateTimeFormatterBuilder().appendValue(YEAR, 4)
          .appendValue(MONTH_OF_YEAR, 2)
          .appendValue(DAY_OF_MONTH, 2)
          .appendValue(HOUR_OF_DAY, 2)
          .appendOptional(new DateTimeFormatterBuilder().appendValue(MINUTE_OF_HOUR, 2)
              .appendOptional(DateTimeFormatter.ofPattern("ss"))
              .toFormatter())
          .appendOptional(new DateTimeFormatterBuilder()
              .appendFraction(MILLI_OF_SECOND, 0, 9, true)
              .toFormatter())
          .appendPattern("X")
          .toFormatter();

  private boolean m_MustAlertPasswordExpiration = false;
  private String m_PwdLastSetFieldName;
  private int m_PwdLastSetFieldFormat;
  private int m_PwdMaxAge;
  private int m_PwdExpirationReminderDelay;
  private String ldapImpl;
  private String m_UserBaseDN;
  private String m_UserLoginFieldName;
  private LdapConfiguration configuration = new LdapConfiguration();

  @Override
  public void loadProperties(SettingBundle settings) {
    String serverName = getServerName();
    configuration.setSecure(settings.getBoolean(serverName + ".LDAPSecured", false));
    configuration.setLdapHost(settings.getString(serverName + ".LDAPHost"));
    if (configuration.isSecure()) {
      configuration.setLdapPort(settings.getInteger(serverName + ".LDAPSecuredPort", 636));
    } else {
      configuration.setLdapPort(settings.getInteger(serverName + ".LDAPPort", 389));
    }
    configuration.setTimeout(settings.getInteger(serverName + ".Timeout", 0));
    ldapImpl = settings.getString(serverName + ".LDAPImpl", "unknown");
    configuration.setUsername(settings.getString(serverName + ".LDAPAccessLogin"));
    configuration.setPassword(settings.getString(serverName + ".LDAPAccessPasswd").getBytes(UTF_8));
    m_UserBaseDN = settings.getString(serverName + ".LDAPUserBaseDN", "");
    m_UserLoginFieldName = settings.getString(serverName + ".LDAPUserLoginFieldName");

    // get parameters about user alert if password is about to expire or is already expired
    m_MustAlertPasswordExpiration = settings.getBoolean(serverName + ".MustAlertPasswordExpiration",
        false);
    if (m_MustAlertPasswordExpiration) {
      m_PwdLastSetFieldName = settings.getString(serverName + ".LDAPPwdLastSetFieldName", null);
      m_PwdMaxAge = settings.getInteger(serverName + ".LDAPPwdMaxAge", Integer.MAX_VALUE);

      String propValue =
          settings.getString(serverName + ".LDAPPwdLastSetFieldFormat", "nanoseconds");
      m_PwdLastSetFieldFormat = propValue.equals("nanoseconds") ? 0 : 1;

      m_PwdExpirationReminderDelay =
          settings.getInteger(serverName + ".PwdExpirationReminderDelay", 5);
      if (m_PwdLastSetFieldName == null) {
        m_MustAlertPasswordExpiration = false;
      }
    }
  }

  @Override
  protected AuthenticationConnection<LDAPConnection> openConnection() throws AuthenticationException {
    LDAPConnection ldapConnection;
    if (configuration.isSecure()) {
      ldapConnection = new LDAPConnection(new LDAPJSSESecureSocketFactory());
    } else {
      ldapConnection = new LDAPConnection();
    }
    if (configuration.getTimeout() > 0) {
      ldapConnection.setSocketTimeOut(configuration.getTimeout());
    }
    try {
      ldapConnection.connect(configuration.getLdapHost(), configuration.getLdapPort());
    } catch (LDAPException ex) {
      throw new AuthenticationHostException(
          "Connection to the LDAP server failed with the following configuration: "
          + configuration, ex);
    }
    return new AuthenticationConnection<>(ldapConnection);
  }

  @Override
  protected void closeConnection(AuthenticationConnection connection) throws AuthenticationException {
    // disconnect from the server
    try {
      LDAPConnection ldapConnection = getLDAPConnection(connection);
      if (ldapConnection != null && ldapConnection.isConnected()) {
        ldapConnection.disconnect();
      }
    } catch (Exception ex) {
      throw new AuthenticationHostException(
          "Cannot close the connection with the LDAP server with the following configuration: " +
              configuration, ex);
    }
  }

  @Override
  protected void doAuthentication(AuthenticationConnection connection,
      AuthenticationCredential credential)
      throws AuthenticationException {
    String searchString = m_UserLoginFieldName + "=" + credential.getLogin();
    String[] attrNames;
    long nbDaysBeforeExpiration = 0;

    /*
     * Step 1 : Find LDAP User object with given login
     */
    // retrieve or not password last set date
    if (m_MustAlertPasswordExpiration) {
      attrNames = new String[]{"uid", m_PwdLastSetFieldName};
    } else {
      attrNames = new String[]{"uid"};
    }

    // bind to LDAP with administrator account
    LDAPConnection ldapConnection = getLDAPConnection(connection);
    String login = credential.getLogin();
    String password = credential.getPassword();
    try {
      ldapConnection.bind(LDAPConnection.LDAP_V3, configuration.getUsername(), configuration
          .getPassword());
    } catch (LDAPException e) {
      throw new AuthenticationHostException(e);
    }

    // bind to LDAP with administrator account
    LDAPEntry fe = null;
    String[] baseDNs = extractBaseDNs(m_UserBaseDN);
    for (String baseDN : baseDNs) {
      try {

        LDAPSearchResults res = ldapConnection.search(baseDN, LDAPConnection.SCOPE_SUB,
            searchString, attrNames, false);
        if (res.hasMore()) {
          fe = res.next();
          break;
        }
      } catch (LDAPException ex) {
        throw new AuthenticationHostException(ex);
      }
    }

    // No user found
    if (fe == null) {
      throw new AuthenticationBadCredentialException("User not found with login: " + login
          + ";LoginField=" + m_UserLoginFieldName);
    }

    // Calculate nb days before password expiration
    if (m_MustAlertPasswordExpiration) {
      nbDaysBeforeExpiration = calculateDaysBeforeExpiration(fe);
      if (nbDaysBeforeExpiration < 0) {
        throw new AuthenticationPasswordExpired("User=" + login);
      }
    }

    // Checks if password is correct
    String userFullDN = fe.getDN();
    if (!StringUtil.isDefined(password)) {
      throw new AuthenticationBadCredentialException(
          "Password not set for user with login: " + login);
    }
    try {

      ldapConnection.bind(LDAPConnection.LDAP_V3, userFullDN, password.getBytes(UTF_8));

    } catch (LDAPException ex) {
      throw new AuthenticationBadCredentialException("Bad credential for user with login: "
          + login, ex);
    }

    if (m_MustAlertPasswordExpiration && (nbDaysBeforeExpiration < m_PwdExpirationReminderDelay)) {
      throw new AuthenticationPasswordAboutToExpireException(
          "The password of the user with login " + login + " is about to expire");
    }
  }

  /**
   * Given an user ldap entry, compute the numbers of days before password expiration
   *
   * @param fe the user ldap entry
   * @throws AuthenticationPasswordMustBeChangedAtNextLogon
   * @return duration in days
   */
  private long calculateDaysBeforeExpiration(LDAPEntry fe)
      throws AuthenticationPasswordMustBeChangedAtNextLogon {
    LDAPAttribute pwdLastSetAttr = fe.getAttribute(m_PwdLastSetFieldName);

    // if password last set attribute is not found, return max value: user won't be notified
    if (pwdLastSetAttr == null) {
      return Integer.MAX_VALUE;
    }

    // convert ldap value
    ZonedDateTime pwdLastSet = null;
    switch (m_PwdLastSetFieldFormat) {
      case FORMAT_NANOSECOND:
        long lastSetValue = Long.parseLong(pwdLastSetAttr.getStringValue());

        // Special value : 0, password must be changed
        if (lastSetValue == 0) {
          throw new AuthenticationPasswordMustBeChangedAtNextLogon("user=" + fe.getDN());
        }
        lastSetValue =
            (lastSetValue / INTERVALS_PER_MILLISECOND) - MILLISECONDS_BETWEEN_1601_AND_1970;
        pwdLastSet =
            ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastSetValue), ZoneId.systemDefault());
        break;

      case FORMAT_TIMESTAMP:
        try {
          String ldapValue = pwdLastSetAttr.getStringValue();
          if (ldapValue == null) {
            SilverLogger.getLogger(this)
                .warn("Attribute {0} has no value! Use max integer value", m_PwdLastSetFieldFormat);
            return Integer.MAX_VALUE;
          }
          pwdLastSet = OffsetDateTime.parse(ldapValue, GENERALIZED_TIME)
              .atZoneSameInstant(ZoneId.systemDefault());
        } catch (DateTimeParseException e) {
          SilverLogger.getLogger(this).error(e);
        }
        break;

      default:
        throw new IllegalArgumentException("Unknown time format: " + m_PwdLastSetFieldFormat +
            ". Expected either 'nanoseconds' or 'TimeStamp' format");
    }
    long delayInDays = Duration.between(pwdLastSet, ZonedDateTime.now()).getSeconds() / 86400;
    return delayInDays + m_PwdMaxAge;
  }

  @Override
  protected void doChangePassword(AuthenticationConnection connection,
      AuthenticationCredential credential, String newPassword) throws AuthenticationException {
    String login = credential.getLogin();
    String oldPassword = credential.getPassword();
    String userFullDN;
    String searchString = m_UserLoginFieldName + "=" + login;
    String[] strAttributes = {"sAMAccountName", "memberOf"};
    LDAPConnection ldapConnection = getLDAPConnection(connection);
    try {
      // Bind as the admin for the search
      ldapConnection.bind(LDAPConnection.LDAP_V3, configuration.getUsername(), configuration
          .getPassword());

      // Get user DN

      LDAPSearchResults res = ldapConnection.search(m_UserBaseDN, LDAPConnection.SCOPE_SUB,
          searchString, strAttributes, false);
      if (!res.hasMore()) {
        throw new AuthenticationBadCredentialException("User not found with login: " + login
            + ";LoginField=" + m_UserLoginFieldName);
      }
      LDAPEntry fe = res.next();
      userFullDN = fe.getDN();

      LDAPModification[] mod;
      if ("opends".equalsIgnoreCase(ldapImpl) || "openldap".equalsIgnoreCase(ldapImpl)) {
        // re bind with the requested user to verify old password
        ldapConnection.bind(LDAPConnection.LDAP_V3, userFullDN, oldPassword.getBytes(UTF_8));
        // prepare password change
        mod = changeOpenDSPassword(newPassword);
      } else { //Active Directory (something else ?)
        // Connection must be secure on an ActiveDirectory
        if (!configuration.isSecure()) {
          Exception e = new UnsupportedOperationException(
              "LDAP connection must be secured to allow password update");
          throw new AuthenticationException(e);
        }
        // prepare password change (old password will be verified by DELETE modification)
        mod = getActiveDirectoryPasswordChange(oldPassword, newPassword);
      }
      // Perform the update
      ldapConnection.modify(userFullDN, mod);
    } catch (Exception ex) {
      throw new AuthenticationHostException(ex);
    }
  }

  private LDAPModification[] getActiveDirectoryPasswordChange(String oldPassword, String newPassword) {
    // Convert passwords to UTF-16LE
    byte[] oldUnicodePassword = getActiveDirectoryUnicodePwd(oldPassword);
    byte[] newUnicodePassword = getActiveDirectoryUnicodePwd(newPassword);
    LDAPModification[] res = new LDAPModification[2];
    res[0] = new LDAPModification(LDAPModification.DELETE, new LDAPAttribute("unicodePwd",
        oldUnicodePassword));
    res[1] = new LDAPModification(LDAPModification.ADD, new LDAPAttribute("unicodePwd",
        newUnicodePassword));
    return res;
  }

  private LDAPModification[] getActiveDirectoryPasswordReset(String newPassword) {
    // Convert password to UTF-16LE
    byte[] newUnicodePassword = getActiveDirectoryUnicodePwd(newPassword);
    return new LDAPModification[]{new LDAPModification(LDAPModification.REPLACE, new LDAPAttribute(
      "unicodePwd",
      newUnicodePassword))};
  }

  private byte[] getActiveDirectoryUnicodePwd(String password) {
    String newQuotedPassword = "\"" + password + "\"";
    return newQuotedPassword.getBytes(Charsets.UTF_16LE);
  }

  private LDAPModification[] changeOpenDSPassword(String newPassword) {
    // prepare password change
    return new LDAPModification[]{new LDAPModification(LDAPModification.REPLACE, new LDAPAttribute(
      "userPassword",
      newPassword))};
  }

  private static String[] extractBaseDNs(String baseDN) {
    // if no separator, return a array with only the baseDN
    if (!baseDN.contains(BASEDN_SEPARATOR)) {
      String[] baseDNs = new String[1];
      baseDNs[0] = baseDN;
      return baseDNs;
    }

    StringTokenizer st = new StringTokenizer(baseDN, BASEDN_SEPARATOR);
    List<String> baseDNs = new ArrayList<String>();
    while (st.hasMoreTokens()) {
      baseDNs.add(st.nextToken());
    }
    return baseDNs.toArray(new String[baseDNs.size()]);
  }

  @Override
  protected void doResetPassword(AuthenticationConnection connection, String login,
      String newPassword)
      throws AuthenticationException {
    String userFullDN;
    String searchString = m_UserLoginFieldName + "=" + login;
    String[] strAttributes = {"sAMAccountName", "memberOf"};
    LDAPConnection ldapConnection = getLDAPConnection(connection);
    try {
      // Bind as the admin for the search
      ldapConnection.bind(LDAPConnection.LDAP_V3, configuration.getUsername(), configuration
          .getPassword());

      // Get user DN

      LDAPSearchResults res = ldapConnection.search(
          m_UserBaseDN, LDAPConnection.SCOPE_SUB, searchString, strAttributes, false);
      if (!res.hasMore()) {
        throw new AuthenticationBadCredentialException("User not found with login: " + login
            + ";LoginField=" + m_UserLoginFieldName);
      }
      LDAPEntry fe = res.next();
      userFullDN = fe.getDN();

      LDAPModification[] mod = null;
      if (!StringUtil.isDefined(ldapImpl) || "ad".equalsIgnoreCase(ldapImpl)) {
        // prepare password change
        mod = getActiveDirectoryPasswordReset(newPassword);
      } else if ("opends".equalsIgnoreCase(ldapImpl) || "openldap".equalsIgnoreCase(ldapImpl)) {
        // prepare password change
        mod = changeOpenDSPassword(newPassword);
      }

      // Perform the update
      ldapConnection.modify(userFullDN, mod);
    } catch (Exception ex) {
      throw new AuthenticationHostException(ex);
    }
  }

  private static LDAPConnection getLDAPConnection(AuthenticationConnection connection) {
    return (LDAPConnection) connection.getConnector();
  }
}
