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
      SocialNetworkConnector connector =  SocialNetworkService.getInstance().getSocialNetworkConnector(networkId);
      connector.setJavascriptAttributes(request);
    }
  }

  private String getRedirectURL(SocialNetworkID networkId, HttpServletRequest request, MyProfileRoutes route) {
    StringBuffer redirectURL = new StringBuffer();
    redirectURL.append(URLManager.getFullApplicationURL(request));

    switch(route) {
      case LinkToSVP :
        redirectURL.append("/RMyProfil/jsp/CreateLinkToSVP?&networkId=");
        break;

      case PublishStatus :
        redirectURL.append("/RMyProfil/jsp/DoPublishStatus?&networkId=");
        break;
    }

    redirectURL.append(networkId);

    return redirectURL.toString();
  }

  public String buildAuthenticationURL(HttpServletRequest request, MyProfileRoutes route) {
    SocialNetworkID networkId = SocialNetworkID.valueOf(request.getParameter("networkId"));
    SocialNetworkConnector connector =  SocialNetworkService.getInstance().getSocialNetworkConnector(networkId);

    return connector.buildAuthenticateUrl( getRedirectURL(networkId, request, route) ) ;
  }

  public void linkToSilverpeas(MyProfilSessionController mpsc, HttpServletRequest request) {
    SocialNetworkID networkId = SocialNetworkID.valueOf( request.getParameter("networkId") );
    SocialNetworkConnector connector =  SocialNetworkService.getInstance().getSocialNetworkConnector(networkId);

    AccessToken authorizationToken;
    try {
      authorizationToken = connector.exchangeForAccessToken(request, getRedirectURL(networkId, request, MyProfileRoutes.LinkToSVP));
    } catch (SocialNetworkAuthorizationException e) {
      request.setAttribute("errorMessage", "authorizationFailed");
      return;
    }

    SocialNetworkService.getInstance().storeAuthorizationToken(request.getSession(true), networkId, authorizationToken);
    String profileId = connector.getUserProfileId(authorizationToken);
    SocialNetworkService.getInstance().createExternalAccount(networkId, mpsc.getUserId(), profileId);
  }

  public void unlinkFromSilverpeas(MyProfilSessionController myProfilSC, HttpServletRequest request) {
    myProfilSC.unlinkSocialNetworkFromSilverpeas( SocialNetworkID.valueOf(request.getParameter("networkId")) );
  }

  public void publishStatus(MyProfilSessionController myProfilSC, HttpServletRequest request) {
    SocialNetworkID networkId = SocialNetworkID.valueOf( request.getParameter("networkId") );
    SocialNetworkConnector connector =  SocialNetworkService.getInstance().getSocialNetworkConnector(networkId);

    AccessToken authorizationToken;
    try {
      authorizationToken = connector.exchangeForAccessToken(request, getRedirectURL(networkId, request, MyProfileRoutes.PublishStatus));
    } catch (SocialNetworkAuthorizationException e) {
      request.setAttribute("errorMessage", "authorizationFailed");
      return;
    }

    String status = myProfilSC.getUserFul(myProfilSC.getUserId()).getStatus();
    SocialNetworkService.getInstance().storeAuthorizationToken(request.getSession(true), networkId, authorizationToken);
    connector.updateStatus(authorizationToken, status);
  }
}
