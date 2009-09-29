/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.todo.servlets;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.todo.control.ToDoSessionController;

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class TodoRequestRouter extends ComponentRequestRouter {
  /**
   * This method creates a TodoSessionController instance
   * 
   * @param mainSessionCtrl
   *          The MainSessionController instance
   * @param context
   *          Context of current component instance
   * @return a TodoSessionController instance
   */
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext context) {
    ComponentSessionController component = (ComponentSessionController) new ToDoSessionController(
        mainSessionCtrl, context);
    return component;
  }

  /**
   * This method has to be implemented in the component request rooter class.
   * returns the session control bean name to be put in the request object ex :
   * for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "todo";
  }

  /**
   * This method has to be implemented by the component request rooter it has to
   * compute a destination page
   * 
   * @param function
   *          The entering request function (ex : "Main.jsp")
   * @param componentSC
   *          The component Session Controller, build and initialised.
   * @param request
   *          The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex :
   *         "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {

    SilverTrace.info("todo", "TodoRequestRooter.getDestination()",
        "root.MSG_GEN_ENTER_METHOD");
    ToDoSessionController scc = (ToDoSessionController) componentSC;
    String destination = "";

    try {

      if (function.startsWith("Main")) {
        destination = "/todo/jsp/todo.jsp";
      } else if (function.startsWith("searchResult")) {
        destination = "/todo/jsp/todoEdit.jsp?Action=Update&ToDoId="
            + request.getParameter("Id");
      } else if (function.startsWith("diffusion")) {
        // initialisation du userPanel avec les participants
        destination = scc.initSelectionPeas();
      } else if (function.startsWith("saveMembers")) {
        // retour du userPanel
        Collection attendees = scc.getUserSelected();
        scc.setCurrentAttendees(attendees);
        destination = "/todo/jsp/todoEdit.jsp?Action=DiffusionListOK";
      } else {
        destination = "/todo/jsp/" + function;
      }
    } catch (Exception exce_all) {
      request.setAttribute("javax.servlet.jsp.jspException", exce_all);
      return "/admin/jsp/errorpageMain.jsp";
    }
    SilverTrace.info("todo", "TodoRequestRooter.getDestination()",
        "root.MSG_GEN_EXIT_METHOD");
    return destination;
  }
}
