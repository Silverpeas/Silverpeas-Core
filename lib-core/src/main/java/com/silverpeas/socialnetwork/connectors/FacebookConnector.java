package com.silverpeas.socialnetwork.connectors;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.springframework.social.connect.UserProfile;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.GrantType;
import org.springframework.social.oauth2.OAuth2Operations;
import org.springframework.social.oauth2.OAuth2Parameters;

import com.silverpeas.socialnetwork.service.AccessToken;
import com.silverpeas.socialnetwork.service.SocialNetworkAuthorizationException;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.util.exception.SilverpeasException;

@Named("facebookConnector")
public class FacebookConnector extends AbstractSocialNetworkConnector {

	private FacebookConnectionFactory connectionFactory = null;
	private String consumerKey = null;
	private String secretKey = null;

	public FacebookConnector() { }

	@PostConstruct
	void init() {
    super.init();
	  consumerKey = getSettings().getString("facebook.consumerKey");
    secretKey = getSettings().getString("facebook.secretKey");
    connectionFactory = new FacebookConnectionFactory(consumerKey, secretKey);
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
			throw new SocialNetworkAuthorizationException("FacebookConnector.exchangeForAccessToken", SilverpeasException.WARNING, error_reason);
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

  /* (non-Javadoc)
   * @see com.silverpeas.socialnetwork.connectors.SocialNetworkConnector#updateStatus(com.silverpeas.socialnetwork.service.AccessToken, java.lang.String)
   */
  @Override
  public void updateStatus(AccessToken authorizationToken, String status) {
    FacebookTemplate facebook = new FacebookTemplate(authorizationToken.getAccessGrant().getAccessToken());
    facebook.feedOperations().updateStatus(status);
  }

  @Override
  public void setJavascriptAttributes(HttpServletRequest request) {
    request.setAttribute("FB_loadSDK", getSDKLoadingScript(request));
  }

  private String getSDKLoadingScript(HttpServletRequest request) {
    StringBuffer code = new StringBuffer();

    code.append("<div id=\"fb-root\"></div>\n");
    code.append("<script>\n");

    code.append(" window.fbAsyncInit = function() {\n");

    code.append("   FB.init({\n");
    code.append("     appId      : '").append(consumerKey).append("', // App ID\n");
    code.append("     channelUrl : '").append(getChannelURL(request)).append("', // Channel File\n");
    code.append("     status     : true, // check login status\n");
    code.append("     cookie     : true, // enable cookies to allow the server to access the session\n");
    code.append("     xfbml      : true  // parse XFBML\n");
    code.append("   });\n");

    code.append("   if (initFB) { initFB() };\n");
    code.append(" };\n");


    code.append(" // Load the SDK Asynchronously\n");
    code.append(" (function(d){\n");
    code.append("     var js, id = 'facebook-jssdk'; if (d.getElementById(id)) {return;}\n");
    code.append("     js = d.createElement('script'); js.id = id; js.async = true;\n");
    code.append("     js.src = '//connect.facebook.net/en_US/all.js';\n");
    code.append("     d.getElementsByTagName('head')[0].appendChild(js);\n");
    code.append("  }(document));\n");

    code.append("</script>\n");

    return code.toString();
  }

  private String getChannelURL(HttpServletRequest request) {
    String fullURL = URLManager.getFullApplicationURL(request);
    fullURL = fullURL.substring( fullURL.indexOf("//") );

    return fullURL + "/socialNetwork/jsp/channelFB.html";
  }



}
