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
package org.silverpeas.core.web.authentication;

import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.cache.model.Cache;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.security.authentication.AuthenticationCredential;
import org.silverpeas.core.socialnetwork.model.ExternalAccount;
import org.silverpeas.core.socialnetwork.model.SocialNetworkID;
import org.silverpeas.core.socialnetwork.service.AccessToken;
import org.silverpeas.core.socialnetwork.service.SocialNetworkService;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.sso.SilverpeasSsoPrincipal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Parameters used in the authentication process. Theses parameters are fetched from both the
 * incoming HTTP request used for authenticating the user and its HTTP session.
 */
public class AuthenticationParameters {

  private final SettingBundle authenticationSettings = ResourceLocator.getSettingBundle(
      "org.silverpeas.authentication.settings.authenticationSettings");
  private static final int KEY_MAX_LENGTH = 12;

  private String login;
  private String password;
  private String domainId;
  private String domainIdParam;
  private boolean casMode;
  private SilverpeasSsoPrincipal ssoPrincipal;
  private boolean userByInternalAuthTokenMode;
  private boolean useNewEncryptionMode;
  private boolean secured;

  private boolean socialNetworkMode;
  private AuthenticationCredential credential;

  AuthenticationParameters(HttpServletRequest request) {
    HttpSession session = request.getSession();
    UserDetail userByInternalAuthToken = getUserByInternalAuthToken(request);
    this.ssoPrincipal = getSSOPrincipal(request);
    this.casMode = (getCASUser(session) != null);
    checkSocialNetworkMode(session);

    String stringKey = convert2Alpha(session.getId());
    useNewEncryptionMode = StringUtil.isDefined(request
        .getParameter("Var2"));

    domainIdParam = request.getParameter("DomainId");
    secured = request.isSecure();

    final String notTakenIntoAccount = "";
    if (userByInternalAuthToken != null) {
      userByInternalAuthTokenMode = true;
      login = userByInternalAuthToken.getLogin();
      domainId = userByInternalAuthToken.getDomainId();
      password = notTakenIntoAccount;
    } else if (ssoPrincipal != null) {
      login = ssoPrincipal.getLogin();
      password = notTakenIntoAccount;
    } else if (casMode) {
      login = getCASUser(session);
      password = notTakenIntoAccount;
    } else if (socialNetworkMode) {
      // nothing else to do
    } else if (useNewEncryptionMode) {
      login = request.getParameter("Var2");
      password = request.getParameter("Password");
    } else {
      // Get the parameters from the login page
      login = request.getParameter("Login");
      password = request.getParameter("Password");
    }

    decodeLogin(stringKey, useNewEncryptionMode);
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

  public boolean isCasMode() {
    return casMode;
  }

  public boolean isSsoMode() {
    return ssoPrincipal != null;
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
      return ssoPrincipal.getDomainId();
    } else if (isCasMode()) {
      return authenticationSettings.getString("cas.authentication.domainId", "0");
    }
    OrganizationController controller = OrganizationControllerProvider.getOrganisationController();
    return controller.getDomain(domainIdParam).getId();
  }

  private void checkSocialNetworkMode(HttpSession session) {
    this.socialNetworkMode = false;
    final SocialNetworkID networkId = SocialNetworkService.getInstance().getSocialNetworkIDUsedForLogin(session);
    if (networkId != null) {
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
      this.socialNetworkMode = true;
    }
  }

  private void decodeLogin(String stringKey, boolean newEncryptMode) {
    CredentialEncryption encryption = CredentialEncryption.getInstance();
    login = newEncryptMode ? encryption.decode(login, stringKey, false) : login;
  }

  /**
   * Convert a string to a string with only letters in upper case
   * @param toConvert the string to convert
   * @return the String in UpperCase
   */
  private String convert2Alpha(String toConvert) {
    StringBuilder alphaString = new StringBuilder();
    String convertInUpperCase = toConvert.toUpperCase();
    for (int i = 0; i < toConvert.length() && alphaString.length() < KEY_MAX_LENGTH; i++) {
      int asciiCode = convertInUpperCase.charAt(i);
      if (asciiCode >= 65 && asciiCode <= 90) {
        alphaString.append(toConvert.charAt(i));
      }
    }
    // We fill the key to keyMaxLength char. if not enough letters in
    // sessionId.
    if (alphaString.length() < KEY_MAX_LENGTH) {
      alphaString.append("ZFGHZSZHHJNT".substring(0, KEY_MAX_LENGTH
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

  private SilverpeasSsoPrincipal getSSOPrincipal(HttpServletRequest request) {
    if (request.getUserPrincipal() instanceof SilverpeasSsoPrincipal) {
      return (SilverpeasSsoPrincipal) request.getUserPrincipal();
    }
    return null;
  }

  /**
   * Internal server method of user authentication. This method consists to use {@link
   * Cache}. The module that must authenticate a user by this
   * way have to set a token value to the request attribute "internalAuthToken". The token has to be
   * a key of the common cache that references a {@link UserDetail}
   * @param request the request to perform
   * @return the details of a user.
   */
  private UserDetail getUserByInternalAuthToken(HttpServletRequest request) {
    String internalAuthToken = (String) request.getAttribute("internalAuthToken");
    if (StringUtil.isDefined(internalAuthToken)) {
      Cache cache = CacheAccessorProvider.getApplicationCacheAccessor().getCache();
      if (cache.get(internalAuthToken) instanceof UserDetail) {
        return (UserDetail) cache.remove(internalAuthToken);
      }
    }
    return null;
  }
}
