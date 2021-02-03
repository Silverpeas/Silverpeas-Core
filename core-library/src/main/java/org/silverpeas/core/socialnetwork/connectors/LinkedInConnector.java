/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.socialnetwork.qualifiers.LinkedIn;
import org.silverpeas.core.socialnetwork.service.AccessToken;
import org.springframework.social.linkedin.api.impl.LinkedInTemplate;
import org.springframework.social.linkedin.connect.LinkedInConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

@LinkedIn
@Service
@Singleton
public class LinkedInConnector extends AbstractSocialNetworkConnector {
  private LinkedInConnectionFactory connectionFactory = null;
  private String consumerKey = null;
  private String secretKey = null;

  @PostConstruct
  @Override
  void init() {
    super.init();
    consumerKey = getSettings().getString("linkedIn.consumerKey");
    secretKey = getSettings().getString("linkedIn.secretKey");
    connectionFactory = new LinkedInConnectionFactory(consumerKey, secretKey);
  }

  @Override
  public String getUserProfileId(AccessToken authorizationToken) {
    AccessGrant accessGrant = authorizationToken.getAccessGrant();
    return getConnectionFactory().createConnection(accessGrant)
        .getApi()
        .profileOperations()
        .getUserProfile()
        .getId();
  }

  @Override
  public void updateStatus(AccessToken authorizationToken, String status) {
    LinkedInTemplate linkedIn =
        new LinkedInTemplate(authorizationToken.getAccessGrant().getAccessToken());
    linkedIn.networkUpdateOperations().createNetworkUpdate(status);
  }

  @Override
  public void setJavascriptAttributes(HttpServletRequest request) {
    request.setAttribute("LI_loadSDK", getSDKLoadingScript());
  }

  @Override
  protected LinkedInConnectionFactory getConnectionFactory() {
    return connectionFactory;
  }

  private String getSDKLoadingScript() {
    StringBuilder code = new StringBuilder();

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
