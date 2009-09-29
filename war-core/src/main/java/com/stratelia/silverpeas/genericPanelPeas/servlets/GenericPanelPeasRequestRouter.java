/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.genericPanelPeas.servlets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.genericPanelPeas.control.GenericPanelPeasSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasTrappedException;

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class GenericPanelPeasRequestRouter extends ComponentRequestRouter {

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
    return new GenericPanelPeasSessionController(mainSessionCtrl,
        componentContext);
  }

  /**
   * This method has to be implemented in the component request rooter class.
   * returns the session control bean name to be put in the request object ex :
   * for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "genericPanelPeas";
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
    String destination = "";
    GenericPanelPeasSessionController genericPanelPeasSC = (GenericPanelPeasSessionController) componentSC;
    SilverTrace.info("genericPanelPeas", "getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Function=" + function);

    try {
      if (function.startsWith("Main")) {
        genericPanelPeasSC.initSC(request.getParameter("PanelKey"));
        destination = "genericPanelPeas.jsp";
      } else if (function.startsWith("DoOperation")) {
        String op = request.getParameter("SpecificOperation");

        if (genericPanelPeasSC.isMultiSelect()) {
          genericPanelPeasSC.setSelectedElements(getSelected(request,
              genericPanelPeasSC.getNbMaxDisplayed()));
        }

        SilverTrace.info("genericPanelPeas", "getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "Operation=" + op);
        if ("GENERICPANELPREVIOUSUSER".equals(op)) {
          genericPanelPeasSC.previousUserPage();
          destination = "genericPanelPeas.jsp";
        } else if ("GENERICPANELNEXTUSER".equals(op)) {
          genericPanelPeasSC.nextUserPage();
          destination = "genericPanelPeas.jsp";
        } else if ("GENERICPANELAPPLYFILTER".equals(op)) {
          genericPanelPeasSC.setFilters(getFilters(request));
          destination = "genericPanelPeas.jsp";
        } else if ("GENERICPANELCANCEL".equals(op)) {
          request.setAttribute("HostUrl", genericPanelPeasSC.getCancelURL());
          destination = "goBack.jsp";
        } else if ("GENERICPANELZOOMTOITEM".equals(op)) {
          request.setAttribute("HostUrl", genericPanelPeasSC.getZoomToItemURL()
              + "?elementId=" + request.getParameter("userId"));
          destination = "goBack.jsp";
        } else if ((op != null) && (op.startsWith("GENERICPANELMINIFILTER"))) {
          genericPanelPeasSC.setMiniFilter(request.getParameter("miniFilter"
              + op.substring("GENERICPANELMINIFILTER".length())), op
              .substring("GENERICPANELMINIFILTER".length()));
          destination = "genericPanelPeas.jsp";
        } else // Go...
        {
          if (genericPanelPeasSC.isMultiSelect()) {
            genericPanelPeasSC.setSelectedUsers(op);
          } else {
            String userId = request.getParameter("userId");
            genericPanelPeasSC.setSelectedUser(userId, op);
          }
          request.setAttribute("HostUrl", genericPanelPeasSC.getGoBackURL());
          destination = "goBack.jsp";
        }
      } else {
        destination = function;
      }

      // Prepare the parameters
      if (destination.equals("genericPanelPeas.jsp")) {
        request.setAttribute("isZoomToItemValid", new Boolean(
            genericPanelPeasSC.isZoomToItemValid()));
        request.setAttribute("isFilterValid", new Boolean(genericPanelPeasSC
            .isFilterValid()));
        request.setAttribute("isMultiSelect", new Boolean(genericPanelPeasSC
            .isMultiSelect()));
        request.setAttribute("isSelectable", new Boolean(genericPanelPeasSC
            .isSelectable()));
        request.setAttribute("pageName", genericPanelPeasSC.getPageName());
        request.setAttribute("pageSubTitle", genericPanelPeasSC
            .getPageSubTitle());
        request.setAttribute("searchTokens", genericPanelPeasSC
            .getSearchTokens());
        request.setAttribute("searchNumber", genericPanelPeasSC
            .getSearchUsersNumber());
        request.setAttribute("selectedNumber", genericPanelPeasSC
            .getSelectedNumber());
        request.setAttribute("pageNavigation", genericPanelPeasSC
            .getPageNavigation());
        request.setAttribute("elementsToDisplay", genericPanelPeasSC.getPage());
        request.setAttribute("operationsToDisplay", genericPanelPeasSC
            .getPanelOperations());
        request.setAttribute("columnsHeader", genericPanelPeasSC
            .getColumnsHeader());
        request.setAttribute("miniFilterSelect", genericPanelPeasSC
            .getMiniFilterString());
        request.setAttribute("HostSpaceName", genericPanelPeasSC
            .getHostSpaceName());
        request.setAttribute("HostComponentName", genericPanelPeasSC
            .getHostComponentName());
        request.setAttribute("HostPath", genericPanelPeasSC.getHostPath());
      }
      request.setAttribute("ToPopup", new Boolean(genericPanelPeasSC
          .isPopupMode()));

      destination = "/genericPanelPeas/jsp/" + destination;
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      if (e instanceof SilverpeasTrappedException) {
        destination = "/admin/jsp/errorpageTrapped.jsp";
      } else {
        destination = "/admin/jsp/errorpageMain.jsp";
      }
    }

    SilverTrace.info("genericPanelPeas", "getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
    return destination;
  }

  protected String[] getFilters(HttpServletRequest request) {
    ArrayList filters = new ArrayList();
    int i = 0;
    String theValue = null;

    theValue = request.getParameter("filter" + Integer.toString(i));
    while (theValue != null) {
      filters.add(theValue);
      i++;
      theValue = request.getParameter("filter" + Integer.toString(i));
    }
    return (String[]) filters.toArray(new String[0]);
  }

  protected Set getSelected(HttpServletRequest request, int nbMaxDisplayed) {
    HashSet selected = new HashSet();
    int i = 0;
    String theValue = null;

    for (i = 0; i < nbMaxDisplayed; i++) {
      theValue = request.getParameter("element" + Integer.toString(i));
      if ((theValue != null) && (theValue.length() > 0)) {
        selected.add(theValue);
      }
    }
    return selected;
  }
}
