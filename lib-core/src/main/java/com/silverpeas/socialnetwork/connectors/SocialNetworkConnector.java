package com.silverpeas.socialnetwork.connectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.social.connect.UserProfile;

import com.silverpeas.socialnetwork.service.AccessToken;
import com.silverpeas.socialnetwork.service.SocialNetworkAuthorizationException;


public interface SocialNetworkConnector {

	/**
	 * Build URL to call for authentication over social network
	 *
	 * @param callBackURL	the URL to call to after authentication
	 *
	 * @return authentication URL as String
	 */
	String buildAuthenticateUrl(String callBackURL);

	AccessToken exchangeForAccessToken(HttpServletRequest request, String callBackURL) throws SocialNetworkAuthorizationException;

	UserProfile getUserProfile(AccessToken authorizationToken);

	String getUserProfileId(AccessToken authorizationToken);

  void updateStatus(AccessToken authorizationToken, String status);

  void setJavascriptAttributes(HttpServletRequest request);
}
