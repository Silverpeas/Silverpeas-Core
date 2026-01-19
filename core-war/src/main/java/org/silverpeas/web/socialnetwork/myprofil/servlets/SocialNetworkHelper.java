/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.socialnetwork.myprofil.servlets;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.silverpeas.core.socialnetwork.connectors.AccessToken;
import org.silverpeas.core.socialnetwork.connectors.SocialNetworkConnector;
import org.silverpeas.core.socialnetwork.model.ExternalAccount;
import org.silverpeas.core.socialnetwork.model.SocialNetworkID;
import org.silverpeas.core.socialnetwork.service.SocialNetworkAuthorizationException;
import org.silverpeas.core.socialnetwork.service.SocialNetworkService;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.web.socialnetwork.myprofil.control.MyProfilSessionController;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

import static org.silverpeas.web.socialnetwork.myprofil.servlets.MyProfileRoutes.LinkToSVP;
import static org.silverpeas.web.socialnetwork.myprofil.servlets.MyProfileRoutes.PublishStatus;

public class SocialNetworkHelper {

  @Inject
  private SocialNetworkService socialNetworkService;

  protected SocialNetworkHelper() {

  }

  public void getAllMyNetworks(MyProfilSessionController mpsc, HttpServletRequest request) {
    request.setAttribute("userNetworks", mpsc.getAllMyNetworks());
  }

  public void setupJSAttributes(MyProfilSessionController mpsc, HttpServletRequest request) {
    Map<SocialNetworkID, ExternalAccount> allMyNetworks = mpsc.getAllMyNetworks();
    for (SocialNetworkID networkId : allMyNetworks.keySet()) {
      SocialNetworkConnector connector =
          SocialNetworkService.getInstance().getSocialNetworkConnector(networkId);
      connector.setJavascriptAttributes(request);
    }
  }

  private String getRedirectURL(SocialNetworkID networkId, HttpServletRequest request,
      MyProfileRoutes route) {
    StringBuilder redirectURL = new StringBuilder();
    redirectURL.append(URLUtil.getFullApplicationURL(request));

    if (route == LinkToSVP) {
      redirectURL.append("/RMyProfil/jsp/AddLinkToSVP?&networkId=");
    } else if (route == PublishStatus) {
      redirectURL.append("/RMyProfil/jsp/DoPublishStatus?&networkId=");
    }

    redirectURL.append(networkId);

    return redirectURL.toString();
  }

  public String buildAuthenticationURL(HttpServletRequest request, MyProfileRoutes route) {
    SocialNetworkID networkId = SocialNetworkID.valueOf(request.getParameter("networkId"));
    SocialNetworkConnector connector = socialNetworkService.getSocialNetworkConnector(networkId);

    return connector.buildAuthenticateUrl(getRedirectURL(networkId, request, route));
  }

  public void linkToSilverpeas(MyProfilSessionController mpsc, HttpServletRequest request) {
    TokenNegociation negociation = negotiateAccessToken(request, LinkToSVP);
    if (negociation == null) return;
    String profileId = negociation.connector.getUserProfileId(negociation.token);
    socialNetworkService.createExternalAccount(negociation.networkId, mpsc.getUserId(), profileId);
  }

  public void unlinkFromSilverpeas(MyProfilSessionController myProfilSC,
      HttpServletRequest request) {
    myProfilSC.unlinkSocialNetworkFromSilverpeas(SocialNetworkID.valueOf(request
        .getParameter("networkId")));
  }

  public void publishStatus(MyProfilSessionController myProfilSC, HttpServletRequest request) {
    TokenNegociation negociation = negotiateAccessToken(request, PublishStatus);
    if (negociation == null) return;

    String status = myProfilSC.getUserFull(myProfilSC.getUserId()).getStatus();
    negociation.connector.updateStatus(negociation.token, status);
  }

  private @Nullable TokenNegociation negotiateAccessToken(HttpServletRequest request,
      MyProfileRoutes route) {
    SocialNetworkID networkId = SocialNetworkID.valueOf(request.getParameter("networkId"));
    SocialNetworkConnector connector = socialNetworkService.getSocialNetworkConnector(networkId);

    AccessToken authorizationToken;
    try {
      authorizationToken =
          connector.exchangeForAccessToken(request, getRedirectURL(networkId, request, route));
    } catch (SocialNetworkAuthorizationException e) {
      request.setAttribute("errorMessage", "authorizationFailed");
      return null;
    }

    socialNetworkService.storeAuthorizationToken(request.getSession(true), networkId,
        authorizationToken);
    return new TokenNegociation(networkId, connector, authorizationToken);
  }

  private static class TokenNegociation {
    public final SocialNetworkID networkId;
    public final SocialNetworkConnector connector;
    public final AccessToken token;

    public TokenNegociation(SocialNetworkID networkId, SocialNetworkConnector connector,
        AccessToken token) {
      this.networkId = networkId;
      this.connector = connector;
      this.token = token;
    }
  }
}
