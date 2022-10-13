/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.socialnetwork.connectors;

import org.silverpeas.core.socialnetwork.service.AccessToken;
import org.silverpeas.core.socialnetwork.service.SocialNetworkAuthorizationException;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.GrantType;
import org.springframework.social.oauth2.OAuth2Operations;
import org.springframework.social.oauth2.OAuth2Parameters;

import javax.servlet.http.HttpServletRequest;

public abstract class AbstractSocialNetworkConnector implements SocialNetworkConnector {

  private SettingBundle settings = null;

  void init() {
    settings = ResourceLocator.getSettingBundle(
        "org.silverpeas.social.settings.socialNetworkSettings");
  }

  @Override
  public String buildAuthenticateUrl(String callBackURL) {
    OAuth2Operations oauthOperations = getConnectionFactory().getOAuthOperations();
    OAuth2Parameters params = new OAuth2Parameters();
    params.setRedirectUri(callBackURL);
    params.setScope("email,publish_stream,offline_access");

    return oauthOperations.buildAuthorizeUrl(GrantType.AUTHORIZATION_CODE, params);
  }

  @Override
  public AccessToken exchangeForAccessToken(HttpServletRequest request, String callBackURL)
      throws SocialNetworkAuthorizationException {
    String errorReason = request.getParameter("error_reason");
    if (errorReason != null) {
      throw new SocialNetworkAuthorizationException(
          "Social Network Authorization Asking failed: " + errorReason);
    } else {
      String authorizationCode = request.getParameter("code");

      OAuth2Operations oauthOperations = getConnectionFactory().getOAuthOperations();
      AccessGrant accessGrant =
          oauthOperations.exchangeForAccess(authorizationCode, callBackURL, null);
      return new AccessToken(accessGrant);
    }

  }

  @Override
  public UserProfile getUserProfile(AccessToken authorizationToken) {
    AccessGrant accessGrant = authorizationToken.getAccessGrant();
    return getConnectionFactory().createConnection(accessGrant).fetchUserProfile();
  }

  @Override
  public abstract String getUserProfileId(AccessToken authorizationToken);

  protected SettingBundle getSettings() {
    return settings;
  }

  protected abstract <T> OAuth2ConnectionFactory<T> getConnectionFactory();
}
