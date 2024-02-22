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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingFormatArgumentException;
import java.util.StringTokenizer;

import static java.time.temporal.ChronoField.*;
import static org.silverpeas.core.util.Charsets.UTF_8;

/**
 * This class performs the LDAP authentication
 * @author tleroi, mmoquillon
 */
public class AuthenticationLDAP extends Authentication {

  private static final int INTERVALS_PER_MILLISECOND = 1000000 / 100;
  private static final long MILLISECONDS_BETWEEN_1601_AND_1970 = Long.parseLong("11644473600000");
  private static final String BASE_DN_SEPARATOR = ";;";
  private static final int FORMAT_NANOSECOND = 0;
  private static final int FORMAT_TIMESTAMP = 1;

  /**
   * Formatter used to format the 'TimeStamp' representation of a LDAP attribute. Its pattern is
   * derived from the GeneralizedTime type as defined by the LDAP standard. See <a
   * href="https://ldapwiki.com/wiki/GeneralizedTime>Generalized time</a> for more information.
   * Here, it is a simple attempt to implement the standard with some restrictions in order to not
   * complexity the code for date time format not used by the commonly used LDAP servers: we don't
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
  private static final String USER_NOT_FOUND_WITH_LOGIN = "User not found with login: ";
  private static final String UNICODE_PASS_ATTR = "unicodePwd";

  private boolean mustAlertPasswordExpiration = false;
  private String pwdLastSetFieldName;
  private int pwdDateTimeFieldFormat;
  private int pwdMaxAge;
  private int pwdExpirationReminderDelay = -1;
  private String pwdExpirationTimeFieldName;
  private String ldapImpl;
  private String userBaseDN;
  private String userLoginFieldName;
  private final LdapConfiguration configuration = new LdapConfiguration();

  @Override
  public void loadProperties(SettingBundle settings) {
    final String serverName = getServerName();
    configuration.setEncryptedCredentials(settings.getBoolean(serverName + ".encryptedCredentials",
        false));
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
    configuration.setPassword(settings.getString(serverName + ".LDAPAccessPasswd"));
    userBaseDN = settings.getString(serverName + ".LDAPUserBaseDN", "");
    userLoginFieldName = settings.getString(serverName + ".LDAPUserLoginFieldName");

    // get parameters about user alert if password is about to expire or is already expired
    setPasswordExpirationConfiguration(serverName, settings);
  }

  private void setPasswordExpirationConfiguration(final String serverName,
      final SettingBundle settings) {
    mustAlertPasswordExpiration = settings.getBoolean(serverName + ".MustAlertPasswordExpiration",
        false);
    pwdExpirationReminderDelay = -1;
    if (mustAlertPasswordExpiration) {
      pwdExpirationTimeFieldName =
          settings.getString(serverName + ".LDAPPwdExpirationTimeFieldName", null);
      if (pwdExpirationTimeFieldName == null) {
        pwdLastSetFieldName = settings.getString(serverName + ".LDAPPwdLastSetFieldName", null);
        if (pwdLastSetFieldName == null) {
          mustAlertPasswordExpiration = false;
        } else {
          pwdMaxAge = settings.getInteger(serverName + ".LDAPPwdMaxAge", Integer.MAX_VALUE);
          pwdExpirationReminderDelay =
              settings.getInteger(serverName + ".PwdExpirationReminderDelay", 5);
        }
      }
      if (mustAlertPasswordExpiration) {
        final String propValue = settings.getString(serverName + ".LDAPTimeFieldFormat", "");
        if (propValue.equals("nanoseconds")) {
          pwdDateTimeFieldFormat = FORMAT_NANOSECOND;
        } else if (propValue.equals("TimeStamp")) {
          pwdDateTimeFieldFormat = FORMAT_TIMESTAMP;
        } else {
          throw new MissingFormatArgumentException("The property " + serverName +
              ".LDAPTimeFieldFormat isn't valued correctly. Expected either 'nanoseconds' or " +
              "'TimeStamp'");
        }
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  protected AuthenticationConnection<LDAPConnection> openConnection()
      throws AuthenticationException {
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
  @SuppressWarnings({"unchecked", "rawtypes"})
  protected void closeConnection(AuthenticationConnection connection)
      throws AuthenticationException {
    // disconnect from the server
    try {
      //noinspection unchecked
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
  @SuppressWarnings({"unchecked", "rawtypes"})
  protected void doAuthentication(final AuthenticationConnection connection,
      final AuthenticationCredential credential)
      throws AuthenticationException {
    // bind to LDAP with administrator account
    //noinspection unchecked
    LDAPConnection ldapConnection = openLDAPConnection(connection);

    // find the LDAP entry matching the specified credential
    LDAPEntry userEntry = findLDAPUserMatchingCredential(ldapConnection, credential);

    // checks password expiration date time
    ZonedDateTime pwdExpirationDateTime = checkPasswordExpiration(userEntry);

    // Checks if password is correct
    checkPasswordIsValid(ldapConnection, userEntry, credential);

    if (pwdExpirationDateTime != null && pwdExpirationReminderDelay > -1) {
      Duration durationBeforePwdExpiration =
          Duration.between(ZonedDateTime.now(), pwdExpirationDateTime);
      long daysCountBeforeExpiration = durationBeforePwdExpiration.getSeconds() / 86400;
      if (daysCountBeforeExpiration < pwdExpirationReminderDelay) {
        throw new AuthenticationPasswordAboutToExpireException(
            "The password of the user with login " + credential.getLogin() + " is about to expire");
      }
    }
  }

  private void checkPasswordIsValid(final LDAPConnection ldapConnection, final LDAPEntry userEntry,
      final AuthenticationCredential credential) throws AuthenticationBadCredentialException {
    String userFullDN = userEntry.getDN();
    if (!StringUtil.isDefined(credential.getPassword())) {
      throw new AuthenticationBadCredentialException(
          "Password not set for user with login: " + credential.getLogin());
    }
    try {
      ldapConnection.bind(LDAPConnection.LDAP_V3, userFullDN,
          credential.getPassword().getBytes(UTF_8));
    } catch (LDAPException ex) {
      throw new AuthenticationBadCredentialException("Bad credential for user with login: "
          + credential.getLogin(), ex);
    }
  }

  @Nullable
  private ZonedDateTime checkPasswordExpiration(final LDAPEntry userEntry)
      throws AuthenticationPasswordMustBeChangedAtNextLogon, AuthenticationPasswordExpired {
    ZonedDateTime pwdExpirationDateTime;
    if (mustAlertPasswordExpiration) {
      pwdExpirationDateTime = getPasswordExpirationDateTime(userEntry);
      if (pwdExpirationDateTime == null) {
        // special case of MS AD
        throw new AuthenticationPasswordMustBeChangedAtNextLogon(
            "Password required to be change at next logon for user " + userEntry.getDN());
      } else if (pwdExpirationDateTime.isBefore(ZonedDateTime.now())) {
        throw new AuthenticationPasswordExpired("Password expired for user " + userEntry.getDN());
      }
    } else {
      pwdExpirationDateTime = null;
    }
    return pwdExpirationDateTime;
  }

  @Nonnull
  private LDAPEntry findLDAPUserMatchingCredential(final LDAPConnection ldapConnection,
      final AuthenticationCredential credential)
      throws AuthenticationHostException, AuthenticationBadCredentialException {
    String searchString = userLoginFieldName + "=" + credential.getLogin();
    String[] attrNames = getLDAPAttributeNamesToSeek();
    String[] baseDNs = extractBaseDNs(userBaseDN);
    LDAPEntry userEntry = null;
    for (String baseDN : baseDNs) {
      try {
        LDAPSearchResults res = ldapConnection.search(baseDN, LDAPConnection.SCOPE_SUB,
            searchString, attrNames, false);
        if (res.hasMore()) {
          userEntry = res.next();
          break;
        }
      } catch (LDAPException ex) {
        throw new AuthenticationHostException(ex);
      }
    }

    // No user found
    if (userEntry == null) {
      throw new AuthenticationBadCredentialException(
          USER_NOT_FOUND_WITH_LOGIN + credential.getLogin() + " and LDAP login field " +
              userLoginFieldName);
    }
    return userEntry;
  }

  @Nonnull
  private String[] getLDAPAttributeNamesToSeek() {
    final String[] attrNames;
    if (mustAlertPasswordExpiration) {
      if (pwdExpirationTimeFieldName != null) {
        attrNames = new String[]{"uid", pwdExpirationTimeFieldName};
      } else {
        attrNames = new String[]{"uid", pwdLastSetFieldName};
      }
    } else {
      attrNames = new String[]{"uid"};
    }
    return attrNames;
  }

  @Nonnull
  private LDAPConnection openLDAPConnection(
      final AuthenticationConnection<LDAPConnection> connection)
      throws AuthenticationHostException {
    final LDAPConnection ldapConnection = getLDAPConnection(connection);
    try {
      ldapConnection.bind(LDAPConnection.LDAP_V3, configuration.getUsername(), configuration
          .getPassword());
    } catch (LDAPException e) {
      throw new AuthenticationHostException(e);
    }
    return ldapConnection;
  }

  private ZonedDateTime getPasswordExpirationDateTime(final LDAPEntry entry) {
    ZonedDateTime expirationDateTime;
    if (pwdExpirationTimeFieldName != null) {
      final LDAPAttribute pwdExpirationTime = entry.getAttribute(pwdExpirationTimeFieldName);
      expirationDateTime =
          getPasswordExpirationDateTime(pwdExpirationTimeFieldName, pwdExpirationTime);
    } else {
      final LDAPAttribute pwdChangeTime = entry.getAttribute(pwdLastSetFieldName);
      ZonedDateTime lastChangeDateTime =
          getPasswordExpirationDateTime(pwdLastSetFieldName, pwdChangeTime);
      expirationDateTime =
          lastChangeDateTime != null ? lastChangeDateTime.plusDays(pwdMaxAge) : null;
    }
    return expirationDateTime;
  }

  private ZonedDateTime getPasswordExpirationDateTime(final String attrName,
      final LDAPAttribute dateTimeAttr) {
    final ZonedDateTime expirationDateTime;
    if (dateTimeAttr == null) {
      SilverLogger.getLogger(this)
          .warn("Not such LDAP attribute {0}! No password expiration", attrName);
      expirationDateTime = ZonedDateTime.now().plusDays(1);
    } else {
      final String dateTimeValue = dateTimeAttr.getStringValue();
      if (dateTimeValue == null) {
        SilverLogger.getLogger(this)
            .warn("LDAP Attribute {0} not valued! No password expiration", attrName);
        expirationDateTime = ZonedDateTime.now().plusDays(1);
      } else {
        expirationDateTime = parseDateTimeValue(dateTimeValue);
      }
    }
    return expirationDateTime;
  }

  private ZonedDateTime parseDateTimeValue(final String value) {
    ZonedDateTime dateTime;
    if (pwdDateTimeFieldFormat == FORMAT_NANOSECOND) {
      long nanoseconds = Long.parseLong(value);
      if (nanoseconds == 0) {
        // special case in MS AD
        dateTime = null;
      } else {
        nanoseconds =
            (nanoseconds / INTERVALS_PER_MILLISECOND) - MILLISECONDS_BETWEEN_1601_AND_1970;
        dateTime =
            ZonedDateTime.ofInstant(Instant.ofEpochMilli(nanoseconds), ZoneId.systemDefault());
      }
    } else {
      dateTime =
          OffsetDateTime.parse(value, GENERALIZED_TIME).atZoneSameInstant(ZoneId.systemDefault());
    }
    return dateTime;
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  protected void doChangePassword(AuthenticationConnection connection,
      AuthenticationCredential credential, String newPassword) throws AuthenticationException {
    String login = credential.getLogin();
    String oldPassword = credential.getPassword();
    String userFullDN;
    String searchString = userLoginFieldName + "=" + login;
    String[] strAttributes = {"sAMAccountName", "memberOf"};
    //noinspection unchecked
    LDAPConnection ldapConnection = getLDAPConnection(connection);
    try {
      // Bind as the admin for the search
      LDAPSearchResults res = search(ldapConnection, login, searchString, strAttributes);
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

  private LDAPSearchResults search(final LDAPConnection ldapConnection, final String login,
      final String searchString,
      final String[] strAttributes)
      throws LDAPException, AuthenticationBadCredentialException {
    ldapConnection.bind(LDAPConnection.LDAP_V3, configuration.getUsername(), configuration
        .getPassword());

    // Get user DN

    LDAPSearchResults res = ldapConnection.search(userBaseDN, LDAPConnection.SCOPE_SUB,
        searchString, strAttributes, false);
    if (!res.hasMore()) {
      throw new AuthenticationBadCredentialException(
          USER_NOT_FOUND_WITH_LOGIN + login + ";LoginField=" + userLoginFieldName);
    }
    return res;
  }

  private LDAPModification[] getActiveDirectoryPasswordChange(String oldPassword,
      String newPassword) {
    // Convert passwords to UTF-16LE
    byte[] oldUnicodePassword = getActiveDirectoryUnicodePwd(oldPassword);
    byte[] newUnicodePassword = getActiveDirectoryUnicodePwd(newPassword);
    LDAPModification[] res = new LDAPModification[2];
    res[0] = new LDAPModification(LDAPModification.DELETE, new LDAPAttribute(UNICODE_PASS_ATTR,
        oldUnicodePassword));
    res[1] = new LDAPModification(LDAPModification.ADD, new LDAPAttribute(UNICODE_PASS_ATTR,
        newUnicodePassword));
    return res;
  }

  private LDAPModification[] getActiveDirectoryPasswordReset(String newPassword) {
    // Convert password to UTF-16LE
    byte[] newUnicodePassword = getActiveDirectoryUnicodePwd(newPassword);
    return new LDAPModification[]{new LDAPModification(LDAPModification.REPLACE, new LDAPAttribute(
        UNICODE_PASS_ATTR,
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
    if (!baseDN.contains(BASE_DN_SEPARATOR)) {
      String[] baseDNs = new String[1];
      baseDNs[0] = baseDN;
      return baseDNs;
    }

    StringTokenizer st = new StringTokenizer(baseDN, BASE_DN_SEPARATOR);
    List<String> baseDNs = new ArrayList<>();
    while (st.hasMoreTokens()) {
      baseDNs.add(st.nextToken());
    }
    return baseDNs.toArray(new String[0]);
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  protected void doResetPassword(AuthenticationConnection connection, String login,
      final boolean loginIgnoreCase, String newPassword)
      throws AuthenticationException {
    String userFullDN;
    String searchString = userLoginFieldName + "=" + login;
    String[] strAttributes = {"sAMAccountName", "memberOf"};
    //noinspection unchecked
    LDAPConnection ldapConnection = getLDAPConnection(connection);
    try {
      // Bind as the admin for the search
      LDAPSearchResults res = search(ldapConnection, login, searchString, strAttributes);
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

  private static LDAPConnection getLDAPConnection(
      AuthenticationConnection<LDAPConnection> connection) {
    return connection.getConnector();
  }
}
