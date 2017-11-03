/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.socialnetwork.connectors;

import org.silverpeas.core.socialnetwork.qualifiers.Facebook;
import org.silverpeas.core.socialnetwork.service.AccessToken;
import org.silverpeas.core.util.URLUtil;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

@Facebook
@Singleton
public class FacebookConnector extends AbstractSocialNetworkConnector {

  private FacebookConnectionFactory connectionFactory = null;
  private String consumerKey = null;
  private String secretKey = null;

  @PostConstruct
  @Override
  void init() {
    super.init();
    consumerKey = getSettings().getString("facebook.consumerKey");
    secretKey = getSettings().getString("facebook.secretKey");
    connectionFactory = new FacebookConnectionFactory(consumerKey, secretKey);
  }

  @Override
  public String getUserProfileId(AccessToken authorizationToken) {
    AccessGrant accessGrant = authorizationToken.getAccessGrant();
    return getConnectionFactory().createConnection(accessGrant)
        .getApi()
        .userOperations()
        .getUserProfile()
        .getId();
  }

  @Override
  public void updateStatus(AccessToken authorizationToken, String status) {
    FacebookTemplate facebook =
        new FacebookTemplate(authorizationToken.getAccessGrant().getAccessToken());
    facebook.feedOperations().updateStatus(status);
  }

  @Override
  public void setJavascriptAttributes(HttpServletRequest request) {
    request.setAttribute("FB_loadSDK", getSDKLoadingScript(request));
  }

  @Override
  protected FacebookConnectionFactory getConnectionFactory() {
    return connectionFactory;
  }

  private String getSDKLoadingScript(HttpServletRequest request) {
    StringBuilder code = new StringBuilder();

    code.append("<div id=\"fb-root\"></div>\n");
    code.append("<script>\n");

    code.append(" window.fbAsyncInit = function() {\n");

    code.append("   FB.init({\n");
    code.append("     appId      : '").append(consumerKey).append("', // App ID\n");
    code.append("     channelUrl : '").append(getChannelURL(request))
        .append("', // Channel File\n");
    code.append("     status     : true, // check login status\n");
    code
        .append(
        "     cookie     : true, // enable cookies to allow the server to access the session\n");
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
    String fullURL = URLUtil.getFullApplicationURL(request);
    fullURL = fullURL.substring(fullURL.indexOf("//"));

    return fullURL + "/socialNetwork/jsp/channelFB.html";
  }
}
