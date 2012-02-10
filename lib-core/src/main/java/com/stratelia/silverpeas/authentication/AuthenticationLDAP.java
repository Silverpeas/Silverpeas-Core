/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * AuthenticationLDAP.java
 *
 * Created on 6 aout 2001
 */

package com.stratelia.silverpeas.authentication;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import com.google.common.base.Charsets;
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

/**
 * This class performs the LDAP authentification
 * @author tleroi
 * @version
 */
public class AuthenticationLDAP extends Authentication {
  private final static int INTERVALS_PER_MILLISECOND = 1000000 / 100;
  private final static long MILLISECONDS_BETWEEN_1601_AND_1970 = Long.parseLong("11644473600000");
  private final static String BASEDN_SEPARATOR = ";;";
  private final static int FORMAT_NANOSECOND = 0;
  private final static int FORMAT_TIMESTAMP = 1;

  protected boolean m_IsSecured = false;
  protected boolean m_MustAlertPasswordExpiration = false;
  protected String m_PwdLastSetFieldName;
  protected int m_PwdLastSetFieldFormat;
  protected int m_PwdMaxAge;
  protected int m_PwdExpirationReminderDelay;
  protected String m_Host;
  protected int m_Port;
  protected String ldapImpl;
  protected String m_AccessLogin;
  protected String m_AccessPasswd;
  protected String m_UserBaseDN;
  protected String m_UserLoginFieldName;
  protected LDAPConnection m_LDAPConnection = null;

  public void init(String authenticationServerName, ResourceLocator propFile) {
    // Lecture du fichier de proprietes
    m_IsSecured = getBooleanProperty(propFile, authenticationServerName + ".LDAPSecured", false);
    m_Host = propFile.getString(authenticationServerName + ".LDAPHost");
    if (m_IsSecured) {
      m_Port = Integer.parseInt(propFile.getString(authenticationServerName + ".LDAPSecuredPort"));
    } else {
      m_Port = Integer.parseInt(propFile.getString(authenticationServerName + ".LDAPPort"));
    }
    ldapImpl = propFile.getString(authenticationServerName + ".LDAPImpl");
    m_AccessLogin = propFile.getString(authenticationServerName + ".LDAPAccessLogin");
    m_AccessPasswd = propFile.getString(authenticationServerName + ".LDAPAccessPasswd");
    m_UserBaseDN = propFile.getString(authenticationServerName + ".LDAPUserBaseDN");
    m_UserLoginFieldName = propFile.getString(authenticationServerName + ".LDAPUserLoginFieldName");

    // get parameters about user alert if password is about to expire
    m_MustAlertPasswordExpiration = getBooleanProperty(propFile, authenticationServerName
        + ".MustAlertPasswordExpiration", false);
    if (m_MustAlertPasswordExpiration) {
      m_PwdLastSetFieldName =
          propFile.getString(authenticationServerName + ".LDAPPwdLastSetFieldName");
      String propValue = propFile.getString(authenticationServerName + ".LDAPPwdMaxAge");
      m_PwdMaxAge = (propValue == null) ? Integer.MAX_VALUE : Integer.parseInt(propValue);

      propValue = propFile.getString(authenticationServerName + ".LDAPPwdLastSetFieldFormat");
      m_PwdLastSetFieldFormat = ( (propValue == null) || (propValue.equals("nanoseconds")) ) ? 0 : 1;

      propValue = propFile.getString(authenticationServerName + ".PwdExpirationReminderDelay");
      m_PwdExpirationReminderDelay = (propValue == null) ? 5 : Integer.parseInt(propValue);
      if (m_PwdLastSetFieldName == null) {
        m_MustAlertPasswordExpiration = false;
      }
    }
    SilverTrace.info("authentication", "AuthenticationLDAP.internalAuthentication()",
        "root.MSG_GEN_PARAM_VALUE", "javax.net.ssl.trustStore = "
        + System.getProperty("javax.net.ssl.trustStore"));
  }

  @Override
  protected void openConnection() throws AuthenticationException {
    boolean doConnect = false;
    // Connect to server
    if (m_LDAPConnection == null) {
      if (m_IsSecured) {
        m_LDAPConnection = new LDAPConnection(new LDAPJSSESecureSocketFactory());
      } else {
        m_LDAPConnection = new LDAPConnection();
      }
      doConnect = true;
    } else if (m_LDAPConnection.isConnected() == false) {
      doConnect = true;
    }
    if (doConnect) {
      try {
        m_LDAPConnection.connect(m_Host, m_Port);
      } catch (LDAPException ex) {
        throw new AuthenticationHostException(
            "AuthenticationLDAP.openConnection()", SilverpeasException.ERROR,
            "root.EX_CONNECTION_OPEN_FAILED", "Host=" + m_Host + ";Port=" + String.valueOf(m_Port),
            ex);
      }
    }
  }

  @Override
  protected void closeConnection() throws AuthenticationException {
    // disconnect from the server
    try {
      if (m_LDAPConnection != null && m_LDAPConnection.isConnected()) {
        m_LDAPConnection.disconnect();
      }
      m_LDAPConnection = null;
    } catch (Exception ex) {
      throw new AuthenticationHostException( "AuthenticationLDAP.closeConnection()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_CLOSE_FAILED", "Host=" + m_Host + ";Port="
          + String.valueOf(m_Port), ex);
    }
  }

  @Override
  protected void internalAuthentication(String login, String passwd)
      throws AuthenticationException {
    String searchString = m_UserLoginFieldName + "=" + login;
    String[] attrNames;
    int nbDaysBeforeExpiration = 0;

    // retrieve or not password last set date
    if (m_MustAlertPasswordExpiration) {
      attrNames = new String[] { "uid", m_PwdLastSetFieldName };
    }
    else {
      attrNames = new String[] { "uid" };
    }
    try {
      m_LDAPConnection.bind(LDAPConnection.LDAP_V3, m_AccessLogin,
          m_AccessPasswd.getBytes(Charsets.UTF_8));
    } catch (LDAPException e) {
      throw new AuthenticationHostException("AuthenticationLDAP.internalAuthentication()",
          SilverpeasException.ERROR, "authentication.EX_LDAP_ACCESS_ERROR", e);
    }
    String userFullDN = null;
    String[] baseDNs = extractBaseDNs(m_UserBaseDN);
    for (String baseDN : baseDNs) {
      try {
        SilverTrace.info("authentication", "AuthenticationLDAP.internalAuthentication()",
            "root.MSG_GEN_PARAM_VALUE", "UserFilter=" + searchString + ", baseDN = " + baseDN);
        LDAPSearchResults res = m_LDAPConnection.search(baseDN, LDAPConnection.SCOPE_SUB,
            searchString, attrNames, false);
        if (res.hasMore()) {
          LDAPEntry fe = res.next();
          if (fe != null) {
            userFullDN = fe.getDN();
            SilverTrace.debug("authentication", "AuthenticationLDAP.internalAuthentication()",
                "root.MSG_GEN_PARAM_VALUE", "m_MustAlertPasswordExpiration="
                + m_MustAlertPasswordExpiration);
            if (m_MustAlertPasswordExpiration) {
              nbDaysBeforeExpiration = calculateDaysBeforeExpiration(fe);
            }
          }
        }
      } catch (LDAPException ex) {
        throw new AuthenticationHostException("AuthenticationLDAP.internalAuthentication()",
            SilverpeasException.ERROR, "authentication.EX_LDAP_ACCESS_ERROR", ex);
      }
    }
    if (userFullDN == null) {
      throw new AuthenticationBadCredentialException("AuthenticationLDAP.internalAuthentication()",
          SilverpeasException.ERROR, "authentication.EX_USER_NOT_FOUND", "User=" + login
          + ";LoginField=" + m_UserLoginFieldName);
    }

    if (!StringUtil.isDefined(passwd)) {
      throw new AuthenticationBadCredentialException("AuthenticationLDAP.internalAuthentication()",
          SilverpeasException.ERROR, "authentication.EX_PWD_EMPTY", "User=" + login);
    }
    try {
      SilverTrace.info("authentication", "AuthenticationLDAP.internalAuthentication()",
          "authentication.MSG_TRY_TO_AUTHENTICATE_USER", "UserDN=" + userFullDN);
      m_LDAPConnection.bind(LDAPConnection.LDAP_V3, userFullDN, passwd.getBytes(Charsets.UTF_8));
      SilverTrace.info("authentication", "AuthenticationLDAP.internalAuthentication()",
          "authentication.MSG_USER_AUTHENTIFIED", "User=" + login);
    } catch (LDAPException ex) {
      throw new AuthenticationBadCredentialException("AuthenticationLDAP.internalAuthentication()",
          SilverpeasException.ERROR, "authentication.EX_AUTHENTICATION_BAD_CREDENTIAL", "User="
          + login, ex);
    }

    if (m_MustAlertPasswordExpiration && (nbDaysBeforeExpiration < m_PwdExpirationReminderDelay)) {
      throw new AuthenticationPasswordAboutToExpireException("AuthenticationLDAP.internalAuthentication()",
          SilverpeasException.WARNING, "authentication.EX_AUTHENTICATION_PASSWORD_ABOUT_TO_EXPIRE",
          "User=" + login);
    }
  }

  /**
   * Given an user ldap entry, compute the numbers of days before password expiration
   * @param fe the user ldap entry
   * @return duration in days
   */
  private int calculateDaysBeforeExpiration(LDAPEntry fe) {
    SilverTrace.debug("authentication",
        "AuthenticationLDAP.calculateDaysBeforeExpiration()",
        "root.MSG_GEN_ENTER_METHOD");
    LDAPAttribute pwdLastSetAttr = fe.getAttribute(m_PwdLastSetFieldName);

    // if password last set attribute is not found, return max value : user
    // won't be notified
    SilverTrace.debug("authentication", "AuthenticationLDAP.calculateDaysBeforeExpiration()",
        "root.MSG_GEN_PARAM_VALUE", "pwdLastSetAttr is null ? "  + (pwdLastSetAttr == null));
    if (pwdLastSetAttr == null) {
      return Integer.MAX_VALUE;
    }

    // convert ldap value
    Date pwdLastSet = null;
    switch (m_PwdLastSetFieldFormat) {
      case FORMAT_NANOSECOND:
        long lastSetValue = Long.parseLong(pwdLastSetAttr.getStringValue());
        SilverTrace.debug("authentication", "AuthenticationLDAP.calculateDaysBeforeExpiration()",
            "root.MSG_GEN_PARAM_VALUE", "lastSetValue = " + lastSetValue);
        lastSetValue = lastSetValue / INTERVALS_PER_MILLISECOND;
        SilverTrace.debug("authentication", "AuthenticationLDAP.calculateDaysBeforeExpiration()",
            "root.MSG_GEN_PARAM_VALUE", "lastSetValue = " + lastSetValue);
        lastSetValue -= MILLISECONDS_BETWEEN_1601_AND_1970;
        SilverTrace.debug("authentication", "AuthenticationLDAP.calculateDaysBeforeExpiration()",
            "root.MSG_GEN_PARAM_VALUE", "lastSetValue = " + lastSetValue);
        pwdLastSet = new Date(lastSetValue);
        break;

      case FORMAT_TIMESTAMP:
        try {
          DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
          String ldapValue = pwdLastSetAttr.getStringValue();
          if (ldapValue == null) {
            SilverTrace.error("authentication", "AuthenticationLDAP.calculateDaysBeforeExpiration()",
                "authentication.NO_VALUE", "m_PwdLastSetField="+m_PwdLastSetFieldName);
            return Integer.MAX_VALUE;
          }
          else if (ldapValue.length() >=14 ) {
            ldapValue = ldapValue.substring(0,14);
          }
          else {
            SilverTrace.error("authentication", "AuthenticationLDAP.calculateDaysBeforeExpiration()",
                "authentication.EX_BAD_DATE_FORMAT", "ldapValue="+ldapValue);
            return Integer.MAX_VALUE;
          }
          pwdLastSet = format.parse(pwdLastSetAttr.getStringValue());
        } catch (ParseException e) {
          SilverTrace.error("authentication", "AuthenticationLDAP.calculateDaysBeforeExpiration()",
              "authentication.EX_BAD_DATE_FORMAT", e);
        }
    }
    SilverTrace.debug("authentication", "AuthenticationLDAP.calculateDaysBeforeExpiration()",
        "root.MSG_GEN_PARAM_VALUE", "pwdLastSet = " + DateUtil.getOutputDateAndHour(pwdLastSet, "fr"));

    Date now = new Date();
    long delayInMilliseconds = pwdLastSet.getTime() - now.getTime();
    SilverTrace.debug("authentication", "AuthenticationLDAP.calculateDaysBeforeExpiration()",
        "root.MSG_GEN_PARAM_VALUE", "delayInMilliseconds = " + delayInMilliseconds);
    int delayInDays = Math.round((float) ((delayInMilliseconds / (1000 * 3600 * 24)) + m_PwdMaxAge));
    SilverTrace.debug("authentication", "AuthenticationLDAP.calculateDaysBeforeExpiration()",
        "root.MSG_GEN_EXIT_METHOD", "delayInDays = " + delayInDays);
    return delayInDays;
  }

  /**
   * Overrides Authentication.internalChangePassword to offer password update capabilities
   * @param login user login
   * @param oldPassword user old password
   * @param newPassword user new password
   * @throws AuthenticationException
   */
  @Override
  protected void internalChangePassword(String login, String oldPassword, String newPassword)
      throws AuthenticationException {
    String userFullDN = null;
    String searchString = m_UserLoginFieldName + "=" + login;
    String[] strAttributes = { "sAMAccountName", "memberOf" };

    try {
      // Bind as the admin for the search
      m_LDAPConnection.bind(LDAPConnection.LDAP_V3, m_AccessLogin,
          m_AccessPasswd.getBytes(Charsets.UTF_8));

      // Get user DN
      SilverTrace.info("authentication", "AuthenticationLDAP.changePassword()",
          "root.MSG_GEN_PARAM_VALUE", "UserFilter=" + searchString);
      LDAPSearchResults res = m_LDAPConnection.search(m_UserBaseDN, LDAPConnection.SCOPE_SUB,
          searchString, strAttributes, false);
      if (!res.hasMore()) {
        throw new AuthenticationBadCredentialException(
            "AuthenticationLDAP.changePassword()", SilverpeasException.ERROR,
            "authentication.EX_USER_NOT_FOUND", "User=" + login
            + ";LoginField=" + m_UserLoginFieldName);
      }
      LDAPEntry fe = res.next();
      userFullDN = fe.getDN();

      // re bind with the requested user to verify old password
      m_LDAPConnection.bind(LDAPConnection.LDAP_V3, userFullDN, oldPassword.getBytes(Charsets.UTF_8));

      LDAPModification mod = null;
      if (!StringUtil.isDefined(ldapImpl) || "ad".equalsIgnoreCase(ldapImpl)) {
        // prepare password change
        mod = changeActiveDirectoryPassword(newPassword);        
      } else if ("opends".equalsIgnoreCase(ldapImpl) || "openldap".equalsIgnoreCase(ldapImpl)) {
        // prepare password change
        mod = changeOpenDSPassword(newPassword);
      }
      // Perform the update
      m_LDAPConnection.modify(userFullDN, mod);
    } catch (Exception ex) {
      throw new AuthenticationHostException(
          "AuthenticationLDAP.internalAuthentication()",
          SilverpeasException.ERROR, "authentication.EX_LDAP_ACCESS_ERROR", ex);
    }
  }
  
  private LDAPModification changeActiveDirectoryPassword(String newPassword) {
    // Convert password to UTF-16LE
    String newQuotedPassword = "\"" + newPassword + "\"";
    byte[] newUnicodePassword = newQuotedPassword.getBytes(Charsets.UTF_16LE);

    // prepare password change
    return new LDAPModification(LDAPModification.REPLACE, new LDAPAttribute("unicodePwd",
        newUnicodePassword));
  }
  
  private LDAPModification changeOpenDSPassword(String newPassword) throws UnsupportedEncodingException {
    // prepare password change
    return new LDAPModification(LDAPModification.REPLACE, new LDAPAttribute("userPassword",
        newPassword));
  }

  static String[] extractBaseDNs(String baseDN) {
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
  protected void internalResetPassword(String login, String newPassword)
      throws AuthenticationException {
    String userFullDN = null;
    String searchString = m_UserLoginFieldName + "=" + login;
    String[] strAttributes = { "sAMAccountName", "memberOf" };

    try {
      // Bind as the admin for the search
      m_LDAPConnection.bind(LDAPConnection.LDAP_V3, m_AccessLogin,
          m_AccessPasswd.getBytes(Charsets.UTF_8));

      // Get user DN
      SilverTrace.info("authentication", "AuthenticationLDAP.changePassword()",
          "root.MSG_GEN_PARAM_VALUE", "UserFilter=" + searchString);
      LDAPSearchResults res = m_LDAPConnection.search(
          m_UserBaseDN, LDAPConnection.SCOPE_SUB, searchString, strAttributes, false);
      if (!res.hasMore()) {
        throw new AuthenticationBadCredentialException("AuthenticationLDAP.internalResetPassword()",
            SilverpeasException.ERROR, "authentication.EX_USER_NOT_FOUND", "User=" + login
            + ";LoginField=" + m_UserLoginFieldName);
      }
      LDAPEntry fe = res.next();
      userFullDN = fe.getDN();
      
      LDAPModification mod = null;
      if (!StringUtil.isDefined(ldapImpl) || "ad".equalsIgnoreCase(ldapImpl)) {
        // prepare password change
        mod = changeActiveDirectoryPassword(newPassword);
      } else if ("opends".equalsIgnoreCase(ldapImpl) || "openldap".equalsIgnoreCase(ldapImpl)) {
        // prepare password change
        mod = changeOpenDSPassword(newPassword);
      }

      // Perform the update
      m_LDAPConnection.modify(userFullDN, mod);
    } catch (Exception ex) {
      throw new AuthenticationHostException("AuthenticationLDAP.internalResetPassword()",
          SilverpeasException.ERROR, "authentication.EX_LDAP_ACCESS_ERROR", ex);
    }
  }

}