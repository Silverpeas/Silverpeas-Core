package com.silverpeas.socialnetwork.service.facebook;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.springframework.social.connect.UserProfile;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.linkedin.connect.LinkedInConnectionFactory;
import org.springframework.social.oauth1.OAuthToken;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.GrantType;
import org.springframework.social.oauth2.OAuth2Operations;
import org.springframework.social.oauth2.OAuth2Parameters;
import org.springframework.stereotype.Service;

import com.silverpeas.socialnetwork.model.ExternalAccount;
import com.silverpeas.socialnetwork.model.SocialNetworkID;
import com.silverpeas.socialnetwork.service.AbstractSocialNetworkService;
import com.silverpeas.socialnetwork.service.AccessToken;
import com.silverpeas.socialnetwork.service.SocialNetworkAuthorizationException;
import com.silverpeas.socialnetwork.service.SocialNetworkService;
import com.stratelia.webactiv.util.exception.SilverpeasException;

@Named("serviceFacebook")
public class SocialNetworkServiceFacebook extends AbstractSocialNetworkService {

	private Map<String,OAuthToken> requestTokens = new HashMap<String, OAuthToken>();
	private FacebookConnectionFactory connectionFactory = null;

	public SocialNetworkServiceFacebook() {
		connectionFactory = new FacebookConnectionFactory("210906708982608", "bb7d245661ffbe2801b23d46d1337f35");
	}

	@Override
	public String buildAuthenticateUrl(String callBackURL) {
		OAuth2Operations oauthOperations = connectionFactory.getOAuthOperations();
		OAuth2Parameters params = new OAuth2Parameters();
		params.setRedirectUri(callBackURL);
		params.setScope("email,publish_stream,offline_access");

		return oauthOperations.buildAuthorizeUrl(GrantType.AUTHORIZATION_CODE, params);
	}

	@Override
	public AccessToken exchangeForAccessToken(HttpServletRequest request, String callBackURL) throws SocialNetworkAuthorizationException {
		String error_reason = request.getParameter("error_reason");
		if (error_reason != null) {
			throw new SocialNetworkAuthorizationException("SocialNetworkServiceFacebook.exchangeForAccessToken", SilverpeasException.WARNING, error_reason);
		}
		else {
			String authorizationCode = request.getParameter("code");

			OAuth2Operations oauthOperations = connectionFactory.getOAuthOperations();
			AccessGrant accessGrant = oauthOperations.exchangeForAccess(authorizationCode, callBackURL, null);
			return new AccessToken(accessGrant);
		}

	}

	@Override
	public UserProfile getUserProfile(AccessToken authorizationToken) {
		AccessGrant accessGrant = authorizationToken.getAccessGrant();
		UserProfile profile =  connectionFactory.createConnection(accessGrant).fetchUserProfile();

		return profile;
	}

	@Override
	public String getUserProfileId(AccessToken authorizationToken) {
		AccessGrant accessGrant = authorizationToken.getAccessGrant();
		String profileId =  connectionFactory.createConnection(accessGrant).getApi().userOperations().getUserProfile().getId();

		return profileId;
	}

}
