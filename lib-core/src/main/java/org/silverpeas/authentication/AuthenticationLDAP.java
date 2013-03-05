/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package org.silverpeas.authentication;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.ldap.LDAPModification;
import com.novell.ldap.LDAPSearchResults;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import org.silverpeas.authentication.exception.AuthenticationBadCredentialException;
import org.silverpeas.authentication.exception.AuthenticationException;
import org.silverpeas.authentication.exception.AuthenticationHostException;
import org.silverpeas.authentication.exception.AuthenticationPasswordAboutToExpireException;
import org.silverpeas.authentication.exception.AuthenticationPasswordExpired;
import org.silverpeas.authentication.exception.AuthenticationPasswordMustBeChangedAtNextLogon;

import static org.silverpeas.authentication.Authentication.module;
import static org.silverpeas.util.Charsets.UTF_8;
import static org.silverpeas.util.Charsets.UTF_16LE;

/**
 * This class performs the LDAP authentification
 */
public class AuthenticationLDAP extends Authentication {

  private final static int INTERVALS_PER_MILLISECOND = 1000000 / 100;
  private final static long MILLISECONDS_BETWEEN_1601_AND_1970 = Long.parseLong("11644473600000");
  private final static String BASEDN_SEPARATOR = ";;";
  private final static int FORMAT_NANOSECOND = 0;
  private final static int FORMAT_TIMESTAMP = 1;
  protected boolean secureConnection = false;
  protected boolean mustAlertPasswordExpiration = false;
  protected String passwordLastSetFieldName;
  protected int passwordLastSetFieldFormat;
  protected int mxAgeForPassword;
  protected int passwordExpirationReminderDelay;
  protected String ldapHost;
  protected int ldapPort;
  protected String ldapImpl;
  protected String accessLogin;
  protected String accessPasswd;
  protected String userBaseDN;
  protected String userLoginFieldName;
  protected LDAPConnection ldapConnection = null;

  @Override
  public void loadProperties(ResourceLocator settings) {
    String serverName = getServerName();
    secureConnection = settings.getBoolean(serverName + ".LDAPSecured", false);
    ldapHost = settings.getString(serverName + ".LDAPHost");
    if (secureConnection) {
      ldapPort = Integer.parseInt(settings.getString(serverName + ".LDAPSecuredPort"));
    } else {
      ldapPort = Integer.parseInt(settings.getString(serverName + ".LDAPPort"));
    }
    ldapImpl = settings.getString(serverName + ".LDAPImpl");
    accessLogin = settings.getString(serverName + ".LDAPAccessLogin");
    accessPasswd = settings.getString(serverName + ".LDAPAccessPasswd");
    userBaseDN = settings.getString(serverName + ".LDAPUserBaseDN");
    userLoginFieldName = settings.getString(serverName + ".LDAPUserLoginFieldName");

    // get parameters about user alert if password is about to expire or is already expired
    mustAlertPasswordExpiration = settings.getBoolean(serverName
        + ".MustAlertPasswordExpiration", false);
    if (mustAlertPasswordExpiration) {
      passwordLastSetFieldName = settings.getString(serverName + ".LDAPPwdLastSetFieldName");
      String propValue = settings.getString(serverName + ".LDAPPwdMaxAge");
      mxAgeForPassword = (propValue == null) ? Integer.MAX_VALUE : Integer.parseInt(propValue);
      propValue = settings.getString(serverName + ".LDAPPwdLastSetFieldFormat");
      passwordLastSetFieldFormat = ((propValue == null) || (propValue.equals("nanoseconds"))) ? 0
          : 1;
      propValue = settings.getString(serverName + ".PwdExpirationReminderDelay");
      passwordExpirationReminderDelay = (propValue == null) ? 5 : Integer.parseInt(propValue);
      if (passwordLastSetFieldName == null) {
        mustAlertPasswordExpiration = false;
      }
    }
    SilverTrace.info(module, "AuthenticationLDAP.doAuthentication()",
        "root.MSG_GEN_PARAM_VALUE", "javax.net.ssl.trustStore = "
        + System.getProperty("javax.net.ssl.trustStore"));
  }

  @Override
  protected AuthenticationConnection<LDAPConnection> openConnection() throws
      AuthenticationException {
    LDAPConnection ldapConnection;
    if (secureConnection) {
      ldapConnection = new LDAPConnection(new LDAPJSSESecureSocketFactory());
    } else {
      ldapConnection = new LDAPConnection();
    }
    try {
      ldapConnection.connect(ldapHost, ldapPort);
    } catch (LDAPException ex) {
      throw new AuthenticationHostException("AuthenticationLDAP.openConnection()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", "Host=" + ldapHost
          + ";Port=" + String.valueOf(ldapPort), ex);
    }
    return new AuthenticationConnection<LDAPConnection>(ldapConnection);
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
      throw new AuthenticationHostException("AuthenticationLDAP.closeConnection()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_CLOSE_FAILED", "Host=" + ldapHost
          + ";Port=" + ldapPort, ex);
    }
  }

  @Override
  protected void doAuthentication(AuthenticationConnection connection,
      AuthenticationCredential credential)
      throws AuthenticationException {
    String searchString = userLoginFieldName + "=" + credential.getLogin();
    String[] attrNames;
    int nbDaysBeforeExpiration = 0;

    /*
     * Step 1 : Find LDAP User object with given login
     */

    // retrieve or not password last set date
    if (mustAlertPasswordExpiration) {
      attrNames = new String[]{"uid", passwordLastSetFieldName};
    } else {
      attrNames = new String[]{"uid"};
    }

    // bind to LDAP with administrator account
    LDAPConnection ldapConnection = getLDAPConnection(connection);
    String login = credential.getLogin();
    String password = credential.getPassword();
    try {
      ldapConnection.bind(LDAPConnection.LDAP_V3, accessLogin, accessPasswd.getBytes(UTF_8));
    } catch (LDAPException e) {
      throw new AuthenticationHostException("AuthenticationLDAP.doAuthentication()",
          SilverpeasException.ERROR, "authentication.EX_LDAP_ACCESS_ERROR", e);
    }

    // bind to LDAP with administrator account
    LDAPEntry fe = null;
    String[] baseDNs = extractBaseDNs(userBaseDN);
    for (String baseDN : baseDNs) {
      try {
        SilverTrace.info(module, "AuthenticationLDAP.doAuthentication()",
            "root.MSG_GEN_PARAM_VALUE", "UserFilter=" + searchString + ", baseDN = " + baseDN);
        LDAPSearchResults res = ldapConnection.search(baseDN, LDAPConnection.SCOPE_SUB,
            searchString, attrNames, false);
        if (res.hasMore()) {
          fe = res.next();
          break;
        }
      } catch (LDAPException ex) {
        throw new AuthenticationHostException("AuthenticationLDAP.doAuthentication()",
            SilverpeasException.ERROR, "authentication.EX_LDAP_ACCESS_ERROR", ex);
      }
    }

    // No user found
    if (fe == null) {
      throw new AuthenticationBadCredentialException("AuthenticationLDAP.doAuthentication()",
          SilverpeasException.ERROR, "authentication.EX_USER_NOT_FOUND", "User=" + login
          + ";LoginField=" + userLoginFieldName);
    }

    // Calculate nb days before password expiration
    SilverTrace.debug(module, "AuthenticationLDAP.doAuthentication()",
        "root.MSG_GEN_PARAM_VALUE", "m_MustAlertPasswordExpiration=" + mustAlertPasswordExpiration);
    if (mustAlertPasswordExpiration) {
      nbDaysBeforeExpiration = calculateDaysBeforeExpiration(fe);
      if (nbDaysBeforeExpiration < 0) {
        throw new AuthenticationPasswordExpired("User=" + login);
      }
    }

    // Checks if password is correct
    String userFullDN = fe.getDN();
    if (!StringUtil.isDefined(password)) {
      throw new AuthenticationBadCredentialException("AuthenticationLDAP.doAuthentication()",
          SilverpeasException.ERROR, "authentication.EX_PWD_EMPTY", "User=" + login);
    }
    try {
      SilverTrace.info(module, "AuthenticationLDAP.doAuthentication()",
          "authentication.MSG_TRY_TO_AUTHENTICATE_USER", "UserDN=" + userFullDN);
      ldapConnection.bind(LDAPConnection.LDAP_V3, userFullDN, password.getBytes(UTF_8));
      SilverTrace.info(module, "AuthenticationLDAP.doAuthentication()",
          "authentication.MSG_USER_AUTHENTIFIED", "User=" + login);
    } catch (LDAPException ex) {
      throw new AuthenticationBadCredentialException("AuthenticationLDAP.doAuthentication()",
          SilverpeasException.ERROR, "authentication.EX_AUTHENTICATION_BAD_CREDENTIAL", "User="
          + login, ex);
    }

    if (mustAlertPasswordExpiration && (nbDaysBeforeExpiration < passwordExpirationReminderDelay)) {
      throw new AuthenticationPasswordAboutToExpireException("AuthenticationLDAP.doAuthentication()",
          SilverpeasException.WARNING, "authentication.EX_AUTHENTICATION_PASSWORD_ABOUT_TO_EXPIRE",
          "User=" + login);
    }
  }

  /**
   * Given an user ldap entry, compute the numbers of days before password expiration
   *
   * @param fe the user ldap entry
   * @throws org.silverpeas.authentication.exception.AuthenticationPasswordMustBeChangedAtNextLogon
   * @return duration in days
   */
  private int calculateDaysBeforeExpiration(LDAPEntry fe) throws
      AuthenticationPasswordMustBeChangedAtNextLogon {
    SilverTrace.debug(module, "AuthenticationLDAP.calculateDaysBeforeExpiration()",
        "root.MSG_GEN_ENTER_METHOD");
    LDAPAttribute pwdLastSetAttr = fe.getAttribute(passwordLastSetFieldName);

    // if password last set attribute is not found, return max value : user
    // won't be notified
    SilverTrace.debug(module, "AuthenticationLDAP.calculateDaysBeforeExpiration()",
        "root.MSG_GEN_PARAM_VALUE", "pwdLastSetAttr is null ? " + (pwdLastSetAttr == null));
    if (pwdLastSetAttr == null) {
      return Integer.MAX_VALUE;
    }

    // convert ldap value
    Date pwdLastSet;
    switch (passwordLastSetFieldFormat) {
      case FORMAT_NANOSECOND:
        long lastSetValue = Long.parseLong(pwdLastSetAttr.getStringValue());

        // Special value : 0, password must be changed
        if (lastSetValue == 0) {
          throw new AuthenticationPasswordMustBeChangedAtNextLogon("user=" + fe.getDN());
        }
        SilverTrace.debug(module, "AuthenticationLDAP.calculateDaysBeforeExpiration()",
            "root.MSG_GEN_PARAM_VALUE", "lastSetValue = " + lastSetValue);
        lastSetValue = lastSetValue / INTERVALS_PER_MILLISECOND;
        SilverTrace.debug(module, "AuthenticationLDAP.calculateDaysBeforeExpiration()",
            "root.MSG_GEN_PARAM_VALUE", "lastSetValue = " + lastSetValue);
        lastSetValue -= MILLISECONDS_BETWEEN_1601_AND_1970;
        SilverTrace.debug(module, "AuthenticationLDAP.calculateDaysBeforeExpiration()",
            "root.MSG_GEN_PARAM_VALUE", "lastSetValue = " + lastSetValue);
        pwdLastSet = new Date(lastSetValue);
        break;

      case FORMAT_TIMESTAMP:
      default:
        try {
          String ldapValue = pwdLastSetAttr.getStringValue();
          if (ldapValue == null) {
            SilverTrace.error(module, "AuthenticationLDAP.calculateDaysBeforeExpiration()",
                "authentication.NO_VALUE", "m_PwdLastSetField=" + passwordLastSetFieldName);
            return Integer.MAX_VALUE;
          } else if (ldapValue.length() >= 14) {
            ldapValue = ldapValue.substring(0, 14);
          } else {
            SilverTrace.error(module, "AuthenticationLDAP.calculateDaysBeforeExpiration()",
                "authentication.EX_BAD_DATE_FORMAT", "ldapValue=" + ldapValue);
            return Integer.MAX_VALUE;
          }
          pwdLastSet = DateUtil.parse(ldapValue, "yyyyMMddHHmmss");
        } catch (ParseException e) {
          SilverTrace.error(module, "AuthenticationLDAP.calculateDaysBeforeExpiration()",
              "authentication.EX_BAD_DATE_FORMAT", e);
          return Integer.MAX_VALUE;
        }
    }
    SilverTrace.debug(module, "AuthenticationLDAP.calculateDaysBeforeExpiration()",
        "root.MSG_GEN_PARAM_VALUE", "pwdLastSet = " + DateUtil
        .getOutputDateAndHour(pwdLastSet, "fr"));

    long delayInMilliseconds = pwdLastSet.getTime() - System.currentTimeMillis();
    SilverTrace.debug(module, "AuthenticationLDAP.calculateDaysBeforeExpiration()",
        "root.MSG_GEN_PARAM_VALUE", "delayInMilliseconds = " + delayInMilliseconds);
    Long delayInDays = (delayInMilliseconds / (1000L * 3600L * 24L)) + mxAgeForPassword;
    SilverTrace.debug(module, "AuthenticationLDAP.calculateDaysBeforeExpiration()",
        "root.MSG_GEN_EXIT_METHOD", "delayInDays = " + delayInDays);
    return delayInDays.intValue();
  }

  /**
   * Overrides Authentication.internalChangePassword to offer password update capabilities.
   *
   * @param login user login
   * @param oldPassword user old password
   * @param newPassword user new password
   * @throws AuthenticationException
   */
  @Override
  protected void doChangePassword(AuthenticationConnection connection,
      AuthenticationCredential credential, String newPassword)
      throws AuthenticationException {
    String login = credential.getLogin();
    String oldPassword = credential.getPassword();
    String userFullDN;
    String searchString = userLoginFieldName + "=" + login;
    String[] strAttributes = {"sAMAccountName", "memberOf"};
    LDAPConnection ldapConnection = getLDAPConnection(connection);
    try {
      // Bind as the admin for the search
      ldapConnection.bind(LDAPConnection.LDAP_V3, accessLogin, accessPasswd.getBytes(UTF_8));
      // Get user DN
      SilverTrace.info(module, "AuthenticationLDAP.changePassword()",
          "root.MSG_GEN_PARAM_VALUE", "UserFilter=" + searchString);
      LDAPSearchResults res = ldapConnection.search(userBaseDN, LDAPConnection.SCOPE_SUB,
          searchString, strAttributes, false);
      if (!res.hasMore()) {
        throw new AuthenticationBadCredentialException("AuthenticationLDAP.changePassword()",
            SilverpeasException.ERROR, "authentication.EX_USER_NOT_FOUND", "User=" + login
            + ";LoginField=" + userLoginFieldName);
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
        if (!secureConnection) {
          Exception e = new UnsupportedOperationException(
              "LDAP connection must be secured to allow password update");
          throw new AuthenticationException("AuthenticationLDAP.changePassword",
              SilverpeasException.ERROR, "authentication.EX_CANT_CHANGE_USERPASSWORD", e);
        }
        // prepare password change (old password will be verified by DELETE modification)
        mod = getActiveDirectoryPasswordChange(oldPassword, newPassword);
      }

      // Perform the update
      ldapConnection.modify(userFullDN, mod);
    } catch (Exception ex) {
      throw new AuthenticationHostException(
          "AuthenticationLDAP.doChangePassword()",
          SilverpeasException.ERROR, "authentication.EX_LDAP_ACCESS_ERROR", ex);
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
      "unicodePwd", newUnicodePassword))};
  }

  private byte[] getActiveDirectoryUnicodePwd(String password) {
    String newQuotedPassword = "\"" + password + "\"";
    return newQuotedPassword.getBytes(UTF_16LE);
  }

  private LDAPModification[] changeOpenDSPassword(String newPassword) throws
      UnsupportedEncodingException {
    // prepare password change
    return new LDAPModification[]{new LDAPModification(LDAPModification.REPLACE, new LDAPAttribute(
      "userPassword", newPassword))};
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
    String searchString = userLoginFieldName + "=" + login;
    String[] strAttributes = {"sAMAccountName", "memberOf"};
    LDAPConnection ldapConnection = getLDAPConnection(connection);
    try {
      // Bind as the admin for the search
      ldapConnection.bind(LDAPConnection.LDAP_V3, accessLogin, accessPasswd.getBytes(UTF_8));
      // Get user DN
      SilverTrace.info(module, "AuthenticationLDAP.changePassword()",
          "root.MSG_GEN_PARAM_VALUE", "UserFilter=" + searchString);
      LDAPSearchResults res = ldapConnection.search(userBaseDN, LDAPConnection.SCOPE_SUB,
          searchString, strAttributes, false);
      if (!res.hasMore()) {
        throw new AuthenticationBadCredentialException("AuthenticationLDAP.doResetPassword()",
            SilverpeasException.ERROR, "authentication.EX_USER_NOT_FOUND", "User=" + login
            + ";LoginField=" + userLoginFieldName);
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
      throw new AuthenticationHostException("AuthenticationLDAP.doResetPassword()",
          SilverpeasException.ERROR, "authentication.EX_LDAP_ACCESS_ERROR", ex);
    }
  }

  private static LDAPConnection getLDAPConnection(AuthenticationConnection connection) {
    return (LDAPConnection) connection.getConnector();
  }
}
