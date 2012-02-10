package com.silverpeas.socialnetwork.connectors;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.springframework.social.connect.UserProfile;
import org.springframework.social.linkedin.api.impl.LinkedInTemplate;
import org.springframework.social.linkedin.connect.LinkedInConnectionFactory;
import org.springframework.social.oauth1.AuthorizedRequestToken;
import org.springframework.social.oauth1.OAuth1Operations;
import org.springframework.social.oauth1.OAuth1Parameters;
import org.springframework.social.oauth1.OAuthToken;

import com.silverpeas.socialnetwork.service.AccessToken;

@Named("linkedInConnector")
public class LinkedInConnector extends AbstractSocialNetworkConnector  {
	private Map<String,OAuthToken> requestTokens = new HashMap<String, OAuthToken>();
	private LinkedInConnectionFactory connectionFactory = null;
  private String consumerKey = null;
  private String secretKey = null;

	 public LinkedInConnector() { }

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

  /* (non-Javadoc)
   * @see com.silverpeas.socialnetwork.connectors.SocialNetworkConnector#updateStatus(com.silverpeas.socialnetwork.service.AccessToken, java.lang.String)
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
