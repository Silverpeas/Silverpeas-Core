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

package com.silverpeas.authentication;

import com.silverpeas.socialnetwork.model.ExternalAccount;
import com.silverpeas.socialnetwork.model.SocialNetworkID;
import com.silverpeas.socialnetwork.service.AccessToken;
import com.silverpeas.socialnetwork.service.SocialNetworkService;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;
import net.sourceforge.spnego.SpnegoPrincipal;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
import org.silverpeas.cache.service.CacheServiceFactory;
import org.silverpeas.core.admin.OrganisationControllerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Parameters used in the authentication process. Theses parameters are fetched from both the
 * incoming HTTP request used for authenticating the user and its HTTP session.
 */
public class AuthenticationParameters {

  private final int keyMaxLength = 12;

  private String login;
  private String password;
  private String domainId;
  private String storedPassword;
  private String cryptedPassword;
  private String clearPassword;
  private boolean casMode;
  private boolean ssoMode;
  private boolean userByInternalAuthTokenMode;

  private boolean socialNetworkMode;
  private SocialNetworkID networkId;

  public AuthenticationParameters(HttpServletRequest request) {
    ResourceLocator authenticationSettings = new ResourceLocator(
        "com.silverpeas.authentication.settings.authenticationSettings",
        "");
    HttpSession session = request.getSession();
    boolean cookieEnabled = authenticationSettings.getBoolean(
        "cookieEnabled", false);
    UserDetail userByInternalAuthToken = getUserByInternalAuthToken(request);
    this.ssoMode = (getSSOUser(request) != null);
    this.casMode = (getCASUser(session) != null);
    checkSocialNetworkMode(session);

    String stringKey = convert2Alpha(session.getId());
    boolean useNewEncryptionMode = StringUtil.isDefined(request
        .getParameter("Var2"));

    if (userByInternalAuthToken != null) {
      userByInternalAuthTokenMode = true;
      login = userByInternalAuthToken.getLogin();
      domainId = userByInternalAuthToken.getDomainId();
      password = "";
    } else if (ssoMode) {
      login = getSSOUser(request);
      password = "";
    } else if (casMode) {
      login = getCASUser(session);
      password = "";
    } else if (socialNetworkMode) {
      // nothing else to do
    } else if (useNewEncryptionMode) {
      login = request.getParameter("Var2");
      password = request.getParameter("Var1");
      storedPassword = request.getParameter("tq");
      cryptedPassword = request.getParameter("dq");
    } else {
      // Get the parameters from the login page
      login = request.getParameter("Login");
      password = request.getParameter("Password");
      storedPassword = request.getParameter("storePassword");
      cryptedPassword = request.getParameter("cryptedPassword");
    }

    SilverTrace.debug("authentication", "AuthenticationServlet.doPost()",
        "root.MSG_GEN_PARAM_VALUE", "sCryptedPassword = "
        + cryptedPassword);

    decodePassword(cookieEnabled, stringKey, useNewEncryptionMode);
  }

  private void checkSocialNetworkMode(HttpSession session) {
    this.socialNetworkMode = false;
    this.networkId = SocialNetworkService.getInstance().getSocialNetworkIDUsedForLogin(session);
    if (this.networkId != null) {
      AccessToken authorizationToken =
          SocialNetworkService.getInstance().getStoredAuthorizationToken(session, networkId);
      String profileId =
          SocialNetworkService.getInstance().getSocialNetworkConnector(networkId)
          .getUserProfileId(authorizationToken);
      ExternalAccount account =
          SocialNetworkService.getInstance().getExternalAccount(networkId, profileId);

      UserDetail user = OrganisationControllerFactory
          .getOrganisationController().getUserDetail(account.getSilverpeasUserId());
      this.domainId = user.getDomainId();
      this.login = user.getLogin();
      this.socialNetworkMode = (user != null);
    }
  }

  private void decodePassword(boolean cookieEnabled, String stringKey,
      boolean newEncryptMode) {
    CredentialEncryption encryption = CredentialEncryptionFactory.getInstance()
        .getEncryption();
    if (newEncryptMode) {
      String decodedLogin = encryption.decode(login, stringKey, false);
      clearPassword = ((!StringUtil.isDefined(cryptedPassword)) ? encryption
          .decode(password, stringKey, false) : encryption.decode(
          password, stringKey, true));
      if (cookieEnabled) {
        if (StringUtil.isDefined(cryptedPassword)) {
          decodedLogin = encryption.decode(login, stringKey, true);
        }
      }
      login = decodedLogin;
    } else {
      clearPassword = ((!StringUtil.isDefined(cryptedPassword)) ? password
          : encryption.decode(password));
    }
  }

  /**
   * Convert a string to a string with only letters in upper case
   * @param toConvert
   * @return the String in UpperCase
   */
  private String convert2Alpha(String toConvert) {
    String alphaString = "";
    for (int i = 0; i < toConvert.length()
        && alphaString.length() < keyMaxLength; i++) {
      int asciiCode = toConvert.toUpperCase().charAt(i);
      if (asciiCode >= 65 && asciiCode <= 90) {
        alphaString += toConvert.substring(i, i + 1);
      }
    }
    // We fill the key to keyMaxLength char. if not enough letters in
    // sessionId.
    if (alphaString.length() < keyMaxLength) {
      alphaString += "ZFGHZSZHHJNT".substring(0, keyMaxLength
          - alphaString.length());
    }
    return alphaString;
  }

  private String getCASUser(HttpSession session) {
    String casUser = null;
    if (session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION) != null) {
      casUser = ((Assertion) session
          .getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION))
          .getPrincipal().getName();
    }
    return casUser;
  }

  private String getSSOUser(HttpServletRequest request) {
    if (request.getUserPrincipal() instanceof SpnegoPrincipal) {
      return request.getRemoteUser();
    }
    return null;
  }

  /**
   * Internal server method of user authentication. This method consists to use {@link
   * org.silverpeas.cache.service.CacheService}. The module that must authenticate a user by this
   * way have to set a token value to the request attribute "internalAuthToken". The token has to be
   * a key of the common cache that references a {@link UserDetail}
   * @param request
   * @return
   */
  private UserDetail getUserByInternalAuthToken(HttpServletRequest request) {
    String internalAuthToken = (String) request.getAttribute("internalAuthToken");
    if (StringUtil.isDefined(internalAuthToken)) {
      if (CacheServiceFactory.getCacheService().get(internalAuthToken) instanceof UserDetail) {
        return (UserDetail) CacheServiceFactory.getCacheService().remove(internalAuthToken);
      }
    }
    return null;
  }

  public String getLogin() {
    return login;
  }

  public String getPassword() {
    return password;
  }

  public String getStoredPassword() {
    return storedPassword;
  }

  public String getCryptedPassword() {
    return cryptedPassword;
  }

  public String getClearPassword() {
    return clearPassword;
  }

  public boolean isCasMode() {
    return casMode;
  }

  public boolean isSsoMode() {
    return ssoMode;
  }

  public boolean isUserByInternalAuthTokenMode() {
    return userByInternalAuthTokenMode;
  }

  public boolean isSocialNetworkMode() {
    return socialNetworkMode;
  }

  public String getDomainId() {
    return domainId;
  }

}
