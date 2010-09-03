/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.notificationUser.servlets;


import com.stratelia.silverpeas.notificationUser.control.NotificationUserSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;

/**
 * Class declaration
 * @author
 */
public class NotificationUserRequestRouter extends ComponentRequestRouter {

  /**
   * 
   */
  private static final long serialVersionUID = -5858231857279380747L;

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  @Override
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new NotificationUserSessionController(mainSessionCtrl,
        componentContext);
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
   * @param function The entering request function (ex : "Main.jsp")
   * @param componentSC The component Session Control, build and initialised.
   * @param request The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/notificationUser/jsp/notificationUser.jsp?flag=user")
   */
  @Override
  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {
    // remarques
    // tous les paramètres des la jsp sont transferé par la request.
    // le UserPanel étant unique par session, il est impératif de récupérér
    // les
    // objets selectionnés via userPanel et de transporter
    // les id des ses de jsp en jsp en soumettant un formulaire.
    // En effet, la notification peut être utilisé "en même temps" qu' le
    // client
    // utiliser userPanelPeas. Cela mélange les objets selectionnée.
    String destination = "";
    NotificationUserSessionController nuSC = (NotificationUserSessionController) componentSC;
    SilverTrace.info("notificationUser", "NotificationUserRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "function=" + function);

    String popupMode = null;
    String editTargets = null;
    String txtTitle = null;
    String txtMessage = null;
    String notificationId = null;
    String priorityId = null;

    try {
      request.setCharacterEncoding("UTF-8");
      if (function.startsWith("Main")) {
        String[] selectedIdUsers = new String[0];
        String[] selectedIdGroups = new String[0];
        String theTargetsUsers = request.getParameter("theTargetsUsers");
        String theTargetsGroups = request.getParameter("theTargetsGroups");

        nuSC.resetNotification();
        if (theTargetsUsers != null || theTargetsGroups != null) {// appel pour
          // notifier
          // les targets
          popupMode = request.getParameter("popupMode");
          editTargets = request.getParameter("editTargets");
          selectedIdUsers = nuSC.initTargetsUsers(theTargetsUsers);
          selectedIdGroups = nuSC.initTargetsGroups(theTargetsGroups);
          destination = "/notificationUser/jsp/notificationSender.jsp?popupMode="
              + popupMode + "&editTargets=" + editTargets;
        } else {
          // appel standard
          destination = "/notificationUser/jsp/notificationSender.jsp";
        }
        request.setAttribute("SelectedIdUsers", selectedIdUsers);
        request.setAttribute("SelectedIdGroups", selectedIdGroups);
      } else if ("SetTarget".equals(function)) {
        // récupération des is des objet selectionés
        String[] idUsers = request.getParameterValues("selectedUsers");
        String[] idGroups = request.getParameterValues("selectedGroups");
        // paramètres jsp
        popupMode = request.getParameter("popupMode");
        editTargets = request.getParameter("editTargets");
        txtTitle = request.getParameter("txtTitle");
        txtMessage = request.getParameter("txtMessage");
        notificationId = request.getParameter( "notificationId");
        priorityId = request.getParameter("priorityId");
        nuSC.setTxtTitle(txtTitle);
        nuSC.setTxtMessage(txtMessage);
        nuSC.setNotificationId(notificationId);
        nuSC.setPriorityId(priorityId);
        StringBuffer paramValue = new StringBuffer("?popupMode=").append(popupMode);
        // initialisation des paramètres d'initialisations de UserPanel/UserPanelPeas
        destination = nuSC.initSelectionPeas(idUsers, idGroups, paramValue.toString());
      } else if (function.startsWith("GetTarget")) {
        // récupération des objets sélélectionnés
        String[] selectedIdUsers = nuSC.getTargetIdUsers();
        String[] selectedIdGroups = nuSC.getTargetIdGroups();

        // paramètres jsp
        popupMode = request.getParameter("popupMode");
        editTargets = request.getParameter("editTargets");

        request.setAttribute("txtTitle", nuSC.getTxtTitle());
        request.setAttribute("txtMessage", nuSC.getTxtMessage());
        request.setAttribute("notificationId", nuSC.getNotificationId());
        request.setAttribute("priorityId", nuSC.getPriorityId());

        StringBuffer paramValue = new StringBuffer("?popupMode=").append(popupMode);
        request.setAttribute("SelectedIdUsers", selectedIdUsers);
        request.setAttribute("SelectedIdGroups", selectedIdGroups);
        destination = "/notificationUser/jsp/notificationSender.jsp" + paramValue.toString();
      } else if (function.startsWith("sendNotif")) {
        SilverTrace.debug("notificationUser", "NotificationUserRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "Enter sendNotif");
        // récupération des donnés sélélectionnées
        String[] selectedIdUsers = request.getParameterValues("selectedUsers");
        String[] selectedIdGroups = request.getParameterValues("selectedGroups");

        for (Enumeration e = request.getParameterNames(); e.hasMoreElements();) {
          String nom = (String) e.nextElement();
          SilverTrace.debug("notificationUser",
              "NotificationUserRequestRouter.getDestination()",
              "root.MSG_GEN_PARAM_VALUE", "nom=" + nom);
          SilverTrace.debug("notificationUser",
              "NotificationUserRequestRouter.getDestination()",
              "root.MSG_GEN_PARAM_VALUE", "value=" + request.getParameter(nom));
        }
        popupMode = request.getParameter("popupMode");
        editTargets = request.getParameter("editTargets");
        txtTitle = request.getParameter("txtTitle");
        txtMessage = request.getParameter("txtMessage");
        notificationId = request.getParameter("notificationId");
        priorityId = request.getParameter("priorityId");

        SilverTrace.debug("notificationUser", "NotificationUserRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "notificationId=" + notificationId);
        SilverTrace.debug("notificationUser", "NotificationUserRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "priorityId=" + priorityId);
        SilverTrace.debug("notificationUser", "NotificationUserRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "txtMessage=" + txtMessage);
        SilverTrace.debug("notificationUser", "NotificationUserRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "txtTitle=" + txtTitle);
        SilverTrace.debug("notificationUser", "NotificationUserRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "popupMode=" + popupMode);
        SilverTrace.debug("notificationUser", "NotificationUserRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "editTargets=" + editTargets);
        nuSC.sendMessage("", notificationId, priorityId, txtTitle, txtMessage,
            selectedIdUsers, selectedIdGroups);
        request.setAttribute("SelectedIdUsers", new String[0]);
        request.setAttribute("SelectedIdGroups", new String[0]);
        destination = "/notificationUser/jsp/notificationSender.jsp?Action=sendNotif&"
            + "notificationId=&priorityId=&txtTitle=&txtMessage=&popupMode="
            + popupMode + "&editTargets=" + editTargets + "&compoId=";
      } else if (function.startsWith("emptyAll")) {
        request.setAttribute("SelectedIdUsers", new String[0]);
        request.setAttribute("SelectedIdGroups", new String[0]);

        editTargets = request.getParameter("editTargets");
        popupMode = request.getParameter("popupMode");
        destination = "/notificationUser/jsp/notificationSender.jsp?Action=emptyAll&notificationId"
            + "=&priorityId=&txtTitle=&txtMessage=&popupMode="
            + popupMode + "&editTargets=" + editTargets + "&compoId=";
      } else {
        destination = "/notificationUser/jsp/" + function;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }
    SilverTrace.info("notificationUser",
        "NotificationUserRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "destination=" + destination);
    return destination;
  }
}
