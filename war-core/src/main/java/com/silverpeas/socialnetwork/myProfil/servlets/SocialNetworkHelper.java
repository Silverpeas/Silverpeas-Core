/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.socialnetwork.myProfil.servlets;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.socialnetwork.connectors.SocialNetworkConnector;
import com.silverpeas.socialnetwork.model.ExternalAccount;
import com.silverpeas.socialnetwork.model.SocialNetworkID;
import com.silverpeas.socialnetwork.myProfil.control.MyProfilSessionController;
import com.silverpeas.socialnetwork.service.AccessToken;
import com.silverpeas.socialnetwork.service.SocialNetworkAuthorizationException;
import com.silverpeas.socialnetwork.service.SocialNetworkService;
import com.stratelia.silverpeas.peasCore.URLManager;

public class SocialNetworkHelper {

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
    StringBuffer redirectURL = new StringBuffer();
    redirectURL.append(URLManager.getFullApplicationURL(request));

    switch (route) {
      case LinkToSVP:
        redirectURL.append("/RMyProfil/jsp/CreateLinkToSVP?&networkId=");
        break;

      case PublishStatus:
        redirectURL.append("/RMyProfil/jsp/DoPublishStatus?&networkId=");
        break;
    }

    redirectURL.append(networkId);

    return redirectURL.toString();
  }

  public String buildAuthenticationURL(HttpServletRequest request, MyProfileRoutes route) {
    SocialNetworkID networkId = SocialNetworkID.valueOf(request.getParameter("networkId"));
    SocialNetworkConnector connector =
        SocialNetworkService.getInstance().getSocialNetworkConnector(networkId);

    return connector.buildAuthenticateUrl(getRedirectURL(networkId, request, route));
  }

  public void linkToSilverpeas(MyProfilSessionController mpsc, HttpServletRequest request) {
    SocialNetworkID networkId = SocialNetworkID.valueOf(request.getParameter("networkId"));
    SocialNetworkConnector connector =
        SocialNetworkService.getInstance().getSocialNetworkConnector(networkId);

    AccessToken authorizationToken;
    try {
      authorizationToken =
          connector.exchangeForAccessToken(request, getRedirectURL(networkId, request,
          MyProfileRoutes.LinkToSVP));
    } catch (SocialNetworkAuthorizationException e) {
      request.setAttribute("errorMessage", "authorizationFailed");
      return;
    }

    SocialNetworkService.getInstance().storeAuthorizationToken(request.getSession(true), networkId,
        authorizationToken);
    String profileId = connector.getUserProfileId(authorizationToken);
    SocialNetworkService.getInstance()
        .createExternalAccount(networkId, mpsc.getUserId(), profileId);
  }

  public void unlinkFromSilverpeas(MyProfilSessionController myProfilSC, HttpServletRequest request) {
    myProfilSC.unlinkSocialNetworkFromSilverpeas(SocialNetworkID.valueOf(request
        .getParameter("networkId")));
  }

  public void publishStatus(MyProfilSessionController myProfilSC, HttpServletRequest request) {
    SocialNetworkID networkId = SocialNetworkID.valueOf(request.getParameter("networkId"));
    SocialNetworkConnector connector =
        SocialNetworkService.getInstance().getSocialNetworkConnector(networkId);

    AccessToken authorizationToken;
    try {
      authorizationToken =
          connector.exchangeForAccessToken(request, getRedirectURL(networkId, request,
          MyProfileRoutes.PublishStatus));
    } catch (SocialNetworkAuthorizationException e) {
      request.setAttribute("errorMessage", "authorizationFailed");
      return;
    }

    String status = myProfilSC.getUserFul(myProfilSC.getUserId()).getStatus();
    SocialNetworkService.getInstance().storeAuthorizationToken(request.getSession(true), networkId,
        authorizationToken);
    connector.updateStatus(authorizationToken, status);
  }
}
