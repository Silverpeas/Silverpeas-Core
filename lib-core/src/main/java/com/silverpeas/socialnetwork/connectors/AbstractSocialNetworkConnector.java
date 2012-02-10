package com.silverpeas.socialnetwork.connectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.social.connect.UserProfile;

import com.silverpeas.socialnetwork.service.AccessToken;
import com.silverpeas.socialnetwork.service.SocialNetworkAuthorizationException;
import com.stratelia.webactiv.util.ResourceLocator;

public abstract class AbstractSocialNetworkConnector implements SocialNetworkConnector{

	private ResourceLocator settings = null;

	void init() {
	  settings = new ResourceLocator(
        "com.silverpeas.socialnetwork.settings.socialNetworkSettings", "");
	}

	@Override
	abstract public String buildAuthenticateUrl(String callBackURL);

	@Override
	abstract public AccessToken exchangeForAccessToken(HttpServletRequest request, String callBackURL) throws SocialNetworkAuthorizationException;

	@Override
	abstract public UserProfile getUserProfile(AccessToken authorizationToken);

	@Override
	abstract public String getUserProfileId(AccessToken authorizationToken);

	protected ResourceLocator getSettings() {
	  return settings;
	}
}
