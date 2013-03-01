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
package com.stratelia.silverpeas.authentication;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class manage the authentication for a given domain
 */
public class AuthenticationServer {

  protected String fallbackType;
  protected int nbServers;
  protected List<Authentication> authServers;
  protected boolean allowPasswordChange;

  public AuthenticationServer(String authServerName) {
    nbServers = 0;
    try {
      ResourceLocator propFile = new ResourceLocator("com.stratelia.silverpeas.authentication."
          + authServerName, "");
      fallbackType = propFile.getString("fallbackType");
      allowPasswordChange = propFile.getBoolean("allowPasswordChange", false);
      int nbServers = Integer.parseInt(propFile.getString("autServersCount"));
      authServers = new ArrayList<Authentication>();
      for (int i = 0; i < nbServers; i++) {
        String serverName = "autServer" + i;
        if (propFile.getBoolean(serverName + ".enabled", true)) {
          try {
            Authentication autObj = (Authentication) Class.forName(propFile.getString(serverName
                + ".type")).newInstance();
            autObj.init(serverName, propFile);
            autObj.setEnabled(true);
            authServers.add(autObj);
            this.nbServers++;
          } catch (Exception ex) {
            SilverTrace.error("authentication", "AuthenticationServer.AuthenticationServer",
                "authentication.EX_CANT_INSTANCIATE_SERVER_CLASS", authServerName + " / "
                + serverName, ex);
          }
        }
      }
    } catch (Exception e) {
      SilverTrace.error("authentication", "AuthenticationServer.AuthenticationServer",
          "authentication.EX_DOMAIN_INFO_ERROR", "Server=" + authServerName, e);
    }
  }

  public void authenticate(String login, String passwd, HttpServletRequest request) throws
      AuthenticationException {
    boolean bNotFound = true;
    AuthenticationException lastException = null;

    if ((login == null) || (login.length() <= 0)) {
      throw new AuthenticationException("AuthenticationServer.authenticate",
          SilverpeasException.ERROR, "authentication.EX_LOGIN_EMPTY");
    }
    int i = 0;
    while ((i < nbServers) && bNotFound) {
      Authentication autObj = authServers.get(i);
      if (autObj.getEnabled()) {
        try {
          autObj.authenticate(login, passwd, request);
          bNotFound = false;
        } catch (AuthenticationPasswordAboutToExpireException ex) {
          // authentication succeeded but throw exception to alert that password is about to expire
          // Store information in request and return
          bNotFound = false;
          if (request != null) {
            request.getSession().
                setAttribute(Authentication.PASSWORD_IS_ABOUT_TO_EXPIRE, Boolean.TRUE);
          }
        } catch (AuthenticationPwdNotAvailException ex) {
          SilverTrace.info("authentication", "AuthenticationServer.authenticate",
              "authentication.EX_PWD_NOT_AVAILABLE", "ServerNbr=" + i + ";User=" + login, ex);
          lastException = ex;
        } catch (AuthenticationHostException ex) {
          if ("none".equals(fallbackType)) {
            throw ex;
          }
          SilverTrace.info("authentication",
              "AuthenticationServer.authenticate",
              "authentication.EX_AUTHENTICATION_HOST_ERROR", "ServerNbr="
              + i + ";User=" + login, ex);
          lastException = ex;
        } catch (AuthenticationBadCredentialException ex) {
          if ("none".equals(fallbackType) || "ifNotRejected".equals(fallbackType)) {
            throw ex;
          }
          SilverTrace.info("authentication",
              "AuthenticationServer.authenticate",
              "authentication.EX_AUTHENTICATION_BAD_CREDENTIAL", "ServerNbr="
              + i + ";User=" + login, ex);
          lastException = ex;
        } catch (AuthenticationException ex) {
          if ("none".equals(fallbackType)) {
            throw ex;
          }
          SilverTrace.info("authentication", "AuthenticationServer.authenticate",
              "authentication.EX_AUTHENTICATION_REJECTED_BY_SERVER", "ServerNbr=" + i
              + ";User=" + login, ex);
          lastException = ex;
        }
      }
      i++;
    }
    if (bNotFound) {
      if (lastException == null) {
        throw new AuthenticationException("AuthenticationServer.authenticate",
            SilverpeasException.ERROR, "authentication.EX_NO_SERVER_AVAILABLE");
      }
      throw new AuthenticationException("AuthenticationServer.authenticate",
          SilverpeasException.ERROR, "authentication.EX_AUTHENTICATION_FAILED_LAST_ERROR",
          lastException);
    }
  }

  public void changePassword(String login, String oldPassword, String newPassword) throws
      AuthenticationException {
    int i;
    Authentication autObj;
    boolean bNotFound = true;
    AuthenticationException lastException = null;

    if (!allowPasswordChange) {
      throw new AuthenticationPwdChangeNotAvailException("AuthenticationServer.changePassword",
          SilverpeasException.ERROR, "authentication.EX_PASSWD_CHANGE_NOTAVAILABLE");
    }

    if ((login == null) || (login.length() <= 0)) {
      throw new AuthenticationException("AuthenticationServer.changePassword",
          SilverpeasException.ERROR, "authentication.EX_LOGIN_EMPTY");
    }
    i = 0;
    while ((i < nbServers) && bNotFound) {
      autObj = authServers.get(i);
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
          if ("none".equals(fallbackType)) {
            throw ex;
          }
          SilverTrace.info("authentication",
              "AuthenticationServer.changePassword",
              "authentication.EX_AUTHENTICATION_HOST_ERROR", "ServerNbr="
              + Integer.toString(i) + ";User=" + login, ex);
          lastException = ex;

        } catch (AuthenticationBadCredentialException ex) {
          if ("none".equals(fallbackType) || "ifNotRejected".equals(fallbackType)) {
            throw ex;
          }
          SilverTrace.info("authentication", "AuthenticationServer.changePassword",
              "authentication.EX_AUTHENTICATION_BAD_CREDENTIAL", "ServerNbr="
              + Integer.toString(i) + ";User=" + login, ex);
          lastException = ex;

        } catch (AuthenticationException ex) {
          if ("none".equals(fallbackType)) {
            throw ex;
          }
          SilverTrace.info("authentication",
              "AuthenticationServer.changePassword",
              "authentication.EX_AUTHENTICATION_REJECTED_BY_SERVER",
              "ServerNbr=" + Integer.toString(i) + ";User=" + login, ex);
          lastException = ex;
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

  public boolean isPasswordChangeAllowed() {
    return allowPasswordChange;
  }

  public void resetPassword(String login, String newPassword) throws AuthenticationException {
    if (!allowPasswordChange) {
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
    while ((i < nbServers) && bNotFound) {
      autObj = authServers.get(i);
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
          if ("none".equals(fallbackType)) {
            throw ex;
          }
          SilverTrace.info("authentication", "AuthenticationServer.resetPassword",
              "authentication.EX_AUTHENTICATION_HOST_ERROR", "ServerNbr=" + i + ";User=" + login, ex);
          lastException = ex;
        } catch (AuthenticationBadCredentialException ex) {
          if ("none".equals(fallbackType) || "ifNotRejected".equals(fallbackType)) {
            throw ex;
          }
          SilverTrace.info("authentication", "AuthenticationServer.resetPassword",
              "authentication.EX_AUTHENTICATION_BAD_CREDENTIAL",
              "ServerNbr=" + i + ";User=" + login, ex);
          lastException = ex;
        } catch (AuthenticationException ex) {
          if ("none".equals(fallbackType)) {
            throw ex;
          }
          SilverTrace.info("authentication", "AuthenticationServer.resetPassword",
              "authentication.EX_AUTHENTICATION_REJECTED_BY_SERVER", "ServerNbr=" + i + ";User="
              + login, ex);
          lastException = ex;
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
