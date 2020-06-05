/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

package org.silverpeas.web.todo.servlets;

import org.silverpeas.core.personalorganizer.model.Attendee;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.web.todo.control.ToDoSessionController;

import java.util.Collection;

/**
 * Class declaration
 * @author
 */
public class TodoRequestRouter extends ComponentRequestRouter<ToDoSessionController> {

  private static final long serialVersionUID = 6455939825707914384L;

  /**
   * This method creates a TodoSessionController instance
   * @param mainSessionCtrl The MainSessionController instance
   * @param context Context of current component instance
   * @return a TodoSessionController instance
   */
  public ToDoSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext context) {
    return new ToDoSessionController(mainSessionCtrl, context);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "todo";
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param scc The component Session Controller, build and initialised.
   * @param request The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function, ToDoSessionController scc,
      HttpRequest request) {

    String destination = "";

    try {

      final String defaultDestination = "/todo/jsp/todo.jsp";
      if (function.startsWith("Main")) {
        scc.getSelectedTodoIds().clear();
        destination = defaultDestination;
      } else if (function.startsWith("searchResult")) {
        destination = "/todo/jsp/todoEdit.jsp?Action=Update&ToDoId="
            + request.getParameter("Id");
      } else if (function.startsWith("diffusion")) {
        // initialisation du userPanel avec les participants
        destination = scc.initSelectionPeas();
      } else if (function.startsWith("saveMembers")) {
        // retour du userPanel
        Collection<Attendee> attendees = scc.getUserSelected();
        scc.setCurrentAttendees(attendees);
        destination = "/todo/jsp/todoEdit.jsp?Action=DiffusionListOK";
      } else if ("DeleteTodo".equals(function)) {
        request.mergeSelectedItemsInto(scc.getSelectedTodoIds());
        if (!scc.getSelectedTodoIds().isEmpty()) {
          scc.removeTabToDo(scc.getSelectedTodoIds().toArray(new String[scc.getSelectedTodoIds().size()]));
        }
        scc.getSelectedTodoIds().clear();
        destination = defaultDestination;
      } else if ("KeepingSelected".equals(function)) {
        request.mergeSelectedItemsInto(scc.getSelectedTodoIds());
        destination = defaultDestination;
      } else if ("todoPagination".equals(function)) {
        request.mergeSelectedItemsInto(scc.getSelectedTodoIds());
        destination = defaultDestination;
      } else {
        scc.getSelectedTodoIds().clear();
        destination = "/todo/jsp/" + function;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      return "/admin/jsp/errorpageMain.jsp";
    }

    return destination;
  }
}
