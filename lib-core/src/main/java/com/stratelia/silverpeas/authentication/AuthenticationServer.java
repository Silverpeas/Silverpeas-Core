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

package com.stratelia.silverpeas.authentication;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class manage the authentication for a given domain
 * @author tleroi
 * @version
 */
public class AuthenticationServer {
  protected String m_FallbackType;
  protected int m_nbServers;
  protected List<Authentication> m_AutServers;
  protected boolean m_allowPasswordChange;

  public AuthenticationServer(String authServerName) {
    int nbServers;
    int i;
    Authentication autObj;
    String serverName;
    ResourceLocator propFile;

    m_nbServers = 0;
    try {
      propFile = new ResourceLocator("com.stratelia.silverpeas.authentication."
          + authServerName, "");
      m_FallbackType = propFile.getString("fallbackType");
      m_allowPasswordChange = getBooleanProperty(propFile, "allowPasswordChange", false);
      nbServers = Integer.parseInt(propFile.getString("autServersCount"));
      m_AutServers = new ArrayList<Authentication>();
      for (i = 0; i < nbServers; i++) {
        serverName = "autServer" + i;
        if (getBooleanProperty(propFile, serverName + ".enabled", true)) {
          try {
            autObj = (Authentication) Class.forName(
                propFile.getString(serverName + ".type")).newInstance();
            autObj.init(serverName, propFile);
            autObj.setEnabled(true);
            m_AutServers.add(autObj);
            m_nbServers++;
          } catch (Exception ex) {
            SilverTrace.error("authentication",
                "AuthenticationServer.AuthenticationServer",
                "authentication.EX_CANT_INSTANCIATE_SERVER_CLASS",
                authServerName + " / " + serverName, ex);
          }
        }
      }
    } catch (Exception e) {
      SilverTrace.error("authentication",
          "AuthenticationServer.AuthenticationServer",
          "authentication.EX_DOMAIN_INFO_ERROR", "Server=" + authServerName, e);
    }
  }

  public void authenticate(String login, String passwd,
      HttpServletRequest request) throws AuthenticationException {
    int i;
    Authentication autObj;
    boolean bNotFound = true;
    AuthenticationException lastException = null;

    if ((login == null) || (login.length() <= 0)) {
      throw new AuthenticationException("AuthenticationServer.authenticate",
          SilverpeasException.ERROR, "authentication.EX_LOGIN_EMPTY");
    }
    i = 0;
    while ((i < m_nbServers) && bNotFound) {
      autObj = m_AutServers.get(i);
      if (autObj.getEnabled()) {
        try {
          autObj.authenticate(login, passwd, request);
          bNotFound = false;
        } catch (AuthenticationPasswordAboutToExpireException ex) {
          // authentication succeeded but throw exception to alert that password
          // is about to expire
          // Store information in request and return
          bNotFound = false;
          if (request != null) {
            request.getSession().
                setAttribute(Authentication.PASSWORD_IS_ABOUT_TO_EXPIRE, Boolean.TRUE);
          }
        } catch (AuthenticationPwdNotAvailException ex) {
          SilverTrace.info("authentication",
              "AuthenticationServer.authenticate",
              "authentication.EX_PWD_NOT_AVAILABLE", "ServerNbr="
              + Integer.toString(i) + ";User=" + login, ex);
          lastException = ex;
        } catch (AuthenticationHostException ex) {
          if (m_FallbackType.equals("none")) {
            throw ex;
          } else {
            SilverTrace.info("authentication",
                "AuthenticationServer.authenticate",
                "authentication.EX_AUTHENTICATION_HOST_ERROR", "ServerNbr="
                + i + ";User=" + login, ex);
            lastException = ex;
          }
        } catch (AuthenticationBadCredentialException ex) {
          if (m_FallbackType.equals("none")
              || m_FallbackType.equals("ifNotRejected")) {
            throw ex;
          } else {
            SilverTrace.info("authentication",
                "AuthenticationServer.authenticate",
                "authentication.EX_AUTHENTICATION_BAD_CREDENTIAL", "ServerNbr="
                + i + ";User=" + login, ex);
            lastException = ex;
          }
        } catch (AuthenticationException ex) {
          if (m_FallbackType.equals("none")) {
            throw ex;
          } else {
            SilverTrace.info("authentication",
                "AuthenticationServer.authenticate",
                "authentication.EX_AUTHENTICATION_REJECTED_BY_SERVER",
                "ServerNbr=" + i + ";User=" + login, ex);
            lastException = ex;
          }
        }
      }
      i++;
    }
    if (bNotFound) {
      if (lastException == null) {
        throw new AuthenticationException("AuthenticationServer.authenticate",
            SilverpeasException.ERROR, "authentication.EX_NO_SERVER_AVAILABLE");
      } else {
        throw new AuthenticationException("AuthenticationServer.authenticate",
            SilverpeasException.ERROR,
            "authentication.EX_AUTHENTICATION_FAILED_LAST_ERROR", lastException);
      }
    }
  }

  public void changePassword(String login, String oldPassword,
      String newPassword) throws AuthenticationException {
    int i;
    Authentication autObj;
    boolean bNotFound = true;
    AuthenticationException lastException = null;

    if (!m_allowPasswordChange) {
      throw new AuthenticationPwdChangeNotAvailException("AuthenticationServer.changePassword",
          SilverpeasException.ERROR, "authentication.EX_PASSWD_CHANGE_NOTAVAILABLE");
    }

    if ((login == null) || (login.length() <= 0)) {
      throw new AuthenticationException("AuthenticationServer.changePassword",
          SilverpeasException.ERROR, "authentication.EX_LOGIN_EMPTY");
    }
    i = 0;
    while ((i < m_nbServers) && bNotFound) {
      autObj = m_AutServers.get(i);
      if (autObj.getEnabled()) {
        try {
          autObj.changePassword(login, oldPassword, newPassword);
          bNotFound = false;
        } catch (AuthenticationPwdChangeNotAvailException ex) {
          SilverTrace.info("authentication",
              "AuthenticationServer.changePassword",
              "authentication.EX_PASSWD_CHANGE_NOTAVAILABLE", "ServerNbr="
              + Integer.toString(i) + ";User=" + login, ex);
          lastException = ex;
        } catch (AuthenticationHostException ex) {
          if (m_FallbackType.equals("none")) {
            throw ex;
          } else {
            SilverTrace.info("authentication",
                "AuthenticationServer.changePassword",
                "authentication.EX_AUTHENTICATION_HOST_ERROR", "ServerNbr="
                + Integer.toString(i) + ";User=" + login, ex);
            lastException = ex;
          }
        } catch (AuthenticationBadCredentialException ex) {
          if (m_FallbackType.equals("none")
              || m_FallbackType.equals("ifNotRejected")) {
            throw ex;
          } else {
            SilverTrace.info("authentication",
                "AuthenticationServer.changePassword",
                "authentication.EX_AUTHENTICATION_BAD_CREDENTIAL", "ServerNbr="
                + Integer.toString(i) + ";User=" + login, ex);
            lastException = ex;
          }
        } catch (AuthenticationException ex) {
          if (m_FallbackType.equals("none")) {
            throw ex;
          } else {
            SilverTrace.info("authentication",
                "AuthenticationServer.changePassword",
                "authentication.EX_AUTHENTICATION_REJECTED_BY_SERVER",
                "ServerNbr=" + Integer.toString(i) + ";User=" + login, ex);
            lastException = ex;
          }
        }
      }
      i++;
    }
    if (bNotFound) {
      if (lastException == null) {
        throw new AuthenticationException(
            "AuthenticationServer.changePassword", SilverpeasException.ERROR,
            "authentication.EX_NO_SERVER_AVAILABLE");
      } else {
        throw new AuthenticationException(
            "AuthenticationServer.changePassword", SilverpeasException.ERROR,
            "authentication.EX_AUTHENTICATION_FAILED_LAST_ERROR", lastException);
      }
    }
  }

  protected final boolean getBooleanProperty(ResourceLocator resources,
      String propertyName, boolean defaultValue) {
    String value = resources.getString(propertyName);
    if (value != null) {
      return "true".equalsIgnoreCase(value);
    }
    return defaultValue;
  }

  public boolean isPasswordChangeAllowed() {
    return m_allowPasswordChange;
  }

  public void resetPassword(String login, String newPassword) throws AuthenticationException {
    if (!m_allowPasswordChange) {
      throw new AuthenticationPwdChangeNotAvailException("AuthenticationServer.resetPassword",
          SilverpeasException.ERROR, "authentication.EX_PASSWD_CHANGE_NOTAVAILABLE");
    }

    if ((login == null) || (login.length() <= 0)) {
      throw new AuthenticationException("AuthenticationServer.resetPassword",
          SilverpeasException.ERROR, "authentication.EX_LOGIN_EMPTY");
    }

    Authentication autObj;
    boolean bNotFound = true;
    AuthenticationException lastException = null;
    int i = 0;
    while ((i < m_nbServers) && bNotFound) {
      autObj = m_AutServers.get(i);
      if (autObj.getEnabled()) {
        try {
          autObj.resetPassword(login, newPassword);
          bNotFound = false;
        } catch (AuthenticationPwdChangeNotAvailException ex) {
          SilverTrace.info("authentication", "AuthenticationServer.resetPassword",
              "authentication.EX_PASSWD_CHANGE_NOTAVAILABLE",
              "ServerNbr=" + Integer.toString(i) + ";User=" + login, ex);
          lastException = ex;
        } catch (AuthenticationHostException ex) {
          if (m_FallbackType.equals("none")) {
            throw ex;
          } else {
            SilverTrace.info("authentication", "AuthenticationServer.resetPassword",
                "authentication.EX_AUTHENTICATION_HOST_ERROR",
                "ServerNbr=" + Integer.toString(i) + ";User=" + login, ex);
            lastException = ex;
          }
        } catch (AuthenticationBadCredentialException ex) {
          if (m_FallbackType.equals("none") || m_FallbackType.equals("ifNotRejected")) {
            throw ex;
          } else {
            SilverTrace.info("authentication", "AuthenticationServer.resetPassword",
                "authentication.EX_AUTHENTICATION_BAD_CREDENTIAL",
                "ServerNbr=" + Integer.toString(i) + ";User=" + login, ex);
            lastException = ex;
          }
        } catch (AuthenticationException ex) {
          if (m_FallbackType.equals("none")) {
            throw ex;
          } else {
            SilverTrace.info("authentication", "AuthenticationServer.resetPassword",
                "authentication.EX_AUTHENTICATION_REJECTED_BY_SERVER",
                "ServerNbr=" + Integer.toString(i) + ";User=" + login, ex);
            lastException = ex;
          }
        }
      }
      i++;
    }
    if (bNotFound) {
      if (lastException == null) {
        throw new AuthenticationException("AuthenticationServer.resetPassword",
            SilverpeasException.ERROR, "authentication.EX_NO_SERVER_AVAILABLE");
      } else {
        throw new AuthenticationException("AuthenticationServer.resetPassword",
            SilverpeasException.ERROR, "authentication.EX_AUTHENTICATION_FAILED_LAST_ERROR",
            lastException);
      }
    }
  }
}
