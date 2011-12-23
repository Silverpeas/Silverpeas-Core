package com.silverpeas.socialnetwork.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.social.connect.UserProfile;
import org.springframework.social.oauth1.OAuthToken;

import com.silverpeas.socialnetwork.model.ExternalAccount;
import com.silverpeas.socialnetwork.model.SocialNetworkID;


public interface SocialNetworkService {

	static final String AUTHORIZATION_TOKEN_SESSION_ATTR = "socialnetwork_authorization_token";
  static final String SOCIALNETWORK_ID_SESSION_ATTR = "socialnetwork_id";

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

	ExternalAccount getExternalAccount(SocialNetworkID networkId, String profileId);

	String getUserProfileId(AccessToken authorizationToken);

	void createExternalAccount(SocialNetworkID linkedin, String userId, String profileId);
}
