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

/*
 * Authentication.java
 *
 * Created on 6 aout 2001
 */

package com.stratelia.silverpeas.authentication;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * This class must be extended by all classes that can perform authentication of users.
 * @author tleroi
 * @version
 */
public abstract class Authentication {
  protected boolean m_Enabled = true;

  public final static String ENC_TYPE_UNIX = "CryptUnix";
  public final static String ENC_TYPE_MD5 = "CryptMd5";
  public final static String ENC_TYPE_CLEAR = "ClearText";

  public static final String PASSWORD_IS_ABOUT_TO_EXPIRE = "Svp_Pwd_About_To_Expire";
  public static final String PASSWORD_CHANGE_ALLOWED = "Svp_Password_Change_Allowed";

  void setEnabled(boolean enabled) {
    m_Enabled = enabled;
  }

  boolean getEnabled() {
    return m_Enabled;
  }

  abstract public void init(String authenticationServerName,
      ResourceLocator propFile);

  /**
   * This method authenticates the user with the given (clear text) password. It returns true if the
   * user is validated, or throws an exception if the authentication could not be peformed, whatever
   * the reason. If the authentication could not be performed because the credentials are invalid
   * (e.g. wrong password), the AuthenticationException code should be set to
   * EXCEPTION_BAD_CREDENTIALS.
   */
  public boolean authenticate(String login, String passwd,
      HttpServletRequest request) throws AuthenticationException {
    if ((login == null) || (login.length() <= 0)) {
      throw new AuthenticationException("AuthenticationServer.authenticate",
          SilverpeasException.ERROR, "authentication.EX_LOGIN_EMPTY");
    }
    // Authenticate
    try {
      openConnection();
      internalAuthentication(login, passwd, request);
      closeConnection();
    } finally {
      try {
        closeConnection();
      } catch (AuthenticationException closeEx) {
        // The exception that could occur in the emergency stop is not
        // interesting
        SilverTrace.error("authentication", "Authentication.authenticate",
            "root.EX_EMERGENCY_CONNECTION_CLOSE_FAILED", "", closeEx);
      }
    }
    return (true);
  }

  abstract protected void openConnection() throws AuthenticationException;

  abstract protected void internalAuthentication(String login, String passwd)
      throws AuthenticationException;

  abstract protected void closeConnection() throws AuthenticationException;

  protected void internalAuthentication(String login, String passwd,
      HttpServletRequest request) throws AuthenticationException {
    internalAuthentication(login, passwd);
  }

  protected boolean getBooleanProperty(ResourceLocator resources,
      String propertyName, boolean defaultValue) {
    String value;
    boolean valret = defaultValue;
    value = resources.getString(propertyName);
    if (value != null) {
      if ("true".equalsIgnoreCase(value)) {
        valret = true;
      } else {
        valret = false;
      }
    }
    return valret;
  }

  /**
   * Change user password
   * @param login user login
   * @param oldPassword user old password
   * @param newPassword user new password
   * @return true if succeeded
   * @throws AuthenticationException
   * @see internalChangePassword
   */
  public boolean changePassword(String login, String oldPassword,
      String newPassword) throws AuthenticationException {
    if ((login == null) || (login.length() <= 0)) {
      throw new AuthenticationException("AuthenticationServer.changePassword",
          SilverpeasException.ERROR, "authentication.EX_LOGIN_EMPTY");
    }
    // Authenticate
    try {
      openConnection();
      internalChangePassword(login, oldPassword, newPassword);
      closeConnection();
    } finally {
      try {
        closeConnection();
      } catch (AuthenticationException closeEx) { // The exception that could
        // occur in the emergency stop is not interesting
        SilverTrace.error("authentication", "Authentication.changePassword",
            "root.EX_EMERGENCY_CONNECTION_CLOSE_FAILED", "", closeEx);
      }
    }
    return (true);
  }

  public boolean resetPassword(String login, String newPassword)
      throws AuthenticationException {
    if ((login == null) || (login.length() == 0)) {
      throw new AuthenticationException("AuthenticationServer.resetPassword",
          SilverpeasException.ERROR, "authentication.EX_LOGIN_EMPTY");
    }

    // Authenticate
    try {
      openConnection();
      internalResetPassword(login, newPassword);
      closeConnection();
    } finally {
      try {
        closeConnection();
      } catch (AuthenticationException closeEx) {
        // The exception that could occur in the emergency stop is not interesting.
        SilverTrace.error("authentication", "Authentication.resetPassword",
            "root.EX_EMERGENCY_CONNECTION_CLOSE_FAILED", "", closeEx);
      }
    }
    return true;
  }

  /**
   * This method systematically throws UnsupportedOperationException ! So you have to override this
   * method to offer password update capabilities
   * @param login user login
   * @param oldPassword user old password
   * @param newPassword user new password
   * @throws AuthenticationException
   */
  protected void internalChangePassword(String login, String oldPassword,
      String newPassword) throws AuthenticationException {
    throw new AuthenticationPwdChangeNotAvailException(
        "AuthenticationServer.changePassword", SilverpeasException.ERROR,
        "authentication.EX_PASSWD_CHANGE_NOTAVAILABLE");
  }

  protected void internalResetPassword(String login, String newPassword)
      throws AuthenticationException {
    throw new AuthenticationPwdChangeNotAvailException(
        "AuthenticationServer.internalResetPassword", SilverpeasException.ERROR,
        "authentication.EX_PASSWD_CHANGE_NOTAVAILABLE");
  }

}
