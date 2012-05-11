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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.myLinksPeas.servlets;

import com.silverpeas.myLinks.model.LinkDetail;
import com.silverpeas.myLinksPeas.control.MyLinksPeasSessionController;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;

public class MyLinksPeasRequestRouter extends ComponentRequestRouter<MyLinksPeasSessionController> {

  private static final long serialVersionUID = 8154867777629797580L;

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "MyLinks";
  }

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  public MyLinksPeasSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new MyLinksPeasSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param myLinksSC The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function, MyLinksPeasSessionController myLinksSC,
      HttpServletRequest request) {
    SilverTrace.info("myLinksPeas", "MyLinksPeasRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "User=" + myLinksSC.getUserId() + " Function=" + function);

    String destination = "";
    String rootDest = "/myLinksPeas/jsp/";

    try {
      if (function.startsWith("Main")) {
        myLinksSC.setScope(MyLinksPeasSessionController.SCOPE_USER);
        myLinksSC.setUrl(null);

        destination = getDestination("ViewLinks", myLinksSC, request);
      } else if (function.equals("ComponentLinks")) {
        // recupere l'id de l'instance
        String instanceId = request.getParameter("InstanceId");
        String url = request.getParameter("UrlReturn");
        myLinksSC.setInstanceId(instanceId);
        myLinksSC.setUrl(url);
        request.setAttribute("UrlReturn", url);
        request.setAttribute("InstanceId", instanceId);

        destination = getDestination("ViewLinks", myLinksSC, request);
      } else if (function.equals("ObjectLinks")) {
        // recupere l'id de l'objet et de l'instance
        String objectId = request.getParameter("ObjectId");
        String instanceId = request.getParameter("InstanceId");
        String url = request.getParameter("UrlReturn");
        myLinksSC.setUrl(url);
        myLinksSC.setInstanceId(instanceId);
        myLinksSC.setObjectId(objectId);
        request.setAttribute("UrlReturn", url);

        destination = getDestination("ViewLinks", myLinksSC, request);
      } else if (function.equals("ViewLinks")) {
        Collection<LinkDetail> links = null;
        int scope = myLinksSC.getScope();
        switch (scope) {
          case MyLinksPeasSessionController.SCOPE_COMPONENT:
            links = myLinksSC.getAllLinksByInstance();
            break;
          case MyLinksPeasSessionController.SCOPE_OBJECT:
            links = myLinksSC.getAllLinksByObject();
            break;
          default:
            links = myLinksSC.getAllLinksByUser();
        }
        request.setAttribute("Links", links);
        request.setAttribute("UrlReturn", myLinksSC.getUrl());
        request.setAttribute("InstanceId", myLinksSC.getInstanceId());
        destination = rootDest + "viewLinks.jsp";
      } else if (function.equals("NewLink")) {
        boolean isVisible = myLinksSC.getScope() == MyLinksPeasSessionController.SCOPE_USER;
        request.setAttribute("IsVisible", new Boolean(isVisible));
        // appel jsp
        destination = rootDest + "linkManager.jsp";
      } else if (function.equals("CreateLink")) {
        // récupération des paramètres venus de l'écran de saisie et
        // création de
        // l'objet LinkDetail
        myLinksSC.createLink(generateLink(request, false));
        // retour sur le liste des liens
        destination = getDestination("ViewLinks", myLinksSC, request);
      } else if (function.equals("CreateLinkFromComponent")) {
        // récupération des paramètres transmis et création de l'objet
        // LinkDetail
        myLinksSC.createLink(generateLink(request, true));
        // affichage d'une fenêtre de confirmation
        destination = rootDest + "confirm.jsp";
      } else if (function.equals("EditLink")) {
        String linkId = request.getParameter("LinkId");
        LinkDetail link = myLinksSC.getLink(linkId);
        request.setAttribute("Link", link);
        boolean isVisible = myLinksSC.getScope() == MyLinksPeasSessionController.SCOPE_USER;
        request.setAttribute("IsVisible", new Boolean(isVisible));
        // appel jsp
        destination = rootDest + "linkManager.jsp";
      } else if (function.equals("UpdateLink")) {
        // récupération des paramètres venus de l'écran de saisie
        String linkId = request.getParameter("LinkId");
        LinkDetail link = generateLink(request, false);
        link.setLinkId(Integer.parseInt(linkId));
        // modification du lien
        myLinksSC.updateLink(link);
        // retour sur le liste des liens
        destination = getDestination("ViewLinks", myLinksSC, request);
      } else if (function.equals("DeleteLinks")) {
        Object o = request.getParameterValues("linkCheck");
        if (o != null) {
          String[] links = (String[]) o;
          myLinksSC.deleteLinks(links);
        }
        destination = getDestination("ViewLinks", myLinksSC, request);
      } else {
        destination = getDestination("ViewLinks", myLinksSC, request);
      }

    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    SilverTrace.info("myLinks", "MyLinksPeasRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
    return destination;
  }

  private LinkDetail generateLink(HttpServletRequest request, boolean decode)
      throws UnsupportedEncodingException {
    String name = request.getParameter("Name");
    String description = request.getParameter("Description");
    if (decode) {
      name = URLDecoder.decode(name, "UTF-8");
      description = URLDecoder.decode(description, "UTF-8");
    }
    String url = request.getParameter("Url");

    // supprimer le context en début d'url
    String sRequestURL = request.getRequestURL().toString();
    String m_sAbsolute =
        sRequestURL.substring(0, sRequestURL.length() - request.getRequestURI().length());
    String urlServeur = m_sAbsolute;
    if (url.startsWith(urlServeur)) {
      url = url.substring(urlServeur.length(), url.length());
    }
    String context = URLManager.getApplicationURL();
    if (url.startsWith(context)) {
      url = url.substring(context.length(), url.length());
    }
    boolean visible = StringUtil.getBooleanValue(request.getParameter("Visible"));
    boolean popup = StringUtil.getBooleanValue(request.getParameter("Popup"));
    if (!StringUtil.isDefined(name)) {
      name = url;
    }
    return new LinkDetail(name, description, url, visible, popup);
  }
}
