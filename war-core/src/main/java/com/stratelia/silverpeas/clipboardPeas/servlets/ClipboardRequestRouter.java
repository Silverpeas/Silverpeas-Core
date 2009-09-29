/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.clipboardPeas.servlets;

import javax.servlet.http.*;

import com.stratelia.webactiv.clipboard.control.ejb.*;
import com.stratelia.silverpeas.clipboardPeas.control.ClipboardSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.peasCore.*;
import com.stratelia.silverpeas.peasCore.servlets.*;

/*
 * CVS Informations
 *
 * $Id: ClipboardRequestRouter.java,v 1.3 2009/03/16 10:28:14 neysseri Exp $
 *
 * $Log: ClipboardRequestRouter.java,v $
 * Revision 1.3.2.1  2009/04/30 10:35:21  dlesimple
 * no message
 *
 * Revision 1.3  2009/03/16 10:28:14  neysseri
 * SilverpeasV5 compliance
 *
 * Revision 1.2  2006/01/12 12:27:22  dlesimple
 * Notification par popup Jsp
 *
 * Revision 1.1.1.1  2002/08/06 14:47:55  nchaix
 * no message
 *
 * Revision 1.3  2002/04/03 14:39:12  mguillem
 * Gestion des sessions (isAlived)
 *
 * Revision 1.2  2002/02/15 10:25:44  mguillem
 * Journal des connexions
 *
 * Revision 1.1  2002/01/30 11:00:34  tleroi
 * Move Bus peas to BusIHM
 *
 * Revision 1.1  2002/01/28 14:42:06  tleroi
 * Split clipboard and personalization
 *
 * Revision 1.4  2002/01/04 14:03:48  mmarengo
 * Stabilisation Lot 2
 * SilverTrace
 * Exception
 *
 */

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class ClipboardRequestRouter extends ComponentRequestRouter {
  /**
   * Method declaration
   * 
   * 
   * @param mainSessionCtrl
   * @param componentContext
   * 
   * @return
   * 
   * @see
   */
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new ClipboardSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented in the component request rooter class.
   * returns the session control bean name to be put in the request object ex :
   * for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "clipboardScc";
  }

  /**
   * This method has to be implemented by the component request rooter it has to
   * compute a destination page
   * 
   * @param function
   *          The entering request function (ex : "Main.jsp")
   * @param componentSC
   *          The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   *         "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {
    SilverTrace.info("clipboardPeas",
        "ClipboardRequestRooter.getDestination()", "root.MSG_GEN_ENTER_METHOD",
        " componentName = " + componentSC.getClass().getName()
            + "; function = " + function);
    ClipboardSessionController clipboardSC = (ClipboardSessionController) componentSC;
    String destination = "";

    if (function.startsWith("copyForm")) {
      destination = "/clipboard/jsp/copyForm.jsp";
    } else if (function.startsWith("paste")) {
      clipboardSC.setComponentRooterName(request.getParameter("compR"));
      clipboardSC.setSpaceId(request.getParameter("SpaceFrom"));
      clipboardSC.setComponentId(request.getParameter("ComponentFrom"));
      clipboardSC.setJSPPage(request.getParameter("JSPPage"));
      clipboardSC.setTargetFrame(request.getParameter("TargetFrame"));

      String componentName = clipboardSC.getComponentRooterName();

      if (componentName != null) {
        SilverTrace.info("clipboardPeas",
            "ClipboardRequestRooter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "compR = " + componentName);
        if (clipboardSC.getComponentId() != null)
          destination = URLManager
              .getURL(null, request.getParameter("SpaceFrom"), request
                  .getParameter("ComponentFrom"))
              + "paste.jsp";
        else
          destination = URLManager.getURL(URLManager.CMP_JOBSTARTPAGEPEAS)
              + "paste.jsp";
      } else {
        SilverTrace.info("clipboardPeas",
            "ClipboardRequestRooter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "compR is null" + componentName);
        destination = "/clipboard/jsp/clipboard.jsp";
      }
    } else if (function.startsWith("clipboardRefresh")) {
      destination = "/clipboard/jsp/clipboard.jsp";
    } else if (function.startsWith("clipboard")) {
      clipboardSC.setComponentRooterName(request.getParameter("compR"));
      clipboardSC.setSpaceId(request.getParameter("SpaceFrom"));
      clipboardSC.setComponentId(request.getParameter("ComponentFrom"));
      clipboardSC.setJSPPage(request.getParameter("JSPPage"));
      clipboardSC.setTargetFrame(request.getParameter("TargetFrame"));
      destination = "/clipboard/jsp/clipboard.jsp";
    } else if (function.startsWith("delete")) {
      try {
        ClipboardBm userClipboard = clipboardSC.getClipboard();
        int max = userClipboard.size();
        int removed = 0;

        for (int i = 0; i < max; i++) {
          String removedValue = request.getParameter("clipboardId"
              + String.valueOf(i));

          if (removedValue != null) {
            SilverTrace.info("clipboardPeas",
                "ClipboardRequestRooter.getDestination()",
                "root.MSG_GEN_PARAM_VALUE", "clipboardId" + String.valueOf(i)
                    + " = " + removedValue);
            userClipboard.remove(i - removed);
            removed++;
          }
        }
      } catch (Exception e) {
        SilverTrace.error("clipboardPeas",
            "ClipboardRequestRooter.getDestination()",
            "clipboardPeas.EX_CANT_WRITE", "delete.jsp");
      }
      destination = "/clipboard/jsp/clipboard.jsp";
    } else if (function.startsWith("selectObject")) {
      try {
        ClipboardBm userClipboard = clipboardSC.getClipboard();
        String objectIndex = request.getParameter("Id");
        String objectStatus = request.getParameter("Status");

        if ((objectIndex != null) && (objectStatus != null)) {
          SilverTrace.info("clipboardPeas",
              "ClipboardRequestRooter.getDestination()",
              "root.MSG_GEN_PARAM_VALUE", "selectObject " + objectIndex
                  + " -> " + objectStatus);
          userClipboard.setSelected(Integer.valueOf(objectIndex).intValue(),
              Boolean.valueOf(objectStatus).booleanValue());
        }
      } catch (Exception e) {
        SilverTrace.error("clipboardPeas",
            "ClipboardRequestRooter.getDestination()",
            "clipboardPeas.EX_CANT_WRITE", "delete.jsp");
      }
      destination = "/clipboard/jsp/Idle.jsp";
    } else if (function.startsWith("selectionpaste")) {
      try {
        ClipboardBm userClipboard = clipboardSC.getClipboard();
        int max = userClipboard.size();

        for (int i = 0; i < max; i++) {
          String removedValue = request.getParameter("clipboardId"
              + String.valueOf(i));

          userClipboard.setSelected(i, removedValue != null);
        }
      } catch (Exception e) {
        SilverTrace.error("clipboardPeas",
            "ClipboardRequestRooter.getDestination()",
            "clipboardPeas.EX_CANT_WRITE", "selectionpaste.jsp");
      }
      String componentName = clipboardSC.getComponentRooterName();

      if (componentName != null) {
        SilverTrace.info("clipboardPeas",
            "ClipboardRequestRooter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "compR = " + componentName);
        destination = URLManager.getURL(null,
            request.getParameter("SpaceFrom"), request
                .getParameter("ComponentFrom"))
            + "paste.jsp";
      } else {
        SilverTrace.info("clipboardPeas",
            "ClipboardRequestRooter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "compR is null" + componentName);
        destination = "/clipboard/jsp/clipboard.jsp";
      }
    } else {
      destination = "/clipboard/jsp/" + function;
    }

    SilverTrace.info("clipboardPeas",
        "ClipboardRequestRooter.getDestination()", "root.MSG_GEN_EXIT_METHOD",
        "Destination=" + destination);
    return destination;
  }

  public void updateSessionManagement(HttpSession session, String destination) {
    SilverTrace.info("clipboardPeas",
        "ClipboardRequestRouter.updateSessionManagement",
        "root.MSG_GEN_PARAM_VALUE", "dest=" + destination);

    if (destination.startsWith("/clipboard/jsp/Idle")) {
      // Set the isalived time
      SessionManager.getInstance().setIsAlived(session);
    } else {
      // Set the last accessed time
      SessionManager.getInstance().setLastAccess(session);
    }
  }

}
