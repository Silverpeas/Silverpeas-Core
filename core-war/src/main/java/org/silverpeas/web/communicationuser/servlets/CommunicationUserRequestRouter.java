/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.web.communicationuser.servlets;

import org.silverpeas.web.communicationuser.control.CommunicationUserSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.web.http.HttpRequest;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 * Class declaration
 * @author
 */
public class CommunicationUserRequestRouter extends
    ComponentRequestRouter<CommunicationUserSessionController> {

  private static final long serialVersionUID = 3353765477159128428L;

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  @Override
  public CommunicationUserSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new CommunicationUserSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for communicationUser, returns
   * "communicationUser"
   */
  @Override
  public String getSessionControlBeanName() {
    return "communicationUser";
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param commUserSC The component Session Control, build and initialised.
   * @param request The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/communicationUser/jsp/communicationUser.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, CommunicationUserSessionController commUserSC,
      HttpRequest request) {
    String destination = "";

    try {
      if (function.startsWith("Main")) {
        request.setAttribute("ConnectedUsersList", commUserSC.getDistinctConnectedUsersList());
        destination = "/communicationUser/jsp/connectedUsers.jsp";
      } else if (function.startsWith("OpenDiscussion")) {
        String userId = request.getParameter("userId");
        if (userId != null) {
          File fileDiscussion = commUserSC.getExistingFileDiscussion(userId);
          if (fileDiscussion == null) {
            fileDiscussion = commUserSC.createDiscussion(userId);
          }

          commUserSC.addCurrentDiscussion(fileDiscussion);
          request.setAttribute("UserName", commUserSC.getUserDetail().getDisplayedName());
          request.setAttribute("UserIdDest", userId);
          request.setAttribute("UserNameDest", commUserSC.getOrganisationController()
              .getUserDetail(
              userId).getDisplayedName());
          destination = "/communicationUser/jsp/discussion.jsp";
        }
      } else if (function.startsWith("ExportDiscussion")) {
        String userId = request.getParameter("userId");
        if (userId != null) {
          Collection<File> listCurrentDiscussion = commUserSC.getListCurrentDiscussion();
          Iterator<File> it = listCurrentDiscussion.iterator();
          String currentUserId = commUserSC.getUserId();
          File fileDiscussion = null;
          String fileName, userId1, userId2;
          boolean trouve = false;
          while (it.hasNext() && !trouve) {
            fileDiscussion = it.next();
            fileName = fileDiscussion.getName(); // userId1.userId2.txt
            userId1 = fileName.substring(0, fileName.indexOf('.'));
            userId2 = fileName.substring(fileName.indexOf('.') + 1, fileName.lastIndexOf('.'));
            trouve = ((userId.equals(userId1) && currentUserId.equals(userId2))
                || (userId.equals(userId2) && currentUserId.equals(userId1)));
          }
          if (!trouve) {
            throw new IOException("Fichier de discussion non trouvé !!");
          }
          request.setAttribute("FileDiscussion", fileDiscussion);
          destination = "/communicationUser/jsp/historyMessages.jsp";
        }
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    return destination;
  }
}