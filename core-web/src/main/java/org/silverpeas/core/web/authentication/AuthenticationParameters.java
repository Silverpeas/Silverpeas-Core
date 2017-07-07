/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
package org.silverpeas.core.web.authentication;

import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.cache.model.Cache;
import org.silverpeas.core.security.authentication.AuthenticationCredential;
import org.silverpeas.core.socialnetwork.model.ExternalAccount;
import org.silverpeas.core.socialnetwork.model.SocialNetworkID;
import org.silverpeas.core.socialnetwork.service.AccessToken;
import org.silverpeas.core.socialnetwork.service.SocialNetworkService;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.spnego.SpnegoPrincipal;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Parameters used in the authentication process. Theses parameters are fetched from both the
 * incoming HTTP request used for authenticating the user and its HTTP session.
 */
public class AuthenticationParameters {

  private final SettingBundle authenticationSettings = ResourceLocator.getSettingBundle(
      "org.silverpeas.authentication.settings.authenticationSettings");
  private final int keyMaxLength = 12;

  private String login;
  private String password;
  private String domainId;
  private String storedPassword;
  private String cryptedPassword;
  private String clearPassword;
  private String domainIdParam;
  private boolean casMode;
  private boolean ssoMode;
  private boolean userByInternalAuthTokenMode;
  private boolean useNewEncryptionMode;
  private boolean secured;

  private boolean socialNetworkMode;
  private SocialNetworkID networkId;
  private AuthenticationCredential credential;

  public AuthenticationParameters(HttpServletRequest request) {
    HttpSession session = request.getSession();
    boolean cookieEnabled = authenticationSettings.getBoolean(
        "cookieEnabled", false);
    UserDetail userByInternalAuthToken = getUserByInternalAuthToken(request);
    this.ssoMode = (getSSOUser(request) != null);
    this.casMode = (getCASUser(session) != null);
    checkSocialNetworkMode(session);

    String stringKey = convert2Alpha(session.getId());
    useNewEncryptionMode = StringUtil.isDefined(request
        .getParameter("Var2"));

    domainIdParam = request.getParameter("DomainId");
    secured = request.isSecure();

    final String noTakenIntoAccount = "";
    if (userByInternalAuthToken != null) {
      userByInternalAuthTokenMode = true;
      login = userByInternalAuthToken.getLogin();
      domainId = userByInternalAuthToken.getDomainId();
      password = noTakenIntoAccount;
    } else if (ssoMode) {
      login = getSSOUser(request);
      password = noTakenIntoAccount;
    } else if (casMode) {
      login = getCASUser(session);
      password = noTakenIntoAccount;
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

    decodePassword(cookieEnabled, stringKey, useNewEncryptionMode);
  }

  public void setCredential(final AuthenticationCredential credential) {
    this.credential = credential;
  }

  public AuthenticationCredential getCredential() {
    return this.credential;
  }

  public boolean isNewEncryptionMode() {
    return this.useNewEncryptionMode;
  }

  public boolean isSecuredAccess() {
    return this.secured;
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
    if (isUserByInternalAuthTokenMode() || isSocialNetworkMode()) {
      return domainId;
    } else if (isSsoMode()) {
      return authenticationSettings.getString("sso.authentication.domainId", "0");
    } else if (isCasMode()) {
      return authenticationSettings.getString("cas.authentication.domainId", "0");
    }
    OrganizationController controller = OrganizationControllerProvider.getOrganisationController();
    return controller.getDomain(domainIdParam).getId();
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

      UserDetail user = OrganizationControllerProvider
          .getOrganisationController().getUserDetail(account.getSilverpeasUserId());
      this.domainId = user.getDomainId();
      this.login = user.getLogin();
      this.socialNetworkMode = (user != null);
    }
  }

  private void decodePassword(boolean cookieEnabled, String stringKey,
      boolean newEncryptMode) {
    CredentialEncryption encryption = CredentialEncryption.getInstance();
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
    StringBuilder alphaString = new StringBuilder();
    for (int i = 0; i < toConvert.length()
        && alphaString.length() < keyMaxLength; i++) {
      int asciiCode = toConvert.toUpperCase().charAt(i);
      if (asciiCode >= 65 && asciiCode <= 90) {
        alphaString.append(toConvert.substring(i, i + 1));
      }
    }
    // We fill the key to keyMaxLength char. if not enough letters in
    // sessionId.
    if (alphaString.length() < keyMaxLength) {
      alphaString.append("ZFGHZSZHHJNT".substring(0, keyMaxLength
          - alphaString.length()));
    }
    return alphaString.toString();
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
   * Cache}. The module that must authenticate a user by this
   * way have to set a token value to the request attribute "internalAuthToken". The token has to be
   * a key of the common cache that references a {@link UserDetail}
   * @param request
   * @return
   */
  private UserDetail getUserByInternalAuthToken(HttpServletRequest request) {
    String internalAuthToken = (String) request.getAttribute("internalAuthToken");
    if (StringUtil.isDefined(internalAuthToken)) {
      Cache cache = CacheServiceProvider.getApplicationCacheService().getCache();
      if (cache.get(internalAuthToken) instanceof UserDetail) {
        return (UserDetail) cache.remove(internalAuthToken);
      }
    }
    return null;
  }
}
