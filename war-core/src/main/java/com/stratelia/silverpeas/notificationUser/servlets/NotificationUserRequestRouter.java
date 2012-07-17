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

package com.stratelia.silverpeas.notificationUser.servlets;

import com.silverpeas.util.ArrayUtil;
import com.stratelia.silverpeas.notificationManager.GroupRecipient;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.silverpeas.notificationUser.control.NotificationUserSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Class declaration
 * <p/>
 * @author
 */
public class NotificationUserRequestRouter extends
    ComponentRequestRouter<NotificationUserSessionController> {

  private static final long serialVersionUID = -5858231857279380747L;

  /**
   * Method declaration
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
   * @param function The entering request function (ex : "Main.jsp")
   * @param nuSC The component Session Control, build and initialised.
   * @param request The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/notificationUser/jsp/notificationUser.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, NotificationUserSessionController nuSC,
      HttpServletRequest request) {
    // remarques
    // tous les paramètres des la jsp sont transferé par la request.
    // le UserPanel étant unique par session, il est impératif de récupérér
    // les objets selectionnés via userPanel et de transporter
    // les id des ses de jsp en jsp en soumettant un formulaire.
    // En effet, la notification peut être utilisée "en même temps" que le
    // client utilises userPanelPeas. Cela mélange les objets selectionnée.
    String destination = "";
    SilverTrace.info("notificationUser", "NotificationUserRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "function=" + function);

    try {
      request.setCharacterEncoding("UTF-8");
      if (function.startsWith("Main")) {
        String[] selectedIdUsers = new String[0];
        String[] selectedIdGroups = new String[0];
        String theTargetsUsers = request.getParameter("theTargetsUsers");
        String theTargetsGroups = request.getParameter("theTargetsGroups");

        nuSC.resetNotification();
        if (theTargetsUsers != null || theTargetsGroups != null) {// appel pour
          // notifier les targets
          String popupMode = request.getParameter("popupMode");
          String editTargets = request.getParameter("editTargets");
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
        String popupMode = request.getParameter("popupMode");
        String txtTitle = request.getParameter("txtTitle");
        String txtMessage = request.getParameter("txtMessage");
        String notificationId = request.getParameter("notificationId");
        String priorityId = request.getParameter("priorityId");
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
        String popupMode = request.getParameter("popupMode");

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
        String[] selectedIdUsers = request.getParameterValues("selectedUsers");
        List<UserRecipient> selectUserRecipients = null;
        if (selectedIdUsers != null) {
          selectUserRecipients = new ArrayList<UserRecipient>(selectedIdUsers.length);
          for (String selectedIdUser : selectedIdUsers) {
            selectUserRecipients.add(new UserRecipient(selectedIdUser));
          }
        }
        String[] selectedIdGroups = request.getParameterValues("selectedGroups");
        List<GroupRecipient> selectGroupRecipients = null;
        if (selectedIdGroups != null) {
          selectGroupRecipients = new ArrayList<GroupRecipient>(selectedIdGroups.length);
          for (String selectedIdGroup : selectedIdGroups) {
            selectGroupRecipients.add(new GroupRecipient(selectedIdGroup));
          }
        }

        String popupMode = request.getParameter("popupMode");
        String editTargets = request.getParameter("editTargets");
        String txtTitle = request.getParameter("txtTitle");
        String txtMessage = request.getParameter("txtMessage");
        String notificationId = request.getParameter("notificationId");
        String priorityId = request.getParameter("priorityId");

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
        if (selectUserRecipients != null || selectGroupRecipients != null) {
          nuSC.sendMessage("", notificationId, priorityId, txtTitle, txtMessage,
              selectUserRecipients, selectGroupRecipients);
        }
        request.setAttribute("SelectedIdUsers", ArrayUtil.EMPTY_STRING_ARRAY);
        request.setAttribute("SelectedIdGroups", ArrayUtil.EMPTY_STRING_ARRAY);
        destination = "/notificationUser/jsp/notificationSender.jsp?Action=sendNotif&"
            + "notificationId=&priorityId=&txtTitle=&txtMessage=&popupMode="
            + popupMode + "&editTargets=" + editTargets + "&compoId=";
      } else if (function.startsWith("emptyAll")) {
        request.setAttribute("SelectedIdUsers", ArrayUtil.EMPTY_STRING_ARRAY);
        request.setAttribute("SelectedIdGroups", ArrayUtil.EMPTY_STRING_ARRAY);

        String editTargets = request.getParameter("editTargets");
        String popupMode = request.getParameter("popupMode");
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
    SilverTrace.info("notificationUser", "NotificationUserRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "destination=" + destination);
    return destination;
  }
}
