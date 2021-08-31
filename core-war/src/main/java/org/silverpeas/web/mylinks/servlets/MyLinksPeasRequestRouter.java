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
package org.silverpeas.web.mylinks.servlets;

import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.core.notification.message.MessageNotifier;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.http.RequestParameterDecoder;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.webapi.mylinks.MyLinkEntity;
import org.silverpeas.web.mylinks.control.MyLinksPeasSessionController;

import java.util.Collection;

public class MyLinksPeasRequestRouter extends ComponentRequestRouter<MyLinksPeasSessionController> {

  private static final long serialVersionUID = 8154867777629797580L;
  private static final String VIEW_LINKS_VIEW = "ViewLinks";
  private static final String INSTANCE_ID_PARAM = "InstanceId";
  private static final String URL_RETURN_PARAM = "UrlReturn";

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  @Override
  public String getSessionControlBeanName() {
    return "MyLinks";
  }

  @Override
  public MyLinksPeasSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new MyLinksPeasSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param myLinksSC The component Session Control, build and initialised.
   * @param request the current HttpRequest
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, MyLinksPeasSessionController myLinksSC,
      HttpRequest request) {
    String destination;
    String rootDest = "/myLinksPeas/jsp/";
    try {
      if (function.startsWith("Main")) {
        myLinksSC.setScope(MyLinksPeasSessionController.SCOPE_USER);
        myLinksSC.setUrl(null);
        MessageNotifier.addInfo(myLinksSC.getString("myLinks.draganddrop.info"));
        destination = getDestination(VIEW_LINKS_VIEW, myLinksSC, request);
      } else if (function.equals("ComponentLinks")) {
        // Retrieve instance identifier
        String instanceId = request.getParameter(INSTANCE_ID_PARAM);
        String url = request.getParameter(URL_RETURN_PARAM);
        if (!StringUtil.isDefined(url)) {
          url = URLUtil.getApplicationURL() + URLUtil.getURL(null, instanceId) + "Main";
        }
        myLinksSC.setInstanceId(instanceId);
        myLinksSC.setUrl(url);
        request.setAttribute(URL_RETURN_PARAM, url);
        request.setAttribute(INSTANCE_ID_PARAM, instanceId);
        destination = getDestination(VIEW_LINKS_VIEW, myLinksSC, request);
      } else if (function.equals("ObjectLinks")) {
        // retrieve object id and instance id
        String objectId = request.getParameter("ObjectId");
        String instanceId = request.getParameter(INSTANCE_ID_PARAM);
        String url = request.getParameter(URL_RETURN_PARAM);
        myLinksSC.setUrl(url);
        myLinksSC.setInstanceId(instanceId);
        myLinksSC.setObjectId(objectId);
        request.setAttribute(URL_RETURN_PARAM, url);
        destination = getDestination(VIEW_LINKS_VIEW, myLinksSC, request);
      } else if (function.equals(VIEW_LINKS_VIEW)) {
        final Collection<LinkDetail> links;
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
        request.setAttribute(URL_RETURN_PARAM, myLinksSC.getUrl());
        request.setAttribute(INSTANCE_ID_PARAM, myLinksSC.getInstanceId());
        destination = rootDest + "viewLinks.jsp";
      } else if (function.equals("CreateLink")) {
        MyLinkEntity myLinkEntity = RequestParameterDecoder.decode(request, MyLinkEntity.class);
        myLinksSC.createLink(myLinkEntity);
        destination = getDestination(VIEW_LINKS_VIEW, myLinksSC, request);
      } else if (function.equals("UpdateLink")) {
        MyLinkEntity myLinkEntity = RequestParameterDecoder.decode(request, MyLinkEntity.class);
        myLinksSC.updateLink(myLinkEntity);
        destination = getDestination(VIEW_LINKS_VIEW, myLinksSC, request);
      } else if (function.equals("DeleteLinks")) {
        Object o = request.getParameterValues("linkCheck");
        if (o != null) {
          String[] links = (String[]) o;
          myLinksSC.deleteLinks(links);
        }
        destination = getDestination(VIEW_LINKS_VIEW, myLinksSC, request);
      } else {
        destination = getDestination(VIEW_LINKS_VIEW, myLinksSC, request);
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }
    return destination;
  }
}
