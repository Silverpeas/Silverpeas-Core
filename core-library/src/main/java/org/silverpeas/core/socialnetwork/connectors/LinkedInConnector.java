/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.socialnetwork.connectors;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.silverpeas.core.socialnetwork.qualifiers.LinkedIn;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.linkedin.connect.LinkedInConnectionFactory;
import org.springframework.social.oauth1.AuthorizedRequestToken;
import org.springframework.social.oauth1.OAuth1Operations;
import org.springframework.social.oauth1.OAuth1Parameters;
import org.springframework.social.oauth1.OAuthToken;

import org.silverpeas.core.socialnetwork.service.AccessToken;

@LinkedIn
@Singleton
public class LinkedInConnector extends AbstractSocialNetworkConnector {
  private Map<String, OAuthToken> requestTokens = new HashMap<>();
  private LinkedInConnectionFactory connectionFactory = null;
  private String consumerKey = null;
  private String secretKey = null;

  public LinkedInConnector() {
  }

  @PostConstruct
  void init() {
    super.init();
    consumerKey = getSettings().getString("linkedIn.consumerKey");
    secretKey = getSettings().getString("linkedIn.secretKey");
    connectionFactory = new LinkedInConnectionFactory(consumerKey, secretKey);
  }

  @Override
  public String buildAuthenticateUrl(String callBackURL) {
    OAuth1Operations oauthOperations = connectionFactory.getOAuthOperations();
    OAuth1Parameters params = new OAuth1Parameters();

    // Build Request token and store it for future use
    OAuthToken requestToken = oauthOperations.fetchRequestToken(callBackURL, null);
    requestTokens.put(requestToken.getValue(), requestToken);

    return oauthOperations.buildAuthenticateUrl(requestToken.getValue(), params);
  }

  @Override
  public AccessToken exchangeForAccessToken(HttpServletRequest request, String callBackURL) {
    String authVerifier = request.getParameter("oauth_verifier");
    String oauthToken = request.getParameter("oauth_token");

    OAuth1Operations oauthOperations = connectionFactory.getOAuthOperations();
    OAuthToken savedToken = requestTokens.get(oauthToken);

    if (savedToken != null) {
      AuthorizedRequestToken authorizedRequestToken =
          new AuthorizedRequestToken(savedToken, authVerifier);
      OAuthToken accessToken = oauthOperations.exchangeForAccessToken(authorizedRequestToken, null);
      return new AccessToken(accessToken);
    }

    return null;
  }

  @Override
  public UserProfile getUserProfile(AccessToken authorizationToken) {
    return connectionFactory.createConnection(authorizationToken.getoAuthToken())
        .fetchUserProfile();
  }

  @Override
  public String getUserProfileId(AccessToken authorizationToken) {
    return connectionFactory.createConnection(authorizationToken.getoAuthToken()).getApi()
        .profileOperations().getProfileId();
  }

  /*
   * (non-Javadoc)
   * @see
   * SocialNetworkConnector#updateStatus(com.silverpeas.
   * socialnetwork.service.AccessToken, java.lang.String)
   */
  @Override
  public void updateStatus(AccessToken authorizationToken, String status) {
    connectionFactory.createConnection(authorizationToken.getoAuthToken()).updateStatus(status);
  }

  @Override
  public void setJavascriptAttributes(HttpServletRequest request) {
    request.setAttribute("LI_loadSDK", getSDKLoadingScript(request));
  }

  private String getSDKLoadingScript(HttpServletRequest request) {
    StringBuffer code = new StringBuffer();

    code.append("<script type=\"text/javascript\">\n");
    code.append("function onLoadLinkedIn() {\n");
    code.append("   if (initLI) { initLI() };\n");
    code.append(" };\n");
    code.append("</script>\n");

    code.append("<script type=\"text/javascript\" src=\"http://platform.linkedin.com/in.js\">\n");
    code.append("  api_key: ").append(consumerKey).append("\n");
    code.append("  onLoad: onLoadLinkedIn\n");
    code.append("  authorize: true\n");
    code.append("</script>\n");

    return code.toString();
  }

}
