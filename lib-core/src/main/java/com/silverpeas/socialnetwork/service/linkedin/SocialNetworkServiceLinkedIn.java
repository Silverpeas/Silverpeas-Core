package com.silverpeas.socialnetwork.service.linkedin;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.springframework.social.connect.UserProfile;
import org.springframework.social.linkedin.connect.LinkedInConnectionFactory;
import org.springframework.social.oauth1.AuthorizedRequestToken;
import org.springframework.social.oauth1.OAuth1Operations;
import org.springframework.social.oauth1.OAuth1Parameters;
import org.springframework.social.oauth1.OAuthToken;

import com.silverpeas.socialnetwork.service.AbstractSocialNetworkService;
import com.silverpeas.socialnetwork.service.AccessToken;

@Named("serviceLinkedIn")
public class SocialNetworkServiceLinkedIn extends AbstractSocialNetworkService  {
	private Map<String,OAuthToken> requestTokens = new HashMap<String, OAuthToken>();
	private LinkedInConnectionFactory connectionFactory = null;

	public SocialNetworkServiceLinkedIn() {
		connectionFactory = new LinkedInConnectionFactory("178g9y3p1850", "ejpvgiKBhH0jwXYm");
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
			AuthorizedRequestToken authorizedRequestToken = new AuthorizedRequestToken(savedToken, authVerifier);
			OAuthToken accessToken = oauthOperations.exchangeForAccessToken(authorizedRequestToken, null);
			return new AccessToken(accessToken);
		}

		return null;
	}

	@Override
	public UserProfile getUserProfile(AccessToken authorizationToken) {
		 return connectionFactory.createConnection(authorizationToken.getoAuthToken()).fetchUserProfile();
	}

	@Override
	public String getUserProfileId(AccessToken authorizationToken) {
		return connectionFactory.createConnection(authorizationToken.getoAuthToken()).getApi().getProfileId();
	}

}
