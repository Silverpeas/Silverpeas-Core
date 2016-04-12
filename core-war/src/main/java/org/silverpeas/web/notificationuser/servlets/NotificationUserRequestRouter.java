/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.notificationuser.servlets;

import org.silverpeas.web.notificationuser.Notification;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.web.notificationuser.control.NotificationUserSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.util.StringUtil;

/**
 * Class declaration
 * <p/>
 * @author
 */
public class NotificationUserRequestRouter extends ComponentRequestRouter<NotificationUserSessionController> {

  private static final long serialVersionUID = -5858231857279380747L;

  /**
   * Method declaration
   *
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  @Override
  public NotificationUserSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new NotificationUserSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for notificationUser, returns
   * "notificationUser"
   */
  @Override
  public String getSessionControlBeanName() {
    return "notificationUser";
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param nuSC The component Session Control, build and initialised.
   * @param request The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/notificationUser/jsp/notificationUser.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, NotificationUserSessionController nuSC,
      HttpRequest request) {
    // remarques
    // tous les paramètres des la jsp sont transferé par la request.
    // le UserPanel étant unique par session, il est impératif de récupérér
    // les objets selectionnés via userPanel et de transporter
    // les id des ses de jsp en jsp en soumettant un formulaire.
    // En effet, la notification peut être utilisée "en même temps" que le
    // client utilises userPanelPeas. Cela mélange les objets selectionnée.
    String destination;


    try {
      request.setCharacterEncoding("UTF-8");
      if (function.startsWith("Main")) {
        String theTargetsUsers = request.getParameter("theTargetsUsers");
        String theTargetsGroups = request.getParameter("theTargetsGroups");

        Notification notification = nuSC.resetNotification();
        if (theTargetsUsers != null || theTargetsGroups != null) {
          // predefined targets are given
          notification = nuSC.initTargets(theTargetsUsers, theTargetsGroups);
        }
        request.setAttribute("Notification", notification);

        boolean popupMode = StringUtil.getBooleanValue(request.getParameter("popupMode"));
        String param = request.getParameter("editTargets");
        boolean editTargets = true;
        if (StringUtil.isDefined(param)) {
          editTargets = StringUtil.getBooleanValue(param);
        }
        request.setAttribute("popupMode", popupMode);
        request.setAttribute("editTargets", editTargets);

        destination = "/notificationUser/jsp/notificationSender.jsp";
      } else if ("SetTarget".equals(function)) {
        String popupMode = request.getParameter("popupMode");
        nuSC.setNotification(request2Notification(request));
        StringBuilder paramValue = new StringBuilder("?popupMode=").append(popupMode);
        destination = nuSC.initSelectionPeas(paramValue.toString());
      } else if (function.startsWith("GetTarget")) {
        boolean popupMode = StringUtil.getBooleanValue(request.getParameter("popupMode"));
        request.setAttribute("popupMode", popupMode);
        request.setAttribute("editTargets", true);
        request.setAttribute("Notification", nuSC.getNotificationWithNewRecipients());
        destination = "/notificationUser/jsp/notificationSender.jsp";
      } else if (function.equals("SendNotif")) {
        Notification notification = request2Notification(request);
        nuSC.sendMessage(notification);
        destination = "/peasCore/jsp/close.jsp";
      } else {
        destination = "/notificationUser/jsp/" + function;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    return destination;
  }

  private Notification request2Notification(HttpRequest request) {
    Notification notification = new Notification();
    notification.setSubject(request.getParameter("txtTitle"));
    notification.setBody(request.getParameter("txtMessage"));
    notification.setChannel(request.getParameter("notificationId"));
    notification.setPriority(request.getParameter("priorityId"));
    notification.setUsers(StringUtil.split(request.getParameter("selectedUsers")));
    notification.setGroups(StringUtil.split(request.getParameter("selectedGroups")));
    return notification;
  }
}
