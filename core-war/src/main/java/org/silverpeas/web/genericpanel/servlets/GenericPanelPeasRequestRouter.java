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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

package org.silverpeas.web.genericpanel.servlets;

import org.silverpeas.web.genericpanel.control.GenericPanelPeasSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.exception.SilverpeasTrappedException;
import org.silverpeas.core.web.http.HttpRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class declaration
 * @author
 */
public class GenericPanelPeasRequestRouter extends
    ComponentRequestRouter<GenericPanelPeasSessionController> {

  private static final long serialVersionUID = 157358334718653187L;

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  public GenericPanelPeasSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new GenericPanelPeasSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "genericPanelPeas";
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param genericPanelPeasSC The component Session Control, build and initialised.
   * @param request
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function,
      GenericPanelPeasSessionController genericPanelPeasSC, HttpRequest request) {
    String destination = "";


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
        request.setAttribute("isZoomToItemValid", genericPanelPeasSC.isZoomToItemValid());
        request.setAttribute("isFilterValid", genericPanelPeasSC.isFilterValid());
        request.setAttribute("isMultiSelect", genericPanelPeasSC.isMultiSelect());
        request.setAttribute("isSelectable", genericPanelPeasSC.isSelectable());
        request.setAttribute("pageName", genericPanelPeasSC.getPageName());
        request.setAttribute("pageSubTitle", genericPanelPeasSC.getPageSubTitle());
        request.setAttribute("searchTokens", genericPanelPeasSC.getSearchTokens());
        request.setAttribute("searchNumber", genericPanelPeasSC.getSearchUsersNumber());
        request.setAttribute("selectedNumber", genericPanelPeasSC.getSelectedNumber());
        request.setAttribute("pageNavigation", genericPanelPeasSC.getPageNavigation());
        request.setAttribute("elementsToDisplay", genericPanelPeasSC.getPage());
        request.setAttribute("operationsToDisplay", genericPanelPeasSC.getPanelOperations());
        request.setAttribute("columnsHeader", genericPanelPeasSC.getColumnsHeader());
        request.setAttribute("miniFilterSelect", genericPanelPeasSC.getMiniFilterString());
        request.setAttribute("HostSpaceName", genericPanelPeasSC.getHostSpaceName());
        request.setAttribute("HostComponentName", genericPanelPeasSC.getHostComponentName());
        request.setAttribute("HostPath", genericPanelPeasSC.getHostPath());
      }
      request.setAttribute("ToPopup", genericPanelPeasSC.isPopupMode());

      destination = "/genericPanelPeas/jsp/" + destination;
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      if (e instanceof SilverpeasTrappedException) {
        destination = "/admin/jsp/errorpageTrapped.jsp";
      } else {
        destination = "/admin/jsp/errorpageMain.jsp";
      }
    }


    return destination;
  }

  protected String[] getFilters(HttpServletRequest request) {
    List<String> filters = new ArrayList<String>();
    int i = 0;

    String theValue = request.getParameter("filter" + Integer.toString(i));
    while (theValue != null) {
      filters.add(theValue);
      i++;
      theValue = request.getParameter("filter" + Integer.toString(i));
    }
    return filters.toArray(new String[filters.size()]);
  }

  protected Set<String> getSelected(HttpServletRequest request, int nbMaxDisplayed) {
    HashSet<String> selected = new HashSet<String>();
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
